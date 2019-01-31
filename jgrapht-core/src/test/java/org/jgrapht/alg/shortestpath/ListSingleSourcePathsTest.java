/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Dimitrios Michail
 */
public class ListSingleSourcePathsTest
{

    @Test
    public void test()
    {
        int n = 50;
        DirectedPseudograph<Integer,
            DefaultWeightedEdge> g = new DirectedPseudograph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER,
                false);
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> gen =
            new GnpRandomGraphGenerator<>(n, 0.7);
        gen.generateGraph(g);

        List<GraphPath<Integer, DefaultWeightedEdge>> p = new ArrayList<>();
        Map<Integer, GraphPath<Integer, DefaultWeightedEdge>> map = new HashMap<>();
        for (int i = 1; i < n; i++) {
            GraphPath<Integer, DefaultWeightedEdge> path =
                new DijkstraShortestPath<>(g).getPath(0, i);
            p.add(path);
            map.put(i, path);
        }

        ListSingleSourcePathsImpl<Integer, DefaultWeightedEdge> paths =
            new ListSingleSourcePathsImpl<>(g, 0, map);

        assertEquals(0, paths.getSourceVertex().intValue());
        assertEquals(0d, paths.getWeight(0), 1e-9);
        for (int i = 1; i < n; i++) {
            assertEquals(p.get(i - 1).getWeight(), paths.getWeight(i), 1e-9);
            assertEquals(p.get(i - 1).getEdgeList(), paths.getPath(i).getEdgeList());
        }
        assertEquals(Double.POSITIVE_INFINITY, paths.getWeight(n), 1e-9);
    }

}
