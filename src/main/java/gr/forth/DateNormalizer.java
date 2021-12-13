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

/** The generator is responsible for generating a new timestamp value based on 
 * the given date. It uses a date (i.e. 15/01/1984) and a parameter indicating whether
 * the timestamp should be in the end of the day, or its beginning. The data is 
 * given using the parameter "text" and supports many different formats for dates (e.g.
 * 15/01/1984 or 15 Jan 1984, etc.); for more information check the corresponding method in the 
 * class UtilsTime. The other parameter has the name and takes the values "upper" and 
 * "lower", indicating whether the timestamp should be in the end of the given date, or
 * in the beginning correspondingly.
 *
 * @author Konstantina Konsolaki &lt;konsolak@ics.forth.gr&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 */
@Log4j2
public class DateNormalizer implements CustomGenerator {
    private String text;
    private Bounds bounds;
    private TimeDateLevel report;

    enum Bounds {
        Upper, Lower
    }
    
    enum TimeDateLevel {
        Date, 
        Date_and_Time
    }

    /** Sets the value of the argument with the given value.
     * 
     * @param name the name of the argument (the generator recognizes the arguments "text" and "bound")
     * @param value the value of the corresponding argument
     * @throws CustomGeneratorException it is thrown if the name of the argument is different 
     * from the expected one ("text" or "bound") */
    @Override
    public void setArg(String name, String value) throws CustomGeneratorException {
        switch(name){
            case Labels.TEXT:
                text = value;    
                break;
            case Labels.BOUND:
                bounds = Bounds.valueOf(value);    
                break;
            case Labels.REPORT:
                report=TimeDateLevel.valueOf(value);
                break;
            default:
                throw new CustomGeneratorException("Unrecognized argument name: " + name);
        }
    }

    /** Returns the value of the generated BMDate value 
     * 
     * @return the generated value 
     * @throws CustomGeneratorException is thrown if any of the required arguments is missing 
     * or is null */
    @Override
    public String getValue() throws CustomGeneratorException {
        if(text == null){
            throw new CustomGeneratorException("Missing \""+Labels.TEXT+"\" argument");
        }
        if(bounds == null){
            throw new CustomGeneratorException("Missing \""+Labels.BOUND+"\" argument");
        }
        return getFormatedDate(bounds.toString(), text, report);
    }

    /** Returns the type of the generated value (It can be either a URI or a Literal value)
     * 
     * @return the type of the generated value 
     * @throws CustomGeneratorException is thrown if any of the given types is missing or is null */
    @Override
    public String getValueType() throws CustomGeneratorException {
        return text.startsWith("http") ? Labels.URI : Labels.LITERAL;
    }

    /* Converts the date and returns the string representation of the date and the time */
    private static String getFormatedDate(String bounds, String time_str, TimeDateLevel reportLevel) {
        String xsdDate = "";
            Date formatDate = UtilsTime.validate(time_str, bounds);
            if (formatDate != null) {
                xsdDate = UtilsTime.convertStringoXSDString(formatDate, reportLevel==TimeDateLevel.Date_and_Time);
            } else {
                xsdDate = "Unknown-Format";
            }
        return xsdDate;
    }
    
    @Override
    public void usesNamespacePrefix() {
        log.error("The "+this.getClass().getName()+" custom generator does not support injecting prefix yet");
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