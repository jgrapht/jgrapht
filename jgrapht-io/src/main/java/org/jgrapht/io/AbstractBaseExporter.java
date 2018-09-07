/*
 * (C) Copyright 2017-2017, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.io;

import java.util.*;

/**
 * Base implementation for a graph exporter.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 * @since March 2017
 */
abstract class AbstractBaseExporter<V, E>
{
    /**
     * Provides an identifier for a vertex.
     */
    protected ComponentNameProvider<V> vertexIDProvider;

    /**
     * Provides an identifier for an edge.
     */
    protected ComponentNameProvider<E> edgeIDProvider;

    /**
     * Constructor
     *
     * @param vertexIDProvider the vertex id provider. Must not be null.
     */
    public AbstractBaseExporter(ComponentNameProvider<V> vertexIDProvider)
    {
        this(vertexIDProvider, null);
    }

    /**
     * Constructor
     *
     * @param vertexIDProvider the vertex id provider. Must not be null.
     * @param edgeIDProvider the edge id provider
     */
    public AbstractBaseExporter(
        ComponentNameProvider<V> vertexIDProvider, ComponentNameProvider<E> edgeIDProvider)
    {
        this.vertexIDProvider =
            Objects.requireNonNull(vertexIDProvider, "Vertex id provider cannot be null");
        this.edgeIDProvider = edgeIDProvider;
    }

    /**
     * Get the vertex id provider
     *
     * @return the vertex id provider
     */
    public ComponentNameProvider<V> getVertexIDProvider()
    {
        return vertexIDProvider;
    }

    /**
     * Set the vertex id provider
     *
     * @param vertexIDProvider the new vertex id provider. Must not be null.
     */
    public void setVertexIDProvider(ComponentNameProvider<V> vertexIDProvider)
    {
        this.vertexIDProvider =
            Objects.requireNonNull(vertexIDProvider, "Vertex id provider cannot be null");
    }

    /**
     * Get the edge id provider
     *
     * @return The edge provider
     */
    public ComponentNameProvider<E> getEdgeIDProvider()
    {
        return edgeIDProvider;
    }

    /**
     * Set the edge id provider.
     *
     * @param edgeIDProvider the new edge id provider. Must not be null.
     */
    public void setEdgeIDProvider(ComponentNameProvider<E> edgeIDProvider)
    {
        this.edgeIDProvider = edgeIDProvider;
    }

}
