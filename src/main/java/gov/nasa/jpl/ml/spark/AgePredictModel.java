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

package gov.nasa.jpl.ml.spark.authorage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import opennlp.tools.authorage.AgeClassifyFactory;
import opennlp.tools.util.model.BaseModel;
import opennlp.tools.util.InvalidFormatException;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.mllib.regression.LinearRegressionModel;

/**
 * TODO: Documentation
 */
public class AgePredictModel extends BaseModel {
    private static final String COMPONENT_NAME = "AgePredictSGD";
    
    private static final String AGEPREDICT_MODEL_ENTRY_NAME = "agepredict.model";
    private static final String VOCABULARY_ENTRY_NAME = "agepredict.vocab";
    
    public AgePredictModel(String languageCode, LinearRegressionModel agePredictModel,
			   String[] vocabulary, Map<String, String> manifestInfoEntries,
			   AgeClassifyFactory factory) {

	super(COMPONENT_NAME, languageCode, manifestInfoEntries, factory);
	artifactMap.put(AGEPREDICT_MODEL_ENTRY_NAME, agePredictModel);
	artifactMap.put(VOCABULARY_ENTRY_NAME, vocabulary);
	
	checkArtifactMap();
	
    }
    
    public AgePredictModel(URL modelURL)
	throws IOException, InvalidFormatException {
	super(COMPONENT_NAME, modelURL);
    }
    
    public AgePredictModel(File file) throws InvalidFormatException, IOException {
	super(COMPONENT_NAME, file);
    }
    
    public AgeClassifyFactory getFactory() {
	return (AgeClassifyFactory) this.toolFactory;
    }
    
    public LinearRegressionModel getModel() {
	return (LinearRegressionModel) artifactMap.get(AGEPREDICT_MODEL_ENTRY_NAME);
    }
    
    public String[] getVocabulary() {
	return (String[]) artifactMap.get(VOCABULARY_ENTRY_NAME);
    }
    
}