/*
 * (C) Copyright 2009-2021, by Tom Larkworthy and Contributors.
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
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Tom Larkworthy
 */
public class FloydWarshallShortestPathsTest
{
    // ~ Methods ----------------------------------------------------------------

    @Test
    public void testCompareWithDijkstra()
    {
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> gen =
            new GnmRandomGraphGenerator<>(10, 15);

        for (int i = 0; i < 10; i++) {
            // Generate directed graph
            SimpleDirectedGraph<Integer,
                DefaultWeightedEdge> directed = new SimpleDirectedGraph<>(
                    SupplierUtil.createIntegerSupplier(),
                    SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER, false);
            gen.generateGraph(directed);

            // setup our shortest path measurer
            FloydWarshallShortestPaths<Integer, DefaultWeightedEdge> fw =
                new FloydWarshallShortestPaths<>(directed);

            for (Integer v1 : directed.vertexSet()) {
                for (Integer v2 : directed.vertexSet()) {

                    GraphPath<Integer, DefaultWeightedEdge> dPath =
                        new DijkstraShortestPath<>(directed).getPath(v1, v2);
                    if (dPath == null) {
                        assertNull(fw.getPath(v1, v2));
                    } else {
                        double fwSp = fw.getPathWeight(v1, v2);
                        double dijSp = dPath.getWeight();
                        assertTrue(
                            (Math.abs(dijSp - fwSp) < .01)
                                || (Double.isInfinite(fwSp) && Double.isInfinite(dijSp)));
                        GraphPath<Integer, DefaultWeightedEdge> path = fw.getPath(v1, v2);
                        if (!path.getEdgeList().isEmpty()) {
                            this.verifyPath(directed, path, fw.getPathWeight(v1, v2));
                        }
                    }
                }
            }

            // Generate Undirected graph
            SimpleGraph<Integer,
                DefaultWeightedEdge> undirected = new SimpleGraph<>(
                    SupplierUtil.createIntegerSupplier(),
                    SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER, false);
            gen.generateGraph(undirected);

            // setup our shortest path measurer
            fw = new FloydWarshallShortestPaths<>(undirected);

            for (Integer v1 : undirected.vertexSet()) {
                for (Integer v2 : undirected.vertexSet()) {
                    GraphPath<Integer, DefaultWeightedEdge> dPath =
                        new DijkstraShortestPath<>(undirected).getPath(v1, v2);

                    if (dPath == null) {
                        assertNull(fw.getPath(v1, v2));
                    } else {
                        double fwSp = fw.getPathWeight(v1, v2);
                        double dijSp = dPath.getWeight();
                        assertTrue(
                            (Math.abs(dijSp - fwSp) < .01)
                                || (Double.isInfinite(fwSp) && Double.isInfinite(dijSp)));
                        GraphPath<Integer, DefaultWeightedEdge> path = fw.getPath(v1, v2);
                        if (!path.getEdgeList().isEmpty()) {
                            this.verifyPath(undirected, path, fw.getPathWeight(v1, v2));
                            List<Integer> vertexPath = path.getVertexList();
                            assertEquals(fw.getFirstHop(v1, v2), vertexPath.get(1));
                            assertEquals(
                                fw.getLastHop(v1, v2), vertexPath.get(vertexPath.size() - 2));
                        }
                    }

                }
            }
        }
    }

    /**
     * Verify whether the path calculated by FloydWarshallShortestPaths is an actual valid path.
     */
    private <V, E> void verifyPath(Graph<V, E> graph, GraphPath<V, E> path, double pathCost)
    {
        assertEquals(pathCost, path.getWeight(), .00000001);
        double verifiedEdgeCost = 0;
        List<V> vertexList = new ArrayList<>();
        vertexList.add(path.getStartVertex());

        V v = path.getStartVertex();
        for (E e : path.getEdgeList()) {
            assertNotNull(e);
            verifiedEdgeCost += graph.getEdgeWeight(e);
            try {
                v = Graphs.getOppositeVertex(graph, e, v);
            } catch (IllegalArgumentException ex) {
                fail(
                    "Invalid path encountered: the sequence of edges does not present a valid path through the graph");
            }
        }
        assertEquals(pathCost, verifiedEdgeCost, .00000001);
        assertEquals(path.getStartVertex(), path.getVertexList().get(0));
        assertEquals(path.getEndVertex(), path.getVertexList().get(path.getLength()));
    }

    @Test
    public void testWeightedEdges()
    {
        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> weighted =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        weighted.addVertex("a");
        weighted.addVertex("b");
        DefaultWeightedEdge edge = weighted.addEdge("a", "b");
        weighted.setEdgeWeight(edge, 5.0);
        FloydWarshallShortestPaths<String, DefaultWeightedEdge> fw =
            new FloydWarshallShortestPaths<>(weighted);
        double sD = fw.getPathWeight("a", "b");
        assertEquals(5.0, sD, 0.1);
        GraphPath<String, DefaultWeightedEdge> path = fw.getPath("a", "b");
        assertNotNull(path);
        assertEquals(Collections.singletonList(edge), path.getEdgeList());
        assertEquals("a", path.getStartVertex());
        assertEquals("b", path.getEndVertex());
        assertEquals(5.0, path.getWeight(), 0);
        assertEquals(weighted, path.getGraph());
        List<String> vertexPath = path.getVertexList();
        assertEquals(fw.getFirstHop("a", "b"), vertexPath.get(1));
        assertEquals(fw.getLastHop("a", "b"), vertexPath.get(vertexPath.size() - 2));
        assertNull(fw.getPath("b", "a"));
    }
}
