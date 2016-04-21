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

import com.damnhandy.uri.template.MalformedUriTemplateException;
import com.damnhandy.uri.template.UriTemplate;
import com.damnhandy.uri.template.VariableExpansionException;
import eu.delving.x3ml.engine.Generator;
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
import static eu.delving.x3ml.X3MLEngine.exception;
import static eu.delving.x3ml.engine.X3ML.*;
import static eu.delving.x3ml.engine.X3ML.Helper.generatorStream;
import static eu.delving.x3ml.engine.X3ML.Helper.literalValue;
import static eu.delving.x3ml.engine.X3ML.Helper.typedLiteralValue;
import static eu.delving.x3ml.engine.X3ML.Helper.uriValue;
import static eu.delving.x3ml.engine.X3ML.SourceType.constant;
import static eu.delving.x3ml.engine.X3ML.SourceType.xpath;
import gr.forth.Utils;

/**
 * @author Gerald de Jong <gerald@delving.eu>
 * @author Nikos Minadakis <minadakn@ics.forth.gr>
 * @author Yannis Marketakis <marketak@ics.forth.gr>
 */

public class X3MLGeneratorPolicy implements Generator {
    private static final Pattern BRACES = Pattern.compile("\\{[?;+#]?([^}]+)\\}");
    private Map<String, GeneratorSpec> generatorMap = new TreeMap<String, GeneratorSpec>();
    private Map<String, String> namespaceMap = new TreeMap<String, String>();
    private UUIDSource uuidSource;
    private SourceType defaultSourceType;
    private String languageFromMapping;

    public interface CustomGenerator {
        void setArg(String name, String value) throws CustomGeneratorException;

        String getValue() throws CustomGeneratorException;

        String getValueType() throws CustomGeneratorException;

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
            GeneratorPolicy policy = (GeneratorPolicy) generatorStream().fromXML(inputStream);
            for (GeneratorSpec generator : policy.generators) {
                if (generatorMap.containsKey(generator.name)) {
                    throw exception("Duplicate generator name: " + generator.name);
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
        String argDefaultValue="text";
        String name=generatorElem.getName();
        if (name == null) {
            throw exception("Value function name missing");
        }
        if ("UUID".equals(name)) {
            return uriValue(uuidSource.generateUUID());
        }
        if ("Literal".equals(name)) {
            ArgValue value = argValues.getArgValue(argDefaultValue, xpath);
            if (value == null) {
                throw exception(Utils.produceLabelGeneratorMissingArgumentError(generatorElem, argDefaultValue));
            }
            if (value.string == null || value.string.isEmpty()) {
                throw exception(Utils.produceLabelGeneratorEmptyArgumentError(generatorElem));
            }
            return literalValue(value.string, getLanguage(value.language, argValues));
        }
        if ("prefLabel".equals(name)) {
            ArgValue value = argValues.getArgValue(argDefaultValue, xpath);
            if (value == null) {
                throw exception(Utils.produceLabelGeneratorMissingArgumentError(generatorElem, argDefaultValue));
            }
            if (value.string == null || value.string.isEmpty()) {
                throw exception(Utils.produceLabelGeneratorEmptyArgumentError(generatorElem));
            }
            return literalValue(value.string, getLanguage(value.language, argValues));
        }
        if ("Constant".equals(name)) {
            ArgValue value = argValues.getArgValue(argDefaultValue, constant);
            if (value == null) {
                throw exception(Utils.produceLabelGeneratorMissingArgumentError(generatorElem, argDefaultValue));
            }
            return literalValue(value.string, getLanguage(value.language, argValues));
        }
        if ("existingURI".equals(name)) {
            return null;
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
            for (CustomArg customArg : generator.custom.setArgs) {
                SourceType sourceType = defaultSourceType;
                if (customArg.type != null) {
                    sourceType = SourceType.valueOf(customArg.type);
                }
                ArgValue argValue = argValues.getArgValue(customArg.name, sourceType);
                if(argValue==null){
                    throw exception("Cannot find arg with name \""+customArg.name+"\""+
                                    " in generator with name \""+generator.name+"\""+
                                    "[Mapping: "+RootElement.mappingCounter+", Link: "+RootElement.linkCounter+"]");
                }
                instance.setArg(customArg.name, argValue.string);
            }
            String value = instance.getValue();
            String returnType = instance.getValueType();
          
            //Custom Generator Prefix Addition
            if (returnType.equals("URI")) {

                if (generator.prefix != null) { // use URI template
                    String namespaceUri = namespaceMap.get(generator.prefix);
                    if (namespaceUri == null) {
                        throw exception("No namespace for prefix " + generator.prefix + "in generator policy");
                    }
                    return uriValue(namespaceUri + value);
                } else {
                    return uriValue(value);
                }
            }
            else if (returnType.equals("UUID")) {
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
        catch (InvocationTargetException e) {
            throw new X3MLEngine.X3MLException("Custom generator unable to instantiate: " + className, e);
        }
        catch (InstantiationException e) {
            throw new X3MLEngine.X3MLException("Custom generator unable to instantiate: " + className, e);
        }
        catch (IllegalAccessException e) {
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
                ArgValue argValue = argValues.getArgValue(argument, defaultSourceType);
                if (argValue == null || argValue.string == null) {
                    throw exception(String.format(
                            "Argument failure in generator %s: %s",
                            generator, argument
                    ));
                }
                uriTemplate.set(argument, argValue.string);
            }
            return uriValue(namespaceUri + uriTemplate.expand());
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
            ArgValue argValue = argValues.getArgValue(argument, defaultSourceType);
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
        List<String> arguments = new ArrayList<String>();
        while (braces.find()) {
            Collections.addAll(arguments, braces.group(1).split(","));
        }
        return arguments;
    }

    private String getLanguage(String language, ArgValues argValues) {
        ArgValue languageArg = argValues.getArgValue("language", defaultSourceType);
        if (languageArg != null) {
            language = languageArg.string;
            if (language.isEmpty()) language = null; // to strip language
        }
        return language;
    }
}
