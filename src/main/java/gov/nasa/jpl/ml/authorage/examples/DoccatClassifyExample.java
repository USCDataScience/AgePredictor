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

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

//import opennlp.tools.ml.naivebayes.NaiveBayesTrainer;

/**
 * TODO: Documentation
 */
public class DoccatClassifyExample
{
    DoccatModel model;

    private final String INPUT_PATH = ""; /* Set input path */   
    private final String MODEL_PATH = ""; /* Set model output path */
    
    public static void main( String[] args ) throws IOException
    {
        DoccatClassifyExample myClassifier = new DoccatClassifyExample();
	myClassifier.train();
	myClassifier.classify("");
    }
    
    public void train() {
	InputStream dataIn = null;
	try {
	    dataIn = new FileInputStream(INPUT_PATH);
	    ObjectStream lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
	    ObjectStream sampleStream = new DocumentSampleStream(lineStream);
	    
	    // Specifies the minimum number of times a feature must be seen
	    
	    TrainingParameters params = new TrainingParameters();
	    params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(0));
	    params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(30));
	    //params.put(TrainingParameters.ALGORITHM_PARAM, NaiveBayesTrainer.NAIVE_BAYES_VALUE);

	    model = DocumentCategorizerME.train("en", sampleStream, params);
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
	DocumentCategorizerME classifier = new DocumentCategorizerME(model);
	double[] outcomes = classifier.categorize(text);
	String predictedCategory = classifier.getBestCategory(outcomes);
	
	System.out.println("Model prediction: " + predictedCategory);
    }
    
    
}
