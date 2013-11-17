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
/* -------------------------
 * UndirectedCycleBaseTest.java
 * -------------------------
 * (C) Copyright 2013, by Nikolay Ognyanov
 *
 * Original Author: Nikolay Ognyanov
 * Contributor(s) :
 *
 * $Id$
 *
 * Changes
 * -------
 * 06-Sep-2013 : Initial revision (NO);
 */
package org.jgrapht.alg.cycle;

import static org.junit.Assert.assertTrue;

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;

public class UndirectedCycleBaseTest
{
    private static int   MAX_SIZE = 10;
    private static int[] RESULTS  = { 0, 0, 0, 1, 3, 6, 10, 15, 21, 28, 36 };

    @Test
    public void test()
    {
        PatonCycleBase<Integer, DefaultEdge> patonFinder =
            new PatonCycleBase<Integer, DefaultEdge>();

        testAlgorithm(patonFinder);
    }

    private void testAlgorithm(
                               UndirectedCycleBase<Integer, DefaultEdge>
                               finder)
    {
        SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>
            (
             new ClassBasedEdgeFactory<Integer, DefaultEdge>
             (
              DefaultEdge.class
             )
            );
        for (int i = 0; i < 7; i++) {
            graph.addVertex(i);
        }

        finder.setGraph(graph);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);
        checkResult(finder, 1);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);
        checkResult(finder, 2);
        graph.addEdge(3, 1);
        checkResult(finder, 3);
        graph.addEdge(3, 4);
        graph.addEdge(4, 2);
        checkResult(finder, 4);
        graph.addEdge(4, 5);
        checkResult(finder, 4);
        graph.addEdge(5, 2);
        checkResult(finder, 5);
        graph.addEdge(5, 6);
        graph.addEdge(6, 4);
        checkResult(finder, 6);

        for (int size = 1; size <= MAX_SIZE; size++) {
            graph = new SimpleGraph<Integer, DefaultEdge>
                (
                 new ClassBasedEdgeFactory<Integer, DefaultEdge>
                 (
                  DefaultEdge.class
                 )
                );
            for (int i = 0; i < size; i++) {
                graph.addVertex(i);
            }
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i != j) {
                        graph.addEdge(i, j);
                    }
                }
            }
            finder.setGraph(graph);
            checkResult(finder, RESULTS[size]);
        }
    }

    private void checkResult(UndirectedCycleBase
                             <Integer, DefaultEdge> finder,
                             int size)
    {
        assertTrue(finder.findCycleBase().size() == size);
    }
}
