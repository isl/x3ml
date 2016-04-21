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
package eu.delving.x3ml.engine;

import com.hp.hpl.jena.rdf.model.Resource;
import eu.delving.x3ml.INPUT_TYPE;
import eu.delving.x3ml.X3MLEngine;
import org.w3c.dom.Node;
import static eu.delving.x3ml.X3MLEngine.exception;
import static eu.delving.x3ml.engine.X3ML.ArgValue;
import static eu.delving.x3ml.engine.X3ML.Condition;
import static eu.delving.x3ml.engine.X3ML.GeneratedValue;
import static eu.delving.x3ml.engine.X3ML.GeneratorElement;
import static eu.delving.x3ml.engine.X3ML.SourceType;
import gr.forth.ics.isl.x3ml_reverse_utils.AssociationTable;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import static org.joox.JOOX.$;

/**
 * This abstract class is above Domain, Path, and Range and carries most of
 * their contextual information.
 *
 * @author Gerald de Jong <gerald@delving.eu>
 * @author Nikos Minadakis <minadakn@ics.forth.gr>
 * @author Yannis Marketakis <marketak@ics.forth.gr>
 */
public abstract class GeneratorContext {

    public final Root.Context context;
    public final Root.ContextRDF contextRDF;
    public final GeneratorContext parent;
    public final Node node;
    public final int index;
    public final Resource resource;
    
    protected GeneratorContext(Root.Context context, GeneratorContext parent, Node node, int index) {
        this.context = context;
        this.parent = parent;
        this.node = node;
        this.index = index;
        this.resource=null;
        this.contextRDF = null;
    }
    
    protected GeneratorContext(Root.ContextRDF context, GeneratorContext parent, Resource resource , int index) {
        this.contextRDF = context;
        this.parent = parent;
        this.resource = resource;
        this.index = index;
        this.node=null;
        this.context =null;
    }

    public GeneratedValue get(String variable, VariableScope scope) {
        if (parent == null) {
            throw exception("Parent context missing");
        }
        return parent.get(variable, scope);
    }

    public void put(String variable, VariableScope scope, GeneratedValue generatedValue) {
        if (parent == null) {
            throw exception("Parent context missing");
        }
        System.out.println("PUTTING VAR"+variable+"\t"+scope+"\t"+generatedValue.text);
        parent.put(variable, scope, generatedValue);
    }

    public String evaluate(String expression) {
        return context.input().valueAt(node, expression);
    }

    public GeneratedValue getInstance(final GeneratorElement generator, String globalVariable, String variable, String typeAwareVar, String unique) {
        if(generator == null){
            throw exception("Value generator missing");
        }
        GeneratedValue generatedValue;
        if(globalVariable != null){
            generatedValue = get(globalVariable, VariableScope.GLOBAL);
            if (generatedValue == null) {
                generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                    @Override
                    public ArgValue getArgValue(String name, SourceType sourceType) {
                        return context.input().evaluateArgument(node, index, generator, name, sourceType);
                    }
                });
                put(globalVariable, VariableScope.GLOBAL, generatedValue);
            }
        }
        else if(typeAwareVar != null){
            if(variable!=null){
                generatedValue = get(variable, VariableScope.WITHIN_MAPPING);
                if (generatedValue == null) {
                    generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                        @Override
                        public ArgValue getArgValue(String name, SourceType sourceType) {
                            return context.input().evaluateArgument(node, index, generator, name, sourceType);
                        }
                    });
                    put(variable,VariableScope.WITHIN_MAPPING, generatedValue);
                    context.putGeneratedValue(extractXPath(node) + unique+"-"+typeAwareVar, generatedValue);
                    this.createAssociationTable(generatedValue, null, extractAssocTableXPath(node));
                }
            }else{
                String nodeName = extractXPath(node) + unique+"-"+typeAwareVar;
                String xpathProper=extractAssocTableXPath(node);
                generatedValue = context.getGeneratedValue(nodeName);
                if (generatedValue == null) {
                    generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                        @Override
                        public ArgValue getArgValue(String name, SourceType sourceType) {
                            return context.input().evaluateArgument(node, index, generator, name, sourceType);
                        }
                    });
                    GeneratedValue genArg=null;
                    if(generator.getName().equalsIgnoreCase("Literal")){
                        genArg = context.policy().generate(generator, new Generator.ArgValues() {
                            @Override
                            public ArgValue getArgValue(String name, SourceType sourceType) {
                                return context.input().evaluateArgument2(node, index, generator, name, sourceType);

                            }
                        });
                    }
                    context.putGeneratedValue(nodeName, generatedValue);
                    this.createAssociationTable(generatedValue, genArg, xpathProper);
                }
            }
        }
        else{
            if(variable != null){
                if(X3MLEngine.inputType==INPUT_TYPE.XML){
                    generatedValue = get(variable, VariableScope.WITHIN_MAPPING);
                    if (generatedValue == null) {
                        generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                            @Override
                            public ArgValue getArgValue(String name, SourceType sourceType) {
                                return context.input().evaluateArgument(node, index, generator, name, sourceType);
                            }
                        });
                        put(variable, VariableScope.WITHIN_MAPPING, generatedValue);
                    }
                }else{
                    generatedValue = get(variable, VariableScope.WITHIN_MAPPING);
                    if (generatedValue == null) {
                        generatedValue = contextRDF.policy().generate(generator, new Generator.ArgValues() {
                        @Override
                        public ArgValue getArgValue(String name, SourceType sourceType) {
                            return null;
    //                        return context.input().evaluateArgument(node, index, generator, name, sourceType);
                        }
                        });
                        put(variable, VariableScope.WITHIN_MAPPING, generatedValue);
                    }
                }
            }
            else{
                if(X3MLEngine.inputType==INPUT_TYPE.XML){
                    String nodeName = extractXPath(node) + unique;
                    String xpathProper=extractAssocTableXPath(node);
                    generatedValue = context.getGeneratedValue(nodeName);
                    if (generatedValue == null) {
                        generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                            @Override
                            public ArgValue getArgValue(String name, SourceType sourceType) {
                                return context.input().evaluateArgument(node, index, generator, name, sourceType);
                            }
                        });
                        GeneratedValue genArg=null;
                        if(generator.getName().equalsIgnoreCase("Literal")){
                            genArg = context.policy().generate(generator, new Generator.ArgValues() {
                                @Override
                                public ArgValue getArgValue(String name, SourceType sourceType) {
                                    return context.input().evaluateArgument2(node, index, generator, name, sourceType);

                                }
                            });
                        }
                        context.putGeneratedValue(nodeName, generatedValue);
                        this.createAssociationTable(generatedValue, genArg, xpathProper);
                    }
                }else{
                     String nodeName = resource.getURI() + unique;
                    generatedValue = contextRDF.getGeneratedValue(nodeName);
                    if (generatedValue == null) {
                        generatedValue = contextRDF.policy().generate(generator, new Generator.ArgValues() {
                            @Override
                            public ArgValue getArgValue(String name, SourceType sourceType) {
                                System.out.println("getArgValue: "+name+"\t"+sourceType);
                                return contextRDF.input().getLabel(resource,generator.getArgs().get(0).value, sourceType);
                            }
                        });
                        if(generatedValue==null){    //use existing URI
                            generatedValue=new GeneratedValue(X3ML.GeneratedType.URI, resource.getURI());
                        }
                        contextRDF.putGeneratedValue(nodeName, generatedValue);
                    }
                }
            }
        }
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
    
    public static void appendAssociationTable(String xpathEpxr, String key){
        xpathEpxr=xpathEpxr.replace("///", "/").replaceAll("//", "/");
        AssociationTable.addEntry(xpathEpxr,key);
    }
    
    public static void exportAssociationTable(String filename) throws IOException{
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
        writer.append(AssociationTable.exportAll());
        writer.flush();
        writer.close();
    }
    
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
            String soFar = extractXPath(node.getParentNode());
            int sibNumber = 0;
            Node sib = node;
            while (sib.getPreviousSibling() != null) {
                sib = sib.getPreviousSibling();
                if (sib.getNodeType() == Node.ELEMENT_NODE) {
                    sibNumber++;
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
