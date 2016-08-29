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
 * MinimumSourceSinkCutTest.java
 * -----------------
 * (C) Copyright 2016, by Joris Kinable and Contributors.
 *
 * Original Author:  Joris Kinable
 * Contributor(s): -
 *
 * $Id$
 *
 * Changes
 * -------
 * Aug-2016 : Initial version (JK);
 */
package org.jgrapht.alg.flow;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.interfaces.MinimumSTCutAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joris Kinable
 */
public abstract class MinimumSourceSinkCutTest extends MaximumFlowMinimumCutAlgorithmTestBase{

    public static final int NR_RANDOM_TESTS=20;

    abstract MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> createSolver(Graph<Integer, DefaultWeightedEdge> network);


    private void runTestDirected(DirectedGraph<Integer, DefaultWeightedEdge> network,
                                 int source,
                                 int sink,
                                 double expectedCutWeight) {
        network.addVertex(source);
        network.addVertex(sink);

        MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> mc = createSolver(network);
        double cutWeight=mc.calculateMinCut(source, sink);
        Set<Integer> sourcePartition=mc.getSourcePartition();
        Set<Integer> sinkPartition=mc.getSinkPartition();
        Set<DefaultWeightedEdge> cutEdges=mc.getCutEdges();

        this.verifyDirected(network, source, sink, expectedCutWeight, cutWeight, sourcePartition, sinkPartition, cutEdges);
    }

    void verifyDirected(DirectedGraph<Integer, DefaultWeightedEdge> network,
                          int source,
                          int sink,
                          double expectedCutWeight,
                          double cutWeight,
                          Set<Integer> sourcePartition,
                          Set<Integer> sinkPartition,
                          Set<DefaultWeightedEdge> cutEdges){

        assertEquals(expectedCutWeight, cutWeight);
        assertTrue(sourcePartition.contains(source));
        assertTrue(sinkPartition.contains(sink));
        assertTrue(Collections.disjoint(sourcePartition, sinkPartition));
        Set<Integer> unionSet=new HashSet<>(sourcePartition);
        unionSet.addAll(sinkPartition);
        unionSet.removeAll(network.vertexSet());
        assertTrue(unionSet.isEmpty());

        assertEquals(
                network.edgeSet().stream().filter(e -> sourcePartition.contains(network.getEdgeSource(e)) && sinkPartition.contains(network.getEdgeTarget(e))).collect(Collectors.toSet()),
                cutEdges
        );
        assertEquals(cutWeight, cutEdges.stream().mapToDouble(network::getEdgeWeight).sum());
    }

    private void runTestUndirected(
            UndirectedGraph<Integer, DefaultWeightedEdge> network,
            int source,
            int sink,
            double expectedCutWeight)
    {
        MinimumSTCutAlgorithm<Integer, DefaultWeightedEdge> mc = createSolver(network);
        double cutWeight=mc.calculateMinCut(source, sink);
        Set<Integer> sourcePartition=mc.getSourcePartition();
        Set<Integer> sinkPartition=mc.getSinkPartition();
        Set<DefaultWeightedEdge> cutEdges=mc.getCutEdges();

        this.verifyUndirected(network, source, sink, expectedCutWeight, cutWeight, sourcePartition, sinkPartition, cutEdges);
    }

    void verifyUndirected(UndirectedGraph<Integer, DefaultWeightedEdge> network,
                          int source,
                          int sink,
                          double expectedCutWeight,
                          double cutWeight,
                          Set<Integer> sourcePartition,
                          Set<Integer> sinkPartition,
                          Set<DefaultWeightedEdge> cutEdges){


        assertEquals(expectedCutWeight, cutWeight);
        assertTrue(sourcePartition.contains(source));
        assertTrue(sinkPartition.contains(sink));
        assertTrue(Collections.disjoint(sourcePartition, sinkPartition));
        Set<Integer> unionSet=new HashSet<>(sourcePartition);
        unionSet.addAll(sinkPartition);
        unionSet.removeAll(network.vertexSet());
        assertTrue(unionSet.isEmpty());

        assertEquals(
                network.edgeSet().stream().filter(e -> sourcePartition.contains(network.getEdgeSource(e)) ^ sourcePartition.contains(network.getEdgeTarget(e))).collect(Collectors.toSet()),
                cutEdges
        );
        assertEquals(cutWeight, cutEdges.stream().mapToDouble(network::getEdgeWeight).sum());

    }

    public void testProblematicCase(){
        UndirectedGraph<Integer, DefaultWeightedEdge> network=new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
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


    public void testDirectedN0()
    {
        runTestDirected(
                getDirectedN0(),
                1,
                4,
                5.0);
    }

    public void testDirectedN1()
    {
        runTestDirected(
                getDirectedN1(),
                1,
                4057218,
                0.0);
    }

    public void testDirectedN2() {
        runTestDirected(
                getDirectedN2(),
                3,
                6,
                2.0);
    }

    public void testDirectedN3() {
        runTestDirected(
                getDirectedN3(),
                5,
                6,
                4.0);
    }

    public void testDirectedN4() {
        runTestDirected(
                getDirectedN4(),
                1,
                4,
                2000000000.0);
    }

    public void testDirectedN6() {
        runTestDirected(
                getDirectedN6(),
                1,
                50,
                20.0
        );
    }

    public void testDirectedN7() {
        runTestDirected(
                getDirectedN7(),
                1,
                50,
                31.0
        );
    }

    public void testDirectedN8(){
        runTestDirected(
                getDirectedN8(),
                0,
                5,
                23.0
        );
    }

    public void testDirectedN9(){
        runTestDirected(
                getDirectedN9(),
                0,
                8,
                22.0
        );
    }

    public void testDirectedN10(){
        runTestDirected(
                getDirectedN10(),
                1,
                99,
                173.0
        );
    }

    public void testDirectedN11(){
        runTestDirected(
                getDirectedN11(),
                1,
                99,
                450.0
        );
    }

    public void testDirectedN12(){
        runTestDirected(
                getDirectedN12(),
                1,
                99,
                203.0
        );
    }


    /*************** TEST CASES FOR UNDIRECTED GRAPHS ***************/

    public void testUndirectedN1(){
        runTestUndirected(getUndirectedN1(), 0, 8, 28);
    }

    public void testUndirectedN2(){
        runTestUndirected(getUndirectedN2(), 1, 4, 93);
    }

    public void testUndirectedN3(){
        runTestUndirected(getUndirectedN3(), 1, 49, 104);
    }

    public void testUndirectedN4(){
        runTestUndirected(getUndirectedN4(), 1, 99, 634);
    }

    public void testUndirectedN5(){
        runTestUndirected(getUndirectedN5(), 1, 49, 112);
    }

    public void testUndirectedN6(){
        runTestUndirected(getUndirectedN6(), 1, 69, 194);
    }

    public void testUndirectedN7(){
        runTestUndirected(getUndirectedN7(), 1, 69, 33);
    }

    public void testUndirectedN8(){
        runTestUndirected(getUndirectedN8(), 1, 99, 501);
    }

    public void testUndirectedN9(){
        runTestUndirected(getUndirectedN9(), 1, 2, 0);
    }
}
