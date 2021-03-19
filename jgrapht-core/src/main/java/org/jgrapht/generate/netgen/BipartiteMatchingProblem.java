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

import java.util.Set;
import java.util.function.Function;

/**
 * This class represents a bipartite matching problem. The problem can be weighted or unweighted
 * depending on the {@link BipartiteMatchingProblem#isWeighted()}.
 * <p>
 * The minimum weight (minimum cost) perfect bipartite matching problem is defined as follows: \[
 * \begin{align} \mbox{minimize}~&amp; \sum_{e \in E}c_e\cdot x_e &amp;\\ \mbox{s.t.
 * }&amp;\sum_{e\in \delta(v)} x_e = 1 &amp; \forall v\in V\\ &amp;x_e \in \{0,1\} &amp; \forall
 * e\in E \end{align} \] Here $\delta(v)$ denotes the set of edges incident to the vertex $v$. The
 * parameters $c_{e}$ define a cost of adding the edge $e$ to the matching. If the problem is
 * unweighted, the values $c_e$ are equal to 1 in the problem formulation.
 * <p>
 * This class can define bipartite matching problems without the requirement that every edge must be
 * matched, i.e. non-perfect matching problems. These problems are called maximum cardinality
 * bipartite matching problems. The goal of the maximum cardinality matching problem is to find a
 * matching with maximum number of edges. If the cost function is used in this setup, the goal is to
 * find the cheapest matching among all matchings of maximum cardinality.
 *
 * @param <V> the graph vertex types
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see org.jgrapht.alg.interfaces.MatchingAlgorithm
 */
public interface BipartiteMatchingProblem<V, E>
{

    /**
     * Returns the graph, which defines the problem
     *
     * @return the graph, which defines the problem
     */
    Graph<V, E> getGraph();

    /**
     * Returns one of the 2 partitions of the graph (no 2 vertices in this set share an edge)
     *
     * @return one of the 2 partitions of the graph
     */
    Set<V> getPartition1();

    /**
     * Returns one of the 2 partitions of the graph (no 2 vertices in this set share an edge)
     *
     * @return one of the 2 partitions of the graph
     */
    Set<V> getPartition2();

    /**
     * Returns a cost function of this problem. This function must be defined for all edges of the
     * graph. In the case the problem is unweighted, the function must return any constant value for
     * all edges.
     *
     * @return a cost function of this problem
     */
    Function<E, Double> getCosts();

    /**
     * Determines if this problem is weighted or not.
     *
     * @return {@code true} is the problem is weighted, {@code false} otherwise
     */
    boolean isWeighted();

    /**
     * Dumps the problem edge costs to the underlying graph.
     */
    default void dumpCosts()
    {
        Graph<V, E> graph = getGraph();
        Function<E, Double> costs = getCosts();
        for (E edge : graph.edgeSet()) {
            graph.setEdgeWeight(edge, costs.apply(edge));
        }
    }

    /**
     * Default implementation of a Bipartite Matching Problem
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    class BipartiteMatchingProblemImpl<V, E>
        implements
        BipartiteMatchingProblem<V, E>
    {
        private final Graph<V, E> graph;
        private final Set<V> partition1;
        private final Set<V> partition2;
        private final Function<E, Double> costs;
        private final boolean weighted;

        /**
         * Constructs a new bipartite matching problem
         *
         * @param graph a graph, which defines the problem
         * @param partition1 one of the partitions of the graph
         * @param partition2 one of the partitions of the graph
         * @param costs problem cost function
         * @param weighted is the problem is weighted or not
         */
        public BipartiteMatchingProblemImpl(
            Graph<V, E> graph, Set<V> partition1, Set<V> partition2, Function<E, Double> costs,
            boolean weighted)
        {
            this.graph = graph;
            this.partition1 = partition1;
            this.partition2 = partition2;
            this.costs = costs;
            this.weighted = weighted;
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
        public Function<E, Double> getCosts()
        {
            return costs;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<V> getPartition1()
        {
            return partition1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<V> getPartition2()
        {
            return partition2;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isWeighted()
        {
            return weighted;
        }

    }
}
