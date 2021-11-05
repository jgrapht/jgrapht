/*
 * (C) Copyright 2020-2021, by Sebastiano Vigna and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.GraphIterables;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;

/**
 * Eigenvector-centrality implementation.
 *
 * <p>
 * Eigenvector centrality, introduced in 1895 by Edmund Landau for chess tournaments, associates
 * with a (weighted) graph the left dominant eigenvector of its adjacency matrix. More information
 * can be found on <a href="https://en.wikipedia.org/wiki/Eigenvector_centrality">wikipedia</a>.
 * </p>
 *
 * <p>
 * This is a simple iterative implementation of the
 * <a href="https://en.wikipedia.org/wiki/Power_iteration">power method</a> which stops after a
 * given number of iterations or if centrality values between two iterations do not change more than
 * a predefined value (technically, we stop when the &#x2113;<sub>2</sub> norm of the difference
 * between the current estimate and the next one drops below a given threshold). Correspondingly,
 * the result will be &#x2113;<sub>2</sub>-normalized.
 * </p>
 *
 * <p>
 * Each iteration of the algorithm runs in linear time O(n+m) when n is the number of nodes and m
 * the number of edges of the graph. The maximum number of iterations can be adjusted by the caller.
 * The default value is {@link EigenvectorCentrality#MAX_ITERATIONS_DEFAULT}. Also in case of
 * weighted graphs, negative weights are not expected.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Sebastiano Vigna
 */
public final class EigenvectorCentrality<V, E>
    implements
    VertexScoringAlgorithm<V, Double>
{
    /**
     * Default number of maximum iterations.
     */
    public static final int MAX_ITERATIONS_DEFAULT = 100;

    /**
     * Default value for the tolerance. The calculation will stop if the &#x2113;<sub>2</sub> norm
     * of the difference of centrality values between iterations changes less than this value.
     */
    public static final double TOLERANCE_DEFAULT = 0.0001;

    private final Graph<V, E> g;
    private Map<V, Double> scores;

    /**
     * Create and execute an instance of EigenvectorCentrality
     *
     * @param g the input graph
     */
    public EigenvectorCentrality(final Graph<V, E> g)
    {
        this(g, MAX_ITERATIONS_DEFAULT, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of EigenvectorCentrality
     *
     * @param g the input graph
     * @param maxIterations the maximum number of iterations to perform
     */
    public EigenvectorCentrality(final Graph<V, E> g, final int maxIterations)
    {
        this(g, maxIterations, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of EigenvectorCentrality.
     *
     * @param g the input graph
     * @param maxIterations the maximum number of iterations to perform
     * @param tolerance calculation will stop if the &#x2113;<sub>2</sub> norm of the difference of
     *        centrality values between iterations changes less than this value
     */
    public EigenvectorCentrality(
        final Graph<V, E> g, final int maxIterations, final double tolerance)
    {
        this.g = g;
        this.scores = new HashMap<>();

        validate(maxIterations, tolerance);
        run(maxIterations, tolerance);
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
    private void validate(final int maxIterations, final double tolerance)
    {
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("Maximum iterations must be positive");
        }

        if (tolerance <= 0.0) {
            throw new IllegalArgumentException("Tolerance not valid, must be positive");
        }
    }

    private void run(int maxIterations, final double tolerance)
    {
        // initialization
        final int totalVertices = g.vertexSet().size();
        final GraphIterables<V, E> iterables = g.iterables();

        final double initScore = Math.sqrt(1.0d / totalVertices);
        for (final V v : iterables.vertices()) {
            scores.put(v, initScore);
        }

        // run the power method
        Map<V, Double> nextScores = new HashMap<>();
        double l2Norm = tolerance;

        while (maxIterations > 0 && l2Norm >= tolerance) {
            // compute next iteration scores
            double sumOfSquares = 0d;
            for (final V v : iterables.vertices()) {
                double vNewValue = 0d;

                for (final E e : iterables.incomingEdgesOf(v)) {
                    final V w = Graphs.getOppositeVertex(g, e, v);
                    vNewValue += scores.get(w) * g.getEdgeWeight(e);
                }

                sumOfSquares += vNewValue * vNewValue;
                nextScores.put(v, vNewValue);
            }

            final double l2NormFactor = 1 / Math.sqrt(sumOfSquares);

            double sumOfDiffs2 = 0;
            // Normalize and evaluate norm
            for (final V v : iterables.vertices()) {
                final double score = nextScores.get(v) * l2NormFactor;
                nextScores.put(v, score);
                final double d = scores.get(v) - score;
                sumOfDiffs2 += d * d;
            }

            // swap scores
            final Map<V, Double> tmp = scores;
            scores = nextScores;
            nextScores = tmp;

            l2Norm = Math.sqrt(sumOfDiffs2);

            // progress
            maxIterations--;
        }

    }

}
