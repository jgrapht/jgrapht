/*
 * (C) Copyright 2016-2018, by Barak Naveh and Contributors.
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
package org.jgrapht.alg.lca;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

/**
 * Tests for the {@link NaiveLCAFinder}
 *
 * @author Barak Naveh
 * @author Alexandru Valeanu
 */
public class NaiveLCAFinderTest
{

    private static <V,
        E> void checkLcas(NaiveLCAFinder<V, E> finder, V a, V b, Collection<V> expectedSet)
    {
        Set<V> lcaSet = finder.getLCASet(a, b);
        Assert.assertTrue(lcaSet.containsAll(expectedSet));
        Assert.assertEquals(lcaSet.size(), expectedSet.size());
    }

    @Test
    public void testNormalCases()
    {
        SimpleDirectedGraph<String, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        g.addVertex("g");
        g.addVertex("h");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("c", "d");
        g.addEdge("d", "e");
        g.addEdge("b", "f");
        g.addEdge("b", "g");
        g.addEdge("f", "e");
        g.addEdge("e", "h");

        NaiveLCAFinder<String, DefaultEdge> finder = new NaiveLCAFinder<>(g);

        Assert.assertEquals("f", finder.getLCA("f", "h"));
        Assert.assertEquals("f", finder.getLCA("h", "f"));
        Assert.assertEquals("b", finder.getLCA("g", "h"));
        Assert.assertEquals("c", finder.getLCA("c", "c"));
        Assert.assertEquals("a", finder.getLCA("a", "e")); // tests one path not descending

        checkLcas(finder, "f", "h", Arrays.asList("f"));
        checkLcas(finder, "h", "f", Arrays.asList("f"));
        checkLcas(finder, "g", "h", Arrays.asList("b"));
        checkLcas(finder, "c", "c", Arrays.asList("c"));
        checkLcas(finder, "a", "e", Arrays.asList("a"));
    }

    @Test
    public void testNoLca()
    {
        SimpleDirectedGraph<String, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        g.addVertex("g");
        g.addVertex("h");
        g.addVertex("i");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("c", "d");
        g.addEdge("d", "e");
        g.addEdge("f", "g");
        g.addEdge("f", "h");
        g.addEdge("g", "i");
        g.addEdge("h", "i");

        NaiveLCAFinder<String, DefaultEdge> finder = new NaiveLCAFinder<>(g);

        Assert.assertNull(finder.getLCA("i", "e"));
        Assert.assertTrue(finder.getLCASet("i", "e").isEmpty());
    }

    @Test
    public void testLoops()
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        g.addVertex("g");
        g.addVertex("h");
        g.addVertex("i");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("c", "d");
        g.addEdge("d", "e");
        g.addEdge("b", "f");
        g.addEdge("b", "g");
        g.addEdge("f", "e");
        g.addEdge("e", "h");
        g.addEdge("h", "e");
        g.addEdge("h", "h");
        g.addEdge("i", "i");

        NaiveLCAFinder<String, DefaultEdge> finder = new NaiveLCAFinder<>(g);

        Assert.assertEquals("f", finder.getLCA("h", "f"));
        Assert.assertNull(finder.getLCA("a", "i"));

        checkLcas(finder, "h", "f", Arrays.asList("f"));
        Assert.assertTrue(finder.getLCASet("a", "i").isEmpty());
    }

    @Test
    public void testArrivalOrder()
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("g");
        g.addVertex("e");
        g.addVertex("h");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("a", "g");
        g.addEdge("b", "g");
        g.addEdge("g", "e");
        g.addEdge("e", "h");

        NaiveLCAFinder<String, DefaultEdge> finder = new NaiveLCAFinder<>(g);

        Assert.assertEquals("b", finder.getLCA("b", "h"));
        Assert.assertEquals("b", finder.getLCA("c", "e"));

        checkLcas(finder, "b", "h", Arrays.asList("b"));
        checkLcas(finder, "c", "e", Arrays.asList("b"));
    }

    @Test
    public void testTwoLcas()
    {

        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");

        g.addEdge("a", "c");
        g.addEdge("a", "d");
        g.addEdge("b", "c");
        g.addEdge("b", "d");

        NaiveLCAFinder<String, DefaultEdge> finder = new NaiveLCAFinder<>(g);

        checkLcas(finder, "c", "d", Arrays.asList("a", "b"));
    }

}
