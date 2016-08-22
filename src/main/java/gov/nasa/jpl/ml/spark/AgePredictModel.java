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

import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.mllib.regression.LassoModel;

/**
 * Wrapper for all the components needed for the Regression model. Supports serialization
 * and deserialization from file.
 */
public class AgePredictModel implements Serializable {
    private String languageCode;
    private LassoModel model;
    private String[] vocabulary;
    private AgeClassifyContextGeneratorWrapper wrapper;
    
    public AgePredictModel(String languageCode, LassoModel agePredictModel, String[] vocabulary,
			   AgeClassifyContextGeneratorWrapper wrapper) {

	this.languageCode = languageCode;
	this.model = agePredictModel;
	this.vocabulary = vocabulary;
	this.wrapper = wrapper;
    }
    
    public static AgePredictModel readModel(File file) throws IOException {
	//deserialize from file
	AgePredictModel model;
	try{
	    FileInputStream fin = new FileInputStream(file);
	    ObjectInputStream ois = new ObjectInputStream(fin);
	    model = (AgePredictModel) ois.readObject();
	    ois.close();

	    return model;

	}catch(Exception e){
	    e.printStackTrace();
	    return null;
	}
	
    }
    
    public AgeClassifyContextGeneratorWrapper getContext() {
	return this.wrapper;
    }
    
    public LassoModel getModel() {
	return this.model;
    }
    
    public String[] getVocabulary() {
	return this.vocabulary;
    }
    
    public static void writeModel(AgePredictModel model, File file) {
	try {
	    //serialize to file
	    FileOutputStream fout = new FileOutputStream(file);
	    ObjectOutputStream oos = new ObjectOutputStream(fout);
	    oos.writeObject(model);
	    oos.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
    }
    
}