/*
 * (C) Copyright 2017-2021, by Assaf Mizrachi and Contributors.
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
package org.jgrapht.alg.scoring;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.scoring.BetweennessCentrality.OverflowStrategy;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;
import org.junit.experimental.categories.Category;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BetweennessCentralityTest
{

    @Test(expected = NullPointerException.class)
    public void testNullGraph()
    {
        Graph<Integer, DefaultEdge> g = null;
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g);
        bc.getScores();
    }

    @Test
    public void testEmptyGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g);
        Map<Integer, Double> scores = bc.getScores();
        assertTrue(scores.isEmpty());
    }

    @Test
    public void testEmptyGraphNormalized()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g, true);
        Map<Integer, Double> scores = bc.getScores();
        assertTrue(scores.isEmpty());
    }

    @Test
    public void testSingletonGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(0);
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g);
        Map<Integer, Double> scores = bc.getScores();
        assertEquals(0.0, scores.get(0), 0.0);
    }

    @Test
    public void testSingletonGraphNormalized()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(0);
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g, true);
        Map<Integer, Double> scores = bc.getScores();
        assertEquals(0.0, scores.get(0), 0.0);
    }

    @Test
    public void testK2Graph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        g.addEdge(0, 1);
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g);
        Map<Integer, Double> scores = bc.getScores();
        assertEquals(0.0, scores.get(0), 0.0);
        assertEquals(0.0, scores.get(1), 0.0);
    }

    @Test
    public void testK2GraphNormalized()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        g.addEdge(0, 1);
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g, true);
        Map<Integer, Double> scores = bc.getScores();
        assertEquals(0.0, scores.get(0), 0.0);
        assertEquals(0.0, scores.get(1), 0.0);
    }

    @Test
    public void testUnweighted1()
    {
        Graph<Integer, DefaultEdge> g = createUnweighted1();
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g);
        Map<Integer, Double> scores = bc.getScores();
        assertGraph1(scores);
    }

    @Test
    public void testAsWeighted1()
    {
        Graph<Integer, DefaultEdge> g = new AsWeightedGraph<>(createUnweighted1(), new HashMap<>());
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g);
        Map<Integer, Double> scores = bc.getScores();
        assertGraph1(scores);
    }

    @Test
    public void testNormalization()
    {
        Graph<Integer, DefaultEdge> g = new AsWeightedGraph<>(createUnweighted1(), new HashMap<>());
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g, true);
        Map<Integer, Double> scores = new HashMap<>(bc.getScores());
        int n = g.vertexSet().size();
        scores.forEach((v, score) -> scores.put(v, score * ((n - 1) * (n - 2))));
        assertGraph1(scores);
    }

    @Test
    public void testUnweighted2()
    {
        Graph<Integer, DefaultEdge> g = createUnweighted2();
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g);
        Map<Integer, Double> scores = bc.getScores();
        assertGraph2(scores);
    }

    @Test
    public void testUnweighted3()
    {
        Graph<Integer, DefaultEdge> g = createUnweighted3();
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g);
        Map<Integer, Double> scores = bc.getScores();
        assertGraph3(scores);

    }

    @Test
    public void testUnweighted4()
    {
        Graph<Integer, DefaultEdge> g = createUnweighted4();
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g);
        Map<Integer, Double> scores = bc.getScores();
        assertGraph4(scores);

    }

    @Test
    public void testWeighted5()
    {
        Graph<String, DefaultWeightedEdge> g = createWeighted5();
        VertexScoringAlgorithm<String, Double> bc = new BetweennessCentrality<>(g);
        Map<String, Double> scores = bc.getScores();
        assertGraph5(scores);

    }

    @Test
    public void testWeighted6()
    {
        Graph<Integer, DefaultWeightedEdge> g = createWeighted6();
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g);
        Map<Integer, Double> scores = bc.getScores();
        assertGraph6(scores);

    }

    @Test
    public void testStar()
    {
        testStar(5);
        testStar(12);

    }

    private void testStar(int order)
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> generator = new StarGraphGenerator<>(order);
        Map<String, Integer> resultMap = new HashMap<>();
        generator.generateGraph(g, resultMap);
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g);

        assertStar(bc.getScores(), resultMap.get(StarGraphGenerator.CENTER_VERTEX), order);

    }

    private void assertStar(Map<Integer, Double> scores, Integer center, int order)
    {
        for (Integer v : scores.keySet()) {
            if (v.equals(center)) {
                assertEquals((order - 2) * (order - 1) / 2, scores.get(v), 0.0);
            } else {
                assertEquals(0.0, scores.get(v), 0.0);
            }
        }

    }

    @Test
    public void testLinear()
    {
        testLinear(5);
        testLinear(12);
        testLinear(37);
    }

    private void testLinear(int order)
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> generator = new LinearGraphGenerator<>(order);
        Map<String, Integer> resultMap = new HashMap<>();
        generator.generateGraph(g, resultMap);
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g);

        if (order == 5) {
            assertLinear5(bc.getScores());
        } else {
            assertLinear(bc.getScores(), order);
        }

    }

    private void assertLinear5(Map<Integer, Double> scores)
    {
        for (Integer v : scores.keySet()) {
            if (v.equals(0) || v.equals(4)) {
                assertEquals(0.0, scores.get(v), 0.0);
            } else if (v.equals(1) || v.equals(3)) {
                assertEquals(3.0, scores.get(v), 0.0);
            } else if (v.equals(2)) {
                assertEquals(4.0, scores.get(v), 0.0);
            } else {
                throw new IllegalArgumentException("Unexpected vertex " + v);
            }
        }
    }

    private void assertLinear(Map<Integer, Double> scores, int order)
    {
        for (int i = 0; i < order / 2; i++) {
            assertEquals(scores.get(i), scores.get(order - i - 1), 0.0);
        }
    }

    @Test
    public void testRing()
    {
        testRing(5);
        testRing(12);
        testRing(37);
    }

    private void testRing(int order)
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> generator = new RingGraphGenerator<>(order);
        Map<String, Integer> resultMap = new HashMap<>();
        generator.generateGraph(g, resultMap);
        VertexScoringAlgorithm<Integer, Double> bc = new BetweennessCentrality<>(g);

        if (order == 5) {
            assertRing5(bc.getScores());
        } else {
            assertRing(bc.getScores(), order);
        }

    }

    private void assertRing5(Map<Integer, Double> scores)
    {
        for (Integer v : scores.keySet()) {
            assertEquals(1.0, scores.get(v), 0.0);
        }
    }

    private void assertRing(Map<Integer, Double> scores, int order)
    {
        for (int i = 0; i < order - 1; i++) {
            assertEquals(scores.get(i), scores.get(i + 1), 0.0);
        }
    }

    private void assertGraph3(Map<Integer, Double> scores)
    {
        assertEquals(0.0, scores.get(1), 0.0);
        assertEquals(1.5, scores.get(2), 0.0);
        assertEquals(1.0, scores.get(3), 0.0);
        assertEquals(4.5, scores.get(4), 0.0);
        assertEquals(3.0, scores.get(5), 0.0);
        assertEquals(0.0, scores.get(6), 0.0);
    }

    private void assertGraph4(Map<Integer, Double> scores)
    {
        assertEquals(0.0, scores.get(1), 0.0);
        assertEquals(3.5, scores.get(2), 0.0);
        assertEquals(1.0, scores.get(3), 0.0);
        assertEquals(1.0, scores.get(4), 0.0);
        assertEquals(0.5, scores.get(5), 0.0);
    }

    private void assertGraph5(Map<String, Double> scores)
    {
        assertEquals(0.0, scores.get("A"), 0.0);
        assertEquals(3.0, scores.get("B"), 0.0);
        assertEquals(6.0, scores.get("C"), 0.0);
        assertEquals(10.0, scores.get("D"), 0.0);
        assertEquals(5.0, scores.get("E"), 0.0);
        assertEquals(5.0, scores.get("F"), 0.0);
        assertEquals(1.0, scores.get("G"), 0.0);
    }

    private void assertGraph1(Map<Integer, Double> scores)
    {

        assertEquals(3.0, scores.get(1), 0.0);
        assertEquals(0.0, scores.get(2), 0.0);
        assertEquals(3.0, scores.get(3), 0.0);
        assertEquals(15.0, scores.get(4), 0.0);
        assertEquals(6.0, scores.get(5), 0.0);
        assertEquals(6.0, scores.get(6), 0.0);
        assertEquals(7.0, scores.get(7), 0.0);
        assertEquals(0.0, scores.get(8), 0.0);
        assertEquals(0.0, scores.get(9), 0.0);
    }

    private void assertGraph2(Map<Integer, Double> scores)
    {
        assertEquals(43.0, scores.get(0), 0.0);
        assertEquals(25.0, scores.get(1), 0.0);
        assertEquals(70.0, scores.get(2), 0.0);
        assertEquals(40.0, scores.get(3), 0.0);
        assertEquals(13.0, scores.get(4), 0.0);
        assertEquals(0.0, scores.get(5), 0.0);
        assertEquals(0.0, scores.get(6), 0.0);
        assertEquals(36.0, scores.get(7), 0.0);
        assertEquals(0.0, scores.get(8), 0.0);
        assertEquals(0.0, scores.get(9), 0.0);
        assertEquals(0.0, scores.get(10), 0.0);
        assertEquals(0.0, scores.get(11), 0.0);
        assertEquals(0.0, scores.get(12), 0.0);
        assertEquals(0.0, scores.get(13), 0.0);
        assertEquals(0.0, scores.get(14), 0.0);
    }

    private void assertGraph6(Map<Integer, Double> scores)
    {
        assertEquals(0.0, scores.get(0), 0.0);
        assertEquals(1.0, scores.get(1), 0.0);
        assertEquals(0.0, scores.get(2), 0.0);
    }

    private Graph<Integer, DefaultEdge> createUnweighted1()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);
        g.addVertex(9);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 4);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(4, 6);
        g.addEdge(5, 6);
        g.addEdge(5, 7);
        g.addEdge(5, 8);
        g.addEdge(6, 7);
        g.addEdge(6, 8);
        g.addEdge(7, 8);
        g.addEdge(7, 9);

        return g;
    }

    private Graph<Integer, DefaultEdge> createUnweighted2()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addVertex(8);
        g.addVertex(9);
        g.addVertex(10);
        g.addVertex(11);
        g.addVertex(12);
        g.addVertex(13);
        g.addVertex(14);
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(0, 5);
        g.addEdge(1, 6);
        g.addEdge(1, 9);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(2, 10);
        g.addEdge(2, 14);
        g.addEdge(3, 7);
        g.addEdge(4, 11);
        g.addEdge(7, 8);
        g.addEdge(7, 12);
        g.addEdge(7, 13);

        return g;
    }

    private Graph<Integer, DefaultEdge> createUnweighted3()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addEdge(1, 2);
        g.addEdge(1, 5);
        g.addEdge(2, 3);
        g.addEdge(2, 5);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(4, 6);

        return g;
    }

    private Graph<Integer, DefaultEdge> createUnweighted4()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(3, 5);
        g.addEdge(4, 5);

        return g;
    }

    private Graph<String, DefaultWeightedEdge> createWeighted5()
    {
        Graph<String, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addVertex("D");
        g.addVertex("E");
        g.addVertex("F");
        g.addVertex("G");

        DefaultWeightedEdge e;

        e = g.addEdge("A", "B");
        g.setEdgeWeight(e, 0.7);

        e = g.addEdge("A", "D");
        g.setEdgeWeight(e, 0.3);

        e = g.addEdge("B", "C");
        g.setEdgeWeight(e, 0.9);

        e = g.addEdge("C", "A");
        g.setEdgeWeight(e, 1.3);

        e = g.addEdge("C", "D");
        g.setEdgeWeight(e, 0.57);

        e = g.addEdge("D", "B");
        g.setEdgeWeight(e, 1.0);

        e = g.addEdge("D", "E");
        g.setEdgeWeight(e, 0.8);

        e = g.addEdge("D", "F");
        g.setEdgeWeight(e, 0.2);

        e = g.addEdge("E", "G");
        g.setEdgeWeight(e, 0.4);

        e = g.addEdge("F", "E");
        g.setEdgeWeight(e, 0.6);

        e = g.addEdge("G", "F");
        g.setEdgeWeight(e, 0.2);

        return g;
    }

    private Graph<Integer, DefaultWeightedEdge> createWeighted6()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);

        DefaultWeightedEdge e;

        e = g.addEdge(2, 1);
        g.setEdgeWeight(e, 1);

        e = g.addEdge(1, 0);
        g.setEdgeWeight(e, 1);

        e = g.addEdge(2, 0);
        g.setEdgeWeight(e, 49);

        return g;
    }

    @Test(expected = ArithmeticException.class)
    public void testOverflow()
    {
        final Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 3300; i++)
            g.addVertex(i);
        for (int i = 0; i < 3290; i++)
            for (int j = 0; j < 10; j++)
                g.addEdge(i, i - i % 10 + 10 + j);
        VertexScoringAlgorithm<Integer, Double> bc =
            new BetweennessCentrality<>(g, false, OverflowStrategy.THROW_EXCEPTION_ON_OVERFLOW);
        bc.getScores();
    }

    @Test
    @Category(SlowTests.class)
    public void testIgnoreOverflow()
    {
        final Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < 3300; i++)
            g.addVertex(i);
        for (int i = 0; i < 3290; i++)
            for (int j = 0; j < 10; j++)
                g.addEdge(i, i - i % 10 + 10 + j);
        VertexScoringAlgorithm<Integer, Double> bc =
            new BetweennessCentrality<>(g, false, OverflowStrategy.IGNORE_OVERFLOW);
        Map<Integer, Double> scores = bc.getScores();
        assertEquals(scores.get(9), 0d, 1e-9);
        assertEquals(scores.get(10), Double.NaN, 1e-9);
        assertEquals(scores.get(3289), Double.NaN, 1e-9);
        assertEquals(scores.get(3290), 0d, 1e-9);
    }

}
