/*
 * (C) Copyright 2026-2026, by seilat and Contributors.
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
package org.jgrapht.alg.interfaces;

import org.jgrapht.*;

import java.util.*;

/**
 * Default immutable {@link HamiltonianPathSearchResult} implementation returned by the
 * interface's static factory methods. Package-private: it is an implementation detail, not part
 * of the public API.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author seilat
 */
final class DefaultHamiltonianPathSearchResult<V, E>
    implements HamiltonianPathSearchResult<V, E>
{
    private final Status status;
    private final GraphPath<V, E> path;
    private final long statesExpanded;

    DefaultHamiltonianPathSearchResult(Status status, GraphPath<V, E> path, long statesExpanded)
    {
        this.status = status;
        this.path = path;
        this.statesExpanded = statesExpanded;
    }

    @Override
    public Status getStatus()
    {
        return status;
    }

    @Override
    public Optional<GraphPath<V, E>> getPath()
    {
        return Optional.ofNullable(path);
    }

    @Override
    public long getStatesExpanded()
    {
        return statesExpanded;
    }

    @Override
    public String toString()
    {
        return "HamiltonianPathSearchResult{status=" + status
            + ", statesExpanded=" + statesExpanded + "}";
    }
}
