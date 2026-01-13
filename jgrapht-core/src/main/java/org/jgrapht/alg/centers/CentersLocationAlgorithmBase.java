/*
 * (C) Copyright 2026, by Jose Alejandro Cornejo-Acosta and Contributors.
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
package org.jgrapht.alg.centers;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.interfaces.CentersLocationAlgorithm;

/**
 * Base class for Centers Location algorithms.
 *
 * <p>
 * This class provides implementations of utilities for Centers Location classes.
 * </p>
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 *
 * @author Jose Alejandro Cornejo-Acosta
 */
public abstract class CentersLocationAlgorithmBase<V, E>
    implements CentersLocationAlgorithm<V, E>
{

    /**
     * Checks that graph is undirected, complete, and non-empty
     *
     * @param graph the graph
     * @throws IllegalArgumentException if graph is not undirected
     * @throws IllegalArgumentException if graph is not complete
     * @throws IllegalArgumentException if graph contains no vertices
     */
    protected void checkGraph(Graph<V, E> graph)
    {
        GraphTests.requireUndirected(graph);

        requireNotEmpty(graph);

        if (!GraphTests.isComplete(graph)) {
            throw new IllegalArgumentException("Graph is not complete");
        }
    }

    /**
     * Checks that graph is not empty
     *
     * @param graph the graph
     * @throws IllegalArgumentException if graph contains no vertices
     */
    protected void requireNotEmpty(Graph<V, E> graph)
    {
        if (graph.vertexSet().isEmpty()) {
            throw new IllegalArgumentException("Graph contains no vertices");
        }
    }
}
