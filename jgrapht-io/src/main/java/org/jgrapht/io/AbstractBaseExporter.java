/*
 * (C) Copyright 2017-2017, by Dimitrios Michail and Contributors.
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
package org.jgrapht.io;

import java.util.*;

/**
 * Base implementation for a graph exporter.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
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
