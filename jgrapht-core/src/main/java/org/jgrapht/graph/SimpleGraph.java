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
/* ----------------
 * SimpleGraph.java
 * ----------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   CHristian Hammer
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

import org.jgrapht.*;
import org.jgrapht.util.VertexPair;


/**
 * A simple graph. A simple graph is an undirected graph for which at most one
 * edge connects any two vertices, and loops are not permitted. If you're unsure
 * about simple graphs, see: <a
 * href="http://mathworld.wolfram.com/SimpleGraph.html">
 * http://mathworld.wolfram.com/SimpleGraph.html</a>.
 */
public class SimpleGraph<V, E>
    extends AbstractBaseGraph<V, E>
    implements UndirectedGraph<V, E>
{
    

    private static final long serialVersionUID = 3545796589454112304L;

    

    /**
     * Creates a new simple graph with the specified edge factory.
     *
     * @param ef the edge factory of the new graph.
     */
    public SimpleGraph(EdgeFactory<V, E> ef)
    {
        super(ef, false, false);
    }

    /**
     * Creates a new simple graph.
     *
     * @param edgeClass class on which to base factory for edges
     */
    public SimpleGraph(Class<? extends E> edgeClass)
    {
        this(new ClassBasedEdgeFactory<V, E>(edgeClass));
    }
    
    /**
	 * Builder for {@link SimpleGraph}.
	 * 
	 * @author Thomas Feichtinger (t.feichtinger[at]gmail[dot]com)
	 */
	public static class Builder<V, E> {

		private final Class<? extends E> edgeclass;
		private final Set<V> vertices;
		private final List<VertexPair<V>> edges;

		public Builder(final Class<? extends E> edgeClass) {
			this.edgeclass = edgeClass;
			this.vertices = new HashSet<V>();
			this.edges = new ArrayList<VertexPair<V>>();
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
		 * @return the builder
		 */
		public Builder<V, E> edge(final V source, final V target) {
			if (!vertices.contains(source)) {
				vertices.add(source);
			}
			if (!vertices.contains(target)) {
				vertices.add(target);
			}
			edges.add(new VertexPair<V>(source, target));
			return this;
		}

		/**
		 * Builds the actual graph from the state of this builder.
		 * 
		 * @return the graph
		 */
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
}

// End SimpleGraph.java
