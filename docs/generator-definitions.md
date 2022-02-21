# Well-known generator definitions 

This page contains the definitions and the documentation of a few commonly-used generators. 
Note that this page contains the basic definitions and not their variations (e.g. hashed generators) 
since these are properly covered [here](https://github.com/isl/x3ml/blob/master/docs/x3ml-language.md#simple-templates)

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

// definition and documentation goes here

## ConcatMultipleTerms

// definition and documentation goes here

## URNFromTextualContent

// definition and documentation goes here

## RemoveTerm

// definition and documentation goes here

## URIExistingOrNew

// definition and documentation goes her

## MultipleHashing

// definition and documentation goes her
