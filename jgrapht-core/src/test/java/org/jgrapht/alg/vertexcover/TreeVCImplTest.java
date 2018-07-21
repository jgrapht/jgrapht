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
package org.jgrapht.alg.vertexcover;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.interfaces.VertexCoverAlgorithm;
import org.jgrapht.generate.BarabasiAlbertForestGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

/**
 * Tests for {@link TreeVCImpl}
 *
 * @author Alexandru Valeanu
 */
public class TreeVCImplTest {

    public static <V, E> boolean isVC(Graph<V, E> graph, VertexCoverAlgorithm.VertexCover<V> vertexCover){
        if (!graph.vertexSet().containsAll(vertexCover))
            return false;

        for (E edge: graph.edgeSet()){
            V u = graph.getEdgeSource(edge);
            V v = graph.getEdgeTarget(edge);

            if (!vertexCover.contains(u) && !vertexCover.contains(v))
                return false;
        }

        return true;
    }

    @Test(expected = NullPointerException.class)
    public void testNullGraph(){
        new TreeVCImpl<Integer, DefaultEdge>(null, Collections.emptySet()).getVertexCover();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoRoots(){
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);
        new TreeVCImpl<>(tree, Collections.emptySet()).getVertexCover();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultipleRootsInTheSameTree(){
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);
        tree.addVertex(1);
        tree.addVertex(2);
        tree.addEdge(1, 2);

        new TreeVCImpl<>(tree, tree.vertexSet()).getVertexCover();
    }

    @Test
    public void testSmallTree(){
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= 16; i++) {
            tree.addVertex(i);
        }

        tree.addEdge(1, 2);
        tree.addEdge(1, 3);
        tree.addEdge(2, 4);
        tree.addEdge(3, 5);
        tree.addEdge(4, 6);
        tree.addEdge(4, 7);
        tree.addEdge(6, 11);
        tree.addEdge(6, 12);
        tree.addEdge(6, 13);
        tree.addEdge(7, 14);
        tree.addEdge(7, 15);
        tree.addEdge(5, 8);
        tree.addEdge(5, 9);
        tree.addEdge(5, 10);
        tree.addEdge(10, 16);

        VertexCoverAlgorithm.VertexCover<Integer> vc = new TreeVCImpl<>(tree, 1).getVertexCover();
        Assert.assertTrue(isVC(tree, vc));
        Assert.assertEquals(6, vc.size());
    }

    @Test
    public void testSmallTree2(){
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= 10; i++) {
            tree.addVertex(i);
        }

        tree.addEdge(1, 2);
        tree.addEdge(2, 3);
        tree.addEdge(2, 8);
        tree.addEdge(3, 4);
        tree.addEdge(4, 5);
        tree.addEdge(4, 6);
        tree.addEdge(4, 7);
        tree.addEdge(8, 9);
        tree.addEdge(8, 10);


        VertexCoverAlgorithm.VertexCover<Integer> vc = new TreeVCImpl<>(tree, 1).getVertexCover();
        Assert.assertTrue(isVC(tree, vc));
        Assert.assertEquals(3, vc.size());
    }

    @Test
    public void testSmallTree3(){
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= 10; i++) {
            tree.addVertex(i);
        }

        tree.addEdge(1, 2);
        tree.addEdge(2, 8);
        tree.addEdge(2, 3);
        tree.addEdge(2, 4);
        tree.addEdge(2, 5);
        tree.addEdge(2, 9);
        tree.addEdge(3, 6);
        tree.addEdge(6, 7);
        tree.addEdge(7, 10);


        VertexCoverAlgorithm.VertexCover<Integer> vc = new TreeVCImpl<>(tree, 1).getVertexCover();
        Assert.assertTrue(isVC(tree, vc));
        Assert.assertEquals(3, vc.size());
    }

    @Test
    public void testLineGraph(){
        for (int i = 1; i <= 100; i++) {
            Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);

            for (int j = 1; j <= i; j++) {
                tree.addVertex(j);
            }

            for (int j = 1; j < i; j++){
                tree.addEdge(j, j + 1);
            }

            VertexCoverAlgorithm.VertexCover<Integer> vc = new TreeVCImpl<>(tree, 1).getVertexCover();
            Assert.assertTrue(isVC(tree, vc));
            Assert.assertEquals(i / 2, vc.size());
        }
    }

    @Test
    public void testRandomTrees(){
        final int NUM_TESTS = 800;
        Random random = new Random(0x88);

        for (int test = 0; test < NUM_TESTS; test++) {
            final int N = 1 + random.nextInt(50);

            Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(1),
                    SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                    new BarabasiAlbertForestGenerator<>(1, N, random);

            generator.generateGraph(tree);

            RecursiveExactVCImpl<Integer, DefaultEdge> recursiveExactVC = new RecursiveExactVCImpl<>(tree);
            TreeVCImpl<Integer, DefaultEdge> treeVC = new TreeVCImpl<>(tree, 1);

            VertexCoverAlgorithm.VertexCover<Integer> exactVC = recursiveExactVC.getVertexCover();
            VertexCoverAlgorithm.VertexCover<Integer> vc = treeVC.getVertexCover();

            Assert.assertEquals(exactVC.size(), vc.size());
            Assert.assertTrue(isVC(tree, vc));
        }
    }

    public static <V, E> Graph<V, E> takeSubgraph(Graph<V, E> graph, Set<V> vertices){
        Graph<V, E> subgraph = new SimpleGraph<>(graph.getVertexSupplier(), graph.getEdgeSupplier(), false);

        for (V v: vertices)
            subgraph.addVertex(v);

        for (V v: vertices){
            for (E edge: graph.edgesOf(v)){
                V u = Graphs.getOppositeVertex(graph, edge, v);

                if (vertices.contains(u))
                    subgraph.addEdge(u, v);
            }
        }

        return subgraph;
    }

    @Test
    public void testRandomForests(){
        final int NUM_TESTS = 500;
        Random random = new Random(0x88_88);

        for (int test = 0; test < NUM_TESTS; test++) {
            final int T = 1 + random.nextInt(20);
            final int N = T + random.nextInt(60);

            Graph<Integer, DefaultEdge> forest = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(1),
                    SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                    new BarabasiAlbertForestGenerator<>(T, N, random);

            generator.generateGraph(forest);

            for (Set<Integer> component: new ConnectivityInspector<>(forest).connectedSets()){
                final Graph<Integer, DefaultEdge> tree = takeSubgraph(forest, component);
                final int root = component.iterator().next();

                RecursiveExactVCImpl<Integer, DefaultEdge> recursiveExactVC = new RecursiveExactVCImpl<>(tree);
                TreeVCImpl<Integer, DefaultEdge> treeVC = new TreeVCImpl<>(tree, root);

                VertexCoverAlgorithm.VertexCover<Integer> exactVC = recursiveExactVC.getVertexCover();
                VertexCoverAlgorithm.VertexCover<Integer> vc = treeVC.getVertexCover();

                Assert.assertEquals(exactVC.size(), vc.size());
                Assert.assertTrue(isVC(tree, vc));
            }
        }
    }
}