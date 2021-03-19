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

import java.io.*;
import java.nio.charset.*;
import java.util.Map;
import java.util.function.*;

/**
 * Interface for an importer using consumers.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public interface EventDrivenImporter<V, E>
{
    /**
     * Add an ImportEvent consumer.
     * 
     * @param consumer the consumer
     */
    void addImportEventConsumer(Consumer<ImportEvent> consumer);

    /**
     * Remove an ImportEvent consumer.
     * 
     * @param consumer the consumer
     */
    void removeImportEventConsumer(Consumer<ImportEvent> consumer);

    /**
     * Add a vertex count consumer.
     * 
     * @param consumer the consumer
     */
    void addVertexCountConsumer(Consumer<Integer> consumer);

    /**
     * Remove a vertex count consumer.
     * 
     * @param consumer the consumer
     */
    void removeVertexCountConsumer(Consumer<Integer> consumer);

    /**
     * Add an edge count consumer.
     * 
     * @param consumer the consumer
     */
    void addEdgeCountConsumer(Consumer<Integer> consumer);

    /**
     * Remove an edge count consumer.
     * 
     * @param consumer the consumer
     */
    void removeEdgeCountConsumer(Consumer<Integer> consumer);

    /**
     * Add a vertex consumer.
     * 
     * @param consumer the consumer
     */
    void addVertexConsumer(Consumer<V> consumer);

    /**
     * Remove a vertex consumer.
     * 
     * @param consumer the consumer
     */
    void removeVertexConsumer(Consumer<V> consumer);

    /**
     * Add a vertex with attributes consumer.
     * 
     * @param consumer the consumer
     */
    void addVertexWithAttributesConsumer(BiConsumer<V, Map<String, Attribute>> consumer);

    /**
     * Remove a vertex with attributes consumer
     * 
     * @param consumer the consumer
     */
    void removeVertexWithAttributesConsumer(BiConsumer<V, Map<String, Attribute>> consumer);

    /**
     * Add an edge consumer.
     * 
     * @param consumer the consumer
     */
    void addEdgeConsumer(Consumer<E> consumer);

    /**
     * Remove an edge consumer.
     * 
     * @param consumer the consumer
     */
    void removeEdgeConsumer(Consumer<E> consumer);

    /**
     * Add an edge with attributes consumer.
     * 
     * @param consumer the consumer
     */
    void addEdgeWithAttributesConsumer(BiConsumer<E, Map<String, Attribute>> consumer);

    /**
     * Remove an edge with attributes consumer
     * 
     * @param consumer the consumer
     */
    void removeEdgeWithAttributesConsumer(BiConsumer<E, Map<String, Attribute>> consumer);

    /**
     * Add a graph attribute consumer.
     * 
     * @param consumer the consumer
     */
    void addGraphAttributeConsumer(BiConsumer<String, Attribute> consumer);

    /**
     * Remove a graph attribute consumer.
     * 
     * @param consumer the consumer
     */
    void removeGraphAttributeConsumer(BiConsumer<String, Attribute> consumer);

    /**
     * Add a vertex attribute consumer.
     * 
     * @param consumer the consumer
     */
    void addVertexAttributeConsumer(BiConsumer<Pair<V, String>, Attribute> consumer);

    /**
     * Remove a vertex attribute consumer.
     * 
     * @param consumer the consumer
     */
    void removeVertexAttributeConsumer(BiConsumer<Pair<V, String>, Attribute> consumer);

    /**
     * Add an edge attribute consumer.
     * 
     * @param consumer the consumer
     */
    void addEdgeAttributeConsumer(BiConsumer<Pair<E, String>, Attribute> consumer);

    /**
     * Remove an edge attribute consumer.
     * 
     * @param consumer the consumer
     */
    void removeEdgeAttributeConsumer(BiConsumer<Pair<E, String>, Attribute> consumer);

    /**
     * Import a graph
     * 
     * @param input the input reader
     * @throws ImportException in case any error occurs, such as I/O or parse error
     */
    void importInput(Reader input);

    /**
     * Import a graph
     * 
     * @param in the input stream
     * @throws ImportException in case any error occurs, such as I/O or parse error
     */
    default void importInput(InputStream in)
    {
        importInput(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    /**
     * Import a graph
     * 
     * @param file the file to read from
     * @throws ImportException in case any error occurs, such as I/O or parse error
     */
    default void importInput(File file)
    {
        try {
            importInput(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ImportException(e);
        }
    }

}
