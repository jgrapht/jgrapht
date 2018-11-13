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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.function.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dimitrios Michail
 */
public class JohnsonShortestPathsTest
{

    @Test
    public void testIssue408()
    {
        Graph<Integer,
            DefaultEdge> graph = GraphTypeBuilder
                .directed().edgeClass(DefaultEdge.class)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).allowingMultipleEdges(false)
                .allowingSelfLoops(false).buildGraph();

        for (int i = 0; i < 7; i++) {
            graph.addVertex();
        }
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);

        graph.addEdge(4, 5);
        graph.addEdge(5, 6);
        graph.addEdge(6, 4);

        JohnsonShortestPaths<Integer, DefaultEdge> sp = new JohnsonShortestPaths<>(graph);

        assertEquals(2.0, sp.getPathWeight(0, 2), 0.0);
        assertEquals(1.0, sp.getPathWeight(4, 5), 0.0);
        assertTrue(Double.isInfinite(sp.getPathWeight(3, 4)));
    }

    @Test
    public void testWikipediaExample()
    {
        Graph<String,
            DefaultWeightedEdge> g = GraphTypeBuilder
                .directed().vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeClass(DefaultWeightedEdge.class).weighted(true).allowingMultipleEdges(true)
                .allowingSelfLoops(true).buildGraph();

        g.addVertex("w");
        g.addVertex("y");
        g.addVertex("x");
        g.addVertex("z");
        g.setEdgeWeight(g.addEdge("w", "z"), 2);
        g.setEdgeWeight(g.addEdge("y", "w"), 4);
        g.setEdgeWeight(g.addEdge("x", "w"), 6);
        g.setEdgeWeight(g.addEdge("x", "y"), 3);
        g.setEdgeWeight(g.addEdge("z", "x"), -7);
        g.setEdgeWeight(g.addEdge("y", "z"), 5);
        g.setEdgeWeight(g.addEdge("z", "y"), -3);

        JohnsonShortestPaths<String, DefaultWeightedEdge> alg = new JohnsonShortestPaths<>(g);
        assertEquals(-1d, alg.getPathWeight("z", "w"), 1e-9);
        assertEquals(-4d, alg.getPathWeight("z", "y"), 1e-9);
        assertEquals(0, alg.getPathWeight("z", "z"), 1e-9);
        assertEquals(-7, alg.getPathWeight("z", "x"), 1e-9);
    }

    @Test
    public void testRandomGraphsCompareWithFloydWarshall()
    {
        final int tests = 20;
        final int n = 50;
        final double p = 0.55;

        Random rng = new Random();

        List<Supplier<Graph<Integer, DefaultWeightedEdge>>> graphs = new ArrayList<>();
        graphs.add(
            () -> GraphTypeBuilder
                .directed().vertexSupplier(SupplierUtil.createIntegerSupplier())
                .edgeClass(DefaultWeightedEdge.class).weighted(true).allowingMultipleEdges(true)
                .allowingSelfLoops(true).buildGraph());

        for (Supplier<Graph<Integer, DefaultWeightedEdge>> gSupplier : graphs) {
            GraphGenerator<Integer, DefaultWeightedEdge, Integer> gen =
                new GnpRandomGraphGenerator<>(n, p, rng, false);
            for (int i = 0; i < tests; i++) {
                Graph<Integer, DefaultWeightedEdge> g = gSupplier.get();
                gen.generateGraph(g);

                // assign weights
                for (DefaultWeightedEdge e : g.edgeSet()) {
                    Integer u = g.getEdgeSource(e);
                    Integer v = g.getEdgeTarget(e);
                    double rWeight;
                    if (u >= v) {
                        rWeight = (n + 1) + 2 * (n + 1) * rng.nextDouble();
                    } else {
                        rWeight = rng.nextDouble();
                        if (rng.nextDouble() < 0.5) {
                            rWeight *= -1;
                        }
                    }
                    g.setEdgeWeight(e, rWeight);
                }

                try {
                    // run Johnson algorithm
                    JohnsonShortestPaths<Integer, DefaultWeightedEdge> fw =
                        new JohnsonShortestPaths<>(g);

                    // run Floyd-Warshall algorithm
                    FloydWarshallShortestPaths<Integer, DefaultWeightedEdge> fw1 =
                        new FloydWarshallShortestPaths<>(g);

                    for (Integer v : g.vertexSet()) {
                        for (Integer u : g.vertexSet()) {
                            // compare with Dijkstra
                            assertEquals(
                                fw1.getPath(v, u).getWeight(), fw.getPath(v, u).getWeight(), 1e-9);
                        }
                    }
                } catch (RuntimeException e) {
                    // negative weight cycle, skip test
                    assertEquals("Graph contains a negative-weight cycle", e.getMessage());
                }
            }
        }

    }

}
