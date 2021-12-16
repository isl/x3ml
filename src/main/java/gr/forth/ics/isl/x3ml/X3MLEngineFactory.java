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

package gr.forth.ics.isl.x3ml;

import static gr.forth.ics.isl.x3ml.X3MLEngine.exception;
import gr.forth.ics.isl.x3ml.engine.Generator;
import gr.forth.ics.isl.x3ml.engine.GeneratorContext;
import gr.forth.Utils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.Lang;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.w3c.dom.Element;

/** This class instantiates X3ML Engine and handles the ingestion of user-defined details 
 * in a straightforward manner. Furthermore it can execute the X3ML engine for producing 
 * the desired output. The rationale of this class is to act as an abstraction layer, hiding the 
 * complex details of instantiating and executing the X3ML engine. This means that it requires 
 * only the basic information (mappings and input) and all the rest can be either provided by the user 
 * if needed or some default values are used (i.e. if any value is given for the UUID size then a default value is being used).
 * 
 * The following block shows how the X3MLEngineFactory is being exploited for instantiating and executing 
 * the X3MLEngine a very simple configuration.
 * 
 * X3MLEngineFactory.create()
 *                  .withMappings("mappings.x3ml")
 *                  .withInputFiles("input1.xml", "input2.xml")
 *                  .execute();
 * 
 * The following list enumerates the details that can be configured, their description, and if 
 * they are mandatory or optional; mandatory resources should be provided for the proper execution 
 * of the X3ML Engine, while for the optional resources their default values can be exploited.
 * <ul>
 * <li><b>X3ML mappings file</b>: it is the file that contains the X3ML mappings and 
 * it exists in the form of an XML document. This resource is <u>mandatory</u></li>
 * <li><b>input file(s) or input folder(s)</b>: X3ML engine uses as input a set of files in XML format. This 
 * means that either a list of files or entire folders should be used as input. 
 * This resource is <u>mandatory</u></li>
 * <li><b>generator policy file</b>: the XML file containing the details of the generator policies. 
 * This resource is optional, unless the user defines a generator policy file, only the built-in
 * generators will be exploited.</li>
 * <li><b>UUID length</b>: the value of the length for the generated UUIDs. This resource is optional, 
 * so unless the user defines a value the default value that will be used is 4. </li>
 * <li><b>Terminology </b>: The SKOS terminology, exploited within the mappings. </li>
 * <li><b>output file and format</b>: the name/path of the file containing the transformed results or 
 * alternatively the System.out stream. The format can be one of the following (RDF/XML, NTRIPLES, TURTLE).
 * This resource is optional, so unless the user defines them the results will be exported in the System.out stream 
 * in RDF/XML format.</li>
 * <li><b>association table contents</b>: the contents of the association table between the XML input and the 
 * produced RDF output. The contents are exported as an XML file with a user-defined filename/path. 
 * This resource is optional, so unless the user defines it the contents of the association will not be exported. </li>
 * </ul>
 *
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 */
public class X3MLEngineFactory {
    private Set<File> mappingsFiles;
    private List<InputStream> mappingStreams;
    private Set<InputStream> inputStreams;
    private Pair<InputStream,Lang> terminologyStream;
    private Set<Pair<File, Boolean>> inputFolders;
    private InputStream generatorPolicyStream;
    private int uuidSize;
    private String associationTableFile;
    private Pair<OutputStream,OutputFormat> output;
    private boolean progressReporting;
    private static final Logger LOGGER=LogManager.getLogger(X3MLEngineFactory.class);
            
//            Logger.getLogger(X3MLEngineFactory.class);
    
    public enum OutputFormat{
        RDF_XML, 
        RDF_XML_PLAIN, 
        NTRIPLES, 
        TURTLE,
        TRIG
    }
    
    /* Instantiate the factory with the default values */
    private X3MLEngineFactory(){
        this.mappingsFiles=new HashSet<>();
        this.mappingStreams=new ArrayList<>();
        this.inputStreams=new HashSet<>();
        this.inputFolders=new HashSet<>();
        this.generatorPolicyStream=null;
        this.uuidSize=-1;
        this.progressReporting=false;
        this.associationTableFile=null;
        this.output=Pair.of(null, OutputFormat.RDF_XML);
    }
    
    /**Creates a new instance of the X3MLEngineFactory class. After the creation the new 
     * instance contains the default values for many of the configuration details and requires 
     * only the addition of the mappings (the X3ML mappings file) and the input file (or alternatively 
     * the input files, or the folder containing the input files).
     * 
     * @return a new instance of the X3MLEngineFactory class
     */
    public static X3MLEngineFactory create(){
        LOGGER.debug("Created an instance of X3MLEngineFactory");
        return new X3MLEngineFactory();
    }
    
    /**Adds the mappings file(-s) in the X3MLEngineFactory. 
     * 
     * @param mappingsFiles the files with the mappings (X3ML)
     * @return the updated X3MLEngineFactory instance
     */
    public X3MLEngineFactory withMappings(File ... mappingsFiles){
        for(File f : mappingsFiles){
            LOGGER.debug("Added the X3ML mappings file ("+f.getAbsolutePath()+")");
        }
        this.mappingsFiles.addAll(Arrays.asList(mappingsFiles));
        return this;
    }
    
    /**Adds a collection of mappings files in the X3MLEngineFactory. 
     * 
     * @param mappingFilesCollection the files collection with the mappings (X3ML)
     * @return the updated X3MLEngineFactory instance
     */
    public X3MLEngineFactory withMappings(Collection<File> mappingFilesCollection){
        for(File f : mappingsFiles){
            LOGGER.debug("Added the X3ML mappings file ("+f.getAbsolutePath()+")");
        }
        this.mappingsFiles.addAll(mappingFilesCollection);
        return this;
    }
    
    /**Adds a terminology in the X3MLEngineFactory. 
     * 
     * @param terminologyFile a SKOS terminology file
     * @param terminologyLang the serialization format of the SKOS terminology
     * @return the updated X3MLEngineFactory instance
     */
    public X3MLEngineFactory withTerminology(File terminologyFile, Lang terminologyLang){
        try{
            InputStream is=new FileInputStream(terminologyFile);
            this.terminologyStream=Pair.of(is, terminologyLang);
            return this;
        }catch(FileNotFoundException ex){
            throw exception("Cannot find terminology file",ex);
        }
    }
    
    /**Adds a terminology in the X3MLEngineFactory. 
     * 
     * @param terminologyFile a SKOS terminology file
     * @param terminologyLang the serialization format of the SKOS terminology
     * @return the updated X3MLEngineFactory instance
     */
    public X3MLEngineFactory withTerminology(InputStream terminologyFile, Lang terminologyLang){
        this.terminologyStream=Pair.of(terminologyFile, terminologyLang);
        return this;
    }
    
    /** Adds the input files in the X3MLEngineFactory. The methods accepts more than one files 
     * that will be concatenated for producing a single input file. 
     * 
     * @param inputFiles the input (XML) files
     * @return the updated X3MLEngineFactory instance
     */
    public X3MLEngineFactory withInputFiles(File ... inputFiles){
        try{
            for(File f : inputFiles){
                LOGGER.debug("Added the XML input file ("+f.getAbsolutePath()+")");
                this.inputStreams.add(new FileInputStream(f));
            }
            return this;
        }catch(FileNotFoundException ex){
            throw exception("Cannot find input file",ex);
        }
    }
    
    /** Adds the collection of input files in the X3MLEngineFactory.
     * The given files will be concatenated for producing a single input file. 
     * 
     * @param inputFilesCollection the collection input (XML) files
     * @return the updated X3MLEngineFactory instance
     */
    public X3MLEngineFactory withInputFiles(Collection<File> inputFilesCollection){
        try{
            for(File f : inputFilesCollection){
                LOGGER.debug("Added the XML input file ("+f.getAbsolutePath()+")");
                this.inputStreams.add(new FileInputStream(f));
            }
            return this;
        }catch(FileNotFoundException ex){
            throw exception("Cannot find input file",ex);
        }
    }
    
    /**Adds the folder that contains the input files (in XML format). 
     * 
     * @param inputFolder the folder that contains the (XML) input files
     * @param recursiveSearch if true it will search in the closure of the folder for XML files, 
     * otherwise it will return only the direct contents of the given directory
     * @return the updated X3MLEngineFactory instance */
    public X3MLEngineFactory withInputFolder(File inputFolder, boolean recursiveSearch){
        LOGGER.debug("Added the XML input folder ("+inputFolder.getAbsolutePath()+"), recursive search: "+recursiveSearch);
        this.inputFolders.add(Pair.of(inputFolder, recursiveSearch));
        return this;
    }
    
    /**Adds the generator policy file.
     * 
     * @param generatorPolicyFile the file (in XML) that contains the generator policy (for URIs and Literals)
     * @return the updated X3MLEngineFactory instance */
    public X3MLEngineFactory withGeneratorPolicy(File generatorPolicyFile){
        LOGGER.debug("Added the Generator policy file ("+generatorPolicyFile.getAbsolutePath()+")");
        try{
            this.generatorPolicyStream=new FileInputStream(generatorPolicyFile);
            return this;
        }catch(FileNotFoundException ex){
            throw exception("Cannot find generator policy file",ex);
        }
    }
    
    /**Adds the mappings streams in the X3MLEngineFactory. 
     * 
     * @param mappingsStreams the input streams with the mappings (X3ML)
     * @return the updated X3MLEngineFactory instance
     */
    public X3MLEngineFactory withMappings(InputStream ... mappingsStreams){
        LOGGER.debug("Added "+mappingsStreams.length+" X3ML mappings input stream");
        this.mappingStreams.addAll(Arrays.asList(mappingsStreams));
        return this;
    }
    
    /**Adds the collection with mappings streams in the X3MLEngineFactory. 
     * 
     * @param mappingStreamsCollection the collection of input streams with the mappings (X3ML)
     * @return the updated X3MLEngineFactory instance
     */
    public X3MLEngineFactory withMappingsStreams(Collection<InputStream> mappingStreamsCollection){
        LOGGER.debug("Added "+mappingStreamsCollection.size()+" X3ML mappings input stream");
        this.mappingStreams.addAll(mappingStreamsCollection);
        return this;
    }
    
    /**Adds the remote mappings (accessible through the given URLs) in the X3MLEngineFactory. 
     * 
     * @param urls the URLs pointing to the remote mappings (X3ML)
     * @return the updated X3MLEngineFactory instance
     */
    public X3MLEngineFactory withMappings(URL ... urls){
        try{
            for(URL url : urls){
                LOGGER.debug("Added remote X3ML mappings from "+url);
                this.mappingStreams.add(url.openStream());
            }
        }catch(IOException ex){
            throw exception("Cannot find/fetch X3ML mappings from remote location",ex);
        }
        return this;
    }
    
    /** Adds the input resources in the X3MLEngineFactory. The methods accepts more than one input resources
     * that will be concatenated for producing a single input resource. 
     * 
     * @param inputStreams the input (XML) streams
     * @return the updated X3MLEngineFactory instance
     */
    public X3MLEngineFactory withInput(InputStream ... inputStreams){
        LOGGER.debug("Added "+inputStreams.length+" input streams");
        this.inputStreams.addAll(Arrays.asList(inputStreams));
        return this;
    }
    
    /** Adds the collection of input resources in the X3MLEngineFactory. 
     * The given input resources will be concatenated for producing a single input resource. 
     * 
     * @param inputStreamsCollection the collection of input (XML) streams
     * @return the updated X3MLEngineFactory instance
     */
    public X3MLEngineFactory withInput(Collection<InputStream> inputStreamsCollection){
        LOGGER.debug("Added "+inputStreamsCollection.size()+" input streams");
        this.inputStreams.addAll(inputStreamsCollection);
        return this;
    }
    
    /** Adds the remote input resources in the X3MLEngineFactory. The methods accepts more than one input resources
     * that will be concatenated for producing a single input resource. 
     * 
     * @param urls the remote input (XML) resources
     * @return the updated X3MLEngineFactory instance
     */
    public X3MLEngineFactory withInput(URL ... urls){
        try{
            for(URL url : urls){
                LOGGER.debug("Added remote input resource with URL "+url);
                this.inputStreams.add(url.openStream());
            }
        }catch(IOException ex){
            throw exception("Cannot find/fetch input resources from remote location",ex);
        }
        return this;
    }
    
    /**Adds the generator policy resources.
     * 
     * @param generatorPolicyStream the stream that contains the generator policy (for URIs and Literals)
     * @return the updated X3MLEngineFactory instance */
    public X3MLEngineFactory withGeneratorPolicy(InputStream generatorPolicyStream){
        this.generatorPolicyStream=generatorPolicyStream;
        return this;
    }
    
    /**Adds the generator policy resources from a remote location
     * 
     * @param url the URL pointing to the location of the the generator policy (for URIs and Literals)
     * @return the updated X3MLEngineFactory instance */
    public X3MLEngineFactory withGeneratorPolicy(URL url){
        try{
            this.generatorPolicyStream=url.openStream();
        }catch(IOException ex){
            throw exception("Unable to find/fetch the remote generator policy resources",ex);
        }
        return this;
    }
    
    /**Sets the size of the UUID generator. The value denotes the length of the generated UUID value. 
     * For example if the value is set to 2 the it will generate UUIDs like uuid:AD, uuid:AY, 
     * if it is set to 3, it will generate UUIDs like uuid:ACB, etc.
     * The default value that is used is 4, if no other value is provided.
     * 
     * @param uuidLength value of the length for the produced UUIDs
     * @return the updated X3MLEngineFactory instance */
    public X3MLEngineFactory withUuidSize(int uuidLength){
        LOGGER.debug("Set the UUID size to "+uuidLength);
        this.uuidSize=uuidLength;
        return this;
    }
    
    /**Sets the details about the transformed resources. More specifically it allows 
     * defining the name of the file that will be exported, as well as the desired 
     * format (one of RDF/XML, NTRIPLES, TURTLE).
     * If the filename is left intentionally or is left null then instead of exporting
     * the resources on a file, they will be exported @ System.out
     * The default behavior is to export transformed data to System.out in RDF/XML format.
     * 
     * @param filename the name of the file containing the exported data
     * @param format the format of the exported data 
     * @return the updated X3MLEngineFactory instance */
    public X3MLEngineFactory withOutput(String filename, OutputFormat format){
        String outputMsg=(filename==null || filename.isEmpty())? "System.out":"File("+filename+")";
        LOGGER.debug("Set the output to "+outputMsg+" and the format to "+format);
        if(filename!=null){
            try{
                OutputStream os=new FileOutputStream(new File(filename));
                this.output=Pair.of(os, format);
            }catch(FileNotFoundException ex){
                throw exception("Cannot find the output file, "+filename,ex);
            }
        }
        return this;
    }
    
    /**Sets the details about the transformed resources. More specifically it allows 
     * defining the file that will be exported, as well as the desired 
     * format (one of RDF/XML, NTRIPLES, TURTLE).
     * If the file is left null then instead of exporting
     * the resources on a file, they will be exported @ System.out
     * 
     * @param outputFile the file where the exported data will be exported
     * @param format the format of the exported data 
     * @return the updated X3MLEngineFactory instance */
    public X3MLEngineFactory withOutput(File outputFile, OutputFormat format){
        String outputMsg=(outputFile==null)? "System.out":"File("+outputFile.getAbsolutePath()+")";
        LOGGER.debug("Set the output to "+outputMsg+" and the format to "+format);
        if(outputFile!=null){
            try{
                OutputStream os=new FileOutputStream(outputFile);
                this.output=Pair.of(os, format);
            }catch(FileNotFoundException ex){
                throw exception("Cannot find the output file, "+outputFile.getAbsolutePath(),ex);
            }
        }
        return this;
    }
    
    /**Sets the details about the transformed resources. More specifically it specifies 
     * the output stream to be used for exporting the transformed contents, 
     * as well as the desired  format (one of RDF/XML, NTRIPLES, TURTLE).
     * 
     * @param outputStream the stream to be used for exporting the transformed data
     * @param format the format of the exported data 
     * @return the updated X3MLEngineFactory instance */
    public X3MLEngineFactory withOutput(OutputStream outputStream, OutputFormat format){
        String outputMsg=(outputStream==null)? "System.out":"OutputStream";
        LOGGER.debug("Set the output to "+outputMsg+" and the format to "+format);
        if(outputStream!=null){
            this.output=Pair.of(outputStream, format);    
        }
        return this;
    }
    
    /**Sets the name of the file where the contents of the association table will be exported, 
     * as a file in XML format. If the value is left intensionally left or null then the 
     * contents of the association table will not be exported. 
     * The default behavior is NOT to export the contents of the association table 
     *
     * @param associationTableFilename the filename of the XML file containing the contents of the 
     * association table
     * @return the updated X3MLEngineFactory instance */
    public X3MLEngineFactory withAssociationTable(String associationTableFilename){
        if(associationTableFilename==null || associationTableFilename.isEmpty()){
            LOGGER.debug("Disabled the export of the association table");
        }else{
            LOGGER.debug("Enabled the export of the association table in the file "+associationTableFilename);
        }
        this.associationTableFile=associationTableFilename;
        return this;
    }
    
    /** Sets the level of logging to verbose for more informative logging messages.
     * This option enables the debug messages to appear. 
     * 
     * @return the updated X3MLEngineFactory instance */
    public X3MLEngineFactory withVerboseLogging(){
        LOGGER.debug("Changed the logging level to verbose");
        Configurator.setRootLevel(Level.DEBUG);
        return this;
    }
    
    /** Completely disables the logging for X3ML Engine transformation. 
     * 
     * @return the updated X3MLEngineFactory instance */
    public X3MLEngineFactory withoutLogging(){
        LOGGER.debug("Disabled logging");
        Configurator.setRootLevel(Level.OFF);
        return this;
    }
    
    /** Reports the progress of the transformations. 
     * 
     * @return the updated X3MLEngineFactory instance */
    public X3MLEngineFactory withProgressReporting(){
        LOGGER.debug("Enabled the progress reporting");
        this.progressReporting=true;
        return this;
    }
    
    /** Execute the X3ML Engine with the given configuration. If the mandatory resources 
     * have not been defined (the X3ML mappings file and the XML input file(s)/folder) then 
     * an exception is thrown, and the execution is terminated.
     * If the optional resources have not been defined then the default values are used 
     * (for more information about the resources and their default values see the description of the
     * X3MLEngineFactory class). */
    public void execute(){
        this.validateConfig();
        this.informUserAboutConfiguration();
        X3MLEngine engine=this.createX3MLEngine();
        X3MLEngine.REPORT_PROGRESS=this.progressReporting;
        Generator policy=X3MLGeneratorPolicy.load(this.getGeneratorPolicy(), X3MLGeneratorPolicy.createUUIDSource(this.uuidSize));
        Element sourceRoot=this.getInput();
        X3MLEngine.Output engineOutput = engine.execute(sourceRoot, policy);
        this.outputResults(engineOutput);
        this.outputAssociationTable();
    }
    
    /* creates an instance of the X3ML engine using the provided X3ML mappings file */
    private X3MLEngine createX3MLEngine(){
        try{
            for(File f : this.mappingsFiles){
                this.mappingStreams.add(new FileInputStream(f));
            }
            if(this.terminologyStream == null){
                return X3MLEngine.load(new ByteArrayInputStream(Utils.mergeMultipleMappingFiles(this.mappingStreams).getBytes()));
            }else{
                return X3MLEngine.load(new ByteArrayInputStream(Utils.mergeMultipleMappingFiles(this.mappingStreams).getBytes()), 
                                       this.terminologyStream.getLeft(), this.terminologyStream.getRight());
            }
        }catch(FileNotFoundException ex){
            throw exception("Cannot find X3ML mappings resources", ex);
        }
    }
    
    /* creates an input stream using the provided generator policy otherwise null 
    (no generator polixy file will be used)*/
    private InputStream getGeneratorPolicy(){
        return this.generatorPolicyStream;
    }
    
    /* parses the input (either it is a single file, multiple files, single folder or multiple folders).
    It uses all the given resources to produce a single input element (DOM) */
    private Element getInput(){
        try{
            for(String filepath : this.getInputFilesListing()){
                this.inputStreams.add(new FileInputStream(new File(filepath)));
            }
        }catch(FileNotFoundException ex){
            throw exception("Cannot find XML input file",ex);
        }
        if(inputStreams.isEmpty()){
            throw exception("The XML input file list is empty");
        }
        return Utils.parseMultipleXMLFiles(this.inputStreams);
    }
    
    /* Validates that the mandatory elements (input and mappings) have been provided */
    private void validateConfig(){
        if(this.mappingsFiles.isEmpty() && this.mappingStreams.isEmpty()){
            throw exception("The X3ML mappings x3ml are missing.");
        }
        if(this.inputStreams.isEmpty() && this.inputFolders.isEmpty()){
            throw exception("The input file(s) or folder(s) are missing.");
        }
    }
    
    private void outputResults(X3MLEngine.Output engineOutput){
        switch(this.output.getRight()){
            case RDF_XML:
                if(this.output.getLeft()==null){
                    engineOutput.writeXML(System.out);
                }else{
                    engineOutput.write(this.output.getLeft(), "application/rdf+xml");
                }
                break;
            case RDF_XML_PLAIN:
                if(this.output.getLeft()==null){
                    engineOutput.write(System.out,"application/rdf+xml_plain");
                }else{
                    engineOutput.write(this.output.getLeft(), "application/rdf+xml_plain");
                }
                break;
            case NTRIPLES:
                if(this.output.getLeft()==null){
                    engineOutput.write(System.out,"application/n-triples");
                }else{
                    engineOutput.write(this.output.getLeft(), "application/n-triples");
                }
                break;
            case TURTLE:
                if(this.output.getLeft()==null){
                    engineOutput.write(System.out,"text/turtle");
                }else{
                    engineOutput.write(this.output.getLeft(), "text/turtle");
                }
                break;
            case TRIG:
                if(this.output.getLeft()==null){
                    engineOutput.write(System.out,"application/trig");
                }else{
                    engineOutput.write(this.output.getLeft(), "application/trig");
                }
                break;
        }
    }
    
    /* Outputs (if configured to do so) the contents of the association table */
    private void outputAssociationTable(){
        try{
            if(this.associationTableFile!=null && !this.associationTableFile.isEmpty()){
                GeneratorContext.exportAssociationTable(this.associationTableFile);
            }
        }catch(IOException ex){
            throw exception("An error occured while exporting the contens of the association table in "
                           +"file "+this.associationTableFile, ex);
        }
    }
    
    /* prints - using logger - the configuration details */
    private void informUserAboutConfiguration(){
        LOGGER.info(X3MLEngine.retrieveX3MLEngineVersion());
        LOGGER.info("# X3ML Engine Mappings Files/Streams: "+this.mappingStreams.size());
        LOGGER.info("# Input Files/Streams: "+(this.inputStreams.size()+this.getInputFilesListing().size()));
        LOGGER.info("UUID size: "+this.uuidSize);
        LOGGER.info("Generator policy used: "+(this.getGeneratorPolicy()==null));
        String outputMsg=(this.output.getLeft()==null)?"System.out":"OutputStream";
        LOGGER.info("Output: "+outputMsg);
        LOGGER.info("Output format: "+this.output.getRight());
        LOGGER.info("Report Progress: "+this.progressReporting);
        String associationTableExportMsg=(this.associationTableFile==null || !this.associationTableFile.isEmpty())?"Disabled":"Enabled, file: "+this.associationTableFile;
        LOGGER.info("Export association table: "+associationTableExportMsg);
    }
    
    /* returns the input files (their absolute paths) taking into account all the files and the folders
    that has been provided by the user */
    private Collection<String> getInputFilesListing(){
        Set<String> retSet=new HashSet<>();
        for(Pair<File,Boolean> folder : this.inputFolders){
            for(File f : Utils.retrieveXMLfiles(folder.getLeft(), folder.getRight())){
                retSet.add(f.getAbsolutePath());
            }
        }
        return retSet;
    }
}