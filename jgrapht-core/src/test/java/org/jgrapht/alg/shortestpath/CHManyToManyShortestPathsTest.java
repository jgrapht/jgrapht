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
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.jgrapht.alg.shortestpath.ContractionHierarchyAlgorithm.ContractionEdge;
import static org.jgrapht.alg.shortestpath.ContractionHierarchyAlgorithm.ContractionVertex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for {@link CHManyToManyShortestPaths}.
 *
 * @author Semen Chudakov
 */
public class CHManyToManyShortestPathsTest extends BaseManyToManyShortestPathsTest {

    @Test
    public void testEmptyGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        new CHManyToManyShortestPaths<>(graph).getManyTwoManyPaths(Collections.emptySet(), Collections.emptySet());
    }

    @Test(expected = NullPointerException.class)
    public void testSourcesIsNull() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        new CHManyToManyShortestPaths<>(graph).getManyTwoManyPaths(null, Collections.emptySet());
    }

    @Test(expected = NullPointerException.class)
    public void testTargetsIsNull() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        new CHManyToManyShortestPaths<>(graph).getManyTwoManyPaths(Collections.emptySet(), null);
    }

    @Test
    public void testNoPath() {
        Graph<Integer, DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);

        ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths<Integer, DefaultWeightedEdge> shortestPaths
                = new CHManyToManyShortestPaths<>(graph).getManyTwoManyPaths(
                new HashSet<>(Collections.singletonList(1)), new HashSet<>(Collections.singletonList(2)));

        assertEquals(Double.POSITIVE_INFINITY, shortestPaths.getWeight(1, 2), 1e-9);
        assertNull(shortestPaths.getPath(1, 2));
    }

    @Test
    public void testDifferentSourcesAndTargets1() {
        Graph<Integer, DefaultWeightedEdge> graph = getSimpleGraph();

        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> contraction =
                new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED)).computeContractionHierarchy();

        testDifferentSourcesAndTargetsSimpleGraph(new CHManyToManyShortestPaths<>(
                graph, contraction.getFirst(), contraction.getSecond()));
    }

    @Test
    public void testDifferentSourcesAndTargets2() {
        Graph<Integer, DefaultWeightedEdge> graph = getMultigraph();

        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> contraction =
                new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED)).computeContractionHierarchy();

        testDifferentSourcesAndTargetsMultigraph(new CHManyToManyShortestPaths<>(
                graph, contraction.getFirst(), contraction.getSecond()));
    }

    @Test
    public void testSourcesEqualTargets1() {
        Graph<Integer, DefaultWeightedEdge> graph = getSimpleGraph();

        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> contraction =
                new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED)).computeContractionHierarchy();

        testSourcesEqualTargetsSimpleGraph(new CHManyToManyShortestPaths<>(graph, contraction.getFirst(), contraction.getSecond()));
    }

    @Test
    public void testSourcesEqualTargets2() {
        Graph<Integer, DefaultWeightedEdge> graph = getMultigraph();

        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> contraction =
                new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED)).computeContractionHierarchy();


        testSourcesEqualTargetsMultigraph(new CHManyToManyShortestPaths<>(graph, contraction.getFirst(), contraction.getSecond()));
    }

    @Test
    public void testMoreSourcesThanTargets1() {
        Graph<Integer, DefaultWeightedEdge> graph = getSimpleGraph();

        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> contraction =
                new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED)).computeContractionHierarchy();


        ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths<Integer, DefaultWeightedEdge> shortestPaths
                = new CHManyToManyShortestPaths<>(graph, contraction.getFirst(), contraction.getSecond())
                .getManyTwoManyPaths(
                        new HashSet<>(Arrays.asList(1, 3, 7, 9)),
                        new HashSet<>(Collections.singletonList(5))
                );

        assertEquals(2.0, shortestPaths.getWeight(1, 5), 1e-9);
        assertEquals(Arrays.asList(1, 4, 5), shortestPaths.getPath(1, 5).getVertexList());

        assertEquals(2.0, shortestPaths.getWeight(3, 5), 1e-9);
        assertEquals(Arrays.asList(3, 6, 5), shortestPaths.getPath(3, 5).getVertexList());

        assertEquals(2.0, shortestPaths.getWeight(7, 5), 1e-9);
        assertEquals(Arrays.asList(7, 4, 5), shortestPaths.getPath(7, 5).getVertexList());

        assertEquals(2.0, shortestPaths.getWeight(9, 5), 1e-9);
        assertEquals(Arrays.asList(9, 6, 5), shortestPaths.getPath(9, 5).getVertexList());
    }

    @Test
    public void testMoreSourcesThanTargets2() {
        Graph<Integer, DefaultWeightedEdge> graph = getMultigraph();

        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> contraction =
                new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED)).computeContractionHierarchy();

        ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths<Integer, DefaultWeightedEdge> shortestPaths
                = new CHManyToManyShortestPaths<>(graph, contraction.getFirst(), contraction.getSecond())
                .getManyTwoManyPaths(
                        new HashSet<>(Arrays.asList(2, 3, 4, 5, 6)),
                        new HashSet<>(Collections.singletonList(1))
                );

        assertEquals(3.0, shortestPaths.getWeight(2, 1), 1e-9);
        assertEquals(Arrays.asList(2, 1), shortestPaths.getPath(2, 1).getVertexList());

        assertEquals(8.0, shortestPaths.getWeight(3, 1), 1e-9);
        assertEquals(Arrays.asList(3, 2, 1), shortestPaths.getPath(3, 1).getVertexList());

        assertEquals(19.0, shortestPaths.getWeight(4, 1), 1e-9);
        assertEquals(Arrays.asList(4, 3, 2, 1), shortestPaths.getPath(4, 1).getVertexList());

        assertEquals(32.0, shortestPaths.getWeight(5, 1), 1e-9);
        assertEquals(Arrays.asList(5, 4, 3, 2, 1), shortestPaths.getPath(5, 1).getVertexList());

        assertEquals(23.0, shortestPaths.getWeight(6, 1), 1e-9);
        assertEquals(Arrays.asList(6, 1), shortestPaths.getPath(6, 1).getVertexList());
    }

    @Test
    public void testOnRandomGraphs() {
        int numOfVertices = 100;
        int vertexDegree = 5;
        int numOfIterations = 10;
        int maxNumOfRandomVertices = 15;

        Random random = new Random(SEED);

        for (int i = 0; i < numOfIterations; i++) {
            Graph<Integer, DefaultWeightedEdge> graph = generateRandomGraph(
                    numOfVertices, vertexDegree * numOfVertices, random);

            Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                    Map<Integer, ContractionVertex<Integer>>> contraction
                    = new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED)).computeContractionHierarchy();

            ManyToManyShortestPathsAlgorithm<Integer, DefaultWeightedEdge> algorithm
                    = new CHManyToManyShortestPaths<>(graph, contraction.getFirst(), contraction.getSecond());

            int numOfSources = random.nextInt(maxNumOfRandomVertices);
            int numOfTargets = random.nextInt(maxNumOfRandomVertices);

            Set<Integer> sources = getRandomVertices(graph, numOfSources, random);
            Set<Integer> targets = getRandomVertices(graph, numOfTargets, random);
            test(algorithm, graph, sources, targets);
        }
    }
}