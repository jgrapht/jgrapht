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

import org.jgrapht.*;

/**
 * An algorithm which computes shortest paths between vertices.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public interface ShortestPathAlgorithm<V, E>
{

    /**
     * Get a shortest path from a source vertex to a sink vertex.
     * 
     * @param source the source vertex
     * @param sink the target vertex
     * @return a shortest path or null if no path exists
     */
    GraphPath<V, E> getPath(V source, V sink);

    /**
     * Get the weight of the shortest path from a source vertex to a sink vertex. Returns
     * {@link Double#POSITIVE_INFINITY} if no path exists.
     *
     * @param source the source vertex
     * @param sink the sink vertex
     * @return the weight of the shortest path from a source vertex to a sink vertex, or
     *         {@link Double#POSITIVE_INFINITY} if no path exists
     */
    double getPathWeight(V source, V sink);

    /**
     * Compute all shortest paths starting from a single source vertex.
     * 
     * @param source the source vertex
     * @return the shortest paths
     */
    SingleSourcePaths<V, E> getPaths(V source);

    /**
     * A set of paths starting from a single source vertex.
     * 
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    interface SingleSourcePaths<V, E>
    {
        /**
         * Returns the graph over which this set of paths is defined.
         *
         * @return the graph
         */
        Graph<V, E> getGraph();

        /**
         * Returns the single source vertex.
         *
         * @return the single source vertex
         */
        V getSourceVertex();

        /**
         * Return the weight of the path from the source vertex to the sink vertex. If no such path
         * exists, {@link Double#POSITIVE_INFINITY} is returned. The weight of the path between a
         * vertex and itself is always zero.
         * 
         * @param sink the sink vertex
         * @return the weight of the path between source and sink vertices or
         *         {@link Double#POSITIVE_INFINITY} in case no such path exists
         */
        double getWeight(V sink);

        /**
         * Return the path from the source vertex to the sink vertex.
         * 
         * @param sink the sink vertex
         * @return the path from the source vertex to the sink vertex or null if no such path exists
         */
        GraphPath<V, E> getPath(V sink);
    }

}
