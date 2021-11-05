/*
 * (C) Copyright 2019-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.opt.graph.sparse;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.junit.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static org.junit.Assert.*;

/**
 * Tests
 * 
 * @author Dimitrios Michail
 */
public class SparseIntGraphTest
{

    @Test
    public void testUndirected()
    {
        testUndirected((vc, edges) -> new SparseIntUndirectedGraph(vc, edges));
    }

    @Test
    public void testUndirectedWithLoops()
    {
        testUndirectedWithLoops((vc, edges) -> new SparseIntUndirectedGraph(vc, edges));
    }

    @Test
    public void testUndirectedWeighted()
    {
        testUndirectedWeighted((vc, edges) -> new SparseIntUndirectedWeightedGraph(vc, edges));
    }

    @Test
    public void testDirected()
    {
        testDirected((vc, edges) -> new SparseIntDirectedGraph(vc, edges));
    }
    
    @Test
    public void testDirectedLazyIncoming()
    {
        testDirected((vc, edges) -> new SparseIntDirectedGraph(vc, edges, IncomingEdgesSupport.LAZY_INCOMING_EDGES));
    }
    
    @Test
    public void testDirectedLazyNoIncoming()
    {
        testDirectedNoIncoming((vc, edges) -> new SparseIntDirectedGraph(vc, edges, IncomingEdgesSupport.NO_INCOMING_EDGES));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testDirectedLazyNoIncomingFail()
    {
        testDirected((vc, edges) -> new SparseIntDirectedGraph(vc, edges, IncomingEdgesSupport.NO_INCOMING_EDGES));
    }

    @Test
    public void testDirectedWeighted()
    {
        testDirectedWeighted((vc, edges) -> new SparseIntDirectedWeightedGraph(vc, edges));
    }

    public static void testUndirected(
        BiFunction<Integer, List<Pair<Integer, Integer>>, Graph<Integer, Integer>> graphSupplier)
    {
        final Integer vertexCount = 6;
        List<Pair<Integer, Integer>> edges = Arrays
            .asList(
                Pair.of(0, 5), Pair.of(0, 2), Pair.of(3, 4), Pair.of(1, 4), Pair.of(0, 1),
                Pair.of(3, 1), Pair.of(2, 4));

        Graph<Integer, Integer> g = graphSupplier.apply(vertexCount, edges);

        assertEquals(vertexCount.intValue(), g.vertexSet().size());
        assertTrue(g.containsVertex(0));
        assertTrue(g.containsVertex(1));
        assertTrue(g.containsVertex(2));
        assertTrue(g.containsVertex(3));
        assertTrue(g.containsVertex(4));
        assertTrue(g.containsVertex(5));

        assertEquals(3, g.degreeOf(0));
        assertEquals(3, g.inDegreeOf(0));
        assertEquals(3, g.outDegreeOf(0));
        assertEquals(Set.of(0, 1, 4), g.edgesOf(0));
        assertEquals(Set.of(0, 1, 4), g.incomingEdgesOf(0));
        assertEquals(Set.of(0, 1, 4), g.outgoingEdgesOf(0));

        assertEquals(3, g.degreeOf(1));
        assertEquals(3, g.inDegreeOf(1));
        assertEquals(3, g.outDegreeOf(1));
        assertEquals(Set.of(3, 4, 5), g.edgesOf(1));
        assertEquals(Set.of(3, 4, 5), g.incomingEdgesOf(1));
        assertEquals(Set.of(3, 4, 5), g.outgoingEdgesOf(1));

        assertEquals(2, g.degreeOf(2));
        assertEquals(2, g.inDegreeOf(2));
        assertEquals(2, g.outDegreeOf(2));
        assertEquals(Set.of(1, 6), g.edgesOf(2));
        assertEquals(Set.of(1, 6), g.incomingEdgesOf(2));
        assertEquals(Set.of(1, 6), g.outgoingEdgesOf(2));

        assertEquals(2, g.degreeOf(3));
        assertEquals(2, g.inDegreeOf(3));
        assertEquals(2, g.outDegreeOf(3));
        assertEquals(Set.of(2, 5), g.edgesOf(3));
        assertEquals(Set.of(2, 5), g.incomingEdgesOf(3));
        assertEquals(Set.of(2, 5), g.outgoingEdgesOf(3));

        assertEquals(3, g.degreeOf(4));
        assertEquals(3, g.inDegreeOf(4));
        assertEquals(3, g.outDegreeOf(4));
        assertEquals(Set.of(2, 3, 6), g.edgesOf(4));
        assertEquals(Set.of(2, 3, 6), g.incomingEdgesOf(4));
        assertEquals(Set.of(2, 3, 6), g.outgoingEdgesOf(4));

        assertEquals(1, g.degreeOf(5));
        assertEquals(1, g.inDegreeOf(5));
        assertEquals(1, g.outDegreeOf(5));
        assertEquals(Set.of(0), g.edgesOf(5));
        assertEquals(Set.of(0), g.incomingEdgesOf(5));
        assertEquals(Set.of(0), g.outgoingEdgesOf(5));

        assertEquals(Integer.valueOf(0), g.getEdgeSource(0));
        assertEquals(Integer.valueOf(5), g.getEdgeTarget(0));
        assertEquals(Integer.valueOf(0), g.getEdgeSource(1));
        assertEquals(Integer.valueOf(2), g.getEdgeTarget(1));
        assertEquals(Integer.valueOf(3), g.getEdgeSource(2));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(2));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(3));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(3));
        assertEquals(Integer.valueOf(0), g.getEdgeSource(4));
        assertEquals(Integer.valueOf(1), g.getEdgeTarget(4));
        assertEquals(Integer.valueOf(3), g.getEdgeSource(5));
        assertEquals(Integer.valueOf(1), g.getEdgeTarget(5));
        assertEquals(Integer.valueOf(2), g.getEdgeSource(6));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(6));

        assertEquals(
            IntStream.range(0, edges.size()).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.edgeSet());
        assertEquals(
            IntStream.range(0, 6).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.vertexSet());

        GraphType type = g.getType();
        assertTrue(type.isAllowingCycles());
        assertTrue(type.isAllowingMultipleEdges());
        assertTrue(type.isAllowingSelfLoops());
        assertTrue(type.isUndirected());
        assertFalse(type.isModifiable());
        assertFalse(type.isDirected());
        assertFalse(type.isMixed());
        assertFalse(type.isWeighted());

        int i = 0;
        for (Pair<Integer, Integer> p : edges) {
            assertEquals(Integer.valueOf(i), g.getEdge(p.getFirst(), p.getSecond()));
            i++;
        }

        int j = 0;
        for (Pair<Integer, Integer> p : edges) {
            Set<Integer> edgeSet = Collections.singleton(Integer.valueOf(j));
            assertEquals(edgeSet, g.getAllEdges(p.getFirst(), p.getSecond()));
            j++;
        }

    }

    public static void testUndirectedWithLoops(
        BiFunction<Integer, List<Pair<Integer, Integer>>, Graph<Integer, Integer>> graphSupplier)
    {
        final int vertexCount = 4;
        List<Pair<Integer, Integer>> edges = Arrays
            .asList(
                Pair.of(0, 0), Pair.of(0, 1), Pair.of(0, 2), Pair.of(0, 0), Pair.of(0, 1),
                Pair.of(1, 1), Pair.of(1, 2));

        Graph<Integer, Integer> g = graphSupplier.apply(vertexCount, edges);

        assertEquals(4, g.vertexSet().size());
        assertTrue(g.containsVertex(0));
        assertTrue(g.containsVertex(1));
        assertTrue(g.containsVertex(2));
        assertTrue(g.containsVertex(3));

        assertEquals(7, g.degreeOf(0));
        assertEquals(7, g.inDegreeOf(0));
        assertEquals(7, g.outDegreeOf(0));
        assertEquals(Set.of(0, 3, 1, 4, 2), g.edgesOf(0));
        assertEquals(Set.of(0, 3, 1, 4, 2), g.incomingEdgesOf(0));
        assertEquals(Set.of(0, 3, 1, 4, 2), g.outgoingEdgesOf(0));

        assertEquals(5, g.degreeOf(1));
        assertEquals(5, g.inDegreeOf(1));
        assertEquals(5, g.outDegreeOf(1));
        assertEquals(Set.of(1, 4, 5, 6), g.edgesOf(1));
        assertEquals(Set.of(1, 4, 5, 6), g.incomingEdgesOf(1));
        assertEquals(Set.of(1, 4, 5, 6), g.outgoingEdgesOf(1));

        assertEquals(2, g.degreeOf(2));
        assertEquals(2, g.inDegreeOf(2));
        assertEquals(2, g.outDegreeOf(2));
        assertEquals(Set.of(2, 6), g.edgesOf(2));
        assertEquals(Set.of(2, 6), g.incomingEdgesOf(2));
        assertEquals(Set.of(2, 6), g.outgoingEdgesOf(2));

        assertEquals(0, g.degreeOf(3));
        assertEquals(0, g.inDegreeOf(3));
        assertEquals(0, g.outDegreeOf(3));
        assertEquals(Set.of(), g.edgesOf(3));
        assertEquals(Set.of(), g.incomingEdgesOf(3));
        assertEquals(Set.of(), g.outgoingEdgesOf(3));

        assertEquals(Integer.valueOf(0), g.getEdgeSource(0));
        assertEquals(Integer.valueOf(0), g.getEdgeTarget(0));
        assertEquals(Integer.valueOf(0), g.getEdgeSource(1));
        assertEquals(Integer.valueOf(1), g.getEdgeTarget(1));
        assertEquals(Integer.valueOf(0), g.getEdgeSource(2));
        assertEquals(Integer.valueOf(2), g.getEdgeTarget(2));
        assertEquals(Integer.valueOf(0), g.getEdgeSource(3));
        assertEquals(Integer.valueOf(0), g.getEdgeTarget(3));
        assertEquals(Integer.valueOf(0), g.getEdgeSource(4));
        assertEquals(Integer.valueOf(1), g.getEdgeTarget(4));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(5));
        assertEquals(Integer.valueOf(1), g.getEdgeTarget(5));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(6));
        assertEquals(Integer.valueOf(2), g.getEdgeTarget(6));

        assertEquals(
            IntStream.range(0, edges.size()).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.edgeSet());
        assertEquals(
            IntStream.range(0, 4).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.vertexSet());

        GraphType type = g.getType();
        assertTrue(type.isAllowingCycles());
        assertTrue(type.isAllowingMultipleEdges());
        assertTrue(type.isAllowingSelfLoops());
        assertTrue(type.isUndirected());
        assertFalse(type.isModifiable());
        assertFalse(type.isDirected());
        assertFalse(type.isMixed());
        assertFalse(type.isWeighted());

    }

    public static void testUndirectedWeighted(
        BiFunction<Integer, List<Triple<Integer, Integer, Double>>,
            Graph<Integer, Integer>> graphSupplier)
    {
        final Integer vertexCount = 6;
        List<Triple<Integer, Integer, Double>> edges = Arrays
            .asList(
                Triple.of(0, 5, 1d), Triple.of(0, 2, 2d), Triple.of(3, 4, 3d), Triple.of(1, 4, 4d),
                Triple.of(0, 1, 5d), Triple.of(3, 1, 6d), Triple.of(2, 4, 7d));

        Graph<Integer, Integer> g = graphSupplier.apply(vertexCount, edges);

        assertEquals(6, g.vertexSet().size());
        assertTrue(g.containsVertex(0));
        assertTrue(g.containsVertex(1));
        assertTrue(g.containsVertex(2));
        assertTrue(g.containsVertex(3));
        assertTrue(g.containsVertex(4));
        assertTrue(g.containsVertex(5));

        assertEquals(3, g.degreeOf(0));
        assertEquals(3, g.inDegreeOf(0));
        assertEquals(3, g.outDegreeOf(0));
        assertEquals(Set.of(0, 1, 4), g.edgesOf(0));
        assertEquals(Set.of(0, 1, 4), g.incomingEdgesOf(0));
        assertEquals(Set.of(0, 1, 4), g.outgoingEdgesOf(0));

        assertEquals(3, g.degreeOf(1));
        assertEquals(3, g.inDegreeOf(1));
        assertEquals(3, g.outDegreeOf(1));
        assertEquals(Set.of(3, 4, 5), g.edgesOf(1));
        assertEquals(Set.of(3, 4, 5), g.incomingEdgesOf(1));
        assertEquals(Set.of(3, 4, 5), g.outgoingEdgesOf(1));

        assertEquals(2, g.degreeOf(2));
        assertEquals(2, g.inDegreeOf(2));
        assertEquals(2, g.outDegreeOf(2));
        assertEquals(Set.of(1, 6), g.edgesOf(2));
        assertEquals(Set.of(1, 6), g.incomingEdgesOf(2));
        assertEquals(Set.of(1, 6), g.outgoingEdgesOf(2));

        assertEquals(2, g.degreeOf(3));
        assertEquals(2, g.inDegreeOf(3));
        assertEquals(2, g.outDegreeOf(3));
        assertEquals(Set.of(2, 5), g.edgesOf(3));
        assertEquals(Set.of(2, 5), g.incomingEdgesOf(3));
        assertEquals(Set.of(2, 5), g.outgoingEdgesOf(3));

        assertEquals(3, g.degreeOf(4));
        assertEquals(3, g.inDegreeOf(4));
        assertEquals(3, g.outDegreeOf(4));
        assertEquals(Set.of(2, 3, 6), g.edgesOf(4));
        assertEquals(Set.of(2, 3, 6), g.incomingEdgesOf(4));
        assertEquals(Set.of(2, 3, 6), g.outgoingEdgesOf(4));

        assertEquals(1, g.degreeOf(5));
        assertEquals(1, g.inDegreeOf(5));
        assertEquals(1, g.outDegreeOf(5));
        assertEquals(Set.of(0), g.edgesOf(5));
        assertEquals(Set.of(0), g.incomingEdgesOf(5));
        assertEquals(Set.of(0), g.outgoingEdgesOf(5));

        assertEquals(Integer.valueOf(0), g.getEdgeSource(0));
        assertEquals(Integer.valueOf(5), g.getEdgeTarget(0));
        assertEquals(1d, g.getEdgeWeight(0), 1e-16);
        assertEquals(Integer.valueOf(0), g.getEdgeSource(1));
        assertEquals(Integer.valueOf(2), g.getEdgeTarget(1));
        assertEquals(2d, g.getEdgeWeight(1), 1e-16);
        assertEquals(Integer.valueOf(3), g.getEdgeSource(2));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(2));
        assertEquals(3d, g.getEdgeWeight(2), 1e-16);
        assertEquals(Integer.valueOf(1), g.getEdgeSource(3));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(3));
        assertEquals(4d, g.getEdgeWeight(3), 1e-16);
        assertEquals(Integer.valueOf(0), g.getEdgeSource(4));
        assertEquals(Integer.valueOf(1), g.getEdgeTarget(4));
        assertEquals(5d, g.getEdgeWeight(4), 1e-16);
        assertEquals(Integer.valueOf(3), g.getEdgeSource(5));
        assertEquals(Integer.valueOf(1), g.getEdgeTarget(5));
        assertEquals(6d, g.getEdgeWeight(5), 1e-16);
        assertEquals(Integer.valueOf(2), g.getEdgeSource(6));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(6));
        assertEquals(7d, g.getEdgeWeight(6), 1e-16);

        assertEquals(5d, g.getEdgeWeight(4), 1e-16);
        g.setEdgeWeight(4, 14d);
        assertEquals(14d, g.getEdgeWeight(4), 1e-16);

        assertEquals(
            IntStream.range(0, edges.size()).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.edgeSet());
        assertEquals(
            IntStream.range(0, 6).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.vertexSet());

        GraphType type = g.getType();
        assertTrue(type.isAllowingCycles());
        assertTrue(type.isAllowingMultipleEdges());
        assertTrue(type.isAllowingSelfLoops());
        assertTrue(type.isUndirected());
        assertFalse(type.isModifiable());
        assertFalse(type.isDirected());
        assertFalse(type.isMixed());
        assertTrue(type.isWeighted());

        int i = 0;
        for (Triple<Integer, Integer, Double> p : edges) {
            assertEquals(Integer.valueOf(i), g.getEdge(p.getFirst(), p.getSecond()));
            i++;
        }

        int j = 0;
        for (Triple<Integer, Integer, Double> p : edges) {
            Set<Integer> edgeSet = Collections.singleton(Integer.valueOf(j));
            assertEquals(edgeSet, g.getAllEdges(p.getFirst(), p.getSecond()));
            j++;
        }
    }

    public static void testDirected(
        BiFunction<Integer, List<Pair<Integer, Integer>>, Graph<Integer, Integer>> graphSupplier)
    {
        final Integer vertexCount = 8;
        List<Pair<Integer, Integer>> edges = Arrays
            .asList(
                Pair.of(0, 1), Pair.of(1, 0), Pair.of(1, 4), Pair.of(1, 5), Pair.of(1, 6),
                Pair.of(2, 4), Pair.of(2, 4), Pair.of(2, 4), Pair.of(3, 4), Pair.of(4, 5),
                Pair.of(5, 6), Pair.of(7, 6), Pair.of(7, 7));

        Graph<Integer, Integer> g = graphSupplier.apply(vertexCount, edges);

        assertEquals(vertexCount.intValue(), g.vertexSet().size());
        assertEquals(edges.size(), g.edgeSet().size());

        assertEquals(
            IntStream.range(0, edges.size()).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.edgeSet());
        assertEquals(
            IntStream.range(0, vertexCount).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.vertexSet());

        for (int i = 0; i < vertexCount; i++) {
            assertTrue(g.containsVertex(i));
        }

        assertEquals(2, g.degreeOf(0));
        assertEquals(1, g.inDegreeOf(0));
        assertEquals(1, g.outDegreeOf(0));
        assertEquals(Set.of(0, 1), g.edgesOf(0));
        assertEquals(Set.of(1), g.incomingEdgesOf(0));
        assertEquals(Set.of(0), g.outgoingEdgesOf(0));

        assertEquals(5, g.degreeOf(1));
        assertEquals(1, g.inDegreeOf(1));
        assertEquals(4, g.outDegreeOf(1));
        assertEquals(Set.of(0, 1, 2, 3, 4), g.edgesOf(1));
        assertEquals(Set.of(0), g.incomingEdgesOf(1));
        assertEquals(Set.of(1, 2, 3, 4), g.outgoingEdgesOf(1));

        assertEquals(3, g.degreeOf(2));
        assertEquals(0, g.inDegreeOf(2));
        assertEquals(3, g.outDegreeOf(2));
        assertEquals(Set.of(5, 6, 7), g.edgesOf(2));
        assertEquals(Set.of(), g.incomingEdgesOf(2));
        assertEquals(Set.of(5, 6, 7), g.outgoingEdgesOf(2));

        assertEquals(1, g.degreeOf(3));
        assertEquals(0, g.inDegreeOf(3));
        assertEquals(1, g.outDegreeOf(3));
        assertEquals(Set.of(8), g.edgesOf(3));
        assertEquals(Set.of(), g.incomingEdgesOf(3));
        assertEquals(Set.of(8), g.outgoingEdgesOf(3));

        assertEquals(6, g.degreeOf(4));
        assertEquals(5, g.inDegreeOf(4));
        assertEquals(1, g.outDegreeOf(4));
        assertEquals(Set.of(2, 5, 6, 7, 8, 9), g.edgesOf(4));
        assertEquals(Set.of(2, 5, 6, 7, 8), g.incomingEdgesOf(4));
        assertEquals(Set.of(9), g.outgoingEdgesOf(4));

        assertEquals(3, g.degreeOf(5));
        assertEquals(2, g.inDegreeOf(5));
        assertEquals(1, g.outDegreeOf(5));
        assertEquals(Set.of(3, 9, 10), g.edgesOf(5));
        assertEquals(Set.of(3, 9), g.incomingEdgesOf(5));
        assertEquals(Set.of(10), g.outgoingEdgesOf(5));

        assertEquals(3, g.degreeOf(6));
        assertEquals(3, g.inDegreeOf(6));
        assertEquals(0, g.outDegreeOf(6));
        assertEquals(Set.of(4, 10, 11), g.edgesOf(6));
        assertEquals(Set.of(4, 10, 11), g.incomingEdgesOf(6));
        assertEquals(Set.of(), g.outgoingEdgesOf(6));

        assertEquals(3, g.degreeOf(7));
        assertEquals(1, g.inDegreeOf(7));
        assertEquals(2, g.outDegreeOf(7));
        assertEquals(Set.of(11, 12), g.edgesOf(7));
        assertEquals(Set.of(12), g.incomingEdgesOf(7));
        assertEquals(Set.of(11, 12), g.outgoingEdgesOf(7));

        assertEquals(Integer.valueOf(0), g.getEdgeSource(0));
        assertEquals(Integer.valueOf(1), g.getEdgeTarget(0));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(1));
        assertEquals(Integer.valueOf(0), g.getEdgeTarget(1));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(2));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(2));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(3));
        assertEquals(Integer.valueOf(5), g.getEdgeTarget(3));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(4));
        assertEquals(Integer.valueOf(6), g.getEdgeTarget(4));
        assertEquals(Integer.valueOf(2), g.getEdgeSource(5));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(5));
        assertEquals(Integer.valueOf(2), g.getEdgeSource(6));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(6));
        assertEquals(Integer.valueOf(2), g.getEdgeSource(7));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(7));
        assertEquals(Integer.valueOf(3), g.getEdgeSource(8));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(8));
        assertEquals(Integer.valueOf(4), g.getEdgeSource(9));
        assertEquals(Integer.valueOf(5), g.getEdgeTarget(9));
        assertEquals(Integer.valueOf(5), g.getEdgeSource(10));
        assertEquals(Integer.valueOf(6), g.getEdgeTarget(10));
        assertEquals(Integer.valueOf(7), g.getEdgeSource(11));
        assertEquals(Integer.valueOf(6), g.getEdgeTarget(11));
        assertEquals(Integer.valueOf(7), g.getEdgeSource(12));
        assertEquals(Integer.valueOf(7), g.getEdgeTarget(12));

        GraphType type = g.getType();
        assertTrue(type.isAllowingCycles());
        assertTrue(type.isAllowingMultipleEdges());
        assertTrue(type.isAllowingSelfLoops());
        assertTrue(type.isDirected());
        assertFalse(type.isModifiable());
        assertFalse(type.isUndirected());
        assertFalse(type.isMixed());
        assertFalse(type.isWeighted());

        assertEquals(Integer.valueOf(0), g.getEdge(0, 1));
        assertEquals(Set.of(0), g.getAllEdges(0, 1));
        assertEquals(Integer.valueOf(1), g.getEdge(1, 0));
        assertEquals(Set.of(1), g.getAllEdges(1, 0));
        assertEquals(Integer.valueOf(2), g.getEdge(1, 4));
        assertEquals(Set.of(2), g.getAllEdges(1, 4));
        assertEquals(Integer.valueOf(3), g.getEdge(1, 5));
        assertEquals(Set.of(3), g.getAllEdges(1, 5));
        assertEquals(Integer.valueOf(4), g.getEdge(1, 6));
        assertEquals(Set.of(4), g.getAllEdges(1, 6));
        assertEquals(Integer.valueOf(5), g.getEdge(2, 4));
        assertEquals(Set.of(5, 6, 7), g.getAllEdges(2, 4));
        assertEquals(Integer.valueOf(8), g.getEdge(3, 4));
        assertEquals(Set.of(8), g.getAllEdges(3, 4));
        assertEquals(Integer.valueOf(9), g.getEdge(4, 5));
        assertEquals(Set.of(9), g.getAllEdges(4, 5));
        assertEquals(Integer.valueOf(10), g.getEdge(5, 6));
        assertEquals(Set.of(10), g.getAllEdges(5, 6));
        assertEquals(Integer.valueOf(11), g.getEdge(7, 6));
        assertEquals(Set.of(11), g.getAllEdges(7, 6));
        assertEquals(Integer.valueOf(12), g.getEdge(7, 7));
        assertEquals(Set.of(12), g.getAllEdges(7, 7));

    }

    public static void testDirectedNoIncoming(
        BiFunction<Integer, List<Pair<Integer, Integer>>, Graph<Integer, Integer>> graphSupplier)
    {
        final Integer vertexCount = 8;
        List<Pair<Integer, Integer>> edges = Arrays
            .asList(
                Pair.of(0, 1), Pair.of(1, 0), Pair.of(1, 4), Pair.of(1, 5), Pair.of(1, 6),
                Pair.of(2, 4), Pair.of(2, 4), Pair.of(2, 4), Pair.of(3, 4), Pair.of(4, 5),
                Pair.of(5, 6), Pair.of(7, 6), Pair.of(7, 7));

        Graph<Integer, Integer> g = graphSupplier.apply(vertexCount, edges);

        assertEquals(vertexCount.intValue(), g.vertexSet().size());
        assertEquals(edges.size(), g.edgeSet().size());

        assertEquals(
            IntStream.range(0, edges.size()).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.edgeSet());
        assertEquals(
            IntStream.range(0, vertexCount).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.vertexSet());

        for (int i = 0; i < vertexCount; i++) {
            assertTrue(g.containsVertex(i));
        }

        assertEquals(1, g.outDegreeOf(0));
        assertEquals(new HashSet<>(Arrays.asList(0)), g.outgoingEdgesOf(0));

        assertEquals(4, g.outDegreeOf(1));
        assertEquals(new HashSet<>(Arrays.asList(1, 2, 3, 4)), g.outgoingEdgesOf(1));

        assertEquals(3, g.outDegreeOf(2));
        assertEquals(new HashSet<>(Arrays.asList(5, 6, 7)), g.outgoingEdgesOf(2));

        assertEquals(1, g.outDegreeOf(3));
        assertEquals(new HashSet<>(Arrays.asList(8)), g.outgoingEdgesOf(3));

        assertEquals(1, g.outDegreeOf(4));
        assertEquals(new HashSet<>(Arrays.asList(9)), g.outgoingEdgesOf(4));

        assertEquals(1, g.outDegreeOf(5));
        assertEquals(new HashSet<>(Arrays.asList(10)), g.outgoingEdgesOf(5));

        assertEquals(0, g.outDegreeOf(6));
        assertEquals(new HashSet<>(), g.outgoingEdgesOf(6));

        assertEquals(2, g.outDegreeOf(7));
        assertEquals(new HashSet<>(Arrays.asList(11, 12)), g.outgoingEdgesOf(7));

        assertEquals(Integer.valueOf(0), g.getEdgeSource(0));
        assertEquals(Integer.valueOf(1), g.getEdgeTarget(0));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(1));
        assertEquals(Integer.valueOf(0), g.getEdgeTarget(1));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(2));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(2));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(3));
        assertEquals(Integer.valueOf(5), g.getEdgeTarget(3));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(4));
        assertEquals(Integer.valueOf(6), g.getEdgeTarget(4));
        assertEquals(Integer.valueOf(2), g.getEdgeSource(5));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(5));
        assertEquals(Integer.valueOf(2), g.getEdgeSource(6));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(6));
        assertEquals(Integer.valueOf(2), g.getEdgeSource(7));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(7));
        assertEquals(Integer.valueOf(3), g.getEdgeSource(8));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(8));
        assertEquals(Integer.valueOf(4), g.getEdgeSource(9));
        assertEquals(Integer.valueOf(5), g.getEdgeTarget(9));
        assertEquals(Integer.valueOf(5), g.getEdgeSource(10));
        assertEquals(Integer.valueOf(6), g.getEdgeTarget(10));
        assertEquals(Integer.valueOf(7), g.getEdgeSource(11));
        assertEquals(Integer.valueOf(6), g.getEdgeTarget(11));
        assertEquals(Integer.valueOf(7), g.getEdgeSource(12));
        assertEquals(Integer.valueOf(7), g.getEdgeTarget(12));

        GraphType type = g.getType();
        assertTrue(type.isAllowingCycles());
        assertTrue(type.isAllowingMultipleEdges());
        assertTrue(type.isAllowingSelfLoops());
        assertTrue(type.isDirected());
        assertFalse(type.isModifiable());
        assertFalse(type.isUndirected());
        assertFalse(type.isMixed());
        assertFalse(type.isWeighted());

        assertEquals(Integer.valueOf(0), g.getEdge(0, 1));
        assertEquals(Collections.singleton(Integer.valueOf(0)), g.getAllEdges(0, 1));
        assertEquals(Integer.valueOf(1), g.getEdge(1, 0));
        assertEquals(Collections.singleton(Integer.valueOf(1)), g.getAllEdges(1, 0));
        assertEquals(Integer.valueOf(2), g.getEdge(1, 4));
        assertEquals(Collections.singleton(Integer.valueOf(2)), g.getAllEdges(1, 4));
        assertEquals(Integer.valueOf(3), g.getEdge(1, 5));
        assertEquals(Collections.singleton(Integer.valueOf(3)), g.getAllEdges(1, 5));
        assertEquals(Integer.valueOf(4), g.getEdge(1, 6));
        assertEquals(Collections.singleton(Integer.valueOf(4)), g.getAllEdges(1, 6));
        assertEquals(Integer.valueOf(5), g.getEdge(2, 4));
        assertEquals(new HashSet<>(Arrays.asList(5, 6, 7)), g.getAllEdges(2, 4));
        assertEquals(Integer.valueOf(8), g.getEdge(3, 4));
        assertEquals(Collections.singleton(Integer.valueOf(8)), g.getAllEdges(3, 4));
        assertEquals(Integer.valueOf(9), g.getEdge(4, 5));
        assertEquals(Collections.singleton(Integer.valueOf(9)), g.getAllEdges(4, 5));
        assertEquals(Integer.valueOf(10), g.getEdge(5, 6));
        assertEquals(Collections.singleton(Integer.valueOf(10)), g.getAllEdges(5, 6));
        assertEquals(Integer.valueOf(11), g.getEdge(7, 6));
        assertEquals(Collections.singleton(Integer.valueOf(11)), g.getAllEdges(7, 6));
        assertEquals(Integer.valueOf(12), g.getEdge(7, 7));
        assertEquals(Collections.singleton(Integer.valueOf(12)), g.getAllEdges(7, 7));

    }

    public static void testDirectedWeighted(
        BiFunction<Integer, List<Triple<Integer, Integer, Double>>,
            Graph<Integer, Integer>> graphSupplier)
    {
        List<Triple<Integer, Integer, Double>> edges = Arrays
            .asList(
                Triple.of(0, 1, 0d), Triple.of(1, 0, 1d), Triple.of(1, 4, 2d), Triple.of(1, 5, 3d),
                Triple.of(1, 6, 4d), Triple.of(2, 4, 5d), Triple.of(2, 4, 6d), Triple.of(2, 4, 7d),
                Triple.of(3, 4, 8d), Triple.of(4, 5, 9d), Triple.of(5, 6, 10d),
                Triple.of(7, 6, 11d), Triple.of(7, 7, 12d));

        int vertices = 8;

        Graph<Integer, Integer> g = graphSupplier.apply(vertices, edges);

        assertEquals(vertices, g.vertexSet().size());
        assertEquals(edges.size(), g.edgeSet().size());

        assertEquals(
            IntStream.range(0, edges.size()).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.edgeSet());
        assertEquals(
            IntStream.range(0, vertices).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.vertexSet());

        for (int i = 0; i < vertices; i++) {
            assertTrue(g.containsVertex(i));
        }

        assertEquals(2, g.degreeOf(0));
        assertEquals(1, g.inDegreeOf(0));
        assertEquals(1, g.outDegreeOf(0));
        assertEquals(Set.of(0, 1), g.edgesOf(0));
        assertEquals(Set.of(1), g.incomingEdgesOf(0));
        assertEquals(Set.of(0), g.outgoingEdgesOf(0));

        assertEquals(5, g.degreeOf(1));
        assertEquals(1, g.inDegreeOf(1));
        assertEquals(4, g.outDegreeOf(1));
        assertEquals(Set.of(0, 1, 2, 3, 4), g.edgesOf(1));
        assertEquals(Set.of(0), g.incomingEdgesOf(1));
        assertEquals(Set.of(1, 2, 3, 4), g.outgoingEdgesOf(1));

        assertEquals(3, g.degreeOf(2));
        assertEquals(0, g.inDegreeOf(2));
        assertEquals(3, g.outDegreeOf(2));
        assertEquals(Set.of(5, 6, 7), g.edgesOf(2));
        assertEquals(Set.of(), g.incomingEdgesOf(2));
        assertEquals(Set.of(5, 6, 7), g.outgoingEdgesOf(2));

        assertEquals(1, g.degreeOf(3));
        assertEquals(0, g.inDegreeOf(3));
        assertEquals(1, g.outDegreeOf(3));
        assertEquals(Set.of(8), g.edgesOf(3));
        assertEquals(Set.of(), g.incomingEdgesOf(3));
        assertEquals(Set.of(8), g.outgoingEdgesOf(3));

        assertEquals(6, g.degreeOf(4));
        assertEquals(5, g.inDegreeOf(4));
        assertEquals(1, g.outDegreeOf(4));
        assertEquals(Set.of(2, 5, 6, 7, 8, 9), g.edgesOf(4));
        assertEquals(Set.of(2, 5, 6, 7, 8), g.incomingEdgesOf(4));
        assertEquals(Set.of(9), g.outgoingEdgesOf(4));

        assertEquals(3, g.degreeOf(5));
        assertEquals(2, g.inDegreeOf(5));
        assertEquals(1, g.outDegreeOf(5));
        assertEquals(Set.of(3, 9, 10), g.edgesOf(5));
        assertEquals(Set.of(3, 9), g.incomingEdgesOf(5));
        assertEquals(Set.of(10), g.outgoingEdgesOf(5));

        assertEquals(3, g.degreeOf(6));
        assertEquals(3, g.inDegreeOf(6));
        assertEquals(0, g.outDegreeOf(6));
        assertEquals(Set.of(4, 10, 11), g.edgesOf(6));
        assertEquals(Set.of(4, 10, 11), g.incomingEdgesOf(6));
        assertEquals(Set.of(), g.outgoingEdgesOf(6));

        assertEquals(3, g.degreeOf(7));
        assertEquals(1, g.inDegreeOf(7));
        assertEquals(2, g.outDegreeOf(7));
        assertEquals(Set.of(11, 12), g.edgesOf(7));
        assertEquals(Set.of(12), g.incomingEdgesOf(7));
        assertEquals(Set.of(11, 12), g.outgoingEdgesOf(7));

        assertEquals(Integer.valueOf(0), g.getEdgeSource(0));
        assertEquals(Integer.valueOf(1), g.getEdgeTarget(0));
        assertEquals(0d, g.getEdgeWeight(0), 1e-16);
        assertEquals(Integer.valueOf(1), g.getEdgeSource(1));
        assertEquals(Integer.valueOf(0), g.getEdgeTarget(1));
        assertEquals(1d, g.getEdgeWeight(1), 1e-16);
        assertEquals(Integer.valueOf(1), g.getEdgeSource(2));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(2));
        assertEquals(2d, g.getEdgeWeight(2), 1e-16);
        assertEquals(Integer.valueOf(1), g.getEdgeSource(3));
        assertEquals(Integer.valueOf(5), g.getEdgeTarget(3));
        assertEquals(3d, g.getEdgeWeight(3), 1e-16);
        assertEquals(Integer.valueOf(1), g.getEdgeSource(4));
        assertEquals(Integer.valueOf(6), g.getEdgeTarget(4));
        assertEquals(4d, g.getEdgeWeight(4), 1e-16);
        assertEquals(Integer.valueOf(2), g.getEdgeSource(5));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(5));
        assertEquals(5d, g.getEdgeWeight(5), 1e-16);
        assertEquals(Integer.valueOf(2), g.getEdgeSource(6));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(6));
        assertEquals(6d, g.getEdgeWeight(6), 1e-16);
        assertEquals(Integer.valueOf(2), g.getEdgeSource(7));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(7));
        assertEquals(7d, g.getEdgeWeight(7), 1e-16);
        assertEquals(Integer.valueOf(3), g.getEdgeSource(8));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(8));
        assertEquals(8d, g.getEdgeWeight(8), 1e-16);
        assertEquals(Integer.valueOf(4), g.getEdgeSource(9));
        assertEquals(Integer.valueOf(5), g.getEdgeTarget(9));
        assertEquals(9d, g.getEdgeWeight(9), 1e-16);
        assertEquals(Integer.valueOf(5), g.getEdgeSource(10));
        assertEquals(Integer.valueOf(6), g.getEdgeTarget(10));
        assertEquals(10d, g.getEdgeWeight(10), 1e-16);
        assertEquals(Integer.valueOf(7), g.getEdgeSource(11));
        assertEquals(Integer.valueOf(6), g.getEdgeTarget(11));
        assertEquals(11d, g.getEdgeWeight(11), 1e-16);
        assertEquals(Integer.valueOf(7), g.getEdgeSource(12));
        assertEquals(Integer.valueOf(7), g.getEdgeTarget(12));
        assertEquals(12d, g.getEdgeWeight(12), 1e-16);

        for (int i = 0; i < edges.size(); i++) {
            assertEquals(Double.valueOf(i), g.getEdgeWeight(i), 1e-16);
            g.setEdgeWeight(i, 100 + g.getEdgeWeight(i));
            assertEquals(Double.valueOf(i) + 100, g.getEdgeWeight(i), 1e-16);
        }

        GraphType type = g.getType();
        assertTrue(type.isAllowingCycles());
        assertTrue(type.isAllowingMultipleEdges());
        assertTrue(type.isAllowingSelfLoops());
        assertTrue(type.isDirected());
        assertTrue(type.isWeighted());
        assertFalse(type.isModifiable());
        assertFalse(type.isUndirected());
        assertFalse(type.isMixed());

        assertEquals(Integer.valueOf(0), g.getEdge(0, 1));
        assertEquals(Set.of(0), g.getAllEdges(0, 1));
        assertEquals(Integer.valueOf(1), g.getEdge(1, 0));
        assertEquals(Set.of(1), g.getAllEdges(1, 0));
        assertEquals(Integer.valueOf(2), g.getEdge(1, 4));
        assertEquals(Set.of(2), g.getAllEdges(1, 4));
        assertEquals(Integer.valueOf(3), g.getEdge(1, 5));
        assertEquals(Set.of(3), g.getAllEdges(1, 5));
        assertEquals(Integer.valueOf(4), g.getEdge(1, 6));
        assertEquals(Set.of(4), g.getAllEdges(1, 6));
        assertEquals(Integer.valueOf(5), g.getEdge(2, 4));
        assertEquals(Set.of(5, 6, 7), g.getAllEdges(2, 4));
        assertEquals(Integer.valueOf(8), g.getEdge(3, 4));
        assertEquals(Set.of(8), g.getAllEdges(3, 4));
        assertEquals(Integer.valueOf(9), g.getEdge(4, 5));
        assertEquals(Set.of(9), g.getAllEdges(4, 5));
        assertEquals(Integer.valueOf(10), g.getEdge(5, 6));
        assertEquals(Set.of(10), g.getAllEdges(5, 6));
        assertEquals(Integer.valueOf(11), g.getEdge(7, 6));
        assertEquals(Set.of(11), g.getAllEdges(7, 6));
        assertEquals(Integer.valueOf(12), g.getEdge(7, 7));
        assertEquals(Set.of(12), g.getAllEdges(7, 7));

    }

}
