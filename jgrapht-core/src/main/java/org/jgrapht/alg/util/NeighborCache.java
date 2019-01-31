/*
 * (C) Copyright 2017-2018, by Szabolcs Besenyei and Contributors.
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
package org.jgrapht.alg.util;

import org.jgrapht.*;
import org.jgrapht.event.*;
import org.jgrapht.util.*;

import java.util.*;
import java.util.function.*;

/**
 * Maintains a cache of each vertex's neighbors. While lists of neighbors can be obtained from
 * {@link Graphs}, they are re-calculated at each invocation by walking a vertex's incident edges,
 * which becomes inordinately expensive when performed often.
 * 
 * <p>
 * The cache also keeps track of successors and predecessors for each vertex. This means that the
 * result of the union of calling predecessorsOf(v) and successorsOf(v) is equal to the result of
 * calling neighborsOf(v) for a given vertex v.
 * 
 * @param <V> the vertex type
 * @param <E> the edge type
 * 
 * @author Szabolcs Besenyei
 */
public class NeighborCache<V, E>
    implements
    GraphListener<V, E>
{
    private Map<V, Neighbors<V>> successors = new HashMap<>();
    private Map<V, Neighbors<V>> predecessors = new HashMap<>();
    private Map<V, Neighbors<V>> neighbors = new HashMap<>();

    private Graph<V, E> graph;

    /**
     * Constructor
     * 
     * @param graph the input graph
     * @throws NullPointerException if the input graph is null
     */
    public NeighborCache(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph);
    }

    /**
     * Returns the unique predecessors of the given vertex if it exists in the cache, otherwise it
     * is initialized.
     * 
     * @param v the given vertex
     * @return the unique predecessors of the given vertex
     */
    public Set<V> predecessorsOf(V v)
    {
        return fetch(v, predecessors, k -> new Neighbors<>(Graphs.predecessorListOf(graph, v)));
    }

    /**
     * Returns the unique successors of the given vertex if it exists in the cache, otherwise it is
     * initialized.
     * 
     * @param v the given vertex
     * @return the unique successors of the given vertex
     */
    public Set<V> successorsOf(V v)
    {
        return fetch(v, successors, k -> new Neighbors<>(Graphs.successorListOf(graph, v)));
    }

    /**
     * Returns the unique neighbors of the given vertex if it exists in the cache, otherwise it is
     * initialized.
     * 
     * @param v the given vertex
     * @return the unique neighbors of the given vertex
     */
    public Set<V> neighborsOf(V v)
    {
        return fetch(v, neighbors, k -> new Neighbors<>(Graphs.neighborListOf(graph, v)));
    }

    /**
     * Returns a list of vertices which are adjacent to a specified vertex. If the graph is a
     * multigraph, vertices may appear more than once in the returned list. Because a list of
     * neighbors can not be efficiently maintained, it is reconstructed on every invocation, by
     * duplicating entries in the neighbor set. It is thus more efficient to use
     * {@link #neighborsOf} unless duplicate neighbors are important.
     *
     * @param v the vertex whose neighbors are desired
     *
     * @return all neighbors of the specified vertex
     */
    public List<V> neighborListOf(V v)
    {
        Neighbors<V> nbrs = neighbors.get(v);
        if (nbrs == null) {
            nbrs = new Neighbors<>(Graphs.neighborListOf(graph, v));
            neighbors.put(v, nbrs);
        }
        return nbrs.getNeighborList();
    }

    private Set<V> fetch(V vertex, Map<V, Neighbors<V>> map, Function<V, Neighbors<V>> func)
    {
        return map.computeIfAbsent(vertex, func).getNeighbors();
    }

    @Override
    public void edgeAdded(GraphEdgeChangeEvent<V, E> e)
    {
        assert e
            .getSource() == this.graph : "This NeighborCache is added as a listener to a graph other than the one specified during the construction of this NeighborCache!";

        V source = e.getEdgeSource();
        V target = e.getEdgeTarget();

        if (successors.containsKey(source)) {
            successors.get(source).addNeighbor(target);
        }

        if (predecessors.containsKey(target)) {
            predecessors.get(target).addNeighbor(source);
        }

        if (neighbors.containsKey(source)) {
            neighbors.get(source).addNeighbor(target);
        }

        if (neighbors.containsKey(target)) {
            neighbors.get(target).addNeighbor(source);
        }
    }

    @Override
    public void edgeRemoved(GraphEdgeChangeEvent<V, E> e)
    {
        assert e
            .getSource() == this.graph : "This NeighborCache is added as a listener to a graph other than the one specified during the construction of this NeighborCache!";

        V source = e.getEdgeSource();
        V target = e.getEdgeTarget();

        if (successors.containsKey(source)) {
            successors.get(source).removeNeighbor(target);
        }

        if (predecessors.containsKey(target)) {
            predecessors.get(target).removeNeighbor(source);
        }

        if (neighbors.containsKey(source)) {
            neighbors.get(source).removeNeighbor(target);
        }

        if (neighbors.containsKey(target)) {
            neighbors.get(target).removeNeighbor(source);
        }
    }

    @Override
    public void vertexAdded(GraphVertexChangeEvent<V> e)
    {
        // Nothing to cache until there are edges
    }

    @Override
    public void vertexRemoved(GraphVertexChangeEvent<V> e)
    {
        assert e
            .getSource() == this.graph : "This NeighborCache is added as a listener to a graph other than the one specified during the construction of this NeighborCache!";

        successors.remove(e.getVertex());
        predecessors.remove(e.getVertex());
        neighbors.remove(e.getVertex());
    }

    /**
     * Stores cached neighbors for a single vertex. Includes support for live neighbor sets and
     * duplicate neighbors.
     */
    static class Neighbors<V>
    {
        private Map<V, ModifiableInteger> neighborCounts = new LinkedHashMap<>();

        // TODO could eventually make neighborSet modifiable, resulting
        // in edge removals from the graph
        private Set<V> neighborSet = Collections.unmodifiableSet(neighborCounts.keySet());

        public Neighbors(Collection<V> neighbors)
        {
            // add all current neighbors
            for (V neighbor : neighbors) {
                addNeighbor(neighbor);
            }
        }

        public void addNeighbor(V v)
        {
            ModifiableInteger count = neighborCounts.get(v);
            if (count == null) {
                count = new ModifiableInteger(1);
                neighborCounts.put(v, count);
            } else {
                count.increment();
            }
        }

        public void removeNeighbor(V v)
        {
            ModifiableInteger count = neighborCounts.get(v);
            if (count == null) {
                throw new IllegalArgumentException(
                    "Attempting to remove a neighbor that wasn't present");
            }

            count.decrement();
            if (count.getValue() == 0) {
                neighborCounts.remove(v);
            }
        }

        public Set<V> getNeighbors()
        {
            return neighborSet;
        }

        public List<V> getNeighborList()
        {
            List<V> neighbors = new ArrayList<>();
            for (Map.Entry<V, ModifiableInteger> entry : neighborCounts.entrySet()) {
                V v = entry.getKey();
                int count = entry.getValue().intValue();
                for (int i = 0; i < count; i++) {
                    neighbors.add(v);
                }
            }
            return neighbors;
        }

        @Override
        public String toString()
        {
            return neighborSet.toString();
        }
    }
}
