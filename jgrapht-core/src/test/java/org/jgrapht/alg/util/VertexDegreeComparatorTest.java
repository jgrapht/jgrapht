/*
 * (C) Copyright 2016-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.util;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for VertexDegreeComparator
 *
 * @author Joris Kinable
 */
public class VertexDegreeComparatorTest
{

    protected static final int TEST_REPEATS = 20;

    private GraphGenerator<Integer, DefaultEdge, Integer> randomGraphGenerator;

    public VertexDegreeComparatorTest()
    {
        randomGraphGenerator = new GnmRandomGraphGenerator<>(100, 1000, 0);
    }

    @Test
    public void testVertexDegreeComparator()
    {
        for (int repeat = 0; repeat < TEST_REPEATS; repeat++) {
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            randomGraphGenerator.generateGraph(graph);
            List<Integer> vertices = new ArrayList<>(graph.vertexSet());
            // Sort in ascending vertex degree
            Collections.sort(
                vertices,
                new VertexDegreeComparator<>(graph, VertexDegreeComparator.Order.ASCENDING));
            for (int i = 0; i < vertices.size() - 1; i++)
                assertTrue(graph.degreeOf(vertices.get(i)) <= graph.degreeOf(vertices.get(i + 1)));

            // Sort in descending vertex degree
            Collections.sort(
                vertices,
                new VertexDegreeComparator<>(graph, VertexDegreeComparator.Order.DESCENDING));
            for (int i = 0; i < vertices.size() - 1; i++)
                assertTrue(graph.degreeOf(vertices.get(i)) >= graph.degreeOf(vertices.get(i + 1)));
        }

    }

}
