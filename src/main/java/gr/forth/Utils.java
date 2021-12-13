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
import gr.forth.ics.isl.x3ml.X3MLEngine;
import gr.forth.ics.isl.x3ml.engine.X3ML;
import gr.forth.ics.isl.x3ml.engine.X3ML.Mapping;
import gr.forth.ics.isl.x3ml.engine.X3ML.RootElement;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import static gr.forth.ics.isl.x3ml.X3MLEngine.exception;
import java.net.URL;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.Lang;

/**
 * @author Yannis Marketakis (marketak 'at' ics 'dot' forth 'dot' gr)
 * @author Nikos Minadakis (minadakn 'at' ics 'dot' forth 'dot' gr)
 */
@Log4j2
public class Utils {
    
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
                                  .append(" does not contain any value. ")
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
            log.error(msg.replaceAll("(?m)^[ \t]*\r?\n", ""));
        }
    }
    
    /** It reads the contents of the given folder, it concatenates the contents of the XML documents 
     * that exist in the folder and it produces the XML tree (using DOM structures) and returns the root element.
     * The method searches for files either in the given folder only or contents that might exist in sub-folders as well.
     * Furthermore it takes into account only files with extension .xml.
     * 
     * @param folderPath the path of the corresponding folder containing XML input data
     * @param recursiveSearch if true it will search in the closure of the folder for XML files, 
     * otherwise it will return only the direct contents of the given directory
     * @return the root element of the XML tree that is being created from the concatenation of the XML documents in the folder
     * @throws Exception if the given path does not respond to a folder
     */
    public static Element parseFolderWithXmlFiles(String folderPath, boolean recursiveSearch) throws Exception{
        Collection<InputStream> xmlInputFilesCollection=new HashSet<>();
        for(File file : Utils.retrieveXMLfiles(new File(folderPath), recursiveSearch)){
            xmlInputFilesCollection.add(new FileInputStream(file));
        }
        return Utils.parseMultipleXMLFiles(xmlInputFilesCollection);
    }
    
    /** Returns the XML files that exist under the given folder. If the recursiveSearch parameter 
     * is enabled the instead of listing the direct contents of the given directory, all the 
     * XML files that are descendants of this directory will be returned.
     * 
     * @param folder the folder that contains XML input data
     * @param recursiveSearch if true it will search in the closure of the folder for XML files, 
     * otherwise it will return only the direct contents of the given directory
     * @return the collection of the XML input files that exist in the given directory */
    public static Collection<File> retrieveXMLfiles(File folder, boolean recursiveSearch){
        if(!folder.isDirectory()){
            throw exception("The given path (\""+folder.getAbsolutePath()+"\") does not correspond to a directory");
        }
        Collection<File> retCol=new HashSet<>();
        List<File> foldersLeftToCheck=new ArrayList<>();
        for(File file : folder.listFiles()){
            if(file.isFile() && file.getName().toLowerCase().endsWith("xml")){
                retCol.add(file);
            }else if(file.isDirectory()){
                foldersLeftToCheck.add(file);
            }else{
                log.debug("Skipping file \""+file.getPath()+"\" - It might not be an XML file");
            }
        }
        if(recursiveSearch){
            while(!foldersLeftToCheck.isEmpty()){
                File subFolder=foldersLeftToCheck.remove(0);
                File[] subFolderContents=subFolder.listFiles();
                for(File f : subFolderContents){
                    if(f.isFile() && f.getName().toLowerCase().endsWith("xml")){
                        retCol.add(f);
                    }else if(f.isDirectory()){
                        foldersLeftToCheck.add(f);
                    }else{
                        log.debug("Skipping file \""+f.getPath()+"\" - It might not be an XML file");
                    }
                }
            }
        }
        return retCol;
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
        Multimap<String, X3ML.EntityElement> typeAwareVariablesVsEntity=HashMultimap.create();
        for(Mapping mapping : initialElement.mappings.mappings){
            Multimap<String,X3ML.EntityElement> variablesVsEntity=HashMultimap.create();
            variablesVsEntity=retrieveEntitiesWithVariable(mapping.domain, variablesVsEntity);
            globalVariablesVsEntity=retrieveEntitiesWithGlobalVariable(mapping.domain, globalVariablesVsEntity);
            typeAwareVariablesVsEntity=retrieveEntitiesWithTypeAwareVariable(mapping.domain, typeAwareVariablesVsEntity);
            if(mapping.links != null){
                for(X3ML.LinkElement linkEl : mapping.links){
                    variablesVsEntity=retrieveEntitiesWithVariable(linkEl, variablesVsEntity);
                    variablesVsEntity=retrieveEntitiesWithVariable(linkEl.path, variablesVsEntity);
                    variablesVsEntity=retrieveEntitiesWithVariable(linkEl.range, variablesVsEntity);
                    globalVariablesVsEntity=retrieveEntitiesWithGlobalVariable(linkEl, globalVariablesVsEntity);
                    globalVariablesVsEntity=retrieveEntitiesWithGlobalVariable(linkEl.path, globalVariablesVsEntity);
                    globalVariablesVsEntity=retrieveEntitiesWithGlobalVariable(linkEl.range, globalVariablesVsEntity);
                    typeAwareVariablesVsEntity=retrieveEntitiesWithTypeAwareVariable(linkEl, typeAwareVariablesVsEntity);
                    typeAwareVariablesVsEntity=retrieveEntitiesWithTypeAwareVariable(linkEl.path, typeAwareVariablesVsEntity);
                    typeAwareVariablesVsEntity=retrieveEntitiesWithTypeAwareVariable(linkEl.range, typeAwareVariablesVsEntity);
                }
            }
            validateVariablesAndEntities(variablesVsEntity);
        }
        validateVariablesAndEntities(typeAwareVariablesVsEntity);
        validateVariablesAndEntities(globalVariablesVsEntity);
        return initialElement;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithVariable(X3ML.DomainElement domain, Multimap<String, X3ML.EntityElement> multimap){
        if(domain.target_node.entityElement.variable_deprecated!=null){
            multimap.put(domain.target_node.entityElement.variable_deprecated, domain.target_node.entityElement);
        }
        return multimap;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithVariable(X3ML.LinkElement link, Multimap<String, X3ML.EntityElement> multimap){
        if(link.range.target_node.entityElement.variable_deprecated!=null){
            multimap.put(link.range.target_node.entityElement.variable_deprecated, link.range.target_node.entityElement);
        }
        return multimap;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithVariable(X3ML.PathElement path, Multimap<String, X3ML.EntityElement> multimap) {
        if(path.target_relation.entities!=null){
            for(X3ML.EntityElement entityElem : path.target_relation.entities){
                if(entityElem.variable_deprecated!=null){
                    multimap.put(entityElem.variable_deprecated, entityElem);
                }
            }
        }
        return multimap;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithVariable(X3ML.RangeElement range, Multimap<String, X3ML.EntityElement> multimap){
        if(range.target_node.entityElement.additionals!=null){
            for(X3ML.Additional additionalElem : range.target_node.entityElement.additionals){
                for(X3ML.EntityElement entityElem : additionalElem.entityElement){
                    if(entityElem.variable_deprecated!=null){
                        multimap.put(entityElem.variable_deprecated, entityElem);
                    }
                }
            }
        }
        return multimap;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithTypeAwareVariable(X3ML.DomainElement domain, Multimap<String, X3ML.EntityElement> multimap){
        if(domain.target_node.entityElement.variable!=null){
            multimap.put(domain.target_node.entityElement.variable, domain.target_node.entityElement);
        }
        return multimap;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithTypeAwareVariable(X3ML.LinkElement link, Multimap<String, X3ML.EntityElement> multimap){
        if(link.range.target_node.entityElement.variable!=null){
            multimap.put(link.range.target_node.entityElement.variable, link.range.target_node.entityElement);
        }
        return multimap;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithTypeAwareVariable(X3ML.PathElement path, Multimap<String, X3ML.EntityElement> multimap) {
        if(path.target_relation.entities!=null){
            for(X3ML.EntityElement entityElem : path.target_relation.entities){
                if(entityElem.variable!=null){
                    multimap.put(entityElem.variable, entityElem);
                }
            }
        }
        return multimap;
    }
    
    private static Multimap<String, X3ML.EntityElement> retrieveEntitiesWithTypeAwareVariable(X3ML.RangeElement range, Multimap<String, X3ML.EntityElement> multimap){
        if(range.target_node.entityElement.additionals!=null){
            for(X3ML.Additional additionalElem : range.target_node.entityElement.additionals){
                for(X3ML.EntityElement entityElem : additionalElem.entityElement){
                    if(entityElem.variable!=null){
                        multimap.put(entityElem.variable, entityElem);
                    }
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
                for(X3ML.EntityElement entityElem : additionalElem.entityElement){
                    if(entityElem.globalVariable!=null){
                        multimap.put(entityElem.globalVariable, entityElem);
                    }
                }
            }
        }
        return multimap;
    }
    
    private static void validateVariablesAndEntities(Multimap<String,X3ML.EntityElement> multimap){
        for(String variable : multimap.keySet()){
            X3ML.InstanceGeneratorElement instanceGeneratorFound=null;
            List<X3ML.LabelGeneratorElement> labelGeneratorFound=null;
            for(X3ML.EntityElement entityElem : multimap.get(variable)){
                if(entityElem.instanceGenerator!=null){
                    instanceGeneratorFound=entityElem.instanceGenerator;
//                    labelGeneratorFound=entityElem.labelGenerators;
                }
            }
            if(instanceGeneratorFound==null){
                throw exception("The variable \""+variable+"\" has been declared however the details of the entity "
                               +"(i.e.instance_generator, label generators, etc.) are missing");
            }else{
                for(X3ML.EntityElement entityElem : multimap.get(variable)){
                    if(entityElem.instanceGenerator==null){
                        entityElem.instanceGenerator=instanceGeneratorFound;
//                        entityElem.labelGenerators=labelGeneratorFound;
                    }
                }
            }
        }
    }
    
    /**Merges multiple mapping files and produces a single file containing the merged contents.
     * The method validates that the contents of the corresponding files are valid and 
     * consistent with respect to the X3ML schema and produces a single block of X3ML mappings.
     * The method returns the X3ML representation of the merged X3ML mappings. 
     * 
     * @param mappingFiles the files containing X3ML mappings
     * @return the string representation of the merged X3ML mappings. */
    public static String mergeMultipleMappingFiles(File ... mappingFiles){
        try{
            List<InputStream> streams=new ArrayList<>();
            for(File f : mappingFiles){
                streams.add(new FileInputStream(f));
            }
            return Utils.mergeMultipleMappingFiles(streams);
        }catch(FileNotFoundException ex){
            throw exception("An error occured while merging the X3ML mapping files",ex);
        }
    }
    
    /**Merges multiple mapping files and produces a single file containing the merged contents.
     * The method validates that the contents of the corresponding files are valid and 
     * consistent with respect to the X3ML schema and produces a single block of X3ML mappings.
     * The method returns the X3ML representation of the merged X3ML mappings. 
     * 
     * @param mappingFiles the files containing X3ML mappings as inputStreams
     * @return the string representation of the merged X3ML mappings. */
    public static String mergeMultipleMappingFiles(Collection<InputStream> mappingFiles){
        try{
            Document masterMappingsFile=null;
            List<InputStream> inputStreams=new ArrayList<>();
            // first validate the given X3ML mapping files
            for(InputStream mappingFile : mappingFiles){
                InputStream is=X3MLEngine.validateX3MLMappings(mappingFile);
                inputStreams.add(is);
            }
            
            for(InputStream inputMappingFile : inputStreams){
                if(masterMappingsFile==null){   //the first mappings file will be the master doc
                    masterMappingsFile=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputMappingFile);
                }else{
                    Document singleMappingsFile=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputMappingFile);
                    masterMappingsFile=Utils.mergeNamespacesBlockFromX3mlFile(masterMappingsFile,singleMappingsFile.getElementsByTagName("namespace"));
                    masterMappingsFile=Utils.concatMappingsBlockFromX3mlFile(masterMappingsFile,singleMappingsFile.getElementsByTagName("mapping"));
                }
            }
            return Utils.exportMappingsFile(masterMappingsFile);
        }catch(IOException | ParserConfigurationException | SAXException ex){
            throw exception("An error occured while validating the X3ML mapping files",ex);
        }
    }
    
    /** Appends the given value with a URN:UUID scheme prefix.
     * 
     * @param originalValue the original value
     * @return the original value enriched with a URN:UUID scheme prefix */
    public static String urnValue(String originalValue){
        return Labels.URN+":"+Labels.UUID+":"+originalValue;
    }
    
    /** Checks if the given term contains an affirmative value. Affirmative values 
     * are considered the following: {yes, true, 1}. The comparison for affirmative values is 
     * being while ignoring the case of the terms. 
     * If the given term does not belong to the acceptance set, or it is null 
     * the the result will be false.
     * 
     * @param term the term that will be checked if it is affirmative or not
     * @return yes if the term is affirmative, otherwise false. */
    public static boolean isAffirmative(String term){
        if(term!=null){
            if(term.equalsIgnoreCase(Labels.TRUE) || term.equalsIgnoreCase(Labels.YES) || term.equalsIgnoreCase("1")){
                return true;
            }
        }
        return false;
    }
    
    /** The method parses the given terminology resource and identifies if it is a URL 
     * or a file resource. In addition it defines the serialization format of the resource
     * using the suffix extension of the resource (e.g. terms.nt appears in NT format).
     * Once identified the above the method returns them as a pair of an InputStream and the 
     * corresponding language. 
     * 
     * @param terminologyResource the resource (either URL or file) containing the terms
     * @return a Pair containing the InputStream of the resource and the serialization format */
    public static Pair<InputStream, Lang> getTerminologyResourceDetails(String terminologyResource){
        String extension=terminologyResource.toLowerCase().substring(terminologyResource.lastIndexOf(".")+1);
        Lang lang=null;
        log.debug("The extracted extension of the terminology resource is "+extension);
        switch(extension){
            case Labels.RDF:
                lang=Lang.RDFXML;
                break;
            case Labels.NT:
            case Labels.NTRIPLES:
                lang=Lang.NTRIPLES;
                break;
            case Labels.TRIG:
                lang=Lang.TRIG;
                break;
            case Labels.TTL:
            case Labels.TURTLE:
                lang=Lang.TURTLE;
                break;
            default:
                log.error("Cannot identify the serialization format (based on the extension) of the terminology resource "+terminologyResource);
                throw exception("Cannot identify the serialization format (based on the extension) of the terminology resource "+terminologyResource);
        }
        try{
            InputStream inputStream;
            if(terminologyResource.toLowerCase().startsWith(Labels.HTTP)){   //URL resource
                inputStream=new URL(terminologyResource).openStream();
            }else{                                                           //File resource
                inputStream=new FileInputStream(new File(terminologyResource));
            }
            return Pair.of(inputStream, lang);
        }catch(IOException ex){
            log.error("An error occured while reading the contents of the terminology stream ",ex);
            throw exception("An error occured while reading the contents of the terminology stream ",ex);
        }
    }
    
    /* merges the namespaces blocks that are given in the master doc that is provided. Returns the updated document*/
    private static Document mergeNamespacesBlockFromX3mlFile(Document masterDoc, NodeList newNamespaceElems){
        Map<String,String> existingNamespaces=new HashMap<>();
        NodeList existingNamespaceElems=masterDoc.getElementsByTagName("namespace");
        for(int i=0;i<existingNamespaceElems.getLength();i++){
            existingNamespaces.put(existingNamespaceElems.item(i).getAttributes().getNamedItem("prefix").getNodeValue(),
                                   existingNamespaceElems.item(i).getAttributes().getNamedItem("uri").getNodeValue());
        }
        for(int i=0;i<newNamespaceElems.getLength();i++){
            String nsPrefix=newNamespaceElems.item(i).getAttributes().getNamedItem("prefix").getNodeValue();
            String nsUri=newNamespaceElems.item(i).getAttributes().getNamedItem("uri").getNodeValue();
            if(existingNamespaces.containsKey(nsPrefix)){
                if(!existingNamespaces.get(nsPrefix).equals(nsUri)){
                    throw exception("The declaration of the namespace with prefix \""+nsPrefix+"\" is not the same in all "+
                                    "mapping  files. ("+existingNamespaces.get(nsPrefix)+" != "+nsUri+")");
                }
            }else{
                existingNamespaces.put(nsPrefix, nsUri);
                Node replicatedNamespace=masterDoc.importNode(newNamespaceElems.item(i), true);
                masterDoc.getElementsByTagName("namespaces").item(0).appendChild(replicatedNamespace);
            }
        }
        return masterDoc;
    }
    
    /* concatenates the given mappings after the last existing mapping in the given master doc. Returns the updated document */
    private static Document concatMappingsBlockFromX3mlFile(Document masterDoc, NodeList newMappingsElems){
        for(int i=0;i<newMappingsElems.getLength();i++){
            Node replMappingNode=masterDoc.importNode(newMappingsElems.item(i), true);
            masterDoc.getElementsByTagName("mappings").item(0).appendChild(replMappingNode);
        }
        return masterDoc;
    }
    
    private static String exportMappingsFile(Document doc){
        try{
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();
        }catch(IllegalArgumentException |  TransformerException | TransformerFactoryConfigurationError ex){
            throw exception("An error occured while exporting the contnets of the X3ML mappings file",ex);
        }
    }
}