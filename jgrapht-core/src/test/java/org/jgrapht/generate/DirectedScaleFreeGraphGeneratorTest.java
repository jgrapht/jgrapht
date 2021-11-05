/*
 * (C) Copyright 2019-2021, by Amr ALHOSSARY and Contributors.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link DirectedScaleFreeGraphGenerator}
 *
 * @author Amr ALHOSSARY
 */
public class DirectedScaleFreeGraphGeneratorTest
{
    private static final long SEED = 17L;

    @Test
    public void testBadParameters()
    {
        try {
            new DirectedScaleFreeGraphGenerator<>(-0.5f, 0.33f, 0.5f, 0.5f, 500, 500, SEED);
            fail("Bad alpha checking");
        } catch (IllegalArgumentException e) {
        }
        try {
            new DirectedScaleFreeGraphGenerator<>(0.33f, -0.5f, 0.5f, 0.5f, 500, 500, SEED);
            fail("Bad gamma checking");
        } catch (IllegalArgumentException e) {
        }
        try {
            new DirectedScaleFreeGraphGenerator<>(0.66f, 0.66f, 0.5f, 0.5f, 500, 500, SEED);
            fail("Bad alpha + gamma checking");
        } catch (IllegalArgumentException e) {
        }
        try {
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, -0.5f, 0.5f, 500, 500, SEED);
            fail("Bad deltaIn checking");
        } catch (IllegalArgumentException e) {
        }
        try {
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, -0.5f, 500, 500, SEED);
            fail("Bad deltaOut checking");
        } catch (IllegalArgumentException e) {
        }
        try {
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, -1, -1, SEED);
            fail("Bad target checking");
        } catch (IllegalArgumentException e) {
        }
        try {
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, 500, 500, null);
            fail("Bad random number generator checking");
        } catch (NullPointerException e) {
        }

    }

    @Test
    public void testIncompatibleGraph()
    {
        DirectedScaleFreeGraphGenerator<Integer, DefaultEdge> generator =
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, 1000, 0, SEED);
        generator.setAllowingMultipleEdges(true);
        generator.setAllowingSelfLoops(false);
        DefaultDirectedGraph<Integer, DefaultEdge> g =
            new DefaultDirectedGraph<Integer, DefaultEdge>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        try {
            generator.generateGraph(g);
            fail("Bad checking for allowingMultipleEdges");
        } catch (IllegalArgumentException e) {
        }

        generator = new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, 1000, 0, SEED);
        generator.setAllowingMultipleEdges(false);
        generator.setAllowingSelfLoops(true);
        DirectedMultigraph<Integer, DefaultEdge> directedMultigraph =
            new DirectedMultigraph<Integer, DefaultEdge>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        try {
            generator.generateGraph(directedMultigraph);
            fail("Bad checking for allowingSelfLoops");
        } catch (IllegalArgumentException e) {
        }

    }

    @Test
    public void testNumberOfEdges()
    {
        DirectedScaleFreeGraphGenerator<Integer, DefaultEdge> generator =
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, 1000, 0, SEED);
        generator.setAllowingMultipleEdges(false);
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(g);
        assertEquals(1000, g.edgeSet().size());
    }

    @Test
    public void testNumberOfNodes()
    {
        DirectedScaleFreeGraphGenerator<Integer, DefaultEdge> generator =
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, -1, 1000, SEED);
        generator.setAllowingMultipleEdges(false);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(g);
        assertEquals(1000, g.vertexSet().size());
    }

    @Test
    public void testZeroCases()
    {
        DirectedScaleFreeGraphGenerator<Integer, DefaultEdge> generator =
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, -1, 0, SEED);
        generator.setAllowingMultipleEdges(false);
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(g);
        assertEquals(0, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());

        generator = new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, 0, 0, SEED);
        g = new DirectedPseudograph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(g);
        assertEquals(0, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());
    }

    @Test
    public void testNoOutDegreeZero()
    {
        DirectedScaleFreeGraphGenerator<Integer, DefaultEdge> generator =
            new DirectedScaleFreeGraphGenerator<>(0.3f, 0.0f, 0.5f, 0.5f, -1, 1000, SEED);
        generator.setAllowingMultipleEdges(false);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(g);
        long outDegreeZero = g.vertexSet().stream().filter(v -> g.outDegreeOf(v) == 0).count();
        assertEquals(0, outDegreeZero);
    }

    @Test
    public void testNoInDegreeZero()
    {
        DirectedScaleFreeGraphGenerator<Integer, DefaultEdge> generator =
            new DirectedScaleFreeGraphGenerator<>(0.0f, 0.3f, 0.5f, 0.5f, -1, 1000, SEED);
        generator.setAllowingMultipleEdges(false);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(g);
        long inDegreeZero = g.vertexSet().stream().filter(v -> g.inDegreeOf(v) == 0).count();
        assertEquals(0, inDegreeZero);
    }

}
