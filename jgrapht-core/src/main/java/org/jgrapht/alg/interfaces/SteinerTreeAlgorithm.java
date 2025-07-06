/*
 * (C) Copyright 2025, by Lena Büttel and Contributors.
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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * An algorithm which computes a <a href="https://en.wikipedia.org/wiki/Steiner_tree_problem">Steiner
 * tree</a> of a given graph. A Steiner tree is a tree that connects a given set of vertices (called
 * Steiner points or terminals) with minimum total weight, possibly using additional vertices not in
 * the original set.
 *
 * @param <V> the graph vertices type
 * @param <E> the graph edge type
 */
public interface SteinerTreeAlgorithm<V, E>
{
    /**
     * Computes a Steiner tree.
     *
     * @param steinerPoints the set of vertices (terminals) that must be connected by the Steiner tree
     * @return a Steiner tree connecting all the specified vertices
     */
	SteinerTree<E> getSteinerTree(Set<V> steinerPoints);

    /**
     * A Steiner tree.
     *
     * @param <E> the graph edge type
     */
    interface SteinerTree<E>
        extends Iterable<E>
    {
        /**
         * Returns the weight of the Steiner tree.
         *
         * @return weight of the Steiner tree
         */
        double getWeight();

        /**
         * Set of edges of the Steiner tree.
         *
         * @return edge set of the Steiner tree
         */
        Set<E> getEdges();

        /**
         * Returns an iterator over the edges in the Steiner tree.
         *
         * @return iterator over the edges in the Steiner tree.
         */
        @Override
        default Iterator<E> iterator()
        {
            return getEdges().iterator();
        }
    }

    /**
     * Default implementation of the Steiner tree interface.
     *
     * @param <E> the graph edge type
     */
    class SteinerTreeImpl<E>
        implements SteinerTree<E>, Serializable
    {
        private static final long serialVersionUID = 402707108331703333L;

        private final double weight;
        private final Set<E> edges;

        /**
         * Construct a new Steiner tree.
         *
         * @param edges the edges
         * @param weight the weight
         */
        public SteinerTreeImpl(Set<E> edges, double weight)
        {
            this.edges = edges;
            this.weight = weight;
        }

        @Override
        public double getWeight()
        {
            return weight;
        }

        @Override
        public Set<E> getEdges()
        {
            return edges;
        }

        @Override
        public String toString()
        {
            return "Steiner-Tree [weight=" + weight + ", edges=" + edges + "]";
        }
    }

}
