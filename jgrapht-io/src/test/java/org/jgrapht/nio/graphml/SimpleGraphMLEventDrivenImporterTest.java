/*
 * (C) Copyright 2019-2021, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.nio.graphml;

import org.jgrapht.alg.util.*;
import org.junit.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests
 * 
 * @author Dimitrios Michail
 */
public class SimpleGraphMLEventDrivenImporterTest
{

    private static final String NL = System.getProperty("line.separator");

    @Test
    public void testUndirectedUnweighted()
    {
        // @formatter:off
        String input = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL + 
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"" + NL +  
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + 
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL + 
            "<graph id=\"G\" edgedefault=\"undirected\">" + NL + 
            "<edge source=\"2\" target=\"3\"/>" + NL + 
            "<node id=\"1\"/>" + NL +
            "<node id=\"2\"/>" + NL + 
            "<node id=\"3\"/>" + NL +  
            "<edge source=\"1\" target=\"2\"/>" + NL + 
            "<edge source=\"3\" target=\"1\"/>"+ NL + 
            "</graph>" + NL + 
            "</graphml>";
        // @formatter:on

        SimpleGraphMLEventDrivenImporter importer = new SimpleGraphMLEventDrivenImporter();

        List<Pair<Integer, Integer>> collected = new ArrayList<>();
        importer.addEdgeConsumer(q -> {
            assertNull(q.getThird());
            collected.add(Pair.of(Integer.valueOf(q.getFirst()), Integer.valueOf(q.getSecond())));
        });
        importer.importInput(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        int[][] edges = { { 2, 3 }, { 1, 2 }, { 3, 1 } };

        int i = 0;
        for (int[] edge : edges) {
            Pair<Integer, Integer> e = collected.get(i);
            assertEquals(Integer.valueOf(edge[0]), e.getFirst());
            assertEquals(Integer.valueOf(edge[1]), e.getSecond());
            i++;
        }
    }

    @Test
    public void testWithAttributesWeightedGraphs()
    {
        // @formatter:off
        String input =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " + NL +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" " + 
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + NL +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + 
            "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + NL +
            "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\"/>" + NL +
            "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>" + NL +
            "<key id=\"d2\" for=\"edge\" attr.name=\"cost\" attr.type=\"double\"/>" + NL +
            "<graph id=\"G\" edgedefault=\"undirected\">" + NL +
            "<node id=\"n0\">" + NL +
            "<data key=\"d0\">green</data>" + NL +
            "</node>" + NL +
            "<node id=\"n1\"/>" + NL +
            "<node id=\"n2\">" + NL +
            "<data key=\"d0\">blue</data>" + NL +
            "</node>" + NL+
            "<edge id=\"e0\" source=\"n0\" target=\"n2\">" + NL +
            "<data key=\"d1\">2.0</data>" + NL +
            "</edge>" + NL +
            "<edge id=\"e1\" source=\"n0\" target=\"n1\">" + NL +
            "<data key=\"d1\">3.0</data>" + NL +
            "<data key=\"d2\">13.0</data>" + NL +
            "</edge>" + NL +
            "<edge id=\"e2\" source=\"n1\" target=\"n2\"/>" + NL +
            "</graph>" + NL +
            "</graphml>";
        // @formatter:on

        SimpleGraphMLEventDrivenImporter importer = new SimpleGraphMLEventDrivenImporter();

        importer.addEdgeAttributeConsumer((p, a) -> {
            String key = p.getSecond();
            String value = a.getValue();

            if (key.equals("cost")) {
                assertEquals(value, "13.0");
            }
        });

        List<Triple<String, String, Double>> collected = new ArrayList<>();
        importer.addEdgeConsumer(q -> {
            collected.add(q);
        });
        importer.importInput(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        assertEquals(collected.get(0).getFirst(), "n0");
        assertEquals(collected.get(0).getSecond(), "n2");
        assertEquals(collected.get(0).getThird(), 2.0, 1e-9);

        assertEquals(collected.get(1).getFirst(), "n0");
        assertEquals(collected.get(1).getSecond(), "n1");
        assertEquals(collected.get(1).getThird(), 3.0, 1e-9);

        assertEquals(collected.get(2).getFirst(), "n1");
        assertEquals(collected.get(2).getSecond(), "n2");
        assertNull(collected.get(2).getThird());

    }

}
