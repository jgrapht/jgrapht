/*
 * (C) Copyright 2018-2021, by Timofey Chudakov and Contributors.
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
import org.jheaps.tree.*;

/**
 * This class is a data structure for Kolmogorov's Blossom V algorithm.
 * <p>
 * Is used to maintain an auxiliary graph whose nodes correspond to alternating trees in the Blossom
 * V algorithm. Let's denote the current tree $T$ and some other tree $T'$. Every tree edge contains
 * three heaps:<br>
 * <ol>
 * <li>a heap of (+, +) cross-tree edges. This heap contains all edges between two "+" nodes where
 * one node belongs to tree $T$ and another to $T'$. The (+, +) cross-tree edges are used to augment
 * the matching.</li>
 * <li>a heap of (+, -) cross-tree edges</li>
 * <li>a heap of (-, +) cross-tree edges</li>
 * </ol>
 * <b>Note:</b> from the tree edge perspective there is no difference between a heap of (+, -) and
 * (-, +) cross-tree edges. That's why we distinguish these heaps by the direction of the edge. Here
 * the direction is considered with respect to the trees $T$ and $T'$ based upon the notation
 * introduced above.
 * <p>
 * Every tree edge is directed from one tree to another and every tree edge belongs to the two
 * doubly linked lists of tree edges. The presence of a tree edge in these lists in maintained by
 * the two-element arrays {@link BlossomVTreeEdge#prev} and {@link BlossomVTreeEdge#next}. For one
 * tree the edge is an outgoing tree edge; for the other, an incoming. In the first case it belongs
 * to the {@code tree.first[0]} linked list; in the second, to the {@code tree.first[1]} linked
 * list.
 * <p>
 * Let {@code tree} be a tail of the edge, and {@code oppositeTree} a head of the edge. Then
 * {@code edge.head[0] == oppositeTree} and {@code edge.head[1] == tree}.
 *
 * @author Timofey Chudakov
 * @see KolmogorovWeightedPerfectMatching
 * @see BlossomVTree
 * @see BlossomVEdge
 */
class BlossomVTreeEdge
{
    /**
     * Two-element array of trees this edge is incident to.
     */
    BlossomVTree[] head;
    /**
     * A two-element array of references to the previous elements in the circular doubly linked
     * lists of tree edges. The lists are circular with one exception: the lastElement.next[dir] ==
     * null. Each list belongs to one of the endpoints of this edge.
     */
    BlossomVTreeEdge[] prev;
    /**
     * A two-element array of references to the next elements in the circular doubly linked lists of
     * tree edges. The lists are circular with one exception: the lastElementInTheList.next[dir] ==
     * null. Each list belongs to one of the endpoints of this edge.
     */
    BlossomVTreeEdge[] next;
    /**
     * A heap of (+, +) cross-tree edges
     */
    MergeableAddressableHeap<Double, BlossomVEdge> plusPlusEdges;
    /**
     * A heap of (-, +) cross-tree edges
     */
    MergeableAddressableHeap<Double, BlossomVEdge> plusMinusEdges0;
    /**
     * A heap of (+, -) cross-tree edges
     */
    MergeableAddressableHeap<Double, BlossomVEdge> plusMinusEdges1;

    /**
     * Constructs a new tree edge by initializing arrays and heaps
     */
    public BlossomVTreeEdge()
    {
        this.head = new BlossomVTree[2];
        this.prev = new BlossomVTreeEdge[2];
        this.next = new BlossomVTreeEdge[2];
        this.plusPlusEdges = new PairingHeap<>();
        this.plusMinusEdges0 = new PairingHeap<>();
        this.plusMinusEdges1 = new PairingHeap<>();
    }

    /**
     * Removes this edge from both doubly linked lists of tree edges.
     */
    public void removeFromTreeEdgeList()
    {
        for (int dir = 0; dir < 2; dir++) {
            if (prev[dir] != null) {
                prev[dir].next[dir] = next[dir];
            } else {
                // this is the first edge in this direction
                head[1 - dir].first[dir] = next[dir];
            }
            if (next[dir] != null) {
                next[dir].prev[dir] = prev[dir];
            }
        }
        head[0] = head[1] = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "BlossomVTreeEdge (" + head[0].id + ":" + head[1].id + ")";
    }

    /**
     * Adds {@code edge} to the heap of (-, +) cross-tree edges. As explained in the class
     * description, this method chooses {@link BlossomVTreeEdge#plusMinusEdges0} or
     * {@link BlossomVTreeEdge#plusMinusEdges1} based upon the {@code direction}. The key is
     * edge.slack
     *
     * @param edge an edge to add to the current heap of (-, +) cross-tree edges.
     * @param direction direction of this tree edge wrt. current tree and opposite tree
     */
    public void addToCurrentMinusPlusHeap(BlossomVEdge edge, int direction)
    {
        edge.handle = getCurrentMinusPlusHeap(direction).insert(edge.slack, edge);
    }

    /**
     * Adds {@code edge} to the heap of (+, -) cross-tree edges. As explained in the class
     * description, this method chooses {@link BlossomVTreeEdge#plusMinusEdges0} or
     * {@link BlossomVTreeEdge#plusMinusEdges1} based upon the {@code direction}. The key is
     * edge.slack
     *
     * @param edge an edge to add to the current heap of (+, -) cross-tree edges.
     * @param direction direction of this tree edge wrt. current tree and opposite tree
     */
    public void addToCurrentPlusMinusHeap(BlossomVEdge edge, int direction)
    {
        edge.handle = getCurrentPlusMinusHeap(direction).insert(edge.slack, edge);
    }

    /**
     * Adds {@code edge} to the heap of (+, +) cross-tree edges. The key is edge.slack
     *
     * @param edge an edge to add to the heap of (+, +) cross-tree edges
     */
    public void addPlusPlusEdge(BlossomVEdge edge)
    {
        edge.handle = plusPlusEdges.insert(edge.slack, edge);
    }

    /**
     * Removes {@code edge} from the current heap of (-, +) cross-tree edges. As explained in the
     * class description, this method chooses {@link BlossomVTreeEdge#plusMinusEdges0} or
     * {@link BlossomVTreeEdge#plusMinusEdges1} based upon the {@code direction}.
     *
     * @param edge an edge to remove
     */
    public void removeFromCurrentMinusPlusHeap(BlossomVEdge edge)
    {
        edge.handle.delete();
        edge.handle = null;
    }

    /**
     * Removes {@code edge} from the current heap of (+, -) cross-tree edges. As explained in the
     * class description, this method chooses {@link BlossomVTreeEdge#plusMinusEdges0} or
     * {@link BlossomVTreeEdge#plusMinusEdges1} based upon the {@code direction}.
     *
     * @param edge an edge to remove
     */
    public void removeFromCurrentPlusMinusHeap(BlossomVEdge edge)
    {
        edge.handle.delete();
        edge.handle = null;
    }

    /**
     * Removes {@code edge} from the heap of (+, +) cross-tree edges.
     *
     * @param edge an edge to remove
     */
    public void removeFromPlusPlusHeap(BlossomVEdge edge)
    {
        edge.handle.delete();
        edge.handle = null;
    }

    /**
     * Returns the current heap of (-, +) cross-tree edges. Always returns a heap different from
     * {@code getCurrentPlusMinusHeap(currentDir)}
     *
     * @param currentDir the current direction of this edge
     * @return returns current heap of (-, +) cross-tree edges
     */
    public MergeableAddressableHeap<Double, BlossomVEdge> getCurrentMinusPlusHeap(int currentDir)
    {
        return currentDir == 0 ? plusMinusEdges0 : plusMinusEdges1;
    }

    /**
     * Returns the current heap of (+, -) cross-tree edges. Always returns a heap different from
     * {@code getCurrentMinusPlusHeap(currentDir)}
     *
     * @param currentDir the current direction of this edge
     * @return returns current heap of (+, -) cross-tree edges
     */
    public MergeableAddressableHeap<Double, BlossomVEdge> getCurrentPlusMinusHeap(int currentDir)
    {
        return currentDir == 0 ? plusMinusEdges1 : plusMinusEdges0;
    }
}
