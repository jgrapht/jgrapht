/*
 * (C) Copyright 2019-2020, by Semen Chudakov and Contributors.
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
import org.junit.*;

/**
 * Tests for {@link DijkstraManyToManyShortestPaths}.
 *
 * @author Semen Chudakov
 */
public class DijkstraManyToManyShortestPathsTest
    extends
    BaseManyToManyShortestPathsTest
{

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
    public void testOnRandomGraphs()
    {
        super.testOnRandomGraphs(100, 20, new int[][] { { 50, 30 }, { 40, 40 }, { 30, 50 } }, 50);
    }

    @Override
    protected ManyToManyShortestPathsAlgorithm<Integer, DefaultWeightedEdge> getAlgorithm(
        Graph<Integer, DefaultWeightedEdge> graph)
    {
        return new DijkstraManyToManyShortestPaths<>(graph);
    }
}
