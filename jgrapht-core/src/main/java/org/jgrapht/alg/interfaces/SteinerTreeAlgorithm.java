/*
 * (C) Copyright 2025, by TODO and Contributors.
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

import java.io.*;
import java.util.*;

/**
 * TODO: add docs for steiner tree
 * An algorithm which computes a <a href="https://en.wikipedia.org/wiki/Spanning_tree"> spanning
 * tree</a> of a given connected graph. In the case of disconnected graphs it would rather derive a
 * spanning <i>forest</i>.
 *
 * @param <V> the graph vertices type
 * @param <E> the graph edge type
 */
public interface SteinerTreeAlgorithm<V, E>
{
    /**
     * Computes a Steiner tree.
     *
     * @param steinerPoints the Steiner vertices
     * @return a Steiner tree
     */
	SteinerTree<E> getSpanningTree(Set<V> steinerPoints);

    /**
     * A spanning tree.
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
     * Default implementation of the spanning tree interface.
     *
     * @param <E> the graph edge type
     */
    class SpeinerTreeImpl<E>
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
        public SpeinerTreeImpl(Set<E> edges, double weight)
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
