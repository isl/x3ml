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

import static eu.delving.x3ml.AllTests.document;
import static eu.delving.x3ml.AllTests.engine;
import static eu.delving.x3ml.AllTests.policy;
import static eu.delving.x3ml.AllTests.resource;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import gr.forth.ics.isl.x3ml.X3MLEngine;
import gr.forth.ics.isl.x3ml.engine.GeneratorContext;
import gr.forth.ics.isl.x3ml_reverse_utils.AssociationTable;

public class TestAssociationTable {

    @BeforeClass
    public static void setUp() {
        X3MLEngine.ENABLE_ASSOCIATION_TABLE = true;
    }

    @AfterClass
    public static void tearDown() {
        // because this flag is a static variable we need to make sure that we reset it
        // after we are done with association table tests
        X3MLEngine.ENABLE_ASSOCIATION_TABLE = false;
    }

    @Before
    public void before() {
        AssociationTable.clearAssociationTable();
    }

    @Test
    public void testCustomLiteralGenerator() throws IOException {
        // test to check that proper xpath is generated not only for default Literal
        // generator but also for a custom one like DateNormalizer
        X3MLEngine engine = engine("/association_table/01_date-mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/association_table/01_date-input.xml"),
                policy("/association_table/01_date-generator-policy.xml"));
        output.close();

        String expected = IOUtils.toString(
                resource("/association_table/01_date-expected-association-table.xml"),
                StandardCharsets.UTF_8);
        assertEquals(expected, GeneratorContext.exportAssociationTableToString());
    }
}
