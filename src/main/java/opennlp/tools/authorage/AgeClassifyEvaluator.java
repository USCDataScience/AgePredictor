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
package opennlp.tools.authorage;

import opennlp.tools.tokenize.TokenSample;
import opennlp.tools.util.eval.Evaluator;
import opennlp.tools.util.eval.Mean;

/**
 * TODO: Documentation
 */
public class AgeClassifyEvaluator extends Evaluator<AuthorAgeSample>{
    
    private AgeClassifyME classifier;
    
    private Mean accuracy = new Mean();
    
    public AgeClassifyEvaluator(AgeClassifyME classifier,
				AgeClassifyEvaluationMonitor ... listeners) {
	super(listeners);
	this.classifier = classifier;
    }
    
    
    public AuthorAgeSample processSample(AuthorAgeSample sample) {
	String document[] = sample.getText();

	double probs[] = classifier.getProbabilities(document);

	String cat = classifier.getBestCategory(probs);

	if (sample.getCategory().equals(cat)) {
	    accuracy.add(1);
	}
	else {
	    accuracy.add(0);
	}
	
	return new AuthorAgeSample(cat, sample.getText());
    }
    
    public double getAccuracy() {
	return accuracy.mean();
    }
    
    public long getDocumentCount() {
	return accuracy.count();
    }
    
    
    @Override
    public String toString() {
	return "Accuracy: " + accuracy.mean() + "\n" +
	    "Number of documents: " + accuracy.count();
    }

}