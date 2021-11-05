/*
 * (C) Copyright 2013-2021, by Nikolay Ognyanov and Contributors.
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
import org.jgrapht.alg.interfaces.*;

import java.util.*;

/**
 * Find a cycle basis of an undirected graph using a variant of Paton's algorithm.
 * 
 * <p>
 * See:<br>
 * K. Paton, An algorithm for finding a fundamental set of cycles for an undirected linear graph,
 * Comm. ACM 12 (1969), pp. 514-518.
 * 
 * <p>
 * Note that Paton's algorithm produces a fundamental cycle basis while this implementation produces
 * a <a href=
 * "https://en.wikipedia.org/wiki/Cycle_space#Fundamental_and_weakly_fundamental_bases">weakly
 * fundamental cycle basis</a>. A cycle basis is called weakly fundamental if there exists a linear
 * ordering of the cycles in a cycle basis such that each cycle includes at least one edge that is
 * not part of any previous cycle. Every fundamental cycle basis is weakly fundamental (for all
 * linear orderings) but not necessarily vice versa.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Nikolay Ognyanov
 */
public class PatonCycleBase<V, E>
    implements
    CycleBasisAlgorithm<V, E>
{
    private Graph<V, E> graph;

    /**
     * Create a cycle base finder for the specified graph.
     *
     * @param graph the input graph
     * @throws IllegalArgumentException if the graph argument is <code>null</code> or the graph is
     *         not undirected
     */
    public PatonCycleBase(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireUndirected(graph);
    }

    /**
     * Return an undirected cycle basis of a graph. Works only for undirected graphs which do not
     * have multiple (parallel) edges.
     * 
     * @return an undirected cycle basis
     * @throws IllegalArgumentException if the graph is not undirected
     * @throws IllegalArgumentException if the graph contains multiple edges between two vertices
     */
    @Override
    public CycleBasis<V, E> getCycleBasis()
    {
        GraphTests.requireUndirected(graph);

        if (GraphTests.hasMultipleEdges(graph)) {
            throw new IllegalArgumentException("Graphs with multiple edges not supported");
        }

        Map<V, Map<V, E>> used = new HashMap<>();
        Map<V, E> parent = new HashMap<>();
        ArrayDeque<V> stack = new ArrayDeque<>();

        Set<List<E>> cycles = new LinkedHashSet<>();
        int totalLength = 0;
        double totalWeight = 0d;

        for (V root : graph.vertexSet()) {
            // Loop over the connected
            // components of the graph.
            if (parent.containsKey(root)) {
                continue;
            }

            // Free some memory in case of
            // multiple connected components.
            used.clear();

            // Prepare to walk the spanning tree.
            parent.put(root, null);
            used.put(root, new HashMap<>());
            stack.push(root);

            // Do the walk. It is a BFS with
            // a LIFO instead of the usual
            // FIFO. Thus it is easier to
            // find the cycles in the tree.
            while (!stack.isEmpty()) {
                V current = stack.pop();
                Map<V, E> currentUsed = used.get(current);
                for (E e : graph.edgesOf(current)) {
                    V neighbor = Graphs.getOppositeVertex(graph, e, current);
                    if (!used.containsKey(neighbor)) {
                        // found a new node
                        parent.put(neighbor, e);
                        Map<V, E> neighbourUsed = new HashMap<>();
                        neighbourUsed.put(current, e);
                        used.put(neighbor, neighbourUsed);
                        stack.push(neighbor);
                    } else if (neighbor.equals(current)) {
                        // found a self loop
                        List<E> cycle = new ArrayList<>();
                        cycle.add(e);
                        totalWeight += graph.getEdgeWeight(e);
                        totalLength += 1;
                        cycles.add(cycle);
                    } else if (!currentUsed.containsKey(neighbor)) {
                        // found a cycle
                        Map<V, E> neighbourUsed = used.get(neighbor);

                        double weight = 0d;
                        List<E> cycle = new ArrayList<>();

                        cycle.add(e);
                        weight += graph.getEdgeWeight(e);

                        V v = current;
                        while (!neighbourUsed.containsKey(v)) {
                            E p = parent.get(v);
                            cycle.add(p);
                            weight += graph.getEdgeWeight(p);
                            v = Graphs.getOppositeVertex(graph, p, v);
                        }
                        E a = neighbourUsed.get(v);
                        cycle.add(a);
                        weight += graph.getEdgeWeight(a);

                        neighbourUsed.put(current, e);

                        cycles.add(cycle);
                        totalLength += cycle.size();
                        totalWeight += weight;
                    }
                }
            }
        }

        return new CycleBasisImpl<V, E>(graph, cycles, totalLength, totalWeight);
    }
}
