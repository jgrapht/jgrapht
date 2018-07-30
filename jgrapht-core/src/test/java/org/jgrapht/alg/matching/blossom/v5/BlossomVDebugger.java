/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.matching.blossom.v5;

import org.jgrapht.util.FibonacciHeap;
import org.jheaps.MergeableAddressableHeap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 * This class contains auxiliary methods for testing {@link KolmogorovMinimumWeightPerfectMatching} and related
 * classes
 *
 * @author Timofey Chudakov
 */
public class BlossomVDebugger {

    /**
     * Returns the mapping from original graph vertices to the internal nodes used in the algortihm
     *
     * @param state the state of the algorithm
     * @param <V>   graph vertex type
     * @param <E>   graph edge type
     * @return the mapping from original graph vertices to the internal nodes used in the algortihm
     */
    public static <V, E> Map<V, Node> getVertexMap(State<V, E> state) {
        Map<V, Node> vertexMap = new HashMap<>(state.nodeNum);
        for (int i = 0; i < state.nodeNum; i++) {
            vertexMap.put(state.graphVertices.get(i), state.nodes[i]);
        }
        return vertexMap;
    }

    /**
     * Returns the mapping from original graph edges to the internal edges used in the algorithm.
     *
     * @param state the state of the algorithm
     * @param <V>   graph vertex type
     * @param <E>   graph edge type
     * @return the mapping from original graph edges to the internal edges used in the algorithm.
     */
    public static <V, E> Map<E, Edge> getEdgeMap(State<V, E> state) {
        Map<E, Edge> edgeMap = new HashMap<>(state.edgeNum);
        for (int i = 0; i < state.edgeNum; i++) {
            edgeMap.put(state.graphEdges.get(i), state.edges[i]);
        }
        return edgeMap;
    }

    /**
     * Returns all edge incident to {@code node}
     *
     * @param node some node
     * @return all edge incident to {@code node}
     */
    public static Set<Edge> getEdgesOf(Node node) {
        Set<Edge> edges = new HashSet<>();
        for (Node.IncidentEdgeIterator iterator = node.incidentEdgesIterator(); iterator.hasNext(); ) {
            edges.add(iterator.next());
        }
        return edges;
    }

    /**
     * Returns all tree edges incident to {@code tree}
     *
     * @param tree some alternating tree
     * @return all tree edges incident to {@code tree}
     */
    public static Set<TreeEdge> getTreeEdgesOf(Tree tree) {
        Set<TreeEdge> result = new HashSet<>();
        for (Tree.TreeEdgeIterator iterator = tree.treeEdgeIterator(); iterator.hasNext(); ) {
            result.add(iterator.next());
        }
        return result;
    }

    /**
     * Returns the first tree edge between {@code from} and {@code to}. If there is no tree edge between
     * these two alternating trees, this method fails.
     *
     * @param from some alternating tree
     * @param to   some alternating tree
     * @return the first tree edge between {@code from} and {@code to}
     */
    public static TreeEdge getTreeEdge(Tree from, Tree to) {
        TreeEdge treeEdge = null;
        for (Tree.TreeEdgeIterator iterator = from.treeEdgeIterator(); iterator.hasNext(); ) {
            treeEdge = iterator.next();
            if (treeEdge.head[iterator.getCurrentDirection()] == to) {
                return treeEdge;
            }
        }
        fail();
        return treeEdge;
    }

    /**
     * Returns all tree edges between {@code from} and {@code to}
     *
     * @param from some alternating tree
     * @param to   some alternating tree
     * @return all tree edges between {@code from} and {@code to}
     */
    public static Set<TreeEdge> getTreeEdgesBetween(Tree from, Tree to) {
        Set<TreeEdge> result = new HashSet<>();
        for (Tree.TreeEdgeIterator iterator = from.treeEdgeIterator(); iterator.hasNext(); ) {
            TreeEdge treeEdge = iterator.next();
            if (treeEdge.head[iterator.getCurrentDirection()] == to) {
                result.add(treeEdge);
            }
        }
        return result;
    }

    /**
     * Returns all tree children of the {@code node}
     *
     * @param node some node
     * @return all tree children of the {@code node}
     */
    public static Set<Node> getChildrenOf(Node node) {
        Set<Node> children = new HashSet<>();
        for (Node child = node.firstTreeChild; child != null; child = child.treeSiblingNext) {
            children.add(child);
        }
        return children;
    }

    /**
     * Returns all tree roots of the alternating trees stored in the {@code state}
     *
     * @param state the state of the algorithm
     * @param <V>   graph vertex type
     * @param <E>   graph edge type
     * @return all tree roots of the alternating trees stored in the {@code state}
     */
    public static <V, E> Set<Node> getTreeRoots(State<V, E> state) {
        Set<Node> treeRoots = new HashSet<>();
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            treeRoots.add(root);
        }
        return treeRoots;
    }

    /**
     * Returns a set of all nodes of the {@code tree}
     *
     * @param tree an alternating tree
     * @return a set of all nodes of the {@code tree}
     */
    public static Set<Node> getTreeNodes(Tree tree) {
        Set<Node> nodes = new HashSet<>();
        for (Tree.TreeNodeIterator iterator = tree.treeNodeIterator(); iterator.hasNext(); ) {
            nodes.add(iterator.next());
        }
        return nodes;
    }

    /**
     * Returns the direction from the {@code tree} to the opposite tree, that are connected via {@code treeEdge}.
     * More precisely, returns {@code dir} such that {@code treeEdge.head[dir] != tree}.
     *
     * @param treeEdge tree edge incident to {@code tree}
     * @param tree     an alternating tree
     * @return dir such that {@code treeEdge.head[dir] != tree}
     */
    public static int getDirToOpposite(TreeEdge treeEdge, Tree tree) {
        return treeEdge.head[0] == tree ? 1 : 0;
    }

    /**
     * Returns current heap of (+, -) cross-tree edges if {@code tree} is considered as the current tree.
     *
     * @param treeEdge tree edge incident to the {@code tree}
     * @param tree some alternating tree
     * @return current heap of (+, -) cross-tree edges if {@code tree} is considered as the current tree.
     */
    public static MergeableAddressableHeap<Double, Edge> getPlusMinusHeap(TreeEdge treeEdge, Tree tree) {
        return treeEdge.head[0] == tree ? treeEdge.getCurrentPlusMinusHeap(1) : treeEdge.getCurrentPlusMinusHeap(0);
    }

    /**
     * Returns current heap of (-, +) cross-tree edges if {@code tree} is considered as the current tree.
     *
     * @param treeEdge tree edge incident to the {@code tree}
     * @param tree some alternating tree
     * @return current heap of (-, +) cross-tree edges if {@code tree} is considered as the current tree.
     */
    public static MergeableAddressableHeap<Double, Edge> getMinusPlusHeap(TreeEdge treeEdge, Tree tree) {
        return treeEdge.head[0] == tree ? treeEdge.getCurrentMinusPlusHeap(1) : treeEdge.getCurrentMinusPlusHeap(0);
    }
}
