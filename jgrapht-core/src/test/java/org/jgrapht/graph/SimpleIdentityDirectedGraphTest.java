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
package org.jgrapht.graph;

import org.jgrapht.*;
import org.jgrapht.graph.specifics.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import static org.junit.Assert.*;

/**
 * A unit test for simple directed graph when the backing map is an IdentityHashMap
 *
 */
public class SimpleIdentityDirectedGraphTest
{
    public static class Holder<T>
    {
        T t;

        public Holder(T t)
        {
            this.t = t;
        }

        public T getT()
        {
            return t;
        }

        public void setT(T t)
        {
            this.t = t;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Holder<T> holder = TypeUtil.uncheckedCast(o);

            return !(t != null ? !t.equals(holder.t) : holder.t != null);

        }

        @Override
        public int hashCode()
        {
            return t != null ? t.hashCode() : 0;
        }
    }

    public static class SimpleIdentityDirectedGraph<V, E>
        extends
        AbstractBaseGraph<V, E>
    {
        private static final long serialVersionUID = 4600490314100246989L;

        public SimpleIdentityDirectedGraph(Class<? extends E> edgeClass)
        {
            super(
                null, SupplierUtil.createSupplier(edgeClass), DefaultGraphType.directedSimple(),
                new IdentitySpecificsStrategy<>());
        }
    }

    private static class IdentitySpecificsStrategy<V, E>
        implements
        GraphSpecificsStrategy<V, E>
    {

        private static final long serialVersionUID = 1L;

        @Override
        public Function<GraphType,
            IntrusiveEdgesSpecifics<V, E>> getIntrusiveEdgesSpecificsFactory()
        {
            return (Function<GraphType, IntrusiveEdgesSpecifics<V, E>> & Serializable) (type) -> {
                if (type.isWeighted()) {
                    return new WeightedIntrusiveEdgesSpecifics<V, E>(new IdentityHashMap<>());
                } else {
                    return new UniformIntrusiveEdgesSpecifics<>(new IdentityHashMap<>());
                }
            };
        }

        @Override
        public BiFunction<Graph<V, E>, GraphType, Specifics<V, E>> getSpecificsFactory()
        {
            return (BiFunction<Graph<V, E>, GraphType,
                Specifics<V, E>> & Serializable) (graph, type) -> {
                    if (type.isDirected()) {
                        return new DirectedSpecifics<V, E>(
                            graph, new IdentityHashMap<>(), getEdgeSetFactory());
                    } else {
                        return new UndirectedSpecifics<>(
                            graph, new IdentityHashMap<>(), getEdgeSetFactory());
                    }
                };
        }

    }

    // ~ Instance fields --------------------------------------------------------

    Graph<Holder<String>, DefaultEdge> gEmpty;
    private Graph<Holder<String>, DefaultEdge> g1;
    private Graph<Holder<String>, DefaultEdge> g2;
    private Graph<Holder<String>, DefaultEdge> g3;
    private Graph<Holder<String>, DefaultEdge> g4;
    private DefaultEdge eLoop;
    private Supplier<DefaultEdge> eSupplier;
    private Holder<String> v1 = new Holder<>("v1");
    private Holder<String> v2 = new Holder<>("v2");
    private Holder<String> v3 = new Holder<>("v3");
    private Holder<String> v4 = new Holder<>("v4");
    private DefaultEdge e12_1;
    private DefaultEdge e12_2;
    private DefaultEdge e12_3;
    private DefaultEdge e21_1;
    private DefaultEdge e21_2;
    private DefaultEdge e13_1;
    private DefaultEdge e23_1;
    private DefaultEdge e31_1;
    private DefaultEdge e32_1;
    private DefaultEdge e23_2;
    private DefaultEdge e34_1;
    private DefaultEdge e41_1;

    /**
     * Class to test for boolean addEdge(V, V, E)
     */
    @Test
    public void testAddEdgeEdge()
    {
        try {
            g1.addEdge(v1, v1, eLoop); // loops not allowed
            Assert.fail("Should not get here.");
        } catch (IllegalArgumentException e) {
        }

        try {
            g3.addEdge(v1, v1, null);
            Assert.fail("Should not get here.");
        } catch (NullPointerException e) {
        }

        DefaultEdge e = eSupplier.get();

        try {
            g1.addEdge(new Holder<>("ya"), new Holder<>("ya"), e); // no such vertex in graph
            Assert.fail("Should not get here.");
        } catch (IllegalArgumentException ile) {
        }

        assertFalse(g2.addEdge(v2, v1, e));
        assertFalse(g3.addEdge(v2, v1, e));
        assertTrue(g4.addEdge(v2, v1, e));
    }

    /**
     * Class to test for Edge addEdge(Object, Object)
     */
    @Test
    public void testAddEdgeObjectObject()
    {
        try {
            g1.addEdge(v1, v1); // loops not allowed
            Assert.fail("Should not get here.");
        } catch (IllegalArgumentException e) {
        }

        try {
            g3.addEdge(null, null);
            Assert.fail("Should not get here.");
        } catch (NullPointerException e) {
        }

        try {
            g1.addEdge(v2, v1); // no such vertex in graph
            Assert.fail("Should not get here.");
        } catch (IllegalArgumentException ile) {
        }

        assertNull(g2.addEdge(v2, v1));
        assertNull(g3.addEdge(v2, v1));
        assertNotNull(g4.addEdge(v2, v1));
    }

    /**
     * .
     */
    @Test
    public void testAddVertex()
    {
        assertEquals(1, g1.vertexSet().size());
        assertEquals(2, g2.vertexSet().size());
        assertEquals(3, g3.vertexSet().size());
        assertEquals(4, g4.vertexSet().size());

        assertFalse(g1.addVertex(v1));
        assertTrue(g1.addVertex(v2));
        assertEquals(2, g1.vertexSet().size());
    }

    /**
     * Class to test for boolean containsEdge(Edge)
     */
    @Test
    public void testContainsEdgeEdge()
    {
        assertTrue(g2.containsEdge(e12_1));
        assertTrue(g2.containsEdge(e21_1));

        assertTrue(g3.containsEdge(e12_2));
        assertTrue(g3.containsEdge(e21_2));
        assertTrue(g3.containsEdge(e23_1));
        assertTrue(g3.containsEdge(e32_1));
        assertTrue(g3.containsEdge(e31_1));
        assertTrue(g3.containsEdge(e13_1));

        assertTrue(g4.containsEdge(e12_3));
        assertTrue(g4.containsEdge(e23_2));
        assertTrue(g4.containsEdge(e34_1));
        assertTrue(g4.containsEdge(e41_1));
    }

    /**
     * Class to test for boolean containsEdge(Object, Object)
     */
    @Test
    public void testContainsEdgeObjectObject()
    {
        assertFalse(g1.containsEdge(v1, v2));
        assertFalse(g1.containsEdge(v1, v1));

        assertTrue(g2.containsEdge(v1, v2));
        assertTrue(g2.containsEdge(v2, v1));

        assertTrue(g3.containsEdge(v1, v2));
        assertTrue(g3.containsEdge(v2, v1));
        assertTrue(g3.containsEdge(v3, v2));
        assertTrue(g3.containsEdge(v2, v3));
        assertTrue(g3.containsEdge(v1, v3));
        assertTrue(g3.containsEdge(v3, v1));

        assertFalse(g4.containsEdge(v1, v4));
        g4.addEdge(v1, v4);
        assertTrue(g4.containsEdge(v1, v4));

        assertFalse(g3.containsEdge(v4, v2));
        assertFalse(g3.containsEdge(null, null));
    }

    /**
     * .
     */
    @Test
    public void testContainsVertex()
    {
        assertTrue(g1.containsVertex(v1));

        v1.setT("V1");

        assertTrue(g1.containsVertex(v1)); // shows #hashCode is bypassed
    }

    /**
     * .
     */
    @Test
    public void testEdgeSet()
    {
        assertEquals(0, g1.edgeSet().size());

        assertEquals(2, g2.edgeSet().size());
        assertTrue(g2.containsEdge(e12_1));
        assertTrue(g2.containsEdge(e21_1));

        assertEquals(6, g3.edgeSet().size());
        assertTrue(g3.containsEdge(e12_2));
        assertTrue(g3.containsEdge(e21_2));
        assertTrue(g3.containsEdge(e23_1));
        assertTrue(g3.containsEdge(e32_1));
        assertTrue(g3.containsEdge(e31_1));
        assertTrue(g3.containsEdge(e13_1));

        assertEquals(4, g4.edgeSet().size());
        assertTrue(g4.containsEdge(e12_3));
        assertTrue(g4.containsEdge(e23_2));
        assertTrue(g4.containsEdge(e34_1));
        assertTrue(g4.containsEdge(e41_1));
    }

    /**
     * .
     */
    @Test
    public void testEdgesOf()
    {
        assertEquals(g4.edgesOf(v1).size(), 2);
        assertEquals(g3.edgesOf(v1).size(), 4);

        Iterator<DefaultEdge> iter = g3.edgesOf(v1).iterator();
        int count = 0;

        while (iter.hasNext()) {
            iter.next();
            count++;
        }

        assertEquals(count, 4);
    }

    /**
     * .
     */
    @Test
    public void testGetAllEdges()
    {
        assertEquals(1, g3.getAllEdges(v1, v2).size());
        assertTrue(g3.getAllEdges(v1, v2).contains(e12_2));

        assertEquals(1, g3.getAllEdges(v2, v1).size());
        assertTrue(g3.getAllEdges(v2, v1).contains(e21_2));
    }

    /**
     * .
     */
    @Test
    public void testGetEdge()
    {
        assertEquals(e12_1, g2.getEdge(v1, v2));
        assertEquals(e21_1, g2.getEdge(v2, v1));

        assertEquals(e12_2, g3.getEdge(v1, v2));
        assertEquals(e21_2, g3.getEdge(v2, v1));
        assertEquals(e21_2, g3.getEdge(v2, v1));
        assertEquals(e32_1, g3.getEdge(v3, v2));
        assertEquals(e31_1, g3.getEdge(v3, v1));
        assertEquals(e13_1, g3.getEdge(v1, v3));

        assertEquals(e12_3, g4.getEdge(v1, v2));
        assertEquals(e23_2, g4.getEdge(v2, v3));
        assertEquals(e34_1, g4.getEdge(v3, v4));
        assertEquals(e41_1, g4.getEdge(v4, v1));
    }

    /**
     * .
     */
    @Test
    public void testGetEdgeSupplier()
    {
        assertNotNull(g1.getEdgeSupplier());
        Supplier<DefaultEdge> es = g1.getEdgeSupplier();
        DefaultEdge e = es.get();
        assertNotNull(e);
        assertNull(g1.getEdgeSource(e));
        assertNull(g1.getEdgeTarget(e));
    }

    /**
     * .
     */
    @Test
    public void testInDegreeOf()
    {
        assertEquals(0, g1.inDegreeOf(v1));

        assertEquals(1, g2.inDegreeOf(v1));
        assertEquals(1, g2.inDegreeOf(v2));

        assertEquals(2, g3.inDegreeOf(v1));
        assertEquals(2, g3.inDegreeOf(v2));
        assertEquals(2, g3.inDegreeOf(v3));

        assertEquals(1, g4.inDegreeOf(v1));
        assertEquals(1, g4.inDegreeOf(v2));
        assertEquals(1, g4.inDegreeOf(v3));
        assertEquals(1, g4.inDegreeOf(v4));

        try {
            g3.inDegreeOf(new Holder<>(""));
            Assert.fail("Should not get here.");
        } catch (IllegalArgumentException e) {
        }

        try {
            g3.inDegreeOf(null);
            Assert.fail("Should not get here.");
        } catch (NullPointerException e) {
        }
    }

    /**
     * .
     */
    @Test
    public void testIncomingOutgoingEdgesOf()
    {
        Set<DefaultEdge> e1to2 = g2.outgoingEdgesOf(v1);
        Set<DefaultEdge> e2from1 = g2.incomingEdgesOf(v2);
        assertEquals(e1to2, e2from1);
    }

    /**
     * .
     */
    @Test
    public void testOutDegreeOf()
    {
        assertEquals(1, g2.outDegreeOf(v1));
        assertEquals(1, g2.outDegreeOf(v2));
        assertEquals(2, g3.outDegreeOf(v1));
        assertEquals(2, g3.outDegreeOf(v2));
        assertEquals(2, g3.outDegreeOf(v3));
        assertEquals(1, g4.outDegreeOf(v1));
        assertEquals(1, g4.outDegreeOf(v2));
        assertEquals(1, g4.outDegreeOf(v3));
        assertEquals(1, g4.outDegreeOf(v4));
    }

    /**
     * .
     */
    @Test
    public void testOutgoingEdgesOf()
    {
        assertEquals(0, g1.outgoingEdgesOf(v1).size());
        assertEquals(1, g2.outgoingEdgesOf(v1).size());
        assertTrue(g2.outgoingEdgesOf(v1).contains(e12_1));
        assertEquals(1, g2.outgoingEdgesOf(v2).size());
        assertTrue(g2.outgoingEdgesOf(v2).contains(e21_1));
        assertEquals(2, g3.outgoingEdgesOf(v1).size());
        assertTrue(g3.outgoingEdgesOf(v1).contains(e12_2));
        assertTrue(g3.outgoingEdgesOf(v1).contains(e13_1));
        assertEquals(2, g3.outgoingEdgesOf(v2).size());
        assertTrue(g3.outgoingEdgesOf(v2).contains(e23_1));
        assertTrue(g3.outgoingEdgesOf(v2).contains(e21_2));
        assertEquals(2, g3.outgoingEdgesOf(v3).size());
        assertTrue(g3.outgoingEdgesOf(v3).contains(e31_1));
        assertTrue(g3.outgoingEdgesOf(v3).contains(e32_1));
        assertEquals(1, g4.outgoingEdgesOf(v1).size());
        assertTrue(g4.outgoingEdgesOf(v1).contains(e12_3));
        assertEquals(1, g4.outgoingEdgesOf(v2).size());
        assertTrue(g4.outgoingEdgesOf(v2).contains(e23_2));
        assertEquals(1, g4.outgoingEdgesOf(v3).size());
        assertTrue(g4.outgoingEdgesOf(v3).contains(e34_1));
        assertEquals(1, g4.outgoingEdgesOf(v4).size());
        assertTrue(g4.outgoingEdgesOf(v4).contains(e41_1));
    }

    /**
     * Class to test for boolean removeEdge(Edge)
     */
    @Test
    public void testRemoveEdgeEdge()
    {
        assertEquals(g4.edgeSet().size(), 4);
        g4.removeEdge(v1, v2);
        assertEquals(g4.edgeSet().size(), 3);
        assertFalse(g4.removeEdge(eLoop));
        assertTrue(g4.removeEdge(g4.getEdge(v2, v3)));
        assertEquals(g4.edgeSet().size(), 2);
    }

    /**
     * Class to test for Edge removeEdge(Object, Object)
     */
    @Test
    public void testRemoveEdgeObjectObject()
    {
        assertEquals(g4.edgeSet().size(), 4);
        g4.removeEdge(v1, v2);
        assertEquals(g4.edgeSet().size(), 3);
        assertFalse(g4.removeEdge(eLoop));
        assertTrue(g4.removeEdge(g4.getEdge(v2, v3)));
        assertEquals(g4.edgeSet().size(), 2);
    }

    @Test
    public void testRemoveAllEdgesObjectObject()
    {
        assertEquals(2, g2.edgeSet().size());
        assertTrue(g2.containsEdge(v1, v2));
        Set<DefaultEdge> edges = g2.getAllEdges(v1, v2);
        assertEquals(edges, g2.removeAllEdges(v1, v2));
        assertEquals(1, g2.edgeSet().size());
        assertFalse(g2.containsEdge(v1, v2));

        assertEquals(4, g4.edgeSet().size());
        edges = g4.getAllEdges(v3, v4);
        assertEquals(edges, g4.removeAllEdges(v3, v4));
        assertEquals(3, g4.edgeSet().size());
        assertFalse(g4.containsEdge(v3, v4));
        // No edge to remove.
        assertEquals(Collections.emptySet(), g4.removeAllEdges(v3, v2));
        assertEquals(3, g4.edgeSet().size());
        // Missing vertex.
        assertEquals(null, g4.removeAllEdges(v1, new Holder<>("v5")));
    }

    /**
     * .
     */
    @Test
    public void testRemoveVertex()
    {
        assertEquals(4, g4.vertexSet().size());
        assertTrue(g4.removeVertex(v1));
        assertEquals(3, g4.vertexSet().size());

        assertEquals(2, g4.edgeSet().size());
        assertFalse(g4.removeVertex(v1));
        assertTrue(g4.removeVertex(v2));
        assertEquals(1, g4.edgeSet().size());
        assertTrue(g4.removeVertex(v3));
        assertEquals(0, g4.edgeSet().size());
        assertEquals(1, g4.vertexSet().size());
        assertTrue(g4.removeVertex(v4));
        assertEquals(0, g4.vertexSet().size());
    }

    /**
     * .
     */
    @Test
    public void testVertexSet()
    {
        assertEquals(1, g1.vertexSet().size());
        assertTrue(g1.containsVertex(v1));

        assertEquals(2, g2.vertexSet().size());
        assertTrue(g2.containsVertex(v1));
        assertTrue(g2.containsVertex(v2));

        assertEquals(3, g3.vertexSet().size());
        assertTrue(g3.containsVertex(v1));
        assertTrue(g3.containsVertex(v2));
        assertTrue(g3.containsVertex(v3));

        assertEquals(4, g4.vertexSet().size());
        assertTrue(g4.containsVertex(v1));
        assertTrue(g4.containsVertex(v2));
        assertTrue(g4.containsVertex(v3));
        assertTrue(g4.containsVertex(v4));
    }

    @Test
    public void testReversedView()
    {
        Graph<Holder<String>, DefaultEdge> g = new SimpleIdentityDirectedGraph<>(DefaultEdge.class);
        Graph<Holder<String>, DefaultEdge> r = new EdgeReversedGraph<>(g);

        g.addVertex(v1);
        g.addVertex(v2);
        DefaultEdge e = g.addEdge(v1, v2);

        verifyReversal(g, r, e);

        // We have implicitly verified that r is backed by g for additive
        // operations (since we constructed it before adding anything to g).
        // Now verify for deletion.

        g.removeEdge(e);

        assertTrue(r.edgeSet().isEmpty());
        assertEquals(0, r.inDegreeOf(v1));
        assertEquals(0, r.outDegreeOf(v1));
        assertEquals(0, r.inDegreeOf(v2));
        assertEquals(0, r.outDegreeOf(v2));
        assertTrue(r.incomingEdgesOf(v1).isEmpty());
        assertTrue(r.outgoingEdgesOf(v1).isEmpty());
        assertTrue(r.incomingEdgesOf(v2).isEmpty());
        assertTrue(r.outgoingEdgesOf(v2).isEmpty());
    }

    private void verifyReversal(
        Graph<Holder<String>, DefaultEdge> g, Graph<Holder<String>, DefaultEdge> r, DefaultEdge e)
    {
        assertTrue(r.containsVertex(v1));
        assertTrue(r.containsVertex(v2));

        assertEquals(g.vertexSet(), r.vertexSet());
        assertEquals(g.edgeSet(), r.edgeSet());

        assertTrue(r.containsEdge(v2, v1));
        assertSame(e, r.getEdge(v2, v1));
        assertFalse(r.containsEdge(v1, v2));
        assertNull(r.getEdge(v1, v2));

        Set<DefaultEdge> s = r.getAllEdges(v1, v2);
        assertEquals(0, s.size());

        s = r.getAllEdges(v2, v1);
        assertEquals(1, s.size());
        assertSame(e, s.iterator().next());

        assertEquals(1, r.inDegreeOf(v1));
        assertEquals(0, r.inDegreeOf(v2));
        assertEquals(0, r.outDegreeOf(v1));
        assertEquals(1, r.outDegreeOf(v2));

        assertEquals(g.edgeSet(), r.incomingEdgesOf(v1));
        assertTrue(r.outgoingEdgesOf(v1).isEmpty());
        assertTrue(r.incomingEdgesOf(v2).isEmpty());
        assertEquals(g.edgeSet(), r.outgoingEdgesOf(v2));

        assertSame(v2, r.getEdgeSource(e));
        assertSame(v1, r.getEdgeTarget(e));
    }

    @Before
    public void setUp()
    {
        gEmpty = new SimpleIdentityDirectedGraph<>(DefaultEdge.class);
        g1 = new SimpleIdentityDirectedGraph<>(DefaultEdge.class);
        g2 = new SimpleIdentityDirectedGraph<>(DefaultEdge.class);
        g3 = new SimpleIdentityDirectedGraph<>(DefaultEdge.class);
        g4 = new SimpleIdentityDirectedGraph<>(DefaultEdge.class);

        eSupplier = g1.getEdgeSupplier();
        eLoop = eSupplier.get();

        g1.addVertex(v1);

        g2.addVertex(v1);
        g2.addVertex(v2);
        e12_1 = g2.addEdge(v1, v2);
        e21_1 = g2.addEdge(v2, v1);

        g3.addVertex(v1);
        g3.addVertex(v2);
        g3.addVertex(v3);
        e12_2 = g3.addEdge(v1, v2);
        e21_2 = g3.addEdge(v2, v1);
        e23_1 = g3.addEdge(v2, v3);
        e32_1 = g3.addEdge(v3, v2);
        e31_1 = g3.addEdge(v3, v1);
        e13_1 = g3.addEdge(v1, v3);

        g4.addVertex(v1);
        g4.addVertex(v2);
        g4.addVertex(v3);
        g4.addVertex(v4);
        e12_3 = g4.addEdge(v1, v2);
        e23_2 = g4.addEdge(v2, v3);
        e34_1 = g4.addEdge(v3, v4);
        e41_1 = g4.addEdge(v4, v1);

        // change vertex values

        v1.setT("_v1");
        v2.setT("_v2");
        v3.setT("_v3");
        v4.setT("_v4");
    }
}
