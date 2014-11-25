/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* ---------------------
 * VertexCoversTest.java
 * ---------------------
 * (C) Copyright 2003-2008, by Linda Buisman and Contributors.
 *
 * Original Author:  Linda Buisman
 * Contributor(s):   Barak Naveh
 *
 * $Id$
 *
 * Changes
 * -------
 * 06-Nov-2003 : Initial revision (LB);
 * 10-Nov-2003 : Adapted to VertexCovers (BN);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * Tests the vertex cover algorithms.
 *
 * @author Linda Buisman
 * @since Nov 6, 2003
 */
public class VertexCoversTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final int TEST_GRAPH_SIZE = 200;
    private static final int TEST_REPEATS = 20;

    private static final Random rnd = new Random();
    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testFind2ApproximationCover()
    {
        for (int i = 0; i < TEST_REPEATS; i++) {
            Graph<Integer, DefaultEdge> g = createRandomGraph();
            assertTrue(
                isCover(VertexCovers.find2ApproximationCover(g), g));
        }
    }

    /**
     * .
     */
    public void testFindGreedyCover()
    {
        for (int i = 0; i < TEST_REPEATS; i++) {
            Graph<Integer, DefaultEdge> g = createRandomGraph();
            Set<Integer> c =
                VertexCovers.findGreedyCover(
                    Graphs.undirectedGraph(g));
            assertTrue(isCover(c, g));
        }
    }

    /**
     * Checks if the specified vertex set covers every edge of the graph. Uses
     * the definition of Vertex Cover - removes every edge that is incident on a
     * vertex in vertexSet. If no edges are left, vertexSet is a vertex cover
     * for the specified graph.
     *
     * @param vertexSet the vertices to be tested for covering the graph.
     * @param g the graph to be covered.
     *
     * @return
     */
    private boolean isCover(
        Set<Integer> vertexSet,
        Graph<Integer, DefaultEdge> g)
    {
        Set<DefaultEdge> uncoveredEdges = new HashSet<DefaultEdge>(g.edgeSet());

        for (Iterator<Integer> i = vertexSet.iterator(); i.hasNext();) {
            uncoveredEdges.removeAll(g.edgesOf(i.next()));
        }

        return uncoveredEdges.size() == 0;
    }

    /**
     * Create a random graph of TEST_GRAPH_SIZE nodes.
     *
     * @return
     */
    private Graph<Integer, DefaultEdge> createRandomGraph()
    {
        // TODO: move random graph generator to be under GraphGenerator
        // framework.
        Pseudograph<Integer, DefaultEdge> g =
            new Pseudograph<Integer, DefaultEdge>(DefaultEdge.class);

        for (int i = 0; i < TEST_GRAPH_SIZE; i++) {
            g.addVertex(new Integer(i));
        }

        List<Integer> vertices = new ArrayList<Integer>(g.vertexSet());
        // join every vertex with a random number of other vertices
        for (int source = 0; source < TEST_GRAPH_SIZE; source++) {
            int numEdgesToCreate = rnd.nextInt(TEST_GRAPH_SIZE / 2) + 1;

            for (int j = 0; j < numEdgesToCreate; j++) {
                // find a random vertex to join to
                int target = (int) Math.floor(Math.random() * TEST_GRAPH_SIZE);
                g.addEdge(vertices.get(source), vertices.get(target));
            }
        }

        return g;
    }
}

// End VertexCoversTest.java
