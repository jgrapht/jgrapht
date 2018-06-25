/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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

import java.util.Collections;
import java.util.Set;

/**
 * Algorithm to compute a vertex partition of a graph.
 *
 * @param <V> vertex the graph vertex type
 *
 * @author Alexandru Valeanu
 * @since June 2018
 */
public interface PartitionAlgorithm<V> {

    /**
     * Computes a vertex partition.
     *
     * @return a vertex partition
     */
    Partition<V> getPartition();

    /**
     * Check if the input partition is valid.
     *
     * @param partition the input vertex partition
     * @return true if the input partition is valid, false otherwise
     */
    boolean isValidPartition(Partition<V> partition);

    /**
     * A <a href="https://en.wikipedia.org/wiki/Graph_partition">graph partition</a>
     *
     * @param <V> the vertex type
     */
    interface Partition<V>{

        /**
         * Returns the first component of the partition
         *
         * @return the first component of the partition
         */
        Set<V> getFirstPartition();

        /**
         * Returns the second component of the partition
         *
         * @return the second component of the partition
         */
        Set<V> getSecondPartition();
    }

    /**
     * Default implementation of a vertex partition
     *
     * @param <V> the vertex type
     */
    class PartitionImpl<V> implements Partition<V> {

        private final Set<V> firstPartition;
        private final Set<V> secondPartition;

        public PartitionImpl(Set<V> firstPartition, Set<V> secondPartition) {
            this.firstPartition = Collections.unmodifiableSet(firstPartition);
            this.secondPartition = Collections.unmodifiableSet(secondPartition);
        }

        @Override
        public Set<V> getFirstPartition() {
            return firstPartition;
        }

        @Override
        public Set<V> getSecondPartition() {
            return secondPartition;
        }
    }
}
