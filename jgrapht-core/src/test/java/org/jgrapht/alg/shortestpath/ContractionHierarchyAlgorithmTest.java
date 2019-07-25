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
import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.Test;

import java.util.Map;
import java.util.Random;

import static org.jgrapht.alg.shortestpath.ContractionHierarchyAlgorithm.ContractionEdge;
import static org.jgrapht.alg.shortestpath.ContractionHierarchyAlgorithm.ContractionVertex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link ContractionHierarchyAlgorithm}..
 */
public class ContractionHierarchyAlgorithmTest {
    /**
     * Seed for random numbers generator used in tests.
     */
    private static final long SEED = 19L;

    @Test
    public void testEmptyGraph() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        ContractionHierarchyAlgorithm<Integer, DefaultWeightedEdge> contractor
                = new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED));
        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> p = contractor.computeContractionHierarchy();

        assertNotNull(p);


        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph = p.getFirst();
        Map<Integer, ContractionVertex<Integer>> contractionMapping = p.getSecond();


        assertNotNull(contractionGraph);
        assertNotNull(contractionMapping);

        assertTrue(contractionGraph.vertexSet().isEmpty());
        assertTrue(contractionGraph.edgeSet().isEmpty());
        assertTrue(contractionMapping.keySet().isEmpty());
    }


    @Test
    public void testDirectedGraph1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 2, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 1);

        ContractionHierarchyAlgorithm<Integer, DefaultWeightedEdge> contractor
                = new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED));
        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> p = contractor.computeContractionHierarchy();

        assertNotNull(p);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph = p.getFirst();
        Map<Integer, ContractionVertex<Integer>> contractionMapping = p.getSecond();

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(3, contractionGraph.vertexSet().size());
        assertEquals(2, contractionGraph.edgeSet().size());

        assertTrue(contractionGraph.containsEdge(contractionMapping.get(1), contractionMapping.get(2)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(2), contractionMapping.get(3)));
    }

    @Test
    public void testDirectedGraph2() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 2, 1);
        Graphs.addEdgeWithVertices(graph, 2, 1, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 1);
        Graphs.addEdgeWithVertices(graph, 3, 2, 1);
        Graphs.addEdgeWithVertices(graph, 3, 1, 1);
        Graphs.addEdgeWithVertices(graph, 1, 3, 1);

        ContractionHierarchyAlgorithm<Integer, DefaultWeightedEdge> contractor
                = new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED));
        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> p = contractor.computeContractionHierarchy();

        assertNotNull(p);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph = p.getFirst();
        Map<Integer, ContractionVertex<Integer>> contractionMapping = p.getSecond();

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(3, contractionGraph.vertexSet().size());
        assertEquals(6, contractionGraph.edgeSet().size());
    }

    @Test
    public void testDirectedGraph3() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 3, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 1);

        Graphs.addEdgeWithVertices(graph, 3, 4, 1);
        Graphs.addEdgeWithVertices(graph, 3, 5, 1);

        ContractionHierarchyAlgorithm<Integer, DefaultWeightedEdge> contractor
                = new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED));
        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> p = contractor.computeContractionHierarchy();

        assertNotNull(p);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph = p.getFirst();
        Map<Integer, ContractionVertex<Integer>> contractionMapping = p.getSecond();

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(5, contractionGraph.vertexSet().size());
        assertEquals(4, contractionGraph.edgeSet().size());

        assertTrue(contractionGraph.containsEdge(contractionMapping.get(1), contractionMapping.get(3)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(2), contractionMapping.get(3)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(3), contractionMapping.get(4)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(3), contractionMapping.get(5)));
    }


    @Test
    public void testUndirectedGraph1() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 2, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 1);

        ContractionHierarchyAlgorithm<Integer, DefaultWeightedEdge> contractor
                = new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED));
        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> p = contractor.computeContractionHierarchy();

        assertNotNull(p);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph = p.getFirst();
        Map<Integer, ContractionVertex<Integer>> contractionMapping = p.getSecond();

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(3, contractionGraph.vertexSet().size());
        assertEquals(4, contractionGraph.edgeSet().size());

        assertTrue(contractionGraph.containsEdge(contractionMapping.get(1), contractionMapping.get(2)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(2), contractionMapping.get(1)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(2), contractionMapping.get(3)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(3), contractionMapping.get(2)));
    }

    @Test
    public void testUndirectedGraph2() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 2, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 1);
        Graphs.addEdgeWithVertices(graph, 3, 1, 1);

        ContractionHierarchyAlgorithm<Integer, DefaultWeightedEdge> contractor
                = new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED));
        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> p = contractor.computeContractionHierarchy();

        assertNotNull(p);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph = p.getFirst();
        Map<Integer, ContractionVertex<Integer>> contractionMapping = p.getSecond();

        assertEquals(3, graph.vertexSet().size());

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(3, contractionGraph.vertexSet().size());
        assertEquals(6, contractionGraph.edgeSet().size());
    }

    @Test
    public void testUndirectedGraph3() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 3, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3, 1);

        Graphs.addEdgeWithVertices(graph, 3, 4, 1);
        Graphs.addEdgeWithVertices(graph, 3, 5, 1);

        ContractionHierarchyAlgorithm<Integer, DefaultWeightedEdge> contractor
                = new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED));
        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> p = contractor.computeContractionHierarchy();

        assertNotNull(p);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph = p.getFirst();
        Map<Integer, ContractionVertex<Integer>> contractionMapping = p.getSecond();

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(5, contractionGraph.vertexSet().size());
        assertEquals(8, contractionGraph.edgeSet().size());

        assertTrue(contractionGraph.containsEdge(contractionMapping.get(1), contractionMapping.get(3)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(3), contractionMapping.get(1)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(2), contractionMapping.get(3)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(3), contractionMapping.get(2)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(3), contractionMapping.get(4)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(4), contractionMapping.get(3)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(3), contractionMapping.get(5)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(5), contractionMapping.get(3)));
    }

    @Test
    public void testUndirectedGraph4() {
        Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 2, 3);
        Graphs.addEdgeWithVertices(graph, 1, 4, 1);

        Graphs.addEdgeWithVertices(graph, 2, 3, 3);
        Graphs.addEdgeWithVertices(graph, 2, 5, 1);

        Graphs.addEdgeWithVertices(graph, 3, 6, 1);

        Graphs.addEdgeWithVertices(graph, 4, 5, 1);
        Graphs.addEdgeWithVertices(graph, 4, 7, 1);

        Graphs.addEdgeWithVertices(graph, 5, 6, 1);
        Graphs.addEdgeWithVertices(graph, 5, 8, 1);

        Graphs.addEdgeWithVertices(graph, 6, 9, 1);

        Graphs.addEdgeWithVertices(graph, 7, 8, 3);
        Graphs.addEdgeWithVertices(graph, 8, 9, 3);

        ContractionHierarchyAlgorithm<Integer, DefaultWeightedEdge> contractor
                = new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED));
        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> p = contractor.computeContractionHierarchy();

        assertNotNull(p);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph = p.getFirst();
        Map<Integer, ContractionVertex<Integer>> contractionMapping = p.getSecond();

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(9, contractionGraph.vertexSet().size());
        assertEquals(24, contractionGraph.edgeSet().size());

        assertTrue(contractionGraph.containsEdge(contractionMapping.get(1), contractionMapping.get(2)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(2), contractionMapping.get(1)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(1), contractionMapping.get(4)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(4), contractionMapping.get(1)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(2), contractionMapping.get(3)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(3), contractionMapping.get(2)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(2), contractionMapping.get(5)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(5), contractionMapping.get(2)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(3), contractionMapping.get(6)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(6), contractionMapping.get(3)));

        assertTrue(contractionGraph.containsEdge(contractionMapping.get(4), contractionMapping.get(5)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(5), contractionMapping.get(4)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(4), contractionMapping.get(7)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(7), contractionMapping.get(4)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(5), contractionMapping.get(6)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(6), contractionMapping.get(5)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(5), contractionMapping.get(8)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(8), contractionMapping.get(5)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(6), contractionMapping.get(9)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(9), contractionMapping.get(6)));

        assertTrue(contractionGraph.containsEdge(contractionMapping.get(7), contractionMapping.get(8)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(8), contractionMapping.get(7)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(8), contractionMapping.get(9)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(9), contractionMapping.get(8)));

    }


    @Test
    public void testPseudograph() {
        Graph<Integer, DefaultWeightedEdge> graph = new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addEdgeWithVertices(graph, 1, 2, 1);
        Graphs.addEdgeWithVertices(graph, 1, 2, 2);
        Graphs.addEdgeWithVertices(graph, 1, 2, 3);

        Graphs.addEdgeWithVertices(graph, 2, 1, 1);
        Graphs.addEdgeWithVertices(graph, 2, 1, 2);

        Graphs.addEdgeWithVertices(graph, 2, 2, 1);
        Graphs.addEdgeWithVertices(graph, 2, 2, 2);

        ContractionHierarchyAlgorithm<Integer, DefaultWeightedEdge> contractor
                = new ContractionHierarchyAlgorithm<>(graph, () -> new Random(SEED));
        Pair<Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>>,
                Map<Integer, ContractionVertex<Integer>>> p = contractor.computeContractionHierarchy();

        assertNotNull(p);

        Graph<ContractionVertex<Integer>, ContractionEdge<DefaultWeightedEdge>> contractionGraph = p.getFirst();
        Map<Integer, ContractionVertex<Integer>> contractionMapping = p.getSecond();

        assertTrue(contractionGraph.getType().isDirected());
        assertTrue(contractionGraph.getType().isSimple());

        assertEquals(2, contractionGraph.vertexSet().size());
        assertEquals(2, contractionGraph.edgeSet().size());

        assertTrue(contractionGraph.containsEdge(contractionMapping.get(1), contractionMapping.get(2)));
        assertTrue(contractionGraph.containsEdge(contractionMapping.get(2), contractionMapping.get(1)));

        assertEquals(1, contractionGraph.getEdgeWeight(contractionGraph.getEdge(
                contractionMapping.get(1),
                contractionMapping.get(2)
        )), 1e-9);
        assertEquals(1, contractionGraph.getEdgeWeight(contractionGraph.getEdge(
                contractionMapping.get(2),
                contractionMapping.get(1)
        )), 1e-9);
    }
}