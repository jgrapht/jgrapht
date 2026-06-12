/*
 * (C) Copyright 2017-2026, by Dimitrios Michail and Contributors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.IntStream;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.MultiObjectiveShortestPathAlgorithm.MultiObjectiveSingleSourcePaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultEdgeFunction;
import org.jgrapht.graph.DirectedPseudograph;
import org.junit.jupiter.api.Test;

/**
 * Test {@link LabelCorrectingMultiObjectiveShortestPath}.
 *
 * @author Mario Fuentes Jimenez
 */
public class LabelCorrectingMultiObjectiveShortestPathTest
{

    @Test
    public void testGraphDirected()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);

        DefaultEdge e12 = g.addEdge(1, 2);
        DefaultEdge e13 = g.addEdge(1, 3);
        DefaultEdge e32 = g.addEdge(3, 2);

        DefaultEdgeFunction<DefaultEdge, double[]> f =
            new DefaultEdgeFunction<>(new double[] { 0.0, 0.0 });

        f.set(e12, new double[] { 5.0, 2.0 });
        f.set(e13, new double[] { 2.0, 8.0 });
        f.set(e32, new double[] { 1.0, 4.0 });

        MultiObjectiveSingleSourcePaths<Integer, DefaultEdge> paths =
            new LabelCorrectingMultiObjectiveShortestPath<>(g, f).getPaths(1);

        List<GraphPath<Integer, DefaultEdge>> paths12 = paths.getPaths(2);
        assertEquals(2, paths12.size());
    }

    @Test
    public void testDominatedPaths()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);

        DefaultEdge e12 = g.addEdge(1, 2);
        DefaultEdge e13 = g.addEdge(1, 3);
        DefaultEdge e32 = g.addEdge(3, 2);

        DefaultEdgeFunction<DefaultEdge, double[]> f =
            new DefaultEdgeFunction<>(new double[] { 0.0, 0.0 });

        f.set(e12, new double[] { 3.0, 3.0 });
        f.set(e13, new double[] { 5.0, 5.0 });
        f.set(e32, new double[] { 5.0, 5.0 });

        MultiObjectiveSingleSourcePaths<Integer, DefaultEdge> paths =
            new LabelCorrectingMultiObjectiveShortestPath<>(g, f).getPaths(1);

        List<GraphPath<Integer, DefaultEdge>> paths12 = paths.getPaths(2);
        assertEquals(1, paths12.size());
    }

    @Test
    public void testApproximation()
    {
        DirectedPseudograph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);

        DefaultEdge e12 = g.addEdge(1, 2);
        DefaultEdge e13 = g.addEdge(1, 3);
        DefaultEdge e32 = g.addEdge(3, 2);

        DefaultEdgeFunction<DefaultEdge, double[]> f =
            new DefaultEdgeFunction<>(new double[] { 0.0, 0.0 });

        f.set(e12, new double[] { 10.0, 10.0 });
        f.set(e13, new double[] { 10.5, 9.8 });
        f.set(e32, new double[] { 0.0, 0.0 });

        MultiObjectiveSingleSourcePaths<Integer, DefaultEdge> exactPaths =
            new LabelCorrectingMultiObjectiveShortestPath<>(g, f, 0.0).getPaths(1);

        List<GraphPath<Integer, DefaultEdge>> exactPaths12 = exactPaths.getPaths(2);
        assertEquals(2, exactPaths12.size());

        MultiObjectiveSingleSourcePaths<Integer, DefaultEdge> approximatePaths =
            new LabelCorrectingMultiObjectiveShortestPath<>(g, f, 0.10).getPaths(1);

        List<GraphPath<Integer, DefaultEdge>> approximatePaths12 = approximatePaths.getPaths(2);
        assertEquals(1, approximatePaths12.size());
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
            new LabelCorrectingMultiObjectiveShortestPath<>(g, f).getPaths(1);

        List<GraphPath<Integer, DefaultEdge>> paths11 = paths1.getPaths(1);
        assertEquals(1, paths11.size());
        List<GraphPath<Integer, DefaultEdge>> paths12 = paths1.getPaths(2);
        assertEquals(0, paths12.size());

        MultiObjectiveSingleSourcePaths<Integer, DefaultEdge> paths2 =
            new LabelCorrectingMultiObjectiveShortestPath<>(g, f).getPaths(2);

        List<GraphPath<Integer, DefaultEdge>> paths21 = paths2.getPaths(1);
        assertEquals(0, paths21.size());
        List<GraphPath<Integer, DefaultEdge>> paths22 = paths2.getPaths(2);
        assertEquals(1, paths22.size());
    }

    @Test
    public void testCompareWithMartinShortestPath()
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

        MultiObjectiveSingleSourcePaths<Integer, DefaultEdge> martinPaths =
            new MartinShortestPath<>(g, f).getPaths(1);

        MultiObjectiveSingleSourcePaths<Integer, DefaultEdge> labelCorrectingPaths =
            new LabelCorrectingMultiObjectiveShortestPath<>(g, f).getPaths(1);

        for (int i = 1; i <= 5; i++) {
            assertEquals(martinPaths.getPaths(i).size(), labelCorrectingPaths.getPaths(i).size());
        }
    }

}
