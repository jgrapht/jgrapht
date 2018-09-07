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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Cycles}.
 * 
 * @author Dimitrios Michail
 */
public class CyclesTest
{
    @Test
    public void testUndirected1()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<DefaultEdge> cycle = new ArrayList<>();
        cycle.add(Graphs.addEdgeWithVertices(graph, 0, 1));
        cycle.add(Graphs.addEdgeWithVertices(graph, 1, 2));
        cycle.add(Graphs.addEdgeWithVertices(graph, 2, 0));

        GraphPath<Integer, DefaultEdge> graphPath = Cycles.simpleCycleToGraphPath(graph, cycle);

        assertEquals(graphPath.getStartVertex(), graphPath.getEndVertex());
        assertUndirectedCycle(graphPath.getGraph(), graphPath.getEdgeList());
    }

    @Test
    public void testUndirected2()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<DefaultEdge> cycle = new ArrayList<>();
        cycle.add(Graphs.addEdgeWithVertices(graph, 1, 2));
        cycle.add(Graphs.addEdgeWithVertices(graph, 3, 4));
        cycle.add(Graphs.addEdgeWithVertices(graph, 0, 1));
        cycle.add(Graphs.addEdgeWithVertices(graph, 4, 5));
        cycle.add(Graphs.addEdgeWithVertices(graph, 5, 0));
        cycle.add(Graphs.addEdgeWithVertices(graph, 2, 3));
        Graphs.addEdgeWithVertices(graph, 5, 6);
        Graphs.addEdgeWithVertices(graph, 6, 7);

        GraphPath<Integer, DefaultEdge> graphPath = Cycles.simpleCycleToGraphPath(graph, cycle);

        assertEquals(graphPath.getStartVertex(), graphPath.getEndVertex());
        assertUndirectedCycle(graphPath.getGraph(), graphPath.getEdgeList());
    }

    @Test
    public void testSelfLoop1()
    {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        List<DefaultEdge> cycle = new ArrayList<>();
        cycle.add(Graphs.addEdgeWithVertices(graph, 0, 0));

        GraphPath<Integer, DefaultEdge> graphPath = Cycles.simpleCycleToGraphPath(graph, cycle);

        assertEquals(graphPath.getStartVertex(), graphPath.getEndVertex());
        assertUndirectedCycle(graphPath.getGraph(), graphPath.getEdgeList());
    }

    @Test
    public void testDirected1()
    {
        Graph<Integer, DefaultEdge> graph = new DirectedPseudograph<>(DefaultEdge.class);
        List<DefaultEdge> cycle = new ArrayList<>();
        cycle.add(Graphs.addEdgeWithVertices(graph, 0, 1));
        cycle.add(Graphs.addEdgeWithVertices(graph, 3, 2));
        cycle.add(Graphs.addEdgeWithVertices(graph, 3, 4));
        cycle.add(Graphs.addEdgeWithVertices(graph, 1, 2));
        cycle.add(Graphs.addEdgeWithVertices(graph, 0, 4));

        GraphPath<Integer, DefaultEdge> graphPath = Cycles.simpleCycleToGraphPath(graph, cycle);

        assertEquals(graphPath.getStartVertex(), graphPath.getEndVertex());
        assertUndirectedCycle(graphPath.getGraph(), graphPath.getEdgeList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUndirectedNotSimple1()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<DefaultEdge> cycle = new ArrayList<>();
        cycle.add(Graphs.addEdgeWithVertices(graph, 0, 1));
        cycle.add(Graphs.addEdgeWithVertices(graph, 1, 2));
        cycle.add(Graphs.addEdgeWithVertices(graph, 2, 0));
        cycle.add(Graphs.addEdgeWithVertices(graph, 2, 3));
        cycle.add(Graphs.addEdgeWithVertices(graph, 3, 4));
        cycle.add(Graphs.addEdgeWithVertices(graph, 4, 2));

        Cycles.simpleCycleToGraphPath(graph, cycle);
    }

    // assert that a list of edges is a cycle (without respecting edge directions)
    private void assertUndirectedCycle(Graph<Integer, DefaultEdge> g, List<DefaultEdge> edges)
    {
        if (edges.isEmpty()) {
            return;
        }

        DefaultEdge prev = null;
        DefaultEdge first = null, last = null;
        Iterator<DefaultEdge> it = edges.iterator();
        Set<DefaultEdge> dupCheck = new HashSet<>();
        while (it.hasNext()) {
            DefaultEdge cur = it.next();
            assertTrue(dupCheck.add(cur));
            if (prev == null) {
                first = cur;
            } else {
                assertTrue(
                    g.getEdgeSource(cur).equals(g.getEdgeSource(prev))
                        || g.getEdgeSource(cur).equals(g.getEdgeTarget(prev))
                        || g.getEdgeTarget(cur).equals(g.getEdgeSource(prev))
                        || g.getEdgeTarget(cur).equals(g.getEdgeTarget(prev)));
            }
            if (!it.hasNext()) {
                last = cur;
            }
            prev = cur;
        }
        if (edges.size() > 1) {
            assertTrue(
                g.getEdgeSource(first).equals(g.getEdgeSource(last))
                    || g.getEdgeSource(first).equals(g.getEdgeTarget(last))
                    || g.getEdgeTarget(first).equals(g.getEdgeSource(last))
                    || g.getEdgeTarget(first).equals(g.getEdgeTarget(last)));
        }
    }

}
