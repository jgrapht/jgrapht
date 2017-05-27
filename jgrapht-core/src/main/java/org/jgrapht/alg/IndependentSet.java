package org.jgrapht.alg;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.interfaces.MinimumWeightedVertexCoverAlgorithm;
import org.jgrapht.alg.interfaces.MinimumVertexCoverAlgorithm.VertexCover;
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

public abstract class IndependentSet<V, E> {
	
	/**
     * Computes a maximum independent set for the {@param graph}.
     * @param graph				an undirected graph
     * @return maximum independent set
     */
	public static <V, E> Set<V> getIndependentSet(UndirectedGraph<V, E> graph) 
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
	public static <V, E> Set<V> getIndependentSet(UndirectedGraph<V, E> graph, Map<V, Double> weights) 
	{
		MinimumWeightedVertexCoverAlgorithm<V, E> mvc = new RecursiveExactVCImpl<V, E>();
		VertexCover<V> vertexCover = mvc.getVertexCover(graph, weights);
		
		Set<V> result = new HashSet<V>(graph.vertexSet());
		result.removeAll(vertexCover.getVertices());
		
		return result;
	}
	
	/**
     * Computes the weight of a maximum weight independent set for {@param graph}.
     * @param graph				an undirected graph
     * @param weights			map with non-negative weights for vertices
     * @return weight of maximum weight independent set
     */
	public static <V, E> Double getWeight(UndirectedGraph<V, E> graph, Map<V, Double> weights) 
	{
		Set<V> set = getIndependentSet(graph, weights);
		double weight = 0;
		
		for (V v : set) {
			weight += weights.get(v);
		}
		
		return weight;
	}
}
