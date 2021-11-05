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
package org.jgrapht.opt.graph.fastutil;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.specifics.*;

import java.io.*;
import java.util.function.*;

/**
 * A specifics strategy implementation using fastutil maps for storage specialized for integer
 * vertices.
 * 
 * @author Dimitrios Michail
 *
 * @param <E> the graph edge type
 */
public class FastutilIntVertexGSS<E>
    implements
    GraphSpecificsStrategy<Integer, E>
{
    private static final long serialVersionUID = 803286406699705306L;

    @Override
    public BiFunction<Graph<Integer, E>, GraphType, Specifics<Integer, E>> getSpecificsFactory()
    {
        return (BiFunction<Graph<Integer, E>, GraphType,
            Specifics<Integer, E>> & Serializable) (graph, type) -> {
                if (type.isDirected()) {
                    return new DirectedSpecifics<>(
                        graph, new Int2ReferenceLinkedOpenHashMap<>(), getEdgeSetFactory());
                } else {
                    return new UndirectedSpecifics<>(
                        graph, new Int2ReferenceLinkedOpenHashMap<>(), getEdgeSetFactory());
                }
            };
    }

    @Override
    public Function<GraphType,
        IntrusiveEdgesSpecifics<Integer, E>> getIntrusiveEdgesSpecificsFactory()
    {
        return (Function<GraphType, IntrusiveEdgesSpecifics<Integer, E>> & Serializable) (type) -> {
            if (type.isWeighted()) {
                return new WeightedIntrusiveEdgesSpecifics<Integer, E>(
                    new Object2ObjectLinkedOpenHashMap<>());
            } else {
                return new UniformIntrusiveEdgesSpecifics<>(new Object2ObjectLinkedOpenHashMap<>());
            }
        };
    }

}
