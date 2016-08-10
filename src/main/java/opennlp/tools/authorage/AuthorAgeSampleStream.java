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
import java.util.Arrays;

import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.FilterObjectStream;
import opennlp.tools.util.ObjectStream;

/**
 * TODO: Documentation
 */ 
public class AuthorAgeSampleStream extends FilterObjectStream<String, AuthorAgeSample> {
    public AuthorAgeSampleStream(ObjectStream<String> samples) {
	super(samples);
    }

    public AuthorAgeSample read() throws IOException {
	String sampleString = samples.read();
	
	if (sampleString != null) {
	    // Whitespace tokenize entire string
	    String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(sampleString);
	    
	    AuthorAgeSample sample;
	    
	    if (tokens.length > 1) {
		String docTokens[] = new String[tokens.length - 1];
		System.arraycopy(tokens, 1, docTokens, 0, tokens.length -1);
		
		// input can be both an age number or age category
		try {
		    int age = Integer.valueOf(tokens[0]);
		    sample = new AuthorAgeSample(age, docTokens);
		} catch (NumberFormatException e) {
		    // try category as a string
		    String category = tokens[0];
		    sample = new AuthorAgeSample(category, docTokens); 
		} catch (Exception e) {
		    e.printStackTrace();
		    return null;
		}
		
	    }
	    else {
		throw new IOException(
		    "Empty lines, or lines with only a category string are not allowed!");
	    }
	
	    return sample;
	}

	return null;
    }
    

}
