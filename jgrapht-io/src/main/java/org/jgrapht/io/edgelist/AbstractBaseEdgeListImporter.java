/*
 * (C) Copyright 2019-2019, by Dimitrios Michail and Contributors.
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
package org.jgrapht.io.edgelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jgrapht.alg.util.Pair;

/**
 * Base implementation for an edge list importer. The importer uses consumers to notify interested
 * parties.
 *
 * @author Dimitrios Michail
 */
abstract class AbstractBaseEdgeListImporter
{
    private List<Consumer<Integer>> nodeCountConsumers;
    private List<Consumer<Integer>> edgeCountConsumers;
    private List<BiConsumer<Pair<Integer, Integer>, Double>> edgeConsumers;

    private Map<String, Integer> vertexMap;
    private int nextId;

    /**
     * Constructor
     */
    public AbstractBaseEdgeListImporter()
    {
        this.nodeCountConsumers = new ArrayList<>();
        this.edgeCountConsumers = new ArrayList<>();
        this.edgeConsumers = new ArrayList<>();
        this.vertexMap = new HashMap<>();
        this.nextId = 0;
    }

    /**
     * Add a node count consumer.
     * 
     * @param consumer the consumer
     */
    public void addNodeCountConsumer(Consumer<Integer> consumer)
    {
        nodeCountConsumers.add(consumer);
    }

    /**
     * Remove a node count consumer.
     * 
     * @param consumer the consumer
     */
    public void removeNodeCountConsumer(Consumer<Integer> consumer)
    {
        nodeCountConsumers.remove(consumer);
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
     * Add an edge consumer.
     * 
     * @param consumer the consumer
     */
    public void addEdgeConsumer(BiConsumer<Pair<Integer, Integer>, Double> consumer)
    {
        edgeConsumers.add(consumer);
    }

    /**
     * Remove an edge consumer.
     * 
     * @param consumer the consumer
     */
    public void removeEdgeConsumer(BiConsumer<Pair<Integer, Integer>, Double> consumer)
    {
        edgeConsumers.remove(consumer);
    }

    /**
     * Notify for the node count.
     * 
     * @param nodeCount the number of nodes in the graph
     */
    protected void notifyNodeCount(Integer nodeCount)
    {
        nodeCountConsumers.forEach(c -> c.accept(nodeCount));
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
     * Notify for an edge.
     * 
     * @param source the edge source
     * @param target the edge target
     * @param weight the edge weight or null
     */
    protected void notifyEdge(Integer source, Integer target, Double weight)
    {
        edgeConsumers.forEach(c -> c.accept(Pair.of(source, target), weight));
    }

    /**
     * Map a vertex identifier to an integer.
     * 
     * @param id the vertex identifier
     * @return the integer
     */
    protected Integer mapVertexToInteger(String id)
    {
        return vertexMap.computeIfAbsent(id, (keyId) -> {
            return nextId++;
        });
    }

}
