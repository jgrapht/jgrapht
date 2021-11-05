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

import java.util.*;

import static org.jgrapht.util.ArrayUtil.*;

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
 * The tour computed with a {@code Nearest-Neighbor-Heuristic} can vary depending on the first
 * vertex visited. The first vertex for the next or for multiple subsequent tour computations (calls
 * of {@link #getTour(Graph)}) can be specified in the constructors
 * {@link #NearestNeighborHeuristicTSP(Object)} or {@link #NearestNeighborHeuristicTSP(Iterable)}.
 * This can be used for example to ensure that the first vertices visited are different for
 * subsequent calls of {@code  getTour(Graph)}. Once each specified first vertex is used, the first
 * vertex in subsequent tour computations is selected randomly from the graph. Alternatively
 * {@link #NearestNeighborHeuristicTSP(Random)} or {@link #NearestNeighborHeuristicTSP(long)} can be
 * used to specify a {@code Random} used to randomly select the vertex visited first.
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
 * @author Hannes Wellmann
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
        this(null, new Random());
    }

    /**
     * Constructor
     *
     * @param first First vertex to visit
     * @throws NullPointerException if first is null
     */
    public NearestNeighborHeuristicTSP(V first)
    {
        this(
            Collections
                .singletonList(
                    Objects.requireNonNull(first, "Specified initial vertex cannot be null")),
            new Random());
    }

    /**
     * Constructor
     *
     * @param initialVertices The Iterable of vertices visited first in subsequent tour computations
     *        (per call of {@link #getTour(Graph)} another vertex of the Iterable is used as first)
     * @throws NullPointerException if first is null
     */
    public NearestNeighborHeuristicTSP(Iterable<V> initialVertices)
    {
        this(
            Objects.requireNonNull(initialVertices, "Specified initial vertices cannot be null"),
            new Random());
    }

    /**
     * Constructor
     *
     * @param seed seed for the random number generator
     */
    public NearestNeighborHeuristicTSP(long seed)
    {
        this(null, new Random(seed));
    }

    /**
     * Constructor
     *
     * @param rng Random number generator
     * @throws NullPointerException if rng is null
     */
    public NearestNeighborHeuristicTSP(Random rng)
    {
        this(null, Objects.requireNonNull(rng, "Random number generator cannot be null"));
    }

    /**
     * Constructor
     *
     * @param initialVertices The Iterable of vertices visited first in subsequent tour
     *        computations, or null to choose at random
     * @param rng Random number generator
     */
    private NearestNeighborHeuristicTSP(Iterable<V> initialVertices, Random rng)
    {
        if (initialVertices != null) {
            Iterator<V> iterator = initialVertices.iterator();
            this.initiaVertex = iterator.hasNext() ? iterator : null;
        }
        this.rng = rng;
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

        Set<V> vertexSet = graph.vertexSet();
        int n = vertexSet.size();

        @SuppressWarnings("unchecked") V[] path = (V[]) vertexSet.toArray(new Object[n + 1]);
        List<V> pathList = Arrays.asList(path); // List backed by path-array

        // move initial vertex to the beginning
        int initalIndex = getFirstVertexIndex(pathList);
        swap(path, 0, initalIndex);

        // search nearest neighbors
        int limit = n - 1; // last vertex won't be changed -> no need to check it
        for (int i = 1; i < limit; i++) {
            V v = path[i - 1];
            // path before i is established. The element at i must be the closest to element at i-1.
            // -> get nearest of remaining elements (index >= i) and set it as next in path
            int nearestNeighbor = getNearestNeighbor(v, path, i, graph);

            swap(path, i, nearestNeighbor);
        }

        path[n] = path[0]; // close tour manually. Arrays.asList does not support add
        return closedVertexListToTour(pathList, graph);
    }

    /**
     * Returns the start vertex of the tour about to compute.
     *
     * @param path the initial path, containing all vertices in unspecified order
     * @return the vertex to start with
     * @throws IllegalArgumentException if the specified initial vertex is not in the graph
     */
    private int getFirstVertexIndex(List<V> path)
    {
        if (initiaVertex != null) {
            V first = initiaVertex.next();
            if (!initiaVertex.hasNext()) {
                initiaVertex = null; // release the resource backing the iterator immediately
            }
            int initialIndex = path.indexOf(first);
            if (initialIndex < 0) {
                throw new IllegalArgumentException("Specified initial vertex is not in graph");
            }
            return initialIndex;
        } else { // first not specified
            return rng.nextInt(path.size() - 1); // path has size n+1
        }
    }

    /**
     * Find the vertex in the range staring at {@code from} that is closest to the element at index
     * from-1.
     *
     * @param current the vertex for which the nearest neighbor is searched
     * @param vertices the vertices of the graph. The unvisited neighbors start at index
     *        {@code start}
     * @param start the index of the first vertex to consider
     * @param g the graph containing the vertices
     *
     * @return the index of the unvisited vertex closest to the vertex at firstNeighbor-1.
     */
    private static <V, E> int getNearestNeighbor(V current, V[] vertices, int start, Graph<V, E> g)
    {
        int closest = -1;
        double minDist = Double.MAX_VALUE;

        int n = vertices.length - 1; // last element in vertices is null
        for (int i = start; i < n; i++) {
            V v = vertices[i];
            double vDist = g.getEdgeWeight(g.getEdge(current, v));
            if (vDist < minDist) {
                closest = i;
                minDist = vDist;
            }
        }
        return closest;
    }
}
