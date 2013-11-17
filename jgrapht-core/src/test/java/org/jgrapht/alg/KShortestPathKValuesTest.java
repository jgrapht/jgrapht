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
 * KShortestPathKValuesTest.java
 * -------------------------
 * (C) Copyright 2007-2010, by France Telecom
 *
 * Original Author:  Guillaume Boulmier and Contributors.
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Jun-2007 : Initial revision (GB);
 * 06-Dec-2010 : Bugfixes (GB);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.util.*;


/**
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
@SuppressWarnings("unchecked")
public class KShortestPathKValuesTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

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

    public void testMaxSizeValueCompleteGraph6()
    {
        KShortestPathCompleteGraph6 graph = new KShortestPathCompleteGraph6();

        for (
            int maxSize = 1;
            maxSize <= calculateNbElementaryPathsForCompleteGraph(6);
            maxSize++)
        {
            KShortestPaths finder = new KShortestPaths(graph, "vS", maxSize);

            assertEquals(finder.getPaths("v1").size(), maxSize);
            assertEquals(finder.getPaths("v2").size(), maxSize);
            assertEquals(finder.getPaths("v3").size(), maxSize);
            assertEquals(finder.getPaths("v4").size(), maxSize);
            assertEquals(finder.getPaths("v5").size(), maxSize);
        }
    }

    public void testNbReturnedPaths()
    {
        KShortestPathCompleteGraph4 kSPCompleteGraph4 =
            new KShortestPathCompleteGraph4();
        verifyNbPathsForAllPairsOfVertices(kSPCompleteGraph4);

        KShortestPathCompleteGraph5 kSPCompleteGraph5 =
            new KShortestPathCompleteGraph5();
        verifyNbPathsForAllPairsOfVertices(kSPCompleteGraph5);

        KShortestPathCompleteGraph6 kSPCompleteGraph6 =
            new KShortestPathCompleteGraph6();
        verifyNbPathsForAllPairsOfVertices(kSPCompleteGraph6);
    }

    /**
     * Compute the total number of paths between every pair of vertices in a
     * complete graph with <code>n</code> vertices.
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

    private void verifyNbPathsForAllPairsOfVertices(Graph graph)
    {
        long nbPaths =
            calculateNbElementaryPathsForCompleteGraph(
                graph.vertexSet().size());
        int maxSize = Integer.MAX_VALUE;

        for (
            Iterator sourceIterator = graph.vertexSet().iterator();
            sourceIterator.hasNext();)
        {
            Object sourceVertex = sourceIterator.next();

            KShortestPaths finder =
                new KShortestPaths(graph, sourceVertex,
                    maxSize);
            for (
                Iterator targetIterator = graph.vertexSet().iterator();
                targetIterator.hasNext();)
            {
                Object targetVertex = targetIterator.next();
                if (targetVertex != sourceVertex) {
                    assertEquals(finder.getPaths(targetVertex).size(), nbPaths);
                }
            }
        }
    }
}

// End KShortestPathKValuesTest.java
