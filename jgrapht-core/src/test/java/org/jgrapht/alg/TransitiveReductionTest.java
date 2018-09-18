/*
 * (C) Copyright 2015-2018, by Christophe Thiebaud and Contributors.
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
package org.jgrapht.alg;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

public class TransitiveReductionTest
{

    // @formatter:off
    static final int[][] matrix = new int[][] {
        {0, 1, 1, 0, 0},
        {0, 0, 0, 0, 0},
        {0, 0, 0, 1, 1},
        {0, 0, 0, 0, 1},
        {0, 1, 0, 0, 0}
    };

    static final int[][] expected_transitively_reduced_matrix = new int[][] {
        {0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0},
        {0, 0, 0, 1, 0},
        {0, 0, 0, 0, 1},
        {0, 1, 0, 0, 0}
    };
    // @formatter:on

    @Test
    public void testInternals()
    {

        // @formatter:off
        final int[][] expected_path_matrix = new int[][] {
            {0, 1, 1, 1, 1},
            {0, 0, 0, 0, 0},
            {0, 1, 0, 1, 1},
            {0, 1, 0, 0, 1},
            {0, 1, 0, 0, 0}
        };

        // @formatter:on

        // System.out.println(Arrays.deepToString(matrix) + " original matrix");

        final int n = matrix.length;

        // calc path matrix
        int[][] path_matrix = new int[n][n];
        {
            {
                System.arraycopy(matrix, 0, path_matrix, 0, matrix.length);

                final BitSet[] pathMatrixAsBitSetArray = asBitSetArray(path_matrix);

                TransitiveReduction.transformToPathMatrix(pathMatrixAsBitSetArray);

                path_matrix = asIntArray(pathMatrixAsBitSetArray);
            }
            // System.out.println(Arrays.deepToString(path_matrix) + " path
            // matrix");

            Assert.assertArrayEquals(expected_path_matrix, path_matrix);
        }

        // calc transitive reduction
        {
            int[][] transitively_reduced_matrix = new int[n][n];
            {
                System
                    .arraycopy(path_matrix, 0, transitively_reduced_matrix, 0, path_matrix.length);

                final BitSet[] transitivelyReducedMatrixAsBitSetArray =
                    asBitSetArray(transitively_reduced_matrix);

                TransitiveReduction.transitiveReduction(transitivelyReducedMatrixAsBitSetArray);

                transitively_reduced_matrix = asIntArray(transitivelyReducedMatrixAsBitSetArray);
            }

            // System.out.println(Arrays.deepToString(transitively_reduced_matrix)
            // + " transitive reduction");

            Assert.assertArrayEquals(
                expected_transitively_reduced_matrix, transitively_reduced_matrix);
        }
    }

    static private BitSet[] asBitSetArray(final int[][] intArray)
    {
        final BitSet[] ret = new BitSet[intArray.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new BitSet(intArray[i].length);
            for (int j = 0; j < intArray[i].length; j++) {
                ret[i].set(j, intArray[i][j] == 1);
            }
        }
        return ret;
    }

    static private int[][] asIntArray(final BitSet[] bitsetArray)
    {
        final int[][] ret = new int[bitsetArray.length][bitsetArray.length];
        for (int i = 0; i < ret.length; i++) {
            for (int j = 0; j < ret.length; j++) {
                ret[i][j] = bitsetArray[i].get(j) ? 1 : 0;
            }
        }
        return ret;

    }

    @Test(expected = NullPointerException.class)
    public void testReduceNull()
    {
        TransitiveReduction.INSTANCE.reduce(null);
    }

    @Test
    public void testReduceNoVertexNoEdge()
    {
        SimpleDirectedGraph<String, DefaultEdge> graph =
            new SimpleDirectedGraph<>(DefaultEdge.class);
        TransitiveReduction.INSTANCE.reduce(graph);
        assertEquals(graph.vertexSet().size(), 0);
        assertEquals(graph.edgeSet().size(), 0);
    }

    @Test
    public void testReduceSomeVerticesNoEdge()
    {
        SimpleDirectedGraph<String, DefaultEdge> graph =
            new SimpleDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("x");
        graph.addVertex("y");
        graph.addVertex("z");
        TransitiveReduction.INSTANCE.reduce(graph);
        assertEquals(graph.vertexSet().size(), 3);
        assertEquals(graph.edgeSet().size(), 0);
    }

    @Test
    public void testReduceAlreadyReduced()
    {
        SimpleDirectedGraph<String, DefaultEdge> graph =
            new SimpleDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("x");
        graph.addVertex("y");
        graph.addVertex("z");
        graph.addEdge("x", "y");
        graph.addEdge("y", "z");

        assertEquals(graph.vertexSet().size(), 3);
        assertEquals(graph.edgeSet().size(), 2);

        // reduce !
        TransitiveReduction.INSTANCE.reduce(graph);

        assertEquals(graph.vertexSet().size(), 3);
        assertEquals(graph.edgeSet().size(), 2);

        assertTrue(graph.containsEdge("x", "y"));
        assertTrue(graph.containsEdge("y", "z"));
        assertFalse(graph.containsEdge("x", "z"));
    }

    @Test
    public void testReduceBasic()
    {
        SimpleDirectedGraph<String, DefaultEdge> graph =
            new SimpleDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("x");
        graph.addVertex("y");
        graph.addVertex("z");
        graph.addEdge("x", "y");
        graph.addEdge("y", "z");
        graph.addEdge("x", "z"); // <-- reduce me, please

        assertEquals(graph.vertexSet().size(), 3);
        assertEquals(graph.edgeSet().size(), 3);

        // reduce !
        TransitiveReduction.INSTANCE.reduce(graph);

        assertEquals(graph.vertexSet().size(), 3);
        assertEquals(graph.edgeSet().size(), 2);

        assertTrue(graph.containsEdge("x", "y"));
        assertTrue(graph.containsEdge("y", "z"));
        assertFalse(graph.containsEdge("x", "z"));
    }

    @Test
    public void testReduceFarAway()
    {
        SimpleDirectedGraph<String, DefaultEdge> graph =
            new SimpleDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("a");
        graph.addVertex("b");
        graph.addVertex("c");
        graph.addVertex("x");
        graph.addVertex("y");
        graph.addVertex("z");
        graph.addEdge("a", "b");
        graph.addEdge("b", "c");
        graph.addEdge("c", "x");
        graph.addEdge("x", "y");
        graph.addEdge("y", "z");
        graph.addEdge("a", "z"); // <-- reduce me, please

        assertEquals(graph.vertexSet().size(), 6);
        assertEquals(graph.edgeSet().size(), 6);

        // reduce !
        TransitiveReduction.INSTANCE.reduce(graph);

        assertEquals(graph.vertexSet().size(), 6);
        assertEquals(graph.edgeSet().size(), 5);

        assertTrue(graph.containsEdge("a", "b"));
        assertTrue(graph.containsEdge("b", "c"));
        assertTrue(graph.containsEdge("c", "x"));
        assertTrue(graph.containsEdge("x", "y"));
        assertTrue(graph.containsEdge("y", "z"));
        assertFalse(graph.containsEdge("a", "z"));
    }

    @Test
    public void testReduceCanonicalGraph()
    {
        Graph<Integer, DefaultEdge> graph = fromMatrixToDirectedGraph(matrix);

        // a few spot tests to verify the graph looks like it should
        assertFalse(graph.containsEdge(0, 0));
        assertTrue(graph.containsEdge(0, 1));
        assertTrue(graph.containsEdge(2, 4));
        assertTrue(graph.containsEdge(4, 1));

        assertEquals(graph.vertexSet().size(), 5);
        assertEquals(graph.edgeSet().size(), 6);

        // reduce !
        TransitiveReduction.INSTANCE.reduce(graph);

        assertEquals(graph.vertexSet().size(), 5);
        assertEquals(graph.edgeSet().size(), 4);

        // equivalent spot tests on the reduced graph
        assertFalse(graph.containsEdge(0, 0));
        assertFalse(graph.containsEdge(0, 1));
        assertFalse(graph.containsEdge(2, 4));
        assertTrue(graph.containsEdge(4, 1));

        // the full verification; less readable, but somewhat more complete :)
        int[][] actual_transitively_reduced_matrix = fromDirectedGraphToMatrix(graph);
        assertArrayEquals(expected_transitively_reduced_matrix, actual_transitively_reduced_matrix);
    }

    static private Graph<Integer, DefaultEdge> fromMatrixToDirectedGraph(final int[][] matrix)
    {
        final SimpleDirectedGraph<Integer, DefaultEdge> graph =
            new SimpleDirectedGraph<>(DefaultEdge.class);
        for (int i = 0; i < matrix.length; i++) {
            graph.addVertex(i);
        }
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == 1) {
                    graph.addEdge(i, j);
                }
            }
        }

        return graph;
    }

    private int[][] fromDirectedGraphToMatrix(final Graph<Integer, DefaultEdge> directedGraph)
    {
        final List<Integer> vertices = new ArrayList<>(directedGraph.vertexSet());
        final int n = vertices.size();
        final int[][] matrix = new int[n][n];

        final Set<DefaultEdge> edges = directedGraph.edgeSet();
        for (final DefaultEdge edge : edges) {
            final Integer v1 = directedGraph.getEdgeSource(edge);
            final Integer v2 = directedGraph.getEdgeTarget(edge);

            final int v_1 = vertices.indexOf(v1);
            final int v_2 = vertices.indexOf(v2);

            matrix[v_1][v_2] = 1;
        }
        return matrix;

    }

}
