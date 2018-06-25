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
package org.jgrapht.alg.matching.blossom_v;

import org.jgrapht.util.FibonacciHeap;

import java.util.HashSet;
import java.util.Set;

public class BlossomVDebugger {
    public static Set<Edge> edgesOf(Node node) {
        Set<Edge> edges = new HashSet<>();
        for (Node.IncidentEdgeIterator iterator = node.adjacentEdgesIterator(); iterator.hasNext(); ) {
            edges.add(iterator.next());
        }
        return edges;
    }

    public static Set<TreeEdge> treeEdgesOf(Tree tree) {
        Set<TreeEdge> result = new HashSet<>();
        for (Tree.TreeEdgeIterator iterator = tree.treeEdgeIterator(); iterator.hasNext(); ) {
            result.add(iterator.next());
        }
        return result;
    }

    public static TreeEdge getTreeEdge(Tree from, Tree to) {
        for (Tree.TreeEdgeIterator iterator = from.treeEdgeIterator(); iterator.hasNext(); ) {
            TreeEdge treeEdge = iterator.next();
            if (treeEdge.head[iterator.getCurrentDirection()] == to) {
                return treeEdge;
            }
        }
        return null;
    }

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

    public static Tree getOppositeTree(TreeEdge treeEdge, Tree tree) {
        return treeEdge.head[0] == tree ? treeEdge.head[1] : treeEdge.head[0];
    }

    public static Set<Node> childrenOf(Node node) {
        Set<Node> children = new HashSet<>();
        for (Node child = node.firstTreeChild; child != null; child = child.treeSiblingNext) {
            children.add(child);
        }
        return children;
    }

    public static <V, E> Set<Node> treeRoots(State<V, E> state) {
        Set<Node> treeRoots = new HashSet<>();
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            treeRoots.add(root);
        }
        return treeRoots;
    }

    public static Set<Node> getTreeNodes(Tree tree) {
        Set<Node> nodes = new HashSet<>();
        for (Tree.TreeNodeIterator iterator = tree.treeNodeIterator(); iterator.hasNext(); ) {
            nodes.add(iterator.next());
        }
        return nodes;
    }

    public static int dirToOpposite(TreeEdge treeEdge, Tree tree) {
        return treeEdge.head[0] == tree ? 1 : 0;
    }

    public static FibonacciHeap<Edge> getPlusMinusHeap(TreeEdge treeEdge, Tree tree) {
        return treeEdge.head[0] == tree ? treeEdge.getCurrentPlusMinusHeap(1) : treeEdge.getCurrentPlusMinusHeap(0);
    }

    public static FibonacciHeap<Edge> getMinusPlusHeap(TreeEdge treeEdge, Tree tree) {
        return treeEdge.head[0] == tree ? treeEdge.getCurrentMinusPlusHeap(1) : treeEdge.getCurrentMinusPlusHeap(0);
    }

    public static Node getOpposite(Edge edge, Node node) {
        return edge.head[0] == node ? edge.head[1] : edge.head[0];
    }

    public static Node getOppositeOriginal(Edge edge, Node endPoint) {
        return edge.headOriginal[0] == endPoint ? edge.headOriginal[1] : edge.headOriginal[0];
    }
}
