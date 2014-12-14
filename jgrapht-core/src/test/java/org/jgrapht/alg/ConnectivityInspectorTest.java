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
 * ConnectivityInspectorTest.java
 * ------------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 07-Aug-2003 : Initial revision (BN);
 * 20-Apr-2005 : Added StrongConnectivityInspector test (JVS);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;


/**
 * .
 *
 * @author Barak Naveh
 */
public class ConnectivityInspectorTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";
    private static final String V4 = "v4";

    //~ Instance fields --------------------------------------------------------

    //
    DefaultEdge e1;
    DefaultEdge e2;
    DefaultEdge e3;
    DefaultEdge e3_b;
    DefaultEdge u;

    //~ Methods ----------------------------------------------------------------

    /**
     * .
     *
     * @return a graph
     */
    public Pseudograph<String, DefaultEdge> create()
    {
        Pseudograph<String, DefaultEdge> g =
            new Pseudograph<String, DefaultEdge>(DefaultEdge.class);

        assertEquals(0, g.vertexSet().size());
        g.addVertex(V1);
        assertEquals(1, g.vertexSet().size());
        g.addVertex(V2);
        assertEquals(2, g.vertexSet().size());
        g.addVertex(V3);
        assertEquals(3, g.vertexSet().size());
        g.addVertex(V4);
        assertEquals(4, g.vertexSet().size());

        assertEquals(0, g.edgeSet().size());

        e1 = g.addEdge(V1, V2);
        assertEquals(1, g.edgeSet().size());

        e2 = g.addEdge(V2, V3);
        assertEquals(2, g.edgeSet().size());

        e3 = g.addEdge(V3, V1);
        assertEquals(3, g.edgeSet().size());

        e3_b = g.addEdge(V3, V1);
        assertEquals(4, g.edgeSet().size());
        assertNotNull(e3_b);

        u = g.addEdge(V1, V1);
        assertEquals(5, g.edgeSet().size());
        u = g.addEdge(V1, V1);
        assertEquals(6, g.edgeSet().size());

        return g;
    }

    /**
     * .
     */
    public void testDirectedGraph()
    {
        ListenableDirectedGraph<String, DefaultEdge> g =
            new ListenableDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);

        g.addEdge(V1, V2);

        ConnectivityInspector<String, DefaultEdge> inspector =
            new ConnectivityInspector<String, DefaultEdge>(g);
        g.addGraphListener(inspector);

        assertEquals(false, inspector.isGraphConnected());

        g.addEdge(V1, V3);

        assertEquals(true, inspector.isGraphConnected());
    }

    /**
     * .
     */
    public void testIsGraphConnected()
    {
        Pseudograph<String, DefaultEdge> g = create();
        ConnectivityInspector<String, DefaultEdge> inspector =
            new ConnectivityInspector<String, DefaultEdge>(g);

        assertEquals(false, inspector.isGraphConnected());

        g.removeVertex(V4);
        inspector = new ConnectivityInspector<String, DefaultEdge>(g);
        assertEquals(true, inspector.isGraphConnected());

        g.removeVertex(V1);
        assertEquals(1, g.edgeSet().size());

        g.removeEdge(e2);
        g.addEdge(V2, V2);
        assertEquals(1, g.edgeSet().size());

        inspector = new ConnectivityInspector<String, DefaultEdge>(g);
        assertEquals(false, inspector.isGraphConnected());
    }

    /**
     * .
     */
    public void testStronglyConnected1()
    {
        DirectedGraph<String, DefaultEdge> g =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);

        g.addEdge(V1, V2);
        g.addEdge(V2, V1); // strongly connected

        g.addEdge(V3, V4); // only weakly connected

        StrongConnectivityInspector<String, DefaultEdge> inspector =
            new StrongConnectivityInspector<String, DefaultEdge>(g);

        // convert from List to Set because we need to ignore order
        // during comparison
        Set<Set<String>> actualSets =
            new HashSet<Set<String>>(inspector.stronglyConnectedSets());

        // construct the expected answer
        Set<Set<String>> expectedSets = new HashSet<Set<String>>();
        Set<String> set = new HashSet<String>();
        set.add(V1);
        set.add(V2);
        expectedSets.add(set);
        set = new HashSet<String>();
        set.add(V3);
        expectedSets.add(set);
        set = new HashSet<String>();
        set.add(V4);
        expectedSets.add(set);

        assertEquals(expectedSets, actualSets);

        actualSets.clear();

        List<DirectedSubgraph<String, DefaultEdge>> subgraphs =
            inspector.stronglyConnectedSubgraphs();
        for (DirectedSubgraph<String, DefaultEdge> sg : subgraphs) {
            actualSets.add(sg.vertexSet());

            StrongConnectivityInspector<String, DefaultEdge> ci =
                new StrongConnectivityInspector<String, DefaultEdge>(sg);
            assertTrue(ci.isStronglyConnected());
        }

        assertEquals(expectedSets, actualSets);
    }

    /**
     * .
     */
    public void testStronglyConnected2()
    {
        DirectedGraph<String, DefaultEdge> g =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);

        g.addEdge(V1, V2);
        g.addEdge(V2, V1); // strongly connected

        g.addEdge(V4, V3); // only weakly connected
        g.addEdge(V3, V2); // only weakly connected

        StrongConnectivityInspector<String, DefaultEdge> inspector =
            new StrongConnectivityInspector<String, DefaultEdge>(g);

        // convert from List to Set because we need to ignore order
        // during comparison
        Set<Set<String>> actualSets =
            new HashSet<Set<String>>(inspector.stronglyConnectedSets());

        // construct the expected answer
        Set<Set<String>> expectedSets = new HashSet<Set<String>>();
        Set<String> set = new HashSet<String>();
        set.add(V1);
        set.add(V2);
        expectedSets.add(set);
        set = new HashSet<String>();
        set.add(V3);
        expectedSets.add(set);
        set = new HashSet<String>();
        set.add(V4);
        expectedSets.add(set);

        assertEquals(expectedSets, actualSets);

        actualSets.clear();

        List<DirectedSubgraph<String, DefaultEdge>> subgraphs =
            inspector.stronglyConnectedSubgraphs();
        for (DirectedSubgraph<String, DefaultEdge> sg : subgraphs) {
            actualSets.add(sg.vertexSet());

            StrongConnectivityInspector<String, DefaultEdge> ci =
                new StrongConnectivityInspector<String, DefaultEdge>(sg);
            assertTrue(ci.isStronglyConnected());
        }

        assertEquals(expectedSets, actualSets);
    }

    /**
     * .
     */
    public void testStronglyConnected3()
    {
        DirectedGraph<String, DefaultEdge> g =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
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

        StrongConnectivityInspector<String, DefaultEdge> inspector =
            new StrongConnectivityInspector<String, DefaultEdge>(g);

        // convert from List to Set because we need to ignore order
        // during comparison
        Set<Set<String>> actualSets =
            new HashSet<Set<String>>(inspector.stronglyConnectedSets());

        // construct the expected answer
        Set<Set<String>> expectedSets = new HashSet<Set<String>>();
        Set<String> set = new HashSet<String>();
        set.add(V1);
        set.add(V2);
        set.add(V3);
        expectedSets.add(set);
        set = new HashSet<String>();
        set.add(V4);
        expectedSets.add(set);

        assertEquals(expectedSets, actualSets);

        actualSets.clear();

        List<DirectedSubgraph<String, DefaultEdge>> subgraphs =
            inspector.stronglyConnectedSubgraphs();

        for (DirectedSubgraph<String, DefaultEdge> sg : subgraphs) {
            actualSets.add(sg.vertexSet());

            StrongConnectivityInspector<String, DefaultEdge> ci =
                new StrongConnectivityInspector<String, DefaultEdge>(sg);
            assertTrue(ci.isStronglyConnected());
        }

        assertEquals(expectedSets, actualSets);
    }

    public void testStronglyConnected4()
    {
        DefaultDirectedGraph<Integer, String> graph =
            new DefaultDirectedGraph<Integer, String>(
                new EdgeFactory<Integer, String>() {
                    @Override
                    public String createEdge(Integer from, Integer to)
                    {
                        return (from + "->" + to).intern();
                    }
                });

        new RingGraphGenerator<Integer, String>(3).generateGraph(
            graph,
            new VertexFactory<Integer>() {
                private int i = 0;

                @Override
                public Integer createVertex()
                {
                    return i++;
                }
            },
            null);

        StrongConnectivityInspector<Integer, String> sc =
            new StrongConnectivityInspector<Integer, String>(
                graph);
        Set<Set<Integer>> expected = new HashSet<Set<Integer>>();
        expected.add(graph.vertexSet());
        assertEquals(
            expected,
            new HashSet<Set<Integer>>(sc.stronglyConnectedSets()));
    }
}

// End ConnectivityInspectorTest.java
