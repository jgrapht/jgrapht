/*
 * (C) Copyright 2008-2018, by CHEN Kui and Contributors.
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
package org.jgrapht.graph.concurrent;

import junit.extensions.*;
import junit.framework.*;
import junit.textui.*;
import org.jgrapht.graph.*;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Test class AsSynchronizedGraph.
 *
 * @author CHEN Kui
 */
public class AsSynchronizedGraphTest
{
    private ArrayList<Integer> vertices;
    private ArrayList<DefaultEdge> edges;
    private AsSynchronizedGraph<Integer, DefaultEdge> g;
    private Vector<ArrayList<Order>> ordersList;

    @Test
    public void testAddVertex()
    {
        g = new AsSynchronizedGraph.Builder<Integer, DefaultEdge>()
            .build(new SimpleGraph<>(DefaultEdge.class));
        ordersList = new Vector<>();
        for (int i = 0; i < 20; i++) {
            ordersList.add(new ArrayList<>());
        }
        for (int i = 0; i < 1000; i++) {
            int index = (int) (Math.random() * ordersList.size());
            ordersList.get(index).add(new AddV(i));
        }
        TestSuite ts = new ActiveTestSuite();
        for (int i = 0; i < ordersList.size(); i++)
            ts.addTest(new TestThread("runAsThread"));
        TestRunner.run(ts);
        assertEquals(1000, g.vertexSet().size());
        for (int i = 0; i < 1000; i++) {
            assertTrue(g.containsVertex(i));
        }
        assertEquals(1000, iteratorCnt(g.vertexSet().iterator()));
        g.addVertex(1000);
        assertEquals(1001, g.vertexSet().size());
        assertEquals(1001, iteratorCnt(g.vertexSet().iterator()));
    }

    @Test
    public void testAddEdge()
    {
        g = new AsSynchronizedGraph.Builder<Integer, DefaultEdge>()
            .cacheEnable().build(new SimpleGraph<>(DefaultEdge.class));
        ArrayList<DefaultEdge> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++)
            g.addVertex(i);
        ordersList = new Vector<>();
        for (int i = 0; i < 20; i++) {
            ordersList.add(new ArrayList<>());
        }
        for (int i = 0; i < 1000; i++) {
            int index = (int) (Math.random() * ordersList.size());
            DefaultEdge e = new DefaultEdge();
            ordersList.get(index).add(new AddE(i, (i + 1) % 1000, e));
            list.add(e);
        }
        TestSuite ts = new ActiveTestSuite();
        for (int i = 0; i < ordersList.size(); i++)
            ts.addTest(new TestThread("runAsThread"));
        TestRunner.run(ts);
        assertEquals(1000, g.edgeSet().size());
        for (int i = 0; i < 1000; i++)
            assertTrue(g.containsEdge(list.get(i)));
        assertEquals(1000, iteratorCnt(g.edgeSet().iterator()));
        assertEquals(2, g.edgesOf(3).size());
        assertEquals(2, g.incomingEdgesOf(3).size());
        assertEquals(2, g.outgoingEdgesOf(3).size());
        g.addEdge(1, 3);
        assertEquals(1001, g.edgeSet().size());
        assertEquals(1001, iteratorCnt(g.edgeSet().iterator()));
        assertEquals(3, g.edgesOf(3).size());
        assertEquals(3, g.incomingEdgesOf(3).size());
        assertEquals(3, g.outgoingEdgesOf(3).size());
    }

    @Test
    public void testRemoveEdge()
    {
        g = new AsSynchronizedGraph.Builder<Integer, DefaultEdge>()
            .cacheEnable().build(new SimpleGraph<>(DefaultEdge.class));
        edges = new ArrayList<>();
        TestSuite ts = new ActiveTestSuite();
        for (int i = 0; i < 1000; i++) {
            g.addVertex(i);
        }
        for (int i = 0; i < 1000; i++) {
            DefaultEdge e = new DefaultEdge();
            g.addEdge(i, (i + 1) % 1000, e);
            edges.add(e);
        }
        for (int i = 0; i < 5; i++) {
            ts.addTest(new TestThread("removeEdge"));
        }
        TestRunner.run(ts);
        assertEquals(400, g.edgeSet().size());
        assertEquals(400, iteratorCnt(g.edgeSet().iterator()));
        g.removeEdge(edges.get(0));
        g.removeEdge(edges.get(1));
        assertEquals(398, g.edgeSet().size());
        assertEquals(398, iteratorCnt(g.edgeSet().iterator()));
    }

    @Test
    public void testRemoveVertex()
    {
        g = new AsSynchronizedGraph.Builder<Integer, DefaultEdge>()
            .cacheEnable().build(new DirectedPseudograph<>(DefaultEdge.class));
        vertices = new ArrayList<>();
        TestSuite ts = new ActiveTestSuite();
        for (int i = 0; i < 100; i++) {
            g.addVertex(i);
            vertices.add(i);
        }
        for (int i = 0; i < 100; i++)
            for (int j = 0; j < 100; j++)
                g.addEdge(i, j);
        ts.addTest(new TestThread("removeVertex"));
        ts.addTest(new TestThread("removeVertex"));
        ts.addTest(new TestThread("removeVertex"));
        TestRunner.run(ts);
        assertEquals(10, g.vertexSet().size());
        assertEquals(10, iteratorCnt(g.vertexSet().iterator()));
        assertEquals(100, g.edgeSet().size());
        assertEquals(100, iteratorCnt(g.edgeSet().iterator()));
        assertEquals(10, g.incomingEdgesOf(vertices.get(0)).size());
        assertEquals(10, g.outgoingEdgesOf(vertices.get(0)).size());
        assertEquals(19, g.edgesOf(vertices.get(0)).size());
        g.removeVertex(vertices.get(1));
        assertEquals(9, g.vertexSet().size());
        assertEquals(9, iteratorCnt(g.vertexSet().iterator()));
        assertEquals(81, g.edgeSet().size());
        assertEquals(81, iteratorCnt(g.edgeSet().iterator()));
        assertEquals(9, g.incomingEdgesOf(vertices.get(0)).size());
        assertEquals(9, g.outgoingEdgesOf(vertices.get(0)).size());
        assertEquals(17, g.edgesOf(vertices.get(0)).size());
    }

    @Test
    public void testOthers()
    {
        g = new AsSynchronizedGraph.Builder<Integer, DefaultEdge>()
            .cacheDisable().build(new Pseudograph<>(DefaultEdge.class));
        Set<Integer> vertSet = g.vertexSet();
        Set<DefaultEdge> edgeSet = g.edgeSet();
        g.addVertex(1);
        g.addVertex(2);
        assertEquals(2, vertSet.size());
        assertEquals(2, iteratorCnt(vertSet.iterator()));
        g.addVertex(3);
        g.addVertex(4);
        assertEquals(4, vertSet.size());
        assertEquals(4, iteratorCnt(vertSet.iterator()));
        assertEquals(0, edgeSet.size());
        assertEquals(0, iteratorCnt(edgeSet.iterator()));

        g.addEdge(1, 2);
        assertEquals(1, g.edgesOf(2).size());
        assertEquals(1, iteratorCnt(g.edgesOf(2).iterator()));
        assertEquals(1, g.outgoingEdgesOf(1).size());
        assertEquals(1, g.incomingEdgesOf(2).size());

        g.addEdge(2, 3);
        assertEquals(2, edgeSet.size());
        assertEquals(2, iteratorCnt(edgeSet.iterator()));
        assertEquals(2, g.edgesOf(2).size());
        assertEquals(2, iteratorCnt(g.edgesOf(2).iterator()));
        assertEquals(2, g.outgoingEdgesOf(2).size());
        assertEquals(2, g.incomingEdgesOf(2).size());
        assertFalse(g.isCacheEnabled());
        g.setCache(true);
        assertTrue(g.isCacheEnabled());
        g.addEdge(2, 4);
        assertEquals(3, g.edgesOf(2).size());
        assertEquals(3, iteratorCnt(g.edgesOf(2).iterator()));
        assertEquals(3, g.outgoingEdgesOf(2).size());
        assertEquals(3, g.incomingEdgesOf(2).size());
        g.addEdge(2, 2);
        assertEquals(4, g.edgesOf(2).size());
        assertEquals(4, iteratorCnt(g.edgesOf(2).iterator()));
        assertEquals(4, g.outgoingEdgesOf(2).size());
        assertEquals(4, g.incomingEdgesOf(2).size());

        g.removeVertex(3);
        assertEquals(3, vertSet.size());
        assertEquals(3, iteratorCnt(vertSet.iterator()));
        assertEquals(3, edgeSet.size());
        assertEquals(3, iteratorCnt(edgeSet.iterator()));
        assertEquals(3, g.edgeSet().size());
        assertEquals(3, g.incomingEdgesOf(2).size());
        assertEquals(3, g.outgoingEdgesOf(2).size());
    }

    @Test
    public void testScenario()
    {
        g = new AsSynchronizedGraph<>(new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class));
        TestSuite ts = new ActiveTestSuite();
        ArrayList<Order> order1 = new ArrayList<>();
        ArrayList<Order> order2 = new ArrayList<>();
        ArrayList<Order> order3 = new ArrayList<>();
        ArrayList<Order> order4 = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            order1.add(new AddV(i));
        createOrder(order1, 2, 9, true); // add 21 edges
        createOrder(order1, 4, 7, false); // rm 3 edges
        for (int i = 10; i < 20; i++)
            order2.add(new AddV(i));
        createOrder(order2, 10, 20, true); // add 45 edges
        order2.add(new SetCache());
        createOrder(order2, 14, 18, false); // rm 6 edges
        for (int i = 20; i < 30; i++)
            order3.add(new AddV(i));
        order3.add(new SetCache());
        createOrder(order3, 25, 30, true); // add 15 edges
        createOrder(order3, 25, 30, false); // rm 15 edges
        for (int i = 30; i < 60; i++)
            order4.add(new AddV(i));
        createOrder(order4, 30, 60, true); // add 435 edges
        ordersList = new Vector<>();
        ordersList.add(order1);
        ordersList.add(order2);
        ordersList.add(order3);
        ordersList.add(order4);
        for (int i = 0; i < ordersList.size(); i++)
            ts.addTest(new TestThread("runAsThread"));
        TestRunner.run(ts);
        assertFalse(g.isCacheEnabled());
        assertEquals(60, g.vertexSet().size());
        assertEquals(60, iteratorCnt(g.vertexSet().iterator()));
        for (int i = 0; i < 60; i++)
            assertTrue(g.containsVertex(i));
        assertEquals(21 - 3 + 45 - 6 + 435, g.edgeSet().size());
        assertEquals(21 - 3 + 45 - 6 + 435, iteratorCnt(g.edgeSet().iterator()));
        assertEquals(6, g.outgoingEdgesOf(2).size());
        assertEquals(6, g.incomingEdgesOf(2).size());
        assertEquals(6, g.edgesOf(2).size());
        assertEquals(9, g.edgesOf(10).size());
        new SetCache().execute();
        order1.clear();
        createOrder(order1, 1, 10, false); // rm 18 edges
        order2.clear();
        createOrder(order2, 10, 20, false); // rm 39 edges
        order3.clear();
        for (int i = 3; i < 15; i++) // rm 12 vertices, 0 edges
            order3.add(new RmV(i));
        for (int i = 30; i < 40; i++) // rm 10 vertices
            order3.add(new RmV(i));
        ts = new ActiveTestSuite();
        ordersList.clear();
        ordersList.add(order1);
        ordersList.add(order2);
        ordersList.add(order3);
        for (int i = 0; i < ordersList.size(); i++)
            ts.addTest(new TestThread("runAsThread"));
        TestRunner.run(ts);
        assertEquals(38, g.vertexSet().size());
        assertEquals(190, g.edgeSet().size());
        assertEquals(0, g.edgesOf(2).size());
        assertEquals(19, g.edgesOf(41).size());
    }

    @Test
    public void testCopyless()
    {
        g = new AsSynchronizedGraph.Builder<Integer, DefaultEdge>()
            .setCopyless().build(new Pseudograph<>(DefaultEdge.class));
        vertices = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            g.addVertex(i);
            vertices.add(i);
        }
        edges = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            DefaultEdge e = new DefaultEdge();
            g.addEdge(i, (i + 1) % 1000, e);
            edges.add(e);
        }
        TestSuite ts = new ActiveTestSuite();
        ts.addTest(new TestThread("verifyEdges"));
        ts.addTest(new TestThread("removeEdge"));
        TestRunner.run(ts);
    }

    private void createOrder(ArrayList<Order> list, int start, int end, boolean add)
    {
        for (int i = start; i < end - 1; i++) {
            for (int j = i + 1; j < end; j++) {
                if (add)
                    list.add(new AddE(i, j, new DefaultEdge()));
                else
                    list.add(new RmE(i, j));
            }
        }
    }

    private interface Order
    {
        void execute();
    }

    private class AddV
        implements
        Order
    {
        int vertex;

        public AddV(int v)
        {
            vertex = v;
        }

        @Override
        public void execute()
        {
            g.addVertex(vertex);
        }
    }

    private class AddE
        implements
        Order
    {
        DefaultEdge e;
        int s, t;

        public AddE(int s, int t, DefaultEdge e)
        {
            this.e = e;
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute()
        {
            g.addEdge(s, t, e);
        }
    }

    private class SetCache
        implements
        Order
    {
        @Override
        public void execute()
        {
            synchronized (g) {
                g.setCache(!g.isCacheEnabled());
            }
        }
    }

    private class RmV
        implements
        Order
    {
        int v;

        public RmV(int v)
        {
            this.v = v;
        }

        @Override
        public void execute()
        {
            g.removeVertex(v);
        }
    }

    private class RmE
        implements
        Order
    {
        int s, t;

        public RmE(int s, int t)
        {
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute()
        {
            g.removeEdge(s, t);
        }
    }

    private <K> int iteratorCnt(Iterator<K> it)
    {
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }

    public class TestThread
        extends
        TestCase
    {
        public TestThread(String s)
        {
            super(s);
        }

        public void addVertex()
        {
            while (true) {
                int id;
                synchronized (vertices) {
                    if (vertices.size() != 0)
                        id = vertices.remove(0);
                    else
                        return;
                }
                g.addVertex(id);
            }
        }

        public void removeEdge()
        {
            while (true) {
                DefaultEdge e;
                synchronized (edges) {
                    if (edges.size() > 400)
                        e = edges.remove(0);
                    else
                        return;
                }
                g.removeEdge(e);
            }
        }

        public void verifyEdges()
        {
            while (true) {
                int c;
                synchronized (vertices) {
                    if (vertices.size() > 10) {
                        c = vertices.remove(0);
                    } else {
                        return;
                    }
                }
                g.getLock().readLock().lock();
                try {
                    for (DefaultEdge e : g.edgesOf(c)) {
                        assertTrue(g.containsEdge(e));
                    }
                } finally {
                    g.getLock().readLock().unlock();
                }
            }
        }

        public void removeVertex()
        {
            while (true) {
                int c;
                synchronized (vertices) {
                    if (vertices.size() > 10)
                        c = vertices.remove(0);
                    else
                        return;
                }
                g.removeVertex(c);
            }
        }

        public void runAsThread()
        {
            List<Order> orders = ordersList.remove(0);
            for (Order o : orders) {
                o.execute();
            }
        }
    }
}
