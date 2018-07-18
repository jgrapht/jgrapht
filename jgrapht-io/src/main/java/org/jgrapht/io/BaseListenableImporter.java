/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.jgrapht.alg.util.Pair;

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
 * @since May 2018
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
    public void removeGraphAttributeConsumer(
        BiConsumer<String, Attribute> consumer)
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
