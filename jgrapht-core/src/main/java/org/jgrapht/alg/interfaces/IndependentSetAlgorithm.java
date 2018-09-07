/*
 * (C) Copyright 2018-2018, by Joris Kinable and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
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
