/*
 * (C) Copyright 2007-2026, by France Telecom and Contributors.
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
package org.jgrapht.alg.connectivity;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.util.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joris Kinable
 */
public class BiconnectivityInspectorTest
{
    @Test
    public void testBiconnected()
    {
        BiconnectedGraph graph = new BiconnectedGraph();

        BiconnectivityInspector<String, DefaultEdge> inspector =
            new BiconnectivityInspector<>(graph);

        assertTrue(inspector.isBiconnected());
        assertEquals(0, inspector.getCutpoints().size());
    }

    @Test
    public void testLinearGraph()
    {
        int nbVertices = 5;
        Graph<Object, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.OBJECT_SUPPLIER, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        LinearGraphGenerator<Object, DefaultEdge> generator =
            new LinearGraphGenerator<>(nbVertices);
        generator.generateGraph(graph);

        BiconnectivityInspector<Object, DefaultEdge> inspector =
            new BiconnectivityInspector<>(graph);

        assertEquals(nbVertices - 2, inspector.getCutpoints().size());
    }

    @Test
    public void testNotBiconnected()
    {
        NotBiconnectedGraph graph = new NotBiconnectedGraph();

        BiconnectivityInspector<String, DefaultEdge> inspector =
            new BiconnectivityInspector<>(graph);

        assertEquals(2, inspector.getCutpoints().size());
    }

    @Test
    public void testBorderCases()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        assertFalse(new BiconnectivityInspector<>(g).isBiconnected()); // empty graph
        g.addVertex(0);
        assertFalse(new BiconnectivityInspector<>(g).isBiconnected()); // graph on 1 vertex
        g.addVertex(1);
        assertFalse(new BiconnectivityInspector<>(g).isBiconnected()); // graph on 2 vertices
                                                                       // without edges
        g.addEdge(0, 1);
        assertTrue(new BiconnectivityInspector<>(g).isBiconnected()); // graph with one edge
    }

    @Test
    public void testNotBiconnectedWithIsolatedVertex()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2));
        g.addEdge(0, 1);

        BiconnectivityInspector<Integer, DefaultEdge> inspector =
            new BiconnectivityInspector<>(g);

        assertFalse(inspector.isConnected());
        assertFalse(inspector.isBiconnected());
        assertFalse(GraphTests.isBiconnected(g));
    }

    @Test
    public void testConnectedComponents1()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(4, 5);

        BiconnectivityInspector<Integer, DefaultEdge> inspector = new BiconnectivityInspector<>(g);
        assertEquals(2, inspector.getConnectedComponents().size());
        assertFalse(inspector.isConnected());

        Graph<Integer, DefaultEdge> g1 = new AsSubgraph<>(g, Set.of(1, 2, 3));
        Graph<Integer, DefaultEdge> g2 = new AsSubgraph<>(g, Set.of(4, 5));

        for (Integer v : g1.vertexSet())
            assertEquals(g1, inspector.getConnectedComponent(v));
        for (Integer v : g2.vertexSet())
            assertEquals(g2, inspector.getConnectedComponent(v));
    }

    @Test
    public void testWikiGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14));
        int[][] edges = { { 1, 3 }, { 1, 2 }, { 2, 4 }, { 3, 4 }, { 4, 5 }, { 5, 6 }, { 6, 7 },
            { 7, 8 }, { 7, 9 }, { 9, 10 }, { 9, 11 }, { 11, 12 }, { 12, 13 }, { 13, 14 },
            { 12, 14 }, { 7, 14 } };
        for (int[] edge : edges)
            g.addEdge(edge[0], edge[1]);

        BiconnectivityInspector<Integer, DefaultEdge> inspector = new BiconnectivityInspector<>(g);

        assertTrue(inspector.isConnected());

        Set<Integer> expectedCutpoints = Set.of(4, 5, 6, 7, 9);
        assertEquals(expectedCutpoints, inspector.getCutpoints());

        Set<DefaultEdge> expectedBridges = new HashSet<>();
        expectedBridges.add(g.getEdge(4, 5));
        expectedBridges.add(g.getEdge(5, 6));
        expectedBridges.add(g.getEdge(6, 7));
        expectedBridges.add(g.getEdge(7, 8));
        expectedBridges.add(g.getEdge(9, 10));
        assertEquals(expectedBridges, inspector.getBridges());

        // Check vertex to block mapping
        List<Graph<Integer, DefaultEdge>> blocks = new ArrayList<>();
        blocks.add(new AsSubgraph<>(g, Set.of(1, 2, 3, 4))); // 0
        blocks.add(new AsSubgraph<>(g, Set.of(4, 5))); // 1
        blocks.add(new AsSubgraph<>(g, Set.of(5, 6))); // 2
        blocks.add(new AsSubgraph<>(g, Set.of(6, 7))); // 3
        blocks.add(new AsSubgraph<>(g, Set.of(7, 8))); // 4
        blocks.add(new AsSubgraph<>(g, Set.of(9, 10))); // 5
        blocks.add(new AsSubgraph<>(g, Set.of(7, 9, 11, 12, 13, 14))); // 6

        for (int v : Arrays.asList(1, 2, 3))
            assertEquals(Collections.singleton(blocks.get(0)), inspector.getBlocks(v));
        assertEquals(Collections.singleton(blocks.get(4)), inspector.getBlocks(8));
        for (int v : Arrays.asList(11, 12, 13, 14)) {
            assertEquals(Collections.singleton(blocks.get(6)), inspector.getBlocks(v));
        }

        // cutpoints reside in multiple blocks
        assertEquals(Set.of(blocks.get(0), blocks.get(1)), inspector.getBlocks(4));
        assertEquals(Set.of(blocks.get(1), blocks.get(2)), inspector.getBlocks(5));
        assertEquals(Set.of(blocks.get(2), blocks.get(3)), inspector.getBlocks(6));
        assertEquals(Set.of(blocks.get(3), blocks.get(4), blocks.get(6)), inspector.getBlocks(7));
        assertEquals(Set.of(blocks.get(5), blocks.get(6)), inspector.getBlocks(9));

    }

    @Test
    public void testMultiGraph()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2));
        DefaultEdge bridge = g.addEdge(0, 1);
        g.addEdge(1, 1);
        g.addEdge(1, 2);
        g.addEdge(1, 2);

        BiconnectivityInspector<Integer, DefaultEdge> inspector = new BiconnectivityInspector<>(g);

        assertEquals(Collections.singleton(1), inspector.getCutpoints());
        assertEquals(Collections.singleton(bridge), inspector.getBridges());

        List<Graph<Integer, DefaultEdge>> blocks = new ArrayList<>();
        blocks.add(new AsSubgraph<>(g, Set.of(0, 1))); // 0
        blocks.add(new AsSubgraph<>(g, Set.of(1, 2))); // 1

        assertEquals(new HashSet<>(blocks), inspector.getBlocks());
    }

    @Test
    public void testMultiGraph2()
    {
        Graph<Integer, DefaultEdge> g = new Multigraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14));
        int[][] edges = { { 1, 3 }, { 1, 2 }, { 2, 4 }, { 3, 4 }, { 4, 5 }, { 5, 6 }, { 6, 7 },
            { 7, 8 }, { 7, 9 }, { 9, 10 }, { 9, 11 }, { 11, 12 }, { 12, 13 }, { 13, 14 },
            { 12, 14 }, { 7, 14 }, { 1, 3 }, { 1, 2 }, { 2, 4 }, { 3, 4 }, { 4, 5 }, { 5, 6 },
            { 6, 7 }, { 7, 8 }, { 7, 9 }, { 9, 10 }, { 9, 11 }, { 11, 12 }, { 12, 13 }, { 13, 14 },
            { 12, 14 }, { 7, 14 } };
        for (int[] edge : edges)
            g.addEdge(edge[0], edge[1]);

        BiconnectivityInspector<Integer, DefaultEdge> inspector = new BiconnectivityInspector<>(g);

        assertTrue(inspector.isConnected());

        Set<Integer> expectedCutpoints = Set.of(4, 5, 6, 7, 9);
        assertEquals(expectedCutpoints, inspector.getCutpoints());

        assertEquals(Collections.emptySet(), inspector.getBridges());

        // Check vertex to block mapping
        List<Graph<Integer, DefaultEdge>> blocks = new ArrayList<>();
        blocks.add(new AsSubgraph<>(g, Set.of(1, 2, 3, 4))); // 0
        blocks.add(new AsSubgraph<>(g, Set.of(4, 5))); // 1
        blocks.add(new AsSubgraph<>(g, Set.of(5, 6))); // 2
        blocks.add(new AsSubgraph<>(g, Set.of(6, 7))); // 3
        blocks.add(new AsSubgraph<>(g, Set.of(7, 8))); // 4
        blocks.add(new AsSubgraph<>(g, Set.of(9, 10))); // 5
        blocks.add(new AsSubgraph<>(g, Set.of(7, 9, 11, 12, 13, 14))); // 6

        for (int v : Arrays.asList(1, 2, 3))
            assertEquals(Collections.singleton(blocks.get(0)), inspector.getBlocks(v));
        assertEquals(Collections.singleton(blocks.get(4)), inspector.getBlocks(8));
        for (int v : Arrays.asList(11, 12, 13, 14)) {
            assertEquals(Collections.singleton(blocks.get(6)), inspector.getBlocks(v));
        }

        // cutpoints reside in multiple blocks
        assertEquals(Set.of(blocks.get(0), blocks.get(1)), inspector.getBlocks(4));
        assertEquals(Set.of(blocks.get(1), blocks.get(2)), inspector.getBlocks(5));
        assertEquals(Set.of(blocks.get(2), blocks.get(3)), inspector.getBlocks(6));
        assertEquals(Set.of(blocks.get(3), blocks.get(4), blocks.get(6)), inspector.getBlocks(7));
        assertEquals(Set.of(blocks.get(5), blocks.get(6)), inspector.getBlocks(9));
    }

    @Test
    public void testGithubIssueBug798()
    {
        Graph<Integer, DefaultEdge> g = GraphTypeBuilder.undirected().allowingSelfLoops(false)
            .allowingMultipleEdges(false).edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
            .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();
        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(0, 2);
        DefaultEdge e03 = g.addEdge(0, 3);

        BiconnectivityInspector<Integer, DefaultEdge> bi = new BiconnectivityInspector<>(g);

        assertFalse(bi.isBiconnected());

        Set<Integer> cutpoints = bi.getCutpoints();
        assertEquals(1, cutpoints.size());
        assertTrue(cutpoints.contains(0));

        Set<DefaultEdge> bridges = bi.getBridges();
        assertEquals(1, bridges.size());
        assertTrue(bridges.contains(e03));

        assertEquals(2, bi.getBlocks(0).size());
        assertEquals(1, bi.getBlocks(1).size());
        assertEquals(1, bi.getBlocks(2).size());
        assertEquals(1, bi.getBlocks(3).size());
        assertEquals(2, bi.getBlocks().size());
    }
    @Test
    public void testLongPathDoesNotUseCallStack()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        int size = 10000;
        for (int i = 0; i < size; i++) {
            graph.addVertex(i);
            if (i > 0)
                graph.addEdge(i - 1, i);
        }
        BiconnectivityInspector<Integer, DefaultEdge> inspector =
            new BiconnectivityInspector<>(graph);
        assertEquals(size - 2, inspector.getCutpoints().size());
        assertFalse(inspector.getCutpoints().contains(0));
        assertFalse(inspector.getCutpoints().contains(size - 1));
        assertEquals(graph.edgeSet(), inspector.getBridges());
        assertEquals(size - 1, inspector.getBlocks().size());
        assertEquals(2, inspector.getBlocks(size / 2).size());
        assertEquals(1, inspector.getConnectedComponents().size());
        assertSame(inspector.getCutpoints(), inspector.getCutpoints());
    }

    @Test
    public void testLongCycleDoesNotUseCallStack()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        int size = 10000;
        for (int i = 0; i < size; i++) {
            graph.addVertex(i);
            if (i > 0)
                graph.addEdge(i - 1, i);
        }
        graph.addEdge(size - 1, 0);
        BiconnectivityInspector<Integer, DefaultEdge> inspector =
            new BiconnectivityInspector<>(graph);
        assertTrue(inspector.isBiconnected());
        assertTrue(inspector.getCutpoints().isEmpty());
        assertTrue(inspector.getBridges().isEmpty());
        assertEquals(1, inspector.getBlocks().size());
        assertEquals(graph.vertexSet(), inspector.getBlocks().iterator().next().vertexSet());
    }

    @Test
    public void testAllSmallSimpleGraphsAgainstRemovalOracle()
    {
        int size = 5;
        for (int mask = 0; mask < (1 << (size * (size - 1) / 2)); mask++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            for (int v = 0; v < size; v++)
                graph.addVertex(v);
            int bit = 0;
            for (int v = 0; v < size; v++)
                for (int w = v + 1; w < size; w++)
                    if ((mask & (1 << bit++)) != 0)
                        graph.addEdge(v, w);
            assertMatchesRemovalOracle(graph);
            assertMatchesBlockOracle(graph);
        }
    }

    @Test
    public void testPseudographsAgainstRemovalOracle()
    {
        Random random = new Random(1266);
        for (int trial = 0; trial < 250; trial++) {
            Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
            for (int v = 0; v < 7; v++)
                graph.addVertex(v);
            for (int edge = 0; edge < 15; edge++)
                graph.addEdge(random.nextInt(7), random.nextInt(7));
            assertMatchesRemovalOracle(graph);
        }
    }

    @Test
    public void testDirectedPseudographsAgainstRemovalOracle()
    {
        Random random = new Random(850);
        for (int trial = 0; trial < 250; trial++) {
            Graph<Integer, DefaultEdge> graph = new DirectedPseudograph<>(DefaultEdge.class);
            for (int v = 0; v < 7; v++)
                graph.addVertex(v);
            for (int edge = 0; edge < 15; edge++)
                graph.addEdge(random.nextInt(7), random.nextInt(7));
            assertMatchesRemovalOracle(graph);
        }
    }

    @Test
    public void testInspectionRemainsLazyAndCached()
    {
        Graph<Integer, DefaultEdge> base = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(base, Arrays.asList(0, 1, 2));
        base.addEdge(0, 1);
        base.addEdge(1, 2);
        int[] traversals = { 0 };
        Graph<Integer, DefaultEdge> graph = new GraphDelegator<>(base) {
            @Override
            public Set<DefaultEdge> edgesOf(Integer vertex)
            {
                traversals[0]++;
                return super.edgesOf(vertex);
            }
        };
        BiconnectivityInspector<Integer, DefaultEdge> inspector =
            new BiconnectivityInspector<>(graph);
        assertEquals(0, traversals[0]);
        assertEquals(Collections.singleton(1), inspector.getCutpoints());
        int afterInspection = traversals[0];
        assertTrue(afterInspection >= 3);
        inspector.getBridges();
        inspector.getBlocks();
        inspector.isConnected();
        inspector.isBiconnected();
        assertEquals(afterInspection, traversals[0]);
    }

    // Enumerate maximal connected vertex sets that remain connected after any vertex removal.
    private void assertMatchesBlockOracle(Graph<Integer, DefaultEdge> graph)
    {
        List<Integer> vertices = new ArrayList<>(graph.vertexSet());
        Set<Set<Integer>> candidates = new HashSet<>();
        for (int mask = 0; mask < (1 << vertices.size()); mask++) {
            if (Integer.bitCount(mask) < 2)
                continue;
            Set<Integer> subset = new HashSet<>();
            for (int i = 0; i < vertices.size(); i++)
                if ((mask & (1 << i)) != 0)
                    subset.add(vertices.get(i));
            Graph<Integer, DefaultEdge> subgraph = new AsSubgraph<>(graph, subset);
            if (countComponents(subgraph, null, null) != 1)
                continue;
            boolean biconnected = true;
            for (Integer removed : subset)
                if (countComponents(subgraph, removed, null) != 1)
                    biconnected = false;
            if (biconnected)
                candidates.add(subset);
        }
        Set<Set<Integer>> maximal = new HashSet<>(candidates);
        maximal.removeIf(candidate -> candidates.stream().anyMatch(
            other -> other.size() > candidate.size() && other.containsAll(candidate)));
        Set<Set<Integer>> actual = new HashSet<>();
        for (Graph<Integer, DefaultEdge> block : new BiconnectivityInspector<>(graph).getBlocks())
            actual.add(block.vertexSet());
        assertEquals(maximal, actual, graph.toString());
    }

    private void assertMatchesRemovalOracle(Graph<Integer, DefaultEdge> graph)
    {
        int components = countComponents(graph, null, null);
        Set<Integer> cutpoints = new HashSet<>();
        for (Integer vertex : graph.vertexSet())
            if (countComponents(graph, vertex, null) > components)
                cutpoints.add(vertex);
        Set<DefaultEdge> bridges = new HashSet<>();
        for (DefaultEdge edge : graph.edgeSet())
            if (countComponents(graph, null, edge) > components)
                bridges.add(edge);
        BiconnectivityInspector<Integer, DefaultEdge> inspector =
            new BiconnectivityInspector<>(graph);
        assertEquals(cutpoints, inspector.getCutpoints(), graph.toString());
        assertEquals(bridges, inspector.getBridges(), graph.toString());
        assertEquals(components, inspector.getConnectedComponents().size());
        Set<Integer> covered = new HashSet<>();
        for (Graph<Integer, DefaultEdge> component : inspector.getConnectedComponents())
            covered.addAll(component.vertexSet());
        assertEquals(graph.vertexSet(), covered);
    }

    // A plain breadth-first traversal, independent of DFS discovery times and lowpoints.
    private int countComponents(
        Graph<Integer, DefaultEdge> graph, Integer removedVertex, DefaultEdge removedEdge)
    {
        Set<Integer> visited = new HashSet<>();
        int components = 0;
        for (Integer start : graph.vertexSet()) {
            if (start.equals(removedVertex) || !visited.add(start))
                continue;
            components++;
            Deque<Integer> queue = new ArrayDeque<>();
            queue.add(start);
            while (!queue.isEmpty()) {
                Integer vertex = queue.remove();
                for (DefaultEdge edge : graph.edgesOf(vertex)) {
                    if (edge.equals(removedEdge))
                        continue;
                    Integer next = Graphs.getOppositeVertex(graph, edge, vertex);
                    if (!next.equals(removedVertex) && visited.add(next))
                        queue.add(next);
                }
            }
        }
        return components;
    }

}
