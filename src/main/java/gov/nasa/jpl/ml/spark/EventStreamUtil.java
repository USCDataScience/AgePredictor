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

import java.util.Collection;
import java.util.Iterator;

import opennlp.tools.ml.model.Event;
import opennlp.tools.util.ObjectStream;
    
import org.apache.spark.api.java.JavaRDD;

public class EventStreamUtil {
    
    public static ObjectStream<Event> createEventStream(final Collection<EventWrapper> samples) {
	
	return new ObjectStream<Event>() {    
	    private Iterator<EventWrapper> iterator = samples.iterator();
	    
	    public Event read() {
		if (iterator.hasNext()) {
		    return iterator.next().getEvent();
		}
		else {
		    return null;
		}
	    }
	        
	    public void reset() {
		iterator = samples.iterator();
	    }
	        
	    public void close() {
	    }
	        
	};    
    }

    public static ObjectStream<Event> createEventStream(final JavaRDD<EventWrapper> samples) {
	
	return new ObjectStream<Event>() {    
	    private Iterator<EventWrapper> iterator = samples.toLocalIterator();
	    
	    public Event read() {
		if (iterator.hasNext()) {
		    return iterator.next().getEvent();
		}
		else {
		    return null;
		}
	    }
	        
	    public void reset() {
		iterator = samples.toLocalIterator();
	    }
	        
	    public void close() {
	    }
	        
	};    
    }
}