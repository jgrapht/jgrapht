/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.clique;

import org.jgrapht.*;
import org.jgrapht.alg.color.*;
import org.jgrapht.alg.cycle.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.traverse.*;

import java.util.*;

/**
 * Calculates a <a href = "http://mathworld.wolfram.com/MaximumClique.html">maximum cardinality
 * clique</a> in a <a href="https://en.wikipedia.org/wiki/Chordal_graph">chordal graph</a>. A
 * chordal graph is a simple graph in which all
 * <a href="http://mathworld.wolfram.com/GraphCycle.html"> cycles</a> of four or more vertices have
 * a <a href="http://mathworld.wolfram.com/CycleChord.html"> chord</a>. A chord is an edge that is
 * not part of the cycle but connects two vertices of the cycle.
 *
 * To compute the clique, this implementation relies on the {@link ChordalityInspector} to compute a
 * <a href=
 * "https://en.wikipedia.org/wiki/Chordal_graph#Perfect_elimination_and_efficient_recognition">
 * perfect elimination order</a>.
 *
 * The maximum clique for a chordal graph is computed in $\mathcal{O}(|V| + |E|)$ time.
 *
 * All the methods in this class are invoked in a lazy fashion, meaning that computations are only
 * started once the method gets invoked.
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 *
 * @author Timofey Chudakov
 */
public class ChordalGraphMaxCliqueFinder<V, E>
    implements
    CliqueAlgorithm<V>
{
    private final Graph<V, E> graph;
    private final ChordalityInspector.IterationOrder iterationOrder;

    private Clique<V> maximumClique;
    private boolean isChordal = true;

    /**
     * Creates a new ChordalGraphMaxCliqueFinder instance. The {@link ChordalityInspector} used in
     * this implementation uses the default {@link MaximumCardinalityIterator} iterator.
     *
     * @param graph graph
     */
    public ChordalGraphMaxCliqueFinder(Graph<V, E> graph)
    {
        this(graph, ChordalityInspector.IterationOrder.MCS);
    }

    /**
     * Creates a new ChordalGraphMaxCliqueFinder instance. The {@link ChordalityInspector} used in
     * this implementation uses either the {@link MaximumCardinalityIterator} iterator or the
     * {@link LexBreadthFirstIterator} iterator, depending on the parameter {@code iterationOrder}.
     *
     * @param graph graph
     * @param iterationOrder constant which defines iterator to be used by the
     *        {@code ChordalityInspector} in this implementation.
     */
    public ChordalGraphMaxCliqueFinder(
        Graph<V, E> graph, ChordalityInspector.IterationOrder iterationOrder)
    {
        this.graph = Objects.requireNonNull(graph);
        this.iterationOrder = Objects.requireNonNull(iterationOrder);
    }

    /**
     * Lazily computes some maximum clique of the {@code graph}.
     */
    private void lazyComputeMaximumClique()
    {
        if (maximumClique == null && isChordal) {
            ChordalGraphColoring<V, E> cgc = new ChordalGraphColoring<>(graph, iterationOrder);
            VertexColoringAlgorithm.Coloring<V> coloring = cgc.getColoring();
            List<V> perfectEliminationOrder = cgc.getPerfectEliminationOrder();
            if (coloring == null) {
                isChordal = false; // Graph isn't chordal
                return;
            }
            // finds the vertex with the maximum cardinality predecessor list
            Map<V, Integer> vertexInOrder = getVertexInOrder(perfectEliminationOrder);
            Map.Entry<V,
                Integer> maxEntry = coloring
                    .getColors().entrySet().stream().max(Comparator.comparing(Map.Entry::getValue))
                    .orElse(null);
            if (maxEntry == null) {
                maximumClique = new CliqueImpl<>(Collections.emptySet());
            } else {
                Set<V> cliqueSet = getPredecessors(vertexInOrder, maxEntry.getKey());
                cliqueSet.add(maxEntry.getKey());
                maximumClique = new CliqueImpl<>(cliqueSet);
            }
        }
    }

    /**
     * Returns a map containing vertices from the {@code vertexOrder} mapped to their indices in
     * {@code vertexOrder}.
     *
     * @param vertexOrder a list with vertices.
     * @return a mapping of vertices from {@code vertexOrder} to their indices in
     *         {@code vertexOrder}.
     */
    private Map<V, Integer> getVertexInOrder(List<V> vertexOrder)
    {
        Map<V, Integer> vertexInOrder = new HashMap<>(vertexOrder.size());
        int i = 0;
        for (V vertex : vertexOrder) {
            vertexInOrder.put(vertex, i++);
        }
        return vertexInOrder;
    }

    /**
     * Returns the predecessors of {@code vertex} in the order defined by {@code map}. More
     * precisely, returns those of {@code vertex}, whose mapped index in {@code map} is less then
     * the index of {@code vertex}.
     *
     * @param vertexInOrder defines the mapping of vertices in {@code graph} to their indices in
     *        order.
     * @param vertex the vertex whose predecessors in order are to be returned.
     * @return the predecessors of {@code vertex} in order defines by {@code map}.
     */
    private Set<V> getPredecessors(Map<V, Integer> vertexInOrder, V vertex)
    {
        Set<V> predecessors = new HashSet<>();
        Integer vertexPosition = vertexInOrder.get(vertex);
        Set<E> edges = graph.edgesOf(vertex);
        for (E edge : edges) {
            V oppositeVertex = Graphs.getOppositeVertex(graph, edge, vertex);
            Integer destPosition = vertexInOrder.get(oppositeVertex);
            if (destPosition < vertexPosition)
                predecessors.add(oppositeVertex);
        }
        return predecessors;
    }

    /**
     * Returns a <a href="http://mathworld.wolfram.com/MaximumClique.html">maximum cardinality
     * clique</a> of the inspected {@code graph}. If the graph isn't chordal, returns null.
     *
     * @return a maximum clique of the {@code graph} if it is chordal, null otherwise.
     */
    public Clique<V> getClique()
    {
        lazyComputeMaximumClique();
        return maximumClique;
    }
}
