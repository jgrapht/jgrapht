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

import org.jgrapht.util.FibonacciHeapNode;

/**
 * This class is a supporting data structure for Kolmogorov's Blossom V algorithm.
 * <p>
 * Represents an edge between two nodes. Also, the minimum weight perfect matching problem is formulated on
 * an undirected graph, each edge has direction, i.e. it is an arc. According to this direction it is present in
 * two circular doubly linked lists of incident edges. The references to the next and previous edges of this list
 * are maintained via {@link Edge#next} and {@link Edge#prev} arrays.
 * <p>
 * For example, let a $e = \{u, v\}$ be an edge in the graph $G = (V, E)$. Let's assume that after initialization
 * this edge has become directed from $u$ to $v$, i.e. now $e = (u, v)$. Then now edge $e$ belongs to the linked lists
 * {@code u.first[0]} and {@code v.first[1]}. In other words, $e$ is an outgoing edge of $u$ and an incoming edge
 * of $v$. For convenience during computation, {@code e.head[0] = v} and {@code e.head[1] = u}. Therefore, while
 * iterating over incident edges of a node {@code x} in the direction {@code dir}, we can easily access opposite node
 * by {@code x.head[dir]}.
 *
 * @author Timofey Chudakov
 * @see KolmogorovMinimumWeightPerfectMatching
 * @since June 2018
 */
class Edge {
    /**
     * FibonacciHeapsNode of the FibonacciHeap this edge is stored in.
     * <p>
     * <em>This variable doesn't need to be necessarily set to {@code null} after the edge is removed from FibonacciHeap
     * due to performance reasons. Therefore, no assumptions should be made about whether this edge belongs to some heap or not
     * based upon this variable being {@code null} or not.</em>
     */
    FibonacciHeapNode<Edge> fibNode;
    /**
     * The slack of this edge. If this edge is an outer edge and doesn't connect 2 infinity nodes,
     * then its slack is subject to lazy delta spreading technique. Otherwise, it has a valid slack.
     * <p>
     * The true slack of the edge can be computed as following: for each of its two current endpoints $\{u, v\}$
     * we subtract the endpoint.tree.eps if the endpoint is a "+" outer node or add this value if it is a "-" outer
     * node. After that we have valid slack of this edge.
     */
    double slack;
    /**
     * A two-element array of original endpoints of this edge. The are used to quickly determine original endpoint
     * of an edge and compute penultimate blossom. This is done while one of the current endpoints of this edge is
     * being shrunk or expanded
     * <p>
     * These values stay unchanged throughout the course of the algorithm.
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
     * A two-element array of references to the previous elements in the circular doubly linked lists of edges.
     * Each list belongs to one of the <b>current</b> endpoints of this edge.
     */
    Edge[] prev;
    /**
     * A two-element array of references to the next elements in the circular doubly linked lists of edges.
     * Each list belongs to one of the <b>current</b> endpoints of this edge.
     */
    Edge[] next;
    /**
     * Position of this edge in the array {@code state.edges}. This helps to determine generic counterpart of
     * this edge in constant time.
     */
    int pos;

    /**
     * Constructs a new edge by initializing the arrays
     */
    public Edge(int pos) {
        headOriginal = new Node[2];
        head = new Node[2];
        next = new Edge[2];
        prev = new Edge[2];
        this.pos = pos;
    }

    /**
     * Returns an opposite edge with respect to the {@code endpoint}. <b>Note:</b> here we assume that
     * {@code endpoint} is one of the current endpoints. The first enforces this rule.
     *
     * @param endpoint one of the current endpoints of this edge
     * @return node opposite to the {@code endpoint}
     */
    public Node getOpposite(Node endpoint) {
        if (head[0] != endpoint && head[1] != endpoint) {
            return null; // strict mode, this code should never be executed
        }
        return head[0] == endpoint ? head[1] : head[0];
    }

    /**
     * Returns the original endpoint of this edge under the {@code endpoint}, which must be one of
     * the current endpoints.
     *
     * @param endpoint one of the current endpoints of this edge
     * @return the original endpoint opposite to the {@code endpoint}
     */
    public Node getCurrentOriginal(Node endpoint) {
        if (head[0] != endpoint && head[1] != endpoint) {
            return null; // strict mode, this code should never be executed
        }
        return head[0] == endpoint ? headOriginal[0] : headOriginal[1];
    }

    /**
     * Returns the direction to the opposite node with respect to the {@code current}.
     * {@code current} must be one of the current endpoints of this edge.
     *
     * @param current one of the current endpoint of this edge.
     * @return the direction from the {@code current}
     */
    public int getDirFrom(Node current) {
        return head[0] == current ? 1 : 0;
    }

    @Override
    public String toString() {
        return "Edge (" + head[0].pos + "," + head[1].pos + "), original: [" + headOriginal[0].pos + "," + headOriginal[1].pos + "], slack: " + slack + ", true slack: " + getTrueSlack()
                + (getTrueSlack() == 0 ? ", tight" : "");
    }

    /**
     * Returns the true slack of this edge, i.e. the slack after applying lazy dual updates
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


