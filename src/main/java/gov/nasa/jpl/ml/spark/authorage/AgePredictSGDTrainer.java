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
 1;95;0c* Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gov.nasa.jpl.ml.spark.authorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.ml.feature.Normalizer;
import org.apache.spark.ml.linalg.SparseVector;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.LassoModel;
import org.apache.spark.mllib.regression.LassoWithSGD;
import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.ArrayType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import opennlp.tools.util.TrainingParameters;
import scala.Tuple2;

/**
 * TODO: Documentation
 */
public class AgePredictSGDTrainer {

    public static final String CUTOFF_PARAM = "Cutoff";
    public static final int CUTOFF_DEFAULT = 5;

    public static final String ITERATIONS_PARAM = "Iterations";
    public static final int ITERATIONS_DEFAULT = 100;

    public static final String STEPSIZE_PARAM = "StepSize";
    public static final double STEPSIZE_DEFAULT = 1.0;
    
    public static final String REG_PARAM = "Regularization";
    public static final double REG_DEFAULT = 0.1;
    
    public static void generateEvents(SparkSession spark, String dataIn, 
				      String tokenizer, String featureGenerators,
				      String outDir) throws IOException {
	
	AgeClassifyContextGeneratorWrapper wrapper = new AgeClassifyContextGeneratorWrapper(
	    tokenizer, featureGenerators);
	
	JavaRDD<String> data = spark.sparkContext().textFile(dataIn, 48).toJavaRDD()
	    .cache();
	
	JavaRDD<EventWrapper> samples = data.map(new CreateEvents(wrapper)).cache();
	
	JavaRDD<EventWrapper> validSamples = samples.filter(
	    new Function<EventWrapper, Boolean>() {
		@Override
		public Boolean call(EventWrapper s) { 
		    if (s != null) {
			return s.getValue() != null;
		    }
		    return false; 
		}
	    }).repartition(8);
	
	File dir = new File(outDir);
	if (dir.exists()) {
	    FileUtils.cleanDirectory(dir); //clean out directory (this is optional -- but good know)
	    FileUtils.forceDelete(dir); //delete directory
	}
	
	validSamples.saveAsTextFile(outDir);
    }
    
    private static int getCutoff(Map<String, String> params) {
	
	String cutoffString = params.get(CUTOFF_PARAM);
	
	if (cutoffString != null)
	    return Integer.parseInt(cutoffString);
	else
	    return CUTOFF_DEFAULT;
    }

    private static int getIterations(Map<String, String> params) {

	String iterationString = params.get(ITERATIONS_PARAM);
	
	if (iterationString != null)
	    return Integer.parseInt(iterationString);
	else
	    return ITERATIONS_DEFAULT;
    }
    
    private static double getStepSize(Map<String, String> params) {
	
	String stepString = params.get(STEPSIZE_PARAM);

	if (stepString != null)
	    return Double.parseDouble(stepString);
	else
	    return STEPSIZE_DEFAULT;
    }
    
    private static double getReg(Map<String, String> params) {
	
	String regString = params.get(REG_PARAM);

	if (regString != null)
	    return Double.parseDouble(regString);
	else
	    return REG_DEFAULT;
    }
    
    public static AgePredictModel createModel(String languageCode, SparkSession spark, 
					      String eventDir, AgeClassifyContextGeneratorWrapper wrapper,
					      TrainingParameters trainParams) throws IOException {
	
	Map<String, String> params = trainParams.getSettings();
	
	int cutoff = getCutoff(params);
	int iterations = getIterations(params);
	
	JavaRDD<String> data = spark.sparkContext().textFile(eventDir, 24).toJavaRDD()
	    .cache();
	
	JavaRDD<Row> samples = data.map(
	    new Function<String, Row>() {
		public Row call(String s) {
		    if (s == null) {
			return null;
		    }
		    String[] parts = s.split(",");
		    if (parts.length != 3) {
			return null;
		    }
		    try {
			if (parts[0] != "-1") {
			    Integer value = Integer.parseInt(parts[0]);
			        
			    String[] text = parts[2].split(" ");
			    //add in the category as another feature
			    List<String> tokens= new ArrayList<String>(Arrays.asList(text));
			    
			    for (int i = 0; i < text.length / 18; i++) {
				tokens.add("cat=" + parts[1]);
			    }			    
			    
			    //System.out.println("Event:" + value + "," + Arrays.toString(tokens.toArray()));
			    return RowFactory.create(value, tokens.toArray());
			} else {
			    return null;
			}
		    } catch (Exception e) {
			return null;
		    }

		}
	    }).cache();
	
	JavaRDD<Row> validSamples = samples.filter(
	    new Function<Row, Boolean>() {
                @Override
                public Boolean call(Row s) { return s != null; }
	    }).cache();
	
	samples.unpersist();
	
	StructType schema = new StructType(new StructField [] {
                new StructField("value", DataTypes.IntegerType, false, Metadata.empty()),
		new StructField("context", new ArrayType(DataTypes.StringType, true), false, Metadata.empty())
            });

	Dataset<Row> df = spark.createDataFrame(validSamples, schema).cache();
	
	CountVectorizerModel cvm = new CountVectorizer()
	    .setInputCol("context")
	    .setOutputCol("feature")
	    .setMinDF(cutoff)
	    .fit(df);
	
	Normalizer normalizer = new Normalizer()
	    .setInputCol("feature")
	    .setOutputCol("normFeature")
	    .setP(1.0);
	
	Dataset<Row> eventDF = cvm.transform(df).select("value", "feature");			
	//System.out.println("Vocab: " + cvm.vocabulary().length + "," + Arrays.toString(cvm.vocabulary()));
	Dataset<Row> normDF = normalizer.transform(eventDF).select("value", "normFeature");	

	JavaRDD<Row> events = normDF.javaRDD().cache();
 
	eventDF.unpersist();
	normDF.unpersist();
	
	JavaRDD<LabeledPoint> parsedData = events.map(
	    new Function<Row, LabeledPoint>() {
		public LabeledPoint call(Row r) {
		    Integer val = r.getInt(0);
		    SparseVector vec = (SparseVector) r.get(1);
		    
		    Vector features = Vectors.sparse(vec.size(), vec.indices(), vec.values());
		    return new LabeledPoint(val, features);
		}
	    }).cache();
	
	double stepSize = getStepSize(params);
	double regParam = getReg(params);
	
	LassoWithSGD algorithm = (LassoWithSGD) new LassoWithSGD().setIntercept(true);
	
	algorithm.optimizer()
	    .setNumIterations(iterations)
	    .setStepSize(stepSize)
	    .setRegParam(regParam);
	
	final LassoModel model = algorithm.run(JavaRDD.toRDD(parsedData));

	System.out.println("Coefficients: " + Arrays.toString(model.weights().toArray()));
	System.out.println("Intercept: " + model.intercept());
 
	// Evaluate model on training examples and compute training error
	JavaRDD<Tuple2<Double, Double>> valuesAndPreds = parsedData.map(
	    new Function<LabeledPoint, Tuple2<Double, Double>>() {
		public Tuple2<Double, Double> call(LabeledPoint point) {
		    double prediction = model.predict(point.features());
		    System.out.println(prediction + "," + point.label()); 
		    return new Tuple2<>(prediction, point.label());
		}
	    }).cache();
       
	double MAE = new JavaDoubleRDD(valuesAndPreds.map(
	   new Function<Tuple2<Double, Double>, Object>() {
	       public Object call(Tuple2<Double, Double> pair) {
		   return Math.abs(pair._1() - pair._2());
	       }
	   }).rdd()).mean();
	
	JavaRDD<Vector> vectors = valuesAndPreds.map(
	    new Function<Tuple2<Double, Double>, Vector>() {
		public Vector call(Tuple2<Double, Double> pair) {
		    return Vectors.dense(pair._1(), pair._2());
		}
	    });
	Matrix correlMatrix = Statistics.corr(vectors.rdd(), "pearson");
	
	System.out.println("Training Mean Absolute Error: " + MAE);
	System.out.println("Correlation:\n" + correlMatrix.toString());

	Map<String, String> manifestInfoEntries = new HashMap<String, String>();
	return new AgePredictModel(languageCode, model, cvm.vocabulary(), wrapper);
    }
}
