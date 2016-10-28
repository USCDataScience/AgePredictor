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
import java.io.Serializable;

import opennlp.tools.authorage.AgeClassifyME;
import opennlp.tools.authorage.AgeClassifyModel;
    
public class AgeClassifyModelWrapper implements Serializable {
    private File model;
	
    public AgeClassifyModelWrapper(File modelIn) {
	this.model = modelIn;
    }
    
    public AgeClassifyModel getModel() throws IOException {
	return new AgeClassifyModel(this.model);
    }
    
    public AgeClassifyME getClassifier() throws IOException {
	AgeClassifyModel model = new AgeClassifyModel(this.model);
	return new AgeClassifyME(model);
    }


}