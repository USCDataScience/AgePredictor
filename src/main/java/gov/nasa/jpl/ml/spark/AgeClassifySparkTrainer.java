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

package gov.nasa.jpl.ml.spark.authorage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;

import opennlp.tools.ml.EventTrainer;
import opennlp.tools.ml.model.Event;
import opennlp.tools.ml.model.MaxentModel;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ObjectStreamUtil;

import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.cmdline.TerminateToolException;

import opennlp.tools.authorage.AgeClassifyFactory;
import opennlp.tools.authorage.AgeClassifyModel;
import opennlp.tools.authorage.AuthorAgeSample;
import opennlp.tools.authorage.AuthorAgeSampleStream;
import opennlp.tools.ml.AgeClassifyTrainerFactory;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;

/**
 * TODO: Documentation
 */
public class AgeClassifySparkTrainer {
    
    public static AgeClassifyModel createModel(String languageCode, String dataIn,
					       AgeClassifyContextGeneratorWrapper wrapper,
					       TrainingParameters trainParams) throws IOException {
	
	SparkConf conf = new SparkConf().setAppName("AgeClassifySparkTrainer");
	JavaSparkContext sc = new JavaSparkContext(conf);
	
	JavaRDD<String> data = sc.textFile(dataIn);
	JavaRDD<AuthorAgeSample> samples = data.map(new CreateAuthorAgeSamples(wrapper));
	//filter out error samples
	JavaRDD<AuthorAgeSample> validSamples = samples.filter(new Function<AuthorAgeSample, Boolean>() {
		public Boolean call(AuthorAgeSample s) { return s != null; }
	    });
	
	ObjectStream<AuthorAgeSample> sampleStream = ObjectStreamUtil.createObjectStream(validSamples.collect());
	ObjectStream<Event> eventStream = new AgeClassifyEventStream(sampleStream);
	
	Map<String, String> entries = new HashMap<String, String>();
						    
						    
	EventTrainer trainer = AgeClassifyTrainerFactory
            .getEventTrainer(trainParams.getSettings(), entries);
        MaxentModel ageModel = trainer.train(eventStream);
	
	sc.stop();
	
	Map<String, String> manifestInfoEntries = new HashMap<String, String>();
	return new AgeClassifyModel(languageCode, ageModel, manifestInfoEntries,
	    new AgeClassifyFactory(wrapper.getTokenizer(), wrapper.getFeatureGenerators()));
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
	
	AgeClassifyContextGeneratorWrapper wrapper = new AgeClassifyContextGeneratorWrapper("opennlp.tools.tokenize.SentenceTokenizer",
											    "opennlp.tools.tokenize.BagOfWordsTokenizer");
	
	AgeClassifyModel model;
	try {
	    model = AgeClassifySparkTrainer.createModel("en", input, wrapper, params);
	} catch (IOException e) {
	    throw new TerminateToolException(-1,
	        "IO error while reading training data or indexing data: " + e.getMessage(), e);
	}
	CmdLineUtil.writeModel("age classifier", new File(output), model);
    }
}

