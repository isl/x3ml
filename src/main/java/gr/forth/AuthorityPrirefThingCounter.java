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

import static gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy.CustomGenerator;
import static gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy.CustomGeneratorException;
import lombok.extern.log4j.Log4j2;

/**
 * a date interpreter
 * 
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 */
@Log4j2
public class AuthorityPrirefThingCounter implements CustomGenerator {

    private String authority;
    private String priref;
    private static String prirefOld;
    

    private String thing;
    private static String thingOld;

    private static int counterInt = 0;

    @Override
    public void setArg(String name, String value) throws CustomGeneratorException {

        if ("authority".equals(name)) {

            authority = value;
        } else if ("priref".equals(name)) {
            priref = value;
        } else if ("thing".equals(name)) {
            thing = value;
        } else {
            throw new CustomGeneratorException("Unrecognized argument name: " + name);
        }
    }

    @Override
    public String getValue() throws CustomGeneratorException {
        if (authority == null) {
            throw new CustomGeneratorException("Missing text argument");
        }
        if (priref == null) {
            throw new CustomGeneratorException("Missing bounds argument");
        }

        if (thing == null) {
            throw new CustomGeneratorException("Missing text argument");
        }

        return getCounterObject(authority, priref, thing);
    }

    @Override
    public String getValueType() throws CustomGeneratorException {
        return  "URI";
    }
    
    @Override
    public void usesNamespacePrefix() {
        log.debug("The "+this.getClass().getName()+" custom generator creates only URIs therefore it does not support injecting prefix");
        ;
    }

    private String getCounterObject(String authority, String priref, String thing) {
        
        String counterObject = "";

        if (counterInt == 0) {
            setOldValues();
        }

        if (prirefOld.equals(priref) && thingOld.equals(thing)){
                counterInt++;
        } 
        
        else {
            counterInt = 1;
            setOldValues();
        }
        
        counterObject = authority + "/" + priref + "/" + thing + "/" + counterInt;

        return counterObject;

    }

    private void setOldValues() {
        prirefOld = priref;
        thingOld = thing;
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
