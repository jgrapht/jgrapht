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
 * Unit tests for MaskVertexSet.
 *
 * @author Andrew Gainer-Dewar
 */
public class MaskVertexSetTest
{
    private Graph<String, DefaultEdge> directed;
    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";
    private String v4 = "v4";
    private DefaultEdge e1;

    private MaskVertexSet<String> testMaskVertexSet;

    @Before
    public void setUp()
    {
        directed = new DefaultDirectedGraph<>(DefaultEdge.class);

        directed.addVertex(v1);
        directed.addVertex(v2);
        directed.addVertex(v3);
        directed.addVertex(v4);

        e1 = directed.addEdge(v1, v2);
        directed.addEdge(v2, v3);

        testMaskVertexSet = new MaskVertexSet<>(directed.vertexSet(), v -> v == v1);
    }

    @Test
    public void testContains()
    {
        assertFalse(testMaskVertexSet.contains(v1));
        assertTrue(testMaskVertexSet.contains(v2));

        assertFalse(testMaskVertexSet.contains(e1));
    }

    @Test
    public void testSize()
    {
        assertEquals(3, testMaskVertexSet.size());
    }

    @Test
    public void testIterator()
    {
        Iterator<String> it = testMaskVertexSet.iterator();
        assertTrue(it.hasNext());
        assertEquals(v2, it.next());
        assertTrue(it.hasNext());
        assertEquals(v3, it.next());
        assertTrue(it.hasNext());
        assertEquals(v4, it.next());
        assertFalse(it.hasNext());
    }
}
