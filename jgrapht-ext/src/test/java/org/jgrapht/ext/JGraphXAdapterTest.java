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
package org.jgrapht.ext;

import com.mxgraph.model.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.fail;

/**
 * Test methods for the class JGraphXAdapter.
 */
public class JGraphXAdapterTest
{

    /**
     * Test scenarios under normal conditions.
     */
    @Test
    public void genericTest()
    {
        ListenableGraph<String, DefaultEdge> jGraphT =
            new DefaultListenableGraph<>(new DefaultDirectedGraph<>(DefaultEdge.class));

        // fill graph with data
        String v1 = "Vertex 1";
        String v2 = "Vertex 2";
        String v3 = "Vertex 3";
        String v4 = "Vertex 4";

        jGraphT.addVertex(v1);
        jGraphT.addVertex(v2);
        jGraphT.addVertex(v3);
        jGraphT.addVertex(v4);

        final int expectedEdges = 5;
        jGraphT.addEdge(v1, v2);
        jGraphT.addEdge(v1, v3);
        jGraphT.addEdge(v1, v4);
        jGraphT.addEdge(v2, v3);
        jGraphT.addEdge(v3, v4);

        // Create jgraphx graph and test it
        JGraphXAdapter<String, DefaultEdge> graphX =
            new JGraphXAdapter<String, DefaultEdge>(jGraphT);
        testMapping(graphX);

        // test if all values are in the jgraphx graph
        Object[] expectedArray = { v1, v2, v3, v4 };
        Arrays.sort(expectedArray);

        Object[] realArray = graphX.getCellToVertexMap().values().toArray();
        Arrays.sort(realArray);
        Assert.assertArrayEquals(expectedArray, realArray);

        realArray = graphX.getVertexToCellMap().keySet().toArray();
        Arrays.sort(realArray);
        Assert.assertArrayEquals(expectedArray, realArray);

        int edgesCount = graphX.getCellToEdgeMap().values().size();
        Assert.assertEquals(expectedEdges, edgesCount);

        edgesCount = graphX.getEdgeToCellMap().keySet().size();
        Assert.assertEquals(expectedEdges, edgesCount);
    }

    /**
     * Tests the correct implementation of the GraphListener interface.
     */
    @Test
    public void listenerTest()
    {
        ListenableGraph<String, String> jGraphT =
            new DefaultListenableGraph<>(new DefaultDirectedGraph<>(String.class));

        JGraphXAdapter<String, String> graphX = new JGraphXAdapter<String, String>(jGraphT);

        // add some data to the jgrapht graph - changes should be propagated
        // through jgraphxadapters graphlistener interface

        String v1 = "Vertex 1";
        String v2 = "Vertex 2";
        String v3 = "Vertex 3";
        String v4 = "Vertex 4";

        jGraphT.addVertex(v1);
        jGraphT.addVertex(v2);
        jGraphT.addVertex(v3);
        jGraphT.addVertex(v4);

        jGraphT.addEdge(v1, v2, "Edge 1");
        jGraphT.addEdge(v1, v3, "Edge 2");
        jGraphT.addEdge(v1, v4, "Edge 3");
        jGraphT.addEdge(v2, v3, "Edge 4");
        jGraphT.addEdge(v3, v4, "Edge 5");

        int expectedEdges = jGraphT.edgeSet().size();

        testMapping(graphX);

        // test if all values are in the jgraphx graph
        Object[] expectedArray = { v1, v2, v3, v4 };
        Arrays.sort(expectedArray);

        Object[] realArray = graphX.getCellToVertexMap().values().toArray();
        Arrays.sort(realArray);
        Assert.assertArrayEquals(expectedArray, realArray);

        realArray = graphX.getVertexToCellMap().keySet().toArray();
        Arrays.sort(realArray);
        Assert.assertArrayEquals(expectedArray, realArray);

        int edgesCount = graphX.getCellToEdgeMap().values().size();
        Assert.assertEquals(expectedEdges, edgesCount);

        edgesCount = graphX.getEdgeToCellMap().keySet().size();
        Assert.assertEquals(expectedEdges, edgesCount);

        // remove some data from the jgraphT graph
        jGraphT.removeVertex(v4);
        jGraphT.removeVertex(v3);

        jGraphT.removeEdge(v1, v2);

        int expectedEdgesAfterRemove = jGraphT.edgeSet().size();

        // test if all values are in the jgraphx graph
        expectedArray = new Object[] { v1, v2 };
        Arrays.sort(expectedArray);

        realArray = graphX.getCellToVertexMap().values().toArray();
        Arrays.sort(realArray);
        Assert.assertArrayEquals(expectedArray, realArray);

        realArray = graphX.getVertexToCellMap().keySet().toArray();
        Arrays.sort(realArray);
        Assert.assertArrayEquals(expectedArray, realArray);

        edgesCount = graphX.getCellToEdgeMap().values().size();
        Assert.assertEquals(expectedEdgesAfterRemove, edgesCount);

        edgesCount = graphX.getEdgeToCellMap().keySet().size();
        Assert.assertEquals(expectedEdgesAfterRemove, edgesCount);
    }

    /**
     * Tests conditions if graph is initialized without a JgraphT graph.
     */
    @Test
    public void nullInitializationTest()
    {
        try {
            new JGraphXAdapter<String, String>(null);
            fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            // expected result
            Assert.assertTrue(true);
        } catch (Exception e) {
            fail("Unexpected error encountered during " + " creation of JGraphXAdapter with null");
        }
    }

    /**
     * Tests the JGraphXAdapter with 1.000 nodes and 1.000 edges.
     */
    @Test
    public void loadTest()
    {
        final int maxVertices = 1000;
        final int maxEdges = 1000;

        ListenableGraph<Integer, DefaultEdge> jGraphT =
            new DefaultListenableGraph<>(new DefaultDirectedGraph<>(DefaultEdge.class));

        for (int i = 0; i < maxVertices; i++) {
            jGraphT.addVertex(i);
        }

        for (int i = 0; i < maxEdges; i++) {
            jGraphT.addEdge(i, (i + 1) % jGraphT.vertexSet().size());
        }

        JGraphXAdapter<Integer, DefaultEdge> graphX = null;

        try {
            graphX = new JGraphXAdapter<Integer, DefaultEdge>(jGraphT);
        } catch (Exception e) {
            fail(
                "Unexpected error while creating JgraphXAdapter with" + maxVertices
                    + " vertices and " + maxEdges + " Edges");
        }

        testMapping(graphX);

    }

    /**
     * Tests if JGraphXAdapter works with not-listenable Graphs.
     */
    @Test
    public void notListenableTest()
    {
        Graph<String, String> jGraphT = new DefaultDirectedGraph<String, String>(String.class);
        // fill graph with data
        String v1 = "Vertex 1";
        String v2 = "Vertex 2";
        String v3 = "Vertex 3";
        String v4 = "Vertex 4";

        jGraphT.addVertex(v1);
        jGraphT.addVertex(v2);
        jGraphT.addVertex(v3);

        final int expectedEdges = 3;
        jGraphT.addEdge(v1, v2, "Edge 1");
        jGraphT.addEdge(v1, v3, "Edge 2");
        jGraphT.addEdge(v2, v3, "Edge 3");

        JGraphXAdapter<String, String> graphX = new JGraphXAdapter<String, String>(jGraphT);

        jGraphT.addVertex(v4);
        jGraphT.addEdge(v1, v4, "Edge 4");
        jGraphT.addEdge(v3, v4, "Edge 5");

        testMapping(graphX);

        // test if all values are in the jgraphx graph
        Object[] expectedArray = { v1, v2, v3 };
        Arrays.sort(expectedArray);

        Object[] realArray = graphX.getCellToVertexMap().values().toArray();
        Arrays.sort(realArray);
        Assert.assertArrayEquals(expectedArray, realArray);

        realArray = graphX.getVertexToCellMap().keySet().toArray();
        Arrays.sort(realArray);
        Assert.assertArrayEquals(expectedArray, realArray);

        int edgesCount = graphX.getCellToEdgeMap().values().size();
        Assert.assertEquals(expectedEdges, edgesCount);

        edgesCount = graphX.getEdgeToCellMap().keySet().size();
        Assert.assertEquals(expectedEdges, edgesCount);
    }

    /**
     * Test if duplicate Entries are saved only once.
     */
    @Test
    public void duplicateEntriesTest()
    {
        ListenableGraph<String, DefaultEdge> jGraphT =
            new DefaultListenableGraph<>(new DefaultDirectedGraph<>(DefaultEdge.class));

        JGraphXAdapter<String, DefaultEdge> graphX =
            new JGraphXAdapter<String, DefaultEdge>(jGraphT);

        // fill graph with data
        String v1 = "Vertex 1";
        String v2 = "Vertex 2";
        String v3 = "Vertex 3";
        String v4 = "Vertex 4";
        DefaultEdge edge1 = new DefaultEdge();

        jGraphT.addVertex(v1);
        jGraphT.addVertex(v2);
        jGraphT.addVertex(v3);
        jGraphT.addVertex(v4);
        jGraphT.addVertex(v1);
        jGraphT.addVertex(v2);
        jGraphT.addVertex(v3);
        jGraphT.addVertex(v4);

        /*
         * edge1 is added 3 times with different source/target vertices it should only add it once.
         * A new edge is added with source-target combination already in the graph it should not be
         * added to the graph.
         */
        final int expectedEdges = 3;
        jGraphT.addEdge(v1, v2, edge1);
        jGraphT.addEdge(v1, v2, new DefaultEdge());
        jGraphT.addEdge(v1, v3, edge1);
        jGraphT.addEdge(v1, v4, edge1);
        jGraphT.addEdge(v2, v3);
        jGraphT.addEdge(v3, v4);

        testMapping(graphX);

        // test if all values are in the jgraphx graph
        Object[] expectedArray = { v1, v2, v3, v4 };
        Arrays.sort(expectedArray);

        Object[] realArray = graphX.getCellToVertexMap().values().toArray();
        Arrays.sort(realArray);
        Assert.assertArrayEquals(expectedArray, realArray);

        realArray = graphX.getVertexToCellMap().keySet().toArray();
        Arrays.sort(realArray);
        Assert.assertArrayEquals(expectedArray, realArray);

        int edgesCount = graphX.getCellToEdgeMap().values().size();
        Assert.assertEquals(expectedEdges, edgesCount);

        edgesCount = graphX.getEdgeToCellMap().keySet().size();
        Assert.assertEquals(expectedEdges, edgesCount);
    }

    // ========================Helper Methods===============================

    /**
     * Tests the mapping of the graph for consistency. Mapping includes: - getCellToEdgeMap -
     * getEdgeToCellMap - getCellToVertexMap - getVertexToCellMap
     *
     * @param graph The graph to be tested
     *
     * @param <E> The class used for the edges of the JGraphXAdapter
     *
     * @param <V> The class used for the vertices of the JGraphXAdapter
     */
    private <V, E> void testMapping(JGraphXAdapter<V, E> graph)
    {

        // Edges
        HashMap<mxICell, E> cellToEdgeMap = graph.getCellToEdgeMap();
        HashMap<E, mxICell> edgeToCellMap = graph.getEdgeToCellMap();

        // Test for null
        if (cellToEdgeMap == null) {
            fail("GetCellToEdgeMap returned null");
        }

        if (edgeToCellMap == null) {
            fail("GetEdgeToCellMap returned null");
        }

        // Compare keys to values
        if (!compare(edgeToCellMap.values(), cellToEdgeMap.keySet())) {
            fail("CellToEdgeMap has not the " + "same keys as the values in EdgeToCellMap");
        }

        if (!compare(cellToEdgeMap.values(), edgeToCellMap.keySet())) {
            fail("EdgeToCellMap has not the " + "same keys as the values in CellToEdgeMap");
        }

        // Vertices
        HashMap<mxICell, V> cellToVertexMap = graph.getCellToVertexMap();
        HashMap<V, mxICell> vertexToCellMap = graph.getVertexToCellMap();

        // Test for null
        if (cellToVertexMap == null) {
            fail("GetVertexToCellMap returned null");
        }

        if (vertexToCellMap == null) {
            fail("GetCellToVertexMap returned null");
        }

        // Compare keys to values
        if (!compare(vertexToCellMap.values(), cellToVertexMap.keySet())) {
            fail("CellToVertexMap has not the same " + "keys as the values in VertexToCellMap");
        }

        if (!compare(cellToVertexMap.values(), vertexToCellMap.keySet())) {
            fail("VertexToCellMap has not the same " + "keys as the values in CellToVertexMap");
        }
    }

    /**
     * Compares a collection to a set by creating a new set from the collection and using equals.
     *
     * @param collection The collection that is compared
     *
     * @param set The set that is compared
     *
     * @param <T> The classtype of the set and collection.
     *
     * @return True, if set and collection are equivalent; False if not.
     *
     */
    private <T> boolean compare(Collection<T> collection, Set<T> set)
    {
        Set<T> compareSet = new HashSet<T>();
        compareSet.addAll(collection);

        return set.equals(compareSet);
    }
}
