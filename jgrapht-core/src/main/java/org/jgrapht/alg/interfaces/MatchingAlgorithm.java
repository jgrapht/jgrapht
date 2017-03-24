/*
 * (C) Copyright 2013-2017, by Alexey Kudinkin and Contributors.
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

import java.io.*;
import java.util.*;

/**
 * Allows to derive a <a href="http://en.wikipedia.org/wiki/Matching_(graph_theory)">matching</a> of
 * a given graph.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public interface MatchingAlgorithm<V, E>
{
    /**
     * Default tolerance used by algorithms comparing floating point values.
     */
    double DEFAULT_EPSILON = 1e-9;

    /*
     * TODO after next release: Rename computeMatching() to getMatching() and deprecate
     * computeMatching().
     */

    /**
     * Compute a matching for a given graph.
     *
     * @return a matching
     */
    Matching<E> getMatching();

    /**
     * Compute a matching for a given graph.
     * 
     * @return a matching
     * @deprecated This method has been renamed to {@link #getMatching()}
     */
    @Deprecated
    default Matching<E> computeMatching()
    {
        return getMatching();
    }

    /**
     * A graph matching.
     *
     * @param <E> the graph edge type
     */
    interface Matching<E>
    {
        /**
         * Returns the weight of the matching.
         *
         * @return the weight of the matching
         */
        double getWeight();

        /**
         * Get the edges of the matching.
         *
         * @return the edges of the matching
         */
        Set<E> getEdges();
    }

    /**
     * A default implementation of the matching interface.
     *
     * @param <E> the graph edge type
     */
    class MatchingImpl<E>
        implements Matching<E>, Serializable
    {
        private static final long serialVersionUID = 4767675421846527768L;

        private Set<E> edges;
        private double weight;

        /**
         * Construct a new instance
         *
         * @param edges the edges of the matching
         * @param weight the weight of the matching
         */
        public MatchingImpl(Set<E> edges, double weight)
        {
            this.edges = edges;
            this.weight = weight;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getWeight()
        {
            return weight;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<E> getEdges()
        {
            return edges;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Matching [edges=" + edges + ", weight=" + weight + "]";
        }

    }

}

// End MatchingAlgorithm.java
