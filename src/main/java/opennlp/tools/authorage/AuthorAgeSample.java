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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * TODO: Documentation
 */
public class AuthorAgeSample {
    private final String ageCategory;
    
    private final List<String> text;    
    
    public AuthorAgeSample(Integer age, String text[]) {
	if (age == null) {
	    throw new IllegalArgumentException("Age cannot be null");
	}
	if (text == null) {
	    throw new IllegalArgumentException("Text cannot be null");
	}
	
	if (age < 18) {
	    this.ageCategory = "<18";
	}
	else if (age >= 18 && age <= 27) {
	    this.ageCategory = "18-27";
	}
	else if (age >= 28 && age <= 37) {
	    this.ageCategory = "28-37";
	}
	else { // (age > 37)
	    this.ageCategory = ">37";
	}
	
	// Will use when more data from older age groups are obtained
	/* 
	if (age < 18) {
	    ageCategory = "<18";
	}
	else if (age >= 18 && age <= 24) {
	    ageCategory = "18-24";
	}
	else if (age >= 25 && age <= 30) {
	    ageCategory = "25-30";
	}
	else if (age >= 30 && age <= 37) {
	    ageCategory = "30-37";
	}
	else if (age >= 38 && age <= 45) {
	    ageCategory = "38-45";
	}
	else if (age >= 45 && age <= 55) {
	    ageCategory = "45-55";
	}
	else if (age >= 56 && age <= 65) {
	    ageCategory = "56-65";
	}
	else { // (age > 65)
	    ageCategory = ">65";
	}
	*/
	
	this.text = Collections
	    .unmodifiableList(new ArrayList<String>(Arrays.asList(text)));
    }
    
    public String getCategory() {
	return this.ageCategory;
    }

    public String[] getText() {
	return this.text.toArray(new String[this.text.size()]);
    }
    
}
