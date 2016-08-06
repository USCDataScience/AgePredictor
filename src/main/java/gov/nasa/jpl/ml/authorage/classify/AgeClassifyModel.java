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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import opennlp.tools.ml.model.AbstractModel;
import opennlp.tools.ml.model.MaxentModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.model.BaseModel;

/**
 * TODO: Documentation
 */
public class AgeClassifyModel extends BaseModel {
    private static final String COMPONENT_NAME = "AgeClassifyME";
    private static final String AUTHORAGE_MODEL_ENTRY_NAME = "authorage.model";
    
    public AgeClassifyModel(String languageCode, MaxentModel ageClassifyModel,
			    Map<String, String> manifestInfoEntries, AgeClassifyFactory factory) {
	super(COMPONENT_NAME, languageCode, manifestInfoEntries, factory);
	artifactMap.put(AUTHORAGE_MODEL_ENTRY_NAME, ageClassifyModel);
	checkArtifactMap();
    }


    public AgeClassifyModel(URL modelURL)
	throws IOException, InvalidFormatException {
	super(COMPONENT_NAME, modelURL);
    }

    public AgeClassifyModel(File file) throws InvalidFormatException, IOException {
	super(COMPONENT_NAME, file);
    }
    
    public AgeClassifyFactory getFactory() {
	return (AgeClassifyFactory) this.toolFactory;
    }
    
    public MaxentModel getMaxentModel() {
	return (MaxentModel) artifactMap.get(AUTHORAGE_MODEL_ENTRY_NAME);
    }
    
}
