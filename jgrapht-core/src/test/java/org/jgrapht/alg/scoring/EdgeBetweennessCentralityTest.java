/*
 * (C) Copyright 2003-2020, by Barak Naveh and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for class {@link EdgeBetweennessCentrality}.
 *
 * @author Edwin Ouwehand
 */
public class EdgeBetweennessCentralityTest {

    private static final double EPSILON = 0.01;

    @Test(expected = NullPointerException.class)
    public void noNullGraph() {
        new EdgeBetweennessCentrality<>(null);
    }

    @Test
    public void emptyGraph() {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(graph);
        Map<DefaultEdge, Double> map = ebc.getScores();
        assertTrue(map.isEmpty());
    }

    @Test
    public void noEdges() {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(graph);
        Map<DefaultEdge, Double> map = ebc.getScores();
        assertTrue(map.isEmpty());
    }

    @Test
    public void singleEdge() {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        DefaultEdge edge = graph.addEdge("A", "B");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(graph);
        Map<DefaultEdge, Double> map = ebc.getScores();
        assertEquals(1, map.size());
        assertEquals(1, map.get(edge), EPSILON);
    }

    @Test
    public void twoShortestPaths() {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        DefaultEdge edge1 = graph.addEdge("A", "B");
        DefaultEdge edge2 = graph.addEdge("A", "C");
        DefaultEdge edge3 = graph.addEdge("B", "D");
        DefaultEdge edge4 = graph.addEdge("C", "D");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(graph);
        Map<DefaultEdge, Double> map = ebc.getScores();

        assertEquals(1.5, map.get(edge1), EPSILON);
        assertEquals(1.5, map.get(edge2), EPSILON);
        assertEquals(1.5, map.get(edge3), EPSILON);
        assertEquals(1.5, map.get(edge4), EPSILON);
    }

    @Test
    public void threeShortestPaths() {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");
        DefaultEdge edge1 = graph.addEdge("A", "B");
        DefaultEdge edge2 = graph.addEdge("A", "C");
        DefaultEdge edge3 = graph.addEdge("A", "D");
        DefaultEdge edge4 = graph.addEdge("B", "E");
        DefaultEdge edge5 = graph.addEdge("C", "E");
        DefaultEdge edge6 = graph.addEdge("D", "E");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(graph);
        Map<DefaultEdge, Double> map = ebc.getScores();

        assertEquals(1.333, map.get(edge1), EPSILON);
        assertEquals(1.333, map.get(edge2), EPSILON);
        assertEquals(1.333, map.get(edge3), EPSILON);
        assertEquals(1.333, map.get(edge4), EPSILON);
        assertEquals(1.333, map.get(edge5), EPSILON);
        assertEquals(1.333, map.get(edge6), EPSILON);
    }

    @Test
    public void loop() {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        DefaultEdge edge1 = graph.addEdge("A", "B");
        DefaultEdge edge2 = graph.addEdge("B", "A");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(graph);
        Map<DefaultEdge, Double> map = ebc.getScores();
        assertEquals(2, map.size());
        assertEquals(1, map.get(edge1), EPSILON);
        assertEquals(1, map.get(edge2), EPSILON);
    }

    @Test
    public void tree() {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        DefaultEdge edge1 = graph.addEdge("A", "B");
        DefaultEdge edge2 = graph.addEdge("A", "C");
        DefaultEdge edge3 = graph.addEdge("A", "D");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(graph);
        Map<DefaultEdge, Double> map = ebc.getScores();

        assertEquals(1.0, map.get(edge1), EPSILON);
        assertEquals(1.0, map.get(edge2), EPSILON);
        assertEquals(1.0, map.get(edge3), EPSILON);
    }

    @Test
    public void twoDisconnectedGraphs() {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        DefaultEdge edge1 = graph.addEdge("A", "B");
        DefaultEdge edge2 = graph.addEdge("B", "A");
        DefaultEdge edge3 = graph.addEdge("C", "D");
        DefaultEdge edge4 = graph.addEdge("D", "C");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(graph);
        Map<DefaultEdge, Double> map = ebc.getScores();

        assertEquals(1, map.get(edge1), EPSILON);
        assertEquals(1, map.get(edge2), EPSILON);
        assertEquals(1, map.get(edge3), EPSILON);
        assertEquals(1, map.get(edge4), EPSILON);
    }

    @Test
    public void reflexiveEdge() {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        DefaultEdge edge = graph.addEdge("A", "A");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(graph);
        Map<DefaultEdge, Double> map = ebc.getScores();
        assertEquals(0, map.get(edge), EPSILON);
    }

    @Test
    public void anotherGraph() {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");
        graph.addVertex("F");

        DefaultEdge edge1 = graph.addEdge("A", "B");
        DefaultEdge edge2 = graph.addEdge("B", "A");
        DefaultEdge edge3 = graph.addEdge("B", "C");
        DefaultEdge edge4 = graph.addEdge("C", "A");
        DefaultEdge edge5 = graph.addEdge("A", "D");
        DefaultEdge edge6 = graph.addEdge("D", "F");
        DefaultEdge edge7 = graph.addEdge("A", "E");
        DefaultEdge edge8 = graph.addEdge("E", "F");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(graph);
        Map<DefaultEdge, Double> map = ebc.getScores();

        assertEquals(3.0, map.get(edge1), EPSILON);
        assertEquals(4.0, map.get(edge2), EPSILON);
        assertEquals(2.0, map.get(edge3), EPSILON);
        assertEquals(5.0, map.get(edge4), EPSILON);
        assertEquals(4.5, map.get(edge5), EPSILON);
        assertEquals(2.5, map.get(edge6), EPSILON);
        assertEquals(4.5, map.get(edge7), EPSILON);
        assertEquals(2.5, map.get(edge8), EPSILON);
    }

    @Test
    public void linearGraph() {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");

        DefaultEdge edge1 = graph.addEdge("A", "B");
        DefaultEdge edge2 = graph.addEdge("B", "A");
        DefaultEdge edge3 = graph.addEdge("C", "B");
        DefaultEdge edge4 = graph.addEdge("B", "C");
        DefaultEdge edge5 = graph.addEdge("C", "D");
        DefaultEdge edge6 = graph.addEdge("D", "C");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(graph);
        Map<DefaultEdge, Double> map = ebc.getScores();

        assertEquals(3.0, map.get(edge1), EPSILON);
        assertEquals(3.0, map.get(edge2), EPSILON);
        assertEquals(4.0, map.get(edge3), EPSILON);
        assertEquals(4.0, map.get(edge4), EPSILON);
        assertEquals(3.0, map.get(edge5), EPSILON);
        assertEquals(3.0, map.get(edge6), EPSILON);
    }

    @Test
    public void paperTrivial() {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");
        graph.addVertex("F");
        graph.addVertex("G");

        DefaultEdge edge1 = graph.addEdge("A", "B");
        DefaultEdge edge2 = graph.addEdge("B", "C");
        DefaultEdge edge3 = graph.addEdge("C", "D");
        DefaultEdge edge4 = graph.addEdge("D", "E");
        DefaultEdge edge5 = graph.addEdge("D", "F");
        DefaultEdge edge6 = graph.addEdge("F", "G");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(graph);
        Map<DefaultEdge, Double> map = ebc.getScores();

        assertEquals(6.0, map.get(edge1), EPSILON);
        assertEquals(10.0, map.get(edge2), EPSILON);
        assertEquals(12.0, map.get(edge3), EPSILON);
        assertEquals(4.0, map.get(edge4), EPSILON);
        assertEquals(8.0, map.get(edge5), EPSILON);
        assertEquals(5.0, map.get(edge6), EPSILON);
    }

    @Test
    public void paperFrac() {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");
        graph.addVertex("F");
        graph.addVertex("G");

        DefaultEdge edge1 = graph.addEdge("A", "B");
        DefaultEdge edge2 = graph.addEdge("A", "C");
        DefaultEdge edge3 = graph.addEdge("B", "D");
        DefaultEdge edge4 = graph.addEdge("C", "D");
        DefaultEdge edge5 = graph.addEdge("C", "E");
        DefaultEdge edge6 = graph.addEdge("D", "F");
        DefaultEdge edge7 = graph.addEdge("E", "F");
        DefaultEdge edge8 = graph.addEdge("F", "G");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(graph);
        Map<DefaultEdge, Double> map = ebc.getScores();

        assertEquals(2.166, map.get(edge1), EPSILON);
        assertEquals(3.833, map.get(edge2), EPSILON);
        assertEquals(4.166, map.get(edge3), EPSILON);
        assertEquals(3.166, map.get(edge4), EPSILON);
        assertEquals(3.666, map.get(edge5), EPSILON);
        assertEquals(6.333, map.get(edge6), EPSILON);
        assertEquals(3.666, map.get(edge7), EPSILON);
        assertEquals(6.0, map.get(edge8), EPSILON);
    }

    @Test
    public void weighted() {
        Graph<String, DefaultEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        DefaultEdge edge1 = graph.addEdge("A", "B");
        graph.setEdgeWeight(edge1, 2);
        DefaultEdge edge2 = graph.addEdge("A", "C");
        DefaultEdge edge3 = graph.addEdge("B", "D");
        DefaultEdge edge4 = graph.addEdge("C", "D");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(graph);
        Map<DefaultEdge, Double> map = ebc.getScores();

        assertEquals(2, map.get(edge1), EPSILON);
        assertEquals(1, map.get(edge2), EPSILON);
        assertEquals(2, map.get(edge3), EPSILON);
        assertEquals(1, map.get(edge4), EPSILON);
    }
}
