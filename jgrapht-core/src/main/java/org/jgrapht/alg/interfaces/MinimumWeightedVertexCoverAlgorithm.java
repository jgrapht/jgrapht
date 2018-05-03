/*
 * (C) Copyright 2016-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.interfaces;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.jgrapht.*;

/**
 * Computes a weighted vertex cover in an undirected graph. A vertex cover of a graph is a set of
 * vertices such that each edge of the graph is incident to at least one vertex in the set. A
 * minimum vertex cover is a vertex cover having the smallest possible number of vertices for a
 * given graph. The size of a minimum vertex cover of a graph $G$ is known as the vertex cover number.
 * A vertex cover of minimum weight is a vertex cover where the sum of weights assigned to the
 * individual vertices in the cover has been minimized. The minimum vertex cover problem is a
 * special case of the minimum weighted vertex cover problem where all vertices have equal weight.
 * Consequently, any algorithm designed for the weighted version of the problem can also solve
 * instances of the unweighted version.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Joris Kinable
 */
public interface MinimumWeightedVertexCoverAlgorithm<V, E>
    extends MinimumVertexCoverAlgorithm<V, E>
{

    /**
     * {@inheritDoc}
     */
    @Override
    default VertexCover<V> getVertexCover(Graph<V, E> graph)
    {
        Map<V, Double> vertexWeightMap = graph
            .vertexSet().stream().collect(Collectors.toMap(Function.identity(), vertex -> 1.0));
        return getVertexCover(graph, vertexWeightMap);
    }

    /**
     * Computes a vertex cover; the weight of each vertex is provided in the in the
     * {@code vertexWeightMap}.
     * 
     * @param graph the input graph
     * @param vertexWeightMap map containing non-negative weights for each vertex
     * @return a vertex cover
     */
    VertexCover<V> getVertexCover(Graph<V, E> graph, Map<V, Double> vertexWeightMap);
}
