/*
 * (C) Copyright 2007-2018, by Vinayak R Borkar and Contributors.
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
package org.jgrapht.alg;

import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 */
public class TransitiveClosureTest
{
    // ~ Methods ----------------------------------------------------------------

    @Test
    public void testLinearGraph()
    {
        SimpleDirectedGraph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        int N = 10;
        LinearGraphGenerator<Integer, DefaultEdge> gen = new LinearGraphGenerator<>(N);
        gen.generateGraph(graph);
        TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(graph);

        assertEquals(true, graph.edgeSet().size() == ((N * (N - 1)) / 2));
        for (int i = 0; i < N; ++i) {
            for (int j = i + 1; j < N; ++j) {
                assertEquals(true, graph.getEdge(i, j) != null);
            }
        }
    }

    @Test
    public void testRingGraph()
    {
        SimpleDirectedGraph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        int N = 10;
        RingGraphGenerator<Integer, DefaultEdge> gen = new RingGraphGenerator<>(N);
        gen.generateGraph(graph);
        TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(graph);

        assertEquals(true, graph.edgeSet().size() == (N * (N - 1)));
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) {
                assertEquals(true, (i == j) || (graph.getEdge(i, j) != null));
            }
        }
    }

    @Test
    public void testNoVerticesDag()
    {
        DirectedAcyclicGraph<Integer, DefaultEdge> graph =
            new DirectedAcyclicGraph<>(DefaultEdge.class);

        TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(graph);

        assertEquals(0, graph.edgeSet().size());
    }

    @Test
    public void testEmptyDag()
    {
        DirectedAcyclicGraph<Integer, DefaultEdge> graph = new DirectedAcyclicGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        int n = 10;
        EmptyGraphGenerator<Integer, DefaultEdge> gen = new EmptyGraphGenerator<>(n);
        gen.generateGraph(graph);

        TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(graph);

        assertEquals(0, graph.edgeSet().size());
    }

    @Test
    public void testCompleteBipartiteDag()
    {
        DirectedAcyclicGraph<Integer, DefaultEdge> graph = new DirectedAcyclicGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        CompleteBipartiteGraphGenerator<Integer, DefaultEdge> gen =
            new CompleteBipartiteGraphGenerator<>(5, 5);
        gen.generateGraph(graph);

        TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(graph);

        assertEquals(25, graph.edgeSet().size());
    }

    @Test
    public void testLinearGraphForDag()
    {
        DirectedAcyclicGraph<Integer, DefaultEdge> graph = new DirectedAcyclicGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        int n = 10;
        LinearGraphGenerator<Integer, DefaultEdge> gen = new LinearGraphGenerator<>(n);
        gen.generateGraph(graph);

        TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(graph);

        assertEquals((n * (n - 1)) / 2, graph.edgeSet().size());
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                assertNotNull(graph.getEdge(i, j));
            }
        }
    }
}
