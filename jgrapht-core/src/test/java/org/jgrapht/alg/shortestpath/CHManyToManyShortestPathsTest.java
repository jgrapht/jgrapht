/*
 * (C) Copyright 2019-2021, by Semen Chudakov and Contributors.
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
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.ConcurrencyUtil;
import org.junit.*;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

import static org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation.ContractionHierarchy;
import static org.junit.Assert.assertEquals;

/**
 * Test for {@link CHManyToManyShortestPaths}.
 *
 * @author Semen Chudakov
 */
public class CHManyToManyShortestPathsTest
    extends
    BaseManyToManyShortestPathsTest
{
    /**
     * Executor which is supplied to the {@link CHManyToManyShortestPaths} in this test case.
     */
    private static ThreadPoolExecutor executor;

    @BeforeClass
    public static void createExecutor()
    {
        executor =
            ConcurrencyUtil.createThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    }

    @AfterClass
    public static void shutdownExecutor()
        throws InterruptedException
    {
        ConcurrencyUtil.shutdownExecutionService(executor);
    }

    @Test
    public void testEmptyGraph()
    {
        super.testEmptyGraph();
    }

    @Test(expected = NullPointerException.class)
    public void testSourcesIsNull()
    {
        super.testSourcesIsNull();
    }

    @Test(expected = NullPointerException.class)
    public void testTargetsIsNull()
    {
        super.testTargetsIsNull();
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
    public void testMoreSourcesThanTargets1()
    {
        Graph<Integer, DefaultWeightedEdge> graph = getSimpleGraph();

        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor)
                .computeContractionHierarchy();

        ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths<Integer,
            DefaultWeightedEdge> shortestPaths = new CHManyToManyShortestPaths<>(hierarchy)
                .getManyToManyPaths(Set.of(1, 3, 7, 9), Set.of(5));

        assertEquals(2.0, shortestPaths.getWeight(1, 5), 1e-9);
        assertEquals(Arrays.asList(1, 4, 5), shortestPaths.getPath(1, 5).getVertexList());

        assertEquals(2.0, shortestPaths.getWeight(3, 5), 1e-9);
        assertEquals(Arrays.asList(3, 6, 5), shortestPaths.getPath(3, 5).getVertexList());

        assertEquals(2.0, shortestPaths.getWeight(7, 5), 1e-9);
        assertEquals(Arrays.asList(7, 4, 5), shortestPaths.getPath(7, 5).getVertexList());

        assertEquals(2.0, shortestPaths.getWeight(9, 5), 1e-9);
        assertEquals(Arrays.asList(9, 6, 5), shortestPaths.getPath(9, 5).getVertexList());
    }

    @Test
    public void testMoreSourcesThanTargets2()
    {
        Graph<Integer, DefaultWeightedEdge> graph = getMultigraph();

        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor)
                .computeContractionHierarchy();

        ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths<Integer,
            DefaultWeightedEdge> shortestPaths = new CHManyToManyShortestPaths<>(hierarchy)
                .getManyToManyPaths(Set.of(2, 3, 4, 5, 6), Set.of(1));

        assertEquals(3.0, shortestPaths.getWeight(2, 1), 1e-9);
        assertEquals(Arrays.asList(2, 1), shortestPaths.getPath(2, 1).getVertexList());

        assertEquals(8.0, shortestPaths.getWeight(3, 1), 1e-9);
        assertEquals(Arrays.asList(3, 2, 1), shortestPaths.getPath(3, 1).getVertexList());

        assertEquals(19.0, shortestPaths.getWeight(4, 1), 1e-9);
        assertEquals(Arrays.asList(4, 3, 2, 1), shortestPaths.getPath(4, 1).getVertexList());

        assertEquals(32.0, shortestPaths.getWeight(5, 1), 1e-9);
        assertEquals(Arrays.asList(5, 4, 3, 2, 1), shortestPaths.getPath(5, 1).getVertexList());

        assertEquals(23.0, shortestPaths.getWeight(6, 1), 1e-9);
        assertEquals(Arrays.asList(6, 1), shortestPaths.getPath(6, 1).getVertexList());
    }

    @Test
    public void testOnRandomGraphs()
    {
        super.testOnRandomGraphs(40, 5, new int[][] { { 10, 15 }, { 10, 10 }, { 15, 10 } }, 10);
    }

    @Override
    protected ManyToManyShortestPathsAlgorithm<Integer, DefaultWeightedEdge> getAlgorithm(
        Graph<Integer, DefaultWeightedEdge> graph)
    {
        ContractionHierarchy<Integer, DefaultWeightedEdge> hierarchy =
            new ContractionHierarchyPrecomputation<>(graph, () -> new Random(SEED), executor)
                .computeContractionHierarchy();
        return new CHManyToManyShortestPaths<>(hierarchy);
    }
}
