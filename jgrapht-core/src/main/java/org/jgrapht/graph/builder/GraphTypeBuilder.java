/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
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
import org.jgrapht.util.*;

import java.util.function.*;

/**
 * A builder class for the hierarchy of {@link Graph}s that the library provides.
 *
 * <p>
 * The following example creates a directed graph which allows multiple (parallel) edges and
 * self-loops: <blockquote>
 * 
 * <pre>
 * Graph&lt;Integer,
 *     DefaultEdge&gt; g = GraphTypeBuilder
 *         .&lt;Integer, DefaultEdge&gt; directed().allowingMultipleEdges(true).allowingSelfLoops(true)
 *         .edgeClass(DefaultEdge.class).buildGraph();
 * </pre>
 * 
 * </blockquote>
 * 
 * Similarly one could get a weighted multigraph by using: <blockquote>
 * 
 * <pre>
 * Graph&lt;Integer, DefaultWeightedEdge&gt; g = GraphTypeBuilder
 *     .&lt;Integer, DefaultWeightedEdge&gt; undirected().allowingMultipleEdges(true)
 *     .allowingSelfLoops(false).edgeClass(DefaultWeightedEdge.class).weighted(true).buildGraph();
 * </pre>
 * 
 * </blockquote>
 * 
 * <p>
 * The builder also provides the ability to construct a graph from another graph such as:
 * <blockquote>
 * 
 * <pre>
 * Graph&lt;Integer, DefaultWeightedEdge&gt; g1 = GraphTypeBuilder
 *     .&lt;Integer, DefaultWeightedEdge&gt; undirected().allowingMultipleEdges(true)
 *     .allowingSelfLoops(false).edgeClass(DefaultWeightedEdge.class).weighted(true).buildGraph();
 * 
 * Graph&lt;Integer, DefaultWeightedEdge&gt; g2 = GraphTypeBuilder.asGraph(g1).buildGraph();
 * </pre>
 * 
 * </blockquote>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 * 
 * @see GraphType
 * @see GraphBuilder
 */
public final class GraphTypeBuilder<V, E>
{
    private boolean undirected;
    private boolean directed;
    private boolean weighted;
    private boolean allowingMultipleEdges;
    private boolean allowingSelfLoops;
    private Supplier<V> vertexSupplier;
    private Supplier<E> edgeSupplier;

    private GraphTypeBuilder(boolean directed, boolean undirected)
    {
        this.directed = directed;
        this.undirected = undirected;
        this.weighted = false;
        this.allowingMultipleEdges = false;
        this.allowingSelfLoops = false;
    }

    /**
     * Create a graph type builder for a directed graph.
     * 
     * @return the graph type builder
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    public static <V, E> GraphTypeBuilder<V, E> directed()
    {
        return new GraphTypeBuilder<>(true, false);
    }

    /**
     * Create a graph type builder for an undirected graph.
     * 
     * @return the graph type builder
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    public static <V, E> GraphTypeBuilder<V, E> undirected()
    {
        return new GraphTypeBuilder<>(false, true);
    }

    /**
     * Create a graph type builder for a mixed graph.
     * 
     * @return the graph type builder
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    public static <V, E> GraphTypeBuilder<V, E> mixed()
    {
        return new GraphTypeBuilder<>(true, true);
    }

    /**
     * Create a graph type builder which will create a graph with the same type as the one provided.
     * 
     * @param type the graph type
     * @return the graph type builder
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    public static <V, E> GraphTypeBuilder<V, E> forGraphType(GraphType type)
    {
        GraphTypeBuilder<V, E> builder = new GraphTypeBuilder<>(
            type.isDirected() || type.isMixed(), type.isUndirected() || type.isMixed());
        builder.weighted = type.isWeighted();
        builder.allowingSelfLoops = type.isAllowingSelfLoops();
        builder.allowingMultipleEdges = type.isAllowingMultipleEdges();
        return builder;
    }

    /**
     * Create a graph type builder which will create the same graph type as the parameter graph. The
     * new graph will use the same vertex and edge suppliers as the input graph.
     * 
     * @param graph a graph
     * @return a type builder
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    public static <V, E> GraphTypeBuilder<V, E> forGraph(Graph<V, E> graph)
    {
        GraphTypeBuilder<V, E> builder = forGraphType(graph.getType());
        builder.vertexSupplier = graph.getVertexSupplier();
        builder.edgeSupplier = graph.getEdgeSupplier();
        return builder;
    }

    /**
     * Set whether the graph will be weighted or not.
     * 
     * @param weighted if true the graph will be weighted
     * @return the graph type builder
     */
    public GraphTypeBuilder<V, E> weighted(boolean weighted)
    {
        this.weighted = weighted;
        return this;
    }

    /**
     * Set whether the graph will allow self loops (edges with same source and target vertices).
     * 
     * @param allowingSelfLoops if true the graph will allow self-loops
     * @return the graph type builder
     */
    public GraphTypeBuilder<V, E> allowingSelfLoops(boolean allowingSelfLoops)
    {
        this.allowingSelfLoops = allowingSelfLoops;
        return this;
    }

    /**
     * Set whether the graph will allow multiple (parallel) edges between the same two vertices.
     * 
     * @param allowingMultipleEdges if true the graph will allow multiple (parallel) edges
     * @return the graph type builder
     */
    public GraphTypeBuilder<V, E> allowingMultipleEdges(boolean allowingMultipleEdges)
    {
        this.allowingMultipleEdges = allowingMultipleEdges;
        return this;
    }

    /**
     * Set the vertex supplier.
     * 
     * @param vertexSupplier the vertex supplier to use
     * @return the graph type builder
     * @param <V1> the graph vertex type
     */
    public <V1 extends V> GraphTypeBuilder<V1, E> vertexSupplier(Supplier<V1> vertexSupplier)
    {
        GraphTypeBuilder<V1, E> newBuilder = TypeUtil.uncheckedCast(this);
        newBuilder.vertexSupplier = vertexSupplier;
        return newBuilder;
    }

    /**
     * Set the edge supplier.
     * 
     * @param edgeSupplier the edge supplier to use
     * @return the graph type builder
     * @param <E1> the graph edge type
     */
    public <E1 extends E> GraphTypeBuilder<V, E1> edgeSupplier(Supplier<E1> edgeSupplier)
    {
        GraphTypeBuilder<V, E1> newBuilder = TypeUtil.uncheckedCast(this);
        newBuilder.edgeSupplier = edgeSupplier;
        return newBuilder;
    }

    /**
     * Set the vertex class.
     * 
     * @param vertexClass the vertex class
     * @return the graph type builder
     * @param <V1> the graph vertex type
     */
    public <V1 extends V> GraphTypeBuilder<V1, E> vertexClass(Class<V1> vertexClass)
    {
        GraphTypeBuilder<V1, E> newBuilder = TypeUtil.uncheckedCast(this);
        newBuilder.vertexSupplier = SupplierUtil.createSupplier(vertexClass);
        return newBuilder;
    }

    /**
     * Set the edge class.
     * 
     * @param edgeClass the edge class
     * @return the graph type builder
     * @param <E1> the graph edge type
     */
    public <E1 extends E> GraphTypeBuilder<V, E1> edgeClass(Class<E1> edgeClass)
    {
        GraphTypeBuilder<V, E1> newBuilder = TypeUtil.uncheckedCast(this);
        newBuilder.edgeSupplier = SupplierUtil.createSupplier(edgeClass);
        return newBuilder;
    }

    /**
     * Build the graph type.
     * 
     * @return a graph type
     */
    public GraphType buildType()
    {
        DefaultGraphType.Builder typeBuilder = new DefaultGraphType.Builder();
        if (directed && undirected) {
            typeBuilder = typeBuilder.mixed();
        } else if (directed) {
            typeBuilder = typeBuilder.directed();
        } else if (undirected) {
            typeBuilder = typeBuilder.undirected();
        }
        return typeBuilder
            .allowMultipleEdges(allowingMultipleEdges).allowSelfLoops(allowingSelfLoops)
            .weighted(weighted).build();
    }

    /**
     * Build the graph and acquire a {@link GraphBuilder} in order to add vertices and edges.
     * 
     * @return a graph builder
     */
    public GraphBuilder<V, E, Graph<V, E>> buildGraphBuilder()
    {
        return new GraphBuilder<V, E, Graph<V, E>>(buildGraph());
    }

    /**
     * Build the actual graph.
     * 
     * @return the graph
     * @throws UnsupportedOperationException in case a graph type is not supported
     */
    public Graph<V, E> buildGraph()
    {
        if (directed && undirected) {
            throw new UnsupportedOperationException("Mixed graphs are not supported");
        } else if (directed) {
            if (allowingSelfLoops && allowingMultipleEdges) {
                if (weighted) {
                    return new DirectedWeightedPseudograph<>(vertexSupplier, edgeSupplier);
                } else {
                    return new DirectedPseudograph<>(vertexSupplier, edgeSupplier, false);
                }
            } else if (allowingMultipleEdges) {
                if (weighted) {
                    return new DirectedWeightedMultigraph<>(vertexSupplier, edgeSupplier);
                } else {
                    return new DirectedMultigraph<>(vertexSupplier, edgeSupplier, false);
                }
            } else if (allowingSelfLoops) {
                if (weighted) {
                    return new DefaultDirectedWeightedGraph<>(vertexSupplier, edgeSupplier);
                } else {
                    return new DefaultDirectedGraph<>(vertexSupplier, edgeSupplier, false);
                }

            } else {
                if (weighted) {
                    return new SimpleDirectedWeightedGraph<>(vertexSupplier, edgeSupplier);
                } else {
                    return new SimpleDirectedGraph<>(vertexSupplier, edgeSupplier, false);
                }
            }
        } else {
            if (allowingSelfLoops && allowingMultipleEdges) {
                if (weighted) {
                    return new WeightedPseudograph<>(vertexSupplier, edgeSupplier);
                } else {
                    return new Pseudograph<>(vertexSupplier, edgeSupplier, false);
                }
            } else if (allowingMultipleEdges) {
                if (weighted) {
                    return new WeightedMultigraph<>(vertexSupplier, edgeSupplier);
                } else {
                    return new Multigraph<>(vertexSupplier, edgeSupplier, false);
                }
            } else if (allowingSelfLoops) {
                if (weighted) {
                    return new DefaultUndirectedWeightedGraph<>(vertexSupplier, edgeSupplier);
                } else {
                    return new DefaultUndirectedGraph<>(vertexSupplier, edgeSupplier, false);
                }

            } else {
                if (weighted) {
                    return new SimpleWeightedGraph<>(vertexSupplier, edgeSupplier);
                } else {
                    return new SimpleGraph<>(vertexSupplier, edgeSupplier, false);
                }
            }
        }
    }

}
