/*
 * (C) Copyright 2017-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.stream.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for GraphMeasurer
 *
 * @author Joris Kinable
 * @author Alexandru Valeanu
 */
public class GraphMeasurerTest
{

    private final double EPSILON = 0.000000001;

    private Graph<Integer, DefaultEdge> getGraph1()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        IntStream.range(0, 7).forEach(i -> g.addVertex());
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 5);
        g.addEdge(3, 4);
        g.addEdge(5, 6);
        return g;
    }

    private Graph<Integer, DefaultEdge> getGraph2()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        IntStream.range(0, 7).forEach(i -> g.addVertex());
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 4);
        g.addEdge(1, 5);
        g.addEdge(5, 6);
        return g;
    }

    private Graph<Integer, DefaultEdge> getGraph3()
    {
        Graph<Integer, DefaultEdge> g = new SimpleWeightedGraph<>(DefaultEdge.class);

        Random random = new Random(12345678);

        final int N = 100;
        final int M = 100000;

        for (int i = 0; i < N; i++) {
            g.addVertex(i);
        }

        for (int i = 0; i < N - 1; i++) {
            g.addEdge(i, i + 1);
            g.setEdgeWeight(g.getEdge(i, i + 1), 50 + random.nextInt(50));
        }

        for (int i = N - 1; i < M; i++) {
            int u = random.nextInt(N);
            int v = random.nextInt(N);

            if (u != v) {
                g.addEdge(u, v);
                g.setEdgeWeight(g.getEdge(u, v), 100 + random.nextInt(200));
            }

        }

        return g;
    }

    @Test
    public void testVertexEccentricityG1()
    {
        Graph<Integer, DefaultEdge> g1 = getGraph1();
        List<ShortestPathAlgorithm<Integer, DefaultEdge>> spAlgs =
            Arrays.asList(new FloydWarshallShortestPaths<>(g1), new JohnsonShortestPaths<>(g1));
        for (ShortestPathAlgorithm<Integer, DefaultEdge> spAlg : spAlgs) {
            GraphMeasurer<Integer, DefaultEdge> gdm = new GraphMeasurer<>(g1, spAlg);
            Map<Integer, Double> vertexEccentricity = gdm.getVertexEccentricityMap();
            assertEquals(3.0, vertexEccentricity.get(0), EPSILON);
            assertEquals(2.0, vertexEccentricity.get(1), EPSILON);
            assertEquals(3.0, vertexEccentricity.get(2), EPSILON);
            assertEquals(3.0, vertexEccentricity.get(3), EPSILON);
            assertEquals(4.0, vertexEccentricity.get(4), EPSILON);
            assertEquals(3.0, vertexEccentricity.get(5), EPSILON);
            assertEquals(4.0, vertexEccentricity.get(6), EPSILON);
        }
    }

    @Test
    public void testVertexEccentricityG2()
    {
        Graph<Integer, DefaultEdge> g2 = getGraph2();
        List<ShortestPathAlgorithm<Integer, DefaultEdge>> spAlgs =
            Arrays.asList(new FloydWarshallShortestPaths<>(g2), new JohnsonShortestPaths<>(g2));
        for (ShortestPathAlgorithm<Integer, DefaultEdge> spAlg : spAlgs) {
            GraphMeasurer<Integer, DefaultEdge> gdm = new GraphMeasurer<>(g2, spAlg);
            Map<Integer, Double> vertexEccentricity = gdm.getVertexEccentricityMap();
            assertEquals(3.0, vertexEccentricity.get(0), EPSILON);
            assertEquals(2.0, vertexEccentricity.get(1), EPSILON);
            assertEquals(3.0, vertexEccentricity.get(2), EPSILON);
            assertEquals(3.0, vertexEccentricity.get(3), EPSILON);
            assertEquals(3.0, vertexEccentricity.get(4), EPSILON);
            assertEquals(2.0, vertexEccentricity.get(5), EPSILON);
            assertEquals(3.0, vertexEccentricity.get(6), EPSILON);
        }
    }

    @Test
    public void testDiameterEmptyGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        GraphMeasurer<Integer, DefaultEdge> gdm = new GraphMeasurer<>(g);
        double diameter = gdm.getDiameter();
        assertEquals(0.0, diameter, EPSILON);
    }

    @Test
    public void testDiameterDisconnectedGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1));
        GraphMeasurer<Integer, DefaultEdge> gdm = new GraphMeasurer<>(g);
        double diameter = gdm.getDiameter();
        assertTrue(Double.isInfinite(diameter));
    }

    @Test
    public void testDiameterDirectedGraph1()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(g, 0, 1, 10);
        GraphMeasurer<Integer, DefaultWeightedEdge> gdm = new GraphMeasurer<>(g);
        double diameter = gdm.getDiameter();
        assertTrue(Double.isInfinite(diameter));
    }

    @Test
    public void testDiameterDirectedGraph2()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(g, 0, 1, 10);
        Graphs.addEdgeWithVertices(g, 1, 0, 12);
        GraphMeasurer<Integer, DefaultWeightedEdge> gdm = new GraphMeasurer<>(g);
        double diameter = gdm.getDiameter();
        assertEquals(12.0, diameter, EPSILON);
    }

    @Test
    public void testRadiusEmptyGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        GraphMeasurer<Integer, DefaultEdge> gdm = new GraphMeasurer<>(g);
        double radius = gdm.getRadius();
        assertEquals(0.0, radius, EPSILON);
    }

    @Test
    public void testRadiusDisconnectedGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1));
        GraphMeasurer<Integer, DefaultEdge> gdm = new GraphMeasurer<>(g);
        double radius = gdm.getRadius();
        assertTrue(Double.isInfinite(radius));
    }

    @Test
    public void testGraphCenterG1()
    {
        Graph<Integer, DefaultEdge> g1 = getGraph1();
        GraphMeasurer<Integer, DefaultEdge> gdm = new GraphMeasurer<>(g1);
        Set<Integer> graphCenter1 = gdm.getGraphCenter();
        assertEquals(new HashSet<>(Collections.singletonList(1)), graphCenter1);
    }

    @Test
    public void testGraphCenterG2()
    {
        Graph<Integer, DefaultEdge> g2 = getGraph2();
        GraphMeasurer<Integer, DefaultEdge> gdm = new GraphMeasurer<>(g2);
        Set<Integer> graphCenter2 = gdm.getGraphCenter();
        assertEquals(new HashSet<>(Arrays.asList(1, 5)), graphCenter2);
    }

    @Test
    public void testGraphPeripheryG1()
    {
        Graph<Integer, DefaultEdge> g1 = getGraph1();
        GraphMeasurer<Integer, DefaultEdge> gdm = new GraphMeasurer<>(g1);
        Set<Integer> graphPeriphery1 = gdm.getGraphPeriphery();
        assertEquals(new HashSet<>(Arrays.asList(4, 6)), graphPeriphery1);
    }

    @Test
    public void testGraphPeripheryG2()
    {
        Graph<Integer, DefaultEdge> g2 = getGraph2();
        GraphMeasurer<Integer, DefaultEdge> gdm = new GraphMeasurer<>(g2);
        Set<Integer> graphPeriphery2 = gdm.getGraphPeriphery();
        assertEquals(new HashSet<>(Arrays.asList(0, 2, 3, 4, 6)), graphPeriphery2);
    }

    @Test
    public void testGraphPseudoPeripheryG1()
    {
        Graph<Integer, DefaultEdge> g1 = getGraph1();
        GraphMeasurer<Integer, DefaultEdge> gdm = new GraphMeasurer<>(g1);
        Set<Integer> graphPseudoPeriphery1 = gdm.getGraphPseudoPeriphery();
        assertEquals(new HashSet<>(Arrays.asList(4, 6)), graphPseudoPeriphery1);
    }

    @Test
    public void testGraphPseudoPeripheryG2()
    {
        Graph<Integer, DefaultEdge> g2 = getGraph2();
        GraphMeasurer<Integer, DefaultEdge> gdm = new GraphMeasurer<>(g2);
        Set<Integer> graphPseudoPeriphery2 = gdm.getGraphPseudoPeriphery();
        assertEquals(new HashSet<>(Arrays.asList(0, 2, 3, 4, 6)), graphPseudoPeriphery2);
    }

    @Test
    public void testGraphPseudoPeripheryG3()
    {
        Graph<Integer, DefaultEdge> g3 = getGraph3();
        GraphMeasurer<Integer, DefaultEdge> gdm = new GraphMeasurer<>(g3);
        Set<Integer> graphPseudoPeriphery3 = gdm.getGraphPseudoPeriphery();
        assertEquals(
            new HashSet<>(
                Arrays.asList(
                    6, 7, 13, 17, 19, 20, 21, 24, 32, 36, 37, 39, 41, 42, 46, 48, 51, 53, 60, 61,
                    63, 64, 66, 67, 69, 70, 71, 83, 89, 90, 95, 98)),
            graphPseudoPeriphery3);
    }
}
