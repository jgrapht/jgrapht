/*
 * (C) Copyright 2003-2018, by Linda Buisman and Contributors.
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
package org.jgrapht.alg.vertexcover;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * Base class for vertex cover tests
 *
 * @author Linda Buisman
 */
public class VertexCoverTestUtils
{

    public VertexCoverTestUtils()
    {
    }

    // ~ Static fields/initializers ---------------------------------------------

    public final static int TEST_GRAPH_SIZE = 200;
    public final static int TEST_REPEATS = 20;

    public final static Random rnd = new Random(0);

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
    static boolean isCover(
        Graph<Integer, DefaultEdge> g, VertexCoverAlgorithm.VertexCover<Integer> vertexCover)
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
    static Graph<Integer, DefaultEdge> createRandomPseudoGraph(int vertices)
    {
        Pseudograph<Integer, DefaultEdge> g = new Pseudograph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> graphGenerator =
            new GnmRandomGraphGenerator<>(vertices, rnd.nextInt(vertices / 2) + 1);
        graphGenerator.generateGraph(g);
        return g;
    }
}
