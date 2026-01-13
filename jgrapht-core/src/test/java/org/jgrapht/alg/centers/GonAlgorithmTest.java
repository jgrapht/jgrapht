/*
 * (C) Copyright 2026, by Jose Alejandro Cornejo-Acosta and Contributors.
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
package org.jgrapht.alg.centers;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.TestUtil;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link GonHeuristic}
 *
 * @author Jose Alejandro Cornejo-Acosta
 */
public class GonAlgorithmTest
{
    /**
     * Directed graph
     */
    @Test
    public void testDirectedGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 5);
        GonHeuristic<Integer, DefaultWeightedEdge> gon =
            new GonHeuristic<>(new Random());

        assertThrows(IllegalArgumentException.class, () -> {
            gon.getCenters(graph, 1);
        });

    }

    /**
     * Empty graph
     */
    @Test
    public void testEmptyGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        GonHeuristic<Integer, DefaultWeightedEdge> gon =
            new GonHeuristic<>(new Random());
        assertThrows(IllegalArgumentException.class, () -> {
            gon.getCenters(graph, 1);
        });
    }

    /**
     * Not complete
     */
    @Test
    public void testNoCompleteGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        GonHeuristic<Integer, DefaultWeightedEdge> gon =
            new GonHeuristic<>(new Random());
        assertThrows(IllegalArgumentException.class, () -> {
            gon.getCenters(graph, 1);
        });
    }

    /**
     * There is only one center
     */
    @Test
    public void testGetOneCenter()
    {
        int[][] edges = {{1, 2, 5}};
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        GonHeuristic<Integer, DefaultEdge> gon =
            new GonHeuristic<>(new Random());
        Set<Integer> centers = gon.getCenters(graph, 1);
        assertEquals(1, centers.size());
        assertTrue(centers.contains(1) || centers.contains(2));
        assertFalse(centers.contains(5)); // is the weight, not a vertex
    }

    /**
     * There are only two centers
     */
    @Test
    public void testGetTwoCenters()
    {
        int[][] edges = {{1, 2, 5}};
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        GonHeuristic<Integer, DefaultEdge> gon =
            new GonHeuristic<>(new Random());
        Set<Integer> centers = gon.getCenters(graph, 2);
        assertEquals(2, centers.size());
        assertTrue(centers.contains(1) && centers.contains(2));
        assertFalse(centers.contains(5)); // is the weight, not a vertex
    }

    /**
     * Trying to compute more centers than vertices
     */
    @Test
    public void testGetMoreCenters()
    {
        int[][] edges = {{1, 2, 5}};
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        GonHeuristic<Integer, DefaultEdge> gon =
            new GonHeuristic<>(new Random());
        assertThrows(IllegalArgumentException.class, () -> {
            gon.getCenters(graph, 3);
        });
    }

    /**
     * One center with minimum covering radius 5
     */
    @Test
    public void testGetOneCenterWithMinCovRadius()
    {
        int[][] allDist = 
           {{0, 5, 5},
            {5, 0, 5},
            {5, 5, 0}
        };

        Graph<Integer, DefaultWeightedEdge> graph = createGraphFromMatrixDistances(allDist);

        GonHeuristic<Integer, DefaultWeightedEdge> gon =
            new GonHeuristic<>(new Random());
        Set<Integer> centers = gon.getCenters(graph, 1);
        assertEquals(5, covRadius(graph, centers));
    }

    /**
     * Test with dummy graph of five vertices for two centers
     */
    @Test
    public void testDummyGraph5TwoCenters()
    {
        int[][] allDist = 
           {{0, 8, 10, 11, 15},
            {8, 0, 2, 3, 7},
            {10, 2, 0, 1, 5},
            {11, 3, 1, 0, 4},
            {15, 7, 5, 4, 0}
        };
        Graph<Integer, DefaultWeightedEdge> graph = createGraphFromMatrixDistances(allDist);
        var gon = new GonHeuristic<Integer, DefaultWeightedEdge>(Set.of(0));

        var centers = gon.getCenters(graph, 2);
        assertTrue(centers.contains(4));  // vertex 4 is the farthest from {0}
        assertEquals(7, covRadius(graph, centers));   // covering radius is 7
    }

    /**
     * Test with dummy graph of five vertices for three centers
     */
    @Test
    public void testDummyGraph5ThreeCenters()
    {
        int[][] allDist = 
           {{0, 8, 10, 11, 15},
            {8, 0, 2, 3, 7},
            {10, 2, 0, 1, 5},
            {11, 3, 1, 0, 4},
            {15, 7, 5, 4, 0}
        };
        Graph<Integer, DefaultWeightedEdge> graph = createGraphFromMatrixDistances(allDist);
        var gon = new GonHeuristic<Integer, DefaultWeightedEdge>(Set.of(0, 4));
        var centers = gon.getCenters(graph, 3);
        assertTrue(centers.contains(1));  // vertex 1 is the farthest from {0, 4}
        assertEquals(3, covRadius(graph, centers));   // covering radius is 2
    }

    /**
     * Test with dummy graph of five vertices for four centers
     */
    @Test
    public void testDummyGraph5FourCenters()
    {
        int[][] allDist = 
           {{0, 8, 10, 11, 15},
            {8, 0, 2, 3, 7},
            {10, 2, 0, 1, 5},
            {11, 3, 1, 0, 4},
            {15, 7, 5, 4, 0}
        };
        Graph<Integer, DefaultWeightedEdge> graph = createGraphFromMatrixDistances(allDist);
        var gon = new GonHeuristic<Integer, DefaultWeightedEdge>(Set.of(0, 4, 1));
        var centers = gon.getCenters(graph, 4);
        assertTrue(centers.contains(3));  // vertex 3 is the farthest from {0, 4, 1}
        assertEquals(1, covRadius(graph, centers));   // covering radius is 1
    }

    /**
     * Test with instance kroA200 from TSPLIB, whose optimal solution for different values of k is
     * known and reported in:
     *  * J. Garcia-Diaz, R. Menchaca-Mendez, R. Menchaca-Mendez, S. Pomares Hernández, J. C. Pérez-Sansalvador and N. Lakouari, 
     * "Approximation Algorithms for the Vertex K-Center Problem: Survey and Experimental Evaluation," in IEEE Access, vol. 7, 
     * pp. 109228-109245, 2019, doi: 10.1109/ACCESS.2019.2933875.
     * 
     */
    @Test
    public void testKroA200FiveCenters() throws IOException
    {
        String filePath = getClass().getResource("/kroA200.tsp").getPath();
        int n = 200;
        Graph<Integer, DefaultWeightedEdge> graph = loadFromTSPLIB(filePath, n);
        var gon = new GonHeuristic<Integer, DefaultWeightedEdge>(new Random());
        
        // for k=5
        var centers = gon.getCenters(graph, 5);
        double r = covRadius(graph, centers);
        // the optimal solution is 911.41, so we check that the solution is at most 2 times worse
        assertTrue(r <= 2 * 911.41, "For k=5, covering radius is " + r);

        // for k=10
        centers = gon.getCenters(graph, 10);
        r = covRadius(graph, centers);
        // the optimal solution is 598.81, so we check that the solution is at most 2 times worse
        assertTrue(r <= 2 * 598.81, "For k=10, covering radius is " + r);

        // for k=20
        centers = gon.getCenters(graph, 20);
        r = covRadius(graph, centers);
        // the optimal solution is 389.30, so we check that the solution is at most 2 times worse
        assertTrue(r <= 2 * 389.30, "For k=20, covering radius is " + r);

        // for k=40
        centers = gon.getCenters(graph, 40);
        r = covRadius(graph, centers);
        // the optimal solution is 258.25, so we check that the solution is at most 2 times worse
        assertTrue(r <= 2 * 258.25, "For k=40, covering radius is " + r);
    }

    // utilities
    static Graph<Integer, DefaultWeightedEdge> createGraphFromMatrixDistances(int[][] allDist)
    {
        int n = allDist.length;
        var graph = GraphTypeBuilder
            .<Integer, DefaultWeightedEdge>undirected().allowingMultipleEdges(false)
            .allowingSelfLoops(false).edgeClass(DefaultWeightedEdge.class).weighted(true).buildGraph();

        // add edges
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (i != j) {
                    Graphs.addEdgeWithVertices(graph, i, j, allDist[i][j]);
                }
            }
        }
        return graph;
    }

    static int covRadius(Graph<Integer, DefaultWeightedEdge> graph, Set<Integer> centers){
        int radius = 0;
        for (Integer v : graph.vertexSet()) {
            int minDist = Integer.MAX_VALUE;
            for (Integer c : centers) {
                if (v.equals(c)) {
                    minDist = 0;
                    break;
                }
                int dist = (int) graph.getEdgeWeight(graph.getEdge(v, c));
                if (dist < minDist) {
                    minDist = dist;
                }
            }
            if (minDist > radius) {
                radius = minDist;
            }
        }
        return radius;
    }

    static Graph<Integer, DefaultWeightedEdge> loadFromTSPLIB(String filePath, int n) throws IOException{
        var graph = GraphTypeBuilder
            .<Integer, DefaultWeightedEdge>undirected()
            .allowingMultipleEdges(false)
            .allowingSelfLoops(false)
            .edgeClass(DefaultWeightedEdge.class)
            .weighted(true)
            .buildGraph();

            int[][] coordinates = new int[n][2];
            Scanner scanner = new Scanner(new File(filePath));
            for (int i = 0; i < n; i++) {
                int id, x, y;
                id = scanner.nextInt();
                x = scanner.nextInt();
                y = scanner.nextInt();
                coordinates[i][0] = x;
                coordinates[i][1] = y;
            }

            // create graph
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    int dx = coordinates[i][0] - coordinates[j][0];
                    int dy = coordinates[i][1] - coordinates[j][1];
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    Graphs.addEdgeWithVertices(graph, i, j, dist);
                }
            }
            scanner.close();
            return graph;
    }
}
