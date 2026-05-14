/*
 * (C) Copyright 2019-2026, by Semen Chudakov and Contributors.
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link DefaultManyToManyShortestPaths}.
 *
 * @author Semen Chudakov
 */
public class DefaultManyToManyShortestPathsTest extends BaseManyToManyShortestPathsTest
{
    @Test
    public void testEmptyGraph()
    {
        super.testEmptyGraph();
    }

    @Test
    public void testSourcesIsNull()
    {
        assertThrows(NullPointerException.class, () -> super.testSourcesIsNull());
    }

    @Test
    public void testTargetsIsNull()
    {
        assertThrows(NullPointerException.class, () -> super.testTargetsIsNull());
    }

    @Test
    public void testNoPath()
    {
        super.testNoPath();
    }

    @Test
    public void testDifferentSourcesAndTargetsSimpleGraph()
    {
        super.testDifferentSourcesAndTargetsSimpleGraph();
    }

    @Test
    public void testDifferentSourcesAndTargetsMultigraph()
    {
        super.testDifferentSourcesAndTargetsMultigraph();
    }

    @Test
    public void testSourcesEqualTargetsSimpleGraph()
    {
        super.testSourcesEqualTargetsSimpleGraph();
    }

    @Test
    public void testSourcesEqualTargetsMultigraph()
    {
        super.testSourcesEqualTargetsMultigraph();
    }

    @Test
    public void testOnRandomGraphs()
    {
        super.testOnRandomGraphs(30, 5, new int[][] { { 5, 10 }, { 5, 5 }, { 10, 5 } }, 10);
    }

    /**
     * Regression test that protects DefaultManyToManyShortestPaths against an accidental
     * silent-bypass of its user-supplied algorithm function on the inherited
     * {@link BaseManyToManyShortestPaths#getPaths(Object)} path.
     *
     * <p>
     * If a future maintainer were to push the optimization recently added to
     * {@link DijkstraManyToManyShortestPaths#getPaths(Object)} up into the abstract base class,
     * the user-supplied algorithm function would stop being consulted on
     * {@code getPaths(source)} calls. This test wraps the function in a counting spy and
     * asserts that the spy is invoked, so any such silent rerouting fails loudly here.
     * </p>
     */
    @Test
    public void testGetPathsConsultsProvidedAlgorithmFunction()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.setEdgeWeight(graph.addEdge(1, 2), 1.0);
        graph.setEdgeWeight(graph.addEdge(2, 3), 1.0);
        graph.setEdgeWeight(graph.addEdge(1, 3), 3.0);

        AtomicInteger getPathInvocations = new AtomicInteger(0);
        Function<Graph<Integer, DefaultWeightedEdge>,
            ShortestPathAlgorithm<Integer, DefaultWeightedEdge>> spyFunction =
                g -> new ShortestPathAlgorithm<Integer, DefaultWeightedEdge>()
                {
                    private final ShortestPathAlgorithm<Integer, DefaultWeightedEdge> inner =
                        new DijkstraShortestPath<>(g);

                    @Override
                    public GraphPath<Integer, DefaultWeightedEdge> getPath(
                        Integer source, Integer sink)
                    {
                        getPathInvocations.incrementAndGet();
                        return inner.getPath(source, sink);
                    }

                    @Override
                    public double getPathWeight(Integer source, Integer sink)
                    {
                        return inner.getPathWeight(source, sink);
                    }

                    @Override
                    public SingleSourcePaths<Integer, DefaultWeightedEdge> getPaths(Integer source)
                    {
                        return inner.getPaths(source);
                    }
                };

        DefaultManyToManyShortestPaths<Integer, DefaultWeightedEdge> alg =
            new DefaultManyToManyShortestPaths<>(graph, spyFunction);
        SingleSourcePaths<Integer, DefaultWeightedEdge> paths = alg.getPaths(1);

        // Sanity: the result must agree with a fresh oracle on every target.
        assertCorrectPaths(graph, paths, 1, graph.vertexSet());

        // The whole point of this test: the user-supplied function must be reached.
        // The exact invocation count is implementation detail of the inherited base-class
        // fallback, so we only assert it is positive.
        assertTrue(
            getPathInvocations.get() > 0,
            "DefaultManyToManyShortestPaths.getPaths(source) must consult the user-supplied "
                + "algorithm function; a zero invocation count indicates the function was "
                + "silently bypassed (e.g. by a base-class shortcut).");
    }

    @Override
    protected ManyToManyShortestPathsAlgorithm<Integer, DefaultWeightedEdge> getAlgorithm(
        Graph<Integer, DefaultWeightedEdge> graph)
    {
        return new DefaultManyToManyShortestPaths<>(graph);
    }
}
