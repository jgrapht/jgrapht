/*
 * (C) Copyright 2007-2018, by France Telecom and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.graph.*;
import org.junit.*;

import static org.junit.Assert.*;

public class KSPExampleTest
{
    // ~ Methods ----------------------------------------------------------------

    @Test
    public void testFourReturnedPathsJGraphT()
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> graph = new KSPExampleGraph();

        String sourceVertex = "S";
        KShortestSimplePaths<String, DefaultWeightedEdge> ksp = new KShortestSimplePaths<>(graph);

        String targetVertex = "T";
        assertEquals(3, ksp.getPaths(sourceVertex, targetVertex, 4).size());
    }

    @Test
    public void testThreeReturnedPathsJGraphT()
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> graph = new KSPExampleGraph();

        String sourceVertex = "S";
        int nbPaths = 3;
        KShortestSimplePaths<String, DefaultWeightedEdge> ksp = new KShortestSimplePaths<>(graph);

        String targetVertex = "T";
        assertEquals(nbPaths, ksp.getPaths(sourceVertex, targetVertex, nbPaths).size());
    }

    @Test
    public void testTwoReturnedPathsJGraphT()
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> graph = new KSPExampleGraph();

        String sourceVertex = "S";
        int nbPaths = 2;
        KShortestSimplePaths<String, DefaultWeightedEdge> ksp = new KShortestSimplePaths<>(graph);

        String targetVertex = "T";
        assertEquals(nbPaths, ksp.getPaths(sourceVertex, targetVertex, nbPaths).size());
    }
}

// End $file.name$
