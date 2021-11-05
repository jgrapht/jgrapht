/*
 * (C) Copyright 2017-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.color;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Coloring tests
 * 
 * @author Dimitrios Michail
 */
public class GreedyColoringTest
    extends
    BaseColoringTest
{

    @Override
    protected VertexColoringAlgorithm<Integer> getAlgorithm(Graph<Integer, DefaultEdge> graph)
    {
        return new GreedyColoring<>(graph);
    }

    @Override
    protected int getExpectedResultOnDSaturNonOptimalGraph()
    {
        return 4;
    }

    @Test
    public void testGreedy()
    {
        Graph<Integer, DefaultEdge> g = createGraph1();

        Coloring<Integer> coloring = new GreedyColoring<>(g).getColoring();
        assertEquals(3, coloring.getNumberColors());
        Map<Integer, Integer> colors = coloring.getColors();
        assertEquals(0, colors.get(1).intValue());
        assertEquals(1, colors.get(2).intValue());
        assertEquals(2, colors.get(3).intValue());
        assertEquals(1, colors.get(4).intValue());
        assertEquals(1, colors.get(5).intValue());
    }

}
