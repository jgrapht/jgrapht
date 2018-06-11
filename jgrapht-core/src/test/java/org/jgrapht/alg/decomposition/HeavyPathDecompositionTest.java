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
package org.jgrapht.alg.decomposition;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.generate.BarabasiAlbertForestGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tests for {@link HeavyPathDecomposition}
 *
 * @author Alexandru Valeanu
 */
public class HeavyPathDecompositionTest {

    public static int log2(int n) // returns 0 for n=0
    {
        int log = 0;
        if( ( n & 0xffff0000 ) != 0 ) { n >>>= 16; log = 16; }
        if( n >= 256 ) { n >>>= 8; log += 8; }
        if( n >= 16  ) { n >>>= 4; log += 4; }
        if( n >= 4   ) { n >>>= 2; log += 2; }
        return log + ( n >>> 1 );
    }

    private static <V, E> int countMaxPath(Set<V> vertexSet, Map<V, V> father, List<List<V>> paths){
        Map<V, Integer> whichPath = new HashMap<>();

        for (int i = 0; i < paths.size(); i++) {
            List<V> path = paths.get(i);

            for (int j = 0; j < path.size(); j++) {
                whichPath.put(path.get(j), i);
            }
        }

        int maxim = 0;

        for (V v: vertexSet){
            if (whichPath.containsKey(v)){
                int lastPath = -1;
                int cnt = 0;

                while (v != null){
                    if (lastPath != whichPath.get(v)){
                        lastPath = whichPath.get(v);
                        cnt++;
                    }

                    v = father.getOrDefault(v, null);
                }

                maxim = Math.max(maxim, cnt);
            }
        }

        return maxim;
    }

    private static <V, E> boolean isValidDecomposition(Graph<V, E> graph, Set<V> roots, List<List<V>> paths){
        Map<V, Integer> whichPath = new HashMap<>();

        for (int i = 0; i < paths.size(); i++) {
            List<V> path = paths.get(i);

            for (int j = 0; j < path.size(); j++) {
                // Check if a vertex appear more than once in the decomposition
                if (whichPath.containsKey(path.get(j)))
                    return false;

                whichPath.put(path.get(j), i);

                // Check if the path is actually a valid path in the graph
                if (j > 0){
                    if (!graph.containsEdge(path.get(j - 1), path.get(j))){
                        return false;
                    }
                }
            }
        }

        ConnectivityInspector<V, E> connectivityInspector = new ConnectivityInspector<>(graph);

        // Check if every reachable vertex from a root is in a path
        for (V root: roots){
            for (V v: connectivityInspector.connectedSetOf(root))
                if (!whichPath.containsKey(v)){
                    return false;
                }
        }

        return true;
    }

    @Test
    public void testLineGraph(){
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= 11; i++)
            graph.addVertex(i);

        for (int i = 1; i < 11; i++)
            graph.addEdge(i, i + 1);

        HeavyPathDecomposition<Integer, DefaultEdge> heavyPathDecomposition = new HeavyPathDecomposition<>(graph, 1);

        Assert.assertTrue(isValidDecomposition(graph, Collections.singleton(1), heavyPathDecomposition.getPaths()));
        Assert.assertEquals(1, heavyPathDecomposition.numberOfPaths());
    }

    @Test
    public void testLineGraph2(){
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= 11; i++)
            graph.addVertex(i);

        for (int i = 1; i < 11; i++)
            graph.addEdge(i, i + 1);

        HeavyPathDecomposition<Integer, DefaultEdge> heavyPathDecomposition = new HeavyPathDecomposition<>(graph, 5);

        Assert.assertTrue(isValidDecomposition(graph, Collections.singleton(1), heavyPathDecomposition.getPaths()));
        Assert.assertEquals(2, heavyPathDecomposition.numberOfPaths());
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

        HeavyPathDecomposition<Integer, DefaultEdge> heavyPathDecomposition = new HeavyPathDecomposition<>(graph, 1);

        Assert.assertTrue(isValidDecomposition(graph, Collections.singleton(1), heavyPathDecomposition.getPaths()));

//        System.out.println(heavyPathDecomposition.getPaths());
//
//        System.out.println(countMaxPath(graph.vertexSet(), heavyPathDecomposition.getFather(),
//                heavyPathDecomposition.getPaths()));
//
//        System.out.println(log2(graph.vertexSet().size()));
    }

    @Test
    public void testDisconnectedSmallGraph(){
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        graph.addVertex(1);
        graph.addVertex(2);
        graph.addEdge(1, 2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addEdge(3, 4);

        HeavyPathDecomposition<Integer, DefaultEdge> heavyPathDecomposition = new HeavyPathDecomposition<>(graph, 1);

        Assert.assertTrue(isValidDecomposition(graph, Collections.singleton(1), heavyPathDecomposition.getPaths()));
        Assert.assertEquals(1, heavyPathDecomposition.numberOfPaths());
    }

    @Test
    public void testRandomTrees(){
        final int NUM_TESTS = 100;
        Random random = new Random(0x2882);

        for (int test = 0; test < NUM_TESTS; test++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                    SupplierUtil.createIntegerSupplier(0), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                    new BarabasiAlbertForestGenerator<>(1,
                            4096 + random.nextInt(1 << 13), random);

            generator.generateGraph(graph, null);

           Set<Integer> roots = Collections.singleton(graph.vertexSet().iterator().next());

            HeavyPathDecomposition<Integer, DefaultEdge> heavyPathDecomposition =
                    new HeavyPathDecomposition<>(graph, roots);

            Assert.assertTrue(isValidDecomposition(graph, roots, heavyPathDecomposition.getPaths()));

//            System.out.println(countMaxPath(graph.vertexSet(), heavyPathDecomposition.getFather(),
//                    heavyPathDecomposition.getPaths()));
//
//            System.out.println(log2(graph.vertexSet().size()));
        }
    }

    @Test
    public void testRandomForests(){
        final int NUM_TESTS = 1000;
        Random random = new Random(0x1881);

        for (int test = 0; test < NUM_TESTS; test++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                    SupplierUtil.createIntegerSupplier(0), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            BarabasiAlbertForestGenerator<Integer, DefaultEdge> generator =
                    new BarabasiAlbertForestGenerator<>(1 + random.nextInt(20),
                            50 + random.nextInt(1 << 11), random);

            generator.generateGraph(graph, null);

            ConnectivityInspector<Integer, DefaultEdge> connectivityInspector = new ConnectivityInspector<>(graph);
            List<Set<Integer>> connectedComponents = connectivityInspector.connectedSets();
            Set<Integer> roots = connectedComponents.stream().map(component -> component.iterator().next()).collect(Collectors.toSet());

            HeavyPathDecomposition<Integer, DefaultEdge> heavyPathDecomposition =
                    new HeavyPathDecomposition<>(graph, roots);

            Assert.assertTrue(isValidDecomposition(graph, roots, heavyPathDecomposition.getPaths()));

//            System.out.println(countMaxPath(graph.vertexSet(), heavyPathDecomposition.getFather(),
//                    heavyPathDecomposition.getPaths()));
//
//            System.out.println(log2(graph.vertexSet().size()));
        }
    }
}