/*
 * (C) Copyright 2024-2024, by Louis Depiesse, Auguste Célérier and Contributors.
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

package org.jgrapht.alg.median;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.jgrapht.Graphs;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.junit.jupiter.api.Test;

public class MedianGraphTest {
	
	  @Test
	    /**
	     * Tests the `isMedian` method with median and non-median graphs.
	     * 
	     * Test Cases:
	     * 1. Median Graph: 
	     *    1 ----- 2
	     *    |       |
	     *    |       |
	     *    3 ----- 4
	     *    |       |
	     *    |       |
	     *    5 ----- 6
	     *    
	     * 2. Non-median graph (K4 - complete graph with 4 vertices).
	     * Representation:
	     *    1 ----- 2
	     *    | \   / |
	     *    |  \ /  |
	     *    |  / \  |
	     *    | /   \ |
	     *    3 ----- 4
	     */
	    public void testIsMedian() {
	    	
	    	// Test Case 1: Median Graph
	        int[][] edges1 = { { 1, 2 }, { 2, 4 }, { 4, 6 }, { 6, 5 }, { 5, 3 }, { 3, 1 }, { 3, 4 } };
	            Graph<Integer, DefaultEdge> graph1 = getGraph(edges1);
	        // Check that the graph is a median graph
	        assertTrue(MedianGraph.isMedian(graph1), "Expected the graph to be a median graph");
	        
	        // Test Case 2: Non median Graph
	        int[][] edges2 = { { 1, 2 }, { 2, 4 }, { 4, 3 }, { 3, 1 }, { 1, 4 }, { 2, 3 } };
	        	Graph<Integer, DefaultEdge> graph2 = getGraph(edges2);
	        // Check that the graph is NOT a median graph
	        assertFalse(MedianGraph.isMedian(graph2), "Expected the graph NOT to be a median graph");

	        
	    }
	  
	  /**
	     * Creates a graph from the list of its edges
	     *
	     * @param edges the edge list of a graph
	     * @return a graph specified by the {@code edges}
	     */
	    private Graph<Integer, DefaultEdge> getGraph(int[][] edges)
	    {
	        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
	        for (int[] edge : edges) {
	            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);
	        }
	        return graph;
	    }
	  
}
