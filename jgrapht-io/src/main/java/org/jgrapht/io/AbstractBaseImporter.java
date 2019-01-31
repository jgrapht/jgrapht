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

import org.jgrapht.*;

import java.util.*;

/**
 * Base implementation for a graph importer.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
abstract class AbstractBaseImporter<V, E>
{
    /**
     * Constructs new vertices
     */
    protected VertexProvider<V> vertexProvider;

    /**
     * Constructs new edges
     */
    protected EdgeProvider<V, E> edgeProvider;

    /**
     * Updates already constructed vertices
     */
    protected ComponentUpdater<V> vertexUpdater;

    /**
     * Updates graph properties
     */
    protected ComponentUpdater<Graph<V, E>> graphUpdater;

    /**
     * Constructor
     *
     * @param vertexProvider the vertex provider. Must not be null.
     * @param edgeProvider the edge provider. Must not be null.
     */
    public AbstractBaseImporter(VertexProvider<V> vertexProvider, EdgeProvider<V, E> edgeProvider)
    {
        this(vertexProvider, edgeProvider, (c, a) -> {
        }, (c, a) -> {
        });
    }

    /**
     * Constructor
     *
     * @param vertexProvider the vertex provider. Must not be null.
     * @param edgeProvider the edge provider. Must not be null.
     * @param vertexUpdater the vertex updater. Must not be null.
     * @param graphUpdater the graph updater. Must not be null.
     */
    public AbstractBaseImporter(
        VertexProvider<V> vertexProvider, EdgeProvider<V, E> edgeProvider,
        ComponentUpdater<V> vertexUpdater, ComponentUpdater<Graph<V, E>> graphUpdater)
    {
        this.vertexProvider =
            Objects.requireNonNull(vertexProvider, "Vertex provider cannot be null");
        this.edgeProvider = Objects.requireNonNull(edgeProvider, "Edge provider cannot be null");
        this.vertexUpdater = Objects.requireNonNull(vertexUpdater, "Vertex updater cannot be null");
        this.graphUpdater = Objects.requireNonNull(graphUpdater, "Graph updater cannot be null");
    }

    /**
     * Get the vertex provider
     *
     * @return the vertex provider
     */
    public VertexProvider<V> getVertexProvider()
    {
        return vertexProvider;
    }

    /**
     * Set the vertex provider
     *
     * @param vertexProvider the new vertex provider. Must not be null.
     */
    public void setVertexProvider(VertexProvider<V> vertexProvider)
    {
        this.vertexProvider =
            Objects.requireNonNull(vertexProvider, "Vertex provider cannot be null");
    }

    /**
     * Get the edge provider
     *
     * @return The edge provider
     */
    public EdgeProvider<V, E> getEdgeProvider()
    {
        return edgeProvider;
    }

    /**
     * Set the edge provider.
     *
     * @param edgeProvider the new edge provider. Must not be null.
     */
    public void setEdgeProvider(EdgeProvider<V, E> edgeProvider)
    {
        this.edgeProvider = Objects.requireNonNull(edgeProvider, "Edge provider cannot be null");
    }

    /**
     * Get the vertex updater
     *
     * @return the vertex updater
     */
    public ComponentUpdater<V> getVertexUpdater()
    {
        return vertexUpdater;
    }

    /**
     * Set the vertex updater.
     *
     * @param vertexUpdater the new vertex updater. Must not be null.
     */
    public void setVertexUpdater(ComponentUpdater<V> vertexUpdater)
    {
        this.vertexUpdater = Objects.requireNonNull(vertexUpdater, "Vertex updater cannot be null");
    }

    /**
     * Get the graph updater.
     *
     * @return the graph updater
     */
    public ComponentUpdater<Graph<V, E>> getGraphUpdater()
    {
        return graphUpdater;
    }

    /**
     * Set the graph updater.
     *
     * @param graphUpdater the new graph updater. Must not be null.
     */
    public void setGraphUpdater(ComponentUpdater<Graph<V, E>> graphUpdater)
    {
        this.graphUpdater = Objects.requireNonNull(graphUpdater, "Graph updater cannot be null");
    }

}
