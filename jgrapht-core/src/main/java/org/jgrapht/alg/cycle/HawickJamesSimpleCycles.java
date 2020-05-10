/*
 * (C) Copyright 2014-2020, by Luiz Kill and Contributors.
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

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * Find all simple cycles of a directed graph using the algorithm described by Hawick and James.
 *
 * <p>
 * See:<br>
 * K. A. Hawick, H. A. James. Enumerating Circuits and Loops in Graphs with Self-Arcs and
 * Multiple-Arcs. Computational Science Technical Note CSTN-013, 2008
 *
 * @param <V> the vertex type.
 * @param <E> the edge type.
 *
 * @author Luiz Kill
 */
public class HawickJamesSimpleCycles<V, E>
    implements
    DirectedSimpleCycles<V, E>
{

    private Graph<V, E> graph;

    private int nVertices = 0;
    private long nCycles = 0;
    private List<List<V>> cycles = null;

    // The main state of the algorithm
    private Integer start = 0;
    private List<Integer>[] Ak = null;
    private List<Integer>[] B = null;
    private boolean[] blocked = null;
    private ArrayDeque<Integer> stack = null;

    // Indexing the vertices
    private V[] iToV = null;
    private Map<V, Integer> vToI = null;

    private int pathLimit = 0;
    private boolean hasLimit = false;
    private Runnable operation;

    /**
     * Create a simple cycle finder with an unspecified graph.
     */
    public HawickJamesSimpleCycles()
    {
    }

    /**
     * Create a simple cycle finder for the specified graph.
     *
     * @param graph the DirectedGraph in which to find cycles.
     *
     * @throws IllegalArgumentException if the graph argument is <code>
     * null</code>.
     */
    public HawickJamesSimpleCycles(Graph<V, E> graph)
        throws IllegalArgumentException
    {
        this.graph = GraphTests.requireDirected(graph, "Graph must be directed");
    }

    @SuppressWarnings("unchecked")
    private void initState()
    {
        nCycles = 0;
        nVertices = graph.vertexSet().size();
        blocked = new boolean[nVertices];
        stack = new ArrayDeque<>(nVertices);

        B = new ArrayList[nVertices];
        for (int i = 0; i < nVertices; i++) {
            B[i] = new ArrayList<>();
        }

        iToV = (V[]) graph.vertexSet().toArray();
        vToI = new HashMap<>();
        for (int i = 0; i < iToV.length; i++) {
            vToI.put(iToV[i], i);
        }

        Ak = buildAdjacencyList();

        stack.clear();
    }

    @SuppressWarnings("unchecked")
    private List<Integer>[] buildAdjacencyList()
    {
        @SuppressWarnings("rawtypes") List[] Ak = new ArrayList[nVertices];
        for (int j = 0; j < nVertices; j++) {
            V v = iToV[j];
            List<V> s = Graphs.successorListOf(graph, v);
            Ak[j] = new ArrayList<Integer>(s.size());

            for (V value : s) {
                Ak[j].add(vToI.get(value));
            }
        }

        return Ak;
    }

    private void clearState()
    {
        Ak = null;
        nVertices = 0;
        blocked = null;
        stack = null;
        iToV = null;
        vToI = null;
        B = null;
        operation = () -> {
        };
    }

    private boolean circuit(Integer v, int steps)
    {
        boolean f = false;

        stack.push(v);
        blocked[v] = true;

        for (Integer w : Ak[v]) {
            if (w < start) {
                continue;
            }

            if (Objects.equals(w, start)) {
                operation.run();

                f = true;
            } else if (!blocked[w]) {
                if (limitReached(steps) || circuit(w, steps + 1)) {
                    f = true;
                }
            }
        }

        if (f) {
            unblock(v);
        } else {
            for (Integer w : Ak[v]) {
                if (w < start) {
                    continue;
                }

                if (!B[w].contains(v)) {
                    B[w].add(v);
                }
            }
        }

        stack.pop();

        return f;
    }

    private void unblock(Integer u)
    {
        blocked[u] = false;

        for (int wPos = 0; wPos < B[u].size(); wPos++) {
            Integer w = B[u].get(wPos);

            int sizeBeforeRemove = B[u].size();
            B[u].removeAll(singletonList(w));
            wPos -= (sizeBeforeRemove - B[u].size());

            if (blocked[w]) {
                unblock(w);
            }
        }
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
        throws IllegalArgumentException
    {
        if (graph == null) {
            throw new IllegalArgumentException("Null graph.");
        }

        initState();
        cycles = new ArrayList<>();
        operation = () -> {
            List<V> cycle = stack.stream().map(v -> iToV[v]).collect(toList());
            Collections.reverse(cycle);
            cycles.add(cycle);
        };
        analyzeCircuits();
        List<List<V>> result = cycles;
        clearState();
        return result;
    }

    /**
     * Print to the standard output all simple cycles without building a list to keep them, thus
     * avoiding high memory consumption when investigating large and much connected graphs.
     */
    public void printSimpleCycles()
    {
        if (graph == null) {
            throw new IllegalArgumentException("Null graph.");
        }

        initState();
        operation = () -> {
            stack.stream().map(i -> iToV[i].toString() + " ").forEach(System.out::print);
            System.out.println();
        };

        analyzeCircuits();
        clearState();
    }

    /**
     * Count the number of simple cycles. It can count up to Long.MAX cycles in a graph.
     * 
     * @return the number of simple cycles
     */
    public long countSimpleCycles()
    {
        if (graph == null) {
            throw new IllegalArgumentException("Null graph.");
        }

        initState();
        nCycles = 0;
        operation = () -> nCycles++;
        analyzeCircuits();
        clearState();
        return nCycles;
    }

    private void analyzeCircuits()
    {
        for (int i = 0; i < nVertices; i++) {
            for (int j = 0; j < nVertices; j++) {
                blocked[j] = false;
                B[j].clear();
            }

            start = vToI.get(iToV[i]);
            circuit(start, 0);
        }
    }

    /**
     * Limits the maximum number of edges in a cycle.
     *
     * @param pathLimit maximum paths.
     */
    public void setPathLimit(int pathLimit)
    {
        this.pathLimit = pathLimit - 1;
        this.hasLimit = true;
    }

    /**
     * This is the default behaviour of the algorithm. It will keep looking as long as there are
     * paths available.
     */
    public void clearPathLimit()
    {
        this.hasLimit = false;
    }

    private boolean limitReached(int steps)
    {
        return hasLimit && steps >= pathLimit;
    }
}
