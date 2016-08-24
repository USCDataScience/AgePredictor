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

import scala.Tuple2;

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;


import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.ml.linalg.SparseVector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.Dataset;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.*;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.mllib.regression.LassoModel;

import opennlp.tools.authorage.AgeClassifyModel;
import opennlp.tools.authorage.AgeClassifyME;
import opennlp.tools.util.featuregen.FeatureGenerator;

import gov.nasa.jpl.ml.spark.authorage.EventWrapper;
import gov.nasa.jpl.ml.spark.authorage.AgePredictModel;

/**
 * TODO: Documentation
 */
public class AgePredictEvaluator {
    
    public static void evaluate(SparkSession spark, File classifyModel, File linModel, File report, 
				List<Row> data) throws IOException {
	
	final AgePredictModel model = AgePredictModel.readModel(linModel);
	
	StructType schema = new StructType(new StructField [] {
                new StructField("value", DataTypes.IntegerType, false, Metadata.empty()),
                new StructField("context", new ArrayType(DataTypes.StringType, true), false, Metadata.empty())
            });

	Dataset<Row> eventDF = spark.createDataFrame(data, schema).cache();
	
	CountVectorizerModel cvm = new CountVectorizerModel(model.getVocabulary())
	    .setInputCol("text")
	    .setOutputCol("feature");
	
	JavaRDD<Row> events = cvm.transform(eventDF).select("value", "feature").javaRDD()
	    .cache();
	eventDF.unpersist();
	
	JavaRDD<LabeledPoint> parsedData = events.map(
	    new Function<Row, LabeledPoint>() {
		public LabeledPoint call(Row r) {
		    Integer val = r.getInt(0);
		    Vector features = (Vector)r.get(1);
		    
		    return new LabeledPoint(val, features);
		}
	    });
	parsedData.cache();
	
	final LassoModel reg = model.getModel();
	// Evaluate model on training examples and compute training error
	JavaRDD<Tuple2<Double, Double>> valuesAndPreds = parsedData.map(
	    new Function<LabeledPoint, Tuple2<Double, Double>>() {
		public Tuple2<Double, Double> call(LabeledPoint point) {
		    double prediction = reg.predict(point.features());
		    return new Tuple2<>(prediction, (double)point.label());
		}
	    }).cache();
	
	double MSE = new JavaDoubleRDD(valuesAndPreds.map(
	   new Function<Tuple2<Double, Double>, Object>() {
	       public Object call(Tuple2<Double, Double> pair) {
		   return Math.pow(pair._1() - pair._2(), 2.0);
	       }
	   }).rdd()).mean();
	
	if (report != null) {
	    //write pairs to a file, so they can be plotted in the future 
	    Iterator<Tuple2<Double, Double>> iterator = valuesAndPreds.toLocalIterator();
	    report.createNewFile();
	    FileWriter writer = new FileWriter(report); 
	    while (iterator.hasNext()) {
		Tuple2<Double, Double> pair = iterator.next();
		writer.write(pair._1() + "," + pair._2());
	    }
	    writer.close();
	}

	System.out.println("Training Mean Squared Error = " + MSE);
    }

}