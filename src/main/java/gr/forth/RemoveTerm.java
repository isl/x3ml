/* 
 * Copyright 2017 marketak.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gr.forth;

import gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy.CustomGeneratorException;
import gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy.CustomGenerator;
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
public class RemoveTerm implements CustomGenerator{
    private boolean containsPrefix;
    private String text;
    private String termToRemove;
    private boolean removeAllOccurrences;

    @Override
    public void setArg(String name, String value) throws CustomGeneratorException {
        if(name.equals(Labels.TERM_TO_REMOVE)){
            this.termToRemove=value;
        }else if(name.equals(Labels.TEXT)){
            this.text=value;
        }else if(name.equals(Labels.REMOVE_ALL_OCCURRENCES)){
            if(value.toLowerCase().equals(Labels.YES) | 
                    value.toLowerCase().equals("y") | 
                    value.toLowerCase().equals(Labels.TRUE) | 
                    value.toLowerCase().equals("T")){
                this.removeAllOccurrences=true;
            }else{
                this.removeAllOccurrences=false;
            }
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
        log.debug("Using RemoveTerm Generator with the following settings: ["+
                "Term to remove: "+this.termToRemove+"\t"+
                "Text: "+this.text+"\t"+
                "Remove all Occurrences: "+this.removeAllOccurrences+"\t"+
                "Contains Namespace Prefix: "+this.containsPrefix+"]");
        if(text.isEmpty()){
            throw new CustomGeneratorException("Missing text arguments");
        }
        else {
            if(removeAllOccurrences){
                return this.text.replaceAll(termToRemove, "");
            }else{
                return this.text.replaceFirst(termToRemove, "");
            }
        }
    }

    /** Returns the type of the generated value. The generator is responsible for constructing 
     * identifiers, and labels therefore it is expected to return either a URI or a Literal value.
     * The method uses the JENA IRI validator, for checking if a URI is valid or not.
     * 
     * @return the type of the generated value (i.e. URI or UUID)
     * @throws CustomGeneratorException if the argument is missing or null */
    @Override
    public String getValueType() throws CustomGeneratorException {
        if(this.containsPrefix){
            log.debug("The return type of \""+this.getValue()+"\" is "+Labels.URI+" (A Namespace prefix has been declared)");
            return Labels.URI;
        }else if(this.getValue()!=null){
            if(UriValidator.isValid(this.getValue())){
                log.debug("The return type of \""+UriValidator.encodeURI(this.getValue())+"\" is "+Labels.URI+" (It is a valid IRI)");
                return Labels.URI;
            }else{
                log.debug("The return type of \""+this.getValue()+"\" is "+Labels.LITERAL+" (There are IRI violations)");
                return Labels.LITERAL;
            }
        }else{
            return Labels.LITERAL;
        }
    }
     
    @Override
    public void usesNamespacePrefix() {
        this.containsPrefix=true;
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
