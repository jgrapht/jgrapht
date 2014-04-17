package org.jgrapht.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.VertexPair;

/**
 * Builder for a SimpleGraph.
 * 
 * @author Thomas Feichtinger
 */
public class SimpleGraphBuilder<V, E> {

	private final Class<? extends E> edgeclass;
	private final Set<V> vertices;
	private final List<VertexPair<V>> edges;

	public SimpleGraphBuilder(final Class<? extends E> edgeClass) {
		this.edgeclass = edgeClass;
		this.vertices = new HashSet<V>();
		this.edges = new ArrayList<VertexPair<V>>();
	}

	public SimpleGraphBuilder(final Class<? extends E> edgeClass, V... vertices) {
		this(edgeClass);
		vertices(vertices);
	}

	public SimpleGraphBuilder<V, E> vertices(final V... vertices) {
		for (final V vertex : vertices) {
			this.vertices.add(vertex);
		}
		return this;
	}

	public SimpleGraphBuilder<V, E> edge(final V source, final V target) {
		if (!vertices.contains(source)) {
			vertices.add(source);
		}
		if (!vertices.contains(target)) {
			vertices.add(target);
		}
		edges.add(new VertexPair<V>(source, target));
		return this;
	}

	public SimpleGraph<V, E> build() {
		final SimpleGraph<V, E> g = new SimpleGraph<V, E>(edgeclass);
		for (final V v : vertices) {
			g.addVertex(v);
		}
		for (final VertexPair<V> edge : edges) {
			g.addEdge(edge.getFirst(), edge.getSecond());
		}
		return g;
	}
}
