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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
     * Check if the given vertex partition is valid.
     *
     * @param partition the input vertex partition
     * @return true if the input partition is valid, false otherwise
     */
    boolean isValidPartition(Partition<V> partition);

    /**
     * A <a href="https://en.wikipedia.org/wiki/Graph_partition">graph partition</a>.
     *
     * @param <V> the vertex type
     */
    interface Partition<V>{

        /**
         * Get the number of partitions classes.
         *
         * @return the number of partitions classes
         */
        default int getNumberPartitions(){
            return getPartitionClasses().size();
        }

        /**
         * Get the index-th partition class (0-based).
         *
         * @param index index of the partition class to return
         * @return the index-th partition class
         * @throws IndexOutOfBoundsException if the index is out of range
         *         (<tt>index &lt; 0 || index &gt;= getNumberPartitions()</tt>)
         */
        default Set<V> getPartitionClass(int index){
            if (index < 0 || index >= getNumberPartitions())
                throw new IndexOutOfBoundsException("Partition class " + index + "doesn't exist!");

            return getPartitionClasses().get(index);
        }

        /**
         * Get the partition classes. This method returns a partitioning of
         * the vertices in the graph in disjoint partition classes.
         *
         * @return a list of partition classes
         */
        List<Set<V>> getPartitionClasses();
    }

    /**
     * Default implementation of a vertex partition
     *
     * @param <V> the vertex type
     */
    class PartitionImpl<V> implements Partition<V>, Serializable {

        private static final long serialVersionUID = 3702471090706836080L;

        /*
            Partition classes
         */
        private final List<Set<V>> classes;

        /**
         * Construct a new vertex partition.
         *
         * @param classes the partition classes
         * @throws NullPointerException if {@code classes} is {@code null}
         */
        public PartitionImpl(List<Set<V>> classes) {
            this.classes = Collections.unmodifiableList(Objects.requireNonNull(classes)
                    .stream()
                    .map(Collections::unmodifiableSet)
                    .collect(Collectors.toList()));
        }

        @Override
        public List<Set<V>> getPartitionClasses() {
            return classes;
        }
    }
}
