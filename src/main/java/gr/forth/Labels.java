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
package gr.forth;

/** The class contains the necessary resources, in terms of labels that are used 
 * within mappings. The main rationale is to keep all the labels that are declared here, 
 * and exploited in other classes, for reducing the updates, when changes are required.
 * 
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 */
public class Labels {
    
    public static final String ANY="any";
    public static final String BOUND="bound";
    public static final String BROADER_TRANSITIVE="broaderTransitive";
    public static final String CONSTANT="Constant";
    public static final String DIFF_TERMS_DELIMITER="diffTermsDelim";
    public static final String EN_LANGUAGE="en";
    public static final String EXACT_MATCH="exactMatch";
    public static final String HTTP="http";
    public static final String INSTANCE="instance";
    public static final String LABEL="label";
    public static final String LANG="lang";
    public static final String LANGUAGE="language";
    public static final String LITERAL="Literal";
    public static final String NT="nt";
    public static final String NTRIPLES="ntriples";
    public static final String PERCENT_CHARACTER="%";
    public static final String PERCENT_CHARACTER_ENCODED="%25";
    public static final String PREF_LABEL="prefLabel";
    public static final String PREFIX="prefix";
    public static final String REMOVE_ALL_OCCURRENCES="removeAllOccurrences";
    public static final String RDF="rdf";
    public static final String RDFS="rdfs";
    public static final String SAME_MERGING_DELIMITER="<SAME_DELIM>";
    public static final String SAME_TERM_DELIMITER="sameTermsDelim";
    public static final String SKOS="skos";
    public static final String SLASH_CHARACTER="/";
    public static final String SLASH_CHARACTER_ENCODED="%2F";
    public static final String TERM_TO_REMOVE="termToRemove";
    public static final String TEXT="text";
    public static final String REPORT="report";
    public static final String TRIG="trig";
    public static final String TRUE="true";
    public static final String TTL="ttl";
    public static final String TURTLE="turtle";
    public static final String URI="URI";
    public static final String URI_SEPARATOR="uri_separator";
    public static final String URIorUUID="URIorUUID";
    public static final String URN="URN";
    public static final String UUID="UUID";
    public static final String XML="xml";
    public static final String YES="YES";
    
    /* Custom Generator labels */
    public static final String _HASHED_CONTENTS="_HASHED_CONTENTS";
    public static final String _RANDOM_UUID="_RANDOM_UUID";
    
    /* Namespaces */
    public static final String RDF_NAMESPACE="http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDFS_NAMESPACE="http://www.w3.org/2000/01/rdf-schema#";
    public static final String SKOS_NAMESPACE="http://www.w3.org/2004/02/skos/core#";
    public static final String XML_NAMESPACE="http://www.w3.org/XML/1998/namespace";
    
    /* Labels to be exploited from console starter */
    public static final String ASSOC_TABLE="assocTable";
    public static final String INPUT="input";
    public static final String X3ML="x3ml";
    public static final String FORMAT="format";
    public static final String OUTPUT="output";
    public static final String POLICY="policy";
    public static final String REPORT_PROGRESS="reportProgress";
    public static final String UUID_TEST_SIZE="uuidTestSize";
    public static final String MERGE_WITH_ASSOCIATION_TABLE="mergeAssocWithRDF";
    public static final String TERMS="terms";
    public static final String ASSOC_TABLE_SHORT="a";
    public static final String INPUT_SHORT="i";
    public static final String X3ML_SHORT="x";
    public static final String FORMAT_SHORT="f";
    public static final String OUTPUT_SHORT="o";
    public static final String POLICY_SHORT="p";
    public static final String REPORT_PROGRESS_SHORT="r";
    public static final String UUID_TEST_SIZE_SHORT="u";
    public static final String MERGE_WITH_ASSOCIATION_TABLE_SHORT="m";
    public static final String TERMS_SHORT="t";    
    
    /* Labels related to the output */
    public static final String OUTPUT_FORMAT_NTRIPLE="N-TRIPLE";
    public static final String OUTPUT_FORMAT_RDF_XML_ABBREV="RDF/XML-ABBREV";
    public static final String OUTPUT_FORMAT_RDF_XML="RDF/XML";
    public static final String OUTPUT_FORMAT_TRIG="TRIG";
    public static final String OUTPUT_FORMAT_TURTLE="TURTLE";
    public static final String OUTPUT_MIME_TYPE_NTRIPLES="application/n-triples";
    public static final String OUTPUT_MIME_TYPE_TRIG="application/trig";
    public static final String OUTPUT_MIME_TYPE_TURTLE="text/turtle";
    public static final String OUTPUT_MIME_TYPE_RDF_XML="application/rdf+xml";
    public static final String OUTPUT_MIME_TYPE_RDF_XML_ABBREV="application/rdf+xml_plain";
    public static final String OUTPUT_EXTENSION_NTRIPLES="nt";
    public static final String OUTPUT_EXTENSION_RDF="rdf";
    public static final String OUTPUT_EXTENSION_TRIG="trig";
    public static final String OUTPUT_EXTENSION_TURTLE="ttl";
    public static final String OUTPUT_EXTENSION_XML="xml";
}