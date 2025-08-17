/*
 * (C) Copyright 2024-2024, by Sung Ho Yoon and Contributors.
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
package org.jgrapht.graph;

import static org.junit.jupiter.api.Assertions.*;

import org.jgrapht.*;
import org.junit.jupiter.api.*;

@DisplayName("Unmodifiable graph view tests")
public class AsUnmodifiableGraphTest {

    private DefaultWeightedEdge loop;
    private DefaultWeightedEdge e12;
    private DefaultWeightedEdge e23;
    private DefaultWeightedEdge e24;
    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";
    private String v4 = "v4";
    private Graph<String, DefaultWeightedEdge> baseGraph;
    private Graph<String, DefaultWeightedEdge> unmodifiableGraph;

    @BeforeEach
    public void setUp()
    {
        this.baseGraph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        baseGraph.addVertex(v1);
        baseGraph.addVertex(v2);
        baseGraph.addVertex(v3);
        baseGraph.addVertex(v4);
        e12 = Graphs.addEdge(baseGraph, v1, v2, 6d);
        e23 = Graphs.addEdge(baseGraph, v2, v3, 456d);
        e24 = Graphs.addEdge(baseGraph, v2, v4, 0.587d);
        loop = Graphs.addEdge(baseGraph, v4, v4, 6781234453486d);

        this.unmodifiableGraph = new AsUnmodifiableGraph<>(this.baseGraph);
    }

    @DisplayName("Test null graph")
    @Test
    public void testNullGraph() {
        assertThrows(NullPointerException.class, () -> new AsUnmodifiableGraph<>(null));
    }

    @DisplayName("Test edge addition")
    @Test
    public void testAddEdge()
    {
        assertThrows(UnsupportedOperationException.class, () -> unmodifiableGraph.addEdge(v3, v4));
        assertThrows(UnsupportedOperationException.class, () -> {
            DefaultWeightedEdge e34 = new DefaultWeightedEdge();
            e34.source = v3;
            e34.target = v4;
            e34.weight = 2d;
            unmodifiableGraph.addEdge(v3, v4, e34);
        });
    }

    @DisplayName("Test vertex addition")
    @Test
    public void testAddVertex() {
        assertThrows(UnsupportedOperationException.class, () -> unmodifiableGraph.addVertex());
        assertThrows(UnsupportedOperationException.class, () -> unmodifiableGraph.addVertex("v5"));
    }

    @DisplayName("Test edge weight modification")
    @Test
    public void testSetEdgeWeight() {
        assertThrows(UnsupportedOperationException.class, () -> unmodifiableGraph.setEdgeWeight(v1, v2, 0d));
        assertEquals(6d, baseGraph.getEdgeWeight(e12));
    }
}
