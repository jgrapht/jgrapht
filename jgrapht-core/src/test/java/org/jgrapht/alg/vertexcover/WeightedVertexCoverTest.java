/*
 * (C) Copyright 2018-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.vertexcover;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * Tests the weighted vertex cover algorithms
 *
 * @author Joris Kinable
 */
public interface WeightedVertexCoverTest
{

    <V, E> VertexCoverAlgorithm<V> createWeightedSolver(
        Graph<V, E> graph, Map<V, Double> vertexWeightMap);

    // ------- Helper methods ------

    static Map<Integer, Double> getRandomVertexWeights(Graph<Integer, DefaultEdge> graph)
    {
        Map<Integer, Double> vertexWeights = new HashMap<>();
        for (Integer v : graph.vertexSet())
            vertexWeights.put(v, 1.0 * VertexCoverTestUtils.rnd.nextInt(25));
        return vertexWeights;
    }
}
