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

import java.util.*;

import static org.jgrapht.alg.matching.blossom.v5.BlossomVNode.Label.*;

/**
 * This class is a data structure for Kolmogorov's Blossom V algorithm.
 * <p>
 * It represents a vertex of graph, and contains three major blocks of data needed for the
 * algorithm.
 * <ul>
 * <li>Node's state information, i.e. {@link BlossomVNode#label}, {@link BlossomVNode#isTreeRoot},
 * etc. This information is maintained dynamically and is changed by
 * {@link BlossomVPrimalUpdater}</li>
 * <li>Information needed to maintain alternating tree structure. It is designed to be able to
 * quickly plant subtrees, split and concatenate child lists, traverse the tree up and down</li>
 * <li>information needed to maintain a "pyramid" of contracted nodes. The common use-cases are to
 * traverse the nodes of a blossom, to move from some node up to the outer blossom (or penultimate
 * blossom, if the outer one is being expanded)</li>
 * </ul>
 * <p>
 * Each node has a dual variable. This is the only information that can be changed by the
 * {@link BlossomVDualUpdater}. This variable is updated lazily due to performance reasons.
 * <p>
 * The edges incident to a node are stored in two linked lists. The first linked list is used for
 * outgoing edges; the other, for incoming edges. The notions of outgoing and incoming edges are
 * symmetric in the context of this algorithm since the initial graph is undirected. The first
 * element in the list of outgoing edges is {@code BlossomVNode#first[0]}, the first element in the
 * list of incoming edges is {@code BlossomVNode#first[1]}.
 * <p>
 * A node is called a <i>plus</i> node if it belongs to the even layer of some alternating tree
 * (root has layer 0). Then its label is {@link Label#PLUS}. A node is called a <i>minus</i> node if
 * it belongs to the odd layer of some alternating tree. Then its label is {@link Label#MINUS}. A
 * node is called an <i>infinity</i> or <i>free</i> node if it doesn't belong to any alternating
 * tree. A node is called <i>outer</i> it belongs to the surface graph, i.e. it is not contracted. A
 * node is called a <i>blossom</i> or <i>pseudonode</i> if it emerged from contracting an odd
 * circuit. This implies that this node doesn't belong to the original graph. A node is called
 * <i>matched</i>, if it is matched to some other node. If a node is free, it means that it is
 * matched. If a node is not a free node, then it necessarily belongs to some tree. If a node isn't
 * matched, it necessarily is a tree root.
 *
 * @author Timofey Chudakov
 * @see KolmogorovWeightedPerfectMatching
 */
class BlossomVNode
{
    /**
     * Node from the heap this node is stored in
     */
    AddressableHeap.Handle<Double, BlossomVNode> handle;
    /**
     * True if this node is a tree root, implies that this node is outer and isn't matched
     */
    boolean isTreeRoot;
    /**
     * True if this node is a blossom node (also called a "pseudonode", the notions are equivalent)
     */
    boolean isBlossom;
    /**
     * True if this node is outer, i.e. it isn't contracted in some blossom and belongs to the
     * surface graph
     */
    boolean isOuter;
    /**
     * Support variable to identify the nodes which have been "processed" in some sense by the
     * algorithm. Is used in the shrink and expand operations.
     * <p>
     * For example, during the shrink operation we traverse the odd circuit and apply dual changes.
     * All nodes from this odd circuit are marked, i.e. {@code node.isMarked == true}. When a node
     * on this circuit is traversed, we set {@code node.isProcessed} to {@code true}. When a (+, +)
     * inner edge is encountered, we can determine whether the opposite endpoint has been processed
     * or not depending on the value of this variable. Without this variable inner (+, +) edges can
     * be processed twice (which is wrong).
     */
    boolean isProcessed;
    /**
     * Support variable. In particular, it is used in shrink and expand operation to quickly
     * determine whether a node belongs to the current blossom or not. Is similar to the
     * {@link BlossomVNode#isProcessed}
     */
    boolean isMarked;

    /**
     * Current label of this node. Is valid if this node is outer.
     */
    Label label;
    /**
     * Two-element array of references of the first elements in the linked lists of edges that are
     * incident to this node. first[0] is the first outgoing edge, first[1] is the first incoming
     * edge, see {@link BlossomVEdge}.
     */
    BlossomVEdge[] first;
    /**
     * Current dual variable of this node. If the node belongs to a tree and is an outer node, then
     * this value may not be valid. The true dual variable is $dual + tree.eps$ if this is a "+"
     * node and $dual - tree.eps$ if this is a "-" node.
     */
    double dual;
    /**
     * An edge which is incident to this node and currently belongs to the matching
     */
    BlossomVEdge matched;
    /**
     * A (+, inf) edge incident to this node. This variable is used during fractional matching
     * initialization and is assigned only to the infinity nodes. In fact, it is used to determine
     * for a particular infinity node the "cheapest" edge to connect it to the tree. The "cheapest"
     * means the edge with minimum slack. When the dual change is bounded by the dual constraints on
     * the (+, inf) edges, we choose the "cheapest" best edge, increase the duals of the tree if
     * needed, and grow this edge.
     */
    BlossomVEdge bestEdge;
    /**
     * Reference to the tree this node belongs to
     */
    BlossomVTree tree;
    /**
     * An edge to the parent node in the tree structure.
     */
    BlossomVEdge parentEdge;
    /**
     * The first child in the linked list of children of this node.
     */
    BlossomVNode firstTreeChild;

    /**
     * Reference of the next tree sibling in the doubly linked list of children of the node
     * parentEdge.getOpposite(this). Is null if this node is the last child of the parent node.
     * <p>
     * If this node is a tree root, references the next tree root in the doubly linked list of tree
     * roots or is null if this is the last tree root.
     */
    BlossomVNode treeSiblingNext;
    /**
     * Reference of the previous tree sibling in the doubly linked list of children of the node
     * parentEdge.getOpposite(this). If this node is the first child of the parent node (i.e.
     * parentEdge.getOpposite(this).firstTreeChild == this), references the last sibling.
     * <p>
     * If this node is a tree root, references the previous tree root in the doubly linked list of
     * tree roots. The first element in the linked list of tree roots is a dummy node which is
     * stored in {@code nodes[nodeNum]}. This is done to quickly determine the first actual tree
     * root via {@code nodes[nodeNum].treeSiblingNext}.
     */
    BlossomVNode treeSiblingPrev;

    /**
     * Reference of the blossom this node is contained in. The blossom parent is always one layer
     * higher than this node.
     */
    BlossomVNode blossomParent;
    /**
     * Reference of some blossom that is higher than this node. This variable is used for the path
     * compression technique. It is used to quickly find the penultimate grandparent of this node,
     * i.e. a grandparent whose blossomParent is an outer node.
     */
    BlossomVNode blossomGrandparent;
    /**
     * Reference of the next node in the blossom structure in the circular singly linked list of
     * blossom nodes. Is used to traverse the blossom nodes in a cyclic order.
     */
    BlossomVEdge blossomSibling;
    /**
     * Position of this node in the array {@code state.nodes}. This helps to determine generic
     * counterpart of this node in constant time.
     */
    int pos;

    /**
     * Constructs a new "+" node with a {@link Label#PLUS} label.
     */
    public BlossomVNode(int pos)
    {
        this.first = new BlossomVEdge[2];
        this.label = PLUS;
        this.pos = pos;
    }

    /**
     * Insert the {@code edge} into linked list of incident edges of this node in the specified
     * direction {@code dir}
     *
     * @param edge edge to insert in the linked list of incident edges
     * @param dir the direction of this edge with respect to this node
     */
    public void addEdge(BlossomVEdge edge, int dir)
    {
        if (first[dir] == null) {
            // the list in the direction dir is empty
            first[dir] = edge.next[dir] = edge.prev[dir] = edge;
        } else {
            // the list in the direction dir isn't empty
            // append this edge to the end of the linked list
            edge.prev[dir] = first[dir].prev[dir];
            edge.next[dir] = first[dir];
            first[dir].prev[dir].next[dir] = edge;
            first[dir].prev[dir] = edge;
        }
        /*
         * this constraint is used to maintain the following feature: if an edge has direction dir
         * with respect to this node, then edge.head[dir] is the opposite node
         */
        edge.head[1 - dir] = this;
    }

    /**
     * Removes the {@code edge} from the linked list of edges incident to this node. Updates the
     * first[dir] reference if needed.
     *
     * @param edge the edge to remove
     * @param dir the directions of the {@code edge} with respect to this node
     */
    public void removeEdge(BlossomVEdge edge, int dir)
    {
        if (edge.prev[dir] == edge) {
            // it is the only edge of this node in the direction dir
            first[dir] = null;
        } else {
            // remove edge from the linked list
            edge.prev[dir].next[dir] = edge.next[dir];
            edge.next[dir].prev[dir] = edge.prev[dir];
            if (first[dir] == edge) {
                first[dir] = edge.next[dir];
            }
        }
    }

    /**
     * Helper method, returns the tree grandparent of this node
     *
     * @return the tree grandparent of this node
     */
    public BlossomVNode getTreeGrandparent()
    {
        BlossomVNode t = parentEdge.getOpposite(this);
        return t.parentEdge.getOpposite(t);
    }

    /**
     * Helper method, returns the tree parent of this node or null if this node has no tree parent
     *
     * @return node's tree parent or null if this node has no tree parent
     */
    public BlossomVNode getTreeParent()
    {
        return parentEdge == null ? null : parentEdge.getOpposite(this);
    }

    /**
     * Appends the {@code child} to the end of the linked list of children of this node. The
     * {@code parentEdge} becomes the parent edge of the {@code child}.
     * <p>
     * Variable {@code grow} is used to determine whether the {@code child} was an infinity node and
     * now is being added in tree structure. Then we have to set {@code child.firstTreeChild} to
     * {@code null} so that all its tree structure variables are changed. This allows us to avoid
     * overwriting the fields during tree destroying.
     *
     * @param child the new child of this node
     * @param parentEdge the edge between this node and {@code child}
     * @param grow true if {@code child} is being grown
     */
    public void addChild(BlossomVNode child, BlossomVEdge parentEdge, boolean grow)
    {
        child.parentEdge = parentEdge;
        child.tree = tree;
        child.treeSiblingNext = firstTreeChild;
        if (grow) {
            // with this check we are able to avoid destroying the tree structure during the augment
            // operation
            child.firstTreeChild = null;
        }
        if (firstTreeChild == null) {
            child.treeSiblingPrev = child;
        } else {
            child.treeSiblingPrev = firstTreeChild.treeSiblingPrev;
            firstTreeChild.treeSiblingPrev = child;
        }
        firstTreeChild = child;
    }

    /**
     * Helper method, returns a node this node is matched to.
     *
     * @return a node this node is matched to.
     */
    public BlossomVNode getOppositeMatched()
    {
        return matched.getOpposite(this);
    }

    /**
     * If this node is a tree root then this method removes this node from the tree root doubly
     * linked list. Otherwise, removes this vertex from the doubly linked list of tree children and
     * updates parent.firstTreeChild accordingly.
     */
    public void removeFromChildList()
    {
        if (isTreeRoot) {
            treeSiblingPrev.treeSiblingNext = treeSiblingNext;
            if (treeSiblingNext != null) {
                treeSiblingNext.treeSiblingPrev = treeSiblingPrev;
            }
        } else {
            if (treeSiblingPrev.treeSiblingNext == null) {
                // this vertex is the first child => we have to update parent.firstTreeChild
                parentEdge.getOpposite(this).firstTreeChild = treeSiblingNext;
            } else {
                // this vertex isn't the first child
                treeSiblingPrev.treeSiblingNext = treeSiblingNext;
            }
            if (treeSiblingNext == null) {
                // this vertex is the last child => we have to set treeSiblingPrev of the firstChild
                if (parentEdge.getOpposite(this).firstTreeChild != null) {
                    parentEdge.getOpposite(this).firstTreeChild.treeSiblingPrev = treeSiblingPrev;
                }
            } else {
                // this vertex isn't the last child
                treeSiblingNext.treeSiblingPrev = treeSiblingPrev;
            }
        }
    }

    /**
     * Appends the child list of this node to the beginning of the child list of the
     * {@code blossom}.
     *
     * @param blossom the node to which the children of the current node are moved
     */
    public void moveChildrenTo(BlossomVNode blossom)
    {
        if (firstTreeChild != null) {
            if (blossom.firstTreeChild == null) {
                blossom.firstTreeChild = firstTreeChild;
            } else {
                BlossomVNode t = blossom.firstTreeChild.treeSiblingPrev;
                // concatenating child lists
                firstTreeChild.treeSiblingPrev.treeSiblingNext = blossom.firstTreeChild;
                blossom.firstTreeChild.treeSiblingPrev = firstTreeChild.treeSiblingPrev;
                // setting reference to the last child and updating firstTreeChild reference of the
                // blossom
                firstTreeChild.treeSiblingPrev = t;
                blossom.firstTreeChild = firstTreeChild;
            }
            firstTreeChild = null; // now this node has no children
        }
    }

    /**
     * Computes and returns the penultimate blossom of this node, i.e. the blossom which isn't outer
     * but whose blossomParent is outer. This method also applies path compression technique to the
     * blossomGrandparent references. More precisely, it finds the penultimate blossom of this node
     * and changes blossomGrandparent references of the previous nodes to point to the resulting
     * penultimate blossom.
     *
     * @return the penultimate blossom of this node
     */
    public BlossomVNode getPenultimateBlossom()
    {
        BlossomVNode current = this;
        while (true) {
            if (!current.blossomGrandparent.isOuter) {
                current = current.blossomGrandparent;
            } else if (current.blossomGrandparent != current.blossomParent) {
                // this is the case when current.blossomGrandparent has been removed
                current.blossomGrandparent = current.blossomParent;
            } else {
                break;
            }
        }
        /*
         * Current references the penultimate blossom we were looking for. Now we change
         * blossomParent references to point to current
         */
        BlossomVNode prev = this;
        BlossomVNode next;
        while (prev != current) {
            next = prev.blossomGrandparent;
            prev.blossomGrandparent = current; // apply path compression
            prev = next;
        }

        return current;
    }

    /**
     * Computes and returns the penultimate blossom of this node. The return value of this method
     * always equals to the value returned by {@link BlossomVNode#getPenultimateBlossom()}. However,
     * the main difference is that this method changes the blossomGrandparent references to point to
     * the node that is previous to the resulting penultimate blossom. This method is used during
     * the expand operation.
     *
     * @return the penultimate blossom of this node
     */
    public BlossomVNode getPenultimateBlossomAndFixBlossomGrandparent()
    {
        BlossomVNode current = this;
        BlossomVNode prev = null;
        while (true) {
            if (!current.blossomGrandparent.isOuter) {
                prev = current;
                current = current.blossomGrandparent;
            } else if (current.blossomGrandparent != current.blossomParent) {
                // this is the case when current.blossomGrandparent has been removed
                current.blossomGrandparent = current.blossomParent;
            } else {
                break;
            }
        }
        /*
         * Now current node is the penultimate blossom, prev.blossomParent == current. All the
         * nodes, that are lower than prev, must have blossomGrandparent referencing a node, that is
         * not higher than prev
         */
        if (prev != null) {
            BlossomVNode prevNode = this;
            BlossomVNode nextNode;
            while (prevNode != prev) {
                nextNode = prevNode.blossomGrandparent;
                prevNode.blossomGrandparent = prev;
                prevNode = nextNode;
            }
        }
        return current;
    }

    /**
     * Checks whether this node is a plus node
     *
     * @return true if the label of this node is {@link Label#PLUS}, false otherwise
     */
    public boolean isPlusNode()
    {
        return label == PLUS;
    }

    /**
     * Checks whether this node is a minus node
     *
     * @return true if the label of this node is {@link Label#MINUS}, false otherwise
     */
    public boolean isMinusNode()
    {
        return label == MINUS;
    }

    /**
     * Checks whether this node is an infinity node
     *
     * @return true if the label of this node is {@link Label#INFINITY}, false otherwise
     */
    public boolean isInfinityNode()
    {
        return label == INFINITY;
    }

    /**
     * Returns the true dual variable of this node. If this node is outer and belongs to some tree
     * then it is subject to the lazy delta spreading technique. Otherwise, its dual is valid.
     *
     * @return the actual dual variable of this node
     */
    public double getTrueDual()
    {
        if (isInfinityNode() || !isOuter) {
            return dual;
        }
        return isPlusNode() ? dual + tree.eps : dual - tree.eps;
    }

    /**
     * Returns an iterator over all incident edges of this node
     *
     * @return a new instance of IncidentEdgeIterator for this node
     */
    public IncidentEdgeIterator incidentEdgesIterator()
    {
        return new IncidentEdgeIterator();
    }

    @Override
    public String toString()
    {
        return "BlossomVNode pos = " + pos + ", dual: " + dual + ", true dual: " + getTrueDual()
            + ", label: " + label + (isMarked ? ", marked" : "")
            + (isProcessed ? ", processed" : "")
            + (blossomParent == null || isOuter ? "" : ", blossomParent = " + blossomParent.pos)
            + (matched == null ? "" : ", matched = " + matched);
    }

    /**
     * Represents nodes' labels
     */
    public enum Label
    {
        /**
         * The node is on an even layer in the tree (root has layer 0)
         */
        PLUS,
        /**
         * The node is on an odd layer in the tree (root has layer 0)
         */
        MINUS,
        /**
         * This node doesn't belong to any tree and is matched to some other node
         */
        INFINITY
    }

    /**
     * An iterator for traversing the edges incident to this node.
     * <p>
     * This iterator has a feature that during every step it knows the next edge it'll return to the
     * caller. That's why it is safe to modify the current edge (move it to another node, for
     * example).
     */
    public class IncidentEdgeIterator
        implements
        Iterator<BlossomVEdge>
    {

        /**
         * The direction of the current edge
         */
        private int currentDir;
        /**
         * Direction of the {@code nextEdge}
         */
        private int nextDir;
        /**
         * The edge that will be returned after the next call to
         * {@link IncidentEdgeIterator#next()}. Is null if all incident edges of the current node
         * have been traversed.
         */
        private BlossomVEdge nextEdge;

        /**
         * Constructs a new instance of the IncidentEdgeIterator.
         */
        public IncidentEdgeIterator()
        {
            nextDir = first[0] == null ? 1 : 0;
            nextEdge = first[nextDir];
        }

        /**
         * Returns the direction of the edge returned by this iterator
         *
         * @return the direction of the edge returned by this iterator
         */
        public int getDir()
        {
            return currentDir;
        }

        @Override
        public boolean hasNext()
        {
            return nextEdge != null;
        }

        @Override
        public BlossomVEdge next()
        {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            BlossomVEdge result = nextEdge;
            advance();
            return result;
        }

        /**
         * Advances this iterator to the next incident edge. If previous edge was the last one with
         * direction 0, then the direction of this iterator changes. If previous edge was the last
         * incident edge, then {@code nextEdge} becomes null.
         */
        private void advance()
        {
            currentDir = nextDir;
            nextEdge = nextEdge.next[nextDir];
            if (nextEdge == first[0]) {
                nextEdge = first[1];
                nextDir = 1;
            } else if (nextEdge == first[1]) {
                nextEdge = null;
            }
        }
    }
}
