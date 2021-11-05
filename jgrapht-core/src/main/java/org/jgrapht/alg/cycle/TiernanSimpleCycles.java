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

import java.util.*;

/**
 * Find all simple cycles of a directed graph using the Tiernan's algorithm.
 *
 * <p>
 * See:<br>
 * J.C.Tiernan An Efficient Search Algorithm Find the Elementary Circuits of a Graph.,
 * Communications of the ACM, vol.13, 12, (1970), pp. 722 - 726.
 *
 * @param <V> the vertex type.
 * @param <E> the edge type.
 *
 * @author Nikolay Ognyanov
 */
public class TiernanSimpleCycles<V, E>
    implements
    DirectedSimpleCycles<V, E>
{
    private Graph<V, E> graph;

    /**
     * Create a simple cycle finder with an unspecified graph.
     */
    public TiernanSimpleCycles()
    {
    }

    /**
     * Create a simple cycle finder for the specified graph.
     *
     * @param graph - the DirectedGraph in which to find cycles.
     *
     * @throws IllegalArgumentException if the graph argument is <code>
     * null</code>.
     */
    public TiernanSimpleCycles(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireDirected(graph, "Graph must be directed");
    }

    /**
     * Get the graph
     * 
     * @return graph
     */
    public Graph<V, E> getGraph()
    {
        return graph;
    }

    /**
     * Set the graph
     * 
     * @param graph graph
     */
    public void setGraph(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireDirected(graph, "Graph must be directed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<List<V>> findSimpleCycles()
    {
        if (graph == null) {
            throw new IllegalArgumentException("Null graph.");
        }
        Map<V, Integer> indices = new HashMap<>();
        List<V> path = new ArrayList<>();
        Set<V> pathSet = new HashSet<>();
        Map<V, Set<V>> blocked = new HashMap<>();
        List<List<V>> cycles = new LinkedList<>();

        int index = 0;
        for (V v : graph.vertexSet()) {
            blocked.put(v, new HashSet<>());
            indices.put(v, index++);
        }

        Iterator<V> vertexIterator = graph.vertexSet().iterator();
        if (!vertexIterator.hasNext()) {
            return cycles;
        }

        V startOfPath;
        V endOfPath;
        V temp;
        int endIndex;
        boolean extensionFound;

        endOfPath = vertexIterator.next();
        path.add(endOfPath);
        pathSet.add(endOfPath);

        // A mostly straightforward implementation
        // of the algorithm. Except that there is
        // no real need for the state machine from
        // the original paper.
        while (true) {
            // path extension
            do {
                extensionFound = false;
                for (E e : graph.outgoingEdgesOf(endOfPath)) {
                    V n = graph.getEdgeTarget(e);
                    int cmp = indices.get(n).compareTo(indices.get(path.get(0)));
                    if ((cmp > 0) && !pathSet.contains(n) && !blocked.get(endOfPath).contains(n)) {
                        path.add(n);
                        pathSet.add(n);
                        endOfPath = n;
                        extensionFound = true;
                        break;
                    }
                }
            } while (extensionFound);

            // circuit confirmation
            startOfPath = path.get(0);
            if (graph.containsEdge(endOfPath, startOfPath)) {
                List<V> cycle = new ArrayList<>(path);
                cycles.add(cycle);
            }

            // vertex closure
            if (path.size() > 1) {
                blocked.get(endOfPath).clear();
                endIndex = path.size() - 1;
                path.remove(endIndex);
                pathSet.remove(endOfPath);
                --endIndex;
                temp = endOfPath;
                endOfPath = path.get(endIndex);
                blocked.get(endOfPath).add(temp);
                continue;
            }

            // advance initial index
            if (vertexIterator.hasNext()) {
                path.clear();
                pathSet.clear();
                endOfPath = vertexIterator.next();
                path.add(endOfPath);
                pathSet.add(endOfPath);
                for (V vt : blocked.keySet()) {
                    blocked.get(vt).clear();
                }
                continue;
            }

            // terminate
            break;
        }

        return cycles;
    }
}
