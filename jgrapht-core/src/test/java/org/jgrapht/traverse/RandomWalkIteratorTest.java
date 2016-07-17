/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2016, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* ----------------
 * RandomWalkIteratorTest.java
 * ----------------
 * (C) Copyright 2016-, by Assaf Mizrachi and Contributors.
 *
 * Original Author:  Assaf Mizrachi
 * Contributor(s):   
 *
 * $Id$
 *
 * Changes
 * -------
 *
 */
package org.jgrapht.traverse;

import java.util.Iterator;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EnhancedTestCase;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.LinearGraphGenerator;
import org.jgrapht.generate.RingGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * Tests for the {@link RandomWalkIterator} class.
 * 
 * @author Assaf Mizrachi
 *
 */
public class RandomWalkIteratorTest extends EnhancedTestCase {

    /**
     * Tests empty graph
     */
    public void testEmptyGraph() { 
        DirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        Iterator<String> iter = new RandomWalkIterator<>(graph);
        assertFalse(iter.hasNext());
    }
    
    /**
     * Tests single node graph
     */
    public void testSingleNode() {
        DirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);        
        graph.addVertex("123");
        Iterator<String> iter = new RandomWalkIterator<>(graph);
        assertTrue(iter.hasNext());
        assertEquals("123", iter.next());
        assertFalse(iter.hasNext());
    }
    
    /**
     * Tests iterator does not have more elements after reaching sink vertex.
     */
    public void testSink() {
        DirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);    
        int graphSize = 10;
        LinearGraphGenerator<String, DefaultEdge> graphGenerator = new LinearGraphGenerator<>(graphSize);
        graphGenerator.generateGraph(graph, new VertexFactory<String>() {
            private int index = 1;
            @Override
            public String createVertex() {
                return String.valueOf(index++);
            }
        }, null);
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
    public void testExhausted() {
        UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        RingGraphGenerator<String, DefaultEdge> graphGenerator = new RingGraphGenerator<>(10);
        graphGenerator.generateGraph(graph, new VertexFactory<String>() {
            private int index = 1;
            @Override
            public String createVertex() {
                return String.valueOf(index++);
            }
        }, null);
        
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
    public void testDeterministic() {
        DirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);    
        int ringSize = 5;
        RingGraphGenerator<String, DefaultEdge> graphGenerator = new RingGraphGenerator<>(ringSize);
        graphGenerator.generateGraph(graph, new VertexFactory<String>() {
            private int index = 0;
            @Override
            public String createVertex() {
                return String.valueOf(index++);
            }
        }, null);
        Iterator<String> iter = new RandomWalkIterator<>(graph, "0", false, 20);
        int step = 0;
        while(iter.hasNext()) {
            step++;
            assertEquals(String.valueOf(step % ringSize), iter.next());
        }
    }
    
}
