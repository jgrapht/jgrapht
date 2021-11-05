/*
 * (C) Copyright 2006-2021, by John V Sichi and Contributors.
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

import org.jgrapht.*;

import java.util.*;

/**
 * Provides an edge-reversed view $g'$ of a directed graph $g$. The vertex sets for the two graphs
 * are the same, but g' contains an edge $(v2, v1)$ iff g$$ contains an edge $(v1, v2)$. $g'$ is
 * backed by $g$, so changes to $g$ are reflected in $g'$, and vice versa.
 *
 * <p>
 * This class allows you to use a directed graph algorithm in reverse. For example, suppose you have
 * a directed graph representing a tree, with edges from parent to child, and you want to find all
 * of the parents of a node. To do this, simply create an edge-reversed graph and pass that as input
 * to {@link org.jgrapht.traverse.DepthFirstIterator}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author John V. Sichi
 * @see AsUndirectedGraph
 */
public class EdgeReversedGraph<V, E>
    extends
    GraphDelegator<V, E>
    implements
    Graph<V, E>
{
    private static final long serialVersionUID = -3806030402468293063L;

    /**
     * Creates a new EdgeReversedGraph.
     *
     * @param g the base (backing) graph on which the edge-reversed view will be based.
     */
    public EdgeReversedGraph(Graph<V, E> g)
    {
        super(g);
    }

    /**
     * @see Graph#getEdge(Object, Object)
     */
    @Override
    public E getEdge(V sourceVertex, V targetVertex)
    {
        return super.getEdge(targetVertex, sourceVertex);
    }

    /**
     * @see Graph#getAllEdges(Object, Object)
     */
    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        return super.getAllEdges(targetVertex, sourceVertex);
    }

    /**
     * @see Graph#addEdge(Object, Object)
     */
    @Override
    public E addEdge(V sourceVertex, V targetVertex)
    {
        return super.addEdge(targetVertex, sourceVertex);
    }

    /**
     * @see Graph#addEdge(Object, Object, Object)
     */
    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        return super.addEdge(targetVertex, sourceVertex, e);
    }

    /**
     * @see Graph#inDegreeOf(Object)
     */
    @Override
    public int inDegreeOf(V vertex)
    {
        return super.outDegreeOf(vertex);
    }

    /**
     * @see Graph#outDegreeOf(Object)
     */
    @Override
    public int outDegreeOf(V vertex)
    {
        return super.inDegreeOf(vertex);
    }

    /**
     * @see Graph#incomingEdgesOf(Object)
     */
    @Override
    public Set<E> incomingEdgesOf(V vertex)
    {
        return super.outgoingEdgesOf(vertex);
    }

    /**
     * @see Graph#outgoingEdgesOf(Object)
     */
    @Override
    public Set<E> outgoingEdgesOf(V vertex)
    {
        return super.incomingEdgesOf(vertex);
    }

    /**
     * @see Graph#removeEdge(Object, Object)
     */
    @Override
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        return super.removeEdge(targetVertex, sourceVertex);
    }

    /**
     * @see Graph#getEdgeSource(Object)
     */
    @Override
    public V getEdgeSource(E e)
    {
        return super.getEdgeTarget(e);
    }

    /**
     * @see Graph#getEdgeTarget(Object)
     */
    @Override
    public V getEdgeTarget(E e)
    {
        return super.getEdgeSource(e);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return toStringFromSets(vertexSet(), edgeSet(), getType().isDirected());
    }
}
