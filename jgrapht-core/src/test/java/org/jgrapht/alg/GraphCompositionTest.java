/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
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
package org.jgrapht.alg;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import static org.jgrapht.alg.Composition.compose;

import junit.framework.*;
import org.junit.BeforeClass;

/**
 * .
 *
 * @author Barak Naveh
 */
public class GraphCompositionTest
    extends TestCase
{
    // ~ Instance fields --------------------------------------------------------

    //
    Graph<Integer, DefaultEdge> zero;
    Graph<Integer, DefaultEdge> id;
    Graph<Integer, DefaultEdge> first;
    Graph<Integer, DefaultEdge> second;
    Graph<Integer, DefaultEdge> second_first;

    // ~ Methods ----------------------------------------------------------------

    @BeforeClass
    public void setUp() {
        zero = new DefaultDirectedGraph<>(DefaultEdge.class);
        id = new DefaultDirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(id, 0, 0);
        Graphs.addEdgeWithVertices(id, 1, 1);
        Graphs.addEdgeWithVertices(id, 2, 2);
        first = new DefaultDirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(id, 0, 1);
        second = new DefaultDirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(id, 1, 2);
        second_first = new DefaultDirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(id, 0, 2);
    }

    /**
     * .
     */
    public void testComposition()
    {
        // checking id
        assertEquals(compose(zero, id), zero);
        assertEquals(compose(id, zero), zero);
        assertEquals(compose(first, id), first);
        assertEquals(compose(id, first), first);
        assertEquals(compose(second, id), second);
        assertEquals(compose(id, second), second);

        // checking zero
        assertEquals(compose(first, zero), zero);
        assertEquals(compose(zero, first), zero);
        assertEquals(compose(second, zero), zero);
        assertEquals(compose(zero, second), zero);

        assertEquals(compose(second, first), second_first);
    }

}

// End ConnectivityInspectorTest.java
