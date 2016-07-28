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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import static eu.delving.x3ml.X3MLEngine.exception;
import eu.delving.x3ml.engine.X3ML;
import eu.delving.x3ml.engine.X3ML.Mapping;
import eu.delving.x3ml.engine.X3ML.RootElement;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Yannis Marketakis (marketak 'at' ics 'dot' forth 'dot' gr)
 * @author Nikos Minadakis (minadakn 'at' ics 'dot' forth 'dot' gr)
 */
public class Utils {
    private static final Logger LOGGER=Logger.getLogger(Utils.class);
    
    /**Produces an error message describing the missing argument from the given label generators.
     * The method is being used for constructing the error message that will be shown to the user 
     * when he wants to use a label generator with some arguments that do not exist in the XML input. 
     * The messages contains a brief description (for humans), as well as the textual description 
     * of the label generator and the missing argument name. 
     * 
     * @param generator the generator that is being used.
     * @param expectedValue the name of the argument that cannot be found in the input
     * @return a string representation of the error message.
     */
    public static String produceLabelGeneratorMissingArgumentError(X3ML.GeneratorElement generator, String expectedValue){
        return new StringBuilder().append("LabelGenerator Error: ")
                                  .append("The attribute ")
                                  .append("\"")
                                  .append(expectedValue)
                                  .append("\"")
                                  .append(" is missing from the generator. ")
                                  .append("[Mapping #: ")
                                  .append(X3ML.RootElement.mappingCounter)
                                  .append(", Link #: ")
                                  .append(X3ML.RootElement.linkCounter)
                                  .append("]. ")
                                  .append(generator).toString();
    }
    
    /**Produces an error message describing that the given label generator does not have any arguments.
     * The method is being used for constructing the error message that will be shown to the user 
     * when he wants to use a label generator without providing any arguments. 
     * The messages contains a brief description (for humans), as well as the textual description of the 
     * label generator . 
     * 
     * @param generator the generator that is being used.
     * @return a string representation of the error message.
     */
    public static String produceLabelGeneratorEmptyArgumentError(X3ML.GeneratorElement generator){
        return new StringBuilder().append("LabelGenerator Error: ") 
                                  .append("The label generator with name ")
                                  .append("\"")
                                  .append(generator.getName())
                                  .append("\"")
                                  .append(" does not containg any value. ")
                                  .append("[Mapping #: ")
                                  .append(X3ML.RootElement.mappingCounter)
                                  .append(", Link #: ")
                                  .append(X3ML.RootElement.linkCounter)
                                  .append("]. ")
                                  .append(generator).toString();
    }
    
    /** Output the error messages that are given using an error logger.
     *  If the error logger is disabled (through the log4j.properties file then nothing will be reported)
     * 
     * @param messages the error messages to be reported
     */
    public static void printErrorMessages(String ... messages){
        for(String msg : messages){
            LOGGER.error(msg.replaceAll("(?m)^[ \t]*\r?\n", ""));
        }
    }
    
    /** It reads the contents of the given folder, it concatenates the contents of the XML documents 
     * that exist in the folder and it produces the XML tree (using DOM structures) and returns the root element.
     * The method searches for files only in the given folder and ignores any contents that might exist in sub-folders.
     * Furthermore it takes into account only files with extension .xml.
     * 
     * @param folderPath the path of the corresponding folder containing XML input data
     * @return the root element of the XML tree that is being created from the concatenation of the XML documents in the folder
     * @throws Exception if the given path does not respond to a folder
     */
    public static Element parseFolderWithXmlFiles(String folderPath) throws Exception{
        File folder=new File(folderPath);
        if(!folder.isDirectory()){
            throw new Exception("The given path (\""+folderPath+"\") does not correspond to a directory");
        }
        Collection<InputStream> xmlInputFilesCollection=new HashSet<>();
        for(File file : folder.listFiles()){
            if(file.getName().toLowerCase().endsWith("xml")){
                xmlInputFilesCollection.add(new FileInputStream(file));
            }else{
                LOGGER.debug("Skipping file \""+file.getPath()+"\" - It might not be an XML file");
            }
        }
        return Utils.parseMultipleXMLFiles(xmlInputFilesCollection);
    }
    
    /** The method takes as input a set of XML input files and produces an XML tree (using DOM structures)
     * that corresponds to the concatenation of the contents of the given XML input files. 
     * If the root element of the given XML inputs is not the same then an exception is thrown.
     * 
     * @param xmlFileInputStreams a collection of XML input files as InputStreams
     * @return the root element of the XML tree that is being created from the concatenation of the given XML InputStreams
     */
    public static Element parseMultipleXMLFiles(Collection<InputStream> xmlFileInputStreams){
        try{
            Document masterDoc=DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            for(InputStream is : xmlFileInputStreams){
                if(masterDoc.getDocumentElement()==null){   //only for the first file
                    masterDoc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                }else{
                    Document singleDoc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                    Element rootElement=singleDoc.getDocumentElement();
                    if(!rootElement.getNodeName().equals(masterDoc.getDocumentElement().getNodeName())){
                        throw exception("The given XML input files have different root nodes: ["
                                       +rootElement.getNodeName()+" , "+masterDoc.getDocumentElement().getNodeName()
                                       +"]");
                    }
                    NodeList children=rootElement.getChildNodes();
                    for(int i=0;i<children.getLength();i++){
                        Node childClone=masterDoc.importNode(children.item(i), true);
                        masterDoc.getDocumentElement().appendChild(childClone);
                    }
                }
            }
            return masterDoc.getDocumentElement();
        }catch(ParserConfigurationException | IOException | SAXException ex){
            throw exception("An error occured while concatenating XML documents");
        }
    }
    
    /**The method validates the X3ML mappings file as regards the variables it contains.
     * More specifically it validates that all the entities that have variables declared, 
     * either contain the necessary details (i.e. type, instance and label generator) or there is 
     * an entity with the same variable that contain those details. If there is a variable defined 
     * but the corresponding details are missing, then an X3MLException is thrown.
     * Upon the successful validation, the given X3ML mappings file is being updated 
     * so that it contains the missing elements.
     * 
     * @param initialElement the root element of the X3ML mappings file
     * @return the updated root element of the X3ML mappings file
     */
    public static RootElement parseX3MLAgainstVariables(RootElement initialElement){
        Multimap<String, X3ML.EntityElement> globalVariablesVsEntity=HashMultimap.create();
        for(Mapping mapping : initialElement.mappings){
            Multimap<String,X3ML.EntityElement> variablesVsEntity=HashMultimap.create();
            variablesVsEntity=retrieveEntitiesWithVariable(mapping.domain, variablesVsEntity);
            globalVariablesVsEntity=retrieveEntitiesWithGlobalVariable(mapping.domain, globalVariablesVsEntity);
            if(mapping.links != null){
                for(X3ML.LinkElement linkEl : mapping.links){
                    variablesVsEntity=retrieveEntitiesWithVariable(linkEl, variablesVsEntity);
                    variablesVsEntity=retrieveEntitiesWithVariable(linkEl.path, variablesVsEntity);
                    variablesVsEntity=retrieveEntitiesWithVariable(linkEl.range, variablesVsEntity);
                    globalVariablesVsEntity=retrieveEntitiesWithGlobalVariable(linkEl, globalVariablesVsEntity);
                    globalVariablesVsEntity=retrieveEntitiesWithGlobalVariable(linkEl.path, globalVariablesVsEntity);
                    globalVariablesVsEntity=retrieveEntitiesWithGlobalVariable(linkEl.range, globalVariablesVsEntity);
                }
            }
            validateVariablesAndEntities(variablesVsEntity);
        }
        validateVariablesAndEntities(globalVariablesVsEntity);
        return initialElement;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithVariable(X3ML.DomainElement domain, Multimap<String, X3ML.EntityElement> multimap){
        if(domain.target_node.entityElement.variable!=null){
            multimap.put(domain.target_node.entityElement.variable, domain.target_node.entityElement);
        }
        return multimap;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithVariable(X3ML.LinkElement link, Multimap<String, X3ML.EntityElement> multimap){
        if(link.range.target_node.entityElement.variable!=null){
            multimap.put(link.range.target_node.entityElement.variable, link.range.target_node.entityElement);
        }
        return multimap;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithVariable(X3ML.PathElement path, Multimap<String, X3ML.EntityElement> multimap) {
        if(path.target_relation.entities!=null){
            for(X3ML.EntityElement entityElem : path.target_relation.entities){
                if(entityElem.variable!=null){
                    multimap.put(entityElem.variable, entityElem);
                }
            }
        }
        return multimap;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithVariable(X3ML.RangeElement range, Multimap<String, X3ML.EntityElement> multimap){
        if(range.target_node.entityElement.additionals!=null){
            for(X3ML.Additional additionalElem : range.target_node.entityElement.additionals){
                if(additionalElem.entityElement.variable!=null){
                    multimap.put(additionalElem.entityElement.variable, additionalElem.entityElement);
                }
            }
        }
        return multimap;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithGlobalVariable(X3ML.DomainElement domain, Multimap<String, X3ML.EntityElement> multimap){
        if(domain.target_node.entityElement.globalVariable!=null){
            multimap.put(domain.target_node.entityElement.globalVariable, domain.target_node.entityElement);
        }
        return multimap;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithGlobalVariable(X3ML.LinkElement link, Multimap<String, X3ML.EntityElement> multimap){
        if(link.range.target_node.entityElement.globalVariable!=null){
            multimap.put(link.range.target_node.entityElement.globalVariable, link.range.target_node.entityElement);
        }
        return multimap;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithGlobalVariable(X3ML.PathElement path, Multimap<String, X3ML.EntityElement> multimap){
        if(path.target_relation.entities!=null){
            for(X3ML.EntityElement entityElem : path.target_relation.entities){
                if(entityElem.globalVariable!=null){
                    multimap.put(entityElem.globalVariable, entityElem);
                }
            }
        }
        return multimap;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithGlobalVariable(X3ML.RangeElement range, Multimap<String, X3ML.EntityElement> multimap){
        if(range.target_node.entityElement.additionals!=null){
            for(X3ML.Additional additionalElem : range.target_node.entityElement.additionals){
                if(additionalElem.entityElement.globalVariable!=null){
                    multimap.put(additionalElem.entityElement.globalVariable, additionalElem.entityElement);
                }
            }
        }
        return multimap;
    }
    
    private static void validateVariablesAndEntities(Multimap<String,X3ML.EntityElement> multimap){
        for(String variable : multimap.keySet()){
            List<X3ML.TypeElement> typeElementsFound=null;
            X3ML.InstanceGeneratorElement instanceGeneratorFound=null;
            List<X3ML.LabelGeneratorElement> labelGeneratorFound=null;
            for(X3ML.EntityElement entityElem : multimap.get(variable)){
                if(entityElem.typeElements!=null && !entityElem.typeElements.isEmpty() && 
                   entityElem.instanceGenerator!=null){
                    if(typeElementsFound==null){
                        typeElementsFound=entityElem.typeElements;
                        instanceGeneratorFound=entityElem.instanceGenerator;
                        labelGeneratorFound=entityElem.labelGenerators;
                    }
                }
            }
            if(typeElementsFound==null){
                throw exception("The variable \""+variable+"\" has been declared however the details of the entity "
                               +"(i.e. type, instance_generator, etc.) are missing");
            }else{
                for(X3ML.EntityElement entityElem : multimap.get(variable)){
                    if(entityElem.typeElements==null){
                        entityElem.typeElements=typeElementsFound;
                        entityElem.instanceGenerator=instanceGeneratorFound;
                        entityElem.labelGenerators=labelGeneratorFound;
                    }
                }
            }
        }
    }
}