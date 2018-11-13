/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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

import org.jgrapht.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit tests for {@link MaskSubgraph} class.
 *
 * @author Dimitrios Michail
 */
public class MaskSubgraphTest
{

    @Test
    public void testUnmodifiable()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        assertFalse(new MaskSubgraph<>(g, v -> v.equals(5), e -> false).getType().isModifiable());
    }

    @Test
    public void testInOutEdgesUndirected()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5));
        DefaultEdge e12 = g.addEdge(1, 2);
        DefaultEdge e13 = g.addEdge(1, 3);
        DefaultEdge e23 = g.addEdge(2, 3);
        DefaultEdge e24_1 = g.addEdge(2, 4);
        DefaultEdge e24_2 = g.addEdge(2, 4);
        DefaultEdge e24_3 = g.addEdge(2, 4);
        g.addEdge(3, 5);
        DefaultEdge e44 = g.addEdge(4, 4);
        g.addEdge(4, 5);

        Graph<Integer, DefaultEdge> sg =
            new MaskSubgraph<>(g, v -> v.equals(5), e -> e.equals(e24_3));

        assertEquals(6, sg.edgeSet().size());

        assertEquals(new HashSet<>(Arrays.asList(e12, e13)), sg.edgesOf(1));
        assertEquals(new HashSet<>(Arrays.asList(e12, e24_1, e24_2, e23)), sg.edgesOf(2));
        assertEquals(new HashSet<>(Arrays.asList(e13, e23)), sg.edgesOf(3));
        assertEquals(new HashSet<>(Arrays.asList(e24_1, e24_2, e44)), sg.edgesOf(4));

        assertEquals(2, sg.degreeOf(1));
        assertEquals(4, sg.degreeOf(2));
        assertEquals(2, sg.degreeOf(3));
        assertEquals(4, sg.degreeOf(4));

        assertEquals(new HashSet<>(Arrays.asList(e12, e13)), sg.incomingEdgesOf(1));
        assertEquals(new HashSet<>(Arrays.asList(e12, e24_1, e24_2, e23)), sg.incomingEdgesOf(2));
        assertEquals(new HashSet<>(Arrays.asList(e13, e23)), sg.incomingEdgesOf(3));
        assertEquals(new HashSet<>(Arrays.asList(e24_1, e24_2, e44)), sg.incomingEdgesOf(4));

        assertEquals(2, sg.inDegreeOf(1));
        assertEquals(4, sg.inDegreeOf(2));
        assertEquals(2, sg.inDegreeOf(3));
        assertEquals(4, sg.inDegreeOf(4));

        assertEquals(new HashSet<>(Arrays.asList(e12, e13)), sg.outgoingEdgesOf(1));
        assertEquals(new HashSet<>(Arrays.asList(e12, e24_1, e24_2, e23)), sg.outgoingEdgesOf(2));
        assertEquals(new HashSet<>(Arrays.asList(e13, e23)), sg.outgoingEdgesOf(3));
        assertEquals(new HashSet<>(Arrays.asList(e24_1, e24_2, e44)), sg.outgoingEdgesOf(4));

        assertEquals(2, sg.outDegreeOf(1));
        assertEquals(4, sg.outDegreeOf(2));
        assertEquals(2, sg.outDegreeOf(3));
        assertEquals(4, sg.outDegreeOf(4));
    }

    @Test
    public void testInOutEdgesDirected()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5));
        DefaultEdge e12 = g.addEdge(1, 2);
        DefaultEdge e13 = g.addEdge(1, 3);
        DefaultEdge e23 = g.addEdge(2, 3);
        DefaultEdge e24_1 = g.addEdge(2, 4);
        DefaultEdge e24_2 = g.addEdge(2, 4);
        DefaultEdge e24_3 = g.addEdge(2, 4);
        g.addEdge(3, 5);
        DefaultEdge e44 = g.addEdge(4, 4);
        g.addEdge(4, 5);

        Graph<Integer, DefaultEdge> sg =
            new MaskSubgraph<>(g, v -> v.equals(5), e -> e.equals(e24_3));

        assertEquals(6, sg.edgeSet().size());

        assertEquals(new HashSet<>(Arrays.asList(e12, e13)), sg.edgesOf(1));
        assertEquals(new HashSet<>(Arrays.asList(e12, e24_1, e24_2, e23)), sg.edgesOf(2));
        assertEquals(new HashSet<>(Arrays.asList(e13, e23)), sg.edgesOf(3));
        assertEquals(new HashSet<>(Arrays.asList(e24_1, e24_2, e44)), sg.edgesOf(4));

        assertEquals(2, sg.degreeOf(1));
        assertEquals(4, sg.degreeOf(2));
        assertEquals(2, sg.degreeOf(3));
        assertEquals(4, sg.degreeOf(4));

        assertEquals(new HashSet<>(), sg.incomingEdgesOf(1));
        assertEquals(new HashSet<>(Arrays.asList(e12)), sg.incomingEdgesOf(2));
        assertEquals(new HashSet<>(Arrays.asList(e13, e23)), sg.incomingEdgesOf(3));
        assertEquals(new HashSet<>(Arrays.asList(e24_1, e24_2, e44)), sg.incomingEdgesOf(4));

        assertEquals(0, sg.inDegreeOf(1));
        assertEquals(1, sg.inDegreeOf(2));
        assertEquals(2, sg.inDegreeOf(3));
        assertEquals(3, sg.inDegreeOf(4));

        assertEquals(new HashSet<>(Arrays.asList(e12, e13)), sg.outgoingEdgesOf(1));
        assertEquals(new HashSet<>(Arrays.asList(e24_1, e24_2, e23)), sg.outgoingEdgesOf(2));
        assertEquals(new HashSet<>(), sg.outgoingEdgesOf(3));
        assertEquals(new HashSet<>(Arrays.asList(e44)), sg.outgoingEdgesOf(4));

        assertEquals(2, sg.outDegreeOf(1));
        assertEquals(3, sg.outDegreeOf(2));
        assertEquals(0, sg.outDegreeOf(3));
        assertEquals(1, sg.outDegreeOf(4));
    }

}
