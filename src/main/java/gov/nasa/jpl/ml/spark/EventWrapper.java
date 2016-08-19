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

import java.io.Serializable;

import opennlp.tools.ml.model.Event;

public class EventWrapper implements Serializable {
    private Integer value;
    private String outcome;
    private String[] context;
    
    public EventWrapper(Integer outcome, String[] context) {
	this.value = outcome;
	
	if (outcome < 18) {
	    this.outcome = "xx-18";
	}
	else if (outcome >= 18 && outcome <= 24) {
	    this.outcome = "18-24";
	}
	else if (outcome >= 25 && outcome <= 34) {
	    this.outcome = "25-34";
	}
	else if (outcome >= 35 && outcome <= 49) {
	    this.outcome = "35-49";
	}
	else if (outcome >= 50 && outcome <= 64) {
	    this.outcome = "50-64";
	}
	else { // (outcome > 65)
	    this.outcome = "65-xx";
	}
	
	this.context = context;
    }
    
    public EventWrapper(String outcome, String[] context) {
	this.outcome = outcome;
	this.context = context;
	this.value = null;
    }
    
    public Integer getValue() {
	return value;
    }
    
    public String getOutcome() {
	return outcome;
    }
    
    public String[] getContext() {
	return context;
    }
    
    public Event getEvent() {
	return new Event(outcome, context);
    }
    

    public String toString() {
	StringBuilder sb = new StringBuilder();
	if (value != null) {
	    sb.append(value).append(",");
	} else {
	    sb.append("-1").append(",");
	}
	sb.append(outcome).append(",");
	
	if (context.length > 0) {
	    sb.append(context[0]);
	}
	for (int ci = 1; ci < context.length; ci++) {
	    sb.append(" ").append(context[ci]);
	}
	return sb.toString();
    }
}

