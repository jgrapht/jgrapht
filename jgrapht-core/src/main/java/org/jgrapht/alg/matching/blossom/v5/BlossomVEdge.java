/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.alg.matching.blossom.v5;

import org.jheaps.*;

import java.util.*;

/**
 * This class is a data structure for Kolmogorov's Blossom V algorithm.
 * <p>
 * It represents an edge between two nodes. Even though the minimum weight perfect matching problem
 * is formulated on an undirected graph, each edge has direction, i.e. it is an arc. According to
 * this direction it is present in two circular doubly linked lists of incident edges. The
 * references to the next and previous edges of this list are maintained via
 * {@link BlossomVEdge#next} and {@link BlossomVEdge#prev} references. The direction of an edge
 * isn't stored in the edge, this property is only reflected by the presence of an edge in the list
 * of outgoing or incoming edges.
 * <p>
 * For example, let a $e = \{u, v\}$ be an edge in the graph $G = (V, E)$. Let's assume that after
 * initialization this edge has become directed from $u$ to $v$, i.e. now $e = (u, v)$. Then edge
 * $e$ belongs to the linked lists {@code u.first[0]} and {@code v.first[1]}. In other words, $e$ is
 * an outgoing edge of $u$ and an incoming edge of $v$. For convenience during computation,
 * {@code e.head[0] = v} and {@code e.head[1] = u}. Therefore, while iterating over incident edges
 * of a node {@code x} in the direction {@code dir}, we can easily access opposite node by
 * {@code x.head[dir]}.
 * <p>
 * An edge is called an <i>infinity</i> edge if it connects a "+" node with an infinity node. An
 * edge is called <i>free</i> if it connects two infinity nodes. An edge is called <i>matched</i> if
 * it belongs to the matching. During the shrink or expand operations an edge is called an
 * <i>inner</i> edge if it connects two nodes of the blossom. It is called a <i>boundary</i> edge if
 * it is incident to exactly one blossom node. An edge is called <i>tight</i> if its reduced cost
 * (reduced weight, slack, all three notions are equivalent) is zero. <b>Note:</b> in this algorithm
 * we use lazy delta spreading, so the {@link BlossomVEdge#slack} isn't necessarily equal to the
 * actual slack of an edge.
 *
 * @author Timofey Chudakov
 * @see KolmogorovMinimumWeightPerfectMatching
 */
class BlossomVEdge
{
    /**
     * Position of this edge in the array {@code state.edges}. This helps to determine generic
     * counterpart of this edge in constant time.
     */
    final int pos;
    /**
     * A heap node from the heap this edge is stored in.
     * <p>
     * <em>This variable doesn't need to be necessarily set to {@code null} after the edge is
     * removed from the heap it was stored in due to performance reasons. Therefore, no assumptions
     * should be made about whether this edge belongs to some heap or not based upon this variable
     * being {@code null} or not.</em>
     */
    AddressableHeap.Handle<Double, BlossomVEdge> handle;
    /**
     * The slack of this edge. If an edge is an outer edge and doesn't connect 2 infinity nodes,
     * then its slack is subject to lazy delta spreading technique. Otherwise, this variable equals
     * the edge's true slack.
     * <p>
     * The true slack of the edge can be computed as following: for each of its two current
     * endpoints $\{u, v\}$ we subtract the endpoint.tree.eps if the endpoint is a "+" outer node or
     * add this value if it is a "-" outer node. After that we have valid slack for this edge.
     */
    double slack;
    /**
     * A two-element array of original endpoints of this edge. They are used to quickly determine
     * original endpoints of an edge and compute the penultimate blossom. This is done while one of
     * the current endpoints of this edge is being shrunk or expanded.
     * <p>
     * These values stay unchanged throughout the course of the algorithm.
     */
    BlossomVNode[] headOriginal;
    /**
     * A two-element array of current endpoints of this edge. These values change when previous
     * endpoints are contracted into blossoms or are expanded. For node head[0] this is an incoming
     * edge (direction 1) and for the node head[1] this is an outgoing edge (direction 0). This
     * feature is used to be able to access the opposite node via an edge by
     * {@code incidentEdgeIterator.next().head[incidentEdgeIterator.getDir()] }.git
     */
    BlossomVNode[] head;
    /**
     * A two-element array of references to the previous elements in the circular doubly linked
     * lists of edges. Each list belongs to one of the <b>current</b> endpoints of this edge.
     */
    BlossomVEdge[] prev;
    /**
     * A two-element array of references to the next elements in the circular doubly linked lists of
     * edges. Each list belongs to one of the <b>current</b> endpoints of this edge.
     */
    BlossomVEdge[] next;

    /**
     * Constructs a new edge by initializing the arrays
     */
    public BlossomVEdge(int pos)
    {
        headOriginal = new BlossomVNode[2];
        head = new BlossomVNode[2];
        next = new BlossomVEdge[2];
        prev = new BlossomVEdge[2];
        this.pos = pos;
    }

    /**
     * Returns the opposite edge with respect to the {@code endpoint}. <b>Note:</b> here we assume
     * that {@code endpoint} is one of the current endpoints.
     *
     * @param endpoint one of the current endpoints of this edge
     * @return node opposite to the {@code endpoint}
     */
    public BlossomVNode getOpposite(BlossomVNode endpoint)
    {
        if (endpoint != head[0] && endpoint != head[1]) { // we need this check during finishing
                                                          // phase
            return null;
        }
        return head[0] == endpoint ? head[1] : head[0];
    }

    /**
     * Returns the original endpoint of this edge for some current {@code endpoint}.
     *
     * @param endpoint one of the current endpoints of this edge
     * @return the original endpoint of this edge which has the same direction as {@code endpoint}
     *         with respect to this edge
     */
    public BlossomVNode getCurrentOriginal(BlossomVNode endpoint)
    {
        if (endpoint != head[0] && endpoint != head[1]) { // we need this check during finishing
                                                          // phase
            return null;
        }
        return head[0] == endpoint ? headOriginal[0] : headOriginal[1];
    }

    /**
     * Returns the direction to the opposite node with respect to the {@code current}.
     * {@code current} must be one of the current endpoints of this edge.
     *
     * @param current one of the current endpoints of this edge.
     * @return the direction from the {@code current}
     */
    public int getDirFrom(BlossomVNode current)
    {
        return head[0] == current ? 1 : 0;
    }

    @Override
    public String toString()
    {
        return "BlossomVEdge (" + head[0].pos + "," + head[1].pos + "), original: ["
            + headOriginal[0].pos + "," + headOriginal[1].pos + "], slack: " + slack
            + ", true slack: " + getTrueSlack() + (getTrueSlack() == 0 ? ", tight" : "");
    }

    /**
     * Returns the true slack of this edge, i.e. the slack after applying lazy dual updates
     *
     * @return the true slack of this edge
     */
    public double getTrueSlack()
    {
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

    /**
     * Moves the tail of the {@code edge} from the node {@code from} to the node {@code to}
     *
     * @param from the previous edge's tail
     * @param to the new edge's tail
     */
    public void moveEdgeTail(BlossomVNode from, BlossomVNode to)
    {
        int dir = getDirFrom(from);
        from.removeEdge(this, dir);
        to.addEdge(this, dir);
    }

    /**
     * Returns a new instance of blossom nodes iterator
     *
     * @param root the root of the blossom
     * @return a new instance of blossom nodes iterator
     */
    public BlossomVEdge.BlossomNodesIterator blossomNodesIterator(BlossomVNode root)
    {
        return new BlossomVEdge.BlossomNodesIterator(root, this);
    }

    /**
     * An iterator which traverses all nodes in the blossom. It starts from the endpoints of the
     * (+,+) edge and goes up to the blossom root. These two paths to the blossom root are called
     * branches. The branch of the blossomFormingEdge.head[0] has direction 0, the one has direction
     * 1.
     * <p>
     * <b>Note:</b> the nodes returned by this iterator aren't consecutive
     * <p>
     * <b>Note:</b> this iterator must return the blossom root in the first branch, i.e. when the
     * direction is 0. This feature is needed to setup the blossomSibling references correctly
     */
    public static class BlossomNodesIterator
        implements
        Iterator<BlossomVNode>
    {
        /**
         * Blossom's root
         */
        private BlossomVNode root;
        /**
         * The node this iterator is currently on
         */
        private BlossomVNode currentNode;
        /**
         * Helper variable, is used to determine whether currentNode has been returned or not
         */
        private BlossomVNode current;
        /**
         * The current direction of this iterator
         */
        private int currentDirection;
        /**
         * The (+, +) edge of the blossom
         */
        private BlossomVEdge blossomFormingEdge;

        /**
         * Constructs a new BlossomNodeIterator for the {@code root} and {@code blossomFormingEdge}
         *
         * @param root the root of the blossom (the node which isn't matched to another node in the
         *        blossom)
         * @param blossomFormingEdge a (+, +) edge in the blossom
         */
        public BlossomNodesIterator(BlossomVNode root, BlossomVEdge blossomFormingEdge)
        {
            this.root = root;
            this.blossomFormingEdge = blossomFormingEdge;
            currentNode = current = blossomFormingEdge.head[0];
            currentDirection = 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext()
        {
            if (current != null) {
                return true;
            }
            current = advance();
            return current != null;
        }

        /**
         * @return the current direction of this iterator
         */
        public int getCurrentDirection()
        {
            return currentDirection;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BlossomVNode next()
        {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            BlossomVNode result = current;
            current = null;
            return result;
        }

        /**
         * Advances this iterator to the next node in the blossom
         *
         * @return an unvisited node in the blossom
         */
        private BlossomVNode advance()
        {
            if (currentNode == null) {
                return null;
            }
            if (currentNode == root && currentDirection == 0) {
                // we have just traversed blossom's root and now start to traverse the second branch
                currentDirection = 1;
                currentNode = blossomFormingEdge.head[1];
                if (currentNode == root) {
                    currentNode = null;
                }
            } else if (currentNode.getTreeParent() == root && currentDirection == 1) {
                // we have just finished traversing the blossom's nodes
                currentNode = null;
            } else {
                currentNode = currentNode.getTreeParent();
            }
            return currentNode;
        }
    }
}
