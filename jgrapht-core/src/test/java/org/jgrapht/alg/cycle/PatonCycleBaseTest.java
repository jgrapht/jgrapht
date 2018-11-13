/*
 * (C) Copyright 2013-2018, by Nikolay Ognyanov, Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.CycleBasisAlgorithm.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.stream.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PatonCycleBaseTest
{
    private static int MAX_SIZE = 10;
    private static int[] RESULTS = { 0, 0, 0, 1, 3, 6, 10, 15, 21, 28, 36 };

    @Test
    public void testAlgorithm()
    {
        SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        for (int i = 0; i < 7; i++) {
            graph.addVertex(i);
        }

        CycleBasisAlgorithm<Integer, DefaultEdge> finder = new PatonCycleBase<>(graph);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);
        checkResult(finder, 1);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);
        checkResult(finder, 2);
        graph.addEdge(3, 1);
        checkResult(finder, 3);
        graph.addEdge(3, 4);
        graph.addEdge(4, 2);
        checkResult(finder, 4);
        graph.addEdge(4, 5);
        checkResult(finder, 4);
        graph.addEdge(5, 2);
        checkResult(finder, 5);
        graph.addEdge(5, 6);
        graph.addEdge(6, 4);
        checkResult(finder, 6);

        for (int size = 1; size <= MAX_SIZE; size++) {
            graph = new SimpleGraph<>(DefaultEdge.class);
            for (int i = 0; i < size; i++) {
                graph.addVertex(i);
            }
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i != j) {
                        graph.addEdge(i, j);
                    }
                }
            }
            finder = new PatonCycleBase<>(graph);
            checkResult(finder, RESULTS[size]);
        }
    }

    private void checkResult(CycleBasisAlgorithm<Integer, DefaultEdge> finder, int size)
    {
        assertTrue(finder.getCycleBasis().getCycles().size() == size);
    }

    @Test
    public void testPatonCycleBasis()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, IntStream.rangeClosed(1, 7).boxed().collect(Collectors.toList()));
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 4);
        g.addEdge(2, 5);
        g.addEdge(3, 6);
        g.addEdge(3, 7);

        g.addEdge(4, 5);
        g.addEdge(6, 7);
        g.addEdge(4, 6);

        // @formatter:off
        //
        //        1
        //       /  \
        //      2     3
        //           |  \
        //   4 - 5   6   7
        //   |       |
        //   ---------
        // 
        // @formatter:on

        Set<List<DefaultEdge>> ucb = new PatonCycleBase<>(g).getCycleBasis().getCycles();

        int[] cyclesSizes = { 3, 5, 3 };
        Iterator<List<DefaultEdge>> it = ucb.iterator();
        for (int i = 0; i < 3; i++) {
            List<DefaultEdge> cycle = it.next();
            assertEquals(cyclesSizes[i], cycle.size());
        }
    }

    @Test
    public void testPatonCycleBasis1()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, IntStream.rangeClosed(1, 15).boxed().collect(Collectors.toList()));
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 4);
        g.addEdge(1, 12);
        g.addEdge(3, 5);
        g.addEdge(3, 6);
        g.addEdge(12, 13);
        g.addEdge(6, 7);
        g.addEdge(6, 8);
        g.addEdge(13, 14);
        g.addEdge(7, 9);
        g.addEdge(8, 10);
        g.addEdge(14, 15);
        g.addEdge(10, 11);
        g.addEdge(2, 11);
        g.addEdge(5, 4);
        g.addEdge(5, 9);
        g.addEdge(9, 10);
        g.addEdge(9, 11);
        g.addEdge(10, 14);
        g.addEdge(11, 15);

        CycleBasis<Integer, DefaultEdge> ucb = new PatonCycleBase<>(g).getCycleBasis();

        int[] cyclesSizes = { 3, 8, 8, 9, 5, 7, 4 };
        Iterator<List<DefaultEdge>> it = ucb.getCycles().iterator();
        for (int i = 0; i < 7; i++) {
            List<DefaultEdge> cycle = it.next();
            assertEquals(cyclesSizes[i], cycle.size());
            assertCycle(g, cycle);
        }
        assertEquals(44, ucb.getLength());
        assertEquals(44d, ucb.getWeight(), 1e-9);
    }

    @Test
    public void testPatonCycleBasis2()
    {
        SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < 7; i++) {
            graph.addVertex(i);
        }

        CycleBasisAlgorithm<Integer, DefaultEdge> finder = new PatonCycleBase<>(graph);
        CycleBasis<Integer, DefaultEdge> basis;

        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);
        basis = finder.getCycleBasis();
        assertEquals(1, basis.getCycles().size());
        assertEquals(3, basis.getLength());
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);
        basis = finder.getCycleBasis();
        assertEquals(2, basis.getCycles().size());
        assertEquals(6, basis.getLength());
        graph.addEdge(3, 1);
        basis = finder.getCycleBasis();
        assertEquals(3, basis.getCycles().size());
        assertEquals(9, basis.getLength());
        graph.addEdge(3, 4);
        graph.addEdge(4, 2);
        basis = finder.getCycleBasis();
        assertEquals(4, basis.getCycles().size());
        assertEquals(12, basis.getLength());
        graph.addEdge(4, 5);
        basis = finder.getCycleBasis();
        assertEquals(4, basis.getCycles().size());
        assertEquals(12, basis.getLength());
        graph.addEdge(5, 2);
        basis = finder.getCycleBasis();
        assertEquals(5, basis.getCycles().size());
        assertEquals(15, basis.getLength());
        graph.addEdge(5, 6);
        graph.addEdge(6, 4);
        basis = finder.getCycleBasis();
        assertEquals(6, basis.getCycles().size());
        assertEquals(18, basis.getLength());

        for (int size = 1; size <= MAX_SIZE; size++) {
            graph = new SimpleGraph<>(DefaultEdge.class);
            finder = new PatonCycleBase<>(graph);
            for (int i = 0; i < size; i++) {
                graph.addVertex(i);
            }
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i != j) {
                        graph.addEdge(i, j);
                    }
                }
            }
            basis = finder.getCycleBasis();
            assertEquals(RESULTS[size], basis.getCycles().size());
            assertEquals(3 * RESULTS[size], basis.getLength());
            assertEquals(3.0 * RESULTS[size], basis.getWeight(), 1e-9);
            for (List<DefaultEdge> c : basis.getCycles()) {
                assertCycle(graph, c);
            }
        }
    }

    @Test
    public void testPatonCycleBasis3()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, IntStream.rangeClosed(1, 15).boxed().collect(Collectors.toList()));
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 4);
        g.addEdge(2, 5);
        g.addEdge(3, 6);
        g.addEdge(3, 7);
        g.addEdge(4, 8);
        g.addEdge(4, 9);
        g.addEdge(5, 10);
        g.addEdge(5, 11);
        g.addEdge(6, 12);
        g.addEdge(6, 13);
        g.addEdge(7, 14);
        g.addEdge(7, 15);

        g.addEdge(8, 9);
        g.addEdge(10, 11);
        g.addEdge(12, 13);
        g.addEdge(14, 15);
        g.addEdge(8, 10);
        g.addEdge(9, 11);
        g.addEdge(10, 12);
        g.addEdge(11, 13);
        g.addEdge(12, 14);
        g.addEdge(8, 11);
        g.addEdge(9, 12);
        g.addEdge(10, 13);
        g.addEdge(11, 14);
        g.addEdge(12, 15);
        g.addEdge(8, 12);
        g.addEdge(9, 13);
        g.addEdge(10, 14);
        g.addEdge(11, 15);
        g.addEdge(8, 13);
        g.addEdge(9, 14);
        g.addEdge(10, 15);
        g.addEdge(8, 14);
        g.addEdge(9, 15);
        g.addEdge(8, 15);

        CycleBasis<Integer, DefaultEdge> ucb = new PatonCycleBase<>(g).getCycleBasis();

        Iterator<List<DefaultEdge>> it = ucb.getCycles().iterator();
        for (int i = 0; i < 24; i++) {
            List<DefaultEdge> cycle = it.next();
            assertCycle(g, cycle);
        }
        assertEquals(85, ucb.getLength());
        assertEquals(85d, ucb.getWeight(), 1e-9);
    }

    @Test
    public void testPatonCycleBasis4()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, IntStream.rangeClosed(1, 7).boxed().collect(Collectors.toList()));
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 4);
        g.addEdge(2, 5);
        g.addEdge(3, 6);
        g.addEdge(3, 7);

        g.addEdge(4, 5);
        g.addEdge(6, 7);
        g.addEdge(4, 6);

        // @formatter:off
        // 
        //        1
        //       /  \
        //      2     3
        //           |  \
        //   4 - 5   6   7
        //   |       |
        //   ---------
        // 
        // @formatter:on

        CycleBasis<Integer, DefaultEdge> ucb = new PatonCycleBase<>(g).getCycleBasis();

        Iterator<List<DefaultEdge>> it = ucb.getCycles().iterator();
        for (int i = 0; i < 3; i++) {
            List<DefaultEdge> cycle = it.next();
            assertCycle(g, cycle);
        }
        assertEquals(11, ucb.getLength());
        assertEquals(11d, ucb.getWeight(), 1e-9);
    }

    @Test
    public void testPatonCycleBasis5()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, IntStream.rangeClosed(1, 15).boxed().collect(Collectors.toList()));
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 4);
        g.addEdge(2, 5);
        g.addEdge(3, 6);
        g.addEdge(3, 7);
        g.addEdge(4, 8);
        g.addEdge(4, 9);
        g.addEdge(5, 10);
        g.addEdge(5, 11);
        g.addEdge(6, 12);
        g.addEdge(6, 13);
        g.addEdge(7, 14);
        g.addEdge(7, 15);

        g.addEdge(8, 9);
        g.addEdge(10, 11);
        g.addEdge(12, 13);
        g.addEdge(14, 15);
        g.addEdge(8, 10);

        CycleBasis<Integer, DefaultEdge> ucb = new PatonCycleBase<>(g).getCycleBasis();

        int[] cyclesSizes = { 3, 3, 3, 5, 3 };
        Iterator<List<DefaultEdge>> it = ucb.getCycles().iterator();
        for (int i = 0; i < 5; i++) {
            List<DefaultEdge> cycle = it.next();
            assertCycle(g, cycle);
            assertEquals(cyclesSizes[i], cycle.size());
        }
        assertEquals(17, ucb.getLength());
        assertEquals(17d, ucb.getWeight(), 1e-9);
    }

    @Test
    public void testPatonCycleBasis6()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, IntStream.rangeClosed(1, 7).boxed().collect(Collectors.toList()));
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 4);
        g.addEdge(2, 5);
        g.addEdge(2, 3);
        g.addEdge(3, 6);
        g.addEdge(3, 7);

        g.addEdge(4, 6);
        g.addEdge(5, 7);

        CycleBasis<Integer, DefaultEdge> ucb = new PatonCycleBase<>(g).getCycleBasis();

        int[] cyclesSizes = { 3, 4, 4 };
        Iterator<List<DefaultEdge>> it = ucb.getCycles().iterator();
        for (int i = 0; i < 3; i++) {
            List<DefaultEdge> cycle = it.next();
            assertEquals(cyclesSizes[i], cycle.size());
        }
    }

    @Test
    public void testPatonCycleBasis7()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, IntStream.rangeClosed(1, 7).boxed().collect(Collectors.toList()));
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 4);
        g.addEdge(2, 5);

        g.addEdge(4, 5);
        g.addEdge(4, 3);

        // @formatter:off
        // 
        //        1
        //       /  \
        //      2    3
        //           |
        //   4 - 5   |
        //   |       |
        //   ---------
        // 
        // @formatter:on

        CycleBasis<Integer, DefaultEdge> ucb = new PatonCycleBase<>(g).getCycleBasis();

        Iterator<List<DefaultEdge>> it = ucb.getCycles().iterator();
        for (int i = 0; i < 2; i++) {
            List<DefaultEdge> cycle = it.next();
            assertCycle(g, cycle);
        }
        assertEquals(7, ucb.getLength());
        assertEquals(7d, ucb.getWeight(), 1e-9);
    }

    @Test
    public void testPatonCycleBasis8()
    {
        final int n = 200;
        final double p = 0.7;
        final int graphs = 10;
        GnpRandomGraphGenerator<Integer, DefaultEdge> gen = new GnpRandomGraphGenerator<>(n, p);
        for (int i = 0; i < graphs; i++) {
            Graph<Integer,
                DefaultEdge> g = new SimpleGraph<>(
                    SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(),
                    false);
            gen.generateGraph(g);
            CycleBasis<Integer, DefaultEdge> ucb = new PatonCycleBase<>(g).getCycleBasis();

            int k = new ConnectivityInspector<>(g).connectedSets().size();
            int cycleSpaceDimension = g.edgeSet().size() - g.vertexSet().size() + k;

            assertEquals(cycleSpaceDimension, ucb.getCycles().size());
            for (List<DefaultEdge> cycle : ucb.getCycles()) {
                assertCycle(g, cycle);
            }
        }
    }

    @Test
    public void testZeroCycleSpaceDimension()
    {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addEdge(0, 1);
        graph.addEdge(2, 3);

        CycleBasisAlgorithm<Integer, DefaultEdge> fcb = new PatonCycleBase<>(graph);
        CycleBasis<Integer, DefaultEdge> cb = fcb.getCycleBasis();
        assertEquals(0, cb.getCycles().size());
        assertEquals(0, cb.getLength());
        assertEquals(0d, cb.getWeight(), 1e-9);
    }

    @Test
    public void testWithLoops()
    {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3));
        DefaultEdge e01 = graph.addEdge(0, 1);
        DefaultEdge e12 = graph.addEdge(1, 2);
        DefaultEdge e23 = graph.addEdge(2, 3);
        DefaultEdge e30 = graph.addEdge(3, 0);
        DefaultEdge e00 = graph.addEdge(0, 0);
        DefaultEdge e11 = graph.addEdge(1, 1);
        DefaultEdge e22 = graph.addEdge(2, 2);
        DefaultEdge e33 = graph.addEdge(3, 3);

        CycleBasisAlgorithm<Integer, DefaultEdge> fcb = new PatonCycleBase<>(graph);
        CycleBasis<Integer, DefaultEdge> cb = fcb.getCycleBasis();

        int dimension = 5;
        Iterator<List<DefaultEdge>> it = cb.getCycles().iterator();
        for (int i = 0; i < dimension; i++) {
            List<DefaultEdge> c = it.next();
            assertCycle(graph, c);
            switch (i) {
            case 0:
                assertEquals(Collections.singletonList(e00), c);
                break;
            case 1:
                assertEquals(Collections.singletonList(e33), c);
                break;
            case 2:
                assertEquals(Arrays.asList(e12, e23, e30, e01), c);
                break;
            case 3:
                assertEquals(Collections.singletonList(e22), c);
                break;
            case 4:
                assertEquals(Collections.singletonList(e11), c);
                break;
            }
        }

        assertEquals(5, cb.getCycles().size());
        assertEquals(8, cb.getLength());
        assertEquals(8d, cb.getWeight(), 1e-9);
    }

    @Test
    public void testSingleLoops()
    {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph, Collections.singletonList(0));
        DefaultEdge e1 = graph.addEdge(0, 0);

        CycleBasisAlgorithm<Integer, DefaultEdge> fcb = new PatonCycleBase<>(graph);
        CycleBasis<Integer, DefaultEdge> cb = fcb.getCycleBasis();

        int dimension = 1;
        Iterator<List<DefaultEdge>> it = cb.getCycles().iterator();
        for (int i = 0; i < dimension; i++) {
            List<DefaultEdge> c = it.next();
            assertCycle(graph, c);
            switch (i) {
            case 0:
                assertEquals(Collections.singletonList(e1), c);
                break;
            }
        }
        assertEquals(1, cb.getCycles().size());
        assertEquals(1, cb.getLength());
        assertEquals(1d, cb.getWeight(), 1e-9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultipleEdges()
    {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph, Collections.singletonList(0));
        graph.addEdge(0, 0);
        graph.addEdge(0, 0);

        new PatonCycleBase<>(graph).getCycleBasis();
    }

    @Test
    public void testDisconnectedAndWeights()
    {
        WeightedPseudograph<Integer, DefaultWeightedEdge> graph =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3, 4, 5));
        graph.setEdgeWeight(graph.addEdge(0, 1), 2.0);
        graph.setEdgeWeight(graph.addEdge(1, 2), 7.0);
        graph.setEdgeWeight(graph.addEdge(2, 0), 13.0);
        graph.setEdgeWeight(graph.addEdge(3, 4), 102.0);
        graph.setEdgeWeight(graph.addEdge(4, 5), 107.0);
        graph.setEdgeWeight(graph.addEdge(5, 3), 113.0);
        CycleBasisAlgorithm<Integer, DefaultWeightedEdge> fcb = new PatonCycleBase<>(graph);
        CycleBasis<Integer, DefaultWeightedEdge> cb = fcb.getCycleBasis();
        assertEquals(2, cb.getCycles().size());
        assertEquals(6, cb.getLength());
        assertEquals(344d, cb.getWeight(), 1e-9);
    }

    // assert that a list of edges is a cycle
    private void assertCycle(Graph<Integer, DefaultEdge> g, List<DefaultEdge> edges)
    {
        if (edges.isEmpty()) {
            return;
        }

        boolean isDirected = g.getType().isDirected();
        DefaultEdge prev = null;
        DefaultEdge first = null, last = null;
        Iterator<DefaultEdge> it = edges.iterator();
        Set<DefaultEdge> dupCheck = new HashSet<>();
        while (it.hasNext()) {
            DefaultEdge cur = it.next();
            assertTrue(dupCheck.add(cur));
            if (prev == null) {
                first = cur;
            } else {
                if (isDirected) {
                    assertTrue(g.getEdgeSource(cur).equals(g.getEdgeTarget(prev)));
                } else {
                    assertTrue(
                        g.getEdgeSource(cur).equals(g.getEdgeSource(prev))
                            || g.getEdgeSource(cur).equals(g.getEdgeTarget(prev))
                            || g.getEdgeTarget(cur).equals(g.getEdgeSource(prev))
                            || g.getEdgeTarget(cur).equals(g.getEdgeTarget(prev)));
                }
            }
            if (!it.hasNext()) {
                last = cur;
            }
            prev = cur;
        }
        if (edges.size() > 1) {
            if (isDirected) {
                assertTrue(g.getEdgeSource(first).equals(g.getEdgeTarget(last)));
            } else {
                assertTrue(
                    g.getEdgeSource(first).equals(g.getEdgeSource(last))
                        || g.getEdgeSource(first).equals(g.getEdgeTarget(last))
                        || g.getEdgeTarget(first).equals(g.getEdgeSource(last))
                        || g.getEdgeTarget(first).equals(g.getEdgeTarget(last)));
            }
        }
    }

}
