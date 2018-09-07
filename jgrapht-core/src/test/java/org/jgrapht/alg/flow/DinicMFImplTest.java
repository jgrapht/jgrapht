/*
 * (C) Copyright 2018-2018, by Kirill Vishnyakov and Contributors.
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
package org.jgrapht.alg.flow;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;

import static org.junit.Assert.*;

public class DinicMFImplTest
    extends
    MaximumFlowAlgorithmTest
{

    private DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g;

    private MaximumFlowAlgorithm<String, DefaultWeightedEdge> dinic;

    private DefaultWeightedEdge edge;

    private final String v1 = "v1";

    private final String v2 = "v2";

    private final String v3 = "v3";

    @Override
    MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> createSolver(
        Graph<Integer, DefaultWeightedEdge> network)
    {
        return new DinicMFImpl<>(network);
    }

    @Before
    public void init()
    {
        g = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }

    @Test
    public void simpleTest1()
    {
        g.addVertex(v1);
        g.addVertex(v2);
        edge = g.addEdge(v1, v2);
        g.setEdgeWeight(edge, 100.0);
        dinic = new DinicMFImpl<>(g);
        double flow = dinic.calculateMaximumFlow(v1, v2);
        assertEquals(100.0, flow, 0);
    }

    @Test
    public void simpleTest2()
    {
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        edge = g.addEdge(v1, v2);
        g.setEdgeWeight(edge, 100.0);
        edge = g.addEdge(v2, v3);
        g.setEdgeWeight(edge, 50.0);
        dinic = new DinicMFImpl<>(g);
        double flow = dinic.calculateMaximumFlow(v1, v3);
        assertEquals(50.0, flow, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionTest1()
    {
        g.addVertex(v1);
        dinic = new DinicMFImpl<>(g);
        double flow = dinic.calculateMaximumFlow(v1, v1);
        System.out.println(flow);
    }

    @Test
    public void disconnectedTest()
    {
        g.addVertex(v1);
        g.addVertex(v2);
        dinic = new DinicMFImpl<>(g);
        double flow = dinic.calculateMaximumFlow(v1, v2);
        assertEquals(0.0, flow, 0);
    }

    @Test
    public void simpleTest3()
    {
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        String v4 = "v4";
        g.addVertex(v4);
        edge = g.addEdge(v1, v2);
        g.setEdgeWeight(edge, 2.0);
        edge = g.addEdge(v2, v3);
        g.setEdgeWeight(edge, 2.0);
        edge = g.addEdge(v3, v4);
        g.setEdgeWeight(edge, 2.0);
        edge = g.addEdge(v2, v4);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v1, v3);
        g.setEdgeWeight(edge, 1.0);
        dinic = new DinicMFImpl<>(g);
        double flow = dinic.calculateMaximumFlow(v1, v2);
        assertEquals(2.0, flow, 0);
    }
}
