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
package org.jgrapht.graph;

import org.jgrapht.*;
import org.jgrapht.graph.specifics.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * A default lookup specifics strategy implementation.
 * 
 * <p>
 * Graphs constructed using this strategy require the least amount of memory, at the expense of slow
 * edge retrievals. Methods which depend on edge retrievals, e.g. getEdge(V u, V v), containsEdge(V
 * u, V v), addEdge(V u, V v), etc may be relatively slow when the average degree of a vertex is
 * high (dense graphs). For a fast implementation, use {@link FastLookupGraphSpecificsStrategy}.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class DefaultGraphSpecificsStrategy<V, E>
    implements
    GraphSpecificsStrategy<V, E>
{
    private static final long serialVersionUID = 7615319421753562075L;

    @Override
    public Function<GraphType, IntrusiveEdgesSpecifics<V, E>> getIntrusiveEdgesSpecificsFactory()
    {
        return (Function<GraphType, IntrusiveEdgesSpecifics<V, E>> & Serializable) (type) -> {
            if (type.isWeighted()) {
                return new WeightedIntrusiveEdgesSpecifics<V, E>(new LinkedHashMap<>());
            } else {
                return new UniformIntrusiveEdgesSpecifics<>(new LinkedHashMap<>());
            }
        };
    }

    @Override
    public BiFunction<Graph<V, E>, GraphType, Specifics<V, E>> getSpecificsFactory()
    {
        return (BiFunction<Graph<V, E>, GraphType,
            Specifics<V, E>> & Serializable) (graph, type) -> {
                if (type.isDirected()) {
                    return new DirectedSpecifics<V, E>(
                        graph, new LinkedHashMap<>(), getEdgeSetFactory());
                } else {
                    return new UndirectedSpecifics<>(
                        graph, new LinkedHashMap<>(), getEdgeSetFactory());
                }
            };
    }

}
