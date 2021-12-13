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

import gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy.CustomGeneratorException;
import gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy.CustomGenerator;
import lombok.extern.log4j.Log4j2;

/** The generator is responsible for constructing UUIDs based on the 
 * textual contents of the given text. More specifically it uses the given 
 * text for generating a UUID. If the same text is given twice then the UUID that 
 * has been generated for the first time, will be reused.
 * 
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 */
@Log4j2
public class TextualContent implements CustomGenerator{
    private String text;

    /** Sets the value of the argument with the given value.
     * 
     * @param name the name of the argument (the generator recognizes the arguments: prefix, delimiter, text#)
     * @param value the value of the corresponding argument
     * @throws CustomGeneratorException it is thrown if the name of the argument is different 
     * from the expected ones */
    @Override
    public void setArg(String name, String value) throws CustomGeneratorException {
        if(name.startsWith(Labels.TEXT)){
            log.debug("Setting UUID for text: "+value+"\t (length: "+value.length()+")");
            this.text=java.util.UUID.nameUUIDFromBytes(value.getBytes()).toString();
            log.debug("Created UUID for text: "+value+"\t UUID: "+this.text+")");
        }else{
            throw new CustomGeneratorException("Unrecognized argument name: "+ name);
        }
    }
    
    /** Returns the generated UUID.
     * 
     * @return the value of the given generator
     * @throws CustomGeneratorException if the argument of the generator is missing or null*/
    @Override
    public String getValue() throws CustomGeneratorException {
        if(text.isEmpty()){
            throw new CustomGeneratorException("Missing text arguments");
        }
        return this.text;
    }

    /** Returns the type of the generated value. The type of this generated value is 
     *  always a urn of the following form: (urn:uuid:GENERATED_UUID)
     * 
     * @return the type of the generated value
     * @throws CustomGeneratorException if the argument is missing or null */
    @Override
    public String getValueType() throws CustomGeneratorException {
        return Labels.URI;
    }
    
    @Override
    public void usesNamespacePrefix() {
        log.debug("The "+this.getClass().getName()+" custom generator creates only URIs therefore it does not support injecting prefix");
        ;
    }

    /** Returns a boolean flag (with value set to false) indicating that this 
     * generator supports merging values from similar elements
     * (elements having the same name). 
     * 
     * @return true*/    
    @Override
    public boolean mergeMultipleValues(){
        return false;
    }
}
