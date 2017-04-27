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

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

import org.jgrapht.graph.*;
import org.jgrapht.graph.DefaultGraphType.Builder;
import org.junit.*;

/**
 * @author Christoph Zauner
 * @author David Janos Csillik
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

        Graph<String, TestEdge> graph =
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

        Graph<String, TestEdge> expectedGraph =
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

        Graph<String, TestEdge> graph =
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

        Graph<String, TestEdge> expectedGraph =
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

        Graph<String, TestEdge> graph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

        String a = "A";
        String b = "B";

        graph.addVertex(a);
        graph.addVertex(b);

        graph.addEdge(a, b);

        Graph<String, TestEdge> expectedGraph =
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

        Graph<String, TestEdge> graph =
            new DefaultDirectedGraph<String, TestEdge>(TestEdge.class);

        String a = "A";
        String b = "B";

        graph.addVertex(a);
        graph.addVertex(b);

        graph.addEdge(a, b);

        Graph<String, TestEdge> expectedGraph =
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

        Graph<String, TestEdge> expectedGraph =
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

        Graph<String, TestEdge> expectedGraph =
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

    /*
     *  Tests for public static <V, E> E addEdge(Graph<V, E> g, V sourceVertex, V targetVertex, double weight)
     */
    @Test
    public void addEdgeTest() {
        SimpleGraph<String, DefaultWeightedEdge> graph = new SimpleGraph<>(new ClassBasedEdgeFactory<String, DefaultWeightedEdge>(DefaultWeightedEdge.class), true);

        String a = "A";
        String b = "B";
        final double weigth = 10.0;

        graph.addVertex(a);
        graph.addVertex(b);

        DefaultWeightedEdge edge = Graphs.<String, DefaultWeightedEdge>addEdge(graph, a, b, weigth);

        Assert.assertNotEquals(null, edge);
        Assert.assertEquals(weigth, graph.getEdgeWeight(edge), 0.0d);
        Assert.assertTrue(graph.getEdgeSource(edge).equals(a));
        Assert.assertTrue(graph.getEdgeTarget(edge).equals(b));
    }

    @Test
    public void addEdgeTwiceTest() {
        SimpleGraph<String, DefaultWeightedEdge> graph = new SimpleGraph<>(new ClassBasedEdgeFactory<String, DefaultWeightedEdge>(DefaultWeightedEdge.class), true);

        String a = "A";
        String b = "B";
        final double weigth = 10.0;

        graph.addVertex(a);
        graph.addVertex(b);

        DefaultWeightedEdge edge = Graphs.<String, DefaultWeightedEdge>addEdge(graph, a, b, weigth);
        edge = Graphs.<String, DefaultWeightedEdge>addEdge(graph, a, b, weigth);

        Assert.assertEquals(null, edge);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addEdgeWithInvalidVertexTest() {
        SimpleGraph<String, DefaultWeightedEdge> graph = new SimpleGraph<>(new ClassBasedEdgeFactory<String, DefaultWeightedEdge>(DefaultWeightedEdge.class), true);

        String a = "A";
        String b = "B";
        String c = "C";
        final double weigth = 10.0;

        graph.addVertex(a);
        graph.addVertex(b);

        Graphs.<String, DefaultWeightedEdge>addEdge(graph, a, c, weigth);
    }

    /*
     *  Tests for public static <V, E> E addEdgeWithVertices(Graph<V, E> g, V sourceVertex, V targetVertex) 
     */
    @Test
    public void addEdgeWithVerticesExistingVerticesTest() {
        SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";

        graph.addVertex(a);
        graph.addVertex(b);

        DefaultEdge edge = Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);

        Assert.assertNotEquals(null, edge);
        Assert.assertTrue(graph.getEdgeSource(edge).equals(a));
        Assert.assertTrue(graph.getEdgeTarget(edge).equals(b));
    }

    @Test
    public void addEdgeWithVerticesNewVerticesTest() {
        SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";

        DefaultEdge edge = Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);

        Assert.assertNotEquals(null, edge);
        Assert.assertTrue(graph.getEdgeSource(edge).equals(a));
        Assert.assertTrue(graph.getEdgeTarget(edge).equals(b));
    }

    /*
     *  Tests for public static <V,
     *      E> boolean addEdgeWithVertices(Graph<V, E> targetGraph, Graph<V, E> sourceGraph, E edge)
     */
    @Test
    public void addEdgeWithVertices2ExistingVerticesTest() {
        SimpleGraph<String, DefaultEdge> graphSource = new SimpleGraph<>(DefaultEdge.class);
        SimpleGraph<String, DefaultEdge> graphTarget = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";

        graphSource.addVertex(a);
        graphSource.addVertex(b);

        graphTarget.addVertex(a);
        graphTarget.addVertex(b);

        DefaultEdge edgeSource = Graphs.<String, DefaultEdge>addEdgeWithVertices(graphSource, a, b);

        Boolean result = Graphs.<String, DefaultEdge>addEdgeWithVertices(graphTarget, graphSource, edgeSource);

        DefaultEdge edgeTarget = graphTarget.getEdge(a, b);
        edgeSource = graphSource.getEdge(a, b);

        Assert.assertTrue(result);
        Assert.assertTrue(graphTarget.getEdgeSource(edgeTarget).equals(a));
        Assert.assertTrue(graphTarget.getEdgeTarget(edgeTarget).equals(b));
        Assert.assertTrue(graphSource.getEdgeSource(edgeSource).equals(a));
        Assert.assertTrue(graphSource.getEdgeTarget(edgeSource).equals(b));
    }

    @Test
    public void addEdgeWithVertices2NewVerticesTest() {
        SimpleGraph<String, DefaultEdge> graphSource = new SimpleGraph<>(DefaultEdge.class);
        SimpleGraph<String, DefaultEdge> graphTarget = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";

        graphSource.addVertex(a);
        graphSource.addVertex(b);

        DefaultEdge edgeSource = Graphs.<String, DefaultEdge>addEdgeWithVertices(graphSource, a, b);

        Boolean result = Graphs.<String, DefaultEdge>addEdgeWithVertices(graphTarget, graphSource, edgeSource);

        DefaultEdge edgeTarget = graphTarget.getEdge(a, b);
        edgeSource = graphSource.getEdge(a, b);

        Assert.assertTrue(result);
        Assert.assertTrue(graphTarget.getEdgeSource(edgeTarget).equals(a));
        Assert.assertTrue(graphTarget.getEdgeTarget(edgeTarget).equals(b));
        Assert.assertTrue(graphSource.getEdgeSource(edgeSource).equals(a));
        Assert.assertTrue(graphSource.getEdgeTarget(edgeSource).equals(b));
    }

    /*
     *  Tests for public static <V,
     *      E> E addEdgeWithVertices(Graph<V, E> g, V sourceVertex, V targetVertex, double weight)
     */
    @Test
    public void addEdgeWithVertices3ExistingVerticesTest() {
        SimpleGraph<String, DefaultWeightedEdge> graph = new SimpleGraph<>(new ClassBasedEdgeFactory<String, DefaultWeightedEdge>(DefaultWeightedEdge.class), true);

        String a = "A";
        String b = "B";
        final double weigth = 10.0;

        graph.addVertex(a);
        graph.addVertex(b);

        DefaultWeightedEdge edge = Graphs.<String, DefaultWeightedEdge>addEdgeWithVertices(graph, a, b, weigth);

        Assert.assertNotEquals(null,  edge);
        Assert.assertEquals(weigth, graph.getEdgeWeight(edge), 0.0d);
        Assert.assertTrue(graph.getEdgeSource(edge).equals(a));
        Assert.assertTrue(graph.getEdgeTarget(edge).equals(b));
    }

    @Test
    public void addEdgeWithVertices3NewVerticesTest() {
        SimpleGraph<String, DefaultWeightedEdge> graph = new SimpleGraph<>(new ClassBasedEdgeFactory<String, DefaultWeightedEdge>(DefaultWeightedEdge.class), true);

        String a = "A";
        String b = "B";        
        final double weigth = 10.0;

        DefaultWeightedEdge edge = Graphs.<String, DefaultWeightedEdge>addEdgeWithVertices(graph, a, b, weigth);

        Assert.assertNotEquals(null,  edge);
        Assert.assertEquals(weigth, graph.getEdgeWeight(edge), 0.0d);
        Assert.assertTrue(graph.getEdgeSource(edge).equals(a));
        Assert.assertTrue(graph.getEdgeTarget(edge).equals(b));
    }

    /*
     *  Tests for public static <V,
     *      E> boolean addGraph(Graph<? super V, ? super E> destination, Graph<V, E> source)
     */
    @Test
    public void addGraphToEmptyGraphTest() {
        SimpleGraph<String, DefaultEdge> graphSource = new SimpleGraph<>(DefaultEdge.class);
        SimpleGraph<String, DefaultEdge> graphTarget = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";

        graphSource.addVertex(a);
        graphSource.addVertex(b);

        Graphs.<String, DefaultEdge>addEdgeWithVertices(graphSource, a, b);

        Boolean modified = Graphs.<String, DefaultEdge>addGraph(graphTarget, graphSource);

        Assert.assertTrue(modified);
        Assert.assertEquals(graphTarget, graphSource);
    }

    @Test
    public void addGraphToTheSameGraphTest() {
        SimpleGraph<String, DefaultEdge> graphSource = new SimpleGraph<>(DefaultEdge.class);
        SimpleGraph<String, DefaultEdge> graphTarget = new SimpleGraph<>(DefaultEdge.class);
        SimpleGraph<String, DefaultEdge> graphTargetCopy;

        String a = "A";
        String b = "B";

        graphSource.addVertex(a);
        graphSource.addVertex(b);
        graphTarget.addVertex(a);
        graphTarget.addVertex(b);

        Graphs.<String, DefaultEdge>addEdgeWithVertices(graphSource, a, b);
        Graphs.<String, DefaultEdge>addEdgeWithVertices(graphTarget, a, b);

        graphTargetCopy = (SimpleGraph<String, DefaultEdge>)((AbstractBaseGraph<String, DefaultEdge>)graphTarget).clone();

        Boolean modified = Graphs.<String, DefaultEdge>addGraph(graphTarget, graphSource);

        Assert.assertFalse(modified);
        Assert.assertEquals(graphTargetCopy, graphTarget);
    }

    /*
     *  Tests for public static <V, 
     *      E> void addGraphReversed(Graph<? super V, ? super E> destination, Graph<V, E> source)
     */
    @Test
    public void addGraphReversedTest() {
        DefaultDirectedGraph<String, DefaultEdge> graphSource = new DefaultDirectedGraph<>(DefaultEdge.class);
        DefaultDirectedGraph<String, DefaultEdge> graphTarget = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";

        graphSource.addVertex(a);
        graphSource.addVertex(b);

        graphSource.addEdge(a, b);

        Graphs.<String, DefaultEdge>addGraphReversed(graphTarget, graphSource);

        DefaultEdge edgeAB = graphTarget.getEdge(a, b);
        DefaultEdge edgeBA = graphTarget.getEdge(b, a);

        Assert.assertNull(edgeAB);        
        Assert.assertNotNull(edgeBA);        
        Assert.assertEquals(a, graphTarget.getEdgeTarget(edgeBA));
        Assert.assertEquals(b, graphTarget.getEdgeSource(edgeBA));
    }

    @Test(expected=IllegalArgumentException.class)
    public void addGraphReversedWithOneInvalidGraph1Test() {
        DefaultDirectedGraph<String, DefaultEdge> graphSource = new DefaultDirectedGraph<>(DefaultEdge.class);
        SimpleGraph<String, DefaultEdge> graphTarget = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";

        graphSource.addVertex(a);
        graphSource.addVertex(b);

        graphSource.addEdge(a, b);

        Graphs.<String, DefaultEdge>addGraphReversed(graphTarget, graphSource);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addGraphReversedWithOneInvalidGraph2Test() {
        SimpleGraph<String, DefaultEdge> graphSource = new SimpleGraph<>(DefaultEdge.class);
        DefaultDirectedGraph<String, DefaultEdge> graphTarget = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";

        graphSource.addVertex(a);
        graphSource.addVertex(b);

        graphSource.addEdge(a, b);

        Graphs.<String, DefaultEdge>addGraphReversed(graphTarget, graphSource);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addGraphReversedWithTwoInvalidGraphsTest() {
        SimpleGraph<String, DefaultEdge> graphSource = new SimpleGraph<>(DefaultEdge.class);
        SimpleGraph<String, DefaultEdge> graphTarget = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";

        graphSource.addVertex(a);
        graphSource.addVertex(b);

        graphSource.addEdge(a, b);

        Graphs.<String, DefaultEdge>addGraphReversed(graphTarget, graphSource);
    }

    /*
     *  Tests for public static <V, E> boolean addAllEdges(
     *      Graph<? super V, ? super E> destination, Graph<V, E> source, Collection<? extends E> edges)
     */
    @Test
    public void addAllEdgesWithEmptyGraphTest() {
        Graph<String, DefaultEdge> graphSource = new SimpleGraph<>(DefaultEdge.class);
        Graph<String, DefaultEdge> graphTarget = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";

        graphSource.addVertex(a);
        graphSource.addVertex(b);

        final DefaultEdge edgeSource = Graphs.<String, DefaultEdge>addEdgeWithVertices(graphSource, a, b);

        Boolean modified = Graphs.<String, DefaultEdge>addAllEdges(graphTarget, graphSource, new ArrayList<DefaultEdge>() {{
            add(edgeSource);
        }});

        Set<DefaultEdge> edgesTarget = graphTarget.edgeSet();

        Assert.assertTrue(modified);
        Assert.assertEquals(1, edgesTarget.size());
        Assert.assertTrue(edgesTarget.contains(edgeSource));
    }

    @Test
    public void addAllEdgesWithTheSameGraphTest() {
        Graph<String, DefaultEdge> graphSource = new SimpleGraph<>(DefaultEdge.class);
        Graph<String, DefaultEdge> graphTarget = new SimpleGraph<>(DefaultEdge.class);
        Graph<String, DefaultEdge> graphTargetCopy;

        String a = "A";
        String b = "B";

        graphSource.addVertex(a);
        graphSource.addVertex(b);

        graphTarget.addVertex(a);
        graphTarget.addVertex(b);

        final DefaultEdge edgeSource = Graphs.<String, DefaultEdge>addEdgeWithVertices(graphSource, a, b);
        Graphs.<String, DefaultEdge>addEdgeWithVertices(graphTarget, a, b);

        graphTargetCopy = (Graph<String, DefaultEdge>)((AbstractBaseGraph<String, DefaultEdge>)graphTarget).clone();

        Boolean modified = Graphs.<String, DefaultEdge>addAllEdges(graphTarget, graphSource, new ArrayList<DefaultEdge>() {{
            add(edgeSource);
        }});

        Assert.assertFalse(modified);
        Assert.assertEquals(graphTargetCopy, graphTarget);
    }

    /*
     *  Tests for public static <V, E> boolean addAllVertices(
     *      Graph<? super V, ? super E> destination, Collection<? extends V> vertices)
     */
    @Test
    public void addAllVerticesWithEmptyGraph() {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";

        Boolean modified = Graphs.<String, DefaultEdge>addAllVertices(graph, new ArrayList<String>() {{
            add(a);
            add(b);
        }});

        Set<String> vertices = graph.vertexSet();

        Assert.assertTrue(modified);
        Assert.assertEquals(2, vertices.size());
        Assert.assertTrue(vertices.contains(a));        
        Assert.assertTrue(vertices.contains(b));        
    }

    @Test
    public void addAllVerticesWithExistingVerticesGraph() {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";

        graph.addVertex(a);
        graph.addVertex(b);

        Boolean modified = Graphs.<String, DefaultEdge>addAllVertices(graph, new ArrayList<String>() {{
            add(a);
            add(b);
        }});

        Set<String> vertices = graph.vertexSet();

        Assert.assertFalse(modified);
        Assert.assertEquals(2, vertices.size());
        Assert.assertTrue(vertices.contains(a));        
        Assert.assertTrue(vertices.contains(b));        
    }

    /*
     *  Tests for public static <V, E> List<V> neighborListOf(Graph<V, E> g, V vertex)
     */
    @Test
    public void neighborListOfWithSimpleGraphTest() {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";
        String e = "E";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);
        graph.addVertex(e);
        graph.addVertex(x);

        Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);
        Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, c);
        Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, d);
        Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, e);

        List<String> neighbors = Graphs.<String, DefaultEdge>neighborListOf(graph, a);

        Assert.assertEquals(4,  neighbors.size());
        Assert.assertTrue(neighbors.contains(b));
        Assert.assertTrue(neighbors.contains(c));
        Assert.assertTrue(neighbors.contains(d));
        Assert.assertTrue(neighbors.contains(e));
    }

    @Test
    public void neighborListOfWithMultigraphTest() {
        Graph<String, DefaultEdge> graph = new Multigraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);
        Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);

        List<String> neighbors = Graphs.<String, DefaultEdge>neighborListOf(graph, a);

        Assert.assertEquals(2,  neighbors.size());
        Assert.assertEquals(b, neighbors.get(0));
        Assert.assertEquals(b, neighbors.get(1));
    }

    /*
     *  Tests for public static <V, E> List<V> predecessorListOf(Graph<V, E> g, V vertex)
     */
    @Test
    public void predecessorListOfWithDefaultDirectedGraphTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";
        String e = "E";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);
        graph.addVertex(e);
        graph.addVertex(x);

        graph.addEdge(b, a);
        graph.addEdge(c, a);
        graph.addEdge(d, a);
        graph.addEdge(e, a);

        List<String> predecessors = Graphs.<String, DefaultEdge>predecessorListOf(graph, a);

        Assert.assertEquals(4,  predecessors.size());
        Assert.assertTrue(predecessors.contains(b));
        Assert.assertTrue(predecessors.contains(c));
        Assert.assertTrue(predecessors.contains(d));
        Assert.assertTrue(predecessors.contains(e));
    }

    @Test
    public void predecessorListOfWithDirectedMultigraphTest() {
        DirectedMultigraph<String, DefaultEdge> graph = new DirectedMultigraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        graph.addEdge(b, a);
        graph.addEdge(b, a);

        List<String> predecessors = Graphs.<String, DefaultEdge>predecessorListOf(graph, a);

        Assert.assertEquals(2,  predecessors.size());
        Assert.assertEquals(b, predecessors.get(0));
        Assert.assertEquals(b, predecessors.get(1));
    }

    /*
     *  Tests for public static <V, E> List<V> successorListOf(Graph<V, E> g, V vertex)
     */
    @Test
    public void successorListOfWithDefaultDirectedGraphTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";
        String e = "E";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);
        graph.addVertex(e);
        graph.addVertex(x);

        graph.addEdge(a, b);
        graph.addEdge(a, c);
        graph.addEdge(a, d);
        graph.addEdge(a, e);

        List<String> predecessors = Graphs.<String, DefaultEdge>successorListOf(graph, a);

        Assert.assertEquals(4,  predecessors.size());
        Assert.assertTrue(predecessors.contains(b));
        Assert.assertTrue(predecessors.contains(c));
        Assert.assertTrue(predecessors.contains(d));
        Assert.assertTrue(predecessors.contains(e));
    }

    @Test
    public void successorListOfWithDirectedMultigraphTest() {
        DirectedMultigraph<String, DefaultEdge> graph = new DirectedMultigraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        graph.addEdge(a, b);
        graph.addEdge(a, b);

        List<String> predecessors = Graphs.<String, DefaultEdge>successorListOf(graph, a);

        Assert.assertEquals(2,  predecessors.size());
        Assert.assertEquals(b, predecessors.get(0));
        Assert.assertEquals(b, predecessors.get(1));
    }

    /*
     *  Tests for public static <V, E> Graph<V, E> undirectedGraph(Graph<V, E> g)
     */
    @Test
    public void undirectedGraphWithSimpleGraphTest() {
        SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        graph.addEdge(a, b);

        Graph<String, DefaultEdge> graphUndirected = Graphs.<String, DefaultEdge>undirectedGraph(graph);

        Assert.assertEquals(3,  graphUndirected.vertexSet().size());
        Assert.assertTrue(graphUndirected.vertexSet().contains(a));
        Assert.assertTrue(graphUndirected.vertexSet().contains(b));
        Assert.assertTrue(graphUndirected.vertexSet().contains(x));

        Assert.assertEquals(1,  graphUndirected.edgeSet().size());
        Assert.assertTrue(graphUndirected.containsEdge(a, b));
    }

    @Test
    public void undirectedGraphWithDefaultDirectedGraphTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        graph.addEdge(a, b);

        Graph<String, DefaultEdge> graphUndirected = Graphs.<String, DefaultEdge>undirectedGraph(graph);

        Assert.assertEquals(3,  graphUndirected.vertexSet().size());
        Assert.assertTrue(graphUndirected.vertexSet().contains(a));
        Assert.assertTrue(graphUndirected.vertexSet().contains(b));
        Assert.assertTrue(graphUndirected.vertexSet().contains(x));

        Assert.assertEquals(1,  graphUndirected.edgeSet().size());
        Assert.assertTrue(graphUndirected.containsEdge(a, b));
    }

    @Test
    public void undirectedGraphWithDirectedMultigraphTest() {
        DirectedMultigraph<String, DefaultEdge> graph = new DirectedMultigraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        graph.addEdge(a, b);
        graph.addEdge(a, b);

        Graph<String, DefaultEdge> graphUndirected = Graphs.<String, DefaultEdge>undirectedGraph(graph);

        Assert.assertEquals(3,  graphUndirected.vertexSet().size());
        Assert.assertTrue(graphUndirected.vertexSet().contains(a));
        Assert.assertTrue(graphUndirected.vertexSet().contains(b));
        Assert.assertTrue(graphUndirected.vertexSet().contains(x));

        Assert.assertEquals(2,  graphUndirected.edgeSet().size());
        Assert.assertTrue(graphUndirected.containsEdge(a, b));
        graphUndirected.removeEdge(a, b);
        Assert.assertTrue(graphUndirected.containsEdge(a, b));
    }

    @Test(expected=IllegalArgumentException.class)
    public void undirectedGraphWithDummyUndirectedGraphTest() {
        DummyGraph<String, DefaultEdge> graph = new DummyGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        graph.addEdge(a, b);
        graph.addEdge(a, b);

        Graphs.<String, DefaultEdge>undirectedGraph(graph);
    }

    /*
     *  Tests for public static <V, E> boolean testIncidence(Graph<V, E> g, E e, V v)
     */
    @Test
    public void testIncidenceTest() {
        SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        DefaultEdge edge = Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);

        Assert.assertNotEquals(null, edge);
        Assert.assertTrue(Graphs.<String, DefaultEdge>testIncidence(graph, edge, a));
        Assert.assertTrue(Graphs.<String, DefaultEdge>testIncidence(graph, edge, b));
        Assert.assertFalse(Graphs.<String, DefaultEdge>testIncidence(graph, edge, x));        
    }

    // Tests for public static <V, E> V getOppositeVertex(Graph<V, E> g, E e, V v)
    @Test
    public void getOppositeVertexTest() {
        SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";

        graph.addVertex(a);
        graph.addVertex(b);

        DefaultEdge edge = Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);

        Assert.assertNotEquals(null, edge);
        Assert.assertEquals(b, Graphs.<String, DefaultEdge>getOppositeVertex(graph, edge, a));
        Assert.assertEquals(a, Graphs.<String, DefaultEdge>getOppositeVertex(graph, edge, b));
    }

    @Test(expected=IllegalArgumentException.class)
    public void getOppositeVertexWithInvalidVertexTest() {
        SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        DefaultEdge edge = Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);

        Graphs.<String, DefaultEdge>getOppositeVertex(graph, edge, x);
    }

    /*
     *  Tests for public static <V, E> boolean removeVertexAndPreserveConnectivity(Graph<V, E> graph, V vertex)
     */
    @Test
    public void removeVertexAndPreserveConnectivity1WithValidVerticesTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(x);

        graph.addEdge(a, x);
        graph.addEdge(x, b);
        graph.addEdge(x, c);

        Boolean found = Graphs.<String, DefaultEdge>removeVertexAndPreserveConnectivity(graph, x);

        Assert.assertTrue(found);

        Assert.assertEquals(2, graph.edgeSet().size());

        Assert.assertFalse(graph.containsEdge(a, x));
        Assert.assertFalse(graph.containsEdge(x, b));
        Assert.assertFalse(graph.containsEdge(x, c));

        Assert.assertTrue(graph.containsEdge(a, b));
        Assert.assertTrue(graph.containsEdge(a, c));
    }

    @Test
    public void removeVertexAndPreserveConnectivity1WithInvalidVertexTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);

        graph.addEdge(a, b);
        graph.addEdge(a, c);

        Boolean found = Graphs.<String, DefaultEdge>removeVertexAndPreserveConnectivity(graph, x);

        Assert.assertFalse(found);

        Assert.assertEquals(2, graph.edgeSet().size());

        Assert.assertTrue(graph.containsEdge(a, b));
        Assert.assertTrue(graph.containsEdge(a, c));
    }

    /*
     *  Tests for public static <V,
     *      E> boolean removeVerticesAndPreserveConnectivity(Graph<V, E> graph, Predicate<V> predicate)
     */
    @Test
    public void removeVerticesAndPreserveConnectivityWithValidVerticesTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(x);

        graph.addEdge(a, x);
        graph.addEdge(x, b);
        graph.addEdge(x, c);

        Boolean found = Graphs.<String, DefaultEdge>removeVerticesAndPreserveConnectivity(graph, (v) -> v.equals(x));

        Assert.assertTrue(found);

        Assert.assertEquals(2, graph.edgeSet().size());

        Assert.assertFalse(graph.containsEdge(a, x));
        Assert.assertFalse(graph.containsEdge(x, b));
        Assert.assertFalse(graph.containsEdge(x, c));

        Assert.assertTrue(graph.containsEdge(a, b));
        Assert.assertTrue(graph.containsEdge(a, c));
    }

    @Test
    public void removeVerticesAndPreserveConnectivityWithInvalidVertexTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);

        graph.addEdge(a, b);
        graph.addEdge(a, c);

        Boolean found = Graphs.<String, DefaultEdge>removeVerticesAndPreserveConnectivity(graph, (v) -> v.equals(x));

        Assert.assertFalse(found);

        Assert.assertEquals(2, graph.edgeSet().size());

        Assert.assertTrue(graph.containsEdge(a, b));
        Assert.assertTrue(graph.containsEdge(a, c));
    }

    /*
     *  Tests for public static <V,
     *      E> boolean removeVertexAndPreserveConnectivity(Graph<V, E> graph, Iterable<V> vertices)
     */
    @Test
    public void removeVertexAndPreserveConnectivity2WithValidVerticesTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(x);

        graph.addEdge(a, x);
        graph.addEdge(x, b);
        graph.addEdge(x, c);

        ArrayList<String> verticesToRemove = new ArrayList<String>() {{
            add(x);
        }};

        Boolean found = Graphs.<String, DefaultEdge>removeVertexAndPreserveConnectivity(graph, verticesToRemove);

        Assert.assertTrue(found);

        Assert.assertEquals(2, graph.edgeSet().size());

        Assert.assertFalse(graph.containsEdge(a, x));
        Assert.assertFalse(graph.containsEdge(x, b));
        Assert.assertFalse(graph.containsEdge(x, c));

        Assert.assertTrue(graph.containsEdge(a, b));
        Assert.assertTrue(graph.containsEdge(a, c));
    }

    @Test
    public void removeVertexAndPreserveConnectivity2WithInvalidVertexTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);

        graph.addEdge(a, b);
        graph.addEdge(a, c);

        ArrayList<String> verticesToRemove = new ArrayList<String>() {{
            add(x);
        }};

        Boolean found = Graphs.<String, DefaultEdge>removeVertexAndPreserveConnectivity(graph, verticesToRemove);

        Assert.assertFalse(found);

        Assert.assertEquals(2, graph.edgeSet().size());

        Assert.assertTrue(graph.containsEdge(a, b));
        Assert.assertTrue(graph.containsEdge(a, c));
    }

    /*
     *  Tests for public static <V, E> void addOutgoingEdges(Graph<V, E> graph, V source, Iterable<V> targets)
     */
    @Test
    public void addOutgoingEdgesWithValidVertexAndWithDefaultDirectedGraphTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(x);

        ArrayList<String> outgoingVertices = new ArrayList<String>() {{
            add(a);
            add(b);
            add(c);
        }};

        Graphs.<String, DefaultEdge>addOutgoingEdges(graph, x, outgoingVertices);

        Assert.assertEquals(3, graph.edgeSet().size());

        Assert.assertTrue(graph.containsEdge(x, a));
        Assert.assertTrue(graph.containsEdge(x, b));
        Assert.assertTrue(graph.containsEdge(x, c));
    }

    @Test
    public void addOutgoingEdgesWithMissingVertexAndWithDefaultDirectedGraphTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);

        ArrayList<String> outgoingVertices = new ArrayList<String>() {{
            add(a);
            add(b);
            add(c);
        }};

        Graphs.<String, DefaultEdge>addOutgoingEdges(graph, x, outgoingVertices);

        Assert.assertEquals(3, graph.edgeSet().size());

        Assert.assertTrue(graph.containsEdge(x, a));
        Assert.assertTrue(graph.containsEdge(x, b));
        Assert.assertTrue(graph.containsEdge(x, c));
    }

    @Test
    public void addOutgoingEdgesWithValidVertexAndWithDirectedMultigraphTest() {
        DirectedMultigraph<String, DefaultEdge> graph = new DirectedMultigraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(x);

        ArrayList<String> outgoingVertices = new ArrayList<String>() {{
            add(a);
            add(a);
            add(b);
            add(c);
        }};

        Graphs.<String, DefaultEdge>addOutgoingEdges(graph, x, outgoingVertices);

        Assert.assertEquals(4, graph.edgeSet().size());

        Assert.assertTrue(graph.containsEdge(x, a));
        Assert.assertTrue(graph.containsEdge(x, b));
        Assert.assertTrue(graph.containsEdge(x, c));

        graph.removeEdge(x, a);

        Assert.assertTrue(graph.containsEdge(x, a));
    }

    /*
     *  Tests for public static <V, E> void addIncomingEdges(Graph<V, E> graph, V target, Iterable<V> sources)
     */
    @Test
    public void addIncomingEdgesWithValidVertexAndWithDefaultDirectedGraphTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(x);

        ArrayList<String> outgoingVertices = new ArrayList<String>() {{
            add(a);
            add(b);
            add(c);
        }};

        Graphs.<String, DefaultEdge>addIncomingEdges(graph, x, outgoingVertices);

        Assert.assertEquals(3, graph.edgeSet().size());

        Assert.assertTrue(graph.containsEdge(a, x));
        Assert.assertTrue(graph.containsEdge(b, x));
        Assert.assertTrue(graph.containsEdge(c, x));
    }

    @Test
    public void addIncomingEdgesWithMissingVertexAndWithDefaultDirectedGraphTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);

        ArrayList<String> outgoingVertices = new ArrayList<String>() {{
            add(a);
            add(b);
            add(c);
        }};

        Graphs.<String, DefaultEdge>addIncomingEdges(graph, x, outgoingVertices);

        Assert.assertEquals(3, graph.edgeSet().size());

        Assert.assertTrue(graph.containsEdge(a, x));
        Assert.assertTrue(graph.containsEdge(b, x));
        Assert.assertTrue(graph.containsEdge(c, x));
    }

    @Test
    public void addIncomingEdgesWithValidVertexAndWithDirectedMultigraphTest() {
        DirectedMultigraph<String, DefaultEdge> graph = new DirectedMultigraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(x);

        ArrayList<String> outgoingVertices = new ArrayList<String>() {{
            add(a);
            add(a);
            add(b);
            add(c);
        }};

        Graphs.<String, DefaultEdge>addIncomingEdges(graph, x, outgoingVertices);

        Assert.assertEquals(4, graph.edgeSet().size());

        Assert.assertTrue(graph.containsEdge(a, x));
        Assert.assertTrue(graph.containsEdge(b, x));
        Assert.assertTrue(graph.containsEdge(c, x));

        graph.removeEdge(a, x);

        Assert.assertTrue(graph.containsEdge(a, x));
    }

    /*
     *  Tests for public static <V, E> boolean vertexHasSuccessors(Graph<V, E> graph, V vertex)
     */
    @Test
    public void vertexHasSuccessorsWithSimpleGraphTest() {
        SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        DefaultEdge edge = Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);

        Assert.assertNotEquals(null, edge);
        Assert.assertTrue(Graphs.<String, DefaultEdge>vertexHasSuccessors(graph, a));
        Assert.assertTrue(Graphs.<String, DefaultEdge>vertexHasSuccessors(graph, b));
        Assert.assertFalse(Graphs.<String, DefaultEdge>vertexHasSuccessors(graph, x));
    }

    @Test
    public void vertexHasSuccessorsWithDefaultDirectedGraphTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        DefaultEdge edge = Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);

        Assert.assertNotEquals(null, edge);
        Assert.assertTrue(Graphs.<String, DefaultEdge>vertexHasSuccessors(graph, a));
        Assert.assertFalse(Graphs.<String, DefaultEdge>vertexHasSuccessors(graph, b));
        Assert.assertFalse(Graphs.<String, DefaultEdge>vertexHasSuccessors(graph, x));
    }

    @Test
    public void vertexHasSuccessorsWithDirectedMultigraphTest() {
        DirectedMultigraph<String, DefaultEdge> graph = new DirectedMultigraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        DefaultEdge edge1 = Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);
        DefaultEdge edge2 = Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);

        Assert.assertNotEquals(null, edge1);
        Assert.assertNotEquals(null, edge2);
        Assert.assertTrue(Graphs.<String, DefaultEdge>vertexHasSuccessors(graph, a));
        Assert.assertFalse(Graphs.<String, DefaultEdge>vertexHasSuccessors(graph, b));
        Assert.assertFalse(Graphs.<String, DefaultEdge>vertexHasSuccessors(graph, x));
    }

    /*
     *  Tests for public static <V, E> boolean vertexHasPredecessors(Graph<V, E> graph, V vertex) 
     */
    @Test
    public void vertexHasPredecessorsWithSimpleGraphTest() {
        SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        DefaultEdge edge = Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);

        Assert.assertNotEquals(null, edge);
        Assert.assertTrue(Graphs.<String, DefaultEdge>vertexHasPredecessors(graph, a));
        Assert.assertTrue(Graphs.<String, DefaultEdge>vertexHasPredecessors(graph, b));
        Assert.assertFalse(Graphs.<String, DefaultEdge>vertexHasPredecessors(graph, x));
    }

    @Test
    public void vertexHasPredecessorsWithDefaultDirectedGraphTest() {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        DefaultEdge edge = Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);

        Assert.assertNotEquals(null, edge);
        Assert.assertTrue(Graphs.<String, DefaultEdge>vertexHasPredecessors(graph, b));
        Assert.assertFalse(Graphs.<String, DefaultEdge>vertexHasPredecessors(graph, a));
        Assert.assertFalse(Graphs.<String, DefaultEdge>vertexHasPredecessors(graph, x));
    }

    @Test
    public void vertexHasPredecessorsWithDirectedMultigraphTest() {
        DirectedMultigraph<String, DefaultEdge> graph = new DirectedMultigraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String x = "X";

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(x);

        DefaultEdge edge1 = Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);
        DefaultEdge edge2 = Graphs.<String, DefaultEdge>addEdgeWithVertices(graph, a, b);

        Assert.assertNotEquals(null, edge1);
        Assert.assertNotEquals(null, edge2);
        Assert.assertTrue(Graphs.<String, DefaultEdge>vertexHasPredecessors(graph, b));
        Assert.assertFalse(Graphs.<String, DefaultEdge>vertexHasPredecessors(graph, a));
        Assert.assertFalse(Graphs.<String, DefaultEdge>vertexHasPredecessors(graph, x));
    }
}

// End GraphsTest.java
