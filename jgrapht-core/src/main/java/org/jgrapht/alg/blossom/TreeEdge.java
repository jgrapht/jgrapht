package org.jgrapht.alg.blossom;

import org.jgrapht.util.FibonacciHeap;

public class TreeEdge {
    Tree from;
    Tree to;
    FibonacciHeap<Edge> plusPlusEdges;
    FibonacciHeap<Edge> plusMinusEdges;
    FibonacciHeap<Edge> minusPlusEdges;

    public TreeEdge(Tree from, Tree to) {
        this.from = from;
        this.to = to;
        this.plusPlusEdges = new FibonacciHeap<>();
        this.plusMinusEdges = new FibonacciHeap<>();
        this.minusPlusEdges = new FibonacciHeap<>();
    }
}
