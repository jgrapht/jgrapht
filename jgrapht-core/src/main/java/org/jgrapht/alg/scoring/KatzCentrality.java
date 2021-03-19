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
 * Katz centrality implementation.
 *
 * <p>
 * The <a href="https://en.wikipedia.org/wiki/Katz_centrality">wikipedia</a> article contains a nice
 * description of Katz centrality. Every path coming into a node contributes to its Katz centrality
 * by &alpha;<sup><var>k</var></sup>, where &alpha; is the <em>damping factor</em> and <var>k</var>
 * is the length of the path.
 * </p>
 *
 * <p>
 * This is a simple iterative implementation of Katz centrality which stops after a given number of
 * iterations or if the Katz centrality values between two iterations do not change more than a
 * predefined value. Each iteration increases the length of the paths contributing to the centrality
 * value. Note that unless the damping factor is smaller than the reciprocal of the
 * <a href="https://en.wikipedia.org/wiki/Spectral_radius">spectral radius</a> of the adjacency
 * matrix, the computation will not converge.
 * </p>
 *
 * <p>
 * This implementation makes it possible to provide an exogenous factor in the form of a
 * {@link ToDoubleFunction} mapping each vertex to its exogenous score. Each path is then multiplied
 * by the exogenous score of its starting vertex. The {@linkplain #exogenousFactorDefaultFunction()
 * default exogenous function} maps all vertices to one, as in standard Katz centrality.
 * </p>
 *
 * <p>
 * Each iteration of the algorithm runs in linear time O(n+m) when n is the number of nodes and m
 * the number of edges of the graph. The maximum number of iterations can be adjusted by the caller.
 * The default value is {@link KatzCentrality#MAX_ITERATIONS_DEFAULT}. Also in case of weighted
 * graphs, negative weights are not expected.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 * @author Pratik Tibrewal
 * @author Sebastiano Vigna
 */
public final class KatzCentrality<V, E>
    implements
    VertexScoringAlgorithm<V, Double>
{
    /**
     * Default number of maximum iterations.
     */
    public static final int MAX_ITERATIONS_DEFAULT = 100;

    /**
     * Default value for the tolerance. The calculation will stop if the difference of Katz
     * centrality values between iterations change less than this value.
     */
    public static final double TOLERANCE_DEFAULT = 0.0001;

    /**
     * Damping factor default value.
     */
    public static final double DAMPING_FACTOR_DEFAULT = 0.01d;

    /**
     * Exogenous factor default function (the constant function returning 1).
     *
     * @return always 1.
     * @param <V> the input type of the function.
     */
    public static final <V> ToDoubleFunction<V> exogenousFactorDefaultFunction()
    {
        return x -> 1;
    }

    private final Graph<V, E> g;
    private Map<V, Double> scores;

    /**
     * Create and execute an instance of KatzCentrality.
     *
     * @param g the input graph
     */
    public KatzCentrality(final Graph<V, E> g)
    {
        this(
            g, DAMPING_FACTOR_DEFAULT, exogenousFactorDefaultFunction(), MAX_ITERATIONS_DEFAULT,
            TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of KatzCentrality.
     *
     * @param g the input graph
     * @param dampingFactor the damping factor
     */
    public KatzCentrality(final Graph<V, E> g, final double dampingFactor)
    {
        this(
            g, dampingFactor, exogenousFactorDefaultFunction(), MAX_ITERATIONS_DEFAULT,
            TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of KatzCentrality.
     *
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param maxIterations the maximum number of iterations to perform
     */
    public KatzCentrality(final Graph<V, E> g, final double dampingFactor, final int maxIterations)
    {
        this(g, dampingFactor, exogenousFactorDefaultFunction(), maxIterations, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of KatzCentrality.
     *
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param maxIterations the maximum number of iterations to perform
     * @param tolerance the calculation will stop if the difference of Katz centrality values
     *        between iterations change less than this value
     */
    public KatzCentrality(
        final Graph<V, E> g, final double dampingFactor, final int maxIterations,
        final double tolerance)
    {
        this(g, dampingFactor, exogenousFactorDefaultFunction(), maxIterations, tolerance);
    }

    /**
     * Create and execute an instance of KatzCentrality.
     *
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param exogenousFactorFunction a provider of exogenous factor per vertex
     */
    public KatzCentrality(
        final Graph<V, E> g, final double dampingFactor,
        final ToDoubleFunction<V> exogenousFactorFunction)
    {
        this(g, dampingFactor, exogenousFactorFunction, MAX_ITERATIONS_DEFAULT, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of KatzCentrality.
     *
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param exogenousFactorFunction a provider of exogenous factor per vertex
     * @param maxIterations the maximum number of iterations to perform
     */
    public KatzCentrality(
        final Graph<V, E> g, final double dampingFactor,
        final ToDoubleFunction<V> exogenousFactorFunction, final int maxIterations)
    {
        this(g, dampingFactor, exogenousFactorFunction, maxIterations, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of KatzCentrality.
     *
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param exogenousFactorFunction a provider of exogenous factor per vertex
     * @param maxIterations the maximum number of iterations to perform
     * @param tolerance the calculation will stop if the difference of Katz centrality values
     *        between iterations change less than this value
     */
    public KatzCentrality(
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

        if (dampingFactor < 0.0) {
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
        for (final V v : g.vertexSet()) {
            scores.put(v, exofactorFunction.applyAsDouble(v));
        }

        // run KatzCentrality
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
