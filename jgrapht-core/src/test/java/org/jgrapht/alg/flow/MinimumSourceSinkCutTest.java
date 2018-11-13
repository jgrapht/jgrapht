/*
 * (C) Copyright 2016-2018, by Joris Kinable and Contributors.
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
import java.util.stream.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Joris Kinable
 */
public abstract class MinimumSourceSinkCutTest
    extends
    MaximumFlowMinimumCutAlgorithmTestBase
{

    public static final int NR_RANDOM_TESTS = 500;

    abstract MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> createSolver(
        Graph<Integer, DefaultWeightedEdge> network);

    private void runTestDirected(
        Graph<Integer, DefaultWeightedEdge> network, int source, int sink, double expectedCutWeight)
    {
        network.addVertex(source);
        network.addVertex(sink);

        MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> mc = createSolver(network);
        double cutWeight = mc.calculateMinCut(source, sink);
        Set<Integer> sourcePartition = mc.getSourcePartition();
        Set<Integer> sinkPartition = mc.getSinkPartition();
        Set<DefaultWeightedEdge> cutEdges = mc.getCutEdges();

        this.verifyDirected(
            network, source, sink, expectedCutWeight, cutWeight, sourcePartition, sinkPartition,
            cutEdges);
    }

    void verifyDirected(
        Graph<Integer, DefaultWeightedEdge> network, int source, int sink, double expectedCutWeight,
        double cutWeight, Set<Integer> sourcePartition, Set<Integer> sinkPartition,
        Set<DefaultWeightedEdge> cutEdges)
    {

        assertEquals(expectedCutWeight, cutWeight, 0);
        assertTrue(sourcePartition.contains(source));
        assertTrue(sinkPartition.contains(sink));
        assertTrue(Collections.disjoint(sourcePartition, sinkPartition));
        Set<Integer> unionSet = new HashSet<>(sourcePartition);
        unionSet.addAll(sinkPartition);
        unionSet.removeAll(network.vertexSet());
        assertTrue(unionSet.isEmpty());

        assertEquals(
            network
                .edgeSet().stream()
                .filter(
                    e -> sourcePartition.contains(network.getEdgeSource(e))
                        && sinkPartition.contains(network.getEdgeTarget(e)))
                .collect(Collectors.toSet()),
            cutEdges);
        assertEquals(cutWeight, cutEdges.stream().mapToDouble(network::getEdgeWeight).sum(), 0);
    }

    private void runTestUndirected(
        Graph<Integer, DefaultWeightedEdge> network, int source, int sink, double expectedCutWeight)
    {
        MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> mc = createSolver(network);
        double cutWeight = mc.calculateMinCut(source, sink);
        Set<Integer> sourcePartition = mc.getSourcePartition();
        Set<Integer> sinkPartition = mc.getSinkPartition();
        Set<DefaultWeightedEdge> cutEdges = mc.getCutEdges();

        this.verifyUndirected(
            network, source, sink, expectedCutWeight, cutWeight, sourcePartition, sinkPartition,
            cutEdges);
    }

    void verifyUndirected(
        Graph<Integer, DefaultWeightedEdge> network, int source, int sink, double expectedCutWeight,
        double cutWeight, Set<Integer> sourcePartition, Set<Integer> sinkPartition,
        Set<DefaultWeightedEdge> cutEdges)
    {

        assertEquals(expectedCutWeight, cutWeight, 0);
        assertTrue(sourcePartition.contains(source));
        assertTrue(sinkPartition.contains(sink));
        assertTrue(Collections.disjoint(sourcePartition, sinkPartition));
        Set<Integer> unionSet = new HashSet<>(sourcePartition);
        unionSet.addAll(sinkPartition);
        unionSet.removeAll(network.vertexSet());
        assertTrue(unionSet.isEmpty());

        assertEquals(
            network
                .edgeSet().stream()
                .filter(
                    e -> sourcePartition.contains(network.getEdgeSource(e))
                        ^ sourcePartition.contains(network.getEdgeTarget(e)))
                .collect(Collectors.toSet()),
            cutEdges);
        assertEquals(cutWeight, cutEdges.stream().mapToDouble(network::getEdgeWeight).sum(), 0);

    }

    @Test
    public void testProblematicCase()
    {
        Graph<Integer, DefaultWeightedEdge> network =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(network, 1, 2, 0);
        Graphs.addEdgeWithVertices(network, 1, 4, 1);
        Graphs.addEdgeWithVertices(network, 1, 5, 1);
        Graphs.addEdgeWithVertices(network, 4, 5, 1);
        Graphs.addEdgeWithVertices(network, 2, 3, 1);
        Graphs.addEdgeWithVertices(network, 2, 6, 1);
        Graphs.addEdgeWithVertices(network, 3, 6, 1);
        Graphs.addEdgeWithVertices(network, 3, 4, 0);
        runTestUndirected(network, 1, 6, 0);
    }

    @Test
    public void testDirectedN0()
    {
        runTestDirected(getDirectedN0(), 1, 4, 5.0);
    }

    @Test
    public void testDirectedN1()
    {
        runTestDirected(getDirectedN1(), 1, 4057218, 0.0);
    }

    @Test
    public void testDirectedN2()
    {
        runTestDirected(getDirectedN2(), 3, 6, 2.0);
    }

    @Test
    public void testDirectedN3()
    {
        runTestDirected(getDirectedN3(), 5, 6, 4.0);
    }

    @Test
    public void testDirectedN4()
    {
        runTestDirected(getDirectedN4(), 1, 4, 2000000000.0);
    }

    @Test
    public void testDirectedN6()
    {
        runTestDirected(getDirectedN6(), 1, 50, 20.0);
    }

    @Test
    public void testDirectedN7()
    {
        runTestDirected(getDirectedN7(), 1, 50, 31.0);
    }

    @Test
    public void testDirectedN8()
    {
        runTestDirected(getDirectedN8(), 0, 5, 23.0);
    }

    @Test
    public void testDirectedN9()
    {
        runTestDirected(getDirectedN9(), 0, 8, 22.0);
    }

    @Test
    public void testDirectedN10()
    {
        runTestDirected(getDirectedN10(), 1, 99, 173.0);
    }

    @Test
    public void testDirectedN11()
    {
        runTestDirected(getDirectedN11(), 1, 99, 450.0);
    }

    @Test
    public void testDirectedN12()
    {
        runTestDirected(getDirectedN12(), 1, 99, 203.0);
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
