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
/* ------------------------
 * SimpleWeightedGraph.java
 * ------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Aug-2003 : Initial revision (BN);
 * 06-Aug-2005 : Made generic (CH);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.WeightedGraph;
import org.jgrapht.util.VertexPair;

/**
 * A simple weighted graph. A simple weighted graph is a simple graph for which
 * edges are assigned weights.
 */
public class SimpleWeightedGraph<V, E> extends SimpleGraph<V, E> implements
		WeightedGraph<V, E> {

	private static final long serialVersionUID = 3906088949100655922L;

	/**
	 * Creates a new simple weighted graph with the specified edge factory.
	 *
	 * @param ef
	 *            the edge factory of the new graph.
	 */
	public SimpleWeightedGraph(EdgeFactory<V, E> ef) {
		super(ef);
	}

	/**
	 * Creates a new simple weighted graph.
	 *
	 * @param edgeClass
	 *            class on which to base factory for edges
	 */
	public SimpleWeightedGraph(Class<? extends E> edgeClass) {
		this(new ClassBasedEdgeFactory<V, E>(edgeClass));
	}

	/**
	 * Builder for {@SimpleWeightedGraph}.
	 * 
	 * @author Thomas Feichtinger (t.feichtinger[at]gmail[dot]com)
	 */
	public static class Builder<V, E> {

		private final Class<? extends E> edgeclass;
		private final Set<V> vertices;
		private final List<VertexPair<V>> edges;
		private final List<Double> weights;

		public Builder(final Class<? extends E> edgeClass) {
			this.edgeclass = edgeClass;
			this.vertices = new HashSet<V>();
			this.edges = new ArrayList<VertexPair<V>>();
			this.weights = new ArrayList<Double>();
		}

		public Builder(final Class<? extends E> edgeClass, V... vertices) {
			this(edgeClass);
			vertices(vertices);
		}

		/**
		 * Adds the specified vertices to the graph. Vertices already contained
		 * in the graph will be ignored.
		 * 
		 * @param vertices
		 *            the vertices to add
		 * @return the builder
		 */
		public Builder<V, E> vertices(final V... vertices) {
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
		public Builder<V, E> edge(final V source, final V target,
				final double weight) {
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
}

// End SimpleWeightedGraph.java
