/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.clique;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

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
