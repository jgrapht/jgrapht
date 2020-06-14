/*
 * (C) Copyright 2017-2017 Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.color;

import org.jgrapht.*;

import java.util.*;

/**
 * The greedy coloring algorithm with a random vertex ordering.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class RandomGreedyColoring<V, E>
    extends
    GreedyColoring<V, E>
{
    /*
     * Random number generator
     */
    private Random rng;

    /**
     * Construct a new coloring algorithm.
     * 
     * @param graph the input graph
     */
    public RandomGreedyColoring(Graph<V, E> graph)
    {
        this(graph, new Random());
    }

    /**
     * Construct a new coloring algorithm
     * 
     * @param graph the input graph
     * @param rng the random number generator
     */
    public RandomGreedyColoring(Graph<V, E> graph, Random rng)
    {
        super(graph);
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<V> getVertexOrdering()
    {
        List<V> order = new ArrayList<V>(graph.vertexSet());
        Collections.shuffle(order, rng);
        return order;
    }

}
