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

import gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** This generator is responsible for constructing URIs that contain UUIDs for some of their arguments 
 * that will be either (a) the result of the hashing over the contents of a particular argument or (b) 
 * a random UUID of a particular argument. It is up to the user to define the desired functionality over every argument 
 * by encoding the functionality in the argument name. More specifically: 
 * (a) if an argument name ends with _HASHED_CONTENTS then a UUID will be generated based on the contents of the argument
 * (b) if an argument name ends with _RANDOM_UUID then a random UUID will be generated ignoring the contents of this argument
 * (c) if an argument name does not end with any of the above then the contents of the argument will be added as they are. 
 * 
 * The separator between different arguments is / character
 * 
 * @author Yannis Marketakis (marketak 'at' ics 'dot' forth 'dot' gr)
 */
public class MultiHashingGenerator implements X3MLGeneratorPolicy.CustomGenerator{
    List<String> evaluatedArguments=new ArrayList<>();
    
    /** Arguments are evaluated in order of definition (as they appear in generator-policy file)
     * 
     * @param name the name of the argument
     * @param value the original value of the agument
     * @throws gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy.CustomGeneratorException for any that might occur */
    @Override
    public void setArg(String name, String value) throws X3MLGeneratorPolicy.CustomGeneratorException {
        if(name.endsWith(Labels._HASHED_CONTENTS)){
            evaluatedArguments.add(UUID.nameUUIDFromBytes(value.getBytes()).toString().toUpperCase());
        }else if(name.endsWith(Labels._RANDOM_UUID)){
            evaluatedArguments.add(UUID.randomUUID().toString().toUpperCase());
        }else{
            evaluatedArguments.add(value);
        }
    }

    @Override
    public void usesNamespacePrefix() {
        ;
    }

    @Override
    public String getValue() throws X3MLGeneratorPolicy.CustomGeneratorException {
        StringBuilder valueBuilder=new StringBuilder();
        for(String value : evaluatedArguments){
            valueBuilder.append(value).append("/");
        }
        return valueBuilder.deleteCharAt(valueBuilder.length()-1).toString();
    }

    @Override
    public String getValueType() throws X3MLGeneratorPolicy.CustomGeneratorException {
        return Labels.URI;
    }

    @Override
    public boolean mergeMultipleValues() {
        return false;
    }
}
