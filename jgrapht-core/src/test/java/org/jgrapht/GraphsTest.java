/*
 * (C) Copyright 2003-2018, by Christoph Zauner and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht;

import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

/**
 * @author Christoph Zauner
 */
public class GraphsTest
{

    //@formatter:off
    /**
     * Graph before removing X:
     *
     *             +--> C
     *             |
     * A +--> B +--+
     *             |
     *             +--> D
     *
     * Expected graph after removing X:
     *
     *             +--> C
     *             |
     * A +--> B +--+
     *             |
     *             +--> D
     */
    //@formatter:on
    @Test
    public void removeVertex_vertexNotFound()
    {

        Graph<String, TestEdge> graph = new DefaultDirectedGraph<>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);

        graph.addEdge(a, b);
        graph.addEdge(b, c);
        graph.addEdge(b, d);

        Graph<String, TestEdge> expectedGraph = new DefaultDirectedGraph<>(TestEdge.class);

        expectedGraph.addVertex(a);
        expectedGraph.addVertex(b);
        expectedGraph.addVertex(c);
        expectedGraph.addVertex(d);

        expectedGraph.addEdge(a, b);
        expectedGraph.addEdge(b, c);
        expectedGraph.addEdge(b, d);

        boolean vertexHasBeenRemoved = Graphs.removeVertexAndPreserveConnectivity(graph, x);

        Assert.assertEquals(expectedGraph, graph);
        Assert.assertFalse(vertexHasBeenRemoved);
    }

    //@formatter:off
    /**
     * Graph before removing B:
     *
     *             +--> C
     *             |
     * A +--> B +--+
     *             |
     *             +--> D
     *
     * Graph after removing B:
     *
     *      +--> C
     *      |
     * A +--+
     *      |
     *      +--> D
     */
    //@formatter:on
    @Test
    public void removeVertex00()
    {
        Graph<String, TestEdge> graph = new DefaultDirectedGraph<>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);

        graph.addEdge(a, b);
        graph.addEdge(b, c);
        graph.addEdge(b, d);

        Graph<String, TestEdge> expectedGraph = new DefaultDirectedGraph<>(TestEdge.class);

        expectedGraph.addVertex(a);
        expectedGraph.addVertex(c);
        expectedGraph.addVertex(d);

        expectedGraph.addEdge(a, c);
        expectedGraph.addEdge(a, d);

        boolean vertexHasBeenRemoved = Graphs.removeVertexAndPreserveConnectivity(graph, b);

        Assert.assertEquals(expectedGraph, graph);
        Assert.assertTrue(vertexHasBeenRemoved);
    }

    //@formatter:off
    /**
     * Graph before removing A:
     *
     * A +--> B
     *
     * Expected graph after removing A:
     *
     * B
     */
    //@formatter:on
    @Test
    public void removeVertex01()
    {
        Graph<String, TestEdge> graph = new DefaultDirectedGraph<>(TestEdge.class);

        String a = "A";
        String b = "B";

        graph.addVertex(a);
        graph.addVertex(b);

        graph.addEdge(a, b);

        Graph<String, TestEdge> expectedGraph = new DefaultDirectedGraph<>(TestEdge.class);

        expectedGraph.addVertex(b);

        boolean vertexHasBeenRemoved = Graphs.removeVertexAndPreserveConnectivity(graph, a);

        Assert.assertEquals(expectedGraph, graph);
        Assert.assertTrue(vertexHasBeenRemoved);
    }

    //@formatter:off
    /**
     * Graph before removing B:
     *
     * A +--> B
     *
     * Expected graph after removing B:
     *
     * A
     */
    //@formatter:on
    @Test
    public void removeVertex02()
    {
        Graph<String, TestEdge> graph = new DefaultDirectedGraph<>(TestEdge.class);

        String a = "A";
        String b = "B";

        graph.addVertex(a);
        graph.addVertex(b);

        graph.addEdge(a, b);

        Graph<String, TestEdge> expectedGraph = new DefaultDirectedGraph<>(TestEdge.class);

        expectedGraph.addVertex(a);

        boolean vertexHasBeenRemoved = Graphs.removeVertexAndPreserveConnectivity(graph, b);

        Assert.assertEquals(expectedGraph, graph);
        Assert.assertTrue(vertexHasBeenRemoved);
    }

    //@formatter:off
    /**
     * Input:
     *
     * A (source, not part of graph)
     * B (target, already part of graph)
     * C (target, not part of graph)
     *
     * Expected output:
     *
     *      +--> B
     *      |
     * A +--+
     *      |
     *      +--> C
     */
    //@formatter:on
    @Test
    public void addOutgoingEdges()
    {
        DefaultDirectedGraph<String, TestEdge> graph = new DefaultDirectedGraph<>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";

        graph.addVertex(b);

        Graph<String, TestEdge> expectedGraph = new DefaultDirectedGraph<>(TestEdge.class);

        expectedGraph.addVertex(a);
        expectedGraph.addVertex(b);
        expectedGraph.addVertex(c);

        expectedGraph.addEdge(a, b);
        expectedGraph.addEdge(a, c);

        List<String> targets = new ArrayList<String>();
        targets.add(b);
        targets.add(c);

        Graphs.addOutgoingEdges(graph, a, targets);

        Assert.assertEquals(expectedGraph, graph);
    }

    //@formatter:off
    /**
     * Input:
     *
     * A (target, not part of graph)
     * B (source, already part of graph)
     * C (source, not part of graph)
     *
     * Expected output:
     *
     *      +--+ B
     *      |
     * A <--+
     *      |
     *      +--+ C
     */
    //@formatter:on
    @Test
    public void addIncomingEdges()
    {
        DefaultDirectedGraph<String, TestEdge> graph = new DefaultDirectedGraph<>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";

        graph.addVertex(b);

        Graph<String, TestEdge> expectedGraph = new DefaultDirectedGraph<>(TestEdge.class);

        expectedGraph.addVertex(a);
        expectedGraph.addVertex(b);
        expectedGraph.addVertex(c);

        expectedGraph.addEdge(b, a);
        expectedGraph.addEdge(c, a);

        List<String> targets = new ArrayList<String>();
        targets.add(b);
        targets.add(c);

        Graphs.addIncomingEdges(graph, a, targets);

        Assert.assertEquals(expectedGraph, graph);
    }

    //@formatter:off
    /**
     * Input:
     *
     *             +--> C
     *             |
     * A +--> B +--+
     *             |
     *             +--> D
     */
    //@formatter:on
    @Test
    public void vertexHasChildren_B()
    {
        DefaultDirectedGraph<String, TestEdge> graph = new DefaultDirectedGraph<>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);

        graph.addEdge(a, b);
        graph.addEdge(b, c);
        graph.addEdge(b, d);

        Assert.assertTrue(Graphs.vertexHasSuccessors(graph, b));
    }

    //@formatter:off
    /**
     * Input:
     *
     *             +--> C
     *             |
     * A +--> B +--+
     *             |
     *             +--> D
     */
    //@formatter:on
    @Test
    public void vertexHasChildren_C()
    {
        DefaultDirectedGraph<String, TestEdge> graph = new DefaultDirectedGraph<>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);

        graph.addEdge(a, b);
        graph.addEdge(b, c);
        graph.addEdge(b, d);

        Assert.assertFalse(Graphs.vertexHasSuccessors(graph, c));
    }

    //@formatter:off
    /**
     * Input:
     *
     *             +--> C
     *             |
     * A +--> B +--+
     *             |
     *             +--> D
     */
    //@formatter:on
    @Test
    public void vertexHasParents_B()
    {
        DefaultDirectedGraph<String, TestEdge> graph = new DefaultDirectedGraph<>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);

        graph.addEdge(a, b);
        graph.addEdge(b, c);
        graph.addEdge(b, d);

        Assert.assertTrue(Graphs.vertexHasPredecessors(graph, b));
    }

    //@formatter:off
    /**
     * Input:
     *
     *             +--> C
     *             |
     * A +--> B +--+
     *             |
     *             +--> D
     */
    //@formatter:on
    @Test
    public void vertexHasParents_A()
    {
        DefaultDirectedGraph<String, TestEdge> graph = new DefaultDirectedGraph<>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);

        graph.addEdge(a, b);
        graph.addEdge(b, c);
        graph.addEdge(b, d);

        Assert.assertFalse(Graphs.vertexHasPredecessors(graph, a));
    }

    @Test
    public void testNeighborSetOf()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(1, 4);
        Set<Integer> neighborSet = Graphs.neighborSetOf(graph, 1);
        Assert.assertEquals(new HashSet<>(Arrays.asList(2, 4)), neighborSet);
    }

}

// End GraphsTest.java
