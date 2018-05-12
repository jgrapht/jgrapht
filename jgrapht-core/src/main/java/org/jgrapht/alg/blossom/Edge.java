package org.jgrapht.alg.blossom;

import org.jgrapht.util.FibonacciHeapNode;

class Edge {
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

    public Node getOppositeOriginal(Node endPoint){
        return headOriginal[0] == endPoint ? headOriginal[1] : headOriginal[0];
    }

    public Edge(Node from, Node to) {
        this(from, to, 0);
    }

    Node getOpposite(Node node) {
        return head[0] == node ? head[1] : head[0];
    }

    @Override
    public String toString() {
        return "Edge (" + headOriginal[0].id + "," + headOriginal[1].id + ")";
    }

    public Node getOuterHead(int direction) {
        // TODO: fix
        return head[direction];
    }
}
