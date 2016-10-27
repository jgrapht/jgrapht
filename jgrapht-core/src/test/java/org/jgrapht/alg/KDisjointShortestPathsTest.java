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
/* -------------------------
 * .java
 * -------------------------
 * (C) Copyright 2007-2016, by France Telecom
 *
 * Original Author: Assaf Mizrachi and Contributors.
 * Contributor(s):
 *
 * $Id$
 *
 * Changes
 * -------
 * 11-Sep-2016 : Initial revision (AM);
 *
 */
package org.jgrapht.alg;

import java.util.List;

import org.jgrapht.EnhancedTestCase;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.generate.LinearGraphGenerator;
import org.jgrapht.generate.RingGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * 
 * Tests for the {@link KDisjointShortestPaths} class.
 * 
 * @author Assaf Mizrachi
 */
public class KDisjointShortestPathsTest extends EnhancedTestCase {

	    
    /**
     * Tests single path
     * 
     * Edges expected in path
     * ---------------
     * 1 --> 2
     */
    public void testSinglePath() {
    	DefaultDirectedGraph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedGraph<>(DefaultWeightedEdge.class);        
        graph.addVertex(1);
        graph.addVertex(2);
        DefaultWeightedEdge edge = graph.addEdge(1, 2);
        graph.setEdgeWeight(edge, 8);
        KDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new KDisjointShortestPaths<>(graph, 1, 5);
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(2);
        assertEquals(1, pathList.size());
        assertEquals(1, pathList.get(0).getLength());
        assertTrue(pathList.get(0).getEdgeList().contains(edge));
        assertEquals(new Integer(2), pathList.get(0).getEndVertex());
//        assertEquals(graph, pathList.get(0).getGraph());
        assertEquals(new Integer(1), pathList.get(0).getStartVertex());
        assertEquals(2, pathList.get(0).getVertexList().size());
        assertTrue(pathList.get(0).getVertexList().contains(1));
        assertTrue(pathList.get(0).getVertexList().contains(2));
        assertEquals(pathList.get(0).getWeight(), 8.0);
    }
    
    /**
     * Tests two disjoint paths from 1 to 3
     * 
     * Edges expected in path 1
     * ---------------
     * 1 --> 3
     * 
     * Edges expected in path 2
     * ---------------
     * 1 --> 2
     * 2 --> 3
     * 
     */
    public void testTwoDisjointPaths() {
    	DefaultDirectedGraph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedGraph<>(DefaultWeightedEdge.class);        
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        DefaultWeightedEdge e23 = graph.addEdge(2, 3);
        DefaultWeightedEdge e13 = graph.addEdge(1, 3);
        KDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new KDisjointShortestPaths<>(graph, 1, 5);
        
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(3);
        
        assertEquals(2, pathList.size());
        
        assertEquals(1, pathList.get(0).getLength());
        assertEquals(2, pathList.get(1).getLength());
        
        assertEquals(1.0, pathList.get(0).getWeight());
        assertEquals(2.0, pathList.get(1).getWeight());
        
        assertTrue(pathList.get(0).getEdgeList().contains(e13));
        
        assertTrue(pathList.get(1).getEdgeList().contains(e12));
        assertTrue(pathList.get(1).getEdgeList().contains(e23));
        
                
    }
    
    /**
     * Tests two joint paths from 1 to 4
     * 
     * Edges expected in path 1
     * ---------------
     * 1 --> 2, w=1
     * 2 --> 6, w=1
     * 6 --> 4, w=1
     * 
     * Edges expected in path 2
     * ---------------
     * 1 --> 5, w=2
     * 5 --> 3, w=2
     * 3 --> 4, w=2
     * 
     * Edges expected in no path 
     * ---------------
     * 2 --> 3, w=3
     * 
     */
    public void testTwoDisjointPaths2() {
    	DefaultDirectedGraph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedGraph<>(DefaultWeightedEdge.class);        
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
        
        KDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new KDisjointShortestPaths<>(graph, 1, 5);
        
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(4);
        assertEquals(2, pathList.size());
        assertEquals(3, pathList.get(0).getLength());
        assertEquals(3, pathList.get(1).getLength());
        assertEquals(3.0, pathList.get(0).getWeight());
        assertEquals(6.0, pathList.get(1).getWeight());
        
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
     * 1 --> 4, w=4
     * 4 --> 5, w=1     
     * 
     * Edges expected in path 2
     * ---------------
     * 1 --> 2, w=1
     * 2 --> 5, w=6
     * 
     * Edges expected in path 3
     * ---------------
     * 1 --> 3, w=4
     * 3 --> 5, w=5
     * 
     * Edges expected in no path 
     * ---------------
     * 2 --> 3, w=1
     * 3 --> 4, w=1
     */
    public void testThreeDisjointPaths() {
    	DefaultDirectedGraph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedGraph<>(DefaultWeightedEdge.class);        
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
        
        KDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new KDisjointShortestPaths<>(graph, 1, 5);
        
        List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(5);
        assertEquals(3, pathList.size());
        assertEquals(2, pathList.get(0).getLength());
        assertEquals(2, pathList.get(1).getLength());
        assertEquals(2, pathList.get(2).getLength());
        
        assertEquals(5.0, pathList.get(0).getWeight());
        assertEquals(7.0, pathList.get(1).getWeight());
        assertEquals(9.0, pathList.get(2).getWeight());
        
        assertTrue(pathList.get(0).getEdgeList().contains(e14));
        assertTrue(pathList.get(0).getEdgeList().contains(e45));
        
        assertTrue(pathList.get(1).getEdgeList().contains(e12));
        assertTrue(pathList.get(1).getEdgeList().contains(e25));                       
        
        assertTrue(pathList.get(2).getEdgeList().contains(e13));
        assertTrue(pathList.get(2).getEdgeList().contains(e35));
    }
    
    /**
     * Only single disjoint path should exist on the line
     */
    public void testLinear() {
    	Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedGraph<>(DefaultWeightedEdge.class);  
    	GraphGenerator<Integer, DefaultWeightedEdge, Integer> graphGenerator = new LinearGraphGenerator<>(20);
    	graphGenerator.generateGraph(graph, new VertexFactory<Integer>() {
			
    		private int i = 1;
    		
			@Override
			public Integer createVertex() {
				return i++;
			}
		}, null);
    	
    	KDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new KDisjointShortestPaths<>(graph, 1, 2);
    	List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(20);
    	
    	assertEquals(1, pathList.size());
        assertEquals(19, pathList.get(0).getLength());
        assertEquals(19.0, pathList.get(0).getWeight());
        
        for (int i = 1; i < 21; i++) {
        	assertTrue(pathList.get(0).getVertexList().contains(i));
        }
    }
    
    /**
     * Exactly single disjoint path should exist on the ring
     */
    public void testRing() {
    	Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedGraph<>(DefaultWeightedEdge.class);  
    	GraphGenerator<Integer, DefaultWeightedEdge, Integer> graphGenerator = new RingGraphGenerator<>(20);
    	graphGenerator.generateGraph(graph, new VertexFactory<Integer>() {
			
    		private int i = 1;
    		
			@Override
			public Integer createVertex() {
				return i++;
			}
		}, null);
    	
    	KDisjointShortestPaths<Integer, DefaultWeightedEdge> alg = new KDisjointShortestPaths<>(graph, 1, 2);
    	List<GraphPath<Integer, DefaultWeightedEdge>> pathList = alg.getPaths(10);
    	
    	assertEquals(1, pathList.size());
        assertEquals(9, pathList.get(0).getLength());
        assertEquals(9.0, pathList.get(0).getWeight());
        
        for (int i = 1; i < 10; i++) {
        	assertTrue(pathList.get(0).getVertexList().contains(i));
        }
    }
}
