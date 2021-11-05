/*
 * (C) Copyright 2018-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.traverse;

import org.jgrapht.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * A maximum cardinality search iterator for an undirected graph.
 * <p>
 * For every vertex in graph its cardinality is defined as the number of its neighbours, which have
 * been already visited by this iterator. Iterator chooses vertex with the maximum cardinality,
 * breaking ties arbitrarily. For more information of maximum cardinality search see: Berry, A.,
 * Blair, J., Heggernes, P. et al. Algorithmica (2004) 39: 287.
 * https://doi.org/10.1007/s00453-004-1084-3
 * <a href="http://www.ii.uib.no/~pinar/MCS-M.pdf"><i>Maximum Cardinality Search for Computing
 * Minimal Triangulations</i></a>.
 * <p>
 * For this iterator to work correctly the graph must not be modified during iteration. Currently
 * there are no means to ensure that, nor to fail-fast. The results of such modifications are
 * undefined.
 * <p>
 * Note: only vertex events are fired by this iterator.
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 * @author Timofey Chudakov
 */
public class MaximumCardinalityIterator<V, E>
    extends
    AbstractGraphIterator<V, E>
{
    /**
     * The maximum index of non-empty set in {@code buckets}.
     */
    private int maxCardinality;
    /**
     * Number of unvisited vertices.
     */
    private int remainingVertices;
    /**
     * Contains current vertex.
     */
    private V current;
    /**
     * Disjoint sets of vertices of the graph, indexed by the cardinalities of already visited
     * neighbours.
     */
    private ArrayList<Set<V>> buckets;
    /**
     * Maps every vertex to its cardinality.
     */
    private Map<V, Integer> cardinalityMap;

    /**
     * Creates a maximum cardinality iterator for the {@code graph}.
     *
     * @param graph the graph to be iterated.
     */
    public MaximumCardinalityIterator(Graph<V, E> graph)
    {
        super(graph);
        remainingVertices = graph.vertexSet().size();
        if (remainingVertices > 0) {
            GraphTests.requireUndirected(graph);
            buckets = new ArrayList<>(Collections.nCopies(graph.vertexSet().size(), null));
            buckets.set(0, new LinkedHashSet<>(graph.vertexSet()));
            cardinalityMap = CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());
            for (V v : graph.vertexSet()) {
                cardinalityMap.put(v, 0);
            }
            maxCardinality = 0;
        }
    }

    /**
     * Checks whether there exists unvisited vertex.
     *
     * @return true if there exists unvisited vertex.
     */
    @Override
    public boolean hasNext()
    {
        if (current != null) {
            return true;
        }
        current = advance();
        if (current != null && nListeners != 0) {
            fireVertexTraversed(createVertexTraversalEvent(current));
        }
        return current != null;
    }

    /**
     * Returns the next vertex in the ordering.
     *
     * @return the next vertex in the ordering.
     */
    @Override
    public V next()
    {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        V result = current;
        current = null;
        if (nListeners != 0) {
            fireVertexFinished(createVertexTraversalEvent(result));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always returns true since this iterator doesn't care about connected components.
     */
    @Override
    public boolean isCrossComponentTraversal()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Trying to disable the cross components nature of this iterator will result into throwing a
     * {@link IllegalArgumentException}.
     */
    @Override
    public void setCrossComponentTraversal(boolean crossComponentTraversal)
    {
        if (!crossComponentTraversal) {
            throw new IllegalArgumentException("Iterator is always cross-component");
        }
    }

    /**
     * Retrieves a vertex from the {@code buckets} with the maximum cardinality and returns it.
     *
     * @return vertex retrieved from {@code buckets}.
     */
    private V advance()
    {
        if (remainingVertices > 0) {
            Set<V> bucket = buckets.get(maxCardinality);
            V vertex = bucket.iterator().next();
            removeFromBucket(vertex);
            if (bucket.isEmpty()) {
                buckets.set(maxCardinality, null);
                do {
                    --maxCardinality;
                } while (maxCardinality >= 0 && buckets.get(maxCardinality) == null);
            }
            updateNeighbours(vertex);
            --remainingVertices;
            return vertex;
        } else {
            return null;
        }
    }

    /**
     * Removes {@code vertex} from the bucket it was contained in.
     *
     * @param vertex the vertex, which has to be removed from the bucket it was contained in.
     * @return the cardinality of the removed vertex or -1, if the vertex wasn't contained in any
     *         bucket.
     */
    private int removeFromBucket(V vertex)
    {
        if (cardinalityMap.containsKey(vertex)) {
            int cardinality = cardinalityMap.get(vertex);
            buckets.get(cardinality).remove(vertex);
            cardinalityMap.remove(vertex);
            if (buckets.get(cardinality).isEmpty()) {
                buckets.set(cardinality, null);
            }
            return cardinality;
        }
        return -1;
    }

    /**
     * Adds the {@code vertex} to the bucket with the given {@code cardinality}.
     *
     * @param vertex the vertex, which has to be added to the the bucket.
     * @param cardinality the cardinality of the destination bucket.
     */
    private void addToBucket(V vertex, int cardinality)
    {
        cardinalityMap.put(vertex, cardinality);
        if (buckets.get(cardinality) == null) {
            buckets.set(cardinality, new LinkedHashSet<>());
        }
        buckets.get(cardinality).add(vertex);
    }

    /**
     * Increments the cardinalities of the neighbours of the {@code vertex} by 1. If the maximum
     * cardinality increases, increments {@code maxCardinality} by 1.
     *
     * @param vertex the vertex whose neighbours are to be updated.
     */
    private void updateNeighbours(V vertex)
    {
        Set<V> processed = new HashSet<>();
        for (E edge : graph.edgesOf(vertex)) {
            V opposite = Graphs.getOppositeVertex(graph, edge, vertex);
            if (cardinalityMap.containsKey(opposite) && !processed.contains(opposite)) {
                processed.add(opposite);
                addToBucket(opposite, removeFromBucket(opposite) + 1);
            }
        }
        if (maxCardinality < graph.vertexSet().size() && maxCardinality >= 0
            && buckets.get(maxCardinality + 1) != null)
        {
            ++maxCardinality;
        }
    }
}
