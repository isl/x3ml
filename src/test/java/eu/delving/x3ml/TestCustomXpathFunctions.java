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

import static eu.delving.x3ml.AllTests.*;
import static org.junit.Assert.assertTrue;


import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import gr.forth.ics.isl.x3ml.X3MLEngine;

public class TestCustomXpathFunctions {

    @Test
    public void testUUIDv3Function() {
        X3MLEngine engine = engine("/custom_functions/01-coin-simple.x3ml");
        X3MLEngine.Output output = engine.execute(document("/custom_functions/01-coin-input.xml"), policy("/custom_functions/00-generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        System.out.println(StringUtils.join(mappingResult, "\n"));
        String[] expectedResult = xmlToNTriples("/custom_functions/01-coin-simple-rdf.xml");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }
}
