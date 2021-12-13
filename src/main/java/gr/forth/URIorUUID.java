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

/** The URIorUUID generator is responsible for generating a URI and in the cases 
 * where it is not valid it generates a UUID. It takes a single parameter (with name "text") 
 * which can be given either from the input, or through a constant. If the given value is not 
 * a valid URI or URN, it generates a UUID. 
 * The generator is also applicable for the cases where the corresponding value does not exist. 
 * For example given the input &lt;FOO:gt;&lt;BAR/:gt;&lt;/FOO:gt; if we want to 
 * generate a value using the XPATH expression BAR/text() then in this case the value 
 * does not exist for the given XPATH expression, however this should NOT trigger an error; instead
 * it should generate a UUID value for the given entity (based on the fact that 
 * the source_node exists (i.e. FOO).
 * 
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 */
@Log4j2
public class URIorUUID implements CustomGenerator {
    private String text;

    /** Sets the value of the argument with the given value.
     * 
     * @param name the name of the argument (the generator recognizes the "text" argument)
     * @param value the value of the corresponding argument
     * @throws CustomGeneratorException it is thrown if the name of the argument is different 
     * from the expected one ("text") */
    @Override
    public void setArg(String name, String value) throws CustomGeneratorException {
        if(Labels.TEXT.equals(name)){
            text = value;
        }else{
            throw new CustomGeneratorException("Unrecognized argument name: " + name);
        }
    }

    /** Returns the value of the URIorUUID generator.
     * 
     * @return the value of the given generator
     * @throws CustomGeneratorException if the argument of the generator is missing or null*/
    @Override
    public String getValue() throws CustomGeneratorException {
        if(text == null){
            throw new CustomGeneratorException("Missing text argument");
        }
        return UriValidator.encodeURI(text).toASCIIString();
    }

    /** Returns the type of the generated value. The generator is responsible for constructing 
     * identifiers, therefore it is expected to return either a URI or a UUID.
     * 
     * @return the type of the generated value (i.e. URI or UUID)
     * @throws CustomGeneratorException if the argument is missing or null */
    @Override
    public String getValueType() throws CustomGeneratorException {
        if(text == null){
            throw new CustomGeneratorException("Missing text argument");
        }
        return UriValidator.isValid(text) ? Labels.URI : Labels.UUID;
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
}