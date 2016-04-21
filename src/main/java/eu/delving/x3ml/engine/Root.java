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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import eu.delving.x3ml.INPUT_TYPE;
import eu.delving.x3ml.X3MLEngine;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static eu.delving.x3ml.engine.X3ML.GeneratedValue;
import gr.forth.Utils;

/**
 * The root of the mapping is where the domain contexts are created. They then
 * fabricate path contexts which in turn make range contexts.
 *
 * @author Gerald de Jong <gerald@delving.eu>
 * @author Nikos Minadakis <minadakn@ics.forth.gr>
 * @author Yannis Marketakis <marketak@ics.forth.gr>
 */
public class Root {

    private final Element rootNode;
    public final Model modelInput;
    private final ModelOutput modelOutput;
    private final XPathInput xpathInput;
    public final ModelInputResources modelInputResources;
    private final Context context;
    private final ContextRDF contextRDF;
    private final Map<String, GeneratedValue> generated = new HashMap<String, GeneratedValue>();
    private final Map<String, Resource> existingResources = new HashMap<String, Resource>();

    public Root(Element rootNode, final Generator generator, NamespaceContext namespaceContext, List<String> prefixes) {
        this.rootNode = rootNode;
        Model model = ModelFactory.createDefaultModel();
        for (String prefix : prefixes) {
            model.setNsPrefix(prefix, namespaceContext.getNamespaceURI(prefix));
        }
        this.modelOutput = new ModelOutput(model, namespaceContext);
        this.xpathInput = new XPathInput(rootNode, namespaceContext, generator.getLanguageFromMapping());
        this.context = new Context() {

            @Override
            public XPathInput input() {
                return xpathInput;
            }

            @Override
            public ModelOutput output() {
                return modelOutput;
            }

            @Override
            public Generator policy() {
                return generator;
            }

            @Override
            public GeneratedValue getGeneratedValue(String xpath) {
                return generated.get(xpath);
            }

            @Override
            public void putGeneratedValue(String xpath, GeneratedValue generatedValue) {
                switch (generatedValue.type) {
                    case URI:
                        generated.put(xpath, generatedValue);
                        break;
                    case LITERAL:
                        break;
                    case TYPED_LITERAL:
                        break;
                }
            }
        };
        this.modelInputResources=null;
        this.modelInput=null;
        this.contextRDF=null;
    }
    
    public Root(Model modelInput, final Generator generator, NamespaceContext namespaceContext, List<String> prefixes) {
        this.modelInput = modelInput;   // This is all the input RDF document
        Model model = ModelFactory.createDefaultModel();
        for (String prefix : prefixes) {
            model.setNsPrefix(prefix, namespaceContext.getNamespaceURI(prefix));
        }
        this.modelOutput = new ModelOutput(model, namespaceContext);
        this.modelInputResources = new ModelInputResources(modelInput);
        this.contextRDF = new ContextRDF() {

            @Override
            public ModelInputResources input() {
                return modelInputResources;
            }

            @Override
            public ModelOutput output() {
                return modelOutput;
            }

            @Override
            public Generator policy() {
                return generator;
            }

            @Override
            public GeneratedValue getGeneratedValue(String xpath) {
                System.out.println("GET GENER: "+xpath+"\t"+generated.get(xpath));
                return generated.get(xpath);
            }

            @Override
            public void putGeneratedValue(String xpath, GeneratedValue generatedValue) {
                switch (generatedValue.type) {
                    case URI:
                        generated.put(xpath, generatedValue);
                        break;
                    case LITERAL:
                        break;
                    case TYPED_LITERAL:
                        break;
                }
            }

            @Override
            public void putExistingResource(String key, Resource existingResource) {
                    existingResources.put(key, existingResource);
            }
        };
        this.rootNode=null;
        this.xpathInput=null;
        this.context=null;
    }

    public ModelOutput getModelOutput() {
        return modelOutput;
    }

    public List<Domain> createDomainContexts(X3ML.DomainElement domain) {
        if(X3MLEngine.inputType==INPUT_TYPE.XML){
            List<Node> domainNodes = xpathInput.nodeList(rootNode, domain.source_node);
            List<Domain> domains = new ArrayList<Domain>();
            int index = 1;
            for (Node domainNode : domainNodes) {
                Domain domainContext = new Domain(context, domain, domainNode, index++);
                try{
                    if (domainContext.resolve()) {
                        domains.add(domainContext);
                    } 
                }catch(X3MLEngine.X3MLException ex){
                    X3MLEngine.exceptionMessagesList+=ex.toString();
                    Utils.printErrorMessages("ERROR FOUND: "+ex.toString());
                }
            }
            return domains;
        }else{
            System.out.println("DOMAIN: "+domain);
            List<Resource> listOfDomainResources=modelInputResources.getResources(modelInput,domain.source_node);
            System.out.println(listOfDomainResources);
            List<Domain> domains = new ArrayList<Domain>();
            int index = 1;
            for (Resource domainResource : listOfDomainResources) {
                Domain domainContext = new Domain(contextRDF, modelInput, domain, domainResource, index++);
                try{
                if (domainContext.resolve()) {
                    domains.add(domainContext);
                } else {
                    System.out.println("Unresolved: " + domainContext);
                }
                }catch(X3MLEngine.X3MLException ex){
                    System.out.println("EXCEPTION: "+ex);
                }   
            }
            return domains;
        }
                
    }

    public interface Context {

        XPathInput input();

        ModelOutput output();

        Generator policy();

        GeneratedValue getGeneratedValue(String xpath);

        void putGeneratedValue(String xpath, GeneratedValue generatedValue);
    }
    
    public interface ContextRDF {

        ModelInputResources input();

        ModelOutput output();

        Generator policy();

        GeneratedValue getGeneratedValue(String xpath);
        
        void putGeneratedValue(String xpath, GeneratedValue generatedValue);
        
        void putExistingResource(String xpath, Resource generatedValue);
    }
}
