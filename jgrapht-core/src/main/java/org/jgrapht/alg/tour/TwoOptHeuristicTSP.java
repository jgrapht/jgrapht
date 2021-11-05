/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
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
import org.jgrapht.util.*;

import java.util.*;

import static org.jgrapht.util.ArrayUtil.*;

/**
 * The 2-opt heuristic algorithm for the TSP problem.
 *
 * <p>
 * The travelling salesman problem (TSP) asks the following question: "Given a list of cities and
 * the distances between each pair of cities, what is the shortest possible route that visits each
 * city exactly once and returns to the origin city?".
 * </p>
 *
 * <p>
 * This is an implementation of the 2-opt improvement heuristic algorithm. The algorithm generates
 * <em>passes</em> initial tours and then iteratively improves the tours until a local minimum is
 * reached. In each iteration it applies the best possible 2-opt move which means to find the best
 * pair of edges $(i,i+1)$ and $(j,j+1)$ such that replacing them with $(i,j)$ and $(i+1,j+1)$
 * minimizes the tour length. The default initial tours use RandomTour, however an alternative
 * algorithm can be provided to create the initial tour. Initial tours generated using
 * NearestNeighborHeuristicTSP give good results and performance.
 * </p>
 *
 * <p>
 * See <a href="https://en.wikipedia.org/wiki/2-opt">wikipedia</a> for more details.
 *
 * <p>
 * This implementation can also be used in order to try to improve an existing tour. See method
 * {@link #improveTour(GraphPath)}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 * @author Hannes Wellmann
 */
public class TwoOptHeuristicTSP<V, E>
    extends
    HamiltonianCycleAlgorithmBase<V, E>
    implements
    HamiltonianCycleImprovementAlgorithm<V, E>
{
    private final int passes;
    private final HamiltonianCycleAlgorithm<V, E> initializer;
    private final double minCostImprovement;

    private Graph<V, E> graph;
    private int n;
    private double[][] dist;
    private Map<V, Integer> index;
    private List<V> revIndex;

    /**
     * Constructor. By default one initial random tour is used.
     */
    public TwoOptHeuristicTSP()
    {
        this(1, new Random());
    }

    /**
     * Constructor
     *
     * @param passes how many initial random tours to check
     */
    public TwoOptHeuristicTSP(int passes)
    {
        this(passes, new Random());
    }

    /**
     * Constructor
     *
     * @param passes how many initial random tours to check
     * @param seed seed for the random number generator
     */
    public TwoOptHeuristicTSP(int passes, long seed)
    {
        this(passes, new Random(seed));
    }

    /**
     * Constructor
     *
     * @param passes how many initial random tours to check
     * @param rng random number generator
     */
    public TwoOptHeuristicTSP(int passes, Random rng)
    {
        this(passes, new RandomTourTSP<>(rng));
    }

    /**
     * Constructor
     *
     * @param passes how many initial random tours to check
     * @param rng random number generator
     * @param minCostImprovement Minimum cost improvement per iteration
     */
    public TwoOptHeuristicTSP(int passes, Random rng, double minCostImprovement)
    {
        this(passes, new RandomTourTSP<>(rng), minCostImprovement);
    }

    /**
     * Constructor
     *
     * @param initializer Algorithm to generate initial tour
     */
    public TwoOptHeuristicTSP(HamiltonianCycleAlgorithm<V, E> initializer)
    {
        this(1, initializer);
    }

    /**
     * Constructor
     *
     * @param passes how many initial tours to check
     * @param initializer Algorithm to generate initial tour
     */
    public TwoOptHeuristicTSP(int passes, HamiltonianCycleAlgorithm<V, E> initializer)
    {
        this(passes, initializer, 1e-8);
    }

    /**
     * Constructor
     *
     * @param passes how many initial tours to check
     * @param initializer Algorithm to generate initial tours
     * @param minCostImprovement Minimum cost improvement per iteration
     */
    public TwoOptHeuristicTSP(
        int passes, HamiltonianCycleAlgorithm<V, E> initializer, double minCostImprovement)
    {
        if (passes < 1) {
            throw new IllegalArgumentException("passes must be at least one");
        }
        this.passes = passes;
        this.initializer =
            Objects.requireNonNull(initializer, "Initial solver algorithm cannot be null");
        this.minCostImprovement = Math.abs(minCostImprovement);
    }

    // algorithm

    /**
     * Computes a 2-approximate tour.
     *
     * @param graph the input graph
     * @return a tour
     * @throws IllegalArgumentException if the graph is not undirected
     * @throws IllegalArgumentException if the graph is not complete
     * @throws IllegalArgumentException if the graph contains no vertices
     */
    @Override
    public GraphPath<V, E> getTour(Graph<V, E> graph)
    {
        checkGraph(graph);
        if (graph.vertexSet().size() == 1) {
            return getSingletonTour(graph);
        }

        // Initialize vertex index and distances
        init(graph);

        // Execute 2-opt for the specified number of passes and a new permutation in each pass
        GraphPath<V, E> best = tourToPath(improve(createInitialTour()));
        for (int i = 1; i < passes; i++) {
            GraphPath<V, E> other = tourToPath(improve(createInitialTour()));
            if (other.getWeight() < best.getWeight()) {
                best = other;
            }
        }
        return best;
    }

    /**
     * Try to improve a tour by running the 2-opt heuristic.
     *
     * @param tour a tour
     * @return a possibly improved tour
     */
    @Override
    public GraphPath<V, E> improveTour(GraphPath<V, E> tour)
    {
        init(tour.getGraph());
        return tourToPath(improve(pathToTour(tour)));
    }

    /**
     * Initialize graph and mapping to integer vertices.
     *
     * @param graph the input graph
     */
    private void init(Graph<V, E> graph)
    {
        this.graph = graph;
        this.n = graph.vertexSet().size();
        this.dist = new double[n][n];
        VertexToIntegerMapping<V> vertex2index = new VertexToIntegerMapping<>(graph.vertexSet());
        this.index = vertex2index.getVertexMap();
        this.revIndex = vertex2index.getIndexList();

        for (E e : graph.edgeSet()) {
            V s = graph.getEdgeSource(e);
            int si = index.get(s);
            V t = graph.getEdgeTarget(e);
            int ti = index.get(t);

            double weight = graph.getEdgeWeight(e);
            dist[si][ti] = weight;
            dist[ti][si] = weight;
        }
    }

    /**
     * Create an initial tour
     *
     * @return a complete tour
     */
    private int[] createInitialTour()
    {
        return pathToTour(initializer.getTour(graph));
    }

    /**
     * Improve the tour using the 2-opt heuristic. In each iteration it applies the best possible
     * 2-opt move which means to find the best pair of edges $(i,i+1)$ and $(j,j+1)$ such that
     * replacing them with $(i,j)$ and $(i+1,j+1)$ minimizes the tour length.
     *
     * <p>
     * The returned array instance might or might not be the input array.
     *
     * @param tour the input tour
     * @return a possibly improved tour
     */
    private int[] improve(int[] tour)
    {
        double minChange;
        while (true) {
            minChange = -minCostImprovement;
            int mini = -1;
            int minj = -1;
            for (int i = 0; i < n - 2; i++) {
                for (int j = i + 2; j < n; j++) {
                    int ci = tour[i];
                    int ci1 = tour[i + 1];
                    int cj = tour[j];
                    int cj1 = tour[j + 1];
                    double change = dist[ci][cj] + dist[ci1][cj1] - dist[ci][ci1] - dist[cj][cj1];
                    if (change < minChange) {
                        minChange = change;
                        mini = i;
                        minj = j;
                    }
                }
            }
            if (mini != -1 && minj != -1) {
                // apply move: reverse path from mini+1 to minj (both inclusive)
                reverse(tour, mini + 1, minj);
            } else {
                return tour;
            }
        }
    }

    /**
     * Transform from an array representation to a graph path.
     *
     * @param tour an array containing the index of the vertices of the tour
     * @return a graph path
     */
    private GraphPath<V, E> tourToPath(int[] tour)
    {
        List<V> tourVertices = new ArrayList<>(n + 1);
        for (int vi : tour) {
            V v = revIndex.get(vi);
            tourVertices.add(v);
        }
        return closedVertexListToTour(tourVertices, graph);
    }

    /**
     * Transform from a path representation to an array representation.
     *
     * @param path graph path
     * @return an array containing the index of the vertices of the tour
     */
    private int[] pathToTour(GraphPath<V, E> path)
    {
        boolean[] visited = new boolean[n];

        List<V> vertexList = path.getVertexList(); // first and last element are the starting vertex
        if (vertexList.size() != n + 1) {
            throw new IllegalArgumentException("Not a valid tour");
        }

        int[] tour = new int[n + 1];
        for (int i = 0; i < n; i++) {
            int vi = index.get(vertexList.get(i));
            if (visited[vi]) {
                throw new IllegalArgumentException("Not a valid tour");
            }
            visited[vi] = true;
            tour[i] = vi;
        }
        tour[n] = tour[0];
        return tour;
    }
}
