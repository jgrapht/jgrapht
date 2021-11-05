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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * Allows obtaining a mapping of all
 * <a href="https://en.wikipedia.org/wiki/Chordal_graph#Minimal_separators"> minimal vertex
 * separators</a> of a graph to their multiplicities
 * <p>
 * In the context of this implementation following definitions are used:
 * <ul>
 * <li>A set of vertices $S$ of a graph $G=(V, E)$ is called a <i>u-v separator</i>, if vertices $u$
 * and $v$ in the induced graph on vertices $V(G) - S$ are in different connected components.</li>
 * <li>A set $S$ is called a <i>minimal u-v separator</i> if it is a u-v separator and no proper
 * subset of $S$ is a u-v separator.</li>
 * <li>A set $S$ is called a <i>minimal vertex separator</i> if it is minimal u-v separator for some
 * vertices $u$ and $v$ of the graph $G$.</li>
 * <li>A set of vertices $S$ is called a <i>minimal separator</i> if no proper subset of $S$ is a
 * separator of the graph $G$.</li>
 * </ul>
 * <p>
 * Let $\sigma = (v_1, v_2, \dots, v_n)$ be some perfect elimination order (peo) of the graph $G =
 * (V, E)$. The induced graph on vertices $(v_1, v_2, \dots, v_i)$ with respect to peo $\sigma$ is
 * denoted as $G_i$. The predecessors set of vertex $v$ with respect to peo $\sigma$ is denoted as
 * $N(v, \sigma)$. A set $B$ is called a <i>base set</i> with respect to $\sigma$, is there exist
 * some vertex $v$ with $t = \sigma(v)$ such that $N(v, \sigma) = B$ and B is not a maximal clique
 * in $G_{t-1}$. The vertices which satisfy conditions described above are called <i>dependent
 * vertices</i> with respect to $\sigma$. The cardinality of the set of dependent vertices is called
 * a multiplicity of the base set $B$. The multiplicity of a minimal vertex separator indicates the
 * number of different pairs of vertices separated by it.The definitions of a base set and a minimal
 * vertex separator in the context of chordal graphs are equivalent.
 * <p>
 * For more information on the topic see: Kumar, P. Sreenivasa &amp; Madhavan, C. E. Veni. (1998).
 * <a href="https://www.sciencedirect.com/science/article/pii/S0166218X98001231?via%3Dihub"> Minimal
 * vertex separators of chordal graphs</a>. Discrete Applied Mathematics. 89. 155-168.
 * 10.1016/S0166-218X(98)00123-1.
 * <p>
 * The running time of the algorithm is $\mathcal{O}(\omega(G)(|V| + |E|))$, where $\omega(G)$ is
 * the size of a maximum clique of the graph $G$.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see ChordalityInspector
 */
public class ChordalGraphMinimalVertexSeparatorFinder<V, E>
{
    /**
     * The graph in which minimal vertex separators to searched in
     */
    private final Graph<V, E> graph;
    /**
     * {@link ChordalityInspector} for testing chordality of the {@code graph}
     */
    private final ChordalityInspector<V, E> chordalityInspector;
    /**
     * A mapping of minimal separators to their multiplicities
     */
    private Map<Set<V>, Integer> minimalSeparatorsWithMultiplicities;

    /**
     * Creates new {@code ChordalGraphMinimalVertexSeparatorFinder} instance. The
     * {@link ChordalityInspector} used in this implementation uses the
     * {@link org.jgrapht.traverse.MaximumCardinalityIterator} iterator
     *
     * @param graph the graph minimal separators to search in
     */
    public ChordalGraphMinimalVertexSeparatorFinder(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph);
        chordalityInspector =
            new ChordalityInspector<>(graph, ChordalityInspector.IterationOrder.MCS);
    }

    /**
     * Computes a set of all minimal separators of the {@code graph} and returns it. Returns null if
     * the {@code graph} isn't chordal.
     *
     * @return computed set of all minimal separators, or null if the {@code graph} isn't chordal
     */
    public Set<Set<V>> getMinimalSeparators()
    {
        lazyComputeMinimalSeparatorsWithMultiplicities();
        return minimalSeparatorsWithMultiplicities == null ? null
            : minimalSeparatorsWithMultiplicities.keySet();
    }

    /**
     * Computes a mapping of all minimal vertex separators of the {@code graph} and returns it.
     * Returns null if the {@code graph} isn't chordal.
     *
     * @return computed mapping of all minimal separators to their multiplicities, or null if the
     *         {@code graph} isn't chordal
     */
    public Map<Set<V>, Integer> getMinimalSeparatorsWithMultiplicities()
    {
        lazyComputeMinimalSeparatorsWithMultiplicities();
        return minimalSeparatorsWithMultiplicities;
    }

    /**
     * Lazy computes a set of all minimal separators and a mapping of all minimal vertex separators
     * to their multiplicities
     */
    private void lazyComputeMinimalSeparatorsWithMultiplicities()
    {
        if (minimalSeparatorsWithMultiplicities == null && chordalityInspector.isChordal()) {
            minimalSeparatorsWithMultiplicities = new HashMap<>();
            List<V> perfectEliminationOrder = chordalityInspector.getPerfectEliminationOrder();
            Map<V, Integer> vertexInOrder = getVertexInOrder(perfectEliminationOrder);
            Set<V> previous;
            Set<V> current = new HashSet<>();
            for (int i = 1; i < perfectEliminationOrder.size(); i++) {
                previous = current;
                current = getPredecessors(vertexInOrder, perfectEliminationOrder.get(i));
                if (current.size() <= previous.size()) {
                    // current set is a minimal separator
                    if (minimalSeparatorsWithMultiplicities.containsKey(current)) {
                        // found another vertex dependent on current set
                        minimalSeparatorsWithMultiplicities
                            .put(current, minimalSeparatorsWithMultiplicities.get(current) + 1);
                    } else {
                        // vertex at position i is the first vertex dependent on current set
                        minimalSeparatorsWithMultiplicities.put(current, 1);
                    }
                }
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
}
