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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

/**
 * Generates a feature for each word in a document.
 */
public class LIWCFeatureGenerator implements FeatureGenerator {
    
    public static final LIWCFeatureGenerator INSTANCE = new LIWCFeatureGenerator();

    private static Map<String, List<String>> liwc = new HashMap<>();
    
    static {
	File[] files = new File("props/wordlists/").listFiles();

	for (File file: files) {
	    String filename = file.getName();
	    filename = filename.substring(0, filename.lastIndexOf('.'));
	    
	    try {
		List<String> list = FileUtils.readLines(file, "utf-8");
		liwc.put(filename, list);
	    } catch (IOException e) {
		//Just ignore?
	    }
	}
    }
	
    
    public LIWCFeatureGenerator() {
    }
    
    @Override
    public Collection<String> extractFeatures(String[] text) {
	
	Collection<String> liwc_features = new ArrayList<String>();
	
	for (String word: text) {
	    Iterator it = liwc.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry<String, List<String>> pair = (Map.Entry)it.next();
		if (pair.getValue().contains(word)) {
		    liwc_features.add("liwc=" + pair.getKey());
		}
	    }
	}
	
	return liwc_features;
    }

}
