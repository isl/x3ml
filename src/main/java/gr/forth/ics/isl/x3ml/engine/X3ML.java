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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import gr.forth.ics.isl.x3ml.X3MLEngine;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import gr.forth.Utils;
import static gr.forth.ics.isl.x3ml.X3MLEngine.exception;
import java.util.Collection;
import lombok.extern.log4j.Log4j;
import org.w3c.dom.Node;

/**
 * This interface defines the XML interpretation of the engine using the XStream
 * library.
 * <p>
 * There is also a helper class for encapsulating related functions.
 * <p>
 * The XSD definition is to be found in /src/main/resources.
 *
 * @author Gerald de Jong &lt;gerald@delving.eu&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 */
public interface X3ML {

    public enum SourceType {

        xpath,
        constant,
        position,
        entireInput,
        xpathPosition
    }

    @XStreamAlias("x3ml")
    public static class RootElement extends Visible {
        public static int mappingCounter=0;
        public static int mappingsTotal=0;
        public static int linkCounter=0;
        public static boolean hasNamedGraphs=false;
        public static int linksTotal=0;


        @XStreamAsAttribute
        public String version;

        @XStreamAsAttribute
        @XStreamAlias("source_type")
        public SourceType sourceType;

        @XStreamAsAttribute
        public String language;

        public Info info;

        public List<MappingNamespace> namespaces;

        
        
        @XStreamAsAttribute
        public String namedgraph;
        
        public Mappings mappings;
        

        public void apply(Root context) {
            if(mappings.namedgraph!=null){
                RootElement.hasNamedGraphs=true;
                Mappings.namedgraphProduced=mappings.namedgraph;
            }
            RootElement.mappingsTotal=mappings.mappings.size();
            for (Mapping mapping : mappings.mappings) {

                RootElement.mappingCounter+=1;
                RootElement.linkCounter=0;
                if(!mapping.skipMapping()){
                    mapping.apply(context);
                }
            }
        }

        @XStreamOmitField
        public String comments;
    }
    
    @XStreamAlias("info")
    public static class Info extends Visible{
        
        public String title;
        
        public String general_description;
        
        public SourceElement source;
        
        public TargetElement target;
        
        @XStreamAlias("mapping_info")
        public MappingInfoElement mappingInfo;
        
        @XStreamAlias("example_data_info")
        public ExampleDataInfo exampleDataInfo;
    }
    
    @XStreamAlias("target")
    public static class TargetElement extends Visible{
        @XStreamImplicit(itemFieldName="target_info")
        public List<TargetInfo> target_info;
        
        @XStreamAlias("target_collection")
        @XStreamOmitField
        public String targetCollection;
    }
    
    @XStreamAlias("target_info")
    public static class TargetInfo extends Visible{
        @XStreamAlias("target_schema")
        public TargetSchema targetSchema;
        
        @XStreamAlias("namespaces")
        public List<MappingNamespace> namespaces;
    }
    
    @XStreamAlias("target_schema")
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"value"})
    public static class TargetSchema extends Visible{
        @XStreamAsAttribute
        public String type;
        
        @XStreamAsAttribute
        public String version;

        @XStreamAsAttribute
        @XStreamAlias("schema_file")
        public String schemaFile;
        
        public String value;
    }
    
    @XStreamAlias("source")
    public static class SourceElement extends Visible{
        @XStreamImplicit(itemFieldName="source_info")
        public List<SourceInfo> source_info;
        
        @XStreamAlias("source_collection")
        public String sourceCollection;
    }
    
    @XStreamAlias("source_info")
    public static class SourceInfo extends Visible{
        @XStreamAlias("source_schema")
        public SourceSchema sourceSchema;
        
        @XStreamAlias("namespaces")
        public List<MappingNamespace> namespaces;
    }
    
    @XStreamAlias("source_schema")
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"value"})
    public static class SourceSchema extends Visible{
        @XStreamAsAttribute
        public String type;
        
        @XStreamAsAttribute
        public String version;

        @XStreamAsAttribute
        @XStreamAlias("schema_file")
        public String schemaFile;
        
        public String value;
    }
    
    @XStreamAlias("mapping_info")
    public static class MappingInfoElement extends Visible{
        @XStreamAlias("mapping_created_by_org")
        public String mappingCreatedByOrg;
        
        @XStreamAlias("mapping_created_by_person")
        public String mappingCreatedByPerson;
        
        @XStreamAlias("in_collaboration_with")
        public String inCollaborationWith;
    }
    
    @XStreamAlias("example_data_info")
    public static class ExampleDataInfo extends Visible{
        @XStreamAlias("example_data_from")
        public String exampleDataFrom;
        
        @XStreamAlias("example_data_contact_person")
        public String exampleDataContactPerson;
        
        @XStreamAlias("example_data_source_record")
        public ExampleDataSourceRecord exampleDataSourceRecord;
        
        @XStreamAlias("generator_policy_info")
        public GeneratorPolicyInfo generatorPolicyInfo;
        
        @XStreamAlias("example_data_target_record")
        public ExampleDataTargetRecord exampleDataTargetRecord;
        
        @XStreamAlias("thesaurus_info")
        public ThesaurusInfo thesaurusInfo;
    }
    
    @XStreamAlias("example_data_source_record")
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"value"})
    public static class ExampleDataSourceRecord extends Visible{
        @XStreamAlias("xml_link")
        @XStreamAsAttribute
        public String xmlLink;
        
        @XStreamAlias("html_link")
        @XStreamAsAttribute
        public String htmlLink;
        
        public String value;
    }
    
    @XStreamAlias("generator_policy_info")
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"value"})
    public static class GeneratorPolicyInfo extends Visible{
        @XStreamAlias("generator_link")
        @XStreamAsAttribute
        public String generatorLink;
        
        public String value;
    }
    
    @XStreamAlias("example_data_target_record")
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"value"})
    public static class ExampleDataTargetRecord extends Visible{
        @XStreamAlias("rdf_link")
        @XStreamAsAttribute
        public String rdfLink;
        
        public String value;
    }
    
    @XStreamAlias("thesaurus_info")
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"value"})
    public static class ThesaurusInfo extends Visible{
        @XStreamAlias("thesaurus_link")
        @XStreamAsAttribute
        public String rdfLink;
        
        public String value;
    }


    @XStreamAlias("mappings")
    public static class Mappings extends Visible {

        @XStreamAsAttribute
        public String namedgraph;
        
        @XStreamImplicit
        public List<Mapping> mappings;
        
        public static String namedgraphProduced;
    }

    @XStreamAlias("mapping") @Log4j
    public static class Mapping extends Visible {

        @XStreamAsAttribute
        public String skip;
        
        @XStreamAsAttribute
        public String namedgraph;
        
        public DomainElement domain;

        @XStreamImplicit
        public List<LinkElement> links;
        
        public static String namedGraphProduced;

        public void apply(Root context) {
            
            if(this.namedgraph!=null){
                RootElement.hasNamedGraphs=true;
                namedGraphProduced=namedgraph;
            }else{
                namedGraphProduced=null;
            }

            List<Domain> domList=context.createDomainContexts(this.domain, namedgraph);
            DomainElement.namedGraphProduced=null;
            int counter=1;
            int domListTotal=domList.size();
            for (Domain domain : domList) {
                if(X3MLEngine.REPORT_PROGRESS && domListTotal>0){
                    if(domListTotal>=20){
                        if(counter%(domListTotal/20)==0){
                            log.info("Round "+X3ML.RootElement.mappingCounter+"/"+X3ML.RootElement.mappingsTotal+", Step 2/2: Creating link nodes: "+((100*(counter))/domListTotal)+"% completed");
                        }
                    }else{
                        log.info("Round "+X3ML.RootElement.mappingCounter+"/"+X3ML.RootElement.mappingsTotal+", Step 2/2: Creating link nodes: "+((100*(counter))/domListTotal)+"% completed");
                    }
                }
                counter++;
                RootElement.linkCounter=0;
                domain.resolve(namedgraph);
                /*The following is necessary for the cases were there are no links or 
                the links are not evaluated (the xpaths are not evaluated).
                The following directive will link resources with labels found in the domain*/
                domain.link();
                if (links == null) {
                    continue;
                }
                RootElement.linksTotal=links.size();
                for (LinkElement linkElement : links) {
                    LinkElement.namedGraphProduced=null;
                    RootElement.linkCounter+=1;
                    if(!linkElement.skipLink()){
                        linkElement.apply(domain,linkElement.namedgraph, namedgraph);
                    }
                }
                
            }
        }
        
        public boolean skipMapping(){
            if(skip!=null){
                if(skip.equalsIgnoreCase("true")){
                    return true;
                }
            }
            return false;
        }
    }

    @XStreamAlias("link")
    public static class LinkElement extends Visible {

        public PathElement path;

        public RangeElement range;
        
        @XStreamAsAttribute
        public String skip;
        
        @XStreamAsAttribute
        public String namedgraph;
        
        public static String namedGraphProduced;

        public void apply(Domain domain,String namedgraph, String mappingNamedGraph) {
            String pathSource = this.path.source_relation.relation.get(0).expression;
            String pathSource2 = "";
            String node_inside = "";

            if (this.path.source_relation.relation.size() > 1) {
                pathSource2 = this.path.source_relation.relation.get(1).expression;
            }

            if (this.path.source_relation.node != null) {
                node_inside = this.path.source_relation.node.expression;

                int equals = pathSource.indexOf("==");

                if (equals >= 0) {

                    String domainForeignKey = pathSource.trim();
                    String rangePrimaryKey = pathSource2.trim();

                    String intermediateFirst = domainForeignKey.substring(domainForeignKey.indexOf("==") + 2).trim();
                    String intermediateSecond = rangePrimaryKey.substring(0, rangePrimaryKey.indexOf("==")).trim();

                    domainForeignKey = domainForeignKey.substring(0, domainForeignKey.indexOf("==")).trim();
                    rangePrimaryKey = rangePrimaryKey.substring(rangePrimaryKey.indexOf("==") + 2).trim();

                    for (Link link : domain.createLinkContexts(this, domainForeignKey, rangePrimaryKey,
                            intermediateFirst, intermediateSecond, node_inside)) {
                        link.range.link(namedgraph, mappingNamedGraph);
                    }

                }
            } else if (pathSource.contains("==")) {

                int equals = pathSource.indexOf("==");
                if (equals >= 0) {
                    String domainForeignKey = pathSource.substring(0, equals).trim();
                    String rangePrimaryKey = pathSource.substring(equals + 2).trim();
                        for (Link link : domain.createLinkContexts(this, domainForeignKey, rangePrimaryKey)) {
                            link.range.link(namedgraph, mappingNamedGraph);
                        }
                }
            } 
            else {
                try{
                    for (Path path : domain.createPathContexts(this.path)) {
                        for (Range range : path.createRangeContexts(this.range)) {
                            range.link(namedgraph, mappingNamedGraph);
                        }
                    }
                }catch(X3MLEngine.X3MLException ex){
                        X3MLEngine.exceptionMessagesList+=ex.toString()+"\n";
                        Utils.printErrorMessages(ex.getMessage());
                }
            }
        }
        
        public boolean skipLink(){
            if(skip!=null){
                if(skip.equalsIgnoreCase("true")){
                    return true;
                }
            }
            return false;
        }
    }

    @XStreamAlias("namespace")
    public static class MappingNamespace extends Visible {

        @XStreamAsAttribute
        public String prefix;
        @XStreamAsAttribute
        public String uri;
    }

    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"expression"})
    public static class Source extends Visible {
        
        @XStreamAsAttribute
        public String skip;

        public String expression;
    }

    @XStreamAlias("domain")
    public static class DomainElement extends Visible {

        public Source source_node;

        public TargetNode target_node;

        public Comments comments;
        
        @XStreamAsAttribute
        public String namedgraph;
        
        public static String namedGraphProduced;
    }

    @XStreamAlias("target_relation")
    @XStreamConverter(TargetRelationConverter.class)
    public static class TargetRelation extends Visible {

        public Condition condition;

        public List<Relationship> properties;

        public List<EntityElement> entities;
    }

    public static class TargetRelationConverter implements Converter {
        // make sure the output is property-entity-property-entity-property

        @Override
        public boolean canConvert(Class type) {
            return TargetRelation.class.equals(type);
        }

        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            TargetRelation relation = (TargetRelation) source;
            if (relation.condition != null) {
                writer.startNode("if");
                context.convertAnother(relation.condition);
                writer.endNode();
            }
            Iterator<Relationship> walkProperties = relation.properties.iterator();
            Relationship relationship = walkProperties.next();
            writer.startNode("relationship");
            context.convertAnother(relationship);
            writer.endNode();
            for (EntityElement entityElement : relation.entities) {
                relationship = walkProperties.next();
                writer.startNode("entity");
                context.convertAnother(entityElement);
                writer.endNode();
                writer.startNode("relationship");
                context.convertAnother(relationship);
                writer.endNode();
            }
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            TargetRelation relation = new TargetRelation();
            relation.properties = new ArrayList<>();
            relation.entities = new ArrayList<>();
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                if ("if".equals(reader.getNodeName())) {
                    relation.condition = (Condition) context.convertAnother(relation, Condition.class);
                } else if ("relationship".equals(reader.getNodeName())) {
                    relation.properties.add((Relationship) context.convertAnother(relation, Relationship.class));
                } else if ("entity".equals(reader.getNodeName())) {
                    relation.entities.add((EntityElement) context.convertAnother(relation, EntityElement.class));
                } else {
                    throw new ConversionException("Unrecognized: " + reader.getNodeName());
                }
                reader.moveUp();
            }
            return relation;
        }
    }

    @XStreamAlias("target_node")
    public static class TargetNode extends Visible {

        @XStreamAlias("if")
        public Condition condition;

        @XStreamAlias("entity")
        public EntityElement entityElement;
    }

    @XStreamAlias("path")
    public static class PathElement extends Visible {

        public SourceRelation source_relation;

        public TargetRelation target_relation;

        public Comments comments;
    }

    @XStreamAlias("source_relation")
    public class SourceRelation extends Visible {

        @XStreamImplicit
        public List<Relation> relation;
          
        public Source node;
    }

    @XStreamAlias("relation")
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"expression"})
    public static class Relation extends Visible {
         public String expression;
    }
    
    @XStreamAlias("range")
    public static class RangeElement extends Visible {

        public Source source_node;

        public TargetNode target_node;

        public Comments comments;
    }

    @XStreamAlias("additional")
    public static class Additional extends Visible {

        @XStreamImplicit
        public List<Relationship> relationship;

        @XStreamImplicit
        public List<EntityElement> entityElement;
    }

    @XStreamAlias("if")
    public static class Condition extends Visible {

        public Narrower narrower;
        public Exists exists;
        public Equals equals;
        public Broader broader;
        public ExactMatch exact_match;
        public AndCondition and;
        public OrCondition or;
        public NotCondition not;

        private static class Outcome {

            final GeneratorContext context;
            boolean failure;

            private Outcome(GeneratorContext context) {
                this.context = context;
            }

            Outcome evaluate(YesOrNo yesOrNo) {
                if (yesOrNo != null && !failure && !yesOrNo.yes(context)) {
                    failure = true;
                }
                return this;
            }
        }

        public boolean failure(GeneratorContext context) {
            return new Outcome(context)
                    .evaluate(narrower)
                    .evaluate(exists)
                    .evaluate(equals)
                    .evaluate(broader)
                    .evaluate(exact_match)
                    .evaluate(and)
                    .evaluate(or)
                    .evaluate(not).failure;
        }

    }

    interface YesOrNo {

        boolean yes(GeneratorContext context);
    }
    
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"expression"})
    @XStreamAlias("exists")
    public static class Exists extends Visible implements YesOrNo {

        public String expression;

        @Override
        public boolean yes(GeneratorContext context) {
            return context.evaluate2(expression);
            // return context.evaluate(expression).length() > 0;
        }
    }

    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"expression"})
    @XStreamAlias("equals")
    public static class Equals extends Visible implements YesOrNo {

        @XStreamAsAttribute
        public String value;

        public String expression;

        @Override
        public boolean yes(GeneratorContext context) {
            return value.equals(context.evaluate(expression));
        }
    }
    
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"expression"})
    @XStreamAlias("broader")
    public static class Broader extends Visible implements YesOrNo {

        @XStreamAsAttribute
        public String value;

        public String expression;

        @Override
        public boolean yes(GeneratorContext context) {
            List<String> broaderTerms=TerminologyModel.getBroaderTerms(value);
            for(String term : broaderTerms){
                if(term.equals(context.evaluate(expression))){
                    return true;
                }
            }
            return value.equals(context.evaluate(expression));
        }
    }
    
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"expression"})
    @XStreamAlias("exact_match")
    public static class ExactMatch extends Visible implements YesOrNo {

        @XStreamAsAttribute
        public String value;

        public String expression;

        @Override
        public boolean yes(GeneratorContext context) {
            List<String> broaderTerms=TerminologyModel.getExactMatchTerms(value);
            for(String term : broaderTerms){
                if(term.equals(context.evaluate(expression))){
                    return true;
                }
            }
            return value.equals(context.evaluate(expression));
        }
    }

    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"expression"})
    @XStreamAlias("narrower")
    public static class Narrower extends Visible implements YesOrNo {

        @XStreamAsAttribute
        public String value;

        public String expression;

        @Override
        public boolean yes(GeneratorContext context) {
            return true;
        }
    }

    @XStreamAlias("and")
    public static class AndCondition extends Visible implements YesOrNo {

        @XStreamImplicit
        List<Condition> list;

        @Override
        public boolean yes(GeneratorContext context) {
            boolean result = true;
            for (Condition condition : list) {
                if (condition.failure(context)) {
                    result = false;
                }
            }
            return result;
        }
    }

    @XStreamAlias("or")
    public static class OrCondition extends Visible implements YesOrNo {

        @XStreamImplicit
        List<Condition> list;

        @Override
        public boolean yes(GeneratorContext context) {
            boolean result = false;
            for (Condition condition : list) {
                if (!condition.failure(context)) {
                    result = true;
                }
            }
            return result;
        }
    }

    @XStreamAlias("not")
    public static class NotCondition extends Visible implements YesOrNo {

        @XStreamAlias("if")
        Condition condition;

        @Override
        public boolean yes(GeneratorContext context) {
            return condition.failure(context);
        }
    }

    @XStreamAlias("relationship")
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"tag"})
    public static class Relationship extends Visible {

        public String tag;

        public String getPrefix() {
            if(tag.startsWith("http:")){    //used a fully qualified name
                return "";
            }else if(tag.equals("MERGE")){   //exploit the MERGE facility 
                return "MERGE";
            }
            else{
                int colon = tag.indexOf(':');
                if (colon <= 0) {
                    throw exception("Unqualified tag: '"+tag+"'. The namespace of the resource is missing");
                }
                return tag.substring(0, colon);
            }
        }

        public String getLocalName() {
            if(tag.startsWith("http:")){    //used a fully qualified name
                return tag;
            }else if(tag.equals("MERGE")){    //exploit the MERGE facility 
                return "MERGE";
            }
            else{
                int colon = tag.indexOf(':');
                if (colon <= 0) {
                    throw exception("Unqualified tag: '"+tag+"'. The namespace of the resource is missing");
                }
                return tag.substring(colon + 1);
            }
        }
    }

    @XStreamAlias("instance_info") // documentation purposes only
    public static class InstanceInfo extends Visible {

        public String language;
        public String constant;
        public String description;

    }

    @XStreamAlias("entity")
    public static class EntityElement extends Visible {

        @XStreamAsAttribute
        @XStreamAlias("variable_deprecated")
        public String variable_deprecated;
        
        @XStreamAsAttribute
        @XStreamAlias("global_variable")
        public String globalVariable;
        
        @XStreamAsAttribute
        @XStreamAlias("variable")
        public String variable;

        @XStreamImplicit
        public List<TypeElement> typeElements;

        @XStreamAlias("instance_info")
        public InstanceInfo instanceInfo; // documentation purposes only

        @XStreamAlias("instance_generator")
        public InstanceGeneratorElement instanceGenerator;

        @XStreamImplicit
        public List<LabelGeneratorElement> labelGenerators;

        @XStreamImplicit
        public List<Additional> additionals;

        public GeneratedValue getInstance(GeneratorContext context, String unique) {
            return context.getInstance(instanceGenerator, globalVariable, variable_deprecated, variable, unique);
        }
        
        public GeneratedValue getInstance(GeneratorContext context, String unique, Node node) {
            return context.getInstance(instanceGenerator, unique, node);
        }
    }

    @XStreamAlias("type")
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"tag"})
    public static class TypeElement extends Visible {

        public String tag;

        @XStreamOmitField
        public String namespaceUri;

        public TypeElement() {
        }

        public TypeElement(String tag, String namespaceUri) {
            this.tag = tag;
            this.namespaceUri = namespaceUri;
        }

        public String getPrefix() {
            if(tag.startsWith("http:")){
                return "";
            }
            else{
                int colon = tag.indexOf(':');
                if (colon <= 0) {
                    throw exception("Unqualified tag: '"+tag+"'. The namespace of the resource is missing");
                }
                return tag.substring(0, colon);
            }
        }

        public String getLocalName() {
            if(tag.startsWith("http:")){
                return tag;
            }
            else{
                int colon = tag.indexOf(':');
                if (colon <= 0) {
                    throw exception("Unqualified tag: '"+tag+"'. The namespace of the resource is missing");
                }
                return tag.substring(colon + 1);
            }
        }
    
    }

    @XStreamAlias("comments")
    public static class Comments extends Visible {

        @XStreamImplicit
        public List<Comment> comments;

    }

    @XStreamAlias("comment")
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"content"})
    public static class Comment extends Visible {

        @XStreamAsAttribute
        public String type;

        public String content;
    }

    public interface GeneratorElement{
        public String getName();
        public List<GeneratorArg> getArgs();
    }
    
    @XStreamAlias("label_generator")
    public static class LabelGeneratorElement extends Visible implements GeneratorElement {

        @XStreamAsAttribute
        public String name;

        @XStreamImplicit
        public List<GeneratorArg> args;
        
        @Override
        public String getName(){
            return this.name;
        }
        
        @Override
        public List<GeneratorArg> getArgs(){
            return this.args;
        }
        
        @Override
        public String toString(){
            return "LabelGenerator(Name: "+getName()+" Args: "+getArgs()+")";
        }
    }

    @XStreamAlias("instance_generator")
    public static class InstanceGeneratorElement extends Visible implements GeneratorElement{

        @XStreamAsAttribute
        public String name;

        @XStreamImplicit
        public List<GeneratorArg> args;
        
        @Override
        public String getName(){
            return this.name;
        }
        
        @Override
        public List<GeneratorArg> getArgs(){
            return this.args;
        }
        
        @Override
        public String toString(){
            return "InstanceGenerator( Name: "+getName()+" Args: "+getArgs()+")";
        }
    }

    @XStreamAlias("arg")
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"value"})
    public static class GeneratorArg extends Visible {

        @XStreamAsAttribute
        public String name;

        @XStreamAsAttribute
        public String type;

        public String value;
        
        @Override
        public String toString(){
            return "(Name: "+name+" Type: "+type+" Value: "+value+")";
        }
    }

    @XStreamAlias("generator_policy")
    public static class GeneratorPolicy extends Visible {

        @XStreamImplicit
        public List<GeneratorSpec> generators;
    }

    @XStreamAlias("generator")
    public static class GeneratorSpec extends Visible {

        @XStreamAsAttribute
        public String name;

        @XStreamAsAttribute
        public String prefix;
        
        @XStreamAsAttribute
        public String shorten;
        
        @XStreamAsAttribute
        public String uuid;
        
        @XStreamAsAttribute
        public String type;

        public CustomGenerator custom;

        public String pattern;
        
        public String description;

        @Override
        public String toString() {
            return name;
        }
    }

    @XStreamAlias("custom")
    public static class CustomGenerator extends Visible {

        @XStreamAsAttribute
        public String generatorClass;

        @XStreamImplicit
        public List<CustomArg> setArgs;

        @Override
        public String toString() {
            return generatorClass;
        }
    }

    @XStreamAlias("set-arg")
    public static class CustomArg extends Visible {

        @XStreamAsAttribute
        public String name;

        @XStreamAsAttribute
        public String type;
    }

    public static class ArgValue {

        public final String string;
        public final String language;

        public ArgValue(String string, String language) {
            this.string = string;
            this.language = language;
        }

        @Override
        public String toString() {
            if (string != null) {
                return "ArgValue(" + string + ")";
            } else {
                return "ArgValue?";
            }
        }
    }

    public enum GeneratedType {

        URI,
        LITERAL,
        TYPED_LITERAL
    }

    public static class GeneratedValue {

        public final GeneratedType type;
        public final String text;
        public final String language;

        public GeneratedValue(GeneratedType type, String text, String language) {
            this.type = type;
            this.text = text;
            this.language = language;
        }

        public GeneratedValue(GeneratedType type, String text) {
            this(type, text, null);
        }

        @Override
        public String toString() {
            return type + ":" + text;
        }
    }

    static class Visible {

        @Override
        public String toString() {
            return Helper.toString(this);
        }
    }

    static class Helper {

        static String toString(Object thing) {
            return "\n" + x3mlStream().toXML(thing);
        }

        public static XStream generatorStream() {
            XStream xstream = new XStream(new PureJavaReflectionProvider(), new XppDriver(new NoNameCoder()));
            xstream.setMode(XStream.NO_REFERENCES);
            xstream.processAnnotations(GeneratorPolicy.class);
            return xstream;
        }

        public static XStream x3mlStream() {
            XStream xstream = new XStream(new PureJavaReflectionProvider(), new XppDriver(new NoNameCoder()));
            xstream.setMode(XStream.NO_REFERENCES);
            xstream.addPermission(NoTypePermission.NONE);
            xstream.allowTypesByWildcard(new String[] {
                "gr.forth.**",
            });
            xstream.processAnnotations(RootElement.class);
            return xstream;
        }

        public static ArgValue argVal(String string, String language) {
            return new ArgValue(string, language);
        }

        public static GeneratedValue uriValue(String uri) {
            return new GeneratedValue(GeneratedType.URI, uri, null);
        }

        public static GeneratedValue literalValue(String literal, String language) {
            return new GeneratedValue(GeneratedType.LITERAL, literal, language);
        }

        public static GeneratedValue literalValue(String literal) {
            return literalValue(literal, null);
        }

        public static GeneratedValue typedLiteralValue(String literal) {
            return new GeneratedValue(GeneratedType.TYPED_LITERAL, literal);
        }
    }
}
