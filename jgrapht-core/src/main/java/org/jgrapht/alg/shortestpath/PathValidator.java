/*
 * (C) Copyright 2016-2018, by Assaf Mizrachi and Contributors.
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

import org.jgrapht.*;

/**
 * May be used to provide external path validations in addition to the basic validations done by
 * {@link KShortestSimplePaths} - that the path is from source to target and that it does not contain
 * loops.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Assaf Mizrachi
 * @since July, 21, 2016
 *
 */
public interface PathValidator<V, E>
{

    /**
     * Checks if an edge can be added to a previous path element.
     * 
     * @param partialPath the path from source vertex up to the current vertex.
     * @param edge the new edge to be added to the path.
     * 
     * @return <code>true</code> if edge can be added, <code>false</code> otherwise.
     */
    public boolean isValidPath(GraphPath<V, E> partialPath, E edge);
}
