/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;

/**
 * A random walk iterator.
 * 
 * "Given a graph and a starting point, we select a neighbor of it at random, and move to this
 * neighbor; then we select a neighbor of this point at random, and move to it etc. The (random)
 * sequence of points selected this way is a random walk on the graph." This very simple definition,
 * together with a comprehensive survey can be found at: "Lovász, L. (1993). Random walks on graphs:
 * A survey. Combinatorics, Paul erdos is eighty, 2(1), 1-46."
 * 
 * In its default variant the probability of selecting an outgoing edge is one over the (out) degree
 * of the vertex. In case the user requests a weighted walk, then the probability of each edge is
 * equal to its weight divided by the total weight of all outgoing edges. The walk can also be
 * bounded by a maximum number of hops (edges traversed). The iterator returns
 * {@link NoSuchElementException} when this bound is reached.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class RandomWalkVertexIterator<V, E>
    implements
    Iterator<V>
{
    private final Random rng;
    private final Graph<V, E> graph;
    private final boolean weighted;
    private final Map<V, Double> outEdgesTotalWeight;
    private final long maxHops;
    private long hops;
    private V nextVertex;

    /**
     * Create a new iterator
     * 
     * @param graph the graph
     * @param vertex the starting vertex
     */
    public RandomWalkVertexIterator(Graph<V, E> graph, V vertex)
    {
        this(graph, vertex, Long.MAX_VALUE, false, new Random());
    }

    /**
     * Create a new iterator
     * 
     * @param graph the graph
     * @param vertex the starting vertex
     * @param maxHops maximum hops to perform during the walk
     */
    public RandomWalkVertexIterator(Graph<V, E> graph, V vertex, long maxHops)
    {
        this(graph, vertex, maxHops, false, new Random());
    }

    /**
     * Create a new iterator
     * 
     * @param graph the graph
     * @param vertex the starting vertex
     * @param maxHops maximum hops to perform during the walk
     * @param weighted whether to perform a weighted random walk (compute probabilities based on the
     *        edge weights)
     * @param rng the random number generator
     */
    public RandomWalkVertexIterator(
        Graph<V, E> graph, V vertex, long maxHops, boolean weighted, Random rng)
    {
        this.graph = Objects.requireNonNull(graph);
        this.weighted = weighted;
        this.outEdgesTotalWeight = new HashMap<>();
        this.hops = 0;
        this.nextVertex = Objects.requireNonNull(vertex);
        if (!graph.containsVertex(vertex)) {
            throw new IllegalArgumentException("Random walk must start at a graph vertex");
        }
        this.maxHops = maxHops;
        this.rng = rng;
    }

    @Override
    public boolean hasNext()
    {
        return nextVertex != null;
    }

    @Override
    public V next()
    {
        if (nextVertex == null) {
            throw new NoSuchElementException();
        }
        V value = nextVertex;
        computeNext();
        return value;
    }

    private void computeNext()
    {
        if (hops >= maxHops) {
            nextVertex = null;
            return;
        }

        hops++;
        if (graph.outDegreeOf(nextVertex) == 0) {
            nextVertex = null;
            return;
        }

        E e = null;
        if (weighted) {
            double outEdgesWeight = outEdgesTotalWeight.computeIfAbsent(nextVertex, v -> {
                return graph
                    .outgoingEdgesOf(v).stream()
                    .collect(Collectors.summingDouble(graph::getEdgeWeight));
            });
            double p = outEdgesWeight * rng.nextDouble();
            double cumulativeP = 0d;
            for (E curEdge : graph.outgoingEdgesOf(nextVertex)) {
                cumulativeP += graph.getEdgeWeight(curEdge);
                if (p <= cumulativeP) {
                    e = curEdge;
                    break;
                }
            }
        } else {
            List<E> outEdges = new ArrayList<>(graph.outgoingEdgesOf(nextVertex));
            e = outEdges.get(rng.nextInt(outEdges.size()));
        }
        nextVertex = Graphs.getOppositeVertex(graph, e, nextVertex);
    }

}
