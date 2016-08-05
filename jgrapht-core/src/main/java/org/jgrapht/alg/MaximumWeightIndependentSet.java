package org.jgrapht.alg;

import java.util.Map;

import org.jgrapht.UndirectedGraph;

public class MaximumWeightIndependentSet<V, E> {

	UndirectedGraph<V, E> g;
	Map<V, Double> weights;
	
	public MaximumWeightIndependentSet(UndirectedGraph<V, E> graph, Map<V, Double> vertexWeightMap) 
	{
		g = graph;
		weights = vertexWeightMap;
	}
	
}
