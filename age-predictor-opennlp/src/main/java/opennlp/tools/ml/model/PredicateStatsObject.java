/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package opennlp.tools.ml.model;

import java.util.HashMap;
import java.util.Map;


/**
 * Stores fields for predicate and category occurences in the corpus so predicate extraction
 * techniques can be easily applied.
 */
public class PredicateStatsObject {    
    public int eventCount;
    
    /**
     * It stores the co-occurrences of Predicate and Category values
     */
    public Map<String, Map<String, Integer>> predicateCategoryCounts;
    
    /**
     * Measures how many times each category was found in the training dataset.
     */
    public Map<String, Integer> categoryCounts;
    
    public PredicateStatsObject() {
        this.eventCount = 0;
	this.predicateCategoryCounts = new HashMap<>();
        this.categoryCounts = new HashMap<>();
    }
}