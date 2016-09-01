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
 * ISGraphComplementImpl.java
 * -----------------
 * (C) Copyright 2016, by Nils Olberg and Contributors.
 *
 * Original Author: Nils Olberg
 * Contributor(s):
 *
 */

package org.jgrapht.alg.independentset;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.interfaces.MinimumVertexCoverAlgorithm.VertexCover;
import org.jgrapht.alg.interfaces.MaximumWeightedIndependentSetAlgorithm;
import org.jgrapht.alg.interfaces.MinimumWeightedVertexCoverAlgorithm;
import org.jgrapht.alg.vertexcover.RecursiveExactVCImpl;


/**
 * Finds a maximum (weight) independent set in a undirected graph. 
 * 
 * The implementation relies on the fact that if V' is a minimum 
 * weight vertex cover for graph G = (V,E) with weight function
 * w: V -> |R_+, then V-V' is a maximum weight independent set in G. 
 *
 * @author Nils Olberg
 */

public class VertexCoverComplementImpl<V, E> implements MaximumWeightedIndependentSetAlgorithm<V, E> {
	
	/**
     * Computes a maximum independent set for the {@param graph}.
     * @param graph				an undirected graph
     * @return maximum independent set
     */
	@Override
	public IndependentSet<V> getIndependentSet(UndirectedGraph<V, E> graph) 
	{
		Map<V,Double> weights = graph.vertexSet().stream().collect(Collectors.toMap(Function.identity() , vertex-> 1.0));
        
		return getIndependentSet(graph, weights);
	}
	
	/**
     * Computes a maximum weight independent set for the {@param graph}.
     * @param graph				an undirected graph
     * @param weights			map with non-negative weights for vertices
     * @return maximum weight independent set
     */
	@Override
	public IndependentSet<V> getIndependentSet(UndirectedGraph<V, E> graph, Map<V, Double> weights) 
	{
		MinimumWeightedVertexCoverAlgorithm<V, E> mvc = new RecursiveExactVCImpl<V, E>();
		VertexCover<V> vertexCover = mvc.getVertexCover(graph, weights);
		
		Set<V> set = new HashSet<V>(graph.vertexSet());
		set.removeAll(vertexCover.getVertices());
		
		double weight = 0;
		for (V v : set) {
			weight += weights.get(v);
		}
		
		return new IndependentSetImpl<V>(set, weight);
	}
}
