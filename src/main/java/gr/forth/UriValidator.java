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

import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.util.UriComponentsBuilder;

/**The class contains the necessary resources that are required for validating and 
 * encoding a URI.
 * 
 * @author Yannis Marketakis (marketak 'at' ics 'dot' forth 'dot' gr)
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 */
@Log4j2
public class UriValidator {
    
    /** The method is responsible for validating a URI.
     * The method first encodes the URI and then validates it.
     * 
     * @param uriString the URI string to be validated
     * @return true if the URI is valid, otherwise false */
    public static boolean isValid(String uriString){
        try{
            return UriValidator.encodeURI(uriString).isAbsolute();
        }catch(IllegalStateException ex){
            log.debug("Invalid URI found",ex);
            return false;
        }
    }
    
    /** The method encodes the given URI by replacing any special characters that 
     * cannot be used as part of the URI (i.e. whitespace, '&gt;', '|', etc.).
     * 
     * @param uriString the URI string that will be encoded
     * @return the encoded URI string */
    public static URI encodeURI(String uriString){
        try{
            log.debug("Checking it the given value is a valid URI: '"+uriString+"'");
            if(new URI(uriString).isAbsolute()){
                log.debug("The URI ('"+uriString+"') is OK");
                return new URI(uriString);
            }
        }catch(URISyntaxException ex){
            log.debug("The given URI string ('"+uriString+"')is not a valid URI");
        }
        return UriComponentsBuilder.fromUriString(uriString).build().encode().toUri();
    }
}