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

import org.jgrapht.alg.util.Pair;
import org.jgrapht.util.FibonacciHeapNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.jgrapht.alg.matching.blossom.v5.Node.Label.*;

/**
 * This class is a supporting data structure for Kolmogorov's Blossom V algorithm. Represents
 * a vertex of graph. Contains information about the current state of the node (i.e. whether
 * it is an outer node, etc.) and the information needed to maintain the alternating tree structure,
 * which is needed to find an augmenting path of tight edges in the graph to increase the cardinality of the
 * matching.
 *
 * @author Timofey Chudakov
 * @see KolmogorovMinimumWeightPerfectMatching
 * @since June 2018
 */
class Node {
    /**
     * Debug field, is used to set the node's id, todo: remove
     */
    private static int currentId = 0;
    /**
     * The reference to the Fibonacci heap node this {@code Node} is stored in
     */
    FibonacciHeapNode<Node> fibNode;

    /**
     * True if this node is a tree root, implies that this node is outer
     */
    boolean isTreeRoot;
    /**
     * True if this node is a blossom node (also called a "pseudonode", the notions are equivalent)
     */
    boolean isBlossom;
    /**
     * True if this node is outer, i.e. it isn't contracted in some blossom
     */
    boolean isOuter;
    /**
     * Support variable to identify the nodes which have been "processed" by the algorithm. Is used
     * in the shrink and expand operations. Is similar to the {@link Node#isMarked}
     */
    boolean isProcessed;
    /**
     * Support variable. In particular, is used in shrink and expand operation to identify the blossom nodes.
     * Is similar to the {@link Node#isProcessed}
     */
    boolean isMarked;
    /**
     * True if this node is a blossom and has been expanded so that it doesn't belong to the surface graph
     * no more. Is used to lazily update the {@link Node#blossomParent} and {@link Node#blossomGrandparent}
     * reference.
     */
    boolean isRemoved;

    /**
     * Stores the current label of this node. Is valid if this node is outer.
     */
    Label label;
    /**
     * References of the first elements in the linked lists of edges that incident to this node.
     * first[0] is the first outgoing edge, first[1] is the first incoming edge.
     */
    Edge[] first;
    /**
     * Current dual variable of this node. If the node belongs to a tree and is an outer node, then this
     * value can be not valid. The true dual variable is $dual + tree.eps$ if this is a "+" node and
     * $dual - tree.eps$ if this is a "-" node.
     */
    double dual;
    /**
     * An edge, which is incident to this node and currently belongs to the matching
     */
    Edge matched;

    /**
     * Reference to the tree this node belongs to
     */
    Tree tree;
    /**
     * An edge to the parent of this node in the tree structure.
     */
    Edge parentEdge;
    /**
     * The first child in the linked list of children of this node.
     */
    Node firstTreeChild;

    /**
     * Reference of the next tree sibling in the doubly linked list of children of the node parentEdge.getOpposite(this).
     * Is null if this node is the last child of the parent node.
     * <p>
     * If this node is a tree root, references the previous tree root in the doubly linked list of tree roots or
     * is null if this is the last tree root.
     */
    Node treeSiblingNext;
    /**
     * Reference of the previous tree sibling in the doubly linked list of children of the node parentEdge.getOpposite(this).
     * If this node is the first child of the parent node (i.e. parentEdge.getOpposite(this).firstTreeChild == this),
     * references the last sibling.
     * <p>
     * If this node is a tree root, references the previous tree root in the doubly linked list of tree roots.
     */
    Node treeSiblingPrev;

    /**
     * Reference of the blossom this node is contained in
     */
    Node blossomParent;
    /**
     * Reference of some blossom that is a grandparent to this node. Is subject to the path compression technique.
     * Is used to quickly find the penultimate grandparent of this node, i.e. a grandparent, whose blossomParent is
     * an outer node.
     */
    Node blossomGrandparent;
    /**
     * Reference of the next node in the blossom structure in the circular singly linked list of blossom nodes
     */
    Edge blossomSibling;

    /**
     * Debug variable. Todo: remove
     */
    int id;

    /**
     * Constructs a new "+" node
     */
    public Node() {
        this.first = new Edge[2];
        this.label = PLUS;
        this.id = currentId++;
    }

    /**
     * Insert the {@code edge} into linked list of incident edges of this node in the specified direction {@code dir}
     *
     * @param edge edge to insert in the linked list of incident edge
     * @param dir  the direction of this edge with respect to this node
     */
    public void addEdge(Edge edge, int dir) {
        if (first[dir] == null) {
            first[dir] = edge.next[dir] = edge.prev[dir] = edge;
        } else {
            // append this edge to the end of the linked list
            edge.prev[dir] = first[dir].prev[dir];
            edge.next[dir] = first[dir];
            first[dir].prev[dir].next[dir] = edge;
            first[dir].prev[dir] = edge;
        }
        // this is used to maintain the following feature: if an edge has direction dir with respect to this
        // node, then edge.head[dir] is the opposite node
        edge.head[1 - dir] = this;
    }

    /**
     * Removes the {@code edge} from the linked list of edges. Updates the first[dir] reference is needed
     *
     * @param edge the edge to remove
     * @param dir  the directions of the {@code edge} with respect to this node
     */
    public void removeEdge(Edge edge, int dir) {
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
    public Node getTreeGrandparent() {
        Node t = parentEdge.getOpposite(this);
        return t.parentEdge.getOpposite(t);
    }

    /**
     * Helper method, returns the tree parent of this node or null if this node has no tree parent
     *
     * @return node's tree parent or null if this node has no tree parent
     */
    public Node getTreeParent() {
        return parentEdge == null ? null : parentEdge.getOpposite(this);
    }

    /**
     * Appends the {@code child} to the end of the linked list of children of this node. The {@code parentEdge}
     * becomes the parent edge of the {@code child}
     *
     * @param child      the new child of this node
     * @param parentEdge the edge between this node and {@code child}
     * @param grow       true if {@code child} is being grown
     */
    public void addChild(Node child, Edge parentEdge, boolean grow) {
        child.parentEdge = parentEdge;
        child.tree = tree;
        child.treeSiblingNext = firstTreeChild;
        if (grow) {
            // if child is being grown => we have to overwrite all its tree structure data
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

    public Node getOppositeMatched() {
        return matched.getOpposite(this);
    }

    /**
     * If this node is a tree root then removes this nodes from the tree roots doubly linked list.
     * Otherwise, removes this vertex from the doubly linked list of tree children and updates
     * parent.firstTreeChild accordingly.
     */
    public void removeFromChildList() {
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
     * Appends the child list of this node to the beginning of the child list of the {@code blossom}.
     *
     * @param blossom the node to which the children of the current node are moved
     */
    public void moveChildrenTo(Node blossom) {
        if (firstTreeChild != null) {
            if (blossom.firstTreeChild == null) {
                blossom.firstTreeChild = firstTreeChild;
            } else {
                Node first = firstTreeChild;
                Node t = blossom.firstTreeChild.treeSiblingPrev;
                // concatenating child lists
                first.treeSiblingPrev.treeSiblingNext = blossom.firstTreeChild;
                blossom.firstTreeChild.treeSiblingPrev = first.treeSiblingPrev;
                // setting reference to the last child and updating firstTreeChild reference of the blossom
                first.treeSiblingPrev = t;
                blossom.firstTreeChild = first;
            }
            firstTreeChild = null; // now this node has no children
        }
    }

    /**
     * Computes and returns the penultimate blossom of this node, i.e. the blossom which isn't outer but whose
     * blossomParent is outer. This method also applies path compression technique to the blossomGrandparent
     * references. More precisely, it finds the penultimate blossom of this node and changes blossomGrandparent
     * references of the previous nodes to point to the resulting penultimate blossom.
     *
     * @return the penultimate blossom of this node
     */
    public Node getPenultimateBlossom() {
        if (blossomParent == null) {
            return null;
        } else {
            Node current = this;
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
            // now current references the penultimate blossom we were looking for
            // now we change blossomParent references to point to current
            Node prev = this;
            Node next;
            while (prev != current) {
                next = prev.blossomGrandparent;
                prev.blossomGrandparent = current; // apply path compression
                prev = next;
            }

            return current;
        }
    }

    /**
     * Computes and returns the penultimate blossom of this node. The return value of this method
     * always equals to the value returned by {@link Node#getPenultimateBlossom()}. However,
     * the main difference is that this method changes the blossomGrandparent references to point
     * to the node that is previous to the resulting penultimate blossom.
     *
     * @return the penultimate blossom of this node
     */
    public Node getPenultimateBlossomAndFixBlossomGrandparent() {
        Node current = this;
        Node prev = null;
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
        // now current is the penultimate blossom, prev.blossomParent == current
        // all the nodes, that are lower than prev, must have blossomGrandparent referencing
        // a node, that is not higher than prev
        if (prev != null) {
            Node prevNode = this;
            Node nextNode;
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
    public boolean isPlusNode() {
        return label == PLUS;
    }

    /**
     * Checks whether this node is a minus node
     *
     * @return true if the label of this node is {@link Label#MINUS}, false otherwise
     */
    public boolean isMinusNode() {
        return label == MINUS;
    }

    /**
     * Checks whether this node is an infinity node
     *
     * @return true if the label of this node is {@link Label#INFINITY}, false otherwise
     */
    public boolean isInfinityNode() {
        return label == INFINITY;
    }

    /**
     * Updates the label of this node
     *
     * @param label the new label of this node
     */
    public void setLabel(Label label) {
        this.label = label;
    }

    /**
     * Returns the true dual variable of this node. If this node is outer and belongs to some tree then
     * it is subject to the laze delta spreading technique. Otherwise, its dual is valid.
     *
     * @return the true dual variable of this node
     */
    public double getTrueDual() {
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
    public IncidentEdgeIterator incidentEdgesIterator() {
        return new IncidentEdgeIterator();
    }

    @Override
    public String toString() {
        return "Node id = " + id + ", dual: " + dual + ", true dual: " + getTrueDual()
                + ", label: " + label + (isMarked ? ", marked" : "") + (isProcessed ? ", processed" : "");
    }

    /**
     * Represents nodes' labels
     */
    public enum Label {
        /**
         * The node is on the even layer in the tree (root has layer 0)
         */
        PLUS,
        /**
         * The node is on the odd layer in the tree (root has layer 0)
         */
        MINUS,
        /**
         * This node doesn't belong to any tree
         */
        INFINITY
    }

    /**
     * An iterator over incident edges of a node
     */
    public class IncidentEdgeIterator implements Iterator<Edge> {

        /**
         * The direction of the edge returned by the {@code IncidentEdgeIterator}
         */
        private int currentDir;
        /**
         * Direction of the {@code nextEdge}
         */
        private int nextDir;
        /**
         * The edge that will be returned after the next call to {@link IncidentEdgeIterator#next()}.
         * Is null if all edges of the current node have been traversed.
         */
        private Edge nextEdge;

        /**
         * Constructs a new instance of the IncidentEdgeIterator.
         */
        public IncidentEdgeIterator() {
            if (first[0] == null) {
                nextEdge = first[1];
                nextDir = 1;

            } else {
                nextEdge = first[0];
                nextDir = 0;
            }
        }

        /**
         * Returns the direction of the edge returned by this iterator
         *
         * @return the direction of the edge returned by this iterator
         */
        public int getDir() {
            return currentDir;
        }

        @Override
        public boolean hasNext() {
            return nextEdge != null;
        }

        @Override
        public Edge next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Edge result = nextEdge;
            advance();
            return result;
        }

        /**
         * Advances this iterator to the next incident edge. If previous edge was the last one with direction
         * 0, then the direction of this iterator is changes. If previous edge was the last incident edge, then
         * {@code nextEdge} becomes null.
         */
        private void advance() {
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
