package org.jgrapht.alg;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
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

public abstract class Clique {
	
	/**
     * Computes a maximum clique for the {@param graph}.
     * @param graph				an undirected graph
     * @return maximum clique
     */
	public static <V, E> Set<V> getMaximumClique(UndirectedGraph<V, E> graph) 
	{
		Map<V,Double> weights = graph.vertexSet().stream().collect(Collectors.toMap(Function.identity() , vertex-> 1.0));
        
		return getMaximumClique(graph, weights);
	}
	
	/**
     * Computes a maximum weight clique for the {@param graph}.
     * @param graph				an undirected graph
     * @param weights			map with non-negative weights for vertices
     * @return maximum weight clique
     */
	public static <V, E> Set<V> getMaximumClique(UndirectedGraph<V, E> graph, Map<V, Double> weights) 
	{
	    // create complement graph
		UndirectedGraph<V, DefaultEdge> complement = new SimpleGraph<V, DefaultEdge>(DefaultEdge.class);
		
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
		
	    Set<V> result = IndependentSet.getIndependentSet(complement, weights);
		return result;
	}
	
	/**
     * Computes the weight of a maximum weight clique for {@param graph}.
     * @param graph				an undirected graph
     * @param weights			map with non-negative weights for vertices
     * @return weight of maximum weight clique
     */
	public static <V, E> Double getWeight(UndirectedGraph<V, E> graph, Map<V, Double> weights) 
	{
		Set<V> set = getMaximumClique(graph, weights);
		double weight = 0;
		
		for (V v : set) {
			weight += weights.get(v);
		}
		
		return weight;
	}
}
