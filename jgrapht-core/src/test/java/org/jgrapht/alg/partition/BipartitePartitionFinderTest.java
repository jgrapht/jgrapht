/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.alg.partition;

import org.jgrapht.Graph;
import org.jgrapht.generate.*;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * Tests for {@link BipartitePartitionFinder}
 *
 * @author Alexandru Valeanu
 */
public class BipartitePartitionFinderTest {

    @Test
    public void testEmptyGraph(){
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        BipartitePartitionFinder<Integer, DefaultEdge> finder = new BipartitePartitionFinder<>(graph);

        Assert.assertTrue(finder.isBipartite());
        Assert.assertTrue(finder.isValidPartition(finder.getPartition()));
    }

    @Test
    public void testBipartite(){
        Random random = new Random(0x88);

        for (int i = 0; i < 100; i++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                    SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            CompleteBipartiteGraphGenerator<Integer, DefaultEdge> generator =
                    new CompleteBipartiteGraphGenerator<>(1 + random.nextInt(100),
                            1 + random.nextInt(200));
            generator.generateGraph(graph);

            BipartitePartitionFinder<Integer, DefaultEdge> finder = new BipartitePartitionFinder<>(graph);

            Assert.assertTrue(finder.isBipartite());
            Assert.assertTrue(finder.isValidPartition(finder.getPartition()));
        }
    }

    @Test
    public void testBipartite2(){
        Random random = new Random(0x88);

        for (int i = 0; i < 100; i++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                    SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            int n1 = 1 + random.nextInt(100);
            int n2 = 1 + random.nextInt(200);
            int m = 4 * n1 * n2 / 10;

            GnmRandomBipartiteGraphGenerator<Integer, DefaultEdge> generator =
                    new GnmRandomBipartiteGraphGenerator<>(n1, n2, m);
            generator.generateGraph(graph);

            BipartitePartitionFinder<Integer, DefaultEdge> finder = new BipartitePartitionFinder<>(graph);

            Assert.assertTrue(finder.isBipartite());
            Assert.assertTrue(finder.isValidPartition(finder.getPartition()));
        }
    }

    @Test
    public void testStarGraph(){
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        StarGraphGenerator<Integer, DefaultEdge> generator = new StarGraphGenerator<>(100);
        generator.generateGraph(graph);

        BipartitePartitionFinder<Integer, DefaultEdge> finder = new BipartitePartitionFinder<>(graph);

        Assert.assertTrue(finder.isBipartite());
        Assert.assertTrue(finder.isValidPartition(finder.getPartition()));
    }

    @Test
    public void testForest(){
        Random random = new Random(0x88);

        for (int i = 0; i < 100; i++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                    SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            final int T = 10 + random.nextInt(50);
            final int N = 100 + random.nextInt(200);

            BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                    new BarabasiAlbertForestGenerator<>(T, N);
            generator.generateGraph(graph);

            BipartitePartitionFinder<Integer, DefaultEdge> finder = new BipartitePartitionFinder<>(graph);

            Assert.assertTrue(finder.isBipartite());
            Assert.assertTrue(finder.isValidPartition(finder.getPartition()));
        }
    }

    @Test
    public void testComplete(){
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        CompleteGraphGenerator<Integer, DefaultEdge> generator = new CompleteGraphGenerator<>(100);
        generator.generateGraph(graph);

        BipartitePartitionFinder<Integer, DefaultEdge> finder = new BipartitePartitionFinder<>(graph);

        Assert.assertFalse(finder.isBipartite());
        Assert.assertNull(finder.getPartition());
    }

    @Test
    public void testEvenCycle(){
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        RingGraphGenerator<Integer, DefaultEdge> generator = new RingGraphGenerator<>(100);
        generator.generateGraph(graph);

        BipartitePartitionFinder<Integer, DefaultEdge> finder = new BipartitePartitionFinder<>(graph);

        Assert.assertTrue(finder.isBipartite());
        Assert.assertTrue(finder.isValidPartition(finder.getPartition()));
    }

    @Test
    public void testOddCycle(){
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        RingGraphGenerator<Integer, DefaultEdge> generator = new RingGraphGenerator<>(101);
        generator.generateGraph(graph);

        BipartitePartitionFinder<Integer, DefaultEdge> finder = new BipartitePartitionFinder<>(graph);

        Assert.assertFalse(finder.isBipartite());
        Assert.assertNull(finder.getPartition());
    }
}