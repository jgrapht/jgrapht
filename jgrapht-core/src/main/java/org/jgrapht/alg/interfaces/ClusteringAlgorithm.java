/*
 * (C) Copyright 2019-2021, by Dimitrios Michail and Contributors.
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
 * An algorithm which computes a graph vertex clustering.
 *
 * @param <V> the graph vertex type
 */
public interface ClusteringAlgorithm<V>
{

    /**
     * Computes a clustering.
     *
     * @return a clustering
     */
    Clustering<V> getClustering();

    /**
     * A clustering. The clusters are integers starting from $0$.
     *
     * @param <V> the graph vertex type
     */
    interface Clustering<V>
        extends
        Iterable<Set<V>>
    {
        /**
         * Get the number of clusters.
         * 
         * @return the number of clusters
         */
        int getNumberClusters();

        /**
         * Get the clusters.
         *
         * @return a list of clusters
         */
        List<Set<V>> getClusters();
    }

    /**
     * Default implementation of the clustering interface.
     *
     * @param <V> the graph vertex type
     */
    class ClusteringImpl<V>
        implements
        Clustering<V>,
        Serializable
    {
        private static final long serialVersionUID = -5718903410443848101L;

        private final List<Set<V>> clusters;

        /**
         * Construct a new clustering.
         *
         * @param clusters clusters
         */
        public ClusteringImpl(List<Set<V>> clusters)
        {
            this.clusters = clusters;
        }

        @Override
        public int getNumberClusters()
        {
            return clusters.size();
        }

        @Override
        public List<Set<V>> getClusters()
        {
            return clusters;
        }

        @Override
        public String toString()
        {
            return "Clustering [k=" + clusters.size() + ", clusters=" + clusters + "]";
        }

        @Override
        public Iterator<Set<V>> iterator()
        {
            return clusters.iterator();
        }
    }

}
