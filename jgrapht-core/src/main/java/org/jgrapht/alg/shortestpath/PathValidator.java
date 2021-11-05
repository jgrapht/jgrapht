/*
 * (C) Copyright 2016-2021, by Assaf Mizrachi and Contributors.
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
 * Path validator for shortest path algorithms. Shortest path algorithms typically maintain a set of
 * partial paths which are iteratively extended until some stopping criterion is met, e.g. the
 * target vertex is reached. A path validator can be used to enforce additional criteria as to the
 * validity of a given path. A partial path can only be extended by another edge if the path
 * validator deems this extension feasible. As per example, a path validator can be used to enforce
 * that a path connecting a source to a terminal vertex visits at least $n$ vertices.
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
