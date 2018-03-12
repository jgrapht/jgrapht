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
package org.jgrapht.alg.shortestpath;

import static org.junit.Assert.*;

import org.junit.*;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;

/**
 * 
 * Tests for the {@link BhandariKDisjointShortestPaths} class.
 * 
 * @author Assaf Mizrachi
 */
public class BhandariKDisjointShortestPathsTest {

        
    /**
     * Tests single path
     * 
     * Edges expected in path
     * ---------------
     * {@literal 1 --> 2}
     */
    @Test
    public void testSinglePath() {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);        
        graph.addVertex(1);
        graph.addVertex(2);
        DefaultWeightedEdge edge = graph.addEdge(1, 2);
        graph.setEdgeWeight(edge, 8);
        BhandariKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new BhandariKDisjointShortestPaths<>(graph, 5);
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 2);
        assertEquals(1, pathList.size());
        assertEquals(1, pathList.get(0).getLength());
        assertTrue(pathList.get(0).getEdgeList().contains(edge));
        assertEquals(new Integer(2), pathList.get(0).getEndVertex());
//        assertEquals(graph, pathList.get(0).getGraph());
        assertEquals(new Integer(1), pathList.get(0).getStartVertex());
        assertEquals(2, pathList.get(0).getVertexList().size());
        assertTrue(pathList.get(0).getVertexList().contains(1));
        assertTrue(pathList.get(0).getVertexList().contains(2));
        assertEquals(pathList.get(0).getWeight(), 8.0, 0.0);
    }
    
    /**
     * Tests two disjoint paths traversing common vertex.
     * 
     * Expected path 1
     * ---------------
     * {@literal 1 --> 2 --> 3 --> 4 --> 5}
     * 
     * Expected path 2
     * ---------------
     * {@literal 1 --> 7 --> 3 --> 6 --> 5}
     * 
     */
    @Test
    public void testTwoDisjointPathsJointNode() {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);        
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);
        graph.addVertex(7);
        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        DefaultWeightedEdge e17 = graph.addEdge(1, 7);
        DefaultWeightedEdge e23 = graph.addEdge(2, 3);
        DefaultWeightedEdge e73 = graph.addEdge(7, 3);
        DefaultWeightedEdge e34 = graph.addEdge(3, 4);
        DefaultWeightedEdge e36 = graph.addEdge(3, 6);
        DefaultWeightedEdge e45 = graph.addEdge(4, 5);
        DefaultWeightedEdge e65 = graph.addEdge(6, 5);
        
        BhandariKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new BhandariKDisjointShortestPaths<>(graph, 2);
        
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 5);
        
        assertEquals(2, pathList.size());
        
        assertEquals(4, pathList.get(0).getLength());
        assertEquals(4, pathList.get(1).getLength());
        
        assertEquals(4.0, pathList.get(0).getWeight(), 0.0);
        assertEquals(4.0, pathList.get(1).getWeight(), 0.0);
        
        assertTrue(pathList.get(0).getEdgeList().contains(e12));
        assertTrue(pathList.get(0).getEdgeList().contains(e23));
        assertTrue(pathList.get(0).getEdgeList().contains(e34));
        assertTrue(pathList.get(0).getEdgeList().contains(e45));
        
        assertTrue(pathList.get(1).getEdgeList().contains(e17));
        assertTrue(pathList.get(1).getEdgeList().contains(e73));
        assertTrue(pathList.get(1).getEdgeList().contains(e36));
        assertTrue(pathList.get(1).getEdgeList().contains(e65));
        
                
    }
    
    /**
     * Tests two disjoint paths from 1 to 3
     * 
     * Edges expected in path 1
     * ---------------
     * {@literal 1 --> 3}
     * 
     * Edges expected in path 2
     * ---------------
     * {@literal 1 --> 2}
     * {@literal 2 --> 3}
     * 
     */
    @Test
    public void testTwoDisjointPaths() {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);        
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        DefaultWeightedEdge e23 = graph.addEdge(2, 3);
        DefaultWeightedEdge e13 = graph.addEdge(1, 3);
        BhandariKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new BhandariKDisjointShortestPaths<>(graph, 5);
        
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 3);
        
        assertEquals(2, pathList.size());
        
        assertEquals(1, pathList.get(0).getLength());
        assertEquals(2, pathList.get(1).getLength());
        
        assertEquals(1.0, pathList.get(0).getWeight(), 0.0);
        assertEquals(2.0, pathList.get(1).getWeight(), 0.0);
        
        assertTrue(pathList.get(0).getEdgeList().contains(e13));
        
        assertTrue(pathList.get(1).getEdgeList().contains(e12));
        assertTrue(pathList.get(1).getEdgeList().contains(e23));
        
                
    }
    
    /**
     * Tests two joint paths from 1 to 4, merge paths
     * is not required.
     * 
     * Edges expected in path 1
     * ---------------
     * {@literal 1 --> 2}, w=1
     * {@literal 2 --> 6}, w=1
     * {@literal 6 --> 4}, w=1
     * 
     * Edges expected in path 2
     * ---------------
     * {@literal 1 --> 5}, w=2
     * {@literal 5 --> 3}, w=2
     * {@literal 3 --> 4}, w=2
     * 
     * Edges expected in no path 
     * ---------------
     * {@literal 2 --> 3}, w=3
     * 
     */
    @Test
    public void testTwoDisjointPathsNoNeedToMerge() {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);        
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);
        
        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        //this edge should not be used
        DefaultWeightedEdge e23 = graph.addEdge(2, 3);
        DefaultWeightedEdge e34 = graph.addEdge(3, 4);
        DefaultWeightedEdge e15 = graph.addEdge(1, 5);
        DefaultWeightedEdge e53 = graph.addEdge(5, 3);
        DefaultWeightedEdge e26 = graph.addEdge(2, 6);
        DefaultWeightedEdge e64 = graph.addEdge(6, 4);
        
        graph.setEdgeWeight(e12, 1);
        graph.setEdgeWeight(e23, 3);
        graph.setEdgeWeight(e34, 2);
        graph.setEdgeWeight(e15, 2);
        graph.setEdgeWeight(e53, 2);
        graph.setEdgeWeight(e26, 1);
        graph.setEdgeWeight(e64, 1);
        
        BhandariKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new BhandariKDisjointShortestPaths<>(graph, 5);
        
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 4);
        assertEquals(2, pathList.size());
        assertEquals(3, pathList.get(0).getLength());
        assertEquals(3, pathList.get(1).getLength());
        assertEquals(3.0, pathList.get(0).getWeight(), 0.0);
        assertEquals(6.0, pathList.get(1).getWeight(), 0.0);
        
        assertTrue(pathList.get(0).getEdgeList().contains(e12));
        assertTrue(pathList.get(0).getEdgeList().contains(e26));
        assertTrue(pathList.get(0).getEdgeList().contains(e64));
        
        assertTrue(pathList.get(1).getEdgeList().contains(e15));
        assertTrue(pathList.get(1).getEdgeList().contains(e53));
        assertTrue(pathList.get(1).getEdgeList().contains(e34));
    }
    
    /**
     * Tests two joint paths from 1 to 4, merge paths
     * is required.
     * 
     * Edges expected in path 1
     * ---------------
     * {@literal 1 --> 2}, w=1
     * {@literal 2 --> 6}, w=2
     * {@literal 6 --> 4}, w=2
     * 
     * Edges expected in path 2
     * ---------------
     * {@literal 1 --> 5}, w=1
     * {@literal 5 --> 3}, w=3
     * {@literal 3 --> 4}, w=3
     * 
     * Edges expected in no path 
     * ---------------
     * {@literal 2 --> 3}, w=1
     * 
     */
    @Test
    public void testTwoDisjointPathsNeedToMerge() {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);        
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);
        
        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        //this edge should not be used
        DefaultWeightedEdge e23 = graph.addEdge(2, 3);
        DefaultWeightedEdge e34 = graph.addEdge(3, 4);
        DefaultWeightedEdge e15 = graph.addEdge(1, 5);
        DefaultWeightedEdge e53 = graph.addEdge(5, 3);
        DefaultWeightedEdge e26 = graph.addEdge(2, 6);
        DefaultWeightedEdge e64 = graph.addEdge(6, 4);
        
        graph.setEdgeWeight(e12, 1);
        graph.setEdgeWeight(e23, 1);
        graph.setEdgeWeight(e34, 1);
        graph.setEdgeWeight(e15, 3);
        graph.setEdgeWeight(e53, 3);
        graph.setEdgeWeight(e26, 2);
        graph.setEdgeWeight(e64, 2);
        
        BhandariKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new BhandariKDisjointShortestPaths<>(graph, 5);
        
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 4);
        assertEquals(2, pathList.size());
        assertEquals(3, pathList.get(0).getLength());
        assertEquals(3, pathList.get(1).getLength());
        assertEquals(5.0, pathList.get(0).getWeight(), 0.0);
        assertEquals(7.0, pathList.get(1).getWeight(), 0.0);
        
        assertTrue(pathList.get(0).getEdgeList().contains(e12));
        assertTrue(pathList.get(0).getEdgeList().contains(e26));
        assertTrue(pathList.get(0).getEdgeList().contains(e64));
        
        assertTrue(pathList.get(1).getEdgeList().contains(e15));
        assertTrue(pathList.get(1).getEdgeList().contains(e53));
        assertTrue(pathList.get(1).getEdgeList().contains(e34));
    }
    
    /**
     * Tests two joint paths from 1 to 4, negative
     * edges exist in path.
     * 
     * Edges expected in path 1
     * ---------------
     * {@literal 1 --> 2}, w=-1
     * {@literal 2 --> 6}, w=-3
     * {@literal 6 --> 4}, w= 3
     * 
     * Edges expected in path 2
     * ---------------
     * {@literal 1 --> 5}, w=-2
     * {@literal 5 --> 3}, w= 2
     * {@literal 3 --> 4}, w=-1
     * 
     * Edges expected in no path 
     * ---------------
     * {@literal 2 --> 3}, w=-1
     * 
     */
    @Test
    public void testTwoDisjointPathsNegative() {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);        
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);
        
        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        //this edge should not be used
        DefaultWeightedEdge e23 = graph.addEdge(2, 3);
        DefaultWeightedEdge e34 = graph.addEdge(3, 4);
        DefaultWeightedEdge e15 = graph.addEdge(1, 5);
        DefaultWeightedEdge e53 = graph.addEdge(5, 3);
        DefaultWeightedEdge e26 = graph.addEdge(2, 6);
        DefaultWeightedEdge e64 = graph.addEdge(6, 4);
        
        graph.setEdgeWeight(e12, -1);
        graph.setEdgeWeight(e23, -1);
        graph.setEdgeWeight(e34, -1);
        graph.setEdgeWeight(e15, -2);
        graph.setEdgeWeight(e53,  2);
        graph.setEdgeWeight(e26, -3);
        graph.setEdgeWeight(e64,  3);
        
        BhandariKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new BhandariKDisjointShortestPaths<>(graph, 5);
        
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 4);
        assertEquals(2, pathList.size());
        assertEquals(3, pathList.get(0).getLength());
        assertEquals(3, pathList.get(1).getLength());
        assertEquals(-1.0, pathList.get(0).getWeight(), 0.0);
        assertEquals(-1.0, pathList.get(1).getWeight(), 0.0);
        
        assertTrue(pathList.get(0).getEdgeList().contains(e12));
        assertTrue(pathList.get(0).getEdgeList().contains(e26));
        assertTrue(pathList.get(0).getEdgeList().contains(e64));
        
        assertTrue(pathList.get(1).getEdgeList().contains(e15));
        assertTrue(pathList.get(1).getEdgeList().contains(e53));
        assertTrue(pathList.get(1).getEdgeList().contains(e34));
    }
    
    /**
     * Tests two joint paths from 1 to 4, reversed edges already exist
     * in graph so not added when preparing for next phase.
     * 
     * Edges expected in path 1
     * ---------------
     * {@literal 1 --> 2}, w=1
     * {@literal 2 --> 6}, w=2
     * {@literal 6 --> 4}, w=2
     * 
     * Edges expected in path 2
     * ---------------
     * {@literal 1 --> 5}, w=1
     * {@literal 5 --> 3}, w=3
     * {@literal 3 --> 4}, w=3
     * 
     * Edges expected in no path 
     * ---------------
     * {@literal 2 --> 3}, w=1
     * 
     */
    @Test
    public void testTwoDisjointPathsWithReversedEdgesExist() {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);        
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);
        
        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        //this edge should not be used
        DefaultWeightedEdge e23 = graph.addEdge(2, 3);
        DefaultWeightedEdge e34 = graph.addEdge(3, 4);
        DefaultWeightedEdge e15 = graph.addEdge(1, 5);
        DefaultWeightedEdge e53 = graph.addEdge(5, 3);
        DefaultWeightedEdge e26 = graph.addEdge(2, 6);
        DefaultWeightedEdge e64 = graph.addEdge(6, 4);
        
        DefaultWeightedEdge e21 = graph.addEdge(2, 1);
        //this edge should not be used
        DefaultWeightedEdge e32 = graph.addEdge(3, 2);
        DefaultWeightedEdge e43 = graph.addEdge(4, 3);
        DefaultWeightedEdge e51 = graph.addEdge(5, 1);
        DefaultWeightedEdge e35 = graph.addEdge(3, 5);
        DefaultWeightedEdge e62 = graph.addEdge(6, 2);
        DefaultWeightedEdge e46 = graph.addEdge(4, 6);
        
        graph.setEdgeWeight(e12, 1);
        graph.setEdgeWeight(e23, 1);
        graph.setEdgeWeight(e34, 1);
        graph.setEdgeWeight(e15, 3);
        graph.setEdgeWeight(e53, 3);
        graph.setEdgeWeight(e26, 2);
        graph.setEdgeWeight(e64, 2);
        
        graph.setEdgeWeight(e21, 1);
        graph.setEdgeWeight(e32, 1);
        graph.setEdgeWeight(e43, 1);
        graph.setEdgeWeight(e51, 3);
        graph.setEdgeWeight(e35, 3);
        graph.setEdgeWeight(e62, 2);
        graph.setEdgeWeight(e46, 2);
        
        BhandariKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new BhandariKDisjointShortestPaths<>(graph, 5);
        
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 4);
        assertEquals(2, pathList.size());
        assertEquals(3, pathList.get(0).getLength());
        assertEquals(3, pathList.get(1).getLength());
        assertEquals(5.0, pathList.get(0).getWeight(), 0.0);
        assertEquals(7.0, pathList.get(1).getWeight(), 0.0);
        
        assertTrue(pathList.get(0).getEdgeList().contains(e12));
        assertTrue(pathList.get(0).getEdgeList().contains(e26));
        assertTrue(pathList.get(0).getEdgeList().contains(e64));
        
        assertTrue(pathList.get(1).getEdgeList().contains(e15));
        assertTrue(pathList.get(1).getEdgeList().contains(e53));
        assertTrue(pathList.get(1).getEdgeList().contains(e34));
    }
    
    /**
     * Tests three joint paths from 1 to 5
     * Edges expected in path 1
     * ---------------
     * {@literal 1 --> 4}, w=4
     * {@literal 4 --> 5}, w=1     
     * 
     * Edges expected in path 2
     * ---------------
     * {@literal 1 --> 2}, w=1
     * {@literal 2 --> 5}, w=6
     * 
     * Edges expected in path 3
     * ---------------
     * {@literal 1 --> 3}, w=4
     * {@literal 3 --> 5}, w=5
     * 
     * Edges expected in no path 
     * ---------------
     * {@literal 2 --> 3}, w=1
     * {@literal 3 --> 4}, w=1
     */
    @Test
    public void testThreeDisjointPaths() {
        Graph<Integer, DefaultWeightedEdge> graph = createThreeDisjointPathsGraph();
        
        DefaultWeightedEdge e12 = graph.getEdge(1, 2);
        DefaultWeightedEdge e25 = graph.getEdge(2, 5);
        DefaultWeightedEdge e13 = graph.getEdge(1, 3);
        DefaultWeightedEdge e35 = graph.getEdge(3, 5);
        DefaultWeightedEdge e14 = graph.getEdge(1, 4);
        DefaultWeightedEdge e45 = graph.getEdge(4, 5);
        
        BhandariKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new BhandariKDisjointShortestPaths<>(graph, 5);
        
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 5);
        assertEquals(3, pathList.size());
        assertEquals(2, pathList.get(0).getLength());
        assertEquals(2, pathList.get(1).getLength());
        assertEquals(2, pathList.get(2).getLength());
        
        assertEquals(5.0, pathList.get(0).getWeight(), 0.0);
        assertEquals(7.0, pathList.get(1).getWeight(), 0.0);
        assertEquals(9.0, pathList.get(2).getWeight(), 0.0);
        
        assertTrue(pathList.get(0).getEdgeList().contains(e14));
        assertTrue(pathList.get(0).getEdgeList().contains(e45));
        
        assertTrue(pathList.get(1).getEdgeList().contains(e12));
        assertTrue(pathList.get(1).getEdgeList().contains(e25));                       
        
        assertTrue(pathList.get(2).getEdgeList().contains(e13));
        assertTrue(pathList.get(2).getEdgeList().contains(e35));
    }
    
    /**
     * Tests three joint paths from 1 to 5
     * Edges expected in path 1
     * ---------------
     * {@literal 1 --> 4}, w=4
     * {@literal 4 --> 5}, w=1     
     * 
     * Edges expected in path 2
     * ---------------
     * {@literal 1 --> 2}, w=1
     * {@literal 2 --> 5}, w=6
     * 
     * Edges expected in path 3
     * ---------------
     * {@literal 1 --> 3}, w=4
     * {@literal 3 --> 5}, w=5
     * 
     * Edges expected in no path 
     * ---------------
     * {@literal 2 --> 3}, w=1
     * {@literal 3 --> 4}, w=1
     */
    @Test
    public void testThreeDisjointPathsMultigraph() {
        Graph<Integer, DefaultWeightedEdge> graph = createThreeDisjointPathsMultiGraph();
        
        BhandariKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new BhandariKDisjointShortestPaths<>(graph, 5);
        
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 5);
        assertEquals(3, pathList.size());
        assertEquals(2, pathList.get(0).getLength());
        assertEquals(2, pathList.get(1).getLength());
        assertEquals(2, pathList.get(2).getLength());
        
        assertEquals(5.0, pathList.get(0).getWeight(), 0.0);
        assertEquals(7.0, pathList.get(1).getWeight(), 0.0);
        assertEquals(9.0, pathList.get(2).getWeight(), 0.0);
        
        assertTrue(pathList.get(0).getVertexList().equals(Arrays.asList(1, 4, 5)));        
        assertTrue(pathList.get(1).getVertexList().equals(Arrays.asList(1, 2, 5)));
        assertTrue(pathList.get(2).getVertexList().equals(Arrays.asList(1, 3, 5)));
    }
    
    private Graph<Integer, DefaultWeightedEdge> createThreeDisjointPathsMultiGraph() {
        DirectedWeightedMultigraph<Integer, DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);        
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        
        for (int i = 0; i < 3; i++) {
            DefaultWeightedEdge e12 = graph.addEdge(1, 2);
            DefaultWeightedEdge e25 = graph.addEdge(2, 5);
            DefaultWeightedEdge e13 = graph.addEdge(1, 3);
            DefaultWeightedEdge e35 = graph.addEdge(3, 5);
            DefaultWeightedEdge e14 = graph.addEdge(1, 4);
            DefaultWeightedEdge e45 = graph.addEdge(4, 5);
            DefaultWeightedEdge e23 = graph.addEdge(2, 3);
            DefaultWeightedEdge e34 = graph.addEdge(3, 4);

            DefaultWeightedEdge e21 = graph.addEdge(2, 1);
            DefaultWeightedEdge e52 = graph.addEdge(5, 2);
            DefaultWeightedEdge e31 = graph.addEdge(3, 1);
            DefaultWeightedEdge e53 = graph.addEdge(5, 3);
            DefaultWeightedEdge e41 = graph.addEdge(4, 1);
            DefaultWeightedEdge e54 = graph.addEdge(5, 4);
            DefaultWeightedEdge e32 = graph.addEdge(3, 2);
            DefaultWeightedEdge e43 = graph.addEdge(4, 3);

            graph.setEdgeWeight(e12, 1);
            graph.setEdgeWeight(e25, 6);
            graph.setEdgeWeight(e13, 4);
            graph.setEdgeWeight(e35, 5);
            graph.setEdgeWeight(e14, 4);
            graph.setEdgeWeight(e45, 1);
            graph.setEdgeWeight(e23, 1);
            graph.setEdgeWeight(e34, 1);

            graph.setEdgeWeight(e21, 1);
            graph.setEdgeWeight(e52, 6);
            graph.setEdgeWeight(e31, 4);
            graph.setEdgeWeight(e53, 5);
            graph.setEdgeWeight(e41, 4);
            graph.setEdgeWeight(e54, 1);
            graph.setEdgeWeight(e32, 1);
            graph.setEdgeWeight(e43, 1);
        }
        
        return graph;
    }    
    
    private Graph<Integer, DefaultWeightedEdge> createThreeDisjointPathsGraph() {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);        
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        
        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        DefaultWeightedEdge e25 = graph.addEdge(2, 5);
        DefaultWeightedEdge e13 = graph.addEdge(1, 3);
        DefaultWeightedEdge e35 = graph.addEdge(3, 5);
        DefaultWeightedEdge e14 = graph.addEdge(1, 4);
        DefaultWeightedEdge e45 = graph.addEdge(4, 5);
        DefaultWeightedEdge e23 = graph.addEdge(2, 3);
        DefaultWeightedEdge e34 = graph.addEdge(3, 4);
        
        graph.setEdgeWeight(e12, 1);
        graph.setEdgeWeight(e25, 6);
        graph.setEdgeWeight(e13, 4);
        graph.setEdgeWeight(e35, 5);
        graph.setEdgeWeight(e14, 4);
        graph.setEdgeWeight(e45, 1);
        graph.setEdgeWeight(e23, 1);
        graph.setEdgeWeight(e34, 1);
        
        return graph;
    }
    
    @Test
    public void testGraphIsNotChanged() {
        Graph<Integer, DefaultWeightedEdge> source = createThreeDisjointPathsGraph();
        Graph<Integer, DefaultWeightedEdge> destination = new DefaultDirectedWeightedGraph<>(source.getEdgeFactory());
        Graphs.addGraph(destination, source);
        
        new BhandariKDisjointShortestPaths<>(source, 5).getPaths(1, 5);
        
        assertEquals(destination, source);
    }
    
    /**
     * Only single disjoint path should exist on the line
     */
    @Test
    public void testLinear() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);  
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> graphGenerator = new LinearGraphGenerator<>(20);
        graphGenerator.generateGraph(graph, new IntegerVertexFactory(1), null);
        
        BhandariKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new BhandariKDisjointShortestPaths<>(graph, 2);
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 20);
        
        assertEquals(1, pathList.size());
        assertEquals(19, pathList.get(0).getLength());
        assertEquals(19.0, pathList.get(0).getWeight(), 0.0);
        
        for (int i = 1; i < 21; i++) {
            assertTrue(pathList.get(0).getVertexList().contains(i));
        }
    }
    
    /**
     * Exactly single disjoint path should exist on the ring
     */
    @Test
    public void testRing() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);  
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> graphGenerator = new RingGraphGenerator<>(20);
        graphGenerator.generateGraph(graph, new IntegerVertexFactory(1), null);
        
        BhandariKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new BhandariKDisjointShortestPaths<>(graph, 2);
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, 10);
        
        assertEquals(1, pathList.size());
        assertEquals(9, pathList.get(0).getLength());
        assertEquals(9.0, pathList.get(0).getWeight(), 0.0);
        
        for (int i = 1; i < 10; i++) {
            assertTrue(pathList.get(0).getVertexList().contains(i));
        }
    }
    
    /**
     * Exactly single disjoint path should exist on the ring
     */
    @Test
    public void testClique() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);  
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> graphGenerator = new CompleteGraphGenerator<>(20);
        graphGenerator.generateGraph(graph, new IntegerVertexFactory(1), null);
        
        BhandariKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new BhandariKDisjointShortestPaths<>(graph, 2);
        
        for (int i = 2; i < 20; i++) {
            List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(1, i);
            assertEquals(2, pathList.size());
        }
    }
    
    /**
     * Exactly single disjoint path should exist on the ring
     */
    @Test
    public void testStar() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);  
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> graphGenerator = new StarGraphGenerator<>(20);
        graphGenerator.generateGraph(graph, new IntegerVertexFactory(1), null);
        
        BhandariKDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new BhandariKDisjointShortestPaths<>(graph, 2);
        
        for (int i = 2; i < 20; i++) {
            List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(i, 1);
            assertEquals(1, pathList.size());
        }
    }    
}
