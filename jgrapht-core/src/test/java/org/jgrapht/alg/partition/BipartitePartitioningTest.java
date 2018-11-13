/*
 * (C) Copyright 2016-2018, by Dimitrios Michail, Alexandru Valeanu and Contributors.
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
package org.jgrapht.alg.partition;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link BipartitePartitioning}
 *
 * @author Alexandru Valeanu
 * @author Dimitrios Michail
 */
public class BipartitePartitioningTest
{

    @Test
    public void testBipartite10()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        assertTrue(GraphTests.isBipartite(g));
        g.addVertex(1);
        assertTrue(GraphTests.isBipartite(g));
        g.addVertex(2);
        assertTrue(GraphTests.isBipartite(g));
        g.addEdge(1, 2);
        assertTrue(GraphTests.isBipartite(g));
        g.addVertex(3);
        assertTrue(GraphTests.isBipartite(g));
        g.addEdge(2, 3);
        assertTrue(GraphTests.isBipartite(g));
        g.addEdge(3, 1);
        assertFalse(GraphTests.isBipartite(g));
    }

    @Test
    public void testBipartite20()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

        for (int i = 0; i < 100; i++) {
            g.addVertex(i);
            if (i > 0) {
                g.addEdge(i, i - 1);
            }
        }
        g.addEdge(99, 0);
        assertTrue(GraphTests.isBipartite(g));
    }

    @Test
    public void testBipartite30()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

        for (int i = 0; i < 101; i++) {
            g.addVertex(i);
            if (i > 0) {
                g.addEdge(i, i - 1);
            }
        }
        g.addEdge(100, 0);
        assertFalse(GraphTests.isBipartite(g));
    }

    @Test
    public void testBipartite40()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        for (int i = 0; i < 101; i++) {
            g.addVertex(i);
            if (i > 0) {
                g.addEdge(i, i - 1);
            }
        }
        g.addEdge(100, 0);
        assertFalse(GraphTests.isBipartite(g));
    }

    @Test
    public void testRandomBipartite()
    {
        GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge> generator =
            new GnpRandomBipartiteGraphGenerator<>(10, 10, 0.8);
        for (int i = 0; i < 100; i++) {
            Graph<Integer, DefaultEdge> g = GraphTestsUtils.createPseudograph();
            generator.generateGraph(g);
            assertTrue(GraphTests.isBipartite(g));
        }
    }

    @Test
    public void testIsBipartitePartition()
    {
        List<Graph<Integer, DefaultEdge>> gList = new ArrayList<>();
        gList.add(new Pseudograph<>(DefaultEdge.class));
        gList.add(new DirectedPseudograph<>(DefaultEdge.class));

        for (Graph<Integer, DefaultEdge> g : gList) {
            Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
            Set<Integer> a = new HashSet<>(Arrays.asList(1, 2));
            Set<Integer> b = new HashSet<>(Arrays.asList(3, 4));
            assertTrue(GraphTests.isBipartitePartition(g, a, b));
            g.addEdge(1, 3);
            g.addEdge(1, 4);
            g.addEdge(1, 3);
            g.addEdge(2, 3);
            g.addEdge(2, 4);
            g.addEdge(4, 1);
            g.addEdge(3, 1);
            assertTrue(GraphTests.isBipartitePartition(g, a, b));
            a.remove(1);
            assertFalse(GraphTests.isBipartitePartition(g, a, b));
            a.add(1);
            assertTrue(GraphTests.isBipartitePartition(g, a, b));
            DefaultEdge e11 = g.addEdge(1, 1);
            assertFalse(GraphTests.isBipartitePartition(g, a, b));
            g.removeEdge(e11);
            assertTrue(GraphTests.isBipartitePartition(g, a, b));
            DefaultEdge e44 = g.addEdge(4, 4);
            assertFalse(GraphTests.isBipartitePartition(g, a, b));
            g.removeEdge(e44);
            assertTrue(GraphTests.isBipartitePartition(g, a, b));
            g.addEdge(4, 3);
            assertFalse(GraphTests.isBipartitePartition(g, a, b));
        }
    }

    @Test
    public void testEmptyGraph()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        BipartitePartitioning<Integer, DefaultEdge> finder = new BipartitePartitioning<>(graph);

        Assert.assertTrue(finder.isBipartite());
        Assert.assertTrue(finder.isValidPartitioning(finder.getPartitioning()));
    }

    @Test
    public void testBipartite()
    {
        Random random = new Random(0x88);

        for (int i = 0; i < 100; i++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            CompleteBipartiteGraphGenerator<Integer, DefaultEdge> generator =
                new CompleteBipartiteGraphGenerator<>(
                    1 + random.nextInt(100), 1 + random.nextInt(200));
            generator.generateGraph(graph);

            BipartitePartitioning<Integer, DefaultEdge> finder = new BipartitePartitioning<>(graph);

            Assert.assertTrue(finder.isBipartite());
            Assert.assertTrue(finder.isValidPartitioning(finder.getPartitioning()));
        }
    }

    @Test
    public void testBipartite2()
    {
        Random random = new Random(0x88);

        for (int i = 0; i < 100; i++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            int n1 = 1 + random.nextInt(100);
            int n2 = 1 + random.nextInt(200);
            int m = 4 * n1 * n2 / 10;

            GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge> generator =
                new GnmRandomBipartiteGraphGenerator<>(n1, n2, m);
            generator.generateGraph(graph);

            BipartitePartitioning<Integer, DefaultEdge> finder = new BipartitePartitioning<>(graph);

            Assert.assertTrue(finder.isBipartite());
            Assert.assertTrue(finder.isValidPartitioning(finder.getPartitioning()));
        }
    }

    @Test
    public void testStarGraph()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        StarGraphGenerator<Integer, DefaultEdge> generator = new StarGraphGenerator<>(100);
        generator.generateGraph(graph);

        BipartitePartitioning<Integer, DefaultEdge> finder = new BipartitePartitioning<>(graph);

        Assert.assertTrue(finder.isBipartite());
        Assert.assertTrue(finder.isValidPartitioning(finder.getPartitioning()));
    }

    @Test
    public void testForest()
    {
        Random random = new Random(0x88);

        for (int i = 0; i < 100; i++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            final int T = 10 + random.nextInt(50);
            final int N = 100 + random.nextInt(200);

            BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertForestGenerator<>(T, N);
            generator.generateGraph(graph);

            BipartitePartitioning<Integer, DefaultEdge> finder = new BipartitePartitioning<>(graph);

            Assert.assertTrue(finder.isBipartite());
            Assert.assertTrue(finder.isValidPartitioning(finder.getPartitioning()));
        }
    }

    @Test
    public void testComplete()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        CompleteGraphGenerator<Integer, DefaultEdge> generator = new CompleteGraphGenerator<>(100);
        generator.generateGraph(graph);

        BipartitePartitioning<Integer, DefaultEdge> finder = new BipartitePartitioning<>(graph);

        Assert.assertFalse(finder.isBipartite());
        Assert.assertNull(finder.getPartitioning());
    }

    @Test
    public void testEvenCycle()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        RingGraphGenerator<Integer, DefaultEdge> generator = new RingGraphGenerator<>(100);
        generator.generateGraph(graph);

        BipartitePartitioning<Integer, DefaultEdge> finder = new BipartitePartitioning<>(graph);

        Assert.assertTrue(finder.isBipartite());
        Assert.assertTrue(finder.isValidPartitioning(finder.getPartitioning()));
    }

    @Test
    public void testOddCycle()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        RingGraphGenerator<Integer, DefaultEdge> generator = new RingGraphGenerator<>(101);
        generator.generateGraph(graph);

        BipartitePartitioning<Integer, DefaultEdge> finder = new BipartitePartitioning<>(graph);

        Assert.assertFalse(finder.isBipartite());
        Assert.assertNull(finder.getPartitioning());
    }
}
