/*
 * (C) Copyright 2019-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.nio;

import java.util.*;
import java.util.function.*;

/**
 * Base implementation for an exporter.
 *
 * @author Dimitrios Michail
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public abstract class BaseExporter<V, E>
{
    /**
     * A graph id provider
     */
    protected Optional<Supplier<String>> graphIdProvider;

    /**
     * A graph attribute provider
     */
    protected Optional<Supplier<Map<String, Attribute>>> graphAttributeProvider;

    /**
     * A vertex id provider
     */
    protected Function<V, String> vertexIdProvider;

    /**
     * A vertex attribute provider
     */
    protected Optional<Function<V, Map<String, Attribute>>> vertexAttributeProvider;

    /**
     * An edge id provider
     */
    protected Optional<Function<E, String>> edgeIdProvider;

    /**
     * An edge attribute provider
     */
    protected Optional<Function<E, Map<String, Attribute>>> edgeAttributeProvider;

    /**
     * Constructor
     * 
     * @param vertexIdProvider the vertex id provider to use. Cannot be null.
     */
    public BaseExporter(Function<V, String> vertexIdProvider)
    {
        this.vertexIdProvider = Objects.requireNonNull(vertexIdProvider);
        this.graphIdProvider = Optional.empty();
        this.graphAttributeProvider = Optional.empty();
        this.vertexAttributeProvider = Optional.empty();
        this.edgeIdProvider = Optional.empty();
        this.edgeAttributeProvider = Optional.empty();
    }

    /**
     * Get the graph id provider.
     * 
     * @return the graph id provider as an {@link Optional}.
     */
    public Optional<Supplier<String>> getGraphIdProvider()
    {
        return graphIdProvider;
    }

    /**
     * Set the graph id provider.
     * 
     * @param graphIdProvider the graph id provider
     */
    public void setGraphIdProvider(Supplier<String> graphIdProvider)
    {
        this.graphIdProvider = Optional.ofNullable(graphIdProvider);
    }

    /**
     * Get the graph attribute provider.
     * 
     * @return the graph attribute provider as an {@link Optional}.
     */
    public Optional<Supplier<Map<String, Attribute>>> getGraphAttributeProvider()
    {
        return graphAttributeProvider;
    }

    /**
     * Set the graph attribute provider.
     * 
     * @param graphAttributeProvider the graph attribute provider
     */
    public void setGraphAttributeProvider(Supplier<Map<String, Attribute>> graphAttributeProvider)
    {
        this.graphAttributeProvider = Optional.ofNullable(graphAttributeProvider);
    }

    /**
     * Get vertex id provider.
     * 
     * @return the vertex id provider
     */
    public Function<V, String> getVertexIdProvider()
    {
        return vertexIdProvider;
    }

    /**
     * Set the vertex id provider
     * 
     * @param vertexIdProvider the vertex id provider
     */
    public void setVertexIdProvider(Function<V, String> vertexIdProvider)
    {
        this.vertexIdProvider = Objects.requireNonNull(vertexIdProvider);
    }

    /**
     * Get the vertex attribute provider
     * 
     * @return the vertex attribute provider as an {@link Optional}
     */
    public Optional<Function<V, Map<String, Attribute>>> getVertexAttributeProvider()
    {
        return vertexAttributeProvider;
    }

    /**
     * Set the vertex attribute provider
     * 
     * @param vertexAttributeProvider the vertex attribute provider
     */
    public void setVertexAttributeProvider(
        Function<V, Map<String, Attribute>> vertexAttributeProvider)
    {
        this.vertexAttributeProvider = Optional.ofNullable(vertexAttributeProvider);
    }

    /**
     * Get the edge id provider
     * 
     * @return the edge id provider as an {@link Optional}.
     */
    public Optional<Function<E, String>> getEdgeIdProvider()
    {
        return edgeIdProvider;
    }

    /**
     * Set edge id provider
     * 
     * @param edgeIdProvider the edge id provider
     */
    public void setEdgeIdProvider(Function<E, String> edgeIdProvider)
    {
        this.edgeIdProvider = Optional.ofNullable(edgeIdProvider);
    }

    /**
     * Get the edge attribute provider
     * 
     * @return the edge attribute provider as an {@link Optional}
     */
    public Optional<Function<E, Map<String, Attribute>>> getEdgeAttributeProvider()
    {
        return edgeAttributeProvider;
    }

    /**
     * Set the edge attribute provider.
     * 
     * @param edgeAttributeProvider the edge attribute provider
     */
    public void setEdgeAttributeProvider(Function<E, Map<String, Attribute>> edgeAttributeProvider)
    {
        this.edgeAttributeProvider = Optional.ofNullable(edgeAttributeProvider);
    }

    /**
     * Get the graph id if present
     * 
     * @return an {@link Optional} of the graph id
     */
    protected Optional<String> getGraphId()
    {
        return graphIdProvider.map(x -> x.get());
    }

    /**
     * Get the vertex id
     * 
     * @param v the vertex
     * @return the id of the vertex
     */
    protected String getVertexId(V v)
    {
        return vertexIdProvider.apply(v);
    }

    /**
     * Get an optional of the edge id
     * 
     * @param e the edge
     * @return the edge id
     */
    protected Optional<String> getEdgeId(E e)
    {
        return edgeIdProvider.map(x -> x.apply(e));
    }

    /**
     * Get vertex attributes
     * 
     * @param v the vertex v
     * @return the vertex attributes as an {@link Optional}
     */
    protected Optional<Map<String, Attribute>> getVertexAttributes(V v)
    {
        return vertexAttributeProvider.map(x -> x.apply(v));
    }

    /**
     * Get an optional of a vertex attribute
     * 
     * @param v the vertex v
     * @param key the attribute key
     * @return the attribute as an {@link Optional}
     */
    protected Optional<Attribute> getVertexAttribute(V v, String key)
    {
        return vertexAttributeProvider.map(x -> x.apply(v).get(key));
    }

    /**
     * Get edge attributes
     * 
     * @param e the edge e
     * @return the edge attributes as an {@link Optional}
     */
    protected Optional<Map<String, Attribute>> getEdgeAttributes(E e)
    {
        return edgeAttributeProvider.map(x -> x.apply(e));
    }

    /**
     * Get an optional of an edge attribute
     * 
     * @param e the edge e
     * @param key the attribute key
     * @return the attribute as an {@link Optional}
     */
    protected Optional<Attribute> getEdgeAttribute(E e, String key)
    {
        return edgeAttributeProvider.map(x -> x.apply(e).get(key));
    }

    /**
     * Get an optional of a graph attribute
     * 
     * @param key the attribute key
     * @return the attribute as an {@link Optional}
     */
    protected Optional<Attribute> getGraphAttribute(String key)
    {
        return graphAttributeProvider.map(x -> x.get().get(key));
    }

}
