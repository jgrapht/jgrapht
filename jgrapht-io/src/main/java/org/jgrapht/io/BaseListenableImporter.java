/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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

import org.jgrapht.alg.util.*;

import java.util.*;
import java.util.function.*;

/**
 * Base implementation for a graph importer which uses consumers for attributes.
 * 
 * <p>
 * The importer will notify the registered consumers in no particular order about any attributes it
 * encounters in the input file.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
class BaseListenableImporter<V, E>
{

    private List<BiConsumer<String, Attribute>> graphAttributeConsumers;
    private List<BiConsumer<Pair<V, String>, Attribute>> vertexAttributeConsumers;
    private List<BiConsumer<Pair<E, String>, Attribute>> edgeAttributeConsumers;

    /**
     * Constructor
     */
    public BaseListenableImporter()
    {
        this.graphAttributeConsumers = new ArrayList<>();
        this.vertexAttributeConsumers = new ArrayList<>();
        this.edgeAttributeConsumers = new ArrayList<>();
    }

    /**
     * Add a graph attribute consumer.
     * 
     * @param consumer the consumer
     */
    public void addGraphAttributeConsumer(BiConsumer<String, Attribute> consumer)
    {
        graphAttributeConsumers.add(consumer);
    }

    /**
     * Remove a graph attribute consumer.
     * 
     * @param consumer the consumer
     */
    public void removeGraphAttributeConsumer(BiConsumer<String, Attribute> consumer)
    {
        graphAttributeConsumers.remove(consumer);
    }

    /**
     * Add a vertex attribute consumer.
     * 
     * @param consumer the consumer
     */
    public void addVertexAttributeConsumer(BiConsumer<Pair<V, String>, Attribute> consumer)
    {
        vertexAttributeConsumers.add(consumer);
    }

    /**
     * Remove a vertex attribute consumer.
     * 
     * @param consumer the consumer
     */
    public void removeVertexAttributeConsumer(BiConsumer<Pair<V, String>, Attribute> consumer)
    {
        vertexAttributeConsumers.remove(consumer);
    }

    /**
     * Add an edge attribute consumer.
     * 
     * @param consumer the consumer
     */
    public void addEdgeAttributeConsumer(BiConsumer<Pair<E, String>, Attribute> consumer)
    {
        edgeAttributeConsumers.add(consumer);
    }

    /**
     * Remove an edge attribute consumer.
     * 
     * @param consumer the consumer
     */
    public void removeEdgeAttributeConsumer(BiConsumer<Pair<E, String>, Attribute> consumer)
    {
        edgeAttributeConsumers.remove(consumer);
    }

    /**
     * Notify for a graph attribute
     * 
     * @param key the attribute key
     * @param value the attribute
     */
    protected void notifyGraph(String key, Attribute value)
    {
        graphAttributeConsumers.forEach(c -> c.accept(key, value));
    }

    /**
     * Notify for a vertex attribute
     * 
     * @param v the vertex
     * @param key the attribute key
     * @param value the attribute
     */
    protected void notifyVertex(V v, String key, Attribute value)
    {
        vertexAttributeConsumers.forEach(c -> c.accept(Pair.of(v, key), value));
    }

    /**
     * Notify for an edge attribute
     * 
     * @param e the edge
     * @param key the attribute key
     * @param value the attribute
     */
    protected void notifyEdge(E e, String key, Attribute value)
    {
        edgeAttributeConsumers.forEach(c -> c.accept(Pair.of(e, key), value));
    }

}
