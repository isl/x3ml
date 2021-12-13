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
import java.util.TreeMap;
import java.util.Map;
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
public class UriExistingOrNew implements CustomGenerator {
    private boolean containsPrefix;
    private String uriExisting;
    private Map<String,String> separators=new TreeMap<>();
    private Map<String,String> texts=new TreeMap<>();

    /** Sets the value of the argument with the given value.
     * 
     * @param name the name of the argument (the generator recognizes the "text" argument)
     * @param value the value of the corresponding argument
     * @throws CustomGeneratorException it is thrown if the name of the argument is different 
     * from the expected one ("text") */
    @Override
    public void setArg(String name, String value) throws CustomGeneratorException {
        if(name.equalsIgnoreCase(Labels.URI)){
            this.uriExisting=value;
        }else if(name.startsWith(Labels.TEXT)){
            this.texts.put(name, value);
        }else if(name.startsWith(Labels.URI_SEPARATOR)){
            this.separators.put(name, value);
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
        log.debug("Using UriExistingOrNew Generator with the following settings: ["+
                "Texts: "+this.texts+"\t"+
                "UriSeparators: "+this.separators+"\t"+
                "Contains Namespace Prefix: "+this.containsPrefix+"]");
        if(this.uriExisting==null && texts.isEmpty()){
            throw new CustomGeneratorException("Missing text and uri arguments");
        }
        if(this.texts.size()>1 && this.texts.size()!=this.separators.size()){
            throw new CustomGeneratorException("Some URI separators are missing. (# texts should be equals with # URI separators)");
        }
        if(this.uriExisting!=null && UriValidator.isValid(this.uriExisting)){
            log.debug("Found valid URI: "+this.uriExisting);
            return UriValidator.encodeURI(this.uriExisting).toASCIIString();
        }
        StringBuilder retValueBuilder=new StringBuilder();
        for(int i=0;i<this.texts.size();i++){
            retValueBuilder.append(this.texts.get((Labels.TEXT+(i+1))))
                           .append(this.separators.get((Labels.URI_SEPARATOR+(i+1))));
        }
        return UriValidator.encodeURI(retValueBuilder.toString()).toASCIIString();
    }

    /** Returns the type of the generated value. The generator is responsible for constructing 
     * identifiers, therefore it is expected to return either a URI or a UUID.
     * 
     * @return the type of the generated value (i.e. URI or UUID)
     * @throws CustomGeneratorException if the argument is missing or null */
    @Override
    public String getValueType() throws CustomGeneratorException {
        if(this.texts.isEmpty()){
            throw new CustomGeneratorException("Missing text argument");
        }
//        return UriValidator.isValid(text) ? Labels.URI : Labels.UUID;
        return Labels.URI;
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
        this.containsPrefix=true;
    }
}