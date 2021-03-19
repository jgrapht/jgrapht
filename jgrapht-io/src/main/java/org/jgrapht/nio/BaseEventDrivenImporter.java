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

import org.jgrapht.alg.util.*;

import java.util.*;
import java.util.function.*;

/**
 * Base implementation for an importer which uses consumers to notify interested parties. Note that
 * this importer does not compute anything, it simply calls the appropriate consumers to do the
 * actual work.
 *
 * @author Dimitrios Michail
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public abstract class BaseEventDrivenImporter<V, E>
{
    private List<Consumer<Integer>> vertexCountConsumers;
    private List<Consumer<Integer>> edgeCountConsumers;
    private List<Consumer<V>> vertexConsumers;
    private List<BiConsumer<V, Map<String, Attribute>>> vertexWithAttributesConsumers;
    private List<Consumer<E>> edgeConsumers;
    private List<BiConsumer<E, Map<String, Attribute>>> edgeWithAttributesConsumers;
    private List<BiConsumer<String, Attribute>> graphAttributeConsumers;
    private List<BiConsumer<Pair<V, String>, Attribute>> vertexAttributeConsumers;
    private List<BiConsumer<Pair<E, String>, Attribute>> edgeAttributeConsumers;
    private List<Consumer<ImportEvent>> importEventConsumers;

    /**
     * Constructor
     */
    public BaseEventDrivenImporter()
    {
        this.vertexCountConsumers = new ArrayList<>();
        this.edgeCountConsumers = new ArrayList<>();
        this.vertexConsumers = new ArrayList<>();
        this.vertexWithAttributesConsumers = new ArrayList<>();
        this.edgeConsumers = new ArrayList<>();
        this.edgeWithAttributesConsumers = new ArrayList<>();
        this.graphAttributeConsumers = new ArrayList<>();
        this.vertexAttributeConsumers = new ArrayList<>();
        this.edgeAttributeConsumers = new ArrayList<>();
        this.importEventConsumers = new ArrayList<>();
    }

    /**
     * Add an ImportEvent consumer.
     * 
     * @param consumer the consumer
     */
    public void addImportEventConsumer(Consumer<ImportEvent> consumer)
    {
        importEventConsumers.add(consumer);
    }

    /**
     * Remove an ImportEvent consumer.
     * 
     * @param consumer the consumer
     */
    public void removeImportEventConsumer(Consumer<ImportEvent> consumer)
    {
        importEventConsumers.remove(consumer);
    }

    /**
     * Add a vertex count consumer.
     * 
     * @param consumer the consumer
     */
    public void addVertexCountConsumer(Consumer<Integer> consumer)
    {
        vertexCountConsumers.add(consumer);
    }

    /**
     * Remove a vertex count consumer.
     * 
     * @param consumer the consumer
     */
    public void removeVertexCountConsumer(Consumer<Integer> consumer)
    {
        vertexCountConsumers.remove(consumer);
    }

    /**
     * Add an edge count consumer.
     * 
     * @param consumer the consumer
     */
    public void addEdgeCountConsumer(Consumer<Integer> consumer)
    {
        edgeCountConsumers.add(consumer);
    }

    /**
     * Remove an edge count consumer.
     * 
     * @param consumer the consumer
     */
    public void removeEdgeCountConsumer(Consumer<Integer> consumer)
    {
        edgeCountConsumers.remove(consumer);
    }

    /**
     * Add a vertex consumer.
     * 
     * @param consumer the consumer
     */
    public void addVertexConsumer(Consumer<V> consumer)
    {
        vertexConsumers.add(consumer);
    }

    /**
     * Remove a vertex consumer.
     * 
     * @param consumer the consumer
     */
    public void removeVertexConsumer(Consumer<V> consumer)
    {
        vertexConsumers.remove(consumer);
    }

    /**
     * Add an edge consumer.
     * 
     * @param consumer the consumer
     */
    public void addEdgeConsumer(Consumer<E> consumer)
    {
        edgeConsumers.add(consumer);
    }

    /**
     * Remove an edge consumer.
     * 
     * @param consumer the consumer
     */
    public void removeEdgeConsumer(Consumer<E> consumer)
    {
        edgeConsumers.remove(consumer);
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
     * Add a vertex with attributes consumer.
     * 
     * @param consumer the consumer
     */
    public void addVertexWithAttributesConsumer(BiConsumer<V, Map<String, Attribute>> consumer)
    {
        vertexWithAttributesConsumers.add(consumer);
    }

    /**
     * Remove a vertex with attributes consumer
     * 
     * @param consumer the consumer
     */
    public void removeVertexWithAttributesConsumer(BiConsumer<V, Map<String, Attribute>> consumer)
    {
        vertexWithAttributesConsumers.remove(consumer);
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
     * Add an edge with attributes consumer.
     * 
     * @param consumer the consumer
     */
    public void addEdgeWithAttributesConsumer(BiConsumer<E, Map<String, Attribute>> consumer)
    {
        edgeWithAttributesConsumers.add(consumer);
    }

    /**
     * Remove an edge with attributes consumer
     * 
     * @param consumer the consumer
     */
    public void removeEdgeWithAttributesConsumer(BiConsumer<E, Map<String, Attribute>> consumer)
    {
        edgeWithAttributesConsumers.remove(consumer);
    }

    /**
     * Notify for the vertex count.
     * 
     * @param vertexCount the number of vertices in the graph
     */
    protected void notifyVertexCount(Integer vertexCount)
    {
        vertexCountConsumers.forEach(c -> c.accept(vertexCount));
    }

    /**
     * Notify for the edge count.
     * 
     * @param edgeCount the number of edges in the graph
     */
    protected void notifyEdgeCount(Integer edgeCount)
    {
        edgeCountConsumers.forEach(c -> c.accept(edgeCount));
    }

    /**
     * Notify for a vertex.
     * 
     * @param v the vertex
     */
    protected void notifyVertex(V v)
    {
        vertexConsumers.forEach(c -> c.accept(v));
    }

    /**
     * Notify for a vertex with attributes.
     * 
     * @param v the vertex
     * @param attrs the attributes
     */
    protected void notifyVertexWithAttributes(V v, Map<String, Attribute> attrs)
    {
        vertexWithAttributesConsumers.forEach(c -> c.accept(v, attrs));
    }

    /**
     * Notify for an edge.
     * 
     * @param e the edge
     */
    protected void notifyEdge(E e)
    {
        edgeConsumers.forEach(c -> c.accept(e));
    }

    /**
     * Notify for an edge with attributes.
     * 
     * @param e the edge
     * @param attrs the attributes
     */
    protected void notifyEdgeWithAttributes(E e, Map<String, Attribute> attrs)
    {
        edgeWithAttributesConsumers.forEach(c -> c.accept(e, attrs));
    }

    /**
     * Notify for a graph attribute
     * 
     * @param key the attribute key
     * @param value the attribute
     */
    protected void notifyGraphAttribute(String key, Attribute value)
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
    protected void notifyVertexAttribute(V v, String key, Attribute value)
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
    protected void notifyEdgeAttribute(E e, String key, Attribute value)
    {
        edgeAttributeConsumers.forEach(c -> c.accept(Pair.of(e, key), value));
    }

    /**
     * Notify for an importer ImportEvent
     * 
     * @param importEvent the ImportEvent
     */
    protected void notifyImportEvent(ImportEvent importEvent)
    {
        importEventConsumers.forEach(c -> c.accept(importEvent));
    }

}
