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

package gov.nasa.jpl.ml.authorage.example;

import java.util.ArrayList;
import java.util.List;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

import opennlp.tools.authorage.AgeClassifyFactory;
import opennlp.tools.authorage.AgeClassifyME;
import opennlp.tools.authorage.AgeClassifyModel;
import opennlp.tools.authorage.AuthorAgeSample;
import opennlp.tools.authorage.AuthorAgeSampleStream;

//import opennlp.tools.ml.naivebayes.NaiveBayesTrainer;

/**
 * TODO: Documentation
 */
public class AgeClassifyExample {
    
    AgeClassifyModel model;
    
    private final String INPUT_PATH = "/Users/joeyhong/eclipse-workspace/AgePrediction/data/blogs/blogs-test.txt";
    private final String MODEL_PATH = "/Users/joeyhong/eclipse-workspace/AgePrediction/model/blogs-test-model";

    public static void main( String[] args ) throws IOException
    {
        AgeClassifyExample myClassifier = new AgeClassifyExample();
	myClassifier.train();
	//myClassifier.saveModel();
	myClassifier.classify("Hello World!");
    }
    
    public void train() {
	InputStream dataIn = null;
	ObjectStream<AuthorAgeSample> sampleStream = null;

	try {
	    dataIn = new FileInputStream(INPUT_PATH);
	    ObjectStream<String> lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
	    sampleStream = new AuthorAgeSampleStream(lineStream);
	    
	    // Specifies the minimum number of times a feature must be seen
	    
	    TrainingParameters params = new TrainingParameters();
	    params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(0));
	    params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(100));
	    //params.put(TrainingParameters.ALGORITHM_PARAM, NaiveBayesTrainer.NAIVE_BAYES_VALUE);
	    
	    AgeClassifyFactory factory = new AgeClassifyFactory();
	    model = AgeClassifyME.train("en", sampleStream, params, factory);
	} catch (IOException e) {
	    // Failed to read or parse training data, training failed
	    e.printStackTrace();
	} finally {
	    if (dataIn != null) {
		try {
		    dataIn.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
    }
	
    public void saveModel() {
	OutputStream modelOutput = null;
	try {
	    modelOutput = new BufferedOutputStream(new FileOutputStream(MODEL_PATH));
	    model.serialize(modelOutput);
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    if (modelOutput != null) {
		try {
		    modelOutput.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
    }
    
    public void classify(String text) throws IOException {
	AgeClassifyME classifier = new AgeClassifyME(model);
	String predictedCategory = classifier.predict(text);
	
	System.out.println("Model prediction: " + predictedCategory);
    }
    
    
}
