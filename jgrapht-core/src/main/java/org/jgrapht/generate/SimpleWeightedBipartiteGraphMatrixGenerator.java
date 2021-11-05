/*
 * (C) Copyright 2016-2021, by Barak Naveh and Contributors.
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
