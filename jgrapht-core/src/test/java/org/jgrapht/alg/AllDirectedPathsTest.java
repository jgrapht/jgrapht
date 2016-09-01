/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2012, by Barak Naveh and Contributors.
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
 * AllDirectedPathsTest.java
 * -------------------------
 * (C) Copyright 2016-2016, Vera-Licona Research Group and Contributors.
 *
 * Original Author:  Andrew Gainer-Dewar, Ph.D. (Vera-Licona Research Group)
 * Contributor(s):
 *
 * Changes
 * -------
 * Feb-2016 : Initial version;
 *
 */
package org.jgrapht.alg;

import junit.framework.TestCase;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

/**
 * Test cases for the AllDirectedPaths algorithm.
 *
 * @author Andrew Gainer-Dewar
 **/

public class AllDirectedPathsTest
    extends TestCase {
    private static final String I1 = "I1";
    private static final String I2 = "I2";
    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";
    private static final String D = "D";
    private static final String E = "E";
    private static final String F = "F";
    private static final String O1 = "O1";
    private static final String O2 = "O2";

    public void testSmallExampleGraph () {
        AllDirectedPaths<String, DefaultEdge> pathFindingAlg =
            new AllDirectedPaths<>(toyGraph());

        Set<String> sources = new HashSet<>();
        sources.add(I1);
        sources.add(I2);

        Set<String> targets = new HashSet<>();
        targets.add(O1);
        targets.add(O2);

        List<GraphPath<String, DefaultEdge>> allPaths =
            pathFindingAlg.getAllPaths(sources, targets, true, null);

        assertEquals("Toy network should have correct number of simple paths", 7, allPaths.size());
    }

    public void testTrivialPaths () {
        AllDirectedPaths<String, DefaultEdge> pathFindingAlg =
            new AllDirectedPaths<>(toyGraph());

        Set<String> sources = new HashSet<>();
        sources.add(I1);

        Set<String> targets = new HashSet<>();
        targets.add(I1);
        targets.add(A);

        List<GraphPath<String, DefaultEdge>> allPaths =
            pathFindingAlg.getAllPaths(sources, targets, true, null);

        assertEquals("Toy network should have correct number of trivial simple paths", 2, allPaths.size());
    }


    public void testCycleBehavior () {
        DirectedGraph<String, DefaultEdge> toyGraph = toyGraph();
        toyGraph.addEdge(D, A);

        AllDirectedPaths<String, DefaultEdge> pathFindingAlg =
            new AllDirectedPaths<>(toyGraph);

        Set<String> sources = new HashSet<>();
        sources.add(I1);
        sources.add(I2);

        Set<String> targets = new HashSet<>();
        targets.add(O1);
        targets.add(O2);

        List<GraphPath<String, DefaultEdge>> allPathsWithoutCycle =
            pathFindingAlg.getAllPaths(sources, targets, true, 8);

        List<GraphPath<String, DefaultEdge>> allPathsWithCycle =
            pathFindingAlg.getAllPaths(sources, targets, false, 8);

        assertEquals("Toy network with cycle should have correct number of paths with cycle", 13, allPathsWithCycle.size());
        assertEquals("Toy network with cycle should have correct number of simple paths", 7, allPathsWithoutCycle.size());
    }

    public void testMustBoundIfNonSimplePaths () {
        // Goofy hack to test for an exception

        AllDirectedPaths<String, DefaultEdge> pathFindingAlg =
            new AllDirectedPaths<>(toyGraph());

        Set<String> sources = Collections.singleton(I1);
        Set<String> targets = Collections.singleton(O1);

        try {
            pathFindingAlg.getAllPaths(sources, targets, false, null);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // This is the expected outcome, so the test passes
        }
    }

    private static DirectedGraph<String, DefaultEdge> toyGraph () {
        DirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex(I1);
        graph.addVertex(I2);
        graph.addVertex(A);
        graph.addVertex(B);
        graph.addVertex(C);
        graph.addVertex(D);
        graph.addVertex(E);
        graph.addVertex(F);
        graph.addVertex(O1);
        graph.addVertex(O2);

        graph.addEdge(I1, A);
        graph.addEdge(I1, B);

        graph.addEdge(I2, B);
        graph.addEdge(I2, C);

        graph.addEdge(A, B);
        graph.addEdge(A, D);
        graph.addEdge(A, E);

        graph.addEdge(B, E);

        graph.addEdge(C, B);
        graph.addEdge(C, F);

        graph.addEdge(D, E);

        graph.addEdge(E, O1);

        graph.addEdge(F, O2);

        return graph;
    }
}
