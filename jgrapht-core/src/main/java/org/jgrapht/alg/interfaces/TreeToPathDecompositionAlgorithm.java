/*
 * (C) Copyright 2018-2021, by Alexandru Valeanu and Contributors.
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
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

/**
 * An algorithm which computes a decomposition into disjoint paths for a given tree/forest
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public interface TreeToPathDecompositionAlgorithm<V, E>
{
    /**
     * Computes a path decomposition.
     *
     * @return a path decomposition
     */
    PathDecomposition<V, E> getPathDecomposition();

    /**
     * A path decomposition.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    interface PathDecomposition<V, E>
    {
        /**
         * Set of edges of the path decomposition.
         * 
         * @return edge set of the path decomposition
         */
        Set<E> getEdges();

        /**
         * Set of disjoint paths of the decomposition
         *
         * @return list of vertex paths
         */
        Set<GraphPath<V, E>> getPaths();

        /**
         * @return number of paths in the decomposition
         */
        default int numberOfPaths()
        {
            return getPaths().size();
        }
    }

    /**
     * Default implementation of the path decomposition interface.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    class PathDecompositionImpl<V, E>
        implements
        PathDecomposition<V, E>,
        Serializable
    {

        private static final long serialVersionUID = 8468626434814461297L;
        private final Set<E> edges;
        private final Set<GraphPath<V, E>> paths;

        /**
         * Construct a new path decomposition.
         *
         * @param graph the graph
         * @param edges the edges
         * @param paths the vertex paths
         */
        public PathDecompositionImpl(Graph<V, E> graph, Set<E> edges, List<List<V>> paths)
        {
            this.edges = edges;

            Set<GraphPath<V, E>> arrayUnenforcedSet = paths
                .stream().map(path -> new GraphWalk<>(graph, path, path.size()))
                .collect(Collectors.toCollection(ArrayUnenforcedSet::new));

            this.paths = Collections.unmodifiableSet(arrayUnenforcedSet);
        }

        @Override
        public Set<E> getEdges()
        {
            return edges;
        }

        @Override
        public Set<GraphPath<V, E>> getPaths()
        {
            return paths;
        }

        @Override
        public String toString()
        {
            return "Path-Decomposition [edges=" + edges + "," + "paths=" + getPaths() + "]";
        }
    }

}
