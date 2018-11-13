/*
 * (C) Copyright 2015-2018, by Andrew Chen and Contributors.
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
package org.jgrapht.graph.builder;

import org.jgrapht.*;
import org.jgrapht.graph.*;

/**
 * Base class for builders of {@link Graph}
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @param <G> type of the resulting graph
 * @param <B> type of this builder
 * 
 * @author Andrew Chen
 */
public abstract class AbstractGraphBuilder<V, E, G extends Graph<V, E>,
    B extends AbstractGraphBuilder<V, E, G, B>>
{
    protected final G graph;

    /**
     * Creates a builder based on {@code baseGraph}. {@code baseGraph} must be mutable.
     *
     * @param baseGraph the graph object to base building on
     */
    public AbstractGraphBuilder(G baseGraph)
    {
        this.graph = baseGraph;
    }

    /**
     * @return the {@code this} object.
     */
    protected abstract B self();

    /**
     * Adds {@code vertex} to the graph being built.
     *
     * @param vertex the vertex to add
     *
     * @return this builder object
     *
     * @see Graph#addVertex(Object)
     */
    public B addVertex(V vertex)
    {
        this.graph.addVertex(vertex);
        return this.self();
    }

    /**
     * Adds each vertex of {@code vertices} to the graph being built.
     *
     * @param vertices the vertices to add
     *
     * @return this builder object
     *
     * @see #addVertex(Object)
     */
    @SafeVarargs
    public final B addVertices(V... vertices)
    {
        for (V vertex : vertices) {
            this.addVertex(vertex);
        }
        return this.self();
    }

    /**
     * Adds an edge to the graph being built. The source and target vertices are added to the graph,
     * if not already included.
     *
     * @param source source vertex of the edge.
     * @param target target vertex of the edge.
     *
     * @return this builder object
     *
     * @see Graphs#addEdgeWithVertices(Graph, Object, Object)
     */
    public B addEdge(V source, V target)
    {
        Graphs.addEdgeWithVertices(this.graph, source, target);
        return this.self();
    }

    /**
     * Adds the specified edge to the graph being built. The source and target vertices are added to
     * the graph, if not already included.
     *
     * @param source source vertex of the edge.
     * @param target target vertex of the edge.
     * @param edge edge to be added to this graph.
     * @return this builder object
     *
     * @see Graph#addEdge(Object, Object, Object)
     */
    public B addEdge(V source, V target, E edge)
    {
        this.addVertex(source);
        this.addVertex(target);
        this.graph.addEdge(source, target, edge);
        return this.self();
    }

    /**
     * Adds a chain of edges to the graph being built. The vertices are added to the graph, if not
     * already included.
     *
     * @param first the first vertex
     * @param second the second vertex
     * @param rest the remaining vertices
     * @return this builder object
     *
     * @see #addEdge(Object, Object)
     */
    @SafeVarargs
    public final B addEdgeChain(V first, V second, V... rest)
    {
        this.addEdge(first, second);
        V last = second;
        for (V vertex : rest) {
            this.addEdge(last, vertex);
            last = vertex;
        }
        return this.self();
    }

    /**
     * Adds all the vertices and all the edges of the {@code sourceGraph} to the graph being built.
     *
     * @param sourceGraph the source graph
     * @return this builder object
     *
     * @see Graphs#addGraph(Graph, Graph)
     */
    public B addGraph(Graph<? extends V, ? extends E> sourceGraph)
    {
        Graphs.addGraph(this.graph, sourceGraph);
        return this.self();
    }

    /**
     * Removes {@code vertex} from the graph being built, if such vertex exist in graph.
     *
     * @param vertex the vertex to remove
     *
     * @return this builder object
     *
     * @see Graph#removeVertex(Object)
     */
    public B removeVertex(V vertex)
    {
        this.graph.removeVertex(vertex);
        return this.self();
    }

    /**
     * Removes each vertex of {@code vertices} from the graph being built, if such vertices exist in
     * graph.
     *
     * @param vertices the vertices to remove
     *
     * @return this builder object
     *
     * @see #removeVertex(Object)
     */
    @SafeVarargs
    public final B removeVertices(V... vertices)
    {
        for (V vertex : vertices) {
            this.removeVertex(vertex);
        }
        return this.self();
    }

    /**
     * Removes an edge going from source vertex to target vertex from the graph being built, if such
     * vertices and such edge exist in the graph.
     *
     * @param source source vertex of the edge.
     * @param target target vertex of the edge.
     *
     * @return this builder object
     *
     * @see Graph#removeVertex(Object)
     */
    public B removeEdge(V source, V target)
    {
        this.graph.removeEdge(source, target);
        return this.self();
    }

    /**
     * Removes the specified edge from the graph. Removes the specified edge from this graph if it
     * is present.
     *
     * @param edge edge to be removed from this graph, if present.
     * @return this builder object
     *
     * @see Graph#removeEdge(Object)
     */
    public B removeEdge(E edge)
    {
        this.graph.removeEdge(edge);
        return this.self();
    }

    /**
     * Adds an weighted edge to the graph being built. The source and target vertices are added to
     * the graph, if not already included.
     *
     * @param source source vertex of the edge.
     * @param target target vertex of the edge.
     * @param weight weight of the edge.
     *
     * @return this builder object
     *
     * @see Graphs#addEdgeWithVertices(Graph, Object, Object, double)
     */
    public B addEdge(V source, V target, double weight)
    {
        Graphs.addEdgeWithVertices(this.graph, source, target, weight);
        return this.self();
    }

    /**
     * Adds the specified weighted edge to the graph being built. The source and target vertices are
     * added to the graph, if not already included.
     *
     * @param source source vertex of the edge.
     * @param target target vertex of the edge.
     * @param edge edge to be added to this graph.
     * @param weight weight of the edge.
     *
     * @return this builder object
     *
     * @see Graph#addEdge(Object, Object, Object)
     * @see Graph#setEdgeWeight(Object, double)
     */
    public B addEdge(V source, V target, E edge, double weight)
    {
        this.graph.addEdge(source, target, edge);
        this.graph.setEdgeWeight(edge, weight);
        return this.self();
    }

    /**
     * Build the graph. Calling any method (including this method) on this builder object after
     * calling this method is undefined behaviour.
     *
     * @return the built graph.
     */
    public G build()
    {
        return this.graph;
    }

    /**
     * Build an unmodifiable version graph. Calling any method (including this method) on this
     * builder object after calling this method is undefined behaviour.
     *
     * @return the built unmodifiable graph.
     *
     * @see #build()
     */
    public Graph<V, E> buildAsUnmodifiable()
    {
        return new AsUnmodifiableGraph<>(this.graph);
    }
}
