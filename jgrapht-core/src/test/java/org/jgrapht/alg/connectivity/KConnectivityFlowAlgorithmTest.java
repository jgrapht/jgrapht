/*
 * (C) Copyright 2020-2023, by Azim Barhoumi, Paul Enjalbert and Contributors.
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

package org.jgrapht.alg.connectivity;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for K-Connectivenness implemented with graph flows.
 * 
 * @author Azim Barhoumi
 * @author Paul Enjalbert
 */
public class KConnectivityFlowAlgorithmTest
{
    @Test
    public void testEdgeConnectivitySmallUndirectedGraph()
    {
        SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 1);

        KConnectivityFlowAlgorithm<Integer, DefaultEdge> algo = new KConnectivityFlowAlgorithm<>(graph);

        assertEquals(2, algo.getEdgeConnectivity(), "Edge connectivity of a triangle graph should be 2.");
    }

    @Test
    public void testVertexConnectivitySmallUndirectedGraph()
    {
        SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 1);

        KConnectivityFlowAlgorithm<Integer, DefaultEdge> algo = new KConnectivityFlowAlgorithm<>(graph);

        assertEquals(2, algo.getVertexConnectivity(), "Vertex connectivity of a triangle graph should be 2.");
    }

    @Test
    public void testEdgeConnectivityLargeUndirectedGraph()
    {
        SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        
        for (int i = 0; i < 8; i++) {
            graph.addVertex(i);
        }
        
        int[][] edges = {
                {0, 1}, {0, 2}, {0, 4},
                {1, 3}, {1, 5},
                {2, 3}, {2, 6},
                {3, 7},
                {4, 5}, {4, 6},
                {5, 7},
                {6, 7}
        };
        
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }

        KConnectivityFlowAlgorithm<Integer, DefaultEdge> algo = new KConnectivityFlowAlgorithm<>(graph);

        assertEquals(3, algo.getEdgeConnectivity(), "Edge connectivity of a large cycle graph should be 3.");
    }

    @Test
    public void testVertexConnectivityLargeUndirectedGraph()
    {
        SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        
        for (int i = 0; i < 8; i++) {
            graph.addVertex(i);
        }
        
        int[][] edges = {
                {0, 1}, {0, 2}, {0, 4},
                {1, 3}, {1, 5},
                {2, 3}, {2, 6},
                {3, 7},
                {4, 5}, {4, 6},
                {5, 7},
                {6, 7}
        };
        
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }
        KConnectivityFlowAlgorithm<Integer, DefaultEdge> algo = new KConnectivityFlowAlgorithm<>(graph);

        assertEquals(3, algo.getVertexConnectivity(), "Vertex connectivity of a this cycle graph should be 3.");
    }

    @Test
    public void testEdgeConnectivitySmallDirectedGraph()
    {
        SimpleDirectedGraph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 1);

        KConnectivityFlowAlgorithm<Integer, DefaultEdge> algo = new KConnectivityFlowAlgorithm<>(graph);

        assertEquals(1, algo.getEdgeConnectivity(), "Edge connectivity of a directed triangle graph should be 1.");
    }

    @Test
    public void testVertexConnectivitySmallDirectedGraph()
    {
        SimpleDirectedGraph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 1);

        KConnectivityFlowAlgorithm<Integer, DefaultEdge> algo = new KConnectivityFlowAlgorithm<>(graph);

        assertEquals(1, algo.getVertexConnectivity(), "Vertex connectivity of a directed triangle graph should be 1.");
    }

    @Test
    public void testEdgeConnectivityLargeDirectedGraph()
    {
        SimpleDirectedGraph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        
        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }
        
        int[][] edges = {
                {0, 1}, {0, 2},
                {1, 2}, {1, 3}, {1, 4}, {1, 5},
                {2, 3}, {2, 4}, {2, 4},
                {3, 4}, {3, 5},
                {4, 0}, {4, 1}, {4, 2}, {4, 5},
                {5, 0}, {5, 1}, {5, 2}  
         };
        
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }

        KConnectivityFlowAlgorithm<Integer, DefaultEdge> algo = new KConnectivityFlowAlgorithm<>(graph);

        assertEquals(2, algo.getEdgeConnectivity(), "Edge connectivity of a large directed cycle graph should be 1.");
    }

    @Test
    public void testVertexConnectivityLargeDirectedGraph()
    {
        SimpleDirectedGraph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        
        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }
        
        int[][] edges = {
                {0, 1}, {0, 2},
                {1, 2}, {1, 3}, {1, 4}, {1, 5},
                {2, 3}, {2, 4}, {2, 4},
                {3, 4}, {3, 5},
                {4, 0}, {4, 1}, {4, 2}, {4, 5},
                {5, 0}, {5, 1}, {5, 2}  
         };
        
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }
        
        KConnectivityFlowAlgorithm<Integer, DefaultEdge> algo = new KConnectivityFlowAlgorithm<>(graph);

        assertEquals(2, algo.getVertexConnectivity(), "Vertex connectivity of a large directed cycle graph should be 2.");
    }
}
