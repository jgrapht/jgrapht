/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.*;
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;
import org.junit.experimental.categories.*;

import java.util.*;
import java.util.stream.*;

import static org.jgrapht.alg.isomorphism.IsomorphismTestUtil.*;

/**
 * Tests for {@link AHUForestIsomorphismInspector}
 *
 * @author Alexandru Valeanu
 */
public class AHUForestIsomorphismInspectorTest
{

    @Test(expected = UnsupportedOperationException.class)
    public void testMissingSupplier()
    {
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("1");
        tree1.addVertex("2");
        tree1.addEdge("1", "2");
        tree1.addVertex("3");

        AHUForestIsomorphismInspector<String,
            DefaultEdge> forestIsomorphism = new AHUForestIsomorphismInspector<>(
                tree1, new HashSet<>(Arrays.asList("1", "2")), tree1,
                new HashSet<>(Arrays.asList("1", "2")));

        forestIsomorphism.isomorphismExists();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyGraph()
    {
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        Set<String> roots = new HashSet<>();

        AHUForestIsomorphismInspector<String, DefaultEdge> isomorphism =
            new AHUForestIsomorphismInspector<>(tree1, roots, tree1, roots);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<String, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree1, treeMapping));
    }

    @Test
    public void testSingleVertex()
    {
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("1");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);
        tree2.addVertex("A");

        AHUForestIsomorphismInspector<String, DefaultEdge> isomorphism =
            new AHUForestIsomorphismInspector<>(
                tree1, Collections.singleton("1"), tree2, Collections.singleton("A"));

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<String, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test(expected = NullPointerException.class)
    public void testNullGraphs()
    {
        new AHUForestIsomorphismInspector<String, DefaultEdge>(null, new HashSet<>(), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRoot()
    {
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("a");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("A");

        AHUForestIsomorphismInspector<String, DefaultEdge> isomorphism =
            new AHUForestIsomorphismInspector<>(
                tree1, Collections.singleton("b"), tree2, Collections.singleton("A"));

        isomorphism.getMapping();
    }

    @Test
    public void testSmallForest()
    {
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        tree1.addVertex("a");
        tree1.addVertex("b");
        tree1.addVertex("c");

        tree1.addEdge("a", "b");
        tree1.addEdge("a", "c");

        tree1.addVertex("d");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        tree2.addVertex("A");
        tree2.addVertex("B");
        tree2.addVertex("C");

        tree2.addEdge("B", "A");
        tree2.addEdge("A", "C");

        tree2.addVertex("D");

        AHUForestIsomorphismInspector<String,
            DefaultEdge> forestIsomorphism = new AHUForestIsomorphismInspector<>(
                tree1, new HashSet<>(Arrays.asList("b", "d")), tree2,
                new HashSet<>(Arrays.asList("A", "D")));

        Assert.assertFalse(forestIsomorphism.isomorphismExists());
    }

    @Test
    public void testSmallForest2()
    {
        Map<Integer, Integer> map = new HashMap<>();

        Pair<Graph<Integer, DefaultEdge>,
            Graph<Integer, DefaultEdge>> pair = parseGraph(
                "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]",
                "[{2,1}, {3,0}, {4,0}, {5,1}, {6,1}, {7,0}, {8,1}, {9,6}, {10,1}, {11,6}, "
                    + "{12,0}, {13,7}, {14,5}, {15,1}, {16,0}, {17,0}, {18,17}, {19,7}]",
                "{0=12, 1=10, 2=0, 3=8, 4=3, 5=16, 6=7, 7=18, 8=11, 9=17, 10=6, 11=14, 12=9, "
                    + "13=5, 14=15, 15=2, 16=19, 17=13, 18=4, 19=1}",
                map);

        Graph<Integer, DefaultEdge> forest1 = pair.getFirst();
        Graph<Integer, DefaultEdge> forest2 = pair.getSecond();

        Set<Integer> roots1 = new ConnectivityInspector<>(forest1)
            .connectedSets().stream().map(x -> x.iterator().next()).collect(Collectors.toSet());

        Set<Integer> roots2 = roots1.stream().map(map::get).collect(Collectors.toSet());

        AHUForestIsomorphismInspector<Integer, DefaultEdge> isomorphism =
            new AHUForestIsomorphismInspector<>(forest1, roots1, forest2, roots2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();

        Assert.assertTrue(areIsomorphic(forest1, forest2, treeMapping));
    }

    @Test
    @Category(SlowTests.class)
    public void testHugeNumberOfChildren()
    {
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
            new AHUForestIsomorphismInspector<>(
                tree1, Collections.singleton(1), tree2, Collections.singleton(mapping.get(1)));

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    @Category(SlowTests.class)
    public void testRandomForests()
    {
        Random random = new Random(0x2312);
        final int NUM_TESTS = 1000;

        for (int test = 0; test < NUM_TESTS; test++) {
            final int N = 10 + random.nextInt(200);

            Graph<Integer, DefaultEdge> tree1 = generateForest(N, random);

            Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair =
                generateIsomorphicGraph(tree1, random);

            Graph<Integer, DefaultEdge> tree2 = pair.getFirst();

            Set<Integer> roots1 = new ConnectivityInspector<>(tree1)
                .connectedSets().stream().map(x -> x.iterator().next()).collect(Collectors.toSet());

            Set<Integer> roots2 =
                roots1.stream().map(x -> pair.getSecond().get(x)).collect(Collectors.toSet());

            AHUForestIsomorphismInspector<Integer, DefaultEdge> isomorphism =
                new AHUForestIsomorphismInspector<>(tree1, roots1, tree2, roots2);

            Assert.assertTrue(isomorphism.isomorphismExists());
            IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();

            Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
        }
    }

    @Test
    @Category(SlowTests.class)
    public void testHugeRandomForest()
    {
        final int N = 50_000;
        Graph<Integer, DefaultEdge> tree1 = generateForest(N, new Random(0x88));

        Pair<Graph<Integer, DefaultEdge>, Map<Integer, Integer>> pair =
            generateIsomorphicGraph(tree1, new Random(0x88));

        Graph<Integer, DefaultEdge> tree2 = pair.getFirst();

        Set<Integer> roots1 = new ConnectivityInspector<>(tree1)
            .connectedSets().stream().map(x -> x.iterator().next()).collect(Collectors.toSet());

        Set<Integer> roots2 =
            roots1.stream().map(x -> pair.getSecond().get(x)).collect(Collectors.toSet());

        AHUForestIsomorphismInspector<Integer, DefaultEdge> isomorphism =
            new AHUForestIsomorphismInspector<>(tree1, roots1, tree2, roots2);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }
}
