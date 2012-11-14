/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
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

import junit.framework.TestCase;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


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

    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testFind2ApproximationCover()
    {
        for (int i = 0; i < TEST_REPEATS; i++) {
            final Graph<Integer, DefaultEdge> g = createRandomGraph();
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
            final Graph<Integer, DefaultEdge> g = createRandomGraph();
            final Set<Integer> c =
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
        final Set<Integer> vertexSet,
        final Graph<Integer, DefaultEdge> g)
    {
        final Set<DefaultEdge> uncoveredEdges = new HashSet<DefaultEdge>(g.edgeSet());

        for (Iterator<Integer> i = vertexSet.iterator(); i.hasNext();) {
            uncoveredEdges.removeAll(g.edgesOf(i.next()));
        }

        return uncoveredEdges.isEmpty();
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
        final Pseudograph<Integer, DefaultEdge> g =
            new Pseudograph<Integer, DefaultEdge>(DefaultEdge.class);

        for (int i = 0; i < TEST_GRAPH_SIZE; i++) {
            g.addVertex(new Integer(i));
        }

        final List<Integer> vertices = new ArrayList<Integer>(g.vertexSet());

        // join every vertex with a random number of other vertices
        for (int source = 0; source < TEST_GRAPH_SIZE; source++) {
            final int numEdgesToCreate =
                (int) Math.random() * TEST_GRAPH_SIZE / 2 + 1;

            for (int j = 0; j < numEdgesToCreate; j++) {
                // find a random vertex to join to
                final int target = (int) Math.floor(Math.random() * TEST_GRAPH_SIZE);
                g.addEdge(vertices.get(source), vertices.get(target));
            }
        }

        return g;
    }
}

// End VertexCoversTest.java
