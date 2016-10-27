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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import opennlp.tools.ml.AgeClassifyTrainerFactory;
import opennlp.tools.ml.AgeClassifyTrainerFactory.TrainerType;
import opennlp.tools.ml.EventTrainer;
import opennlp.tools.ml.model.Event;
import opennlp.tools.ml.model.MaxentModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

/**
 * TODO: Documentation
 */
public class AgeClassifyME {
    protected AgeClassifyContextGenerator contextGenerator;
    
    private AgeClassifyFactory factory;
    private AgeClassifyModel model;
    
    public AgeClassifyME(AgeClassifyModel ageModel) {
	this.model = ageModel;
	this.factory = ageModel.getFactory();

	this.contextGenerator = new AgeClassifyContextGenerator(
	    this.factory.getFeatureGenerators());
    }
    
    
    public String getBestCategory(double[] outcome) {
	return this.model.getMaxentModel().getBestOutcome(outcome);
    }
    
    public int getNumCategories() {
	return this.model.getMaxentModel().getNumOutcomes();
    }
    
    public String getCategory(int index) {
	return this.model.getMaxentModel().getOutcome(index);
    }
    
    public int getIndex(String category) {
	return this.model.getMaxentModel().getIndex(category);
    }
    
    public double[] getProbabilities(String text[]) {
	return this.model.getMaxentModel().eval(
	       contextGenerator.getContext(text));
    }
    
    public double[] getProbabilities(String documentText) {
	Tokenizer tokenizer = this.factory.getTokenizer();
	return getProbabilities(tokenizer.tokenize(documentText));
    }
    
    public String predict(String documentText) {
	double probs[] = getProbabilities(documentText);
	String category = getBestCategory(probs);
	
	return category;
    }
    
    public Map<String, Double> scoreMap(String documentText) {
	Map<String, Double> probs = new HashMap<String, Double>();
	
	double[] categories = getProbabilities(documentText);
	
	int numCategories = getNumCategories();
	for (int i = 0; i < numCategories; i++) {
	    String category = getCategory(i);
	    probs.put(category, categories[getIndex(category)]);
	}
	return probs;
    }
    
    public SortedMap<Double, Set<String>> sortedScoreMap(String documentText) {
	SortedMap<Double, Set<String>> sortedMap = new TreeMap<Double, Set<String>>();
	
	double[] categories = getProbabilities(documentText);
	
	int numCategories = getNumCategories();
	for (int i = 0; i < numCategories; i++) {
	    String category = getCategory(i);
	    double score = categories[getIndex(category)];
	    
	    if (sortedMap.containsKey(score)) {
		sortedMap.get(score).add(category);
	    } else {
		Set<String> newset = new HashSet<String>();
		newset.add(category);
		sortedMap.put(score, newset);
	    }
	}
	return sortedMap;
    }
    
    
    public static AgeClassifyModel train(String languageCode,
        ObjectStream<AuthorAgeSample> samples, TrainingParameters trainParams,
       	AgeClassifyFactory factory) throws IOException {
	
	Map<String, String> entries = new HashMap<String, String>();

	MaxentModel ageModel = null;
	
	TrainerType trainerType = AgeClassifyTrainerFactory
	    .getTrainerType(trainParams.getSettings());
	
	ObjectStream<Event> eventStream = new AgeClassifyEventStream(samples,
	    factory.createContextGenerator());
	
	EventTrainer trainer = AgeClassifyTrainerFactory
	    .getEventTrainer(trainParams.getSettings(), entries);
	ageModel = trainer.train(eventStream);
	
	Map<String, String> manifestInfoEntries = new HashMap<String, String>();

	return new AgeClassifyModel(languageCode, ageModel, manifestInfoEntries,
				    factory);
    }
    

}