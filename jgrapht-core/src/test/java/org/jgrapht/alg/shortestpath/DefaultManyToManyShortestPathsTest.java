/*
 * (C) Copyright 2019-2019, by Semen Chudakov and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link DefaultManyToManyShortestPaths}.
 *
 * @author Semen Chudakov
 */
public class DefaultManyToManyShortestPathsTest extends BaseManyToManyShortestPathsTest {

    @Test
    public void testEmptyGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        new DefaultManyToManyShortestPaths<>(graph).getManyTwoManyPaths(Collections.emptySet(), Collections.emptySet());
    }

    @Test(expected = NullPointerException.class)
    public void testSourcesIsNull() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        new DefaultManyToManyShortestPaths<>(graph).getManyTwoManyPaths(null, Collections.emptySet());
    }

    @Test(expected = NullPointerException.class)
    public void testTargetsIsNull() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        new DefaultManyToManyShortestPaths<>(graph).getManyTwoManyPaths(Collections.emptySet(), null);
    }

    @Test
    public void testNoPath() {
        Graph<Integer, DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);

        ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths<Integer, DefaultWeightedEdge> shortestPaths
                = new DefaultManyToManyShortestPaths<>(graph).getManyTwoManyPaths(
                new HashSet<>(Collections.singletonList(1)), new HashSet<>(Collections.singletonList(2)));

        assertEquals(Double.POSITIVE_INFINITY, shortestPaths.getWeight(1, 2), 1e-9);
        assertNull(shortestPaths.getPath(1, 2));
    }

    @Test
    public void testDifferentSourcesAndTargets1() {
        testDifferentSourcesAndTargetsSimpleGraph(new DefaultManyToManyShortestPaths<>(getSimpleGraph()));
    }

    @Test
    public void testDifferentSourcesAndTargets2() {
        testDifferentSourcesAndTargetsMultigraph(new DefaultManyToManyShortestPaths<>(getMultigraph()));
    }

    @Test
    public void testSourcesEqualTargets1() {
        testSourcesEqualTargetsSimpleGraph(new DefaultManyToManyShortestPaths<>(getSimpleGraph()));
    }

    @Test
    public void testSourcesEqualTargets2() {
        testSourcesEqualTargetsMultigraph(new DefaultManyToManyShortestPaths<>(getMultigraph()));
    }

    @Test
    public void testOnRandomGraphs() {
        int numOfVertices = 100;
        int vertexDegree = 20;
        int numOfIterations = 50;
        int numOfRandomVertices = 100;

        Random random = new Random(SEED);

        for (int i = 0; i < numOfIterations; i++) {
            Graph<Integer, DefaultWeightedEdge> graph = generateRandomGraph(
                    numOfVertices, vertexDegree * numOfVertices, random);

            ManyToManyShortestPathsAlgorithm<Integer, DefaultWeightedEdge> algorithm
                    = new DefaultManyToManyShortestPaths<>(graph);

            Set<Integer> sources = getRandomVertices(graph, numOfRandomVertices, random);
            Set<Integer> targets = getRandomVertices(graph, numOfRandomVertices, random);
            test(algorithm, graph, sources, targets);
        }
    }
}