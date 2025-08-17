/*
 * (C) Copyright 2018-2023, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PruferTreeGenerator}
 *
 * @author Alexandru Valeanu
 */
public class PruferTreeGeneratorTest
{

    @Test
    public void testNullPruferSequence()
    {
        assertThrows(IllegalArgumentException.class, () -> new PruferTreeGenerator<>(null));
    }

    @Test
    public void testEmptyPruferSequence()
    {
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        PruferTreeGenerator<Integer, DefaultEdge> generator =
            new PruferTreeGenerator<>(new int[] {});

        generator.generateGraph(tree);
        assertEquals(2, tree.vertexSet().size());
    }

    @Test
    public void testInvalidPruferSequence()
    {
        assertThrows(IllegalArgumentException.class, () -> new PruferTreeGenerator<>(new int[] { 10 }));
    }

    @Test
    public void testPruferSequence()
    {
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        PruferTreeGenerator<Integer, DefaultEdge> generator =
            new PruferTreeGenerator<>(new int[] { 4, 4, 4, 5 });

        generator.generateGraph(tree);

        assertEquals(6, tree.vertexSet().size());

        int[] degrees = tree.vertexSet().stream().mapToInt(tree::degreeOf).toArray();
        Arrays.sort(degrees);

        assertArrayEquals(new int[] { 1, 1, 1, 1, 2, 4 }, degrees);
    }

    @Test
    public void testZeroVertices()
    {
        assertThrows(IllegalArgumentException.class, () -> new PruferTreeGenerator<>(0));
    }

    @Test
    public void testNullRNG()
    {
        assertThrows(NullPointerException.class, () -> new PruferTreeGenerator<>(100, null));
    }

    @Test
    public void testDirectedGraph()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            Graph<Integer, DefaultEdge> tree = new DirectedAcyclicGraph<>(
                SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            PruferTreeGenerator<Integer, DefaultEdge> generator = new PruferTreeGenerator<>(10);

            generator.generateGraph(tree);
        });
    }

    @Test
    public void testNullGraph()
    {
        assertThrows(NullPointerException.class, () -> {
            PruferTreeGenerator<Integer, DefaultEdge> generator = new PruferTreeGenerator<>(10);

            generator.generateGraph(null);
        });
    }

    @Test
    public void testOneVertex()
    {
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        PruferTreeGenerator<Integer, DefaultEdge> generator = new PruferTreeGenerator<>(1, 0x99);

        generator.generateGraph(tree);
        assertTrue(GraphTests.isTree(tree));
    }

    @Test
    public void testExistingVertices()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            CompleteGraphGenerator<Integer, DefaultEdge> completeGraphGenerator =
                new CompleteGraphGenerator<>(10);

            completeGraphGenerator.generateGraph(tree);

            PruferTreeGenerator<Integer, DefaultEdge> generator = new PruferTreeGenerator<>(100, 0x99);

            generator.generateGraph(tree);
        });
    }

    @Test
    public void testRandomSizes()
    {
        Random random = new Random(0x88);
        final int numTests = 500;

        for (int test = 0; test < numTests; test++) {
            Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

            PruferTreeGenerator<Integer, DefaultEdge> generator =
                new PruferTreeGenerator<>(1 + random.nextInt(5000), random);

            generator.generateGraph(tree);
            assertTrue(GraphTests.isTree(tree));
        }
    }

    @Test
    public void testHugeSize()
    {
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        PruferTreeGenerator<Integer, DefaultEdge> generator =
            new PruferTreeGenerator<>(100_000, 0x99);

        generator.generateGraph(tree);
        assertTrue(GraphTests.isTree(tree));
    }
}
