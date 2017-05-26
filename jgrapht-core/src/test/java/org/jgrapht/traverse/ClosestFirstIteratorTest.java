/*
 * (C) Copyright 2003-2017, by John V Sichi and Contributors.
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
package org.jgrapht.traverse;

import org.jgrapht.*;
import org.jgrapht.graph.*;

/**
 * Tests for ClosestFirstIterator.
 *
 * @author John V. Sichi, Patrick Sharp
 * @since Sep 3, 2003
 */
public class ClosestFirstIteratorTest
    extends CrossComponentIteratorTest
{
    // ~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testRadius()
    {
        result = new StringBuffer();

        Graph<String, DefaultWeightedEdge> graph = createDirectedGraph();

        // NOTE: pick 301 as the radius because it discriminates
        // the boundary case edge between v7 and v9
        AbstractGraphIterator<String, ?> iterator = new ClosestFirstIterator<>(graph, "1", 301);

        while (iterator.hasNext()) {
            result.append(iterator.next());

            if (iterator.hasNext()) {
                result.append(',');
            }
        }

        assertEquals("1,2,3,5,6,7", result.toString());
    }

    /**
     * .
     */
    public void testNoStart()
    {
        result = new StringBuffer();

        Graph<String, DefaultWeightedEdge> graph = createDirectedGraph();

        AbstractGraphIterator<String, ?> iterator = new ClosestFirstIterator<>(graph);

        while (iterator.hasNext()) {
            result.append(iterator.next());

            if (iterator.hasNext()) {
                result.append(',');
            }
        }

        assertEquals("1,2,3,5,6,7,9,4,8,orphan", result.toString());
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
    String getExpectedCCStr1() {
        return "orphan";
    }

    @Override
    String getExpectedCCStr2() {
        return "orphan,7,9,4,8,2";
    }

    @Override
    String getExpectedCCStr3() {
        return "orphan,7,9,4,8,2,3,5,6,1";
    }

    @Override
    AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(Graph<String, DefaultWeightedEdge> g, Iterable<String> startVertex) {
        return new ClosestFirstIterator<>(g, startVertex);
    }
}

// End ClosestFirstIteratorTest.java
