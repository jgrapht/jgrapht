/*
 * (C) Copyright 2016-2018, by Assaf Mizrachi and Contributors.
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
 * May be used to provide external path validations in addition to the basic validations done by
 * {@link KShortestSimplePaths} - that the path is from source to target and that it does not
 * contain loops.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Assaf Mizrachi
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
