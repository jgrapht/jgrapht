/*
 * (C) Copyright 2003-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.graph;

import org.jgrapht.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link AsGraphUnion} class.
 *
 * @author Joris Kinable
 */
public class AsGraphUnionTest
{

    // ~ Instance fields --------------------------------------------------------

    private String v0 = "v0";
    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";
    private String v4 = "v4";

    private DefaultWeightedEdge e1 = new DefaultWeightedEdge(); // (v0,v1);
    private DefaultWeightedEdge e2 = new DefaultWeightedEdge(); // (v1,v4);
    private DefaultWeightedEdge e3 = new DefaultWeightedEdge(); // (v4,v0);
    private DefaultWeightedEdge e4 = new DefaultWeightedEdge(); // (v1,v2);
    private DefaultWeightedEdge e5 = new DefaultWeightedEdge(); // (v2,v3);
    private DefaultWeightedEdge e6 = new DefaultWeightedEdge(); // (v3,v4);
    private DefaultWeightedEdge e7 = new DefaultWeightedEdge(); // (v4,v1);
    private DefaultWeightedEdge e8 = new DefaultWeightedEdge(); // (v4,v4);

    Graph<String, DefaultWeightedEdge> undirectedGraph1;
    Graph<String, DefaultWeightedEdge> undirectedGraph2;

    Graph<String, DefaultWeightedEdge> directedGraph1;
    Graph<String, DefaultWeightedEdge> directedGraph2;

    // ~ Methods ----------------------------------------------------------------

    @Before
    public void setUp()
    {
        undirectedGraph1 = new WeightedPseudograph<>(DefaultWeightedEdge.class);
        undirectedGraph2 = new WeightedPseudograph<>(DefaultWeightedEdge.class);
        directedGraph1 = new DirectedPseudograph<>(DefaultWeightedEdge.class);
        directedGraph2 = new DirectedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addAllVertices(undirectedGraph1, Arrays.asList(v0, v1, v4));
        Graphs.addAllVertices(undirectedGraph2, Arrays.asList(v1, v2, v3, v4));
        Graphs.addAllVertices(directedGraph1, Arrays.asList(v0, v1, v4));
        Graphs.addAllVertices(directedGraph2, Arrays.asList(v1, v2, v3, v4));

        undirectedGraph1.addEdge(v0, v1, e1);
        undirectedGraph1.addEdge(v1, v4, e2);
        undirectedGraph1.addEdge(v4, v0, e3);
        undirectedGraph1.addEdge(v4, v4, e8);

        directedGraph1.addEdge(v0, v1, e1);
        directedGraph1.addEdge(v1, v4, e2);
        directedGraph1.addEdge(v4, v0, e3);
        directedGraph1.addEdge(v4, v4, e8);

        undirectedGraph2.addEdge(v4, v1, e7);
        undirectedGraph2.addEdge(v1, v2, e4);
        undirectedGraph2.addEdge(v2, v3, e5);
        undirectedGraph2.addEdge(v3, v4, e6);

        directedGraph2.addEdge(v4, v1, e7);
        directedGraph2.addEdge(v1, v2, e4);
        directedGraph2.addEdge(v2, v3, e5);
        directedGraph2.addEdge(v3, v4, e6);
    }

    /**
     * Create and test the union of two Undirected Graphs
     */
    @Test
    public void testUndirectedGraphUnion()
    {
        Graph<String, DefaultWeightedEdge> graphUnion =
            new AsGraphUnion<>(undirectedGraph1, undirectedGraph2);

        assertTrue(graphUnion.getType().isUndirected());
        assertTrue(graphUnion.getType().isWeighted());
        assertFalse(graphUnion.getType().isModifiable());

        assertEquals(new HashSet<>(Arrays.asList(v0, v1, v2, v3, v4)), graphUnion.vertexSet());
        assertEquals(
            new HashSet<>(Arrays.asList(e1, e2, e3, e4, e5, e6, e7, e8)), graphUnion.edgeSet());

        assertEquals(new HashSet<>(Arrays.asList(e1, e3)), graphUnion.edgesOf(v0));
        assertEquals(new HashSet<>(Arrays.asList(e1, e2, e4, e7)), graphUnion.edgesOf(v1));
        assertEquals(new HashSet<>(Arrays.asList(e4, e5)), graphUnion.edgesOf(v2));
        assertEquals(new HashSet<>(Arrays.asList(e5, e6)), graphUnion.edgesOf(v3));
        assertEquals(new HashSet<>(Arrays.asList(e2, e3, e6, e7, e8)), graphUnion.edgesOf(v4));

        assertEquals(2, graphUnion.degreeOf(v0));
        assertEquals(4, graphUnion.degreeOf(v1));
        assertEquals(2, graphUnion.degreeOf(v2));
        assertEquals(2, graphUnion.degreeOf(v3));
        assertEquals(6, graphUnion.degreeOf(v4));

        assertEquals(new HashSet<>(Arrays.asList(e1, e3)), graphUnion.incomingEdgesOf(v0));
        assertEquals(new HashSet<>(Arrays.asList(e1, e2, e4, e7)), graphUnion.incomingEdgesOf(v1));
        assertEquals(new HashSet<>(Arrays.asList(e4, e5)), graphUnion.incomingEdgesOf(v2));
        assertEquals(new HashSet<>(Arrays.asList(e5, e6)), graphUnion.incomingEdgesOf(v3));
        assertEquals(
            new HashSet<>(Arrays.asList(e2, e3, e6, e7, e8)), graphUnion.incomingEdgesOf(v4));

        assertEquals(2, graphUnion.inDegreeOf(v0));
        assertEquals(4, graphUnion.inDegreeOf(v1));
        assertEquals(2, graphUnion.inDegreeOf(v2));
        assertEquals(2, graphUnion.inDegreeOf(v3));
        assertEquals(6, graphUnion.inDegreeOf(v4));

        assertEquals(new HashSet<>(Arrays.asList(e1, e3)), graphUnion.outgoingEdgesOf(v0));
        assertEquals(new HashSet<>(Arrays.asList(e1, e2, e4, e7)), graphUnion.outgoingEdgesOf(v1));
        assertEquals(new HashSet<>(Arrays.asList(e4, e5)), graphUnion.outgoingEdgesOf(v2));
        assertEquals(new HashSet<>(Arrays.asList(e5, e6)), graphUnion.outgoingEdgesOf(v3));
        assertEquals(
            new HashSet<>(Arrays.asList(e2, e3, e6, e7, e8)), graphUnion.outgoingEdgesOf(v4));

        assertEquals(2, graphUnion.outDegreeOf(v0));
        assertEquals(4, graphUnion.outDegreeOf(v1));
        assertEquals(2, graphUnion.outDegreeOf(v2));
        assertEquals(2, graphUnion.outDegreeOf(v3));
        assertEquals(6, graphUnion.outDegreeOf(v4));

        assertTrue(graphUnion.getEdge(v1, v4) == e2);
        assertTrue(graphUnion.getEdge(v4, v1) == e2);
    }

    /**
     * Create and test the union of two Directed Graphs
     */
    @Test
    public void testDirectedGraphUnion()
    {
        Graph<String, DefaultWeightedEdge> graphUnion =
            new AsGraphUnion<>(directedGraph1, directedGraph2);

        assertTrue(graphUnion.getType().isDirected());
        assertTrue(graphUnion.getType().isWeighted());
        assertFalse(graphUnion.getType().isModifiable());

        assertEquals(new HashSet<>(Arrays.asList(v0, v1, v2, v3, v4)), graphUnion.vertexSet());
        assertEquals(
            new HashSet<>(Arrays.asList(e1, e2, e3, e4, e5, e6, e7, e8)), graphUnion.edgeSet());

        assertEquals(new HashSet<>(Arrays.asList(e1, e3)), graphUnion.edgesOf(v0));
        assertEquals(new HashSet<>(Arrays.asList(e1, e2, e4, e7)), graphUnion.edgesOf(v1));
        assertEquals(new HashSet<>(Arrays.asList(e4, e5)), graphUnion.edgesOf(v2));
        assertEquals(new HashSet<>(Arrays.asList(e5, e6)), graphUnion.edgesOf(v3));
        assertEquals(new HashSet<>(Arrays.asList(e2, e3, e6, e7, e8)), graphUnion.edgesOf(v4));

        assertEquals(2, graphUnion.degreeOf(v0));
        assertEquals(4, graphUnion.degreeOf(v1));
        assertEquals(2, graphUnion.degreeOf(v2));
        assertEquals(2, graphUnion.degreeOf(v3));
        assertEquals(6, graphUnion.degreeOf(v4));

        assertEquals(new HashSet<>(Arrays.asList(e3)), graphUnion.incomingEdgesOf(v0));
        assertEquals(new HashSet<>(Arrays.asList(e1, e7)), graphUnion.incomingEdgesOf(v1));
        assertEquals(new HashSet<>(Arrays.asList(e4)), graphUnion.incomingEdgesOf(v2));
        assertEquals(new HashSet<>(Arrays.asList(e5)), graphUnion.incomingEdgesOf(v3));
        assertEquals(new HashSet<>(Arrays.asList(e2, e6, e8)), graphUnion.incomingEdgesOf(v4));

        assertEquals(1, graphUnion.inDegreeOf(v0));
        assertEquals(2, graphUnion.inDegreeOf(v1));
        assertEquals(1, graphUnion.inDegreeOf(v2));
        assertEquals(1, graphUnion.inDegreeOf(v3));
        assertEquals(3, graphUnion.inDegreeOf(v4));

        assertEquals(new HashSet<>(Arrays.asList(e1)), graphUnion.outgoingEdgesOf(v0));
        assertEquals(new HashSet<>(Arrays.asList(e2, e4)), graphUnion.outgoingEdgesOf(v1));
        assertEquals(new HashSet<>(Arrays.asList(e5)), graphUnion.outgoingEdgesOf(v2));
        assertEquals(new HashSet<>(Arrays.asList(e6)), graphUnion.outgoingEdgesOf(v3));
        assertEquals(new HashSet<>(Arrays.asList(e3, e7, e8)), graphUnion.outgoingEdgesOf(v4));

        assertEquals(1, graphUnion.outDegreeOf(v0));
        assertEquals(2, graphUnion.outDegreeOf(v1));
        assertEquals(1, graphUnion.outDegreeOf(v2));
        assertEquals(1, graphUnion.outDegreeOf(v3));
        assertEquals(3, graphUnion.outDegreeOf(v4));

        assertFalse(directedGraph1.containsEdge(v4, v1));
        assertFalse(directedGraph2.containsEdge(v1, v4));
        assertTrue(graphUnion.getEdge(v1, v4) == e2);
        assertTrue(graphUnion.getEdge(v4, v1) == e7);
    }

    /**
     * Create and test a Mixed-Graph, obtained by taking the union of a undirected and a directed
     * graph
     */
    @Test
    public void testMixedGraphUnion()
    {
        Graph<String, DefaultWeightedEdge> graphUnion =
            new AsGraphUnion<>(undirectedGraph1, directedGraph2);

        assertTrue(graphUnion.getType().isMixed());
        assertTrue(graphUnion.getType().isWeighted());
        assertFalse(graphUnion.getType().isModifiable());

        assertEquals(new HashSet<>(Arrays.asList(v0, v1, v2, v3, v4)), graphUnion.vertexSet());
        assertEquals(
            new HashSet<>(Arrays.asList(e1, e2, e3, e4, e5, e6, e7, e8)), graphUnion.edgeSet());

        assertEquals(new HashSet<>(Arrays.asList(e1, e3)), graphUnion.edgesOf(v0));
        assertEquals(new HashSet<>(Arrays.asList(e1, e2, e4, e7)), graphUnion.edgesOf(v1));
        assertEquals(new HashSet<>(Arrays.asList(e4, e5)), graphUnion.edgesOf(v2));
        assertEquals(new HashSet<>(Arrays.asList(e5, e6)), graphUnion.edgesOf(v3));
        assertEquals(new HashSet<>(Arrays.asList(e2, e3, e6, e7, e8)), graphUnion.edgesOf(v4));

        assertEquals(2, graphUnion.degreeOf(v0));
        assertEquals(4, graphUnion.degreeOf(v1));
        assertEquals(2, graphUnion.degreeOf(v2));
        assertEquals(2, graphUnion.degreeOf(v3));
        assertEquals(6, graphUnion.degreeOf(v4));

        assertEquals(new HashSet<>(Arrays.asList(e1, e3)), graphUnion.incomingEdgesOf(v0));
        assertEquals(new HashSet<>(Arrays.asList(e1, e2, e7)), graphUnion.incomingEdgesOf(v1));
        assertEquals(new HashSet<>(Arrays.asList(e4)), graphUnion.incomingEdgesOf(v2));
        assertEquals(new HashSet<>(Arrays.asList(e5)), graphUnion.incomingEdgesOf(v3));
        assertEquals(new HashSet<>(Arrays.asList(e2, e3, e6, e8)), graphUnion.incomingEdgesOf(v4));

        assertEquals(2, graphUnion.inDegreeOf(v0));
        assertEquals(3, graphUnion.inDegreeOf(v1));
        assertEquals(1, graphUnion.inDegreeOf(v2));
        assertEquals(1, graphUnion.inDegreeOf(v3));
        assertEquals(5, graphUnion.inDegreeOf(v4));

        assertEquals(new HashSet<>(Arrays.asList(e1, e3)), graphUnion.outgoingEdgesOf(v0));
        assertEquals(new HashSet<>(Arrays.asList(e1, e2, e4)), graphUnion.outgoingEdgesOf(v1));
        assertEquals(new HashSet<>(Arrays.asList(e5)), graphUnion.outgoingEdgesOf(v2));
        assertEquals(new HashSet<>(Arrays.asList(e6)), graphUnion.outgoingEdgesOf(v3));
        assertEquals(new HashSet<>(Arrays.asList(e2, e3, e7, e8)), graphUnion.outgoingEdgesOf(v4));

        assertEquals(2, graphUnion.outDegreeOf(v0));
        assertEquals(3, graphUnion.outDegreeOf(v1));
        assertEquals(1, graphUnion.outDegreeOf(v2));
        assertEquals(1, graphUnion.outDegreeOf(v3));
        assertEquals(5, graphUnion.outDegreeOf(v4));

        assertTrue(graphUnion.containsEdge(v0, v1)); // undirected edge
        assertTrue(graphUnion.containsEdge(v1, v0)); // undirected edge
        assertTrue(graphUnion.containsEdge(v3, v4)); // directed edge
        assertFalse(graphUnion.containsEdge(v4, v3)); // directed edge
    }

    /**
     * Test the weight combiner for graphs having an edge in common.
     */
    @Test
    public void testWeightCombiner()
    {
        // Create two graphs, both having the same vertices {0,1} and the same weighted edge (0,1)
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> g1 =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g1, Arrays.asList(0, 1));
        DefaultWeightedEdge edge = g1.addEdge(0, 1);
        g1.setEdgeWeight(edge, 10);

        SimpleWeightedGraph<Integer, DefaultWeightedEdge> g2 =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g2, Arrays.asList(0, 1));
        g2.addEdge(0, 1, edge);
        // We need to create a mask of the second graph if we want to store the edge with a
        // different weight. Simply setting g2.setEdgeWeight(edge,20) would override the edge weight
        // for the same edge in g1 as well!
        Map<DefaultWeightedEdge, Double> weightMap = new HashMap<>();
        weightMap.put(edge, 20.0);
        Graph<Integer, DefaultWeightedEdge> g2Masked = new AsWeightedGraph<>(g2, weightMap);

        Graph<Integer, DefaultWeightedEdge> graphUnionSum =
            new AsGraphUnion<>(g1, g2Masked, WeightCombiner.SUM);
        assertEquals(30.0, graphUnionSum.getEdgeWeight(edge), 0);
        Graph<Integer, DefaultWeightedEdge> graphUnionFirst =
            new AsGraphUnion<>(g1, g2Masked, WeightCombiner.FIRST);
        assertEquals(10.0, graphUnionFirst.getEdgeWeight(edge), 0);
        Graph<Integer, DefaultWeightedEdge> graphUnionSecond =
            new AsGraphUnion<>(g1, g2Masked, WeightCombiner.SECOND);
        assertEquals(20.0, graphUnionSecond.getEdgeWeight(edge), 0);
        Graph<Integer, DefaultWeightedEdge> graphUnionMax =
            new AsGraphUnion<>(g1, g2Masked, WeightCombiner.MAX);
        assertEquals(20.0, graphUnionMax.getEdgeWeight(edge), 0);
        Graph<Integer, DefaultWeightedEdge> graphUnionMin =
            new AsGraphUnion<>(g1, g2Masked, WeightCombiner.MIN);
        assertEquals(10.0, graphUnionMin.getEdgeWeight(edge), 0);
        Graph<Integer, DefaultWeightedEdge> graphUnionMult =
            new AsGraphUnion<>(g1, g2Masked, WeightCombiner.MULT);
        assertEquals(200.0, graphUnionMult.getEdgeWeight(edge), 0);

        assertEquals(10.0, g1.getEdgeWeight(edge), 0);
        assertEquals(10.0, g2.getEdgeWeight(edge), 0);
        assertEquals(20.0, g2Masked.getEdgeWeight(edge), 0);
    }

}
