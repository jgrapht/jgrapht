package org.jgrapht.alg;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.interfaces.MinimumWeightedVertexCoverAlgorithm;
import org.jgrapht.alg.interfaces.MinimumVertexCoverAlgorithm.VertexCover;
import org.jgrapht.alg.vertexcover.RecursiveExactVCImpl;

/**
 * Finds a maximum weight independent set in a undirected graph. The implementation 
 * relies on the exact algorithm for the minimum weight vertex cover problem.
 *
 * @author Nils Olberg
 */

public class MaximumWeightIndependentSet<V, E> {

	UndirectedGraph<V, E> g;
	Map<V, Double> weights;
	
	public MaximumWeightIndependentSet(UndirectedGraph<V, E> graph, Map<V, Double> vertexWeightMap) 
	{
		g = graph;
		weights = vertexWeightMap;
	}

	/**
     * Computes a maximum weight independent set in the graph.
     *
     * @return optimal solution
     */
	public Set<V> getIndependentSet() 
	{
		MinimumWeightedVertexCoverAlgorithm<V, E> mvc = new RecursiveExactVCImpl<V, E>();
		VertexCover<V> vertexCover = mvc.getVertexCover(g, weights);
		
		Set<V> result = new HashSet<V>();
		for (V v : g.vertexSet()) {
			if (!vertexCover.getVertices().contains(v)) {
				result.add(v);
			}
		}
		
		return result;
	}
	
	public Double getWeight() 
	{
		Set<V> set = getIndependentSet();
		double weight = 0;
		
		for (V v : set) {
			weight += weights.get(v);
		}
		
		return weight;
	}
}
