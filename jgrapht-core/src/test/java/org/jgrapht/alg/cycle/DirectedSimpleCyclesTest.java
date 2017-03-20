/*
 * (C) Copyright 2013-2017, by Nikolay Ognyanov
 and Contributors.
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

package org.jgrapht.alg.cycle;

import static org.junit.Assert.assertTrue;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

public class DirectedSimpleCyclesTest
{
    private static int MAX_SIZE = 9;
    private static int[] RESULTS = { 0, 1, 3, 8, 24, 89, 415, 2372, 16072, 125673 };

    @Test
    public void test()
    {
        TiernanSimpleCycles<Integer, DefaultEdge> tiernanFinder = new TiernanSimpleCycles<>();
        TarjanSimpleCycles<Integer, DefaultEdge> tarjanFinder = new TarjanSimpleCycles<>();
        JohnsonSimpleCycles<Integer, DefaultEdge> johnsonFinder = new JohnsonSimpleCycles<>();
        SzwarcfiterLauerSimpleCycles<Integer, DefaultEdge> szwarcfiterLauerFinder =
            new SzwarcfiterLauerSimpleCycles<>();
        HawickJamesSimpleCycles<Integer, DefaultEdge> hawickJamesFinder =
            new HawickJamesSimpleCycles<>();

        testAlgorithm(tiernanFinder);
        testAlgorithm(tarjanFinder);
        testAlgorithm(johnsonFinder);
        testAlgorithm(szwarcfiterLauerFinder);
        testAlgorithm(hawickJamesFinder);
    }

    private void testAlgorithm(Cycle<Integer, DefaultEdge> finder)
    {
        Graph<Integer, DefaultEdge> graph =
            new DefaultDirectedGraph<>(new ClassBasedEdgeFactory<>(DefaultEdge.class));
        for (int i = 0; i < 7; i++) {
            graph.addVertex(i);
        }
        finder.setGraph(graph);
        graph.addEdge(0, 0);
        checkResult(finder, 1);
        graph.addEdge(1, 1);
        checkResult(finder, 2);
        graph.addEdge(0, 1);
        graph.addEdge(1, 0);
        checkResult(finder, 3);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);
        checkResult(finder, 4);
        graph.addEdge(6, 6);
        checkResult(finder, 5);

        for (int size = 1; size <= MAX_SIZE; size++) {
            graph = new DefaultDirectedGraph<>(new ClassBasedEdgeFactory<>(DefaultEdge.class));
            for (int i = 0; i < size; i++) {
                graph.addVertex(i);
            }
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    graph.addEdge(i, j);
                }
            }
            finder.setGraph(graph);
            checkResult(finder, RESULTS[size]);
        }
    }

    private void checkResult(Cycle<Integer, DefaultEdge> finder, int size)
    {
        assertTrue(finder.findCycle().size() == size);
    }
}
