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
package gr.forth.ics.isl.x3ml.engine;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import gr.forth.ics.isl.x3ml.X3MLEngine;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static gr.forth.ics.isl.x3ml.engine.X3ML.GeneratedValue;
import gr.forth.Utils;
import java.io.InputStream;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.Lang;

/**
 * The root of the mapping is where the domain contexts are created. They then
 * fabricate path contexts which in turn make range contexts.
 *
 * @author Gerald de Jong &lt;gerald@delving.eu&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 */
@Log4j2
public class Root {

    private final Element rootNode;
    private final ModelOutput modelOutput;
    private final TerminologyModel terminology;
    private final XPathInput xpathInput;
    private final Context context;
    private final Map<String, GeneratedValue> generated = new HashMap<>();
           
    public Root(Element rootNode, final Generator generator, NamespaceContext namespaceContext, List<String> prefixes, Pair<InputStream,Lang> terminologyStream) {
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
        if(terminologyStream!=null){
            this.terminology=new TerminologyModel(terminologyStream.getLeft(),terminologyStream.getRight());
        }else{
            this.terminology=null;
        }
    }

    public ModelOutput getModelOutput() {
        return modelOutput;
    }

    public List<Domain> createDomainContexts(X3ML.DomainElement domain,String namedgraph) {
        List<Node> domainNodes = xpathInput.nodeList(rootNode, domain.source_node);
        List<Domain> domains = new ArrayList<>();
        int domainNodesTotal=domainNodes.size();
        int index = 1;
        for (Node domainNode : domainNodes) {
            if(X3MLEngine.REPORT_PROGRESS){
                if(domainNodesTotal>=20){
                    if(index%(domainNodesTotal/20)==0){
                        log.info("Round "+X3ML.RootElement.mappingCounter+"/"+X3ML.RootElement.mappingsTotal+", Step 1/2: Creating domain nodes: "+((100*(index))/domainNodesTotal)+"% completed ("+index +" domain nodes out of "+domainNodesTotal+" completed)");
                    }
                }else{
                    log.info("Round "+X3ML.RootElement.mappingCounter+"/"+X3ML.RootElement.mappingsTotal+", Step 1/2: Creating domain nodes: "+((100*(index))/domainNodesTotal)+"% completed ("+index +" domain nodes out of "+domainNodesTotal+" completed)");
                }
            }
            Domain domainContext = new Domain(context, domain, domainNode, index++);
            try{
                if (domainContext.resolve(namedgraph)) {
                    domains.add(domainContext);
                } 
            }catch(X3MLEngine.X3MLException ex){
                X3MLEngine.exceptionMessagesList+=ex.toString()+"\n";
               
                Utils.printErrorMessages(ex.getMessage());
            }
        }
        return domains;
    }

    public interface Context {

        XPathInput input();

        ModelOutput output();

        Generator policy();

        GeneratedValue getGeneratedValue(String xpath);

        void putGeneratedValue(String xpath, GeneratedValue generatedValue);        
    }
}
