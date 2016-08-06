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

package gov.nasa.jpl.ml.authorage;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.BaseToolFactory;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ext.ExtensionLoader;

import gov.nasa.jpl.ml.util.featuregen.FeatureGenerator;
import gov.nasa.jpl.ml.util.featuregen.TokenFeatureGenerator;

/**
 * TODO: Documentation
 */ 
public class AgeClassifyFactory extends BaseToolFactory {
    private static final String FEATURE_GENERATORS = "authorage.featureGenerators";
    private static final String TOKENIZER_NAME = "authorage.tokenizer";

    private FeatureGenerator[] featureGenerators; // Defaults to just getting unigrams
    private Tokenizer tokenizer;
    
    public AgeClassifyFactory() {
    }
    
    protected void init(Tokenizer tokenizer, FeatureGenerator[] featureGenerators) {

	this.featureGenerators = featureGenerators;
	this.tokenizer = tokenizer;
    }
    
    
    public AgeClassifyFactory(Tokenizer tokenizer, FeatureGenerator[] featureGenerators) {
	this.init(tokenizer, featureGenerators);
    }
    
    
    @Override
	public void validateArtifactMap() throws InvalidFormatException {
	// nothing to validate
    }
    
    public static AgeClassifyFactory create(String subclassName, Tokenizer tokenizer,
					    FeatureGenerator[] featureGenerators) throws InvalidFormatException {
	if (subclassName == null) {
	    // will create the default factory
	    return new AgeClassifyFactory(tokenizer, featureGenerators);
	}
	try {
	    AgeClassifyFactory factory = ExtensionLoader.instantiateExtension(
	        AgeClassifyFactory.class, subclassName);
	    factory.init(tokenizer, featureGenerators);
	    return factory;
	} catch (Exception e) {
	    String msg = "Could not instantiate the " + subclassName
		+ ". The initialization throw an exception.";
	    System.err.println(msg);
	    e.printStackTrace();
	    throw new InvalidFormatException(msg, e);
	}
	
    }
    
    private FeatureGenerator[] loadFeatureGenerators(String classNames) {
	String[] classes = classNames.split(",");
	FeatureGenerator[] features = new FeatureGenerator[classes.length];

	for (int i = 0; i < classes.length; i++) {
	    features[i] = ExtensionLoader.instantiateExtension(FeatureGenerator.class,
							       classes[i]);
	}
	return features;
    }
    
    public FeatureGenerator[] getFeatureGenerators() {
	if (featureGenerators == null) {
	    if (artifactProvider != null) {
		String classNames = artifactProvider
		    .getManifestProperty(FEATURE_GENERATORS);
		if (classNames != null) {
		    this.featureGenerators = loadFeatureGenerators(classNames);
		}
	    }
	    if (featureGenerators == null) { // could not load using artifact provider
		// load bag of words as default
		FeatureGenerator[] bow = { new TokenFeatureGenerator() };
		this.featureGenerators = bow;
	    }
	}
	return featureGenerators;
    }
    
    public void setFeatureGenerators(FeatureGenerator[] featureGenerators) {
	this.featureGenerators = featureGenerators;
    }
    
    public Tokenizer getTokenizer() {
	if (this.tokenizer == null) {
	    if (artifactProvider != null) {
		String className = artifactProvider.getManifestProperty(TOKENIZER_NAME);
		if (className != null) {
		    this.tokenizer = ExtensionLoader.instantiateExtension(
		        Tokenizer.class, className);
		}
	    }
	    if (this.tokenizer == null) { // could not load using artifact provider
		this.tokenizer = WhitespaceTokenizer.INSTANCE;
	    }
	}
	return tokenizer;
    }

    public void setTokenizer(Tokenizer tokenizer) {
	this.tokenizer = tokenizer;
    }
    
    public AgeClassifyContextGenerator createContextGenerator() {
	return new AgeClassifyContextGenerator(getFeatureGenerators());
    }

}