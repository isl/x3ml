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
import java.util.Date;
import lombok.extern.log4j.Log4j2;

/**
 * an date interpreter
 * 
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 */
@Log4j2
public class GermanDate implements CustomGenerator {

    private String text;
    private Bounds bounds;

    enum Bounds {
        Upper, Lower
    }

    @Override
    public void setArg(String name, String value) throws CustomGeneratorException {
        if ("text".equals(name)) {
            text = value;
        } else if ("bound".equals(name)) {
            bounds = Bounds.valueOf(value);
        } else {
            throw new CustomGeneratorException("Unrecognized argument name: " + name);
        }
    }

    @Override
    public String getValue() throws CustomGeneratorException {
        if (text == null) {
            throw new CustomGeneratorException("Missing text argument");
        }
        if (bounds == null) {
            throw new CustomGeneratorException("Missing bounds argument");
        }
        return getFormatedDate(bounds.toString(), text);
    }

    @Override
    public String getValueType() throws CustomGeneratorException {
        return text.startsWith("http") ? "URI" : "Literal";
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
    
    @Override
    public void usesNamespacePrefix() {
        log.error("The "+this.getClass().getName()+" custom generator does not support injecting prefix yet");
        ;
    }

    private static String getFormatedDate(String bounds, String time_str) {
        String xsdDate = "";
        try {
            Date formatDate = UtilsTime.validate(time_str, bounds);
            if (formatDate != null) {
                xsdDate = UtilsTime.convertStringoXSDString(formatDate, true);
            } else {
                xsdDate = "Unknown-Format";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xsdDate;  
    }
}