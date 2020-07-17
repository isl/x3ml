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
package eu.delving.x3ml;

import gr.forth.ics.isl.x3ml.X3MLEngine;
import static eu.delving.x3ml.AllTests.compareNTriples;
import static eu.delving.x3ml.AllTests.document;
import static eu.delving.x3ml.AllTests.engine;
import static eu.delving.x3ml.AllTests.errorFree;
import static eu.delving.x3ml.AllTests.policy;
import static eu.delving.x3ml.AllTests.xmlToNTriples;
import java.io.FileNotFoundException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author Yannis Marketakis (marketak 'at' ics 'dot' forth 'dot' gr)
 * @author Nikos Minadakis (minadakn 'at' ics 'dot' forth 'dot' gr)
 */
public class TestGenerators {
    private final Logger log = Logger.getLogger(getClass());

    @Test
    public void testBMDateGenWithAbbrevType() throws FileNotFoundException {
        X3MLEngine engine = engine("/generators/01_BMDates_mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/generators/01_BMDates_input.xml"),policy("/generators/01_BMDates_generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/generators/01_BMDates_expectedOutput.rdf");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\nLINES:"+ diff.size() + "\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }   
    
    @Test
    public void testBMDateGenWithFullUriType() throws FileNotFoundException {
        X3MLEngine engine = engine("/generators/02_BMDates_mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/generators/01_BMDates_input.xml"),policy("/generators/01_BMDates_generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/generators/01_BMDates_expectedOutput.rdf");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\nLINES:"+ diff.size() + "\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }   
    
    @Test
    public void testConcatMutipleTerms(){
        X3MLEngine engine = engine("/generators/03_ConcatMultiple-mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/generators/03_ConcatMultiple-input.xml"),policy("/generators/03_ConcatMultiple-generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/generators/03_ConcatMultiple-expectedOutput.rdf");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\nLINES:"+ diff.size() + "\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }
    
    @Test
    public void testUrnFromTextualContent(){
        X3MLEngine engine = engine("/generators/05_URNfromTextualContent-mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/generators/05_URNfromTextualContent-input.xml"),policy("/generators/05_URNfromTextualContent-generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/generators/05_URNfromTextualContent-expectedOutput.rdf");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\nLINES:"+ diff.size() + "\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }
    
    @Test
    public void testURIirUUID(){
        X3MLEngine engine = engine("/generators/04_URIorUUID-mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/generators/04_URIorUUID-input.xml"),policy("/generators/04_URIorUUID-generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/generators/04_URIorUUID-expectedOutput.rdf");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\nLINES:"+ diff.size() + "\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }
    
    @Test
    public void testRemoveTermGenerator(){
        X3MLEngine engine = engine("/generators/06_1_RemoveTerm-mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/generators/06_1_RemoveTerm-input.xml"),policy("/generators/06_RemoveTerm-generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/generators/06_1_RemoveTerm-expectedOutput.rdf");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\nLINES:"+ diff.size() + "\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }
    
    @Test
    public void testRemoveTermGeneratorAllOccurrences(){
        X3MLEngine engine = engine("/generators/06_2_RemoveTerm-mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/generators/06_2_RemoveTerm-input.xml"),policy("/generators/06_RemoveTerm-generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/generators/06_2_RemoveTerm-expectedOutput.rdf");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\nLINES:"+ diff.size() + "\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }
    
    @Test
    public void testRemoveTermGeneratorWithPrefix(){
        X3MLEngine engine = engine("/generators/06_3_RemoveTerm-mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/generators/06_3_RemoveTerm-input.xml"),policy("/generators/06_RemoveTerm-generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/generators/06_3_RemoveTerm-expectedOutput.rdf");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\nLINES:"+ diff.size() + "\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }
    
    @Test
    public void testTypedLiteralGenerator(){
        X3MLEngine engine = engine("/generators/07_TypedLiteralGen_mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/generators/07_TypedLiteralGen_input.xml"),policy("/generators/07_TypedLiteralGen_generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/generators/07_TypedLiteralGen-expectedOutput.rdf");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\nLINES:"+ diff.size() + "\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }
    
    @Test
    public void testHashedUrisGenerator(){
        X3MLEngine engine = engine("/generators/08_HashedUris-mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/generators/08_HashedUris-input.xml"),policy("/generators/08_HashedUris-generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/generators/08_HashedUris-expectedOutput.rdf");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\nLINES:"+ diff.size() + "\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }
    
    @Test
    public void testCustomInstanceGenerators(){
        X3MLEngine engine = engine("/generators/09_CustomInstanceGenerators-mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/generators/09_CustomInstanceGenerators-input.xml"),policy("/generators/09_CustomInstanceGenerators-generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/generators/09_CustomInstanceGenerators-expectedOutput.rdf");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\nLINES:"+ diff.size() + "\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }
    
    @Test
    public void testUriExistingOrNewGenerator(){
        X3MLEngine engine = engine("/generators/10_UriExistingOrNew-mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/generators/10_UriExistingOrNew-input.xml"),policy("/generators/10_UriExistingOrNew-generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/generators/10_UriExistingOrNew-expectedOutput.rdf");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\nLINES:"+ diff.size() + "\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }
    
    @Test
    public void testMultiHashingGenerator(){
        X3MLEngine engine = engine("/generators/11_MultiHashedUris-mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/generators/11_MultiHashedUris-input.xml"),policy("/generators/11_MultiHashedUris-generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/generators/11_MultiHashedUris-expectedOutput.rdf");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\nLINES:"+ diff.size() + "\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }
}