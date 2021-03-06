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

package edu.usc.irds.agepredictor.spark.authorage;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.spark.api.java.function.Function;

import opennlp.tools.util.featuregen.FeatureGenerator;

public class CreateEvents implements Function<String, EventWrapper> {
    private AgeClassifyContextGeneratorWrapper wrapper;
    
    public CreateEvents(String tok, String fg) {
	this.wrapper = new AgeClassifyContextGeneratorWrapper(tok, fg);
    }
    
    public CreateEvents(AgeClassifyContextGeneratorWrapper w) {
	this.wrapper = w;
    }
    
    @Override
    public EventWrapper call(String s) {
	String category;
	String text;

	try {
	    category = s.split("\t", 2)[0]; 
	    text = s.split("\t", 2)[1];
	} catch (Exception e) {
	    //not in correct format. ignore
	    return null;
	}

	//first tokenize the text
	String tokens[] = this.wrapper.getTokenizer().tokenize(text);
	
	//then extract features using all the feature generators
	Collection<String> context = new LinkedList<String>();
	
	FeatureGenerator[] featureGenerators = this.wrapper.getFeatureGenerators();
	for (FeatureGenerator featureGenerator : featureGenerators) {
	    Collection<String> extracted = 
		featureGenerator.extractFeatures(tokens);
	    context.addAll(extracted);
	}
	
	String features[] = context.toArray(new String[context.size()]); 
	
	if (features.length > 0) {
	    //input can be both an age number or age category
	    try {
		int age = Integer.valueOf(category);
		
		EventWrapper event = new EventWrapper(age, features);
		System.out.println("Event: " + event);
		return event;
	    } catch (NumberFormatException e) {
		
		//try category as a string
		EventWrapper event =  new EventWrapper(category, features); 
		return event;
	    } catch (Exception e) {
		return null;
	    }
	} 
	else {
	    return null;
	}
    }
    
}
    