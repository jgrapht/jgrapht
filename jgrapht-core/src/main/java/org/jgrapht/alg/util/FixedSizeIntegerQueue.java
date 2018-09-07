/*
 * (C) Copyright 2017-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.util;

/**
 * Primitive but efficient implementation of a fixed size queue for integers. Note: this queue is
 * not implemented as a ring, so at most $N$ enqueue operations are allowed, where $N$ is the
 * maximum capacity of the queue! After that, queue.clear() must be invoked.
 *
 * @author Joris Kinable
 */
public final class FixedSizeIntegerQueue
{
    /* Storage array for the elements in the queue */
    private final int[] vs;
    /* Index of left most element in the queue */
    private int i = 0;
    /* Index of right most element in the queue. If i==n, the queue is empty */
    private int n = 0;

    /**
     * Create a queue of fixed size.
     *
     * @param capacity size of the queue
     */
    public FixedSizeIntegerQueue(int capacity)
    {
        assert capacity > 0;
        vs = new int[capacity];
    }

    /**
     * Add an element to the queue.
     *
     * @param e element
     */
    public void enqueue(int e)
    {
        assert n < vs.length;
        vs[n++] = e;
    }

    /**
     * Poll the first element from the queue.
     *
     * @return the first element.
     */
    public int poll()
    {
        assert !isEmpty();
        return vs[i++];
    }

    /**
     * Check if the queue has any items.
     *
     * @return true if the queue is empty
     */
    public boolean isEmpty()
    {
        return i == n;
    }

    /**
     * Returns the number of items in the queue.
     * 
     * @return number of items in the queue
     */
    public int size()
    {
        return n - i;
    }

    /** Empty the queue. */
    public void clear()
    {
        i = 0;
        n = 0;
    }

    /**
     * Returns a textual representation of the queue.
     * 
     * @return a textual representation of the queue.
     */
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for (int j = i; j < n; j++)
            s.append(vs[j]).append(" ");
        return s.toString();
    }
}
