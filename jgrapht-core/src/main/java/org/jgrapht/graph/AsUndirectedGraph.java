/*
 * (C) Copyright 2003-2021, by John V Sichi and Contributors.
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
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;

/**
 * An undirected view of the backing directed graph specified in the constructor. This graph allows
 * modules to apply algorithms designed for undirected graphs to a directed graph by simply ignoring
 * edge direction. If the backing directed graph is an
 * <a href="http://mathworld.wolfram.com/OrientedGraph.html">oriented graph</a>, then the view will
 * be a simple graph; otherwise, it will be a multigraph. Query operations on this graph "read
 * through" to the backing graph. Attempts to add edges will result in an
 * <code>UnsupportedOperationException</code>, but vertex addition/removal and edge removal are all
 * supported (and immediately reflected in the backing graph).
 *
 * <p>
 * Note that edges returned by this graph's accessors are really just the edges of the underlying
 * directed graph. Since there is no interface distinction between directed and undirected edges,
 * this detail should be irrelevant to algorithms.
 * </p>
 *
 * <p>
 * This graph does <i>not</i> pass the hashCode and equals operations through to the backing graph,
 * but relies on <code>Object</code>'s <code>equals</code> and <code>hashCode</code> methods. This
 * graph will be serializable if the backing graph is serializable.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author John V. Sichi
 */
public class AsUndirectedGraph<V, E>
    extends
    GraphDelegator<V, E>
    implements
    Serializable,
    Graph<V, E>
{
    private static final long serialVersionUID = 325983813283133557L;

    private static final String NO_EDGE_ADD = "this graph does not support edge addition";

    /**
     * Constructor for AsUndirectedGraph.
     *
     * @param g the backing directed graph over which an undirected view is to be created.
     * @throws IllegalArgumentException if the graph is not directed
     */
    public AsUndirectedGraph(Graph<V, E> g)
    {
        super(g);
        GraphTests.requireDirected(g);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        Set<E> forwardList = super.getAllEdges(sourceVertex, targetVertex);

        if (sourceVertex.equals(targetVertex)) {
            // avoid duplicating loops
            return forwardList;
        }

        Set<E> reverseList = super.getAllEdges(targetVertex, sourceVertex);
        Set<E> list = new ArrayUnenforcedSet<>(forwardList.size() + reverseList.size());
        list.addAll(forwardList);
        list.addAll(reverseList);

        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getEdge(V sourceVertex, V targetVertex)
    {
        E edge = super.getEdge(sourceVertex, targetVertex);

        if (edge != null) {
            return edge;
        }

        // try the other direction
        return super.getEdge(targetVertex, sourceVertex);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public E addEdge(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(NO_EDGE_ADD);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        throw new UnsupportedOperationException(NO_EDGE_ADD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int degreeOf(V vertex)
    {
        return super.degreeOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> incomingEdgesOf(V vertex)
    {
        return super.edgesOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inDegreeOf(V vertex)
    {
        return super.degreeOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> outgoingEdgesOf(V vertex)
    {
        return super.edgesOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int outDegreeOf(V vertex)
    {
        return super.degreeOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphType getType()
    {
        return super.getType().asUndirected();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return super.toStringFromSets(vertexSet(), edgeSet(), false);
    }
}
