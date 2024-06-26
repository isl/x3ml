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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.Iterator;

/**
 * This class is used to iterate over a NodeList.
 * So we can use for-each and don't have to always copy NodeList into a List.
 */
public class IterableNodeList implements Iterable<Node> {
    private final NodeList nodeList;

    public IterableNodeList(NodeList nodeList) {
        this.nodeList = nodeList;
    }

    @Override
    public Iterator<Node> iterator() {
        return new Iterator<Node>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < nodeList.getLength();
            }

            @Override
            public Node next() {
                return nodeList.item(index++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove not supported.");
            }
        };
    }

    public int size() {
        return nodeList.getLength();
    }

    public boolean isEmpty() {
        return nodeList.getLength() == 0;
    }
}
