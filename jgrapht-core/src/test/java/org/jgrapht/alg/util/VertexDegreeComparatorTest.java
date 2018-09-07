/*
 * (C) Copyright 2016-2018, by Joris Kinable and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.util;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

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
