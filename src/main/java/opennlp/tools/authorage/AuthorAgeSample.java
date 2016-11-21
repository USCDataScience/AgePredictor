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

package opennlp.tools.authorage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * TODO: Documentation
 */
public class AuthorAgeSample implements Serializable{
	private static final long serialVersionUID = 5374409234122582256L;

	private final String ageCategory;
    
    private final List<String> text;    
    
    public AuthorAgeSample(String category, String text[]) {
	if (category == null) {
	    throw new IllegalArgumentException("Age cannot be null");
	}
	if (text == null) {
	    throw new IllegalArgumentException("Text cannot be null");
	}
	
	this.ageCategory = category;
	
	this.text = Collections
	    .unmodifiableList(new ArrayList<String>(Arrays.asList(text)));
    }


    public AuthorAgeSample(Integer age, String text[]) {
	if (age == null) {
	    throw new IllegalArgumentException("Age cannot be null");
	}
	if (text == null) {
	    throw new IllegalArgumentException("Text cannot be null");
	}
	
	if (age < 18) {
	    ageCategory = "xx-18";
	}
	else if (age >= 18 && age <= 24) {
	    ageCategory = "18-24";
	}
	else if (age >= 25 && age <= 34) {
	    ageCategory = "25-34";
	}
	else if (age >= 35 && age <= 49) {
	    ageCategory = "35-49";
	}
	else if (age >= 50 && age <= 64) {
	    ageCategory = "50-64";
	}
	else { // (age > 65)
	    ageCategory = "65-xx";
	}
	
	this.text = Collections
	    .unmodifiableList(new ArrayList<String>(Arrays.asList(text)));
    }
    
    public String getCategory() {
	return this.ageCategory;
    }

    public String[] getText() {
	return this.text.toArray(new String[this.text.size()]);
    }

    @Override
    public String toString() {
	StringBuilder sampleString = new StringBuilder();

	sampleString.append(ageCategory).append('\t');

	for (String s : text) {
	    sampleString.append(s).append(' ');
	}

	if (sampleString.length() > 0) {
	    // remove last space
	    sampleString.setLength(sampleString.length() - 1);
	}

	return sampleString.toString();
    }
    
    
}
