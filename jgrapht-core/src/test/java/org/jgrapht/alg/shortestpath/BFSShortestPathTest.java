/*
 * (C) Copyright 2018-2021, by Karri Sai Satish Kumar Reddy and Contributors.
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
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BFSShortestPathTest
{
    // ~ Static fields/initializers ---------------------------------------------

    static final String V1 = "v1";
    static final String V2 = "v2";
    static final String V3 = "v3";
    static final String V4 = "v4";
    static final String V5 = "v5";

    // ~ Instance fields --------------------------------------------------------

    DefaultEdge e12;
    DefaultEdge e13;
    DefaultEdge e35;
    DefaultEdge e24;
    DefaultEdge e45;

    protected Graph<String, DefaultEdge> create()
    {
        Graph<String, DefaultEdge> g;

        g = new DefaultDirectedGraph<>(DefaultEdge.class);

        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);

        e12 = Graphs.addEdgeWithVertices(g, V1, V2);

        e13 = Graphs.addEdgeWithVertices(g, V1, V3);

        e24 = Graphs.addEdgeWithVertices(g, V2, V4);

        e35 = Graphs.addEdgeWithVertices(g, V3, V5);

        e45 = Graphs.addEdgeWithVertices(g, V4, V5);

        return g;

    }

    @Test
    public void testPathBetween()
    {
        GraphPath<String, DefaultEdge> path;
        Graph<String, DefaultEdge> g = create();

        path = BFSShortestPath.findPathBetween(g, V1, V2);
        assertEquals(Arrays.asList(e12), path.getEdgeList());

        path = BFSShortestPath.findPathBetween(g, V1, V4);
        assertEquals(Arrays.asList(e12, e24), path.getEdgeList());

        path = BFSShortestPath.findPathBetween(g, V1, V5);
        assertEquals(Arrays.asList(e13, e35), path.getEdgeList());

        path = BFSShortestPath.findPathBetween(g, V4, V3);
        assertNull(path);
    }

    @Test
    public void testAllPaths()
    {
        List<DefaultEdge> path;
        Graph<String, DefaultEdge> g = create();

        SingleSourcePaths<String, DefaultEdge> tree = new BFSShortestPath<>(g).getPaths(V1);

        path = tree.getPath(V1).getEdgeList();
        assertEquals(Arrays.asList(), path);

        path = tree.getPath(V2).getEdgeList();
        assertEquals(Arrays.asList(e12), path);

        path = tree.getPath(V3).getEdgeList();
        assertEquals(Arrays.asList(e13), path);

        path = tree.getPath(V4).getEdgeList();
        assertEquals(Arrays.asList(e12, e24), path);

        path = tree.getPath(V5).getEdgeList();
        assertEquals(Arrays.asList(e13, e35), path);

    }

}
