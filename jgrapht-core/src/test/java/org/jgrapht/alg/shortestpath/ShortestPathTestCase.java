/*
 * (C) Copyright 2003-2018, by John V Sichi and Contributors.
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
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * .
 *
 * @author John V. Sichi
 */
public abstract class ShortestPathTestCase
{
    // ~ Static fields/initializers ---------------------------------------------

    static final String V1 = "v1";
    static final String V2 = "v2";
    static final String V3 = "v3";
    static final String V4 = "v4";
    static final String V5 = "v5";

    // ~ Instance fields --------------------------------------------------------

    DefaultWeightedEdge e12;
    DefaultWeightedEdge e13;
    DefaultWeightedEdge e15;
    DefaultWeightedEdge e24;
    DefaultWeightedEdge e34;
    DefaultWeightedEdge e45;

    // ~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    @Test
    public void testPathBetween()
    {
        List<DefaultWeightedEdge> path;
        Graph<String, DefaultWeightedEdge> g = create();

        path = findPathBetween(g, V1, V2);
        assertEquals(Arrays.asList(new DefaultWeightedEdge[] { e12 }), path);

        path = findPathBetween(g, V1, V4);
        assertEquals(Arrays.asList(new DefaultWeightedEdge[] { e12, e24 }), path);

        path = findPathBetween(g, V1, V5);
        assertEquals(Arrays.asList(new DefaultWeightedEdge[] { e12, e24, e45 }), path);

        path = findPathBetween(g, V3, V4);
        assertEquals(Arrays.asList(new DefaultWeightedEdge[] { e13, e12, e24 }), path);
    }

    protected abstract List<DefaultWeightedEdge> findPathBetween(
        Graph<String, DefaultWeightedEdge> g, String src, String dest);

    protected Graph<String, DefaultWeightedEdge> create()
    {
        return createWithBias(false);
    }

    protected Graph<String, DefaultWeightedEdge> createWithBias(boolean negate)
    {
        Graph<String, DefaultWeightedEdge> g;
        double bias = 1;
        if (negate) {
            // negative-weight edges are being tested, so only a directed graph
            // makes sense
            g = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
            bias = -1;
        } else {
            // by default, use an undirected graph
            g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        }

        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);

        e12 = Graphs.addEdge(g, V1, V2, bias * 2);

        e13 = Graphs.addEdge(g, V1, V3, bias * 3);

        e24 = Graphs.addEdge(g, V2, V4, bias * 5);

        e34 = Graphs.addEdge(g, V3, V4, bias * 20);

        e45 = Graphs.addEdge(g, V4, V5, bias * 5);

        e15 = Graphs.addEdge(g, V1, V5, bias * 100);

        return g;
    }
}
