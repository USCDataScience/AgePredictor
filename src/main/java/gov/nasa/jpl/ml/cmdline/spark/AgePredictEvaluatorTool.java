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

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;
import java.io.FileInputStream;
import java.io.IOException;

import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.cmdline.SystemInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ParagraphStream;
import opennlp.tools.util.PlainTextByLineStream;

import opennlp.tools.cmdline.ArgumentParser;
import opennlp.tools.cmdline.ObjectStreamFactory;
import opennlp.tools.cmdline.StreamFactoryRegistry;
import opennlp.tools.cmdline.params.EvaluatorParams;
import opennlp.tools.cmdline.AbstractEvaluatorTool;
import opennlp.tools.cmdline.TerminateToolException;
import opennlp.tools.authorage.AuthorAgeSample;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

import opennlp.tools.authorage.AgeClassifyModel;
import opennlp.tools.authorage.AgeClassifyME;
import opennlp.tools.util.featuregen.FeatureGenerator;

import gov.nasa.jpl.ml.spark.authorage.AgePredictModel;
import gov.nasa.jpl.ml.spark.authorage.AgeClassifyContextGeneratorWrapper;
import gov.nasa.jpl.ml.cmdline.params.PredictEvalToolParams;
import gov.nasa.jpl.ml.spark.authorage.AgePredictEvaluator;
import gov.nasa.jpl.ml.cmdline.CLI;

/**
 * TODO: Documentation
 */ 
public class AgePredictEvaluatorTool 
    extends AbstractEvaluatorTool<AuthorAgeSample, PredictEvalToolParams> {

    public AgePredictEvaluatorTool() {
	super(AuthorAgeSample.class, PredictEvalToolParams.class);
    }
    
     @Override
	 public String getShortDescription() {
	 return "measures the performance of the AgePredict model with the reference data";
     }
     
     @Override
     @SuppressWarnings({"unchecked"})
     public String getHelp(String format) {
	 if ("".equals(format) || StreamFactoryRegistry.DEFAULT_FORMAT.equals(format)) {
	     return getBasicHelp(paramsClass,
				 StreamFactoryRegistry.getFactory(type, StreamFactoryRegistry.DEFAULT_FORMAT)
				 .<PredictEvalToolParams>getParameters());
	 } else {
	     ObjectStreamFactory<AuthorAgeSample> factory = StreamFactoryRegistry.getFactory(type, format);
	     if (null == factory) {
		 throw new TerminateToolException(1, "Format " + format + " is not found.\n" + getHelp());
	     }
	     return "Usage: " + CLI.CMD + " " + getName() + " " +
		 ArgumentParser.createUsage(paramsClass, factory.<PredictEvalToolParams>getParameters());
	 }
     }
     
     public void run(String format, String[] args) {
	 validateAllArgs(args, this.paramsClass, format);
	 
	 SparkSession spark = SparkSession
	     .builder()
	     .appName("AgePredictEvaluator")
	     .getOrCreate();
	 
	 params = ArgumentParser.parse(ArgumentParser.filter(args, this.paramsClass), this.paramsClass);
	 ObjectStream<String> documentStream;
	 List<Row> data = new ArrayList<Row>();

	 AgePredictModel model = null;
	 AgeClassifyME classify = null;
	 try {    
	     model = AgePredictModel.readModel(params.getModel());
	     classify = new AgeClassifyME(new AgeClassifyModel(params.getClassifyModel()));
	 } catch (IOException e) {
	     e.printStackTrace();
	     return;
	 }

	 AgeClassifyContextGeneratorWrapper contextGen = model.getContext();
	 try {
	     documentStream = new ParagraphStream(
	         new PlainTextByLineStream(new FileInputStream(params.getData()), 
		     SystemInputStreamFactory.encoding()));

	     String document;
	     FeatureGenerator[] featureGenerators = contextGen.getFeatureGenerators();
	     while ((document = documentStream.read()) != null) {
		 String label = document.split("\t", 2)[0]; 
		 String text = document.split("\t", 2)[1];
		  
		 String[] tokens = contextGen.getTokenizer().tokenize(text);
		  
		 double prob[] = classify.getProbabilities(tokens);
		 String category = classify.getBestCategory(prob);
		  
		 Collection<String> context = new ArrayList<String>();
		  
		 for (FeatureGenerator featureGenerator : featureGenerators) {
		          Collection<String> extractedFeatures =
			      featureGenerator.extractFeatures(tokens);
			  context.addAll(extractedFeatures);
		 }
		  
		 if (category != null) {
		     for (int i = 0; i < tokens.length / 9; i++) {
			 context.add("cat="+ category);
		     }
		 }
		 if (context.size() > 0) {
		     try {
			 int age = Integer.valueOf(label);
			 System.out.println("Row:" + age + "," +  Arrays.toString(context.toArray()));
			  
			 data.add(RowFactory.create(age, context.toArray()));
		     } catch (Exception e) {
			 //do nothing
		     }
		 }
	     } 
	 } catch (IOException e) {
	     CmdLineUtil.handleStdinIoError(e);
	 }
	 
	 try {
	     AgePredictEvaluator.evaluate(spark, params.getClassifyModel(), params.getModel(), 
					  params.getReport(), data);
	 } catch (IOException e) {
	     System.err.println("failed");
	     throw new TerminateToolException(-1, "IO error while reading test data: "
					      + e.getMessage(), e);
	 }
	 
     }
     
}