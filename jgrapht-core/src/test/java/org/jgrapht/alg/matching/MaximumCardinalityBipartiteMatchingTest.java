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
package org.jgrapht.alg.matching;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.stream.*;

import static org.junit.Assert.*;

/**
 * Test class for maximum cardinality bipartite matching algorithms
 * 
 * @author Joris Kinable
 */
public abstract class MaximumCardinalityBipartiteMatchingTest
{

    public abstract MatchingAlgorithm<Integer, DefaultEdge> getMatchingAlgorithm(
        Graph<Integer, DefaultEdge> graph, Set<Integer> partition1, Set<Integer> partition2);

    /**
     * Random test graph 1
     */
    @Test
    public void testBipartiteMatching1()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<Integer> partition1 = Arrays.asList(0, 1, 2, 3);
        List<Integer> partition2 = Arrays.asList(4, 5, 6, 7);
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);

        DefaultEdge e02 = graph.addEdge(partition1.get(0), partition2.get(2));
        DefaultEdge e11 = graph.addEdge(partition1.get(1), partition2.get(1));
        DefaultEdge e20 = graph.addEdge(partition1.get(2), partition2.get(0));

        MatchingAlgorithm<Integer, DefaultEdge> bm =
            getMatchingAlgorithm(graph, new HashSet<>(partition1), new HashSet<>(partition2));
        List<DefaultEdge> l1 = Arrays.asList(e11, e02, e20);
        Set<DefaultEdge> matching = new HashSet<>(l1);
        MatchingAlgorithm.Matching<Integer, DefaultEdge> bmMatching = bm.getMatching();
        assertEquals(3, bmMatching.getEdges().size(), 0);
        assertEquals(matching, bmMatching.getEdges());
    }

    /**
     * Random test graph 2
     */
    @Test
    public void testBipartiteMatching2()
    {
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

        MatchingAlgorithm<Integer, DefaultEdge> bm =
            getMatchingAlgorithm(graph, new HashSet<>(partition1), new HashSet<>(partition2));
        MatchingAlgorithm.Matching<Integer, DefaultEdge> bmMatching = bm.getMatching();
        assertEquals(6, bmMatching.getEdges().size(), 0);
        List<DefaultEdge> l1 = Arrays.asList(e21, e13, e00, e42, e34, e55);
        Set<DefaultEdge> matching = new HashSet<>(l1);
        assertEquals(matching, bmMatching.getEdges());
    }

    /**
     * Find a maximum matching on a graph without edges
     */
    @Test
    public void testEmptyMatching()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<Integer> partition1 = Collections.singletonList(0);
        List<Integer> partition2 = Collections.singletonList(1);
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);
        MatchingAlgorithm<Integer, DefaultEdge> bm =
            getMatchingAlgorithm(graph, new HashSet<>(partition1), new HashSet<>(partition2));
        MatchingAlgorithm.Matching<Integer, DefaultEdge> bmMatching = bm.getMatching();
        assertEquals(Collections.EMPTY_SET, bmMatching.getEdges());
    }

    @Test
    public void testGraph1()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Set<Integer> partition1 = Set.of(0, 1, 2, 3, 4, 5, 6);
        Set<Integer> partition2 = Set.of(7, 8, 9);
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);
        int[][] edges = { { 5, 8 }, { 4, 9 }, { 2, 7 }, { 6, 9 }, { 1, 9 } };
        for (int[] edge : edges)
            graph.addEdge(edge[0], edge[1]);

        MatchingAlgorithm<Integer, DefaultEdge> matcher =
            getMatchingAlgorithm(graph, partition1, partition2);
        MatchingAlgorithm.Matching<Integer, DefaultEdge> matching = matcher.getMatching();
        assertEquals(3, matching.getEdges().size());
    }

    @Test
    public void testGraph2()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Set<Integer> partition1 = Set.of(0, 1, 2, 3, 4, 5, 6);
        Set<Integer> partition2 = Set.of(7, 8, 9);
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);
        int[][] edges =
            { { 5, 8 }, { 4, 9 }, { 2, 7 }, { 6, 9 }, { 1, 9 }, { 0, 8 }, { 3, 7 }, { 1, 7 } };
        for (int[] edge : edges)
            graph.addEdge(edge[0], edge[1]);

        MatchingAlgorithm<Integer, DefaultEdge> matcher =
            getMatchingAlgorithm(graph, partition1, partition2);
        MatchingAlgorithm.Matching<Integer, DefaultEdge> matching = matcher.getMatching();
        this.verifyMatching(graph, matching, matching.getEdges().size());
    }

    /**
     * Issue 233 instance
     */
    @Test
    public void testBipartiteMatchingIssue233()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        Graphs.addAllVertices(g, IntStream.rangeClosed(0, 3).boxed().collect(Collectors.toList()));

        Set<Integer> left = Set.of(0, 1);
        Set<Integer> right = Set.of(2, 3);

        g.addEdge(0, 2);
        g.addEdge(0, 3);
        g.addEdge(1, 2);

        MatchingAlgorithm.Matching<Integer, DefaultEdge> m =
            getMatchingAlgorithm(g, left, right).getMatching();
        assertTrue(m.getEdges().contains(g.getEdge(1, 2)));
        assertTrue(m.getEdges().contains(g.getEdge(0, 3)));
        assertEquals(2, m.getEdges().size());
    }

    @Test
    public void testPseudoGraph()
    {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        Set<Integer> partition1 = Set.of(0, 1, 2);
        Set<Integer> partition2 = Set.of(3, 4, 5);
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);
        int[][] edges = { { 0, 3 }, { 1, 4 }, { 2, 5 }, { 0, 3 }, { 0, 0 } };
        for (int[] edge : edges)
            graph.addEdge(edge[0], edge[1]);

        MatchingAlgorithm<Integer, DefaultEdge> matcher =
            getMatchingAlgorithm(graph, partition1, partition2);
        MatchingAlgorithm.Matching<Integer, DefaultEdge> matching = matcher.getMatching();
        this.verifyMatching(graph, matching, 3);
    }

    @Test
    public void testRandomBipartiteGraphs()
    {
        Random random = new Random(1);
        int vertices = 100;

        for (int k = 0; k < 100; k++) {
            int edges = random.nextInt(maxEdges(vertices) / 2);
            GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge> generator =
                new GnmRandomBipartiteGraphGenerator<>(vertices, vertices / 2, edges, 0);

            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            generator.generateGraph(graph);

            MatchingAlgorithm<Integer, DefaultEdge> matcher = getMatchingAlgorithm(
                graph, generator.getFirstPartition(), generator.getSecondPartition());
            MatchingAlgorithm.Matching<Integer, DefaultEdge> m = matcher.getMatching();
            this.verifyMatching(graph, m, m.getEdges().size());
        }
    }

    private <V,
        E> void verifyMatching(Graph<V, E> g, MatchingAlgorithm.Matching<V, E> m, int cardinality)
    {
        Set<V> matched = new HashSet<>();
        double weight = 0;
        for (E e : m.getEdges()) {
            V source = g.getEdgeSource(e);
            V target = g.getEdgeTarget(e);
            if (matched.contains(source))
                fail("vertex is incident to multiple matches in the matching");
            matched.add(source);
            if (matched.contains(target))
                fail("vertex is incident to multiple matches in the matching");
            matched.add(target);
            weight += g.getEdgeWeight(e);
        }
        assertEquals(m.getWeight(), weight, 0.0000001);
        assertEquals(cardinality, m.getEdges().size());
        assertEquals(m.getEdges().size() * 2, matched.size()); // Ensure that there are no
                                                               // self-loops

        DenseEdmondsMaximumCardinalityMatching<V, E> matcher =
            new DenseEdmondsMaximumCardinalityMatching<>(g);
        assertTrue(matcher.isMaximumMatching(m)); // Certify that the matching is indeed maximum
    }

    private static int maxEdges(int n)
    {
        if (n % 2 == 0) {
            return Math.multiplyExact(n / 2, n - 1);
        } else {
            return Math.multiplyExact(n, (n - 1) / 2);
        }
    }

}
