package org.jgrapht.alg.blossom;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

class TreeEdge {
    Tree[] head;
    TreeEdge[] next;
    FibonacciHeap<Edge> plusPlusEdges;
    FibonacciHeap<Edge> plusMinusEdges0;
    FibonacciHeap<Edge> plusMinusEdges1;


    public TreeEdge(Tree from, Tree to) {
        this.head = new Tree[2];
        head[0] = to;
        head[1] = from;
        this.next = new TreeEdge[2];
        this.plusPlusEdges = new FibonacciHeap<>();
        this.plusMinusEdges0 = new FibonacciHeap<>();
        this.plusMinusEdges1 = new FibonacciHeap<>();
    }

    public FibonacciHeapNode<Edge> addToCurrentMinusPlusHeap(Edge edge, int direction) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        getCurrentMinusPlusHeap(direction).insert(edgeNode, edge.slack);
        return edgeNode;
    }

    public FibonacciHeapNode<Edge> addToCurrentPlusMinusHeap(Edge edge, int direction) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        getCurrentPlusMinusHeap(direction).insert(edgeNode, edge.slack);
        return edgeNode;
    }

    public FibonacciHeap<Edge> getCurrentMinusPlusHeap(int currentDir) {
        return currentDir == 0 ? plusMinusEdges0 : plusMinusEdges1;
    }

    public FibonacciHeap<Edge> getCurrentPlusMinusHeap(int currentDir) {
        return currentDir == 0 ? plusMinusEdges1 : plusMinusEdges0;
    }

    public FibonacciHeapNode<Edge> addPlusPlusEdge(Edge edge) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        this.plusPlusEdges.insert(edgeNode, edge.slack);
        return edgeNode;
    }
}
