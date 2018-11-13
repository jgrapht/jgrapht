/*
 * (C) Copyright 2003-2018, by John V Sichi and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * .
 *
 * @author John V. Sichi
 */
public class CycleDetectorTest
{
    // ~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";
    private static final String V4 = "v4";
    private static final String V5 = "v5";
    private static final String V6 = "v6";
    private static final String V7 = "v7";

    // ~ Methods ----------------------------------------------------------------

    /**
     * .
     *
     * @param g
     */
    public void createGraph(Graph<String, DefaultEdge> g)
    {
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);
        g.addVertex(V6);
        g.addVertex(V7);

        g.addEdge(V1, V2);
        g.addEdge(V2, V3);
        g.addEdge(V3, V4);
        g.addEdge(V4, V1);
        g.addEdge(V4, V5);
        g.addEdge(V5, V6);
        g.addEdge(V1, V6);

        // test an edge which leads into a cycle, but where the source
        // is not itself part of a cycle
        g.addEdge(V7, V1);
    }

    /**
     * .
     */
    @Test
    public void testDirectedWithCycle()
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        createGraph(g);

        Set<String> cyclicSet = new HashSet<>();
        cyclicSet.add(V1);
        cyclicSet.add(V2);
        cyclicSet.add(V3);
        cyclicSet.add(V4);

        Set<String> acyclicSet = new HashSet<>();
        acyclicSet.add(V5);
        acyclicSet.add(V6);
        acyclicSet.add(V7);

        runTest(g, cyclicSet, acyclicSet);
    }

    /**
     * .
     */
    @Test
    public void testDirectedWithDoubledCycle()
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        // build the graph: vertex order is chosen specifically
        // to exercise old bug-cases in CycleDetector
        g.addVertex(V2);
        g.addVertex(V1);
        g.addVertex(V3);

        g.addEdge(V1, V2);
        g.addEdge(V2, V3);
        g.addEdge(V3, V1);
        g.addEdge(V2, V1);

        Set<String> cyclicSet = new HashSet<>();
        cyclicSet.add(V1);
        cyclicSet.add(V2);
        cyclicSet.add(V3);

        Set<String> acyclicSet = new HashSet<>();

        runTest(g, cyclicSet, acyclicSet);
    }

    /**
     * .
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDirectedWithoutCycle()
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        createGraph(g);
        g.removeVertex(V2);

        Set<String> cyclicSet = Collections.EMPTY_SET; // hb: I would like
                                                       // EMPTY_SET to be typed
                                                       // as well...
        Set<String> acyclicSet = g.vertexSet();

        runTest(g, cyclicSet, acyclicSet);
    }

    private void runTest(
        Graph<String, DefaultEdge> g, Set<String> cyclicSet, Set<String> acyclicSet)
    {
        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(g);

        Set<String> emptySet = Collections.emptySet();

        assertEquals(!cyclicSet.isEmpty(), detector.detectCycles());

        assertEquals(cyclicSet, detector.findCycles());

        for (String v : cyclicSet) {
            assertEquals(true, detector.detectCyclesContainingVertex(v));
            assertEquals(cyclicSet, detector.findCyclesContainingVertex(v));
        }

        for (String v : acyclicSet) {
            assertEquals(false, detector.detectCyclesContainingVertex(v));
            assertEquals(emptySet, detector.findCyclesContainingVertex(v));
        }
    }

    @Test
    public void testVertexEquals()
    {
        DefaultDirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        assertEquals(0, graph.edgeSet().size());

        String vertexA = "A";
        String vertexB = "B";
        String vertexC = new String("A");

        assertNotSame(vertexA, vertexC);

        graph.addVertex(vertexA);
        graph.addVertex(vertexB);

        graph.addEdge(vertexA, vertexB);
        graph.addEdge(vertexB, vertexC);

        assertEquals(2, graph.edgeSet().size());
        assertEquals(2, graph.vertexSet().size());

        CycleDetector<String, DefaultEdge> cycleDetector = new CycleDetector<>(graph);
        Set<String> cycleVertices = cycleDetector.findCycles();

        boolean foundCycle = cycleDetector.detectCyclesContainingVertex(vertexA);
        boolean foundVertex = graph.containsVertex(vertexA);

        Set<String> subCycle = cycleDetector.findCyclesContainingVertex(vertexA);

        assertEquals(2, cycleVertices.size());
        assertEquals(2, subCycle.size()); // fails with zero items
        assertTrue(foundCycle); // fails with no cycle found which includes
                                // vertexA
        assertTrue(foundVertex);
    }
}
