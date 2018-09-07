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
package org.jgrapht.alg.matching;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for GreedyMaximumCardinalityMatching
 * 
 * @author Joris Kinable
 */
public class GreedyMaximumCardinalityMatchingTest
{

    /**
     * Generate a number of random graphs, find a random matching and check whether the matching
     * returned is valid. Not sorted
     */
    @Test
    public void testRandomGraphs()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> generator =
            new GnmRandomGraphGenerator<>(200, 120);
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        for (int i = 0; i < 100; i++) {
            generator.generateGraph(graph);
            MatchingAlgorithm<Integer, DefaultEdge> matcher =
                new GreedyMaximumCardinalityMatching<>(graph, false);
            MatchingAlgorithm.Matching<Integer, DefaultEdge> m = matcher.getMatching();

            Set<Integer> matched = new HashSet<>();
            double weight = 0;
            for (DefaultEdge e : m.getEdges()) {
                Integer source = graph.getEdgeSource(e);
                Integer target = graph.getEdgeTarget(e);
                if (matched.contains(source))
                    fail("vertex is incident to multiple matches in the matching");
                matched.add(source);
                if (matched.contains(target))
                    fail("vertex is incident to multiple matches in the matching");
                matched.add(target);
                weight += graph.getEdgeWeight(e);
            }
            assertEquals(m.getWeight(), weight, 0.0000001);
        }
    }

    /**
     * Generate a number of random graphs, find a random matching and check whether the matching
     * returned is valid. Sorted.
     */
    @Test
    public void testRandomGraphs2()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> generator =
            new GnmRandomGraphGenerator<>(200, 120);
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        for (int i = 0; i < 1; i++) {
            generator.generateGraph(graph);
            MatchingAlgorithm<Integer, DefaultEdge> matcher =
                new GreedyMaximumCardinalityMatching<>(graph, true);
            MatchingAlgorithm.Matching<Integer, DefaultEdge> m = matcher.getMatching();

            Set<Integer> matched = new HashSet<>();
            double weight = 0;
            for (DefaultEdge e : m.getEdges()) {
                Integer source = graph.getEdgeSource(e);
                Integer target = graph.getEdgeTarget(e);
                if (matched.contains(source))
                    fail("vertex is incident to multiple matches in the matching");
                matched.add(source);
                if (matched.contains(target))
                    fail("vertex is incident to multiple matches in the matching");
                matched.add(target);
                weight += graph.getEdgeWeight(e);
            }
            assertEquals(m.getWeight(), weight, 0.0000001);
        }
    }

}
