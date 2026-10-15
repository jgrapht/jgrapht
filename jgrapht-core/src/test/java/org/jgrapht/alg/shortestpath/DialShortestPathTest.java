/*
 * (C) Copyright 2019-2023, by Semen Chudakov and Contributors.
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

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DialShortestPathTest {

    @Test
    public void directed_tinyGraph_matchesDijkstra() {
        var g = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList("A","B","C","D"));
        setW(g, "A","B", 2);
        setW(g, "A","C", 5);
        setW(g, "B","C", 1);
        setW(g, "B","D", 4);
        setW(g, "C","D", 1);

        var dial = new DialShortestPath<String, DefaultWeightedEdge>(g, 5);
        var dijk = new DijkstraShortestPath<String, DefaultWeightedEdge>(g);

        GraphPath<String, DefaultWeightedEdge> pDial = dial.getPath("A","D");
        GraphPath<String, DefaultWeightedEdge> pDijk = dijk.getPath("A","D");

        assertNotNull(pDial);
        assertNotNull(pDijk);
        assertEquals(pDijk.getWeight(), pDial.getWeight(), 1e-9);
        assertEquals(pDijk.getVertexList(), pDial.getVertexList());
    }

    @Test
    public void undirected_matchesDijkstra() {
        var g = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1,2,3,4));
        setW(g, 1,2, 3);
        setW(g, 2,3, 2);
        setW(g, 1,3, 10);
        setW(g, 3,4, 1);

        var dial = new DialShortestPath<Integer, DefaultWeightedEdge>(g, 10);
        var dijk = new DijkstraShortestPath<Integer, DefaultWeightedEdge>(g);

        var pDial = dial.getPath(1,4);
        var pDijk = dijk.getPath(1,4);

        assertNotNull(pDial);
        assertEquals(pDijk.getWeight(), pDial.getWeight(), 1e-9);
        assertEquals(pDijk.getVertexList(), pDial.getVertexList());
    }

    @Test
    public void zeroWeightEdges_supported() {
        var g = new SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1,2,3));
        setW(g, 1,2, 0);
        setW(g, 2,3, 0);

        var dial = new DialShortestPath<Integer, DefaultWeightedEdge>(g, 0);
        var path = dial.getPath(1,3);

        assertNotNull(path);
        assertEquals(0.0, path.getWeight(), 1e-9);
        assertEquals(List.of(1,2,3), path.getVertexList());
    }

    @Test
    public void disconnected_returnsInfinityAndNullPath() {
        var g = new SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1,2,3));
        setW(g, 1,2, 1); // no path from 1 to 3

        var dial = new DialShortestPath<Integer, DefaultWeightedEdge>(g, 5);
        var paths = dial.getPaths(1);

        // SingleSourcePaths in this JGraphT version exposes getWeight(sink)
        assertTrue(Double.isInfinite(paths.getWeight(3)));
        assertNull(paths.getPath(3));
    }

    @Test
    public void negativeWeight_throws() {
        var g = new SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1,2));
        setW(g, 1,2, -1);
        assertThrows(IllegalArgumentException.class,
                () -> new DialShortestPath<Integer, DefaultWeightedEdge>(g, 10).getPaths(1));
    }

    @Test
    public void nonIntegerWeight_throws() {
        var g = new SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1,2));
        var e = g.addEdge(1,2);
        g.setEdgeWeight(e, 1.5);
        assertThrows(IllegalArgumentException.class,
                () -> new DialShortestPath<Integer, DefaultWeightedEdge>(g, 10).getPaths(1));
    }

    // ---------- helpers ----------

    private static void setW(SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> g, String u, String v, double w) {
        var e = g.addEdge(u, v);
        g.setEdgeWeight(e, w);
    }

    private static void setW(SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> g, Integer u, Integer v, double w) {
        var e = g.addEdge(u, v);
        g.setEdgeWeight(e, w);
    }

    private static void setW(SimpleWeightedGraph<Integer, DefaultWeightedEdge> g, Integer u, Integer v, double w) {
        var e = g.addEdge(u, v);
        g.setEdgeWeight(e, w);
    }
}
