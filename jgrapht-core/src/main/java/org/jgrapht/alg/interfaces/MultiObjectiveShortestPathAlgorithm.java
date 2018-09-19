/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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

import java.util.*;

/**
 * An algorithm which computes multi-objective shortest paths between vertices.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
public interface MultiObjectiveShortestPathAlgorithm<V, E>
{

    /**
     * Get a shortest path from a source vertex to a sink vertex.
     *
     * @param source the source vertex
     * @param sink the target vertex
     * @return a shortest path or null if no path exists
     */
    List<GraphPath<V, E>> getPaths(V source, V sink);

    /**
     * Compute all shortest paths starting from a single source vertex.
     *
     * @param source the source vertex
     * @return the shortest paths
     */
    MultiObjectiveSingleSourcePaths<V, E> getPaths(V source);

    /**
     * A set of paths starting from a single source vertex.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    interface MultiObjectiveSingleSourcePaths<V, E>
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
         * Return the path from the source vertex to the sink vertex.
         *
         * @param sink the sink vertex
         * @return the path from the source vertex to the sink vertex or null if no such path exists
         */
        List<GraphPath<V, E>> getPaths(V sink);
    }

}
