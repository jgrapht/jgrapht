/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -----------------
 * MinimumWeightedVertexCoverAlgorithm.java
 * -----------------
 * (C) Copyright 2016, by Joris Kinable and Contributors.
 *
 * Original Author:  Joris Kinable
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 28-Jul-2016 : Initial revision (JK);
 *
 */
package org.jgrapht.alg.interfaces;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.util.WeightedVertexSet;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Computes a weighted vertex cover in an undirected graph. A vertex cover of a graph is a set of vertices such that each edge of
 * the graph is incident to at least one vertex in the set. A minimum vertex cover is a vertex cover having the smallest
 * possible number of vertices for a given graph. The size of a minimum vertex cover of a graph G is known as the vertex
 * cover number. A vertex cover of minimum weight is a vertex cover where the sum of weights assigned to the individual
 * vertices in the cover has been minimized. The minimum vertex cover problem is a special case of the minimum weighted
 * vertex cover problem where all vertices have equal weight. Consequently, any algorithm designed for the weighted version
 * of the problem can also solve instances of the unweighted version.
 */
public interface MinimumWeightedVertexCoverAlgorithm<V,E> extends MinimumVertexCoverAlgorithm<V,E> {

    @Override
    default WeightedVertexSet<V> getVertexCover(UndirectedGraph<V,E> graph){
        Map<V,Double> vertexWeightMap=graph.vertexSet().stream().collect(Collectors.toMap(Function.identity() , vertex-> 1.0));
        return getVertexCover(graph, vertexWeightMap);
    }

    /**
     * Computes a vertex cover; the weight of each vertex is provided in the {@param vertexWeightMap}.
     * @param vertexWeightMap map containing non-negative weights for each vertex
     * @return a vertex cover
     */
    
    WeightedVertexSet<V> getVertexCover(UndirectedGraph<V,E> graph, Map<V, Double> vertexWeightMap);
}
