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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import eu.delving.x3ml.INPUT_TYPE;
import eu.delving.x3ml.X3MLEngine;
import static eu.delving.x3ml.X3MLEngine.exception;
import org.w3c.dom.Node;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static eu.delving.x3ml.engine.X3ML.PathElement;
import static eu.delving.x3ml.engine.X3ML.RangeElement;
import static eu.delving.x3ml.engine.X3ML.Relationship;

/**
 * The path relationship handled here. Intermediate nodes possible. Expecting
 * always one more path than entity, and they are interlaced. Marshalling
 * handled specially.
 *
 * @author Gerald de Jong <gerald@delving.eu>
 * @author Nikos Minadakis <minadakn@ics.forth.gr>
 * @author Yannis Marketakis <marketak@ics.forth.gr>
 */
public class Path extends GeneratorContext {

    public final Domain domain;
    public final PathElement path;
    public Relationship relationship;
    public Property property;
    public List<IntermediateNode> intermediateNodes;
    public List<Resource> lastResources;
    public Property lastProperty;

    public Path(Root.Context context, Domain domain, PathElement path, Node node, int index) {
        super(context, domain, node, index);
        this.domain = domain;
        this.path = path;
    }
    
    public Path(Root.ContextRDF contextRDF, Domain domain, PathElement path, Resource resource, int index) {
        super(contextRDF, domain, resource, index);
        this.domain = domain;
        this.path = path;
    }

    public boolean resolve() {
        X3ML.TargetRelation relation = path.target_relation;
        if (conditionFails(relation.condition, this)) {
            return false;
        }
        if (relation.properties == null || relation.properties.isEmpty()) {
            throw exception("Target relation must have at least one property");
        }
        if (relation.entities != null) {
            if (relation.entities.size() + 1 != relation.properties.size()) {
                throw exception("Target relation must have one more property than entity");
            }
        } else if (relation.properties.size() != 1) {
            throw exception("Target relation must just one property if it has no entities");
        }
        relationship = relation.properties.get(0);
        if(X3MLEngine.inputType==INPUT_TYPE.XML){
            property = context.output().createProperty(relationship);
        }else{
            property = contextRDF.output().createProperty(relationship);
        }
        intermediateNodes = createIntermediateNodes(relation.entities, relation.properties, this);
        return true;
    }

    public void link() {
        domain.link();
        if (!domain.entityResolver.hasResources()) {
            throw exception("Domain node has no resource");
        }
        lastResources = domain.entityResolver.resources;
        lastProperty = property;
        for (IntermediateNode intermediateNode : intermediateNodes) {
            intermediateNode.entityResolver.link();
            if (!intermediateNode.entityResolver.hasResources()) {
                throw exception("Intermediate node has no resources");
            }
            for (Resource lastResource : lastResources) {
                for (Resource resolvedResource : intermediateNode.entityResolver.resources) {
                    lastResource.addProperty(lastProperty, resolvedResource);
                }
            }
            lastResources = intermediateNode.entityResolver.resources;
            lastProperty = intermediateNode.property;
        }
    }

    public List<Range> createRangeContexts(RangeElement range) {
//        if (range.source_node == null) {
//            throw exception("Range source absent: " + range);
//        }
        String expression = path.source_relation.relation.get(0).expression;
        if (range.source_node.expression.equals(expression)) {
            expression = "";
        }
        List<Node> rangeNodes = context.input().nodeList(node, expression);
        List<Range> ranges = new ArrayList<Range>();
        int index = 1;
        for (Node rangeNode : rangeNodes) {
            Range rangeContext = new Range(context, this, range, rangeNode, index++);
            if (rangeContext.resolve()) {
                ranges.add(rangeContext);
            }
        }
        return ranges;
    }
    
    public List<Range> createRangeContexts(Model modelInput,RangeElement range) {
//        if (range.source_node == null) {
//            throw exception("Range source absent: " + range);
//        }
        //It compares the expression in the Path with the expression in the Range
//        String expression = path.source_relation.relation.get(0).expression;
//        if (range.source_node.expression.equals(expression)) {
//            expression = "";
//        }
        List<Resource> rangeNodes = contextRDF.input().getResourcesForRange(modelInput, domain.resource, path.source_relation, range.source_path);
        System.out.println("FINAL RANGE RES: "+rangeNodes);

        List<Range> ranges = new ArrayList<Range>();
        int index = 1;
        for (Resource rangeNode : rangeNodes) {
            Range rangeContext = new Range(contextRDF, this, range, rangeNode, index++);
            if (rangeContext.resolve()) {   //This one creates the URI of the range nodes (e.g. ID)
                ranges.add(rangeContext);
            }
        }
        return ranges;
    }

    private List<IntermediateNode> createIntermediateNodes(List<X3ML.EntityElement> entityList, List<Relationship> propertyList, GeneratorContext generatorContext) {
        List<IntermediateNode> intermediateNodesFound = new ArrayList<IntermediateNode>();
        if (entityList != null) {
            Iterator<Relationship> walkProperty = propertyList.iterator();
            walkProperty.next(); // ignore
            int intermediateNodeCounter=1;
            for (X3ML.EntityElement entityElement : entityList) {
                IntermediateNode intermediateNode = new IntermediateNode(entityElement, walkProperty.next(), generatorContext, intermediateNodeCounter++);
                if (intermediateNode.resolve()) {
                    intermediateNodesFound.add(intermediateNode);
                }
            }
        }
        return intermediateNodesFound;
    }

    private class IntermediateNode {

        public final X3ML.EntityElement entityElement;
        public final Relationship relationship;
        public final GeneratorContext generatorContext;
        public EntityResolver entityResolver;
        public Property property;
        public final int intermediateNodeIndex;

        private IntermediateNode(X3ML.EntityElement entityElement, Relationship relationship, GeneratorContext generatorContext, int intermediateNodeIndex) {
            this.entityElement = entityElement;
            this.relationship = relationship;
            this.generatorContext = generatorContext;
            this.intermediateNodeIndex=intermediateNodeIndex;
        }

        public boolean resolve() {
            entityResolver = new EntityResolver(context.output(), entityElement, generatorContext);
            if (!entityResolver.resolve(0,this.intermediateNodeIndex)) {   
                return false;
            }
            property = context.output().createProperty(relationship);
            return true;
        }
    }

}
