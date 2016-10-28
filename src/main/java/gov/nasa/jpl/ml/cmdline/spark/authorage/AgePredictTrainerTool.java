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

import org.apache.spark.sql.SparkSession;

import gov.nasa.jpl.ml.cmdline.CLI;
import gov.nasa.jpl.ml.cmdline.params.PredictTrainingToolParams;
import gov.nasa.jpl.ml.spark.authorage.AgeClassifyContextGeneratorWrapper;
import gov.nasa.jpl.ml.spark.authorage.AgePredictModel;
import gov.nasa.jpl.ml.spark.authorage.AgePredictSGDTrainer;
import opennlp.tools.cmdline.ArgumentParser;
import opennlp.tools.cmdline.BasicCmdLineTool;
import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.cmdline.TerminateToolException;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.ext.ExtensionLoader;
import opennlp.tools.util.featuregen.BagOfWordsFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGenerator;

/**
 * TODO: Documentation
 */
public class AgePredictTrainerTool extends BasicCmdLineTool { 
    private Class paramsClass;
    private PredictTrainingToolParams params;
    
    public AgePredictTrainerTool() {
	this.paramsClass = PredictTrainingToolParams.class;
    }
    
    @Override
    public String getShortDescription() {
	return "trainer for the age predictor";
    }
    
    @Override
    public String getHelp() {
	return "Usage: " + CLI.CMD + " " + getName() + " " +
	    ArgumentParser.createUsage(this.paramsClass);
    }
    
    @Override
    public void run(String[] args) {
	String errMessage = ArgumentParser.validateArgumentsLoudly(args, this.paramsClass);
	if (null != errMessage) {
	    throw new TerminateToolException(1, errMessage + "\n" + getHelp());
	}
	
	SparkSession spark = SparkSession
	    .builder()
	    .appName("AgePredictTrainer")
	    .getOrCreate();

	params = (PredictTrainingToolParams) ArgumentParser.parse(
	    ArgumentParser.filter(args, this.paramsClass), this.paramsClass);
	
	System.out.println("Feature Generators: " + params.getFeatureGenerators());
	System.out.println("Tokenizer: " + params.getTokenizer());
	
	try {
	    AgePredictSGDTrainer.generateEvents(spark, params.getData(), params.getTokenizer(),
					 params.getFeatureGenerators(), params.getEvents());
	} catch (IOException e) {
            throw new TerminateToolException(-1,
	        "IO error while reading training data or indexing data: " + e.getMessage(), e);
        }
	
	AgeClassifyContextGeneratorWrapper wrapper = new AgeClassifyContextGeneratorWrapper(params.getTokenizer(),
	    params.getFeatureGenerators());
	FeatureGenerator[] featureGenerators = wrapper.getFeatureGenerators();
	Tokenizer tokenizer = wrapper.getTokenizer();

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
	}
	if (mlParams == null) {
	    mlParams = new TrainingParameters();
	    mlParams.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(100));
	    mlParams.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(5));
	}
		
	File modelOutFile = params.getModel();
	
	AgePredictModel model;
	try {
	    model = AgePredictSGDTrainer.createModel(params.getLang(), spark, params.getEvents(), wrapper, mlParams);
	} catch (IOException e) {
	    throw new TerminateToolException(-1,
	        "IO error while reading training data or indexing data: " + e.getMessage(), e);
	} 
	
	AgePredictModel.writeModel(model, modelOutFile);
	spark.stop();
    }
    
    private static Tokenizer createTokenizer(String tokenizer) {
	if(tokenizer != null) {
	    return ExtensionLoader.instantiateExtension(Tokenizer.class, tokenizer);
	}
	return WhitespaceTokenizer.INSTANCE;
    }


    private static FeatureGenerator[] createFeatureGenerators(String featureGeneratorsNames) {
	if(featureGeneratorsNames == null) {
	    FeatureGenerator[] def = { new BagOfWordsFeatureGenerator()};
	    return def;
	}
	String[] classes = featureGeneratorsNames.split(",");
	FeatureGenerator[] featureGenerators = new FeatureGenerator[classes.length];
	for (int i = 0; i < featureGenerators.length; i++) {
	    featureGenerators[i] = ExtensionLoader.instantiateExtension(
	         FeatureGenerator.class, classes[i]);
	}
	return featureGenerators;
    }
    
   
}
	    
