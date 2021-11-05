/*
 * (C) Copyright 2003-2021, by John V Sichi and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.*;
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * .
 *
 * @author John V. Sichi
 */
public class GraphGeneratorTest
{
    // ~ Static fields/initializers ---------------------------------------------

    private static final int SIZE = 10;

    // ~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    @Test
    public void testEmptyGraphGenerator()
    {
        GraphGenerator<Object, DefaultEdge, Object> gen = new EmptyGraphGenerator<>(SIZE);
        Graph<Object, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        Map<String, Object> resultMap = new HashMap<>();
        gen.generateGraph(g, resultMap);
        assertEquals(SIZE, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());
        assertTrue(resultMap.isEmpty());
    }

    /**
     * .
     */
    @Test
    public void testLinearGraphGenerator()
    {
        GraphGenerator<Object, DefaultEdge, Object> gen = new LinearGraphGenerator<>(SIZE);
        Graph<Object, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        Map<String, Object> resultMap = new HashMap<>();
        gen.generateGraph(g, resultMap);
        assertEquals(SIZE, g.vertexSet().size());
        assertEquals(SIZE - 1, g.edgeSet().size());

        Object startVertex = resultMap.get(LinearGraphGenerator.START_VERTEX);
        Object endVertex = resultMap.get(LinearGraphGenerator.END_VERTEX);

        for (Object vertex : g.vertexSet()) {
            if (vertex == startVertex) {
                assertEquals(0, g.inDegreeOf(vertex));
                assertEquals(1, g.outDegreeOf(vertex));

                continue;
            }

            if (vertex == endVertex) {
                assertEquals(1, g.inDegreeOf(vertex));
                assertEquals(0, g.outDegreeOf(vertex));

                continue;
            }

            assertEquals(1, g.inDegreeOf(vertex));
            assertEquals(1, g.outDegreeOf(vertex));
        }
    }

    /**
     * .
     */
    @Test
    public void testRingGraphGenerator()
    {
        GraphGenerator<Object, DefaultEdge, Object> gen = new RingGraphGenerator<>(SIZE);
        Graph<Object, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        Map<String, Object> resultMap = new HashMap<>();
        gen.generateGraph(g, resultMap);
        assertEquals(SIZE, g.vertexSet().size());
        assertEquals(SIZE, g.edgeSet().size());

        Object startVertex = g.vertexSet().iterator().next();
        assertEquals(1, g.outDegreeOf(startVertex));

        Object nextVertex = startVertex;
        Set<Object> seen = new HashSet<>();

        for (int i = 0; i < SIZE; ++i) {
            DefaultEdge nextEdge = g.outgoingEdgesOf(nextVertex).iterator().next();
            nextVertex = g.getEdgeTarget(nextEdge);
            assertEquals(1, g.inDegreeOf(nextVertex));
            assertEquals(1, g.outDegreeOf(nextVertex));
            assertFalse(seen.contains(nextVertex));
            seen.add(nextVertex);
        }

        // do you ever get the feeling you're going in circles?
        assertTrue(nextVertex == startVertex);
        assertTrue(resultMap.isEmpty());
    }

    /**
     * .
     */
    @Test
    public void testCompleteGraphGenerator()
    {
        Graph<Object, DefaultEdge> completeGraph = new SimpleGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        CompleteGraphGenerator<Object, DefaultEdge> completeGenerator =
            new CompleteGraphGenerator<>(10);
        completeGenerator.generateGraph(completeGraph);

        // complete graph with 10 vertices has 10*(10-1)/2 = 45 edges
        assertEquals(45, completeGraph.edgeSet().size());
    }

    @Test
    public void testCompleteGraphGeneratorWithDirectedGraph()
    {
        Graph<Object, DefaultEdge> completeGraph = new SimpleDirectedGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        CompleteGraphGenerator<Object, DefaultEdge> completeGenerator =
            new CompleteGraphGenerator<>(10);
        completeGenerator.generateGraph(completeGraph);

        // complete graph with 10 vertices has 10*(10-1) = 90 edges
        assertEquals(90, completeGraph.edgeSet().size());
    }

    @Test
    public void testCompleteGraphGeneratorWithPreexistingVertices()
    {
        Graph<Object, DefaultEdge> completeGraph = new SimpleGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        for (int i = 0; i < 10; i++)
            completeGraph.addVertex();
        CompleteGraphGenerator<Object, DefaultEdge> completeGenerator =
            new CompleteGraphGenerator<>();
        completeGenerator.generateGraph(completeGraph);

        // complete graph with 10 vertices has 10*(10-1)/2 = 45 edges
        assertEquals(45, completeGraph.edgeSet().size());
    }

    /**
     * .
     */
    @Test
    public void testScaleFreeGraphGenerator()
    {
        Graph<Object, DefaultEdge> graph = new DefaultDirectedGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        ScaleFreeGraphGenerator<Object, DefaultEdge> generator = new ScaleFreeGraphGenerator<>(500);
        generator.generateGraph(graph);
        ConnectivityInspector<Object, DefaultEdge> inspector = new ConnectivityInspector<>(graph);
        assertTrue("generated graph is not connected", inspector.isConnected());

        try {
            new ScaleFreeGraphGenerator<>(-50);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }

        try {
            new ScaleFreeGraphGenerator<>(-50, 31337);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }

        generator = new ScaleFreeGraphGenerator<>(0);
        Graph<Object, DefaultEdge> empty = new DefaultDirectedGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(empty);
        assertTrue("non-empty graph generated", empty.vertexSet().isEmpty());
    }

    /**
     * .
     */
    @Test
    public void testCompleteBipartiteGraphGenerator()
    {
        Graph<Object, DefaultEdge> completeBipartiteGraph = new SimpleGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        CompleteBipartiteGraphGenerator<Object, DefaultEdge> completeBipartiteGenerator =
            new CompleteBipartiteGraphGenerator<>(10, 4);
        completeBipartiteGenerator.generateGraph(completeBipartiteGraph);

        // Complete bipartite graph with 10 and 4 vertices should have 14
        // total vertices and 4*10=40 total edges
        assertEquals(14, completeBipartiteGraph.vertexSet().size());
        assertEquals(40, completeBipartiteGraph.edgeSet().size());
    }

    @Test
    public void testCompleteBipartiteGraphGeneratorWithPreexistingVertices()
    {
        Graph<Object, DefaultEdge> completeBipartiteGraph = new SimpleGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        Set<Object> partitionA = new HashSet<>();
        for (int i = 0; i < 10; i++)
            partitionA.add(completeBipartiteGraph.addVertex());
        Set<Object> partitionB = new HashSet<>();
        for (int i = 0; i < 4; i++)
            partitionB.add(completeBipartiteGraph.addVertex());

        CompleteBipartiteGraphGenerator<Object, DefaultEdge> completeBipartiteGenerator =
            new CompleteBipartiteGraphGenerator<>(partitionA, partitionB);
        completeBipartiteGenerator.generateGraph(completeBipartiteGraph);

        // Complete bipartite graph with 10 and 4 vertices should have 14
        // total vertices and 4*10=40 total edges
        assertEquals(14, completeBipartiteGraph.vertexSet().size());
        assertEquals(40, completeBipartiteGraph.edgeSet().size());
    }

    /**
     * .
     */
    @Test
    public void testHyperCubeGraphGenerator()
    {
        Graph<Object, DefaultEdge> hyperCubeGraph = new SimpleGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        HyperCubeGraphGenerator<Object, DefaultEdge> hyperCubeGenerator =
            new HyperCubeGraphGenerator<>(4);
        hyperCubeGenerator.generateGraph(hyperCubeGraph);

        // Hypercube of 4 dimensions should have 2^4=16 vertices and
        // 4*2^(4-1)=32 total edges
        assertEquals(16, hyperCubeGraph.vertexSet().size());
        assertEquals(32, hyperCubeGraph.edgeSet().size());
    }

    /**
     * .
     */
    @Test
    public void testStarGraphGenerator()
    {
        Map<String, Object> map = new HashMap<>();
        Graph<Object, DefaultEdge> starGraph = new SimpleGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        StarGraphGenerator<Object, DefaultEdge> starGenerator = new StarGraphGenerator<>(10);
        starGenerator.generateGraph(starGraph, map);

        // Star graph of order 10 should have 10 vertices and 9 edges
        assertEquals(9, starGraph.edgeSet().size());
        assertEquals(10, starGraph.vertexSet().size());
        assertTrue(map.get(StarGraphGenerator.CENTER_VERTEX) != null);
    }

    /**
     * .
     */
    @Test
    public void testGridGraphGenerator()
    {
        int rows = 3;
        int cols = 4;

        GridGraphGenerator<String, String> generator = new GridGraphGenerator<>(rows, cols);
        Map<String, String> resultMap = new HashMap<>();

        // validating a directed and undirected graph
        Graph<String, String> directedGridGraph = new DefaultDirectedGraph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.createStringSupplier(1), false);
        generator.generateGraph(directedGridGraph, resultMap);
        validateGridGraphGenerator(rows, cols, directedGridGraph, resultMap);

        resultMap.clear();
        Graph<String, String> undirectedGridGraph = new SimpleGraph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.createStringSupplier(1), false);
        generator.generateGraph(undirectedGridGraph, resultMap);
        validateGridGraphGenerator(rows, cols, undirectedGridGraph, resultMap);
    }

    public void validateGridGraphGenerator(
        int rows, int cols, Graph<String, String> gridGraph, Map<String, String> resultMap)
    {
        // graph structure validations
        int expectedVerticeNum = rows * cols;
        assertEquals(
            "number of vertices is wrong (" + gridGraph.vertexSet().size() + "), should be "
                + expectedVerticeNum,
            expectedVerticeNum, gridGraph.vertexSet().size());
        int expectedEdgesNum = (((rows - 1) * cols) + ((cols - 1) * rows))
            * ((gridGraph.getType().isUndirected()) ? 1 : 2);
        assertEquals(
            "number of edges is wrong (" + gridGraph.edgeSet().size() + "), should be "
                + expectedEdgesNum,
            expectedEdgesNum, gridGraph.edgeSet().size());

        int cornerVertices = 0, borderVertices = 0, innerVertices = 0, neighborsSize;
        int expCornerVertices = 4;
        int expBorderVertices = Math.max(((rows - 2) * 2) + ((cols - 2) * 2), 0);
        int expInnerVertices = Math.max((rows - 2) * (cols - 2), 0);
        Set<String> neighbors = new HashSet<>();

        for (String v : gridGraph.vertexSet()) {
            neighbors.clear();
            neighbors.addAll(Graphs.neighborListOf(gridGraph, v));
            neighborsSize = neighbors.size();
            assertTrue(
                "vertex with illegal number of neighbors (" + neighborsSize + ").",
                (neighborsSize == 2) || (neighborsSize == 3) || (neighborsSize == 4));
            if (neighborsSize == 2) {
                cornerVertices++;
            } else if (neighborsSize == 3) {
                borderVertices++;
            } else if (neighborsSize == 4) {
                innerVertices++;
            }
        }
        assertEquals(
            "there should be exactly " + expCornerVertices
                + " corner (with two neighbors) vertices. " + " actual number is " + cornerVertices
                + ".",
            expCornerVertices, cornerVertices);
        assertEquals(
            "there should be exactly " + expBorderVertices
                + " border (with three neighbors) vertices. " + " actual number is "
                + borderVertices + ".",
            expBorderVertices, borderVertices);
        assertEquals(
            "there should be exactly " + expInnerVertices
                + " inner (with four neighbors) vertices. " + " actual number is " + innerVertices
                + ".",
            expInnerVertices, innerVertices);

        // result map validations
        Set<String> keys = resultMap.keySet();
        assertEquals(
            "result map contains should contains exactly 4 corner verices", 4, keys.size());

        for (String key : keys) {
            neighbors.clear();
            neighbors.addAll(Graphs.neighborListOf(gridGraph, resultMap.get(key)));
            neighborsSize = neighbors.size();
            assertEquals("corner vertex should have exactly 2 neighbors", 2, neighborsSize);
        }
    }
}
