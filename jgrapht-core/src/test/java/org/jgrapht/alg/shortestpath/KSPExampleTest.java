/*
 * (C) Copyright 2007-2018, by France Telecom and Contributors.
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

import org.jgrapht.graph.*;
import org.junit.*;

import static org.junit.Assert.assertEquals;

public class KSPExampleTest
{
    // ~ Methods ----------------------------------------------------------------

    @Test
    public void testFourReturnedPathsJGraphT()
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> graph = new KSPExampleGraph();

        String sourceVertex = "S";
        BellmanFordKShortestSimplePaths<String, DefaultWeightedEdge>
            ksp = new BellmanFordKShortestSimplePaths<>(graph);

        String targetVertex = "T";
        assertEquals(3, ksp.getPaths(sourceVertex, targetVertex, 4).size());
    }

    @Test
    public void testThreeReturnedPathsJGraphT()
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> graph = new KSPExampleGraph();

        String sourceVertex = "S";
        int nbPaths = 3;
        BellmanFordKShortestSimplePaths<String, DefaultWeightedEdge>
            ksp = new BellmanFordKShortestSimplePaths<>(graph);

        String targetVertex = "T";
        assertEquals(nbPaths, ksp.getPaths(sourceVertex, targetVertex, nbPaths).size());
    }

    @Test
    public void testTwoReturnedPathsJGraphT()
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> graph = new KSPExampleGraph();

        String sourceVertex = "S";
        int nbPaths = 2;
        BellmanFordKShortestSimplePaths<String, DefaultWeightedEdge>
            ksp = new BellmanFordKShortestSimplePaths<>(graph);

        String targetVertex = "T";
        assertEquals(nbPaths, ksp.getPaths(sourceVertex, targetVertex, nbPaths).size());
    }
}
