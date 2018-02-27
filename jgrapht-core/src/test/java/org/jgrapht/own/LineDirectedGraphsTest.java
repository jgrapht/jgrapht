package org.jgrapht.own;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;
/* Test for line directed graph
* @author Arica Chakraborty*/

public class LineDirectedGraphsTest {
    /*Test to check whether the createListGraph method of the ListDirectedGraphs class is working for diorected graphs or not*/
    @Test
    public void isWorking() {
        SimpleDirectedGraph<Integer,DefaultEdge> graph = new SimpleDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addEdge(1,2);
        graph.addEdge(2,4);
        graph.addEdge(4,3);
        graph.addEdge(3,2);
        LineDirectedGraphs lineDirectedGraph = new LineDirectedGraphs();
        Graph<Integer,DefaultEdge> resultingGraph= lineDirectedGraph.createListGraph(graph);
        Set<Integer> setOne = resultingGraph.vertexSet();
        assertTrue(setOne.contains(12));
        assertTrue(setOne.contains(24));
        assertTrue(setOne.contains(43));
        assertTrue(setOne.contains(32));
        assertTrue(lineDirectedGraph.isConnected(12,24));
        assertTrue(lineDirectedGraph.isConnected(24,43));
        assertTrue(lineDirectedGraph.isConnected(43,32));
        assertFalse(lineDirectedGraph.isConnected(43,24));

    }
}