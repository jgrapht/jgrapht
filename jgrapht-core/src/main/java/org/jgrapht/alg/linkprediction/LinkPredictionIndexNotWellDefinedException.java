/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.linkprediction;

import org.jgrapht.alg.util.Pair;

/**
 * An exception used to notify that a link prediction index is not well defined.
 * 
 * @author Dimitrios Michail
 */
public class LinkPredictionIndexNotWellDefinedException
    extends
    RuntimeException
{
    private static final long serialVersionUID = -8832535053621910719L;

    private Pair<?, ?> vertexPair;

    /**
     * Constructs a new exception with {@code null} as its detail message. The cause is not
     * initialized, and may subsequently be initialized by a call to {@link #initCause}.
     */
    public LinkPredictionIndexNotWellDefinedException()
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
    public LinkPredictionIndexNotWellDefinedException(String message)
    {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized,
     * and may subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the
     *        {@link #getMessage()} method.
     * @param vertexPair the vertex pair which caused the error. The pair is saved for later
     *        retrieval by the {@link #getVertexPair()} method.
     */
    public LinkPredictionIndexNotWellDefinedException(String message, Pair<?, ?> vertexPair)
    {
        super(message);
        this.vertexPair = vertexPair;
    }

    /**
     * Get the vertex pair which caused the error. May be null.
     * 
     * @return the vertex pair which caused the error
     */
    public Pair<?, ?> getVertexPair()
    {
        return vertexPair;
    }

    /**
     * Set the vertex pair which caused the error. May be null.
     * 
     * @param vertexPair the vertex pair to set
     */
    public void setVertexPair(Pair<?, ?> vertexPair)
    {
        this.vertexPair = vertexPair;
    }

}
