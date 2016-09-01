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

package org.jgrapht.alg.clique;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.independentset.VertexCoverComplementImpl;
import org.jgrapht.alg.interfaces.MaximumWeightedCliqueAlgorithm;
import org.jgrapht.alg.interfaces.MaximumWeightedIndependentSetAlgorithm;
import org.jgrapht.graph.SimpleGraph;


/**
 * Finds a maximum (weight) clique in a undirected graph. A clique
 * for graph G = (V, E) with weight function w: V -> |R_+ is a subset of vertices 
 * V', such that all vertices in V' are pairwise connected via an edge in G. 
 * The weight of a clique V' is the sum of all w(v), v ∈ V'.
 * 
 * The implementation relies on the fact that if V is a maximum weight independent
 * set for graph G = (V,E) with weight function w: V -> |R_+, then V is a 
 * a maximum weight clique in the complement of G. The complement of a graph G
 * is the graph G' = (V',E'), such that for all u,v ∈ V
 * 		
 * 		{u,v} ∈ E <=> not {u,v} ∈ E'
 *
 * @author Nils Olberg
 */

public class ISGraphComplementImpl<V, E> implements MaximumWeightedCliqueAlgorithm<V, E> {
	
	/**
     * Computes a maximum clique for the {@param graph}.
     * @param graph				an undirected graph
     * @return maximum clique
     */
    @Override
	public Clique<V> getClique(UndirectedGraph<V, E> graph, Class<? extends E> edgeClass) 
	{
		Map<V,Double> weights = graph.vertexSet().stream().collect(Collectors.toMap(Function.identity() , vertex-> 1.0));
        
		return getClique(graph, weights, edgeClass);
	}
	
	/**
     * Computes a maximum weight clique for the {@param graph}.
     * @param graph				an undirected graph
     * @param weights			map with non-negative weights for vertices
     * @return maximum weight clique
     */
    @Override
	public Clique<V> getClique(UndirectedGraph<V, E> graph, Map<V, Double> weights, Class<? extends E> edgeClass) 
	{
	    // create complement graph
		UndirectedGraph<V, E> complement = new SimpleGraph<V, E>(edgeClass);
		
		for (V v : graph.vertexSet()) {
			complement.addVertex(v);
		}
		
	    for (V u : graph.vertexSet()) {
	    	for (V v : graph.vertexSet()) {
	    		if (!Graphs.neighborListOf(graph, u).contains(v) && !v.equals(u)) {
	    			complement.addEdge(u, v);
	    		}
	    	}
	    }
		
	    MaximumWeightedIndependentSetAlgorithm<V, E> mwis = new VertexCoverComplementImpl<V, E>();
	    Set<V> clique = mwis.getIndependentSet(complement, weights).getVertices();

		double weight = 0;
		for (V v : clique) {
			weight += weights.get(v);
		}
		
		return new CliqueImpl<V>(clique, weight);
	}
}
