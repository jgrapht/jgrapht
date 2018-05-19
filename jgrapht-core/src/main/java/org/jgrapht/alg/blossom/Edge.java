package org.jgrapht.alg.blossom;

import org.jgrapht.util.FibonacciHeapNode;

class Edge {
    FibonacciHeapNode<Edge> fibNode;
    double slack;
    Node[] headOriginal;
    Node[] head;

    // next edges in the doubly linked circular lists of edges
    Edge[] next;
    Edge[] prev;

    public Edge() {
        headOriginal = new Node[2];
        head = new Node[2];
        next = new Edge[2];
        prev = new Edge[2];
    }

    public Node getOppositeOriginal(Node endPoint) {
        return headOriginal[0] == endPoint ? headOriginal[1] : headOriginal[0];
    }

    public int getDirTo(Node current) {
        return head[0] == current ? 0 : 1;
    }

    public int getDirFrom(Node current) {
        return head[0] == current ? 1 : 0;
    }

    public Node getOpposite(Node node) {
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

    public double getTrueSlack() {
        double result = slack;
        if (!head[0].isOuter && head[1].isOuter) {
            return result;
        } else {
            if (head[0].tree != null) {
                if (head[0].isPlusNode()) {
                    result -= head[0].tree.eps;
                } else {
                    result += head[0].tree.eps;
                }
            }
            if (head[1].tree != null) {
                if (head[1].isPlusNode()) {
                    result -= head[1].tree.eps;
                } else {
                    result += head[1].tree.eps;
                }
            }
            return result;
        }
    }
}


