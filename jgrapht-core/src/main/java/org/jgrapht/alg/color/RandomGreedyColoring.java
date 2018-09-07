/*
 * (C) Copyright 2017-2017 Dimitrios Michail and Contributors.
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
