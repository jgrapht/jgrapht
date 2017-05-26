/*
 * (C) Copyright 2003-2017, by Liviu Rau and Contributors.
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
 * Tests for the {@link BreadthFirstIterator} class.
 *
 * <p>
 * NOTE: This test uses hard-coded expected ordering isn't really guaranteed by the specification of
 * the algorithm. This could cause false failures if the traversal implementation changes.
 * </p>
 *
 * @author Liviu Rau, Patrick Sharp
 * @since Jul 30, 2003
 */
public class BreadthFirstIteratorTest
    extends CrossComponentIteratorTest
{
    // ~ Methods ----------------------------------------------------------------

    @Override
    String getExpectedStr1()
    {
        return "1,2,3,4,5,6,7,8,9";
    }

    @Override
    String getExpectedStr2()
    {
        return "1,2,3,4,5,6,7,8,9,orphan";
    }

    @Override
    AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(
        Graph<String, DefaultWeightedEdge> g, String vertex)
    {
        AbstractGraphIterator<String, DefaultWeightedEdge> i =
            new BreadthFirstIterator<>(g, vertex);
        i.setCrossComponentTraversal(true);

        return i;
    }

    @Override
    String getExpectedCCStr1() {
        return "orphan";
    }

    @Override
    String getExpectedCCStr2() {
        return "orphan,7,8,9,2,4";
    }

    @Override
    String getExpectedCCStr3() {
        return "orphan,7,8,9,2,4,3,5,6,1";
    }

    @Override
    AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(Graph<String, DefaultWeightedEdge> g, Iterable<String> startVertex) {
        return new BreadthFirstIterator<>(g, startVertex);
    }
}

// End BreadthFirstIteratorTest.java
