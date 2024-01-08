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
import gr.forth.ics.isl.x3ml.engine.Root;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Element;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.DatasetGraphSimpleMem;
import gr.forth.ics.isl.x3ml.engine.Domain;
import gr.forth.ics.isl.x3ml.engine.X3ML;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static gr.forth.ics.isl.x3ml.engine.X3ML.Helper.x3mlStream;
import static gr.forth.ics.isl.x3ml.engine.X3ML.MappingNamespace;
import static gr.forth.ics.isl.x3ml.engine.X3ML.RootElement;
import gr.forth.ics.isl.x3ml.engine.X3ML.TargetInfo;
import gr.forth.Labels;
import gr.forth.Utils;
import gr.forth.ics.isl.x3ml.engine.ModelOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.Lang;

/**
 * The engine is created from an X3ML file which is loaded from an input stream.
 *
 * It has an execute method which takes a DOM root node and a value generator
 * and produces a graph in its output.
 *
 * @author Gerald de Jong &lt;gerald@delving.eu&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 */
@Log4j2
public class X3MLEngine {
    private static final String VERSION = "1.0";
    private static final String X3ML_SCHEMA_FOLDER="/schema/";
    private static final String X3ML_SCHEMA_FILENAME="x3ml.xsd";
    private static final String GENERATOR_POLICY_SCHEMA_FILENAME="generatorPolicy.xsd";
    public static boolean ENABLE_ASSOCIATION_TABLE=false;
    public static boolean REPORT_PROGRESS=false;
    private RootElement rootElement;
    private NamespaceContext namespaceContext = new XPathContext();
    private List<String> prefixes = new ArrayList<>();
    public static String exceptionMessagesList="";
    private static Pair<InputStream,Lang> terminologyStream=null;
    public static final String PROJECT_PROPERTIES_FILENAME="x3ml.project.properties";
    public static final String PROJECT_PROPERTIES_VERSION_PROPERTY="X3ML_ENGINE_VERSION";
    

    public static List<String> validate(InputStream inputStream) {
        try{
            return validateStream(inputStream);
        }catch (SAXException e) {
            throw new X3MLException("Unable to validate: SAX", e);
        }catch (IOException e) {
            throw new X3MLException("Unable to validate: IO", e);
        }
    }

    /** The method is responsible for loading X3ML mappings, that are given as 
     * an InputStream, and then: (a) validate them with respect to the X3ML schema and
     * (b) construct the corresponding X3MLEngine instance. 
     * 
     * @param mappingsStream the X3ML mappings contents as a stream
     * @return an X3MLEngine instance
     * @throws X3MLException for any error that might occur during validation, instantiation. */
    public static X3MLEngine load(InputStream mappingsStream) throws X3MLException {
        X3MLEngine.terminologyStream=null;
        InputStream is=validateX3MLMappings(mappingsStream);
        RootElement rootElement = (RootElement) x3mlStream().fromXML(is);
        rootElement=Utils.parseX3MLAgainstVariables(rootElement);
        if (!VERSION.equals(rootElement.version)) {
            throw exception("Incorrect X3ML Version "+rootElement.version+ ", expected "+VERSION);
        }
        return new X3MLEngine(rootElement);
    }
    
    /** The method is responsible for loading X3ML mappings and SKOS terminology, that are given as 
     *  InputStream instances and then: 
     * (a) validate the X3ML mappings with respect to the X3ML schema, 
     * (b) load the SKOS terminology 
     * (c) construct the corresponding X3MLEngine instance. 
     * 
     * @param mappingsStream the X3ML mappings contents as a stream
     * @param terminologyStream the SKOS terminology 
     * @param terminologyLang the serialization format of the SKOS terminology
     * @return an X3MLEngine instance
     * @throws X3MLException for any error that might occur during validation, instantiation. */
    public static X3MLEngine load(InputStream mappingsStream, InputStream terminologyStream, Lang terminologyLang) throws X3MLException {
        X3MLEngine.terminologyStream=Pair.of(terminologyStream, terminologyLang);
        InputStream is=validateX3MLMappings(mappingsStream);
        RootElement rootElement = (RootElement) x3mlStream().fromXML(is);
        rootElement=Utils.parseX3MLAgainstVariables(rootElement);
        if (!VERSION.equals(rootElement.version)) {
            throw exception("Incorrect X3ML Version "+rootElement.version+ ", expected "+VERSION);
        }
        return new X3MLEngine(rootElement);
    }
    
    /** Validate that the X3ML mappings file is a valid XML file and is compliant with 
     * the X3ML schema. 
     * 
     * @param inputStream the X3ML mappings file as an input stream
     * @return the validated inputStream (returned because the offset of the initial inputStream has been moved to EOF)
     * @throws X3MLException if the X3ML mappings file is not valid */
    public static InputStream validateX3MLMappings(InputStream inputStream) throws X3MLException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayOutputStream baosForValidation = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try{
            while ((len = inputStream.read(buffer)) > -1 ) {
                baos.write(buffer, 0, len);
                baosForValidation.write(buffer, 0, len);
            }
            baos.flush();
            baosForValidation.flush();
        }catch(IOException ex){
            throw new X3MLException("Cannot read the contents of X3ML mappings file. Detailed log:\n"+ex.toString());
        }
        InputStream is = new ByteArrayInputStream(baos.toByteArray()); 
        try{
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        }catch(IOException | ParserConfigurationException | SAXException ex){
            throw new X3MLException("Cannot parse X3ML mappings file. Check that is is a valid XML file. Detailed log:\n"+ex.toString());
        }
        X3MLEngine.validateX3MLMappingsWithSchema(new ByteArrayInputStream(baosForValidation.toByteArray()));
        return new ByteArrayInputStream(baos.toByteArray());
    }
    
    /* validates the X3ML mappings with respect to X3ML schema */
    private static void validateX3MLMappingsWithSchema(InputStream inputStream){
        try{
            URL schemaUrl=X3MLEngine.class.getResource(X3MLEngine.X3ML_SCHEMA_FOLDER+X3MLEngine.X3ML_SCHEMA_FILENAME);
            Source x3mlFile=new StreamSource(inputStream);
            SchemaFactory schemaFactory=SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema=schemaFactory.newSchema(schemaUrl);
            Validator validator=schema.newValidator();
            validator.validate(x3mlFile);
        }catch(SAXException | IOException ex){
            throw exception("An error occurred while validating X3ML mappings file", ex);
        }
    }
    
    public void useAssociationTable(boolean flag){
        X3MLEngine.ENABLE_ASSOCIATION_TABLE=flag;
    }

    public static void save(X3MLEngine engine, OutputStream outputStream) throws X3MLException {
        x3mlStream().toXML(engine.rootElement, outputStream);
    }

    public static X3MLException exception(String message) {
        return new X3MLException(message);
    }

    public static X3MLException exception(String message, Throwable throwable) {
        return new X3MLException(message, throwable);
    }
    
    public static String retrieveX3MLEngineVersion(){
        try{
            Properties propertiesFile=new Properties();
            propertiesFile.load(X3MLCommandLine.class.getClassLoader().getResourceAsStream(X3MLEngine.PROJECT_PROPERTIES_FILENAME));
            return "X3ML Engine version: "+propertiesFile.getProperty(X3MLEngine.PROJECT_PROPERTIES_VERSION_PROPERTY);
        }catch(IOException ex){
            log.error("An error occured while retrieving X3ML Engine version",ex);
            throw new X3MLException("An error occured while retrieving X3ML Engine version",ex);
        }
    }

    public Output execute(Element sourceRoot, Generator generator) throws X3MLException {
        Root rootContext = new Root(sourceRoot, generator, namespaceContext, prefixes, terminologyStream);
        generator.setDefaultArgType(rootElement.sourceType);
        generator.setLanguageFromMapping(rootElement.language);
        if (rootElement.namespaces != null) {
            for (MappingNamespace mn : rootElement.namespaces) {
                generator.setNamespace(mn.prefix, mn.uri);
            }
        }
        if(this.rootElement.info !=null){
            for(X3ML.SourceInfo sourceInfoBlock : this.rootElement.info.source.source_info){
                if(sourceInfoBlock.namespaces != null){
                    for(MappingNamespace namespace : sourceInfoBlock.namespaces){
                        generator.setNamespace(namespace.prefix, namespace.uri);
                    }
                }
            }
            for(TargetInfo targetInfoBlock : this.rootElement.info.target.target_info){
                if(targetInfoBlock.namespaces != null){
                    for(MappingNamespace namespace : targetInfoBlock.namespaces){
                        generator.setNamespace(namespace.prefix, namespace.uri);
                    }
                }
            }
        }
        this.initializeAll();  
        rootElement.apply(rootContext);
        return rootContext.getModelOutput();
    }
    
    private void initializeAll(){
        X3MLEngine.exceptionMessagesList="";  
        RootElement.mappingCounter=0;
        RootElement.linkCounter=0;
        Domain.globalVariables.clear();
        ModelOutput.quadGraph=new DatasetGraphSimpleMem();
    }

    @Override
    public String toString() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + x3mlStream().toXML(rootElement);
    }

    public interface Output {

        void write(OutputStream outputStream, String rdfFormat);

        void writeXML(OutputStream outputStream);

        Model getModel();
        
        String[] toStringArray();

        void close();
    }

    private X3MLEngine(RootElement rootElement) {
        this.rootElement = rootElement;
        if (this.rootElement.namespaces != null) {
            for (MappingNamespace namespace : this.rootElement.namespaces) {
                ((XPathContext) namespaceContext).addNamespace(namespace.prefix, namespace.uri);
                if(!namespace.prefix.isEmpty()){
                    prefixes.add(namespace.prefix);
                }
            }
        }
        if(this.rootElement.info !=null){
            for(X3ML.SourceInfo sourceInfoBlock : this.rootElement.info.source.source_info){
                if(sourceInfoBlock.namespaces != null){
                    for(MappingNamespace namespace : sourceInfoBlock.namespaces){
                        ((XPathContext)namespaceContext).addNamespace(namespace.prefix, namespace.uri);
                    }
                }
            }
            for(TargetInfo targetInfoBlock : this.rootElement.info.target.target_info){
                if(targetInfoBlock.namespaces != null){
                    for(MappingNamespace namespace : targetInfoBlock.namespaces){
                        ((XPathContext)namespaceContext).addNamespace(namespace.prefix, namespace.uri);
                        if(!namespace.prefix.isEmpty()){
                            prefixes.add(namespace.prefix);
                        }
                    }
                }
            }
        }
        this.addDefaultNamespaces();
    }
    
    private void addDefaultNamespaces(){
        ((XPathContext) namespaceContext).addNamespace(Labels.RDF, Labels.RDF_NAMESPACE);
        prefixes.add(Labels.RDF);
        ((XPathContext) namespaceContext).addNamespace(Labels.RDFS, Labels.RDFS_NAMESPACE);
        prefixes.add(Labels.RDFS);
        ((XPathContext) namespaceContext).addNamespace(Labels.XML, Labels.XML_NAMESPACE);
        prefixes.add(Labels.XML);
        ((XPathContext) namespaceContext).addNamespace(Labels.SKOS, Labels.SKOS_NAMESPACE);
        prefixes.add(Labels.SKOS);
    }

    private class XPathContext implements NamespaceContext {
        private Map<String, String> prefixUri = new TreeMap<>();
        private Map<String, String> uriPrefix = new TreeMap<>();

        void addNamespace(String prefix, String uri) {
            validateNamespace(prefix, uri);
            log.debug("Adding namespace with prefix '"+prefix+"' and URI '"+uri+"'");
            if(!prefix.trim().isEmpty() && !uri.trim().isEmpty()){
                prefixUri.put(prefix, uri);
                uriPrefix.put(uri, prefix);
            }
        }

        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new X3MLException("Null prefix!");
            }
            return prefixUri.get(prefix);
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return uriPrefix.get(namespaceURI);
        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            String prefix = getPrefix(namespaceURI);
            if (prefix == null) return null;
            List<String> list = new ArrayList<>();
            list.add(prefix);
            return list.iterator();
        }
        
        private void validateNamespace(String prefix, String uri){
            if(prefix.trim().isEmpty() && uri.trim().isEmpty()){
                log.warn("Possible wrong namespace declaration. The prefix and the URI of a namespace are empty");
            }else if(prefix.trim().isEmpty()){
                String uriMessageValue=(uri.trim().isEmpty())?"(empty)":"\""+uri+"\"";
                log.error("Invalid namespace declaration: the prefix of a namespace cannot be empty [Prefix: (empty), URI: "+uriMessageValue+"]");
                throw exception("Invalid namespace declaration: the prefix of a namespace cannot be empty [Prefix: (empty), URI: "+uriMessageValue+"]");
            }else if(uri.trim().isEmpty()){
                String prefixMessageValue=(prefix.trim().isEmpty())?"(empty)":"\""+prefix+"\"";
                log.error("Invalid namespace declaration: the URI of a namespace cannot be empty [Prefix: "+prefixMessageValue+", URI: (empty)]");
                throw exception("Invalid namespace declaration: the URI of a namespace cannot be empty [Prefix: "+prefixMessageValue+", URI: (empty)]");
            }
        }
    }

    public static List<String> validateStream(InputStream inputStream) throws SAXException, IOException {
        Schema schema = schemaFactory().newSchema(new StreamSource(inputStream(X3ML_SCHEMA_FILENAME)));
        Validator validator = schema.newValidator();
        final List<String> errors = new ArrayList<>();
        validator.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                errors.add(errorMessage(exception));
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                errors.add(errorMessage(exception));
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                errors.add(errorMessage(exception));
            }
        });
        StreamSource source = new StreamSource(inputStream);
        validator.validate(source);
        return errors;
    }

    private static SchemaFactory schemaFactory() {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            schemaFactory.setResourceResolver(new ResourceResolver());
        }
        catch (Exception e) {
            throw new RuntimeException("Configuring schema factory", e);
        }
        return schemaFactory;
    }

    private static String errorMessage(SAXParseException e) {
        return String.format(
                "%d:%d - %s",
                e.getLineNumber(), e.getColumnNumber(), e.getMessage()
        );
    }

    public static class ResourceResolver implements LSResourceResolver {

        @Override
        public LSInput resolveResource(String type, final String namespaceUri, final String publicId, final String systemId, final String baseUri) {
            return new ResourceInput(publicId, systemId, baseUri);
        }

        private static class ResourceInput implements LSInput {

            private String publicId, systemId, baseUri;

            private ResourceInput(String publicId, String systemId, String baseUri) {
                this.publicId = publicId;
                this.systemId = systemId;
                this.baseUri = baseUri;
            }

            @Override
            public Reader getCharacterStream() {
                try {
                    return new InputStreamReader(getByteStream(), "UTF-8");
                }
                catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void setCharacterStream(Reader reader) {
                throw new RuntimeException();
            }

            @Override
            public InputStream getByteStream() {
                return inputStream(systemId);
            }

            @Override
            public void setByteStream(InputStream inputStream) {
                throw new RuntimeException();
            }

            @Override
            public String getStringData() {
                try {
                    return IOUtils.toString(getByteStream());
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void setStringData(String s) {
                throw new RuntimeException();
            }

            @Override
            public String getSystemId() {
                return systemId;
            }

            @Override
            public void setSystemId(String s) {
                this.systemId = s;
            }

            @Override
            public String getPublicId() {
                return publicId;
            }

            @Override
            public void setPublicId(String s) {
                this.publicId = s;
            }

            @Override
            public String getBaseURI() {
                return baseUri;
            }

            @Override
            public void setBaseURI(String s) {
                throw new RuntimeException();
            }

            @Override
            public String getEncoding() {
                return "UTF-8";
            }

            @Override
            public void setEncoding(String s) {
                throw new RuntimeException();
            }

            @Override
            public boolean getCertifiedText() {
                return false;
            }

            @Override
            public void setCertifiedText(boolean b) {
                throw new RuntimeException();
            }
        }
    }

    private static InputStream inputStream(String fileName) {
        return X3MLEngine.class.getResourceAsStream(X3ML_SCHEMA_FOLDER + fileName);
    }

    public static class X3MLException extends RuntimeException {

        public X3MLException(String s) {
            super(s);
        }

        public X3MLException(String s, Throwable throwable) {
            super(s, throwable);
        }
    }
}