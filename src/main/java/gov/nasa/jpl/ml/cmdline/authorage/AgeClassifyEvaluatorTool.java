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

package gov.nasa.jpl.ml.cmdline.authorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
 
import opennlp.tools.cmdline.AbstractEvaluatorTool;
import opennlp.tools.cmdline.ArgumentParser.OptionalParameter;
import opennlp.tools.cmdline.ArgumentParser.ParameterDescription;
import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.TerminateToolException;
import opennlp.tools.cmdline.params.EvaluatorParams;
import opennlp.tools.authorage.AgeClassifyEvaluationMonitor;
import opennlp.tools.authorage.AgeClassifyModel;
import opennlp.tools.authorage.AgeClassifyEvaluator;
import opennlp.tools.authorage.AgeClassifyME;
import opennlp.tools.authorage.AuthorAgeSample;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.eval.EvaluationMonitor;

/**
 * TODO: Documentation
 */
public final class AgeClassifyEvaluatorTool extends
    AbstractEvaluatorTool<AuthorAgeSample, EvaluatorParams> {
    
    public AgeClassifyEvaluatorTool() {
	super(AuthorAgeSample.class, EvaluatorParams.class);
    }

    public String getShortDescription() {
	return "measures the performance of the AgeClassify model with the reference data";
    }

    public void run(String format, String[] args) {
	super.run(format, args);
	
	AgeClassifyModel model;
	try{
	    model = new AgeClassifyModel(params.getModel());
	} catch (Exception e) {
	    e.printStackTrace();
	    return;
	}
	
	List<EvaluationMonitor<AuthorAgeSample>> listeners = new LinkedList<EvaluationMonitor<AuthorAgeSample>>();
	if (params.getMisclassified()) {
	    listeners.add(new AgeClassifyEvaluationErrorListener());
	}
	
	AgeClassifyEvaluator evaluator = new AgeClassifyEvaluator(
	    new AgeClassifyME(model),
	    listeners.toArray(new AgeClassifyEvaluationMonitor[listeners.size()]));

	final PerformanceMonitor monitor = new PerformanceMonitor("doc");

	ObjectStream<AuthorAgeSample> measuredSampleStream = new ObjectStream<AuthorAgeSample>() {

	    public AuthorAgeSample read() throws IOException {
		monitor.incrementCounter();
		return sampleStream.read();
	    }

	    public void reset() throws IOException {
		sampleStream.reset();
	    }

	    public void close() throws IOException {
		sampleStream.close();
	    }
	};

	monitor.startAndPrintThroughput();

	try {
	    evaluator.evaluate(measuredSampleStream);
	} catch (IOException e) {
	    System.err.println("failed");
	    throw new TerminateToolException(-1, "IO error while reading test data: "
					     + e.getMessage(), e);
	} finally {
	    try {
		measuredSampleStream.close();
	    } catch (IOException e) {
		// sorry that this can fail
	    }
	}

	monitor.stopAndPrintFinalResult();

	System.out.println();

	System.out.println(evaluator);
    }
    
}