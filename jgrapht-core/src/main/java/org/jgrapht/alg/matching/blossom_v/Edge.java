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

import org.jgrapht.util.FibonacciHeapNode;

/**
 * This class is a supporting data structure for Kolmogorov's Blossom V algorithm. Represents an edge
 * between two nodes. Each edge has direction. According to this direction it is present in two circular
 * doubly linked lists of incident edges. The references to the next and previous edges of this list
 * are maintained via {@link Edge#next} and {@link Edge#prev}
 *
 * @author Timofey Chudakov
 * @see KolmogorovMinimumWeightPerfectMatching
 * @since June 2018
 */
class Edge {
    /**
     * FibonacciHeapsNode of the FibonacciHeap this edge is stored in
     */
    FibonacciHeapNode<Edge> fibNode;
    /**
     * The slack of this edge. Is this edge is an outer edge and doesn't connect 2 infinity nodes,
     * then its slack is subject to lazy delta spreading technique. For each of its two current endpoints
     * we we subtract the endpoint.tree.eps if the endpoint is a "+" node or add this value if it is a "-" node.
     * After that we have valid slack of this edge.
     */
    double slack;
    /**
     * A two-element array of original endpoints of this edge. They stay unchanged throughout the course of the
     * algorithm
     */
    Node[] headOriginal;
    /**
     * A two-element array of current endpoints of this edge. These values change when previous endpoints are
     * contracted into blossoms or are expanded. For node head[0] this is an incoming edge (direction 1) and for
     * the node head[1] this is an outgoing edge (direction 0). This feature is used to be able to access the
     * opposite node via an edge by incidentEdgeIterator.next().head[incidentEdgeIterator.getDir()].
     */
    Node[] head;

    /**
     * A two-element array of references to the next elements in the circular doubly linked lists of edges.
     * Each list belongs to one of the current endpoints of this edge.
     */
    Edge[] next;
    /**
     * A two-element array of references to the previous elements in the circular doubly linked lists of edges.
     * Each list belongs to one of the current endpoints of this edge.
     */
    Edge[] prev;

    /**
     * Constructs a new edge by initializing the arrays
     */
    public Edge() {
        headOriginal = new Node[2];
        head = new Node[2];
        next = new Edge[2];
        prev = new Edge[2];
    }

    /**
     * Returns an opposite edge with respect to the {@code endpoints}
     *
     * @param endpoint one of the endpoints of this edge
     * @return node opposite to the {@code endpoint}
     */
    public Node getOpposite(Node endpoint) {
        if (head[0] != endpoint && head[1] != endpoint) {
            return null; // strict mode, todo: remove
        }
        return head[0] == endpoint ? head[1] : head[0];
    }

    /**
     * Returns the original endpoint of this edge opposite to the {@code endpoint}, which is current endpoint
     *
     * @param endpoint one of the current endpoints of this edge
     * @return the original endpoint opposite to the {@code endpoint}
     */
    public Node getCurrentOriginal(Node endpoint) {
        if (head[0] != endpoint && head[1] != endpoint) {
            return null; // strict mode, todo: remove
        }
        return head[0] == endpoint ? headOriginal[0] : headOriginal[1];
    }

    /**
     * Returns the direction to the opposite node with respect to the {@code current}
     *
     * @param current current endpoint of this edge
     * @return the direction from the {@code current}
     */
    public int getDirFrom(Node current) {
        return head[0] == current ? 1 : 0;
    }

    @Override
    public String toString() {
        return "Edge (" + head[0].id + "," + head[1].id + "), original: [" + headOriginal[0].id + "," + headOriginal[1].id + "], slack: " + slack + ", true slack: " + getTrueSlack()
                + (getTrueSlack() == 0 ? ", tight" : "");
    }

    /**
     * Returns the true slack of this edge, i.e. the slack after applying lazy delta spreading updates
     *
     * @return the true slack of this edge
     */
    public double getTrueSlack() {
        double result = slack;

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


