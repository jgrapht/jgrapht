/*
 * (C) Copyright 2011-2018, by Robby McKilliam and Contributors.
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
package org.jgrapht.alg;

import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Robby McKilliam
 */
public class StoerWagnerMinimumCutTest
{
    // ~ Instance fields --------------------------------------------------------

    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";
    private String v4 = "v4";
    private String v5 = "v5";
    private String v6 = "v6";
    private String v7 = "v7";
    private String v8 = "v8";

    /**
     * Test of mergeVertices method, of class StoerWagnerMinimumCut.
     */
    @Test
    public void testMinCut14()
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);

        DefaultWeightedEdge e;
        e = g.addEdge(v1, v2);
        g.setEdgeWeight(e, 3.0);
        e = g.addEdge(v1, v3);
        g.setEdgeWeight(e, 2.0);
        e = g.addEdge(v1, v4);
        g.setEdgeWeight(e, 4.0);
        e = g.addEdge(v2, v3);
        g.setEdgeWeight(e, 1.0);
        e = g.addEdge(v3, v4);
        g.setEdgeWeight(e, 1.0);

        StoerWagnerMinimumCut<String, DefaultWeightedEdge> mincut = new StoerWagnerMinimumCut<>(g);

        assertEquals(4.0, mincut.minCutWeight(), 0.000001);
    }

    /**
     * Test of mergeVertices method, of class StoerWagnerMinimumCut.
     */
    @Test
    public void testMinCutDisconnected()
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);

        DefaultWeightedEdge e;
        e = g.addEdge(v1, v2);
        g.setEdgeWeight(e, 3.0);
        e = g.addEdge(v1, v3);
        g.setEdgeWeight(e, 2.0);
        e = g.addEdge(v2, v3);
        g.setEdgeWeight(e, 1.0);

        StoerWagnerMinimumCut<String, DefaultWeightedEdge> mincut = new StoerWagnerMinimumCut<>(g);

        assertEquals(0.0, mincut.minCutWeight(), 0.000001);
    }

    /**
     * Test of StoerWagnerMinimumCut when a 0-weight edge exists.
     */
    @Test
    public void testMinCut0Weight()
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addVertex(v5);
        g.addVertex(v6);
        g.addVertex(v7);
        g.addVertex(v8);

        DefaultWeightedEdge e;
        e = g.addEdge(v1, v2);
        g.setEdgeWeight(e, 1.0);
        e = g.addEdge(v2, v3);
        g.setEdgeWeight(e, 2.0);
        e = g.addEdge(v3, v4);
        g.setEdgeWeight(e, 0.0);
        e = g.addEdge(v4, v5);
        g.setEdgeWeight(e, 1.0);
        e = g.addEdge(v5, v6);
        g.setEdgeWeight(e, 2.0);
        e = g.addEdge(v6, v1);
        g.setEdgeWeight(e, 0.0);
        e = g.addEdge(v6, v8);
        g.setEdgeWeight(e, 1.0);
        e = g.addEdge(v8, v7);
        g.setEdgeWeight(e, 0.0);
        e = g.addEdge(v7, v3);
        g.setEdgeWeight(e, 2.0);

        StoerWagnerMinimumCut<String, DefaultWeightedEdge> mincut = new StoerWagnerMinimumCut<>(g);

        Set<String> solution1 = new HashSet<>();
        Collections.addAll(solution1, v4, v5, v6, v8);
        Set<String> solution2 = new HashSet<>();
        Collections.addAll(solution2, v1, v2, v3, v7);

        assertEquals(0.0, mincut.minCutWeight(), 0.000001);
        assertTrue(mincut.minCut().equals(solution1) || mincut.minCut().equals(solution2));
    }

    /**
     * Test of StoerWagnerMinimumCut when a <1-weight edge exists.
     */
    @Test
    public void testMinCutSmallWeight()
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);

        DefaultWeightedEdge e;
        e = g.addEdge(v1, v2);
        g.setEdgeWeight(e, 0.5);
        e = g.addEdge(v2, v3);
        g.setEdgeWeight(e, 1.0);
        e = g.addEdge(v3, v4);
        g.setEdgeWeight(e, 0.5);
        e = g.addEdge(v4, v1);
        g.setEdgeWeight(e, 1.0);

        StoerWagnerMinimumCut<String, DefaultWeightedEdge> mincut = new StoerWagnerMinimumCut<>(g);

        Set<String> solution1 = new HashSet<>();
        Collections.addAll(solution1, v1, v4);
        Set<String> solution2 = new HashSet<>();
        Collections.addAll(solution2, v2, v3);

        assertEquals(1.0, mincut.minCutWeight(), 0.000001);
        assertTrue(mincut.minCut().equals(solution1) || mincut.minCut().equals(solution2));
    }

    /**
     * Test of StoerWagnerMinimumCut on a Multigraph.
     */
    @Test
    public void testMinCutMultigraph()
    {
        WeightedMultigraph<String, DefaultWeightedEdge> g =
            new WeightedMultigraph<>(DefaultWeightedEdge.class);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);

        DefaultWeightedEdge e;
        e = g.addEdge(v1, v2);
        g.setEdgeWeight(e, 1.5);
        e = g.addEdge(v1, v2);
        g.setEdgeWeight(e, 1.5);
        e = g.addEdge(v2, v3);
        g.setEdgeWeight(e, 2.0);

        StoerWagnerMinimumCut<String, DefaultWeightedEdge> mincut = new StoerWagnerMinimumCut<>(g);

        Set<String> solution1 = new HashSet<>();
        Collections.addAll(solution1, v1, v2);
        Set<String> solution2 = new HashSet<>();
        Collections.addAll(solution2, v3);

        assertEquals(2.0, mincut.minCutWeight(), 0.000001);
        assertTrue(mincut.minCut().equals(solution1) || mincut.minCut().equals(solution2));
    }

    /**
     * Test of StoerWagnerMinimumCut on an unweighted graph
     */
    @Test
    public void testMinCutUnweighted()
    {
        SimpleGraph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addVertex(v5);
        g.addVertex(v6);

        g.addEdge(v1, v2);
        g.addEdge(v2, v3);
        g.addEdge(v3, v1);
        g.addEdge(v4, v5);
        g.addEdge(v5, v6);
        g.addEdge(v6, v4);
        g.addEdge(v3, v4);

        StoerWagnerMinimumCut<String, DefaultEdge> mincut = new StoerWagnerMinimumCut<>(g);

        Set<String> solution1 = new HashSet<>();
        Collections.addAll(solution1, v1, v2, v3);
        Set<String> solution2 = new HashSet<>();
        Collections.addAll(solution2, v4, v5, v6);

        assertEquals(1.0, mincut.minCutWeight(), 0.000001);
        assertTrue(mincut.minCut().equals(solution1) || mincut.minCut().equals(solution2));
    }

    /**
     * Test of StoerWagnerMinimumCut on empty and small graphs
     */
    @Test
    public void testMinCutEmpty()
    {
        SimpleGraph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        boolean caught = false;
        // No vertices
        try {
            new StoerWagnerMinimumCut<>(g);
        } catch (IllegalArgumentException ex) {
            caught = true;
        }
        assertTrue(caught);
    }

    /**
     * Test of StoerWagnerMinimumCut on empty and small graphs
     */
    @Test
    public void testMinCutSingleton()
    {
        SimpleGraph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        boolean caught = false;
        // 1 vertex
        g.addVertex(v1);
        try {
            new StoerWagnerMinimumCut<>(g);
        } catch (IllegalArgumentException ex) {
            caught = true;
        }
        assertTrue(caught);
    }

    /**
     * Test of StoerWagnerMinimumCut on empty and small graphs
     */
    @Test
    public void testMinCutDoubleton()
    {
        SimpleGraph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        StoerWagnerMinimumCut<String, DefaultEdge> mincut;

        // 2 vertices, no edges
        g.addVertex(v1);
        g.addVertex(v2);
        mincut = new StoerWagnerMinimumCut<>(g);

        Set<String> solution1 = new HashSet<>();
        Collections.addAll(solution1, v1);
        Set<String> solution2 = new HashSet<>();
        Collections.addAll(solution2, v2);

        assertEquals(0.0, mincut.minCutWeight(), 0.000001);
        assertTrue(mincut.minCut().equals(solution1) || mincut.minCut().equals(solution2));
    }

    /**
     * Test of StoerWagnerMinimumCut on empty and small graphs
     */
    @Test
    public void testMinCutSmall()
    {
        SimpleGraph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        StoerWagnerMinimumCut<String, DefaultEdge> mincut;

        // 2 vertices, 1 edge
        g.addVertex(v1);
        g.addVertex(v2);
        g.addEdge(v1, v2);

        Set<String> solution1 = new HashSet<>();
        Collections.addAll(solution1, v1);
        Set<String> solution2 = new HashSet<>();
        Collections.addAll(solution2, v2);

        mincut = new StoerWagnerMinimumCut<>(g);

        assertEquals(1.0, mincut.minCutWeight(), 0.000001);
        assertTrue(mincut.minCut().equals(solution1) || mincut.minCut().equals(solution2));
    }
}
