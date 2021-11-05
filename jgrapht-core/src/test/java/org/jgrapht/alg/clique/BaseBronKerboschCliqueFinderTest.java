/*
 * (C) Copyright 2005-2021, by John V Sichi and Contributors.
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
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

/**
 * .
 *
 * @author John V. Sichi
 */
public abstract class BaseBronKerboschCliqueFinderTest
{
    protected static final String V1 = "v1";
    protected static final String V2 = "v2";
    protected static final String V3 = "v3";
    protected static final String V4 = "v4";
    protected static final String V5 = "v5";
    protected static final String V6 = "v6";
    protected static final String V7 = "v7";
    protected static final String V8 = "v8";
    protected static final String V9 = "v9";
    protected static final String V10 = "v10";

    protected abstract BaseBronKerboschCliqueFinder<String, DefaultEdge> createFinder1(
        Graph<String, DefaultEdge> graph);

    protected abstract BaseBronKerboschCliqueFinder<Object, DefaultEdge> createFinder2(
        Graph<Object, DefaultEdge> graph);

    protected abstract BaseBronKerboschCliqueFinder<Object, DefaultEdge> createFinder2(
        Graph<Object, DefaultEdge> graph, long timeout, TimeUnit unit);

    @Test
    public void testFindBiggest()
    {
        SimpleGraph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        createGraph(g);

        BaseBronKerboschCliqueFinder<String, DefaultEdge> finder = createFinder1(g);

        Collection<Set<String>> cliques = new HashSet<>();
        finder.maximumIterator().forEachRemaining(cliques::add);

        assertEquals(2, cliques.size());

        Set<Set<String>> expected = new HashSet<>();

        Set<String> set = new HashSet<>();
        set.add(V1);
        set.add(V2);
        set.add(V3);
        set.add(V4);
        expected.add(set);

        set = new HashSet<>();
        set.add(V1);
        set.add(V2);
        set.add(V9);
        set.add(V10);
        expected.add(set);

        // convert result from Collection to Set because we don't want
        // order to be significant
        Set<Set<String>> actual = new HashSet<>(cliques);

        assertEquals(expected, actual);
    }

    @Test
    public void testFindAll()
    {
        SimpleGraph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        createGraph(g);

        MaximalCliqueEnumerationAlgorithm<String, DefaultEdge> finder = createFinder1(g);

        Collection<Set<String>> cliques = new HashSet<>();
        finder.iterator().forEachRemaining(cliques::add);

        assertEquals(5, cliques.size());

        Set<Set<String>> expected = new HashSet<>();

        Set<String> set = new HashSet<>();
        set.add(V1);
        set.add(V2);
        set.add(V3);
        set.add(V4);
        expected.add(set);

        set = new HashSet<>();
        set.add(V5);
        set.add(V6);
        set.add(V7);
        expected.add(set);

        set = new HashSet<>();
        set.add(V3);
        set.add(V4);
        set.add(V5);
        expected.add(set);

        set = new HashSet<>();
        set.add(V7);
        set.add(V8);
        expected.add(set);

        set = new HashSet<>();
        set.add(V1);
        set.add(V2);
        set.add(V9);
        set.add(V10);
        expected.add(set);

        // convert result from Collection to Set because we don't want
        // order to be significant
        Set<Set<String>> actual = new HashSet<>(cliques);

        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonSimple()
    {
        Graph<String, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        g.addVertex("1");
        g.addVertex("2");
        g.addEdge("1", "2");
        g.addEdge("1", "2");

        MaximalCliqueEnumerationAlgorithm<String, DefaultEdge> finder = createFinder1(g);
        Iterator<Set<String>> it = finder.iterator();
        while (it.hasNext()) {
            it.next();
        }
    }

    public static void createGraph(Graph<String, DefaultEdge> g)
    {
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);
        g.addVertex(V6);
        g.addVertex(V7);
        g.addVertex(V8);
        g.addVertex(V9);
        g.addVertex(V10);

        // biggest clique: { V1, V2, V3, V4 }
        g.addEdge(V1, V2);
        g.addEdge(V1, V3);
        g.addEdge(V1, V4);
        g.addEdge(V2, V3);
        g.addEdge(V2, V4);
        g.addEdge(V3, V4);

        // smaller clique: { V5, V6, V7 }
        g.addEdge(V5, V6);
        g.addEdge(V5, V7);
        g.addEdge(V6, V7);

        // for fun, add an overlapping clique { V3, V4, V5 }
        g.addEdge(V3, V5);
        g.addEdge(V4, V5);

        // make V8 less lonely
        g.addEdge(V7, V8);

        // add one more maximal which is also the biggest { V1, V2, V9, V10 }
        g.addEdge(V1, V9);
        g.addEdge(V1, V10);
        g.addEdge(V2, V9);
        g.addEdge(V2, V10);
        g.addEdge(V9, V10);
    }

    @Test
    public void testComplete()
    {
        final int size = 6;
        Graph<Object, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        CompleteGraphGenerator<Object, DefaultEdge> completeGraphGenerator =
            new CompleteGraphGenerator<>(size);
        completeGraphGenerator.generateGraph(g);

        MaximalCliqueEnumerationAlgorithm<Object, DefaultEdge> finder = createFinder2(g);

        Set<Set<Object>> cliques = new HashSet<>();
        finder.iterator().forEachRemaining(cliques::add);
        assertEquals(1, cliques.size());

        cliques.stream().forEach(clique -> assertEquals(size, clique.size()));
    }

}
