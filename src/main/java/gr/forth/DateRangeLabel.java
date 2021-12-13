/*==============================================================================
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
==============================================================================*/
package gr.forth;

import gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy.CustomGenerator;
import gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy.CustomGeneratorException;
import lombok.extern.log4j.Log4j2;

/**
 * 
 */
@Log4j2
public class DateRangeLabel implements CustomGenerator {

    private String date1 = "";
    private String date2 = "";

    @Override
    public void setArg(String name, String value) throws CustomGeneratorException {
        if ("date1".equals(name)) {
            date1 = value;
        } else if ("date2".equals(name)) {
            date2 = value;
        } else {
            throw new CustomGeneratorException("Unrecognized argument name: " + name);
        }
    }

    @Override
    public String getValue() throws CustomGeneratorException {
        if (date1.equalsIgnoreCase(date2)) {
        	return date1;
        }
        if (!date1.isEmpty() && !date2.isEmpty()) {

        	return (date1 + " - " + date2);
        }
        if (date1.isEmpty()) {
        	return date2;
        } else {
        	return date1;
        }
    }

    @Override
    public String getValueType() throws CustomGeneratorException {
        return "Literal";
    }      
    
    @Override
    public void usesNamespacePrefix() {
        log.debug("The "+this.getClass().getName()+" custom generator creates only Literals therefore it does not support injecting prefix");
        ;
    }
    
    /** Returns a boolean flag (with value set to false) indicating that this 
     * generator DOES NOT support merging values from similar elements
     * (elements having the same name). 
     * 
     * @return false*/
    @Override
    public boolean mergeMultipleValues(){
        return false;
    }
}