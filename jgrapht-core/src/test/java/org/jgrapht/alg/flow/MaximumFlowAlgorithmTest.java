/*
 * (C) Copyright 2016-2021, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.flow;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Joris Kinable
 */
public abstract class MaximumFlowAlgorithmTest
    extends
    MaximumFlowMinimumCutAlgorithmTestBase
{

    abstract MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> createSolver(
        Graph<Integer, DefaultWeightedEdge> network);

    private void runTestDirected(
        Graph<Integer, DefaultWeightedEdge> network, int[] sources, int[] sinks,
        double[] expectedResults)
    {
        assertTrue(sources.length == sinks.length);

        MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver = createSolver(network);

        // Calculate the max flow for each source/sink pair
        for (int i = 0; i < sources.length; i++) {
            verifyDirected(
                sources[i], sinks[i], expectedResults[i], network,
                solver.getMaximumFlow(sources[i], sinks[i]));
        }
    }

    static void verifyDirected(
        int source, int sink, double expectedResult, Graph<Integer, DefaultWeightedEdge> network,
        MaximumFlowAlgorithm.MaximumFlow<DefaultWeightedEdge> maxFlow)
    {
        Double flowValue = maxFlow.getValue();
        Map<DefaultWeightedEdge, Double> flow = maxFlow.getFlowMap();

        // Verify that the maximum flow value
        assertEquals(expectedResult, flowValue, EdmondsKarpMFImpl.DEFAULT_EPSILON);

        // Verify that every edge is contained in the flow map
        for (DefaultWeightedEdge e : network.edgeSet()) {
            assertTrue(flow.containsKey(e));
        }

        // Verify that the flow on every arc is between [-DEFAULT_EPSILON, edge_capacity]
        for (DefaultWeightedEdge e : flow.keySet()) {
            assertTrue(network.containsEdge(e));
            assertTrue(flow.get(e) >= -EdmondsKarpMFImpl.DEFAULT_EPSILON);
            assertTrue(
                flow.get(e) <= (network.getEdgeWeight(e) + EdmondsKarpMFImpl.DEFAULT_EPSILON));
        }

        // Verify flow preservation: amount of incoming flow must equal amount of outgoing flow
        // (exception for the source/sink vertices)
        for (Integer v : network.vertexSet()) {
            double balance = 0.0;
            for (DefaultWeightedEdge e : network.outgoingEdgesOf(v)) {
                balance -= flow.get(e);
            }
            for (DefaultWeightedEdge e : network.incomingEdgesOf(v)) {
                balance += flow.get(e);
            }
            if (v.equals(source)) {
                assertEquals(-flowValue, balance, MaximumFlowAlgorithmBase.DEFAULT_EPSILON);
            } else if (v.equals(sink)) {
                assertEquals(flowValue, balance, MaximumFlowAlgorithmBase.DEFAULT_EPSILON);
            } else {
                assertEquals(0.0, balance, MaximumFlowAlgorithmBase.DEFAULT_EPSILON);
            }
        }
    }

    private void runTestUndirected(
        Graph<Integer, DefaultWeightedEdge> graph, int source, int sink, int expectedResult)
    {
        MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver = createSolver(graph);

        verifyUndirected(graph, source, sink, expectedResult, solver);
    }

    static void verifyUndirected(
        Graph<Integer, DefaultWeightedEdge> graph, int source, int sink, int expectedResult,
        MaximumFlowAlgorithm<Integer, DefaultWeightedEdge> solver)
    {
        MaximumFlowAlgorithm.MaximumFlow<DefaultWeightedEdge> maxFlow =
            solver.getMaximumFlow(source, sink);
        Double flowValue = maxFlow.getValue();
        Map<DefaultWeightedEdge, Double> flow = maxFlow.getFlowMap();

        assertEquals(expectedResult, flowValue.intValue());

        // Verify that every edge is contained in the flow map
        for (DefaultWeightedEdge e : graph.edgeSet())
            assertTrue(flow.containsKey(e));

        // Verify that the flow on every arc is between [-DEFAULT_EPSILON, edge_capacity]
        for (DefaultWeightedEdge e : flow.keySet()) {
            assertTrue(graph.containsEdge(e));
            assertTrue(flow.get(e) >= -EdmondsKarpMFImpl.DEFAULT_EPSILON);
            assertTrue(flow.get(e) <= (graph.getEdgeWeight(e) + EdmondsKarpMFImpl.DEFAULT_EPSILON));
        }

        // Verify flow preservation: amount of incoming flow must equal amount of outgoing flow
        // (exception for the source/sink vertices)
        for (Integer u : graph.vertexSet()) {
            double balance = 0.0;
            for (DefaultWeightedEdge e : graph.edgesOf(u)) {
                Integer v = solver.getFlowDirection(e);
                if (u == v) // incoming flow
                    balance += flow.get(e);
                else // outgoing flow
                    balance -= flow.get(e);
            }

            if (u.equals(source)) {
                assertEquals(-flowValue, balance, MaximumFlowAlgorithmBase.DEFAULT_EPSILON);
            } else if (u.equals(sink)) {
                assertEquals(flowValue, balance, MaximumFlowAlgorithmBase.DEFAULT_EPSILON);
            } else {
                assertEquals(0.0, balance, MaximumFlowAlgorithmBase.DEFAULT_EPSILON);
            }
        }

    }

    @Test
    public void testDirectedN0()
    {
        runTestDirected(getDirectedN0(), new int[] { 1 }, new int[] { 4 }, new double[] { 5.0 });
    }

    @Test
    public void testDirectedN1()
    {
        runTestDirected(
            getDirectedN1(), new int[] { 1 }, new int[] { 4057218 }, new double[] { 0.0 });
    }

    @Test
    public void testDirectedN2()
    {
        runTestDirected(getDirectedN2(), new int[] { 3 }, new int[] { 6 }, new double[] { 2 });
    }

    @Test
    public void testDirectedN3()
    {
        runTestDirected(getDirectedN3(), new int[] { 5 }, new int[] { 6 }, new double[] { 4.0 });
    }

    @Test
    public void testDirectedN4()
    {
        runTestDirected(
            getDirectedN4(), new int[] { 1 }, new int[] { 4 }, new double[] { 2000000000.0 });
    }

    @Test
    public void testDirectedN6()
    {
        runTestDirected(getDirectedN6(), new int[] { 1 }, new int[] { 50 }, new double[] { 20.0 });
    }

    @Test
    public void testDirectedN7()
    {
        runTestDirected(getDirectedN7(), new int[] { 1 }, new int[] { 50 }, new double[] { 31.0 });
    }

    @Test
    public void testDirectedN8()
    {
        runTestDirected(getDirectedN8(), new int[] { 0 }, new int[] { 5 }, new double[] { 23 });
    }

    @Test
    public void testDirectedN9()
    {
        runTestDirected(getDirectedN9(), new int[] { 0 }, new int[] { 8 }, new double[] { 22 });
    }

    @Test
    public void testDirectedN10()
    {
        runTestDirected(getDirectedN10(), new int[] { 1 }, new int[] { 99 }, new double[] { 173 });
    }

    @Test
    public void testDirectedN11()
    {
        runTestDirected(getDirectedN11(), new int[] { 1 }, new int[] { 99 }, new double[] { 450 });
    }

    @Test
    public void testDirectedN12()
    {
        runTestDirected(getDirectedN12(), new int[] { 1 }, new int[] { 99 }, new double[] { 203 });
    }

    /*************** TEST CASES FOR UNDIRECTED GRAPHS ***************/

    @Test
    public void testUndirectedN1()
    {
        runTestUndirected(getUndirectedN1(), 0, 8, 28);
    }

    @Test
    public void testUndirectedN2()
    {
        runTestUndirected(getUndirectedN2(), 1, 4, 93);
    }

    @Test
    public void testUndirectedN3()
    {
        runTestUndirected(getUndirectedN3(), 1, 49, 104);
    }

    @Test
    public void testUndirectedN4()
    {
        runTestUndirected(getUndirectedN4(), 1, 99, 634);
    }

    @Test
    public void testUndirectedN5()
    {
        runTestUndirected(getUndirectedN5(), 1, 49, 112);
    }

    @Test
    public void testUndirectedN6()
    {
        runTestUndirected(getUndirectedN6(), 1, 69, 194);
    }

    @Test
    public void testUndirectedN7()
    {
        runTestUndirected(getUndirectedN7(), 1, 69, 33);
    }

    @Test
    public void testUndirectedN8()
    {
        runTestUndirected(getUndirectedN8(), 1, 99, 501);
    }

    @Test
    public void testUndirectedN9()
    {
        runTestUndirected(getUndirectedN9(), 1, 2, 0);
    }
}
