/*
 * (C) Copyright 2018-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.vertexcover;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexCoverAlgorithm;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests the weighted vertex cover algorithms
 *
 * @author Joris Kinable
 */
public interface WeightedVertexCoverTest {

     <V, E> VertexCoverAlgorithm<V> createWeightedSolver(Graph<V, E> graph, Map<V, Double> vertexWeightMap);

    // ------- Helper methods ------

    static Map<Integer, Double> getRandomVertexWeights(Graph<Integer, DefaultEdge> graph)
    {
        Map<Integer, Double> vertexWeights = new HashMap<>();
        for (Integer v : graph.vertexSet())
            vertexWeights.put(v, 1.0 * VertexCoverTestUtils.rnd.nextInt(25));
        return vertexWeights;
    }
}
