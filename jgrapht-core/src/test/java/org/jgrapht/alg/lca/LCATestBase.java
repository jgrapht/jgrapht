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
package org.jgrapht.alg.lca;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.interfaces.LCAAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.UnionFind;
import org.jgrapht.generate.BarabasiAlbertForestGenerator;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tests for LCA algorithms on trees and forests
 *
 * @author Alexandru Valeanu
 */
public abstract class LCATestBase {

    abstract <V, E> LCAAlgorithm<V> createSolver(Graph<V, E> graph, Set<V> roots);

    @Test
    public void testOneNode() {
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex("a");

        Assert.assertEquals("a", createSolver(g, Collections.singleton("a")).getLCA("a", "a"));
    }

    @Test
    public void testTwoRootsInTheSameTree(){
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex("b");
        g.addVertex("a");
        g.addVertex("c");
        g.addVertex("d");

        g.addEdge("a", "b");
        g.addEdge("c", "d");

        // Either a or b is a valid answer

        String lca = createSolver(g, g.vertexSet()).getLCA("a", "b");

        Assert.assertTrue(lca.equals("a") || lca.equals("b"));
    }

    @Test
    public void testDisconnectSmallGraph(){
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex("a");
        g.addVertex("b");

        LCAAlgorithm<String> lcaAlgorithm = createSolver(g, Collections.singleton("a"));

        Assert.assertNull(lcaAlgorithm.getLCA("a", "b"));
        Assert.assertNull(lcaAlgorithm.getLCA("b", "a"));
        Assert.assertEquals("a", lcaAlgorithm.getLCA("a", "a"));
        Assert.assertEquals("b", lcaAlgorithm.getLCA("b", "b"));
    }

    @Test
    public void testDisconnectSmallGraph2(){
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");

        LCAAlgorithm<String> lcaAlgorithm = createSolver(g, Collections.singleton("a"));

        Assert.assertNull(lcaAlgorithm.getLCA("b", "c"));
        Assert.assertNull(lcaAlgorithm.getLCA("c", "b"));
        Assert.assertEquals("a", lcaAlgorithm.getLCA("a", "a"));
        Assert.assertEquals("b", lcaAlgorithm.getLCA("b", "b"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyGraph(){
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        LCAAlgorithm<String> lcaAlgorithm = createSolver(g, Collections.singleton("a"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptySetOfRoots(){
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex("a");

        LCAAlgorithm<String> lcaAlgorithm = createSolver(g, Collections.emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRootNotInGraph(){
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex("a");

        LCAAlgorithm<String> lcaAlgorithm = createSolver(g, Collections.singleton("b"));
    }

    @Test
    public void testGraphAllPossibleQueries(){
        final int N = 100;

        Random random = new Random(0x88_88);

        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(0), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertForestGenerator<>(1, N, random);

        generator.generateGraph(g, null);

        Integer root = g.vertexSet().iterator().next();

        LCAAlgorithm<Integer> lcaAlgorithm1 = createSolver(g, Collections.singleton(root));
        LCAAlgorithm<Integer> lcaAlgorithm2;

        if (lcaAlgorithm1 instanceof EulerTourRMQLCAFinder)
            lcaAlgorithm2 = new BinaryLiftingLCAFinder<>(g, Collections.singleton(root));
        else
            lcaAlgorithm2 = new EulerTourRMQLCAFinder<>(g, Collections.singleton(root));

        List<Pair<Integer, Integer>> queries = new ArrayList<>(N * N);

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                queries.add(Pair.of(i, j));
            }
        }

        List<Integer> lcas1 = lcaAlgorithm1.getLCAs(queries);
        List<Integer> lcas2 = lcaAlgorithm2.getLCAs(queries);

        for (int i = 0; i < queries.size(); i++) {
            Assert.assertEquals(lcas1.get(i), lcas2.get(i));
        }
    }

    @Test
    public void testLongChain(){
        final int N = 2_000;
        final int Q = 100_000;

        Random random = new Random(0x88);

        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= N; i++)
            g.addVertex(i);

        for (int i = 1; i < N; i++)
            g.addEdge(i, i + 1);

        List<Pair<Integer, Integer>> queries = generateQueries(Q, new ArrayList<>(g.vertexSet()), random);

        LCAAlgorithm<Integer> lcaAlgorithm = createSolver(g, Collections.singleton(N));

        List<Integer> lcas = lcaAlgorithm.getLCAs(queries);

        for (int i = 0; i < Q; i++) {
            int a = queries.get(i).getFirst();
            int b = queries.get(i).getSecond();

            Assert.assertEquals((int)lcas.get(i), Math.max(a, b));
        }
    }

    @Test
    public void testBinaryTree() {
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("b", "d");
        g.addEdge("d", "e");

        LCAAlgorithm<String> lcaAlgorithm = createSolver(g, Collections.singleton("a"));

        Assert.assertEquals("b", lcaAlgorithm.getLCA("c", "e"));
        Assert.assertEquals("b", lcaAlgorithm.getLCA("b", "d"));
        Assert.assertEquals("d", lcaAlgorithm.getLCA("d", "e"));
    }

    @Test
    public void testSmallTree(){
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= 11; i++)
            graph.addVertex(i);

        graph.addEdge(1, 2);
        graph.addEdge(2, 4);
        graph.addEdge(2, 5);
        graph.addEdge(2, 6);
        graph.addEdge(4, 7);
        graph.addEdge(4, 8);
        graph.addEdge(6, 9);
        graph.addEdge(1, 3);
        graph.addEdge(3, 10);
        graph.addEdge(3, 11);

        LCAAlgorithm<Integer> lcaAlgorithm = createSolver(graph, Collections.singleton(1));

        Assert.assertEquals(3, (int)lcaAlgorithm.getLCA(10, 11));
        Assert.assertEquals(2, (int)lcaAlgorithm.getLCA(8, 9));
        Assert.assertEquals(1, (int)lcaAlgorithm.getLCA(5, 11));
        Assert.assertEquals(2, (int)lcaAlgorithm.getLCA(5, 6));
        Assert.assertEquals(2, (int)lcaAlgorithm.getLCA(4, 2));
        Assert.assertEquals(2, (int)lcaAlgorithm.getLCA(4, 5));
        Assert.assertEquals(2, (int)lcaAlgorithm.getLCA(2, 2));
        Assert.assertEquals(2, (int)lcaAlgorithm.getLCA(8, 6));
        Assert.assertEquals(4, (int)lcaAlgorithm.getLCA(7, 8));
    }

    @Test
    public void testSmallTree2(){
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= 20; i++)
            graph.addVertex(i);

        graph.addEdge(2, 1);
        graph.addEdge(3, 1);
        graph.addEdge(4, 1);
        graph.addEdge(5, 1);
        graph.addEdge(6, 2);
        graph.addEdge(7, 5);
        graph.addEdge(8, 7);
        graph.addEdge(9, 3);
        graph.addEdge(10, 2);
        graph.addEdge(11, 9);
        graph.addEdge(12, 6);
        graph.addEdge(13, 4);
        graph.addEdge(14, 6);
        graph.addEdge(15, 2);
        graph.addEdge(16, 10);
        graph.addEdge(17, 15);
        graph.addEdge(18, 6);
        graph.addEdge(19, 14);
        graph.addEdge(20, 11);

        LCAAlgorithm<Integer> lcaAlgorithm = createSolver(graph, Collections.singleton(1));


        Assert.assertEquals(1, (int)lcaAlgorithm.getLCA(9, 14));
        Assert.assertEquals(1, (int)lcaAlgorithm.getLCA(10, 9));
        Assert.assertEquals(15, (int)lcaAlgorithm.getLCA(15, 15));
        Assert.assertEquals(1, (int)lcaAlgorithm.getLCA(1, 17));
        Assert.assertEquals(3, (int)lcaAlgorithm.getLCA(3, 3));
        Assert.assertEquals(1, (int)lcaAlgorithm.getLCA(3, 1));
        Assert.assertEquals(1, (int)lcaAlgorithm.getLCA(11, 14));
        Assert.assertEquals(6, (int)lcaAlgorithm.getLCA(18, 19));
        Assert.assertEquals(2, (int)lcaAlgorithm.getLCA(12, 2));
        Assert.assertEquals(2, (int)lcaAlgorithm.getLCA(16, 14));
    }

    @Test
    public void testNonBinaryTreeBatch()
    {
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        g.addVertex("g");
        g.addVertex("h");
        g.addVertex("i");
        g.addVertex("j");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("c", "d");
        g.addEdge("d", "e");
        g.addEdge("b", "f");
        g.addEdge("b", "g");
        g.addEdge("c", "h");
        g.addEdge("c", "i");
        g.addEdge("i", "j");

        LCAAlgorithm<String> lcaAlgorithm = createSolver(g, Collections.singleton("a"));

        Assert.assertEquals("b", lcaAlgorithm.getLCA("b", "h"));
        Assert.assertEquals("b", lcaAlgorithm.getLCA("j", "f"));
        Assert.assertEquals("c", lcaAlgorithm.getLCA("j", "h"));

        // now all together in one call

        List<Pair<String, String>> queries = new ArrayList<>();
        queries.add(Pair.of("b", "h"));
        queries.add(Pair.of("j", "f"));
        queries.add(Pair.of("j", "h"));

        List<String> lcas = lcaAlgorithm.getLCAs(queries);

        Assert.assertEquals(Arrays.asList("b", "b", "c"), lcas);

        // test it the other way around and starting from b
        Assert.assertEquals("b", createSolver(g, Collections.singleton("b")).getLCA("h", "b"));
    }

    // TODO: keep?
    public Graph<Integer, DefaultEdge> generateForest(int t, int n, Random random){
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        for (int i = 0; i < t - 1; i++) {
            g.addVertex();
        }

        BarabasiAlbertGraphGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertGraphGenerator<>(100, 1, n - t + 1, random);

        generator.generateGraph(g, null);

        return g;
    }

    @Test
    public void randomHugeConnectedTree(){
        final int N = 100_000;
        final int Q = 200_000;

        Random random = new Random(0x88);

        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertForestGenerator<>(1, N, random);

        generator.generateGraph(g, null);

        List<Integer> vertexList = new ArrayList<>(g.vertexSet());

        LCAAlgorithm<Integer> lcaAlgorithm1 = createSolver(g, Collections.singleton(vertexList.get(0)));
        LCAAlgorithm<Integer> lcaAlgorithm2;

        if (lcaAlgorithm1 instanceof EulerTourRMQLCAFinder)
            lcaAlgorithm2 = new BinaryLiftingLCAFinder<>(g, vertexList.get(0));
        else
            lcaAlgorithm2 = new EulerTourRMQLCAFinder<>(g, vertexList.get(0));

        List<Pair<Integer, Integer>> queries = generateQueries(Q, vertexList, random);

        List<Integer> lcas1 = lcaAlgorithm1.getLCAs(queries);
        List<Integer> lcas2 = lcaAlgorithm2.getLCAs(queries);

        for (int i = 0; i < Q; i++) {
            Assert.assertEquals(lcas1.get(i), lcas2.get(i));
        }
    }

    private void generatePossiblyDisconnectedTree(int N, Random random, Graph<Integer, DefaultEdge> g, List<Integer> vertices){
        Collections.shuffle(vertices, random);
        UnionFind<Integer> unionFind = new UnionFind<>(Collections.emptySet());

        for (int i = 0; i < N; i++) {
            g.addVertex(vertices.get(i));
            unionFind.addElement(vertices.get(i));
        }

        int M = N / 2 + random.nextInt(N / 2);

        for (int i = 0; i < M; i++) {
            int a = vertices.get(random.nextInt(N));
            int b = vertices.get(random.nextInt(N));

            if (!unionFind.inSameSet(a, b)){
                unionFind.union(a, b);
                g.addEdge(a, b);
            }
        }
    }

    public static <V> List<Pair<V, V>> generateQueries(int Q, List<V> vertexList, Random random){
        List<Pair<V, V>> queries = new ArrayList<>(Q);

        for (int i = 0; i < Q; i++) {
            V a = vertexList.get(random.nextInt(vertexList.size()));
            V b = vertexList.get(random.nextInt(vertexList.size()));

            queries.add(Pair.of(a, b));
        }

        return queries;
    }

    @Test
    public void randomHugePossiblyDisconnectedTree(){
        final int N = 100_000;
        final int Q = 200_000;

        Random random = new Random(0x55);

        final int NUM_TREES = 100 + random.nextInt(200);

        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertForestGenerator<>(NUM_TREES, N, random);

        generator.generateGraph(g, null);

        List<Integer> vertexList = new ArrayList<>(g.vertexSet());

        ConnectivityInspector<Integer, DefaultEdge> connectivityInspector = new ConnectivityInspector<>(g);

        List<Set<Integer>> connectedComponents = connectivityInspector.connectedSets();

        Set<Integer> roots = connectedComponents.stream().map(component -> component.iterator().next()).collect(Collectors.toSet());

        LCAAlgorithm<Integer> lcaAlgorithm1 = createSolver(g, roots);
        LCAAlgorithm<Integer> lcaAlgorithm2;

        if (lcaAlgorithm1 instanceof EulerTourRMQLCAFinder)
            lcaAlgorithm2 = new BinaryLiftingLCAFinder<>(g, roots);
        else
            lcaAlgorithm2 = new EulerTourRMQLCAFinder<>(g, roots);

        List<Pair<Integer, Integer>> queries = generateQueries(Q, vertexList, random);

        List<Integer> lcas1 = lcaAlgorithm1.getLCAs(queries);
        List<Integer> lcas2 = lcaAlgorithm2.getLCAs(queries);

        for (int i = 0; i < Q; i++) {
            Assert.assertEquals(lcas1.get(i), lcas2.get(i));
        }
    }

    @Test
    public void testSmallConnectedTrees(){
        Random random = new Random(0x88);
        final int TESTS = 10_000;
        final int Q = 50;

        for (int test = 0; test < TESTS; test++) {
            final int N = 10 + random.nextInt(100);

            GraphGenerator<Integer, DefaultEdge, Integer> gen =
                    new BarabasiAlbertForestGenerator<>(1, N, random.nextInt());

            Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
                    SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            gen.generateGraph(g);

            List<Integer> vertexList = new ArrayList<>(g.vertexSet());
            Set<Integer> roots = Collections.singleton(vertexList.get(0));

            LCAAlgorithm<Integer> lcaAlgorithm1 = createSolver(g, roots);
            LCAAlgorithm<Integer> lcaAlgorithm2;

            if (lcaAlgorithm1 instanceof EulerTourRMQLCAFinder)
                lcaAlgorithm2 = new BinaryLiftingLCAFinder<>(g, roots);
            else
                lcaAlgorithm2 = new EulerTourRMQLCAFinder<>(g, roots);

            List<Pair<Integer, Integer>> queries = generateQueries(Q, vertexList, random);

            List<Integer> lcas1 = lcaAlgorithm1.getLCAs(queries);
            List<Integer> lcas2 = lcaAlgorithm2.getLCAs(queries);

            for (int i = 0; i < Q; i++) {
                Assert.assertEquals(lcas1.get(i), lcas2.get(i));
            }
        }
    }

    @Test
    public void testSmallDisconnectedTrees(){
        Random random = new Random(0x88);
        final int TESTS = 10_000;
        final int Q = 50;

        for (int test = 0; test < TESTS; test++) {
            final int N = 10 + random.nextInt(200);

            GraphGenerator<Integer, DefaultEdge, Integer> gen =
                    new BarabasiAlbertForestGenerator<>(N / 10, N, random.nextInt());

            Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
                    SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            gen.generateGraph(g);

            Set<Integer> roots = new ConnectivityInspector<>(g).connectedSets().stream()
                    .map(x -> x.iterator().next()).collect(Collectors.toSet());

            LCAAlgorithm<Integer> lcaAlgorithm1 = createSolver(g, roots);
            LCAAlgorithm<Integer> lcaAlgorithm2;

            if (lcaAlgorithm1 instanceof EulerTourRMQLCAFinder)
                lcaAlgorithm2 = new BinaryLiftingLCAFinder<>(g, roots);
            else
                lcaAlgorithm2 = new EulerTourRMQLCAFinder<>(g, roots);

            List<Pair<Integer, Integer>> queries = generateQueries(Q, new ArrayList<>(g.vertexSet()), random);

            List<Integer> lcas1 = lcaAlgorithm1.getLCAs(queries);
            List<Integer> lcas2 = lcaAlgorithm2.getLCAs(queries);

            for (int i = 0; i < Q; i++) {
                Assert.assertEquals(lcas1.get(i), lcas2.get(i));
            }
        }
    }
}