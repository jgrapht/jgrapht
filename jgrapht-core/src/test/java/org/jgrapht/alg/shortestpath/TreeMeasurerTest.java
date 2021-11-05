/*
 * (C) Copyright 2018-2021, by Alexandru Valeanu and Contributors.
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
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link TreeMeasurer}
 *
 * @author Alexandru Valeanu
 */
public class TreeMeasurerTest
{

    @Test
    public void testNoCenters()
    {
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);

        TreeMeasurer<Integer, DefaultEdge> treeMeasurer = new TreeMeasurer<>(tree);

        assertEquals(new HashSet<>(), treeMeasurer.getGraphCenter());
    }

    @Test
    public void testTwoCenters()
    {
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);

        tree.addVertex(1);
        tree.addVertex(2);
        tree.addVertex(3);
        tree.addVertex(4);

        tree.addEdge(1, 2);
        tree.addEdge(2, 3);
        tree.addEdge(3, 4);

        TreeMeasurer<Integer, DefaultEdge> treeMeasurer = new TreeMeasurer<>(tree);

        assertEquals(Set.of(2, 3), treeMeasurer.getGraphCenter());
    }

    @Test
    public void testOneCenter()
    {
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);

        tree.addVertex(1);
        tree.addVertex(2);
        tree.addVertex(3);
        tree.addVertex(4);
        tree.addVertex(5);

        tree.addEdge(1, 2);
        tree.addEdge(2, 3);
        tree.addEdge(3, 4);
        tree.addEdge(4, 5);

        TreeMeasurer<Integer, DefaultEdge> treeMeasurer = new TreeMeasurer<>(tree);

        assertEquals(Set.of(3), treeMeasurer.getGraphCenter());
    }
}
