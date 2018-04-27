package org.jgrapht.alg.blossom;

import org.jgrapht.util.FibonacciHeap;

public class Tree {
    FibonacciHeap<Edge> plusPlusEdges;
    FibonacciHeap<Edge> plusFreeEdges;
    FibonacciHeap<Node> minusBlossoms;

    private double eps;
    private Node root;


    public Tree(Node root) {
        this.root = root;
        plusPlusEdges = new FibonacciHeap<>();
        plusFreeEdges = new FibonacciHeap<>();
        minusBlossoms = new FibonacciHeap<>();
    }

    public Node getRoot() {
        return root;
    }
}
