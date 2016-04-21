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

import eu.delving.x3ml.engine.Generator;
import eu.delving.x3ml.engine.Root;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Element;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.hp.hpl.jena.rdf.model.Model;
import eu.delving.x3ml.engine.Domain;
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
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static eu.delving.x3ml.engine.X3ML.Helper.x3mlStream;
import static eu.delving.x3ml.engine.X3ML.MappingNamespace;
import static eu.delving.x3ml.engine.X3ML.RootElement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * The engine is created from an X3ML file which is loaded from an input stream.
 *
 * It has an execute method which takes a DOM root node and a value generator
 * and produces a graph in its output.
 *
 * @author Gerald de Jong <gerald@delving.eu>
 * @author Nikos Minadakis <minadakn@ics.forth.gr>
 * @author Yannis Marketakis <marketak@ics.forth.gr>
 */

public class X3MLEngine {
    private static final String VERSION = "1.0";
    private RootElement rootElement;
    private NamespaceContext namespaceContext = new XPathContext();
    private List<String> prefixes = new ArrayList<String>();
    public static String exceptionMessagesList="";
    public static INPUT_TYPE inputType;

    public static List<String> validate(InputStream inputStream) {
        try{
            return validateStream(inputStream);
        }catch (SAXException e) {
            throw new X3MLException("Unable to validate: SAX", e);
        }catch (IOException e) {
            throw new X3MLException("Unable to validate: IO", e);
        }
    }

    public static X3MLEngine load(InputStream inputStream) throws X3MLException {
        InputStream is=validateX3MLMappings(inputStream);
        RootElement rootElement = (RootElement) x3mlStream().fromXML(is);
        if (!VERSION.equals(rootElement.version)) {
            throw exception("Incorrect X3ML Version "+rootElement.version+ ", expected "+VERSION);
        }
        return new X3MLEngine(rootElement);
    }
    
    /*validate that the X3ML mappings file is a valid XML file */
    private static InputStream validateX3MLMappings(InputStream inputStream) throws X3MLException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try{
            while ((len = inputStream.read(buffer)) > -1 ) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
        }catch(IOException ex){
            throw new X3MLException("Cannot read the contents of X3ML mappings file. Detailed log:\n"+ex.toString());
        }
        InputStream is = new ByteArrayInputStream(baos.toByteArray()); 
        try{
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        }catch(IOException | ParserConfigurationException | SAXException ex){
            throw new X3MLException("Cannot parse X3ML mappings file. Check that is is a valid XML file. Detailed log:\n"+ex.toString());
        }
        return new ByteArrayInputStream(baos.toByteArray());
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

    public Output execute(Element sourceRoot, Generator generator) throws X3MLException {
        inputType=INPUT_TYPE.XML;
        Root rootContext = new Root(sourceRoot, generator, namespaceContext, prefixes);
        generator.setDefaultArgType(rootElement.sourceType);
        generator.setLanguageFromMapping(rootElement.language);
        if (rootElement.namespaces != null) {
            for (MappingNamespace mn : rootElement.namespaces) {
                generator.setNamespace(mn.prefix, mn.uri);
            }
        }
        this.initializeAll();    
        rootElement.apply(rootContext);
        return rootContext.getModelOutput();
    }
    
    public Output execute(Model model, Generator generator) throws X3MLException {
        inputType=INPUT_TYPE.RDF;
        Root rootContext = new Root(model, generator, namespaceContext, prefixes);
        generator.setDefaultArgType(rootElement.sourceType);
        generator.setLanguageFromMapping(rootElement.language);
        if (rootElement.namespaces != null) {
            for (MappingNamespace mn : rootElement.namespaces) {
                generator.setNamespace(mn.prefix, mn.uri);
            }
        }
        rootElement.apply(rootContext);
        return rootContext.getModelOutput();
    }
    
    private void initializeAll(){
        X3MLEngine.exceptionMessagesList="";  
        RootElement.mappingCounter=0;
        RootElement.linkCounter=0;
        Domain.globalVariables.clear();
    }

    @Override
    public String toString() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + x3mlStream().toXML(rootElement);
    }

    public interface Output {

        void write(PrintStream printStream, String rdfFormat);

        void writeXML(PrintStream printStream);

        Model getModel();
        
        String[] toStringArray();

    }

    private X3MLEngine(RootElement rootElement) {
        this.rootElement = rootElement;
        if (this.rootElement.namespaces != null) {
            for (MappingNamespace namespace : this.rootElement.namespaces) {
                ((XPathContext) namespaceContext).addNamespace(namespace.prefix, namespace.uri);
                prefixes.add(namespace.prefix);
            }
            this.addDefaultNamespaces();
        }
    }
    
    private void addDefaultNamespaces(){
        ((XPathContext) namespaceContext).addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        prefixes.add("rdf");
        ((XPathContext) namespaceContext).addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        prefixes.add("rdfs");
    }

    private class XPathContext implements NamespaceContext {
        private Map<String, String> prefixUri = new TreeMap<String, String>();
        private Map<String, String> uriPrefix = new TreeMap<String, String>();

        void addNamespace(String prefix, String uri) {
            prefixUri.put(prefix, uri);
            uriPrefix.put(uri, prefix);
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
            List<String> list = new ArrayList<String>();
            list.add(prefix);
            return list.iterator();
        }
    }

    public static List<String> validateStream(InputStream inputStream) throws SAXException, IOException {
        Schema schema = schemaFactory().newSchema(new StreamSource(inputStream("x3ml_v1.0.xsd")));
        Validator validator = schema.newValidator();
        final List<String> errors = new ArrayList<String>();
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
        return X3MLEngine.class.getResourceAsStream("/validation/" + fileName);
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

