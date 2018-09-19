/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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
import java.util.function.*;

/**
 * AlphaCentrality implementation.
 * 
 * <p>
 * The <a href="https://en.wikipedia.org/wiki/Alpha_centrality">wikipedia</a> article contains a
 * nice description of AlphaCentrality. You may also refer to this
 * <a href="http://www.leonidzhukov.net/hse/2016/networks/papers/bonacich2001.pdf">paper</a>
 * describing the implementation of the algorithm.
 * </p>
 * 
 * <p>
 * To implement EigenVector Centrality, call AlphaCentrality by passing the value of exogenous
 * factor as zero. Further description of EigenVector Centrality can be found in
 * <a href="https://en.wikipedia.org/wiki/Eigenvector_centrality">wikipedia</a>. To implement Katz
 * Centrality, call AlphaCentrality by passing a non-zero scalar exogenous factor value. Further
 * description of Katz Centrality can be found in
 * <a href="https://en.wikipedia.org/wiki/Katz_centrality">wikipedia</a>.
 * </p>
 *
 * <p>
 * This is a simple iterative implementation of AlphaCentrality which stops after a given number of
 * iterations or if the AlphaCentrality values between two iterations do not change more than a
 * predefined value.
 * </p>
 *
 * <p>
 * Each iteration of the algorithm runs in linear time O(n+m) when n is the number of nodes and m
 * the number of edges of the graph. The maximum number of iterations can be adjusted by the caller.
 * The default value is {@link AlphaCentrality#MAX_ITERATIONS_DEFAULT}. Also in case of weighted
 * graphs, negative weights are not expected.
 * </p>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 * @author Pratik Tibrewal
 */
public final class AlphaCentrality<V, E>
    implements
    VertexScoringAlgorithm<V, Double>
{
    /**
     * Default number of maximum iterations.
     */
    public static final int MAX_ITERATIONS_DEFAULT = 100;

    /**
     * Default value for the tolerance. The calculation will stop if the difference of
     * AlphaCentrality values between iterations change less than this value.
     */
    public static final double TOLERANCE_DEFAULT = 0.0001;

    /**
     * Damping factor default value.
     */
    public static final double DAMPING_FACTOR_DEFAULT = 0.01d;

    /**
     * Exogenous factor default value.
     */
    public static final double EXOGENOUS_FACTOR_DEFAULT = 1.0d;

    private final Graph<V, E> g;
    private Map<V, Double> scores;

    /**
     * Create and execute an instance of AlphaCentrality.
     * 
     * @param g the input graph
     */
    public AlphaCentrality(Graph<V, E> g)
    {
        this(
            g, DAMPING_FACTOR_DEFAULT, EXOGENOUS_FACTOR_DEFAULT, MAX_ITERATIONS_DEFAULT,
            TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of AlphaCentrality.
     * 
     * @param g the input graph
     * @param dampingFactor the damping factor
     */
    public AlphaCentrality(Graph<V, E> g, double dampingFactor)
    {
        this(g, dampingFactor, EXOGENOUS_FACTOR_DEFAULT, MAX_ITERATIONS_DEFAULT, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of AlphaCentrality.
     * 
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param exogenousFactor the exogenous factor
     */
    public AlphaCentrality(Graph<V, E> g, double dampingFactor, double exogenousFactor)
    {
        this(g, dampingFactor, exogenousFactor, MAX_ITERATIONS_DEFAULT, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of AlphaCentrality.
     * 
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param exogenousFactorFunction ToDoubleFunction a provider of exogenous factors per vertex
     */
    public AlphaCentrality(
        Graph<V, E> g, double dampingFactor, ToDoubleFunction<V> exogenousFactorFunction)
    {
        this(g, dampingFactor, exogenousFactorFunction, MAX_ITERATIONS_DEFAULT, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of AlphaCentrality.
     * 
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param exogenousFactor the exogenous factor
     * @param maxIterations the maximum number of iterations to perform
     */
    public AlphaCentrality(
        Graph<V, E> g, double dampingFactor, double exogenousFactor, int maxIterations)
    {
        this(g, dampingFactor, exogenousFactor, maxIterations, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of AlphaCentrality.
     * 
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param exogenousFactorFunction ToDoubleFunction a provider of exogenous factors per vertex
     * @param maxIterations the maximum number of iterations to perform
     */
    public AlphaCentrality(
        Graph<V, E> g, double dampingFactor, ToDoubleFunction<V> exogenousFactorFunction,
        int maxIterations)
    {
        this(g, dampingFactor, exogenousFactorFunction, maxIterations, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of AlphaCentrality.
     * 
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param exogenousFactor the exogenous factor
     * @param maxIterations the maximum number of iterations to perform
     * @param tolerance the calculation will stop if the difference of AlphaCentrality values
     *        between iterations change less than this value
     */
    public AlphaCentrality(
        Graph<V, E> g, double dampingFactor, double exogenousFactor, int maxIterations,
        double tolerance)
    {
        this.g = g;
        this.scores = new HashMap<>();

        validate(dampingFactor, maxIterations, tolerance);
        ToDoubleFunction<V> exofactorFunction = (v) -> exogenousFactor;
        run(dampingFactor, exofactorFunction, maxIterations, tolerance);
    }

    /**
     * Create and execute an instance of AlphaCentrality.
     * 
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param exogenousFactorFunction ToDoubleFunction a provider of exogenous factors per vertex
     * @param maxIterations the maximum number of iterations to perform
     * @param tolerance the calculation will stop if the difference of AlphaCentrality values
     *        between iterations change less than this value
     */
    public AlphaCentrality(
        Graph<V, E> g, double dampingFactor, ToDoubleFunction<V> exogenousFactorFunction,
        int maxIterations, double tolerance)
    {
        this.g = g;
        this.scores = new HashMap<>();

        validate(dampingFactor, maxIterations, tolerance);
        run(dampingFactor, exogenousFactorFunction, maxIterations, tolerance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<V, Double> getScores()
    {
        return Collections.unmodifiableMap(scores);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getVertexScore(V v)
    {
        if (!g.containsVertex(v)) {
            throw new IllegalArgumentException("Cannot return score of unknown vertex");
        }
        return scores.get(v);
    }

    /* Checks for the valid values of the parameters */
    private void validate(double dampingFactor, int maxIterations, double tolerance)
    {
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("Maximum iterations must be positive");
        }

        if (dampingFactor < 0.0 || dampingFactor > 1.0) {
            throw new IllegalArgumentException("Damping factor not valid");
        }

        if (tolerance <= 0.0) {
            throw new IllegalArgumentException("Tolerance not valid, must be positive");
        }
    }

    private void run(
        double dampingFactor, ToDoubleFunction<V> exofactorFunction, int maxIterations,
        double tolerance)
    {
        // initialization
        int totalVertices = g.vertexSet().size();

        double initScore = 1.0d / totalVertices;
        for (V v : g.vertexSet()) {
            scores.put(v, initScore);
        }

        // run AlphaCentrality
        Map<V, Double> nextScores = new HashMap<>();
        double maxChange = tolerance;

        while (maxIterations > 0 && maxChange >= tolerance) {
            // compute next iteration scores
            maxChange = 0d;
            for (V v : g.vertexSet()) {
                double contribution = 0d;

                for (E e : g.incomingEdgesOf(v)) {
                    V w = Graphs.getOppositeVertex(g, e, v);
                    contribution += dampingFactor * scores.get(w) * g.getEdgeWeight(e);
                }

                double vOldValue = scores.get(v);
                double vNewValue = contribution + exofactorFunction.applyAsDouble(v);
                maxChange = Math.max(maxChange, Math.abs(vNewValue - vOldValue));
                nextScores.put(v, vNewValue);
            }

            // swap scores
            Map<V, Double> tmp = scores;
            scores = nextScores;
            nextScores = tmp;

            // progress
            maxIterations--;
        }

    }

}
