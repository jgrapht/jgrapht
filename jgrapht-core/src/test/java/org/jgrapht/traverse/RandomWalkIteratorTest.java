/*
 * (C) Copyright 2016-2018, by Assaf Mizrachi and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for the {@link RandomWalkIterator} class.
 * 
 * @author Assaf Mizrachi
 *
 */
public class RandomWalkIteratorTest
{

    /**
     * Tests empty graph
     */
    @Test
    public void testEmptyGraph()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        Iterator<String> iter = new RandomWalkIterator<>(graph);
        assertFalse(iter.hasNext());
    }

    /**
     * Tests single node graph
     */
    @Test
    public void testSingleNode()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("123");
        Iterator<String> iter = new RandomWalkIterator<>(graph);
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
        Iterator<String> iter = new RandomWalkIterator<>(graph);
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
        int maxSteps = 4;
        Iterator<String> iter = new RandomWalkIterator<>(graph, "1", false, maxSteps);
        for (int i = 0; i < maxSteps; i++) {
            assertTrue(iter.hasNext());
            assertNotNull(iter.next());
        }
        assertFalse(iter.hasNext());
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
        Iterator<String> iter = new RandomWalkIterator<>(graph, "0", false, 20);
        int step = 0;
        while (iter.hasNext()) {
            step++;
            assertEquals(String.valueOf(step % ringSize), iter.next());
        }
    }

}
