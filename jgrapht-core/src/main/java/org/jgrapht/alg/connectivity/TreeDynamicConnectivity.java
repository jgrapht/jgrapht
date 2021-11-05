/*
 * (C) Copyright 2020-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.connectivity;

import org.jgrapht.util.*;

import java.util.*;
import java.util.stream.*;

import static org.jgrapht.util.AVLTree.TreeNode;
import static org.jgrapht.util.DoublyLinkedList.ListNode;

/**
 * Data structure for storing dynamic trees and querying node connectivity
 * <p>
 * This data structure supports the following operations:
 * <ul>
 * <li>Adding an element in $\mathcal{O}(\log 1)$</li>
 * <li>Checking if an element in present in $\mathcal{O}(1)$</li>
 * <li>Connecting two elements in $\mathcal{O}(\log n)$</li>
 * <li>Checking if two elements are connected in $\mathcal{O}(\log n)$</li>
 * <li>Removing connection between two nodes in $\mathcal{O}(\log n)$</li>
 * <li>Removing an element in $\mathcal{O}(deg(element)\cdot\log n + 1)$</li>
 * </ul>
 * <p>
 * This data structure doesn't allow to store graphs with cycles. Also, the edges are considered to
 * be undirected. The memory complexity is linear in the number of inserted elements. The
 * implementation is based on the <a href="https://en.wikipedia.org/wiki/Euler_tour_technique">
 * Euler tour technique</a>.
 * <p>
 * For the description of the Euler tour data structure, we refer to the <i>Monika Rauch Henzinger,
 * Valerie King: Randomized dynamic graph algorithms with polylogarithmic time per operation. STOC
 * 1995: 519-527</i>
 *
 * @param <T> element type
 * @author Timofey Chudakov
 */
public class TreeDynamicConnectivity<T>
{

    /**
     * Mapping from tree minimums to the trees they're stored in. This map contains one entry per
     * each tree, which has at least two nodes.
     */
    private Map<TreeNode<T>, AVLTree<T>> minToTreeMap;
    /**
     * Mapping from the user specified values to the internal nodes they're represented by
     */
    private Map<T, Node> nodeMap;
    /**
     * Mapping from zero-degree nodes to their trees. This map contains one entry for each
     * zero-degree node
     */
    private Map<Node, AVLTree<T>> singletonNodes;

    /**
     * Constructs a new {@code TreeDynamicConnectivity} instance
     */
    public TreeDynamicConnectivity()
    {
        minToTreeMap = new HashMap<>();
        nodeMap = new HashMap<>();
        singletonNodes = new HashMap<>();
    }

    /**
     * Adds an {@code element} to this data structure. If the {@code element} has been added before,
     * this method returns {@code false} and has no effect.
     * <p>
     * This method has $\mathcal{O}(\log 1)$ running time complexity
     *
     * @param element an element to add
     * @return {@code true} upon successful modification, {@code false} otherwise
     */
    public boolean add(T element)
    {
        if (contains(element)) {
            return false;
        }

        AVLTree<T> newTree = new AVLTree<>();
        Node node = new Node(element);

        nodeMap.put(element, node);
        singletonNodes.put(node, newTree);

        return true;
    }

    /**
     * Removes the {@code element} from this data structure. This method has no effect if the
     * {@code element} hasn't been added to this data structure
     * <p>
     * This method has $\mathcal{O}(deg(element)\cdot\log n + 1)$ running time complexity
     *
     * @param element an element to remove
     * @return {@code true} upon successful modification, {@code false} otherwise
     */
    public boolean remove(T element)
    {
        if (!contains(element)) {
            return false;
        }

        Node node = getNode(element);
        while (!node.isSingleton()) {
            T targetValue = node.arcs.getLast().target.value;
            cut(element, targetValue);
        }

        nodeMap.remove(element);
        singletonNodes.remove(node);
        return true;
    }

    /**
     * Checks if this data structure contains the {@code element}.
     * <p>
     * This method has expected $\mathcal{O}(1)$ running time complexity
     *
     * @param element an element to check presence of
     * @return {@code true} if the {@code element} is stored in this data structure, {@code false}
     *         otherwise
     */
    public boolean contains(T element)
    {
        return nodeMap.containsKey(element);
    }

    /**
     * Adds an edge between the {@code first} and {@code second} elements. The method has no effect
     * if the elements are already connected by some path, i.e. belong to the same tree. In the case
     * some of the nodes haven't been added before, they're added to this data structure.
     * <p>
     * This method has $\mathcal{O}(\log n)$ running time complexity
     *
     * @param first an element
     * @param second an element
     * @return {@code true} upon successful modification, {@code false} otherwise
     */
    public boolean link(T first, T second)
    {
        /*
         * Example: we have two trees [1 - 2] and [3 - 4 - 5]
         *
         * Euler tour of the first tree: [1 - 2] Euler tour of the second tree: [3 - 4 - 5 - 4]
         *
         * By invariant used in this implementation, we do not return to the start node
         *
         * Suppose, that we have a request: link(1, 5)
         */

        addIfAbsent(first);
        addIfAbsent(second);

        if (connected(first, second)) {
            return false;
        }

        Node firstNode = getNode(first);
        Node secondNode = getNode(second);

        AVLTree<T> firstTree = getTree(firstNode);
        AVLTree<T> secondTree = getTree(secondNode);

        minToTreeMap.remove(firstTree.getMin());
        minToTreeMap.remove(secondTree.getMin());

        /*
         * First we make the nodes 1 and 5 the roots of the corresponding trees:
         *
         * [1 - 2] --> [1 - 2] [3 - 4 - 5 - 4] --> [5 - 4 - 3 - 4]
         */
        makeRoot(firstTree, firstNode);
        makeRoot(secondTree, secondNode);

        /*
         * Add one more occurrence for the first element to the second tree:
         *
         * [5 - 4 - 3 - 4] --> [1 - 5 - 4 - 3 - 4]
         */
        TreeNode<T> newFirstOccurrence = secondTree.addMin(first);
        Arc newFirstArc = new Arc(secondNode, newFirstOccurrence);
        if (firstNode.isSingleton()) {
            // newFirstArc becomes the first arc of the first node
            singletonNodes.remove(firstNode);
            firstNode.addArcLast(newFirstArc);
        } else {
            /*
             * Since second element will be not the only element adjacent to the first element, we
             * are going to insert the arc to the second element into the circular list of arcs of
             * the first node
             *
             * Since first element is a root currently, we can find out the last outgoing arc by
             * simply checking the last element in its Euler tour
             *
             * In the example above, the last arc is (1, 2), so we're going to append a new arc (1,
             * 5) after it.
             *
             * By invariant we're maintaining, a subtree tour computed by following the arc is
             * placed after the arc tree node reference.
             *
             * For example, the first node will have 2 arcs: (1, 2) and (1, 5). If we follow the arc
             * (1, 2), a subtour will be just [2]. If we follow the arc (1, 5), the subtour will be
             * [5 - 4 - 3 - 4 - 5]. So, the arc will have the following tree node references
             *
             * (1, 2) [(1) - 2 - 1 - 5 - 4 - 3 - 4 - 5] | | ------------ (1, 5) [1 - 2 - (1) - 5 - 4
             * - 3 - 4 - 5] | | --------------------
             *
             * If we decide to make the arc (1, 5) the first arc, the method will just take the tree
             * node reference of the arc (1, 5) and will place it at the first place (1, 5) [(1) - 5
             * - 4 - 3 - 4 - 5 - 1 - 2] | | ------------ (1, 2) [1 - 5 - 4 - 3 - 4 - 5 - (1) - 2] |
             * | ------------------------------------
             */
            T lastChild = firstTree.getMax().getValue();
            Node lastChildNode = getNode(lastChild);
            Arc arcToLastChild = firstNode.getArcTo(lastChildNode);
            firstNode.addArcAfter(arcToLastChild, newFirstArc);
        }

        /*
         * Add one more occurrence for the second element to the second tree:
         *
         * [1 - 5 - 4 - 3 - 4] -> [1 - 5 - 4 - 3 - 4 - 5]
         *
         */
        TreeNode<T> newSecondOccurrence = secondTree.addMax(second);
        Arc newSecondArc = new Arc(firstNode, newSecondOccurrence);
        if (secondNode.isSingleton()) {
            // newSecondArc becomes the first arc of the second node
            singletonNodes.remove(secondNode);
            secondNode.addArcLast(newSecondArc);
        } else {
            /*
             * Similarly to the first case, we need to find out the last arc of the second node. At
             * this moment, the second tree looks like this:
             *
             * [1 - 5 - 4 - 3 - 4 - 5]
             *
             * The only arc of the node 5 is (5, 4). After the link operation, the node five will
             * have one more arc: (5, 1). The tree node references for the node 5 will look like
             * this:
             *
             * (5, 4) [1 - 2 - 1 - (5) - 4 - 3 - 4 - 5] | | -------------------------- (5, 1) [1 - 2
             * - 1 - 5 - 4 - 3 - 4 - (5)] | | ------------------------------------------
             *
             * Note that the invariant of the arc tree node references has a circular manner: the
             * subtree tour of the arc (5, 1) is [1 - 2 - 1], which is right after the tree node
             * reference of the arc (5, 1).
             */
            T lastChild = secondTree.getMax().getPredecessor().getValue();
            Node lastChildNode = getNode(lastChild);
            Arc arcToLastChild = secondNode.getArcTo(lastChildNode);
            secondNode.addArcAfter(arcToLastChild, newSecondArc);
        }

        /*
         * Merge the first and the second tree to obtain an Euler tour of the combined tree:
         *
         * [1 - 2] + [1 - 5 - 4 - 3 - 4 - 5] = [1 - 2 - 1 - 5 - 4 - 3 - 4 - 5]
         */
        firstTree.mergeAfter(secondTree);
        minToTreeMap.put(firstTree.getMin(), firstTree);

        return true;
    }

    /**
     * Checks if the {@code first} and {@code second} belong to the same tree. The method will
     * return {@code false} if either of the elements hasn't been added to this data structure
     * <p>
     * This method has $\mathcal{O}(\log n)$ running time complexity
     *
     * @param first an element
     * @param second an element
     * @return {@code true} if the elements belong to the same tree, {@code false} otherwise
     */
    public boolean connected(T first, T second)
    {
        if (!contains(first) || !contains(second)) {
            return false;
        }
        Node firstNode = getNode(first);
        if (firstNode.isSingleton()) {
            return false;
        }
        Node secondNode = getNode(second);
        if (secondNode.isSingleton()) {
            return false;
        }
        return getTree(firstNode) == getTree(secondNode);
    }

    /**
     * Removes an edge between the {@code first} and {@code second}. This method doesn't have any
     * effect if there's no edge between these elements
     * <p>
     * This method has $\mathcal{O}(\log n)$ running time complexity
     *
     * @param first an element
     * @param second an element
     * @return {@code true} upon successful modification, {@code false} otherwise
     */
    public boolean cut(T first, T second)
    {
        if (!connected(first, second)) {
            return false;
        }
        /*
         * Suppose, we have a tree [2 - [1] - 5 - 4 - 3], which has the following Euler tour:
         *
         * [1 - 2 - 1 - 5 - 4 - 3 - 4 - 5]
         *
         * Let's assume that we received a query: cut(1, 2)
         */
        Node firstNode = getNode(first);
        Node secondNode = getNode(second);

        AVLTree<T> tree = getTree(firstNode);
        minToTreeMap.remove(tree.getMin());

        /*
         * The arcToSecond is (1, 2). The operation of making the arc (1, 2) the last arc will
         * transform the tree as follows:
         *
         * (1, 2) [(1) - 2 - 1 - 5 - 4 - 3 - 4 - 5] --> [1 - 5 - 4 - 3 - 4 - 5 - (1) - 2] | | |
         * --------------------------------------------------------------------------
         *
         * After this operation, a subtree of the arc (1, 2) is at the end of the Euler tour
         */
        Arc arcToSecond = firstNode.getArcTo(secondNode);
        if (arcToSecond == null) {
            throw new IllegalArgumentException(
                String.format("Elements {%s} and {%s} are not connected", first, second));
        }
        makeLastArc(tree, firstNode, arcToSecond);

        /*
         * Now we remove the subtree of the arc (1, 2) from the Euler tour:
         *
         * |-------> [1 - 5 - 4 - 3 - 4 - 5 - 1] (left part [1 - 5 - 4 - 3 - 4 - 5 - 1 - 2] -----|
         * |-------> [2] (right part)
         */
        AVLTree<T> right = tree.splitAfter(arcToSecond.arcTreeNode);

        /*
         * Removing the last occurrence of the element 1 from the Euler tour:
         *
         * [1 - 5 - 4 - 3 - 4 - 5 - 1] --> [1 - 5 - 4 - 3 - 4 - 5]
         *
         * Now the left part is a valid Euler tour
         */
        tree.removeMax();
        firstNode.removeArc(arcToSecond);
        if (!firstNode.isSingleton()) {
            minToTreeMap.put(tree.getMin(), tree);
        } else {
            singletonNodes.put(firstNode, tree);
        }

        /*
         * Removing the last occurrence of the element 2 from the right tree:
         *
         * [2] -> []
         *
         * The element 2 becomes an element of zero degree (a singleton node). No arcs means an
         * empty tree
         *
         * That's why we place it to the map for zero degree nodes
         */
        Arc secondToFirst = secondNode.getArcTo(firstNode);
        right.removeMax();
        secondNode.removeArc(secondToFirst);
        if (!secondNode.isSingleton()) {
            minToTreeMap.put(right.getMin(), right);
        } else {
            singletonNodes.put(secondNode, right);
        }

        return true;
    }

    /**
     * Makes the {@code node} the root of the tree. In practice, this means that the value of the
     * {@code node} is the first in the Euler tour
     *
     * @param tree a tree the {@code node} is stored in
     * @param node a node to make a root
     */
    private void makeRoot(AVLTree<T> tree, Node node)
    {
        if (node.arcs.isEmpty()) {
            return;
        }
        makeFirstArc(tree, node.arcs.get(0));
    }

    /**
     * Makes the {@code arc} the first arc traversed by the Euler tour
     *
     * @param tree corresponding binary tree the Euler tour is stored in
     * @param arc an arc to use for tree re-rooting
     */
    private void makeFirstArc(AVLTree<T> tree, Arc arc)
    {
        AVLTree<T> right = tree.splitBefore(arc.arcTreeNode);
        tree.mergeBefore(right);
    }

    /**
     * Makes the {@code arc} the last arc of the {@code node} according to the Euler tour
     *
     * @param tree corresponding binary tree the Euler tour is stored in
     * @param node a new root node
     * @param arc an arc incident to the {@code node}
     */
    private void makeLastArc(AVLTree<T> tree, Node node, Arc arc)
    {
        if (node.arcs.size() == 1) {
            makeRoot(tree, node);
        } else {
            Arc nextArc = node.getNextArc(arc);
            makeFirstArc(tree, nextArc);
        }
    }

    /**
     * Returns an internal representation of the {@code element}
     *
     * @param element a user specified node element
     * @return an internal representation of the {@code element}
     */
    private Node getNode(T element)
    {
        return nodeMap.get(element);
    }

    /**
     * Returns a binary tree, which contains an Euler tour of the tree the {@code node} belong to
     *
     * @param node a node
     * @return a corresponding binary tree an Euler tour is stored in
     */
    private AVLTree<T> getTree(Node node)
    {
        if (node.isSingleton()) {
            return singletonNodes.get(node);
        }
        return minToTreeMap.get(node.arcs.get(0).arcTreeNode.getTreeMin());
    }

    /**
     * Adds the {@code element} to this data structure if it is not already present
     *
     * @param element a user specified element
     */
    private void addIfAbsent(T element)
    {
        if (!contains(element)) {
            add(element);
        }
    }

    /**
     * An internal representation of the tree nodes.
     * <p>
     * Keeps track of the node values and outgoing arcs. The outgoing arcs are placed according to
     * the order they are traversed in the Euler tour
     */
    private class Node
    {
        /**
         * Node value
         */
        T value;
        /**
         * Arcs list
         */
        DoublyLinkedList<Arc> arcs;
        /**
         * Target node to arc mapping
         */
        Map<Node, Arc> targetMap;

        /**
         * Constructs a new node
         *
         * @param value a user specified element to store in this node
         */
        public Node(T value)
        {
            this.value = value;
            arcs = new DoublyLinkedList<>();
            targetMap = new HashMap<>();
        }

        /**
         * Removes the {@code arc} from the arc list
         *
         * @param arc an arc to remove
         */
        void removeArc(Arc arc)
        {
            arcs.removeNode(arc.listNode);
            arc.listNode = null;
            targetMap.remove(arc.target);
        }

        /**
         * Append the {@code arc} to the arc list
         *
         * @param arc an arc to add
         */
        void addArcLast(Arc arc)
        {
            arc.listNode = arcs.addElementLast(arc);
            targetMap.put(arc.target, arc);
        }

        /**
         * Inserts the {@code newArc} in the arc list after the {@code arc}
         *
         * @param arc an arc already stored in the arc list
         * @param newArc a new arc to add to the arc list
         */
        void addArcAfter(Arc arc, Arc newArc)
        {
            newArc.listNode = arcs.addElementBeforeNode(arc.listNode.getNext(), newArc);
            targetMap.put(newArc.target, newArc);
        }

        /**
         * Returns an arc, which target is equal to the {@code node}
         *
         * @param node a target of the returned arc
         * @return an arc, which target is equal to the {@code node}
         */
        Arc getArcTo(Node node)
        {
            return targetMap.get(node);
        }

        /**
         * Returns an arc which is stored right after the {@code arc}. The result may be equal to
         * the {@code arc}
         *
         * @param arc an arc stored in the arc list
         * @return an arc which is stored right after the {@code arc}
         */
        Arc getNextArc(Arc arc)
        {
            return arc.listNode.getNext().getValue();
        }

        /**
         * Checks if this node is a zero-degree node
         *
         * @return {@code true} if this node is a singleton node, {@code false otherwise}
         */
        public boolean isSingleton()
        {
            return arcs.isEmpty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return String
                .format(
                    "{%s} -> [%s]", value,
                    arcs
                        .stream().map(a -> a.target.value.toString())
                        .collect(Collectors.joining(",")));
        }
    }

    /**
     * An internal representation of the tree edges.
     * <p>
     * Two arcs are created for every existing tree edge. This complies with the way an Euler tour
     * is constructed.
     */
    private class Arc
    {
        /**
         * The target of this arc
         */
        Node target;
        /**
         * A list node this arc is stored in. This is needed for constant time query time on the
         * doubly linked list.
         */
        ListNode<Arc> listNode;
        /**
         * The occurrence of the source node, which precedes the subtree Euler tour stored in the
         * binary tree
         */
        TreeNode<T> arcTreeNode;

        /**
         * Constructs a new arc with the target node {@code target} and the tree node reference
         * {@code arcTreeNode}
         *
         * @param target target node of this arc
         * @param arcTreeNode source tree node reference
         */
        public Arc(Node target, TreeNode<T> arcTreeNode)
        {
            this.target = target;
            this.arcTreeNode = arcTreeNode;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return String.format("{%s} -> {%s}", arcTreeNode.getValue(), target.value);
        }
    }

}
