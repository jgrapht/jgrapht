/*
 * (C) Copyright 2003-2021, by John V Sichi and Contributors.
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
package org.jgrapht.traverse;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for ClosestFirstIterator.
 *
 * @author John V. Sichi, Patrick Sharp
 */
public class ClosestFirstIteratorTest
    extends
    CrossComponentIteratorTest
{
    // ~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    @Test
    public void testRadius()
    {
        result = new StringBuilder();

        Graph<String, DefaultWeightedEdge> graph = createDirectedGraph();

        // NOTE: pick 301 as the radius because it discriminates
        // the boundary case edge between v7 and v9
        AbstractGraphIterator<String, ?> iterator = new ClosestFirstIterator<>(graph, "1", 301);

        collectResult(iterator, result);
        assertEquals("1,2,3,5,6,7", result.toString());
    }

    /**
     * Test simultaneous search from multiple start vertices.
     */
    @Test
    public void testMultipleStarts()
    {
        result = new StringBuilder();

        Graph<String, DefaultEdge> graph = new DirectedPseudograph<>(DefaultEdge.class);

        graph.addVertex("1");
        graph.addVertex("2");
        graph.addVertex("3");
        graph.addVertex("4");
        graph.addVertex("5");
        graph.addVertex("6");
        graph.addVertex("7");
        graph.addVertex("8");
        graph.addVertex("9");
        graph.addEdge("1", "7");
        graph.addEdge("2", "4");
        graph.addEdge("2", "5");
        graph.addEdge("2", "6");
        graph.addEdge("2", "7");
        graph.addEdge("2", "9");
        graph.addEdge("3", "8");
        graph.addEdge("4", "2");
        graph.addEdge("4", "6");
        graph.addEdge("4", "7");
        graph.addEdge("6", "2");
        graph.addEdge("6", "4");
        graph.addEdge("6", "7");
        graph.addEdge("8", "2");
        graph.addEdge("8", "4");
        graph.addEdge("8", "6");
        graph.addEdge("8", "9");

        List<String> starts = new ArrayList<>();
        starts.add("3");
        starts.add("1");
        AbstractGraphIterator<String, DefaultEdge> iterator =
            new ClosestFirstIterator<>(graph, starts, 2);

        collectResult(iterator, result);
        assertEquals("3,1,8,7,4,9,2,6", result.toString());
    }

    // NOTE: the edge weights make the result deterministic
    @Override
    String getExpectedStr1()
    {
        return "1,2,3,5,6,7,9,4,8";
    }

    @Override
    String getExpectedStr2()
    {
        return getExpectedStr1() + ",orphan";
    }

    @Override
    AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(
        Graph<String, DefaultWeightedEdge> g, String vertex)
    {
        AbstractGraphIterator<String, DefaultWeightedEdge> i =
            new ClosestFirstIterator<>(g, vertex);
        i.setCrossComponentTraversal(true);

        return i;
    }

    @Override
    String getExpectedCCStr1()
    {
        return "orphan,3,7,5,9,6,4,1,2,8";
    }

    @Override
    String getExpectedCCStr2()
    {
        return "orphan,7,9,4,8,2";
    }

    @Override
    String getExpectedCCStr3()
    {
        return "orphan,3,7,5,9,6,4,1,2,8";
    }

    @Override
    int getExpectedCCVertexCount1()
    {
        return 10;
    }

    @Override
    AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(
        Graph<String, DefaultWeightedEdge> g, Iterable<String> startVertex)
    {
        return new ClosestFirstIterator<>(g, startVertex);
    }
}
