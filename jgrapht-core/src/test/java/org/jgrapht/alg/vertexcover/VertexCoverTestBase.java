/*
 * (C) Copyright 2018-2018, by Linda Buisman and Contributors.
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
package org.jgrapht.alg.vertexcover;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MinimumVertexCoverAlgorithm.VertexCover;
import org.jgrapht.alg.util.IntegerVertexFactory;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;


/**
 *  Base class for vertex cover tests
 *
 * @author Linda Buisman
 */
public interface VertexCoverTestBase {

    // ~ Static fields/initializers ---------------------------------------------

    int TEST_GRAPH_SIZE = 200;
    int TEST_REPEATS = 20;

    Random rnd = new Random(0);

    // ------- Helper methods ------

    /**
     * Checks if the specified vertex set covers every edge of the graph. Uses the definition of
     * Vertex Cover - removes every edge that is incident on a vertex in vertexSet. If no edges are
     * left, vertexSet is a vertex cover for the specified graph.
     *
     * @param vertexCover the vertex cover to be tested for covering the graph.
     * @param g the graph to be covered.
     *
     * @return returns true if the provided vertex cover is a valid cover in the given graph
     */
    default boolean isCover(Graph<Integer, DefaultEdge> g, VertexCover<Integer> vertexCover)
    {
        Set<DefaultEdge> uncoveredEdges = new HashSet<>(g.edgeSet());
        for (Integer v : vertexCover)
            uncoveredEdges.removeAll(g.edgesOf(v));

        return uncoveredEdges.isEmpty();
    }

    /**
     * Create a random PSEUDO graph of TEST_GRAPH_SIZE nodes.
     *
     * @return random pseudo graph with TEST_GRAPH_SIZE vertices and a random number of edges drawn
     *         from the domain [1, TEST_GRAPH_SIZE/2]
     */
    default Graph<Integer, DefaultEdge> createRandomPseudoGraph(int vertices)
    {
        Pseudograph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        GraphGenerator<Integer, DefaultEdge, Integer> graphGenerator =
                new GnmRandomGraphGenerator<>(vertices, rnd.nextInt(vertices / 2) + 1);
        graphGenerator.generateGraph(g, new IntegerVertexFactory(), null);
        return g;
    }
}
