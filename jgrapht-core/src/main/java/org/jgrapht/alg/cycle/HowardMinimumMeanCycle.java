/*
 * (C) Copyright 2020-2021, by Semen Chudakov and Contributors.
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of Howard`s algorithm for finding minimum cycle mean in a graph.
 *
 * <p>
 * The algorithm is described in the article: Ali Dasdan, Sandy S. Irani, and Rajesh K. Gupta. 1999.
 * Efficient algorithms for optimum cycle mean and optimum cost to time ratio problems. In
 * Proceedings of the 36th annual ACM/IEEE Design Automation Conference (DAC ’99). Association for
 * Computing Machinery, New York, NY, USA, 37–42. DOI:https://doi.org/10.1145/309847.309862
 *
 * <p>
 * Firstly, the graph is divided into strongly connected components. The minimum cycle mean is then
 * computed as the globally minimum cycle mean over all components. In the process the necessary
 * information is recorded to be able to reconstruct the cycle with minimum mean.
 *
 * <p>
 * The computations are divided into iterations. In each iteration the algorithm tries to update
 * current minimum cycle mean value. There is a possibility to limit the total number of iteration
 * via a constructor parameter.
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 * @author Semen Chudakov
 */
public class HowardMinimumMeanCycle<V, E>
    implements
    MinimumCycleMeanAlgorithm<V, E>
{
    /**
     * The underlying graph.
     */
    private final Graph<V, E> graph;
    /**
     * Algorithm for computing strongly connected components in the {@code graph}.
     */
    private final StrongConnectivityAlgorithm<V, E> strongConnectivityAlgorithm;
    /**
     * Maximum number of iterations performed during the computation. If not provided via
     * constructor the value if defaulted to {@link Integer#MAX_VALUE}.
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
     * Vertex which is used to reconstruct the cycle found on current iteration.
     */
    private V currentCycleVertex;

    /**
     * For each vertex contains an edge, which together for the policy graph on current iteration.
     */
    private Map<V, E> policyGraph;
    /**
     * For each vertex indicates, if it has been reached by a search during computing vertices
     * distance in the policy graph.
     */
    private Map<V, Boolean> reachedVertices;
    /**
     * For each vertex stores its level which is used to find a cycle in the policy graph.
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
    public HowardMinimumMeanCycle(Graph<V, E> graph)
    {
        this(graph, Integer.MAX_VALUE);
    }

    /**
     * Constructs an instance of the algorithm for the given {@code graph} and
     * {@code maximumIterations}.
     *
     * @param graph graph
     * @param maximumIterations maximum number of iterations
     */
    public HowardMinimumMeanCycle(Graph<V, E> graph, int maximumIterations)
    {
        this(graph, maximumIterations, new GabowStrongConnectivityInspector<>(graph), 1e-9);
    }

    /**
     * Constructs an instance of the algorithm for the given {@code graph},
     * {@code maximumIterations}, {@code strongConnectivityAlgorithm} and {@code toleranceEpsilon}.
     *
     * @param graph graph
     * @param maximumIterations maximum number of iterations
     * @param strongConnectivityAlgorithm algorithm to compute strongly connected components
     * @param toleranceEpsilon tolerance to compare floating point numbers
     */
    public HowardMinimumMeanCycle(
        Graph<V, E> graph, int maximumIterations,
        StrongConnectivityAlgorithm<V, E> strongConnectivityAlgorithm, double toleranceEpsilon)
    {
        this.graph = Objects.requireNonNull(graph, "graph should not be null!");
        this.strongConnectivityAlgorithm = Objects
            .requireNonNull(
                strongConnectivityAlgorithm, "strongConnectivityAlgorithm should not be null!");
        if (maximumIterations < 0) {
            throw new IllegalArgumentException("maximumIterations should be non-negative");
        }
        this.maximumIterations = maximumIterations;
        this.comparator = new ToleranceDoubleComparator(toleranceEpsilon);

        this.policyGraph = CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());
        this.reachedVertices = CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());
        this.vertexLevel = CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());
        this.vertexDistance = CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCycleMean()
    {
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
    public GraphPath<V, E> getCycle()
    {
        // best cycle information
        boolean isBestCycleFound = false;
        double bestCycleWeight = 0.0;
        int bestCycleLength = 1;
        V bestCycleVertex = null;

        // search for best cycle over strongly connected components separately
        int numberOfIterations = 0;
        for (Graph<V, E> component : strongConnectivityAlgorithm.getStronglyConnectedComponents()) {
            // special case: connected component is empty
            // or contains one vertex with no incoming edges
            boolean skip = component.vertexSet().size() == 0;
            skip |= component.vertexSet().size() == 1
                && component.incomingEdgesOf(component.vertexSet().iterator().next()).size() == 0;
            if (skip) {
                continue;
            }

            constructPolicyGraph(component);

            // try to improve currently best cycle
            boolean improved = true;
            while (numberOfIterations < maximumIterations && improved) {
                constructCycle(component);

                improved = computeVertexDistance(component);

                ++numberOfIterations;
            }

            // update best cycle information if necessary
            if (isCurrentCycleFound && (!isBestCycleFound
                || currentCycleWeight * bestCycleLength < bestCycleWeight * currentCycleLength))
            {
                isBestCycleFound = true;
                bestCycleWeight = currentCycleWeight;
                bestCycleLength = currentCycleLength;
                bestCycleVertex = currentCycleVertex;
            }

            // iterations limit reached
            if (numberOfIterations == maximumIterations) {
                break;
            }
        }

        if (isBestCycleFound) {
            return buildPath(bestCycleVertex, bestCycleLength, bestCycleWeight);
        }
        // no cycle found in the graph
        return null;
    }

    /**
     * Computes policy graph for {@code component} and stores result in {@code policyGraph} and
     * {@code vertexDistance}. For every vertex in the policy graph an edge with the minimum weight
     * is retained in the policy graph.
     *
     * @param component connected component
     */
    private void constructPolicyGraph(Graph<V, E> component)
    {
        for (V v : component.vertexSet()) {
            vertexDistance.put(v, Double.POSITIVE_INFINITY);
        }

        for (V u : component.vertexSet()) {
            for (E e : component.incomingEdgesOf(u)) {
                V v = Graphs.getOppositeVertex(component, e, u);

                double eWeight = component.getEdgeWeight(e);
                if (eWeight < vertexDistance.get(v)) {
                    vertexDistance.put(v, eWeight);
                    policyGraph.put(v, e);
                }
            }
        }
    }

    /**
     * Finds cycle in the {@code policyGraph} and computes computes its mean. The found cycle is
     * identified by a vertex {@code currentCycleVertex}. The cycle returned by this method does not
     * necessarily has the smalles mean over all cycles in the policy graph.
     *
     * <p>
     * To find cycles this methods assigns a level to each vertex. Initially every vertex has a
     * level equal to $-1$ which means that the vertex has not been visited. During the computations
     * this method starts DFS from every not visited vertex and assigns a unique positive level $l$
     * to every traversed vertex. If DFS comes across a vertex with level $l$ this indicates that a
     * cycle has been detected.
     *
     * @param component connected component
     */
    private void constructCycle(Graph<V, E> component)
    {
        for (V v : component.vertexSet()) {
            vertexLevel.put(v, -1);
        }

        isCurrentCycleFound = false;
        int currentCycleLevel = 0;
        double currentWeight;
        int currentSize;
        for (V u : component.vertexSet()) {
            if (vertexLevel.get(u) >= 0) { // vertex is already belongs to a cycle
                continue;
            }

            // run DFS
            while (vertexLevel.get(u) < 0) {
                vertexLevel.put(u, currentCycleLevel);
                u = Graphs.getOppositeVertex(component, policyGraph.get(u), u);
            }

            // check if a cycle has been found
            if (vertexLevel.get(u) == currentCycleLevel) {
                currentWeight = component.getEdgeWeight(policyGraph.get(u));
                currentSize = 1;

                // compute weight and length of the found cycle
                V v = Graphs.getOppositeVertex(component, policyGraph.get(u), u);
                while (!v.equals(u)) {
                    currentWeight += component.getEdgeWeight(policyGraph.get(v));
                    ++currentSize;

                    v = Graphs.getOppositeVertex(component, policyGraph.get(v), v);
                }

                // update minimum mean value
                if (!isCurrentCycleFound
                    || (currentWeight * currentCycleLength < currentCycleWeight * currentSize))
                {
                    isCurrentCycleFound = true;
                    currentCycleWeight = currentWeight;
                    currentCycleLength = currentSize;
                    currentCycleVertex = u;
                }
            }
            ++currentCycleLevel;
        }
    }

    /**
     * This method runs the reverted BFS starting from {@code currentCycleVertex} to update data in
     * {@code policyGraph} and {@code vertexDistance}. This step is needed to identify if current
     * value of minimum mean is optimal for the {@code graph}. This method also uses
     * {@code comparator} to find out if update value of minium mean is sufficiently smaller than
     * the previous one.
     *
     * @param component connected component
     * @return if the currently best mean has been improved
     */
    private boolean computeVertexDistance(Graph<V, E> component)
    {
        // BFS queue
        Deque<V> queue = new ArrayDeque<>();
        for (V v : component.vertexSet()) {
            reachedVertices.put(v, false);
        }
        queue.addLast(currentCycleVertex);
        reachedVertices.put(currentCycleVertex, true);

        double currentMean = currentCycleWeight / currentCycleLength;

        // run reversed BFS
        while (!queue.isEmpty()) {
            V u = queue.removeFirst();
            for (E e : component.incomingEdgesOf(u)) {
                V v = Graphs.getOppositeVertex(component, e, u);
                if (policyGraph.get(v).equals(e) && !reachedVertices.get(v)) {
                    reachedVertices.put(v, true);
                    double updatedDistance =
                        vertexDistance.get(u) + component.getEdgeWeight(e) - currentMean;
                    vertexDistance.put(v, updatedDistance);
                    queue.addLast(v);
                }
            }
        }

        // identify if the current value of minimum mean
        // is optimal for the graph
        boolean improved = false;
        for (V u : component.vertexSet()) {
            for (E e : component.incomingEdgesOf(u)) {
                V v = Graphs.getOppositeVertex(component, e, u);

                double oldDistance = vertexDistance.get(v);
                double updatedDistance =
                    vertexDistance.get(u) + component.getEdgeWeight(e) - currentMean;

                if (oldDistance > updatedDistance) {
                    // check if the value of minimum mean
                    // has been sufficiently improved
                    if (comparator.compare(oldDistance, updatedDistance) > 0) {
                        improved = true;
                    }
                    vertexDistance.put(v, updatedDistance);
                    policyGraph.put(v, e);
                }
            }
        }
        return improved;
    }

    /**
     * Constructs cycle with minimum mean using information in {@code policyGraph}.
     *
     * @param bestCycleVertex cycle vertex
     * @param bestCycleLength cycle length
     * @param bestCycleWeight cycle weight
     * @return constructed minimum mean cycle
     */
    private GraphPath<V, E> buildPath(
        V bestCycleVertex, int bestCycleLength, double bestCycleWeight)
    {
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

        return new GraphWalk<>(
            graph, bestCycleVertex, bestCycleVertex, pathVertices, pathEdges, bestCycleWeight);
    }
}
