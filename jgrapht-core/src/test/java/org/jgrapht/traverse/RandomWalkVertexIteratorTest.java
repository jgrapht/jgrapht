/*
 * (C) Copyright 2016-2021, by Assaf Mizrachi and Contributors.
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

package org.jgrapht.traverse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.generate.LinearGraphGenerator;
import org.jgrapht.generate.RingGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

/**
 * Tests for the {@link RandomWalkVertexIterator} class.
 * 
 * @author Assaf Mizrachi
 *
 */
public class RandomWalkVertexIteratorTest
{

    /**
     * Tests invalid vertex
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVertex()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        new RandomWalkVertexIterator<>(graph, "unknown", 100);
    }

    /**
     * Tests single node graph
     */
    @Test
    public void testSingleNode()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("123");
        Iterator<String> iter = new RandomWalkVertexIterator<>(graph, "123");
        assertTrue(iter.hasNext());
        assertEquals("123", iter.next());
        assertFalse(iter.hasNext());
    }

    /**
     * Tests iterator does not have more elements after reaching sink vertex.
     */
    @Test
    public void testSink()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .directed().vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeClass(DefaultEdge.class).allowingMultipleEdges(false).allowingSelfLoops(true)
                .buildGraph();
        int graphSize = 10;
        LinearGraphGenerator<String, DefaultEdge> graphGenerator =
            new LinearGraphGenerator<>(graphSize);
        graphGenerator.generateGraph(graph);
        Iterator<String> iter =
            new RandomWalkVertexIterator<>(graph, graph.vertexSet().iterator().next());
        for (int i = 0; i < graphSize; i++) {
            assertTrue(iter.hasNext());
            assertNotNull(iter.next());
        }
        assertFalse(iter.hasNext());
    }

    /**
     * Tests iterator is exhausted after maxSteps
     */
    @Test
    public void testExhausted()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier(1))
                .edgeClass(DefaultEdge.class).allowingMultipleEdges(false).allowingSelfLoops(false)
                .buildGraph();

        RingGraphGenerator<String, DefaultEdge> graphGenerator = new RingGraphGenerator<>(10);
        graphGenerator.generateGraph(graph);
        long maxSteps = 4;
        Iterator<String> iter = new RandomWalkVertexIterator<>(graph, "1", maxSteps);
        List<String> walk = new ArrayList<>();
        while (iter.hasNext()) {
            walk.add(iter.next());
        }
        assertEquals(walk.size(), 5);
    }

    /**
     * Test deterministic walk using directed ring graph.
     */
    @Test
    public void testDeterministic()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .directed().vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeClass(DefaultEdge.class).allowingMultipleEdges(false).allowingSelfLoops(true)
                .buildGraph();

        int ringSize = 5;
        RingGraphGenerator<String, DefaultEdge> graphGenerator = new RingGraphGenerator<>(ringSize);
        graphGenerator.generateGraph(graph);
        Iterator<String> iter = new RandomWalkVertexIterator<>(graph, "0", 20);
        int step = 0;
        while (iter.hasNext()) {
            assertEquals(String.valueOf(step % ringSize), iter.next());
            step++;
        }
    }

    /**
     * Tests for a long time
     */
    @Test
    public void testLongTime()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeClass(DefaultEdge.class).allowingMultipleEdges(false).allowingSelfLoops(false)
                .buildGraph();

        graph.addVertex("0");
        graph.addVertex("1");
        graph.addVertex("2");

        graph.addEdge("0", "1");
        graph.addEdge("1", "2");

        Iterator<String> iter = new RandomWalkVertexIterator<>(graph, "0");
        int count = 0;
        List<String> walk = new ArrayList<>();
        while (iter.hasNext()) {
            if (count >= 10000) {
                break;
            }
            count++;
            walk.add(iter.next());
        }
        assert count == 10000;
    }

}
