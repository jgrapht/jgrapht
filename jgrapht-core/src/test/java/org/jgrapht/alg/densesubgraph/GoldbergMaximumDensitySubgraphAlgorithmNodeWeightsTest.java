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

public class GoldbergMaximumDensitySubgraphAlgorithmNodeWeightsTest
    extends
    GoldbergMaximumDensitySubgraphTestBase<Pair<Integer, Double>, DefaultEdge>
{

    @Override
    protected MaximumDensitySubgraphAlgorithm<Pair<Integer, Double>, DefaultEdge> constructSolver(
        Graph<Pair<Integer, Double>, DefaultEdge> g,
        Function<Graph<Pair<Integer, Double>, DefaultWeightedEdge>,
            MinimumSTCutAlgorithm<Pair<Integer, Double>, DefaultWeightedEdge>> alg)
    {
        return new GoldbergMaximumDensitySubgraphAlgorithmNodeWeights<>(g, s, t, DEFAULT_EPS, alg);
    }

    @Override
    protected Pair<Integer, Double> getAdditionalSink()
    {
        return new Pair<>(-1, 0.0);
    }

    @Override
    protected Pair<Integer, Double> getAdditionalSource()
    {
        return new Pair<>(-2, 0.0);
    }

    @Test
    public void testEmpty1()
    {
        WeightedMultigraph<Pair<Integer, Double>, DefaultEdge> g =
            new WeightedMultigraph<>(DefaultEdge.class);
        test(g, constructSolver(g, PushRelabelMFImpl::new), 0, new ArrayList<>());
    }

    @Test
    public void testEmpty2()
    {
        WeightedMultigraph<Pair<Integer, Double>, DefaultEdge> g =
            new WeightedMultigraph<>(DefaultEdge.class);
        Pair<Integer, Double> p1 = new Pair<>(0, 1.3);
        Pair<Integer, Double> p2 = new Pair<>(1, 2.1);
        addVertices(g, asList(p1, p2));
        test(g, constructSolver(g, PushRelabelMFImpl::new), 2.1, Collections.singletonList(p2));
    }

    @Test
    public void testMinimal()
    {
        SimpleDirectedWeightedGraph<Pair<Integer, Double>, DefaultEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultEdge.class);
        Pair<Integer, Double> v1 = new Pair<>(1, 1.5);
        Pair<Integer, Double> v2 = new Pair<>(0, 2.5);
        addVertices(g, asList(v1, v2));
        addEdgesAndWeights(
            g, Collections.singletonList(new Pair<>(v1, v2)), Collections.singletonList(10.0));
        test(g, constructSolver(g, PushRelabelMFImpl::new), 7, asList(v1, v2));
    }

    @Test
    public void testSmall1()
    {
        SimpleWeightedGraph<Pair<Integer, Double>, DefaultEdge> g =
            new SimpleWeightedGraph<>(DefaultEdge.class);
        ArrayList<Pair<Integer, Double>> vertices = new ArrayList<>();
        vertices.add(new Pair<>(0, 1.51));
        vertices.add(new Pair<>(1, 1.0));
        vertices.add(new Pair<>(2, 1.0));
        addVertices(g, vertices);
        addEdgesAndWeights(
            g,
            asList(
                new Pair<>(vertices.get(0), vertices.get(1)),
                new Pair<>(vertices.get(0), vertices.get(2))),
            asList(4.0, 2.0));
        test(
            g, constructSolver(g, PushRelabelMFImpl::new), 3.255,
            getByIndices(vertices, asList(0, 1)));
    }

    @Test
    public void testSmall2()
    {
        SimpleWeightedGraph<Pair<Integer, Double>, DefaultEdge> g =
            new SimpleWeightedGraph<>(DefaultEdge.class);
        ArrayList<Pair<Integer, Double>> vertices = new ArrayList<>();
        for (int i = 0; i <= 7; i++) {
            vertices.add(new Pair<>(i, 1.1));
        }
        addVertices(g, vertices);
        List<Pair<Pair<Integer, Double>, Pair<Integer, Double>>> edges = asList(
            new Pair<>(vertices.get(0), vertices.get(1)),
            new Pair<>(vertices.get(1), vertices.get(2)),
            new Pair<>(vertices.get(2), vertices.get(3)),
            new Pair<>(vertices.get(3), vertices.get(4)),
            new Pair<>(vertices.get(4), vertices.get(5)),
            new Pair<>(vertices.get(5), vertices.get(6)),
            new Pair<>(vertices.get(6), vertices.get(7)),
            new Pair<>(vertices.get(1), vertices.get(7)),
            new Pair<>(vertices.get(2), vertices.get(7)),
            new Pair<>(vertices.get(3), vertices.get(7)),
            new Pair<>(vertices.get(4), vertices.get(2)));
        List<Double> weights = asList(3.0, 2.0, 1.0, 2.0, 1.0, 3.0, 1.0, 2.0, 1.0, 4.0, 1.0);
        addEdgesAndWeights(g, edges, weights);
        test(
            g, constructSolver(g, PushRelabelMFImpl::new), 3.76666666,
            getByIndices(vertices, asList(0, 1, 2, 3, 4, 7)));
    }

    @Test
    public void testMedium()
    {
        DirectedWeightedMultigraph<Pair<Integer, Double>, DefaultEdge> g =
            new DirectedWeightedMultigraph<>(DefaultEdge.class);
        List<Pair<Integer, Double>> vertices = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        List<Pair<Pair<Integer, Double>, Pair<Integer, Double>>> edges = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            vertices.add(new Pair<>(i, 1.0));
        }
        addVertices(g, vertices);
        for (int i = 1; i <= 50; i++) {
            edges.add(new Pair<>(vertices.get(i), vertices.get(i / 2)));
            weights.add(1 / Math.log10(i + 1));
        }
        for (int j = 50; j <= 100; j++) {
            edges.add(new Pair<>(vertices.get(j), vertices.get(1)));
            weights.add(100 / (double) j);
        }
        List<Pair<Integer, Double>> expected = vertices.subList(50, 101);
        expected.add(vertices.get(0));
        expected.add(vertices.get(1));
        expected.add(vertices.get(2));
        addEdgesAndWeights(g, edges, weights);
        test(g, constructSolver(g, PushRelabelMFImpl::new), 2.411760, expected);
    }
}
