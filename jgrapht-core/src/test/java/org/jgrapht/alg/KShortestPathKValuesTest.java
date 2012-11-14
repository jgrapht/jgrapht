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
    public static long permutation(final int n, final int k)
    {
        if (k <= n) {
            return MathUtil.factorial(n) / MathUtil.factorial(n - k);
        } else {
            return 0;
        }
    }

    public void testMaxSizeValueCompleteGraph6()
    {
        final KShortestPathCompleteGraph6 graph = new KShortestPathCompleteGraph6();

        for (
            int maxSize = 1;
            maxSize <= calculateNbElementaryPathsForCompleteGraph(6);
            maxSize++)
        {
            final KShortestPaths finder = new KShortestPaths(graph, "vS", maxSize);

            assertEquals(finder.getPaths("v1").size(), maxSize);
            assertEquals(finder.getPaths("v2").size(), maxSize);
            assertEquals(finder.getPaths("v3").size(), maxSize);
            assertEquals(finder.getPaths("v4").size(), maxSize);
            assertEquals(finder.getPaths("v5").size(), maxSize);
        }
    }

    public void testNbReturnedPaths()
    {
        final KShortestPathCompleteGraph4 kSPCompleteGraph4 =
            new KShortestPathCompleteGraph4();
        verifyNbPathsForAllPairsOfVertices(kSPCompleteGraph4);

        final KShortestPathCompleteGraph5 kSPCompleteGraph5 =
            new KShortestPathCompleteGraph5();
        verifyNbPathsForAllPairsOfVertices(kSPCompleteGraph5);

        final KShortestPathCompleteGraph6 kSPCompleteGraph6 =
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
    private long calculateNbElementaryPathsForCompleteGraph(final int n)
    {
        long nbPaths = 0;
        for (int k = 1; k <= n - 1; k++) {
            nbPaths += permutation(n - 2, k - 1);
        }
        return nbPaths;
    }

    private void verifyNbPathsForAllPairsOfVertices(final Graph graph)
    {
        final long nbPaths =
            calculateNbElementaryPathsForCompleteGraph(
                graph.vertexSet().size());
        final int maxSize = Integer.MAX_VALUE;

        for (
            Iterator sourceIterator = graph.vertexSet().iterator();
            sourceIterator.hasNext();)
        {
            final Object sourceVertex = sourceIterator.next();

            final KShortestPaths finder =
                new KShortestPaths(graph, sourceVertex,
                    maxSize);
            for (
                Iterator targetIterator = graph.vertexSet().iterator();
                targetIterator.hasNext();)
            {
                final Object targetVertex = targetIterator.next();
                if (targetVertex != sourceVertex) {
                    assertEquals(finder.getPaths(targetVertex).size(), nbPaths);
                }
            }
        }
    }
}

// End KShortestPathKValuesTest.java
