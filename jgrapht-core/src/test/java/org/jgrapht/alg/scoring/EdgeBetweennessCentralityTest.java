/*
 * (C) Copyright 2020-2020, by Dimitrios Michail and Contributors.
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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.EdgeBetweennessCentrality.OverflowStrategy;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

/**
 * Unit tests for {@link EdgeBetweennessCentrality}
 * 
 * @author Dimitrios Michail
 */
public class EdgeBetweennessCentralityTest
{
    @Test
    public void testUndirectedGraph1()
    {
        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(false).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createStringSupplier()).buildGraph();

        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addVertex("D");
        g.addVertex("E");
        g.addVertex("F");
        g.addVertex("G");
        g.addVertex("H");

        g.addEdge("A", "B");
        g.addEdge("A", "C");
        g.addEdge("A", "D");
        g.addEdge("B", "C");
        g.addEdge("B", "D");
        g.addEdge("C", "D");

        DefaultEdge e_D_E = g.addEdge("D", "E");

        g.addEdge("E", "F");
        DefaultEdge e_E_G = g.addEdge("E", "G");
        g.addEdge("F", "G");
        g.addEdge("F", "H");
        g.addEdge("G", "H");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(g);

        assertEquals(16.0, ebc.getEdgeScore(e_D_E), 1e-9);
        assertEquals(7.5, ebc.getEdgeScore(e_E_G), 1e-9);
    }

    @Test
    public void testUndirectedGraph2()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(false).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        for (int i = 1; i < 15; i++) {
            g.addVertex(i);
        }

        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 3);
        DefaultEdge e3_7 = g.addEdge(3, 7);
        g.addEdge(4, 6);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 7);
        DefaultEdge e7_8 = g.addEdge(7, 8);
        g.addEdge(8, 9);
        g.addEdge(8, 12);
        g.addEdge(9, 10);
        DefaultEdge e9_11 = g.addEdge(9, 11);
        g.addEdge(12, 13);
        g.addEdge(12, 14);
        g.addEdge(10, 11);
        DefaultEdge e13_14 = g.addEdge(13, 14);

        EdgeBetweennessCentrality<Integer, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(g);

        assertEquals(33.0, ebc.getEdgeScore(e3_7), 1e-9);
        assertEquals(49.0, ebc.getEdgeScore(e7_8), 1e-9);
        assertEquals(12.0, ebc.getEdgeScore(e9_11), 1e-9);
        assertEquals(1.0, ebc.getEdgeScore(e13_14), 1e-9);
    }

    @Test
    public void testDirectedGraph3()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .directed().allowingMultipleEdges(false).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        for (int i = 1; i < 15; i++) {
            g.addVertex(i);
        }

        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 3);
        DefaultEdge e3_7 = g.addEdge(3, 7);
        g.addEdge(4, 6);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 7);
        DefaultEdge e7_8 = g.addEdge(7, 8);
        g.addEdge(8, 9);
        g.addEdge(8, 12);
        g.addEdge(9, 10);
        DefaultEdge e9_11 = g.addEdge(9, 11);
        g.addEdge(12, 13);
        g.addEdge(12, 14);
        g.addEdge(10, 11);
        DefaultEdge e13_14 = g.addEdge(13, 14);

        EdgeBetweennessCentrality<Integer, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(g);

        assertEquals(24.0, ebc.getEdgeScore(e3_7), 1e-9);
        assertEquals(49.0, ebc.getEdgeScore(e7_8), 1e-9);
        assertEquals(9.0, ebc.getEdgeScore(e9_11), 1e-9);
        assertEquals(1.0, ebc.getEdgeScore(e13_14), 1e-9);
    }

    @Test
    public void testDirectedGraph3Subset()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .directed().allowingMultipleEdges(false).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        for (int i = 1; i < 15; i++) {
            g.addVertex(i);
        }

        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 3);
        DefaultEdge e3_7 = g.addEdge(3, 7);
        g.addEdge(4, 6);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 7);
        DefaultEdge e7_8 = g.addEdge(7, 8);
        g.addEdge(8, 9);
        g.addEdge(8, 12);
        g.addEdge(9, 10);
        DefaultEdge e9_11 = g.addEdge(9, 11);
        g.addEdge(12, 13);
        g.addEdge(12, 14);
        g.addEdge(10, 11);
        DefaultEdge e13_14 = g.addEdge(13, 14);

        EdgeBetweennessCentrality<Integer, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(
            g, OverflowStrategy.THROW_EXCEPTION_ON_OVERFLOW, List.of(1, 2, 4, 11));

        assertEquals(16.0, ebc.getEdgeScore(e3_7), 1e-9);
        assertEquals(21.0, ebc.getEdgeScore(e7_8), 1e-9);
        assertEquals(3.0, ebc.getEdgeScore(e9_11), 1e-9);
        assertEquals(0.0, ebc.getEdgeScore(e13_14), 1e-9);
    }

    @Test
    public void testUndirectedGraphWithWeights()
    {
        Graph<String,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(false).allowingSelfLoops(true).weighted(true)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createStringSupplier()).buildGraph();

        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addVertex("D");
        g.addVertex("E");
        g.addVertex("F");
        g.addVertex("G");
        g.addVertex("H");

        g.addEdge("A", "B");
        g.addEdge("A", "C");
        g.addEdge("A", "D");
        g.addEdge("B", "C");
        g.addEdge("B", "D");
        g.addEdge("C", "D");

        DefaultEdge e_D_E = g.addEdge("D", "E");
        g.setEdgeWeight(e_D_E, 1000.0); // very large

        DefaultEdge e_D_F = g.addEdge("D", "F");

        g.addEdge("E", "F");
        DefaultEdge e_E_G = g.addEdge("E", "G");
        DefaultEdge e_F_G = g.addEdge("F", "G");
        g.addEdge("F", "H");
        g.addEdge("G", "H");

        EdgeBetweennessCentrality<String, DefaultEdge> ebc = new EdgeBetweennessCentrality<>(g);

        assertEquals(0.0, ebc.getEdgeScore(e_D_E), 1e-9);
        assertEquals(16.0, ebc.getEdgeScore(e_D_F), 1e-9);
        assertEquals(1.5, ebc.getEdgeScore(e_E_G), 1e-9);
        assertEquals(5.0, ebc.getEdgeScore(e_F_G), 1e-9);
    }

}
