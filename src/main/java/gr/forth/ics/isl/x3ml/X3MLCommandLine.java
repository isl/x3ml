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

import gr.forth.ics.isl.x3ml.engine.Generator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import static gr.forth.ics.isl.x3ml.X3MLEngine.exception;
import gr.forth.ics.isl.x3ml.engine.GeneratorContext;
import gr.forth.Labels;
import gr.forth.Utils;
import gr.forth.ics.isl.x3ml_reverse_utils.AssociationTableResources;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.Lang;

/**
 * Using commons-cli to make the engine usable on the command line.
 *
 * @author Gerald de Jong &lt;gerald@delving.eu&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 */
@Log4j2
public class X3MLCommandLine {
    static final CommandLineParser PARSER = new PosixParser();
    static final HelpFormatter HELP = new HelpFormatter();
    static Options options = new Options();

    static void error(String message) {
        HELP.setDescPadding(5);
        HELP.setLeftPadding(5);
        HELP.printHelp(
                200,
                "x3ml -xml <input records> -x3ml <mapping file> hello",
                "Options",
                options,
                message
        );
        System.exit(1);
    }
    
    /* Creates the available options for parameterizing X3ML Engine from console */
    private static void createOptionsList(){
        Option inputOption = new Option(Labels.INPUT_SHORT, Labels.INPUT, true,
                "XML input records.\n Option A-single file: --"+Labels.INPUT+" input.xml\n"
                                   +" Option B-multiple files (comma-sep): --"+Labels.INPUT+" input1.xml,input2.xml,input3.xml\n"
                                   +" Option C-folder: --"+Labels.INPUT+" #_folder_path\n"
                                   +" Option D-URL: --"+Labels.INPUT+" @input_url\n"
                                   +" Option E-multiple URLs: --"+Labels.INPUT+" @input_url1,input_url2,input_url3\n"
                                   +" Option F-stdin: --"+Labels.INPUT+" @\n");
        inputOption.setRequired(true);
        
        Option x3mlOption = new Option(Labels.X3ML_SHORT, Labels.X3ML, true,
                "X3ML mapping definition. \n Option A-single file: --"+Labels.X3ML+" mapping.x3ml \n"
                                          +" Option B-multiple files (comma-sep): --"+Labels.X3ML+" mappings1.x3ml,mappings2.x3ml\n"
                                          +" Option C-URL: --"+Labels.X3ML+" @mappings_url\n"
                                          +" Option D-stdin: --"+Labels.X3ML+" @");
        x3mlOption.setRequired(true);
        
        Option outputOption = new Option(Labels.OUTPUT_SHORT, Labels.OUTPUT, true,
                "The output file name: --"+Labels.OUTPUT+" output.rdf"
        );
        
        Option policyOption = new Option(Labels.POLICY_SHORT, Labels.POLICY, true,
                "The value policy file: --"+Labels.POLICY+" policy.xml"
        );
        
        Option outputFormatOption = new Option(Labels.FORMAT_SHORT, Labels.FORMAT, true,
                "Output format. Options:\n --"+Labels.FORMAT+" application/rdf+xml (default)\n "
                                        +" --"+Labels.FORMAT+" application/n-triples \n"
                                        +" --"+Labels.FORMAT+" application/trig \n"
                                        +" --"+Labels.FORMAT+" text/turtle"
        );
        
        Option x3mlValidateOption = new Option(Labels.VALIDATE_SHORT, Labels.VALIDATE, false,
                "Only validate X3ML mapping file : --"+Labels.VALIDATE
        );
        
        Option uuidTestSizeOption = new Option(Labels.UUID_TEST_SIZE_SHORT, Labels.UUID_TEST_SIZE, true,
                "Create a test UUID generator of the given size. \n Default is UUID from operating system"
        );
        
        Option assocTableOption = new Option(Labels.ASSOC_TABLE_SHORT,Labels.ASSOC_TABLE, true, 
                "export the contents of the association table in XML format"
        );
        
        Option mergeAssocWithRDFOption = new Option(Labels.MERGE_WITH_ASSOCIATION_TABLE_SHORT,
                Labels.MERGE_WITH_ASSOCIATION_TABLE, false, 
                "merge the contents of the association table with the RDF output"
        );
        
        Option termsOption = new Option(Labels.TERMS_SHORT, Labels.TERMS, true, 
                "the SKOS taxonomy \n Option A-single file: --"+Labels.TERMS+" skosTerms.nt \n"
                                   +" Option B-URL: --"+Labels.TERMS+" @skos_terms_url\n");
        
        Option reportProgressOption = new Option(Labels.REPORT_PROGRESS_SHORT,Labels.REPORT_PROGRESS, false, 
                "reports the progress of the transformations"
        );
        Option versionNumberOption = new Option(Labels.VERSION_NUMBER_SHORT,Labels.VERSION_NUMBER, false, 
                "reports the version of x3ml engine"
        );
        
        options.addOption(inputOption)
               .addOption(x3mlOption)
               .addOption(outputOption)
               .addOption(outputFormatOption)
               .addOption(policyOption)
               .addOption(x3mlValidateOption)
               .addOption(uuidTestSizeOption)
               .addOption(assocTableOption)
               .addOption(mergeAssocWithRDFOption)
               .addOption(termsOption)
               .addOption(reportProgressOption)
               .addOption(versionNumberOption);
    }

    public static void main(String[] args) {
        createOptionsList();
        try {
            if(checkForVersionReporting(args)){
                System.out.println(X3MLEngine.retrieveX3MLEngineVersion());
            }else{
                CommandLine cli = PARSER.parse(options, args);

                int uuidTestSizeValue = -1;
                String uuidTestSizeString = cli.getOptionValue(Labels.UUID_TEST_SIZE);
                if (uuidTestSizeString != null) {
                    uuidTestSizeValue = Integer.parseInt(uuidTestSizeString);
                }
                go(
                    cli.getOptionValue(Labels.INPUT),
                    cli.getOptionValue(Labels.X3ML),
                    cli.getOptionValue(Labels.POLICY),
                    cli.getOptionValue(Labels.OUTPUT),
                    cli.getOptionValue(Labels.FORMAT),
                    cli.getOptionValue(Labels.TERMS),
                    cli.getOptionValue(Labels.MERGE_WITH_ASSOCIATION_TABLE),
                    cli.hasOption(Labels.ASSOC_TABLE),
                    cli.hasOption(Labels.REPORT_PROGRESS),
                    cli.hasOption(Labels.VALIDATE),
                    uuidTestSizeValue
                );
            }
        }
        catch (Exception e) {
            error(e.getMessage());
        }
    }

    static File file(String name) {
        File file = new File(name);
        if (!file.exists() || !file.isFile()) {
            error("File does not exist: " + name);
        }
        return file;
    }
    
    private static boolean checkForVersionReporting(String[] args) throws ParseException{
        List<String> arguments=Arrays.asList(args);
        return arguments.contains("-"+Labels.VERSION_NUMBER_SHORT) || arguments.contains("--"+Labels.VERSION_NUMBER);        
    }

    static DocumentBuilderFactory documentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory;
    }

    static Element xml(InputStream inputStream) {
        try {
            DocumentBuilder builder = documentBuilderFactory().newDocumentBuilder();
            Document document = builder.parse(inputStream);
            return document.getDocumentElement();
        }
        catch (Exception e) {
            throw exception("Unable to parse XML input. \nDetailed log: "
                            +e.toString()+"\n Please check that the XML Input file is valid");
        }
    }

    static Element xml(File file) {
        return xml(getStream(file));
    }

    static FileInputStream getStream(File file) {
        try {
            return new FileInputStream(file);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static Generator getValuePolicy(String policy, X3MLGeneratorPolicy.UUIDSource uuidSource) {
        FileInputStream stream = null;
        if (policy != null) {
            stream = getStream(file(policy));
        }
        return X3MLGeneratorPolicy.load(stream, uuidSource);
    }

    static PrintStream rdf(String file) {
        if (file != null) {
            try {
                return new PrintStream(new File(file));
            }
            catch (FileNotFoundException e) {
                error(e.getMessage());
                return null;
            }
        }
        else {
            return System.out;
        }
    }

    static void go(String input, String x3ml, String policy, String rdf, String rdfFormat, String terms, String assocTableFilename, boolean mergeAssocTableWithRDF, boolean reportProgress, boolean validateX3ML, int uuidTestSize) throws Exception {
        log.debug("Started executing X3MLEngine with the following parameters: "
                 +"\n\tInput: "+input
                 +"\n\tX3ML Mappings: "+x3ml
                 +"\n\tGenerator policy: "+policy
                 +"\n\tOutput: "+rdf
                 +"\n\tFormat: "+rdfFormat
                 +"\n\tTerminology: "+terms
                 +"\n\tAssociation table: "+assocTableFilename
                 +"\n\tReport progress: "+reportProgress
                 +"\n\tUUID Test Size: "+uuidTestSize
                 +"\n\tX3ML Mappings (for validation): "+validateX3ML
                 +"\n\tMerge Association table with output: "+mergeAssocTableWithRDF) ;
        final String INPUT_FOLDER_PREFIX="#_";
        final String INPUT_PIPED="@";
        Element xmlElement;
        
        /* Check for validation */
        if(validateX3ML){
            try{
                InputStream validationResultsStream=X3MLEngine.validateX3MLMappings(new FileInputStream(x3ml));
                System.out.println("OK");
                System.exit(1);
            }catch(X3MLEngine.X3MLException ex){
                System.err.println(ex.getMessage());
                System.exit(-1);
            }
        }
        
        /* Read the input resource */
        if (INPUT_PIPED.equals(input)) {
            xmlElement = xml(System.in);
        }else if(input.startsWith("@")){  //It contains URLs
            if(input.contains(",")){  // it contains multiple URLs
                Set<InputStream> listOfStreams=new HashSet<>();
                for(String remoteURL : input.replace("@", "").split(",")){
                    listOfStreams.add(new URL(remoteURL).openStream());
                }
                xmlElement=Utils.parseMultipleXMLFiles(listOfStreams);
            }else{  //it contains one URL
                xmlElement = xml(new URL(input.replace("@", "")).openStream());
            }
        }else if(input.contains(",")){
            Set<InputStream> listOfStreams=new HashSet<>();
            try{
                for(String filePath : input.split(",")){
                    listOfStreams.add(new FileInputStream(new File(filePath)));
                }
                xmlElement=Utils.parseMultipleXMLFiles(listOfStreams);
            }catch(FileNotFoundException ex){
                throw exception("Cannot find input files",ex);
            }
        }else if(input.startsWith(INPUT_FOLDER_PREFIX)){
            xmlElement=Utils.parseFolderWithXmlFiles(input.replace(INPUT_FOLDER_PREFIX, ""), true);
        }
        else{
            xmlElement = xml(file(input));
        }
        
        /* Read the X3ML mappings resources */
        InputStream x3mlStream;
        if ("@".equals(x3ml)) {
            x3mlStream = System.in;
        }else if(x3ml.startsWith("@")){  //It contains URLs
            if(x3ml.contains(",")){  // it contains multiple URLs
                Set<InputStream> mappingInputStreams=new HashSet<>();
                for(String mappingsUrl : x3ml.replace("@", "").split(",")){
                    mappingInputStreams.add(new URL(mappingsUrl).openStream());
                }
                x3mlStream=new ByteArrayInputStream(Utils.mergeMultipleMappingFiles(mappingInputStreams).getBytes());
            }else{  //it contains one URL
                x3mlStream = new URL(x3ml.replace("@", "")).openStream();
            }
        }
        else if(x3ml.contains(",")){
            Set<InputStream> mappingInputStreams=new HashSet<>();
            for(String mappingsFile : x3ml.split(",")){
                mappingInputStreams.add(new FileInputStream(new File(mappingsFile)));
            }
            x3mlStream=new ByteArrayInputStream(Utils.mergeMultipleMappingFiles(mappingInputStreams).getBytes());
        }else{
            x3mlStream = getStream(file(x3ml));
        }
        
        /* Read the SKOS terminology (if it exists) */
        Pair<InputStream,Lang> terminologyPair=null;
        if(terms!=null){
            if(terms.startsWith("@")){  //load terminology from a URL
                terminologyPair=Utils.getTerminologyResourceDetails(terms.replace("@", ""));
            }else{  //load terminology from file
                terminologyPair=Utils.getTerminologyResourceDetails(terms);
            }
        }
        
        X3MLEngine engine;
        if(terminologyPair!=null){  //there exists a terminology resource
            engine = X3MLEngine.load(x3mlStream, terminologyPair.getLeft(), terminologyPair.getRight());
        }else{                      // no terminologies exist
            engine = X3MLEngine.load(x3mlStream);
        }
        
        X3MLEngine.REPORT_PROGRESS=reportProgress;
        
        X3MLEngine.Output output = engine.execute(
                xmlElement,
                getValuePolicy(policy, X3MLGeneratorPolicy.createUUIDSource(uuidTestSize))
        );
        if(assocTableFilename!=null){
            try{
                GeneratorContext.exportAssociationTable(assocTableFilename);
            }catch(IOException ex){
                exception("cannot export the contents of the association table",ex);
            }
        }
        if(mergeAssocTableWithRDF){
            output.getModel().add(output.getModel().createResource(AssociationTableResources.ASSOCIATION_TABLE_ENTRY_URI,output.getModel().createResource(AssociationTableResources.ASSOCIATION_TABLE_ENTRY_CLASS)),
                                  output.getModel().createProperty(AssociationTableResources.ASSOCIATION_TABLE_ENTRY_PROPERTY),
                                  GeneratorContext.exportAssociationTableToString().replaceAll("\"", "'")
                                                                                   .replace(AssociationTableResources.ASSOCIATION_TABLE_START_TAG, "")
                                                                                   .replace(AssociationTableResources.ASSOCIATION_TABLE_END_TAG, ""));
        }
        output.write(rdf(rdf), rdfFormat);
    }
}
