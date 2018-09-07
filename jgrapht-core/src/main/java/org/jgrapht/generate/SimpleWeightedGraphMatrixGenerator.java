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
 * A simple weighted graph matrix generator.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class SimpleWeightedGraphMatrixGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    protected List<V> vertices;
    protected double[][] weights;

    /**
     * Set the generator vertices.
     * 
     * @param vertices the graph vertices
     * @return the generator
     */
    public SimpleWeightedGraphMatrixGenerator<V, E> vertices(List<V> vertices)
    {
        this.vertices = vertices;
        return this;
    }

    /**
     * Set the weights of the generator.
     * 
     * @param weights the weights
     * @return the generator
     */
    public SimpleWeightedGraphMatrixGenerator<V, E> weights(double[][] weights)
    {
        this.weights = weights;
        return this;
    }

    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        if (weights == null) {
            throw new IllegalArgumentException(
                "Graph may not be constructed without weight-matrix specified");
        }

        if (vertices == null) {
            throw new IllegalArgumentException(
                "Graph may not be constructed without vertex-set specified");
        }

        assert vertices.size() == weights.length;

        for (V vertex : vertices) {
            target.addVertex(vertex);
        }

        for (int i = 0; i < vertices.size(); ++i) {
            assert vertices.size() == weights[i].length;

            for (int j = 0; j < vertices.size(); ++j) {
                if (i != j) {
                    target.setEdgeWeight(
                        target.addEdge(vertices.get(i), vertices.get(j)), weights[i][j]);
                }
            }
        }
    }
}

// End SimpleWeightedGraphMatrixGenerator.java
