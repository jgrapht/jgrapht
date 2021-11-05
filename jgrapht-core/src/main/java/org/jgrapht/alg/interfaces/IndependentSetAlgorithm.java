/*
 * (C) Copyright 2018-2021, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.interfaces;

import org.jgrapht.util.*;

import java.util.*;

/**
 * Algorithm to compute an
 * <a href="http://mathworld.wolfram.com/IndependentVertexSet.html">Independent Set</a> in a graph.
 *
 * @param <V> vertex the graph vertex type
 *
 * @author Joris Kinable
 */
public interface IndependentSetAlgorithm<V>
{

    /**
     * Computes an independent set; all vertices are considered to have equal weight.
     *
     * @return a vertex independent set
     */
    IndependentSet<V> getIndependentSet();

    /**
     * A (weighted) <a href="http://mathworld.wolfram.com/IndependentVertexSet.html">Independent
     * Set</a>
     *
     * @param <V> the vertex type
     */
    interface IndependentSet<V>
        extends
        Set<V>
    {

        /**
         * Returns the weight of the independent set. When solving a weighted independent set
         * problem, the weight returned is the sum of the weights of the vertices in the independent
         * set. When solving the unweighted variant, the cardinality of the independent set is
         * returned instead.
         *
         * @return weight of the independent set
         */
        double getWeight();
    }

    /**
     * Default implementation of a (weighted) independent set
     *
     * @param <V> the vertex type
     */
    class IndependentSetImpl<V>
        extends
        WeightedUnmodifiableSet<V>
        implements
        IndependentSet<V>
    {

        private static final long serialVersionUID = 4572451196544323306L;

        public IndependentSetImpl(Set<V> independentSet)
        {
            super(independentSet);
        }

        public IndependentSetImpl(Set<V> independentSet, double weight)
        {
            super(independentSet, weight);
        }
    }
}
