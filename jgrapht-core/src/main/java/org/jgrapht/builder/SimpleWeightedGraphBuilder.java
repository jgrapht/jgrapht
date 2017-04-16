package org.jgrapht.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.util.VertexPair;

/**
 * Builder for a SimpleWeightedGraph.
 * 
 * @author Thomas Feichtinger
 */
public class SimpleWeightedGraphBuilder<V, E> {

	private final Class<? extends E> edgeclass;
	private final Set<V> vertices;
	private final List<VertexPair<V>> edges;
	private final List<Double> weights;

	public SimpleWeightedGraphBuilder(final Class<? extends E> edgeClass) {
		this.edgeclass = edgeClass;
		this.vertices = new HashSet<V>();
		this.edges = new ArrayList<VertexPair<V>>();
		this.weights = new ArrayList<Double>();
	}

	public SimpleWeightedGraphBuilder(final Class<? extends E> edgeClass,
			V... vertices) {
		this(edgeClass);
		vertices(vertices);
	}

	/**
	 * Adds the specified vertices to the graph. Vertices already contained in
	 * the graph will be ignored.
	 * 
	 * @param vertices
	 *            the vertices to add
	 * @return the builder
	 */
	public SimpleWeightedGraphBuilder<V, E> vertices(final V... vertices) {
		for (final V vertex : vertices) {
			this.vertices.add(vertex);
		}
		return this;
	}

	/**
	 * Adds an edge between two vertices to the graph.
	 * 
	 * If the vertex is not yet contained in the graph it will be added.
	 * 
	 * @param source
	 *            the source vertex
	 * @param target
	 *            the target vertex
	 * @param weight
	 *            the weight of the edge
	 * @return the builder
	 */
	public SimpleWeightedGraphBuilder<V, E> edge(final V source,
			final V target, final double weight) {
		vertices.add(source);
		vertices.add(target);
		edges.add(new VertexPair<V>(source, target));
		weights.add(weight);
		return this;
	}

	/**
	 * Builds the actual graph from the state of this builder.
	 * 
	 * @return the graph
	 */
	public SimpleWeightedGraph<V, E> build() {
		final SimpleWeightedGraph<V, E> g = new SimpleWeightedGraph<V, E>(
				edgeclass);

		for (final V v : vertices) {
			g.addVertex(v);
		}

		for (int i = 0; i < edges.size(); ++i) {
			E edge = g.addEdge(edges.get(i).getFirst(), edges.get(i)
					.getSecond());
			if (edge != null) {
				g.setEdgeWeight(edge, weights.get(i));
			}
		}
		return g;
	}
}
