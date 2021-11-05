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
package org.jgrapht.alg.independentset;

import org.jgrapht.*;
import org.jgrapht.alg.cycle.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.traverse.*;

import java.util.*;

/**
 * Calculates a <a href = "http://mathworld.wolfram.com/MaximumIndependentVertexSet.html">maximum
 * cardinality independent set</a> in a
 * <a href="https://en.wikipedia.org/wiki/Chordal_graph">chordal graph</a>. A chordal graph is a
 * simple graph in which all <a href="http://mathworld.wolfram.com/GraphCycle.html"> cycles</a> of
 * four or more vertices have a <a href="http://mathworld.wolfram.com/CycleChord.html"> chord</a>. A
 * chord is an edge that is not part of the cycle but connects two vertices of the cycle.
 *
 * To compute the independent set, this implementation relies on the {@link ChordalityInspector} to
 * compute a <a href=
 * "https://en.wikipedia.org/wiki/Chordal_graph#Perfect_elimination_and_efficient_recognition">
 * perfect elimination order</a>.
 *
 * The maximum cardinality independent set for a chordal graph is computed in $\mathcal{O}(|V| +
 * |E|)$ time.
 *
 * All the methods in this class are invoked in a lazy fashion, meaning that computations are only
 * started once the method gets invoked.
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 *
 * @author Timofey Chudakov
 */
public class ChordalGraphIndependentSetFinder<V, E>
    implements
    IndependentSetAlgorithm<V>
{

    private final Graph<V, E> graph;

    private final ChordalityInspector<V, E> chordalityInspector;

    private IndependentSet<V> maximumIndependentSet;

    /**
     * Creates a new ChordalGraphIndependentSetFinder instance. The {@link ChordalityInspector} used
     * in this implementation uses the default {@link MaximumCardinalityIterator} iterator.
     *
     * @param graph graph
     */
    public ChordalGraphIndependentSetFinder(Graph<V, E> graph)
    {
        this(graph, ChordalityInspector.IterationOrder.MCS);
    }

    /**
     * Creates a new ChordalGraphIndependentSetFinder instance. The {@link ChordalityInspector} used
     * in this implementation uses either the {@link MaximumCardinalityIterator} iterator or the
     * {@link LexBreadthFirstIterator} iterator, depending on the parameter {@code iterationOrder}.
     *
     * @param graph graph
     * @param iterationOrder constant which defines iterator to be used by the
     *        {@code ChordalityInspector} in this implementation.
     */
    public ChordalGraphIndependentSetFinder(
        Graph<V, E> graph, ChordalityInspector.IterationOrder iterationOrder)
    {
        this.graph = Objects.requireNonNull(graph);
        chordalityInspector = new ChordalityInspector<>(graph, iterationOrder);
    }

    /**
     * Lazily computes a maximum independent set of the inspected {@code graph}.
     */
    private void lazyComputeMaximumIndependentSet()
    {
        if (maximumIndependentSet == null && chordalityInspector.isChordal()) {
            // iterate the order from the end to the beginning
            // chooses vertices, that don't have neighbors in the current independent set
            // adds all its neighbors to the restricted set

            Set<V> restricted = new HashSet<>();
            Set<V> is = new HashSet<>();
            List<V> perfectEliminationOrder = chordalityInspector.getPerfectEliminationOrder();
            ListIterator<V> reverse =
                perfectEliminationOrder.listIterator(perfectEliminationOrder.size());

            while (reverse.hasPrevious()) {
                V previous = reverse.previous();
                if (!restricted.contains(previous)) {
                    is.add(previous);
                    for (E edge : graph.edgesOf(previous)) {
                        V opposite = Graphs.getOppositeVertex(graph, edge, previous);
                        if (!previous.equals(opposite)) {
                            restricted.add(opposite);
                        }
                    }
                }
            }
            maximumIndependentSet = new IndependentSetImpl<>(is);
        }
    }

    /**
     * Returns a <a href = "http://mathworld.wolfram.com/MaximumIndependentVertexSet.html"> maximum
     * cardinality independent set</a> of the inspected {@code graph}. If the graph isn't chordal,
     * returns null.
     *
     * @return a maximum independent set of the {@code graph} if it is chordal, null otherwise.
     */
    @Override
    public IndependentSet<V> getIndependentSet()
    {
        lazyComputeMaximumIndependentSet();
        return maximumIndependentSet;
    }
}
