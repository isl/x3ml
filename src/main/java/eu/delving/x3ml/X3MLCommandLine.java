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
package eu.delving.x3ml;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
import eu.delving.x3ml.engine.Generator;
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
import java.util.List;
import static eu.delving.x3ml.X3MLEngine.exception;
import eu.delving.x3ml.engine.GeneratorContext;
import gr.forth.ics.isl.x3ml_reverse_utils.AssociationTableResources;
import java.io.IOException;

/**
 * Using commons-cli to make the engine usable on the command line.
 *
 * @author Gerald de Jong <gerald@delving.eu>
 * @author Nikos Minadakis <minadakn@ics.forth.gr>
 * @author Yannis Marketakis <marketak@ics.forth.gr>
 */

public class X3MLCommandLine {
    static final CommandLineParser PARSER = new PosixParser();
    static final HelpFormatter HELP = new HelpFormatter();
    static Options options = new Options();
    private static final String ASSOCIATION_TABLE_PARAMETER_NAME="assocTable";
    private static final String ASSOCIATION_TABLE_TO_RDF_PARAMETER_NAME="mergeAssocWithRDF";

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

    public static void main(String[] args) {
        Option xml = new Option(
                "xml", true,
                "XML input records.\n Option A-single file: -xml input.xml\n"
                                   +" Option B-stdin: -xml @ \n"
                                   +" Option C-multiple files (comma-sep): -xml input1.xml,input2.xml,input3.xml");
        xml.setRequired(true);
        Option x3ml = new Option(
                "x3ml", true,
                "X3ML mapping definition: -x3ml mapping.x3ml (@ = stdin)"
        );
        x3ml.setRequired(true);
        Option rdf = new Option(
                "rdf", true,
                "The RDF output file name: -rdf output.rdf"
        );
        Option policy = new Option(
                "policy", true,
                "The value policy file: -policy policy.xml"
        );
        Option rdfFormat = new Option(
                "format", true,
                "Output format. Options:\n -format application/n-triples\n "
                                        +" -format text/turtle \n"
                                        +" -format application/rdf+xml (default)"
        );
        Option validate = new Option(
                "validate", false,
                "Validate X3ML v1.0 using XSD"
        );
        Option uuidTestSize = new Option(
                "uuidTestSize", true,
                "Create a test UUID generator of the given size. \n Default is UUID from operating system"
        );
        Option assocTable = new Option(
                ASSOCIATION_TABLE_PARAMETER_NAME, true, 
                "export the contents of the association table in XML format"
        );
        Option mergeAssocWithRDF = new Option(
                ASSOCIATION_TABLE_TO_RDF_PARAMETER_NAME, false, 
                "merge the contents of the association table with the RDF output"
        );
        options.addOption(rdfFormat).addOption(rdf).addOption(x3ml).addOption(xml).addOption(policy)
                .addOption(validate).addOption(uuidTestSize).addOption(assocTable).addOption(mergeAssocWithRDF);
        try {
            CommandLine cli = PARSER.parse(options, args);
            int uuidTestSizeValue = -1;
            String uuidTestSizeString = cli.getOptionValue("uuidTestSize");
            if (uuidTestSizeString != null) {
                uuidTestSizeValue = Integer.parseInt(uuidTestSizeString);
            }
            go(
                    cli.getOptionValue("xml"),
                    cli.getOptionValue("x3ml"),
                    cli.getOptionValue("policy"),
                    cli.getOptionValue("rdf"),
                    cli.getOptionValue("format"),
                    cli.getOptionValue(ASSOCIATION_TABLE_PARAMETER_NAME),
                    cli.hasOption(ASSOCIATION_TABLE_TO_RDF_PARAMETER_NAME),
                    cli.hasOption("validate"),
                    uuidTestSizeValue
            );
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
    
    public static Model model(String path){
        return FileManager.get().loadModel(path);
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

    static void go(String xml, String x3ml, String policy, String rdf, String rdfFormat, String assocTableFilename, boolean mergeAssocTableWithRDF, boolean validate, int uuidTestSize) {
        Model inputModel=model(xml);
//        Element xmlElement;
//        if ("@".equals(xml)) {
//            xmlElement = xml(System.in);
//        }
//        else if(xml.contains(",")){
//            Set<InputStream> listOfStreams=new HashSet<>();
//            try{
//                for(String filePath : xml.split(",")){
//                    listOfStreams.add(new FileInputStream(new File(filePath)));
//                }
//                xmlElement=Utils.parseMultipleXMLFiles(listOfStreams);
//            }catch(FileNotFoundException ex){
//                throw exception("Cannot find input files",ex);
//            }
//        }
//        else{
//            xmlElement = xml(file(xml));
//        }
        InputStream x3mlStream;
        if ("@".equals(x3ml)) {
            if (validate) {
                throw exception("Cannot validate when X3ML is piped");
            }
            x3mlStream = System.in;
        }
        else {
            if (validate) {
                List<String> errors = X3MLEngine.validate(getStream(file(x3ml)));
                if (!errors.isEmpty()) {
                    System.out.println("Validation:");
                    for (String error : errors) {
                        System.out.println(error);
                    }
                    return;
                }
            }
            x3mlStream = getStream(file(x3ml));
        }
        X3MLEngine engine = X3MLEngine.load(x3mlStream);
        /* This is being used for the XML input */
//        X3MLEngine.Output output = engine.execute(
//                xmlElement,
//                getValuePolicy(policy, X3MLGeneratorPolicy.createUUIDSource(uuidTestSize))
//        );
        X3MLEngine.Output output = engine.execute(
                inputModel,
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
