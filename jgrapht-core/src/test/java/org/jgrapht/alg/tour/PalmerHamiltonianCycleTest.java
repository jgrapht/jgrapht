/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.alg.tour;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.GraphTests;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.jgrapht.alg.tour.TwoApproxMetricTSPTest.assertHamiltonian;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PalmerHamiltonianCycleTest {

    /**
     * Small graph of 4 nodes.
     */
    @Test
    public void testSmallGraph(){
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");

        graph.addEdge("A", "B");
        graph.addEdge("A", "C");

        graph.addEdge("B", "D");
        graph.addEdge("C", "D");

        GraphPath<String, DefaultEdge> tour = new PalmerHamiltonianCycle<String, DefaultEdge>().getTour(graph);

        assertNotNull(tour);
        assertHamiltonian(graph, tour);
    }

    /**
     * Test that contains a simple cycle of 10 nodes.
     * The graph has a Hamiltonian cycle but it doesn't meet Ore's condition.
     */
    @Test (expected = IllegalArgumentException.class)
    public void testLineGraph(){
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 0; i < 10; i++){
            graph.addVertex(i);
        }

        for (int i = 0; i < 10; i++){
            graph.addEdge(i, (i + 1) % 10);
        }

        GraphPath<Integer, DefaultEdge> tour = new PalmerHamiltonianCycle<Integer, DefaultEdge>().getTour(graph);

        assertNotNull(tour);
        assertHamiltonian(graph, tour);
    }

    /**
     * Test with 500 randomly generated graphs.
     * Method of generation: randomly add edges while the graph doesn't have Ore's property
     */
    @Test
    public void testRandomGraphs(){
        Random random = new Random(0xDEAD);

        final int NUM_TESTS = 500;
        for (int test = 0; test < NUM_TESTS; test++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            final int n = 3 + random.nextInt(150);

            for (int i = 0; i < n; i++) {
                graph.addVertex(i);
            }

            List<Integer> vertexList = new ArrayList<>(graph.vertexSet());

            while (!GraphTests.hasOreProperty(graph)){
                Collections.shuffle(vertexList, random);

                search:
                    for (int i = 0; i < vertexList.size(); i++) {
                        for (int j = i + 1; j < vertexList.size(); j++) {
                            int u = vertexList.get(i);
                            int v = vertexList.get(j);

                            if (!graph.containsEdge(u, v) && graph.degreeOf(u) + graph.degreeOf(v) < n){
                                graph.addEdge(u, v);
                                break search;
                            }
                        }
                    }
            }

            GraphPath<Integer, DefaultEdge> tour = new PalmerHamiltonianCycle<Integer, DefaultEdge>().getTour(graph);

            assertNotNull(tour);
            assertHamiltonian(graph, tour);
        }
    }

    /**
     * Test with 500 randomly generated graphs.
     * Method of generation: make sure that each node has (n+1)/2 neighbours
     */
    @Test
    public void testRandomGraphs2(){
        Random random = new Random(0xBEEF);

        final int NUM_TESTS = 500;
        for (int test = 0; test < NUM_TESTS; test++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            final int n = 3 + random.nextInt(150);

            for (int i = 0; i < n; i++) {
                graph.addVertex(i);
            }

            List<Integer> vertexList = new ArrayList<>(graph.vertexSet());
            boolean changed;

            do{
                changed = false;
                Collections.shuffle(vertexList, random);

                search:
                    for (int v: vertexList){
                        if (graph.degreeOf(v) < (n + 1) / 2) {
                            for (int u: vertexList){
                                if (u != v && !graph.containsEdge(u, v)){
                                    graph.addEdge(u, v);
                                    changed = true;
                                    break search;
                                }
                            }
                        }
                    }

            } while (changed);

            GraphPath<Integer, DefaultEdge> tour = new PalmerHamiltonianCycle<Integer, DefaultEdge>().getTour(graph);

            assertNotNull(tour);
            assertHamiltonian(graph, tour);
        }
    }

    private static Graph<Integer, DefaultEdge> bigGraph = new SimpleGraph<>(DefaultEdge.class);

    @BeforeClass
    public static void generateBigGraph(){
        Random random = new Random(0xC0FFEE);
        final int n = 1000;

        for (int i = 0; i < n; i++) {
            bigGraph.addVertex(i);
        }

        List<Integer> vertexList = new ArrayList<>(bigGraph.vertexSet());

        while (!GraphTests.hasOreProperty(bigGraph)){
            Collections.shuffle(vertexList, random);

            search:
            for (int i = 0; i < vertexList.size(); i++) {
                for (int j = i + 1; j < vertexList.size(); j++) {
                    int u = vertexList.get(i);
                    int v = vertexList.get(j);

                    if (!bigGraph.containsEdge(u, v) && bigGraph.degreeOf(u) + bigGraph.degreeOf(v) < n){
                        bigGraph.addEdge(u, v);
                        break search;
                    }
                }
            }
        }

        assertTrue(GraphTests.hasOreProperty(bigGraph));
    }

    @Test
    public void testBigGraph() {
        GraphPath<Integer, DefaultEdge> tour = new PalmerHamiltonianCycle<Integer, DefaultEdge>().getTour(bigGraph);

        assertNotNull(tour);
        assertHamiltonian(bigGraph, tour);
    }
}