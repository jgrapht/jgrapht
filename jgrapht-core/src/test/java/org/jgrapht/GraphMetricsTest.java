/*
 * (C) Copyright 2017-2018, by Joris Kinable and Contributors.
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

import org.jgrapht.alg.cycle.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

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
}
