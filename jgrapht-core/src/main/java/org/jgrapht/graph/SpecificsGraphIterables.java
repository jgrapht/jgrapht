/*
 * (C) Copyright 2020-2020, by Dimitrios Michail and Contributors.
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
package org.jgrapht.graph;

import java.util.Objects;

import org.jgrapht.Graph;
import org.jgrapht.GraphIterables;
import org.jgrapht.graph.specifics.Specifics;

/**
 * An implementation of the {@link GraphIterables} interface using the graph specifics.
 * 
 * <p>If the specifics support iterables, then this implementation can employ them. Otherwise
 * we inherit the default implementation. 
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class SpecificsGraphIterables<V, E>
    extends
    DefaultGraphIterables<V, E>
{
    protected Specifics<V, E> specifics;
    protected IntrusiveEdgesSpecifics<V, E> intrusiveEdgesSpecifics;

    /**
     * Create a new graph iterables.
     * 
     * @param graph the graph
     * @param specifics the specifics
     * @param intrusiveEdgesSpecifics the intrusive edge specifics
     */
    public SpecificsGraphIterables(
        Graph<V, E> graph, Specifics<V, E> specifics,
        IntrusiveEdgesSpecifics<V, E> intrusiveEdgesSpecifics)
    {
        super(graph);
        this.specifics = Objects.requireNonNull(specifics);
        this.intrusiveEdgesSpecifics = Objects.requireNonNull(intrusiveEdgesSpecifics);
    }

}
