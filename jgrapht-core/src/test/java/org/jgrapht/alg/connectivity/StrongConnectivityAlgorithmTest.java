/*
 * (C) Copyright 2003-2018, by Sarah Komla-Ebri and Contributors.
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
package org.jgrapht.alg.connectivity;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for the GabowStrongConnectivityInspector. Tests are identical to the tests for the
 * KosarajuStrongConnectivityInspector as provided in the ConnectivityInspectorTest class.
 *
 * @author Sarah Komla-Ebri
 * @author Joris Kinable
 */
public class StrongConnectivityAlgorithmTest
{
    // ~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";
    private static final String V4 = "v4";
    private static final String V5 = "v5";

    // ~ Instance fields --------------------------------------------------------

    //
    @Test
    public void testStrongConnectivityClasses()
    {
        Class<?>[] strongConnectivityAlgorithmClasses =
            { GabowStrongConnectivityInspector.class, KosarajuStrongConnectivityInspector.class };
        for (Class<?> strongConnectivityAlgorithm : strongConnectivityAlgorithmClasses) {
            this.testStronglyConnected1(strongConnectivityAlgorithm);
            this.testStronglyConnected2(strongConnectivityAlgorithm);
            this.testStronglyConnected3(strongConnectivityAlgorithm);
            this.testStronglyConnected4(strongConnectivityAlgorithm);
        }
    }

    /**
     * .
     */
    public void testStronglyConnected1(Class<?> strongConnectivityAlgorithm)
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);

        g.addEdge(V1, V2);
        g.addEdge(V2, V1); // strongly connected

        g.addEdge(V3, V4); // only weakly connected

        StrongConnectivityAlgorithm<String, DefaultEdge> inspector =
            this.getStrongConnectivityInspector(g, strongConnectivityAlgorithm);

        // convert from List to Set because we need to ignore order
        // during comparison
        Set<Set<String>> actualSets = new HashSet<>(inspector.stronglyConnectedSets());

        // construct the expected answer
        Set<Set<String>> expectedSets = new HashSet<>();
        Set<String> set = new HashSet<>();
        set.add(V1);
        set.add(V2);
        expectedSets.add(set);
        set = new HashSet<>();
        set.add(V3);
        expectedSets.add(set);
        set = new HashSet<>();
        set.add(V4);
        expectedSets.add(set);

        assertEquals(expectedSets, actualSets);

        actualSets.clear();

        List<Graph<String, DefaultEdge>> subgraphs = inspector.getStronglyConnectedComponents();
        for (Graph<String, DefaultEdge> sg : subgraphs) {
            actualSets.add(sg.vertexSet());

            StrongConnectivityAlgorithm<String, DefaultEdge> ci =
                this.getStrongConnectivityInspector(sg, strongConnectivityAlgorithm);
            assertTrue(ci.isStronglyConnected());
        }

        assertEquals(expectedSets, actualSets);
    }

    /**
     * .
     */
    public void testStronglyConnected2(Class<?> strongConnectivityAlgorithm)
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);

        g.addEdge(V1, V2);
        g.addEdge(V2, V1); // strongly connected

        g.addEdge(V4, V3); // only weakly connected
        g.addEdge(V3, V2); // only weakly connected

        StrongConnectivityAlgorithm<String, DefaultEdge> inspector =
            this.getStrongConnectivityInspector(g, strongConnectivityAlgorithm);

        // convert from List to Set because we need to ignore order
        // during comparison
        Set<Set<String>> actualSets = new HashSet<>(inspector.stronglyConnectedSets());

        // construct the expected answer
        Set<Set<String>> expectedSets = new HashSet<>();
        Set<String> set = new HashSet<>();
        set.add(V1);
        set.add(V2);
        expectedSets.add(set);
        set = new HashSet<>();
        set.add(V3);
        expectedSets.add(set);
        set = new HashSet<>();
        set.add(V4);
        expectedSets.add(set);

        assertEquals(expectedSets, actualSets);

        actualSets.clear();

        List<Graph<String, DefaultEdge>> subgraphs = inspector.getStronglyConnectedComponents();
        for (Graph<String, DefaultEdge> sg : subgraphs) {
            actualSets.add(sg.vertexSet());

            StrongConnectivityAlgorithm<String, DefaultEdge> ci =
                this.getStrongConnectivityInspector(sg, strongConnectivityAlgorithm);
            assertTrue(ci.isStronglyConnected());
        }

        assertEquals(expectedSets, actualSets);
    }

    /**
     * .
     */
    public void testStronglyConnected3(Class<?> strongConnectivityAlgorithm)
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);

        g.addEdge(V1, V2);
        g.addEdge(V2, V3);
        g.addEdge(V3, V1); // strongly connected

        g.addEdge(V1, V4);
        g.addEdge(V2, V4);
        g.addEdge(V3, V4); // weakly connected

        StrongConnectivityAlgorithm<String, DefaultEdge> inspector =
            this.getStrongConnectivityInspector(g, strongConnectivityAlgorithm);

        // convert from List to Set because we need to ignore order
        // during comparison
        Set<Set<String>> actualSets = new HashSet<>(inspector.stronglyConnectedSets());

        // construct the expected answer
        Set<Set<String>> expectedSets = new HashSet<>();
        Set<String> set = new HashSet<>();
        set.add(V1);
        set.add(V2);
        set.add(V3);
        expectedSets.add(set);
        set = new HashSet<>();
        set.add(V4);
        expectedSets.add(set);

        assertEquals(expectedSets, actualSets);

        actualSets.clear();

        List<Graph<String, DefaultEdge>> subgraphs = inspector.getStronglyConnectedComponents();

        for (Graph<String, DefaultEdge> sg : subgraphs) {
            actualSets.add(sg.vertexSet());

            StrongConnectivityAlgorithm<String, DefaultEdge> ci =
                this.getStrongConnectivityInspector(sg, strongConnectivityAlgorithm);
            assertTrue(ci.isStronglyConnected());
        }

        assertEquals(expectedSets, actualSets);
    }

    public void testStronglyConnected4(Class<?> strongConnectivityAlgorithm)
    {
        DefaultDirectedGraph<Integer, String> graph = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createStringSupplier(), false);

        new RingGraphGenerator<Integer, String>(3).generateGraph(graph);

        StrongConnectivityAlgorithm<Integer, String> sc =
            this.getStrongConnectivityInspector(graph, strongConnectivityAlgorithm);
        List<Set<Integer>> expected = new ArrayList<>();
        expected.add(graph.vertexSet());
        assertEquals(expected, sc.stronglyConnectedSets());
    }

    @Test
    public void testCondensation()
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);

        g.addEdge(V1, V2);
        g.addEdge(V2, V1); // strongly connected

        g.addEdge(V3, V4); // only weakly connected
        g.addEdge(V5, V4); // only weakly connected

        StrongConnectivityAlgorithm<String, DefaultEdge> inspector =
            new GabowStrongConnectivityInspector<>(g);

        Graph<Graph<String, DefaultEdge>, DefaultEdge> condensation = inspector.getCondensation();
        assertEquals(
            "([([v1, v2], [(v1,v2), (v2,v1)]), ([v4], []), ([v3], []), ([v5], [])], [(([v3], []),([v4], [])), (([v5], []),([v4], []))])",
            condensation.toString());
    }

    @Test
    public void testCondensation2()
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);

        g.addEdge(V1, V2);
        g.addEdge(V2, V1);
        g.addEdge(V3, V4);
        g.addEdge(V4, V3);

        g.addEdge(V1, V3);
        g.addEdge(V2, V4);

        StrongConnectivityAlgorithm<String, DefaultEdge> inspector =
            new GabowStrongConnectivityInspector<>(g);

        Graph<Graph<String, DefaultEdge>, DefaultEdge> condensation = inspector.getCondensation();
        assertEquals(
            "([([v3, v4], [(v3,v4), (v4,v3)]), ([v1, v2], [(v1,v2), (v2,v1)])], [(([v1, v2], [(v1,v2), (v2,v1)]),([v3, v4], [(v3,v4), (v4,v3)]))])",
            condensation.toString());
    }

    private <V, E> StrongConnectivityAlgorithm<V, E> getStrongConnectivityInspector(
        Graph<V, E> graph, Class<?> strongConnectivityAlgorithm)
    {
        if (strongConnectivityAlgorithm == GabowStrongConnectivityInspector.class)
            return new GabowStrongConnectivityInspector<>(graph);
        else if (strongConnectivityAlgorithm == KosarajuStrongConnectivityInspector.class)
            return new KosarajuStrongConnectivityInspector<>(graph);
        else
            throw new IllegalArgumentException("Unknown strongConnectivityInspectorClass");
    }
}
