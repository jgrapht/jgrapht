package org.jgrapht.alg.steiner;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.SteinerTreeAlgorithm;

import org.jgrapht.alg.interfaces.*;

/**
 * TODO
 * 
 * @param <V>
 * @param <E>
 */
public class KouMarkowskyBermanAlgorithm<V,E> implements SteinerTreeAlgorithm<V, E> {

	private final Graph<V, E> graph;
	
	/**
	 * TODO 
	 * 
	 * @param graph
	 */
	public KouMarkowskyBermanAlgorithm(Graph<V,E> graph) { 
		this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
	}
	
	@Override
	public SteinerTree<E> getSpanningTree(Set<V> steinerPoints) {

		// TODO 
		
		// return result as 
		Set<E> result = new HashSet<>() ; // TODO
		double weight = 0; // TODO
		return new SteinerTreeAlgorithm.SpeinerTreeImpl<>(result, weight); 
	}

}
