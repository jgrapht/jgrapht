/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.scoring;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;

import java.util.*;

/**
 * PageRank implementation.
 * 
 * <p>
 * The <a href="https://en.wikipedia.org/wiki/PageRank">wikipedia</a> article contains a nice
 * description of PageRank. The method can be found on the article: Sergey Brin and Larry Page: The
 * Anatomy of a Large-Scale Hypertextual Web Search Engine. Proceedings of the 7th World-Wide Web
 * Conference, Brisbane, Australia, April 1998. See also the following
 * <a href="http://infolab.stanford.edu/~backrub/google.html">page</a>.
 * </p>
 * 
 * <p>
 * This is a simple iterative implementation of PageRank which stops after a given number of
 * iterations or if the PageRank values between two iterations do not change more than a predefined
 * value. The implementation uses the variant which divides by the number of nodes, thus forming a
 * probability distribution over graph nodes.
 * </p>
 *
 * <p>
 * Each iteration of the algorithm runs in linear time $O(n+m)$ when $n$ is the number of nodes and
 * $m$ the number of edges of the graph. The maximum number of iterations can be adjusted by the
 * caller. The default value is {@link PageRank#MAX_ITERATIONS_DEFAULT}.
 * </p>
 * 
 * <p>
 * If the graph is a weighted graph, a weighted variant is used where the probability of following
 * an edge e out of node $v$ is equal to the weight of $e$ over the sum of weights of all outgoing
 * edges of $v$.
 * </p>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public final class PageRank<V, E>
    implements
    VertexScoringAlgorithm<V, Double>
{
    /**
     * Default number of maximum iterations.
     */
    public static final int MAX_ITERATIONS_DEFAULT = 100;

    /**
     * Default value for the tolerance. The calculation will stop if the difference of PageRank
     * values between iterations change less than this value.
     */
    public static final double TOLERANCE_DEFAULT = 0.0001;

    /**
     * Damping factor default value.
     */
    public static final double DAMPING_FACTOR_DEFAULT = 0.85d;

    /**
     * The input graph
     */
    private final Graph<V, E> graph;

    /**
     * The damping factor
     */
    private final double dampingFactor;

    /**
     * Maximum iterations to run
     */
    private final int maxIterations;

    /**
     * The calculation will stop if the difference of PageRank values between iterations change less
     * than this value
     */
    private final double tolerance;

    /**
     * The result
     */
    private Map<V, Double> scores;

    /**
     * Create and execute an instance of PageRank.
     * 
     * @param graph the input graph
     */
    public PageRank(Graph<V, E> graph)
    {
        this(graph, DAMPING_FACTOR_DEFAULT, MAX_ITERATIONS_DEFAULT, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of PageRank.
     * 
     * @param graph the input graph
     * @param dampingFactor the damping factor
     */
    public PageRank(Graph<V, E> graph, double dampingFactor)
    {
        this(graph, dampingFactor, MAX_ITERATIONS_DEFAULT, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of PageRank.
     * 
     * @param graph the input graph
     * @param dampingFactor the damping factor
     * @param maxIterations the maximum number of iterations to perform
     */
    public PageRank(Graph<V, E> graph, double dampingFactor, int maxIterations)
    {
        this(graph, dampingFactor, maxIterations, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of PageRank.
     * 
     * @param graph the input graph
     * @param dampingFactor the damping factor
     * @param maxIterations the maximum number of iterations to perform
     * @param tolerance the calculation will stop if the difference of PageRank values between
     *        iterations change less than this value
     */
    public PageRank(Graph<V, E> graph, double dampingFactor, int maxIterations, double tolerance)
    {
        this.graph = graph;

        if (maxIterations <= 0) {
            throw new IllegalArgumentException("Maximum iterations must be positive");
        }
        this.maxIterations = maxIterations;

        if (dampingFactor < 0.0 || dampingFactor > 1.0) {
            throw new IllegalArgumentException("Damping factor not valid");
        }
        this.dampingFactor = dampingFactor;

        if (tolerance <= 0.0) {
            throw new IllegalArgumentException("Tolerance not valid, must be positive");
        }
        this.tolerance = tolerance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<V, Double> getScores()
    {
        if (scores == null) {
            scores = Collections.unmodifiableMap(new Algorithm().getScores());
        }
        return scores;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getVertexScore(V v)
    {
        if (!graph.containsVertex(v)) {
            throw new IllegalArgumentException("Cannot return score of unknown vertex");
        }
        return getScores().get(v);
    }

    /**
     * The actual implementation.
     * 
     * <p>
     * We use this pattern with the inner class in order to be able to cache the result but also
     * allow the garbage collector to acquire all auxiliary memory used during the execution of the
     * algorithm.
     * 
     * @author Dimitrios Michail
     * 
     * @param <V> the graph type
     * @param <E> the edge type
     */
    private class Algorithm
    {
        private int totalVertices;
        private boolean isWeighted;

        private Map<V, Integer> vertexIndexMap;
        private V[] vertexMap;

        private double[] weightSum;
        private double[] curScore;
        private double[] nextScore;
        private int[] outDegree;
        private ArrayList<int[]> adjList;
        private ArrayList<double[]> weightsList;

        @SuppressWarnings("unchecked")
        public Algorithm()
        {
            this.totalVertices = graph.vertexSet().size();
            this.isWeighted = graph.getType().isWeighted();

            /*
             * Initialize score, map vertices to [0,n) and pre-compute degrees and adjacency lists
             */
            this.curScore = new double[totalVertices];
            this.nextScore = new double[totalVertices];
            this.vertexIndexMap = new HashMap<>();
            this.vertexMap = (V[]) new Object[totalVertices];
            this.outDegree = new int[totalVertices];
            this.adjList = new ArrayList<>(totalVertices);

            double initScore = 1.0d / totalVertices;
            int i = 0;
            for (V v : graph.vertexSet()) {
                vertexIndexMap.put(v, i);
                vertexMap[i] = v;
                outDegree[i] = graph.outDegreeOf(v);
                curScore[i] = initScore;
                i++;
            }

            if (isWeighted) {
                this.weightSum = new double[totalVertices];
                this.weightsList = new ArrayList<>(totalVertices);

                for (i = 0; i < totalVertices; i++) {
                    V v = vertexMap[i];
                    int[] inNeighbors = new int[graph.inDegreeOf(v)];
                    double[] edgeWeights = new double[graph.inDegreeOf(v)];

                    int j = 0;
                    for (E e : graph.incomingEdgesOf(v)) {
                        V w = Graphs.getOppositeVertex(graph, e, v);
                        Integer mappedVertexId = vertexIndexMap.get(w);
                        inNeighbors[j] = mappedVertexId;
                        double edgeWeight = graph.getEdgeWeight(e);
                        edgeWeights[j] += edgeWeight;
                        weightSum[mappedVertexId] += edgeWeight;
                        j++;
                    }
                    weightsList.add(edgeWeights);
                    adjList.add(inNeighbors);
                }
            } else {
                for (i = 0; i < totalVertices; i++) {
                    V v = vertexMap[i];
                    int[] inNeighbors = new int[graph.inDegreeOf(v)];
                    int j = 0;
                    for (E e : graph.incomingEdgesOf(v)) {
                        V w = Graphs.getOppositeVertex(graph, e, v);
                        inNeighbors[j++] = vertexIndexMap.get(w);
                    }
                    adjList.add(inNeighbors);
                }
            }
        }

        public Map<V, Double> getScores()
        {
            // compute
            if (isWeighted) {
                runWeighted();
            } else {
                run();
            }

            // make results user friendly
            Map<V, Double> scores = new HashMap<>();
            for (int i = 0; i < totalVertices; i++) {
                V v = vertexMap[i];
                scores.put(v, curScore[i]);
            }
            return scores;
        }

        private void run()
        {
            double maxChange = tolerance;
            int iterations = maxIterations;

            while (iterations > 0 && maxChange >= tolerance) {
                double r = teleProp();

                maxChange = 0d;
                for (int i = 0; i < totalVertices; i++) {
                    double contribution = 0d;
                    for (int w : adjList.get(i)) {
                        contribution += dampingFactor * curScore[w] / outDegree[w];
                    }

                    double vOldValue = curScore[i];
                    double vNewValue = r + contribution;
                    maxChange = Math.max(maxChange, Math.abs(vNewValue - vOldValue));
                    nextScore[i] = vNewValue;
                }

                // progress
                swapScores();
                iterations--;
            }
        }

        private void runWeighted()
        {
            double maxChange = tolerance;
            int iterations = maxIterations;

            while (iterations > 0 && maxChange >= tolerance) {
                double r = teleProp();

                maxChange = 0d;
                for (int i = 0; i < totalVertices; i++) {
                    double contribution = 0d;

                    int[] neighbors = adjList.get(i);
                    double[] weights = weightsList.get(i);
                    for (int j = 0, getLength = neighbors.length; j < getLength; j++) {
                        int w = neighbors[j];
                        contribution += dampingFactor * curScore[w] * weights[j] / weightSum[w];
                    }

                    double vOldValue = curScore[i];
                    double vNewValue = r + contribution;
                    maxChange = Math.max(maxChange, Math.abs(vNewValue - vOldValue));
                    nextScore[i] = vNewValue;
                }

                // progress
                swapScores();
                iterations--;
            }
        }

        private double teleProp()
        {
            double r = 0d;
            for (int i = 0; i < totalVertices; i++) {
                if (outDegree[i] > 0) {
                    r += (1d - dampingFactor) * curScore[i];
                } else {
                    r += curScore[i];
                }
            }
            r /= totalVertices;
            return r;
        }

        private void swapScores()
        {
            double[] tmp = curScore;
            curScore = nextScore;
            nextScore = tmp;
        }

    }

}
