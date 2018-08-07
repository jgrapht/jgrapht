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
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import static org.jgrapht.alg.isomorphism.AHUForestIsomorphismInspectorTest.parseGraph;
import static org.jgrapht.alg.isomorphism.AHURootedTreeIsomorphismInspectorTest.*;

/**
 * Tests for {@link AHUUnrootedTreeIsomorphismInspector}
 *
 * @author Alexandru Valeanu
 */
public class AHUUnrootedTreeIsomorphismInspectorTest {

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyGraph(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);

        AHUUnrootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree1);

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

        AHUUnrootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<String, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test(expected = NullPointerException.class)
    public void testNullGraphs(){
        AHUUnrootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testOnlyOneNullGraph(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("a");

        AHUUnrootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, null);
    }

    @Test
    public void testUnrootedIsomorphism(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("1");
        tree1.addVertex("2");
        tree1.addVertex("3");

        tree1.addEdge("1", "2");
        tree1.addEdge("2", "3");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);
        tree2.addVertex("A");
        tree2.addVertex("B");
        tree2.addVertex("C");

        tree2.addEdge("A", "B");
        tree2.addEdge("B", "C");

        AHUUnrootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<String, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testCornerCase(){
        Graph<Integer, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 0; i <= 10; i++)
            tree1.addVertex(i);

        tree1.addEdge(10, 0);
        tree1.addEdge(10, 1);
        tree1.addEdge(10, 2);
        tree1.addEdge(10, 3);

        tree1.addEdge(0, 4);
        tree1.addEdge(0, 6);
        tree1.addEdge(0, 7);

        tree1.addEdge(2, 5);
        tree1.addEdge(5, 8);

        tree1.addEdge(4, 9);

        Graph<Integer, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 0; i <= 9; i++)
            tree2.addVertex(i);

        tree2.addVertex(11);

        tree2.addEdge(11, 1);
        tree2.addEdge(11, 2);
        tree2.addEdge(11, 4);
        tree2.addEdge(11, 7);

        tree2.addEdge(4, 3);
        tree2.addEdge(4, 6);
        tree2.addEdge(4, 0);

        tree2.addEdge(6, 5);

        tree2.addEdge(7, 8);
        tree2.addEdge(8, 9);

        AHUUnrootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testCornerCase2(){
        Graph<Integer, DefaultEdge> tree1 =
                parseGraph("[1, 2, 5, 6, 8, 9, 10, 11, 14, 15]",
                        "[{2,1}, {5,1}, {6,1}, {8,1}, {9,6}, {10,1}, {11,6}, {14,5}, {15,1}]");

        Graph<Integer, DefaultEdge> tree2 =
                parseGraph("[1, 18, 3, 19, 4, 5, 8, 9, 12, 13]",
                        "[{8,12}, {3,12}, {18,12}, {9,12}, {5,18}, {19,12}, {13,12}, {4,13}, {1,18}]");

        AHUUnrootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testCornerCase3(){
        Graph<Integer, DefaultEdge> tree1 =
                parseGraph("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]",
                        "[{1,0}, {2,0}, {3,0}, {4,2}, {5,0}, {6,5}, {7,2}, {8,5}, {9,4}, {10,6}, {11,4}, {12,0}, {13,0}]");

        Graph<Integer, DefaultEdge> tree2 =
                parseGraph("[10, 2, 12, 7, 5, 3, 4, 0, 6, 1, 13, 9, 8, 11]",
                        "[{2,10}, {12,10}, {7,10}, {5,12}, {3,10}, {4,3}, {0,12}, {6,3}, {1,5}, {13,4}, {9,5}, {8,10}, {11,10}]");

        AHUUnrootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testNonIsomorphicAsUnrootedButAsRooted(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);

        for (char c = 'A'; c <= 'C'; c++) {
            tree1.addVertex(String.valueOf(c));
            tree2.addVertex(String.valueOf((char)(c + ' ')));
        }

        tree1.addEdge("A", "B");
        tree1.addEdge("B", "C");

        tree2.addEdge("a", "b");
        tree2.addEdge("b", "c");

        // They are not isomorphic as rooted trees

        AHURootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
                new AHURootedTreeIsomorphismInspector<>(tree1, "A", tree2, "b");

        System.out.println();

        Assert.assertFalse(isomorphism.isomorphismExists());
        Assert.assertNull(isomorphism.getMapping());

        // But they are isomorphic as unrooted trees

        AHUUnrootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism2 =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertTrue(isomorphism2.isomorphismExists());
        IsomorphicGraphMapping<String, DefaultEdge> treeMapping = isomorphism2.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testSmall(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);

        for (char c = 'A'; c <= 'E'; c++) {
            tree1.addVertex(String.valueOf(c));
            tree2.addVertex(String.valueOf((char)(c + ' ')));
        }

        tree1.addEdge("A", "B");
        tree1.addEdge("A", "C");
        tree1.addEdge("C", "D");
        tree1.addEdge("C", "E");

        tree2.addEdge("a", "b");
        tree2.addEdge("a", "c");
        tree2.addEdge("b", "e");
        tree2.addEdge("b", "d");

        AHUUnrootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<String, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testSmall2(){
        Graph<Integer, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= 13; i++) {
            tree1.addVertex(i);
        }

        tree1.addEdge(1, 2);
        tree1.addEdge(1, 3);

        tree1.addEdge(2, 4);
        tree1.addEdge(2, 5);
        tree1.addEdge(2, 6);

        tree1.addEdge(3, 7);
        tree1.addEdge(3, 8);
        tree1.addEdge(3, 9);

        tree1.addEdge(8, 10);
        tree1.addEdge(8, 11);

        tree1.addEdge(9, 12);
        tree1.addEdge(9, 13);

        Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair =
                generateIsomorphicGraph(tree1, new Random(0x88));

        Graph<Integer, DefaultEdge> tree2 = pair.getFirst();

        AHUUnrootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testDisconnectedTree(){
        Graph<Integer, DefaultEdge> tree1 = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        tree1.addVertex(1);
        tree1.addVertex(2);
        tree1.addVertex(3);

        tree1.addEdge(1, 2);

        Graph<Integer, DefaultEdge> tree2 = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        tree2.addVertex(11);
        tree2.addVertex(21);
        tree2.addVertex(31);

        tree2.addEdge(11, 21);

        AHUUnrootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertFalse(isomorphism.isomorphismExists());
        Assert.assertNull(isomorphism.getMapping());

        // Test as forest

        AHUForestIsomorphismInspector<Integer, DefaultEdge> forestIsomorphism =
                new AHUForestIsomorphismInspector<>(tree1, new HashSet<>(Arrays.asList(1, 3)),
                        tree2, new HashSet<>(Arrays.asList(11, 31)));

        Assert.assertTrue(forestIsomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = forestIsomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRoot(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("a");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("A");

        AHUUnrootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        isomorphism.getMapping();
    }

    @Test
    public void testCompositionOfMappings(){
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("1");
        tree1.addVertex("2");
        tree1.addEdge("1", "2");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);
        tree2.addVertex("a");
        tree2.addVertex("b");
        tree2.addEdge("a", "b");

        Graph<String, DefaultEdge> tree3 = new SimpleGraph<>(DefaultEdge.class);
        tree3.addVertex("A");
        tree3.addVertex("B");
        tree3.addEdge("A", "B");

        AHUUnrootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<String, DefaultEdge> mapping12 = isomorphism.getMapping();

        isomorphism = new AHUUnrootedTreeIsomorphismInspector<>(tree2, tree3);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<String, DefaultEdge> mapping23 = isomorphism.getMapping();

        IsomorphicGraphMapping<String, DefaultEdge> mapping13 = mapping12.compose(mapping23);

        Assert.assertTrue(areIsomorphic(tree1, tree3, mapping13));
    }

    @Test
    public void testCompositionOfRandomMappings(){
        final int NUM_TESTS = 1000;
        Random random = new Random(0x11_88_11);

        for (int test = 0; test < NUM_TESTS; test++) {
            final int N = 10 + random.nextInt(150);

            Graph<Integer, DefaultEdge> tree1 = generateTree(N, random);
            Graph<Integer, DefaultEdge> tree2 = generateIsomorphicGraph(tree1, random).getFirst();
            Graph<Integer, DefaultEdge> tree3 = generateIsomorphicGraph(tree2, random).getFirst();

            AHUUnrootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                    new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

            IsomorphicGraphMapping<Integer, DefaultEdge> mapping12 = isomorphism.getMapping();

            isomorphism = new AHUUnrootedTreeIsomorphismInspector<>(tree2, tree3);
            IsomorphicGraphMapping<Integer, DefaultEdge> mapping23 = isomorphism.getMapping();

            IsomorphicGraphMapping<Integer, DefaultEdge> mapping13 = mapping12.compose(mapping23);

            Assert.assertTrue(areIsomorphic(tree1, tree3, mapping13));
        }
    }

    @Test
    public void testLineGraph(){
        final int N = 20_000;
        Graph<Integer, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= N; i++) {
            tree1.addVertex(i);
        }

        for (int i = 1; i <= N - 1; i++) {
            tree1.addEdge(i, i + 1);
        }

        Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair =
                generateIsomorphicGraph(tree1, new Random(0x88));

        Graph<Integer, DefaultEdge> tree2 = pair.getFirst();
        Map<Integer, Integer> mapping = pair.getSecond();

        AHUUnrootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
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

        AHUUnrootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testHugeRandomTree(){
        final int N = 50_000;
        Graph<Integer, DefaultEdge> tree1 = generateTree(N, new Random(0x88));

        Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair =
                generateIsomorphicGraph(tree1, new Random(0x88));

        Graph<Integer, DefaultEdge> tree2 = pair.getFirst();

        AHUUnrootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));

        isomorphism = new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testRandomTrees(){
        Random random = new Random(0x88_88);
        final int NUM_TESTS = 1500;

        for (int test = 0; test < NUM_TESTS; test++) {
            final int N = 10 + random.nextInt(100);

            Graph<Integer, DefaultEdge> tree1 = generateTree(N, random);

            Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair = generateIsomorphicGraph(tree1, random);

            Graph<Integer, DefaultEdge> tree2 = pair.getFirst();

            AHUUnrootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                    new AHUUnrootedTreeIsomorphismInspector<>(tree1, tree2);

            Assert.assertTrue(isomorphism.isomorphismExists());
            IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
            Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
        }
    }
}