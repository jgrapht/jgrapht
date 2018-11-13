/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.opt.graph.fastutil;

import it.unimi.dsi.fastutil.objects.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.specifics.*;

import java.io.*;
import java.util.function.*;

/**
 * A specifics strategy implementation using fastutil maps for storage.
 * 
 * <p>
 * Graphs constructed using this strategy require the least amount of memory, at the expense of slow
 * edge retrievals. Methods which depend on edge retrievals, e.g. getEdge(V u, V v), containsEdge(V
 * u, V v), addEdge(V u, V v), etc may be relatively slow when the average degree of a vertex is
 * high (dense graphs). For a fast implementation, use
 * {@link FastutilFastLookupGSS}.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class FastutilGSS<V, E>
    implements
    GraphSpecificsStrategy<V, E>
{
    private static final long serialVersionUID = -4319431062943632549L;

    @Override
    public BiFunction<Graph<V, E>, GraphType, Specifics<V, E>> getSpecificsFactory()
    {
        return (BiFunction<Graph<V, E>, GraphType,
            Specifics<V, E>> & Serializable) (graph, type) -> {
                if (type.isDirected()) {
                    return new DirectedSpecifics<>(
                        graph, new Object2ObjectLinkedOpenHashMap<>(), getEdgeSetFactory());
                } else {
                    return new UndirectedSpecifics<>(
                        graph, new Object2ObjectLinkedOpenHashMap<>(), getEdgeSetFactory());
                }
            };
    }

    @Override
    public Function<GraphType, IntrusiveEdgesSpecifics<V, E>> getIntrusiveEdgesSpecificsFactory()
    {
        return (Function<GraphType, IntrusiveEdgesSpecifics<V, E>> & Serializable) (type) -> {
            if (type.isWeighted()) {
                return new WeightedIntrusiveEdgesSpecifics<V, E>(
                    new Object2ObjectLinkedOpenHashMap<>());
            } else {
                return new UniformIntrusiveEdgesSpecifics<>(new Object2ObjectLinkedOpenHashMap<>());
            }
        };
    }

}
