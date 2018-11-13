/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.traverse.*;

import java.util.*;

/**
 * Algorithm class which computes a number of distance related metrics for trees.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexandru Valeanu
 */
public class TreeMeasurer<V, E>
{

    /* Input graph */
    private final Graph<V, E> graph;

    /**
     * Constructs a new instance of TreeMeasurer.
     *
     * @param graph input graph
     * @throws NullPointerException if {@code graph} is {@code null}
     */
    public TreeMeasurer(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph);
    }

    private V computeFarthestVertex(BreadthFirstIterator<V, E> bfs)
    {
        V farthest = null;
        int dist = Integer.MIN_VALUE;

        while (bfs.hasNext()) {
            V v = bfs.next();
            int depth = bfs.getDepth(v);

            if (dist < depth) {
                farthest = v;
                dist = depth;
            }
        }

        return farthest;
    }

    /**
     * Compute the <a href="http://mathworld.wolfram.com/GraphCenter.html">graph center</a>. The
     * center of a graph is the set of vertices of graph eccentricity equal to the graph radius.
     *
     * <p>
     * Note: The input graph must be undirected.
     * </p>
     *
     * @return the graph center
     * @throws IllegalArgumentException if {@code graph} is not undirected
     */
    public Set<V> getGraphCenter()
    {
        GraphTests.requireUndirected(graph);

        if (graph.vertexSet().isEmpty())
            return new LinkedHashSet<>();

        V r = graph.vertexSet().iterator().next();

        V v1 = computeFarthestVertex(new BreadthFirstIterator<>(graph, r));

        BreadthFirstIterator<V, E> bfs = new BreadthFirstIterator<>(graph, v1);
        V v2 = computeFarthestVertex(bfs);

        List<V> diameterPath = new ArrayList<>();

        do {
            diameterPath.add(v2);
            v2 = bfs.getParent(v2);

        } while (v2 != null);

        Set<V> graphCenter;

        if (diameterPath.size() % 2 == 1)
            graphCenter = Collections.singleton(diameterPath.get(diameterPath.size() / 2));
        else {
            graphCenter = new LinkedHashSet<>(2);
            graphCenter.add(diameterPath.get(diameterPath.size() / 2));
            graphCenter.add(diameterPath.get(diameterPath.size() / 2 - 1));
        }

        return graphCenter;
    }
}
