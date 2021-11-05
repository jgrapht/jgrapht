/*
 * (C) Copyright 2018-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.matching.blossom.v5;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;

import java.util.*;

import static org.jgrapht.alg.matching.blossom.v5.KolmogorovWeightedPerfectMatching.DEFAULT_OPTIONS;
import static org.jgrapht.alg.matching.blossom.v5.KolmogorovWeightedPerfectMatching.EPS;
import static org.jgrapht.alg.matching.blossom.v5.ObjectiveSense.MAXIMIZE;

/**
 * This class computes weighted matchings in general graphs. Depending on the constructor parameter
 * the weight of the resulting matching is maximized or minimized. If maximum of minimum weight
 * <i>perfect</i> algorithm is needed, see {@link KolmogorovWeightedPerfectMatching}.
 * <p>
 * This class reduces both maximum and minimum weight matching problems to the maximum and minimum
 * weight perfect matching problems correspondingly. See {@link KolmogorovWeightedPerfectMatching}
 * for relative definitions and algorithm description
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see KolmogorovWeightedPerfectMatching
 */
public class KolmogorovWeightedMatching<V, E>
    implements
    MatchingAlgorithm<V, E>
{
    /**
     * The graph we are matching on
     */
    private final Graph<V, E> initialGraph;
    /**
     * The graph created during the reduction
     */
    private Graph<V, E> graph;
    /**
     * The computed matching of the {@code graph}
     */
    private Matching<V, E> matching;
    /**
     * The perfect matching algorithm used during for the problem reduction
     */
    private KolmogorovWeightedPerfectMatching<V, E> perfectMatching;
    /**
     * BlossomVOptions used by the algorithm to match the problem instance
     */
    private BlossomVOptions options;
    /**
     * The objective sense of the algorithm, i.e. whether to maximize or minimize the weight of the
     * resulting matching
     */
    private ObjectiveSense objectiveSense;

    /**
     * Constructs a new instance of the algorithm using the default options. The goal of the
     * constructed algorithm is to minimize the weight of the resulting matching.
     *
     * @param initialGraph the graph for which to find a weighted matching
     */
    public KolmogorovWeightedMatching(Graph<V, E> initialGraph)
    {
        this(initialGraph, DEFAULT_OPTIONS, MAXIMIZE);
    }

    /**
     * Constructs a new instance of the algorithm using the default options. The goal of the
     * constructed algorithm is to maximize or minimize the weight of the resulting matching
     * depending on the {@code maximize} parameter.
     *
     * @param initialGraph the graph for which to find a weighted matching
     * @param objectiveSense objective sense of the algorithm
     */
    public KolmogorovWeightedMatching(Graph<V, E> initialGraph, ObjectiveSense objectiveSense)
    {
        this(initialGraph, DEFAULT_OPTIONS, objectiveSense);
    }

    /**
     * Constructs a new instance of the algorithm with the specified {@code options}. The goal of
     * the constructed algorithm is to minimize the weight of the resulting matching.
     *
     * @param initialGraph the graph for which to find a weighted matching
     * @param options the options which define the strategies for the initialization and dual
     *        updates
     */
    public KolmogorovWeightedMatching(Graph<V, E> initialGraph, BlossomVOptions options)
    {
        this(initialGraph, options, MAXIMIZE);
    }

    /**
     * Constructs a new instance of the algorithm with the specified {@code options}. The goal of
     * the constructed algorithm is to maximize or minimize the weight of the resulting matching
     * depending on the {@code maximize} parameter.
     *
     * @param initialGraph the graph for which to find a weighted matching
     * @param options the options which define the strategies for the initialization and dual
     *        updates
     * @param objectiveSense objective sense of the algorithm
     */
    public KolmogorovWeightedMatching(
        Graph<V, E> initialGraph, BlossomVOptions options, ObjectiveSense objectiveSense)
    {
        this.initialGraph = Objects.requireNonNull(initialGraph);
        this.options = Objects.requireNonNull(options);
        this.objectiveSense = objectiveSense;
    }

    /**
     * Computes and returns a matching of maximum or minimum weight in the {@code initialGraph}
     * depending on the goal of the algorithm.
     *
     * @return weighted matching in the {@code initialGraph}
     */
    @Override
    public Matching<V, E> getMatching()
    {
        if (matching == null) {
            lazyComputeMaximumWeightMatching();
        }
        return matching;
    }

    /**
     * Lazy computes optimal matching in the {@code initialGraph} by reducing the problem to the
     * optimal perfect matching problem.
     */
    private void lazyComputeMaximumWeightMatching()
    {
        Map<V, V> duplicatedVertices = new HashMap<>();
        GraphType type = initialGraph.getType();
        Graph<V, E> graphCopy = GraphTypeBuilder
            .undirected().allowingMultipleEdges(type.isAllowingMultipleEdges())
            .allowingSelfLoops(type.isAllowingSelfLoops())
            .vertexSupplier(initialGraph.getVertexSupplier())
            .edgeSupplier(initialGraph.getEdgeSupplier()).weighted(type.isWeighted()).buildGraph();
        for (V v : initialGraph.vertexSet()) {
            duplicatedVertices.put(v, graphCopy.addVertex());
        }
        for (E edge : initialGraph.edgeSet()) {
            Graphs
                .addEdgeWithVertices(
                    graphCopy, duplicatedVertices.get(initialGraph.getEdgeSource(edge)),
                    duplicatedVertices.get(initialGraph.getEdgeTarget(edge)),
                    initialGraph.getEdgeWeight(edge));
        }
        Map<E, Double> zeroWeightFunction = new HashMap<>();
        for (Map.Entry<V, V> entry : duplicatedVertices.entrySet()) {
            graphCopy.addVertex(entry.getKey());
            zeroWeightFunction.put(graphCopy.addEdge(entry.getKey(), entry.getValue()), 0d);
        }
        this.graph =
            new AsGraphUnion<>(new AsWeightedGraph<>(graphCopy, zeroWeightFunction), initialGraph);
        this.perfectMatching =
            new KolmogorovWeightedPerfectMatching<>(graph, options, objectiveSense);
        matching = perfectMatching.getMatching();
        Set<E> matchingEdges = matching.getEdges();
        matchingEdges.removeIf(e -> !initialGraph.containsEdge(e));
        this.matching = new MatchingImpl<>(initialGraph, matchingEdges, matching.getWeight() / 2);
    }

    /**
     * Performs an optimality test after the perfect matching is computed. This test is done via
     * {@link KolmogorovWeightedPerfectMatching#testOptimality()}
     * <p>
     * More precisely, checks whether dual variables of all pseudonodes and resulting slacks of all
     * edges are non-negative and that slacks of all matched edges are exactly 0. Since the
     * algorithm uses floating point arithmetic, this check is done with precision of
     * {@link KolmogorovWeightedPerfectMatching#EPS}
     * <p>
     * In general, this method should always return true unless the algorithm implementation has a
     * bug.
     *
     * @return true iff the assigned dual variables satisfy the dual linear program formulation AND
     *         complementary slackness conditions are also satisfied. The total error must not
     *         exceed EPS
     */
    public boolean testOptimality()
    {
        return perfectMatching.getError() < EPS;
    }

    /**
     * Computes the error in the solution to the dual linear program. This computation is done via
     * {@link KolmogorovWeightedPerfectMatching#getError()}. More precisely, the total error equals
     * the sum of:
     * <ul>
     * <li>Absolute value of edge slack if negative or the edge is matched</li>
     * <li>Absolute value of pseudonode variable if negative</li>
     * </ul>
     *
     * @return the total numeric error
     */
    public double getError()
    {
        lazyComputeMaximumWeightMatching();
        return perfectMatching.getError();
    }
}
