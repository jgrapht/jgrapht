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

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 */
public class KShortestPathKValuesTest
{
    // ~ Methods ----------------------------------------------------------------

    /**
     * @param k
     * @param n
     *
     * @return A(n,k).
     */
    public static long permutation(int n, int k)
    {
        if (k <= n) {
            return MathUtil.factorial(n) / MathUtil.factorial(n - k);
        } else {
            return 0;
        }
    }

    @Test
    public void testMaxSizeValueCompleteGraph6()
    {
        KShortestPathCompleteGraph6 graph = new KShortestPathCompleteGraph6();

        for (int maxSize = 1; maxSize <= calculateNbElementaryPathsForCompleteGraph(6); maxSize++) {
            KShortestSimplePaths<String, DefaultWeightedEdge> finder =
                new KShortestSimplePaths<>(graph);

            assertEquals(finder.getPaths("vS", "v1", maxSize).size(), maxSize);
            assertEquals(finder.getPaths("vS", "v2", maxSize).size(), maxSize);
            assertEquals(finder.getPaths("vS", "v3", maxSize).size(), maxSize);
            assertEquals(finder.getPaths("vS", "v4", maxSize).size(), maxSize);
            assertEquals(finder.getPaths("vS", "v5", maxSize).size(), maxSize);
        }
    }

    @Test
    public void testNbReturnedPaths()
    {
        KShortestPathCompleteGraph4 kSPCompleteGraph4 = new KShortestPathCompleteGraph4();
        verifyNbPathsForAllPairsOfVertices(kSPCompleteGraph4);

        KShortestPathCompleteGraph5 kSPCompleteGraph5 = new KShortestPathCompleteGraph5();
        verifyNbPathsForAllPairsOfVertices(kSPCompleteGraph5);

        KShortestPathCompleteGraph6 kSPCompleteGraph6 = new KShortestPathCompleteGraph6();
        verifyNbPathsForAllPairsOfVertices(kSPCompleteGraph6);
    }

    /**
     * Compute the total number of paths between every pair of vertices in a complete graph with
     * <code>n</code> vertices.
     *
     * @param n
     *
     * @return
     */
    private long calculateNbElementaryPathsForCompleteGraph(int n)
    {
        long nbPaths = 0;
        for (int k = 1; k <= (n - 1); k++) {
            nbPaths = nbPaths + permutation(n - 2, k - 1);
        }
        return nbPaths;
    }

    private void verifyNbPathsForAllPairsOfVertices(Graph<String, DefaultWeightedEdge> graph)
    {
        long nbPaths = calculateNbElementaryPathsForCompleteGraph(graph.vertexSet().size());
        int maxSize = Integer.MAX_VALUE;

        for (String sourceVertex : graph.vertexSet()) {
            KShortestSimplePaths<String, DefaultWeightedEdge> finder =
                new KShortestSimplePaths<>(graph);
            for (String targetVertex : graph.vertexSet()) {
                if (targetVertex != sourceVertex) {
                    assertEquals(
                        finder.getPaths(sourceVertex, targetVertex, maxSize).size(), nbPaths);
                }
            }
        }
    }
}
