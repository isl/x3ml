# Well-known generator definitions 

This page contains the definitions and the documentation of a few commonly-used generators. 
Note that this page contains the basic definitions and not their variations (e.g. hashed generators) 
since these are properly covered [here](https://github.com/isl/x3ml/blob/master/docs/x3ml-language.md#simple-templates)
Examples of the generators described below can be found [here](https://github.com/isl/x3ml/tree/master/src/test/resources/generators)

### Table of Contents

* **[Simple Label](#simple-label)**
* **[Composite Label](#composite-label)**
* **[TypedLiteral](#typedliteral)**
* **[LocalTermURI](#localtermuri)**
* **[URIorUUID](#urioruuid)**
* **[ConcatMultipleTerms](#concatmultipleterms)**
* **[URNFromTextualContent](#urnfromtextualcontent)**
* **[RemoveTerm](#removeterm)**
* **[URIExistingOrNew](#uriexistingornew)**
* **[MultipleHashing](#multiplehashing)**

## Simple Label 

```xml
<generator name="SimpleLabel">
  <pattern>{label}</pattern>
</generator>
```
This generator is used for constructing labels using only one parameter (with the name ```label```). 
The use of this generator has the same effect, with the use of the X3ML-engine built-in
generator [Literal](https://github.com/isl/x3ml/blob/master/docs/x3ml-language.md#literal))

## Composite Label

```xml
<generator name="CompositeLabel">
  <pattern>{term1} {term2}</pattern>
</generator>
```
This generator is used for constructing labels using two parameter (with the names ```term1``` and ```term2```). 
Based on the aforemetioned definition of the generator, the two terms will be separated using a whitespace character. 
Of course other characters, or strings can be used (for example ```{term1}-{term2}```).
Moreover, constant characters and strings can also be added before and after the parameters 
(for example ```first name:{term1} last name:{term2}```

## TypedLiteral

```xml
<generator name="TypedLiteralGen">
  <custom generatorClass="gr.forth.TypedLiteralGenerator">
    <set-arg name="text"/>
  </custom>
</generator>
```

This generator is used for constructing labels of a certain type (e.g. xsd:decimal, xsd:date, etc.).
Its main difference compared to other generators constructing literal values is that this generator 
will create the literal value and it will assiciate it with its corresponding
type (i.e. as it has been declared in the type element of the specific entity element in X3ML).
For example, given the value 2022-02-21 for the parameter text, and having a type declaration 
of xsd:date, the following literal value will be declared: ```"2022-02-21"^^xsd:date```
 
Since this is a custom generator, it only accepts a single parameter (called ```text```). 
If more than one parameters are requested for generating a literal, the proper XPATH functions can be used (e.g. string-join)

## LocalTermURI

```xml
<generator name="LocalTermURI" prefix="pref">
  <pattern>{hierarchy}/{term}</pattern>
</generator>
```

This generator is used for constructing URIs based on two parameters (called ```hierarchy``` and ```term```), separated by a slash character, 
and using as a prefix a namespace with the abbreviation pref. 
As regards the latter, this has to be declared as a namespace in one of the namespaces section in X3ML mappings definition. 
These sections are:
* source info block (```x3ml -> info -> source -> source_info -> namespaces -> namespace```) 
* target info block (```x3ml -> info -> target -> target_info -> namespaces -> namespace```) 
* namespaces block (```x3ml -> namespaces -> namespace```) 

In addition, the LocalTermGenerator may contain the additional parameters, ```shorten``` and ```uuid```, in order to generate a hashed URI, or a random URI as described 
[here](https://github.com/isl/x3ml/blob/master/docs/x3ml-language.md#hashed-uris-with-templates)
and [here](https://github.com/isl/x3ml/blob/master/docs/x3ml-language.md#hashed-uris-with-templates)

## URIorUUID

```xml
<generator name="URIorUUID">
  <custom generatorClass="gr.forth.URIorUUID">
    <set-arg name="text"/>
  </custom>
</generator>
```

This generator is used for constructing URIs based on the given paramter (called ```text```). 
If the generated value is not a valid URI (e.g. invalid URIs are ```something```, ```5```,```this is not a valid uri```, etc.),
then a random UUID is generated for the corresponding entity (e.g. ```urn:uuid:2df18114-7989-4c58-933c-f5f9b04366cd```).

Notice that this particular generator will work properly only if the values of the parameter ```text``` exist. 
If these values do not exist, then no URIs will be generated.

## ConcatMultipleTerms

```xml
<generator name="ConcatMultipleTerms">
  <custom generatorClass="gr.forth.ConcatMultipleTerms">
    <set-arg name="prefix" type="constant"/>
    <set-arg name="sameTermsDelim" type="constant"/>
    <set-arg name="diffTermsDelim" type="constant"/>
    <set-arg name="text1"/>
    <set-arg name="text2"/>
  </custom>
</generator>
```

This generator is used for producing a URI or a label by merging several occurrences of particular elements from the input. 
The generator allows merging many different elements, identified from the parameters ```text1```, ```text2```, ```text3```, etc.
Moreover, the occurences of the same element (e.g. the one identified from the parameter ```text1```), can be merged using their own 
delimeter, with respect to occurrences of different elements. A complete explanation of the parameters of the generator is given below.
* **prefix**: it is a constant value and denotes the prefix that should be used before concatenating the terms from the input. If the prefix value starts with http then the generated value will be a URI, otherwise, if it is empty or anything else it will generate a literal value.
* **sameTermsDelim**: the delimeter that will be used within the concatenated terms of the same element (the one defined from text#)
* **diffTermsDelim**: the delimeter that will be used within the groups of concatenated terms of different elements 
* **text#**: an arbitrary number of terms indicating the XPath expressions that will fetch the terms that will be concatenated. this term text should be followed by an integer indicating the execution order for the concatenation; for example the terms under text1 will be concatenated, then the terms under text2, etc.


## URNFromTextualContent

```xml
<generator name="URNfromTextualContent">
  <custom generatorClass="gr.forth.TextualContent">
    <set-arg name="text" type="xpath"/>
  </custom>
</generator>
```

This generator is used for constructing a URN using a single parameter (called ```text```). 
It will create URNs of the form ```URN:UUID:d6d2800a-f5d1-3768-9c47-3a5494246c2e```. 
The URN is created by applying a hashing over the textual contents of the given parameter.
This ensures that the same URN will be created for the same textual contents.

The aforementioned generator can be combined with a ```prefix``` attribute. 
In this case, instead of a URN, a URI with the corresponding prefix will be created.

## RemoveTerm

```xml
<generator name="RemoveTerm">
  <custom generatorClass="gr.forth.RemoveTerm">
    <set-arg name="termToRemove" type="constant"/>
    <set-arg name="removeAllOccurrences" type="constant"/>
    <set-arg name="text"/>
  </custom>
</generator>
```

The generator is used for constructing a URI or a label using the contents of a single parameter (called ```text```), 
after removing a particular text segment from it. More details about the parameters of the generator are given below. 
* **text**: the textual content (e.g. taken from the input using a proper XPATH expression)
* **termToRemove**: the term or text segment that will be removed from the given ```text```
* **removeAllOccurrences**: if true then it will remove all the occurrences of ```termToRemove``` from the given ```text```

The aforementioned generator can be combined with a ```prefix``` attribute. 
In this case, instead of a literal, a URI with the corresponding prefix will be created.

## URIExistingOrNew

```xml
<generator name="UriExistingOrNew" prefix="pref">
  <custom generatorClass="gr.forth.UriExistingOrNew">
    <set-arg name="uri" type="xpath"/>
    <set-arg name="text1"/>
    <set-arg name="uri_separator1" type="constant"/>
  </custom>
</generator>
```

This generator can be used for constructing a URI, 
by reusing an existing URI from the input.
If the URI does not exist then it will construct one using a specific pattern. 
The detailed parameters of the generator are shown below:
* **uri**: It is used for declaring the XPATH expression where a potential URI can be found in the input
* **text#**: It is used for expressing which parts of the input will be used for constructing a new URI (if one does not exist). The generator can accept many arguments of this type. The assumption is that the first one will have the name text1, the second one will have the name text2, etc.
* **uri_separator#**: It is used for expressing the characters that will be used between text arguments. The generator can accept many arguments of this type. The assumption is that the first one will have the name uri_separator1, the second one will have the name uri_separator2, etc.

It becomes evident that ```uri``` is used for retrieving the value of an existing URI, while ```text#``` and ```uri_separator#``` 
are used for constructing a new one. The latter are combined using the following scheme:
```namespace_prefix+text1+uri_separator1+text2+uri_separator2+...```

Important: The number of text arguments should be equal to the number of uri_separator_arguments.

## MultipleHashing

```xml
<generator name="MultiHashingGenerator_random" prefix="pref">
  <custom generatorClass="gr.forth.MultiHashingGenerator">
    <set-arg name="hierarchy"/>
    <set-arg name="object_HASHED_CONTENTS"/>
    <set-arg name="term_simple"/>
    <set-arg name="term_RANDOM_UUID"/>
  </custom>
</generator>
```

This generator is used for constructing hashes or producing random UUIDs based on the contents of its arguments.
The difference between this generator and the embedded functionality that produces hashes 
(described [here](https://github.com/isl/x3ml/blob/master/docs/x3ml-language.md#hashed-uris-with-templates)) 
is that hashing or random UUID generation will be supported in several arguments. 

It uses two special suffix encodings for identifying which arguments will be hashed or for which a random UUID will be generated, 
namely \_HASHED_CONTENTS and \_RANDOM_UUID. 
The behaviour of the generator is the following:
* if the argument name ends with the suffix ```_HASHED_CONTENTS``` then a UUID will be generated using the contents of the argument
* if the argument name ends with the suffix ```_RANDOM_UUID``` then a random UUID will be generated, ignoring therefore the contents of the argument (e.g. the value of the argument could be empty in the mappings definition file)
* if the argument name does not fall in any of the above, then it will be evaluated as usual.
