/*
 * (C) Copyright 2018-2018, by CAE Tech Limited and Contributors.
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
package org.jgrapht.alg.connectivity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.generate.GnmRandomBipartiteGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Test for Dulmage-Mendelsohn, based on
 * MaximumCardinailityBipartiteMatchingTest
 *
 * @author Peter Harman
 * @author Joris Kinable
 */
public class DulmageMendelsohnDecompositionTest {

    /**
     * Random test graph 1
     */
    @Test
    public void testBipartiteMatching1() {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<Integer> partition1 = Arrays.asList(0, 1, 2, 3);
        List<Integer> partition2 = Arrays.asList(4, 5, 6, 7);
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);

        DefaultEdge e02 = graph.addEdge(partition1.get(0), partition2.get(2));
        DefaultEdge e11 = graph.addEdge(partition1.get(1), partition2.get(1));
        DefaultEdge e20 = graph.addEdge(partition1.get(2), partition2.get(0));

        DulmageMendelsohnDecomposition<Integer, DefaultEdge> dm = new DulmageMendelsohnDecomposition<>(
                graph, new HashSet<>(partition1), new HashSet<>(partition2));
        DulmageMendelsohnDecomposition.Decomposition<Integer, DefaultEdge> course = dm.getDecomposition(false);
        assertTrue(course.getPartition1DominatedSet().size() + course.getPartition2DominatedSet().size() + course.getPerfectMatchedSets().get(0).size() == graph.vertexSet().size());
        DulmageMendelsohnDecomposition.Decomposition<Integer, DefaultEdge> fine = dm.getDecomposition(true);
        assertTrue(fine.getPartition1DominatedSet().size() + fine.getPartition2DominatedSet().size() + count(fine.getPerfectMatchedSets()) == graph.vertexSet().size());

    }

    /**
     * Random test graph 2
     */
    @Test
    public void testBipartiteMatching2() {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<Integer> partition1 = Arrays.asList(0, 1, 2, 3, 4, 5);
        List<Integer> partition2 = Arrays.asList(6, 7, 8, 9, 10, 11);
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);

        DefaultEdge e00 = graph.addEdge(partition1.get(0), partition2.get(0));
        DefaultEdge e13 = graph.addEdge(partition1.get(1), partition2.get(3));
        DefaultEdge e21 = graph.addEdge(partition1.get(2), partition2.get(1));
        DefaultEdge e34 = graph.addEdge(partition1.get(3), partition2.get(4));
        DefaultEdge e42 = graph.addEdge(partition1.get(4), partition2.get(2));
        DefaultEdge e55 = graph.addEdge(partition1.get(5), partition2.get(5));

        DulmageMendelsohnDecomposition<Integer, DefaultEdge> dm = new DulmageMendelsohnDecomposition<>(
                graph, new HashSet<>(partition1), new HashSet<>(partition2));
        DulmageMendelsohnDecomposition.Decomposition<Integer, DefaultEdge> course = dm.getDecomposition(false);
        assertTrue(course.getPartition1DominatedSet().size() + course.getPartition2DominatedSet().size() + course.getPerfectMatchedSets().get(0).size() == graph.vertexSet().size());
        DulmageMendelsohnDecomposition.Decomposition<Integer, DefaultEdge> fine = dm.getDecomposition(true);
        assertTrue(fine.getPartition1DominatedSet().size() + fine.getPartition2DominatedSet().size() + count(fine.getPerfectMatchedSets()) == graph.vertexSet().size());

    }

    /**
     * Find a maximum matching on a graph without edges
     */
    @Test
    public void testEmptyMatching() {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<Integer> partition1 = Collections.singletonList(0);
        List<Integer> partition2 = Collections.singletonList(1);
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);
        DulmageMendelsohnDecomposition<Integer, DefaultEdge> dm = new DulmageMendelsohnDecomposition<>(
                graph, new HashSet<>(partition1), new HashSet<>(partition2));
        DulmageMendelsohnDecomposition.Decomposition<Integer, DefaultEdge> course = dm.getDecomposition(false);
        assertTrue(course.getPartition1DominatedSet().size() + course.getPartition2DominatedSet().size() + course.getPerfectMatchedSets().get(0).size() == graph.vertexSet().size());
        DulmageMendelsohnDecomposition.Decomposition<Integer, DefaultEdge> fine = dm.getDecomposition(true);
        assertTrue(fine.getPartition1DominatedSet().size() + fine.getPartition2DominatedSet().size() + count(fine.getPerfectMatchedSets()) == graph.vertexSet().size());

    }

    @Test
    public void testGraph1() {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Set<Integer> partition1 = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6));
        Set<Integer> partition2 = new HashSet<>(Arrays.asList(7, 8, 9));
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);
        int[][] edges = {{5, 8}, {4, 9}, {2, 7}, {6, 9}, {1, 9}};
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }

        DulmageMendelsohnDecomposition<Integer, DefaultEdge> dm = new DulmageMendelsohnDecomposition<>(
                graph, partition1, partition2);
        DulmageMendelsohnDecomposition.Decomposition<Integer, DefaultEdge> course = dm.getDecomposition(false);
        assertTrue(course.getPartition1DominatedSet().size() + course.getPartition2DominatedSet().size() + course.getPerfectMatchedSets().get(0).size() == graph.vertexSet().size());
        DulmageMendelsohnDecomposition.Decomposition<Integer, DefaultEdge> fine = dm.getDecomposition(true);
        assertTrue(fine.getPartition1DominatedSet().size() + fine.getPartition2DominatedSet().size() + count(fine.getPerfectMatchedSets()) == graph.vertexSet().size());

    }

    @Test
    public void testGraph2() {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Set<Integer> partition1 = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6));
        Set<Integer> partition2 = new HashSet<>(Arrays.asList(7, 8, 9));
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);
        int[][] edges
                = {{5, 8}, {4, 9}, {2, 7}, {6, 9}, {1, 9}, {0, 8}, {3, 7}, {1, 7}};
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }

        DulmageMendelsohnDecomposition<Integer, DefaultEdge> dm = new DulmageMendelsohnDecomposition<>(
                graph, partition1, partition2);
        DulmageMendelsohnDecomposition.Decomposition<Integer, DefaultEdge> course = dm.getDecomposition(false);
        assertTrue(course.getPartition1DominatedSet().size() + course.getPartition2DominatedSet().size() + course.getPerfectMatchedSets().get(0).size() == graph.vertexSet().size());
        DulmageMendelsohnDecomposition.Decomposition<Integer, DefaultEdge> fine = dm.getDecomposition(true);
        assertTrue(fine.getPartition1DominatedSet().size() + fine.getPartition2DominatedSet().size() + count(fine.getPerfectMatchedSets()) == graph.vertexSet().size());

    }

    /**
     * Issue 233 instance
     */
    @Test
    public void testBipartiteMatchingIssue233() {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        Graphs.addAllVertices(g, IntStream.rangeClosed(0, 3).boxed().collect(Collectors.toList()));

        Set<Integer> left = new HashSet<>(Arrays.asList(0, 1));
        Set<Integer> right = new HashSet<>(Arrays.asList(2, 3));

        g.addEdge(0, 2);
        g.addEdge(0, 3);
        g.addEdge(1, 2);

        DulmageMendelsohnDecomposition<Integer, DefaultEdge> dm = new DulmageMendelsohnDecomposition<>(
                g, left, right);
        DulmageMendelsohnDecomposition.Decomposition<Integer, DefaultEdge> course = dm.getDecomposition(false);
        assertTrue(course.getPartition1DominatedSet().size() + course.getPartition2DominatedSet().size() + course.getPerfectMatchedSets().get(0).size() == g.vertexSet().size());
        DulmageMendelsohnDecomposition.Decomposition<Integer, DefaultEdge> fine = dm.getDecomposition(true);
        assertTrue(fine.getPartition1DominatedSet().size() + fine.getPartition2DominatedSet().size() + count(fine.getPerfectMatchedSets()) == g.vertexSet().size());
    }

    @Test
    public void testRandomBipartiteGraphs() {
        Random random = new Random(1);
        int vertices = 100;

        for (int k = 0; k < 100; k++) {
            int edges = random.nextInt(maxEdges(vertices) / 2);
            GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge> generator
                    = new GnmRandomBipartiteGraphGenerator<>(vertices, vertices, edges, 0);
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            generator.generateGraph(graph);
            DulmageMendelsohnDecomposition<Integer, DefaultEdge> dm = new DulmageMendelsohnDecomposition<>(
                    graph, generator.getFirstPartition(), generator.getSecondPartition());
            DulmageMendelsohnDecomposition.Decomposition<Integer, DefaultEdge> course = dm.getDecomposition(false);
            assertTrue(course.getPartition1DominatedSet().size() + course.getPartition2DominatedSet().size() + course.getPerfectMatchedSets().get(0).size() == vertices * 2);
            DulmageMendelsohnDecomposition.Decomposition<Integer, DefaultEdge> fine = dm.getDecomposition(true);
            assertTrue(fine.getPartition1DominatedSet().size() + fine.getPartition2DominatedSet().size() + count(fine.getPerfectMatchedSets()) == vertices * 2);
        }
    }

    private static <V> int count(List<Set<V>> sets) {
        int n = 0;
        n = sets.stream().map((set) -> set.size()).reduce(n, Integer::sum);
        return n;
    }

    private static int maxEdges(int n) {
        if (n % 2 == 0) {
            return Math.multiplyExact(n / 2, n - 1);
        } else {
            return Math.multiplyExact(n, (n - 1) / 2);
        }
    }
}
