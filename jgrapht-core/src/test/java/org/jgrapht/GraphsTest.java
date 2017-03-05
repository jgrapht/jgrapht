/*
 * (C) Copyright 2003-2017, by Christoph Zauner and Contributors.
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

import static org.junit.Assert.*;

import java.util.*;
import java.util.stream.*;

import org.jgrapht.graph.*;
import org.jgrapht.graph.specifics.*;
import org.junit.*;

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

        DirectedGraph<String, TestEdge> graph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

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

        DirectedGraph<String, TestEdge> expectedGraph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

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

        DirectedGraph<String, TestEdge> graph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

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

        DirectedGraph<String, TestEdge> expectedGraph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

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

        DirectedGraph<String, TestEdge> graph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

        String a = "A";
        String b = "B";

        graph.addVertex(a);
        graph.addVertex(b);

        graph.addEdge(a, b);

        DirectedGraph<String, TestEdge> expectedGraph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

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

        DirectedGraph<String, TestEdge> graph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

        String a = "A";
        String b = "B";

        graph.addVertex(a);
        graph.addVertex(b);

        graph.addEdge(a, b);

        DirectedGraph<String, TestEdge> expectedGraph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

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

        DefaultDirectedGraph<String, TestEdge> graph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";

        graph.addVertex(b);

        DirectedGraph<String, TestEdge> expectedGraph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

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

        DefaultDirectedGraph<String, TestEdge> graph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";

        graph.addVertex(b);

        DirectedGraph<String, TestEdge> expectedGraph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

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

        DefaultDirectedGraph<String, TestEdge> graph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

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

        DefaultDirectedGraph<String, TestEdge> graph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

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

        DefaultDirectedGraph<String, TestEdge> graph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

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

        DefaultDirectedGraph<String, TestEdge> graph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

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
    public void testEdgeAccessorOnDirectedGraph()
    {
        DirectedGraph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        g.addVertex("s");
        g.addVertex("v1");
        g.addVertex("v2");
        g.addVertex("v3");
        g.addVertex("v4");

        DefaultEdge e1 = g.addEdge("s", "v1");
        DefaultEdge e2 = g.addEdge("s", "v2");
        DefaultEdge e3 = g.addEdge("v3", "s");
        DefaultEdge e4 = g.addEdge("v4", "s");
        DefaultEdge e5 = g.addEdge("s", "s");

        IncidentEdgeAccessor<String, DefaultEdge> outAccessor = Graphs.outgoingEdgeAccessor(g);

        List<DefaultEdge> outEdges = StreamSupport
            .stream(outAccessor.edgesOf("s").spliterator(), false).collect(Collectors.toList());

        assertEquals(3, outEdges.size());
        assertTrue(outEdges.contains(e1));
        assertTrue(outEdges.contains(e2));
        assertTrue(outEdges.contains(e5));

        IncidentEdgeAccessor<String, DefaultEdge> inAccessor = Graphs.incomingEdgeAccessor(g);

        List<DefaultEdge> inEdges = StreamSupport
            .stream(inAccessor.edgesOf("s").spliterator(), false).collect(Collectors.toList());

        assertEquals(3, inEdges.size());
        assertTrue(inEdges.contains(e3));
        assertTrue(inEdges.contains(e4));
        assertTrue(inEdges.contains(e5));

        IncidentEdgeAccessor<String, DefaultEdge> inOutAccessor =
            Graphs.incomingOutgoingEdgeAccessor(g);

        List<DefaultEdge> inOutEdges = StreamSupport
            .stream(inOutAccessor.edgesOf("s").spliterator(), false).collect(Collectors.toList());

        assertEquals(5, inOutEdges.size());
        assertTrue(inOutEdges.contains(e1));
        assertTrue(inOutEdges.contains(e2));
        assertTrue(inOutEdges.contains(e3));
        assertTrue(inOutEdges.contains(e4));
        assertTrue(inOutEdges.contains(e5));
    }

    @Test
    public void testEdgeAccessorOnUndirectedGraph()
    {
        UndirectedGraph<String, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        g.addVertex("s");
        g.addVertex("v1");
        g.addVertex("v2");
        g.addVertex("v3");
        g.addVertex("v4");

        DefaultEdge e1 = g.addEdge("s", "v1");
        DefaultEdge e2 = g.addEdge("s", "v2");
        DefaultEdge e3 = g.addEdge("v3", "s");
        DefaultEdge e4 = g.addEdge("v4", "s");
        DefaultEdge e5 = g.addEdge("s", "s");

        IncidentEdgeAccessor<String, DefaultEdge> outAccessor = Graphs.outgoingEdgeAccessor(g);

        List<DefaultEdge> outEdges = StreamSupport
            .stream(outAccessor.edgesOf("s").spliterator(), false).collect(Collectors.toList());

        assertEquals(5, outEdges.size());
        assertTrue(outEdges.contains(e1));
        assertTrue(outEdges.contains(e2));
        assertTrue(outEdges.contains(e3));
        assertTrue(outEdges.contains(e4));
        assertTrue(outEdges.contains(e3));

        IncidentEdgeAccessor<String, DefaultEdge> inAccessor = Graphs.incomingEdgeAccessor(g);

        List<DefaultEdge> inEdges = StreamSupport
            .stream(inAccessor.edgesOf("s").spliterator(), false).collect(Collectors.toList());

        assertEquals(5, inEdges.size());
        assertTrue(inEdges.contains(e1));
        assertTrue(inEdges.contains(e2));
        assertTrue(inEdges.contains(e3));
        assertTrue(inEdges.contains(e4));
        assertTrue(inEdges.contains(e5));

        IncidentEdgeAccessor<String, DefaultEdge> inOutAccessor =
            Graphs.incomingOutgoingEdgeAccessor(g);

        List<DefaultEdge> inOutEdges = StreamSupport
            .stream(inOutAccessor.edgesOf("s").spliterator(), false).collect(Collectors.toList());

        assertEquals(5, inOutEdges.size());
        assertTrue(inOutEdges.contains(e1));
        assertTrue(inOutEdges.contains(e2));
        assertTrue(inOutEdges.contains(e3));
        assertTrue(inOutEdges.contains(e4));
        assertTrue(inOutEdges.contains(e5));
    }

    @Test
    public void testEdgeAccessorOnNoDirectedNorUndirectedGraph()
    {
        Graph<String, DefaultEdge> g = new TestGraph<>(DefaultEdge.class);
        g.addVertex("s");
        g.addVertex("v1");
        g.addVertex("v2");
        g.addVertex("v3");
        g.addVertex("v4");

        DefaultEdge e1 = g.addEdge("s", "v1");
        DefaultEdge e2 = g.addEdge("s", "v2");
        DefaultEdge e3 = g.addEdge("v3", "s");
        DefaultEdge e4 = g.addEdge("v4", "s");
        DefaultEdge e5 = g.addEdge("s", "s");

        IncidentEdgeAccessor<String, DefaultEdge> outAccessor = Graphs.outgoingEdgeAccessor(g);

        List<DefaultEdge> outEdges = StreamSupport
            .stream(outAccessor.edgesOf("s").spliterator(), false).collect(Collectors.toList());

        assertEquals(5, outEdges.size());
        assertTrue(outEdges.contains(e1));
        assertTrue(outEdges.contains(e2));
        assertTrue(outEdges.contains(e3));
        assertTrue(outEdges.contains(e4));
        assertTrue(outEdges.contains(e5));

        IncidentEdgeAccessor<String, DefaultEdge> inAccessor = Graphs.incomingEdgeAccessor(g);

        List<DefaultEdge> inEdges = StreamSupport
            .stream(inAccessor.edgesOf("s").spliterator(), false).collect(Collectors.toList());

        assertEquals(5, inEdges.size());
        assertTrue(inEdges.contains(e1));
        assertTrue(inEdges.contains(e2));
        assertTrue(inEdges.contains(e3));
        assertTrue(inEdges.contains(e4));
        assertTrue(inEdges.contains(e5));

        IncidentEdgeAccessor<String, DefaultEdge> inOutAccessor =
            Graphs.incomingOutgoingEdgeAccessor(g);

        List<DefaultEdge> inOutEdges = StreamSupport
            .stream(inOutAccessor.edgesOf("s").spliterator(), false).collect(Collectors.toList());

        assertEquals(5, inOutEdges.size());
        assertTrue(inOutEdges.contains(e1));
        assertTrue(inOutEdges.contains(e2));
        assertTrue(inOutEdges.contains(e3));
        assertTrue(inOutEdges.contains(e4));
        assertTrue(inOutEdges.contains(e5));
    }

    /**
     * A graph which is neither directed or undirected.
     */
    private class TestGraph<V, E>
        extends AbstractBaseGraph<V, E>
    {
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new graph
         *
         * @param edgeClass class on which to base factory for edges
         */
        public TestGraph(Class<? extends E> edgeClass)
        {
            this(new ClassBasedEdgeFactory<>(edgeClass));
        }

        /**
         * Creates a new graph
         *
         * @param ef the edge factory of the new graph.
         */
        public TestGraph(EdgeFactory<V, E> ef)
        {
            super(ef, true, true);
        }

        @Override
        protected Specifics<V, E> createSpecifics()
        {
            return new FastLookupDirectedSpecifics<>(this);
        }

    }

}

// End GraphsTest.java
