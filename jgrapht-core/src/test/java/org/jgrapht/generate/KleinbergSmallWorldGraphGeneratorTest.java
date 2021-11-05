/*
 * (C) Copyright 2017-2021, by Dimitrios Michail and Contributors.
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
 * @author Dimitrios Michail
 */
public class KleinbergSmallWorldGraphGeneratorTest
{
    @Test
    public void testBadParameters()
    {
        try {
            new KleinbergSmallWorldGraphGenerator<>(-1, 1, 1, 1);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new KleinbergSmallWorldGraphGenerator<>(5, 0, 1, 1);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new KleinbergSmallWorldGraphGenerator<>(5, 9, 1, 1);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new KleinbergSmallWorldGraphGenerator<>(5, 1, -1, 1);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new KleinbergSmallWorldGraphGenerator<>(5, 1, 1, -1);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testUndirected()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new KleinbergSmallWorldGraphGenerator<>(5, 2, 3, 2, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(25, g.vertexSet().size());
    }

    @Test
    public void testDirected()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new KleinbergSmallWorldGraphGenerator<>(5, 2, 3, 2, seed);
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(25, g.vertexSet().size());
    }

    @Test
    public void testDirectedWithUniform()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new KleinbergSmallWorldGraphGenerator<>(5, 2, 3, 0, seed);
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        gen.generateGraph(g);

        assertEquals(25, g.vertexSet().size());
    }

}
