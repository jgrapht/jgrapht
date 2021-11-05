/*
 * (C) Copyright 2018-2021, by Andre Immig and Contributors.
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
package org.jgrapht.alg.densesubgraph;

import org.jgrapht.*;
import org.jgrapht.alg.flow.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;
import java.util.function.*;

import static java.util.Arrays.asList;

/**
 * Tests for {@link GoldbergMaximumDensitySubgraphAlgorithm}
 *
 * @author Andre Immig
 */

public class GoldbergMaximumDensitySubgraphAlgorithmTest
    extends
    GoldbergMaximumDensitySubgraphTestBase<Integer, DefaultEdge>
{

    @Override
    protected MaximumDensitySubgraphAlgorithm<Integer, DefaultEdge> constructSolver(
        Graph<Integer, DefaultEdge> g, Function<Graph<Integer, DefaultWeightedEdge>,
            MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge>> alg)
    {
        return new GoldbergMaximumDensitySubgraphAlgorithm<>(g, s, t, DEFAULT_EPS, alg);
    }

    @Override
    protected Integer getAdditionalSource()
    {
        return -1;
    }

    @Override
    protected Integer getAdditionalSink()
    {
        return -2;
    }

    @Test
    public void testEmpty1()
    {
        WeightedMultigraph<Integer, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        test(g, constructSolver(g, PushRelabelMFImpl::new), 0, new ArrayList<>());
    }

    @Test
    public void testEmpty2()
    {
        WeightedMultigraph<Integer, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        addVertices(g, asList(0, 1));
        test(g, constructSolver(g, PushRelabelMFImpl::new), 0, new ArrayList<>());
    }

    @Test
    public void testMinimal()
    {
        WeightedMultigraph<Integer, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        addVertices(g, asList(0, 1));
        addEdgesAndWeights(
            g, Collections.singletonList(new Pair<>(0, 1)), Collections.singletonList(10.0));
        test(g, constructSolver(g, PushRelabelMFImpl::new), 5, asList(0, 1));
    }

    @Test
    public void testSmall1()
    {
        WeightedMultigraph<Integer, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        addVertices(g, asList(0, 1, 2, 3, 4));
        List<Pair<Integer, Integer>> edges = asList(
            new Pair<>(0, 3), new Pair<>(0, 1), new Pair<>(0, 2), new Pair<>(4, 2),
            new Pair<>(0, 4), new Pair<>(2, 3));
        List<Double> weights = asList(2.0, 1.0, 1.0, 1.0, 3.0, 1.0);
        addEdgesAndWeights(g, edges, weights);
        test(g, constructSolver(g, PushRelabelMFImpl::new), 2, asList(0, 2, 3, 4));
    }

    @Test
    public void testSmall2()
    {
        SimpleWeightedGraph<Integer, DefaultEdge> g = new SimpleWeightedGraph<>(DefaultEdge.class);
        addVertices(g, asList(0, 1, 2, 3, 4, 5, 6, 7));
        List<Pair<Integer, Integer>> edges = asList(
            new Pair<>(0, 1), new Pair<>(1, 2), new Pair<>(2, 3), new Pair<>(3, 4),
            new Pair<>(4, 5), new Pair<>(5, 6), new Pair<>(6, 7), new Pair<>(1, 7),
            new Pair<>(2, 7), new Pair<>(3, 7), new Pair<>(4, 2));
        List<Double> weights = asList(3.0, 2.0, 1.0, 2.0, 1.0, 3.0, 1.0, 2.0, 1.0, 4.0, 1.0);
        addEdgesAndWeights(g, edges, weights);
        test(g, constructSolver(g, PushRelabelMFImpl::new), 2.66666666, asList(0, 1, 2, 3, 4, 7));
    }

    @Test
    public void testSmallWeights()
    {
        SimpleDirectedWeightedGraph<Integer, DefaultEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultEdge.class);
        addVertices(g, asList(0, 1, 2, 3, 4));
        List<Pair<Integer, Integer>> edges = asList(
            new Pair<>(0, 3), new Pair<>(0, 1), new Pair<>(0, 2), new Pair<>(4, 2),
            new Pair<>(0, 4), new Pair<>(2, 3));
        List<Double> weights = asList(0.0002, 0.00000001, 0.001, 0.0009, 0.003, 0.001);
        addEdgesAndWeights(g, edges, weights);
        test(g, constructSolver(g, PushRelabelMFImpl::new), 0.001633333, asList(0, 2, 4));
    }

    @Test
    public void testMedium()
    {
        DirectedWeightedMultigraph<Integer, DefaultEdge> g =
            new DirectedWeightedMultigraph<>(DefaultEdge.class);
        List<Integer> vertices = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        List<Pair<Integer, Integer>> edges = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            vertices.add(i);
        }
        addVertices(g, vertices);
        for (int i = 1; i <= 50; i++) {
            edges.add(new Pair<>(i, i / 2));
            weights.add(1 / Math.log10(i + 1));
        }
        for (int j = 50; j <= 100; j++) {
            edges.add(new Pair<>(j, 1));
            weights.add(100 / (double) j);
        }
        List<Integer> expected = vertices.subList(50, 101);
        expected.add(0);
        expected.add(1);
        expected.add(2);
        addEdgesAndWeights(g, edges, weights);
        test(g, constructSolver(g, PushRelabelMFImpl::new), 1.411760, expected);
    }
}
