/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2010, by Barak Naveh and Contributors.
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

import junit.framework.*;

import org.jgrapht.graph.*;


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
        WeightedMultigraph<String, DefaultWeightedEdge> graph;
        KShortestPaths<String, DefaultWeightedEdge> paths;

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

        this.addGraphEdge(graph, "S", "A", 1.0);
        this.addGraphEdge(graph, "A", "T", 1.0);
        this.addGraphEdge(graph, "A", "B", 1.0);
        this.addGraphEdge(graph, "B", "T", 1.0);
        this.addGraphEdge(graph, "B", "C", 1.0);

        this.addGraphEdge(graph, "C", "D", 1.0);
        this.addGraphEdge(graph, "C", "E", 1.0);
        this.addGraphEdge(graph, "C", "F", 1.0);
        this.addGraphEdge(graph, "D", "G", 1.0);
        this.addGraphEdge(graph, "E", "G", 1.0);
        this.addGraphEdge(graph, "F", "G", 1.0);

        this.addGraphEdge(graph, "G", "H", 1.0);
        this.addGraphEdge(graph, "H", "I", 1.0);
        this.addGraphEdge(graph, "I", "J", 1.0);
        this.addGraphEdge(graph, "J", "K", 1.0);
        this.addGraphEdge(graph, "K", "L", 1.0);
        this.addGraphEdge(graph, "L", "S", 1.0);

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
        WeightedMultigraph<String, DefaultWeightedEdge> graph;
        KShortestPaths<String, DefaultWeightedEdge> paths;

        graph =
            new WeightedMultigraph<String, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");

        this.addGraphEdge(graph, "A", "B", 1.0);
        this.addGraphEdge(graph, "A", "C", 2.0);
        this.addGraphEdge(graph, "B", "D", 1.0);
        this.addGraphEdge(graph, "B", "D", 1.0);
        this.addGraphEdge(graph, "B", "D", 1.0);
        this.addGraphEdge(graph, "B", "E", 1.0);
        this.addGraphEdge(graph, "C", "D", 1.0);

        paths = new KShortestPaths<String, DefaultWeightedEdge>(graph, "A", 2);
        Assert.assertTrue(paths.getPaths("E").size() == 2);

        paths = new KShortestPaths<String, DefaultWeightedEdge>(graph, "A", 3);
        Assert.assertTrue(paths.getPaths("E").size() == 3);

        paths = new KShortestPaths<String, DefaultWeightedEdge>(graph, "A", 4);
        Assert.assertTrue(paths.getPaths("E").size() == 4);
    }

    private void addGraphEdge(
        WeightedMultigraph<String, DefaultWeightedEdge> graph,
        String sourceVertex,
        String targetVertex,
        double weight)
    {
        DefaultWeightedEdge edge = new DefaultWeightedEdge();

        graph.addEdge(sourceVertex, targetVertex, edge);
        graph.setEdgeWeight(edge, weight);
    }
}

// End KSPDiscardsValidPathsTest.java
