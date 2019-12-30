/*
 * (C) Copyright 2019-2019, by Peter Harman and Contributors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.interfaces.HamiltonianCycleAlgorithm;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.MaskSubgraph;
import org.jgrapht.traverse.DepthFirstIterator;

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
 */
public abstract class HamiltonianCycleAlgorithmBase<V, E> implements HamiltonianCycleAlgorithm<V, E> {

    /**
     * Transform from a List representation to a graph path.
     *
     * @param tour a list containing the vertices of the tour
     * @param graph the graph
     * @return a graph path
     */
    protected GraphPath<V, E> vertexListToTour(List<V> tour, Graph<V, E> graph) {
        List<E> edges = new ArrayList<>(tour.size() + 1);
        double tourWeight = 0d;
        Iterator<V> tourIterator = tour.iterator();
        V first = tourIterator.next();
        V u = first;
        while (tourIterator.hasNext()) {
            V v = tourIterator.next();
            E e = graph.getEdge(u, v);
            edges.add(e);
            tourWeight += graph.getEdgeWeight(e);
            u = v;
        }
        E e = graph.getEdge(u, first);
        edges.add(e);
        tourWeight += graph.getEdgeWeight(e);
        tour.add(first);
        return new GraphWalk<>(graph, first, first, tour, edges, tourWeight);
    }

    /**
     * Transform from a Set representation to a graph path.
     *
     * @param tour a set containing the edges of the tour
     * @param graph the graph
     * @return a graph path
     */
    protected GraphPath<V, E> edgeSetToTour(Set<E> tour, Graph<V, E> graph) {
        List<V> vertices = new ArrayList<>(tour.size() + 1);
        List<E> edges = new ArrayList<>(tour.size());
        double tourWeight = 0d;
        Iterator<V> tourIterator = new DepthFirstIterator<>(
                new MaskSubgraph<>(graph, v -> false, e -> !tour.contains(e)));
        V first = tourIterator.next();
        V u = first;
        while (tourIterator.hasNext()) {
            vertices.add(u);
            V v = tourIterator.next();
            E e = graph.getEdge(u, v);
            edges.add(e);
            tourWeight += graph.getEdgeWeight(e);
            u = v;
        }
        vertices.add(u);
        vertices.add(first);
        E e = graph.getEdge(u, first);
        edges.add(e);
        tourWeight += graph.getEdgeWeight(e);
        return new GraphWalk<>(graph, first, first, vertices, edges, tourWeight);
    }

    /**
     * Creates a tour for a graph with 1 vertex
     *
     * @param graph The graph
     * @return A tour with a single vertex
     */
    protected GraphPath<V, E> getSingletonTour(Graph<V, E> graph) {
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
    protected void checkGraph(Graph<V, E> graph) {
        graph = GraphTests.requireUndirected(graph);
        if (!GraphTests.isComplete(graph)) {
            throw new IllegalArgumentException("Graph is not complete");
        }
        if (graph.vertexSet().isEmpty()) {
            throw new IllegalArgumentException("Graph contains no vertices");
        }
    }
}
