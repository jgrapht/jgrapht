/*
 * (C) Copyright 2015-2021, by Graeme Ahokas and Contributors.
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
import org.jgrapht.alg.interfaces.MatchingAlgorithm.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;
import org.junit.experimental.categories.*;

import java.math.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MaximumWeightBipartiteMatchingTest
{

    private SimpleWeightedGraph<String, DefaultWeightedEdge> graph;
    private Set<String> partition1;
    private Set<String> partition2;

    private MaximumWeightBipartiteMatching<String, DefaultWeightedEdge> matcher;

    @Before
    public void setUpGraph()
    {
        graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex("s1");
        graph.addVertex("s2");
        graph.addVertex("s3");
        graph.addVertex("s4");
        graph.addVertex("t1");
        graph.addVertex("t2");
        graph.addVertex("t3");
        graph.addVertex("t4");

        partition1 = new HashSet<>();
        partition1.add("s1");
        partition1.add("s2");
        partition1.add("s3");
        partition1.add("s4");

        partition2 = new HashSet<>();
        partition2.add("t1");
        partition2.add("t2");
        partition2.add("t3");
        partition2.add("t4");
    }

    @Test
    public void maximumWeightBipartiteMatching1()
    {
        DefaultWeightedEdge e1 = graph.addEdge("s1", "t1");
        graph.setEdgeWeight(e1, 1);
        matcher = new MaximumWeightBipartiteMatching<>(graph, partition1, partition2);
        Matching<String, DefaultWeightedEdge> matchings = matcher.getMatching();
        assertEquals(1, matchings.getEdges().size());
        assertTrue(matchings.getEdges().contains(e1));
    }

    @Test
    public void maximumWeightBipartiteMatching2()
    {
        DefaultWeightedEdge e1 = graph.addEdge("s1", "t1");
        graph.setEdgeWeight(e1, 1);
        DefaultWeightedEdge e2 = graph.addEdge("s2", "t1");
        graph.setEdgeWeight(e2, 2);

        matcher = new MaximumWeightBipartiteMatching<>(graph, partition1, partition2);
        Matching<String, DefaultWeightedEdge> matchings = matcher.getMatching();
        assertEquals(1, matchings.getEdges().size());
        assertTrue(matchings.getEdges().contains(e2));
    }

    @Test
    public void maximumWeightBipartiteMatching3()
    {
        DefaultWeightedEdge e1 = graph.addEdge("s1", "t1");
        graph.setEdgeWeight(e1, 2);
        DefaultWeightedEdge e2 = graph.addEdge("s1", "t2");
        graph.setEdgeWeight(e2, 1);
        DefaultWeightedEdge e3 = graph.addEdge("s2", "t1");
        graph.setEdgeWeight(e3, 2);

        matcher = new MaximumWeightBipartiteMatching<>(graph, partition1, partition2);
        Matching<String, DefaultWeightedEdge> matchings = matcher.getMatching();
        assertEquals(2, matchings.getEdges().size());
        assertTrue(matchings.getEdges().contains(e2));
        assertTrue(matchings.getEdges().contains(e3));
    }

    @Test
    public void maximumWeightBipartiteMatching4()
    {
        DefaultWeightedEdge e1 = graph.addEdge("s1", "t1");
        graph.setEdgeWeight(e1, 1);
        DefaultWeightedEdge e2 = graph.addEdge("s1", "t2");
        graph.setEdgeWeight(e2, 1);
        DefaultWeightedEdge e3 = graph.addEdge("s2", "t2");
        graph.setEdgeWeight(e3, 1);

        matcher = new MaximumWeightBipartiteMatching<>(graph, partition1, partition2);
        Matching<String, DefaultWeightedEdge> matchings = matcher.getMatching();
        assertEquals(2, matchings.getEdges().size());
        assertTrue(matchings.getEdges().contains(e1));
        assertTrue(matchings.getEdges().contains(e3));
    }

    @Test
    public void maximumWeightBipartiteMatching5()
    {
        DefaultWeightedEdge e1 = graph.addEdge("s1", "t1");
        graph.setEdgeWeight(e1, 1);
        DefaultWeightedEdge e2 = graph.addEdge("s1", "t2");
        graph.setEdgeWeight(e2, 2);
        DefaultWeightedEdge e3 = graph.addEdge("s2", "t2");
        graph.setEdgeWeight(e3, 2);
        DefaultWeightedEdge e4 = graph.addEdge("s3", "t2");
        graph.setEdgeWeight(e4, 2);
        DefaultWeightedEdge e5 = graph.addEdge("s3", "t3");
        graph.setEdgeWeight(e5, 1);
        DefaultWeightedEdge e6 = graph.addEdge("s4", "t1");
        graph.setEdgeWeight(e6, 1);
        DefaultWeightedEdge e7 = graph.addEdge("s4", "t4");
        graph.setEdgeWeight(e7, 1);

        matcher = new MaximumWeightBipartiteMatching<>(graph, partition1, partition2);
        Matching<String, DefaultWeightedEdge> matchings = matcher.getMatching();
        assertEquals(4, matchings.getEdges().size());
        assertTrue(matchings.getEdges().contains(e1));
        assertTrue(matchings.getEdges().contains(e3));
        assertTrue(matchings.getEdges().contains(e5));
        assertTrue(matchings.getEdges().contains(e7));
        assertEquals(5d, matchings.getWeight(), 1e-9);
    }

    @Test
    @Category(SlowTests.class)
    public void testRandomInstancesFixedSeed()
    {
        testRandomInstance(new Random(17), 100, 0.7, 2);
    }

    @Test
    @Category(SlowTests.class)
    public void testRandomInstances()
    {
        Random rng = new Random();
        testRandomInstance(rng, 100, 0.8, 1);
        testRandomInstance(rng, 1000, 0.8, 1);
    }

    private void testRandomInstance(Random rng, int n, double p, int repeat)
    {
        for (int a = 0; a < repeat; a++) {
            // generate random bipartite
            Graph<Integer, DefaultWeightedEdge> g =
                new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

            Set<Integer> partitionA = CollectionUtil.newLinkedHashSetWithExpectedSize(n);
            for (int i = 0; i < n; i++) {
                g.addVertex(i);
                partitionA.add(i);
            }

            Set<Integer> partitionB = CollectionUtil.newLinkedHashSetWithExpectedSize(n);
            for (int i = 0; i < n; i++) {
                g.addVertex(n + i);
                partitionB.add(n + i);
            }

            // create edges
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    // s->t
                    if (rng.nextDouble() < p) {
                        g.addEdge(i, n + j);
                    }
                }
            }

            // assign random weights
            for (DefaultWeightedEdge e : g.edgeSet()) {
                g.setEdgeWeight(e, 1000 * rng.nextInt());
            }

            // compute maximum weight matching
            MaximumWeightBipartiteMatching<Integer, DefaultWeightedEdge> alg =
                new MaximumWeightBipartiteMatching<>(g, partitionA, partitionB);
            Matching<Integer, DefaultWeightedEdge> matching = alg.getMatching();
            Map<Integer, BigDecimal> pot = alg.getPotentials();
            Comparator<BigDecimal> comparator = Comparator.<BigDecimal> naturalOrder();

            // assert matching
            Map<Integer, Integer> degree = new HashMap<>();
            for (Integer v : g.vertexSet()) {
                degree.put(v, 0);
            }
            for (DefaultWeightedEdge e : matching.getEdges()) {
                Integer s = g.getEdgeSource(e);
                Integer t = g.getEdgeTarget(e);
                degree.put(s, degree.get(s) + 1);
                degree.put(t, degree.get(t) + 1);
            }
            for (Integer v : g.vertexSet()) {
                assertTrue(degree.get(v) <= 1);
            }

            // assert non-negative potentials
            for (Integer v : g.vertexSet()) {
                assertTrue(comparator.compare(pot.get(v), BigDecimal.ZERO) >= 0);
            }

            // assert non-negative reduced cost for edges
            for (DefaultWeightedEdge e : g.edgeSet()) {
                Integer s = g.getEdgeSource(e);
                Integer t = g.getEdgeTarget(e);
                BigDecimal w = BigDecimal.valueOf(g.getEdgeWeight(e));
                assertTrue(comparator.compare(w, pot.get(s).add(pot.get(t))) <= 0);
            }

            // assert tight edges in matching
            for (DefaultWeightedEdge e : matching.getEdges()) {
                Integer s = g.getEdgeSource(e);
                Integer t = g.getEdgeTarget(e);
                BigDecimal w = BigDecimal.valueOf(g.getEdgeWeight(e));
                assertTrue(comparator.compare(w, pot.get(s).add(pot.get(t))) == 0);
            }

            // assert free nodes have zero potential
            for (Integer v : g.vertexSet()) {
                if (degree.get(v) == 0) {
                    assertEquals(pot.get(v), BigDecimal.ZERO);
                }
            }

        }
    }

}
