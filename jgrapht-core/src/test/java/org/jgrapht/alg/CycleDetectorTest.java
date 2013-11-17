/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* ------------------------------
 * CycleDetectorTest.java
 * ------------------------------
 * (C) Copyright 2003-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   Khanh Vu
 *
 * $Id$
 *
 * Changes
 * -------
 * 16-Sept-2004 : Initial revision (JVS);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * .
 *
 * @author John V. Sichi
 */
public class CycleDetectorTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";
    private static final String V4 = "v4";
    private static final String V5 = "v5";
    private static final String V6 = "v6";
    private static final String V7 = "v7";

    //~ Methods ----------------------------------------------------------------

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
    public void testDirectedWithCycle()
    {
        DirectedGraph<String, DefaultEdge> g =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        createGraph(g);

        Set<String> cyclicSet = new HashSet<String>();
        cyclicSet.add(V1);
        cyclicSet.add(V2);
        cyclicSet.add(V3);
        cyclicSet.add(V4);

        Set<String> acyclicSet = new HashSet<String>();
        acyclicSet.add(V5);
        acyclicSet.add(V6);
        acyclicSet.add(V7);

        runTest(g, cyclicSet, acyclicSet);
    }

    /**
     * .
     */
    public void testDirectedWithDoubledCycle()
    {
        DirectedGraph<String, DefaultEdge> g =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);

        // build the graph:  vertex order is chosen specifically
        // to exercise old bug-cases in CycleDetector
        g.addVertex(V2);
        g.addVertex(V1);
        g.addVertex(V3);

        g.addEdge(V1, V2);
        g.addEdge(V2, V3);
        g.addEdge(V3, V1);
        g.addEdge(V2, V1);

        Set<String> cyclicSet = new HashSet<String>();
        cyclicSet.add(V1);
        cyclicSet.add(V2);
        cyclicSet.add(V3);

        Set<String> acyclicSet = new HashSet<String>();

        runTest(g, cyclicSet, acyclicSet);
    }

    /**
     * .
     */
    @SuppressWarnings("unchecked")
    public void testDirectedWithoutCycle()
    {
        DirectedGraph<String, DefaultEdge> g =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        createGraph(g);
        g.removeVertex(V2);

        Set<String> cyclicSet = Collections.EMPTY_SET; // hb: I would like
                                                       // EMPTY_SET to be typed
                                                       // as well...
        Set<String> acyclicSet = g.vertexSet();

        runTest(g, cyclicSet, acyclicSet);
    }

    private void runTest(
        DirectedGraph<String, DefaultEdge> g,
        Set<String> cyclicSet,
        Set<String> acyclicSet)
    {
        CycleDetector<String, DefaultEdge> detector =
            new CycleDetector<String, DefaultEdge>(g);

        Set emptySet = Collections.EMPTY_SET;

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

    public void testVertexEquals()
    {
        DefaultDirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        assertEquals(0, graph.edgeSet().size());

        String vertexA = "A";
        String vertexB = "B";
        String vertexC = new StringBuffer("A").toString();

        assertNotSame(vertexA, vertexC);

        graph.addVertex(vertexA);
        graph.addVertex(vertexB);

        graph.addEdge(vertexA, vertexB);
        graph.addEdge(vertexB, vertexC);

        assertEquals(2, graph.edgeSet().size());
        assertEquals(2, graph.vertexSet().size());

        CycleDetector<String, DefaultEdge> cycleDetector =
            new CycleDetector<String, DefaultEdge>(graph);
        Set<String> cycleVertices = cycleDetector.findCycles();

        boolean foundCycle =
            cycleDetector.detectCyclesContainingVertex(vertexA);
        boolean foundVertex = graph.containsVertex(vertexA);

        Set<String> subCycle =
            cycleDetector.findCyclesContainingVertex(vertexA);

        assertEquals(2, cycleVertices.size());
        assertEquals(2, subCycle.size()); // fails with zero items
        assertTrue(foundCycle); // fails with no cycle found which includes
                                // vertexA
        assertTrue(foundVertex);
    }
}

// End CycleDetectorTest.java
