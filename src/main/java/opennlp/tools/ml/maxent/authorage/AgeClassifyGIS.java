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

package opennlp.tools.ml.maxent.authorage;

import java.io.IOException;

import opennlp.tools.ml.AbstractEventTrainer;
import opennlp.tools.ml.maxent.GISModel;
import opennlp.tools.ml.maxent.GISTrainer;
import opennlp.tools.ml.model.ChiSquaredDataIndexer;
import opennlp.tools.ml.model.DataIndexer;
import opennlp.tools.ml.model.Event;
import opennlp.tools.ml.model.MaxentModel;
import opennlp.tools.ml.model.OnePassDataIndexer;
import opennlp.tools.ml.model.Prior;
import opennlp.tools.ml.model.TwoPassDataIndexer;
import opennlp.tools.ml.model.UniformPrior;
import opennlp.tools.util.ObjectStream;

public class AgeClassifyGIS extends AbstractEventTrainer {
    
    public static final String MAXENT_VALUE = "AUTHORAGE";
    
    public static final String DATA_INDEXER_CHI_SQUARED = "ChiSquared";
    
    
    /**
     * Set this to false if you don't want messages about the progress of model
     * training displayed. Alternately, you can use the overloaded version of
     * trainModel() to conditionally enable progress messages.
     */
    public static boolean PRINT_MESSAGES = true;

    /**
     * If we are using smoothing, this is used as the "number" of times we want
     * the trainer to imagine that it saw a feature that it actually didn't see.
     * Defaulted to 0.1.
     */
    public static double SMOOTHING_OBSERVATION = 0.1;
    
    public AgeClassifyGIS() {
    }
    
    @Override
    public boolean isValid() {
	String dataIndexer = getStringParam(DATA_INDEXER_PARAM,
					    DATA_INDEXER_TWO_PASS_VALUE);

	if (dataIndexer != null) {
	    if (!(DATA_INDEXER_ONE_PASS_VALUE.equals(dataIndexer) 
		  || DATA_INDEXER_TWO_PASS_VALUE.equals(dataIndexer)
		  || DATA_INDEXER_CHI_SQUARED.equals(dataIndexer))) {
		return false;
	    }
	}
	
	String algorithmName = getAlgorithm();

	if (algorithmName != null && !(MAXENT_VALUE.equals(algorithmName))) {
	    return false;
	}

	return true;
    }
    
    public boolean isSortAndMerge() {
	return true;
    }
    
   
    @Override
    public DataIndexer getDataIndexer(ObjectStream<Event> events) throws IOException {
	
	String dataIndexerName = getStringParam(DATA_INDEXER_PARAM,
						DATA_INDEXER_TWO_PASS_VALUE);
	
	int cutoff = getCutoff();
	boolean sortAndMerge = isSortAndMerge();
	DataIndexer indexer = null;
	
	if (DATA_INDEXER_ONE_PASS_VALUE.equals(dataIndexerName)) {
	    indexer = new OnePassDataIndexer(events, cutoff, sortAndMerge);
	} else if (DATA_INDEXER_TWO_PASS_VALUE.equals(dataIndexerName)) {
	    indexer = new TwoPassDataIndexer(events, cutoff, sortAndMerge);
	} else if (DATA_INDEXER_CHI_SQUARED.equals(dataIndexerName)) {
	    indexer = new ChiSquaredDataIndexer(events, cutoff, sortAndMerge);
	}
	else {
	    throw new IllegalStateException("Unexpected data indexer name: "
					    + dataIndexerName);
	}
	return indexer;
    }
   
    
    public MaxentModel doTrain(DataIndexer indexer) throws IOException {
	int iterations = getIterations();

	MaxentModel model;
	
	int threads = getIntParam("Threads", 1);

	model = trainModel(iterations, indexer, true, false, null, 0, threads);

	return model;
    }
    
    /**
     * Train a model using the GIS algorithm.
     *
     * @param iterations
     *          The number of GIS iterations to perform.
     * @param indexer
     *          The object which will be used for event compilation.
     * @param printMessagesWhileTraining
     *          Determines whether training status messages are written to STDOUT.
     * @param smoothing
     *          Defines whether the created trainer will use smoothing while
     *          training the model.
     * @param modelPrior
     *          The prior distribution for the model.
     * @param cutoff
     *          The number of times a predicate must occur to be used in a model.
     * @return The newly trained model, which can be used immediately or saved to
     *         disk using an opennlp.tools.ml.maxent.io.GISModelWriter object.
     */
    public static GISModel trainModel(int iterations, DataIndexer indexer,
				      boolean printMessagesWhileTraining, boolean smoothing, Prior modelPrior,
				      int cutoff, int threads) {
	GISTrainer trainer = new GISTrainer(printMessagesWhileTraining);
	trainer.setSmoothing(smoothing);
	trainer.setSmoothingObservation(SMOOTHING_OBSERVATION);
	if (modelPrior == null) {
	    modelPrior = new UniformPrior();
	}

	return trainer.trainModel(iterations, indexer, modelPrior, cutoff, threads);
    }
    
}
