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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static gr.forth.ics.isl.x3ml.engine.X3ML.GeneratorElement;
import static gr.forth.ics.isl.x3ml.engine.X3ML.Helper.argVal;
import static gr.forth.ics.isl.x3ml.engine.X3ML.SourceType;

import gr.forth.IterableNodeList;
import gr.forth.Labels;
import gr.forth.Utils;
import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import static gr.forth.ics.isl.x3ml.X3MLEngine.exception;
import lombok.extern.log4j.Log4j2;
import net.sf.saxon.dom.DOMNodeList;

import static org.joox.JOOX.$;

/**
 * The source data is accessed using xpath to fetch nodes from a DOM tree.
 * <p>
 * Here we have tools for evaluating xpaths in various contexts.
 *
 * @author Gerald de Jong &lt;gerald@delving.eu&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 */
@Log4j2
public class XPathInput {

    private final XPathFactory pathFactory = new net.sf.saxon.xpath.XPathFactoryImpl();
    private final NamespaceContext namespaceContext;
    private final String languageFromMapping;
    private final Node rootNode;
    public static String domainURIForNamedgraps=null;
    public static String entireInputExportedRefUri=null;
    private Map<String, Map<String, List<Node>>> rangeMapCache = new TreeMap<String, Map<String, List<Node>>>();
    private static Map<String, XPathExpression> regexCache = new HashMap<>();

    public XPathInput(Node rootNode, NamespaceContext namespaceContext, String languageFromMapping) {
        this.rootNode = rootNode;
        this.namespaceContext = namespaceContext;
        this.languageFromMapping = languageFromMapping;
    }

    public X3ML.ArgValue evaluateArgument(Node node, int index, GeneratorElement generatorElement, String argName, SourceType defaultType, boolean mergeMultipleValues) {
        log.debug("Evaluating argument: [Node: {}\tIndex: {}\tGenerator: {}\tArgName: {}\tDefaultType: {}\tMergeMultipleValues: {}]",
                                        node, index, generatorElement, argName, defaultType, mergeMultipleValues);
        X3ML.GeneratorArg foundArg = null;
        SourceType type = defaultType;
        if (generatorElement.getArgs() != null) {
            for (X3ML.GeneratorArg arg : generatorElement.getArgs()) {
                if (arg.name == null) {
//                    throw exception("Argument needs a name in generator "+generatorElement.name);
                    arg.name = "text";
                }
                if (arg.name.equals(argName)) {
                    foundArg = arg;
                    type = sourceType(arg.type, defaultType);
                }
            }

        }
        X3ML.ArgValue value = null;
        switch (type) {

            case xpath:
                if (foundArg == null) {
                    return null;
                }
                String lang = getLanguageFromSource(node);
                if (lang == null) {
                    lang = languageFromMapping;
                }
                if (!foundArg.value.isEmpty()) {
                    
                    if(mergeMultipleValues){
                        if(countNodes(node, foundArg.value)>=1){
                            value = argVal(valueMergedAt(node, foundArg.value), lang);
                            if (value.string.isEmpty()) {
                                value=new X3ML.ArgValue("", "en");
                            }
                        }
                    }else{
                        value = argVal(valueAt(node, foundArg.value), lang);
                        if (value.string.isEmpty()) {
                            if(foundArg.name.equalsIgnoreCase(Labels.LANGUAGE)){
                                value=new X3ML.ArgValue(Labels.EN_LANGUAGE,Labels.EN_LANGUAGE);
                            }else{
                                throw exception("Empty result for arg " + foundArg.name + " at node " + node.getNodeName() + " in generator\n" + generatorElement);
                            }
                        }
                    }
                }
                break;
            case constant:
                if (foundArg == null) {
                    return null;
                }
                value = argVal(foundArg.value, languageFromMapping);
                break;
            case position:
                value = argVal(String.valueOf(index), null);
                break;
            case entireInput:
                value=argVal(this.getEntireXpathInput(), languageFromMapping);
                entireInputExportedRefUri=domainURIForNamedgraps;
                break;
            case xpathPosition:
                value = argVal(extractXPath(node), languageFromMapping);
                break;
            default:
                throw new RuntimeException("Not implemented");
        }
        return value;
    }

    /** Returns the value that can be found in the corresponding node, after the evaluation 
     * of the given XPath expression. More specifically it returns the results after 
     * evaluating the XPath expression on the node. If the XPath expression cannot
     * be evaluated, then an empty String is returned. If there are more than one results 
     * for the given node and expression, then only the first one will be returned. 
     *
     * @param node the node that will be used for evaluating the given XPath expression  
     * @param expression the XPath expression to be used for retrieving particular information from the node
     * @return the value of the node, after evaluating the given XPath expression */
    public String valueAt(Node node, String expression) {
        try{
            log.debug("Evaluating XPATH [Node: {} Expression: {}]", node, expression);
            XPathExpression xe = this.getCompiledExpression(expression);
            String value=((String)xe.evaluate(node, XPathConstants.STRING)).trim();
            log.debug("XPATH Result: {} (length= {})", value, value.length());
            return value;
        }catch(XPathExpressionException ex){
            throw new RuntimeException("XPath Problem: " + expression, ex);
        }
    }

    /** Returns a merging of the values that can be found in the corresponding node, after the evaluation 
     * of the given XPath expression. More specifically it returns the results after 
     * evaluating the XPath expression on the node. If the XPath expression cannot
     * be evaluated, then an empty String is returned. If there are more than one results 
     * for the given node and expression, then their contents are merged and returned.  
     *
     * @param node the node that will be used for evaluating the given XPath expression  
     * @param expression the XPath expression to be used for retrieving particular information from the node
     * @return the merging of the values of the node, after evaluating the given XPath expression */
    public String valueMergedAt(Node node, String expression){
        String retValue="";
        for(Node childNode : nodeList(node, expression)){
            retValue+=childNode.getNodeValue()+Labels.SAME_MERGING_DELIMITER;
        }
        return retValue.substring(0, retValue.length()-Labels.SAME_MERGING_DELIMITER.length());
    }
    
    /**Returns the number of results encountered after evaluating the given XPath expression 
     * in the given node. 
     * 
     * @param node the node that will be used for evaluating the given XPath expression
     * @param expression the XPath expression that will be evaluated on the given node
     * @return the number of results */
    public int countNodes(Node node, String expression) {
        return nodeList(node, expression).size();
    }
    
    public boolean existingNode(Node node, String expression) {
        IterableNodeList nodes = nodeList(node, expression);
        
        if (nodes.isEmpty()) {
            return false;
        }
        return true;
    }

    public static String extractXPath(Node node) {
        String path = $(node).xpath();
        String pathTemp[] = path.split("\\[");
        String position = pathTemp[pathTemp.length-1].replace("]","");
        return position;

    }
    
    
    public IterableNodeList nodeList(Node node, X3ML.Source source) {
        if (source != null) {
            return nodeList(node, source.expression);
        } else {
            List<Node> list = new ArrayList<>(1);
            list.add(node);
            return new IterableNodeList(new DOMNodeList(list));
        }
    }

    public IterableNodeList nodeList(Node context, String expression) {
        log.debug("Evaluating XPATH [Node: {} Expression: {}]", context, expression);

        if (expression == null || expression.length() == 0) {
            List<Node> list = new ArrayList<>(1);
            list.add(context);
            return new IterableNodeList(new DOMNodeList(list));
        }
        try {
            XPathExpression xe = this.getCompiledExpression(expression);
            NodeList nodeList = (NodeList) xe.evaluate(context, XPathConstants.NODESET);
            return new IterableNodeList(nodeList);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("XPath Problem: " + expression, e);
        }
    }
    
    public List<Node> rootNodeList(
            String domainExpression,
            String pathExpression,
            String domainValue,
            String rangeExpression,
            String rangeKeyPath
    ) {
        if (rangeExpression == null || rangeExpression.length() == 0) {
            throw exception("Range expression missing");
        }
        Map<String, List<Node>> rangeMap = getRangeMap(
                rootNode,
                domainExpression,
                pathExpression,
                rangeExpression,
                rangeKeyPath
        );
        List<Node> retList=rangeMap.get(domainValue);
        return (retList==null) ? new ArrayList<Node>() : retList;
    }

    private Map<String, List<Node>> getRangeMap(
            Node context,
            String domainExpression,
            String pathExpression,
            String rangeExpression,
            String rangeKeyPath
    ) {
        String mapName = domainExpression + "|" + pathExpression + "|" + rangeExpression;

        Map<String, List<Node>> map = rangeMapCache.get(mapName);
        if (map == null) {
            map = new HashMap<String, List<Node>>();
            rangeMapCache.put(mapName, map);
            for (Node node : nodeList(context, rangeExpression)) {
                String key = valueAt(node, rangeKeyPath);
                if(key.isEmpty()){
                    Utils.printErrorMessages("Empty value for \""+rangeExpression+"/"+rangeKeyPath+"\"\t The node from the input XML is:\n"+$(node).toString());
                }
                List<Node> value = map.get(key);
                
                if (value == null) {
                    value = new ArrayList<Node>();
                    map.put(key, value);
                }
                value.add(node);
            }
//            Logger log = Logger.getLogger("getRangeMap");
//            log.info("Built Map! " + mapName);
//            for (Map.Entry<String, List<Node>> entry : map.entrySet()) {
//                for (Node node : entry.getValue()) {
//                    log.info("\n" + entry.getKey() + ":=" + $(node).content());
//                }
//            }
        }
        return map;
    }

    private static String getLanguageFromSource(Node node) {
        Node walkNode = node;
        while (walkNode != null) {
            NamedNodeMap attributes = walkNode.getAttributes();
            if (attributes != null) {
                Node lang = attributes.getNamedItemNS("http://www.w3.org/XML/1998/namespace", "lang");
                if (lang != null) {
                    return lang.getNodeValue();
                }
            }
            walkNode = walkNode.getParentNode();
        }
        return null;
    }

    private SourceType sourceType(String value, SourceType defaultType) {
        if (value == null) {
            return defaultType;
        } else {
            return SourceType.valueOf(value);
        }
    }

    private XPath xpath() {
        XPath path = pathFactory.newXPath();
        path.setNamespaceContext(namespaceContext);
        return path;
    }
    
    private String getEntireXpathInput(){
        try{
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(this.rootNode), new StreamResult(writer));
            return "\""+writer.getBuffer().toString()+"\"";
        }catch(IllegalArgumentException | TransformerException |TransformerFactoryConfigurationError ex){
            throw exception("",ex);
        }
    }

    // because compilation of regex is very expensive we are doing it only once
    // and then we cache the compiled regex and reuse it
    private XPathExpression getCompiledExpression(String expression) {
        XPathExpression xe;
        if (regexCache.containsKey(expression)) {
            xe = regexCache.get(expression);
        } else {
            try {
                xe = xpath().compile(expression);
            } catch (XPathExpressionException e) {
                throw new RuntimeException(e);
            }
            regexCache.put(expression, xe);
        }
        return xe;
    }

}
