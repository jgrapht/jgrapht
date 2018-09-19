/*
 * (C) Copyright 2016-2018, by Assaf Mizrachi and Contributors.
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

package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.function.*;

import static org.junit.Assert.*;

/**
 * Tests for the {@link KShortestPaths} class using {@link PathValidator}.
 * 
 * @author Assaf Mizrachi
 *
 */
public class KSPPathValidatorTest
{

    /**
     * Testing that using path validator that denies all requests finds no paths.
     */
    @Test
    public void testBlockAll()
    {
        int size = 5;
        SimpleGraph<String, DefaultEdge> clique = buildCliqueGraph(size);
        for (int i = 0; i < size; i++) {
            KShortestSimplePaths<String, DefaultEdge> ksp =
                new KShortestSimplePaths<>(clique, Integer.MAX_VALUE, (partialPath, edge) -> false);

            for (int j = 0; j < size; j++) {
                if (j == i) {
                    continue;
                }
                int k = 1;
                List<GraphPath<String, DefaultEdge>> paths =
                    ksp.getPaths(String.valueOf(i), String.valueOf(j), k);
                assertTrue(paths.isEmpty());
            }
        }
    }

    /**
     * Testing that using path validator that accepts all requests finds full paths.
     */
    @Test
    public void testAllowAll()
    {
        int size = 5;
        SimpleGraph<String, DefaultEdge> clique = buildCliqueGraph(size);
        for (int i = 0; i < size; i++) {
            KShortestSimplePaths<String, DefaultEdge> ksp =
                new KShortestSimplePaths<>(clique, Integer.MAX_VALUE, (partialPath, edge) -> true);

            for (int j = 0; j < size; j++) {
                if (j == i) {
                    continue;
                }
                List<GraphPath<String, DefaultEdge>> paths =
                    ksp.getPaths(String.valueOf(i), String.valueOf(j), 30);
                assertNotNull(paths);
                assertEquals(16, paths.size());
            }
        }
    }

    /**
     * Testing a ring with only single path allowed between two vertices.
     */
    @Test
    public void testRing()
    {
        int size = 10;
        SimpleGraph<Integer, DefaultEdge> ring = buildRingGraph(size);
        for (int i = 0; i < size; i++) {
            KShortestSimplePaths<Integer, DefaultEdge> ksp =
                new KShortestSimplePaths<>(ring, Integer.MAX_VALUE, (partialPath, edge) -> {
                    if (partialPath == null) {
                        return true;
                    }
                    return Math.abs(
                        partialPath.getEndVertex() - Graphs
                            .getOppositeVertex(ring, edge, partialPath.getEndVertex())) == 1;
                });

            for (int j = 0; j < size; j++) {
                if (j == i) {
                    continue;
                }
                List<GraphPath<Integer, DefaultEdge>> paths = ksp.getPaths(i, j, 2);
                assertNotNull(paths);
                assertEquals(1, paths.size());
            }
        }
    }

    /**
     * Testing a graph where the validator denies the request to go on an edge which cutting it
     * makes the graph disconnected
     */
    @Test
    public void testDisconnected()
    {
        int cliqueSize = 5;
        // generate graph of two cliques connected by single edge
        SimpleGraph<Integer, DefaultEdge> graph = buildGraphForTestDisconnected(cliqueSize);
        for (int i = 0; i < graph.vertexSet().size(); i++) {
            KShortestSimplePaths<Integer, DefaultEdge> ksp =
                new KShortestSimplePaths<>(graph, Integer.MAX_VALUE, (partialPath, edge) -> {
                    // accept all requests but the one to pass through the edge connecting
                    // the two cliques.
                    DefaultEdge connectingEdge = graph.getEdge(cliqueSize - 1, cliqueSize);
                    return connectingEdge != edge;
                });

            for (int j = 0; j < graph.vertexSet().size(); j++) {
                if (j == i) {
                    continue;
                }
                List<GraphPath<Integer, DefaultEdge>> paths = ksp.getPaths(i, j, 100);
                if ((i < cliqueSize && j < cliqueSize) || (i >= cliqueSize && j >= cliqueSize)) {
                    // within the clique - path should exist
                    assertNotNull(paths);
                    assertTrue(paths.size() > 0);
                } else {
                    // else - should not
                    assertNotNull(paths);
                    assertTrue(paths.isEmpty());
                }

            }
        }
    }

    /**
     * Testing that the provided GraphPath and new edge are generated correctly. On a directed line
     * graph, the path at step i is expected to include all vertices [0..i-1] and edges {(0, 1), (1,
     * 2), ... (i-1, i) and where new edge is (i, i+1). v
     */
    @Test
    public void testGraphPath()
    {
        SimpleDirectedGraph<Integer, DefaultEdge> line = buildLineGraph(10);
        KShortestSimplePaths<Integer, DefaultEdge> ksp =
            new KShortestSimplePaths<>(line, new PathValidator<Integer, DefaultEdge>()
            {

                int index = 0;

                @Override
                public boolean isValidPath(
                    GraphPath<Integer, DefaultEdge> partialPath, DefaultEdge edge)
                {
                    assertNotNull(edge);
                    assertEquals(line.getEdgeSource(edge), index, index + 1);

                    List<Integer> expectedVertices = new ArrayList<>();

                    for (int i = 0; i < index + 1; i++) {
                        expectedVertices.add(i);
                    }

                    List<DefaultEdge> expectedEdges = new ArrayList<>();
                    for (int i = 0; i < index; i++) {
                        expectedEdges.add(line.getEdge(i, i + 1));
                    }

                    assertNotNull(partialPath);
                    assertEquals(index, partialPath.getEdgeList().size());
                    assertEquals(expectedEdges, partialPath.getEdgeList());
                    assertEquals(index, partialPath.getEndVertex().intValue());
                    assertEquals(line, partialPath.getGraph());
                    assertEquals(index, partialPath.getLength());
                    assertEquals(0, partialPath.getStartVertex().intValue());
                    assertEquals(index + 1, partialPath.getVertexList().size());
                    assertEquals(expectedVertices, partialPath.getVertexList());
                    assertEquals((double) index, partialPath.getWeight(), 0);

                    index++;
                    return true;
                }
            });

        ksp.getPaths(0, 9, Integer.MAX_VALUE);
    }

    private SimpleGraph<String, DefaultEdge> buildCliqueGraph(int size)
    {
        SimpleGraph<String, DefaultEdge> clique = new SimpleGraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        CompleteGraphGenerator<String, DefaultEdge> graphGenerator =
            new CompleteGraphGenerator<>(size);
        graphGenerator.generateGraph(clique);
        return clique;
    }

    private SimpleGraph<Integer, DefaultEdge> buildGraphForTestDisconnected(int size)
    {
        Supplier<Integer> vSupplier = SupplierUtil.createIntegerSupplier();
        SimpleGraph<Integer, DefaultEdge> graph =
            new SimpleGraph<>(vSupplier, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        CompleteGraphGenerator<Integer, DefaultEdge> completeGraphGenerator =
            new CompleteGraphGenerator<>(size);
        // two complete graphs
        SimpleGraph<Integer, DefaultEdge> east =
            new SimpleGraph<>(vSupplier, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        completeGraphGenerator.generateGraph(east);

        SimpleGraph<Integer, DefaultEdge> west =
            new SimpleGraph<>(vSupplier, SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        completeGraphGenerator.generateGraph(west);

        Graphs.addGraph(graph, east);
        Graphs.addGraph(graph, west);
        // connected by single edge
        graph.addEdge(size - 1, size);

        return graph;
    }

    private SimpleGraph<Integer, DefaultEdge> buildRingGraph(int size)
    {
        SimpleGraph<Integer, DefaultEdge> clique = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        RingGraphGenerator<Integer, DefaultEdge> graphGenerator = new RingGraphGenerator<>(size);
        graphGenerator.generateGraph(clique);
        return clique;
    }

    private SimpleDirectedGraph<Integer, DefaultEdge> buildLineGraph(int size)
    {
        SimpleDirectedGraph<Integer, DefaultEdge> line = new SimpleDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        LinearGraphGenerator<Integer, DefaultEdge> graphGenerator =
            new LinearGraphGenerator<>(size);
        graphGenerator.generateGraph(line);
        return line;
    }

}
