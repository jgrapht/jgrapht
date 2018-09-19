/*
 * (C) Copyright 1999-2018, by Nathan Fiedler and Contributors.
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
package org.jgrapht.util;

import java.util.*;

/**
 * A Fibonacci heap data structure with a custom comparator.
 *
 * Much of the code in this class is based on the algorithms in the "Introduction to Algorithms" by
 * Cormen, Leiserson, and Rivest in Chapter 21. The amortized running time of most of these methods
 * is O(1), making it a very fast data structure. Several have an actual running time of O(1).
 * removeMin() and delete() have O(log n) amortized running times because they do the heap
 * consolidation.
 *
 * <p>
 * <b>Note that this implementation is not synchronized.</b> If multiple threads access a set
 * concurrently, and at least one of the threads modifies the set, it <i>must</i> be synchronized
 * externally. This is typically accomplished by synchronizing on some object that naturally
 * encapsulates the set.
 * </p>
 *
 *
 * @param <K> key type
 * @param <T> data type
 *
 * @author Nathan Fiedler
 * @author Dimitrios Michail
 * 
 * @deprecated In favor of using data structures from the jheaps library. 
 */
@Deprecated
public class GenericFibonacciHeap<K, T>
{
    private static final double ONEOVERLOGPHI = 1.0 / Math.log((1.0 + Math.sqrt(5.0)) / 2.0);

    /**
     * Points to the minimum node in the heap.
     */
    private Node minNode;

    /**
     * Number of nodes in the heap.
     */
    private int nNodes;

    /**
     * Key comparator
     */
    private Comparator<K> comparator;

    /**
     * Constructs an empty heap.
     *
     * @param comparator the comparator for key comparisons
     */
    public GenericFibonacciHeap(Comparator<K> comparator)
    {
        this.comparator = Objects.requireNonNull(comparator, "Key comparator cannot be null");
    }

    /**
     * Tests if the Fibonacci heap is empty or not. Returns true if the heap is empty, false
     * otherwise.
     *
     * <p>
     * Running time: $O(1)$ actual
     *
     * @return true if the heap is empty, false otherwise
     */
    public boolean isEmpty()
    {
        return minNode == null;
    }

    /**
     * Removes all elements from this heap.
     */
    public void clear()
    {
        minNode = null;
        nNodes = 0;
    }

    /**
     * Inserts a new element into the heap. No heap consolidation is performed at this time, the new
     * node is simply inserted into the root list of this heap.
     *
     * <p>
     * Running time: O(1) actual
     *
     * @param key the key
     * @param data the value
     * @return The new heap node
     */
    public Node insert(K key, T data)
    {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        Node node = new Node(key, data);

        // concatenate node into min list
        if (minNode != null) {
            node.left = minNode;
            node.right = minNode.right;
            minNode.right = node;
            node.right.left = node;

            if (comparator.compare(key, minNode.key) < 0) {
                minNode = node;
            }
        } else {
            node.left = node;
            node.right = node;
            minNode = node;
        }

        nNodes++;

        return node;
    }

    /**
     * Returns the smallest element in the heap. This smallest element is the one with the minimum
     * key value.
     *
     * <p>
     * Running time: O(1) actual
     *
     * @return heap node with the smallest key
     */
    public Node min()
    {
        return minNode;
    }

    /**
     * Removes the smallest element from the heap. This will cause the trees in the heap to be
     * consolidated, if necessary.
     *
     * <p>
     * Running time: $O(\log n)$ amortized
     *
     * @return node with the smallest key
     */
    public Node removeMin()
    {
        Node z = minNode;

        if (z != null) {
            int numKids = z.degree;
            Node x = z.child;
            Node tempRight;

            // for each child of z do...
            while (numKids > 0) {
                tempRight = x.right;

                // remove x from child list
                x.left.right = x.right;
                x.right.left = x.left;

                // add x to root list of heap
                x.left = minNode;
                x.right = minNode.right;
                minNode.right = x;
                x.right.left = x;

                // set parent[x] to null
                x.parent = null;
                x = tempRight;
                numKids--;
            }

            // remove z from root list of heap
            z.left.right = z.right;
            z.right.left = z.left;

            if (z == z.right) {
                minNode = null;
            } else {
                minNode = z.right;
                consolidate();
            }

            // decrement size of heap
            nNodes--;

            // clear z
            z.left = null;
            z.right = null;
            z.degree = 0;
            z.child = null;
            z.mark = false;
        }

        return z;
    }

    /**
     * Returns the size of the heap which is measured in the number of elements contained in the
     * heap.
     *
     * <p>
     * Running time: $O(1)$ actual
     *
     * @return number of elements in the heap
     */
    public int size()
    {
        return nNodes;
    }

    /**
     * Joins two Fibonacci heaps into a new one. No heap consolidation is performed at this time.
     * The two root lists are simply joined together. The method assumes that both heaps use the
     * same comparator, otherwise the behavior is undefined.
     *
     * <p>
     * Running time: $O(1)$ actual
     *
     * @param h1 first heap
     * @param h2 second heap
     * @param <K> type of key stored in the heap
     * @param <T> type of data stored in the heap
     *
     * @return new heap containing h1 and h2
     */
    public static <K, T> GenericFibonacciHeap<K, T> union(
        GenericFibonacciHeap<K, T> h1, GenericFibonacciHeap<K, T> h2)
    {
        if (h1 == null || h2 == null) {
            throw new IllegalArgumentException("Heaps cannot be null");
        }

        GenericFibonacciHeap<K, T> h = new GenericFibonacciHeap<>(h1.comparator);

        h.minNode = h1.minNode;

        if (h.minNode != null) {
            if (h2.minNode != null) {
                h.minNode.right.left = h2.minNode.left;
                h2.minNode.left.right = h.minNode.right;
                h.minNode.right = h2.minNode;
                h2.minNode.left = h.minNode;

                if (h1.comparator.compare(h2.minNode.key, h1.minNode.key) < 0) {
                    h.minNode = h2.minNode;
                }
            }
        } else {
            h.minNode = h2.minNode;
        }

        h.nNodes = h1.nNodes + h2.nNodes;

        return h;
    }

    /**
     * Creates a String representation of this Fibonacci heap.
     *
     * @return String of this.
     */
    @Override
    public String toString()
    {
        if (minNode == null) {
            return "FibonacciHeap=[]";
        }

        // create a new stack and put root on it
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(minNode);

        StringBuilder buf = new StringBuilder(512);
        buf.append("FibonacciHeap=[");

        // do a simple breadth-first traversal on the tree
        while (!stack.isEmpty()) {
            Node curr = stack.pop();
            buf.append(curr);
            buf.append(", ");

            if (curr.child != null) {
                stack.push(curr.child);
            }

            Node start = curr;
            curr = curr.right;

            while (curr != start) {
                buf.append(curr);
                buf.append(", ");

                if (curr.child != null) {
                    stack.push(curr.child);
                }

                curr = curr.right;
            }
        }

        buf.append(']');

        return buf.toString();
    }

    /**
     * Performs a cascading cut operation. This cuts y from its parent and then does the same for
     * its parent, and so on up the tree.
     *
     * <p>
     * Running time: O(log n); O(1) excluding the recursion
     *
     * @param y node to perform cascading cut on
     */
    private void cascadingCut(Node y)
    {
        Node z = y.parent;

        // while there's a parent...
        while(z != null){
            // if y is unmarked, set it marked and stop
            if(!y.mark){
                y.mark = true;
                return;
            }else{
                // y is marked, cut it from parent and continue cascading cut with z
                cut(y, z);
                // proceed
                y = z;
                z = z.parent;
            }
        }
    }

    /**
     * Consolidate trees
     */
    private void consolidate()
    {
        int arraySize = ((int) Math.floor(Math.log(nNodes) * ONEOVERLOGPHI)) + 1;

        List<Node> array = new ArrayList<>(arraySize);

        // Initialize degree array
        for (int i = 0; i < arraySize; i++) {
            array.add(null);
        }

        // Find the number of root nodes.
        int numRoots = 0;
        Node x = minNode;

        if (x != null) {
            numRoots++;
            x = x.right;

            while (x != minNode) {
                numRoots++;
                x = x.right;
            }
        }

        // For each node in root list do...
        while (numRoots > 0) {
            // Access this node's degree..
            int d = x.degree;
            Node next = x.right;

            // ..and see if there's another of the same degree.
            for (;;) {
                Node y = array.get(d);
                if (y == null) {
                    // Nope.
                    break;
                }

                // There is, make one of the nodes a child of the other.
                // Do this based on the key value.
                if (comparator.compare(x.key, y.key) > 0) {
                    Node temp = y;
                    y = x;
                    x = temp;
                }

                // FibonacciHeapNode<T> y disappears from root list.
                link(y, x);

                // We've handled this degree, go to next one.
                array.set(d, null);
                d++;
            }

            // Save this node for later when we might encounter another
            // of the same degree.
            array.set(d, x);

            // Move forward through list.
            x = next;
            numRoots--;
        }

        // Set min to null (effectively losing the root list) and
        // reconstruct the root list from the array entries in array[].
        minNode = null;

        for (int i = 0; i < arraySize; i++) {
            Node y = array.get(i);
            if (y == null) {
                continue;
            }

            // We've got a live one, add it to root list.
            if (minNode != null) {
                // First remove node from root list.
                y.left.right = y.right;
                y.right.left = y.left;

                // Now add to root list, again.
                y.left = minNode;
                y.right = minNode.right;
                minNode.right = y;
                y.right.left = y;

                // Check if this is a new min.
                if (comparator.compare(y.key, minNode.key) < 0) {
                    minNode = y;
                }
            } else {
                minNode = y;
            }
        }
    }

    /**
     * The reverse of the link operation: removes x from the child list of y. This method assumes
     * that min is non-null.
     *
     * <p>
     * Running time: O(1)
     *
     * @param x child of y to be removed from y's child list
     * @param y parent of x about to lose a child
     */
    private void cut(Node x, Node y)
    {
        // remove x from childlist of y and decrement degree[y]
        x.left.right = x.right;
        x.right.left = x.left;
        y.degree--;

        // reset y.child if necessary
        if (y.child == x) {
            y.child = x.right;
        }

        if (y.degree == 0) {
            y.child = null;
        }

        // add x to root list of heap
        x.left = minNode;
        x.right = minNode.right;
        minNode.right = x;
        x.right.left = x;

        // set parent[x] to nil
        x.parent = null;

        // set mark[x] to false
        x.mark = false;
    }

    /**
     * Make node y a child of node x.
     *
     * <p>
     * Running time: O(1) actual
     *
     * @param y node to become child
     * @param x node to become parent
     */
    private void link(Node y, Node x)
    {
        // remove y from root list of heap
        y.left.right = y.right;
        y.right.left = y.left;

        // make y a child of x
        y.parent = x;

        if (x.child == null) {
            x.child = y;
            y.right = y;
            y.left = y;
        } else {
            y.left = x.child;
            y.right = x.child.right;
            x.child.right = y;
            y.right.left = y;
        }

        // increase degree[x]
        x.degree++;

        // set mark[y] false
        y.mark = false;
    }

    /**
     * The Fibonacci heap node.
     */
    public class Node
    {
        /**
         * Node data.
         */
        T data;

        /**
         * first child node
         */
        Node child;

        /**
         * left sibling node
         */
        Node left;

        /**
         * parent node
         */
        Node parent;

        /**
         * right sibling node
         */
        Node right;

        /**
         * true if this node has had a child removed since this node was added to its parent
         */
        boolean mark;

        /**
         * key value for this node
         */
        K key;

        /**
         * number of children of this node (does not count grandchildren)
         */
        int degree;

        /**
         * Constructs a new node.
         *
         * @param key the key of this node
         * @param data data for this node
         */
        Node(K key, T data)
        {
            this.key = key;
            this.data = data;
        }

        /**
         * Obtain the key for this node.
         *
         * @return the key
         */
        public K getKey()
        {
            return key;
        }

        /**
         * Obtain the data for this node.
         *
         * @return the data
         */
        public T getData()
        {
            return data;
        }

        /**
         * Set the data of this node
         *
         * @param data the new data
         */
        public void setData(T data)
        {
            this.data = data;
        }

        /**
         * Decreases the key value. The structure of the heap may be changed and will not be
         * consolidated.
         *
         * <p>
         * Running time: $O(1)$ amortized
         *
         * @param k new key value for node x
         *
         * @exception IllegalArgumentException thrown if k is larger than the current key
         */
        public void decreaseKey(K k)
        {
            if (comparator.compare(k, this.key) > 0) {
                throw new IllegalArgumentException(
                    "decreaseKey() got larger key value. Current key: " + key + " new key: " + k);
            }

            if (this.right == null) {
                throw new IllegalArgumentException("Invalid heap node");
            }

            this.key = k;

            Node y = this.parent;

            if ((y != null) && (comparator.compare(this.key, y.key) < 0)) {
                cut(this, y);
                cascadingCut(y);
            }

            if (comparator.compare(this.key, minNode.key) < 0) {
                minNode = this;
            }
        }

        /**
         * Deletes a node from the heap. The trees in the heap will be consolidated, if necessary.
         *
         * <p>
         * Running time: O(log n) amortized
         */
        public void delete()
        {
            // make x as small as possible
            forceDecreaseToMinimum();

            // remove the smallest, which decreases n also
            removeMin();
        }

        /**
         * Force decrease a key to minimum. Used for node deletion.
         */
        private void forceDecreaseToMinimum()
        {
            if (this.right == null) {
                throw new IllegalArgumentException("Invalid heap node");
            }
            Node y = this.parent;

            if (y != null) {
                cut(this, y);
                cascadingCut(y);
            }

            minNode = this;
        }

        /**
         * Return the string representation of this object.
         *
         * @return string representing this object
         */
        @Override
        public String toString()
        {
            return key.toString();
        }
    }

}
