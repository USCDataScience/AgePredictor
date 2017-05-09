/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.tokenize;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;
/**
 * TODO: Documentation
 */
public class SentenceTokenizer implements Tokenizer {

    public static final SentenceTokenizer INSTANCE = new SentenceTokenizer();
    
    private final String TOKEN_MODEL_FILE = "model/opennlp/en-token.bin";
    private final String SENTENCE_MODEL_FILE = "model/opennlp/en-sent.bin";
    
    private SentenceTokenizer() {
    }
    
    public String[] tokenize(String s) {
	InputStream modelIn = null;
	
	List<String> tokens = new ArrayList<String>();
	try {
	    modelIn = new FileInputStream(SENTENCE_MODEL_FILE);
	    SentenceModel model = new SentenceModel(modelIn);
	    
	    SentenceDetector sentenceDetector = new SentenceDetectorME(model);
	    String sentences[] = sentenceDetector.sentDetect(s);
	    
	    for (String sentence : sentences) {
		List<String> toks = 
		    Arrays.asList(tokenizeHelper(sentence));
		tokens.addAll(toks);
		// Add a token for end of sentence
		tokens.add("<SENTENCE>");
	    }
	} 
	catch (IOException e) {
	    System.out.println("Error with loading Sentence model...");
	    e.printStackTrace();
	}
	finally {
	    if (modelIn != null) {
		try {
		    modelIn.close();
		}
		catch (IOException e) {
		}
	    }
	}
       
	return tokens.toArray(new String[tokens.size()]);
    }
    
    private String[] tokenizeHelper(String s) {
	InputStream modelIn = null;
	String[] tokens = new String[0];
	
	try {
	    modelIn = new FileInputStream(TOKEN_MODEL_FILE);
	    TokenizerModel model= new TokenizerModel(modelIn);
	    
	    Tokenizer tokenizer = new TokenizerME(model);
	    tokens = tokenizer.tokenize(s);
	} 
	catch (IOException e) {
	    System.out.println("Error with loading Tokenizer Model...");
	    e.printStackTrace();
	}
	finally {
	    if (modelIn != null) {
		try {
		    modelIn.close();
		}
		catch (IOException e) {
		}
	    }
	}
	
	return tokens;
    }
    
    public Span[] tokenizePos(String s) {
	// Not needed
	return new Span[0];
    }
    
}