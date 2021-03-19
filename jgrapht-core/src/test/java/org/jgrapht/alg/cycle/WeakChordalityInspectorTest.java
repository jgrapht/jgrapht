/*
 * (C) Copyright 2018-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for {@link WeakChordalityInspector}
 *
 * @author Timofey Chudakov
 */
public class WeakChordalityInspectorTest
{

    /**
     * Test on empty graph
     */
    @Test
    public void testIsWeaklyChordal1()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(graph);
        assertTrue(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNull(graphPath);
    }

    /**
     * Test on small chordal graph
     */
    @Test
    public void testIsWeaklyChordal2()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 2, 3 }, { 2, 4 }, { 3, 4 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(graph);
        assertTrue(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNull(graphPath);
    }

    /**
     * Test on small weakly chordal graph
     */
    @Test
    public void testIsWeaklyChordal3()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(graph);
        assertTrue(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNull(graphPath);
    }

    /**
     * Test on hole
     */
    @Test
    public void testIsWeaklyChordal4()
    {
        int[][] edges = { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 }, { 5, 1 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(graph);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(graph, graphPath);
    }

    /**
     * Test on anti hole
     */
    @Test
    public void testIsWeaklyChordal5()
    {
        int[][] edges = { { 1, 3 }, { 1, 4 }, { 1, 5 }, { 1, 6 }, { 2, 4 }, { 2, 5 }, { 2, 6 },
            { 2, 7 }, { 3, 5 }, { 3, 6 }, { 3, 7 }, { 4, 6 }, { 4, 7 }, { 5, 7 }, };
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        TestUtil.constructGraph(graph, edges);

        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(graph);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(graph, graphPath);
    }

    /**
     * Test on weakly chordal pseudograph
     */
    @Test
    public void testIsWeaklyChordal6()
    {
        int[][] edges = { { 1, 1 }, { 1, 1 }, { 1, 2 }, { 1, 2 }, { 1, 2 }, { 1, 3 }, { 2, 4 },
            { 2, 4 }, { 2, 4 }, { 3, 4 }, { 4, 4 }, { 4, 4 }, { 4, 4 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(graph);
        assertTrue(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNull(graphPath);
    }

    /**
     * Test on big not weakly chordal graph
     */
    @Test
    public void testIsWeaklyChordal7()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 7 }, { 2, 8 }, { 2, 10 }, { 2, 5 },
            { 3, 5 }, { 3, 6 }, { 4, 7 }, { 5, 8 }, { 5, 9 }, { 5, 6 }, { 6, 9 }, { 7, 8 },
            { 7, 10 }, { 8, 9 }, { 8, 10 }, { 9, 10 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(graph);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(graph, graphPath);
    }

    /**
     * Test on big chordless cycle
     */
    @Test
    public void testIsWeaklyChordal8()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        int bound = 100;
        for (int i = 0; i < bound; i++) {
            Graphs.addEdgeWithVertices(graph, i, i + 1);
        }
        Graphs.addEdgeWithVertices(graph, 0, bound);
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(graph);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(graph, graphPath);
    }

    /**
     * Test on big complete graph
     */
    @Test
    public void testIsWeaklyChordal9()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        CompleteGraphGenerator<Integer, DefaultEdge> generator = new CompleteGraphGenerator<>(50);
        generator.generateGraph(graph);
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(graph);
        assertTrue(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNull(graphPath);
    }

    @Test
    public void testIsWeaklyChordal10()
    {
        Graph<Integer, DefaultEdge> dodecahedron = NamedGraphGenerator.dodecahedronGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(dodecahedron);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(dodecahedron, graphPath);
    }

    @Test
    public void testIsWeaklyChordal11()
    {
        Graph<Integer, DefaultEdge> bull = NamedGraphGenerator.bullGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(bull);
        assertTrue(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNull(graphPath);
    }

    @Test
    public void testIsWeaklyChordal12()
    {
        Graph<Integer, DefaultEdge> buckyBall = NamedGraphGenerator.buckyBallGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(buckyBall);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(buckyBall, graphPath);
    }

    @Test
    public void testIsWeaklyChordal13()
    {
        Graph<Integer, DefaultEdge> clebsch = NamedGraphGenerator.clebschGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(clebsch);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(clebsch, graphPath);
    }

    @Test
    public void testIsWeaklyChordal14()
    {
        Graph<Integer, DefaultEdge> grötzsch = NamedGraphGenerator.grötzschGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(grötzsch);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(grötzsch, graphPath);
    }

    @Test
    public void testIsWeaklyChordal15()
    {
        Graph<Integer, DefaultEdge> bidiakis = NamedGraphGenerator.bidiakisCubeGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(bidiakis);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(bidiakis, graphPath);
    }

    @Test
    public void testIsWeaklyChordal16()
    {
        Graph<Integer, DefaultEdge> blanusaFirstSnark =
            NamedGraphGenerator.blanusaFirstSnarkGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(blanusaFirstSnark);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(blanusaFirstSnark, graphPath);
    }

    @Test
    public void testIsWeaklyChordal17()
    {
        Graph<Integer, DefaultEdge> doubleStarSnark = NamedGraphGenerator.doubleStarSnarkGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(doubleStarSnark);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(doubleStarSnark, graphPath);
    }

    @Test
    public void testIsWeaklyChordal18()
    {
        Graph<Integer, DefaultEdge> brinkmann = NamedGraphGenerator.brinkmannGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(brinkmann);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(brinkmann, graphPath);
    }

    @Test
    public void testIsWeaklyChordal19()
    {
        Graph<Integer, DefaultEdge> gosset = NamedGraphGenerator.gossetGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(gosset);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(gosset, graphPath);
    }

    @Test
    public void testIsWeaklyChordal20()
    {
        Graph<Integer, DefaultEdge> chvatal = NamedGraphGenerator.chvatalGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(chvatal);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(chvatal, graphPath);
    }

    @Test
    public void testIsWeaklyChordal21()
    {
        Graph<Integer, DefaultEdge> kittell = NamedGraphGenerator.kittellGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(kittell);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(kittell, graphPath);
    }

    @Test
    public void testIsWeaklyChordal22()
    {
        Graph<Integer, DefaultEdge> coxeter = NamedGraphGenerator.coxeterGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(coxeter);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(coxeter, graphPath);
    }

    @Test
    public void testIsWeaklyChordal23()
    {
        Graph<Integer, DefaultEdge> ellinghamHorton78 =
            NamedGraphGenerator.ellinghamHorton78Graph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(ellinghamHorton78);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(ellinghamHorton78, graphPath);
    }

    @Test
    public void testIsWeaklyChordal24()
    {
        Graph<Integer, DefaultEdge> errera = NamedGraphGenerator.erreraGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(errera);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(errera, graphPath);
    }

    @Test
    public void testIsWeaklyChordal25()
    {
        Graph<Integer, DefaultEdge> folkman = NamedGraphGenerator.folkmanGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(folkman);
        assertFalse(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNotNull(graphPath);
        assertIsHoleOrAntiHole(folkman, graphPath);
    }

    @Test
    public void testIsWeaklyChordal26()
    {
        Graph<Integer, DefaultEdge> krackhardtKite = NamedGraphGenerator.krackhardtKiteGraph();
        WeakChordalityInspector<Integer, DefaultEdge> inspector =
            new WeakChordalityInspector<>(krackhardtKite);
        assertTrue(inspector.isWeaklyChordal());
        GraphPath<Integer, DefaultEdge> graphPath = inspector.getCertificate();
        assertNull(graphPath);
    }

    /**
     * Asserts that the specified {@code path} forms a hole or anti-hole in the {@code graph}
     *
     * @param graph the graph that should contain a hole or anti-hole
     * @param path a path in the {@code graph}
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    private <V, E> void assertIsHoleOrAntiHole(Graph<V, E> graph, GraphPath<V, E> path)
    {
        assertTrue(isHole(graph, path) || isAntiHole(graph, path));
    }

    /**
     * Checks whether specified {@code path} forms a hole in the {@code graph}
     *
     * @param graph the graph that should contain a hole
     * @param path a path in the {@code graph}
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true is the {@code path} forms a hole in the {@code graph}, false otherwise
     */
    private <V, E> boolean isHole(Graph<V, E> graph, GraphPath<V, E> path)
    {
        List<V> vertices = path.getVertexList();
        if (vertices.size() < 6 || !vertices.get(0).equals(vertices.get(vertices.size() - 1))) {
            return false;
        }
        for (int i = 0; i < vertices.size() - 1; i++) {
            if (!graph.containsEdge(vertices.get(i), vertices.get(i + 1))) {
                return false;
            }
        }
        for (int i = 0; i < vertices.size() - 2; i++) {
            for (int j = 0; j < vertices.size() - 2; j++) {
                if (Math.abs(i - j) > 1 && graph.containsEdge(vertices.get(i), vertices.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks whether specified {@code path} forms an anti-hole in the {@code graph}
     *
     * @param graph the graph that should contain an anti-hole
     * @param path a path in the {@code graph}
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true is the {@code path} forms an anti-hole in the {@code graph}, false otherwise
     */
    private <V, E> boolean isAntiHole(Graph<V, E> graph, GraphPath<V, E> path)
    {
        List<V> vertices = path.getVertexList();
        if (vertices.size() < 6 || !vertices.get(0).equals(vertices.get(vertices.size() - 1))) {
            return false;
        }
        for (int i = 0; i < vertices.size() - 1; i++) {
            if (graph.containsEdge(vertices.get(i), vertices.get(i + 1))) {
                return false;
            }
        }
        for (int i = 0; i < vertices.size() - 2; i++) {
            for (int j = 0; j < vertices.size() - 2; j++) {
                if (Math.abs(i - j) > 1 && !graph.containsEdge(vertices.get(i), vertices.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}
