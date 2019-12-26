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
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.HamiltonianCycleAlgorithm;
import org.jgrapht.graph.GraphWalk;

/**
 *
 * @author peter
 */
public abstract class HamiltonianCycleAlgorithmBase<V, E> implements HamiltonianCycleAlgorithm<V, E> {

    /**
     * Transform from a List representation to a graph path.
     *
     * @param tour a list containing the vertices of the tour
     * @param graph the graph
     * @return a graph path
     */
    protected GraphPath<V, E> listToTour(List<V> tour, Graph<V, E> graph) {
        List<E> edges = new ArrayList<>(tour.size());
        double tourWeight = 0d;

        for (int i = 1; i < tour.size(); i++) {
            V u = tour.get(i - 1);
            V v = tour.get(i);
            E e = graph.getEdge(u, v);
            edges.add(e);
            tourWeight += graph.getEdgeWeight(e);
        }
        V u = tour.get(tour.size() - 1);
        V v = tour.get(0);
        E e = graph.getEdge(u, v);
        edges.add(e);
        tourWeight += graph.getEdgeWeight(e);
        tour.add(tour.get(0));
        return new GraphWalk<>(graph, tour.get(0), tour.get(0), tour, edges, tourWeight);
    }

    /**
     * Creates a tour for a graph with 1 vertex
     *
     * @param graph
     * @return
     */
    protected GraphPath<V, E> getSingletonTour(Graph<V, E> graph) {
        assert graph.vertexSet().size() == 1;
        V start = graph.vertexSet().iterator().next();
        return new GraphWalk<>(
                graph, start, start, Collections.singletonList(start), Collections.emptyList(), 0d);
    }

    /**
     * Return weight between vertices ignoring edge direction
     *
     * @param graph the graph
     * @param v1 first vertex
     * @param v2 second vertex
     * @return weight of edge v1 to v2 or v2 to v1
     */
    protected double getDistance(Graph<V, E> graph, V v1, V v2) {
        E e = graph.getEdge(v1, v2);
        if (e == null) {
            e = graph.getEdge(v2, v1);
        }
        return graph.getEdgeWeight(e);
    }
}
