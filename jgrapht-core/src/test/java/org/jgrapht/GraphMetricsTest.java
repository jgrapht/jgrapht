/*
 * (C) Copyright 2017-2021, by Joris Kinable and Contributors.
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
package org.jgrapht;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.jgrapht.alg.cycle.TarjanSimpleCycles;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.generate.GridGraphGenerator;
import org.jgrapht.generate.NamedGraphGenerator;
import org.jgrapht.generate.RingGraphGenerator;
import org.jgrapht.generate.WheelGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for GraphMetrics
 * 
 * @author Joris Kinable
 * @author Alexandru Valeanu
 */
public class GraphMetricsTest
{

    private final static double EPSILON = 0.000000001;

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
        Graph<Integer, DefaultEdge> grid = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new GridGraphGenerator<>(3, 4);
        gen.generateGraph(grid);
        assertEquals(4, GraphMetrics.getGirth(grid));
    }

    @Test
    public void testGraphGirthRingGraphEven()
    {
        Graph<Integer, DefaultEdge> ring = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new RingGraphGenerator<>(10);
        gen.generateGraph(ring);
        assertEquals(10, GraphMetrics.getGirth(ring));
    }

    @Test
    public void testGraphGirthRingGraphOdd()
    {
        Graph<Integer, DefaultEdge> ring = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new RingGraphGenerator<>(9);
        gen.generateGraph(ring);
        assertEquals(9, GraphMetrics.getGirth(ring));
    }

    @Test
    public void testGraphGirthWheelGraph()
    {
        Graph<Integer, DefaultEdge> grid = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new WheelGraphGenerator<>(5);
        gen.generateGraph(grid);
        assertEquals(3, GraphMetrics.getGirth(grid));
    }

    @Test
    public void testGraphDirected1()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
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
            Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            gen.generateGraph(graph);

            TarjanSimpleCycles<Integer, DefaultEdge> tarjanSimpleCycles =
                new TarjanSimpleCycles<>(graph);
            int minCycle = tarjanSimpleCycles
                .findSimpleCycles().stream().mapToInt(List::size).min().orElse(Integer.MAX_VALUE);

            assertEquals(minCycle, GraphMetrics.getGirth(graph));
        }
    }

    private static long naiveCountTriangles(Graph<Integer, DefaultEdge> graph)
    {
        return GraphMetrics.naiveCountTriangles(graph, new ArrayList<>(graph.vertexSet()));
    }

    @Test
    public void testCountTriangles()
    {
        final int NUM_TESTS = 300;
        Random random = new Random(0x88_88);

        for (int test = 0; test < NUM_TESTS; test++) {
            final int N = 20 + random.nextInt(100);

            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            BarabasiAlbertGraphGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertGraphGenerator<>(
                    10 + random.nextInt(10), 1 + random.nextInt(7), N, random);

            generator.generateGraph(graph);

            Assert
                .assertEquals(naiveCountTriangles(graph), GraphMetrics.getNumberOfTriangles(graph));
        }
    }

    @Test
    public void testCountTriangles2()
    {
        final int NUM_TESTS = 100;
        Random random = new Random(0x88_88);

        for (int test = 0; test < NUM_TESTS; test++) {
            final int N = 1 + random.nextInt(100);

            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            GraphGenerator<Integer, DefaultEdge, Integer> generator =
                new GnpRandomGraphGenerator<>(N, .55, random.nextInt());

            generator.generateGraph(graph);

            Assert
                .assertEquals(naiveCountTriangles(graph), GraphMetrics.getNumberOfTriangles(graph));
        }
    }

    @Test
    public void testCountTriangles3()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        // Complete graph: expected (|V| choose 3)

        GraphGenerator<Integer, DefaultEdge, Integer> generator = new CompleteGraphGenerator<>(50);
        generator.generateGraph(graph);

        Assert.assertEquals(50 * 49 * 48 / 6, GraphMetrics.getNumberOfTriangles(graph));
        Assert.assertEquals(50 * 49 * 48 / 6, naiveCountTriangles(graph));

        // Wheel graph: expected |V|-1 triangles

        graph.removeAllVertices(new HashSet<>(graph.vertexSet()));
        generator = new WheelGraphGenerator<>(50);
        generator.generateGraph(graph);

        Assert.assertEquals(49, GraphMetrics.getNumberOfTriangles(graph));
        Assert.assertEquals(49, naiveCountTriangles(graph));

        // Named graphs

        NamedGraphGenerator<Integer, DefaultEdge> gen = new NamedGraphGenerator<>();

        graph.removeAllVertices(new HashSet<>(graph.vertexSet()));
        gen.generatePetersenGraph(graph);

        Assert.assertEquals(0, GraphMetrics.getNumberOfTriangles(graph));
        Assert.assertEquals(0, naiveCountTriangles(graph));

        graph.removeAllVertices(new HashSet<>(graph.vertexSet()));
        gen.generateDiamondGraph(graph);

        Assert.assertEquals(2, GraphMetrics.getNumberOfTriangles(graph));
        Assert.assertEquals(2, naiveCountTriangles(graph));

        graph.removeAllVertices(new HashSet<>(graph.vertexSet()));
        gen.generateGoldnerHararyGraph(graph);

        Assert.assertEquals(25, GraphMetrics.getNumberOfTriangles(graph));
        Assert.assertEquals(25, naiveCountTriangles(graph));

        graph.removeAllVertices(new HashSet<>(graph.vertexSet()));
        gen.generateKlein7RegularGraph(graph);

        Assert.assertEquals(56, GraphMetrics.getNumberOfTriangles(graph));
        Assert.assertEquals(56, naiveCountTriangles(graph));
    }

    @Test
    public void testCountTriangles4()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        for (int i = 0; i < 25; i++) {
            g.addVertex(i);
        }

        int[][] edges = { { 0, 1 }, { 1, 2 }, { 0, 3 }, { 1, 3 }, { 2, 5 }, { 3, 5 }, { 4, 5 },
            { 1, 6 }, { 2, 6 }, { 1, 7 }, { 2, 7 }, { 3, 7 }, { 4, 7 }, { 1, 8 }, { 2, 8 },
            { 2, 9 }, { 1, 10 }, { 7, 10 }, { 1, 11 }, { 2, 11 }, { 2, 12 }, { 3, 13 }, { 4, 13 },
            { 1, 15 }, { 6, 15 }, { 9, 15 }, { 1, 16 }, { 4, 16 }, { 11, 16 }, { 1, 18 }, { 2, 18 },
            { 1, 19 }, { 3, 19 }, { 6, 19 }, { 1, 20 }, { 2, 20 }, { 2, 21 }, { 3, 21 }, { 3, 22 },
            { 5, 22 }, { 10, 22 }, { 3, 23 }, { 19, 23 }, { 1, 24 }, { 2, 24 } };

        for (int[] e : edges) {
            g.addEdge(e[0], e[1]);
        }

        long t1 = GraphMetrics.getNumberOfTriangles(g);
        List<Integer> allVertices = new ArrayList<>(g.vertexSet());
        long t2 = GraphMetrics.naiveCountTriangles(g, allVertices);

        assertEquals(t1, t2);
    }

    @Test
    public void testMultipleEdges()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        int[][] edges = { { 0, 1 }, { 1, 2 }, { 2, 0 }, { 1, 3 }, { 2, 3 }, { 2, 1 } };
        for (int[] e : edges) {
            Graphs.addEdgeWithVertices(g, e[0], e[1]);
        }
        assertEquals(4, GraphMetrics.getNumberOfTriangles(g));
    }

    @Test
    public void testMultipleEdges2()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        int[][] edges =
            { { 0, 1 }, { 1, 2 }, { 2, 0 }, { 1, 3 }, { 2, 3 }, { 2, 1 }, { 0, 2 }, { 0, 2 } };
        for (int[] e : edges) {
            Graphs.addEdgeWithVertices(g, e[0], e[1]);
        }
        assertEquals(8, GraphMetrics.getNumberOfTriangles(g));
    }

}
