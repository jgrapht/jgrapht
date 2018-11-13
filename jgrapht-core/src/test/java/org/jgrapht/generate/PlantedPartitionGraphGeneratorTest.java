/*
 * (C) Copyright 2018-2018, by Emilio Cruciani and Contributors.
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
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Emilio Cruciani
 */
public class PlantedPartitionGraphGeneratorTest
{
    private final long SEED = 5;

    /* bad inputs */

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeL()
    {
        new PlantedPartitionGraphGenerator<>(-5, 10, 0.5, 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeK()
    {
        new PlantedPartitionGraphGenerator<>(5, -10, 0.5, 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeP()
    {
        new PlantedPartitionGraphGenerator<>(5, 10, -0.5, 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeQ()
    {
        new PlantedPartitionGraphGenerator<>(5, 10, 0.5, -0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooLargeP()
    {
        new PlantedPartitionGraphGenerator<>(5, 10, 1.5, 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooLargeQ()
    {
        new PlantedPartitionGraphGenerator<>(5, 10, 0.5, 1.1);
    }

    @Test
    public void testSelfLoopContradiction()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new PlantedPartitionGraphGenerator<>(5, 10, 0.5, 0.1, true);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        try {
            gen.generateGraph(g);
            fail("gen.generateGraph() did not throw an IllegalArgumentException as expected");
        } catch (IllegalArgumentException e) {
        }
    }

    /* empty graphs */

    @Test
    public void testZeroL()
    {
        int l = 0;
        int k = 10;
        double p = 0.5;
        double q = 0.1;
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        gen.generateGraph(g);
        assertEquals(0, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());
    }

    @Test
    public void testZeroK()
    {
        int l = 5;
        int k = 0;
        double p = 0.5;
        double q = 0.1;
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        gen.generateGraph(g);
        assertEquals(0, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());
    }

    /* simple graphs */

    @Test
    public void testZeroPSimple()
    {
        int l = 5;
        int k = 10;
        double p = 0.0;
        double q = 0.1;
        int edges = k * k * l * (l - 1) / 2;
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        gen.generateGraph(g);
        assertEquals(l * k, g.vertexSet().size());
        assertTrue(g.edgeSet().size() <= edges);
    }

    @Test
    public void testZeroQSimple()
    {
        int l = 5;
        int k = 10;
        double p = 0.5;
        double q = 0.0;
        int edges = l * k * (k - 1) / 2;
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        gen.generateGraph(g);
        assertEquals(l * k, g.vertexSet().size());
        assertTrue(g.edgeSet().size() <= edges);
    }

    @Test
    public void testOnePSimple()
    {
        int l = 5;
        int k = 10;
        double p = 1.0;
        double q = 0.1;
        int edges = l * k * (k - 1) / 2;
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        gen.generateGraph(g);
        assertEquals(l * k, g.vertexSet().size());
        assertTrue(g.edgeSet().size() >= edges);
    }

    @Test
    public void testOneQSimple()
    {
        int l = 5;
        int k = 10;
        double p = 0.5;
        double q = 1.0;
        int edges = k * k * l * (l - 1) / 2;
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        gen.generateGraph(g);
        assertEquals(l * k, g.vertexSet().size());
        assertTrue(g.edgeSet().size() >= edges);
    }

    /* directed graphs */

    @Test
    public void testZeroPDefault()
    {
        int l = 5;
        int k = 10;
        double p = 0.0;
        double q = 0.1;
        int edges = k * k * l * (l - 1);
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        gen.generateGraph(g);
        assertEquals(l * k, g.vertexSet().size());
        assertTrue(g.edgeSet().size() <= edges);
    }

    @Test
    public void testZeroQDefault()
    {
        int l = 5;
        int k = 10;
        double p = 0.5;
        double q = 0.0;
        int edges = l * k * (k - 1);
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        gen.generateGraph(g);
        assertEquals(l * k, g.vertexSet().size());
        assertTrue(g.edgeSet().size() <= edges);
    }

    @Test
    public void testOnePDefault()
    {
        int l = 5;
        int k = 10;
        double p = 1.0;
        double q = 0.1;
        int edges = l * k * (k - 1);
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        gen.generateGraph(g);
        assertEquals(l * k, g.vertexSet().size());
        assertTrue(g.edgeSet().size() >= edges);
    }

    @Test
    public void testOneQDefault()
    {
        int l = 5;
        int k = 10;
        double p = 0.5;
        double q = 1.0;
        int edges = k * k * l * (l - 1);
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        gen.generateGraph(g);
        assertEquals(l * k, g.vertexSet().size());
        assertTrue(g.edgeSet().size() >= edges);
    }

    /* complete graphs */

    @Test
    public void testCompleteSimpleGraph()
    {
        int l = 5;
        int k = 10;
        double p = 1.0;
        double q = 1.0;
        int d = l * k - 1;
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        gen.generateGraph(g);
        assertEquals(l * k, g.vertexSet().size());
        for (Integer v : g.vertexSet()) {
            assertEquals(d, g.degreeOf(v));
        }
    }

    @Test
    public void testCompleteDefaultDirectedGraph()
    {
        int l = 5;
        int k = 10;
        double p = 1.0;
        double q = 1.0;
        int d = 2 * (l * k - 1);
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        gen.generateGraph(g);
        assertEquals(l * k, g.vertexSet().size());
        for (Integer v : g.vertexSet()) {
            assertEquals(d, g.degreeOf(v));
        }
    }

    /* test getCommunities() */
    @Test
    public void testGetCommunities()
    {
        int l = 5;
        int k = 10;
        double p = 0.5;
        double q = 0.1;

        List<Set<Integer>> groundTruthCommunities = new ArrayList<>(l);
        for (int i = 0; i < l; i++) {
            groundTruthCommunities.add(new LinkedHashSet<>(k));
            for (int j = 0; j < k; j++) {
                groundTruthCommunities.get(i).add(i * k + j);
            }
        }

        PlantedPartitionGraphGenerator<Integer, DefaultEdge> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        gen.generateGraph(g);
        assertEquals(groundTruthCommunities, gen.getCommunities());
    }

    @Test
    public void testCallGetCommunitiesBeforeGenerateGraph()
    {
        int l = 5;
        int k = 10;
        double p = 0.5;
        double q = 0.1;

        PlantedPartitionGraphGenerator<Integer, DefaultEdge> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        try {
            List<Set<Integer>> communities = gen.getCommunities();
            fail("gen.getCommunities() did not throw an IllegalStateException as expected");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void testCallGetCommunitiesMoreThanOnce()
    {
        int l = 5;
        int k = 10;
        double p = 0.5;
        double q = 0.1;

        PlantedPartitionGraphGenerator<Integer, DefaultEdge> gen =
            new PlantedPartitionGraphGenerator<>(l, k, p, q, SEED);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        gen.generateGraph(g);
        Graph<Integer, DefaultEdge> f = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), false);
        try {
            gen.generateGraph(f);
            fail("gen.getCommunities() did not throw an IllegalStateException as expected");
        } catch (IllegalStateException e) {
        }
    }

}
