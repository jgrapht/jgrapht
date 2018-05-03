/*
 * (C) Copyright 2017-2018, by Joris Kinable and Contributors.
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

import static org.junit.Assert.assertEquals;

import java.util.*;

import org.jgrapht.alg.cycle.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.SupplierUtil;
import org.junit.*;

/**
 * Tests for GraphMetrics
 * 
 * @author Joris Kinable
 */
public class GraphMetricsTest
{

    private final double EPSILON = 0.000000001;

    @Test
    public void testGraphDiameter()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(g, 0, 1, 10);
        Graphs.addEdgeWithVertices(g, 1, 0, 12);
        double diameter = GraphMetrics.getDiameter(g);
        assertEquals(12.0, diameter, EPSILON);

    }

    @Test
    public void testGraphRadius()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        double radius = GraphMetrics.getRadius(g);
        assertEquals(0.0, radius, EPSILON);
    }

    @Test
    public void testGraphGirthAcyclic()
    {
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(tree, Arrays.asList(0, 1, 2, 3, 4, 5));
        tree.addEdge(0, 1);
        tree.addEdge(0, 4);
        tree.addEdge(0, 5);
        tree.addEdge(1, 2);
        tree.addEdge(1, 3);

        assertEquals(Integer.MAX_VALUE, GraphMetrics.getGirth(tree));
    }

    @Test
    public void testGraphDirectedAcyclic()
    {
        Graph<Integer, DefaultEdge> tree = new SimpleDirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(tree, Arrays.asList(0, 1, 2, 3));
        tree.addEdge(0, 1);
        tree.addEdge(0, 2);
        tree.addEdge(1, 3);
        tree.addEdge(2, 3);

        assertEquals(Integer.MAX_VALUE, GraphMetrics.getGirth(tree));
    }

    @Test
    public void testGraphDirectedCyclic()
    {
        Graph<Integer, DefaultEdge> tree = new SimpleDirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(tree, Arrays.asList(0, 1, 2, 3));
        tree.addEdge(0, 1);
        tree.addEdge(1, 2);
        tree.addEdge(2, 3);
        tree.addEdge(3, 0);

        assertEquals(4, GraphMetrics.getGirth(tree));
    }

    @Test
    public void testGraphDirectedCyclic2()
    {
        Graph<Integer, DefaultEdge> tree = new SimpleDirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(tree, Arrays.asList(0, 1));
        tree.addEdge(0, 1);
        tree.addEdge(1, 0);

        assertEquals(2, GraphMetrics.getGirth(tree));
    }

    @Test
    public void testGraphGirthGridGraph()
    {
        Graph<Integer, DefaultEdge> grid = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new GridGraphGenerator<>(3, 4);
        gen.generateGraph(grid);
        assertEquals(4, GraphMetrics.getGirth(grid));
    }

    @Test
    public void testGraphGirthRingGraphEven()
    {
        Graph<Integer, DefaultEdge> ring = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new RingGraphGenerator<>(10);
        gen.generateGraph(ring);
        assertEquals(10, GraphMetrics.getGirth(ring));
    }

    @Test
    public void testGraphGirthRingGraphOdd()
    {
        Graph<Integer, DefaultEdge> ring = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new RingGraphGenerator<>(9);
        gen.generateGraph(ring);
        assertEquals(9, GraphMetrics.getGirth(ring));
    }

    @Test
    public void testGraphGirthWheelGraph()
    {
        Graph<Integer, DefaultEdge> grid = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new WheelGraphGenerator<>(5);
        gen.generateGraph(grid);
        assertEquals(3, GraphMetrics.getGirth(grid));
    }

    @Test
    public void testGraphDirected1()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3));
        graph.addEdge(1, 0);
        graph.addEdge(3, 0);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 2);
        assertEquals(2, GraphMetrics.getGirth(graph));
    }

    @Test
    public void testPseudoGraphUndirected()
    {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3));
        graph.addEdge(0, 1);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);
        assertEquals(1, GraphMetrics.getGirth(graph));
    }

    @Test
    public void testPseudoGraphDirected()
    {
        Graph<Integer, DefaultEdge> graph = new DirectedPseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3));
        graph.addEdge(0, 1);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);
        assertEquals(1, GraphMetrics.getGirth(graph));
    }

    @Test
    public void testMultiGraphUndirected()
    {
        Graph<Integer, DefaultEdge> graph = new Multigraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3));
        graph.addEdge(0, 1);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);
        assertEquals(2, GraphMetrics.getGirth(graph));
    }

    @Test
    public void testMultiGraphDirected()
    {
        Graph<Integer, DefaultEdge> graph = new DirectedMultigraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3));
        graph.addEdge(0, 1);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);
        assertEquals(4, GraphMetrics.getGirth(graph));
    }

    @Test
    public void testDirectedGraphs()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnpRandomGraphGenerator<>(10, .55, 0);
        for (int i = 0; i < 10; i++) {
            Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            gen.generateGraph(graph);

            TarjanSimpleCycles<Integer, DefaultEdge> tarjanSimpleCycles =
                new TarjanSimpleCycles<>(graph);
            int minCycle = tarjanSimpleCycles
                .findSimpleCycles().stream().mapToInt(List::size).min().orElse(Integer.MAX_VALUE);

            assertEquals(minCycle, GraphMetrics.getGirth(graph));
        }
    }
}
