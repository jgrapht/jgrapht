/*
 * (C) Copyright 2017-2017, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.graph.*;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for GraphMeasurer
 *
 * @author Joris Kinable
 */
public class GraphMeasurerTest {

    private final double EPSILON=0.000000001;

    private Graph<Integer, DefaultEdge> getGraph1(){
        Graph<Integer, DefaultEdge> g=new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0,1,2,3,4,5,6));
        g.addEdge(0,1);
        g.addEdge(1,2);
        g.addEdge(1,3);
        g.addEdge(1,5);
        g.addEdge(3,4);
        g.addEdge(5,6);
        return g;
    }

    private Graph<Integer, DefaultEdge> getGraph2(){
        Graph<Integer, DefaultEdge> g=new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0,1,2,3,4,5,6));
        g.addEdge(0,1);
        g.addEdge(1,2);
        g.addEdge(1,3);
        g.addEdge(1,4);
        g.addEdge(1,5);
        g.addEdge(5,6);
        return g;
    }

    @Test
    public void testVertexEccentricityG1(){
        Graph<Integer,DefaultEdge> g1=getGraph1();
        List<ShortestPathAlgorithm<Integer, DefaultEdge>> spAlgs=Arrays.asList(new FloydWarshallShortestPaths<>(g1), new JohnsonShortestPaths<>(g1, new IntegerVertexFactory(7)));
        for(ShortestPathAlgorithm<Integer, DefaultEdge> spAlg : spAlgs) {
            GraphMeasurer<Integer, DefaultEdge> gdm=new GraphMeasurer<>(g1, spAlg);
            Map<Integer, Double> vertexEccentricity = gdm.getVertexEccentricityMap();
            assertEquals(3.0, vertexEccentricity.get(0), EPSILON);
            assertEquals(2.0, vertexEccentricity.get(1), EPSILON);
            assertEquals(3.0, vertexEccentricity.get(2), EPSILON);
            assertEquals(3.0, vertexEccentricity.get(3), EPSILON);
            assertEquals(4.0, vertexEccentricity.get(4), EPSILON);
            assertEquals(3.0, vertexEccentricity.get(5), EPSILON);
            assertEquals(4.0, vertexEccentricity.get(6), EPSILON);
        }
    }

    @Test
    public void testVertexEccentricityG2(){
        Graph<Integer,DefaultEdge> g2=getGraph2();
        List<ShortestPathAlgorithm<Integer, DefaultEdge>> spAlgs=Arrays.asList(new FloydWarshallShortestPaths<>(g2), new JohnsonShortestPaths<>(g2, new IntegerVertexFactory(7)));
        for(ShortestPathAlgorithm<Integer, DefaultEdge> spAlg : spAlgs) {
            GraphMeasurer<Integer, DefaultEdge> gdm=new GraphMeasurer<>(g2, spAlg);
            Map<Integer, Double> vertexEccentricity = gdm.getVertexEccentricityMap();
            assertEquals(3.0, vertexEccentricity.get(0), EPSILON);
            assertEquals(2.0, vertexEccentricity.get(1), EPSILON);
            assertEquals(3.0, vertexEccentricity.get(2), EPSILON);
            assertEquals(3.0, vertexEccentricity.get(3), EPSILON);
            assertEquals(3.0, vertexEccentricity.get(4), EPSILON);
            assertEquals(2.0, vertexEccentricity.get(5), EPSILON);
            assertEquals(3.0, vertexEccentricity.get(6), EPSILON);
        }
    }

    @Test
    public void testDiameterEmptyGraph(){
        Graph<Integer, DefaultEdge> g=new SimpleGraph<>(DefaultEdge.class);
        GraphMeasurer<Integer, DefaultEdge> gdm=new GraphMeasurer<>(g);
        double diameter=gdm.getDiameter();
        assertEquals(0.0, diameter, EPSILON);
    }

    @Test
    public void testDiameterDisconnectedGraph(){
        Graph<Integer, DefaultEdge> g=new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0,1));
        GraphMeasurer<Integer, DefaultEdge> gdm=new GraphMeasurer<>(g);
        double diameter=gdm.getDiameter();
        assertTrue(Double.isInfinite(diameter));
    }

    @Test
    public void testDiameterDirectedGraph1(){
        Graph<Integer, DefaultWeightedEdge> g=new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(g, 0, 1, 10);
        GraphMeasurer<Integer, DefaultWeightedEdge> gdm=new GraphMeasurer<>(g);
        double diameter=gdm.getDiameter();
        assertTrue(Double.isInfinite(diameter));
    }

    @Test
    public void testDiameterDirectedGraph2(){
        Graph<Integer, DefaultWeightedEdge> g=new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(g, 0, 1, 10);
        Graphs.addEdgeWithVertices(g, 1, 0, 12);
        GraphMeasurer<Integer, DefaultWeightedEdge> gdm=new GraphMeasurer<>(g);
        double diameter=gdm.getDiameter();
        assertEquals(12.0, diameter, EPSILON);
    }

    @Test
    public void testRadiusEmptyGraph(){
        Graph<Integer, DefaultEdge> g=new SimpleGraph<>(DefaultEdge.class);
        GraphMeasurer<Integer, DefaultEdge> gdm=new GraphMeasurer<>(g);
        double radius=gdm.getRadius();
        assertEquals(0.0, radius, EPSILON);
    }

    @Test
    public void testRadiusDisconnectedGraph(){
        Graph<Integer, DefaultEdge> g=new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0,1));
        GraphMeasurer<Integer, DefaultEdge> gdm=new GraphMeasurer<>(g);
        double radius=gdm.getRadius();
        assertTrue(Double.isInfinite(radius));
    }

    @Test
    public void testGraphCenterG1(){
        Graph<Integer,DefaultEdge> g1=getGraph1();
        GraphMeasurer<Integer, DefaultEdge> gdm=new GraphMeasurer<>(g1);
        Set<Integer> graphCenter1=gdm.getGraphCenter();
        assertEquals(new HashSet<>(Collections.singletonList(1)), graphCenter1);
    }

    @Test
    public void testGraphCenterG2(){
        Graph<Integer,DefaultEdge> g2=getGraph2();
        GraphMeasurer<Integer, DefaultEdge> gdm=new GraphMeasurer<>(g2);
        Set<Integer> graphCenter2=gdm.getGraphCenter();
        assertEquals(new HashSet<>(Arrays.asList(1,5)), graphCenter2);
    }

    @Test
    public void testGraphPeripheryG1(){
        Graph<Integer,DefaultEdge> g1=getGraph1();
        GraphMeasurer<Integer, DefaultEdge> gdm=new GraphMeasurer<>(g1);
        Set<Integer> graphPeriphery1=gdm.getGraphPeriphery();
        assertEquals(new HashSet<>(Arrays.asList(4,6)), graphPeriphery1);
    }

    @Test
    public void testGraphPeripheryG2(){
        Graph<Integer,DefaultEdge> g2=getGraph2();
        GraphMeasurer<Integer, DefaultEdge> gdm=new GraphMeasurer<>(g2);
        Set<Integer> graphPeriphery2=gdm.getGraphPeriphery();
        assertEquals(new HashSet<>(Arrays.asList(0,2,3,4,6)), graphPeriphery2);
    }

}
