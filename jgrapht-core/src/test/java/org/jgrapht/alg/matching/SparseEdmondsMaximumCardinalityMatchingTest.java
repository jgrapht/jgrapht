/*
 * (C) Copyright 2017-2021, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.matching;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for EdmondsMaximumCardinalityMatching
 *
 * @author Joris Kinable
 */
public final class SparseEdmondsMaximumCardinalityMatchingTest
{

    @Test
    public void testDisconnectedGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3, 4, 5, 6));

        int[][] edges = { { 0, 1 }, { 1, 2 }, { 0, 2 }, { 3, 4 }, { 4, 5 }, { 5, 6 }, { 3, 6 } };
        for (int[] edge : edges) {
            g.addEdge(edge[0], edge[1]);
        }

        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(g);
        Matching<Integer, DefaultEdge> match = matcher.getMatching();
        this.verifyMatching(g, match, 3);

        Map<Integer, Integer> oddSetCover = matcher.getOddSetCover();
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(g, match.getEdges(), oddSetCover));
    }

    @Test
    public void testPseudoGraph()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3, 4, 5, 6));

        int[][] edges = { { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 0 }, { 3, 3 }, { 2, 3 } };
        for (int[] edge : edges)
            g.addEdge(edge[0], edge[1]);

        assertEquals(6, g.edgeSet().size());

        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(g);
        Matching<Integer, DefaultEdge> match = matcher.getMatching();
        this.verifyMatching(g, match, 2);

        Map<Integer, Integer> oddSetCover = matcher.getOddSetCover();
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(g, match.getEdges(), oddSetCover));
    }

    @Test
    public void testGraph15()
    {
        // graph: ([0, 1, 2, 3, 4, 5, 6, 7], [{5,1}, {4,3}, {0,6}, {4,2}, {2,1}, {3,6}, {5,0}])
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7));

        int[][] edges = { { 5, 1 }, { 4, 3 }, { 0, 6 }, { 4, 2 }, { 2, 1 }, { 3, 6 }, { 5, 0 } };
        for (int[] edge : edges)
            g.addEdge(edge[0], edge[1]);

        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(g);
        Matching<Integer, DefaultEdge> match = matcher.getMatching();
        this.verifyMatching(g, match, 3);

        Map<Integer, Integer> oddSetCover = matcher.getOddSetCover();
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(g, match.getEdges(), oddSetCover));
    }

    @Test
    public void testGraph14()
    {
        // graph: ([0, 1, 2, 3, 4, 5, 6, 7], [{2,0}, {2,6}, {4,6}, {4,3}, {6,7}, {3,6}, {5,0},
        // {2,5}, {3,7}, {2,4}])
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7));

        int[][] edges = { { 2, 0 }, { 2, 6 }, { 4, 6 }, { 4, 3 }, { 6, 7 }, { 3, 6 }, { 5, 0 },
            { 2, 5 }, { 3, 7 }, { 2, 4 } };
        for (int[] edge : edges)
            g.addEdge(edge[0], edge[1]);

        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(g);
        Matching<Integer, DefaultEdge> match = matcher.getMatching();

        Map<Integer, Integer> oddSetCover = matcher.getOddSetCover();
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(g, match.getEdges(), oddSetCover));
    }

    @Test
    public void testGraph13()
    {
        // graph: ([0, 1, 2, 3, 4], [{0,3}, {0,2}, {4,2}, {0,1}, {1,3}])
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3, 4));

        int[][] edges = { { 0, 3 }, { 0, 2 }, { 4, 2 }, { 0, 1 }, { 1, 3 } };
        for (int[] edge : edges)
            g.addEdge(edge[0], edge[1]);

        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(g);
        Matching<Integer, DefaultEdge> match = matcher.getMatching();

        Map<Integer, Integer> oddSetCover = matcher.getOddSetCover();
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(g, match.getEdges(), oddSetCover));
    }

    @Test
    public void testGraph12()
    {
        // graph: ([0, 1, 2, 3], [{3,2}, {3,1}, {0,3}, {0,1}, {2,1}])
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3));

        int[][] edges = { { 3, 2 }, { 3, 1 }, { 0, 3 }, { 0, 1 }, { 2, 1 } };
        for (int[] edge : edges)
            g.addEdge(edge[0], edge[1]);

        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(g);
        Matching<Integer, DefaultEdge> match = matcher.getMatching();

        Map<Integer, Integer> oddSetCover = matcher.getOddSetCover();
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(g, match.getEdges(), oddSetCover));
    }

    @Test
    public void testGraph11()
    {
        // graph: ([0, 1, 2, 3], [])
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3));

        int[][] edges = { { 0, 2 }, { 1, 0 }, { 2, 1 }, { 0, 3 }, { 2, 3 } };
        for (int[] edge : edges)
            g.addEdge(edge[0], edge[1]);

        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(g);
        Matching<Integer, DefaultEdge> match = matcher.getMatching();

        Map<Integer, Integer> oddSetCover = matcher.getOddSetCover();
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(g, match.getEdges(), oddSetCover));
    }

    @Test
    public void testIsMaximumMatching3()
    {
        // graph: ([0, 1, 2, 3, 4, 5, 6], [{4,0}, {2,3}, {2,0}, {2,5}, {2,6}, {0,1}])
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3, 4, 5, 6));
        g.addEdge(4, 0);
        g.addEdge(2, 3);
        g.addEdge(2, 0);
        g.addEdge(2, 5);
        g.addEdge(2, 6);
        g.addEdge(0, 1);

        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(g);
        Matching<Integer, DefaultEdge> match = matcher.getMatching();

        Map<Integer, Integer> oddSetCover = matcher.getOddSetCover();
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(g, match.getEdges(), oddSetCover));
    }

    @Test
    public void testIsMaximumMatching2()
    {
        // Graph contains one isolated vertex: 6
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3, 4, 5, 7, 8, 9));
        int[][] edges = { { 0, 8 }, { 9, 7 }, { 5, 3 }, { 9, 4 }, { 3, 2 }, { 5, 4 }, { 1, 0 },
            { 3, 8 }, { 4, 7 }, { 2, 0 }, { 8, 5 }, { 0, 5 }, { 8, 1 } };
        for (int[] edge : edges)
            graph.addEdge(edge[0], edge[1]);

        Set<DefaultEdge> mEdges = new HashSet<>();
        mEdges.add(graph.getEdge(0, 1));
        mEdges.add(graph.getEdge(2, 3));
        mEdges.add(graph.getEdge(4, 9));
        mEdges.add(graph.getEdge(5, 8));
        Matching<Integer, DefaultEdge> m = new MatchingAlgorithm.MatchingImpl<>(graph, mEdges, 4);
        verifyMatching(graph, m, 4);
    }

    @Test
    public void testIsMaximum1()
    {
        // graph: ([0, 1, 2, 3, 4, 5, 6], [{5,6}, {1,2}, {0,6}, {4,6}, {2,6}])
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3, 4, 5, 6));
        g.addEdge(5, 6);
        g.addEdge(1, 2);
        g.addEdge(0, 6);
        g.addEdge(4, 6);
        g.addEdge(2, 6);

        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(g);

        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(g, matcher.getMatching().getEdges(), matcher.getOddSetCover()));
    }

    @Test
    public void testRandomGraphsLarge()
    {
        Random random = new Random(1);
        int vertices = 100;

        for (int k = 0; k < 100; k++) {
            int edges = random.nextInt(maxEdges(vertices) / 2);
            GraphGenerator<Integer, DefaultEdge, Integer> generator =
                new GnmRandomGraphGenerator<>(vertices, edges, 0);

            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            generator.generateGraph(graph);
            SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
                new SparseEdmondsMaximumCardinalityMatching<>(graph);

            Matching<Integer, DefaultEdge> m = matcher.getMatching();
            this.verifyMatching(graph, m, m.getEdges().size());
            assertTrue(
                SparseEdmondsMaximumCardinalityMatching
                    .isOptimalMatching(graph, m.getEdges(), matcher.getOddSetCover()));
        }
    }

    @Test
    public void testRandomGraphsBarabasiLarge()
    {
        Random random = new Random(1324);
        int vertices = 250;

        for (int k = 0; k < 10; k++) {

            BarabasiAlbertGraphGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertGraphGenerator<>(6, 6, vertices, random);

            Graph<Integer,
                DefaultEdge> graph = GraphTypeBuilder
                    .undirected().vertexSupplier(SupplierUtil.createIntegerSupplier())
                    .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).weighted(false).buildGraph();

            generator.generateGraph(graph);

            SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
                new SparseEdmondsMaximumCardinalityMatching<>(graph);

            Matching<Integer, DefaultEdge> m = matcher.getMatching();
            assertTrue(
                SparseEdmondsMaximumCardinalityMatching
                    .isOptimalMatching(graph, m.getEdges(), matcher.getOddSetCover()));
        }
    }

    @Test
    public void testRandomGraphsBarabasiLargeNoSeed()
    {
        int vertices = 250;

        for (int k = 0; k < 10; k++) {

            BarabasiAlbertGraphGenerator<Integer, DefaultEdge> generator =
                new BarabasiAlbertGraphGenerator<>(6, 6, vertices);

            Graph<Integer,
                DefaultEdge> graph = GraphTypeBuilder
                    .undirected().vertexSupplier(SupplierUtil.createIntegerSupplier())
                    .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).weighted(false).buildGraph();

            generator.generateGraph(graph);

            SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
                new SparseEdmondsMaximumCardinalityMatching<>(graph);

            Matching<Integer, DefaultEdge> m = matcher.getMatching();
            assertTrue(
                SparseEdmondsMaximumCardinalityMatching
                    .isOptimalMatching(graph, m.getEdges(), matcher.getOddSetCover()));
        }
    }

    @Test
    public void testRandomGraphsSmall()
    {
        for (int n = 4; n < 12; n++) {
            for (int m = 5; m < maxEdges(n); m++) {
                GraphGenerator<Integer, DefaultEdge, Integer> generator =
                    new GnmRandomGraphGenerator<>(n, m);

                for (int i = 0; i < 25; i++) {
                    Graph<Integer,
                        DefaultEdge> graph = new SimpleGraph<>(
                            SupplierUtil.createIntegerSupplier(),
                            SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
                    generator.generateGraph(graph);
                    SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
                        new SparseEdmondsMaximumCardinalityMatching<>(graph);
                    Matching<Integer, DefaultEdge> m1 = matcher.getMatching();
                    assertTrue(
                        SparseEdmondsMaximumCardinalityMatching
                            .isOptimalMatching(graph, m1.getEdges(), matcher.getOddSetCover()));
                }
            }
        }
    }

    @Test
    public void testGraph1()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        int[][] edges = { { 97, 22 }, { 56, 105 }, { 89, 132 }, { 117, 25 }, { 83, 106 },
            { 171, 49 }, { 162, 138 }, { 90, 120 }, { 152, 33 }, { 47, 81 }, { 70, 191 },
            { 23, 142 }, { 80, 53 }, { 106, 111 }, { 7, 9 }, { 11, 71 }, { 186, 177 }, { 196, 28 },
            { 55, 106 }, { 134, 89 }, { 178, 123 }, { 109, 169 }, { 104, 27 }, { 162, 42 },
            { 102, 164 }, { 51, 92 }, { 26, 10 }, { 141, 165 }, { 107, 164 }, { 41, 2 },
            { 125, 46 }, { 189, 59 }, { 68, 104 }, { 161, 36 }, { 154, 143 }, { 129, 92 },
            { 139, 110 }, { 43, 76 }, { 197, 1 }, { 118, 38 }, { 6, 53 }, { 123, 62 }, { 125, 55 },
            { 183, 81 }, { 67, 120 }, { 54, 57 }, { 34, 25 }, { 156, 171 }, { 139, 49 },
            { 108, 142 }, { 54, 184 }, { 124, 199 }, { 82, 191 }, { 23, 85 }, { 181, 71 },
            { 154, 102 }, { 69, 98 }, { 131, 52 }, { 36, 33 }, { 146, 160 }, { 114, 75 },
            { 92, 137 }, { 172, 31 }, { 188, 25 }, { 123, 119 }, { 178, 21 }, { 96, 97 },
            { 72, 118 }, { 34, 106 }, { 175, 185 }, { 138, 121 }, { 185, 183 }, { 46, 62 },
            { 135, 25 }, { 66, 21 }, { 194, 109 }, { 125, 123 }, { 62, 181 }, { 198, 156 },
            { 99, 34 }, { 87, 174 }, { 165, 45 }, { 114, 125 }, { 164, 101 }, { 9, 36 },
            { 102, 146 }, { 138, 189 }, { 159, 117 }, { 78, 69 }, { 50, 66 }, { 27, 63 },
            { 122, 107 }, { 151, 11 }, { 58, 34 }, { 77, 195 }, { 122, 186 }, { 84, 98 },
            { 171, 91 }, { 19, 33 }, { 41, 16 }, { 81, 40 }, { 87, 7 }, { 65, 4 }, { 178, 155 },
            { 130, 106 }, { 131, 42 }, { 174, 71 }, { 30, 103 }, { 186, 83 }, { 108, 185 },
            { 112, 77 }, { 62, 56 }, { 198, 34 }, { 4, 17 }, { 182, 139 }, { 159, 25 }, { 96, 9 },
            { 192, 38 }, { 187, 104 }, { 27, 157 } };
        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);
        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(graph);
        verifyMatching(graph, matcher.getMatching(), 58);
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(
                    graph, matcher.getMatching().getEdges(), matcher.getOddSetCover()));
    }

    @Test
    public void testGraph2()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        int[][] edges = { { 73, 143 }, { 139, 145 }, { 185, 74 }, { 23, 28 }, { 62, 4 },
            { 86, 106 }, { 159, 60 }, { 55, 119 }, { 30, 36 }, { 79, 58 }, { 21, 11 }, { 83, 134 },
            { 166, 33 }, { 128, 122 }, { 46, 147 }, { 48, 67 }, { 182, 93 }, { 92, 105 },
            { 198, 4 }, { 168, 38 }, { 21, 142 }, { 72, 19 }, { 97, 96 }, { 37, 85 }, { 47, 119 },
            { 139, 108 }, { 88, 83 }, { 162, 69 }, { 41, 45 }, { 127, 125 }, { 197, 34 },
            { 63, 189 }, { 153, 135 }, { 184, 102 }, { 19, 44 }, { 75, 2 }, { 168, 165 },
            { 121, 17 }, { 49, 179 }, { 169, 69 }, { 137, 58 }, { 52, 85 }, { 14, 179 },
            { 133, 134 }, { 169, 82 }, { 139, 114 }, { 14, 196 }, { 198, 12 }, { 122, 164 },
            { 94, 62 }, { 124, 98 }, { 34, 8 }, { 199, 178 }, { 59, 194 }, { 56, 196 }, { 15, 121 },
            { 5, 1 }, { 26, 128 }, { 120, 111 }, { 187, 11 }, { 175, 191 }, { 62, 71 }, { 58, 52 },
            { 67, 53 }, { 64, 62 }, { 186, 142 }, { 141, 156 }, { 40, 96 }, { 132, 32 }, { 35, 15 },
            { 45, 149 }, { 199, 143 }, { 100, 147 }, { 130, 29 }, { 38, 155 }, { 26, 77 },
            { 16, 22 }, { 49, 125 }, { 158, 199 }, { 146, 9 }, { 140, 65 }, { 91, 176 },
            { 116, 83 }, { 70, 116 }, { 21, 17 }, { 190, 119 }, { 13, 116 }, { 83, 0 }, { 11, 33 },
            { 113, 13 }, { 195, 150 }, { 179, 139 }, { 71, 56 }, { 128, 40 }, { 22, 91 },
            { 63, 106 }, { 123, 161 }, { 53, 40 }, { 189, 172 }, { 56, 70 }, { 158, 86 },
            { 174, 166 }, { 75, 69 }, { 135, 9 }, { 127, 157 }, { 76, 102 }, { 116, 173 },
            { 90, 145 }, { 167, 75 }, { 135, 29 }, { 7, 179 }, { 96, 44 }, { 178, 50 }, { 60, 194 },
            { 107, 74 }, { 9, 26 }, { 171, 14 }, { 153, 136 }, { 179, 197 }, { 16, 78 },
            { 156, 19 }, { 45, 26 }, { 172, 83 }, { 51, 132 }, { 190, 65 }, { 26, 6 }, { 4, 59 },
            { 63, 84 }, { 155, 8 }, { 125, 154 }, { 42, 97 }, { 182, 69 }, { 184, 196 },
            { 42, 122 }, { 146, 159 }, { 26, 66 }, { 134, 105 }, { 98, 23 }, { 128, 92 },
            { 97, 152 }, { 182, 167 }, { 22, 24 }, { 41, 8 }, { 97, 13 }, { 123, 70 }, { 74, 173 },
            { 143, 77 }, { 90, 18 }, { 97, 59 }, { 21, 94 }, { 83, 15 }, { 49, 90 }, { 65, 105 },
            { 189, 120 }, { 75, 124 }, { 177, 176 }, { 140, 152 }, { 70, 135 }, { 155, 157 },
            { 17, 196 }, { 190, 130 }, { 9, 21 }, { 109, 66 }, { 98, 174 }, { 146, 112 }, { 6, 33 },
            { 83, 138 }, { 40, 17 }, { 185, 64 }, { 84, 136 }, { 153, 16 }, { 134, 1 }, { 45, 106 },
            { 165, 48 }, { 198, 150 }, { 60, 77 }, { 103, 150 }, { 117, 15 }, { 113, 5 },
            { 106, 77 }, { 15, 111 }, { 170, 144 }, { 173, 50 }, { 180, 20 }, { 170, 114 },
            { 34, 117 }, { 111, 131 }, { 173, 46 }, { 19, 175 }, { 89, 149 }, { 52, 15 },
            { 5, 195 }, { 114, 23 }, { 166, 41 }, { 182, 122 }, { 131, 3 }, { 99, 77 }, { 40, 22 },
            { 176, 35 }, { 12, 186 }, { 112, 89 }, { 10, 76 }, { 115, 172 }, { 30, 84 },
            { 180, 93 }, { 98, 11 }, { 160, 120 }, { 141, 18 }, { 112, 11 }, { 73, 114 },
            { 28, 42 }, { 103, 29 }, { 1, 175 }, { 161, 52 }, { 118, 150 }, { 148, 187 },
            { 137, 47 }, { 192, 55 }, { 145, 149 }, { 198, 87 }, { 191, 139 }, { 21, 40 },
            { 134, 174 }, { 136, 162 }, { 4, 46 }, { 39, 64 }, { 68, 38 }, { 73, 109 }, { 74, 34 },
            { 43, 130 }, { 10, 56 }, { 24, 140 }, { 117, 144 }, { 180, 178 }, { 185, 37 },
            { 14, 180 }, { 131, 45 }, { 18, 6 }, { 4, 61 }, { 57, 132 }, { 189, 94 }, { 38, 176 },
            { 104, 124 }, { 125, 31 }, { 45, 156 }, { 145, 170 }, { 182, 125 }, { 106, 185 },
            { 168, 152 }, { 146, 145 }, { 62, 90 }, { 99, 125 }, { 195, 64 }, { 135, 50 },
            { 4, 81 }, { 186, 149 }, { 104, 175 }, { 112, 144 }, { 189, 47 }, { 55, 17 }, { 0, 41 },
            { 1, 19 }, { 192, 97 }, { 192, 37 }, { 111, 99 }, { 197, 137 }, { 174, 38 }, { 64, 80 },
            { 20, 7 }, { 6, 96 }, { 19, 166 }, { 129, 71 }, { 72, 149 }, { 34, 145 }, { 173, 141 },
            { 27, 78 }, { 84, 171 }, { 36, 199 }, { 82, 144 }, { 13, 190 }, { 85, 30 }, { 67, 157 },
            { 44, 126 }, { 24, 23 }, { 163, 187 }, { 61, 39 }, { 105, 152 }, { 49, 165 },
            { 103, 42 }, { 5, 72 }, { 166, 73 }, { 135, 40 }, { 121, 50 }, { 193, 181 },
            { 55, 196 }, { 13, 170 }, { 51, 181 }, { 170, 72 }, { 47, 33 }, { 179, 80 },
            { 135, 186 }, { 127, 10 }, { 184, 114 }, { 60, 12 }, { 121, 157 }, { 42, 16 },
            { 148, 131 }, { 106, 65 }, { 25, 93 }, { 164, 132 }, { 104, 145 }, { 106, 100 },
            { 8, 25 }, { 23, 21 }, { 49, 5 }, { 162, 194 }, { 186, 193 }, { 109, 123 }, { 72, 81 },
            { 141, 126 }, { 37, 56 }, { 5, 77 }, { 144, 192 }, { 35, 32 }, { 6, 100 }, { 151, 25 },
            { 28, 26 }, { 108, 174 }, { 96, 28 }, { 196, 84 }, { 44, 29 }, { 100, 78 }, { 109, 97 },
            { 193, 69 }, { 118, 189 }, { 101, 161 }, { 172, 197 }, { 10, 94 }, { 198, 76 },
            { 178, 170 }, { 4, 158 }, { 97, 50 }, { 112, 194 }, { 39, 189 }, { 157, 131 },
            { 62, 96 }, { 146, 101 }, { 4, 40 }, { 107, 181 }, { 181, 102 }, { 53, 98 },
            { 185, 116 }, { 76, 119 }, { 45, 94 }, { 83, 178 }, { 1, 156 }, { 38, 78 }, { 157, 84 },
            { 103, 181 }, { 161, 60 }, { 156, 186 }, { 71, 165 }, { 10, 90 }, { 111, 27 },
            { 32, 2 }, { 135, 24 }, { 58, 92 }, { 53, 65 }, { 103, 195 }, { 3, 113 }, { 66, 22 },
            { 125, 73 }, { 58, 42 }, { 9, 137 }, { 70, 87 }, { 116, 95 }, { 121, 4 }, { 53, 46 },
            { 61, 7 }, { 112, 35 }, { 175, 125 }, { 194, 33 }, { 183, 167 }, { 172, 156 },
            { 27, 173 }, { 142, 98 }, { 23, 165 }, { 25, 30 }, { 99, 92 }, { 48, 134 }, { 16, 109 },
            { 100, 60 }, { 176, 63 }, { 83, 57 }, { 137, 120 }, { 61, 157 }, { 78, 32 },
            { 154, 132 }, { 179, 72 }, { 99, 90 }, { 194, 1 }, { 11, 127 }, { 159, 129 },
            { 114, 198 }, { 156, 49 }, { 111, 79 }, { 18, 42 }, { 46, 156 }, { 157, 37 },
            { 21, 73 }, { 114, 9 }, { 151, 12 }, { 124, 140 }, { 183, 34 }, { 134, 63 },
            { 194, 75 }, { 151, 99 }, { 85, 120 }, { 108, 85 }, { 106, 68 }, { 48, 24 }, { 15, 42 },
            { 118, 198 }, { 91, 131 }, { 88, 146 }, { 123, 55 }, { 77, 173 }, { 176, 16 },
            { 66, 103 }, { 153, 42 }, { 64, 182 }, { 85, 150 }, { 5, 34 }, { 69, 174 }, { 34, 129 },
            { 74, 179 }, { 49, 86 }, { 195, 90 }, { 88, 99 }, { 184, 29 }, { 110, 80 },
            { 144, 173 }, { 49, 12 }, { 27, 157 }, { 98, 16 }, { 157, 170 }, { 126, 27 },
            { 64, 55 }, { 17, 16 }, { 180, 157 }, { 33, 100 }, { 6, 88 }, { 107, 124 }, { 175, 66 },
            { 71, 158 }, { 85, 111 }, { 166, 143 }, { 8, 100 }, { 5, 59 }, { 111, 11 }, { 22, 104 },
            { 183, 194 }, { 135, 185 }, { 110, 43 }, { 147, 192 }, { 79, 140 }, { 130, 126 },
            { 96, 85 }, { 107, 67 }, { 160, 122 }, { 149, 178 }, { 10, 150 }, { 140, 172 },
            { 128, 111 }, { 77, 170 }, { 102, 11 }, { 60, 65 }, { 30, 163 }, { 5, 94 }, { 181, 45 },
            { 76, 68 }, { 95, 24 }, { 89, 152 }, { 2, 96 }, { 50, 1 }, { 173, 81 }, { 174, 42 },
            { 136, 38 }, { 120, 60 }, { 21, 107 }, { 0, 197 }, { 74, 148 }, { 96, 186 },
            { 114, 57 }, { 184, 24 }, { 194, 99 }, { 86, 16 }, { 135, 144 }, { 110, 177 },
            { 58, 170 }, { 33, 104 }, { 164, 77 }, { 29, 98 }, { 188, 103 }, { 105, 26 },
            { 26, 179 }, { 163, 101 }, { 95, 118 }, { 120, 123 }, { 187, 178 }, { 8, 176 },
            { 35, 64 }, { 67, 104 }, { 5, 48 }, { 44, 114 }, { 105, 45 }, { 32, 171 }, { 134, 164 },
            { 99, 19 }, { 93, 98 }, { 128, 117 }, { 22, 77 }, { 42, 143 }, { 94, 67 }, { 147, 122 },
            { 130, 87 }, { 96, 27 }, { 42, 90 }, { 72, 104 }, { 52, 53 }, { 168, 134 },
            { 164, 109 }, { 76, 199 }, { 6, 127 }, { 11, 49 }, { 8, 19 }, { 167, 121 }, { 158, 46 },
            { 120, 167 }, { 43, 167 }, { 149, 179 }, { 152, 115 }, { 86, 5 }, { 61, 147 },
            { 72, 162 }, { 138, 51 }, { 54, 146 }, { 88, 190 }, { 88, 199 }, { 8, 117 }, { 11, 48 },
            { 144, 78 }, { 77, 19 }, { 38, 161 }, { 115, 9 }, { 74, 126 }, { 113, 178 },
            { 116, 146 }, { 124, 10 }, { 39, 17 }, { 16, 148 }, { 81, 197 }, { 114, 138 },
            { 55, 84 }, { 65, 111 }, { 154, 176 }, { 189, 35 }, { 96, 175 }, { 92, 182 },
            { 73, 67 }, { 141, 152 }, { 167, 100 }, { 67, 172 }, { 16, 73 }, { 32, 93 },
            { 12, 126 }, { 35, 1 }, { 13, 167 }, { 55, 98 }, { 15, 163 }, { 18, 11 }, { 78, 132 },
            { 95, 104 }, { 174, 170 }, { 16, 30 }, { 148, 87 }, { 91, 41 }, { 111, 189 },
            { 135, 172 }, { 113, 60 }, { 196, 147 }, { 64, 88 }, { 141, 5 }, { 19, 62 },
            { 179, 142 }, { 155, 111 }, { 87, 48 }, { 25, 158 }, { 67, 41 }, { 118, 131 },
            { 53, 167 }, { 26, 106 }, { 145, 144 }, { 172, 142 }, { 82, 135 }, { 91, 110 },
            { 167, 193 }, { 63, 86 }, { 17, 97 }, { 104, 157 }, { 133, 177 }, { 30, 129 },
            { 38, 91 }, { 186, 190 }, { 144, 38 }, { 176, 7 }, { 139, 57 }, { 52, 193 }, { 96, 64 },
            { 57, 84 }, { 40, 8 }, { 93, 103 }, { 32, 92 }, { 164, 90 }, { 180, 8 }, { 168, 23 },
            { 95, 34 }, { 154, 58 }, { 92, 17 }, { 176, 112 }, { 101, 110 }, { 109, 23 },
            { 154, 59 }, { 44, 98 }, { 32, 41 }, { 39, 119 }, { 159, 141 }, { 177, 46 },
            { 120, 71 }, { 114, 199 }, { 160, 66 }, { 81, 56 }, { 73, 28 }, { 89, 185 },
            { 130, 91 }, { 158, 67 }, { 68, 74 }, { 59, 143 }, { 130, 64 }, { 74, 124 },
            { 19, 170 }, { 103, 80 }, { 136, 94 }, { 121, 143 }, { 20, 176 }, { 173, 10 },
            { 53, 106 }, { 72, 78 }, { 46, 140 }, { 105, 125 }, { 86, 36 }, { 9, 151 }, { 41, 182 },
            { 80, 77 }, { 55, 63 }, { 127, 82 }, { 67, 160 }, { 164, 64 }, { 164, 60 }, { 191, 10 },
            { 74, 20 }, { 10, 172 }, { 104, 136 }, { 166, 30 }, { 128, 10 }, { 119, 151 },
            { 154, 150 }, { 93, 190 }, { 155, 85 }, { 161, 132 }, { 46, 153 }, { 96, 70 },
            { 89, 75 }, { 15, 150 }, { 31, 0 }, { 155, 152 }, { 189, 18 }, { 123, 154 }, { 67, 51 },
            { 77, 29 }, { 34, 140 }, { 113, 1 }, { 68, 19 }, { 190, 100 }, { 68, 43 }, { 167, 154 },
            { 91, 133 }, { 154, 169 }, { 76, 165 }, { 149, 28 }, { 117, 23 }, { 13, 15 },
            { 170, 0 }, { 156, 58 }, { 40, 57 }, { 62, 6 }, { 50, 110 }, { 40, 116 }, { 52, 69 },
            { 6, 69 }, { 26, 146 }, { 72, 68 }, { 75, 32 }, { 72, 56 }, { 72, 33 }, { 194, 125 },
            { 51, 190 }, { 184, 26 }, { 101, 170 }, { 145, 126 }, { 32, 176 }, { 22, 60 },
            { 14, 198 }, { 17, 189 }, { 148, 177 }, { 194, 84 }, { 87, 55 }, { 71, 172 },
            { 41, 164 }, { 183, 46 }, { 155, 199 }, { 84, 56 }, { 137, 150 }, { 138, 155 },
            { 23, 116 }, { 9, 20 }, { 104, 115 }, { 64, 158 }, { 150, 57 }, { 75, 58 }, { 117, 54 },
            { 72, 62 }, { 160, 184 }, { 2, 167 }, { 170, 56 }, { 137, 194 }, { 42, 181 },
            { 84, 137 }, { 148, 175 }, { 29, 33 }, { 16, 126 }, { 135, 191 }, { 186, 1 },
            { 70, 43 }, { 77, 178 }, { 194, 124 }, { 28, 30 }, { 114, 195 }, { 111, 146 },
            { 137, 116 }, { 6, 41 }, { 168, 63 }, { 149, 91 }, { 0, 125 }, { 180, 96 }, { 57, 78 },
            { 154, 42 }, { 92, 87 }, { 198, 63 }, { 2, 40 }, { 153, 141 }, { 191, 60 }, { 131, 1 },
            { 119, 146 }, { 96, 184 }, { 83, 56 }, { 111, 158 }, { 119, 135 }, { 71, 93 },
            { 155, 25 }, { 78, 109 }, { 140, 55 }, { 80, 34 }, { 119, 111 }, { 152, 171 },
            { 193, 91 }, { 6, 23 }, { 110, 21 }, { 125, 146 }, { 20, 70 }, { 187, 78 }, { 72, 139 },
            { 59, 45 }, { 196, 63 }, { 130, 128 }, { 78, 11 }, { 80, 151 }, { 22, 68 }, { 177, 68 },
            { 77, 145 }, { 87, 150 }, { 16, 18 }, { 129, 107 }, { 87, 126 }, { 77, 12 },
            { 121, 196 }, { 173, 162 }, { 126, 163 }, { 192, 110 }, { 129, 149 }, { 144, 30 },
            { 191, 198 }, { 27, 140 }, { 128, 114 }, { 136, 56 }, { 137, 37 }, { 64, 46 },
            { 44, 36 }, { 111, 106 }, { 165, 160 }, { 13, 8 }, { 97, 115 }, { 118, 152 },
            { 141, 179 }, { 114, 6 }, { 80, 139 }, { 121, 120 }, { 102, 169 }, { 95, 136 },
            { 3, 178 }, { 80, 97 }, { 112, 129 }, { 24, 39 }, { 36, 159 }, { 137, 159 },
            { 178, 61 }, { 10, 109 }, { 107, 5 }, { 137, 61 }, { 136, 110 }, { 157, 17 },
            { 159, 125 }, { 154, 9 }, { 39, 152 }, { 185, 136 }, { 105, 23 }, { 91, 3 },
            { 117, 148 }, { 195, 109 }, { 150, 153 }, { 13, 45 }, { 171, 40 }, { 183, 137 },
            { 187, 181 }, { 34, 148 }, { 84, 192 }, { 77, 74 }, { 1, 135 }, { 14, 5 }, { 149, 49 },
            { 77, 104 }, { 160, 68 }, { 160, 88 }, { 93, 72 }, { 189, 195 }, { 173, 69 },
            { 71, 96 }, { 172, 30 }, { 15, 158 }, { 189, 185 }, { 102, 185 }, { 101, 62 },
            { 165, 15 }, { 178, 0 }, { 69, 29 }, { 7, 132 }, { 123, 79 }, { 0, 30 }, { 108, 17 },
            { 81, 190 }, { 181, 78 }, { 162, 29 }, { 112, 74 }, { 168, 41 }, { 150, 44 },
            { 145, 88 }, { 23, 148 }, { 187, 34 }, { 174, 12 }, { 33, 63 }, { 179, 138 },
            { 27, 199 }, { 198, 83 }, { 65, 192 }, { 197, 10 }, { 92, 81 }, { 12, 22 }, { 56, 34 },
            { 101, 190 }, { 21, 5 }, { 153, 54 }, { 191, 197 }, { 106, 140 }, { 45, 14 },
            { 189, 164 }, { 151, 139 }, { 61, 140 }, { 171, 67 }, { 0, 28 }, { 188, 106 },
            { 68, 7 }, { 72, 120 }, { 73, 94 }, { 136, 182 }, { 155, 102 }, { 141, 60 }, { 47, 16 },
            { 17, 54 }, { 23, 102 }, { 1, 197 }, { 68, 158 }, { 69, 73 }, { 112, 188 }, { 43, 138 },
            { 19, 183 }, { 49, 61 }, { 196, 174 }, { 190, 84 }, { 158, 154 }, { 105, 195 },
            { 61, 54 }, { 35, 167 }, { 134, 29 }, { 74, 96 }, { 104, 109 }, { 87, 136 },
            { 176, 78 }, { 33, 18 }, { 176, 161 }, { 163, 100 }, { 158, 190 }, { 153, 23 },
            { 61, 85 }, { 130, 109 }, { 162, 110 }, { 29, 4 }, { 31, 66 }, { 58, 165 },
            { 118, 194 }, { 147, 77 }, { 146, 139 }, { 180, 183 }, { 144, 186 }, { 58, 157 },
            { 137, 19 }, { 153, 175 }, { 112, 95 }, { 176, 172 }, { 163, 29 }, { 156, 75 },
            { 29, 15 }, { 147, 149 }, { 104, 48 }, { 135, 152 }, { 67, 65 }, { 153, 127 },
            { 70, 14 }, { 114, 141 }, { 29, 183 }, { 144, 115 }, { 191, 150 }, { 73, 76 },
            { 130, 168 }, { 181, 165 }, { 116, 12 }, { 91, 10 }, { 184, 177 }, { 123, 156 },
            { 36, 157 }, { 123, 191 }, { 67, 87 }, { 184, 151 }, { 44, 70 }, { 139, 98 },
            { 85, 163 }, { 61, 128 }, { 29, 0 }, { 192, 0 }, { 108, 189 }, { 170, 87 },
            { 147, 117 }, { 179, 112 }, { 162, 57 }, { 41, 126 }, { 68, 112 }, { 135, 74 },
            { 92, 15 }, { 159, 20 }, { 23, 123 }, { 87, 5 }, { 83, 135 }, { 6, 169 }, { 8, 145 },
            { 7, 103 }, { 2, 118 }, { 1, 25 }, { 48, 86 }, { 176, 158 }, { 12, 10 }, { 28, 111 },
            { 55, 50 }, { 171, 191 }, { 80, 27 }, { 166, 147 }, { 33, 22 }, { 125, 48 },
            { 71, 123 }, { 156, 108 }, { 3, 69 }, { 178, 190 }, { 4, 126 }, { 37, 2 }, { 140, 112 },
            { 118, 147 }, { 176, 61 }, { 176, 175 }, { 12, 64 }, { 73, 24 }, { 11, 24 },
            { 111, 141 }, { 77, 82 }, { 52, 166 }, { 119, 141 }, { 165, 114 }, { 160, 91 },
            { 24, 101 }, { 196, 115 }, { 75, 166 }, { 131, 140 }, { 51, 165 }, { 20, 122 },
            { 34, 65 }, { 19, 30 }, { 140, 108 }, { 65, 78 }, { 126, 155 }, { 137, 42 },
            { 167, 79 }, { 25, 122 }, { 81, 57 }, { 49, 140 }, { 60, 163 }, { 163, 193 },
            { 128, 185 }, { 182, 7 }, { 100, 181 }, { 33, 185 }, { 120, 178 }, { 97, 2 },
            { 91, 137 }, { 143, 40 }, { 50, 127 }, { 57, 157 }, { 38, 0 }, { 32, 44 }, { 186, 196 },
            { 132, 87 }, { 77, 64 }, { 199, 151 }, { 192, 106 }, { 135, 30 }, { 118, 169 },
            { 158, 88 }, { 66, 71 }, { 17, 174 }, { 178, 156 }, { 19, 152 }, { 25, 2 }, { 121, 90 },
            { 136, 12 }, { 50, 197 }, { 24, 191 }, { 22, 10 }, { 23, 156 }, { 171, 154 },
            { 39, 51 }, { 31, 38 }, { 144, 93 }, { 114, 82 }, { 83, 8 }, { 166, 87 }, { 118, 67 },
            { 172, 166 }, { 172, 18 }, { 98, 109 }, { 74, 121 }, { 92, 68 }, { 50, 53 },
            { 106, 125 }, { 84, 179 }, { 34, 199 }, { 96, 132 }, { 107, 127 }, { 124, 62 },
            { 75, 59 }, { 152, 131 }, { 198, 171 }, { 0, 173 }, { 35, 99 }, { 127, 113 },
            { 44, 194 }, { 129, 49 }, { 187, 125 }, { 134, 180 }, { 92, 53 }, { 14, 80 },
            { 11, 23 }, { 37, 136 }, { 10, 178 }, { 125, 56 }, { 158, 11 }, { 114, 13 }, { 135, 4 },
            { 182, 106 }, { 58, 11 }, { 18, 12 }, { 93, 54 }, { 165, 31 }, { 119, 53 }, { 36, 58 },
            { 65, 96 }, { 6, 126 }, { 61, 71 }, { 67, 12 }, { 161, 56 }, { 73, 164 }, { 128, 107 },
            { 26, 65 }, { 70, 162 }, { 133, 193 }, { 37, 116 }, { 193, 121 }, { 158, 33 },
            { 92, 103 }, { 106, 85 }, { 190, 146 }, { 189, 131 }, { 29, 149 }, { 53, 111 },
            { 99, 53 }, { 77, 8 }, { 67, 187 }, { 78, 121 }, { 184, 171 }, { 178, 152 },
            { 191, 82 }, { 190, 152 }, { 67, 136 }, { 103, 32 }, { 40, 91 }, { 95, 70 },
            { 174, 152 }, { 178, 90 }, { 136, 119 }, { 25, 22 }, { 57, 119 }, { 107, 94 },
            { 129, 48 }, { 63, 108 }, { 136, 113 }, { 70, 72 }, { 53, 79 }, { 96, 140 },
            { 115, 183 }, { 174, 165 }, { 49, 162 }, { 0, 109 }, { 60, 55 }, { 106, 166 },
            { 172, 23 }, { 79, 65 }, { 160, 130 }, { 114, 197 }, { 174, 199 }, { 187, 24 },
            { 97, 69 }, { 11, 35 }, { 104, 103 }, { 110, 189 }, { 194, 109 }, { 112, 170 },
            { 194, 172 }, { 34, 40 }, { 100, 38 }, { 18, 169 }, { 77, 13 }, { 188, 54 },
            { 111, 144 }, { 88, 91 }, { 81, 194 }, { 22, 192 }, { 198, 155 }, { 97, 172 },
            { 68, 141 }, { 8, 182 }, { 69, 149 }, { 137, 198 }, { 167, 56 }, { 70, 61 },
            { 186, 120 }, { 101, 125 }, { 124, 176 }, { 178, 57 }, { 77, 108 }, { 49, 198 },
            { 66, 83 }, { 14, 72 }, { 14, 43 }, { 82, 0 }, { 91, 90 }, { 103, 131 }, { 7, 192 },
            { 53, 62 }, { 20, 125 }, { 144, 39 }, { 116, 187 }, { 144, 129 }, { 51, 136 },
            { 95, 90 }, { 155, 6 }, { 69, 183 }, { 179, 10 }, { 80, 154 }, { 126, 197 },
            { 139, 152 }, { 46, 119 }, { 18, 111 }, { 181, 23 }, { 127, 68 }, { 96, 15 },
            { 44, 50 }, { 9, 76 }, { 134, 55 }, { 63, 4 }, { 147, 80 }, { 156, 72 }, { 24, 141 },
            { 10, 1 }, { 161, 31 }, { 11, 39 }, { 141, 86 }, { 189, 84 }, { 164, 119 }, { 94, 142 },
            { 97, 163 }, { 17, 12 }, { 78, 179 }, { 28, 120 }, { 169, 126 }, { 5, 186 }, { 49, 93 },
            { 65, 38 }, { 185, 100 }, { 145, 52 }, { 194, 101 }, { 52, 120 }, { 167, 133 },
            { 132, 65 }, { 125, 164 }, { 109, 134 }, { 187, 166 }, { 186, 60 }, { 52, 138 },
            { 99, 124 }, { 162, 16 }, { 24, 67 }, { 93, 37 }, { 76, 31 }, { 156, 68 }, { 94, 138 },
            { 154, 88 }, { 155, 22 }, { 192, 58 }, { 106, 2 }, { 155, 11 }, { 27, 55 }, { 9, 163 },
            { 0, 169 }, { 21, 137 }, { 83, 93 }, { 103, 151 }, { 117, 14 }, { 63, 131 }, { 5, 88 },
            { 197, 199 }, { 34, 39 }, { 103, 164 }, { 144, 79 }, { 94, 35 }, { 139, 165 },
            { 181, 140 }, { 197, 18 }, { 22, 87 }, { 125, 64 }, { 19, 160 }, { 71, 53 },
            { 54, 144 }, { 17, 149 }, { 94, 38 }, { 161, 108 }, { 6, 60 }, { 1, 40 }, { 12, 134 },
            { 36, 28 }, { 0, 34 }, { 45, 162 }, { 176, 96 }, { 184, 166 }, { 108, 3 }, { 99, 86 },
            { 39, 105 }, { 190, 20 }, { 189, 80 }, { 172, 24 }, { 103, 76 }, { 126, 157 },
            { 21, 182 }, { 100, 40 }, { 154, 84 }, { 103, 44 }, { 94, 196 }, { 162, 42 },
            { 137, 141 }, { 18, 109 }, { 152, 117 }, { 147, 14 }, { 156, 35 }, { 5, 181 },
            { 42, 13 }, { 47, 6 }, { 40, 52 }, { 166, 199 }, { 83, 189 }, { 121, 139 }, { 75, 97 },
            { 36, 141 }, { 119, 17 }, { 11, 156 }, { 198, 108 }, { 104, 59 }, { 194, 186 },
            { 54, 57 }, { 143, 171 }, { 158, 19 }, { 8, 72 }, { 179, 117 }, { 143, 151 },
            { 35, 111 }, { 80, 91 }, { 92, 186 }, { 197, 109 }, { 174, 45 }, { 34, 124 },
            { 60, 51 }, { 3, 96 }, { 187, 41 }, { 106, 57 }, { 50, 105 }, { 79, 119 }, { 8, 181 },
            { 177, 24 }, { 69, 139 }, { 155, 84 }, { 66, 16 }, { 177, 5 }, { 6, 89 }, { 86, 90 },
            { 141, 52 }, { 28, 163 }, { 103, 61 }, { 134, 94 }, { 27, 116 }, { 133, 160 },
            { 78, 172 }, { 45, 183 }, { 148, 79 }, { 108, 124 }, { 135, 94 }, { 181, 123 },
            { 24, 161 }, { 170, 199 }, { 95, 46 }, { 19, 18 }, { 188, 20 }, { 59, 39 }, { 167, 28 },
            { 86, 128 }, { 44, 130 }, { 80, 7 }, { 113, 26 }, { 119, 116 }, { 57, 153 }, { 30, 80 },
            { 36, 131 }, { 62, 80 }, { 109, 147 }, { 179, 123 }, { 135, 163 }, { 147, 68 },
            { 80, 124 }, { 49, 167 }, { 18, 4 }, { 132, 143 }, { 93, 139 }, { 113, 170 },
            { 46, 76 }, { 71, 64 }, { 118, 112 }, { 127, 178 }, { 194, 69 }, { 50, 126 },
            { 1, 107 }, { 167, 5 }, { 132, 89 }, { 102, 36 }, { 151, 46 }, { 100, 184 },
            { 181, 39 }, { 37, 6 }, { 66, 138 }, { 198, 8 }, { 168, 178 }, { 130, 176 },
            { 150, 23 }, { 164, 157 }, { 182, 170 }, { 27, 147 }, { 177, 118 }, { 10, 153 },
            { 141, 101 }, { 76, 26 }, { 117, 84 }, { 64, 108 }, { 180, 6 }, { 102, 192 },
            { 138, 164 }, { 177, 157 }, { 46, 114 }, { 153, 86 }, { 113, 75 }, { 131, 174 },
            { 184, 112 }, { 29, 95 }, { 48, 1 }, { 25, 150 }, { 132, 13 }, { 2, 188 }, { 97, 162 },
            { 68, 173 }, { 118, 117 }, { 98, 163 }, { 159, 66 }, { 131, 159 }, { 82, 133 },
            { 100, 110 }, { 128, 50 }, { 72, 141 }, { 9, 55 }, { 195, 44 }, { 38, 50 },
            { 163, 196 }, { 46, 74 }, { 75, 139 }, { 122, 183 }, { 5, 27 }, { 111, 166 },
            { 112, 61 }, { 129, 130 }, { 6, 85 }, { 91, 191 }, { 197, 69 }, { 31, 41 }, { 75, 154 },
            { 135, 111 }, { 56, 36 }, { 37, 148 }, { 139, 130 }, { 158, 24 }, { 80, 196 },
            { 14, 114 }, { 189, 23 }, { 78, 74 }, { 153, 76 }, { 46, 185 }, { 168, 16 }, { 40, 35 },
            { 183, 118 }, { 139, 33 }, { 95, 55 }, { 132, 150 } };
        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);
        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(graph);
        Matching<Integer, DefaultEdge> m = matcher.getMatching();
        verifyMatching(graph, m, 100);
        assertTrue(m.isPerfect());
        for (Integer v : graph.vertexSet())
            assertTrue(m.isMatched(v));
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(
                    graph, matcher.getMatching().getEdges(), matcher.getOddSetCover()));
    }

    @Test
    public void testGraph3()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        int[][] edges = { { 4, 141 }, { 63, 132 }, { 129, 144 }, { 6, 88 }, { 62, 79 }, { 4, 79 },
            { 125, 88 }, { 26, 133 }, { 21, 152 }, { 98, 80 }, { 107, 55 }, { 8, 33 }, { 153, 74 },
            { 179, 6 }, { 79, 42 }, { 148, 146 }, { 27, 197 }, { 43, 22 }, { 154, 21 }, { 184, 26 },
            { 197, 199 }, { 144, 102 }, { 136, 155 }, { 131, 163 }, { 118, 117 }, { 74, 34 },
            { 168, 166 }, { 119, 72 }, { 148, 7 }, { 84, 46 }, { 34, 156 }, { 133, 97 },
            { 42, 193 }, { 66, 122 }, { 81, 108 }, { 36, 132 }, { 3, 134 }, { 153, 44 },
            { 98, 111 }, { 75, 122 }, { 116, 189 }, { 50, 36 }, { 43, 33 }, { 26, 73 }, { 13, 102 },
            { 15, 121 }, { 188, 166 }, { 93, 102 }, { 8, 99 }, { 60, 78 }, { 32, 143 },
            { 152, 168 }, { 72, 65 }, { 38, 153 }, { 117, 125 }, { 139, 186 }, { 195, 38 },
            { 71, 40 }, { 15, 178 }, { 118, 183 }, { 112, 10 }, { 15, 148 }, { 152, 181 },
            { 6, 190 }, { 177, 48 }, { 52, 47 }, { 11, 180 }, { 30, 61 }, { 186, 187 },
            { 131, 167 }, { 84, 40 }, { 198, 126 }, { 135, 139 }, { 84, 3 }, { 161, 86 },
            { 39, 63 }, { 186, 144 }, { 137, 154 }, { 195, 91 }, { 165, 187 }, { 170, 155 },
            { 79, 121 }, { 85, 5 }, { 179, 124 }, { 100, 49 }, { 58, 51 }, { 59, 62 }, { 58, 91 },
            { 85, 17 }, { 85, 0 }, { 68, 154 }, { 185, 171 }, { 13, 11 }, { 192, 32 }, { 169, 157 },
            { 133, 19 }, { 93, 112 }, { 23, 71 }, { 59, 79 }, { 171, 170 }, { 41, 182 }, { 97, 24 },
            { 71, 162 }, { 105, 3 }, { 183, 91 }, { 78, 172 }, { 165, 96 }, { 120, 184 },
            { 182, 159 }, { 184, 34 }, { 85, 143 }, { 156, 129 }, { 151, 36 }, { 114, 94 },
            { 16, 14 }, { 33, 12 }, { 47, 23 }, { 107, 180 }, { 108, 119 }, { 64, 27 }, { 186, 30 },
            { 196, 51 }, { 104, 117 }, { 15, 99 }, { 73, 17 }, { 53, 132 }, { 35, 37 }, { 76, 169 },
            { 165, 186 }, { 35, 129 }, { 97, 54 }, { 83, 77 }, { 65, 71 }, { 85, 192 }, { 77, 58 },
            { 42, 176 }, { 195, 149 }, { 58, 144 }, { 160, 117 }, { 164, 135 }, { 170, 196 },
            { 108, 17 }, { 144, 26 }, { 186, 15 }, { 161, 127 }, { 167, 173 }, { 145, 75 },
            { 171, 57 }, { 50, 146 }, { 74, 131 }, { 7, 191 }, { 101, 149 }, { 60, 140 },
            { 116, 120 }, { 193, 115 }, { 89, 128 }, { 109, 37 }, { 64, 37 }, { 127, 60 },
            { 154, 104 }, { 192, 118 }, { 57, 174 }, { 69, 153 }, { 78, 76 }, { 120, 181 },
            { 142, 47 }, { 69, 123 }, { 171, 110 }, { 26, 32 }, { 38, 39 }, { 72, 93 }, { 61, 102 },
            { 174, 110 }, { 24, 78 }, { 63, 12 }, { 13, 64 }, { 40, 115 }, { 135, 106 }, { 46, 11 },
            { 157, 177 }, { 188, 112 }, { 9, 87 }, { 138, 4 }, { 189, 128 }, { 153, 54 },
            { 61, 145 }, { 170, 38 }, { 7, 126 }, { 46, 19 }, { 87, 79 }, { 88, 140 }, { 191, 190 },
            { 55, 127 }, { 68, 183 }, { 64, 49 }, { 180, 164 }, { 64, 139 }, { 91, 124 },
            { 118, 53 }, { 148, 16 }, { 23, 73 }, { 100, 114 }, { 59, 183 }, { 35, 42 }, { 45, 17 },
            { 84, 86 }, { 65, 194 }, { 92, 109 }, { 181, 119 }, { 183, 128 }, { 130, 162 },
            { 165, 197 }, { 156, 127 }, { 76, 90 }, { 180, 198 }, { 127, 122 }, { 103, 100 },
            { 188, 39 }, { 55, 93 }, { 188, 69 }, { 191, 90 }, { 83, 183 }, { 20, 90 }, { 95, 144 },
            { 15, 145 }, { 175, 74 }, { 23, 128 }, { 60, 178 }, { 145, 3 }, { 174, 35 },
            { 155, 164 }, { 172, 129 }, { 193, 158 }, { 72, 157 }, { 22, 180 }, { 31, 43 },
            { 24, 6 }, { 175, 10 }, { 124, 164 }, { 169, 7 }, { 2, 114 }, { 117, 126 }, { 179, 80 },
            { 149, 63 }, { 183, 13 }, { 66, 153 }, { 35, 160 }, { 130, 29 }, { 15, 2 }, { 124, 58 },
            { 38, 27 }, { 146, 168 }, { 150, 7 }, { 76, 83 }, { 32, 45 }, { 182, 14 }, { 1, 84 },
            { 63, 169 }, { 23, 114 }, { 162, 9 }, { 31, 83 }, { 146, 19 }, { 67, 186 },
            { 103, 101 }, { 10, 103 }, { 189, 136 }, { 79, 77 }, { 147, 181 }, { 59, 127 },
            { 161, 11 }, { 173, 38 }, { 10, 58 }, { 8, 89 }, { 185, 152 }, { 22, 74 }, { 56, 118 },
            { 120, 89 }, { 84, 6 }, { 175, 71 }, { 76, 115 }, { 101, 73 }, { 88, 92 }, { 149, 143 },
            { 119, 86 }, { 17, 160 }, { 176, 165 }, { 49, 52 }, { 74, 71 }, { 113, 166 },
            { 71, 94 }, { 92, 27 }, { 3, 160 }, { 173, 179 }, { 187, 5 }, { 172, 115 }, { 16, 4 },
            { 37, 85 }, { 26, 113 }, { 12, 37 }, { 1, 103 }, { 133, 80 }, { 183, 22 }, { 136, 91 },
            { 50, 65 }, { 193, 53 }, { 101, 112 }, { 141, 10 }, { 46, 61 }, { 73, 142 },
            { 186, 60 }, { 109, 66 }, { 29, 91 }, { 94, 21 }, { 54, 124 }, { 153, 106 },
            { 110, 68 }, { 58, 82 }, { 169, 193 }, { 28, 14 }, { 165, 132 }, { 108, 140 },
            { 103, 128 }, { 46, 51 }, { 22, 111 }, { 49, 164 }, { 7, 32 }, { 126, 191 },
            { 63, 190 }, { 171, 7 }, { 79, 80 }, { 71, 147 }, { 161, 104 }, { 166, 2 },
            { 185, 179 }, { 83, 146 }, { 87, 180 }, { 141, 101 }, { 137, 125 }, { 66, 89 },
            { 14, 107 }, { 9, 35 }, { 13, 164 }, { 140, 15 }, { 179, 120 }, { 138, 70 }, { 19, 25 },
            { 130, 116 }, { 175, 161 }, { 99, 12 }, { 117, 71 }, { 121, 11 }, { 22, 149 },
            { 57, 46 }, { 8, 184 }, { 46, 153 }, { 178, 85 }, { 52, 166 }, { 103, 197 },
            { 114, 181 }, { 28, 29 }, { 101, 110 }, { 188, 92 }, { 103, 88 }, { 132, 73 },
            { 150, 77 }, { 96, 169 }, { 120, 164 }, { 131, 90 }, { 108, 50 }, { 182, 127 },
            { 100, 63 }, { 128, 25 }, { 184, 9 }, { 19, 86 }, { 132, 87 }, { 143, 184 },
            { 105, 91 }, { 16, 68 }, { 16, 84 }, { 163, 86 }, { 66, 87 }, { 14, 62 }, { 78, 2 },
            { 148, 89 }, { 2, 22 }, { 176, 198 }, { 178, 30 }, { 1, 50 }, { 47, 104 }, { 100, 11 },
            { 144, 38 }, { 33, 137 }, { 74, 102 }, { 179, 44 }, { 40, 10 }, { 117, 16 }, { 91, 57 },
            { 110, 25 }, { 141, 92 }, { 167, 188 }, { 26, 120 }, { 116, 107 }, { 60, 94 },
            { 62, 151 }, { 118, 177 }, { 77, 105 }, { 194, 124 }, { 43, 13 }, { 174, 125 },
            { 180, 163 }, { 56, 34 }, { 9, 91 }, { 58, 38 }, { 116, 108 }, { 58, 176 },
            { 190, 154 }, { 124, 26 }, { 170, 56 }, { 136, 35 }, { 45, 35 }, { 100, 106 },
            { 81, 52 }, { 57, 81 }, { 15, 30 }, { 165, 182 }, { 95, 114 }, { 107, 140 },
            { 129, 122 }, { 149, 40 }, { 101, 145 }, { 196, 106 }, { 191, 166 }, { 168, 30 },
            { 106, 43 }, { 83, 62 }, { 45, 174 }, { 135, 6 }, { 2, 3 }, { 80, 35 }, { 171, 188 },
            { 116, 25 }, { 192, 182 }, { 87, 15 }, { 27, 25 }, { 116, 129 }, { 173, 84 },
            { 141, 26 }, { 185, 82 }, { 155, 196 }, { 198, 45 }, { 18, 29 }, { 59, 80 },
            { 153, 29 }, { 92, 126 }, { 109, 83 }, { 77, 151 }, { 95, 26 }, { 65, 73 }, { 188, 38 },
            { 69, 2 }, { 44, 163 }, { 109, 45 }, { 107, 65 }, { 1, 160 }, { 34, 24 }, { 71, 198 },
            { 160, 125 }, { 35, 133 }, { 97, 126 }, { 41, 118 }, { 49, 48 }, { 34, 117 },
            { 18, 82 }, { 4, 140 }, { 184, 125 }, { 116, 192 }, { 86, 98 }, { 168, 7 }, { 135, 69 },
            { 131, 113 }, { 57, 162 }, { 115, 88 }, { 163, 65 }, { 26, 63 }, { 27, 54 },
            { 129, 126 }, { 66, 1 }, { 38, 198 }, { 19, 18 }, { 150, 111 }, { 0, 151 }, { 25, 93 },
            { 104, 27 }, { 16, 40 }, { 188, 77 }, { 179, 14 }, { 151, 29 }, { 79, 0 }, { 134, 29 },
            { 28, 22 }, { 23, 97 }, { 181, 160 }, { 37, 141 }, { 129, 26 }, { 185, 130 },
            { 182, 10 }, { 189, 197 }, { 53, 25 }, { 195, 4 }, { 32, 164 }, { 66, 62 }, { 96, 199 },
            { 80, 85 }, { 84, 45 }, { 83, 90 }, { 139, 21 }, { 153, 6 }, { 154, 84 }, { 135, 169 },
            { 89, 132 }, { 110, 121 }, { 176, 22 }, { 90, 120 }, { 8, 153 }, { 69, 9 }, { 28, 182 },
            { 105, 177 }, { 101, 31 }, { 106, 127 }, { 173, 68 }, { 81, 15 }, { 19, 162 },
            { 173, 81 }, { 165, 41 }, { 99, 136 }, { 52, 152 }, { 199, 34 }, { 185, 47 },
            { 91, 83 }, { 61, 64 }, { 164, 134 }, { 158, 90 }, { 116, 17 }, { 126, 132 },
            { 153, 132 }, { 6, 59 }, { 149, 174 }, { 63, 48 }, { 7, 108 }, { 193, 25 },
            { 150, 127 }, { 28, 58 }, { 166, 81 }, { 84, 128 }, { 155, 91 }, { 178, 170 },
            { 154, 134 }, { 109, 44 }, { 199, 140 }, { 15, 1 }, { 185, 178 }, { 11, 148 },
            { 106, 133 }, { 13, 179 }, { 179, 165 }, { 90, 25 }, { 60, 123 }, { 182, 151 },
            { 88, 154 }, { 133, 198 }, { 191, 189 }, { 129, 179 }, { 181, 61 }, { 50, 143 },
            { 103, 117 }, { 3, 114 }, { 142, 180 }, { 33, 20 }, { 45, 134 }, { 191, 159 },
            { 61, 184 }, { 180, 20 }, { 183, 38 }, { 142, 169 }, { 153, 178 }, { 0, 84 },
            { 74, 91 }, { 167, 127 }, { 119, 136 }, { 34, 96 }, { 152, 175 }, { 16, 107 },
            { 133, 119 }, { 123, 86 }, { 89, 166 }, { 162, 121 }, { 41, 72 }, { 60, 128 },
            { 54, 173 }, { 128, 70 }, { 165, 133 }, { 34, 183 }, { 160, 34 }, { 152, 115 },
            { 158, 146 }, { 74, 18 }, { 109, 104 }, { 72, 48 }, { 88, 126 }, { 125, 143 },
            { 35, 17 }, { 1, 11 }, { 147, 177 }, { 59, 140 }, { 56, 177 }, { 41, 198 }, { 150, 83 },
            { 159, 190 }, { 199, 89 }, { 198, 138 }, { 67, 18 }, { 16, 94 }, { 60, 158 },
            { 188, 91 }, { 191, 11 }, { 42, 91 }, { 191, 72 }, { 140, 45 }, { 122, 159 },
            { 65, 62 }, { 95, 129 }, { 152, 108 }, { 144, 147 }, { 10, 191 }, { 135, 109 },
            { 0, 36 }, { 77, 27 }, { 35, 71 }, { 54, 26 }, { 131, 93 }, { 136, 152 }, { 191, 164 },
            { 81, 176 }, { 19, 31 }, { 104, 17 }, { 32, 81 }, { 132, 75 }, { 133, 29 },
            { 114, 157 }, { 35, 32 }, { 194, 85 }, { 70, 36 }, { 40, 117 }, { 136, 70 }, { 102, 2 },
            { 34, 132 }, { 101, 146 }, { 182, 94 }, { 80, 65 }, { 121, 112 }, { 97, 47 },
            { 21, 183 }, { 40, 171 }, { 14, 168 }, { 167, 0 }, { 153, 157 }, { 115, 133 },
            { 47, 125 }, { 174, 39 }, { 79, 31 }, { 114, 102 }, { 162, 147 }, { 184, 25 },
            { 8, 53 }, { 94, 126 }, { 136, 143 }, { 167, 58 }, { 180, 81 }, { 149, 49 }, { 43, 80 },
            { 169, 155 }, { 72, 192 }, { 147, 108 }, { 87, 39 }, { 13, 101 }, { 48, 64 },
            { 177, 188 }, { 148, 96 }, { 163, 117 }, { 172, 41 }, { 106, 59 }, { 113, 193 },
            { 152, 16 }, { 95, 54 }, { 24, 156 }, { 154, 176 }, { 31, 117 }, { 114, 19 },
            { 131, 156 }, { 187, 143 }, { 128, 144 }, { 45, 22 }, { 137, 25 }, { 123, 113 },
            { 84, 50 }, { 199, 111 }, { 142, 12 }, { 138, 67 }, { 14, 148 }, { 136, 41 },
            { 150, 56 }, { 82, 142 }, { 3, 35 }, { 61, 73 }, { 141, 90 }, { 192, 129 }, { 18, 138 },
            { 68, 4 }, { 78, 1 }, { 28, 136 }, { 99, 122 }, { 16, 28 }, { 4, 114 }, { 158, 114 },
            { 58, 65 }, { 85, 124 }, { 122, 72 }, { 172, 142 }, { 80, 90 }, { 98, 145 },
            { 153, 17 }, { 135, 78 }, { 34, 191 }, { 59, 9 }, { 180, 160 }, { 181, 40 }, { 35, 70 },
            { 96, 147 }, { 0, 162 }, { 199, 71 }, { 160, 23 }, { 59, 7 }, { 45, 199 }, { 156, 186 },
            { 191, 79 }, { 188, 41 }, { 187, 176 }, { 1, 36 }, { 1, 42 }, { 34, 1 }, { 4, 164 },
            { 121, 34 }, { 123, 172 }, { 191, 74 }, { 188, 183 }, { 157, 148 }, { 8, 126 },
            { 38, 7 }, { 178, 118 }, { 114, 154 }, { 25, 18 }, { 141, 128 }, { 108, 135 },
            { 167, 104 }, { 69, 148 }, { 92, 52 }, { 198, 76 }, { 67, 172 }, { 128, 137 },
            { 172, 95 }, { 83, 22 }, { 131, 101 }, { 88, 17 }, { 161, 163 }, { 146, 27 },
            { 121, 107 }, { 66, 197 }, { 56, 120 }, { 82, 26 }, { 109, 75 }, { 137, 195 },
            { 177, 197 }, { 21, 115 }, { 104, 18 }, { 31, 71 }, { 157, 14 }, { 80, 41 },
            { 132, 55 }, { 2, 138 }, { 128, 38 }, { 57, 121 }, { 37, 187 }, { 16, 181 }, { 0, 196 },
            { 79, 18 }, { 186, 1 }, { 170, 195 }, { 115, 47 }, { 46, 173 }, { 83, 80 } };
        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);
        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(graph);
        verifyMatching(graph, matcher.getMatching(), 100);
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(
                    graph, matcher.getMatching().getEdges(), matcher.getOddSetCover()));
    }

    @Test
    public void testGraph4()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        int[][] edges = { { 142, 104 }, { 176, 103 }, { 117, 140 }, { 9, 160 }, { 23, 106 },
            { 120, 11 }, { 55, 110 }, { 9, 176 }, { 171, 183 }, { 27, 42 }, { 101, 122 },
            { 179, 12 }, { 59, 122 }, { 10, 7 }, { 48, 68 }, { 48, 64 }, { 20, 1 }, { 155, 86 },
            { 111, 45 }, { 56, 137 }, { 29, 149 }, { 77, 110 }, { 135, 86 }, { 192, 87 },
            { 198, 199 }, { 96, 143 }, { 28, 72 }, { 94, 163 }, { 65, 196 }, { 159, 20 },
            { 151, 90 }, { 137, 146 }, { 74, 18 }, { 55, 146 }, { 95, 74 }, { 195, 95 },
            { 112, 80 }, { 47, 95 }, { 2, 10 }, { 168, 188 }, { 179, 137 }, { 48, 147 },
            { 179, 68 }, { 39, 81 }, { 102, 9 }, { 12, 89 }, { 50, 102 }, { 133, 27 }, { 12, 150 },
            { 193, 31 }, { 66, 159 }, { 78, 118 }, { 52, 15 }, { 149, 153 }, { 139, 175 },
            { 126, 59 }, { 54, 176 }, { 32, 65 }, { 118, 34 }, { 129, 18 }, { 61, 188 },
            { 87, 122 }, { 47, 21 }, { 185, 136 }, { 12, 1 }, { 141, 159 }, { 114, 119 },
            { 150, 58 }, { 75, 79 }, { 25, 121 }, { 40, 105 }, { 108, 0 }, { 130, 89 },
            { 188, 174 }, { 64, 198 }, { 50, 3 }, { 42, 105 }, { 2, 194 }, { 105, 187 },
            { 119, 118 }, { 185, 191 }, { 38, 17 }, { 196, 175 }, { 77, 87 }, { 43, 107 },
            { 56, 122 }, { 108, 52 }, { 80, 7 }, { 27, 70 }, { 72, 45 }, { 30, 36 }, { 29, 70 },
            { 186, 109 }, { 89, 45 }, { 19, 12 }, { 181, 39 }, { 92, 141 }, { 41, 7 }, { 91, 75 },
            { 193, 106 }, { 184, 23 }, { 69, 185 }, { 90, 11 }, { 149, 12 }, { 166, 165 },
            { 101, 199 }, { 167, 152 }, { 0, 3 }, { 121, 168 }, { 107, 131 }, { 190, 1 },
            { 195, 182 }, { 129, 54 }, { 149, 31 }, { 141, 173 }, { 61, 80 }, { 5, 153 },
            { 88, 60 }, { 143, 187 }, { 86, 97 }, { 22, 163 }, { 143, 108 }, { 50, 45 }, { 9, 87 },
            { 6, 103 }, { 75, 125 }, { 166, 111 }, { 9, 159 }, { 27, 57 }, { 101, 175 },
            { 37, 125 }, { 22, 113 }, { 68, 71 }, { 48, 113 }, { 122, 168 }, { 136, 135 },
            { 136, 18 }, { 89, 31 }, { 164, 193 }, { 64, 53 }, { 124, 117 }, { 16, 22 },
            { 154, 140 }, { 179, 122 }, { 107, 108 }, { 70, 166 }, { 189, 118 }, { 64, 54 },
            { 197, 62 }, { 139, 127 }, { 55, 169 }, { 106, 20 }, { 135, 172 }, { 24, 192 },
            { 97, 66 }, { 54, 199 }, { 78, 186 }, { 52, 198 }, { 20, 45 }, { 45, 117 },
            { 158, 177 }, { 162, 21 }, { 158, 35 }, { 165, 51 }, { 17, 41 }, { 167, 118 },
            { 80, 116 }, { 101, 62 }, { 2, 23 }, { 17, 81 }, { 41, 192 }, { 10, 93 }, { 42, 95 },
            { 129, 179 }, { 156, 13 }, { 15, 172 }, { 174, 164 }, { 21, 117 }, { 192, 58 },
            { 187, 84 }, { 117, 103 }, { 183, 42 }, { 62, 192 }, { 19, 70 }, { 32, 173 },
            { 77, 114 }, { 166, 77 }, { 5, 41 }, { 189, 2 }, { 39, 74 }, { 183, 0 }, { 144, 182 },
            { 153, 30 }, { 198, 101 }, { 11, 137 }, { 132, 49 }, { 191, 15 }, { 97, 100 },
            { 184, 48 }, { 164, 54 }, { 24, 145 }, { 174, 70 }, { 174, 83 }, { 36, 145 },
            { 3, 128 }, { 104, 17 }, { 143, 29 }, { 147, 149 }, { 133, 75 }, { 153, 110 },
            { 48, 192 }, { 112, 1 }, { 88, 91 }, { 14, 104 }, { 140, 28 }, { 159, 180 },
            { 133, 113 }, { 136, 21 }, { 197, 125 }, { 27, 105 }, { 195, 18 }, { 87, 179 },
            { 60, 168 }, { 107, 35 }, { 184, 62 }, { 143, 36 }, { 54, 173 }, { 198, 18 },
            { 44, 101 }, { 12, 50 }, { 7, 54 }, { 137, 12 }, { 99, 104 }, { 191, 27 }, { 95, 78 },
            { 93, 133 }, { 153, 77 }, { 8, 21 }, { 66, 187 }, { 115, 110 }, { 85, 123 },
            { 75, 146 }, { 145, 197 }, { 18, 185 }, { 192, 153 }, { 30, 189 }, { 27, 124 },
            { 188, 122 }, { 85, 19 }, { 190, 67 }, { 97, 36 }, { 183, 111 }, { 184, 133 },
            { 63, 43 }, { 139, 100 }, { 192, 193 }, { 193, 21 }, { 171, 78 }, { 21, 194 },
            { 167, 105 }, { 96, 108 }, { 63, 118 }, { 86, 48 }, { 191, 171 }, { 64, 189 },
            { 3, 98 }, { 149, 162 }, { 108, 165 }, { 53, 37 }, { 128, 96 }, { 156, 69 },
            { 140, 88 }, { 48, 137 }, { 145, 2 }, { 199, 17 }, { 17, 150 }, { 31, 130 },
            { 172, 73 }, { 51, 184 }, { 67, 122 }, { 183, 107 }, { 104, 140 }, { 113, 156 },
            { 192, 50 }, { 36, 81 }, { 23, 66 }, { 122, 156 }, { 62, 48 }, { 29, 2 }, { 195, 179 },
            { 74, 47 }, { 45, 44 }, { 42, 158 }, { 49, 58 }, { 86, 62 }, { 134, 171 }, { 127, 9 },
            { 67, 5 }, { 104, 54 }, { 88, 43 }, { 104, 198 }, { 111, 59 }, { 88, 147 },
            { 152, 108 }, { 157, 4 }, { 115, 12 }, { 170, 166 }, { 54, 119 }, { 85, 61 },
            { 179, 189 }, { 196, 160 }, { 36, 18 }, { 4, 138 }, { 150, 33 }, { 62, 92 }, { 7, 146 },
            { 158, 135 }, { 86, 56 }, { 154, 24 }, { 118, 32 }, { 51, 101 }, { 62, 91 }, { 91, 52 },
            { 16, 188 }, { 35, 34 }, { 132, 77 }, { 175, 72 }, { 160, 156 }, { 185, 170 },
            { 54, 195 }, { 47, 66 }, { 26, 5 }, { 154, 177 }, { 38, 84 }, { 100, 189 }, { 156, 64 },
            { 125, 190 }, { 40, 138 }, { 57, 131 }, { 40, 134 }, { 105, 90 }, { 128, 31 },
            { 197, 172 }, { 38, 92 }, { 19, 134 }, { 95, 88 }, { 191, 4 }, { 140, 184 },
            { 24, 168 }, { 53, 93 }, { 106, 168 }, { 140, 102 }, { 5, 78 }, { 168, 193 },
            { 129, 42 }, { 11, 144 }, { 165, 175 }, { 9, 23 }, { 91, 151 }, { 182, 34 },
            { 173, 148 }, { 75, 174 }, { 9, 133 }, { 179, 47 }, { 37, 197 }, { 160, 100 },
            { 139, 46 }, { 167, 39 }, { 113, 27 }, { 133, 24 }, { 112, 27 }, { 14, 8 }, { 111, 36 },
            { 138, 151 }, { 126, 9 }, { 44, 115 }, { 125, 52 }, { 142, 50 }, { 35, 177 },
            { 139, 44 }, { 120, 181 }, { 112, 12 }, { 59, 158 }, { 0, 157 }, { 177, 184 },
            { 199, 176 }, { 187, 169 }, { 184, 162 }, { 158, 55 }, { 95, 96 }, { 187, 146 },
            { 79, 74 }, { 106, 87 }, { 131, 157 }, { 21, 150 }, { 43, 93 }, { 20, 69 }, { 13, 31 },
            { 109, 133 }, { 77, 180 }, { 70, 130 }, { 171, 73 }, { 137, 121 }, { 24, 187 },
            { 146, 42 }, { 116, 105 }, { 192, 164 }, { 54, 194 }, { 190, 7 }, { 57, 21 },
            { 60, 21 }, { 176, 111 }, { 135, 66 }, { 54, 62 }, { 33, 19 }, { 76, 188 }, { 30, 11 },
            { 88, 176 }, { 197, 127 }, { 110, 31 }, { 184, 115 }, { 62, 136 }, { 176, 134 },
            { 17, 20 }, { 63, 33 }, { 177, 164 }, { 51, 53 }, { 53, 157 }, { 92, 9 }, { 157, 78 },
            { 43, 51 }, { 56, 138 }, { 150, 6 }, { 16, 185 }, { 12, 97 }, { 74, 129 }, { 152, 65 },
            { 159, 188 }, { 20, 126 }, { 2, 126 }, { 55, 103 }, { 14, 18 }, { 142, 155 },
            { 56, 62 }, { 120, 123 }, { 69, 40 }, { 6, 9 }, { 154, 39 }, { 160, 15 }, { 1, 146 },
            { 182, 157 }, { 100, 133 }, { 71, 186 }, { 10, 179 }, { 130, 171 }, { 91, 141 },
            { 199, 130 }, { 2, 63 }, { 144, 118 }, { 198, 20 }, { 185, 176 }, { 180, 96 },
            { 129, 78 }, { 5, 91 }, { 4, 184 }, { 112, 70 }, { 127, 7 }, { 148, 150 }, { 16, 21 },
            { 83, 13 }, { 151, 16 }, { 46, 31 }, { 52, 57 }, { 73, 10 }, { 78, 105 }, { 131, 143 },
            { 173, 18 }, { 21, 38 }, { 38, 3 }, { 164, 86 }, { 149, 177 }, { 199, 84 }, { 4, 173 },
            { 109, 80 }, { 96, 127 }, { 160, 72 }, { 54, 179 }, { 44, 47 }, { 33, 126 },
            { 53, 184 }, { 155, 36 }, { 129, 21 }, { 43, 118 }, { 16, 54 }, { 67, 43 }, { 144, 62 },
            { 108, 103 }, { 178, 174 }, { 184, 81 }, { 139, 21 } };
        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);
        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(graph);
        verifyMatching(graph, matcher.getMatching(), 99);
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(
                    graph, matcher.getMatching().getEdges(), matcher.getOddSetCover()));
    }

    @Test
    public void testGraph5()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        int[][] edges = { { 55, 4 }, { 9, 118 }, { 70, 115 }, { 179, 146 }, { 122, 136 },
            { 192, 91 }, { 100, 158 }, { 5, 22 }, { 72, 118 }, { 88, 10 }, { 192, 10 }, { 73, 133 },
            { 144, 187 }, { 189, 153 }, { 69, 154 }, { 89, 2 }, { 63, 144 }, { 187, 126 },
            { 38, 115 }, { 19, 10 }, { 128, 77 }, { 49, 45 }, { 176, 50 }, { 185, 60 }, { 34, 22 },
            { 105, 82 }, { 179, 8 }, { 107, 120 }, { 102, 103 }, { 157, 80 }, { 49, 0 },
            { 174, 130 }, { 158, 33 }, { 195, 98 }, { 109, 93 }, { 64, 31 }, { 39, 132 },
            { 26, 88 }, { 77, 78 }, { 8, 164 }, { 143, 141 }, { 162, 110 }, { 128, 188 },
            { 194, 148 }, { 183, 39 }, { 0, 19 }, { 185, 128 }, { 129, 144 }, { 73, 51 },
            { 151, 5 }, { 121, 175 }, { 75, 182 }, { 130, 178 }, { 79, 159 }, { 32, 167 },
            { 128, 92 }, { 193, 103 }, { 1, 84 }, { 68, 177 }, { 115, 179 }, { 134, 183 },
            { 192, 99 }, { 191, 79 }, { 39, 142 }, { 99, 42 }, { 81, 155 }, { 93, 133 },
            { 106, 194 }, { 62, 65 }, { 107, 21 }, { 43, 137 }, { 148, 142 }, { 132, 143 },
            { 160, 119 }, { 17, 44 }, { 153, 90 }, { 7, 51 }, { 129, 141 }, { 40, 88 }, { 26, 193 },
            { 169, 74 }, { 62, 128 }, { 189, 89 }, { 80, 120 }, { 54, 86 }, { 139, 104 },
            { 43, 23 }, { 169, 94 }, { 37, 43 }, { 107, 35 }, { 28, 24 }, { 24, 20 }, { 15, 166 },
            { 145, 110 }, { 1, 191 }, { 73, 132 }, { 6, 30 }, { 153, 144 }, { 76, 34 }, { 137, 84 },
            { 175, 53 }, { 195, 20 }, { 82, 18 }, { 16, 110 }, { 40, 92 }, { 90, 41 }, { 132, 94 },
            { 34, 70 }, { 186, 0 }, { 60, 41 }, { 63, 20 }, { 16, 7 }, { 48, 193 }, { 138, 177 },
            { 164, 122 }, { 79, 11 }, { 3, 135 }, { 43, 52 }, { 160, 43 }, { 145, 15 }, { 93, 180 },
            { 42, 148 }, { 83, 85 }, { 194, 9 }, { 55, 185 }, { 100, 13 }, { 16, 14 }, { 101, 18 },
            { 92, 84 }, { 174, 52 }, { 82, 137 }, { 139, 146 }, { 35, 26 }, { 160, 48 },
            { 107, 102 }, { 178, 172 }, { 165, 145 }, { 71, 128 }, { 122, 60 }, { 36, 196 },
            { 185, 91 }, { 187, 170 }, { 133, 27 }, { 52, 119 }, { 145, 105 }, { 53, 62 },
            { 130, 38 }, { 79, 58 }, { 142, 20 }, { 89, 143 }, { 194, 31 }, { 70, 86 }, { 145, 66 },
            { 9, 51 }, { 65, 109 }, { 41, 77 }, { 48, 169 }, { 159, 162 }, { 156, 16 }, { 4, 84 },
            { 183, 52 }, { 8, 44 }, { 137, 146 }, { 181, 185 }, { 55, 25 }, { 138, 61 },
            { 106, 197 }, { 99, 157 }, { 35, 99 }, { 142, 43 }, { 186, 73 }, { 144, 161 },
            { 77, 52 }, { 182, 155 }, { 85, 132 }, { 184, 146 }, { 53, 96 }, { 103, 73 },
            { 132, 17 }, { 7, 54 }, { 178, 118 }, { 168, 6 }, { 94, 44 }, { 174, 37 }, { 14, 184 },
            { 97, 74 }, { 40, 114 }, { 175, 35 }, { 69, 167 }, { 28, 49 }, { 22, 139 }, { 156, 42 },
            { 46, 41 }, { 63, 135 }, { 55, 58 }, { 187, 122 }, { 72, 77 }, { 120, 191 },
            { 156, 144 }, { 28, 43 }, { 14, 52 }, { 95, 69 }, { 0, 174 }, { 160, 111 }, { 91, 119 },
            { 62, 192 }, { 1, 10 }, { 36, 130 }, { 46, 109 }, { 164, 52 }, { 101, 142 },
            { 180, 67 }, { 119, 147 }, { 189, 130 }, { 134, 102 }, { 168, 106 }, { 191, 99 },
            { 187, 151 }, { 86, 96 }, { 177, 122 }, { 171, 32 }, { 184, 180 }, { 35, 123 },
            { 36, 22 }, { 50, 14 }, { 33, 50 }, { 43, 42 }, { 109, 53 }, { 138, 188 }, { 108, 27 },
            { 104, 160 }, { 101, 31 }, { 190, 131 }, { 50, 62 }, { 190, 196 }, { 45, 15 },
            { 154, 125 }, { 63, 116 }, { 72, 41 }, { 140, 80 }, { 138, 102 }, { 21, 115 },
            { 116, 75 }, { 181, 147 }, { 192, 152 }, { 168, 44 }, { 161, 101 }, { 102, 142 },
            { 63, 173 }, { 147, 142 }, { 63, 10 }, { 163, 139 }, { 34, 67 }, { 123, 184 },
            { 164, 111 }, { 83, 113 }, { 60, 76 }, { 47, 3 }, { 100, 25 }, { 53, 165 }, { 46, 100 },
            { 56, 85 }, { 14, 153 }, { 27, 128 }, { 127, 63 }, { 74, 98 }, { 45, 72 }, { 98, 126 },
            { 114, 166 }, { 193, 186 }, { 60, 197 }, { 24, 83 }, { 179, 176 }, { 29, 128 },
            { 136, 35 }, { 28, 141 }, { 81, 90 }, { 38, 7 }, { 170, 29 }, { 138, 127 }, { 133, 18 },
            { 87, 164 }, { 50, 45 }, { 164, 1 }, { 82, 77 }, { 38, 113 }, { 76, 158 }, { 97, 194 },
            { 10, 118 }, { 42, 157 }, { 142, 190 }, { 1, 144 }, { 94, 16 }, { 44, 78 }, { 8, 168 },
            { 21, 37 }, { 22, 88 }, { 182, 105 }, { 50, 75 }, { 75, 9 }, { 149, 22 }, { 174, 30 },
            { 184, 86 }, { 89, 156 }, { 102, 82 }, { 35, 78 }, { 1, 62 }, { 45, 178 }, { 105, 168 },
            { 62, 14 }, { 59, 67 }, { 91, 70 }, { 174, 190 }, { 10, 124 }, { 17, 33 }, { 181, 146 },
            { 72, 83 }, { 101, 54 }, { 141, 146 }, { 124, 75 }, { 130, 96 }, { 20, 128 },
            { 197, 166 }, { 126, 127 }, { 109, 48 }, { 122, 76 }, { 81, 20 }, { 29, 87 },
            { 64, 136 }, { 113, 105 }, { 67, 56 }, { 86, 7 }, { 158, 81 }, { 102, 166 }, { 93, 37 },
            { 46, 131 }, { 59, 107 }, { 1, 125 }, { 6, 146 }, { 63, 90 }, { 87, 82 }, { 61, 103 },
            { 81, 164 }, { 128, 195 }, { 37, 60 }, { 139, 86 }, { 128, 173 }, { 60, 36 },
            { 38, 72 }, { 61, 116 }, { 116, 1 }, { 188, 137 }, { 149, 179 }, { 0, 183 },
            { 164, 64 }, { 130, 155 }, { 131, 6 }, { 155, 7 }, { 2, 177 }, { 27, 169 }, { 95, 182 },
            { 161, 88 }, { 117, 136 }, { 49, 90 }, { 82, 50 }, { 121, 153 }, { 130, 156 },
            { 158, 133 }, { 199, 160 }, { 9, 20 }, { 26, 7 }, { 113, 99 }, { 38, 136 }, { 44, 81 },
            { 21, 46 }, { 190, 180 }, { 74, 181 }, { 84, 115 }, { 198, 97 }, { 115, 103 },
            { 14, 20 }, { 90, 183 }, { 113, 2 }, { 182, 142 }, { 191, 73 }, { 139, 3 },
            { 148, 138 }, { 160, 29 }, { 68, 7 }, { 43, 73 }, { 41, 0 }, { 36, 178 }, { 136, 134 },
            { 78, 139 }, { 146, 147 }, { 175, 52 }, { 22, 100 }, { 113, 78 }, { 116, 133 },
            { 73, 93 }, { 52, 199 }, { 5, 97 }, { 20, 80 }, { 171, 153 }, { 152, 143 },
            { 100, 165 }, { 36, 122 }, { 47, 29 }, { 165, 182 }, { 98, 4 }, { 62, 178 },
            { 99, 147 }, { 191, 153 }, { 188, 43 }, { 143, 146 }, { 34, 45 }, { 140, 169 },
            { 21, 189 }, { 127, 121 }, { 102, 84 }, { 66, 160 }, { 105, 176 }, { 12, 3 },
            { 197, 64 }, { 12, 129 }, { 158, 178 }, { 163, 141 }, { 54, 106 }, { 103, 157 },
            { 148, 5 } };
        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);
        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(graph);
        verifyMatching(graph, matcher.getMatching(), 98);
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(
                    graph, matcher.getMatching().getEdges(), matcher.getOddSetCover()));
    }

    @Test
    public void testGraph6()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        int[][] edges = { { 54, 119 }, { 97, 64 }, { 94, 171 }, { 128, 13 }, { 123, 174 },
            { 48, 159 }, { 117, 36 }, { 175, 155 }, { 89, 172 }, { 22, 155 }, { 123, 61 },
            { 64, 18 }, { 132, 44 }, { 154, 61 }, { 36, 0 }, { 150, 61 }, { 197, 76 }, { 83, 186 },
            { 180, 91 }, { 4, 121 }, { 92, 123 }, { 195, 109 }, { 58, 76 }, { 172, 56 },
            { 62, 104 }, { 169, 63 }, { 49, 174 }, { 131, 177 }, { 122, 139 }, { 193, 140 },
            { 75, 178 }, { 193, 97 }, { 87, 3 }, { 101, 135 }, { 46, 21 }, { 14, 79 }, { 166, 60 },
            { 67, 151 }, { 151, 190 }, { 126, 110 }, { 148, 103 }, { 51, 118 }, { 153, 36 },
            { 62, 87 }, { 157, 140 }, { 176, 63 }, { 165, 155 }, { 117, 96 }, { 2, 56 }, { 70, 98 },
            { 89, 86 }, { 134, 32 }, { 5, 96 }, { 123, 167 }, { 147, 142 }, { 18, 120 }, { 162, 4 },
            { 31, 94 }, { 189, 145 }, { 8, 27 }, { 198, 165 }, { 173, 109 }, { 152, 131 },
            { 95, 118 }, { 177, 78 }, { 58, 49 }, { 130, 72 }, { 189, 85 }, { 195, 83 },
            { 50, 119 }, { 174, 74 }, { 110, 107 }, { 48, 172 }, { 184, 128 }, { 79, 64 },
            { 177, 56 }, { 192, 46 }, { 145, 46 }, { 95, 191 }, { 45, 103 }, { 117, 158 },
            { 160, 140 }, { 17, 88 }, { 55, 175 }, { 192, 166 }, { 116, 10 }, { 171, 96 },
            { 11, 155 }, { 32, 126 }, { 85, 27 }, { 114, 34 }, { 123, 86 }, { 24, 65 }, { 41, 150 },
            { 184, 129 }, { 92, 104 }, { 110, 117 }, { 145, 184 }, { 44, 31 }, { 184, 94 },
            { 5, 39 }, { 115, 7 }, { 102, 174 }, { 167, 177 }, { 110, 175 }, { 100, 90 },
            { 77, 128 }, { 113, 96 }, { 144, 46 }, { 59, 112 }, { 104, 112 }, { 97, 95 },
            { 117, 3 }, { 61, 120 }, { 38, 164 }, { 130, 15 }, { 40, 12 }, { 133, 20 }, { 49, 109 },
            { 9, 51 }, { 144, 75 }, { 131, 89 }, { 106, 30 }, { 54, 25 }, { 67, 140 }, { 76, 196 },
            { 80, 11 }, { 139, 142 }, { 29, 164 }, { 135, 53 }, { 72, 131 }, { 105, 77 },
            { 144, 179 }, { 36, 191 }, { 43, 127 }, { 143, 152 }, { 51, 82 }, { 4, 197 },
            { 165, 168 }, { 77, 117 }, { 22, 110 }, { 142, 151 }, { 161, 67 }, { 186, 65 },
            { 17, 66 }, { 101, 122 }, { 112, 40 }, { 43, 112 }, { 10, 88 }, { 108, 171 },
            { 129, 30 }, { 117, 179 }, { 13, 97 }, { 84, 44 }, { 168, 65 }, { 128, 175 },
            { 27, 135 }, { 114, 13 }, { 96, 20 }, { 60, 140 }, { 198, 42 }, { 116, 60 },
            { 162, 191 }, { 100, 35 }, { 144, 87 }, { 66, 148 }, { 174, 177 }, { 183, 167 },
            { 185, 138 }, { 183, 194 }, { 95, 166 }, { 92, 20 }, { 88, 93 }, { 110, 34 },
            { 65, 145 }, { 195, 51 }, { 94, 54 }, { 191, 150 }, { 4, 115 }, { 160, 99 },
            { 25, 191 }, { 191, 2 }, { 105, 169 }, { 68, 2 }, { 23, 121 }, { 15, 58 }, { 149, 121 },
            { 128, 83 }, { 21, 75 }, { 136, 127 }, { 108, 193 }, { 79, 67 }, { 146, 108 },
            { 8, 152 }, { 3, 140 }, { 133, 188 }, { 142, 175 }, { 40, 5 }, { 136, 102 }, { 82, 55 },
            { 124, 162 }, { 150, 55 }, { 127, 101 }, { 92, 195 }, { 56, 97 }, { 131, 60 },
            { 84, 78 }, { 90, 147 }, { 34, 11 }, { 1, 154 }, { 179, 17 }, { 76, 112 }, { 117, 64 },
            { 164, 174 }, { 2, 72 }, { 124, 151 }, { 41, 57 }, { 109, 13 }, { 65, 166 },
            { 134, 110 }, { 158, 28 }, { 100, 70 }, { 25, 41 }, { 170, 21 }, { 0, 112 },
            { 117, 73 }, { 175, 112 }, { 47, 182 }, { 169, 44 }, { 86, 82 }, { 183, 110 },
            { 112, 197 }, { 85, 14 }, { 58, 100 }, { 16, 17 }, { 125, 132 }, { 75, 18 }, { 95, 80 },
            { 77, 36 }, { 99, 174 }, { 60, 54 }, { 89, 7 }, { 183, 139 }, { 114, 106 }, { 162, 86 },
            { 190, 6 }, { 81, 165 }, { 63, 106 }, { 125, 103 }, { 194, 59 }, { 100, 17 },
            { 156, 171 }, { 84, 48 }, { 34, 86 }, { 91, 56 }, { 45, 13 }, { 102, 51 }, { 48, 149 },
            { 188, 22 }, { 95, 82 }, { 31, 181 }, { 54, 116 }, { 126, 55 }, { 193, 100 },
            { 145, 120 }, { 11, 114 }, { 34, 178 }, { 133, 47 }, { 157, 17 }, { 71, 67 },
            { 146, 129 }, { 147, 193 }, { 154, 151 }, { 154, 16 }, { 34, 198 }, { 174, 178 },
            { 73, 168 }, { 34, 62 }, { 33, 108 }, { 93, 21 }, { 139, 35 }, { 119, 97 }, { 71, 171 },
            { 111, 33 }, { 13, 43 }, { 23, 74 }, { 99, 133 }, { 14, 24 }, { 3, 33 }, { 0, 122 },
            { 151, 174 }, { 147, 123 }, { 180, 187 }, { 72, 28 }, { 49, 68 }, { 27, 158 },
            { 98, 128 }, { 185, 190 }, { 149, 183 }, { 174, 10 }, { 64, 121 }, { 112, 111 },
            { 53, 66 }, { 108, 149 }, { 44, 145 }, { 155, 58 }, { 131, 104 }, { 24, 83 },
            { 124, 182 }, { 177, 26 }, { 155, 15 }, { 23, 176 }, { 154, 77 }, { 91, 99 },
            { 60, 176 }, { 23, 91 }, { 154, 160 }, { 111, 103 }, { 13, 140 }, { 42, 77 },
            { 105, 35 }, { 9, 198 }, { 105, 24 }, { 146, 135 }, { 117, 67 }, { 145, 140 },
            { 124, 47 }, { 81, 37 }, { 154, 150 }, { 119, 48 }, { 191, 123 }, { 79, 165 },
            { 118, 180 }, { 86, 39 }, { 92, 115 }, { 37, 195 }, { 52, 193 }, { 6, 98 }, { 77, 91 },
            { 131, 151 }, { 76, 54 }, { 147, 143 }, { 95, 198 }, { 89, 134 }, { 104, 90 },
            { 26, 197 }, { 42, 164 }, { 35, 113 }, { 187, 172 }, { 173, 168 } };
        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);
        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(graph);
        verifyMatching(graph, matcher.getMatching(), 96);
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(
                    graph, matcher.getMatching().getEdges(), matcher.getOddSetCover()));
    }

    @Test
    public void testGraph7()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        int[][] edges = { { 101, 127 }, { 65, 51 }, { 15, 137 }, { 166, 180 }, { 123, 77 },
            { 55, 145 }, { 174, 183 }, { 1, 136 }, { 137, 59 }, { 60, 72 }, { 10, 109 }, { 80, 15 },
            { 66, 55 }, { 165, 195 }, { 166, 37 }, { 166, 44 }, { 20, 18 }, { 56, 136 },
            { 172, 189 }, { 181, 1 }, { 88, 109 }, { 191, 25 }, { 114, 25 }, { 11, 37 },
            { 153, 141 }, { 156, 112 }, { 54, 71 }, { 129, 94 }, { 49, 184 }, { 68, 129 },
            { 116, 142 }, { 64, 120 }, { 96, 157 }, { 78, 35 }, { 60, 61 }, { 148, 28 },
            { 191, 167 }, { 123, 175 }, { 54, 90 }, { 187, 50 }, { 158, 34 }, { 85, 119 },
            { 16, 24 }, { 172, 38 }, { 12, 180 }, { 97, 79 }, { 35, 46 }, { 194, 30 }, { 45, 53 },
            { 63, 183 }, { 107, 119 }, { 105, 121 }, { 123, 135 }, { 30, 167 }, { 182, 36 },
            { 109, 161 }, { 103, 6 }, { 178, 57 }, { 114, 163 }, { 183, 162 }, { 70, 24 },
            { 72, 99 }, { 88, 155 }, { 105, 40 }, { 54, 157 }, { 126, 129 }, { 109, 197 },
            { 39, 172 }, { 160, 7 }, { 141, 94 }, { 109, 20 }, { 69, 159 }, { 93, 43 }, { 25, 36 },
            { 144, 189 }, { 61, 141 }, { 163, 22 }, { 101, 102 }, { 87, 176 }, { 16, 115 },
            { 175, 169 }, { 72, 141 }, { 190, 148 }, { 50, 29 }, { 180, 128 }, { 41, 166 },
            { 184, 73 }, { 158, 23 }, { 163, 122 }, { 96, 10 }, { 122, 173 }, { 144, 20 },
            { 11, 199 }, { 93, 136 }, { 147, 180 }, { 189, 197 }, { 177, 54 }, { 178, 40 },
            { 190, 181 }, { 14, 36 }, { 31, 80 }, { 157, 189 }, { 152, 49 }, { 134, 125 },
            { 95, 63 }, { 85, 174 }, { 10, 141 }, { 48, 22 }, { 86, 168 }, { 60, 168 }, { 142, 45 },
            { 155, 38 }, { 196, 9 }, { 100, 84 }, { 135, 98 }, { 176, 49 }, { 153, 154 },
            { 164, 175 }, { 51, 133 }, { 96, 73 }, { 7, 152 }, { 66, 172 }, { 186, 177 },
            { 112, 62 }, { 172, 141 }, { 145, 91 }, { 69, 180 }, { 102, 159 }, { 38, 57 },
            { 138, 30 }, { 169, 133 }, { 150, 76 }, { 27, 102 }, { 196, 199 }, { 24, 56 },
            { 48, 144 }, { 85, 1 }, { 12, 37 }, { 179, 106 }, { 15, 147 }, { 7, 167 }, { 61, 11 },
            { 185, 181 }, { 179, 178 }, { 38, 128 }, { 41, 27 }, { 27, 97 }, { 4, 135 },
            { 111, 15 }, { 71, 117 }, { 43, 13 }, { 181, 68 }, { 168, 121 }, { 182, 12 },
            { 53, 181 }, { 148, 109 }, { 100, 118 }, { 176, 26 }, { 86, 65 }, { 102, 167 },
            { 18, 142 }, { 148, 46 }, { 101, 9 }, { 138, 158 }, { 32, 161 }, { 172, 20 },
            { 139, 31 }, { 145, 32 }, { 59, 108 }, { 131, 52 }, { 6, 184 }, { 123, 157 },
            { 100, 37 }, { 56, 36 }, { 116, 50 }, { 172, 118 }, { 176, 28 }, { 107, 183 },
            { 174, 30 }, { 177, 190 }, { 35, 33 }, { 175, 34 }, { 142, 46 }, { 138, 194 },
            { 71, 160 }, { 96, 65 }, { 66, 32 }, { 175, 176 }, { 36, 88 }, { 4, 54 }, { 9, 120 },
            { 53, 11 }, { 183, 31 }, { 140, 178 }, { 194, 193 }, { 0, 68 }, { 29, 7 }, { 89, 74 },
            { 178, 125 }, { 176, 58 }, { 46, 164 }, { 185, 2 }, { 84, 160 }, { 182, 195 },
            { 76, 171 }, { 41, 173 }, { 24, 168 }, { 117, 120 }, { 171, 156 }, { 106, 154 },
            { 174, 63 }, { 43, 173 }, { 72, 41 }, { 37, 136 }, { 146, 95 }, { 199, 117 },
            { 116, 100 }, { 1, 187 }, { 127, 52 }, { 106, 42 }, { 112, 116 }, { 114, 51 },
            { 126, 117 }, { 8, 122 }, { 96, 160 }, { 1, 156 }, { 78, 19 }, { 14, 178 },
            { 122, 170 }, { 32, 176 }, { 114, 48 }, { 115, 143 }, { 110, 60 }, { 28, 6 }, { 0, 25 },
            { 88, 120 }, { 77, 142 }, { 19, 38 }, { 182, 108 }, { 122, 77 }, { 99, 126 },
            { 157, 170 }, { 117, 138 }, { 45, 90 }, { 54, 141 }, { 13, 79 }, { 32, 110 },
            { 112, 92 }, { 198, 184 }, { 79, 145 }, { 107, 67 }, { 133, 10 }, { 125, 108 },
            { 9, 26 }, { 197, 193 }, { 183, 125 }, { 183, 193 }, { 4, 90 }, { 184, 80 },
            { 171, 55 }, { 110, 74 }, { 9, 55 }, { 10, 132 }, { 77, 15 }, { 67, 197 }, { 195, 116 },
            { 190, 20 }, { 191, 153 }, { 95, 143 }, { 58, 189 }, { 183, 120 }, { 115, 56 },
            { 198, 63 }, { 132, 62 }, { 112, 74 }, { 84, 190 }, { 3, 116 }, { 13, 20 }, { 47, 137 },
            { 19, 33 }, { 130, 137 }, { 16, 58 }, { 9, 130 }, { 17, 106 }, { 116, 30 }, { 177, 94 },
            { 56, 44 }, { 55, 90 }, { 27, 56 }, { 156, 66 }, { 60, 27 }, { 91, 133 }, { 101, 3 },
            { 173, 199 }, { 56, 167 }, { 13, 165 }, { 195, 55 }, { 182, 32 }, { 129, 136 },
            { 78, 170 } };
        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);
        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(graph);
        verifyMatching(graph, matcher.getMatching(), 91);
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(
                    graph, matcher.getMatching().getEdges(), matcher.getOddSetCover()));
    }

    @Test
    public void testGraph8()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        int[][] edges = { { 165, 24 }, { 192, 81 }, { 78, 195 }, { 88, 12 }, { 172, 77 },
            { 58, 166 }, { 197, 94 }, { 187, 43 }, { 191, 11 }, { 130, 44 }, { 150, 116 },
            { 131, 41 }, { 83, 170 }, { 25, 129 }, { 168, 159 }, { 160, 65 }, { 15, 41 },
            { 23, 87 }, { 139, 156 }, { 188, 49 }, { 198, 67 }, { 170, 79 }, { 97, 195 },
            { 46, 10 }, { 82, 84 }, { 47, 175 }, { 8, 141 }, { 68, 180 }, { 34, 147 }, { 63, 54 },
            { 45, 182 }, { 167, 29 }, { 188, 112 }, { 43, 124 }, { 26, 50 }, { 130, 48 },
            { 195, 124 }, { 136, 141 }, { 0, 57 }, { 99, 40 }, { 17, 101 }, { 84, 188 },
            { 125, 92 }, { 152, 4 }, { 29, 9 }, { 166, 10 }, { 111, 47 }, { 59, 162 }, { 111, 119 },
            { 193, 46 }, { 191, 23 }, { 6, 62 }, { 46, 3 }, { 193, 115 }, { 175, 195 },
            { 159, 145 }, { 184, 17 }, { 68, 23 }, { 83, 13 }, { 173, 188 }, { 2, 55 }, { 49, 56 },
            { 59, 96 }, { 8, 116 }, { 147, 53 }, { 76, 183 }, { 23, 33 }, { 28, 13 }, { 149, 53 },
            { 64, 70 }, { 193, 127 }, { 78, 97 }, { 164, 117 }, { 122, 139 }, { 54, 188 },
            { 13, 176 }, { 76, 73 }, { 21, 69 }, { 29, 83 }, { 114, 79 }, { 134, 27 }, { 104, 3 },
            { 141, 66 }, { 136, 27 }, { 91, 29 }, { 9, 106 }, { 123, 191 }, { 124, 52 }, { 63, 12 },
            { 133, 141 }, { 49, 101 }, { 53, 189 }, { 95, 28 }, { 140, 100 }, { 152, 77 },
            { 188, 135 }, { 123, 160 }, { 89, 79 }, { 182, 151 }, { 189, 83 }, { 148, 168 },
            { 104, 170 }, { 24, 96 }, { 116, 47 }, { 94, 130 }, { 38, 9 }, { 9, 83 }, { 89, 69 },
            { 159, 107 }, { 116, 122 }, { 8, 75 }, { 116, 57 }, { 5, 53 }, { 84, 55 }, { 70, 60 },
            { 168, 145 }, { 156, 41 }, { 154, 75 }, { 77, 191 }, { 11, 77 }, { 117, 108 },
            { 115, 42 }, { 114, 164 }, { 140, 6 }, { 112, 3 }, { 144, 91 }, { 42, 71 }, { 116, 64 },
            { 26, 120 }, { 12, 71 }, { 0, 21 }, { 157, 17 }, { 95, 92 }, { 65, 81 }, { 133, 158 },
            { 165, 137 }, { 177, 157 }, { 175, 37 }, { 134, 138 }, { 107, 106 }, { 198, 143 },
            { 181, 42 }, { 42, 102 }, { 40, 32 }, { 37, 180 }, { 109, 194 }, { 137, 150 },
            { 112, 152 }, { 193, 158 }, { 180, 79 }, { 189, 146 }, { 118, 66 }, { 84, 41 },
            { 134, 69 }, { 196, 147 }, { 106, 39 }, { 29, 172 }, { 22, 141 }, { 123, 196 },
            { 38, 189 }, { 98, 38 }, { 52, 157 }, { 132, 3 }, { 36, 48 }, { 70, 26 }, { 196, 10 },
            { 33, 63 }, { 17, 41 }, { 171, 21 }, { 173, 0 }, { 46, 185 }, { 81, 189 }, { 199, 85 },
            { 90, 93 }, { 72, 51 }, { 197, 193 }, { 171, 4 }, { 110, 7 }, { 150, 167 },
            { 122, 133 }, { 159, 69 }, { 115, 104 }, { 36, 171 }, { 123, 68 }, { 119, 48 },
            { 176, 113 }, { 24, 74 }, { 46, 158 }, { 92, 113 }, { 178, 164 }, { 180, 199 },
            { 138, 122 }, { 104, 178 }, { 18, 40 }, { 66, 160 }, { 153, 138 }, { 0, 94 },
            { 98, 51 }, { 137, 53 }, { 126, 147 }, { 136, 185 }, { 47, 31 }, { 118, 199 },
            { 192, 52 }, { 18, 91 }, { 0, 167 }, { 84, 99 }, { 133, 99 }, { 5, 8 }, { 156, 175 },
            { 55, 141 }, { 115, 191 }, { 120, 107 }, { 109, 113 }, { 170, 157 }, { 173, 40 },
            { 119, 39 }, { 84, 133 }, { 123, 162 }, { 108, 24 }, { 111, 193 }, { 180, 149 },
            { 26, 43 }, { 186, 5 }, { 42, 13 }, { 80, 192 }, { 184, 83 }, { 173, 156 }, { 89, 139 },
            { 51, 173 }, { 89, 47 }, { 16, 33 }, { 195, 85 }, { 150, 70 }, { 67, 76 }, { 38, 91 },
            { 108, 189 }, { 146, 88 }, { 61, 132 }, { 23, 90 }, { 142, 169 }, { 9, 55 },
            { 72, 175 }, { 96, 74 }, { 99, 17 }, { 169, 4 }, { 17, 44 }, { 64, 168 }, { 103, 197 },
            { 176, 56 } };
        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);
        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(graph);
        verifyMatching(graph, matcher.getMatching(), 86);
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(
                    graph, matcher.getMatching().getEdges(), matcher.getOddSetCover()));
    }

    @Test
    public void testGraph9()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        int[][] edges = { { 9, 158 }, { 114, 119 }, { 136, 45 }, { 119, 69 }, { 95, 67 },
            { 93, 158 }, { 136, 137 }, { 62, 67 }, { 155, 70 }, { 136, 190 }, { 165, 104 },
            { 136, 55 }, { 180, 125 }, { 18, 49 }, { 105, 157 }, { 187, 120 }, { 120, 53 },
            { 183, 154 }, { 91, 187 }, { 166, 111 }, { 26, 177 }, { 186, 142 }, { 47, 160 },
            { 124, 197 }, { 30, 91 }, { 196, 116 }, { 74, 76 }, { 142, 7 }, { 43, 23 },
            { 121, 135 }, { 107, 73 }, { 180, 43 }, { 23, 156 }, { 34, 72 }, { 59, 10 },
            { 188, 138 }, { 38, 27 }, { 165, 78 }, { 181, 22 }, { 193, 22 }, { 51, 192 },
            { 142, 111 }, { 150, 155 }, { 165, 123 }, { 13, 94 }, { 178, 110 }, { 189, 109 },
            { 158, 159 }, { 51, 149 }, { 198, 149 }, { 116, 142 }, { 124, 35 }, { 112, 197 },
            { 83, 154 }, { 61, 5 }, { 41, 49 }, { 15, 194 }, { 37, 75 }, { 29, 65 }, { 7, 38 },
            { 55, 79 }, { 151, 195 }, { 83, 5 }, { 157, 143 }, { 39, 77 }, { 40, 165 }, { 49, 28 },
            { 10, 189 }, { 43, 195 }, { 32, 45 }, { 170, 139 }, { 128, 35 }, { 37, 116 },
            { 131, 92 }, { 66, 59 }, { 42, 52 }, { 84, 110 }, { 188, 122 }, { 81, 13 }, { 53, 151 },
            { 16, 191 }, { 35, 115 }, { 79, 94 }, { 130, 69 }, { 187, 88 }, { 7, 189 },
            { 145, 123 }, { 42, 63 }, { 17, 60 }, { 92, 6 }, { 34, 67 }, { 0, 154 }, { 80, 47 },
            { 38, 31 }, { 50, 42 }, { 170, 44 }, { 144, 192 }, { 60, 165 }, { 138, 170 },
            { 80, 133 }, { 92, 57 }, { 61, 148 }, { 22, 33 }, { 11, 105 }, { 87, 92 }, { 37, 108 },
            { 65, 143 }, { 110, 163 }, { 199, 189 }, { 81, 102 }, { 99, 126 }, { 136, 33 },
            { 133, 20 }, { 198, 126 }, { 30, 170 }, { 8, 28 }, { 99, 89 }, { 149, 32 }, { 20, 41 },
            { 183, 110 }, { 188, 88 }, { 42, 28 }, { 155, 58 }, { 193, 187 }, { 14, 181 },
            { 0, 11 }, { 56, 199 }, { 11, 122 }, { 130, 102 }, { 102, 89 }, { 47, 156 }, { 54, 92 },
            { 10, 102 }, { 108, 99 }, { 144, 47 }, { 122, 177 }, { 114, 45 }, { 126, 56 },
            { 83, 8 }, { 100, 191 }, { 72, 18 }, { 127, 146 }, { 77, 168 }, { 56, 148 },
            { 148, 139 }, { 15, 196 }, { 176, 147 }, { 110, 161 }, { 136, 41 }, { 86, 10 },
            { 15, 8 }, { 136, 87 }, { 112, 95 }, { 165, 94 }, { 174, 13 }, { 18, 187 }, { 73, 146 },
            { 75, 111 }, { 86, 109 }, { 161, 51 }, { 142, 103 }, { 110, 121 }, { 46, 155 },
            { 100, 143 }, { 158, 65 }, { 165, 177 }, { 67, 6 }, { 62, 83 }, { 167, 42 },
            { 21, 184 }, { 120, 21 }, { 57, 193 }, { 150, 86 }, { 88, 109 }, { 158, 10 },
            { 107, 129 }, { 180, 126 }, { 86, 37 }, { 117, 89 }, { 116, 171 }, { 122, 64 },
            { 176, 109 }, { 96, 71 }, { 30, 17 }, { 61, 1 }, { 191, 99 }, { 69, 173 }, { 59, 55 },
            { 146, 37 }, { 129, 18 }, { 2, 179 }, { 194, 197 }, { 82, 131 }, { 124, 28 },
            { 81, 103 }, { 114, 193 }, { 191, 139 }, { 49, 191 }, { 92, 38 }, { 101, 70 } };
        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);
        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(graph);
        verifyMatching(graph, matcher.getMatching(), 75);
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(
                    graph, matcher.getMatching().getEdges(), matcher.getOddSetCover()));
    }

    @Test
    public void testGraph10()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        int[][] edges = { { 50, 128 }, { 164, 132 }, { 185, 71 }, { 77, 85 }, { 0, 77 },
            { 114, 172 }, { 114, 131 }, { 167, 34 }, { 143, 58 }, { 16, 0 }, { 86, 34 },
            { 116, 180 }, { 147, 36 }, { 120, 7 }, { 100, 105 }, { 125, 114 }, { 85, 101 },
            { 107, 50 }, { 171, 100 }, { 12, 47 }, { 134, 191 }, { 61, 4 }, { 95, 74 }, { 7, 140 },
            { 73, 173 }, { 36, 106 }, { 20, 109 }, { 69, 18 }, { 76, 62 }, { 184, 154 },
            { 40, 152 }, { 143, 95 }, { 190, 132 }, { 100, 125 }, { 109, 81 }, { 112, 174 },
            { 98, 182 }, { 115, 70 }, { 108, 198 }, { 85, 9 }, { 91, 172 }, { 123, 58 }, { 2, 137 },
            { 94, 160 }, { 173, 145 }, { 93, 103 }, { 78, 54 }, { 114, 49 }, { 154, 135 },
            { 122, 7 }, { 88, 50 }, { 86, 152 }, { 58, 65 }, { 39, 156 }, { 108, 27 }, { 110, 149 },
            { 65, 114 }, { 25, 171 }, { 52, 76 }, { 34, 83 }, { 28, 192 }, { 26, 147 }, { 9, 87 },
            { 34, 4 }, { 179, 13 }, { 74, 164 }, { 187, 2 }, { 186, 104 }, { 113, 98 }, { 37, 171 },
            { 43, 61 }, { 30, 85 }, { 95, 155 }, { 91, 2 }, { 199, 120 }, { 150, 109 }, { 36, 8 },
            { 67, 97 }, { 62, 63 }, { 131, 69 }, { 199, 47 }, { 38, 130 }, { 95, 55 }, { 24, 162 },
            { 34, 181 }, { 42, 46 }, { 54, 176 }, { 41, 19 }, { 161, 196 }, { 44, 19 },
            { 191, 138 }, { 54, 148 }, { 168, 59 }, { 196, 7 }, { 176, 178 }, { 17, 110 },
            { 49, 155 }, { 116, 51 }, { 35, 100 }, { 83, 114 }, { 91, 46 }, { 1, 2 }, { 97, 71 },
            { 171, 109 }, { 59, 152 }, { 8, 177 }, { 111, 94 }, { 102, 26 }, { 174, 144 },
            { 177, 54 }, { 52, 83 }, { 31, 181 }, { 44, 133 }, { 87, 59 }, { 73, 108 }, { 136, 4 },
            { 15, 10 }, { 142, 179 }, { 151, 160 }, { 31, 166 }, { 113, 132 }, { 195, 41 },
            { 156, 96 }, { 98, 165 }, { 17, 56 }, { 135, 165 }, { 54, 160 }, { 18, 165 },
            { 86, 160 }, { 100, 24 }, { 109, 77 }, { 155, 92 }, { 73, 100 }, { 6, 124 }, { 93, 30 },
            { 90, 194 }, { 199, 131 }, { 98, 134 }, { 49, 36 }, { 157, 0 }, { 189, 97 },
            { 121, 30 }, { 121, 100 }, { 110, 194 }, { 178, 24 }, { 110, 84 }, { 92, 65 },
            { 32, 143 }, { 79, 73 }, { 11, 146 } };
        for (int[] edge : edges)
            Graphs.addEdgeWithVertices(graph, edge[0], edge[1]);
        SparseEdmondsMaximumCardinalityMatching<Integer, DefaultEdge> matcher =
            new SparseEdmondsMaximumCardinalityMatching<>(graph);
        verifyMatching(graph, matcher.getMatching(), 66);
        assertTrue(
            SparseEdmondsMaximumCardinalityMatching
                .isOptimalMatching(
                    graph, matcher.getMatching().getEdges(), matcher.getOddSetCover()));
    }

    private <V, E> void verifyMatching(Graph<V, E> g, Matching<V, E> m, int cardinality)
    {
        Set<V> matched = new HashSet<>();
        double weight = 0;
        for (E e : m.getEdges()) {
            V source = g.getEdgeSource(e);
            V target = g.getEdgeTarget(e);
            if (matched.contains(source))
                fail("vertex is incident to multiple matches in the matching");
            matched.add(source);
            if (matched.contains(target))
                fail("vertex is incident to multiple matches in the matching");
            matched.add(target);
            weight += g.getEdgeWeight(e);
        }
        assertEquals(m.getWeight(), weight, 0.0000001);
        assertEquals(cardinality, m.getEdges().size());
        assertEquals(m.getEdges().size() * 2, matched.size()); // Ensure that there are no
                                                               // self-loops
    }

    private static int maxEdges(int n)
    {
        if (n % 2 == 0) {
            return Math.multiplyExact(n / 2, n - 1);
        } else {
            return Math.multiplyExact(n, (n - 1) / 2);
        }
    }

}
