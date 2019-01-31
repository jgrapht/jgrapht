/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.color;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static org.junit.Assert.*;

/**
 * Base class for coloring tests.
 * 
 * @author Dimitrios Michail
 */
public abstract class BaseColoringTest
{

    public BaseColoringTest()
    {
        super();
    }

    protected abstract VertexColoringAlgorithm<Integer> getAlgorithm(
        Graph<Integer, DefaultEdge> graph);

    protected abstract int getExpectedResultOnDSaturNonOptimalGraph();

    protected int getExpectedResultOnGraph1()
    {
        return 3;
    }

    protected int getExpectedResultOnMyceil3Graph()
    {
        return 4;
    }

    protected int getExpectedResultOnMyceil4Graph()
    {
        return 5;
    }

    protected void assertColoring(
        Graph<Integer, DefaultEdge> g, Coloring<Integer> coloring, int expectedColors)
    {
        int n = g.vertexSet().size();
        assertTrue(coloring.getNumberColors() <= n);
        assertEquals(expectedColors, coloring.getNumberColors());
        Map<Integer, Integer> colors = coloring.getColors();

        for (Integer v : g.vertexSet()) {
            Integer c = colors.get(v);
            assertNotNull(c);
            assertTrue(c >= 0);
            assertTrue(c < n);
        }

        for (DefaultEdge e : g.edgeSet()) {
            assertNotEquals(colors.get(g.getEdgeSource(e)), colors.get(g.getEdgeTarget(e)));
        }
    }

    protected void testRandomGraphColoring(Random rng)
    {
        final int tests = 5;
        final int n = 20;
        final double p = 0.35;

        List<Function<Graph<Integer, DefaultEdge>, VertexColoringAlgorithm<Integer>>> algs =
            new ArrayList<>();
        algs.add((g) -> getAlgorithm(g));

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnpRandomGraphGenerator<>(n, p, rng, false);

        for (int i = 0; i < tests; i++) {
            Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            gen.generateGraph(g);

            for (Function<Graph<Integer, DefaultEdge>,
                VertexColoringAlgorithm<Integer>> algProvider : algs)
            {
                VertexColoringAlgorithm<Integer> alg = algProvider.apply(g);
                Coloring<Integer> coloring = alg.getColoring();
                assertTrue(coloring.getNumberColors() <= n);
                Map<Integer, Integer> colors = coloring.getColors();

                for (Integer v : g.vertexSet()) {
                    Integer c = colors.get(v);
                    assertNotNull(c);
                    assertTrue(c >= 0);
                    assertTrue(c < n);
                }

                for (DefaultEdge e : g.edgeSet()) {
                    assertNotEquals(colors.get(g.getEdgeSource(e)), colors.get(g.getEdgeTarget(e)));
                }
            }
        }
    }

    final protected Graph<Integer, DefaultEdge> createGraph1()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5));
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 3);
        g.addEdge(1, 4);
        g.addEdge(1, 5);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(3, 5);
        return g;
    }

    final protected Graph<Integer, DefaultEdge> createMyciel3Graph()
    {
        // This is a graph from http://mat.gsia.cmu.edu/COLOR/instances/myciel3.col.
        // SOURCE: Michael Trick (trick@cmu.edu)
        // DESCRIPTION: Graph based on Mycielski transformation.
        // Triangle free (clique number 2) but increasing coloring number
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, IntStream.range(1, 12).boxed().collect(Collectors.toList()));

        g.addEdge(1, 2);
        g.addEdge(1, 4);
        g.addEdge(1, 7);
        g.addEdge(1, 9);
        g.addEdge(2, 3);
        g.addEdge(2, 6);
        g.addEdge(2, 8);
        g.addEdge(3, 5);
        g.addEdge(3, 7);
        g.addEdge(3, 10);
        g.addEdge(4, 5);
        g.addEdge(4, 6);
        g.addEdge(4, 10);
        g.addEdge(5, 8);
        g.addEdge(5, 9);
        g.addEdge(6, 11);
        g.addEdge(7, 11);
        g.addEdge(8, 11);
        g.addEdge(9, 11);
        g.addEdge(10, 11);

        return g;
    }

    final protected Graph<Integer, DefaultEdge> createMyciel4Graph()
    {
        // This is a graph from http://mat.gsia.cmu.edu/COLOR/instances/myciel4.col.
        // SOURCE: Michael Trick (trick@cmu.edu)
        // DESCRIPTION: Graph based on Mycielski transformation.
        // Triangle free (clique number 2) but increasing coloring number
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, IntStream.range(1, 24).boxed().collect(Collectors.toList()));

        g.addEdge(1, 2);
        g.addEdge(1, 4);
        g.addEdge(1, 7);
        g.addEdge(1, 9);
        g.addEdge(1, 13);
        g.addEdge(1, 15);
        g.addEdge(1, 18);
        g.addEdge(1, 20);
        g.addEdge(2, 3);
        g.addEdge(2, 6);
        g.addEdge(2, 8);
        g.addEdge(2, 12);
        g.addEdge(2, 14);
        g.addEdge(2, 17);
        g.addEdge(2, 19);
        g.addEdge(3, 5);
        g.addEdge(3, 7);
        g.addEdge(3, 10);
        g.addEdge(3, 13);
        g.addEdge(3, 16);
        g.addEdge(3, 18);
        g.addEdge(3, 21);
        g.addEdge(4, 5);
        g.addEdge(4, 6);
        g.addEdge(4, 10);
        g.addEdge(4, 12);
        g.addEdge(4, 16);
        g.addEdge(4, 17);
        g.addEdge(4, 21);
        g.addEdge(5, 8);
        g.addEdge(5, 9);
        g.addEdge(5, 14);
        g.addEdge(5, 15);
        g.addEdge(5, 19);
        g.addEdge(5, 20);
        g.addEdge(6, 11);
        g.addEdge(6, 13);
        g.addEdge(6, 15);
        g.addEdge(6, 22);
        g.addEdge(7, 11);
        g.addEdge(7, 12);
        g.addEdge(7, 14);
        g.addEdge(7, 22);
        g.addEdge(8, 11);
        g.addEdge(8, 13);
        g.addEdge(8, 16);
        g.addEdge(8, 22);
        g.addEdge(9, 11);
        g.addEdge(9, 12);
        g.addEdge(9, 16);
        g.addEdge(9, 22);
        g.addEdge(10, 11);
        g.addEdge(10, 14);
        g.addEdge(10, 15);
        g.addEdge(10, 22);
        g.addEdge(11, 17);
        g.addEdge(11, 18);
        g.addEdge(11, 19);
        g.addEdge(11, 20);
        g.addEdge(11, 21);
        g.addEdge(12, 23);
        g.addEdge(13, 23);
        g.addEdge(14, 23);
        g.addEdge(15, 23);
        g.addEdge(16, 23);
        g.addEdge(17, 23);
        g.addEdge(18, 23);
        g.addEdge(19, 23);
        g.addEdge(20, 23);
        g.addEdge(21, 23);
        g.addEdge(22, 23);

        return g;
    }

    final protected Graph<Integer, DefaultEdge> createDSaturNonOptimalGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, IntStream.range(1, 8).boxed().collect(Collectors.toList()));

        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 4);
        g.addEdge(2, 3);
        g.addEdge(2, 5);
        g.addEdge(4, 6);
        g.addEdge(4, 7);
        g.addEdge(5, 6);
        g.addEdge(5, 7);
        g.addEdge(6, 7);

        return g;
    }

    @Test
    public void testMyciel3()
    {
        Graph<Integer, DefaultEdge> g = createMyciel3Graph();
        assertColoring(g, getAlgorithm(g).getColoring(), getExpectedResultOnMyceil3Graph());
    }

    @Test
    public void testMyciel4()
    {
        Graph<Integer, DefaultEdge> g = createMyciel4Graph();
        assertColoring(g, getAlgorithm(g).getColoring(), getExpectedResultOnMyceil4Graph());
    }

    /**
     * Test instance where DSatur greedy coloring is non-optimal.
     */
    @Test
    public void testDSaturNonOptimal()
    {
        Graph<Integer, DefaultEdge> g = createDSaturNonOptimalGraph();
        assertColoring(
            g, getAlgorithm(g).getColoring(), getExpectedResultOnDSaturNonOptimalGraph());
    }

    @Test
    public void testGraph1()
    {
        Graph<Integer, DefaultEdge> g = createGraph1();
        assertColoring(g, getAlgorithm(g).getColoring(), getExpectedResultOnGraph1());
    }

    @Test
    public void testCompleteGraph()
    {
        final int n = 20;
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        CompleteGraphGenerator<Integer, DefaultEdge> gen = new CompleteGraphGenerator<>(n);
        gen.generateGraph(g);
        Coloring<Integer> coloring = getAlgorithm(g).getColoring();
        assertEquals(n, coloring.getNumberColors());
    }

    @Test
    public void testRandomFixedSeed17()
    {
        final long seed = 17;
        Random rng = new Random(seed);
        testRandomGraphColoring(rng);
    }

    @Test
    public void testRandom()
    {
        Random rng = new Random();
        testRandomGraphColoring(rng);
    }

}
