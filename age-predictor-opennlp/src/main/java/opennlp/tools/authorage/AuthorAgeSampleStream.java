/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package opennlp.tools.authorage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.FilterObjectStream;
import opennlp.tools.util.ObjectStream;

/**
 * TODO: Documentation
 */ 
public class AuthorAgeSampleStream extends FilterObjectStream<String, AuthorAgeSample> {
    
    private Tokenizer tokenizer;
    
    public AuthorAgeSampleStream(ObjectStream<String> samples) {
	super(samples);
	this.tokenizer = WhitespaceTokenizer.INSTANCE;
    }
    
    public AuthorAgeSampleStream(ObjectStream<String> samples, Tokenizer tokenizer) {
	super(samples);
	this.tokenizer = tokenizer;
    }

    public AuthorAgeSample read() throws IOException {
	String sampleString = samples.read();
	
	while (sampleString != null) {
	    String category;
	    String text;
	    
	    try {
		category = sampleString.split("\t", 2)[0];
		text = sampleString.split("\t", 2)[1];
	    } catch(Exception e) {
		sampleString = samples.read();
                continue;
	    }

	    String tokens[] = this.tokenizer.tokenize(text);
	    
	    AuthorAgeSample sample;
		    
	    if (tokens.length > 0) {
		//input can be both an age number or age category
		try {
		    int age = Integer.valueOf(category);
		    sample = new AuthorAgeSample(age, tokens);
		} catch (NumberFormatException e) {
		    //try category as a string
		    
		    //possible categories
		    Set<String> categories = new HashSet<String>() {{
			    add("xx-18");
			    add("25-34");
			    add("35-49");
			    add("50-64");
			    add("65-xx");
			}};
		    // Make sure it is a valid category; else read another sample
		    if (!categories.contains(category)) {
			sampleString = samples.read();
			continue;
		    }
		    sample = new AuthorAgeSample(category, tokens); 
		} catch (Exception e) {
		    e.printStackTrace();
		    
		    //try reading another sample
		    sampleString = samples.read();
		    continue;
		}
	    }
	    else {
		//try reading another sample
		sampleString = samples.read();
		continue;
		
		//throw new IOException(
		//    "Empty lines, or lines with only a category string are not allowed!");
		
	    }
	    //System.out.println(Arrays.toString(sample.getText()));
	    return sample;
	}

	return null;
    }
    

}
