/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.util.featuregen;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.List;

import opennlp.tools.util.InvalidFormatException;

/**
 * Generates a feature for each word in a document.
 */
public class BagOfWordsFeatureGenerator implements FeatureGenerator {
    
    public static final BagOfWordsFeatureGenerator INSTANCE = new BagOfWordsFeatureGenerator();

    private static List<String> stopwords = new ArrayList<String>();
   
    static {
	try {
	    stopwords = FileUtils.readLines(new File("props/stopwords.txt"), "utf-8");
	} catch (IOException e) {
	    stopwords = new ArrayList<String>();
	}
    }
    
    public BagOfWordsFeatureGenerator() {
    }
    
    @Override
    public Collection<String> extractFeatures(String[] text) {
	
	Collection<String> bagOfWords = new ArrayList<String>(text.length);

	for (String word : text) {
	    if (!stopwords.contains(word)) {
		bagOfWords.add("bow=" + word);
	    }
	}

	return bagOfWords;
    }

}
