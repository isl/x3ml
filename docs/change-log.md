# Change Log

## 16 December 2021: V 2.0.0 (by FORTH-ICS)
* Fixed issue with transformation of folders ([issue #136](https://github.com/isl/x3ml/issues/136))
* Made X3ML-Engine a JDK-11 API ([issue #147](https://github.com/isl/x3ml/issues/147))
* Migrated to Apache log4j2 logging ([issue #148](https://github.com/isl/x3ml/issues/148))
* Introduced a method for disabling the logging through X3MLEngineFactory ([issue #149](https://github.com/isl/x3ml/issues/149))
* Report X3ML Engine version ([issue #152](https://github.com/isl/x3ml/issues/152))

## 06 May 2021: V 1.9.5 (by FORTH-ICS)
* Fixed issue with the construction of valid URIs with special characters ([issue #144](https://github.com/isl/x3ml/issues/144))


## 17 July 2020: V 1.9.4 (by FORTH-ICS)
* Generator Policy file validation ([issue #138](https://github.com/isl/x3ml/issues/138))
* Implementioned og MultiHashGenerator ([issue #140](https://github.com/isl/x3ml/issues/140))
* Fixed issue with the generation of aggregated files when using TRIG output format ([issue #141](https://github.com/isl/x3ml/issues/141))
* Fixed issue with the generation of a default namedgraph when nQuads are used ([issue #142](https://github.com/isl/x3ml/issues/142))


## 2 September 2019: V 1.9.3 (by FORTH-ICS)
* Enhanced information in the definition of generators (description and type) ([issue #134](https://github.com/isl/x3ml/issues/134))
* Improved performance for the transformation/values generation process ([issue #133](https://github.com/isl/x3ml/issues/133))


## 3 June 2019: V 1.9.2 (by FORTH-ICS)
* Enhanced built-in generators with UUID-suffixed IRIs ([issue #132](https://github.com/isl/x3ml/issues/132))
* Updated functionality for shortening URIs ([issue #115](https://github.com/isl/x3ml/issues/115))


## 12 Sep 2018: V 1.9.1 (by FORTH-ICS)
* Fixed issues with inconsistencies in the error messages ([issue #126](https://github.com/isl/x3ml/issues/126))
* Fixed (grammatically) a few error messages ([issue #127](https://github.com/isl/x3ml/issues/127))
* Implemented a new generator (UriExistingOrNew) ([issue #129](https://github.com/isl/x3ml/issues/129))
* Support intermediates and additional using namedgraphs ([issue #131](https://github.com/isl/x3ml/issues/131))


## 04 June 2018: V 1.9.0 (by FORTH-ICS)
* Update X3ML Schema (XSD) with the addition of a new mandatory field thesaurus_info ([issue #124](https://github.com/isl/x3ml/issues/124))


## 30 April 2018: V 1.8.5-1 (by FORTH-ICS)
* Throw a warning if all the information about a namespace are missing ([issue #123](https://github.com/isl/x3ml/issues/123))


## 30 April 2018: V 1.8.5 (by FORTH-ICS)
* Fixed issue with redundant encoding of slash character '/' in URI generators ([issue #118](https://github.com/isl/x3ml/issues/118))
* Fixed issue that occurs when namespace information (i.e. prefix or URI) apperas to be empty ([issue #120](https://github.com/isl/x3ml/issues/120))
* Fixed issue with unneeded encoding of already encoded strings when using instance generators ([issue #121](https://github.com/isl/x3ml/issues/121))
* Improved error messages that appear when values for types and relationships (of the target schemata) are unqualified ([issue #119](https://github.com/isl/x3ml/issues/119))
* Fixed issue with the generation of multiple label values when variables are used ([issue #122](https://github.com/isl/x3ml/issues/122))


## 19 March 2018: V 1.8.4 (by FORTH-ICS)
* Fixed issue with the default UUID generator ([issue #112](https://github.com/isl/x3ml/issues/112))
* Fixed issue with unexpected delays when transforming large data ([issue #113](https://github.com/isl/x3ml/issues/113))
* Fixed issue with execution termination if resources are missing ([issue #116](https://github.com/isl/x3ml/issues/116))
* Fixed issue with default language used in label generator ([issue #117](https://github.com/isl/x3ml/issues/117))
* Implemented a new custom generator that outputs the type of a literal value in the output ([issue #111](https://github.com/isl/x3ml/issues/111)) 
* Implemented a new custom generator that shortens URIs ([issue #115](https://github.com/isl/x3ml/issues/115)) 
* Support for reporting the progress of the transformations ([issue #114](https://github.com/isl/x3ml/issues/114))


## 16 Nov 2017: V 1.8.3 (by FORTH-ICS)
* Fixed issue raised with the language tags found inside skos terminologies ([issue #103](https://github.com/isl/x3ml/issues/103))
* Fixed issue with the identification of URIs in RemoveTerm generator ([issue #105](https://github.com/isl/x3ml/issues/105))
* Changed the way XPATH expressions are evaluated to support XPATH (version 1.0) functions ([issue #110](https://github.com/isl/x3ml/issues/110))
* Updated the UriValidation mechanism to support also encoding URIs  ([issue #109](https://github.com/isl/x3ml/issues/109))
* Added SKOS in the list of deafult namespaces ([issue #102](https://github.com/isl/x3ml/issues/102))
* Enhanced RemoveTerm generator to support removing all the occurrences or only the first one ([issue #106](https://github.com/isl/x3ml/issues/106))
* Enhanced RemoveTerm generator to support constructing URIs from simple textual labels using a namespace prefix ([issue #107](https://github.com/isl/x3ml/issues/107))


## 12 Sep 2017: V 1.8.2 (by FORTH-ICS)
* Implemented new MERGE functionality ([issue #97](https://github.com/isl/x3ml/issues/97))
* Updated X3ML Engine to support termonilogy functionalities (e.g. broader and exactMatch terms) ([issue #98](https://github.com/isl/x3ml/issues/98))
* Updated X3ML schema to support termonilogy functionalities (e.g. broader and exactMatch terms) ([issue #99](https://github.com/isl/x3ml/issues/99))
* Updated X3MLEngine executor to support transformations using SKOS terminologies ([issue #100](https://github.com/isl/x3ml/issues/100))
* Updated X3MLEngineFactory SKOS terminologies ([issue #101](https://github.com/isl/x3ml/issues/101))


## 3 Aug 2017: V 1.8.1 (by FORTH-ICS)
* Implemented new Generator RemoveTerm ([issue #95](https://github.com/isl/x3ml/issues/95))
* Enhanced the functionality of conditionals ([issue #92](https://github.com/isl/x3ml/issues/96))


## 17 July 2017: V 1.8.0 (by FORTH-ICS)
* Fixed isuue with ConcatMultipleTerms generator ([issue #91](https://github.com/isl/x3ml/issues/91))
* Fixed isuue with TextualContent generator ([issue #92](https://github.com/isl/x3ml/issues/92))
* Enhanced X3MLEngineFactory methods for ingesting collections of resources (files and input streams) in X3ML Engine ([issue #87](https://github.com/isl/x3ml/issues/87))
* Created a new generator for constructing URNs based on the given textual content ([issue #90](https://github.com/isl/x3ml/issues/90))
* Support a new type for exporting RDF data called RDF/XML_Plain ([issue #94](https://github.com/isl/x3ml/issues/94))
* Fixed issue related to updating the logging levels of X3MLEngineFactory class ([issue #88](https://github.com/isl/x3ml/issues/88))
* Harmonized the version of X3ML XSD files ([issue #89](https://github.com/isl/x3ml/issues/89))


## 21 Mar 2017: V 1.7.5 (by FORTH-ICS)
* Fixed a bug with the URIorUUID instance generator ([issue #72](https://github.com/isl/x3ml/issues/72))
* Fixed a bug with the evaluation of XPATH expressions that contain the xml namespace ([issue #75](https://github.com/isl/x3ml/issues/75))
* Fixed a bug raised when using OutputStream instead of PrintStream for exporting resources ([issue #77](https://github.com/isl/x3ml/issues/77))
* Updated the X3ML xsd to version 1.2 ([issue #81](https://github.com/isl/x3ml/issues/81))
* Support long paths for additional nodes ([issue #80](https://github.com/isl/x3ml/issues/80))
* Exploit the namespaces that are declared inside the info section ([issue #82](https://github.com/isl/x3ml/issues/82))
* Made namespace entries optional ([issue #83](https://github.com/isl/x3ml/issues/83))
* Enhance options for starting X3ML Engine from console ([issue #85](https://github.com/isl/x3ml/issues/85))
* Downgraded guava depedency from version 21.0 to version 20.0 ([issue #79](https://github.com/isl/x3ml/issues/79))
* Support OutputStream resources in the X3MLEngineFactory class (([issue #76](https://github.com/isl/x3ml/issues/76)))
* Support adding input resources (Input data, mappings, generator-policy) from publicly available remote locations (e.g. URLs)  ([issue #78](https://github.com/isl/x3ml/issues/78))


## 8 Feb 2017: V 1.7.4 (by FORTH-ICS)
* Updated the functionality of the custom generator MultipleConcatTerms, that concatenates multiple terms from the input ([issue #69](https://github.com/isl/x3ml/issues/69))
* Changed the dependencies of the X3ML-Engine (([issue #73](https://github.com/isl/x3ml/issues/73)))
* Support InputStream resources in the X3MLEngineFactory class (([issue #74](https://github.com/isl/x3ml/issues/74)))


## 1 Nov 2016: V 1.7.3 (by FORTH-ICS)
* Changed the names of the variables ([issue #70](https://github.com/isl/x3ml/issues/70))


## 21 Oct 2016: V 1.7.2 (by FORTH-ICS)
* Fixed a bug that was raised with multiple input files ([issue #49](https://github.com/isl/x3ml/issues/49))
* Fixed a bug with NullPointerException that is thrown when the X3ML mappings file is not valid with respect to the X3ML schema ([issue #63](https://github.com/isl/x3ml/issues/63))
* Fixed a bug that was raised with the use of variables ([issue #66](https://github.com/isl/x3ml/issues/66))
* Changed the hard-coded way of reporting error messages with a configurable that exploits loggers ([issue #48](https://github.com/isl/x3ml/issues/48))
* Updates in the X3ML language specification schema ([issue #67](https://github.com/isl/x3ml/issues/67))
* Changed the name of the type aware variable ([issue #68](https://github.com/isl/x3ml/issues/68))
* NEW functionality for using an entire folder (contain XML files) as input  ([issue #47](https://github.com/isl/x3ml/issues/47))
* NEW functionality for searching recursively for XML files in an input folder ([issue #62](https://github.com/isl/x3ml/issues/62))
* NEW functionality for ommitting replicating entity generation details when variables are used ([issue #46](https://github.com/isl/x3ml/issues/46))
* NEW functionality creation of an X3ML Engine factory to support easier injection of X3ML engine ([issue #50](https://github.com/isl/x3ml/issues/50))
* NEW functionality for parsing mulitple X3ML mapping files ([issue #61](https://github.com/isl/x3ml/issues/61), [issue #62](https://github.com/isl/x3ml/issues/62),[issue #65](https://github.com/isl/x3ml/issues/65))
* NEW custom generator, that concatenates multiple terms from the input ([issue #69](https://github.com/isl/x3ml/issues/69))


## 1 July 2016: V 1.7.1 (by FORTH-ICS)
* Fixed bug with the generation of entities from attribute values in the XML input ([issue #45](https://github.com/isl/x3ml/issues/45))
* Throw more infomative error messages if the XML input or the X3ML mappings file are not valid XML files ([issue #44](https://github.com/isl/x3ml/issues/44))
* NEW functionality for merging the contents of the association table with the RDF output ([issue #43](https://github.com/isl/x3ml/issues/43))


## 23 Feb 2016: V 1.7.0 (by FORTH-ICS)
* Fixed bug with the creation of Literal instances (i.e. text nodes), if the same XPATH has been used for creating another instance ([issue #33](https://github.com/isl/x3ml/issues/33))
* Fixed bug with the creation of xsd:DateTime instances (i.e. text nodes) ([issue #37](https://github.com/isl/x3ml/issues/37))
* Fixed issue with the type of custom generators ([issue #36](https://github.com/isl/x3ml/issues/36))
* Fixed issue with the exported values from a join operation in the association table ([issue #38](https://github.com/isl/x3ml/issues/38))
* Fixed issue with the generation of rdfs labels when the rdfs namespace is not declared  ([issue #29](https://github.com/isl/x3ml/issues/29))
* Fixed issue with misleading messages with generators (confuses instance with label generators) ([issue #42](https://github.com/isl/x3ml/issues/42))
* Fixed issue with NPE thrown when the arguments of a custom instance generator are invalid ([issue #10](https://github.com/isl/x3ml/issues/10))
* Added a new LabelGenerator (for range of dates) and fixed some bugs with the date generators
* NEW functionality for skipping entire mappings ([issue #31](https://github.com/isl/x3ml/issues/31)) 
* NEW functionality for skipping entire links ([issue #39](https://github.com/isl/x3ml/issues/39)) 
* NEW functionality for parsing multiple XML input files in a single run ([issue #27](https://github.com/isl/x3ml/issues/27))
* NEW functionality - introduced global_variable for supporting global scope for variables ([issue #34](https://github.com/isl/x3ml/issues/34))
* NEW functionality - introduced type_aware_vars for supporting the generation of different URIs for the same XPATH input ([issue #40](https://github.com/isl/x3ml/issues/40))


## 11 Jan 2016: V 1.6.2 (by FORTH-ICS)
* Support for multiple instatiations ([issue #1](https://github.com/isl/x3ml/issues/1))
* fixed bug with empty elements ([issue #4](https://github.com/isl/x3ml/issues/4), [issue #21](https://github.com/isl/x3ml/issues/21))
* fixed bug with namespaces not declared in the namespace section ([issue #5](https://github.com/isl/x3ml/issues/5))
* fixed bug with non-informative messages when a namespace is not declared in the namespaces section ([issue #7](https://github.com/isl/x3ml/issues/7))
* fixed bug with non-informative messages when the label generators contain non-valid attributes (issue #9) or empty expressions ([issue #11](https://github.com/isl/x3ml/issues/11))
* fixed bug with the creation of entities from additional and intermediate nodes ([issue #14](https://github.com/isl/x3ml/issues/14), [issue #15](https://github.com/isl/x3ml/issues/15), [issue #16](https://github.com/isl/x3ml/issues/16), [issue #18](https://github.com/isl/x3ml/issues/18))
* fixed bugs with engine failure when join operations (both simple and double) refered to empty or non-existing elements ([issue #23](https://github.com/isl/x3ml/issues/23), [issue #24](https://github.com/isl/x3ml/issues/24), [issue #25](https://github.com/isl/x3ml/issues/25))
* fixed issue with the creation of labels when they are found only in the domain ([issue #3](https://github.com/isl/x3ml/issues/3))
* support exporting the contents of the association table during runtime ([issue #30](https://github.com/isl/x3ml/issues/30))


## 19 Nov 2015: V 1.6.1 (by FORTH-ICS)
* Added an implementation of the association table
* Added junit tests (for testing nested mappings, multiple instantiations, preflabels)
* Removed System.out messages from junit tests
* Merged previous branches.


## 14 Oct 2015: V 1.6 (by FORTH-ICS)
* Exceptions are also added in a static variable so that they can be exploited after the execution (i.e. by the 3M Editor)
* Support for generation of skos:prefLabel labels
* removed a lot of debug messages (completely useless)


## 07 Aug 2015: V 1.5 (by FORTH-ICS)
* Elimination of relation2 elements. Multiple joins are supported by simply using sequences of relation elements.
* fixed bug with empty elements in the input.
* Added support for AuthorityPrirefCounter and generators based on XPATH position
* fixed bug to support fully qualified URIs


## 27 Feb 2014: V 1.4 (by FORTH-ICS)
* Added a new Relation (named relation2) to support double joins


## 23 Oct 2014: V 1.3 (by Delving BV)
* variables moved back to <entity> but passed into instance generator
* incorporated changes regarding getValueType in CustomGenerator
* integrated URIorUUID custom generator
* fixed bug involving creating new path instances when doing a join


## 2 Aug 2014: V 1.2.2 (by Delving BV)
* variables moved to <instance-generator>


## 21 July 2014: V 1.2.1 (by Delving BV)
* fixed bug with variables - labels and additionals were ignored if not in the first var usage


## 6 July 2014: V 1.2 (by Delving BV)
* Entity element now has *instance_info* attribute 
* *type* attribute added to instance_generator arg
* *ArgValues* interface moved inside *Generator* interface
* Introduced usage of *createTypedLiteral* 
* For specialized instance generation code, introduced *CustomGenerator* interface
* Added *custom* element to GeneratorPolicy, with *generatorClass* which implements *CustomGenerator*
* Better generation of test UUIDs, command line can specify size.
* *DomainContext(path)* syntax added to XPath for non-hierarchical source
* Removed *namespaces* from generator policy, inheriting from X3ML namespaces instead


## 29 April 2014: V 1.1 (by Delving BV)
* Major refactor for code legibility, licensing, and javadoc
* The *property* no longer contains *class* but instead itself holds the qualified name
* The tag *property* has been replaced with *relationship*.
* The *class* element is now *type*.
* The *value_generator* is now *instance_generator*
* Multiple *type* possible within entity, variables hold lists now
* Generator argument types and global *source_type* now lowercase
* Argument type *position* introduced, giving the index of the node in its node list
* Language handling
	* An XPath argument tries to find language from xml:lang in source
	* Generators can override using *language* attribute, language="" for none at all
* Command line formats now "application/rdf+xml", "text/turtle" and "application/n-triples"


## 27 Mar 2014: Initial release v1.0 (by Delving BV)
* XSD Schema validation of X3ML integrated
* Command line has -validation option
* Multiple <class> within <entity> needs some work
* Approach to define generator policy needs to be discussed
