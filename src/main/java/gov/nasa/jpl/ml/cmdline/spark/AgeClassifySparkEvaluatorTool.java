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

import java.io.IOException;

import gov.nasa.jpl.ml.cmdline.CLI;
import gov.nasa.jpl.ml.cmdline.params.SparkEvalToolParams;
import gov.nasa.jpl.ml.spark.AgeClassifySparkEvaluator;
import opennlp.tools.authorage.AuthorAgeSample;
import opennlp.tools.cmdline.AbstractEvaluatorTool;
import opennlp.tools.cmdline.ArgumentParser;
import opennlp.tools.cmdline.ObjectStreamFactory;
import opennlp.tools.cmdline.StreamFactoryRegistry;
import opennlp.tools.cmdline.TerminateToolException;

/**
 * TODO: Documentation
 */ 
public class AgeClassifySparkEvaluatorTool 
    extends AbstractEvaluatorTool<AuthorAgeSample, SparkEvalToolParams> {

    public AgeClassifySparkEvaluatorTool() {
	super(AuthorAgeSample.class, SparkEvalToolParams.class);
    }
    
     @Override
	 public String getShortDescription() {
	 return "measures the performance of the AgeClassify model with the reference data";
     }
     
     @Override
     @SuppressWarnings({"unchecked"})
     public String getHelp(String format) {
	 if ("".equals(format) || StreamFactoryRegistry.DEFAULT_FORMAT.equals(format)) {
	     return getBasicHelp(paramsClass,
				 StreamFactoryRegistry.getFactory(type, StreamFactoryRegistry.DEFAULT_FORMAT)
				 .<SparkEvalToolParams>getParameters());
	 } else {
	     ObjectStreamFactory<AuthorAgeSample> factory = StreamFactoryRegistry.getFactory(type, format);
	     if (null == factory) {
		 throw new TerminateToolException(1, "Format " + format + " is not found.\n" + getHelp());
	     }
	     return "Usage: " + CLI.CMD + " " + getName() + " " +
		 ArgumentParser.createUsage(paramsClass, factory.<SparkEvalToolParams>getParameters());
	 }
     }
     
     public void run(String format, String[] args) {
	 validateAllArgs(args, this.paramsClass, format);
	 
	 params = ArgumentParser.parse(ArgumentParser.filter(args, this.paramsClass), this.paramsClass);
	 try {
	     AgeClassifySparkEvaluator.evaluate(params.getModel(), params.getData());
	     
	 } catch (IOException e) {
	     System.err.println("failed");
	     throw new TerminateToolException(-1, "IO error while reading test data: "
					      + e.getMessage(), e);
	 }
	 
	 
     }
     
}