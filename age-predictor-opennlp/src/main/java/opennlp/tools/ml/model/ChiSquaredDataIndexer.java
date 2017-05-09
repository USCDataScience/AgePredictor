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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.tools.util.ObjectStream;

/**
 * An indexer for maxent model data which handles cutoffs for uncommon
 * contextual predicates and provides a unique integer index for each of the
 * predicates.
 */
public class ChiSquaredDataIndexer extends AbstractDataIndexer {

    /**
     * One argument constructor for DataIndexer which calls the two argument
     * constructor assuming no cutoff.
     *
     * @param eventStream
     *          An Event[] which contains the a list of all the Events seen in the
     *          training data.
     */
    public ChiSquaredDataIndexer(ObjectStream<Event> eventStream) throws IOException {
	this(eventStream, 0);
    }

    public ChiSquaredDataIndexer(ObjectStream<Event> eventStream, int cutoff)
	throws IOException {
	this(eventStream, cutoff, true);
    }

    /**
     * Two argument constructor for DataIndexer.
     *
     * @param eventStream
     *          An Event[] which contains the a list of all the Events seen in the
     *          training data.
     * @param cutoff
     *          The minimum number of times a predicate must have been observed in
     *          order to be included in the model.
     */
    public ChiSquaredDataIndexer(ObjectStream<Event> eventStream, int cutoff, boolean sort)
	throws IOException {
	
	Map<String,Integer> predicateIndex = new HashMap<String,Integer>();
	List<ComparableEvent> eventsToCompare;

	System.out.println("Indexing events using cutoff of " + cutoff + "\n");

	System.out.print("\tComputing event counts...  ");
	try {
	    File tmp = File.createTempFile("events", null);
	    tmp.deleteOnExit();
	    Writer osw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmp),"UTF8"));
	    int numEvents = computePredicateStats(eventStream, osw, predicateIndex, cutoff);
	    System.out.println("done. " + numEvents + " events");

	    System.out.print("\tIndexing...  ");

	    FileEventStream fes = new FileEventStream(tmp);
	    try {
		eventsToCompare = index(numEvents, fes, predicateIndex);
	    } finally {
		fes.close();
	    }
	    // done with predicates
	    predicateIndex = null;
	    tmp.delete();
	    System.out.println("done.");

	    if (sort) {
		System.out.print("Sorting and merging events... ");
	    }
	    else {
		System.out.print("Collecting events... ");
	    }
	    sortAndMerge(eventsToCompare,sort);
	    System.out.println("Done indexing.");
	}
	catch(IOException e) {
	    System.err.println(e);
	}
    }
    
    
    private static double chisquare(String s, PredicateStatsObject stats) {
	
	Map<String, Integer> featureCounts = stats.predicateCategoryCounts.get(s);
	int N1, N0, N00, N01, N10, N11;
	double maxScore = 0;
	
	N1 = 0; //number of documents that have the feature
	for (Integer count : featureCounts.values()) {
	    N1 += count;
	}
	N0 = stats.eventCount - N1; //number of documents without feature
	
	for (Map.Entry<String, Integer> item : featureCounts.entrySet()) {
	    String category = item.getKey();
	    
	    N11 = item.getValue(); //number of documents with feature in same category
	    N01 = stats.categoryCounts.get(category) - N11; //number of documents w/o feature in category
	    
	    N00 = N0 - N01; //number of documents w/o feature not in same category
	    N10 = N1 - N11; //number of documents with feature not in category
	    
	    double numerator = stats.eventCount * (N11 * N00 - N10 * N01) * (N11 * N00 - N10 * N01);
	    double denominator = (N11 + N01) * (N11 + N10) * (N10 + N00) * (N01 + N00);
	    
	    double score = numerator/denominator;
	    if (score > maxScore) {
		maxScore = score;
	    }
	}
	return maxScore;
    }
    
    private int computePredicateStats(ObjectStream<Event> eventStream,
				      Writer eventStore,
        Map<String, Integer> predicatesInOut, int cutoff) throws IOException {

	Set<String> predicateSet = new HashSet<String>();
	Map<String, Integer> counter = new HashMap<String, Integer>();
	
	PredicateStatsObject stats = new PredicateStatsObject();
	
	Event ev;
	String category;
	Integer categoryCount;
	
	while ((ev = eventStream.read()) != null) {
	    eventStore.write(FileEventStream.toLine(ev));
	    
	    stats.eventCount++;
	    
	    category = ev.getOutcome();
	    categoryCount = stats.categoryCounts.get(category);
	    if (categoryCount == null) {
		stats.categoryCounts.put(category, 1);
	    } 
	    else {
		stats.categoryCounts.put(category, ++categoryCount);
	    }
	    
	    String[] context = ev.getContext();
	    update(context, predicateSet, counter, cutoff);
	   
	    Map<String, Integer> predicateCounts;
	    
	    Integer predicateCategoryCount = null;
	    for (String s : context) {
		// get counts of documents with predicate
		predicateCounts = stats.predicateCategoryCounts.get(s);
		if (predicateCounts == null) {
		    // initialize
		    stats.predicateCategoryCounts.put(s, new HashMap<String, Integer>());
		}
		else {
		    predicateCategoryCount = predicateCounts.get(category);
		}
		if(predicateCategoryCount==null) {
                    predicateCategoryCount=0;
                }
		stats.predicateCategoryCounts.get(s).put(category, ++predicateCategoryCount);
	    }
	}
	
	predCounts = new int[predicateSet.size()];
	int index = 0;
	Iterator<String> pi = predicateSet.iterator();
	while (pi.hasNext()) {
	    String predicate = pi.next();
	    
	    // TODO: Allow adjustable confidence in predicate relevance (Default 90%)
	    if (chisquare(predicate, stats) > 2.71) {
		predCounts[index] = counter.get(predicate);
		predicatesInOut.put(predicate, index);
		index++;
	    }
	    
	}
	eventStore.close();
	return stats.eventCount;
    }

    private List<ComparableEvent> index(int numEvents, ObjectStream<Event> es, Map<String,Integer> predicateIndex) throws IOException {
	Map<String,Integer> omap = new HashMap<String,Integer>();
	int outcomeCount = 0;
	List<ComparableEvent> eventsToCompare = new ArrayList<ComparableEvent>(numEvents);
	List<Integer> indexedContext = new ArrayList<Integer>();
	
	Event ev;
	while ((ev = es.read()) != null) {
	    String[] econtext = ev.getContext();
	    ComparableEvent ce;

	    int ocID;
	    String oc = ev.getOutcome();

	    if (omap.containsKey(oc)) {
		ocID = omap.get(oc);
	    }
	    else {
		ocID = outcomeCount++;
		omap.put(oc, ocID);
	    }

	    for (String pred : econtext) {
		if (predicateIndex.containsKey(pred)) {
		    indexedContext.add(predicateIndex.get(pred));
		}
	    }

	    // drop events with no active features
	    if (indexedContext.size() > 0) {
		int[] cons = new int[indexedContext.size()];
		for (int ci=0;ci<cons.length;ci++) {
		    cons[ci] = indexedContext.get(ci);
		}
		ce = new ComparableEvent(ocID, cons);
		eventsToCompare.add(ce);
	    }
	    else {
		System.err.println("Dropped event " + ev.getOutcome() + ":" + Arrays.asList(ev.getContext()));
	    }
	    // recycle the TIntArrayList
	    indexedContext.clear();
	}
	outcomeLabels = toIndexedStringArray(omap);
	predLabels = toIndexedStringArray(predicateIndex);
	return eventsToCompare;
    }
    
}