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
package org.jgrapht.alg.cycle;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.EulerianCycleAlgorithm;
import org.jgrapht.graph.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Joris Kinable
 */
public class ChinesePostmanTest {

    @Test(expected = IllegalArgumentException.class)
    public void testGraphNoVertices(){
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        EulerianCycleAlgorithm<Integer, DefaultEdge> alg=new ChinesePostman<>();
        alg.getEulerianCycle(g);
    }

    @Test
    public void testGraphNoEdges(){
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        EulerianCycleAlgorithm<Integer, DefaultEdge> alg=new ChinesePostman<>();
        GraphPath<Integer, DefaultEdge> path=alg.getEulerianCycle(g);
        Assert.assertTrue(path.getEdgeList().isEmpty());
    }

    @Test
    public void testSingleEdgeGraph(){
        Graph<Integer, DefaultWeightedEdge> g=new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        Graphs.addEdge(g, 0, 1, 10);

        EulerianCycleAlgorithm<Integer, DefaultWeightedEdge> alg=new ChinesePostman<>();
        GraphPath<Integer, DefaultWeightedEdge> path=alg.getEulerianCycle(g);
        this.verifyClosedPath(g, path, 20, 2);
    }

    @Test
    public void testGraphWithSelfloop(){
        Graph<Integer, DefaultWeightedEdge> g=new WeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        Graphs.addEdge(g, 0, 1, 10);
        Graphs.addEdge(g, 0, 0, 20);

        EulerianCycleAlgorithm<Integer, DefaultWeightedEdge> alg=new ChinesePostman<>();
        GraphPath<Integer, DefaultWeightedEdge> path=alg.getEulerianCycle(g);
        this.verifyClosedPath(g, path, 40, 3);
    }

    @Test
    public void testGraphWithMultipleEdges(){
        Graph<Integer, DefaultWeightedEdge> g=new WeightedMultigraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
        Graphs.addEdge(g, 1, 2, 1);
        Graphs.addEdge(g, 1, 4, 3);
        Graphs.addEdge(g, 2, 3, 20);
        Graphs.addEdge(g, 2, 3, 10);
        Graphs.addEdge(g, 3, 4, 2);

        EulerianCycleAlgorithm<Integer, DefaultWeightedEdge> alg=new ChinesePostman<>();
        GraphPath<Integer, DefaultWeightedEdge> path=alg.getEulerianCycle(g);
        this.verifyClosedPath(g, path, 42, 8);
    }

    @Test
    public void testUndirectedGraph1(){
        Graph<Character, DefaultWeightedEdge> g=new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'));
        Graphs.addEdge(g, 'A', 'B', 50);
        Graphs.addEdge(g, 'A', 'C', 50);
        Graphs.addEdge(g, 'A', 'D', 50);
        Graphs.addEdge(g, 'B', 'D', 50);
        Graphs.addEdge(g, 'B', 'E', 70);
        Graphs.addEdge(g, 'B', 'F', 50);
        Graphs.addEdge(g, 'C', 'D', 70);
        Graphs.addEdge(g, 'C', 'G', 70);
        Graphs.addEdge(g, 'C', 'H', 120);
        Graphs.addEdge(g, 'D', 'F', 60);
        Graphs.addEdge(g, 'E', 'F', 70);
        Graphs.addEdge(g, 'F', 'H', 60);
        Graphs.addEdge(g, 'G', 'H', 70);

        EulerianCycleAlgorithm<Character, DefaultWeightedEdge> alg=new ChinesePostman<>();
        GraphPath<Character, DefaultWeightedEdge> path=alg.getEulerianCycle(g);
        this.verifyClosedPath(g, path, 1000, 16);
    }

    @Test
    public void testUndirectedGraph2(){
        Graph<Character, DefaultWeightedEdge> g=new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList('A', 'B', 'C', 'D', 'E'));
        Graphs.addEdge(g, 'A', 'B', 8);
        Graphs.addEdge(g, 'A', 'C', 5);
        Graphs.addEdge(g, 'A', 'D', 6);
        Graphs.addEdge(g, 'B', 'C', 5);
        Graphs.addEdge(g, 'B', 'E', 6);
        Graphs.addEdge(g, 'C', 'D', 5);
        Graphs.addEdge(g, 'C', 'E', 5);
        Graphs.addEdge(g, 'D', 'E', 8);

        EulerianCycleAlgorithm<Character, DefaultWeightedEdge> alg=new ChinesePostman<>();
        GraphPath<Character, DefaultWeightedEdge> path=alg.getEulerianCycle(g);
        this.verifyClosedPath(g, path, 60, 10);
    }

    @Test
    public void testUndirectedGraph3(){
        Graph<Integer, DefaultWeightedEdge> g=new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7));
        Graphs.addEdge(g, 1, 2, 5);
        Graphs.addEdge(g, 1, 4, 4);
        Graphs.addEdge(g, 1, 5, 1);
        Graphs.addEdge(g, 2, 3, 3);
        Graphs.addEdge(g, 2, 5, 1);
        Graphs.addEdge(g, 2, 7, 1);
        Graphs.addEdge(g, 3, 4, 2);
        Graphs.addEdge(g, 3, 5, 3);
        Graphs.addEdge(g, 3, 6, 1);
        Graphs.addEdge(g, 3, 7, 2);
        Graphs.addEdge(g, 4, 5, 1);
        Graphs.addEdge(g, 6, 7, 3);

        EulerianCycleAlgorithm<Integer, DefaultWeightedEdge> alg=new ChinesePostman<>();
        GraphPath<Integer, DefaultWeightedEdge> path=alg.getEulerianCycle(g);
        this.verifyClosedPath(g, path, 31, 15);
    }

    @Test
    public void testUndirectedGraph4(){
        Graph<Integer, DefaultWeightedEdge> g=new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        Graphs.addEdge(g, 1, 2, 100);
        Graphs.addEdge(g, 1, 3, 150);
        Graphs.addEdge(g, 1, 4, 200);
        Graphs.addEdge(g, 2, 3, 120);
        Graphs.addEdge(g, 2, 5, 250);
        Graphs.addEdge(g, 3, 6, 200);
        Graphs.addEdge(g, 4, 5, 100);
        Graphs.addEdge(g, 4, 7, 80);
        Graphs.addEdge(g, 4, 8, 160);
        Graphs.addEdge(g, 5, 6, 100);
        Graphs.addEdge(g, 5, 7, 100);
        Graphs.addEdge(g, 6, 7, 150);
        Graphs.addEdge(g, 6, 10, 160);
        Graphs.addEdge(g, 7, 9, 100);
        Graphs.addEdge(g, 8, 9, 40);
        Graphs.addEdge(g, 9, 10, 80);

        EulerianCycleAlgorithm<Integer, DefaultWeightedEdge> alg=new ChinesePostman<>();
        GraphPath<Integer, DefaultWeightedEdge> path=alg.getEulerianCycle(g);
        this.verifyClosedPath(g, path, 2590, 20);
    }

    private <V,E> void verifyClosedPath(Graph<V,E> graph, GraphPath<V,E> path, double expectedWeight, int expectedLength){
        Assert.assertEquals(expectedLength, path.getLength());
        Assert.assertEquals(expectedLength, path.getEdgeList().size());
        Assert.assertEquals(expectedWeight, path.getWeight(), 0.00000001);
        Assert.assertEquals(expectedWeight, path.getEdgeList().stream().mapToDouble(graph::getEdgeWeight).sum(), 0.00000001);

        //all edges of the graph must be visited at least once
        Assert.assertTrue(path.getEdgeList().containsAll(graph.edgeSet()));

        Assert.assertTrue(graph.containsVertex(path.getStartVertex()));
        Assert.assertEquals(path.getStartVertex(), path.getEndVertex());

        //Verify that the path is an actual path in the graph
        Assert.assertEquals(path.getEdgeList().size()+1, path.getVertexList().size());
        List<V> vertexList=path.getVertexList();
        List<E> edgeList=path.getEdgeList();

        //Check start and end vertex
        Assert.assertEquals(vertexList.get(0), path.getStartVertex());
        Assert.assertEquals(vertexList.get(vertexList.size()-1), path.getEndVertex());

        //All vertices and edges in the path must be contained in the graph
        Assert.assertTrue(graph.vertexSet().containsAll(vertexList));
        Assert.assertTrue(graph.edgeSet().containsAll(edgeList));

        for(int i=0; i<vertexList.size()-1; i++){
            V u=vertexList.get(i);
            V v=vertexList.get(i+1);
            E edge=edgeList.get(i);

            if(graph.getType().isUndirected()){
                Assert.assertTrue(Graphs.getOppositeVertex(graph, edge, u).equals(v));
            }else{ //Directed
                Assert.assertTrue(graph.getEdgeSource(edge).equals(u));
                Assert.assertTrue(graph.getEdgeTarget(edge).equals(v));
            }
        }
    }
}
