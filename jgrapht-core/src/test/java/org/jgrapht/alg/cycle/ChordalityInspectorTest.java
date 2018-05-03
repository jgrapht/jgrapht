/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.generate.NamedGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.Pseudograph;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for the {@link ChordalityInspector}
 *
 * @author Timofey Chudakov
 */
@RunWith(Parameterized.class)
public class ChordalityInspectorTest {
    private ChordalityInspector.IterationOrder iterationOrder;

    public ChordalityInspectorTest(ChordalityInspector.IterationOrder iterationOrder) {
        this.iterationOrder = iterationOrder;
    }

    @Parameterized.Parameters
    public static Object[] params() {
        return new Object[]{ChordalityInspector.IterationOrder.MCS, ChordalityInspector.IterationOrder.LEX_BFS};
    }

    /**
     * Tests maximum clique finding on an empty graph.
     */
    @Test
    public void testGetMaximumClique1() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        Set<Integer> clique = inspector.getMaximumClique();
        assertNotNull(clique);
        assertEquals(0, clique.size());
    }

    /**
     * Tests maximum clique finding on a chordal graph
     */
    @Test
    public void testGetMaximumClique2() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 3, 5);
        Graphs.addEdgeWithVertices(graph, 3, 6);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 4, 6);
        Graphs.addEdgeWithVertices(graph, 5, 6);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        Set<Integer> clique = inspector.getMaximumClique();
        assertNotNull(clique);
        assertEquals(4, clique.size());
        assertIsClique(graph, clique);
    }

    /**
     * Tests maximum clique finding on a non-chordal graph
     */
    @Test
    public void testGetMaximumClique3() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 1, 4);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        Set<Integer> clique = inspector.getMaximumClique();
        assertNull(clique);
    }

    /**
     * Tests maximum clique finding on a pseudograph
     */
    @Test
    public void testGetMaximumClique4() {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 1);
        Graphs.addEdgeWithVertices(graph, 1, 1);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 2);
        Graphs.addEdgeWithVertices(graph, 2, 2);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        Set<Integer> clique = inspector.getMaximumClique();
        assertNotNull(clique);
        assertEquals(2, clique.size());
        assertIsClique(graph, clique);
    }


    /**
     * Tests finding of maximum independent set of an empty graph
     */
    @Test
    public void testGetMaximumIndependentSet1() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        Set<Integer> set = inspector.getMaximumIndependentSet();
        assertNotNull(set);
        assertEquals(0, set.size());
    }

    /**
     * Tests finding of maximum independent set on a clique.
     */
    @Test
    public void testGetMaximumIndependentSet2() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 1, 4);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 2, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        Set<Integer> set = inspector.getMaximumIndependentSet();
        assertNotNull(set);
        assertEquals(1, set.size());
    }

    /**
     * Tests finding of a maximum independent set on a non-chordal graph
     */
    @Test
    public void testGetMaximumIndependentSet3() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 2, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        Set<Integer> set = inspector.getMaximumIndependentSet();
        assertNull(set);
    }

    /**
     * Tests finding of a maximum independent set on a pseudograph
     */
    @Test
    public void testGetMaximumIndependentSet4() {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 1);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 3, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 4, 4);
        Graphs.addEdgeWithVertices(graph, 4, 4);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        Set<Integer> set = inspector.getMaximumIndependentSet();
        assertNotNull(set);
        assertEquals(2, set.size());
        assertIsIndependentSet(graph, set);
    }

    /**
     * Tests coloring of an empty graph
     */
    @Test
    public void testGetColoring1() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        VertexColoringAlgorithm.Coloring<Integer> coloring = inspector.getColoring();
        assertNotNull(coloring);
        assertEquals(0, coloring.getNumberColors());
        assertEquals(0, coloring.getColors().size());
        assertEquals(0, coloring.getColorClasses().size());
    }

    /**
     * Tests coloring on a small clique
     */
    @Test
    public void testGetColoring2() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        VertexColoringAlgorithm.Coloring<Integer> coloring = inspector.getColoring();
        assertNotNull(coloring);
        assertEquals(3, coloring.getNumberColors());
        assertIsColoring(graph, coloring);
    }

    /**
     * Tests coloring on a non-chordal graph.
     */
    @Test
    public void testGetColoring3() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 2, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        VertexColoringAlgorithm.Coloring<Integer> coloring = inspector.getColoring();
        assertNull(coloring);
    }

    /**
     * Tests coloring of the big graph
     */
    @Test
    public void testGetColoring4() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 5, 6);
        Graphs.addEdgeWithVertices(graph, 6, 7);
        Graphs.addEdgeWithVertices(graph, 7, 8);
        Graphs.addEdgeWithVertices(graph, 8, 9);
        Graphs.addEdgeWithVertices(graph, 9, 10);
        Graphs.addEdgeWithVertices(graph, 10, 1);
        Graphs.addEdgeWithVertices(graph, 2, 4);
        Graphs.addEdgeWithVertices(graph, 4, 6);
        Graphs.addEdgeWithVertices(graph, 6, 8);
        Graphs.addEdgeWithVertices(graph, 8, 10);
        Graphs.addEdgeWithVertices(graph, 10, 2);
        Graphs.addEdgeWithVertices(graph, 2, 6);
        Graphs.addEdgeWithVertices(graph, 2, 8);
        Graphs.addEdgeWithVertices(graph, 4, 8);
        Graphs.addEdgeWithVertices(graph, 4, 10);
        Graphs.addEdgeWithVertices(graph, 6, 10);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        VertexColoringAlgorithm.Coloring<Integer> coloring = inspector.getColoring();
        assertNotNull(coloring);
        assertIsColoring(graph, coloring);
        assertEquals(5, coloring.getNumberColors());
    }

    /**
     * Tests coloring of a pseudograph
     */
    @Test
    public void testGetColoring5() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 1);
        Graphs.addEdgeWithVertices(graph, 2, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 2, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 4, 4);
        Graphs.addEdgeWithVertices(graph, 4, 4);
        Graphs.addEdgeWithVertices(graph, 5, 5);
        Graphs.addEdgeWithVertices(graph, 5, 5);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        VertexColoringAlgorithm.Coloring<Integer> coloring = inspector.getColoring();
        assertNotNull(coloring);
        assertIsColoring(graph, coloring);
        assertEquals(3, coloring.getNumberColors());
    }

    /**
     * Tests usage of the correct iteration order
     */
    @Test
    public void testIterationOrder() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph, iterationOrder);
        assertEquals(iterationOrder, inspector.getIterationOrder());
    }

    /**
     * Test on the big cycle
     */
    @Test
    public void testGetChordlessCycle() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        int upperBound = 100;
        for (int i = 0; i < upperBound; i++) {
            Graphs.addEdgeWithVertices(graph, i, i + 1);
        }
        Graphs.addEdgeWithVertices(graph, 0, upperBound);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        GraphPath<Integer, DefaultEdge> path = inspector.getHole();
        assertNotNull(path);
        assertIsHole(graph, path);
    }

    /**
     * Tests whether repeated calls to the {@link ChordalityInspector#getPerfectEliminationOrder()} return
     * the same vertex order.
     */
    @Test
    public void testPerfectEliminationOrder() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 2, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph, iterationOrder);
        List<Integer> order1 = inspector.getPerfectEliminationOrder();
        assertNull(inspector.getHole());
        graph.removeVertex(1);
        List<Integer> order2 = inspector.getPerfectEliminationOrder();
        assertEquals(order1, order2);
        assertNull(inspector.getHole());
    }

    /**
     * Tests whether returned list is unmodifiable
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableList() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        List<Integer> perfectEliminationOrder = inspector.getPerfectEliminationOrder();
        perfectEliminationOrder.add(0);
    }

    /**
     * Test chordality inspection of an empty graph
     */
    @Test
    public void testIsChordal1() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        assertTrue(inspector.isChordal());
        List<Integer> perfectEliminationOrder = inspector.getPerfectEliminationOrder();
        assertNotNull(perfectEliminationOrder);
    }

    /**
     * Test on chordal graph with 4 vertices:<br>
     * 1--2 <br>
     * | \| <br>
     * 3--4 <br>
     */
    @Test
    public void testIsChordal2() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 2, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        assertTrue(inspector.isChordal());
        assertNull(inspector.getHole());
        assertNotNull(inspector.getPerfectEliminationOrder());
    }

    /**
     * Test on chordal graph with two connected components: <br>
     * 1-2-3-1  and 4-5-6-4<br>
     */
    @Test
    public void testIsChordal3() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 1);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 5, 6);
        Graphs.addEdgeWithVertices(graph, 6, 4);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        assertTrue(inspector.isChordal());
        assertNull(inspector.getHole());
        assertNotNull(inspector.getPerfectEliminationOrder());
    }

    /**
     * Test on chordal connected graph with 10 vertices
     */
    @Test
    public void testIsChordal4() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 3, 5);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 5, 6);
        Graphs.addEdgeWithVertices(graph, 5, 7);
        Graphs.addEdgeWithVertices(graph, 6, 7);
        Graphs.addEdgeWithVertices(graph, 7, 8);
        Graphs.addEdgeWithVertices(graph, 7, 9);
        Graphs.addEdgeWithVertices(graph, 8, 9);
        Graphs.addEdgeWithVertices(graph, 9, 1);
        Graphs.addEdgeWithVertices(graph, 9, 1);
        Graphs.addEdgeWithVertices(graph, 10, 1);
        Graphs.addEdgeWithVertices(graph, 3, 7);
        Graphs.addEdgeWithVertices(graph, 1, 7);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        assertTrue(inspector.isChordal());
        assertNull(inspector.getHole());
        assertNotNull(inspector.getPerfectEliminationOrder());
    }

    /**
     * Test on graph with 4-vertex cycle: 1-2-3-4-1
     */
    @Test
    public void testIsChordal5() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 4, 1);
        Graphs.addEdgeWithVertices(graph, 1, 5);
        Graphs.addEdgeWithVertices(graph, 5, 2);
        Graphs.addEdgeWithVertices(graph, 2, 6);
        Graphs.addEdgeWithVertices(graph, 6, 3);
        Graphs.addEdgeWithVertices(graph, 3, 7);
        Graphs.addEdgeWithVertices(graph, 7, 4);
        Graphs.addEdgeWithVertices(graph, 4, 8);
        Graphs.addEdgeWithVertices(graph, 8, 1);
        Graphs.addEdgeWithVertices(graph, 5, 6);
        Graphs.addEdgeWithVertices(graph, 6, 7);
        Graphs.addEdgeWithVertices(graph, 7, 8);
        Graphs.addEdgeWithVertices(graph, 8, 5);
        Graphs.addEdgeWithVertices(graph, 5, 7);
        Graphs.addEdgeWithVertices(graph, 6, 8);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        assertFalse(inspector.isChordal());
        GraphPath<Integer, DefaultEdge> path = inspector.getHole();
        assertNotNull(path);
        assertIsHole(graph, path);
        assertNull(inspector.getPerfectEliminationOrder());
    }

    /**
     * Test on the chordal pseudograph
     */
    @Test
    public void testIsChordal6() {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 1);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 3, 1);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        assertTrue(inspector.isChordal());
        assertNull(inspector.getHole());
        assertNotNull(inspector.getPerfectEliminationOrder());
    }

    /**
     * Test of non-chordal pseudograph (cycle 2-3-4-5-2)
     */
    @Test
    public void testIsChordal7() {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 1);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 1);
        Graphs.addEdgeWithVertices(graph, 2, 2);
        Graphs.addEdgeWithVertices(graph, 3, 3);
        Graphs.addEdgeWithVertices(graph, 4, 4);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 5, 2);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        assertFalse(inspector.isChordal());
        GraphPath<Integer, DefaultEdge> path = inspector.getHole();
        assertNotNull(path);
        assertIsHole(graph, path);
        assertNull(inspector.getPerfectEliminationOrder());
    }

    /**
     * Test for correct hole detection
     */
    @Test
    public void testIsChordal8() {
        Graph<Integer, DefaultEdge> ellinghamHorton78 = NamedGraphGenerator.ellinghamHorton78Graph();
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(ellinghamHorton78);
        assertFalse(inspector.isChordal());
        assertIsHole(ellinghamHorton78, inspector.getHole());
    }

    /**
     * Test for correct hole detection
     */
    @Test
    public void testIsChordal9() {
        Graph<Integer, DefaultEdge> gosset = NamedGraphGenerator.gossetGraph();
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(gosset);
        assertFalse(inspector.isChordal());
        assertIsHole(gosset, inspector.getHole());
    }

    /**
     * Test for correct hole detection
     */
    @Test
    public void testIsChordal10() {
        Graph<Integer, DefaultEdge> klein = NamedGraphGenerator.klein3RegularGraph();
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(klein);
        assertFalse(inspector.isChordal());
        assertIsHole(klein, inspector.getHole());
    }

    /**
     * Test for correct hole detection
     */
    @Test
    public void testIsChordal11() {
        Graph<Integer, DefaultEdge> schläfli = NamedGraphGenerator.schläfliGraph();
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(schläfli);
        assertFalse(inspector.isChordal());
        assertIsHole(schläfli, inspector.getHole());
    }

    @Test
    public void testIsChordal12() {
        Graph<Integer, DefaultEdge> buckyBall = NamedGraphGenerator.buckyBallGraph();
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(buckyBall);
        assertFalse(inspector.isChordal());
        assertIsHole(buckyBall, inspector.getHole());
    }


    /**
     * Basic test for {@link ChordalityInspector#isPerfectEliminationOrder(List)}
     */
    @Test
    public void testIsPerfectEliminationOrder1() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        List<Integer> order = Arrays.asList(1, 2, 3, 4);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 1, 4);
        Graphs.addEdgeWithVertices(graph, 2, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        assertFalse(new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

    /**
     * First test on 4-vertex cycle: 1-2-3-4-1 <br>
     * Second test with chord 2-4 added, so that the graph becomes chordal
     */
    @Test
    public void testIsPerfectEliminationOrder2() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        List<Integer> order = Arrays.asList(1, 2, 4, 3);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 4);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph, iterationOrder);
        assertFalse("Not a perfect elimination order: cycle 1->2->3->4->1 has non chord", inspector.isPerfectEliminationOrder(order));
        graph.addEdge(2, 4);
        assertTrue("Valid perfect elimination order: no induced cycles of length > 3", inspector.isPerfectEliminationOrder(order));
    }

    /**
     * Test on chordal graph:<br>
     * .......5<br>
     * ...../.|.\<br>
     * ....4--3--6--7<br>
     * ....|./.|.|\.|<br>
     * ....1--2..9--8<br>
     * ...........\.|<br>
     * ............10 <br>
     */
    @Test
    public void testIsPerfectEliminationOrder3() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        List<Integer> order = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 1, 4);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 3, 5);
        Graphs.addEdgeWithVertices(graph, 3, 6);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 5, 6);
        Graphs.addEdgeWithVertices(graph, 6, 7);
        Graphs.addEdgeWithVertices(graph, 6, 8);
        Graphs.addEdgeWithVertices(graph, 6, 9);
        Graphs.addEdgeWithVertices(graph, 7, 8);
        Graphs.addEdgeWithVertices(graph, 8, 9);
        Graphs.addEdgeWithVertices(graph, 8, 10);
        Graphs.addEdgeWithVertices(graph, 9, 10);
        assertTrue(new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

    /**
     * Test on big chordal graph with valid perfect elimination order
     */
    @Test
    public void testIsPerfectEliminationOrder4() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        List<Integer> order = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 1, 4);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 3, 5);
        Graphs.addEdgeWithVertices(graph, 3, 6);
        Graphs.addEdgeWithVertices(graph, 3, 7);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 5, 6);
        Graphs.addEdgeWithVertices(graph, 5, 7);
        Graphs.addEdgeWithVertices(graph, 6, 7);
        Graphs.addEdgeWithVertices(graph, 6, 8);
        Graphs.addEdgeWithVertices(graph, 7, 9);
        Graphs.addEdgeWithVertices(graph, 7, 10);
        Graphs.addEdgeWithVertices(graph, 7, 11);
        Graphs.addEdgeWithVertices(graph, 9, 10);
        Graphs.addEdgeWithVertices(graph, 9, 11);
        Graphs.addEdgeWithVertices(graph, 9, 12);
        Graphs.addEdgeWithVertices(graph, 10, 11);
        Graphs.addEdgeWithVertices(graph, 11, 12);
        assertTrue("Valid perfect elimination order",
                new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

    /**
     * Test on chordal graph with invalid perfect elimination order
     */
    @Test
    public void testIsPerfectEliminationOrder5() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        List<Integer> order = Arrays.asList(1, 2, 5, 6, 4, 3);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 1, 3);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 2, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 3, 5);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 4, 6);
        Graphs.addEdgeWithVertices(graph, 5, 6);
        assertFalse("Graph is chordal, order isn't perfect elimination order",
                new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

    /**
     * Test on graph with 5-vertex cycle 2-4-6-8-10-2 with no chords
     */
    @Test
    public void testIsPerfectEliminationOrder6() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        List<Integer> order = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        Graphs.addEdgeWithVertices(graph, 2, 3);
        Graphs.addEdgeWithVertices(graph, 2, 4);
        Graphs.addEdgeWithVertices(graph, 3, 4);
        Graphs.addEdgeWithVertices(graph, 4, 5);
        Graphs.addEdgeWithVertices(graph, 4, 6);
        Graphs.addEdgeWithVertices(graph, 5, 6);
        Graphs.addEdgeWithVertices(graph, 6, 7);
        Graphs.addEdgeWithVertices(graph, 6, 8);
        Graphs.addEdgeWithVertices(graph, 7, 8);
        Graphs.addEdgeWithVertices(graph, 8, 9);
        Graphs.addEdgeWithVertices(graph, 8, 10);
        Graphs.addEdgeWithVertices(graph, 9, 10);
        Graphs.addEdgeWithVertices(graph, 10, 1);
        Graphs.addEdgeWithVertices(graph, 10, 2);
        assertFalse("Cycle 2->4->6->8->10->2 has no chords => no perfect elimination order",
                new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

    /**
     * Checks whether every two vertices from {@code set} are adjacent.
     *
     * @param graph the tested graph.
     * @param set   the tested set of vertices.
     * @param <V>   the graph vertex type.
     * @param <E>   the graph edge type.
     */
    private <V, E> void assertIsClique(Graph<V, E> graph, Set<V> set) {
        ArrayList<V> vertices = new ArrayList<>(set);
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = 0; j < i; j++) {
                assertTrue(graph.containsEdge(vertices.get(i), vertices.get(j)));
            }
        }
    }

    /**
     * Checks whether every two vertices from {@code set} aren't adjacent.
     *
     * @param graph the tested graph.
     * @param set   the tested set of vertices.
     * @param <V>   the graph vertex type.
     * @param <E>   the graph edge type.
     */
    private <V, E> void assertIsIndependentSet(Graph<V, E> graph, Set<V> set) {
        ArrayList<V> vertices = new ArrayList<>(set);
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = 0; j < i; j++) {
                assertFalse(graph.containsEdge(vertices.get(i), vertices.get(j)));
            }
        }
    }

    /**
     * Checks whether the {@code coloring} is a valid vertex coloring.
     *
     * @param graph    the tested graph.
     * @param coloring the tested coloring.
     * @param <V>      the graph vertex type.
     * @param <E>      the graph edge type.
     */
    private <V, E> void assertIsColoring(Graph<V, E> graph, VertexColoringAlgorithm.Coloring<V> coloring) {
        Map<V, Integer> colors = coloring.getColors();
        for (V vertex : graph.vertexSet()) {
            for (E edge : graph.edgesOf(vertex)) {
                V opposite = Graphs.getOppositeVertex(graph, edge, vertex);
                if (!vertex.equals(opposite)) {
                    assertNotEquals(colors.get(vertex), colors.get(opposite));
                }
            }
        }
    }

    /**
     * Checks whether {@code cycle} is a hole in {@code graph}
     *
     * @param graph the tested graph.
     * @param path  the tested cycle.
     * @param <V>   graph vertex type.
     * @param <E>   graph edge type.
     */
    private <V, E> void assertIsHole(Graph<V, E> graph, GraphPath<V, E> path) {
        List<V> cycle = path.getVertexList();
        assertTrue(cycle.size() > 4);
        for (int i = 0; i < cycle.size() - 1; i++) {
            assertTrue(graph.containsEdge(cycle.get(i), cycle.get(i + 1)));
        }
        for (int i = 0; i < cycle.size() - 2; i++) {
            for (int j = 0; j < cycle.size() - 2; j++) {
                if (Math.abs(i - j) > 1) {
                    assertFalse(graph.containsEdge(cycle.get(i), cycle.get(j)));
                }
            }
        }
    }
}

