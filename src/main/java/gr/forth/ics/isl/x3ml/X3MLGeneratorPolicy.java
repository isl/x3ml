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

import com.damnhandy.uri.template.MalformedUriTemplateException;
import com.damnhandy.uri.template.UriTemplate;
import com.damnhandy.uri.template.VariableExpansionException;
import gr.forth.ics.isl.x3ml.engine.Generator;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static gr.forth.ics.isl.x3ml.engine.X3ML.*;
import static gr.forth.ics.isl.x3ml.engine.X3ML.Helper.generatorStream;
import static gr.forth.ics.isl.x3ml.engine.X3ML.Helper.typedLiteralValue;
import static gr.forth.ics.isl.x3ml.engine.X3ML.Helper.uriValue;
import static gr.forth.ics.isl.x3ml.engine.X3ML.SourceType.constant;
import static gr.forth.ics.isl.x3ml.engine.X3ML.SourceType.xpath;
import gr.forth.Labels;
import gr.forth.Utils;
import static gr.forth.ics.isl.x3ml.engine.X3ML.Helper.literalValue;
import gr.forth.TextualContent;
import gr.forth.UriValidator;
import gr.forth.ics.isl.x3ml.X3MLEngine.X3MLException;
import static gr.forth.ics.isl.x3ml.X3MLEngine.exception;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import lombok.extern.log4j.Log4j;
import org.xml.sax.SAXException;

/**
 * @author Gerald de Jong &lt;gerald@delving.eu&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 */
@Log4j
public class X3MLGeneratorPolicy implements Generator {
    private static final Pattern BRACES = Pattern.compile("\\{[?;+#]?([^}]+)\\}");
    private static final String GENERATOR_POLICY_SCHEMA_FOLDER="/schema/";
    private static final String GENERATOR_POLICY_SCHEMA_FILENAME="generatorPolicy.xsd";
    private Map<String, GeneratorSpec> generatorMap = new TreeMap<>();
    private Map<String, String> namespaceMap = new TreeMap<>();
    private UUIDSource uuidSource;
    private SourceType defaultSourceType;
    private String languageFromMapping;

    public interface CustomGenerator {
        /**Updates the custom generator values. In particular it updates the argument 
         * with the given name. 
         * 
         * @param name the name of the argument of the custom generator
         * @param value the value of the argument (it can be taken from the input, defined by the user, etc.)
         * @throws CustomGeneratorException if any of the mandatory fields are missing (i.e. an argument is null)*/
        void setArg(String name, String value) throws CustomGeneratorException;
        
        /**Indicates that the custom generator uses a prefix (from the namespaces section).
         The prefix is particularly used for constructing URIs.*/
        void usesNamespacePrefix();
        
        /**Returns the value that has been generated from the custom generator
         * 
         * @return the generated value
         * @throws CustomGeneratorException if any of the mandatory fields are missing (i.e. an argument is null)*/
        String getValue() throws CustomGeneratorException;

        /**Returns the type of the generated value (i.e. URI, UUID, Literal, etc.)
         * 
         * @return the type of the generated value 
         * @throws CustomGeneratorException if any of the mandatory fields are missing (i.e. an argument is null)*/
        String getValueType() throws CustomGeneratorException;

        /**Indicates whether the custom generator supports merging when multiple values exist.
         * This options refers to custom generators, that retrieve their values from the input 
         * (i.e. using XPath expressions) and there are multiple results from the input (i.e. multiple elements)
         * 
         * @return true if the custom generator supports merging multiple values, otherwise false */
        boolean mergeMultipleValues();
    }

    public static class CustomGeneratorException extends Exception {
        public CustomGeneratorException(String message) {
            super(message);
        }
    }

    public static X3MLGeneratorPolicy load(InputStream inputStream, UUIDSource uuidSource) {
        return new X3MLGeneratorPolicy(inputStream, uuidSource);
    }

    public static UUIDSource createUUIDSource(int uuidSize) {
        return uuidSize > 0 ? new TestUUIDSource(uuidSize) : new RealUUIDSource();
    }

    private X3MLGeneratorPolicy(InputStream inputStream, UUIDSource uuidSource) {
        if (inputStream != null) {
            InputStream is=validateGeneratorPolicyFile(inputStream);
            GeneratorPolicy policy = (GeneratorPolicy) generatorStream().fromXML(is);
            for (GeneratorSpec generator : policy.generators) {
                if (generatorMap.containsKey(generator.name)) {
                    throw exception("Duplicate generator name: " + generator.name);
                }
                if(generator.type!=null && !generator.type.equalsIgnoreCase(Labels.INSTANCE) && !generator.type.equalsIgnoreCase(Labels.LABEL) && !generator.type.equalsIgnoreCase(Labels.ANY)){
                    throw exception("Unknown type for gerator: " + generator.name+". The declared type is \""+generator.type+"\" which is uknown. One of the following types are allowed {"+Labels.INSTANCE+" | "+Labels.LABEL+" | "+Labels.ANY+"}");
                }    
                generatorMap.put(generator.name, generator);
            }
        }
        if ((this.uuidSource = uuidSource) == null) throw exception("UUID Source needed");
    }

    @Override
    public void setDefaultArgType(SourceType sourceType) {
        this.defaultSourceType = sourceType;
    }

    @Override
    public void setLanguageFromMapping(String language) {
        if (language != null) {
            this.languageFromMapping = language;
        }
    }

    @Override
    public String getLanguageFromMapping() {
        return languageFromMapping;
    }

    @Override
    public void setNamespace(String prefix, String uri) {
        namespaceMap.put(prefix, uri);
    }

    @Override
    public GeneratedValue generate(GeneratorElement generatorElem, ArgValues argValues) {
        String argDefaultValue=Labels.TEXT;
        String name=generatorElem.getName();
        if (name == null) {
            throw exception("Value function name missing");
        }
        if (Labels.UUID.equals(name)) {
            return uriValue(uuidSource.generateUUID());
        }

        if ("namedgraphURI".equals(name)) {
            return uriValue(argValues.getArgValue("text", constant, false).string);
        }
        if (Labels.LITERAL.equals(name)) {
            ArgValue value = argValues.getArgValue(argDefaultValue, xpath, false);
            
            if (value == null) {
                throw exception(Utils.produceLabelGeneratorMissingArgumentError(generatorElem, argDefaultValue));
            }
            if (value.string == null || value.string.isEmpty()) {
                throw exception(Utils.produceLabelGeneratorEmptyArgumentError(generatorElem));
            }
            return literalValue(value.string, getLanguage(value.language, argValues));
        }
        if (Labels.PREF_LABEL.equals(name)) {
            ArgValue value = argValues.getArgValue(argDefaultValue, xpath, false);
            if (value == null) {
                throw exception(Utils.produceLabelGeneratorMissingArgumentError(generatorElem, argDefaultValue));
            }
            if (value.string == null || value.string.isEmpty()) {
                throw exception(Utils.produceLabelGeneratorEmptyArgumentError(generatorElem));
            }
            return literalValue(value.string, getLanguage(value.language, argValues));
        }
        if (Labels.CONSTANT.equals(name)) {
            ArgValue value = argValues.getArgValue(argDefaultValue, constant, false);
            if (value == null) {
                throw exception(Utils.produceLabelGeneratorMissingArgumentError(generatorElem, argDefaultValue));
            }
            return literalValue(value.string, getLanguage(value.language, argValues));
        }
        GeneratorSpec generator = generatorMap.get(name);
        if (generator == null) throw exception("No generator for " + name);
        if (generator.custom != null) {
            return fromCustomGenerator(generator, argValues);
        }
        else if (generator.prefix != null) { // use URI template
            String namespaceUri = namespaceMap.get(generator.prefix);
            if (namespaceUri == null) {
                throw exception("No namespace for prefix "+ generator.prefix + "in generator policy");
            }
            return fromURITemplate(generator, namespaceUri, argValues);
        }
        else { // use simple substitution
            return fromSimpleTemplate(generator, argValues);
        }
    }

    private GeneratedValue fromCustomGenerator(GeneratorSpec generator, ArgValues argValues) {
        String className = generator.custom.generatorClass;
        try {
            Class<?> customClass = Class.forName(className);
            Constructor<?> constructor = customClass.getConstructor();
            CustomGenerator instance = (CustomGenerator) constructor.newInstance();
            if(generator.prefix!=null){
                instance.usesNamespacePrefix();
            }
            for (CustomArg customArg : generator.custom.setArgs) {
                SourceType sourceType = defaultSourceType;
                if (customArg.type != null) {
                    sourceType = SourceType.valueOf(customArg.type);
                }
                ArgValue argValue = argValues.getArgValue(customArg.name, sourceType, instance.mergeMultipleValues());
                if(argValue==null){
                    if(instance.mergeMultipleValues()){ /*Do not stop if there are elements missing only for specific generators (i.e.  ConcatMultipleTerms)*/
                        argValue=new ArgValue("", "en");
                    }else{
                        throw exception("Cannot find arg with name \""+customArg.name+"\""+
                                    " in generator with name \""+generator.name+"\""+
                                    "[Mapping: "+RootElement.mappingCounter+", Link: "+RootElement.linkCounter+"]");
                    }
                }
                instance.setArg(customArg.name, argValue.string);
            }
            String value = instance.getValue();
            String returnType = instance.getValueType();
            log.debug("Generated Value-Type: ["+value+" , "+returnType+"]");
            //Custom Generator Prefix Addition
            if (returnType.equals(Labels.URI)) {

                if (generator.prefix != null) { // use URI template
                    if(UriValidator.isValid(value)){
                        log.debug("Skip the injection of namespace. "+value+" is already a valid URI"); // used from UriExistingOrNew generator
                        return uriValue(value);
                    }else{
                        log.debug("injecting namespace for constructing a valid URI"); 
                        String namespaceUri = namespaceMap.get(generator.prefix);
                        if (namespaceUri == null) {
                            throw exception("No namespace for prefix " + generator.prefix + "in generator policy");
                        }
                        return uriValue(namespaceUri + value);
                    }
                }else if(generator.custom.generatorClass.equals(TextualContent.class.getCanonicalName())){
                    return uriValue(Utils.urnValue(value));
                }
                else{
                    return uriValue(value);
                }
            }
            else if (returnType.equals(Labels.UUID)) {
                return uriValue(uuidSource.generateUUID());
            }
            else {
                return typedLiteralValue(value);
            }
        }
        catch (ClassNotFoundException e) {
            throw new X3MLEngine.X3MLException("Custom generator class not found: " + className);
        }
        catch (NoSuchMethodException e) {
            throw new X3MLEngine.X3MLException("Custom generator missing default constructor: " + className);
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new X3MLEngine.X3MLException("Custom generator unable to instantiate: " + className, e);
        }
        catch (ClassCastException e) {
            throw new X3MLEngine.X3MLException("Custom generator must implement CustomGenerator: " + className, e);
        }
        catch (CustomGeneratorException e) {
            throw new X3MLEngine.X3MLException("Custom generator failure: " + className, e);
        }
    }

    private GeneratedValue fromURITemplate(GeneratorSpec generator, String namespaceUri, ArgValues argValues) {
        try {
            UriTemplate uriTemplate = UriTemplate.fromTemplate(generator.pattern);
            for (String argument : getVariables(generator.pattern)) {
                ArgValue argValue = argValues.getArgValue(argument, defaultSourceType, false);
                if (argValue == null || argValue.string == null) {
                    throw exception(String.format(
                            "Argument failure in generator %s: %s",
                            generator, argument
                    ));
                }
                uriTemplate.set(argument, argValue.string);
            }
            if(Utils.isAffirmative(generator.uuid)){
                return uriValue(namespaceUri
                               +uriTemplate.expand()
                               +UUID.randomUUID().toString().toUpperCase());
            }else if(Utils.isAffirmative(generator.shorten)){
                String expandedUriPart1=uriTemplate.expand();
                String expandedUriPart2="";
                if(uriTemplate.expand().contains("/")){
                    expandedUriPart1=expandedUriPart1.substring(0, expandedUriPart1.lastIndexOf("/")+1);
                    expandedUriPart2=uriTemplate.expand().substring(uriTemplate.expand().lastIndexOf("/")+1);
                }
                if(expandedUriPart2.isEmpty()){
                    UUID uuid = java.util.UUID.nameUUIDFromBytes(expandedUriPart1.getBytes());
                    return uriValue(namespaceUri + uuid.toString().toUpperCase());
                }else{
                    UUID uuid = java.util.UUID.nameUUIDFromBytes(expandedUriPart2.getBytes());
                    return uriValue(namespaceUri + expandedUriPart1 + uuid.toString().toUpperCase());
                }
            }
            
            String uriSuffix=uriTemplate.expand();
            if(uriSuffix.replaceAll(Labels.PERCENT_CHARACTER_ENCODED, Labels.PERCENT_CHARACTER).contains("%%")){
                return uriValue(namespaceUri + uriTemplate.expand()
                                                               .replaceAll(Labels.SLASH_CHARACTER_ENCODED, Labels.SLASH_CHARACTER));
            }else{
                return uriValue(namespaceUri + uriTemplate.expand()
                                                               .replaceAll(Labels.SLASH_CHARACTER_ENCODED, Labels.SLASH_CHARACTER)
                                                               .replaceAll(Labels.PERCENT_CHARACTER_ENCODED, Labels.PERCENT_CHARACTER));
            }
        }
        catch (MalformedUriTemplateException e) {
            throw exception("Malformed", e);
        }
        catch (VariableExpansionException e) {
            throw exception("Variable", e);
        }
    }

    private GeneratedValue fromSimpleTemplate(GeneratorSpec generator, ArgValues argValues) {
        String result = generator.pattern;
        String language = null;
        for (String argument : getVariables(generator.pattern)) {
            ArgValue argValue = argValues.getArgValue(argument, defaultSourceType, false);
            if (argValue == null || argValue.string == null) {
                throw exception(String.format(
                        "Argument failure in simple template %s: %s",
                        generator, argument
                ));
            }
            result = result.replace(String.format("{%s}", argument), argValue.string);
            if (language == null) language = argValue.language;
        }
        language = getLanguage(language, argValues); // perhaps override
        return literalValue(result, language != null ? language : languageFromMapping);
    }

    // == the rest is for the XML form

    private static class TestUUIDSource implements UUIDSource {
        private final int size, max;
        private int count = 0;

        public TestUUIDSource(int size) {
            this.size = size;
            int maxi = 1;
            for (int walk = 0; walk < size; walk++) {
                maxi *= 26;
            }
            this.max = maxi;
        }

        @Override
        public String generateUUID() {
            StringBuilder uuid = new StringBuilder();
            if (count == max) throw new RuntimeException("Too many test UUIDs at " + count + ". Use a larger size.");
            int c = count++;
            for (int walk = 0; walk < size; walk++) {
                uuid.insert(0, (char) ((c % 26) + 'A'));
                c /= 26;
            }
            uuid.insert(0, "uuid:");
            return uuid.toString();
        }
    }

    private static class RealUUIDSource implements X3MLGeneratorPolicy.UUIDSource {
        @Override
        public String generateUUID() {
            return "urn:uuid:" + UUID.randomUUID();
        }
    }

    private static List<String> getVariables(String pattern) {
        Matcher braces = BRACES.matcher(pattern);
        List<String> arguments = new ArrayList<>();
        while (braces.find()) {
            Collections.addAll(arguments, braces.group(1).split(","));
        }
        return arguments;
    }

    private String getLanguage(String language, ArgValues argValues) {
        ArgValue languageArg = argValues.getArgValue("language", defaultSourceType, false);
        if (languageArg != null) {
            language = languageArg.string;
            if (language.isEmpty()) language = null; // to strip language
        }
        return language;
    }
    
    /** Validates XML document containing XML Generator Policy definitions, w.r.t. to
     * the default XSD definitions.
     * 
     * @param inputStream the XML serialization of the generator policy definitions
     * @return the validated inputStream (returned because the offset of the initial inputStream has been moved to EOF)
     * @throws X3MLException if the generator policy definitions are not valid */
    public static InputStream validateGeneratorPolicyFile(InputStream inputStream) throws X3MLException{
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
            throw new X3MLException("Cannot read the contents of Generator Policy file. Detailed log:\n"+ex.toString());
        }
        InputStream is = new ByteArrayInputStream(baos.toByteArray()); 
        try{
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        }catch(IOException | ParserConfigurationException | SAXException ex){
            throw new X3MLException("Cannot parse the contents of the Generator policy file. Check that is is a valid XML file. Detailed log:\n"+ex.toString());
        }
        X3MLGeneratorPolicy.validateGeneratorPolicyFileWithSchema(new ByteArrayInputStream(baosForValidation.toByteArray()));
        return new ByteArrayInputStream(baos.toByteArray());
    }
    
    /* validates the XML serialization of the generator policy definitions w.r.t. the default schema */
    public static void validateGeneratorPolicyFileWithSchema(InputStream inputStream){
        try{
            URL schemaUrl=X3MLEngine.class.getResource(X3MLGeneratorPolicy.GENERATOR_POLICY_SCHEMA_FOLDER+X3MLGeneratorPolicy.GENERATOR_POLICY_SCHEMA_FILENAME);
            javax.xml.transform.Source policyFile=new StreamSource(inputStream);
            SchemaFactory schemaFactory=SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema=schemaFactory.newSchema(schemaUrl);
            Validator validator=schema.newValidator();
            validator.validate(policyFile);
        }catch(SAXException | IOException ex){
            throw exception("An error occurred while validating the contents of the XML generator policy file", ex);
        }
    }
}
