/*
 * (C) Copyright 2018-2023, by Dimitrios Michail and Contributors.
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

package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple tests for JohnsonSimpleCycles.
 * 
 * @author Dimitrios Michail
 */
public class JohnsonSimpleCyclesTest
{
    @Test
    public void testSmallExample()
    {
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6));
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(2, 5);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 6);
        g.addEdge(6, 1);

        List<List<Integer>> cycles = new JohnsonSimpleCycles<>(g).findSimpleCycles();

        assertEquals(2, cycles.size());

        List<Integer> cycle0 = cycles.get(0);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), cycle0);

        List<Integer> cycle1 = cycles.get(1);
        assertEquals(Arrays.asList(1, 2, 5, 6), cycle1);
    }

}
