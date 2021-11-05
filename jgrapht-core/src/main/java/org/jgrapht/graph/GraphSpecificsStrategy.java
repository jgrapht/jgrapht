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
import java.util.function.*;

/**
 * A graph specifics construction factory.
 * 
 * <p>
 * Such a strategy can be used to adjust the internals of the default graph implementations.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @see FastLookupGraphSpecificsStrategy
 * @see DefaultGraphSpecificsStrategy
 */
public interface GraphSpecificsStrategy<V, E>
    extends
    Serializable
{
    /**
     * Get a function which creates the intrusive edges specifics. The factory will accept the graph
     * type as a parameter.
     * 
     * <p>
     * Note that it is very important to use a map implementation which respects iteration order.
     * 
     * @return a function which creates intrusive edges specifics.
     */
    Function<GraphType, IntrusiveEdgesSpecifics<V, E>> getIntrusiveEdgesSpecificsFactory();

    /**
     * Get a function which creates the specifics. The factory will accept the graph type as a
     * parameter.
     * 
     * @return a function which creates intrusive edges specifics.
     */
    BiFunction<Graph<V, E>, GraphType, Specifics<V, E>> getSpecificsFactory();

    /**
     * Get an edge set factory.
     * 
     * @return an edge set factory
     */
    default EdgeSetFactory<V, E> getEdgeSetFactory()
    {
        return new ArrayUnenforcedSetEdgeSetFactory<>();
    }

}
