/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;

/**
 * An exception used to notify about the detection of a negative cycle. The exception may carry the
 * negative cycle to the user.
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
    public NegativeCycleDetectedException(String message, GraphPath<?, ?> cycle)
    {
        super(message);
        this.cycle = cycle;
    }

    /**
     * Get the actual negative cycle, or null if not provided.
     * 
     * @return the negative cycle or null.
     */
    public GraphPath<?, ?> getCycle()
    {
        return cycle;
    }

    /**
     * Set the negative cycle.
     * 
     * @param cycle the negative cycle.
     */
    public void setCycle(GraphPath<?, ?> cycle)
    {
        this.cycle = cycle;
    }

}
