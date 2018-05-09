package org.jgrapht.alg.blossom;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

class TreeEdge {
    private static int ID = 0;
    int id;

    Tree[] head;
    TreeEdge[] next;
    FibonacciHeap<Edge> plusPlusEdges;
    FibonacciHeap<Edge> plusMinusEdges0;
    FibonacciHeap<Edge> plusMinusEdges1;


    public TreeEdge() {
        this.head = new Tree[2];
        this.next = new TreeEdge[2];
        this.plusPlusEdges = new FibonacciHeap<>();
        this.plusMinusEdges0 = new FibonacciHeap<>();
        this.plusMinusEdges1 = new FibonacciHeap<>();
        id = ID++;
    }

    @Override
    public String toString() {
        return "TreeEdge id = " + id;
    }

    public FibonacciHeapNode<Edge> addToCurrentMinusPlusHeap(Edge edge, double key, int direction) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        getCurrentMinusPlusHeap(direction).insert(edgeNode, key);
        return edgeNode;
    }

    public FibonacciHeapNode<Edge> addToCurrentPlusMinusHeap(Edge edge, double key, int direction) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        getCurrentPlusMinusHeap(direction).insert(edgeNode, key);
        return edgeNode;
    }

    public FibonacciHeapNode<Edge> addPlusPlusEdge(Edge edge, double key) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        this.plusPlusEdges.insert(edgeNode, key);
        return edgeNode;
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
