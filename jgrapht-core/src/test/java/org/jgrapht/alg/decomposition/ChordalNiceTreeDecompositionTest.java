/*
 * (C) Copyright 2018-2018, by Ira Justus Fesefeldt and Contributors.
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
package org.jgrapht.alg.decomposition;


import static org.junit.Assert.assertTrue;

import org.jgrapht.*;
import org.jgrapht.alg.cycle.ChordalityInspector;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.util.SupplierUtil;
import org.junit.*;

/**
 * Tests for the {@link ChordalNiceTreeDecomposition}
 *
 * @author Ira Justus Fesefeldt (PhoenixIra)
 */
public class ChordalNiceTreeDecompositionTest
{

    private Graph<Integer, DefaultEdge> makeCompleteGraph(int n)
    {
        Graph<Integer, DefaultEdge> graph =
            new SimpleGraph<>(SupplierUtil.createIntegerSupplier(), 
                                                  SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        CompleteGraphGenerator<Integer, DefaultEdge> cgGen = new CompleteGraphGenerator<>(n);
        cgGen.generateGraph(graph);
        return graph;
    }

    @Test
    public void testCompleteGraph()
    {
        for (int i = 0; i < 10; i++) {
            Graph<Integer, DefaultEdge> graph = makeCompleteGraph(i);
            ChordalNiceTreeDecomposition<Integer, DefaultEdge> decompBuild =
                new ChordalNiceTreeDecomposition<>(graph);
            assertTrue(NiceTreeDecompositionTestUtil
                .isTreeDecomposition(graph, decompBuild.getDecomposition(), decompBuild.getMap()));
            assertTrue(NiceTreeDecompositionTestUtil.isNiceDecomposition(
                decompBuild.getDecomposition(), decompBuild.getMap(), decompBuild.getRoot()));
        }
    }

    private Graph<Integer, DefaultEdge> makeGraphsWithTriangles(int n)
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(), 
                                                              SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        if (n == 0)
            return graph;
        int freeVertex = 1;
        graph.addVertex(freeVertex++);
        graph.addVertex(freeVertex++);
        graph.addVertex(freeVertex++);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 1);
        for (int i = 1; i < n; i++) {
            graph.addVertex(freeVertex++);
            graph.addEdge(freeVertex - 3, freeVertex - 1);
            graph.addEdge(freeVertex - 2, freeVertex - 1);
        }
        return graph;
    }

    @Test
    public void testGraphsWithTriangles()
    {
        for (int i = 0; i < 10; i++) {
            Graph<Integer, DefaultEdge> graph = makeGraphsWithTriangles(i);
            ChordalNiceTreeDecomposition<Integer, DefaultEdge> decompBuild =
                new ChordalNiceTreeDecomposition<>(graph);
            assertTrue(NiceTreeDecompositionTestUtil
                .isTreeDecomposition(graph, decompBuild.getDecomposition(), decompBuild.getMap()));
            assertTrue(NiceTreeDecompositionTestUtil.isNiceDecomposition(
                decompBuild.getDecomposition(), decompBuild.getMap(), decompBuild.getRoot()));
        }
    }

    private Graph<Integer, DefaultEdge> makeDisconnectedGraph(int n)
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(SupplierUtil.createIntegerSupplier(), 
                                                              SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        for (int i = 0; i < n; i++)
            graph.addVertex(i);
        return graph;
    }

    @Test
    public void testDisconnectedGraph()
    {
        for (int i = 0; i < 10; i++) {
            Graph<Integer, DefaultEdge> graph = makeDisconnectedGraph(i);
            ChordalNiceTreeDecomposition<Integer, DefaultEdge> decompBuild =
                new ChordalNiceTreeDecomposition<>(graph);
            assertTrue(NiceTreeDecompositionTestUtil
                .isTreeDecomposition(graph, decompBuild.getDecomposition(), decompBuild.getMap()));
            assertTrue(NiceTreeDecompositionTestUtil.isNiceDecomposition(
                decompBuild.getDecomposition(), decompBuild.getMap(), decompBuild.getRoot()));
        }
    }

    public Graph<Integer, DefaultEdge> makeLekkerkerkerBolandFamily(int n)
    {
        GraphBuilder<Integer, DefaultEdge, ? extends SimpleGraph<Integer, DefaultEdge>> builder =
            SimpleGraph.createBuilder(SupplierUtil.DEFAULT_EDGE_SUPPLIER);

        builder.addEdge(0, 1);

        for (int i = 3; i < n; i++) {
            builder.addEdge(1, i);
            builder.addEdge(i - 1, i);
        }

        builder.addEdge(n - 1, n);

        return builder.build();
    }

    @Test
    public void testLekkerkerkerBolandFamily()
    {
        for (int i = 0; i < 10; i++) {
            Graph<Integer, DefaultEdge> graph = makeLekkerkerkerBolandFamily(i);
            ChordalNiceTreeDecomposition<Integer, DefaultEdge> decompBuild =
                new ChordalNiceTreeDecomposition<>(graph);
            assertTrue(NiceTreeDecompositionTestUtil
                .isTreeDecomposition(graph, decompBuild.getDecomposition(), decompBuild.getMap()));
            assertTrue(NiceTreeDecompositionTestUtil.isNiceDecomposition(
                decompBuild.getDecomposition(), decompBuild.getMap(), decompBuild.getRoot()));
        }
    }
    
    @Test
    public void testLekkerkerkerBolandFamilyPerfectEliminationOrder()
    {
        for (int i = 0; i < 10; i++) {
            Graph<Integer, DefaultEdge> graph = makeLekkerkerkerBolandFamily(i);
            ChordalityInspector<Integer, DefaultEdge> inspec = new ChordalityInspector<>(graph);
            ChordalNiceTreeDecomposition<Integer, DefaultEdge> decompBuild =
                new ChordalNiceTreeDecomposition<>(graph, inspec.getPerfectEliminationOrder());
            assertTrue(NiceTreeDecompositionTestUtil
                .isTreeDecomposition(graph, decompBuild.getDecomposition(), decompBuild.getMap()));
            assertTrue(NiceTreeDecompositionTestUtil.isNiceDecomposition(
                decompBuild.getDecomposition(), decompBuild.getMap(), decompBuild.getRoot()));
        }
    }

}
