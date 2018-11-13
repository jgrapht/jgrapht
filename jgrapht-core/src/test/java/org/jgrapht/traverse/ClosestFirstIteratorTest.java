/*
 * (C) Copyright 2003-2018, by John V Sichi and Contributors.
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

        graph.addVertex("1624");
        graph.addVertex("6998");
        graph.addVertex("2652");
        graph.addVertex("7383");
        graph.addVertex("5604");
        graph.addVertex("6009");
        graph.addVertex("3344");
        graph.addVertex("1002");
        graph.addVertex("6067");
        graph.addEdge("2652", "1002");
        graph.addEdge("1002", "6067");
        graph.addEdge("1002", "7383");
        graph.addEdge("1002", "6009");
        graph.addEdge("1002", "6998");
        graph.addEdge("7383", "6998");
        graph.addEdge("7383", "6009");
        graph.addEdge("7383", "3344");
        graph.addEdge("6009", "6998");
        graph.addEdge("6009", "7383");
        graph.addEdge("6009", "3344");
        graph.addEdge("1624", "3344");
        graph.addEdge("6998", "6009");
        graph.addEdge("6998", "7383");
        graph.addEdge("6998", "5604");
        graph.addEdge("6998", "3344");
        graph.addEdge("6998", "6067");

        List<String> starts = new ArrayList<String>();
        starts.add("2652");
        starts.add("1624");
        AbstractGraphIterator<String, DefaultEdge> iterator =
            new ClosestFirstIterator<>(graph, starts, 2);

        collectResult(iterator, result);
        assertEquals("2652,1624,1002,3344,6067,7383,6009,6998", result.toString());
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
        return "orphan,7,3,9,5,4,6,1,2,8";
    }

    @Override
    String getExpectedCCStr2()
    {
        return "orphan,7,9,4,8,2";
    }

    @Override
    String getExpectedCCStr3()
    {
        return "orphan,7,3,9,5,4,6,1,2,8";
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
