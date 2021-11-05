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
package org.jgrapht.alg.color;

import org.jgrapht.*;
import org.jgrapht.alg.cycle.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.traverse.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * Calculates a <a href="http://mathworld.wolfram.com/MinimumVertexColoring.html">minimum vertex
 * coloring</a> for a <a href="https://en.wikipedia.org/wiki/Chordal_graph">chordal graph</a>. A
 * chordal graph is a simple graph in which all
 * <a href="http://mathworld.wolfram.com/GraphCycle.html"> cycles</a> of four or more vertices have
 * a <a href="http://mathworld.wolfram.com/CycleChord.html"> chord</a>. A chord is an edge that is
 * not part of the cycle but connects two vertices of the cycle.
 *
 * To compute the vertex coloring, this implementation relies on the {@link ChordalityInspector} to
 * compute a <a href=
 * "https://en.wikipedia.org/wiki/Chordal_graph#Perfect_elimination_and_efficient_recognition">
 * perfect elimination order</a>.
 *
 * The vertex coloring for a chordal graph is computed in $\mathcal{O}(|V| + |E|)$ time.
 *
 * All the methods in this class are invoked in a lazy fashion, meaning that computations are only
 * started once the method gets invoked.
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 *
 * @author Timofey Chudakov
 */
public class ChordalGraphColoring<V, E>
    implements
    VertexColoringAlgorithm<V>
{

    private final Graph<V, E> graph;

    private final ChordalityInspector<V, E> chordalityInspector;

    private Coloring<V> coloring;

    /**
     * Creates a new ChordalGraphColoring instance. The {@link ChordalityInspector} used in this
     * implementation uses the default {@link MaximumCardinalityIterator} iterator.
     *
     * @param graph graph
     */
    public ChordalGraphColoring(Graph<V, E> graph)
    {
        this(graph, ChordalityInspector.IterationOrder.MCS);
    }

    /**
     * Creates a new ChordalGraphColoring instance. The {@link ChordalityInspector} used in this
     * implementation uses either the {@link MaximumCardinalityIterator} iterator or the
     * {@link LexBreadthFirstIterator} iterator, depending on the parameter {@code iterationOrder}.
     *
     * @param graph graph
     * @param iterationOrder constant which defines iterator to be used by the
     *        {@code ChordalityInspector} in this implementation.
     */
    public ChordalGraphColoring(
        Graph<V, E> graph, ChordalityInspector.IterationOrder iterationOrder)
    {
        this.graph = Objects.requireNonNull(graph);
        chordalityInspector = new ChordalityInspector<>(graph, iterationOrder);
    }

    /**
     * Lazily computes the coloring of the graph.
     */
    private void lazyComputeColoring()
    {
        if (coloring == null && chordalityInspector.isChordal()) {
            List<V> perfectEliminationOrder = chordalityInspector.getPerfectEliminationOrder();

            Map<V, Integer> vertexColoring =
                CollectionUtil.newHashMapWithExpectedSize(perfectEliminationOrder.size());
            Map<V, Integer> vertexInOrder = getVertexInOrder(perfectEliminationOrder);
            for (V vertex : perfectEliminationOrder) {
                Set<V> predecessors = getPredecessors(vertexInOrder, vertex);
                Set<Integer> predecessorColors =
                    CollectionUtil.newHashSetWithExpectedSize(predecessors.size());
                predecessors.forEach(v -> predecessorColors.add(vertexColoring.get(v)));

                // find the minimum unused color in the set of predecessors
                int minUnusedColor = 0;
                while (predecessorColors.contains(minUnusedColor)) {
                    ++minUnusedColor;
                }
                vertexColoring.put(vertex, minUnusedColor);
            }
            int maxColor = (int) vertexColoring.values().stream().distinct().count();
            coloring = new ColoringImpl<>(vertexColoring, maxColor);
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
        Map<V, Integer> vertexInOrder =
            CollectionUtil.newHashMapWithExpectedSize(vertexOrder.size());
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
     * Returns a <a href="http://mathworld.wolfram.com/MinimumVertexColoring.html">minimum vertex
     * coloring</a> of the inspected {@code graph}. If the graph isn't chordal, returns null. The
     * number of colors used in the coloring equals the chromatic number of the input graph.
     *
     * @return a coloring of the {@code graph} if it is chordal, null otherwise.
     */
    @Override
    public Coloring<V> getColoring()
    {
        lazyComputeColoring();
        return coloring;
    }

    /**
     * Returns the <a href=
     * "https://en.wikipedia.org/wiki/Chordal_graph#Perfect_elimination_and_efficient_recognition">
     * perfect elimination order</a> used to create the coloring (if one exists). This method
     * returns null if the graph is not chordal.
     *
     * @return the perfect elimination order used to create the coloring, or null if graph is not
     *         chordal.
     */
    public List<V> getPerfectEliminationOrder()
    {
        return chordalityInspector.getPerfectEliminationOrder();
    }
}
