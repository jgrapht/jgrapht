/*
 * (C) Copyright 2003-2018, by Liviu Rau and Contributors.
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
package org.jgrapht.traverse;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for the {@link BreadthFirstIterator} class.
 *
 * <p>
 * NOTE: This test uses hard-coded expected ordering isn't really guaranteed by the specification of
 * the algorithm. This could cause false failures if the traversal implementation changes.
 * </p>
 *
 * @author Liviu Rau
 * @author Patrick Sharp
 */
public class BreadthFirstIteratorTest
    extends
    CrossComponentIteratorTest
{
    // ~ Methods ----------------------------------------------------------------

    @Override
    String getExpectedStr1()
    {
        return "1,2,3,4,5,6,7,8,9";
    }

    @Override
    String getExpectedStr2()
    {
        return "1,2,3,4,5,6,7,8,9,orphan";
    }

    @Override
    AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(
        Graph<String, DefaultWeightedEdge> g, String vertex)
    {
        AbstractGraphIterator<String, DefaultWeightedEdge> i =
            new BreadthFirstIterator<>(g, vertex);
        i.setCrossComponentTraversal(true);

        return i;
    }

    @Override
    String getExpectedCCStr1()
    {
        return "orphan";
    }

    @Override
    String getExpectedCCStr2()
    {
        return "orphan,7,8,9,2,4";
    }

    @Override
    String getExpectedCCStr3()
    {
        return "orphan,7,8,9,2,4,3,5,6,1";
    }

    @Override
    AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(
        Graph<String, DefaultWeightedEdge> g, Iterable<String> startVertex)
    {
        return new BreadthFirstIterator<>(g, startVertex);
    }

    @Test
    public void searchTreeTest()
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("z");

        DefaultEdge e1 = g.addEdge("a", "b");
        DefaultEdge e2 = g.addEdge("b", "c");
        DefaultEdge e3 = g.addEdge("b", "z");
        DefaultEdge e4 = g.addEdge("b", "d");
        DefaultEdge e5 = g.addEdge("d", "e");

        BreadthFirstIterator<String, DefaultEdge> bfs = new BreadthFirstIterator<>(g, "a");
        while (bfs.hasNext())
            bfs.next();

        assertEquals(0, bfs.getDepth("a"));
        assertEquals(1, bfs.getDepth("b"));
        assertEquals(2, bfs.getDepth("c"));
        assertEquals(2, bfs.getDepth("d"));
        assertEquals(3, bfs.getDepth("e"));
        assertEquals(2, bfs.getDepth("z"));

        assertNull(bfs.getSpanningTreeEdge("a"));
        assertEquals(e1, bfs.getSpanningTreeEdge("b"));
        assertEquals(e2, bfs.getSpanningTreeEdge("c"));
        assertEquals(e4, bfs.getSpanningTreeEdge("d"));
        assertEquals(e5, bfs.getSpanningTreeEdge("e"));
        assertEquals(e3, bfs.getSpanningTreeEdge("z"));

        assertNull(bfs.getParent("a"));
        assertEquals("a", bfs.getParent("b"));
        assertEquals("b", bfs.getParent("c"));
        assertEquals("b", bfs.getParent("d"));
        assertEquals("d", bfs.getParent("e"));
        assertEquals("b", bfs.getParent("z"));

    }

    @Test
    public void searchTreeDirectedCycleTest()
    {
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        DefaultEdge e1 = Graphs.addEdgeWithVertices(g, 0, 1);
        DefaultEdge e2 = Graphs.addEdgeWithVertices(g, 1, 2);
        DefaultEdge e3 = Graphs.addEdgeWithVertices(g, 2, 3);
        Graphs.addEdgeWithVertices(g, 3, 0);

        BreadthFirstIterator<Integer, DefaultEdge> bfs = new BreadthFirstIterator<>(g, 0);
        while (bfs.hasNext())
            bfs.next();

        assertEquals(0, bfs.getDepth(0));
        assertEquals(1, bfs.getDepth(1));
        assertEquals(2, bfs.getDepth(2));
        assertEquals(3, bfs.getDepth(3));

        assertNull(bfs.getSpanningTreeEdge(0));
        assertEquals(e1, bfs.getSpanningTreeEdge(1));
        assertEquals(e2, bfs.getSpanningTreeEdge(2));
        assertEquals(e3, bfs.getSpanningTreeEdge(3));

        assertNull(bfs.getParent(0));
        assertEquals(new Integer(0), bfs.getParent(1));
        assertEquals(new Integer(1), bfs.getParent(2));
        assertEquals(new Integer(2), bfs.getParent(3));
    }
}
