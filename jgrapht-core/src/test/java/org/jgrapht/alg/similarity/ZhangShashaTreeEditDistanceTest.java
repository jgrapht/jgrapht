/*
 * (C) Copyright 2020-2021, by Semen Chudakov and Contributors.
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
package org.jgrapht.alg.similarity;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jgrapht.alg.similarity.ZhangShashaTreeEditDistance.EditOperation;
import static org.jgrapht.alg.similarity.ZhangShashaTreeEditDistance.OperationType;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ZhangShashaTreeEditDistance}.
 */
public class ZhangShashaTreeEditDistanceTest
{
    // test graph instances
    int[][] articleTree1 = { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 }, { 5, 6 } };
    int[][] articleTree2 = { { 1, 5 }, { 1, 3 }, { 5, 2 }, { 2, 4 }, { 2, 6 } };
    int[][] tree1 = { { 1, 2 }, { 1, 4 }, { 2, 5 }, { 3, 6 }, { 4, 10 }, { 5, 6 }, { 5, 7 },
        { 5, 9 }, { 7, 8 }, { 10, 4 } };
    int[][] tree2 = { { 0, 1 }, { 0, 3 }, { 1, 4 }, { 1, 0 }, { 2, 5 }, { 3, 0 }, { 3, 9 },
        { 4, 5 }, { 4, 6 }, { 4, 8 }, { 4, 1 }, { 5, 2 }, { 5, 4 }, { 6, 7 }, { 6, 4 }, { 7, 6 },
        { 8, 4 }, { 9, 3 } };
    int[][] tree3 = { { 0, 3 }, { 0, 6 }, { 0, 4 }, { 0, 9 }, { 1, 8 }, { 1, 2 }, { 2, 5 },
        { 2, 1 }, { 2, 4 }, { 3, 0 }, { 4, 7 }, { 4, 2 }, { 4, 0 }, { 5, 2 }, { 6, 0 }, { 7, 4 },
        { 8, 1 }, { 9, 0 } };

    @Test
    public void testTED_treeWithOneVertex_to_treeWithOneVertex()
    {
        Set<EditOperation<Integer>> expectedEditOperations =
            Collections.singleton(new EditOperation<>(OperationType.CHANGE, 1, 1));
        testOnTrees(
            getGraphWithOneVertex(), 1, getGraphWithOneVertex(), 1, 0.0, expectedEditOperations);
    }

    @Test
    public void testTED_treeWithOneVertex_to_articleTree2()
    {
        Set<EditOperation<Integer>> expectedEditOperations = new HashSet<>(
            Arrays
                .asList(
                    new EditOperation<>(OperationType.CHANGE, 1, 1),
                    new EditOperation<>(OperationType.INSERT, 2, null),
                    new EditOperation<>(OperationType.INSERT, 3, null),
                    new EditOperation<>(OperationType.INSERT, 4, null),
                    new EditOperation<>(OperationType.INSERT, 5, null),
                    new EditOperation<>(OperationType.INSERT, 6, null)));
        testOnTrees(
            getGraphWithOneVertex(), 1, readGraph(articleTree2), 1, 5.0, expectedEditOperations);
    }

    @Test
    public void testTED_articleTree1_to_treeWithOneVertex()
    {
        Set<EditOperation<Integer>> expectedEditOperations = new HashSet<>(
            Arrays
                .asList(
                    new EditOperation<>(OperationType.CHANGE, 1, 1),
                    new EditOperation<>(OperationType.REMOVE, 2, null),
                    new EditOperation<>(OperationType.REMOVE, 3, null),
                    new EditOperation<>(OperationType.REMOVE, 4, null),
                    new EditOperation<>(OperationType.REMOVE, 5, null),
                    new EditOperation<>(OperationType.REMOVE, 6, null)));
        testOnTrees(
            readGraph(articleTree1), 1, getGraphWithOneVertex(), 1, 5.0, expectedEditOperations);
    }

    @Test
    public void testTED_articleTree1_to_articleTree2()
    {
        Set<EditOperation<Integer>> expectedEditOperations = new HashSet<>(
            Arrays
                .asList(
                    new EditOperation<>(OperationType.REMOVE, 5, null),
                    new EditOperation<>(OperationType.INSERT, 5, null),
                    new EditOperation<>(OperationType.CHANGE, 1, 1),
                    new EditOperation<>(OperationType.CHANGE, 2, 2),
                    new EditOperation<>(OperationType.CHANGE, 3, 3),
                    new EditOperation<>(OperationType.CHANGE, 4, 4),
                    new EditOperation<>(OperationType.CHANGE, 6, 6)));
        testOnTrees(
            readGraph(articleTree1), 1, readGraph(articleTree2), 1, 2.0, expectedEditOperations);
    }

    @Test
    public void testTED_tree1_to_articleTree2()
    {
        Set<EditOperation<Integer>> expectedEditOperations = new HashSet<>(
            Arrays
                .asList(
                    new EditOperation<>(OperationType.CHANGE, 1, 1),
                    new EditOperation<>(OperationType.REMOVE, 4, null),
                    new EditOperation<>(OperationType.CHANGE, 10, 3),
                    new EditOperation<>(OperationType.REMOVE, 2, null),
                    new EditOperation<>(OperationType.CHANGE, 5, 5),
                    new EditOperation<>(OperationType.REMOVE, 9, null),
                    new EditOperation<>(OperationType.REMOVE, 7, null),
                    new EditOperation<>(OperationType.REMOVE, 8, null),
                    new EditOperation<>(OperationType.INSERT, 2, null),
                    new EditOperation<>(OperationType.CHANGE, 6, 6),
                    new EditOperation<>(OperationType.REMOVE, 3, null),
                    new EditOperation<>(OperationType.INSERT, 4, null)));
        testOnTrees(readGraph(tree1), 1, readGraph(articleTree2), 1, 9.0, expectedEditOperations);
    }

    @Test
    public void testTED_articleTree1_to_tree1()
    {
        Set<EditOperation<Integer>> expectedEditOperations = new HashSet<>(
            Arrays
                .asList(
                    new EditOperation<>(OperationType.CHANGE, 1, 1),
                    new EditOperation<>(OperationType.INSERT, 4, null),
                    new EditOperation<>(OperationType.CHANGE, 3, 10),
                    new EditOperation<>(OperationType.CHANGE, 2, 2),
                    new EditOperation<>(OperationType.CHANGE, 5, 5),
                    new EditOperation<>(OperationType.INSERT, 9, null),
                    new EditOperation<>(OperationType.INSERT, 7, null),
                    new EditOperation<>(OperationType.INSERT, 8, null),
                    new EditOperation<>(OperationType.CHANGE, 6, 6),
                    new EditOperation<>(OperationType.INSERT, 3, null),
                    new EditOperation<>(OperationType.REMOVE, 4, null)));
        testOnTrees(readGraph(articleTree1), 1, readGraph(tree1), 1, 7.0, expectedEditOperations);
    }

    @Test
    public void testTED_tree2_to_tree3()
    {
        Set<EditOperation<Integer>> expectedEditOperations = new HashSet<>(
            Arrays
                .asList(
                    new EditOperation<>(OperationType.CHANGE, 0, 0),
                    new EditOperation<>(OperationType.REMOVE, 3, null),
                    new EditOperation<>(OperationType.CHANGE, 9, 9),
                    new EditOperation<>(OperationType.REMOVE, 1, null),
                    new EditOperation<>(OperationType.CHANGE, 4, 4),
                    new EditOperation<>(OperationType.REMOVE, 8, null),
                    new EditOperation<>(OperationType.REMOVE, 6, null),
                    new EditOperation<>(OperationType.CHANGE, 7, 7),
                    new EditOperation<>(OperationType.REMOVE, 5, null),
                    new EditOperation<>(OperationType.CHANGE, 2, 2),
                    new EditOperation<>(OperationType.INSERT, 5, null),
                    new EditOperation<>(OperationType.INSERT, 1, null),
                    new EditOperation<>(OperationType.INSERT, 8, null),
                    new EditOperation<>(OperationType.INSERT, 6, null),
                    new EditOperation<>(OperationType.INSERT, 3, null)));
        testOnTrees(readGraph(tree2), 0, readGraph(tree3), 0, 10.0, expectedEditOperations);
    }

    /**
     * Tests the {@link ZhangShashaTreeEditDistance} algorithm on {@code tree1} and {@code tree2}
     * instances. This method expects to get edit distance value equal to {@code expectedDistance}
     * and list of edit operations equal to {@code expectedEditOperations}.
     *
     * @param tree1 first tree
     * @param root1 root vertex of {@code tree1}
     * @param tree2 second tree
     * @param root2 root vertex of {@code tree2}
     * @param expectedDistance expected value of edit distance
     * @param expectedEditOperations expected list of edit operations
     */
    private static void testOnTrees(
        Graph<Integer, DefaultEdge> tree1, int root1, Graph<Integer, DefaultEdge> tree2, int root2,
        double expectedDistance, Set<EditOperation<Integer>> expectedEditOperations)
    {
        ZhangShashaTreeEditDistance<Integer, DefaultEdge> treeEditDistance =
            new ZhangShashaTreeEditDistance<>(tree1, root1, tree2, root2);

        double distance = treeEditDistance.getDistance();
        List<EditOperation<Integer>> actualEditOperations =
            treeEditDistance.getEditOperationLists();

        assertEquals(expectedDistance, distance, 1e-9);
        assertEquals(expectedEditOperations, new HashSet<>(actualEditOperations));
    }

    /**
     * Reads graph supplied in form of {@code representation}.
     *
     * @param representation list of graph edges
     * @return created instance of a graph
     */
    protected static Graph<Integer, DefaultEdge> readGraph(int[][] representation)
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int[] ints : representation) {
            Graphs.addEdgeWithVertices(graph, ints[0], ints[1]);
        }
        return graph;
    }

    /**
     * Returns a graph instance with one vertex.
     *
     * @return graph instance which has one vertex
     */
    private static Graph<Integer, DefaultEdge> getGraphWithOneVertex()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        return graph;
    }
}
