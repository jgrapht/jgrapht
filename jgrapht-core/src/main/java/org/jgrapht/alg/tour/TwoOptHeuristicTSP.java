/*
 * (C) Copyright 2018-2020, by Dimitrios Michail and Contributors.
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

import java.util.*;

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
 * This is an implementation of the 2-opt improvement heuristic algorithm. The algorithm generates k
 * initial tours and then iteratively improves the tours until a local minimum is reached. In each
 * iteration it applies the best possible 2-opt move which means to find the best pair of edges
 * $(i,i+1)$ and $(j,j+1)$ such that replacing them with $(i,j)$ and $(i+1,j+1)$ minimizes the tour
 * length. The default initial tours use RandomTour, however an alternative algorithm can be
 * provided to create the initial tour. Initial tours generated using NearestNeighborHeuristicTSP
 * give good results and performance.
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
 */
public class TwoOptHeuristicTSP<V, E>
    extends
    HamiltonianCycleAlgorithmBase<V, E>
    implements
    HamiltonianCycleImprovementAlgorithm<V, E>
{
    private int passes;
    private HamiltonianCycleAlgorithm<V, E> initializer;
    private double minCostImprovement;

    private Graph<V, E> graph;
    private int n;
    private double[][] dist;
    private Map<V, Integer> index;
    private Map<Integer, V> revIndex;

    /**
     * Constructor. By default one initial random tour is used.
     */
    public TwoOptHeuristicTSP()
    {
        this.passes = 1;
        this.initializer = new RandomTourTSP<>();
        this.minCostImprovement = 1e-8;
    }

    /**
     * Constructor
     *
     * @param k how many initial random tours to check
     * @deprecated use {@link #TwoOptHeuristicTSP()} and {@link #setPasses(int)}
     */
    @Deprecated(since = "1.4.1", forRemoval = true)
    public TwoOptHeuristicTSP(int k)
    {
        this(k, new Random());
    }

    /**
     * Constructor
     *
     * @param k how many initial random tours to check
     * @param seed seed for the random number generator
     * @deprecated use {@link #TwoOptHeuristicTSP()} , {@link #setPasses(int)} and
     *             {@link #setInitializer(HamiltonianCycleAlgorithm)}
     */
    @Deprecated(since = "1.4.1", forRemoval = true)
    public TwoOptHeuristicTSP(int k, long seed)
    {
        this(k, new Random(seed));
    }

    /**
     * Constructor
     *
     * @param k how many initial random tours to check
     * @param rng random number generator
     * @deprecated use {@link #TwoOptHeuristicTSP()} , {@link #setPasses(int)} and
     *             {@link #setInitializer(HamiltonianCycleAlgorithm)}
     */
    @Deprecated(since = "1.4.1", forRemoval = true)
    public TwoOptHeuristicTSP(int k, Random rng)
    {
        this(k, new RandomTourTSP<>(rng));
    }

    /**
     * Constructor
     *
     * @param k how many initial random tours to check
     * @param rng random number generator
     * @param minCostImprovement Minimum cost improvement per iteration
     * @deprecated use {@link #TwoOptHeuristicTSP()} , {@link #setPasses(int)},
     *             {@link #setInitializer(HamiltonianCycleAlgorithm)} and
     *             {@link #setMinCostImprovement(double)}
     */
    @Deprecated(since = "1.4.1", forRemoval = true)
    public TwoOptHeuristicTSP(int k, Random rng, double minCostImprovement)
    {
        this(k, new RandomTourTSP<>(rng), minCostImprovement);
    }

    /**
     * Constructor
     *
     * @param initializer Algorithm to generate initial tour
     * @deprecated use {@link #TwoOptHeuristicTSP()} and
     *             {@link #setInitializer(HamiltonianCycleAlgorithm)}
     */
    @Deprecated(since = "1.4.1", forRemoval = true)
    public TwoOptHeuristicTSP(HamiltonianCycleAlgorithm<V, E> initializer)
    {
        this(1, initializer);
    }

    /**
     * Constructor
     *
     * @param k how many initial tours to check
     * @param initializer Algorithm to generate initial tour
     * @deprecated use {@link #TwoOptHeuristicTSP()} , {@link #setPasses(int)} and
     *             {@link #setInitializer(HamiltonianCycleAlgorithm)}
     */
    @Deprecated(since = "1.4.1", forRemoval = true)
    public TwoOptHeuristicTSP(int k, HamiltonianCycleAlgorithm<V, E> initializer)
    {
        this(k, initializer, 1e-8);
    }

    /**
     * Constructor
     *
     * @param k how many initial tours to check
     * @param initializer Algorithm to generate initial tours
     * @param minCostImprovement Minimum cost improvement per iteration
     * @deprecated use {@link #TwoOptHeuristicTSP()} , {@link #setPasses(int)},
     *             {@link #setInitializer(HamiltonianCycleAlgorithm)} and
     *             {@link #setMinCostImprovement(double)}
     */
    @Deprecated(since = "1.4.1", forRemoval = true)
    public TwoOptHeuristicTSP(
        int k, HamiltonianCycleAlgorithm<V, E> initializer, double minCostImprovement)
    {
        setPasses(k);
        setInitializer(initializer);
        setMinCostImprovement(minCostImprovement);
    }

    // setters

    /**
     * Set the number of passes done in {@link #getTour(Graph)}.
     * <p>
     * In each pass an initial tour is computed with the
     * {@link #setInitializer(HamiltonianCycleAlgorithm) initializer}, which is then improved with
     * this {@code TwoOptHeuristicTSP}. The result with the lowest path costs respectively path
     * length of all passes is returned as the final result of {@code getTour(Graph)}.
     * </p>
     *
     * @param passes the number of passes done in {@code getTour(Graph)}
     * @return this algorithm object
     */
    public TwoOptHeuristicTSP<V, E> setPasses(int passes)
    {
        if (passes < 1) {
            throw new IllegalArgumentException("passes must be at least one");
        }
        this.passes = passes;
        return this;
    }

    /**
     * Set the {@link HamiltonianCycleAlgorithm} used to compute the initial tour in each pass.
     *
     * @param initializer the {@code HamiltonianCycleAlgorithm} to compute the initial tour
     * @return this algorithm object
     */
    public TwoOptHeuristicTSP<V, E> setInitializer(HamiltonianCycleAlgorithm<V, E> initializer)
    {
        this.initializer =
            Objects.requireNonNull(initializer, "Initial solver algorithm cannot be null");
        return this;
    }

    /**
     * Set the required {@code minimum cost improvement} a tour modification must produce in order
     * to be considered as improvement.
     * <p>
     * A value to close to zero can cause an endless loop.
     * </p>
     *
     * @param minCostImprovement the minimal cost improvement a tour move must produce
     * @return this algorithm object
     */
    public TwoOptHeuristicTSP<V, E> setMinCostImprovement(double minCostImprovement)
    {
        this.minCostImprovement = Math.abs(minCostImprovement);
        return this;
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

        // Execute 2-opt from k random permutations
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
        this.index = new HashMap<>();
        this.revIndex = new HashMap<>();
        int i = 0;
        for (V v : graph.vertexSet()) {
            index.put(v, i);
            revIndex.put(i, v);
            i++;
        }

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
        int[] newTour = new int[n + 1];
        boolean moved;
        double minChange;
        do {
            moved = false;
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
                // apply move
                int a = 0;
                for (int k = 0; k <= mini; k++) {
                    newTour[a++] = tour[k];
                }
                for (int k = minj; k >= mini + 1; k--) {
                    newTour[a++] = tour[k];
                }
                for (int k = minj + 1; k < n + 1; k++) {
                    newTour[a++] = tour[k];
                }
                // swap tours
                int[] tmp = tour;
                tour = newTour;
                newTour = tmp;
                moved = true;
            }
        } while (moved);

        return tour;
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
        Set<V> visited = new HashSet<>();
        int i = 0;
        int[] tour = new int[n + 1];
        V v = path.getStartVertex();
        tour[i++] = index.get(v);
        for (E e : path.getEdgeList()) {
            v = Graphs.getOppositeVertex(graph, e, v);
            if (!visited.add(v)) {
                throw new IllegalArgumentException("Not a valid tour");
            }
            tour[i++] = index.get(v);
        }
        if (i < n + 1) {
            throw new IllegalArgumentException("Not a valid tour");
        }
        return tour;
    }
}
