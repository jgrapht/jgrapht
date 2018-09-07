/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link TreeMeasurer}
 *
 * @author Alexandru Valeanu
 */
public class TreeMeasurerTest {

    @Test
    public void testNoCenters(){
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);

        TreeMeasurer<Integer, DefaultEdge> treeMeasurer = new TreeMeasurer<>(tree);

        assertEquals(new HashSet<>(), treeMeasurer.getGraphCenter());
    }

    @Test
    public void testTwoCenters(){
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);

        tree.addVertex(1);
        tree.addVertex(2);
        tree.addVertex(3);
        tree.addVertex(4);

        tree.addEdge(1, 2);
        tree.addEdge(2, 3);
        tree.addEdge(3, 4);

        TreeMeasurer<Integer, DefaultEdge> treeMeasurer = new TreeMeasurer<>(tree);

        assertEquals(new HashSet<>(Arrays.asList(2, 3)), treeMeasurer.getGraphCenter());
    }

    @Test
    public void testOneCenter(){
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);

        tree.addVertex(1);
        tree.addVertex(2);
        tree.addVertex(3);
        tree.addVertex(4);
        tree.addVertex(5);

        tree.addEdge(1, 2);
        tree.addEdge(2, 3);
        tree.addEdge(3, 4);
        tree.addEdge(4, 5);

        TreeMeasurer<Integer, DefaultEdge> treeMeasurer = new TreeMeasurer<>(tree);

        assertEquals(new HashSet<>(Collections.singletonList(3)), treeMeasurer.getGraphCenter());
    }
}