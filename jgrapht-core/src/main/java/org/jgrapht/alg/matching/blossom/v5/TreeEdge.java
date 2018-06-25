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
import org.jgrapht.util.FibonacciHeapNode;

class TreeEdge {
    private static int ID = 0;
    int id;

    Tree[] head;
    TreeEdge[] prev;
    TreeEdge[] next;
    FibonacciHeap<Edge> plusPlusEdges;
    FibonacciHeap<Edge> plusMinusEdges0;
    FibonacciHeap<Edge> plusMinusEdges1;


    public TreeEdge() {
        this.head = new Tree[2];
        this.prev = new TreeEdge[2];
        this.next = new TreeEdge[2];
        this.plusPlusEdges = new FibonacciHeap<>();
        this.plusMinusEdges0 = new FibonacciHeap<>();
        this.plusMinusEdges1 = new FibonacciHeap<>();
        id = ID++;
    }

    public void removeFromTreeEdgeList() {
        for (int dir = 0; dir < 2; dir++) {
            if (prev[dir] != null) {
                prev[dir].next[dir] = next[dir];
            } else {
                // this is the first edge in this direction
                head[1 - dir].first[dir] = next[dir];
            }
            if (next[dir] != null) {
                next[dir].prev[dir] = prev[dir];
            }
        }
        head[0] = head[1] = null;
    }

    @Override
    public String toString() {
        return "TreeEdge (" + head[0].id + ":" + head[1].id + "), id = " + id;
    }

    public void addToCurrentMinusPlusHeap(Edge edge, double key, int direction) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        getCurrentMinusPlusHeap(direction).insert(edgeNode, key);
    }

    public void addToCurrentPlusMinusHeap(Edge edge, double key, int direction) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        getCurrentPlusMinusHeap(direction).insert(edgeNode, key);
    }

    public void addPlusPlusEdge(Edge edge, double key) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        this.plusPlusEdges.insert(edgeNode, key);
    }

    public void removeFromCurrentMinusPlusHeap(Edge edge, int direction) {
        getCurrentMinusPlusHeap(direction).delete(edge.fibNode);
        edge.fibNode = null;
    }

    public void removeFromCurrentPlusMinusHeap(Edge edge, int direction) {
        getCurrentPlusMinusHeap(direction).delete(edge.fibNode);
        edge.fibNode = null;
    }

    public void removeFromPlusPlusHeap(Edge edge) {
        plusPlusEdges.delete(edge.fibNode);
        edge.fibNode = null;
    }

    public FibonacciHeap<Edge> getCurrentMinusPlusHeap(int currentDir) {
        return currentDir == 0 ? plusMinusEdges0 : plusMinusEdges1;
    }

    public FibonacciHeap<Edge> getCurrentPlusMinusHeap(int currentDir) {
        return currentDir == 0 ? plusMinusEdges1 : plusMinusEdges0;
    }
}
