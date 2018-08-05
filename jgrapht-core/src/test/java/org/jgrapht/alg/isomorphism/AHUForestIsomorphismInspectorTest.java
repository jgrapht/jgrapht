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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.generate.BarabasiAlbertForestGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.jgrapht.alg.isomorphism.AHURootedTreeIsomorphismInspectorTest.*;

/**
 * Tests for {@link AHUForestIsomorphismInspector}
 *
 * @author Alexandru Valeanu
 */
public class AHUForestIsomorphismInspectorTest {

    public static Graph<Integer, DefaultEdge> parseGraph(String vertices, String edges){
        Graph<Integer, DefaultEdge> forest = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(-100),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        vertices = vertices.substring(1, vertices.length() - 1);

        for (String s: vertices.split(", "))
            forest.addVertex(Integer.valueOf(s));

        edges = edges.substring(1, edges.length() - 1);

        for (String s: edges.split(", ")){
            String[] ends = s.substring(1, s.length() - 1).split(",");
            forest.addEdge(Integer.valueOf(ends[0]), Integer.valueOf(ends[1]));
        }

        return forest;
    }

    public static Pair<Graph<Integer, DefaultEdge>, Graph<Integer, DefaultEdge>> parseGraph(String vertices, String edges,
                                                                              String mapping, Map<Integer, Integer> map){

        Graph<Integer, DefaultEdge> forest = parseGraph(vertices, edges);

        for (String s: mapping.substring(1, mapping.length() - 1).split(", ")){
            String[] ends = s.split("=");
            map.put(Integer.valueOf(ends[0]), Integer.valueOf(ends[1]));
        }

        return Pair.of(forest, generateMappedGraph(forest, map));
    }

    public static Graph<Integer, DefaultEdge> generateForest(int N, Random random){
        BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertForestGenerator<>(N / 10, N, random);

        Graph<Integer, DefaultEdge> forest = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        generator.generateGraph(forest);

        return forest;
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testMissingSupplier(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("1");
        tree1.addVertex("2");
        tree1.addEdge("1", "2");
        tree1.addVertex("3");

        AHUForestIsomorphismInspector<String, DefaultEdge> forestIsomorphism =
                new AHUForestIsomorphismInspector<>(tree1, new HashSet<>(Arrays.asList("1", "2")),
                        tree1, new HashSet<>(Arrays.asList("1", "2")));

        forestIsomorphism.isomorphismExists();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyGraph(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        Set<String> roots = new HashSet<>();

        AHUForestIsomorphismInspector<String, DefaultEdge> isomorphism =
                new AHUForestIsomorphismInspector<>(tree1, roots, tree1, roots);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<String, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree1, treeMapping));
    }

    @Test
    public void testSingleVertex(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("1");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);
        tree2.addVertex("A");

        AHUForestIsomorphismInspector<String, DefaultEdge> isomorphism =
                new AHUForestIsomorphismInspector<>(tree1, Collections.singleton("1"),
                        tree2, Collections.singleton("A"));

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<String, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test(expected = NullPointerException.class)
    public void testNullGraphs(){
        new AHUForestIsomorphismInspector<String, DefaultEdge>(null, new HashSet<>(), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRoot(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("a");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("A");

        AHUForestIsomorphismInspector<String, DefaultEdge> isomorphism =
                new AHUForestIsomorphismInspector<>(tree1, Collections.singleton("b"),
                        tree2, Collections.singleton("A"));

        isomorphism.getMapping();
    }

    @Test
    public void testSmallForest(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(SupplierUtil.createStringSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        tree1.addVertex("a");
        tree1.addVertex("b");
        tree1.addVertex("c");

        tree1.addEdge("a", "b");
        tree1.addEdge("a", "c");

        tree1.addVertex("d");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(SupplierUtil.createStringSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        tree2.addVertex("A");
        tree2.addVertex("B");
        tree2.addVertex("C");


        tree2.addEdge("B", "A");
        tree2.addEdge("A", "C");

        tree2.addVertex("D");

        AHUForestIsomorphismInspector<String, DefaultEdge> forestIsomorphism =
                new AHUForestIsomorphismInspector<>(tree1, new HashSet<>(Arrays.asList("b", "d")),
                        tree2, new HashSet<>(Arrays.asList("A", "D")));

        Assert.assertFalse(forestIsomorphism.isomorphismExists());
    }

    @Test
    public void testSmallForest2(){
        Map<Integer, Integer> map = new HashMap<>();

        Pair<Graph<Integer, DefaultEdge>, Graph<Integer, DefaultEdge>> pair =
                parseGraph("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]",
                        "[{2,1}, {3,0}, {4,0}, {5,1}, {6,1}, {7,0}, {8,1}, {9,6}, {10,1}, {11,6}, " +
                                "{12,0}, {13,7}, {14,5}, {15,1}, {16,0}, {17,0}, {18,17}, {19,7}]",
                        "{0=12, 1=10, 2=0, 3=8, 4=3, 5=16, 6=7, 7=18, 8=11, 9=17, 10=6, 11=14, 12=9, " +
                                "13=5, 14=15, 15=2, 16=19, 17=13, 18=4, 19=1}", map);

        Graph<Integer, DefaultEdge> forest1 = pair.getFirst();
        Graph<Integer, DefaultEdge> forest2 = pair.getSecond();

        Set<Integer> roots1 = new ConnectivityInspector<>(forest1).connectedSets()
                .stream().map(x -> x.iterator().next()).collect(Collectors.toSet());

        Set<Integer> roots2 = roots1.stream().map(map::get).collect(Collectors.toSet());

        AHUForestIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                new AHUForestIsomorphismInspector<>(forest1, roots1, forest2, roots2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();

        Assert.assertTrue(areIsomorphic(forest1, forest2, treeMapping));
    }

    @Test
    public void testHugeNumberOfChildren(){
        final int N = 100_000;
        Graph<Integer, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= N; i++) {
            tree1.addVertex(i);
        }

        for (int i = 2; i <= N; i++) {
            tree1.addEdge(1, i);
        }

        Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair =
                generateIsomorphicGraph(tree1, new Random(0x2882));

        Graph<Integer, DefaultEdge> tree2 = pair.getFirst();
        Map<Integer, Integer> mapping = pair.getSecond();

        AHUForestIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                new AHUForestIsomorphismInspector<>(tree1, Collections.singleton(1),
                        tree2, Collections.singleton(mapping.get(1)));

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testRandomForests(){
        Random random = new Random(0x2312);
        final int NUM_TESTS = 1000;

        for (int test = 0; test < NUM_TESTS; test++) {
            final int N = 10 + random.nextInt(200);

            Graph<Integer, DefaultEdge> tree1 = generateForest(N, random);

            Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair = generateIsomorphicGraph(tree1, random);

            Graph<Integer, DefaultEdge> tree2 = pair.getFirst();

            Set<Integer> roots1 = new ConnectivityInspector<>(tree1).connectedSets()
                    .stream().map(x -> x.iterator().next()).collect(Collectors.toSet());

            Set<Integer> roots2 = roots1.stream().map(x -> pair.getSecond().get(x)).collect(Collectors.toSet());

            AHUForestIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                    new AHUForestIsomorphismInspector<>(tree1, roots1, tree2, roots2);

            Assert.assertTrue(isomorphism.isomorphismExists());
            IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();

            Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
        }
    }

    @Test
    public void testHugeRandomForest(){
        final int N = 50_000;
        Graph<Integer, DefaultEdge> tree1 = generateForest(N, new Random(0x88));

        Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair =
                generateIsomorphicGraph(tree1, new Random(0x88));

        Graph<Integer, DefaultEdge> tree2 = pair.getFirst();

        Set<Integer> roots1 = new ConnectivityInspector<>(tree1).connectedSets()
                .stream().map(x -> x.iterator().next()).collect(Collectors.toSet());

        Set<Integer> roots2 = roots1.stream().map(x -> pair.getSecond().get(x)).collect(Collectors.toSet());

        AHUForestIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                new AHUForestIsomorphismInspector<>(tree1, roots1, tree2, roots2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }
}