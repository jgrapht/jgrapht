/*
 * (C) Copyright 2003-2021, by Tim Shearouse and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.*;

import java.util.*;

/**
 * Generates a complete graph of any size.
 * 
 * <p>
 * A complete graph is a graph where every vertex shares an edge with every other vertex. If it is a
 * directed graph, then edges must always exist in both directions.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Tim Shearouse
 */
public class CompleteGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private final int size;

    /**
     * Construct a new CompleteGraphGenerator.
     *
     * The generator will first add {@code size} nodes to the target graph when invoking
     * {@link #generateGraph(Graph, Map)}. Next, a complete graph is generated on <i>all</i> nodes
     * present in the target graph, including any nodes that were already present in the target
     * graph.
     *
     * @param size number of vertices that will be added to the graph
     * @throws IllegalArgumentException if the specified size is negative
     */
    public CompleteGraphGenerator(int size)
    {
        if (size < 0)
            throw new IllegalArgumentException("size must be non-negative");
        this.size = size;
    }

    /**
     * Construct a new CompleteGraphGenerator.
     *
     * A complete graph will be generated using the vertices already present in the target graph
     * when invoking {@link #generateGraph(Graph, Map)}
     */
    public CompleteGraphGenerator()
    {
        size = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        /*
         * Ensure directed or undirected
         */
        GraphTests.requireDirectedOrUndirected(target);
        boolean isDirected = target.getType().isDirected();

        /*
         * Add vertices
         */
        for (int i = 0; i < size; i++)
            target.addVertex();

        /*
         * Add edges
         */
        List<V> nodes = new ArrayList<>(target.vertexSet());
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                V v = nodes.get(i);
                V u = nodes.get(j);
                target.addEdge(v, u);
                if (isDirected) {
                    target.addEdge(u, v);
                }
            }
        }
    }
}
