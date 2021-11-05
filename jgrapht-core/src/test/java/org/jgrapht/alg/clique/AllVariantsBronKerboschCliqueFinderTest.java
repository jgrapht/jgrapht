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
package org.jgrapht.alg.clique;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Test that all Bron-Kerbosch variants return the same results.
 * 
 * @author Dimitrios Michail
 */
public class AllVariantsBronKerboschCliqueFinderTest
{

    @Test
    public void testRandomInstances()
    {
        final Random rng = new Random(33);
        final double edgeProbability = 0.5;
        final int numberVertices = 30;
        final int repeat = 10;

        GraphGenerator<Integer, DefaultEdge, Integer> gg =
            new GnpRandomGraphGenerator<Integer, DefaultEdge>(
                numberVertices, edgeProbability, rng, false);

        for (int i = 0; i < repeat; i++) {
            Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            gg.generateGraph(g);

            Iterable<Set<Integer>> alg1 = new BronKerboschCliqueFinder<>(g);
            Iterable<Set<Integer>> alg2 = new PivotBronKerboschCliqueFinder<>(g);
            Iterable<Set<Integer>> alg3 = new DegeneracyBronKerboschCliqueFinder<>(g);

            Set<Set<Integer>> cliques1 = new HashSet<>();
            for (Set<Integer> c : alg1) {
                cliques1.add(c);
            }

            Set<Set<Integer>> cliques2 = new HashSet<>();
            for (Set<Integer> c : alg2) {
                cliques2.add(c);
            }

            Set<Set<Integer>> cliques3 = new HashSet<>();
            for (Set<Integer> c : alg3) {
                cliques3.add(c);
            }

            assertEquals(cliques1.size(), cliques2.size());
            assertEquals(cliques2.size(), cliques3.size());
            assertEquals(cliques1, cliques2);
            assertEquals(cliques2, cliques3);
        }
    }

}
