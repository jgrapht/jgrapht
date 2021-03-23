/*
 * (C) Copyright 2003-2021, by Barak Naveh and Contributors.
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
package org.jgrapht.alg.util;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * .
 *
 * @author Charles Fry
 */
public class NeighborCacheTest
{
    // ~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";

    // ~ Methods ----------------------------------------------------------------

    @Test
    public void testNeighborSet()
    {
        // We use Object instead of DefaultEdge for the edge type
        // in order to cover the case in
        // https://sourceforge.net/tracker/index.php?func=detail&aid=3486775&group_id=86459&atid=579687
        ListenableGraph<String, Object> g =
            new DefaultListenableGraph<>(new SimpleGraph<>(Object.class));

        NeighborCache<String, Object> cache = new NeighborCache<>(g);
        g.addGraphListener(cache);

        g.addVertex(V1);
        g.addVertex(V2);

        g.addEdge(V1, V2);

        Set<String> neighbors1 = cache.neighborsOf(V1);

        assertEquals(1, neighbors1.size());
        assertEquals(true, neighbors1.contains(V2));

        g.addVertex(V3);
        g.addEdge(V3, V1);

        Set<String> neighbors3 = cache.neighborsOf(V3);

        assertEquals(2, neighbors1.size());
        assertEquals(true, neighbors1.contains(V3));

        assertEquals(1, neighbors3.size());
        assertEquals(true, neighbors3.contains(V1));

        g.removeEdge(V3, V1);

        assertEquals(1, neighbors1.size());
        assertEquals(false, neighbors1.contains(V3));

        assertEquals(0, neighbors3.size());

        g.removeVertex(V2);

        assertEquals(0, neighbors1.size());
    }

    @Test
    public void testDirectedNeighborSet()
    {
        ListenableGraph<String, Object> g =
            new DefaultListenableGraph<>(new DefaultDirectedGraph<>(Object.class));
        g.addVertex(V1);
        g.addVertex(V2);

        g.addEdge(V1, V2);

        NeighborCache<String, Object> index = new NeighborCache<>(g);
        g.addGraphListener(index);

        Set<String> p = index.predecessorsOf(V1);
        Set<String> s = index.successorsOf(V1);

        assertEquals(0, p.size());
        assertEquals(1, s.size());
        assertEquals(true, s.contains(V2));

        g.addVertex(V3);
        g.addEdge(V3, V1);

        Set<String> q = index.successorsOf(V3);

        assertEquals(1, p.size());
        assertEquals(1, s.size());
        assertEquals(true, p.contains(V3));

        assertEquals(1, q.size());
        assertEquals(true, q.contains(V1));

        g.removeEdge(V3, V1);

        assertEquals(0, q.size());
        assertEquals(0, p.size());

        g.removeVertex(V2);

        assertEquals(0, s.size());
    }

    @Test
    public void testVertexRemoval()
    {
        ListenableGraph<String, DefaultEdge> graph =
            new DefaultListenableGraph<>(new SimpleGraph<>(DefaultEdge.class));

        final String a = "A";
        final String b = "B";
        final String c = "C";
        final String d = "D";

        NeighborCache<String, DefaultEdge> cache = new NeighborCache<>(graph);

        graph.addGraphListener(cache);

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);

        graph.addEdge(d, a);
        graph.addEdge(d, b);
        graph.addEdge(d, c);

        Set<String> neighborsOfD = cache.neighborsOf(d);
        Set<String> neighborsOfC = cache.neighborsOf(c);
        Set<String> neighborsOfB = cache.neighborsOf(b);
        Set<String> neighborsOfA = cache.neighborsOf(a);

        assertThat(neighborsOfD, hasItems(a, b, c));
        assertThat(neighborsOfA.size(), is(1));
        assertThat(neighborsOfB.size(), is(1));
        assertThat(neighborsOfC.size(), is(1));

        graph.removeVertex(d);

        assertTrue(neighborsOfD.isEmpty());

        assertThat(neighborsOfA.size(), is(0));
        assertThat(neighborsOfB.size(), is(0));
        assertThat(neighborsOfC.size(), is(0));

    }

    @Test
    public void testNeighborListCreation()
    {
        ListenableGraph<String, DefaultEdge> graph =
            new DefaultListenableGraph<>(new SimpleGraph<>(DefaultEdge.class));

        final String a = "A";
        final String b = "B";
        final String c = "C";
        final String d = "D";

        NeighborCache<String, DefaultEdge> cache = new NeighborCache<>(graph);

        graph.addGraphListener(cache);

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);

        graph.addEdge(d, a);
        graph.addEdge(d, b);
        graph.addEdge(d, c);

        assertThat(cache.neighborListOf(b), hasItems(d));
        assertThat(cache.neighborListOf(b).size(), is(1));

        graph.addEdge(a, b);

        assertThat(cache.neighborListOf(b), hasItems(a, d));
        assertThat(cache.neighborListOf(b).size(), is(2));

        graph.removeEdge(d, b);

        assertThat(cache.neighborListOf(b), hasItems(a));
        assertThat(cache.neighborListOf(b).size(), is(1));

        graph.removeVertex(b);

        assertThrows(IllegalArgumentException.class, () -> cache.neighborListOf(b));
    }

}
