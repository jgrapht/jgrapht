/*
 * (C) Copyright 2003-2021, by Sarah Komla-Ebri and Contributors.
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
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.util.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Test cases for the GabowStrongConnectivityInspector. Tests are identical to the tests for the
 * KosarajuStrongConnectivityInspector as provided in the ConnectivityInspectorTest class.
 *
 * @author Sarah Komla-Ebri
 * @author Joris Kinable
 * @author Hannes Wellmann
 */
@RunWith(Parameterized.class)
public class StrongConnectivityAlgorithmTest
{
    // ~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";
    private static final String V4 = "v4";
    private static final String V5 = "v5";
    private static final String V6 = "v6";
    private static final String V7 = "v7";
    private static final String V8 = "v8";
    private static final String V9 = "v9";
    private static final String V10 = "v10";
    private static final String V11 = "v11";

    private static final List<String> VERTICES =
        List.of(V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11);

    // ~ Instance fields --------------------------------------------------------

    @Parameter()
    public String name;

    @Parameter(1)
    public Function<Graph<?, ?>, StrongConnectivityAlgorithm<?, ?>> algorithmFactory;

    @Parameters(name = "{0}")
    @SuppressWarnings("unchecked")
    public static List<Object[]> getAlgorithmFactory()
    {
        return List
            .of(
                new Object[] { GabowStrongConnectivityInspector.class.getSimpleName(),
                    (Function<Graph<?, ?>, ?>) GabowStrongConnectivityInspector::new },

                new Object[] { KosarajuStrongConnectivityInspector.class.getSimpleName(),
                    (Function<Graph<?, ?>, ?>) KosarajuStrongConnectivityInspector::new });
    }

    @Test
    public void testStronglyConnected1()
    {
        Graph<String, DefaultEdge> g = createDirectedGraphWithVertices(4);

        g.addEdge(V1, V2);
        g.addEdge(V2, V1); // strongly connected

        g.addEdge(V3, V4); // only weakly connected

        assertStronglyConnectedSets(g, Set.of(V1, V2), Set.of(V3), Set.of(V4));
    }

    @Test
    public void testStronglyConnected2()
    {
        Graph<String, DefaultEdge> g = createDirectedGraphWithVertices(4);

        g.addEdge(V1, V2);
        g.addEdge(V2, V1); // strongly connected

        g.addEdge(V4, V3); // only weakly connected
        g.addEdge(V3, V2); // only weakly connected

        assertStronglyConnectedSets(g, Set.of(V1, V2), Set.of(V3), Set.of(V4));
    }

    @Test
    public void testStronglyConnected3()
    {
        Graph<String, DefaultEdge> g = createDirectedGraphWithVertices(4);

        g.addEdge(V1, V2);
        g.addEdge(V2, V3);
        g.addEdge(V3, V1); // strongly connected

        g.addEdge(V1, V4);
        g.addEdge(V2, V4);
        g.addEdge(V3, V4); // weakly connected

        assertStronglyConnectedSets(g, Set.of(V1, V2, V3), Set.of(V4));
    }

    @Test
    public void testStronglyConnected4()
    {
        Graph<Integer, String> graph = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.createStringSupplier(), false);

        new RingGraphGenerator<Integer, String>(3).generateGraph(graph);

        assertStronglyConnectedSets(graph, Set.of(0, 1, 2));
    }

    @Test
    public void testStronglyConnected5()
    {

        // example from paper "Path-based depth-first search for strong and biconnected components"
        // of Harold N. Gabow (2000)

        Graph<String, DefaultEdge> graph = createDirectedGraphWithVertices(6);

        graph.addEdge(V1, V2);
        graph.addEdge(V1, V3);

        graph.addEdge(V2, V3);
        graph.addEdge(V2, V4);

        graph.addEdge(V4, V3);
        graph.addEdge(V4, V5);

        graph.addEdge(V5, V2);
        graph.addEdge(V5, V6);

        graph.addEdge(V6, V3);
        graph.addEdge(V6, V4);

        assertStronglyConnectedSets(graph, Set.of(V1), Set.of(V2, V4, V5, V6), Set.of(V3));
    }

    @Test
    public void testStronglyConnected6()
    {
        // example from
        // https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm

        Graph<String, DefaultEdge> graph = createDirectedGraphWithVertices(8);

        graph.addEdge(V1, V5);
        graph.addEdge(V2, V1);
        graph.addEdge(V3, V2);
        graph.addEdge(V3, V4);
        graph.addEdge(V4, V3);
        graph.addEdge(V5, V2);
        graph.addEdge(V6, V2);
        graph.addEdge(V6, V5);
        graph.addEdge(V6, V7);
        graph.addEdge(V7, V3);
        graph.addEdge(V7, V6);
        graph.addEdge(V8, V4);
        graph.addEdge(V8, V7);

        assertStronglyConnectedSets(
            graph, Set.of(V1, V2, V5), Set.of(V3, V4), Set.of(V6, V7), Set.of(V8));
    }

    @Test
    public void testStronglyConnected7()
    {
        Graph<String, DefaultEdge> graph = createDirectedGraphWithVertices(5);

        graph.addEdge(V1, V2);
        graph.addEdge(V2, V3);
        graph.addEdge(V3, V4);
        graph.addEdge(V3, V5);
        graph.addEdge(V4, V1);
        graph.addEdge(V5, V3);

        assertStronglyConnectedSets(graph, Set.of(V1, V2, V3, V4, V5));
    }

    @Test
    public void testStronglyConnected8()
    {
        Graph<String, DefaultEdge> graph = createDirectedGraphWithVertices(11);

        graph.addEdge(V1, V2);
        graph.addEdge(V1, V4);
        graph.addEdge(V2, V3);
        graph.addEdge(V2, V5);
        graph.addEdge(V3, V1);
        graph.addEdge(V3, V7);
        graph.addEdge(V4, V3);
        graph.addEdge(V5, V6);
        graph.addEdge(V5, V7);
        graph.addEdge(V6, V7);
        graph.addEdge(V6, V8);
        graph.addEdge(V6, V9);
        graph.addEdge(V6, V10);
        graph.addEdge(V7, V5);
        graph.addEdge(V8, V10);
        graph.addEdge(V9, V10);
        graph.addEdge(V10, V9);

        assertStronglyConnectedSets(
            graph, Set.of(V1, V2, V3, V4), Set.of(V5, V6, V7), Set.of(V8), Set.of(V9, V10),
            Set.of(V11));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCondensation()
    {
        Graph<String, DefaultEdge> g = createDirectedGraphWithVertices(5);

        g.addEdge(V1, V2);
        g.addEdge(V2, V1); // strongly connected

        g.addEdge(V3, V4); // only weakly connected
        g.addEdge(V5, V4); // only weakly connected

        StrongConnectivityAlgorithm<String, DefaultEdge> inspector =
            getStrongConnectivityInspector(g);

        Graph<Graph<String, DefaultEdge>, DefaultEdge> condensation = inspector.getCondensation();

        // assert that the condensation is as expected

        assertThat(
            condensation.vertexSet().stream().map(Graph::vertexSet).collect(Collectors.toList()),
            containsInAnyOrder(Set.of(V1, V2), Set.of(V3), Set.of(V4), Set.of(V5)));

        Graph<String, DefaultEdge> g1 = getOnlyGraphWithVertices(condensation, Set.of(V1, V2));
        Graph<String, DefaultEdge> g2 = getOnlyGraphWithVertices(condensation, Set.of(V3));
        Graph<String, DefaultEdge> g3 = getOnlyGraphWithVertices(condensation, Set.of(V4));
        Graph<String, DefaultEdge> g4 = getOnlyGraphWithVertices(condensation, Set.of(V5));

        // Check edges inside condensed graphs
        assertThat(g1.edgeSet(), is(equalTo(Set.of(g.getEdge(V1, V2), g.getEdge(V2, V1)))));
        assertThat(g2.edgeSet(), empty());
        assertThat(g3.edgeSet(), empty());
        assertThat(g4.edgeSet(), empty());

        // check edges between SCCs
        Set<DefaultEdge> interSCCEdges =
            Set.of(condensation.getEdge(g2, g3), condensation.getEdge(g4, g3));
        assertThat(condensation.edgeSet(), is(equalTo(interSCCEdges)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCondensation2()
    {
        Graph<String, DefaultEdge> g = createDirectedGraphWithVertices(4);

        g.addEdge(V1, V2);
        g.addEdge(V2, V1);
        g.addEdge(V3, V4);
        g.addEdge(V4, V3);

        g.addEdge(V1, V3);
        g.addEdge(V2, V4);

        StrongConnectivityAlgorithm<String, DefaultEdge> inspector =
            getStrongConnectivityInspector(g);

        Graph<Graph<String, DefaultEdge>, DefaultEdge> condensation = inspector.getCondensation();

        // assert that the condensation is as expected

        assertThat(
            condensation.vertexSet().stream().map(Graph::vertexSet).collect(Collectors.toList()),
            containsInAnyOrder(Set.of(V1, V2), Set.of(V3, V4)));

        Graph<String, DefaultEdge> g1 = getOnlyGraphWithVertices(condensation, Set.of(V1, V2));
        Graph<String, DefaultEdge> g2 = getOnlyGraphWithVertices(condensation, Set.of(V3, V4));

        // Check edges inside condensed graphs
        assertThat(g1.edgeSet(), is(equalTo(Set.of(g.getEdge(V1, V2), g.getEdge(V2, V1)))));
        assertThat(g2.edgeSet(), is(equalTo(Set.of(g.getEdge(V3, V4), g.getEdge(V4, V3)))));

        // check edges between SCCs
        assertThat(condensation.edgeSet(), is(equalTo(Set.of(condensation.getEdge(g1, g2)))));

    }

    // --- utility methods ---

    private static Graph<String, DefaultEdge> createDirectedGraphWithVertices(int vertexCount)
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        VERTICES.subList(0, vertexCount).forEach(g::addVertex);
        return g;
    }

    @SafeVarargs
    private <V, E> void assertStronglyConnectedSets(Graph<V, E> graph, Set<V>... expectedSets)
    {
        // Test the SCC algorithm for each vertex of the graph as start vertex
        int vertices = graph.vertexSet().size();

        for (int i = 0; i < vertices; i++) {
            Graph<V, E> g = createRotatedGraphCopy(graph, i);
            Set<V>[] expectedVertices = createdRotatedSets(expectedSets, i, graph);

            StrongConnectivityAlgorithm<V, E> inspector = getStrongConnectivityInspector(g);

            assertThat(inspector.stronglyConnectedSets(), containsInAnyOrder(expectedVertices));

            Set<Set<V>> actualSets = new HashSet<>();
            for (Graph<V, E> sg : inspector.getStronglyConnectedComponents()) {
                actualSets.add(sg.vertexSet());

                StrongConnectivityAlgorithm<V, E> ci = getStrongConnectivityInspector(sg);
                assertTrue(ci.isStronglyConnected());
            }

            assertThat(actualSets, containsInAnyOrder(expectedVertices));
        }
    }

    private static <V, E> Graph<V, E> createRotatedGraphCopy(Graph<V, E> graph, int shift)
    {
        // Because the algorithm implementations don't use linked Maps it is not sufficient to just
        // add vertices in a different order to achieve different start vertices. Instead the
        // vertices are added to a graph-copy in the same order every-time but they are logically
        // shifted by the given shift-length.
        // This logical shift is achieved by shifting the touching vertices of each edges.
        // So if the shift is 1 a edge from v1 to v2 is now going from v2 to v3 and so on.
        List<V> vertexList = new ArrayList<>(graph.vertexSet());
        Graph<V, E> g = GraphTypeBuilder.forGraph(graph).buildGraph();
        vertexList.forEach(g::addVertex); // add all vertices

        for (E edge : graph.edgeSet()) {
            // Apply shift to edges
            V source = graph.getEdgeSource(edge);
            source = getShiftedCounterpart(source, shift, vertexList);

            V target = graph.getEdgeTarget(edge);
            target = getShiftedCounterpart(target, shift, vertexList);

            g.addEdge(source, target);
        }
        return g;
    }

    private static <V> V getShiftedCounterpart(V v, int shift, List<V> vertexList)
    {
        int index = vertexList.indexOf(v);
        int shiftedIndex = (index + shift) % vertexList.size();
        return vertexList.get(shiftedIndex);
    }

    private static <V, E> Set<V>[] createdRotatedSets(Set<V>[] sets, int shift, Graph<V, E> graph)
    {
        List<V> vertexList = new ArrayList<>(graph.vertexSet());
        List<Set<V>> rotatedSets = new ArrayList<>();
        for (Set<V> set : sets) {
            Set<V> newSet = CollectionUtil.newHashSetWithExpectedSize(set.size());
            for (V v : set) {
                v = getShiftedCounterpart(v, shift, vertexList);
                newSet.add(v);
            }
            rotatedSets.add(newSet);
        }

        @SuppressWarnings("unchecked") Set<V>[] rotatedSet =
            (Set<V>[]) rotatedSets.toArray(i -> new Set<?>[i]);
        return rotatedSet;
    }

    private static <T> Graph<T, DefaultEdge> getOnlyGraphWithVertices(
        Graph<Graph<T, DefaultEdge>, DefaultEdge> graph, Set<T> vertices)
    {
        return getOnlyMatch(graph.vertexSet(), g -> g.vertexSet().equals(vertices));
    }

    private static <T> T getOnlyMatch(Collection<T> c, Predicate<T> p)
    {
        List<T> matches = c.stream().filter(p).collect(Collectors.toList());
        if (matches.size() == 1) {
            return matches.get(0);
        } else {
            fail("Not exactly one match: " + matches);
            return null; // never executed, fail() throws
        }
    }

    @SuppressWarnings("unchecked")
    private <V, E> StrongConnectivityAlgorithm<V, E> getStrongConnectivityInspector(Graph<V, E> g)
    {
        return (StrongConnectivityAlgorithm<V, E>) algorithmFactory.apply(g);
    }
}
