/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * Collection of helper methods related to cycles.
 *
 * @author Dimitrios Michail
 */
public abstract class Cycles
{

    /**
     * Transform a simple cycle from an edge set representation to a graph path. A simple cycle
     * contains vertices with degrees either zero or two. This method treats directed graphs as
     * undirected.
     *
     * @param graph the graph
     * @param cycle the simple cycle
     * @return the cycle as a graph path
     * @param <V> graph vertex type
     * @param <E> graph edge type
     * @throws IllegalArgumentException if the provided edge set is not a simple cycle (circuit)
     */
    public static <V, E> GraphPath<V, E> simpleCycleToGraphPath(Graph<V, E> graph, List<E> cycle)
    {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(cycle, "Cycle cannot be null");

        if (cycle.isEmpty()) {
            return null;
        }

        // index
        Map<V, E> firstEdge = new HashMap<>();
        Map<V, E> secondEdge = new HashMap<>();
        for (E e : cycle) {
            V s = graph.getEdgeSource(e);

            if (!firstEdge.containsKey(s)) {
                firstEdge.put(s, e);
            } else {
                if (!secondEdge.containsKey(s)) {
                    secondEdge.put(s, e);
                } else {
                    throw new IllegalArgumentException("Not a simple cycle");
                }
            }

            V t = graph.getEdgeTarget(e);

            if (!firstEdge.containsKey(t)) {
                firstEdge.put(t, e);
            } else {
                if (!secondEdge.containsKey(t)) {
                    secondEdge.put(t, e);
                } else {
                    throw new IllegalArgumentException("Not a simple cycle");
                }
            }
        }

        // traverse
        List<E> edges = new ArrayList<>();
        double weight = 0d;
        E e = cycle.stream().findAny().get();
        edges.add(e);
        weight += graph.getEdgeWeight(e);
        V start = graph.getEdgeSource(e);

        V cur = Graphs.getOppositeVertex(graph, e, start);
        while (!cur.equals(start)) {
            E fe = firstEdge.get(cur);
            if (fe == null) {
                throw new IllegalArgumentException("Not a simple cycle");
            }
            E se = secondEdge.get(cur);
            if (se == null) {
                throw new IllegalArgumentException("Not a simple cycle");
            }
            if (fe.equals(e)) {
                e = se;
            } else if (se.equals(e)) {
                e = fe;
            } else {
                throw new IllegalArgumentException("Not a simple cycle");
            }

            edges.add(e);
            weight += graph.getEdgeWeight(e);
            cur = Graphs.getOppositeVertex(graph, e, cur);
        }

        // return result
        return new GraphWalk<>(graph, start, start, edges, weight);
    }

}
