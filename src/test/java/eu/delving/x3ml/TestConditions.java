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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import static eu.delving.x3ml.AllTests.compareNTriples;
import static eu.delving.x3ml.AllTests.document;
import static eu.delving.x3ml.AllTests.engine;
import static eu.delving.x3ml.AllTests.errorFree;
import static eu.delving.x3ml.AllTests.policy;
import static eu.delving.x3ml.AllTests.xmlToNTriples;
import gr.forth.ics.isl.x3ml.X3MLEngine;
import gr.forth.ics.isl.x3ml.engine.GeneratorContext;
import gr.forth.ics.isl.x3ml.engine.VariableScope;
import gr.forth.ics.isl.x3ml.engine.X3ML;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Gerald de Jong <gerald@delving.eu>
 * @author Nikos Minadakis <minadakn@ics.forth.gr>
 * @author Yannis Marketakis <marketak@ics.forth.gr>
 */

public class TestConditions implements X3ML {
    private final Logger log = Logger.getLogger(getClass());

    @Test
    public void testBasic() {
//        use(
//                "<if>",
//                "  <exists>gumby</exists>",
//                "</if>"
//        )
//                .expect(false)
//                .put("gumby", "yes").expect(true);

        use(
                "<if>",
                "  <equals value=\"pokey\">gumby</equals>",
                "</if>"
        )
                .expect(false)
                .put("gumby", "man").expect(false)
                .put("gumby", "pokey").expect(true);
    }

    @Test
    public void testBooleans() {
        use(
                "<if>",
                "  <not>",
                "    <if>",
                "      <equals value=\"clay man\">gumby</equals>",
                "    </if>",
                "  </not>",
                "</if>"
        )
                .expect(true)
                .put("gumby", "horse").expect(true)
                .put("gumby", "clay man").expect(false);

//        use(
//                "<if>",
//                "  <and>",
//                "    <if>",
//                "      <exists>gumby</exists>",
//                "    </if>",
//                "    <if>",
//                "      <equals value=\"horse\">pokey</equals>",
//                "    </if>",
//                "  </and>",
//                "</if>"
//        )
//                .expect(false)
//                .put("gumby", "present").expect(false)
//                .put("pokey", "dog").expect(false)
//                .put("pokey", "horse").expect(true)
//                .remove("gumby").expect(false);
//
//        use(
//                "<if>",
//                "  <or>",
//                "    <if>",
//                "      <exists>gumby</exists>",
//                "    </if>",
//                "    <if>",
//                "      <equals value=\"horse\">pokey</equals>",
//                "    </if>",
//                "  </or>",
//                "</if>"
//        )
//                .expect(false)
//                .put("gumby", "present").expect(true)
//                .remove("gumby").expect(false)
//                .put("pokey", "dog").expect(false)
//                .put("pokey", "horse").expect(true)
//                .remove("pokey").expect(false);
    }

    @Test
    public void testDisjunctiveComparisonsCompact(){
        X3MLEngine engine = engine("/conditionals/mappings.x3ml");
        X3MLEngine.Output output = engine.execute(document("/conditionals/input.xml"),policy("/conditionals/generator-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/conditionals/expectedOutput.rdf");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\nLINES:"+ diff.size() + "\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }
    
    // ====================================

    static Case use(String... conditionLines) {
        return new Case(conditionLines);
    }

    static class Case extends GeneratorContext {
        Condition condition;
        Map<String, String> known = new TreeMap<String, String>();

        Case(String... conditionLines) {
            super(null, null, null, 0);
            String conditionString = TestConditions.toString(conditionLines);
            this.condition = (Condition) stream().fromXML(conditionString);
            String check = stream().toXML(condition);
            assertEquals("serialization problem", conditionString, check);
        }

        Case put(String key, String value) {
            known.put(key, value);
            return this;
        }

        Case remove(String key) {
            known.remove(key);
            return this;
        }

        Case expect(boolean outcome) {
            boolean success = outcome != condition.failure(this);
            if (!success) {
                fail(condition.toString());
            }
            return this;
        }

        @Override
        public GeneratedValue get(String variable, VariableScope scope) {
            return null;
        }

        @Override
        public void put(String variable, VariableScope scope, GeneratedValue generatedValue) {
        }

        @Override
        public String evaluate(String expression) {
            String evaluation = known.get(expression);
            return evaluation == null ? "" : evaluation;
        }
    }

    private static String toString(String[] array) {
        return StringUtils.join(Arrays.asList(array), "\n");
    }

    private static XStream stream() {
        XStream xstream = new XStream(new PureJavaReflectionProvider(), new XppDriver(new NoNameCoder()));
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.processAnnotations(X3ML.Condition.class);
        return xstream;
    }
}
