/*
 * (C) Copyright 2018-2018, by CAE Tech Limited and Contributors.
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
package org.jgrapht.alg.util;

import java.util.Arrays;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Test for BipartiteInspector, based on
 * MaximumCardinailityBipartiteMatchingTest
 *
 * @author Peter Harman
 * @author Joris Kinable
 */
public class BipartiteInspectorTest {

    @Test
    public void testBipartiteTrue1() {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<Integer> partition1 = Arrays.asList(0, 1, 2, 3);
        List<Integer> partition2 = Arrays.asList(4, 5, 6, 7);
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);

        DefaultEdge e02 = graph.addEdge(partition1.get(0), partition2.get(2));
        DefaultEdge e11 = graph.addEdge(partition1.get(1), partition2.get(1));
        DefaultEdge e20 = graph.addEdge(partition1.get(2), partition2.get(0));

        BipartiteInspector<Integer, DefaultEdge> bi = new BipartiteInspector<>(graph, partition1, partition2);
        assertTrue(bi.isBipartite());
    }

    @Test
    public void testBipartiteTrue2() {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<Integer> partition1 = Arrays.asList(0, 1, 2, 3, 4, 5);
        List<Integer> partition2 = Arrays.asList(6, 7, 8, 9, 10, 11);
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);

        DefaultEdge e00 = graph.addEdge(partition1.get(0), partition2.get(0));
        DefaultEdge e13 = graph.addEdge(partition1.get(1), partition2.get(3));
        DefaultEdge e21 = graph.addEdge(partition1.get(2), partition2.get(1));
        DefaultEdge e34 = graph.addEdge(partition1.get(3), partition2.get(4));
        DefaultEdge e42 = graph.addEdge(partition1.get(4), partition2.get(2));
        DefaultEdge e55 = graph.addEdge(partition1.get(5), partition2.get(5));

        BipartiteInspector<Integer, DefaultEdge> bi = new BipartiteInspector<>(graph, partition1, partition2);
        assertTrue(bi.isBipartite());
    }

    @Test
    public void testBipartiteFalse1() {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<Integer> partition1 = Arrays.asList(0, 1, 2, 3);
        List<Integer> partition2 = Arrays.asList(4, 5, 6, 7);
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);

        DefaultEdge e02 = graph.addEdge(partition1.get(0), partition1.get(2));
        DefaultEdge e11 = graph.addEdge(partition1.get(1), partition2.get(1));
        DefaultEdge e20 = graph.addEdge(partition1.get(2), partition2.get(0));

        BipartiteInspector<Integer, DefaultEdge> bi = new BipartiteInspector<>(graph, partition1, partition2);
        assertFalse(bi.isBipartite());
    }

    @Test
    public void testBipartiteFalse2() {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<Integer> partition1 = Arrays.asList(0, 1, 2, 3, 4, 5);
        List<Integer> partition2 = Arrays.asList(6, 7, 8, 9, 10, 11);
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);

        DefaultEdge e00 = graph.addEdge(partition1.get(0), partition2.get(0));
        DefaultEdge e13 = graph.addEdge(partition1.get(1), partition2.get(3));
        DefaultEdge e21 = graph.addEdge(partition1.get(2), partition1.get(1));
        DefaultEdge e34 = graph.addEdge(partition2.get(3), partition2.get(4));
        DefaultEdge e42 = graph.addEdge(partition1.get(4), partition2.get(2));
        DefaultEdge e55 = graph.addEdge(partition1.get(5), partition2.get(5));

        BipartiteInspector<Integer, DefaultEdge> bi = new BipartiteInspector<>(graph, partition1, partition2);
        assertFalse(bi.isBipartite());
    }

    @Test
    public void testBipartiteFalse3() {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<Integer> partition1 = Arrays.asList(0, 1, 2, 3);
        List<Integer> partition2 = Arrays.asList(4, 5, 6, 7);
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);
        partition2 = Arrays.asList(5, 6, 7);

        DefaultEdge e02 = graph.addEdge(partition1.get(0), partition2.get(2));
        DefaultEdge e11 = graph.addEdge(partition1.get(1), partition2.get(1));
        DefaultEdge e20 = graph.addEdge(partition1.get(2), partition2.get(0));

        BipartiteInspector<Integer, DefaultEdge> bi = new BipartiteInspector<>(graph, partition1, partition2);
        assertFalse(bi.isBipartite());
    }

    @Test
    public void testBipartiteFalse4() {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<Integer> partition1 = Arrays.asList(0, 1, 2, 3, 4, 5);
        List<Integer> partition2 = Arrays.asList(6, 7, 8, 9, 10, 11);
        Graphs.addAllVertices(graph, partition1);
        Graphs.addAllVertices(graph, partition2);
        partition2 = Arrays.asList(6, 7, 8, 9, 10, 11, 12);

        DefaultEdge e00 = graph.addEdge(partition1.get(0), partition2.get(0));
        DefaultEdge e13 = graph.addEdge(partition1.get(1), partition2.get(3));
        DefaultEdge e21 = graph.addEdge(partition1.get(2), partition2.get(1));
        DefaultEdge e34 = graph.addEdge(partition1.get(3), partition2.get(4));
        DefaultEdge e42 = graph.addEdge(partition1.get(4), partition2.get(2));
        DefaultEdge e55 = graph.addEdge(partition1.get(5), partition2.get(5));

        BipartiteInspector<Integer, DefaultEdge> bi = new BipartiteInspector<>(graph, partition1, partition2);
        assertFalse(bi.isBipartite());
    }
}
