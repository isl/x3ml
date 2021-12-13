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

import static gr.forth.ics.isl.x3ml.X3MLEngine.exception;
import gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy.CustomGeneratorException;
import gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy.CustomGenerator;
import java.util.Map;
import java.util.TreeMap;
import lombok.extern.log4j.Log4j2;

/** The generator is responsible for constructing values (either URIs, or literals)
 *  by concatenating multiple elements (that have the same tag name). More specifically 
 * the generator defines that the values of a particular element should be used for 
 * generating the value; they are defined in terms of appropriate XPath expressions 
 * (i.e. ELEMENT_A/text()). If there are more than one such elements then then their values 
 * will be concatenated, using a particular delimeter (which can also be specified by the user).
 * The generator requires the following arguments:
 * <ul><li>prefix: It is the prefix that should be used before the merging of the values.
 * It is defined as a constant and can be either the prefix of a URL, any String value, or empty </li>
 * <li> text#: the text argument followed by a number (i.e. text1). The user can add
 * multiple such arguments, by incrementing the number suffix (i.e. text2, text3, etc.). The number 
 * suffixes indicate also the merging execution row. </li>
 * <li> delimiter: for indicating which is the string that will be used as a delimiter 
 * between the merged values</li></ul>
 * 
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 */
@Log4j2
public class ConcatMultipleTerms implements CustomGenerator{
    private String prefix;
    private String sameTermsDelim;
    private String diffTermsDelim;
    private Map<String,String> text=new TreeMap<>();

    /** Sets the value of the argument with the given value.
     * 
     * @param name the name of the argument (the generator recognizes the arguments: prefix, delimiter, text#)
     * @param value the value of the corresponding argument
     * @throws CustomGeneratorException it is thrown if the name of the argument is different 
     * from the expected ones */
    @Override
    public void setArg(String name, String value) throws CustomGeneratorException {
        if(name.equals(Labels.PREFIX)){
            this.prefix=value;
        }else if(name.startsWith(Labels.SAME_TERM_DELIMITER)){
            this.sameTermsDelim=value;
        }else if(name.startsWith(Labels.DIFF_TERMS_DELIMITER)){
            this.diffTermsDelim=value;
        }else if(name.startsWith(Labels.TEXT)){
            this.text.put(name, value);
        }else{
            throw new CustomGeneratorException("Unrecognized argument name: "+ name);
        }
    }
    
    /** Returns the value of the generator.
     * 
     * @return the value of the given generator
     * @throws CustomGeneratorException if the argument of the generator is missing or null*/
    @Override
    public String getValue() throws CustomGeneratorException {
        if(text.isEmpty()){
            throw new CustomGeneratorException("Missing text arguments");
        }
        String retValue="";
        for(String key : text.keySet()){
            if(!text.get(key).isEmpty()){
                retValue+=text.get(key)+this.diffTermsDelim;
            }
        }if(retValue.isEmpty()){
            throw exception("No value has been generated for custom generator "+this.getClass().getCanonicalName()+" because all values are missing");
        }
        retValue=retValue.substring(0, retValue.length()-this.diffTermsDelim.length());
        retValue=retValue.replaceAll(Labels.SAME_MERGING_DELIMITER, this.sameTermsDelim);
        if(this.getValueType().equals(Labels.URI)){
            return this.prefix+retValue;
        }else{
            return this.prefix+retValue;
        }
    }

    /** Returns the type of the generated value. The generator is responsible for constructing 
     * identifiers, and labels therefore it is expected to return either a URI or a Literal value
     * 
     * @return the type of the generated value (i.e. URI or UUID)
     * @throws CustomGeneratorException if the argument is missing or null */
    @Override
    public String getValueType() throws CustomGeneratorException {
        if(this.prefix!=null && this.prefix.startsWith(Labels.HTTP+":")){
            return Labels.URI;
        }else{
            return Labels.LITERAL;
        }
    }
    
    @Override
    public void usesNamespacePrefix() {
        log.error("The "+this.getClass().getName()+" custom generator does not support injecting prefix yet");
        ;
    }

    /** Returns a boolean flag (with value set to false) indicating that this 
     * generator supports merging values from similar elements
     * (elements having the same name). 
     * 
     * @return true*/    
    @Override
    public boolean mergeMultipleValues(){
        return true;
    }
}
