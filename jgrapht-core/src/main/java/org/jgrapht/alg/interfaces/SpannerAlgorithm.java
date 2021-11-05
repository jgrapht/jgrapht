/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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

import java.io.*;
import java.util.*;

/**
 * An algorithm which computes a
 * <a href="https://en.wikipedia.org/wiki/Glossary_of_graph_theory#spanner">graph spanner</a> of a
 * given graph.
 *
 * @param <E> edge the graph edge type
 *
 * @author Dimitrios Michail
 */
public interface SpannerAlgorithm<E>
{

    /**
     * Computes a graph spanner.
     *
     * @return a graph spanner
     */
    Spanner<E> getSpanner();

    /**
     * A graph spanner.
     *
     * @param <E> the graph edge type
     */
    interface Spanner<E>
        extends
        Set<E>
    {

        /**
         * Returns the weight of the graph spanner.
         * 
         * @return weight of the graph spanner
         */
        double getWeight();
    }

    /**
     * Default implementation of the spanner interface.
     *
     * @param <E> the graph edge type
     */
    class SpannerImpl<E>
        extends
        WeightedUnmodifiableSet<E>
        implements
        Spanner<E>,
        Serializable
    {
        private static final long serialVersionUID = 5951646499902668516L;

        /**
         * Construct a new spanner
         *
         * @param edges the edges
         */
        public SpannerImpl(Set<E> edges)
        {
            super(edges);
        }

        /**
         * Construct a new spanner
         *
         * @param edges the edges
         * @param weight the weight
         */
        public SpannerImpl(Set<E> edges, double weight)
        {
            super(edges, weight);
        }

        @Override
        public String toString()
        {
            return "Spanner [weight=" + weight + ", edges=" + this + "]";
        }
    }

}
