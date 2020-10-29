/*
 * (C) Copyright 2020-2020, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.graph;

import org.jgrapht.Graph;
import org.jgrapht.GraphIterables;

/**
 * Default implementation for the graph iterables which simply delegates to the
 * set implementation.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class DefaultGraphIterables<V, E> implements GraphIterables<V, E> {

	protected Graph<V, E> graph;

	public DefaultGraphIterables(Graph<V, E> graph) {
		this.graph = graph;
	}
	
	@Override
	public Graph<V,E> getGraph() { 
	    return graph;
	}

	@Override
	public Iterable<E> edges() {
		return graph.edgeSet();
	}

	@Override
	public long edgeCount() {
		return graph.edgeSet().size();
	}

	@Override
	public Iterable<V> vertices() {
		return graph.vertexSet();
	}

	@Override
	public long vertexCount() {
		return graph.vertexSet().size();
	}

	@Override
	public Iterable<E> edgesOf(V vertex) {
		return graph.edgesOf(vertex);
	}

	@Override
	public long degreeOf(V vertex) {
		return graph.degreeOf(vertex);
	}

	@Override
	public Iterable<E> incomingEdgesOf(V vertex) {
		return graph.incomingEdgesOf(vertex);
	}

	@Override
	public long inDegreeOf(V vertex) {
		return graph.inDegreeOf(vertex);
	}

	@Override
	public Iterable<E> outgoingEdgesOf(V vertex) {
		return graph.outgoingEdgesOf(vertex);
	}

	@Override
	public long outDegreeOf(V vertex) {
		return graph.outDegreeOf(vertex);
	}

	@Override
	public Iterable<E> allEdges(V sourceVertex, V targetVertex) {
		return graph.getAllEdges(sourceVertex, targetVertex);
	}

}
