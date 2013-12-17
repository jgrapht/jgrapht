/* This program and the accompanying materials are dual-licensed under
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

import java.util.*;

import org.jgrapht.*;


public class SimpleWeightedBipartiteGraphMatrixGenerator<V, E>
    extends WeightedGraphGeneratorAdapter<V, E, V>
{
    

    List<V> first;

    List<V> second;

    

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public SimpleWeightedBipartiteGraphMatrixGenerator<V, E> first(
        List<? extends V> first)
    {
        this.first = new ArrayList<V>(first);
        return this;
    }

    public SimpleWeightedBipartiteGraphMatrixGenerator<V, E> second(
        List<? extends V> second)
    {
        this.second = new ArrayList<V>(second);
        return this;
    }

    @Override public void generateGraph(
        WeightedGraph<V, E> target,
        VertexFactory<V> vertexFactory,
        Map<String, V> resultMap)
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
                target.setEdgeWeight(
                    target.addEdge(first.get(i), second.get(j)),
                    weights[i][j]);
            }
        }
    }
}

// End SimpleWeightedBipartiteGraphMatrixGenerator.java
