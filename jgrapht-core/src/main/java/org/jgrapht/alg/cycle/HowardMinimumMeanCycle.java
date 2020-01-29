/*
 * (C) Copyright 2020-2020, by Semen Chudakov and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.GabowStrongConnectivityInspector;
import org.jgrapht.alg.interfaces.MinimumCycleMeanAlgorithm;
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm;
import org.jgrapht.alg.util.ToleranceDoubleComparator;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of Howard`s algorithm for finding minimum cycle mean in a graph.
 *
 * <p>
 * The algorithm is described in the article: Ali Dasdan, Sandy S. Irani, and Rajesh K. Gupta. 1999.
 * Efficient algorithms for optimum cycle mean and optimum cost to time ratio problems.
 * In Proceedings of the 36th annual ACM/IEEE Design Automation Conference (DAC ’99).
 * Association for Computing Machinery, New York, NY, USA, 37–42.
 * DOI:https://doi.org/10.1145/309847.309862
 *
 * <p>
 * Firstly, the graph is divided into strongly connected components. The minimum
 * cycle mean is then computed as the globally minimum cycle mean over all components.
 * In the process the necessary information is recorded to be able to reconstruct the
 * cycle with minimum mean.
 *
 * <p>
 * The computations are divided into iterations. In each iteration the algorithm tries
 * to update current minimum cycle mean value. There is a possibility to limit the
 * total number of iteration via a constructor parameter.
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 * @author Semen Chudakov
 */
public class HowardMinimumMeanCycle<V, E> implements MinimumCycleMeanAlgorithm<V, E> {
    /**
     * The underlying graph.
     */
    private final Graph<V, E> graph;
    /**
     * Algorithm for computing strongly connected components in the {@code graph}.
     */
    private final StrongConnectivityAlgorithm<V, E> strongConnectivityAlgorithm;
    /**
     * Maximum number of iterations performed during the computation. If not provided
     * via constructor the value if defaulted to {@link Integer#MAX_VALUE}.
     */
    private final int maximumIterations;
    /**
     * Used to compare floating point numbers.
     */
    private final Comparator<Double> comparator;

    /**
     * Determines if a cycle is found on current iteration.
     */
    private boolean isCurrentCycleFound;
    /**
     * Total weight of a cycle found on current iteration.
     */
    private double currentCycleWeight;
    /**
     * Length of a cycle found on current iteration.
     */
    private int currentCycleLength;
    /**
     * Vertex which is used to reconstruct the cycle
     * found on current iteration.
     */
    private V currentCycleVertex;

    /**
     * Determines if the a cycle with globally minimum mean is found.
     */
    private boolean isBestCycleFound;
    /**
     * Total weight of the cycle with minimum mean.
     */
    private double bestCycleWeight;
    /**
     * Length of the cycle with the minimum mean.
     */
    private int bestCycleLength;
    /**
     * Vertex which is used to reconstruct cycle with the
     * minimum mean.
     */
    private V bestCycleVertex;

    /**
     * For each vertex contains an edge, which together
     * for the policy graph on current iteration.
     */
    private Map<V, E> policyGraph;
    /**
     * For each vertex indicates, if it has been reached by a search
     * during computing vertices distance in the policy graph.
     */
    private Map<V, Boolean> reachedVertices;
    /**
     * A DSU-like structure used to find a cycle in the policy
     * graph.
     */
    private Map<V, Integer> vertexLevel;
    /**
     * For each vertex stores its distance in the policy graph.
     */
    private Map<V, Double> vertexDistance;


    /**
     * Constructs an instance of the algorithm for the given {@code graph}.
     *
     * @param graph graph
     */
    public HowardMinimumMeanCycle(Graph<V, E> graph) {
        this(graph, Integer.MAX_VALUE);
    }

    /**
     * Constructs an instance of the algorithm for the given {@code graph}
     * and {@code maximumIterations}.
     *
     * @param graph             graph
     * @param maximumIterations maximum number of iterations
     */
    public HowardMinimumMeanCycle(Graph<V, E> graph, int maximumIterations) {
        this(graph, maximumIterations, new GabowStrongConnectivityInspector<>(graph), 1e-9);
    }

    /**
     * Constructs an instance of the algorithm for the given {@code graph},
     * {@code maximumIterations}, {@code strongConnectivityAlgorithm} and
     * {@code toleranceEpsilon}.
     *
     * @param graph                       graph
     * @param maximumIterations           maximum number of iterations
     * @param strongConnectivityAlgorithm algorithm to compute strongly connected components
     * @param toleranceEpsilon            tolerance to compare floating point numbers
     */
    public HowardMinimumMeanCycle(Graph<V, E> graph, int maximumIterations,
                                  StrongConnectivityAlgorithm<V, E> strongConnectivityAlgorithm, double toleranceEpsilon) {
        this.graph = Objects.requireNonNull(graph, "graph should not be null!");
        this.strongConnectivityAlgorithm = Objects.requireNonNull(strongConnectivityAlgorithm,
                "strongConnectivityAlgorithm should not be null!");
        if (maximumIterations < 0) {
            throw new IllegalArgumentException("maximumIterations should be non-negative");
        }
        this.maximumIterations = maximumIterations;
        this.comparator = new ToleranceDoubleComparator(toleranceEpsilon);

        this.policyGraph = CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());
        this.reachedVertices = CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());
        this.vertexLevel = CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());
        this.vertexDistance = CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());

        this.bestCycleLength = 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCycleMean() {
        GraphPath<V, E> cycle = getCycle();
        if (cycle == null) {
            return Double.POSITIVE_INFINITY;
        }
        return cycle.getWeight() / cycle.getLength();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> getCycle() {
        boolean pathFound = findMinimumCycleMean();
        if (pathFound) {
            return buildPath();
        }
        return null;
    }

    /**
     * Computes minimum mean among all cycles in the {@code graph}.
     * Returns true if a mean (not necessarily minimum) is found.
     *
     * @return true if a mean (not necessarily minimum) is found
     */
    private boolean findMinimumCycleMean() {
        int numberOfIterations = 0;
        boolean iterationsLimitReached = false;
        for (Graph<V, E> component : strongConnectivityAlgorithm.getStronglyConnectedComponents()) {
            if (!buildPolicyGraph(component)) {
                continue;
            }

            while (true) {
                if (++numberOfIterations > maximumIterations) {
                    iterationsLimitReached = true;
                    break;
                }
                findPolicyCycle(component);

                if (!computeVertexDistance(component)) {
                    break;
                }
            }

            if (isCurrentCycleFound &&
                    (!isBestCycleFound || currentCycleWeight * bestCycleLength < bestCycleWeight * currentCycleLength)) {
                isBestCycleFound = true;
                bestCycleWeight = currentCycleWeight;
                bestCycleLength = currentCycleLength;
                bestCycleVertex = currentCycleVertex;
            }

            if (iterationsLimitReached) {
                break;
            }
        }

        return isBestCycleFound;
    }

    /**
     * Computes policy graph for {@code component} and stores result in {@code policyGraph}
     * and {@code vertexDistance}. Returns true if policy graph is constructed successfully.
     *
     * @param component connected component
     * @return true if policy graph is constructed successfully
     */
    private boolean buildPolicyGraph(Graph<V, E> component) {
        if (component.vertexSet().size() == 0) {
            return false;
        }
        if (component.vertexSet().size() == 1 &&
                component.incomingEdgesOf(component.vertexSet().iterator().next()).size() == 0) {
            return false;
        }

        for (V v : component.vertexSet()) {
            vertexDistance.put(v, Double.POSITIVE_INFINITY);
        }

        for (V v : component.vertexSet()) {
            for (E e : component.incomingEdgesOf(v)) {
                V u = Graphs.getOppositeVertex(component, e, v);

                double eWeight = component.getEdgeWeight(e);
                if (eWeight < vertexDistance.get(u)) {
                    vertexDistance.put(u, eWeight);
                    policyGraph.put(u, e);
                }
            }
        }

        return true;
    }

    /**
     * Finds cycle in the {@code policyGraph} using {@code vertexLevel}.
     *
     * @param component connected component
     */
    private void findPolicyCycle(Graph<V, E> component) {
        for (V v : component.vertexSet()) {
            vertexLevel.put(v, -1);
        }

        double currentWeight;
        int currentSize;
        isCurrentCycleFound = false;
        int i = 0;
        for (V u : component.vertexSet()) {
            if (vertexLevel.get(u) >= 0) {
                continue;
            }

            while (vertexLevel.get(u) < 0) {
                vertexLevel.put(u, i);
                u = Graphs.getOppositeVertex(component, policyGraph.get(u), u);
            }

            if (vertexLevel.get(u) == i) {
                currentWeight = component.getEdgeWeight(policyGraph.get(u));
                currentSize = 1;

                for (V v = u; !(v = Graphs.getOppositeVertex(component, policyGraph.get(v), v)).equals(u); ) {
                    currentWeight += component.getEdgeWeight(policyGraph.get(v));
                    ++currentSize;
                }
                if (!isCurrentCycleFound || (currentWeight * currentCycleLength < currentCycleWeight * currentSize)) {
                    isCurrentCycleFound = true;
                    currentCycleWeight = currentWeight;
                    currentCycleLength = currentSize;
                    currentCycleVertex = u;
                }
            }
            ++i;
        }
    }


    /**
     * Contracts the {@code policyGraph} and computes distances to
     * all vertices in {@code component}. Return true if the currently best
     * mean has been improved.
     *
     * @param component connected component
     * @return if the currently best mean has been improved
     */
    private boolean computeVertexDistance(Graph<V, E> component) {
        List<V> queue = new ArrayList<>(Collections.nCopies(graph.vertexSet().size(), null));

        for (V v : component.vertexSet()) {
            reachedVertices.put(v, false);
        }

        int queueFrontIndex = 0;
        int queueBackIndex = 0;
        queue.set(0, currentCycleVertex);
        reachedVertices.put(currentCycleVertex, true);
        vertexDistance.put(currentCycleVertex, 0.0);

        while (queueFrontIndex <= queueBackIndex) {
            V v = queue.get(queueFrontIndex++);
            for (E e : component.incomingEdgesOf(v)) {
                V u = Graphs.getOppositeVertex(component, e, v);
                if (policyGraph.get(u).equals(e) && !reachedVertices.get(u)) {
                    reachedVertices.put(u, true);
                    vertexDistance.put(u, vertexDistance.get(v) + component.getEdgeWeight(e) * currentCycleLength - currentCycleWeight);
                    queue.set(++queueBackIndex, u);
                }
            }
        }

        queueFrontIndex = 0;
        while (queueBackIndex < component.vertexSet().size() - 1) {
            V v = queue.get(queueFrontIndex++);
            for (E e : component.incomingEdgesOf(v)) {
                V u = Graphs.getOppositeVertex(component, e, v);
                if (!reachedVertices.get(u)) {
                    reachedVertices.put(u, true);
                    policyGraph.put(u, e);
                    vertexDistance.put(u, vertexDistance.get(v) + component.getEdgeWeight(e) * currentCycleLength - currentCycleWeight);
                    ++queueBackIndex;
                    queue.set(queueBackIndex, u);
                }
            }
        }

        boolean improved = false;
        for (V v : component.vertexSet()) {
            for (E e : component.incomingEdgesOf(v)) {
                V u = Graphs.getOppositeVertex(component, e, v);

                double delta = vertexDistance.get(v) + component.getEdgeWeight(e) * currentCycleLength - currentCycleWeight;

                if (comparator.compare(delta, vertexDistance.get(u)) < 0) {
                    vertexDistance.put(u, delta);
                    policyGraph.put(u, e);
                    improved = true;
                }
            }
        }
        return improved;
    }

    private GraphPath<V, E> buildPath() {
        if (!isBestCycleFound) {
            return null;
        }
        List<E> pathEdges = new ArrayList<>(bestCycleLength);
        List<V> pathVertices = new ArrayList<>(bestCycleLength + 1);

        V v = bestCycleVertex;
        pathVertices.add(bestCycleVertex);
        do {
            E e = policyGraph.get(v);
            v = Graphs.getOppositeVertex(graph, e, v);

            pathEdges.add(e);
            pathVertices.add(v);

        } while (!v.equals(bestCycleVertex));

        return new GraphWalk<>(graph, bestCycleVertex, bestCycleVertex, pathVertices, pathEdges, bestCycleWeight);
    }
}
