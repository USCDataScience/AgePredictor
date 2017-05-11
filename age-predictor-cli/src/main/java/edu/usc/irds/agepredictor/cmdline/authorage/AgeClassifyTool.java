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

package edu.usc.irds.agepredictor.cmdline.authorage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import edu.usc.irds.agepredictor.cmdline.CLI;
import opennlp.tools.authorage.AgeClassifyME;
import opennlp.tools.authorage.AgeClassifyModel;
import opennlp.tools.cmdline.BasicCmdLineTool;
import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.SystemInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ParagraphStream;
import opennlp.tools.util.PlainTextByLineStream;

/**
 * TODO: Documentation
 */
public class AgeClassifyTool extends BasicCmdLineTool {
    
    @Override
    public String getShortDescription() {
	return "age classifier";
    }
    
    @Override
	public String getHelp() {
	return "Usage: " + CLI.CMD + " " + getName() + " model < documents";
    }
    
    @Override
    public void run(String[] args) {
	
	if (0 == args.length) {
	    System.out.println(getHelp());
	} else {
	    AgeClassifyModel model;
	    try {
		model = new AgeClassifyModel(new File(args[0]));
	    } catch (Exception e) {
		e.printStackTrace();
		return;
	    }
	    
	    AgeClassifyME classify = new AgeClassifyME(model);
	    
	    ObjectStream<String> documentStream;
	    
	    PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "doc");
	    perfMon.start();
	    
	    try {
		documentStream = new ParagraphStream(
		    new PlainTextByLineStream(new SystemInputStreamFactory(), SystemInputStreamFactory.encoding()));
		String document;
		while ((document = documentStream.read()) != null) {
		    String[] tokens = model.getFactory().getTokenizer().tokenize(document);
		    
		    double prob[] = classify.getProbabilities(tokens);
		    String category = classify.getBestCategory(prob);
		    
		    System.out.println(Arrays.toString(tokens));
		    System.out.println(category);
		    
		    perfMon.incrementCounter();
		}
	    } catch (IOException e) {
		CmdLineUtil.handleStdinIoError(e);
	    } 
	
	    perfMon.stopAndPrintFinalResult();
	}
    }
}
