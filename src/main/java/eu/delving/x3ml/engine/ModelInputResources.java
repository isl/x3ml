/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.delving.x3ml.engine;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yannis Marketakis (marketak 'at' ics 'dot' forth 'dot' gr)
 */
public class ModelInputResources {
    public Model modelInput;
    
    public ModelInputResources(Model model){
        this.modelInput=model;
    }
    
    public List<Resource> getResources(Model model, X3ML.Source source){
        if (source != null) {
            return getResources(model, source.expression);
        } else {
            List<Resource> list = new ArrayList<Resource>(0);
            //to check
            return list;
        }
    }

    public List<Resource> getResources(Model model, String expression) {
        List<Resource> list = new ArrayList<Resource>();
        if (expression == null || expression.length() == 0) {
            //to check
            return list;
        }
            for(Statement st : model.listStatements(null, new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), new ResourceImpl(expression)).toList()){
                list.add(st.getSubject());
            }
            return list;
    }
    
    public List<Resource> getResourcesForPath(Model model, Resource resource,  String expression) {
        List<Resource> list = new ArrayList<Resource>();
        if (expression == null || expression.length() == 0) {
            //to check
            return list;
        }
            for(RDFNode node : model.listObjectsOfProperty(resource, new PropertyImpl(expression)).toList()){
                list.add(node.asResource());
            }
            return list;
    }
    
    public List<Resource> getResourcesForRange(Model model, Resource domainResource, String propertyExpr, String rangeExpr) {
        System.out.println("GIVEN: "+domainResource+"\t"+propertyExpr+"\t"+rangeExpr);
        List<Resource> list = new ArrayList<Resource>();
        if (rangeExpr == null || rangeExpr.length() == 0 || propertyExpr == null || propertyExpr.length() == 0) {
            //to check
            return list;
        }
        for(RDFNode node : model.listObjectsOfProperty(domainResource, new PropertyImpl(propertyExpr)).toList()){
            for(Statement st2 : model.listStatements(node.asResource(), new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), new ResourceImpl(rangeExpr.trim())).toList()){
                list.add(st2.getSubject());
            }
        }
            return list;
    }
    
    public X3ML.ArgValue getLabel(Resource resource, String fromProperty, X3ML.SourceType sourceType){
        System.out.println("TYPE: "+sourceType+"\t"+fromProperty);
        
        if(sourceType == X3ML.SourceType.constant){
            return new X3ML.ArgValue(fromProperty, "en");
        }
            
        RDFNode node=modelInput.listObjectsOfProperty(resource, new PropertyImpl(fromProperty)).next();
        X3ML.ArgValue argVal=new X3ML.ArgValue(node.toString(),"en");
        System.out.println("RETURNED VALUE: "+argVal+"\t"+argVal.language);
        return argVal;
    }
    
    public List<String> getValueForJoin(String classUri, String propertyUri){
        List<String> retList=new ArrayList<String>();
        List<Resource> nodes=this.modelInput.listSubjectsWithProperty(new PropertyImpl(propertyUri)).toList();
        for(Resource node : nodes){
            if(!this.modelInput.listStatements(node, new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), new ResourceImpl(classUri)).toList().isEmpty()){
                retList.add(this.modelInput.listObjectsOfProperty(node, new PropertyImpl(propertyUri)).toList().get(0).toString());
            }
        }
        return retList;
    }
    
    public List<String> getValueForJoin(List<Resource> resources, String propertyUri){
        List<String> retList=new ArrayList<String>();
        for(Resource node : resources){
            for(RDFNode objectNode: this.modelInput.listObjectsOfProperty(node, new PropertyImpl(propertyUri)).toList()){
                retList.add(objectNode.toString());
            }
        }
        return retList;
    }
    
    public List<Resource> getResourcesForJoin(String subject, String predicate, String object){
        List<Resource> retList=new ArrayList<Resource>();
        List<Resource> nodes=this.modelInput.listSubjectsWithProperty(new PropertyImpl(predicate),object).toList();
        for(Resource node : nodes){
            if(!this.modelInput.listStatements(node, new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), new ResourceImpl(subject)).toList().isEmpty()){
                retList.add(node.asResource());
            }
        }
        return retList;
    }
    
    public List<Resource> getResourcesForJoin(String subject, String predicate, List<String> objects){
        List<Resource> retList=new ArrayList<Resource>();
        for(String object : objects){
            List<Resource> nodes=this.modelInput.listSubjectsWithProperty(new PropertyImpl(predicate),object).toList();
            for(Resource node : nodes){
                if(!this.modelInput.listStatements(node, new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), new ResourceImpl(subject)).toList().isEmpty()){
                    retList.add(node.asResource());
                }
            }
        }
        return retList;
    }
    
    public List<Resource> getResourcesForDJoin(String subject, String predicate, String object){
        List<Resource> retList=new ArrayList<Resource>();
        List<Resource> nodes=this.modelInput.listSubjectsWithProperty(new PropertyImpl(predicate),object).toList();
        for(Resource node : nodes){
            if(!this.modelInput.listStatements(node, new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), new ResourceImpl(subject)).toList().isEmpty()){
                retList.add(node.asResource());
            }
        }
        return retList;
    }
    
    public String evaluateCondition(String domainUri, String expression){
        String[] values=expression.split("--"); //THE SIZE SHOULD ALWAYS BE ODD
        String resName="?val"+(values.length/2);
        String sparqlQuery="SELECT "+resName+" WHERE{ ";
       
        if(values.length==1){
            sparqlQuery+="<"+domainUri+"> <"+values[0].trim()+"> ?val0\n"; 
        }else{
            sparqlQuery+="<"+domainUri+"> <"+values[0].trim()+"> ?val0 . \n";
            int id=1;
            for(int i=1;i<values.length;i+=2){
                sparqlQuery+="?val"+(id-1)+" <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+values[i].trim()+">. \n"
                            +"?val"+(id-1)+" <"+values[i+1].trim()+"> ?val"+id+". \n";
                id++;
            }
        }
        sparqlQuery+="}";
        Query query = QueryFactory.create(sparqlQuery);
        QueryExecution qe = QueryExecutionFactory.create(query, this.modelInput);
        com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
        String retValue="";
        if(results.hasNext()){

            retValue=results.next().get(resName.replace("?", "")).toString().trim();
        }
        qe.close(); 
        return retValue;
    }
    
}
