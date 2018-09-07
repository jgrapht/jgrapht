/*
 * (C) Copyright 1999-2018, by Nathan Fiedler and Contributors.
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
package org.jgrapht.util;

/**
 * Implements a node of the Fibonacci heap. It holds the information necessary for maintaining the
 * structure of the heap. It also holds the reference to the key value (which is used to determine
 * the heap structure).
 * 
 * @param <T> node data type
 *
 * @author Nathan Fiedler
 */
public class FibonacciHeapNode<T>
{
    /**
     * Node data.
     */
    T data;

    /**
     * first child node
     */
    FibonacciHeapNode<T> child;

    /**
     * left sibling node
     */
    FibonacciHeapNode<T> left;

    /**
     * parent node
     */
    FibonacciHeapNode<T> parent;

    /**
     * right sibling node
     */
    FibonacciHeapNode<T> right;

    /**
     * true if this node has had a child removed since this node was added to its parent
     */
    boolean mark;

    /**
     * key value for this node
     */
    double key;

    /**
     * number of children of this node (does not count grandchildren)
     */
    int degree;

    /**
     * Constructs a new node.
     *
     * @param data data for this node
     */
    public FibonacciHeapNode(T data)
    {
        this.data = data;
    }

    /**
     * Obtain the key for this node.
     *
     * @return the key
     */
    public final double getKey()
    {
        return key;
    }

    /**
     * Obtain the data for this node.
     * 
     * @return the data
     */
    public final T getData()
    {
        return data;
    }

    /**
     * Return the string representation of this object.
     *
     * @return string representing this object
     */
    @Override
    public String toString()
    {
        return Double.toString(key);
    }

    // toString
}

// End FibonacciHeapNode.java
