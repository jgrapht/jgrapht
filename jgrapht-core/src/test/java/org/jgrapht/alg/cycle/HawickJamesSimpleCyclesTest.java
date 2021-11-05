/*
 * (C) Copyright 2013-2021, by Nikolay Ognyanov and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;
import java.util.stream.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for class {@link HawickJamesSimpleCycles}.
 *
 * @author Edwin Ouwehand
 */
public class HawickJamesSimpleCyclesTest
{

    @Test
    public void noCyclesCount()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");

        assertEquals(0, new HawickJamesSimpleCycles<>(graph).countSimpleCycles());
    }

    @Test
    public void reflexiveCycleCount()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addEdge("A", "A");
        assertEquals(1, new HawickJamesSimpleCycles<>(graph).countSimpleCycles());
    }

    @Test
    public void singleDirectCycleCount()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addEdge("A", "B");
        graph.addEdge("B", "A");

        assertEquals(1, new HawickJamesSimpleCycles<>(graph).countSimpleCycles());
    }

    @Test
    public void indirectCycleCount()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");

        assertEquals(1, new HawickJamesSimpleCycles<>(graph).countSimpleCycles());
    }

    @Test
    public void noCyclesFind()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");

        assertTrue(new HawickJamesSimpleCycles<>(graph).findSimpleCycles().isEmpty());
    }

    @Test
    public void reflexiveCycleFind()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addEdge("A", "A");

        List<List<String>> cycles = new HawickJamesSimpleCycles<>(graph).findSimpleCycles();
        assertEquals(1, cycles.size());
        assertEquals(singletonList("A"), cycles.get(0));
    }

    @Test
    public void singleDirectFind()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addEdge("A", "B");
        graph.addEdge("B", "A");

        List<List<String>> cycles = new HawickJamesSimpleCycles<>(graph).findSimpleCycles();

        assertEquals(1, cycles.size());
        assertTrue(cycles.get(0).containsAll(asList("A", "B")));
    }

    @Test
    public void indirectCycleFind()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");

        List<List<String>> cycles = new HawickJamesSimpleCycles<>(graph).findSimpleCycles();

        assertEquals(1, cycles.size());
        assertTrue(cycles.get(0).containsAll(asList("A", "B", "C")));
    }

    @Test
    public void twoCycles()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");

        graph.addEdge("A", "B");
        graph.addEdge("B", "A");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");

        List<List<String>> cycles = new HawickJamesSimpleCycles<>(graph).findSimpleCycles();

        assertEquals(2, cycles.size());
        assertTrue(cycles.get(0).containsAll(asList("A", "B")));
        assertTrue(cycles.get(1).containsAll(asList("A", "B", "C")));
    }

    @Test
    public void twoSharingEdge()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");

        graph.addEdge("B", "C"); // Shared
        graph.addEdge("A", "B");
        graph.addEdge("C", "A");
        graph.addEdge("D", "B");
        graph.addEdge("C", "D");

        List<List<String>> cycles = new HawickJamesSimpleCycles<>(graph).findSimpleCycles();

        assertEquals(2, cycles.size());
        assertTrue(cycles.get(0).containsAll(asList("A", "B", "C")));
        assertTrue(cycles.get(1).containsAll(asList("D", "B", "C")));
    }

    @Test
    public void simplestCycles()
    {
        // We do NOT want to find A -> B, B -> B, B -> A as an additional cycle here,
        // nor B -> A, A -> A, A -> B for that matter. Only the most simple ones.
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addEdge("A", "B");
        graph.addEdge("B", "A");
        graph.addEdge("A", "A");
        graph.addEdge("B", "B");

        List<List<String>> cycles = new HawickJamesSimpleCycles<>(graph).findSimpleCycles();

        assertEquals(3, cycles.size());
        assertEquals(asList("A", "B"), cycles.get(0));
        assertEquals(singletonList("A"), cycles.get(1));
        assertEquals(singletonList("B"), cycles.get(2));
    }

    @Test
    public void complexGraph()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph, asList("A", "B", "C", "D", "E", "F"));
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("B", "E");
        graph.addEdge("C", "D");
        graph.addEdge("D", "E");
        graph.addEdge("E", "F");
        graph.addEdge("F", "A");

        List<List<String>> cycles = new HawickJamesSimpleCycles<>(graph).findSimpleCycles();

        assertEquals(2, cycles.size());

        List<String> cycle0 = cycles.get(0);
        assertTrue(cycle0.containsAll(asList("A", "B", "C", "D", "E", "F")));

        List<String> cycle1 = cycles.get(1);
        assertTrue(cycle1.containsAll(asList("A", "B", "E", "F")));
    }

    @Test
    public void consecutiveRuns()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");

        HawickJamesSimpleCycles<String, DefaultEdge> hjsc = new HawickJamesSimpleCycles<>(graph);

        List<List<String>> run1 = hjsc.findSimpleCycles();
        assertEquals(1, run1.size());
        assertTrue(run1.get(0).containsAll(asList("A", "B", "C")));

        List<List<String>> run2 = hjsc.findSimpleCycles();
        assertEquals(1, run2.size());
        assertTrue(run2.get(0).containsAll(asList("A", "B", "C")));
    }

    @Test
    public void limitPaths1()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");

        graph.addEdge("A", "B");
        graph.addEdge("B", "A");

        HawickJamesSimpleCycles<String, DefaultEdge> hjsc = new HawickJamesSimpleCycles<>(graph);
        hjsc.setPathLimit(1);

        assertTrue(hjsc.findSimpleCycles().isEmpty());
    }

    @Test
    public void limitPaths2()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");

        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");

        HawickJamesSimpleCycles<String, DefaultEdge> hjsc = new HawickJamesSimpleCycles<>(graph);
        hjsc.setPathLimit(2);

        assertTrue(hjsc.findSimpleCycles().isEmpty());
    }

    @Test
    public void limitPathsTwoCycles()
    {
        // Two smaller cycles are still found
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");

        graph.addEdge("A", "B");
        graph.addEdge("B", "A");

        graph.addEdge("C", "D");
        graph.addEdge("D", "C");

        HawickJamesSimpleCycles<String, DefaultEdge> hjsc = new HawickJamesSimpleCycles<>(graph);
        hjsc.setPathLimit(2);

        List<List<String>> cycles = hjsc.findSimpleCycles();
        assertEquals(2, cycles.size());
        assertTrue(cycles.get(0).containsAll(asList("A", "B")));
        assertTrue(cycles.get(1).containsAll(asList("C", "D")));
    }

    @Test
    public void testOrder()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("0");
        graph.addVertex("1");
        graph.addVertex("2");
        graph.addVertex("3");
        graph.addVertex("4");
        graph.addVertex("5");

        graph.addEdge("0", "1");
        graph.addEdge("1", "2");
        graph.addEdge("2", "3");
        graph.addEdge("3", "0");
        graph.addEdge("1", "4");
        graph.addEdge("4", "5");
        graph.addEdge("5", "2");

        HawickJamesSimpleCycles<String, DefaultEdge> hjsc = new HawickJamesSimpleCycles<>(graph);

        List<List<String>> cycles = hjsc.findSimpleCycles();
        assertEquals(2, cycles.size());

        String cycle0 = cycles.get(0).stream().collect(Collectors.joining(","));
        String cycle1 = cycles.get(1).stream().collect(Collectors.joining(","));

        assertEquals(cycle0, "0,1,2,3");
        assertEquals(cycle1, "0,1,4,5,2,3");
    }

}
