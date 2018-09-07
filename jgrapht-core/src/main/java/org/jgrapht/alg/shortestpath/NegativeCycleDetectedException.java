/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.GraphPath;

/**
 * An exception used to notify about the detection of a negative cycle. The exception 
 * may carry the negative cycle to the user.
 * 
 * @author Dimitrios Michail
 */
public class NegativeCycleDetectedException
    extends
    RuntimeException
{
    private static final long serialVersionUID = -8064609917721881630L;

    private GraphPath<?, ?> cycle;
    
    /**
     * Constructs a new exception with {@code null} as its detail message. The cause is not
     * initialized, and may subsequently be initialized by a call to {@link #initCause}.
     */
    public NegativeCycleDetectedException()
    {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized,
     * and may subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the
     *        {@link #getMessage()} method.
     */
    public NegativeCycleDetectedException(String message)
    {
        super(message);
    }
    
    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized,
     * and may subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the
     *        {@link #getMessage()} method.
     * @param cycle the negative weight cycle
     */
    public NegativeCycleDetectedException(String message, GraphPath<?,?> cycle)
    {
        super(message);
        this.cycle = cycle;
    }

    /**
     * Get the actual negative cycle, or null if not provided.
     * @return the negative cycle or null.
     */
    public GraphPath<?, ?> getCycle()
    {
        return cycle;
    }

    /**
     * Set the negative cycle.
     * @param cycle the negative cycle.
     */
    public void setCycle(GraphPath<?, ?> cycle)
    {
        this.cycle = cycle;
    }

}
