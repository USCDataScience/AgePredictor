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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import opennlp.tools.authorage.AgeClassifyFactory;
import opennlp.tools.authorage.AgeClassifyModel;
import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.cmdline.TerminateToolException;
import opennlp.tools.ml.EventTrainer;
import opennlp.tools.ml.authorage.AgeClassifyTrainerFactory;
import opennlp.tools.ml.model.Event;
import opennlp.tools.ml.model.MaxentModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

/**
 * TODO: Documentation
 */
public class AgeClassifySparkTrainer {
    
    public static AgeClassifyModel createModel(String languageCode, String dataIn,
					       String tokenizer, String featureGenerators,
					       TrainingParameters trainParams) throws IOException {
	
	SparkConf conf = new SparkConf().setAppName("AgeClassifySparkTrainer");
	JavaSparkContext sc = new JavaSparkContext(conf);
	
	AgeClassifyContextGeneratorWrapper wrapper = new AgeClassifyContextGeneratorWrapper(
	    tokenizer, featureGenerators);

	JavaRDD<String> data = sc.textFile(dataIn, 8).cache();
	JavaRDD<EventWrapper> samples = data.map(new CreateEvents(wrapper)).cache();
	
	/*
	JavaRDD<EventWrapper> samples = data.map( 
	     new Function<String, EventWrapper>() {
		 public EventWrapper call(String s) {
		     String[] parts = s.split(",");
		     
		     try {
			 if (parts[0] != "-1") {
			     Integer value = Integer.parseInt(parts[0]);
			     
			     String[] text = parts[2].split(" ");
			     return new EventWrapper(value, text);
			 } else {
			     String cat = parts[1];
			     
			     String[] text = parts[2].split(" ");
			     return new EventWrapper(cat, text);
			 }
		     } catch(Exception e) {
			 return null;
		     }
		 }
	     });
	*/

	JavaRDD<EventWrapper> validSamples = samples.filter(
            new Function<EventWrapper, Boolean>() {
		@Override
		public Boolean call(EventWrapper s) { return s != null; }
	    }).cache();
	
	//ObjectStream<Event> eventStream = EventStreamUtil.createEventStream(samples);	
	ObjectStream<Event> eventStream = EventStreamUtil.createEventStream(validSamples.collect());
	
	Map<String, String> entries = new HashMap<String, String>();
					       					    
	EventTrainer trainer = AgeClassifyTrainerFactory
            .getEventTrainer(trainParams.getSettings(), entries);
        MaxentModel ageModel = trainer.train(eventStream);
	
	samples.unpersist();
	data.unpersist();
	
	sc.stop();
	
	Map<String, String> manifestInfoEntries = new HashMap<String, String>();
	
	AgeClassifyFactory factory = AgeClassifyFactory.create("AgeClassifyFactory", 
            wrapper.getTokenizer(), wrapper.getFeatureGenerators());
	return new AgeClassifyModel(languageCode, ageModel, manifestInfoEntries, factory);
    }
	    
    public static void main(String[] args) {
	if (args.length < 2) {
	    System.out.println("usage: <input> <output>\n");
	    System.exit(0);
	}
	
	String input = args[0];
	String output = args[1];
	
	TrainingParameters params = new TrainingParameters();
	params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(0));
	params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(100));
	//params.put(TrainingParameters.ALGORITHM_PARAM, NaiveBayesTrainer.NAIVE_BAYES_VALUE);
	
	AgeClassifyModel model;
	try {
	    model = AgeClassifySparkTrainer.createModel("en", input, 
	        "opennlp.tools.tokenize.SentenceTokenizer", "opennlp.tools.tokenize.BagOfWordsTokenizer", params);
	} catch (IOException e) {
	    throw new TerminateToolException(-1,
	        "IO error while reading training data or indexing data: " + e.getMessage(), e);
	}
	CmdLineUtil.writeModel("age classifier", new File(output), model);
    }
}

