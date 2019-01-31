/*
 * (C) Copyright 2018-2018, by Assaf Mizrachi and Contributors.
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

import java.util.*;

/**
 * An implementation of Bhandari algorithm for finding $K$ edge-<em>disjoint</em> shortest paths.
 * The algorithm determines the $k$ edge-disjoint shortest simple paths in increasing order of
 * weight. Weights can be negative (but no negative cycle is allowed). Only directed simple graphs
 * are allowed.
 *
 * <p>
 * The algorithm is running $k$ sequential Bellman-Ford iterations to find the shortest path at each
 * step. Hence, yielding a complexity of $k$*O(Bellman-Ford).
 * 
 * <ul>
 * <li>Bhandari, Ramesh 1999. Survivable networks: algorithms for diverse routing. 477. Springer. p.
 * 46. ISBN 0-7923-8381-8.
 * <li>Iqbal, F. and Kuipers, F. A. 2015.
 * <a href="https://www.nas.ewi.tudelft.nl/people/Fernando/papers/Wiley.pdf"> Disjoint Paths in
 * Networks </a>. Wiley Encyclopedia of Electrical and Electronics Engineering. 1–11.
 * </ul>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Assaf Mizrachi
 */
public class BhandariKDisjointShortestPaths<V, E>
    extends
    BaseKDisjointShortestPathsAlgorithm<V, E>
{
    /**
     * Creates a new instance of the algorithm.
     *
     * @param graph graph on which shortest paths are searched.
     *
     * @throws IllegalArgumentException if the graph is null.
     * @throws IllegalArgumentException if the graph is undirected.
     * @throws IllegalArgumentException if the graph is not simple.
     */
    public BhandariKDisjointShortestPaths(Graph<V, E> graph)
    {
        super(graph);
    }

    @Override
    protected void transformGraph(List<E> previousPath)
    {

        V source, target;
        E reversedEdge;

        // replace previous path edges with reversed edges with negative weight
        for (E originalEdge : previousPath) {
            source = workingGraph.getEdgeSource(originalEdge);
            target = workingGraph.getEdgeTarget(originalEdge);
            double originalEdgeWeight = workingGraph.getEdgeWeight(originalEdge);
            workingGraph.removeEdge(originalEdge);
            workingGraph.addEdge(target, source);
            reversedEdge = workingGraph.getEdge(target, source);
            workingGraph.setEdgeWeight(reversedEdge, -originalEdgeWeight);
        }
    }

    @Override
    protected GraphPath<V, E> calculateShortestPath(V startVertex, V endVertex)
    {
        return new BellmanFordShortestPath<>(this.workingGraph).getPath(startVertex, endVertex);
    }

}
