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
package gr.forth.ics.isl.x3ml.engine;

import gr.forth.ics.isl.x3ml.X3MLEngine;
import org.w3c.dom.Node;
import static gr.forth.ics.isl.x3ml.engine.X3ML.ArgValue;
import static gr.forth.ics.isl.x3ml.engine.X3ML.Condition;
import static gr.forth.ics.isl.x3ml.engine.X3ML.GeneratedValue;
import static gr.forth.ics.isl.x3ml.engine.X3ML.GeneratorElement;
import static gr.forth.ics.isl.x3ml.engine.X3ML.SourceType;
import gr.forth.ics.isl.x3ml_reverse_utils.AssociationTable;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.w3c.dom.Attr;
import static gr.forth.ics.isl.x3ml.X3MLEngine.exception;
import gr.forth.Labels;
import lombok.extern.log4j.Log4j2;
import static org.joox.JOOX.$;

/**
 * This abstract class is above Domain, Path, and Range and carries most of
 * their contextual information.
 *
 * @author Gerald de Jong &lt;gerald@delving.eu&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 */
@Log4j2
public abstract class GeneratorContext {

    public final Root.Context context;
    public final GeneratorContext parent;
    public final Node node;
    public final int index;
    
    protected GeneratorContext(Root.Context context, GeneratorContext parent, Node node, int index) {
        this.context = context;
        this.parent = parent;
        this.node = node;
        this.index = index;
    }

    /**Retrieves the value that has been generated for the given variable in the given scope.
     * The scope can be either within the same mapping (WITHIN_MAPPING), or global (GLOBAL).
     * 
     * @param variable the name of the variable
     * @param scope the scope (either WITHIN_MAPPING or GLOBAL)
     * @return the generated value for the given variable */
    public GeneratedValue get(String variable, VariableScope scope) {
        if (parent == null) {
            throw exception("Parent context missing");
        }
        return parent.get(variable, scope);
    }

    /**Stores the value that has been generated for the given variable in the given scope.
     * The scope can be either within the same mapping (WITHIN_MAPPING), or global (GLOBAL).
     * 
     * @param variable the name of the variable
     * @param scope the scope (either WITHIN_MAPPING or GLOBAL) 
     * @param generatedValue the generated value */
    public void put(String variable, VariableScope scope, GeneratedValue generatedValue) {
        if (parent == null) {
            throw exception("Parent context missing");
        }
        parent.put(variable, scope, generatedValue);
    }

    public String evaluate(String expression) {
        return context.input().valueAt(node, expression);
    }
    
     public boolean evaluate2(String expression) {
        return context.input().existingNode(node, expression);
    }

    /** Creates or retrieves a value for the node of the input (which is part of this instance). 
     * To this end it uses the given generator and variables declaration. The procedure is the following:
     * - If a global variable has been declared for the entity then it searches if a value has already been 
     * generated for this global variable. If not it generates the value.
     * - If a type-aware variable has been declared for the entity then it searches if a value has already been 
     * generated for this global variable. If not it generates the value.
     * - If a variable has been declared for the entity then it searches if a value has already been 
     * generated for this global variable. If not it generates the value.
     * - It generates (or retrieves) the value for the particular node
     * 
     * @param generator the declared generator element
     * @param globalVariable the name of the declared global variable
     * @param variable_deprecated the name of the declared variable
     * @param variable the name of the type-aware variable
     * @param unique a unique value (usually the type of additional/intermediates) for creating always new instances
     * @return the value that has been generated (either now, or previously )  */
    public GeneratedValue getInstance(final GeneratorElement generator, String globalVariable, String variable_deprecated, String variable, String unique) {
        log.debug("Generate instance/value. Generator Details: "+generator.getName()+", Args"+generator.getArgs());
        log.debug("Generate instance/value. Variable: "+variable);
        log.debug("Generate instance/value. Global Variable: "+globalVariable);
        log.debug("Generate instance/value. Deprecated Variable: "+variable_deprecated);
        log.debug("Generate instance/value. Unique Value: "+unique);
        if(generator == null){
            throw exception("Value generator missing");
        }
        GeneratedValue generatedValue;
        if(globalVariable != null){
            generatedValue = get(globalVariable, VariableScope.GLOBAL);
            if (generatedValue == null) {
                generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                    @Override
                    public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                        try{
                            return context.input().evaluateArgument(node, index, generator, name, sourceType, mergeMultipleValues);
                        }catch(Exception ex){   
                            /*We are doing this for the cases where the XPATH expression does not hold (i.e. 
                            the elemennt is missing or is empty). In this case we should construct a UUID instead 
                            of simply throwing an error message. Related issue: #72 */
                            if(generator.getName().equals(Labels.URIorUUID)){
                                return new ArgValue("X", "en"); 
                            }else{
                                throw exception(ex.getMessage(),ex);
                            } 
                        }
                    }
                });
                put(globalVariable, VariableScope.GLOBAL, generatedValue);
            }
        }
        else if(variable != null){
            if(variable_deprecated!=null){
                generatedValue = get(variable_deprecated, VariableScope.WITHIN_MAPPING);
                if (generatedValue == null) {
                    generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                        @Override
                        public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                            try{
                                return context.input().evaluateArgument(node, index, generator, name, sourceType, mergeMultipleValues);
                            }catch(Exception ex){   
                                /*We are doing this for the cases where the XPATH expression does not hold (i.e. 
                                the elemennt is missing or is empty). In this case we should construct a UUID instead 
                                of simply throwing an error message. Related issue: #72 */
                                if(generator.getName().equals(Labels.URIorUUID)){
                                    return new ArgValue("X", "en"); 
                                }else{
                                    throw exception(ex.getMessage(),ex);
                                } 
                            }
                        }
                    });
                    put(variable_deprecated,VariableScope.WITHIN_MAPPING, generatedValue);
                    context.putGeneratedValue(extractXPath(node) + unique+"-"+variable, generatedValue);
                    if(X3MLEngine.ENABLE_ASSOCIATION_TABLE){
                        this.createAssociationTable(generatedValue, null, extractAssocTableXPath(node));
                    }
                }
            }else{
//                String nodeName = extractXPath(node) + unique+"-"+typeAwareVar;
                String nodeName = extractXPath(Domain.domainNode) + unique+"-"+variable;
                generatedValue = context.getGeneratedValue(nodeName);
                log.debug("Retrieved Generated Value: "+nodeName+"\t"+generatedValue);
                if (generatedValue == null) {
                    generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                        @Override
                        public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                            try{
                                return context.input().evaluateArgument(node, index, generator, name, sourceType, mergeMultipleValues);
                            }catch(Exception ex){   
                                /*We are doing this for the cases where the XPATH expression does not hold (i.e. 
                                the elemennt is missing or is empty). In this case we should construct a UUID instead 
                                of simply throwing an error message. Related issue: #72 */
                                if(generator.getName().equals(Labels.URIorUUID)){
                                    return new ArgValue("X", "en"); 
                                }else{
                                    throw exception(ex.getMessage(),ex);
                                } 
                            }
                        }
                    });
                    GeneratedValue genArg=null;
                    if(generator.getName().equalsIgnoreCase("Literal")){
                        genArg = context.policy().generate(generator, new Generator.ArgValues() {
                            @Override
                            public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                                return context.input().evaluateArgument2(node, index, generator, name, sourceType);

                            }
                        });
                    }
                    log.debug("put generated value: "+nodeName+"\t"+generatedValue);
                    context.putGeneratedValue(nodeName, generatedValue);
                    if(X3MLEngine.ENABLE_ASSOCIATION_TABLE){
                        this.createAssociationTable(generatedValue, genArg, extractAssocTableXPath(node));
                    }
                }
            }
        }
        else{
            if(variable_deprecated != null){
                generatedValue = get(variable_deprecated, VariableScope.WITHIN_MAPPING);
                if (generatedValue == null) {
                    generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                        @Override
                        public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                            return context.input().evaluateArgument(node, index, generator, name, sourceType, mergeMultipleValues);
                        }
                    });
                    /* After generating the value for the entity that has a variable associated with it, 
                    we have to also add the generated value (so that the value can be re-used when the same 
                    input is exploited). Related issue= #66 */
                    put(variable_deprecated, VariableScope.WITHIN_MAPPING, generatedValue);
                    String nodeName = extractXPath(node) + unique;
                    context.putGeneratedValue(nodeName, generatedValue);
                }
            }
            else{
                String nodeName = extractXPath(node) + unique;
                generatedValue = context.getGeneratedValue(nodeName);
                if (generatedValue == null) {
                    generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                        @Override
                        public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                            try{
                                return context.input().evaluateArgument(node, index, generator, name, sourceType, mergeMultipleValues);
                            }catch(Exception ex){   
                                /*We are doing this for the cases where the XPATH expression does not hold (i.e. 
                                the elemennt is missing or is empty). In this case we should construct a UUID instead 
                                of simply throwing an error message. Related issue: #72 */
                                if(generator.getName().equals(Labels.URIorUUID)){
                                    return new ArgValue("X", "en"); 
                                }else{
                                    throw exception(ex.getMessage(),ex);
                                } 
                            }
                        }
                    });
                    GeneratedValue genArg=null;
                    if(generator.getName().equalsIgnoreCase("Literal")){
                        genArg = context.policy().generate(generator, new Generator.ArgValues() {
                            @Override
                            public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                                return context.input().evaluateArgument2(node, index, generator, name, sourceType);

                            }
                        });
                    }
                    context.putGeneratedValue(nodeName, generatedValue);
                    if(X3MLEngine.ENABLE_ASSOCIATION_TABLE){
                        this.createAssociationTable(generatedValue, genArg, extractAssocTableXPath(node));
                    }
                }
            }
        }
        if (generatedValue == null) {
            throw exception("Empty value produced");
        }
        return generatedValue;
    }
    
    /** This method is responsible for reusing the value generated for the domain node (when the MERGE facility is 
     * being used). When the MERGE case is being used inside a link, instead of creating a range node, 
     * it merges it with the node found in domain. 
     * Under the hood, it associates the node found in the range, with the node found in the domain, 
     * so they share the same identifier.
     * 
     * @param generator the declared generator element
     * @param unique a unique value (usually the type of additional/intermediates) for creating always new instances
     * @param domainNode the node found in the domain
     * @return the value that has been generated for the domain */
    public GeneratedValue getInstance(final GeneratorElement generator, String unique, Node domainNode) {
        if(generator == null){
            throw exception("Value generator missing");
        }
        GeneratedValue generatedValue;

        String nodeName = extractXPath(node) + unique;
        String domainNodeName = extractXPath(domainNode) + unique;
        generatedValue = context.getGeneratedValue(domainNodeName);
        
        if (generatedValue == null) {
            generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                @Override
                public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                    try{
                        return context.input().evaluateArgument(node, index, generator, name, sourceType, mergeMultipleValues);
                    }catch(Exception ex){   
                        /*We are doing this for the cases where the XPATH expression does not hold (i.e. 
                        the elemennt is missing or is empty). In this case we should construct a UUID instead 
                        of simply throwing an error message. Related issue: #72 */
                        if(generator.getName().equals(Labels.URIorUUID)){
                            return new ArgValue("X", "en"); 
                        }else{
                            throw exception(ex.getMessage(),ex);
                        } 
                    }
                }
            });
            GeneratedValue genArg=null;
            if(generator.getName().equalsIgnoreCase("Literal")){
                genArg = context.policy().generate(generator, new Generator.ArgValues() {
                    @Override
                    public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                        return context.input().evaluateArgument2(node, index, generator, name, sourceType);

                    }
                });
            }
        }
            
        context.putGeneratedValue(nodeName, generatedValue);
        if (generatedValue == null) {
            throw exception("Empty value produced");
        }
        return generatedValue;
    }

    public boolean conditionFails(Condition condition, GeneratorContext context) {
        return condition != null && condition.failure(context);
    }

    private void createAssociationTable(GeneratedValue generatedValue, GeneratedValue generatedArg, String xpathProper){
        String value="";
        if(generatedValue.type == X3ML.GeneratedType.LITERAL){
            value="\""+generatedValue.text+"\"";
            
            if(generatedArg!=null)
                xpathProper+="/"+generatedArg.text;
            else
                xpathProper+="/text()";
        }
        else if(generatedValue.type == X3ML.GeneratedType.URI)
            value=generatedValue.text;
            if(xpathProper!=null){  //Needs a little more inspection this
                AssociationTable.addEntry(xpathProper,value);
            }
    }
    
    /**Adds a new entry in the association table with the given XPATH expression and 
     * the given key (It is used in the case of joins).
     * 
     * @param xpathEpxr the XPATH expression from one of the tables that are joined
     * @param key the key that is being used for the join */
    public static void appendAssociationTable(String xpathEpxr, String key){
        xpathEpxr=xpathEpxr.replace("///", "/").replaceAll("//", "/");
        AssociationTable.addEntry(xpathEpxr,key);
    }
    
    /** Exports the contents of the association table in XML format.
     * The name of the file is defined from the given parameter.
     * 
     * @param filename the filename where the association table contents will be exported
     * @throws IOException if any error occurs during the exporting.*/
    public static void exportAssociationTable(String filename) throws IOException{
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
        writer.append(AssociationTable.exportAll());
        writer.flush();
        writer.close();
    }
    
    /** Exports the contents of the association table in XML format, and returns their 
     * String representation.
     * 
     * @return a string representation of the association table in XML format.*/
    public static String exportAssociationTableToString(){
        return AssociationTable.exportAll();
    }
    
    public String toString() {
        return extractXPath(node);
    }

    public String toStringAssoc() {
        return extractAssocTableXPath(node);
    }
        
    public static String extractXPath(Node node) {
        if (node == null || node.getNodeType() == Node.DOCUMENT_NODE) {
            return "/";
        } else {
            String soFar="";
            int sibNumber = 0;
            if(node.getNodeType()==Node.ATTRIBUTE_NODE){
                soFar= extractXPath(((Attr)node).getOwnerElement());
            }else{
                soFar = extractXPath(node.getParentNode());

                Node sib = node;
                while (sib.getPreviousSibling() != null) {
                    sib = sib.getPreviousSibling();
                    if (sib.getNodeType() == Node.ELEMENT_NODE) {
                        sibNumber++;
                    }
                }
            }
            return String.format(
                    "%s%s[%d]/",
                    soFar, node.getNodeName(), sibNumber
            );
        }
    }
    
    public static String extractAssocTableXPath(Node node) {
        return $(node).xpath();
    }
}
