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

package edu.usc.irds.agepredictor.authorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.ml.feature.Normalizer;
import org.apache.spark.ml.linalg.SparseVector;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LassoModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.ArrayType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import edu.usc.irds.agepredictor.spark.authorage.AgePredictModel;
import opennlp.tools.authorage.AgeClassifyME;
import opennlp.tools.authorage.AgeClassifyModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.FeatureGenerator;

/**
 * Uses already trained MLLib LassoModel in Spark Local mode. </br>
 * Provides java method which can be conveniently used by other libraries
 */
public class AgePredicterLocal {
	
	private SparkSession spark;
	private AgeClassifyModel classifyModel;
	private AgeClassifyME classify;
	private AgePredictModel model;
	
	public AgePredicterLocal() throws InvalidFormatException, IOException {
		this("./model/classify-bigram.bin", "./model/regression-global.bin");
	}
	
	public AgePredicterLocal(String pathToClassifyModel, String pathToRegressionModel) throws InvalidFormatException, IOException{
		spark = SparkSession.builder().master("local").appName("AgePredict").getOrCreate();
		classifyModel = new AgeClassifyModel(new File(pathToClassifyModel));

		classify = new AgeClassifyME(classifyModel);
		model = AgePredictModel.readModel(new File(pathToRegressionModel));
	}
	
	public double predictAge(String document) throws InvalidFormatException, IOException {
		FeatureGenerator[] featureGenerators = model.getContext().getFeatureGenerators();

		List<Row> data = new ArrayList<Row>();

		String[] tokens = model.getContext().getTokenizer().tokenize(document);

		double prob[] = classify.getProbabilities(tokens);
		String category = classify.getBestCategory(prob);

		Collection<String> context = new ArrayList<String>();

		for (FeatureGenerator featureGenerator : featureGenerators) {
			Collection<String> extractedFeatures = featureGenerator.extractFeatures(tokens);
			context.addAll(extractedFeatures);
		}

		if (category != null) {
			for (int i = 0; i < tokens.length / 18; i++) {
				context.add("cat=" + category);
			}
		}

		if (context.size() > 0) {
			data.add(RowFactory.create(document, context.toArray()));
		}

		StructType schema = new StructType(
				new StructField[] { new StructField("document", DataTypes.StringType, false, Metadata.empty()),
						new StructField("text", new ArrayType(DataTypes.StringType, true), false, Metadata.empty()) });

		Dataset<Row> df = spark.createDataFrame(data, schema);

		CountVectorizerModel cvm = new CountVectorizerModel(model.getVocabulary()).setInputCol("text")
				.setOutputCol("feature");

		Dataset<Row> eventDF = cvm.transform(df);

		Normalizer normalizer = new Normalizer().setInputCol("feature").setOutputCol("normFeature").setP(1.0);

		JavaRDD<Row> normEventDF = normalizer.transform(eventDF).javaRDD();

		Row event = normEventDF.first();

		SparseVector sp = (SparseVector) event.getAs("normFeature");

		final LassoModel linModel = model.getModel();

		Vector testData = Vectors.sparse(sp.size(), sp.indices(), sp.values());
		return linModel.predict(testData.compressed());

	}

	public static void main(String[] args) throws Exception {

		String inputText = "I am very very old person";
		if (args.length > 0) {
			StringBuilder builder = new StringBuilder();
			for (String s : args) {
				builder.append(s);
				builder.append(" ");
			}
			inputText = builder.toString();
		}
		double age = new AgePredicterLocal().predictAge(inputText);
		
		System.out.println("\n===================\n");
		System.out.println(String.format("Text received- '%s' \n Predicted Age - %f%n",inputText, age));
		System.out.println("\n===================\n");
	}
}
