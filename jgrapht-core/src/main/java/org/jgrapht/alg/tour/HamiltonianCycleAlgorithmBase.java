/*
 * (C) Copyright 2019-2021, by Peter Harman and Contributors.
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
package org.jgrapht.alg.tour;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

import java.util.*;

/**
 * Base class for TSP solver algorithms.
 *
 * <p>
 * This class provides implementations of utilities for TSP solver classes.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Peter Harman
 * @author Hannes Wellmann
 */
public abstract class HamiltonianCycleAlgorithmBase<V, E>
    implements
    HamiltonianCycleAlgorithm<V, E>
{

    /**
     * Transform from a List representation to a graph path.
     *
     * @param tour a list containing the vertices of the tour (is modified)
     * @param graph the graph
     * @return a graph path
     */
    protected GraphPath<V, E> vertexListToTour(List<V> tour, Graph<V, E> graph)
    {
        tour.add(tour.get(0));
        return closedVertexListToTour(tour, graph);
    }

    /**
     * Transform from a closed List representation (first and last vertex element are the same) to a
     * graph path.
     *
     * @param tour a closed list containing the vertices of the tour
     * @param graph the graph
     * @return a graph path
     */
    protected GraphPath<V, E> closedVertexListToTour(List<V> tour, Graph<V, E> graph)
    {
        assert tour.get(0) == tour.get(tour.size() - 1);

        List<E> edges = new ArrayList<>(tour.size() - 1);
        double tourWeight = 0d;
        V u = tour.get(0);
        for (V v : tour.subList(1, tour.size())) {
            E e = graph.getEdge(u, v);
            edges.add(e);
            tourWeight += graph.getEdgeWeight(e);
            u = v;
        }
        return new GraphWalk<>(graph, tour.get(0), tour.get(0), tour, edges, tourWeight);
    }

    /**
     * Transform from a Set representation to a graph path.
     *
     * @param tour a set containing the edges of the tour
     * @param graph the graph
     * @return a graph path
     */
    protected GraphPath<V, E> edgeSetToTour(Set<E> tour, Graph<V, E> graph)
    {
        List<V> vertices = new ArrayList<>(tour.size() + 1);

        MaskSubgraph<V, E> tourGraph =
            new MaskSubgraph<>(graph, v -> false, e -> !tour.contains(e));
        new DepthFirstIterator<>(tourGraph).forEachRemaining(vertices::add);

        return vertexListToTour(vertices, graph);
    }

    /**
     * Creates a tour for a graph with 1 vertex
     *
     * @param graph The graph
     * @return A tour with a single vertex
     */
    protected GraphPath<V, E> getSingletonTour(Graph<V, E> graph)
    {
        assert graph.vertexSet().size() == 1;
        V start = graph.vertexSet().iterator().next();
        return new GraphWalk<>(
            graph, start, start, Collections.singletonList(start), Collections.emptyList(), 0d);
    }

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
