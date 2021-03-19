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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;

/**
 * Deprecated implementation of Katz centrality.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 * @author Pratik Tibrewal
 * @deprecated Please use {@link KatzCentrality} instead.
 */
@Deprecated
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
    public AlphaCentrality(final Graph<V, E> g)
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
    public AlphaCentrality(final Graph<V, E> g, final double dampingFactor)
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
    public AlphaCentrality(
        final Graph<V, E> g, final double dampingFactor, final double exogenousFactor)
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
        final Graph<V, E> g, final double dampingFactor,
        final ToDoubleFunction<V> exogenousFactorFunction)
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
        final Graph<V, E> g, final double dampingFactor, final double exogenousFactor,
        final int maxIterations)
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
        final Graph<V, E> g, final double dampingFactor,
        final ToDoubleFunction<V> exogenousFactorFunction, final int maxIterations)
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
        final Graph<V, E> g, final double dampingFactor, final double exogenousFactor,
        final int maxIterations, final double tolerance)
    {
        this.g = g;
        this.scores = new HashMap<>();

        validate(dampingFactor, maxIterations, tolerance);
        final ToDoubleFunction<V> exofactorFunction = (v) -> exogenousFactor;
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
        final Graph<V, E> g, final double dampingFactor,
        final ToDoubleFunction<V> exogenousFactorFunction, final int maxIterations,
        final double tolerance)
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
    public Double getVertexScore(final V v)
    {
        if (!g.containsVertex(v)) {
            throw new IllegalArgumentException("Cannot return score of unknown vertex");
        }
        return scores.get(v);
    }

    /* Checks for the valid values of the parameters */
    private void validate(
        final double dampingFactor, final int maxIterations, final double tolerance)
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
        final double dampingFactor, final ToDoubleFunction<V> exofactorFunction, int maxIterations,
        final double tolerance)
    {
        // initialization
        final int totalVertices = g.vertexSet().size();

        final double initScore = 1.0d / totalVertices;
        for (final V v : g.vertexSet()) {
            scores.put(v, initScore);
        }

        // run AlphaCentrality
        Map<V, Double> nextScores = new HashMap<>();
        double maxChange = tolerance;

        while (maxIterations > 0 && maxChange >= tolerance) {
            // compute next iteration scores
            maxChange = 0d;
            for (final V v : g.vertexSet()) {
                double contribution = 0d;

                for (final E e : g.incomingEdgesOf(v)) {
                    final V w = Graphs.getOppositeVertex(g, e, v);
                    contribution += dampingFactor * scores.get(w) * g.getEdgeWeight(e);
                }

                final double vOldValue = scores.get(v);
                final double vNewValue = contribution + exofactorFunction.applyAsDouble(v);
                maxChange = Math.max(maxChange, Math.abs(vNewValue - vOldValue));
                nextScores.put(v, vNewValue);
            }

            // swap scores
            final Map<V, Double> tmp = scores;
            scores = nextScores;
            nextScores = tmp;

            // progress
            maxIterations--;
        }

    }

}
