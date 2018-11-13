/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
import org.jgrapht.alg.interfaces.MultiObjectiveShortestPathAlgorithm.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;
import java.util.stream.*;

import static org.junit.Assert.assertEquals;

/**
 * Test {@link MartinShortestPath}.
 * 
 * @author Dimitrios Michail
 */
public class MartinShortestPathTest
{

    @Test
    public void testGraphDirected()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        IntStream.range(1, 6).forEach(g::addVertex);
        DefaultEdge e12 = g.addEdge(1, 2);
        DefaultEdge e13 = g.addEdge(1, 3);
        DefaultEdge e14 = g.addEdge(1, 4);
        DefaultEdge e24 = g.addEdge(2, 4);
        DefaultEdge e25 = g.addEdge(2, 5);
        DefaultEdge e34 = g.addEdge(3, 4);
        DefaultEdge e35 = g.addEdge(3, 5);
        DefaultEdge e45 = g.addEdge(4, 5);

        DefaultEdgeFunction<DefaultEdge, double[]> f =
            new DefaultEdgeFunction<>(new double[] { 0.0, 0.0 });

        f.set(e12, new double[] { 1.0, 5.0 });
        f.set(e13, new double[] { 4.0, 2.0 });
        f.set(e14, new double[] { 4.0, 4.0 });
        f.set(e24, new double[] { 1.0, 2.0 });
        f.set(e25, new double[] { 2.0, 5.0 });
        f.set(e34, new double[] { 2.0, 3.0 });
        f.set(e35, new double[] { 6.0, 1.0 });
        f.set(e45, new double[] { 3.0, 3.0 });

        MultiObjectiveSingleSourcePaths<Integer, DefaultEdge> paths1 =
            new MartinShortestPath<>(g, f).getPaths(1);

        List<GraphPath<Integer, DefaultEdge>> paths11 = paths1.getPaths(1);
        assertEquals(1, paths11.size());
        List<GraphPath<Integer, DefaultEdge>> paths12 = paths1.getPaths(2);
        assertEquals(1, paths12.size());
        List<GraphPath<Integer, DefaultEdge>> paths13 = paths1.getPaths(3);
        assertEquals(1, paths13.size());
        List<GraphPath<Integer, DefaultEdge>> paths14 = paths1.getPaths(4);
        assertEquals(2, paths14.size());
        List<GraphPath<Integer, DefaultEdge>> paths15 = paths1.getPaths(5);
        assertEquals(3, paths15.size());

    }

    @Test
    public void testNoPaths()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex(1);
        g.addVertex(2);

        DefaultEdgeFunction<DefaultEdge, double[]> f =
            new DefaultEdgeFunction<>(new double[] { 0.0, 0.0 });

        MultiObjectiveSingleSourcePaths<Integer, DefaultEdge> paths1 =
            new MartinShortestPath<>(g, f).getPaths(1);

        List<GraphPath<Integer, DefaultEdge>> paths11 = paths1.getPaths(1);
        assertEquals(1, paths11.size());
        List<GraphPath<Integer, DefaultEdge>> paths12 = paths1.getPaths(2);
        assertEquals(0, paths12.size());

        MultiObjectiveSingleSourcePaths<Integer, DefaultEdge> paths2 =
            new MartinShortestPath<>(g, f).getPaths(2);

        List<GraphPath<Integer, DefaultEdge>> paths21 = paths2.getPaths(1);
        assertEquals(0, paths21.size());
        List<GraphPath<Integer, DefaultEdge>> paths22 = paths2.getPaths(2);
        assertEquals(1, paths22.size());
    }

}
