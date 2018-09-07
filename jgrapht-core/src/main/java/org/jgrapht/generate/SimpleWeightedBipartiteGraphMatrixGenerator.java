/*
 * (C) Copyright 2016-2018, by Barak Naveh and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.*;

import java.util.*;

/**
 * A simple weighted bipartite graph matrix generator.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class SimpleWeightedBipartiteGraphMatrixGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    protected List<V> first;
    protected List<V> second;
    protected double[][] weights;

    /**
     * Set the first partition of the generator.
     * 
     * @param first the first partition
     * @return the generator
     */
    public SimpleWeightedBipartiteGraphMatrixGenerator<V, E> first(List<? extends V> first)
    {
        this.first = new ArrayList<>(first);
        return this;
    }

    /**
     * Set the second partition of the generator.
     * 
     * @param second the second partition
     * @return the generator
     */
    public SimpleWeightedBipartiteGraphMatrixGenerator<V, E> second(List<? extends V> second)
    {
        this.second = new ArrayList<>(second);
        return this;
    }

    /**
     * Set the weights of the generator.
     * 
     * @param weights the weights
     * @return the generator
     */
    public SimpleWeightedBipartiteGraphMatrixGenerator<V, E> weights(double[][] weights)
    {
        this.weights = weights;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        if (weights == null) {
            throw new IllegalArgumentException(
                "Graph may not be constructed without weight-matrix specified");
        }

        if ((first == null) || (second == null)) {
            throw new IllegalArgumentException(
                "Graph may not be constructed without either of vertex-set partitions specified");
        }

        assert second.size() == weights.length;

        for (V vertex : first) {
            target.addVertex(vertex);
        }

        for (V vertex : second) {
            target.addVertex(vertex);
        }

        for (int i = 0; i < first.size(); ++i) {
            assert first.size() == weights[i].length;

            for (int j = 0; j < second.size(); ++j) {
                target.setEdgeWeight(target.addEdge(first.get(i), second.get(j)), weights[i][j]);
            }
        }
    }
}

// End SimpleWeightedBipartiteGraphMatrixGenerator.java
