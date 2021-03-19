/*
 * (C) Copyright 2020-2021, by Semen Chudakov and Contributors.
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
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.util.ConcurrencyUtil;
import org.jgrapht.util.SupplierUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation.ContractionHierarchy;
import static org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation.ContractionVertex;
import static org.jgrapht.alg.shortestpath.TransitNodeRoutingPrecomputation.AccessVertex;
import static org.jgrapht.alg.shortestpath.TransitNodeRoutingPrecomputation.AccessVertices;
import static org.jgrapht.alg.shortestpath.TransitNodeRoutingPrecomputation.TransitNodeRouting;
import static org.jgrapht.alg.shortestpath.TransitNodeRoutingPrecomputation.VoronoiDiagram;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link TransitNodeRoutingPrecomputation}.
 *
 * @author Semen Chudakov
 */
public class TransitNodeRoutingPrecomputationTest
{
    /**
     * Seed for random numbers generator used in tests.
     */
    private static final long SEED = 19L;

    /**
     * Executor which is supplied to {@link ContractionHierarchyPrecomputation} and
     * {@link TransitNodeRoutingPrecomputation} in this test case.
     */
    private static ThreadPoolExecutor executor;

    @BeforeClass
    public static void createExecutor()
    {
        executor =
            ConcurrencyUtil.createThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    }

    @AfterClass
    public static void shutdownExecutor()
        throws InterruptedException
    {
        ConcurrencyUtil.shutdownExecutionService(executor);
    }

    /**
     * Tests the algorithm on an empty graph to ensure no exception is thrown.
     */
    @Test
    public void testEmptyGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        ContractionHierarchy<Integer, DefaultWeightedEdge> contractionHierarchy =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor)
                .computeContractionHierarchy();
        TransitNodeRoutingPrecomputation<Integer, DefaultWeightedEdge> routing =
            new TransitNodeRoutingPrecomputation<>(contractionHierarchy, 0, executor);
        routing.computeTransitNodeRouting();
    }

    @Test
    public void testOneVertex()
    {
        // initialisation
        Integer vertex = 1;
        Graph<Integer, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        graph.addVertex(vertex);

        ContractionHierarchy<Integer, DefaultWeightedEdge> contractionHierarchy =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor)
                .computeContractionHierarchy();

        // computation
        TransitNodeRoutingPrecomputation<Integer, DefaultWeightedEdge> precomputation =
            new TransitNodeRoutingPrecomputation<>(contractionHierarchy, 1, executor);
        TransitNodeRouting<Integer, DefaultWeightedEdge> routing =
            precomputation.computeTransitNodeRouting();

        Map<Integer, ContractionVertex<Integer>> contractionMapping =
            contractionHierarchy.getContractionMapping();
        ContractionVertex<Integer> contractionVertex = contractionMapping.get(vertex);

        // transit vertices
        assertTrue(routing.getTransitVertices().contains(contractionVertex));

        // access vertices
        AccessVertices<Integer, DefaultWeightedEdge> accessVertices = routing.getAccessVertices();

        List<AccessVertex<Integer, DefaultWeightedEdge>> forwardAccessVertices =
            accessVertices.getForwardAccessVertices(contractionVertex);
        List<AccessVertex<Integer, DefaultWeightedEdge>> backwardAccessVertices =
            accessVertices.getBackwardAccessVertices(contractionVertex);

        assertEquals(forwardAccessVertices.size(), 1);
        assertEquals(forwardAccessVertices.get(0).getVertex(), vertex);
        GraphPath<Integer, DefaultWeightedEdge> expectedPath1 = new GraphWalk<>(
            graph, vertex, vertex, Collections.singletonList(vertex), Collections.emptyList(), 0.0);
        assertEquals(expectedPath1, forwardAccessVertices.get(0).getPath());

        assertEquals(backwardAccessVertices.size(), 1);
        assertEquals(backwardAccessVertices.get(0).getVertex(), vertex);
        GraphPath<Integer, DefaultWeightedEdge> expectedPath2 = new GraphWalk<>(
            graph, vertex, vertex, Collections.singletonList(vertex), Collections.emptyList(), 0.0);
        assertEquals(expectedPath2, forwardAccessVertices.get(0).getPath());

        // locality filter
        assertFalse(routing.getLocalityFilter().isLocal(vertex, vertex));
    }

    @Test
    public void testThreeVertices()
    {
        Integer v1 = 1;
        Integer v2 = 2;
        Integer v3 = 3;
        Graph<Integer, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);
        DefaultWeightedEdge edge1 = Graphs.addEdgeWithVertices(graph, v1, v2, 1.0);
        DefaultWeightedEdge edge2 = Graphs.addEdgeWithVertices(graph, v2, v1, 1.0);
        DefaultWeightedEdge edge3 = Graphs.addEdgeWithVertices(graph, v2, v3, 2.0); // to ensure
                                                                                    // Voronoi
                                                                                    // diagram
                                                                                    // correctness
        DefaultWeightedEdge edge4 = Graphs.addEdgeWithVertices(graph, v3, v2, 2.0);

        ContractionHierarchy<Integer, DefaultWeightedEdge> contractionHierarchy =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor)
                .computeContractionHierarchy();

        // computation
        TransitNodeRoutingPrecomputation<Integer, DefaultWeightedEdge> precomputation =
            new TransitNodeRoutingPrecomputation<>(contractionHierarchy, 1, executor);
        TransitNodeRouting<Integer, DefaultWeightedEdge> routing =
            precomputation.computeTransitNodeRouting();

        Map<Integer, ContractionVertex<Integer>> contractionMapping =
            routing.getContractionHierarchy().getContractionMapping();
        ContractionVertex<Integer> cv1 = contractionMapping.get(v1);
        ContractionVertex<Integer> cv2 = contractionMapping.get(v2);
        ContractionVertex<Integer> cv3 = contractionMapping.get(v3);

        // transit vertices
        assertTrue(routing.getTransitVertices().contains(cv2));

        // access vertices
        AccessVertices<Integer, DefaultWeightedEdge> accessVertices = routing.getAccessVertices();
        List<AccessVertex<Integer, DefaultWeightedEdge>> cv1ForwardAccessVertices =
            accessVertices.getForwardAccessVertices(cv1);
        List<AccessVertex<Integer, DefaultWeightedEdge>> cv1BackwardAccessVertices =
            accessVertices.getBackwardAccessVertices(cv1);

        assertEquals(cv1ForwardAccessVertices.size(), 1);
        assertEquals(cv1ForwardAccessVertices.get(0).getVertex(), v2);
        GraphPath<Integer, DefaultWeightedEdge> expectedPath1 = new GraphWalk<>(
            graph, v1, v2, Arrays.asList(v1, v2), Collections.singletonList(edge1), 1.0);
        assertEquals(expectedPath1, cv1ForwardAccessVertices.get(0).getPath());

        assertEquals(cv1BackwardAccessVertices.size(), 1);
        assertEquals(cv1BackwardAccessVertices.get(0).getVertex(), v2);
        GraphPath<Integer, DefaultWeightedEdge> expectedPath2 = new GraphWalk<>(
            graph, v2, v1, Arrays.asList(v2, v1), Collections.singletonList(edge2), 1.0);
        assertEquals(expectedPath2, cv1BackwardAccessVertices.get(0).getPath());

        List<AccessVertex<Integer, DefaultWeightedEdge>> cv2ForwardAccessVertices =
            accessVertices.getForwardAccessVertices(cv2);
        List<AccessVertex<Integer, DefaultWeightedEdge>> cv2BackwardAccessVertices =
            accessVertices.getBackwardAccessVertices(cv2);
        assertEquals(cv2ForwardAccessVertices.size(), 1);
        assertEquals(cv2BackwardAccessVertices.size(), 1);

        List<AccessVertex<Integer, DefaultWeightedEdge>> cv3ForwardAccessVertices =
            accessVertices.getForwardAccessVertices(cv3);
        List<AccessVertex<Integer, DefaultWeightedEdge>> cv3BackwardAccessVertices =
            accessVertices.getBackwardAccessVertices(cv3);
        assertEquals(cv3ForwardAccessVertices.size(), 1);
        assertEquals(cv3ForwardAccessVertices.get(0).getVertex(), v2);
        GraphPath<Integer, DefaultWeightedEdge> expectedPath3 = new GraphWalk<>(
            graph, v3, v2, Arrays.asList(v3, v2), Collections.singletonList(edge4), 2.0);
        assertEquals(expectedPath3, cv3ForwardAccessVertices.get(0).getPath());

        assertEquals(cv3BackwardAccessVertices.size(), 1);
        assertEquals(cv3ForwardAccessVertices.get(0).getVertex(), v2);
        GraphPath<Integer, DefaultWeightedEdge> expectedPath4 = new GraphWalk<>(
            graph, v2, v3, Arrays.asList(v2, v3), Collections.singletonList(edge3), 2.0);
        assertEquals(expectedPath4, cv3BackwardAccessVertices.get(0).getPath());

        // locality filter
        assertTrue(routing.getLocalityFilter().isLocal(v1, v1));
        assertFalse(routing.getLocalityFilter().isLocal(v1, v2));
        assertTrue(routing.getLocalityFilter().isLocal(v1, v3));
        assertFalse(routing.getLocalityFilter().isLocal(v2, v2));
        assertFalse(routing.getLocalityFilter().isLocal(v2, v3));
        assertTrue(routing.getLocalityFilter().isLocal(v3, v3));
    }

    @Test
    public void testOnRandomGraphs()
    {
        int numOfVertices = 30;
        int vertexDegree = 5;
        int numOfIterations = 50;

        Random random = new Random(SEED);

        for (int i = 0; i < numOfIterations; ++i) {
            Graph<Integer, DefaultWeightedEdge> graph =
                generateRandomGraph(numOfVertices, vertexDegree * numOfVertices, random);
            TransitNodeRoutingPrecomputation<Integer, DefaultWeightedEdge> routing =
                new TransitNodeRoutingPrecomputation<>(graph, executor);
            assertCorrectTNR(graph, routing.computeTransitNodeRouting());
        }
    }

    /**
     * Checks given {@code routing} for correctness wrt the provided {@code graph}. Firstly, checks
     * that the number of transit vertices is equal to $\sqrt{|V|}$, here $V$ is the set of
     * vertices. Secondly, checks that the transit vertices are selected from the top of the
     * contraction hierarchy. Thirdly, checks that the set of sources and targets of many-to-many
     * shortest paths are equal to the set of transit vertices. Fourthly, checks that cell ids in
     * the Voronoi diagram are in range $[-1, |V| - 1]$. Finally, checks that paths for access
     * vertices are computed correctly.
     *
     * @param graph graph
     * @param routing transit node routing
     */
    private void assertCorrectTNR(
        Graph<Integer, DefaultWeightedEdge> graph,
        TransitNodeRouting<Integer, DefaultWeightedEdge> routing)
    {
        int numberOfVertices = graph.vertexSet().size();

        // check transit vertices
        assertEquals((int) Math.sqrt(numberOfVertices), routing.getTransitVertices().size());
        for (ContractionVertex<Integer> vertex : routing.getTransitVertices()) {
            assertTrue(
                vertex.contractionLevel >= numberOfVertices - routing.getTransitVertices().size());
        }

        // many-to-many shortest paths
        Set<Integer> transitVerticesSet = routing
            .getTransitVertices().stream().map(v -> v.vertex)
            .collect(Collectors.toCollection(HashSet::new));
        assertEquals(transitVerticesSet, routing.getTransitVerticesPaths().getSources());
        assertEquals(transitVerticesSet, routing.getTransitVerticesPaths().getTargets());

        // check Voronoi diagram
        VoronoiDiagram<Integer> voronoiDiagram = routing.getVoronoiDiagram();
        for (ContractionVertex<Integer> vertex : routing
            .getContractionHierarchy().getContractionGraph().vertexSet())
        {
            int voronoiCellId = voronoiDiagram.getVoronoiCellId(vertex);
            assertTrue(voronoiCellId >= -1 && voronoiCellId < numberOfVertices);
        }

        // check access vertices
        AccessVertices<Integer, DefaultWeightedEdge> accessVertices = routing.getAccessVertices();
        ShortestPathAlgorithm<Integer, DefaultWeightedEdge> sp =
            new BidirectionalDijkstraShortestPath<>(graph);
        for (ContractionVertex<Integer> vertex : routing
            .getContractionHierarchy().getContractionGraph().vertexSet())
        {
            List<AccessVertex<Integer, DefaultWeightedEdge>> av =
                accessVertices.getForwardAccessVertices(vertex);
            for (AccessVertex<Integer, DefaultWeightedEdge> accessVertex : av) {
                assertEquals(
                    sp.getPath(vertex.vertex, accessVertex.getVertex()), accessVertex.getPath());
            }
        }
        for (ContractionVertex<Integer> vertex : routing
            .getContractionHierarchy().getContractionGraph().vertexSet())
        {
            List<AccessVertex<Integer, DefaultWeightedEdge>> av =
                accessVertices.getBackwardAccessVertices(vertex);
            for (AccessVertex<Integer, DefaultWeightedEdge> accessVertex : av) {
                assertEquals(
                    sp.getPath(accessVertex.getVertex(), vertex.vertex), accessVertex.getPath());
            }
        }
    }

    /**
     * Generates a graph instance from the $G(n,M)$ random graphs model with {@code numOfVertices}
     * vertices and {@code numOfEdges} edges.
     *
     * @param numOfVertices number of vertices in a graph
     * @param numOfEdges number of edges in a graph
     * @param random random generator
     * @return random graph
     */
    private Graph<Integer, DefaultWeightedEdge> generateRandomGraph(
        int numOfVertices, int numOfEdges, Random random)
    {
        DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());

        GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator =
            new GnmRandomGraphGenerator<>(numOfVertices, numOfEdges - numOfVertices + 1, SEED);
        generator.generateGraph(graph);
        makeConnected(graph);
        addEdgeWeights(graph, random);

        return graph;
    }

    /**
     * Makes {@code graph} connected.
     *
     * @param graph a graph
     */
    private void makeConnected(Graph<Integer, DefaultWeightedEdge> graph)
    {
        Object[] vertices = graph.vertexSet().toArray();
        for (int i = 0; i < vertices.length - 1; ++i) {
            graph.addEdge((Integer) vertices[i], (Integer) vertices[i + 1]);
            graph.addEdge((Integer) vertices[i + 1], (Integer) vertices[i]);
        }
    }

    /**
     * Sets weight for every edge in the {@code graph}.
     *
     * @param graph a graph
     * @param random random generator instance
     */
    private void addEdgeWeights(Graph<Integer, DefaultWeightedEdge> graph, Random random)
    {
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            graph.setEdgeWeight(edge, random.nextDouble());
        }
    }
}
