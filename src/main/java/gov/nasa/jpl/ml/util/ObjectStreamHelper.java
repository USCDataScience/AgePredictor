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

package gov.nasa.jpl.ml.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import opennlp.tools.util.ObjectStream;

/**
 * TODO: Documentation
 */
public class ObjectStreamHelper {
    
    public static <T> ObjectStream<T> createObjectStream(final Collection<ObjectStream<T>> streams) {
	for (ObjectStream<T> stream : streams) {
	    if (stream == null) {
		throw new NullPointerException("Stream cannot be null");
	    }
	}
	
	return new ObjectStream<T>() {

	    // private int streamIndex = 0;
	    private Iterator<ObjectStream<T>> iterator = streams.iterator();
	
	    public T read() throws IOException {
		T object = null;
		
		while (iterator.hasNext() && object == null) {
		    object = iterator.next().read();
		    
		}
		
		/*
		while (streamIndex < streams.length && object == null) {
		    object = streams[streamIndex].read();

		    if (object == null)
			streamIndex++;
		}
		*/

		return object;
	    }

	    public void reset() throws IOException, UnsupportedOperationException {
		// streamIndex = 0;
		iterator = streams.iterator();
		
		for (ObjectStream<T> stream : streams) {
		    stream.reset();
		}
	    }

	    public void close() throws IOException {

		for (ObjectStream<T> stream : streams) {
		    stream.close();
		}
	    }};
    }
    
}