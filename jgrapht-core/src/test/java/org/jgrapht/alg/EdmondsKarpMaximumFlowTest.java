/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
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

import junit.framework.TestCase;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.Map;


public final class EdmondsKarpMaximumFlowTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testCornerCases()
    {
        final DirectedWeightedMultigraph<Integer, DefaultWeightedEdge> simple =
            new DirectedWeightedMultigraph<Integer, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        simple.addVertex(0);
        simple.addVertex(1);
        final DefaultWeightedEdge e = simple.addEdge(0, 1);
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
            final EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(
                    simple);
            solver.calculateMaximumFlow(0, 1);
            final Map<DefaultWeightedEdge, Double> flow = solver.getMaximumFlow();
            flow.put(e, 25.0);
            fail();
        } catch (UnsupportedOperationException ex) {
        }
        try {
            final EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(
                    simple);
            solver.calculateMaximumFlow(2, 0);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            final EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(
                    simple);
            solver.calculateMaximumFlow(1, 2);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            final EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(
                    simple);
            solver.calculateMaximumFlow(0, 0);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            final EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge> solver =
                new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(
                    simple);
            solver.calculateMaximumFlow(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        try {
            final EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge> solver =
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
        final int [] tails,
        final int [] heads,
        final double [] capacities,
        final int [] sources,
        final int [] sinks,
        final double [] expectedResults)
    {
        assertTrue(tails.length == heads.length);
        assertTrue(tails.length == capacities.length);
        final DirectedWeightedMultigraph<Integer, DefaultWeightedEdge> network =
            new DirectedWeightedMultigraph<Integer, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        final int m = tails.length;
        for (int i = 0; i < m; i++) {
            network.addVertex(tails[i]);
            network.addVertex(heads[i]);
            final DefaultWeightedEdge e = network.addEdge(tails[i], heads[i]);
            network.setEdgeWeight(e, capacities[i]);
        }
        assertTrue(sources.length == sinks.length);
        final int q = sources.length;
        for (int i = 0; i < q; i++) {
            network.addVertex(sources[i]);
            network.addVertex(sinks[i]);
        }
        final EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge> solver =
            new EdmondsKarpMaximumFlow<Integer, DefaultWeightedEdge>(network);
        assertTrue(solver.getCurrentSource() == null);
        assertTrue(solver.getCurrentSink() == null);
        assertTrue(solver.getMaximumFlowValue() == null);
        assertTrue(solver.getMaximumFlow() == null);
        for (int i = 0; i < q; i++) {
            solver.calculateMaximumFlow(sources[i], sinks[i]);
            assertTrue(solver.getCurrentSource().equals(sources[i]));
            assertTrue(solver.getCurrentSink().equals(sinks[i]));
            final double flowValue = solver.getMaximumFlowValue();
            final Map<DefaultWeightedEdge, Double> flow = solver.getMaximumFlow();
            assertEquals(
                expectedResults[i],
                flowValue,
                EdmondsKarpMaximumFlow.DEFAULT_EPSILON);
            for (final DefaultWeightedEdge e : network.edgeSet()) {
                assertTrue(flow.containsKey(e));
            }
            for (final DefaultWeightedEdge e : flow.keySet()) {
                assertTrue(network.containsEdge(e));
                assertTrue(
                    flow.get(e) >= -EdmondsKarpMaximumFlow.DEFAULT_EPSILON);
                assertTrue(
                    flow.get(e)
                    <= network.getEdgeWeight(e)
                        + EdmondsKarpMaximumFlow.DEFAULT_EPSILON);
            }
            for (final Integer v : network.vertexSet()) {
                double balance = 0.0;
                for (final DefaultWeightedEdge e : network.outgoingEdgesOf(v)) {
                    balance -= flow.get(e);
                }
                for (final DefaultWeightedEdge e : network.incomingEdgesOf(v)) {
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
