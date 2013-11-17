/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -----------------
 * EdmondsKarpMaximumFlowTest.java
 * -----------------
 * (C) Copyright 2008-2008, by Ilya Razenshteyn and Contributors.
 *
 * Original Author:  Ilya Razenshteyn
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 */
package org.jgrapht.alg;

import java.util.*;

import junit.framework.*;

import org.jgrapht.graph.*;


public final class EdmondsKarpMaximumFlowTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testCornerCases()
    {
        DirectedWeightedMultigraph<Integer, DefaultWeightedEdge> simple =
            new DirectedWeightedMultigraph<Integer, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        simple.addVertex(0);
        simple.addVertex(1);
        DefaultWeightedEdge e = simple.addEdge(0, 1);
        try {
            new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(null);
            fail();
        } catch (NullPointerException ex) {
        }
        try {
            new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(
                simple,
                -0.1);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            simple.setEdgeWeight(e, -1.0);
            new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(simple);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            simple.setEdgeWeight(e, 1.0);
            EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(
                    simple);
            solver.calculateMaximumFlow(0, 1);
            Map<DefaultWeightedEdge, Double> flow = solver.getMaximumFlow();
            flow.put(e, 25.0);
            fail();
        } catch (UnsupportedOperationException ex) {
        }
        try {
            EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(
                    simple);
            solver.calculateMaximumFlow(2, 0);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(
                    simple);
            solver.calculateMaximumFlow(1, 2);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(
                    simple);
            solver.calculateMaximumFlow(0, 0);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(
                    simple);
            solver.calculateMaximumFlow(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(
                    simple);
            solver.calculateMaximumFlow(0, null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    /**
     * .
     */
    public void testLogic()
    {
        runTest(
            new int[] {},
            new int[] {},
            new double[] {},
            new int[] { 1 },
            new int[] { 4057218 },
            new double[] { 0.0 });
        runTest(
            new int[] { 3, 1, 4, 3, 2, 8, 2, 5, 7 },
            new int[] { 1, 4, 8, 2, 8, 6, 5, 7, 6 },
            new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            new int[] { 3 },
            new int[] { 6 },
            new double[] { 2 });
        runTest(
            new int[] { 5, 5, 5, 1, 1, 4, 2, 7, 8, 3 },
            new int[] { 1, 4, 2, 7, 8, 3, 8, 6, 6, 6 },
            new double[] { 7, 8, 573146, 31337, 1, 1, 1, 1, 2391717, 170239 },
            new int[] { 5 },
            new int[] { 6 },
            new double[] { 4.0 });
        runTest(
            new int[] { 1, 1, 2, 2, 3 },
            new int[] { 2, 3, 3, 4, 4 },
            new double[] {
                1000000000.0, 1000000000.0, 1.0, 1000000000.0, 1000000000.0
            },
            new int[] { 1 },
            new int[] { 4 },
            new double[] { 2000000000.0 });
    }

    private void runTest(
        int [] tails,
        int [] heads,
        double [] capacities,
        int [] sources,
        int [] sinks,
        double [] expectedResults)
    {
        assertTrue(tails.length == heads.length);
        assertTrue(tails.length == capacities.length);
        DirectedWeightedMultigraph<Integer, DefaultWeightedEdge> network =
            new DirectedWeightedMultigraph<Integer, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        int m = tails.length;
        for (int i = 0; i < m; i++) {
            network.addVertex(tails[i]);
            network.addVertex(heads[i]);
            DefaultWeightedEdge e = network.addEdge(tails[i], heads[i]);
            network.setEdgeWeight(e, capacities[i]);
        }
        assertTrue(sources.length == sinks.length);
        int q = sources.length;
        for (int i = 0; i < q; i++) {
            network.addVertex(sources[i]);
            network.addVertex(sinks[i]);
        }
        EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge> solver =
            new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(network);
        assertTrue(solver.getCurrentSource() == null);
        assertTrue(solver.getCurrentSink() == null);
        assertTrue(solver.getMaximumFlowValue() == null);
        assertTrue(solver.getMaximumFlow() == null);
        for (int i = 0; i < q; i++) {
            solver.calculateMaximumFlow(sources[i], sinks[i]);
            assertTrue(solver.getCurrentSource().equals(sources[i]));
            assertTrue(solver.getCurrentSink().equals(sinks[i]));
            double flowValue = solver.getMaximumFlowValue();
            Map<DefaultWeightedEdge, Double> flow = solver.getMaximumFlow();
            assertEquals(
                expectedResults[i],
                flowValue,
                EdmondsKarpMaximumFlow.DEFAULT_EPSILON);
            for (DefaultWeightedEdge e : network.edgeSet()) {
                assertTrue(flow.containsKey(e));
            }
            for (DefaultWeightedEdge e : flow.keySet()) {
                assertTrue(network.containsEdge(e));
                assertTrue(
                    flow.get(e) >= -EdmondsKarpMaximumFlow.DEFAULT_EPSILON);
                assertTrue(
                    flow.get(e)
                    <= (network.getEdgeWeight(e)
                        + EdmondsKarpMaximumFlow.DEFAULT_EPSILON));
            }
            for (Integer v : network.vertexSet()) {
                double balance = 0.0;
                for (DefaultWeightedEdge e : network.outgoingEdgesOf(v)) {
                    balance -= flow.get(e);
                }
                for (DefaultWeightedEdge e : network.incomingEdgesOf(v)) {
                    balance += flow.get(e);
                }
                if (v.equals(sources[i])) {
                    assertEquals(
                        -flowValue,
                        balance,
                        EdmondsKarpMaximumFlow.DEFAULT_EPSILON);
                } else if (v.equals(sinks[i])) {
                    assertEquals(
                        flowValue,
                        balance,
                        EdmondsKarpMaximumFlow.DEFAULT_EPSILON);
                } else {
                    assertEquals(
                        0.0,
                        balance,
                        EdmondsKarpMaximumFlow.DEFAULT_EPSILON);
                }
            }
        }
    }
}

// End EdmondsKarpMaximumFlowTest.java
