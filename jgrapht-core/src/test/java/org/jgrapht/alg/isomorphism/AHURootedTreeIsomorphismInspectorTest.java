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
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.jgrapht.alg.isomorphism.IsomorphismTestUtil.*;

/**
 * Tests for {@link AHURootedTreeIsomorphismInspector}
 *
 * @author Alexandru Valeanu
 */
public class AHURootedTreeIsomorphismInspectorTest
{

    @Test
    public void testSingleVertex()
    {
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("1");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);
        tree2.addVertex("A");

        AHURootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
            new AHURootedTreeIsomorphismInspector<>(tree1, "1", tree2, "A");

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<String, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test(expected = NullPointerException.class)
    public void testNullGraphs()
    {
        AHURootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
            new AHURootedTreeIsomorphismInspector<>(null, null, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testOnlyOneNullGraph()
    {
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);

        AHURootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
            new AHURootedTreeIsomorphismInspector<>(tree1, null, null, null);
    }

    @Test
    public void testCornerCase()
    {
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

        AHURootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
            new AHURootedTreeIsomorphismInspector<>(tree1, 10, tree2, 11);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testCornerCase2()
    {
        Graph<Integer,
            DefaultEdge> tree1 = parseGraph(
                "[1, 2, 5, 6, 8, 9, 10, 11, 14, 15]",
                "[{2,1}, {5,1}, {6,1}, {8,1}, {9,6}, {10,1}, {11,6}, {14,5}, {15,1}]");

        Graph<Integer,
            DefaultEdge> tree2 = parseGraph(
                "[1, 18, 3, 19, 4, 5, 8, 9, 12, 13]",
                "[{8,12}, {3,12}, {18,12}, {9,12}, {5,18}, {19,12}, {13,12}, {4,13}, {1,18}]");

        AHURootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
            new AHURootedTreeIsomorphismInspector<>(tree1, 1, tree2, 12);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testCornerCase3()
    {
        Graph<Integer, DefaultEdge> tree1 = parseGraph(
            "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]",
            "[{1,0}, {2,0}, {3,0}, {4,2}, {5,0}, {6,5}, {7,2}, {8,5}, {9,4}, {10,6}, {11,4}, {12,0}, {13,0}]");

        Graph<Integer, DefaultEdge> tree2 = parseGraph(
            "[10, 2, 12, 7, 5, 3, 4, 0, 6, 1, 13, 9, 8, 11]",
            "[{2,10}, {12,10}, {7,10}, {5,12}, {3,10}, {4,3}, {0,12}, {6,3}, {1,5}, {13,4}, {9,5}, {8,10}, {11,10}]");

        AHURootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
            new AHURootedTreeIsomorphismInspector<>(tree1, 0, tree2, 10);

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testNonIsomorphic()
    {
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);

        for (char c = 'A'; c <= 'F'; c++) {
            tree1.addVertex(String.valueOf(c));
            tree2.addVertex(String.valueOf((char) (c + ' ')));
        }

        tree1.addEdge("A", "B");
        tree1.addEdge("A", "C");
        tree1.addEdge("B", "F");
        tree1.addEdge("C", "D");
        tree1.addEdge("C", "E");

        tree2.addEdge("a", "b");
        tree2.addEdge("a", "c");
        tree2.addEdge("c", "f");
        tree2.addEdge("c", "d");
        tree2.addEdge("c", "e");

        // They are not isomorphic as rooted trees

        AHURootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
            new AHURootedTreeIsomorphismInspector<>(tree1, "A", tree2, "a");

        Assert.assertFalse(isomorphism.isomorphismExists());
        Assert.assertNull(isomorphism.getMapping());
    }

    @Test
    public void testSmall()
    {
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);

        for (char c = 'A'; c <= 'E'; c++) {
            tree1.addVertex(String.valueOf(c));
            tree2.addVertex(String.valueOf((char) (c + ' ')));
        }

        tree1.addEdge("A", "B");
        tree1.addEdge("A", "C");
        tree1.addEdge("C", "D");
        tree1.addEdge("C", "E");

        tree2.addEdge("a", "b");
        tree2.addEdge("a", "c");
        tree2.addEdge("b", "e");
        tree2.addEdge("b", "d");

        AHURootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
            new AHURootedTreeIsomorphismInspector<>(tree1, "A", tree2, "a");

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<String, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testSmall2()
    {
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
        Map<Integer, Integer> mapping = pair.getSecond();

        AHURootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
            new AHURootedTreeIsomorphismInspector<>(tree1, 1, tree2, mapping.get(1));

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
    public void testDisconnectedTree()
    {
        Graph<Integer, DefaultEdge> tree1 = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        tree1.addVertex(1);
        tree1.addVertex(2);
        tree1.addVertex(3);

        tree1.addEdge(1, 2);

        Graph<Integer, DefaultEdge> tree2 = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        tree2.addVertex(11);
        tree2.addVertex(21);
        tree2.addVertex(31);

        tree2.addEdge(11, 21);

        AHURootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
            new AHURootedTreeIsomorphismInspector<>(tree1, 1, tree2, 11);

        Assert.assertFalse(isomorphism.isomorphismExists());
        Assert.assertNull(isomorphism.getMapping());

        // Test as forest

        AHUForestIsomorphismInspector<Integer,
            DefaultEdge> forestIsomorphism = new AHUForestIsomorphismInspector<>(
                tree1, new HashSet<>(Arrays.asList(1, 3)), tree2,
                new HashSet<>(Arrays.asList(11, 31)));

        Assert.assertTrue(forestIsomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = forestIsomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRoot()
    {
        Graph<String, DefaultEdge> tree1 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("a");

        Graph<String, DefaultEdge> tree2 = new SimpleGraph<>(DefaultEdge.class);
        tree1.addVertex("A");

        AHURootedTreeIsomorphismInspector<String, DefaultEdge> isomorphism =
            new AHURootedTreeIsomorphismInspector<>(tree1, "b", tree2, "A");

        isomorphism.getMapping();
    }

    @Test
    public void testLineGraph()
    {
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

        AHURootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
            new AHURootedTreeIsomorphismInspector<>(tree1, 1, tree2, mapping.get(1));

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }

    @Test
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

        AHURootedTreeIsomorphismInspector<Integer, DefaultEdge> isomorphism =
            new AHURootedTreeIsomorphismInspector<>(tree1, 1, tree2, mapping.get(1));

        Assert.assertTrue(isomorphism.isomorphismExists());
        IsomorphicGraphMapping<Integer, DefaultEdge> treeMapping = isomorphism.getMapping();
        Assert.assertTrue(areIsomorphic(tree1, tree2, treeMapping));
    }
}
