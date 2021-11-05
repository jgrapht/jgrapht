/*
 * (C) Copyright 2020-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.generate.netgen;

import org.jgrapht.Graph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * This class represents a maximum flow problem. Use this class for both directed and undirected
 * maximum flow problems.
 * <p>
 * The single-source, single-sink maximum flow problem is defined as follows: \[ \begin{align}
 * \mbox{maximize}~&amp; \sum_{e \in \delta^+(s)}f_e - \sum_{e \in \delta^-(s)}f_e &amp;\\
 * \mbox{s.t. }&amp;\sum_{e\in \delta^-(v)} f_e = \sum_{e\in \delta^+(v)} f_e &amp; \forall v\in
 * V\setminus \{s, t\} \\ &amp;0 \leq f_e \leq c_e &amp; \forall e\in E \end{align} \] Here
 * $\delta^+(v)$ and $\delta^-(v)$ denote the outgoing and incoming edges of vertex $v$
 * respectively. The value $f_e$ denotes the flow on edge $e$, which is bounded by $c_e$ - the
 * capacity of the edge $e$. The vertex $s$ is a network source, the vertex $t$ - network sink. The
 * edge capacities can be retrieved using {@link MaximumFlowProblem#getCapacities()}. The problem
 * formulation above defines a canonical maximum flow problem, i.e. with only one source and one
 * sink.
 * <p>
 * A maximum flow problem can be defined on a network with multiple sources and sinks. This problem
 * can be reduced to the above problem definition as follows:
 * <ul>
 * <li>Two special vertices are added to the graph: a super source and a super sink;</li>
 * <li>Edges with infinite capacity are added from the super source to every source and from every
 * sink to the super sink</li>
 * </ul>
 * To use this reduction, see {@link MaximumFlowProblem#toSingleSourceSingleSinkProblem()}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see org.jgrapht.alg.interfaces.MaximumFlowAlgorithm
 */
public interface MaximumFlowProblem<V, E>
{

    double CAPACITY_INF = Integer.MAX_VALUE;

    /**
     * Returns the network the problem is defined on.
     *
     * @return the network the problem is defined on.
     */
    Graph<V, E> getGraph();

    /**
     * Returns the source set of this problem.
     *
     * @return the source set of this problem.
     */
    Set<V> getSources();

    /**
     * Returns the sink set of this problem.
     *
     * @return the sink set of this problem.
     */
    Set<V> getSinks();

    /**
     * Returns one source of this problem (a problem is guaranteed to have at least one source). Use
     * this method if the problem is in canonical form (only one source and one sink).
     *
     * @return one source of this problem.
     */
    default V getSource()
    {
        return getSources().iterator().next();
    }

    /**
     * Returns one sink of this problem (a problem is guaranteed to have at least one sink). Use
     * this method if the problem is in canonical form (only one source and one sink).
     *
     * @return one sink of this problem.
     */
    default V getSink()
    {
        return getSinks().iterator().next();
    }

    /**
     * Returns the capacity function of this problem. This function is defined for all edges of the
     * underlying network.
     *
     * @return the capacity function of this problem.
     */
    Function<E, Double> getCapacities();

    /**
     * Converts this problem to the canonical form. Resulting problem is equivalent to the previous
     * one.
     *
     * @return a problem in the canonical form.
     */
    MaximumFlowProblem<V, E> toSingleSourceSingleSinkProblem();

    /**
     * Checks if this problem is in the canonical form.
     *
     * @return {@code true} if this problem is in the canonical form, {@code false} otherwise.
     */
    default boolean isSingleSourceSingleSinkProblem()
    {
        return getSources().size() == 1 && getSinks().size() == 1;
    }

    /**
     * Dumps the network edge capacities to the underlying graph.
     */
    default void dumpCapacities()
    {
        Graph<V, E> graph = getGraph();
        Function<E, Double> capacities = getCapacities();
        for (E edge : graph.edgeSet()) {
            graph.setEdgeWeight(edge, capacities.apply(edge));
        }
    }

    /**
     * Default implementation of a Maximum Flow Problem.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    class MaximumFlowProblemImpl<V, E>
        implements
        MaximumFlowProblem<V, E>
    {
        private final Graph<V, E> graph;
        private final Set<V> sources;
        private final Set<V> sinks;
        private final Function<E, Double> capacities;

        /**
         * Constructs a new maximum flow problem.
         *
         * @param graph flow network
         * @param sources set of network sources
         * @param sinks set of network sinks
         * @param capacities network capacity function
         */
        public MaximumFlowProblemImpl(
            Graph<V, E> graph, Set<V> sources, Set<V> sinks, Function<E, Double> capacities)
        {
            this.graph = graph;
            this.sources = sources;
            this.sinks = sinks;
            this.capacities = capacities;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Graph<V, E> getGraph()
        {
            return graph;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<V> getSources()
        {
            return sources;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<V> getSinks()
        {
            return sinks;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Function<E, Double> getCapacities()
        {
            return capacities;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MaximumFlowProblem<V, E> toSingleSourceSingleSinkProblem()
        {
            Set<E> newEdges = new HashSet<>();

            Set<V> sourceSet = convert(sources, newEdges, true);
            Set<V> sinkSet = convert(sinks, newEdges, false);

            Function<E, Double> updatedCapacities = e -> {
                if (newEdges.contains(e)) {
                    return CAPACITY_INF;
                } else {
                    return capacities.apply(e);
                }
            };

            return new MaximumFlowProblemImpl<>(graph, sourceSet, sinkSet, updatedCapacities);
        }

        /**
         * Adds a new super vertex and connects it to all vertices in {@code vertices}. Depending on
         * the value of {@code sources}, the edges are directed from super vertex or to super
         * vertex. New edges are added to {@code newEdges}.
         *
         * @param vertices set of vertices to connect super vertex to
         * @param newEdges container to add new edges to
         * @param sources {@code true} if super vertex is super source, {@code false} if it's super
         *        sink
         * @return 1 element set containing the super vertex
         */
        private Set<V> convert(Set<V> vertices, Set<E> newEdges, boolean sources)
        {
            if (vertices.size() == 1) {
                return vertices;
            }
            V superVertex = graph.addVertex();
            Set<V> newSourceSet = Collections.singleton(superVertex);

            for (V vertex : vertices) {
                E edge;
                if (sources) {
                    edge = graph.addEdge(superVertex, vertex);
                } else {
                    edge = graph.addEdge(vertex, superVertex);
                }
                newEdges.add(edge);
            }
            return newSourceSet;
        }
    }
}
