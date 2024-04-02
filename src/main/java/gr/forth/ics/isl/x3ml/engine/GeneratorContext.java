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
import gr.forth.ics.isl.x3ml.engine.X3ML.GeneratedType;
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
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        log.debug("Generate instance/value. Generator Details: {} Args {}", generator.getName(), generator.getArgs());
        log.debug("Generate instance/value. Variable: {}", variable);
        log.debug("Generate instance/value. Global Variable: {}", globalVariable);
        log.debug("Generate instance/value. Deprecated Variable: {}", variable_deprecated);
        log.debug("Generate instance/value. Unique Value: {}", unique);
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
                    this.createAssociationTable(generatedValue, generator, node);
                }
            }else{
//                String nodeName = extractXPath(node) + unique+"-"+typeAwareVar;
                String nodeName = extractXPath(Domain.domainNode) + unique+"-"+variable;
                generatedValue = context.getGeneratedValue(nodeName);
                log.debug("Retrieved Generated Value: {}\t{}", nodeName, generatedValue);
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
                    log.debug("put generated value: {}\t{}", nodeName, generatedValue);
                    context.putGeneratedValue(nodeName, generatedValue);
                    this.createAssociationTable(generatedValue, generator, node);
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
                    context.putGeneratedValue(nodeName, generatedValue);
                    this.createAssociationTable(generatedValue, generator, node);
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

    private void createAssociationTable(GeneratedValue generatedValue, GeneratorElement generator, Node node){
        if(X3MLEngine.ENABLE_ASSOCIATION_TABLE) {
            String xpathProper = extractAssocTableXPath(node);

            String value="";
            if(generatedValue.type == GeneratedType.LITERAL || generatedValue.type == GeneratedType.TYPED_LITERAL) {
                // we assume that there is argument named text for generators that generate Literal or Typed Literals
                // and that this argument is of type xpath
                String generatedArg = 
                    generator.getArgs()
                        .stream()
                        .filter(arg -> SourceType.xpath.name().equals(arg.type))
                        .findFirst()
                        .map(arg -> this.rewriteArgXPath(arg.value))
                        .orElse(null);

                value="\""+generatedValue.text+"\"";
                if(generatedArg != null)
                    xpathProper+="/"+generatedArg;
                else
                    xpathProper+="/text()";
            }
            else if(generatedValue.type == X3ML.GeneratedType.URI) {
                value=generatedValue.text;
            }
            
            if(xpathProper!=null){  //Needs a little more inspection this
                AssociationTable.addEntry(xpathProper,value);
            }
        }
    }

    private final Pattern NUMERIC_INDEX_PATTERN = Pattern.compile(".*\\[\\d+\\]$");
    private final Pattern FUNCTION_PATTERN = Pattern.compile(".*\\(.*\\)$");

    /**
     * In case of multiple intermediary elements we re-write xpath to always point to the first one
     * because this is a default behaviour of non merging generators
     */
    public String rewriteArgXPath(String xpath) {
        String[] segments = xpath.split("/");

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];

             // Check if segment is not a function call, not a relative path, 
             // and does not already contain indexed access
            if (!segment.isEmpty() && !segment.equals(".") && !segment.equals("..") 
                && !NUMERIC_INDEX_PATTERN.matcher(segment).matches() 
                && !FUNCTION_PATTERN.matcher(segment).matches()) {
                segments[i] = segment + "[1]";
            }
        }

        return Arrays.stream(segments).collect(Collectors.joining("/"));
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
        
    /**
     * Extracts the XPath expression of the given node.
     */
    private static final HashMap<Node, String> xpathCache = new HashMap<>();
    public static String extractXPath(Node node) {
        if (node == null || node.getNodeType() == Node.DOCUMENT_NODE) {
            return "";
        }

        if (xpathCache.containsKey(node)) {
            System.out.println("Cache hit xpath:");
            return xpathCache.get(node);
        }

        Deque<Node> stack = new ArrayDeque<>();
        while (node != null && node.getNodeType() != Node.DOCUMENT_NODE) {
            stack.push(node);
            node = (node.getNodeType() == Node.ATTRIBUTE_NODE) ? ((Attr) node).getOwnerElement() : node.getParentNode();
        }

        StringBuilder xpathBuilder = new StringBuilder();
        while (!stack.isEmpty()) {
            Node current = stack.pop();
            if (current.getParentNode() != null || current.getNodeType() == Node.ATTRIBUTE_NODE) { // Skip the #document node
                xpathBuilder.append('/');
                xpathBuilder.append(current.getNodeName());

                if (current.getNodeType() != Node.ATTRIBUTE_NODE) {
                    int index = getIndexAmongSiblings(current);
                    xpathBuilder.append('[').append(index).append(']');
                }
            }
        }

        xpathBuilder.append('/'); // Add trailing slash
        String xpath = xpathBuilder.toString();
        xpathCache.put(node, xpath);
        return xpath;
    }

    /**
     * Returns the index of the given node among its siblings with the same name.
     * Because this operation is very expensive, the results are cached.
     * 
     * The assumption is that the node is not moved around in the DOM tree and
     * that the siblings are not added or removed.
     * 
     * Node class doesn't override Object hashCode() and equals()
     * so we assume that for any given document the Node object stays tha same.
     * This allows us to use WeakHashMap to cache the results.
     * 
     * The cache should be cleared after each document is processed.
     */
    private static final HashMap<Node, Integer> indexCache = new HashMap<>();
    private static int getIndexAmongSiblings(Node node) {
        if (indexCache.containsKey(node)) {
            return indexCache.get(node);
        }

        int index = 0; // X3ML uses zero-based indexing, even so XPath is one-based
        for (Node sibling = node.getPreviousSibling(); sibling != null; sibling = sibling.getPreviousSibling()) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE && sibling.getNodeName().equals(node.getNodeName())) {
                index++;
            }
        }

        indexCache.put(node, index);
        return index;
    }

    public static void clear() {
        indexCache.clear();
        xpathCache.clear();
    }

    public static String extractAssocTableXPath(Node node) {
        return $(node).xpath();
    }
}
