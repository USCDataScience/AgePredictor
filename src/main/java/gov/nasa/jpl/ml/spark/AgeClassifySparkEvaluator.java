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
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import opennlp.tools.authorage.AgeClassifyME;
import scala.Tuple2;

/**
* TODO: Documentation
*/ 
public class AgeClassifySparkEvaluator {

    public static class EvaluateSample implements Function<Tuple2<String, String>, Boolean> {
	private AgeClassifyModelWrapper wrapper;
	
	public EvaluateSample(File model) {
	    this.wrapper = new AgeClassifyModelWrapper(model);
	}
	
	public EvaluateSample(AgeClassifyModelWrapper w) {
	    this.wrapper = w;
	}
	
	@Override
	public Boolean call(Tuple2<String, String> sample) throws IOException {
	    String category = sample._1();
	    String text = sample._2();

	    System.out.println("Sample: " + category + ", " + text);
	    AgeClassifyME classifier = this.wrapper.getClassifier();
	    
	    String predicted = classifier.predict(text);
	    return (predicted == category);
	}
    }
    
    public static void evaluate(File model, String dataIn) throws IOException {
	SparkConf conf = new SparkConf().setAppName("AgeClassifySparkEvaluator");
	JavaSparkContext sc = new JavaSparkContext(conf);
	
	JavaRDD<String> data = sc.textFile(dataIn,8).cache();
	
	JavaPairRDD<String, String> samples = data.mapToPair(
	    new PairFunction<String, String, String>() {
		@Override
		public Tuple2<String, String> call(String s) {
		    try {
			String category = s.split("\t", 2)[0];
			String text = s.split("\t", 2)[1];
			
			return new Tuple2<>(category, text);

		    } catch (Exception e) {
			return null;
		    }
		}
	    }).cache();
	
	JavaPairRDD<String, String> validSamples = samples.filter(
		      new Function<Tuple2<String, String>, Boolean>() {
			  @Override
			  public Boolean call(Tuple2<String, String> t) {
			      return (t != null);
			  }
		      }).cache();
	samples.unpersist();
	    
	JavaPairRDD<String, String> correct = validSamples.filter(new EvaluateSample(model)).cache();
	
	long total = validSamples.count();
	long good = correct.count();
	if (total > 0) {   
	    System.out.println("Accuracy: " +  (double) good / (double) total);
	    System.out.println("Number of Documents: " + total);
	}
	
	JavaPairRDD<String, Integer> totalMap = validSamples.aggregateByKey(0,
	    new Function2<Integer, String, Integer>() {
		@Override
		public Integer call(Integer i, String s) {
		    return i + 1;
		}
	    },
            new Function2<Integer, Integer, Integer>() {
      	        @Override
		public Integer call(Integer i1, Integer i2) {
		    return i1 + i2;
		}
	    }).cache();
	
	validSamples.unpersist();
	
	List<Tuple2<String, Integer>> totalCount = totalMap.collect();
	
	JavaPairRDD<String, Integer> correctMap = correct.aggregateByKey(0,
	    new Function2<Integer, String, Integer>() {
		@Override
		public Integer call(Integer i, String s) {
		    return i + 1;
		}
	    },
            new Function2<Integer, Integer, Integer>() {
      	        @Override
		public Integer call(Integer i1, Integer i2) {
		    return i1 + i2;
		}
	    }).cache();
	
	correct.unpersist();

	List<Tuple2<String, Integer>> correctCount = correctMap.collect();
		
	for (int i = 0; i < totalCount.size(); i++) {
	    if (totalCount.get(i)._2() > 0) {
		System.out.println(totalCount.get(i)._1() + ": " + totalCount.get(i)._2() + " documents");
		System.out.println("Accuracy: " + (double) correctCount.get(i)._2() / totalCount.get(i)._2());
	    }
	}
	
	sc.stop();
    } 
    
}