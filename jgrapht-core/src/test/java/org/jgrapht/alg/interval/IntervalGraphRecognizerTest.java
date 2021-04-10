/*
 * (C) Copyright 2018-2018, by Ira Justus Fesefeldt, Dennis Fischer, and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.alg.interval;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Ira Justus Fesefeldt
 * @author Dennis Fischer
 */
public class IntervalGraphRecognizerTest {

    /**
     * The graph with no vertex or edge is trivially an interval graph
     */
    @Test
    public void testEmptyGraph() {
        Graph<Integer, DefaultEdge> emptyGraph = new SimpleGraph<>(DefaultEdge.class);

        IntervalGraphRecognizer<Integer, DefaultEdge> recognizer = new IntervalGraphRecognizer<>(emptyGraph);
        assertTrue(recognizer.isIntervalGraph());
        assertEquals(0, recognizer.getVertexToIntervalMap().size());
        assertEquals(0, recognizer.getIntervalToVertexMap().size());
        assertEquals(0, recognizer.getIntervalsSortedByStartingPoint().size());
    }

    /**
     * First of five cases given by Lekkerkerker and Boland 
     * which at least one of them appears as an induced subgraph in every non-interval graph
     * 
     * A biclaw with AT is a 3-star graph (or claw) with an additional neighbor for every leave
     */
    @Test
    public void testForbiddenSubgraphBiclawWithAT() {
        GraphBuilder<Integer, DefaultEdge, ? extends SimpleGraph<Integer, DefaultEdge>> builder 
            = SimpleGraph.createBuilder(DefaultEdge.class);

        builder.addEdge(0, 1);
        builder.addEdge(0, 2);
        builder.addEdge(0, 3);

        builder.addEdge(1, 4);
        builder.addEdge(2, 5);
        builder.addEdge(3, 6);

        IntervalGraphRecognizer<Integer, DefaultEdge> recognizer = new IntervalGraphRecognizer<>(builder.build());
        assertFalse(recognizer.isIntervalGraph());
    }

    /**
     * Second of five cases given by Lekkerkerker and Boland 
     * which at least one of them appears as an induced subgraph in every non-interval graph
     */
    @Test
    public void testForbiddenSubgraphLekkerkerkerBoland() {


        new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
        GraphBuilder<Integer, DefaultEdge, ? extends SimpleGraph<Integer, DefaultEdge>> builder
            = SimpleGraph.createBuilder(DefaultEdge.class);

        builder.addEdge(0, 1);
        builder.addEdge(0, 2);
        builder.addEdge(0, 3);
        builder.addEdge(0, 4);
        builder.addEdge(0, 5);

        builder.addEdge(1, 2);
        builder.addEdge(2, 3);
        builder.addEdge(3, 4);
        builder.addEdge(4, 5);

        builder.addEdge(3, 6);

        IntervalGraphRecognizer<Integer, DefaultEdge> recognizer = new IntervalGraphRecognizer<>(builder.build());
        assertFalse(recognizer.isIntervalGraph());
    }

    public void testForbiddenSubgraphLekkerkerkerBolandFamily(int n) {
        new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
        GraphBuilder<Integer, DefaultEdge, ? extends SimpleGraph<Integer, DefaultEdge>> builder
            = SimpleGraph.createBuilder(DefaultEdge.class);

        builder.addEdge(0, 1);

        for(int i = 3; i < n; i++) {
            builder.addEdge(1, i);
            builder.addEdge(i - 1, i);
        }

        builder.addEdge(n - 1, n);

        IntervalGraphRecognizer<Integer, DefaultEdge> recognizer = new IntervalGraphRecognizer<>(builder.build());
        assertEquals("n was " + n, n <= 4, recognizer.isIntervalGraph());
    }

    /**
     * Third of five cases given by Lekkerkerker and Boland 
     * which at least one of them appears as an induced subgraph in every non-interval graph
     */
    @Test
    public void testForbiddenSubgraphLekkerkerkerBolandFamily() {
        for(int n = 4; n < 20; n++) {
            testForbiddenSubgraphLekkerkerkerBolandFamily(n);
        }
    }
    
    
    public void testForbiddenSubgraphLekkerkerkerBolandFamily2(int n) {
        GraphBuilder<Integer, DefaultEdge, ? extends SimpleGraph<Integer, DefaultEdge>> builder 
            = SimpleGraph.createBuilder(DefaultEdge.class);
        
        //asteroidal triple: 1,3,5
        builder.addEdge(0, 1);
        builder.addEdge(1, 2);
        builder.addEdge(2, 3);
        builder.addEdge(3, 4);
        builder.addEdge(4, 5);
        builder.addEdge(5, n);
        
        for (int i=5; i<n; i++) {
            builder.addEdge(i, i+1);
            builder.addEdge(i, 2);
            builder.addEdge(i, 4);
        }
        builder.addEdge(n, 2);
        builder.addEdge(n, 4);
        
        IntervalGraphRecognizer<Integer, DefaultEdge> recognizer = new IntervalGraphRecognizer<>(builder.build());
        assertFalse(recognizer.isIntervalGraph());
    }
    
    /**
     * Fourth of five cases given by Lekkerkerker and Boland 
     * which at least one of them appears as an induced subgraph in every non-interval graph
     */
    @Test
    public void testForbiddenSubgraphLekkerkerkerBolandFamily2() {
        for(int n = 6; n < 20; n++) {
            testForbiddenSubgraphLekkerkerkerBolandFamily2(n);
        }
    }

    public boolean isCnAnIntervalGraph(int n) {
        GraphBuilder<Integer, DefaultEdge, ? extends SimpleGraph<Integer, DefaultEdge>> builder 
            = SimpleGraph.createBuilder(DefaultEdge.class);

        for(int i = 0; i < n; i++) {
            builder.addEdge(i, (i + 1) % n);
        }
        
        IntervalGraphRecognizer<Integer, DefaultEdge> recognizer = new IntervalGraphRecognizer<>(builder.build());
        return recognizer.isIntervalGraph();
    }

    /**
     * Fifth of five cases given by Lekkerkerker and Boland 
     * which at least one of them appears as an induced subgraph in every non-interval graph
     *
     * A circle graph of length n
     */
    @Test
    public void testForbiddenSubgraphCn() {
        for(int n = 2; n < 20; n++) {
            assertEquals("Testing C_" + n, n < 4, isCnAnIntervalGraph(n));
        }
    }
    
    public void isCompleteAnIntervalGraph(int n) {
        CompleteGraphGenerator<Integer,DefaultEdge> cgg = 
            new CompleteGraphGenerator<Integer,DefaultEdge>(n);
        Graph<Integer,DefaultEdge> cg =
            new SimpleGraph<>(SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        cgg.generateGraph(cg);
        
        //Every complete Graph is an interval graph
        IntervalGraphRecognizer<Integer, DefaultEdge> recognizer = new IntervalGraphRecognizer<>(cg);
        assertTrue(recognizer.isIntervalGraph());
    }
    
    /*
     * Every complete graph is an interval graph
     */
    @Test
    public void completeGraphTest()
    {
        for(int i=0; i<20; i++) {
            isCompleteAnIntervalGraph(i);
        }
            
    }
    
    public void isUnconnectedAnIntervalGraph(int n) {
        GraphBuilder<Integer, DefaultEdge, ? extends SimpleGraph<Integer, DefaultEdge>> builder 
            = SimpleGraph.createBuilder(DefaultEdge.class);

        for(int i = 0; i < n; i++) {
            builder.addVertex(i);
        }
        
        // Every unconnected Graph is an interval graph
        IntervalGraphRecognizer<Integer, DefaultEdge> recognizer = new IntervalGraphRecognizer<>(builder.build());
        assertTrue(recognizer.isIntervalGraph());
    }
    
    /*
     * Every unconnected Graph is an interval graph
     */
    @Test
    public void unconnectedGraphTest()
    {
        for(int i=0; i<20; i++) {
            isUnconnectedAnIntervalGraph(i);
        }
            
    }
    
    public void isLinearAnIntervalGraph(int n) {
        GraphBuilder<Integer, DefaultEdge, ? extends SimpleGraph<Integer, DefaultEdge>> builder 
            = SimpleGraph.createBuilder(DefaultEdge.class);

        for(int i = 0; i < n-1; i++) {
                builder.addEdge(i, (i + 1));
        }
        
        //Every complete Graph is an interval graph
        IntervalGraphRecognizer<Integer, DefaultEdge> recognizer = new IntervalGraphRecognizer<>(builder.build());
        assertTrue(recognizer.isIntervalGraph());
    }
    
    /*
     * Every linear graph is an interval graph
     */
    @Test
    public void linearGraphTest()
    {
        for(int i=4; i<20; i++) {
            isLinearAnIntervalGraph(i);
        }
            
    }
}
