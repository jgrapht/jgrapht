/*
 * (C) Copyright 2018-2018, by Assaf Mizrachi and Contributors.
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
package org.jgrapht.alg.scoring;

import static org.junit.Assert.*;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

/**
 * 
 * Tests for the {@link ClusteringCoefficient} class.
 * 
 * @author Assaf Mizrachi
 */
public class ClusteringCoefficientTest
{

    @Test(expected = NullPointerException.class)
    public void testNullGraph()
    {
        Graph<Integer, DefaultEdge> g = null;
        VertexScoringAlgorithm<Integer, Double> alg = new ClusteringCoefficient<>(g);
        alg.getScores();
    }

    @Test
    public void testEmptyGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        VertexScoringAlgorithm<Integer, Double> alg = new ClusteringCoefficient<>(g);
        Map<Integer, Double> scores = alg.getScores();
        assertTrue(scores.isEmpty());
    }
    
    @Test
    public void testSingletonGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(0);
        VertexScoringAlgorithm<Integer, Double> alg = new ClusteringCoefficient<>(g);
        Map<Integer, Double> scores = alg.getScores();
        assertEquals(0.0, scores.get(0), 0.0);
    }
    
    @Test
    public void testK2Graph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        g.addEdge(0, 1);
        VertexScoringAlgorithm<Integer, Double> alg = new ClusteringCoefficient<>(g);
        Map<Integer, Double> scores = alg.getScores();
        assertEquals(0.0, scores.get(0), 0.0);
        assertEquals(0.0, scores.get(1), 0.0);
    }
    
    @Test
    public void testStar()
    {
        int order = 5;
        
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> generator = new StarGraphGenerator<>(order);
        Map<String, Integer> resultMap = new HashMap<>();
        generator.generateGraph(g, resultMap);
        VertexScoringAlgorithm<Integer, Double> alg = new ClusteringCoefficient<>(g);
        
        Map<Integer, Double> scores = alg.getScores();

        for (Integer v : scores.keySet()) {
            assertEquals(0.0, scores.get(v), 0.0);
        }

    }
    
    @Test
    public void testLinear()
    {
        int order = 5;
        
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> generator = new LinearGraphGenerator<>(order);
        Map<String, Integer> resultMap = new HashMap<>();
        generator.generateGraph(g, resultMap);
        VertexScoringAlgorithm<Integer, Double> alg = new ClusteringCoefficient<>(g);
        
        Map<Integer, Double> scores = alg.getScores();

        for (Integer v : scores.keySet()) {
            assertEquals(0.0, scores.get(v), 0.0);
        }
    }
    
    @Test
    public void testRing()
    {
        int order = 5;
        
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> generator = new RingGraphGenerator<>(order);
        Map<String, Integer> resultMap = new HashMap<>();
        generator.generateGraph(g, resultMap);
        VertexScoringAlgorithm<Integer, Double> alg = new ClusteringCoefficient<>(g);
        
        Map<Integer, Double> scores = alg.getScores();

        for (Integer v : scores.keySet()) {
            assertEquals(0.0, scores.get(v), 0.0);
        }
    }
    
    @Test
    public void testClique()
    {
        int order = 5;
        
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> generator = new CompleteGraphGenerator<>(order);
        Map<String, Integer> resultMap = new HashMap<>();
        generator.generateGraph(g, resultMap);
        VertexScoringAlgorithm<Integer, Double> alg = new ClusteringCoefficient<>(g);
        
        Map<Integer, Double> scores = alg.getScores();

        for (Integer v : scores.keySet()) {
            assertEquals(1.0, scores.get(v), 0.0);
        }
    }
    
    @Test
    public void testUndirected1()
    {
        Graph<Integer, DefaultEdge> g = createUndirected();
        VertexScoringAlgorithm<Integer, Double> alg = new ClusteringCoefficient<>(g);
        
        Map<Integer, Double> scores = alg.getScores();
        
        assertEquals(0.333, scores.get(1), 0.001);
        assertEquals(0.0, scores.get(2), 0.0);
        assertEquals(1.0, scores.get(3), 0.0);
        assertEquals(1.0, scores.get(4), 0.0);
    }
    
    @Test
    public void testMultipleEdgesDoNotCount()
    {
        Graph<Integer, DefaultEdge> g = createUndirected();
        g.addEdge(1, 2);
        g.addEdge(1, 4);
        VertexScoringAlgorithm<Integer, Double> alg = new ClusteringCoefficient<>(g);
        
        Map<Integer, Double> scores = alg.getScores();
        
        assertEquals(0.333, scores.get(1), 0.001);
        assertEquals(0.0, scores.get(2), 0.0);
        assertEquals(1.0, scores.get(3), 0.0);
        assertEquals(1.0, scores.get(4), 0.0);
    }
    
    @Test
    public void testSelfLoopDoNotCount()
    {
        Graph<Integer, DefaultEdge> g = createUndirected();
        g.addEdge(1, 1);
        g.addEdge(2, 2);
        VertexScoringAlgorithm<Integer, Double> alg = new ClusteringCoefficient<>(g);
        
        Map<Integer, Double> scores = alg.getScores();
        
        assertEquals(0.333, scores.get(1), 0.001);
        assertEquals(0.0, scores.get(2), 0.0);
        assertEquals(1.0, scores.get(3), 0.0);
        assertEquals(1.0, scores.get(4), 0.0);
    }
    
    @Test
    public void testUndirected2()
    {
        Graph<Integer, DefaultEdge> g = createUndirected2();
        
        VertexScoringAlgorithm<Integer, Double> alg = new ClusteringCoefficient<>(g);
        
        Map<Integer, Double> scores = alg.getScores();
        
        assertEquals(1.0, scores.get(0), 0.0);
        assertEquals(0.333, scores.get(1), 0.001);
        assertEquals(0.333, scores.get(2), 0.001);
        assertEquals(0.666, scores.get(3), 0.001);
        assertEquals(0.5, scores.get(4), 0.0);
        assertEquals(0.4, scores.get(5), 0.0);
        assertEquals(1.0, scores.get(6), 0.0);
        assertEquals(1.0, scores.get(7), 0.0);
    }
    
    @Test
    public void testDirected()
    {
        Graph<Integer, DefaultEdge> g = createDirected();
        
        VertexScoringAlgorithm<Integer, Double> alg = new ClusteringCoefficient<>(g);
        
        Map<Integer, Double> scores = alg.getScores();
        
        assertEquals(0.666, scores.get(1), 0.001);
        assertEquals(0.0, scores.get(2), 0.0);
        assertEquals(0.5, scores.get(3), 0.0);
        assertEquals(0.5, scores.get(4), 0.0);
    }
    
    @Test
    public void testDirected2()
    {
        Graph<Integer, DefaultEdge> g = createDirected2();
        
        VertexScoringAlgorithm<Integer, Double> alg = new ClusteringCoefficient<>(g);
        
        Map<Integer, Double> scores = alg.getScores();
        assertEquals(0.916, scores.get(1), 0.001);
        assertEquals(0.571, scores.get(2), 0.001);
        assertEquals(0.567, scores.get(3), 0.001);
        assertEquals(0.916, scores.get(4), 0.001);
        assertEquals(0.518, scores.get(5), 0.001);
        assertEquals(0.333, scores.get(6), 0.001);
        assertEquals(1.000, scores.get(7), 0.001);
        assertEquals(0.800, scores.get(8), 0.001);
        assertEquals(1.000, scores.get(9), 0.001);
        assertEquals(0.800, scores.get(10), 0.001);
    }
    
    private Graph<Integer, DefaultEdge> createUndirected()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> generator = new CompleteGraphGenerator<>(4);
        Map<String, Integer> resultMap = new HashMap<>();
        generator.generateGraph(g, resultMap);
        
        g.removeEdge(2, 3);
        g.removeEdge(2, 4);
        
        return g;
    }
    
    private Graph<Integer, DefaultEdge> createUndirected2()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(1, 2);
        g.addEdge(1, 5);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(3, 4);
        g.addEdge(3, 5);
        g.addEdge(4, 5);
        g.addEdge(4, 6);
        g.addEdge(4, 7);
        g.addEdge(5, 6);
        g.addEdge(5, 7);
        g.addEdge(6, 7);

        return g;
    }
    
    private Graph<Integer, DefaultEdge> createDirected()
    {
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 4);
        g.addEdge(3, 2);
        g.addEdge(3, 4);
        g.addEdge(4, 1);
        g.addEdge(4, 2);
        g.addEdge(4, 3);

        return g;
    }
    
    private Graph<Integer, DefaultEdge> createDirected2()
    {
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);
        g.addVertex(9);
        g.addVertex(10);
        
        g.addEdge(1, 2);
        g.addEdge(1, 5);
        g.addEdge(1, 7);
        g.addEdge(1, 9);
        
        g.addEdge(2, 1);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(2, 5);
        g.addEdge(2, 7);
        g.addEdge(2, 8);
        g.addEdge(2, 9);
        
        g.addEdge(3, 2);
        g.addEdge(3, 4);
        g.addEdge(3, 5);
        g.addEdge(3, 6);
        g.addEdge(3, 7);
        g.addEdge(3, 10);
        
        g.addEdge(4, 1);
        g.addEdge(4, 2);
        g.addEdge(4, 5);
        g.addEdge(4, 7);
        
        g.addEdge(5, 1);
        g.addEdge(5, 2);
        g.addEdge(5, 3);
        g.addEdge(5, 4);
        g.addEdge(5, 7);
        g.addEdge(5, 8);
        g.addEdge(5, 9);
        g.addEdge(5, 10);
        
        g.addEdge(6, 3);
        g.addEdge(6, 7);
        g.addEdge(6, 9);
        
        g.addEdge(7, 2);
        g.addEdge(7, 4);
        g.addEdge(7, 5);
        
        g.addEdge(8, 1);
        g.addEdge(8, 2);
        g.addEdge(8, 4);
        g.addEdge(8, 5);
        g.addEdge(8, 7);
        g.addEdge(8, 9);
        
        g.addEdge(9, 2);
        g.addEdge(9, 5);
        g.addEdge(9, 7);
        
        g.addEdge(10, 1);
        g.addEdge(10, 2);
        g.addEdge(10, 3);
        g.addEdge(10, 5);
        g.addEdge(10, 7);

        return g;
    }
    
}
