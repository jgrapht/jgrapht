/*
 * (C) Copyright 2014-2018, by Luiz Kill and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import static java.util.Collections.frequency;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
public class HawickJamesSimpleCycles<V, E> implements DirectedSimpleCycles<V, E> {

    private Graph<V, E> graph;
    private long nCycles;
    private List<List<V>> cycles;

    // The main state of the algorithm
    private Integer start;
    private List<List<Integer>> adjacencyList;
    private List<List<Integer>> blockedVertices;
    private List<Boolean> blocked;
    private ArrayDeque<Integer> stack;

    // Indexing the vertices
    private List<V> iToV;
    private Map<V, Integer> vToI;

    private Runnable operation;
    private int pathLimit = 0;
    private boolean hasLimit;

    /**
     * Create a simple cycle finder with an unspecified graph.
     */
    public HawickJamesSimpleCycles() { }

    /**
     * Create a simple cycle finder for the specified graph.
     *
     * @param graph the DirectedGraph in which to find cycles.
     *
     * @throws IllegalArgumentException if the graph argument is <code>
     * null</code>.
     */
    public HawickJamesSimpleCycles(Graph<V, E> graph) throws IllegalArgumentException {
        setGraph(graph);
    }

    /**
     * Set the graph
     *
     * @param graph graph
     */
    public void setGraph(Graph<V, E> graph) {
        this.graph = GraphTests.requireDirected(graph, "Graph must be directed");
    }

    /**
     * Get the graph
     *
     * @return graph
     */
    public Graph<V, E> getGraph() {
        return graph;
    }

    private void initState() {
        iToV = new ArrayList<>(graph.vertexSet());
        vToI = iToV.stream().collect(toMap(x -> x, iToV::indexOf));

        blocked = new ArrayList<>(iToV.size());
        blockedVertices = iToV.stream().map(v -> new ArrayList<Integer>()).collect(toList());

        stack = new ArrayDeque<>(iToV.size());
        adjacencyList = buildAdjacencyList();

        stack.clear();
    }

    private List<List<Integer>> buildAdjacencyList() {
        return iToV.stream()
            .map(vertex -> Graphs.successorListOf(graph, vertex))
            .map(successors -> successors.stream().map(s -> vToI.get(s)).collect(toList()))
            .collect(toList());
    }

    private void unblock(Integer u) {
        blocked.add(u, false);

        for (int wPos = 0; wPos < blockedVertices.get(u).size(); wPos++) {
            Integer w = blockedVertices.get(u).get(wPos);

            wPos -= frequency(blockedVertices.get(u), u);
            blockedVertices.get(u).removeAll(singletonList(u));

            if (blocked.get(w)) {
                unblock(w);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<List<V>> findSimpleCycles() throws IllegalArgumentException {
        cycles = new ArrayList<>();
        circuitAnalysis(() -> cycles.add(stack.stream().map(vi -> iToV.get(vi)).collect(toList())));
        return cycles;
    }

    /**
     * Print to the standard output all simple cycles without building a list to keep them, thus
     * avoiding high memory consumption when investigating large and much connected graphs.
     */
    public void printSimpleCycles() {
        circuitAnalysis(() -> {
            stack.stream().map(i -> iToV.get(i).toString() + " ").forEach(System.out::print);
            System.out.println();
        });
    }

    /**
     * Count the number of simple cycles. It can count up to Long.MAX cycles in a graph.
     *
     * @return the number of simple cycles
     */
    public long countSimpleCycles() {
        nCycles = 0;
        circuitAnalysis(() -> nCycles++);
        return nCycles;
    }

    private boolean circuit(Integer vertex, int paths) {
        boolean found = false;

        stack.push(vertex);
        blocked.add(vertex, true);

        for (Integer adjacent : adjacencyList.get(vertex)) {
            if (adjacent < start) {
                continue;
            }

            if (Objects.equals(adjacent, start)) { // Cycle found
                operation.run();
                found = true;
            } else if (!blocked.get(adjacent)) {
                if (limitReached(paths) || circuit(adjacent, paths + 1)) {
                    found = true;
                }
            }
        }

        if (found) {
            unblock(vertex);
        } else {
            adjacencyList.get(vertex).stream()
                .filter(adj -> adj >= start && !blockedVertices.get(adj).contains(vertex))
                .forEach(adj -> blockedVertices.get(adj).add(vertex));
        }

        stack.pop();
        return found;
    }

    private void circuitAnalysis(Runnable operation) {
        if (graph == null) {
            throw new IllegalArgumentException("Null graph.");
        }

        this.operation = operation;
        initState();

        IntStream.range(0, iToV.size()).forEach(i -> {
            blocked.clear();
            iToV.forEach(j -> blocked.add(false));
            IntStream.range(0, iToV.size()).forEach(j -> blockedVertices.get(j).clear());

            start = i;
            circuit(start, 0);
        });
    }

    /**
     * Limits the maximum number of edges in a cycle.
     *
     * @param pathLimit maximum paths.
     */
    public void setPathLimit(int pathLimit) {
        this.pathLimit = pathLimit - 1;
        this.hasLimit = true;
    }

    /**
     * This is the default behaviour of the algorithm.
     * It will keep looking as long as there are paths available.
     */
    public void unlimitedPaths() {
        this.hasLimit = false;
    }

    private boolean limitReached(int steps) {
        return hasLimit && steps >= pathLimit;
    }
}
