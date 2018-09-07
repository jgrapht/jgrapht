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
 * Algorithm to compute a (weighted) <a href="http://mathworld.wolfram.com/Clique.html">Clique</a>
 * in a graph.
 *
 * @param <V> vertex the graph vertex type
 *
 * @author Joris Kinable
 */
public interface CliqueAlgorithm<V>
{

    /**
     * Computes a clique.
     *
     * @return a clique
     */
    Clique<V> getClique();

    /**
     * A <a href="http://mathworld.wolfram.com/Clique.html">Clique</a>
     *
     * @param <V> the vertex type
     */
    interface Clique<V>
        extends
        Set<V>
    {

        /**
         * Returns the weight of the clique. When solving a weighted clique problem, the weight
         * returned is the sum of the weights of the vertices in the clique. When solving the
         * unweighted variant, the cardinality of the clique is returned instead.
         *
         * @return weight of the independent set
         */
        double getWeight();
    }

    /**
     * Default implementation of a (weighted) clique
     *
     * @param <V> the vertex type
     */
    class CliqueImpl<V>
        extends
        WeightedUnmodifiableSet<V>
        implements
        Clique<V>
    {

        private static final long serialVersionUID = -4336873008459736342L;

        public CliqueImpl(Set<V> clique)
        {
            super(clique);
        }

        public CliqueImpl(Set<V> clique, double weight)
        {
            super(clique, weight);
        }
    }
}
