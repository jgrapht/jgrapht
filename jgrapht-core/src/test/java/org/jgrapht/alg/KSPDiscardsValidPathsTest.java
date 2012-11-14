/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2010, by Barak Naveh and Contributors.
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
/* -------------------------
 * KSPDiscardsValidPathsTest.java
 * -------------------------
 * (C) Copyright 2010-2010, by France Telecom
 *
 * Original Author:  Guillaume Boulmier and Contributors.
 *
 * $Id: MaskFunctor.java 645 2008-09-30 19:44:48Z perfecthash $
 *
 * Changes
 * -------
 * 06-Dec-2010 : Initial revision (GB);
 *
 */
package org.jgrapht.alg;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;


@SuppressWarnings("unchecked")
public class KSPDiscardsValidPathsTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    /**
     * Example with a biconnected graph but not 3-connected. With a graph not
     * 3-connected, the start vertex and the end vertex can be disconnected by 2
     * paths.
     */
    public void testNot3connectedGraph()
    {
        final WeightedMultigraph<String, DefaultWeightedEdge> graph;
        final KShortestPaths<String, DefaultWeightedEdge> paths;

        graph =
            new WeightedMultigraph<String, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        graph.addVertex("S");
        graph.addVertex("T");
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");
        graph.addVertex("F");
        graph.addVertex("G");
        graph.addVertex("H");
        graph.addVertex("I");
        graph.addVertex("J");
        graph.addVertex("K");
        graph.addVertex("L");

        addGraphEdge(graph, "S", "A", 1.0);
        addGraphEdge(graph, "A", "T", 1.0);
        addGraphEdge(graph, "A", "B", 1.0);
        addGraphEdge(graph, "B", "T", 1.0);
        addGraphEdge(graph, "B", "C", 1.0);

        addGraphEdge(graph, "C", "D", 1.0);
        addGraphEdge(graph, "C", "E", 1.0);
        addGraphEdge(graph, "C", "F", 1.0);
        addGraphEdge(graph, "D", "G", 1.0);
        addGraphEdge(graph, "E", "G", 1.0);
        addGraphEdge(graph, "F", "G", 1.0);

        addGraphEdge(graph, "G", "H", 1.0);
        addGraphEdge(graph, "H", "I", 1.0);
        addGraphEdge(graph, "I", "J", 1.0);
        addGraphEdge(graph, "J", "K", 1.0);
        addGraphEdge(graph, "K", "L", 1.0);
        addGraphEdge(graph, "L", "S", 1.0);

        paths = new KShortestPaths<String, DefaultWeightedEdge>(graph, "S", 3);

        Assert.assertTrue(paths.getPaths("T").size() == 3);
    }

    /**
     * JUnit test for the bug reported by Bruno Maoili. Example with a connected
     * graph but not 2-connected. With a graph not 2-connected, the start vertex
     * and the end vertex can be disconnected by 1 path.
     */
    public void testBrunoMaoili()
    {
        final WeightedMultigraph<String, DefaultWeightedEdge> graph;
        KShortestPaths<String, DefaultWeightedEdge> paths;

        graph =
            new WeightedMultigraph<String, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");

        addGraphEdge(graph, "A", "B", 1.0);
        addGraphEdge(graph, "A", "C", 2.0);
        addGraphEdge(graph, "B", "D", 1.0);
        addGraphEdge(graph, "B", "D", 1.0);
        addGraphEdge(graph, "B", "D", 1.0);
        addGraphEdge(graph, "B", "E", 1.0);
        addGraphEdge(graph, "C", "D", 1.0);

        paths = new KShortestPaths<String, DefaultWeightedEdge>(graph, "A", 2);
        Assert.assertTrue(paths.getPaths("E").size() == 2);

        paths = new KShortestPaths<String, DefaultWeightedEdge>(graph, "A", 3);
        Assert.assertTrue(paths.getPaths("E").size() == 3);

        paths = new KShortestPaths<String, DefaultWeightedEdge>(graph, "A", 4);
        Assert.assertTrue(paths.getPaths("E").size() == 4);
    }

    private void addGraphEdge(
        final WeightedMultigraph<String, DefaultWeightedEdge> graph,
        final String sourceVertex,
        final String targetVertex,
        final double weight)
    {
        final DefaultWeightedEdge edge = new DefaultWeightedEdge();

        graph.addEdge(sourceVertex, targetVertex, edge);
        graph.setEdgeWeight(edge, weight);
    }
}

// End KSPDiscardsValidPathsTest.java
