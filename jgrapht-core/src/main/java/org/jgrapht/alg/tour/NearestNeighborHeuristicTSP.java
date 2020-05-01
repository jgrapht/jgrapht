/*
 * (C) Copyright 2019-2020, by Peter Harman and Contributors.
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
import org.jgrapht.util.*;

import java.util.*;

/**
 * The nearest neighbour heuristic algorithm for the TSP problem.
 *
 * <p>
 * The travelling salesman problem (TSP) asks the following question: "Given a list of cities and
 * the distances between each pair of cities, what is the shortest possible route that visits each
 * city exactly once and returns to the origin city?".
 * </p>
 *
 * <p>
 * This is perhaps the simplest and most straightforward TSP heuristic. The key to this algorithm is
 * to always visit the nearest city.
 * </p>
 *
 * <p>
 * The tour computed with a {@code Nearest-Neighbor-Heuristic} can vary depending on their first
 * vertex visited. The first vertices for the next or for multiple subsequent tour computations can
 * be controlled with {@link #setFirst(Object)} or {@link #setInitialVertices(Iterable)}.
 * Additionally the {@link #setRandomNumberGenerator(Random) random number generator} can be
 * specified used to randomly select the vertex visited first.
 * </p>
 *
 * <p>
 * The implementation of this class is based on: <br>
 * Nilsson, Christian. "Heuristics for the traveling salesman problem." Linkoping University 38
 * (2003)
 * </p>
 *
 * <p>
 * The runtime complexity of this class is $O(V^2)$.
 * </p>
 *
 * <p>
 * This algorithm requires that the graph is complete.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Peter Harman
 */
public class NearestNeighborHeuristicTSP<V, E>
    extends
    HamiltonianCycleAlgorithmBase<V, E>
{

    private Random rng;
    /** Nulled, if it has no next */
    private Iterator<V> initiaVertex;

    /**
     * Constructor. By default a random vertex is chosen to start.
     */
    public NearestNeighborHeuristicTSP()
    {
        this.rng = new Random();
        this.initiaVertex = null;
    }

    /**
     * Constructor
     *
     * @param first First vertex to visit, or null to choose at random
     * @throws NullPointerException if first is null
     * @deprecated use {@link #NearestNeighborHeuristicTSP()} and {@link #setFirst(Object)}
     *
     */
    @Deprecated(since = "1.4.1", forRemoval = true)
    public NearestNeighborHeuristicTSP(V first)
    {
        this(
            Objects.requireNonNull(first, "Specified initial vertex cannot be null"), new Random());
    }

    /**
     * Constructor
     *
     * @param seed seed for the random number generator
     * @deprecated use {@link #NearestNeighborHeuristicTSP()} and
     *             {@link #setRandomNumberGenerator(Random)}
     */
    @Deprecated(since = "1.4.1", forRemoval = true)
    public NearestNeighborHeuristicTSP(long seed)
    {
        this(null, new Random(seed));
    }

    /**
     * Constructor
     *
     * @param rng Random number generator
     * @throws NullPointerException if rng is null
     * @deprecated use {@link #NearestNeighborHeuristicTSP()} and
     *             {@link #setRandomNumberGenerator(Random)}
     */
    @Deprecated(since = "1.4.1", forRemoval = true)
    public NearestNeighborHeuristicTSP(Random rng)
    {
        this(null, Objects.requireNonNull(rng, "Random number generator cannot be null"));
    }

    /**
     * Constructor
     *
     * @param first First vertex to visit, or null to choose at random
     * @param rng Random number generator
     * @deprecated use {@link #NearestNeighborHeuristicTSP()},
     *             {@link #setRandomNumberGenerator(Random)} and {@link #setFirst(Object)}
     */
    @Deprecated(since = "1.4.1", forRemoval = true)
    private NearestNeighborHeuristicTSP(V first, Random rng)
    {
        if (first != null) {
            setInitialVertices(Collections.singletonList(first));
        }
        this.rng = rng;
    }

    // setters

    /**
     * Set the {@link Random} used to obtain the initial vertex for subsequent tour computations.
     * <p>
     * Vertices previously provided via {@link #setFirst(Object)} or
     * {@link #setInitialVertices(Iterable)} are discarded.
     * <p>
     *
     * @param rng the {@code Random} to use
     * @return this algorithm object
     */
    public NearestNeighborHeuristicTSP<V, E> setRandomNumberGenerator(Random rng)
    {
        this.rng = rng;
        this.initiaVertex = null;
        return this;
    }

    /**
     * Set the {@code initial vertex} visited first in the next tour computation
     * ({@link #getTour(Graph)}).
     * <p>
     * For the tour computation after the next the current {@link #setRandomNumberGenerator(Random)
     * Random} is used to randomly select a first vertex, if no initial vertex is specified in the
     * meantime.
     * </p>
     * <p>
     * Passing {@code null} discards all vertices provided previously via {@link #setFirst(Object)}
     * or {@link #setInitialVertices(Iterable)}.
     * </p>
     *
     * @param first the vertices visited first in the next tour computation ({@link #getTour(Graph)}
     * @return this algorithm object
     */
    public NearestNeighborHeuristicTSP<V, E> setFirst(V first)
    {
        return setInitialVertices(
            first != null ? Collections.singleton(first) : Collections.emptyList());
    }

    /**
     * Set the {@code initial vertices} visited first in the subsequent tour computations.
     * <p>
     * Each vertex returned by the {@link Iterator} of the given {@code initialVertices} is used as
     * first vertex in a separate subsequent call to {@link #getTour(Graph)}. Once the obtained
     * iterator is exhausted, the current {@link #setRandomNumberGenerator(Random) Random} is used
     * to randomly select a first vertex.
     * </p>
     * <p>
     * This method can be used to specify the first visited vertex for multiple subsequent calls of
     * {@code  getTour(Graph)}, for example to ensure that the first vertices visited are different.
     * </p>
     *
     * @param initialVertices the vertices visited first in separate subsequent tour computations
     * @return this algorithm object
     */
    public NearestNeighborHeuristicTSP<V, E> setInitialVertices(Iterable<V> initialVertices)
    {
        Objects.requireNonNull(initialVertices, "Specified initial vertices cannot be null");
        Iterator<V> iterator = initialVertices.iterator();
        this.initiaVertex = iterator.hasNext() ? iterator : null;
        return this;
    }

    // algorithm

    /**
     * Computes a tour using the nearest neighbour heuristic.
     *
     * @param graph the input graph
     * @return a tour
     * @throws IllegalArgumentException if the graph is not undirected
     * @throws IllegalArgumentException if the graph is not complete
     * @throws IllegalArgumentException if the graph contains no vertices
     * @throws IllegalArgumentException if the specified initial vertex is not in the graph
     */
    @Override
    public GraphPath<V, E> getTour(Graph<V, E> graph)
    {
        checkGraph(graph);
        if (graph.vertexSet().size() == 1) {
            return getSingletonTour(graph);
        }
        // Create Set to contain all but first vertex
        Set<V> unvisited = new HashSet<>(graph.vertexSet());
        // Get the initial vertex
        V current = first(graph);
        unvisited.remove(current);
        // Create List to store the tour
        List<V> visited = new ArrayList<>(unvisited.size() + 1);
        visited.add(current);
        // Iterate until tour is complete
        while (!unvisited.isEmpty()) {
            // Find the nearest vertex and add to the tour
            current = nearest(current, unvisited, graph);
            visited.add(current);
        }
        return vertexListToTour(visited, graph);
    }

    /**
     * Get or determine the first vertex
     *
     * @param graph The graph
     * @return A suitable vertex to start
     * @throws IllegalArgumentException if the specified initial vertex is not in the graph
     */
    private V first(Graph<V, E> graph)
    {
        if (initiaVertex != null) { // not null means hasNext
            V first = initiaVertex.next();
            if (!graph.vertexSet().contains(first)) {
                throw new IllegalArgumentException("Specified initial vertex is not in graph");
            }
            if (!initiaVertex.hasNext()) {
                initiaVertex = null; // release resources as soon as possible
            }
            return first;
        } else {
            Set<V> vertices = graph.vertexSet();
            return CollectionUtil.getElement(vertices, rng.nextInt(vertices.size()));
        }
    }

    /**
     * Find the nearest unvisited vertex
     *
     * @param current The last vertex visited
     * @param unvisited Vertices not visited
     * @param graph The graph
     * @return The closest available vertex
     */
    private V nearest(V current, Set<V> unvisited, Graph<V, E> graph)
    {
        Iterator<V> it = unvisited.iterator();
        V closest = it.next();
        double minDist = graph.getEdgeWeight(graph.getEdge(current, closest));
        while (it.hasNext()) {
            V v = it.next();
            double vDist = graph.getEdgeWeight(graph.getEdge(current, v));
            if (vDist < minDist) {
                closest = v;
                minDist = vDist;
            }
        }
        unvisited.remove(closest);
        return closest;
    }
}
