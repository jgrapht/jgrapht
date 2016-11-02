/*
 * (C) Copyright 2016-2016, by Assaf Mizrachi and Contributors.
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

package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;

import junit.framework.*;

/**
 * Tests for the {@link KShortestPaths} class using {@link PathValidator}.
 * 
 * @author Assaf Mizrachi
 *
 */
public class KSPPathValidatorTest
    extends TestCase
{

    /**
     * Testing that using path validator that denies all requests finds no paths.
     */
    public void testBlockAll()
    {
        int size = 5;
        SimpleGraph<String, DefaultEdge> clique = buildCliqueGraph(size);
        for (int i = 0; i < size; i++) {
            KShortestPaths<String,
                DefaultEdge> ksp = new KShortestPaths<String, DefaultEdge>(
                    clique, String.valueOf(i), 1, Integer.MAX_VALUE,
                    new PathValidator<String, DefaultEdge>()
                    {

                        @Override
                        public boolean isValidPath(
                            GraphPath<String, DefaultEdge> prevPath,
                            DefaultEdge edge)
                        {
                            // block all paths
                            return false;
                        }
                    });

            for (int j = 0; j < size; j++) {
                if (j == i) {
                    continue;
                }
                List<GraphPath<String, DefaultEdge>> paths = ksp.getPaths(String.valueOf(j));
                assertNull(paths);
            }
        }
    }

    /**
     * Testing that using path validator that accepts all requests finds full paths.
     */
    public void testAllowAll()
    {
        int size = 5;
        SimpleGraph<String, DefaultEdge> clique = buildCliqueGraph(size);
        for (int i = 0; i < size; i++) {
            KShortestPaths<String,
                DefaultEdge> ksp = new KShortestPaths<String, DefaultEdge>(
                    clique, String.valueOf(i), 30, Integer.MAX_VALUE,
                    new PathValidator<String, DefaultEdge>()
                    {

                        @Override
                        public boolean isValidPath(
                            GraphPath<String, DefaultEdge> prevPath,
                            DefaultEdge edge)
                        {
                            // block all paths
                            return true;
                        }
                    });

            for (int j = 0; j < size; j++) {
                if (j == i) {
                    continue;
                }
                List<GraphPath<String, DefaultEdge>> paths = ksp.getPaths(String.valueOf(j));
                assertNotNull(paths);
                assertEquals(16, paths.size());
            }
        }
    }

    /**
     * Testing a ring with only single path allowed between two vertices.
     */
    public void testRing()
    {
        int size = 10;
        SimpleGraph<Integer, DefaultEdge> ring = buildRingGraph(size);
        for (int i = 0; i < size; i++) {
            KShortestPaths<Integer, DefaultEdge> ksp = new KShortestPaths<Integer, DefaultEdge>(
                ring, i, 2, Integer.MAX_VALUE, new PathValidator<Integer, DefaultEdge>()
                {

                    @Override
                    public boolean isValidPath(
                        GraphPath<Integer, DefaultEdge> prevPath, DefaultEdge edge)
                    {
                        if (prevPath == null) {
                            return true;
                        }
                        return Math.abs(
                            prevPath.getEndVertex() - Graphs
                                .getOppositeVertex(ring, edge, prevPath.getEndVertex())) == 1;
                    }
                });

            for (int j = 0; j < size; j++) {
                if (j == i) {
                    continue;
                }
                List<GraphPath<Integer, DefaultEdge>> paths = ksp.getPaths(j);
                assertNotNull(paths);
                assertEquals(1, paths.size());
            }
        }
    }

    /**
     * Testing a graph where the validator denies the request to go on an edge which cutting it
     * makes the graph disconnected
     */
    public void testDisconnected()
    {
        int cliqueSize = 5;
        // generate graph of two cliques connected by single edge
        SimpleGraph<Integer, DefaultEdge> graph = buildGraphForTestDisconnected(cliqueSize);
        for (int i = 0; i < graph.vertexSet().size(); i++) {
            KShortestPaths<Integer, DefaultEdge> ksp = new KShortestPaths<Integer, DefaultEdge>(
                graph, i, 100, Integer.MAX_VALUE, new PathValidator<Integer, DefaultEdge>()
                {

                    @Override
                    public boolean isValidPath(
                        GraphPath<Integer, DefaultEdge> prevPath, DefaultEdge edge)
                    {
                        // accept all requests but the one to pass through the edge connecting
                        // the two cliques.
                        DefaultEdge connectingEdge = graph.getEdge(cliqueSize - 1, cliqueSize);
                        return connectingEdge != edge;
                    }
                });

            for (int j = 0; j < graph.vertexSet().size(); j++) {
                if (j == i) {
                    continue;
                }
                List<GraphPath<Integer, DefaultEdge>> paths = ksp.getPaths(j);
                if ((i < cliqueSize && j < cliqueSize) || (i >= cliqueSize && j >= cliqueSize)) {
                    // within the clique - path should exist
                    assertNotNull(paths);
                    assertTrue(paths.size() > 0);
                } else {
                    // else - should not
                    assertNull(paths);
                }

            }
        }
    }

    private SimpleGraph<String, DefaultEdge> buildCliqueGraph(int size)
    {
        SimpleGraph<String, DefaultEdge> clique = new SimpleGraph<>(DefaultEdge.class);
        CompleteGraphGenerator<String, DefaultEdge> graphGenerator =
            new CompleteGraphGenerator<>(size);
        graphGenerator.generateGraph(clique, new VertexFactory<String>()
        {

            private int index = 0;

            @Override
            public String createVertex()
            {
                return String.valueOf(index++);
            }
        }, null);

        return clique;
    }

    private SimpleGraph<Integer, DefaultEdge> buildGraphForTestDisconnected(int size)
    {
        SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        VertexFactory<Integer> vertexFactory = new IntegerVertexFactory();

        CompleteGraphGenerator<Integer, DefaultEdge> completeGraphGenerator =
            new CompleteGraphGenerator<>(size);
        // two complete graphs
        SimpleGraph<Integer, DefaultEdge> east = new SimpleGraph<>(DefaultEdge.class);
        completeGraphGenerator.generateGraph(east, vertexFactory, null);

        SimpleGraph<Integer, DefaultEdge> west = new SimpleGraph<>(DefaultEdge.class);
        completeGraphGenerator.generateGraph(west, vertexFactory, null);

        Graphs.addGraph(graph, east);
        Graphs.addGraph(graph, west);
        // connected by single edge
        graph.addEdge(size - 1, size);

        return graph;
    }

    private SimpleGraph<Integer, DefaultEdge> buildRingGraph(int size)
    {
        SimpleGraph<Integer, DefaultEdge> clique = new SimpleGraph<>(DefaultEdge.class);
        RingGraphGenerator<Integer, DefaultEdge> graphGenerator = new RingGraphGenerator<>(size);
        graphGenerator.generateGraph(clique, new IntegerVertexFactory(), null);
        return clique;
    }
}
