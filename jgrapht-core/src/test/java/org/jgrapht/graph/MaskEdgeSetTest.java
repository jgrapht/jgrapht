/*
 * (C) Copyright 2016-2018, by Andrew Gainer-Dewar and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.graph;

import org.jgrapht.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for MaskEdgeSet.
 *
 * @author Andrew Gainer-Dewar
 */
public class MaskEdgeSetTest
{
    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";
    private String v4 = "v4";
    private DefaultEdge e1, e2, e3, loop1, loop2;

    private MaskEdgeSet<String, DefaultEdge> testMaskedEdgeSet;

    @Before
    public void setUp()
    {
        Graph<String, DefaultEdge> directed = new DefaultDirectedGraph<>(DefaultEdge.class);

        directed.addVertex(v1);
        directed.addVertex(v2);
        directed.addVertex(v3);
        directed.addVertex(v4);

        e1 = directed.addEdge(v1, v2);
        e2 = directed.addEdge(v2, v3);
        e3 = directed.addEdge(v2, v4);

        loop1 = directed.addEdge(v1, v1);
        loop2 = directed.addEdge(v4, v4);

        testMaskedEdgeSet =
            new MaskEdgeSet<>(directed, directed.edgeSet(), v -> v == v1, e -> e == e2);
    }

    @Test
    public void testContains()
    {
        assertFalse(testMaskedEdgeSet.contains(e1));
        assertFalse(testMaskedEdgeSet.contains(e2));
        assertTrue(testMaskedEdgeSet.contains(e3));

        assertFalse(testMaskedEdgeSet.contains(loop1));
        assertTrue(testMaskedEdgeSet.contains(loop2));

        assertFalse(testMaskedEdgeSet.contains(v1));
    }

    @Test
    public void testSize()
    {
        assertEquals(2, testMaskedEdgeSet.size());
    }

    @Test
    public void testIterator()
    {
        Iterator<DefaultEdge> it = testMaskedEdgeSet.iterator();
        assertTrue(it.hasNext());
        assertEquals(e3, it.next());
        assertTrue(it.hasNext());
        assertEquals(loop2, it.next());
        assertFalse(it.hasNext());
    }
}
