package org.jgrapht.alg.blossom;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

class Edge implements Comparable<Edge> {
    FibonacciHeapNode<Edge> fibNode;

    double slack;

    Node[] headOriginal;
    Node[] head;

    Edge[] next;
    Edge[] prev;

    public Edge(Node from, Node to, double slack) {
        headOriginal = new Node[2];
        head = new Node[2];
        next = new Edge[2];
        prev = new Edge[2];

        head[0] = headOriginal[0] = from;
        head[1] = headOriginal[1] = to;
        this.slack = slack;
    }

    public Edge(Node from, Node to){
        this(from, to, 0);
    }

    public boolean isTight() {
        return slack == 0;
    }

    public Node getOuterHead(int direction){
        // TODO: fix
        return head[direction];
    }

    @Override
    public int compareTo(Edge o) {
        return Double.compare(slack, o.slack);
    }
}
