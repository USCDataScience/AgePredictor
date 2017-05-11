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

package edu.usc.irds.agepredictor.cmdline.params;

import java.io.File;

import opennlp.tools.cmdline.ArgumentParser.OptionalParameter;
import opennlp.tools.cmdline.ArgumentParser.ParameterDescription;

public interface PredictEvalToolParams {
   
    @ParameterDescription(valueName = "classify model", description = "the classifier model file.")
    @OptionalParameter
	File getClassifyModel();

    @ParameterDescription(valueName = "model", description = "the model file to be evaluated.")
	File getModel();
    
    @ParameterDescription(valueName = "outputFile", description = "the path of the report file.")
    @OptionalParameter
        File getReport();
    
    @ParameterDescription(valueName = "sampleData", description = "data to be used, usually a file name.")
	String getData();

}