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

package gov.nasa.jpl.ml.cmdline.spark.authorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import gov.nasa.jpl.ml.cmdline.CLI;
import gov.nasa.jpl.ml.cmdline.params.SparkTrainingToolParams;
import gov.nasa.jpl.ml.spark.authorage.AgeClassifySparkTrainer;
import opennlp.tools.authorage.AgeClassifyModel;
import opennlp.tools.authorage.AuthorAgeSample;
import opennlp.tools.cmdline.AbstractTrainerTool;
import opennlp.tools.cmdline.ArgumentParser;
import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.cmdline.ObjectStreamFactory;
import opennlp.tools.cmdline.StreamFactoryRegistry;
import opennlp.tools.cmdline.TerminateToolException;
import opennlp.tools.ml.AgeClassifyTrainerFactory;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;

/**
 * TODO: Documentation
 */
public class AgeClassifySparkTrainerTool 
    extends AbstractTrainerTool<AuthorAgeSample, SparkTrainingToolParams> {
    
    protected SparkTrainingToolParams params;
    protected TrainingParameters mlParams;
    
    public AgeClassifySparkTrainerTool() {
	super(AuthorAgeSample.class, SparkTrainingToolParams.class);
    }
     
    @Override
    public String getShortDescription() {
	return "trainer for the author age classifier, using Spark API";
    }
    
    @Override
    @SuppressWarnings({"unchecked"})
    public String getHelp(String format) {
	if ("".equals(format) || StreamFactoryRegistry.DEFAULT_FORMAT.equals(format)) {
	    return getBasicHelp(paramsClass,
				StreamFactoryRegistry.getFactory(type, StreamFactoryRegistry.DEFAULT_FORMAT)
				.<SparkTrainingToolParams>getParameters());
	} else {
	    ObjectStreamFactory<AuthorAgeSample> factory = StreamFactoryRegistry.getFactory(type, format);
	    if (null == factory) {
		throw new TerminateToolException(1, "Format " + format + " is not found.\n" + getHelp());
	    }
	    return "Usage: " + CLI.CMD + " " + getName() + " " +
		ArgumentParser.createUsage(paramsClass, factory.<SparkTrainingToolParams>getParameters());
	}
    }
    
    @Override
    public void run(String format, String[] args) {
	validateAllArgs(args, this.paramsClass, format);

	params = ArgumentParser.parse(
	    ArgumentParser.filter(args, this.paramsClass), this.paramsClass);

	TrainingParameters mlParams = null;
	
	// load training parameters
	String paramFile = params.getParams();
	if (paramFile != null) {
	    CmdLineUtil.checkInputFile("Training Parameter", new File(paramFile));
	    
	    InputStream paramsIn = null;
	    try {
		paramsIn = new FileInputStream(new File(paramFile));
		
		mlParams = new TrainingParameters(paramsIn);
	    } catch(IOException e) {
		throw new TerminateToolException(-1, "Error during parameters loading: " + e.getMessage(), e);
	    }
	    finally {
		try {
		    if (paramsIn != null)
			paramsIn.close();
		} catch (IOException e) {
		    //handle error?
		}
	    }
	    
	    if (!AgeClassifyTrainerFactory.isValid(mlParams.getSettings())) {
		throw new TerminateToolException(1, "Training parameters file '" + paramFile + "' is invalid!");    
	    }
	}
	if (mlParams == null) {
	    mlParams = ModelUtil.createDefaultTrainingParameters();
	}
	
	// load output model file
	File modelOutFile = params.getModel();
	
	CmdLineUtil.checkOutputFile("age classifier model", modelOutFile);
	
	System.out.println("Feature Generators: " + params.getFeatureGenerators());
	System.out.println("Tokenizer: " + params.getTokenizer());

	AgeClassifyModel model;
	try {
	    model = AgeClassifySparkTrainer.createModel(params.getLang(), params.getData(),
	        params.getTokenizer(), params.getFeatureGenerators(), mlParams);
	} catch (IOException e) {
	    throw new TerminateToolException(-1,
	        "IO error while reading training data or indexing data: " + e.getMessage(), e);
	} 
	
	CmdLineUtil.writeModel("age classifier", modelOutFile, model);
    }
    
   
}
	    