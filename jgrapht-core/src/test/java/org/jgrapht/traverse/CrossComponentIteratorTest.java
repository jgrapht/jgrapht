/*
 * (C) Copyright 2003-2018, by Liviu Rau and Contributors.
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
import org.jgrapht.event.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * A basis for testing {@link org.jgrapht.traverse.BreadthFirstIterator} and
 * {@link org.jgrapht.traverse.DepthFirstIterator} classes.
 *
 * @author Patrick Sharp (I pretty much just ripped off Liviu Rau's code from
 *         AbstractGraphIteratorTest)
 */
public abstract class CrossComponentIteratorTest
    extends
    AbstractGraphIteratorTest
{
    // ~ Instance fields --------------------------------------------------------

    StringBuilder result;

    // ~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    @Test
    public void testDirectedGraphViaCCI()
    {
        result = new StringBuilder();

        Graph<String, DefaultWeightedEdge> graph = createDirectedGraph();

        AbstractGraphIterator<String, DefaultWeightedEdge> iterator =
            createIterator(graph, Arrays.asList("orphan", "7", "3"));
        MyTraversalListener<DefaultWeightedEdge> listener = new MyTraversalListener<>();
        iterator.addTraversalListener(listener);

        collectResult(iterator, result);
        assertEquals(getExpectedCCStr3(), result.toString());

        assertEquals(getExpectedCCFinishString(), listener.getFinishString());
    }

    @Test
    public void testDirectedGraphNullConstructors()
    {
        Graph<String, DefaultWeightedEdge> graph = createDirectedGraph();
        doDirectedGraphTest(createIterator(graph, (String) null));
        doDirectedGraphTest(createIterator(graph, (Iterable<String>) null));
    }

    abstract String getExpectedCCStr1();

    abstract String getExpectedCCStr2();

    abstract String getExpectedCCStr3();

    int getExpectedCCVertexCount1()
    {
        return 1;
    }

    String getExpectedCCFinishString()
    {
        return "";
    }

    abstract AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(
        Graph<String, DefaultWeightedEdge> g, Iterable<String> startVertex);

    // ~ Inner Classes ----------------------------------------------------------

    /**
     * Internal traversal listener.
     *
     * @author Barak Naveh
     */
    private class MyTraversalListener<E>
        implements
        TraversalListener<String, E>
    {
        private int componentNumber = 0;
        private int numComponentVertices = 0;

        private String finishString = "";

        /**
         * @see TraversalListener#connectedComponentFinished(ConnectedComponentTraversalEvent)
         */
        @Override
        public void connectedComponentFinished(ConnectedComponentTraversalEvent e)
        {
            switch (componentNumber) {
            case 1:
                assertEquals(getExpectedCCStr1(), result.toString());
                assertEquals(getExpectedCCVertexCount1(), numComponentVertices);

                break;

            case 2:
                assertEquals(getExpectedCCStr2(), result.toString());
                assertEquals(5, numComponentVertices);

                break;

            case 3:
                assertEquals(getExpectedCCStr3(), result.toString());
                assertEquals(4, numComponentVertices);

                break;

            default:
                Assert.fail("Should not get here.");

                break;
            }

            numComponentVertices = 0;
        }

        /**
         * @see TraversalListener#connectedComponentStarted(ConnectedComponentTraversalEvent)
         */
        @Override
        public void connectedComponentStarted(ConnectedComponentTraversalEvent e)
        {
            componentNumber++;
        }

        /**
         * @see TraversalListener#edgeTraversed(EdgeTraversalEvent)
         */
        @Override
        public void edgeTraversed(EdgeTraversalEvent<E> e)
        {
            // to be tested...
        }

        /**
         * @see TraversalListener#vertexTraversed(VertexTraversalEvent)
         */
        @Override
        public void vertexTraversed(VertexTraversalEvent<String> e)
        {
            numComponentVertices++;
        }

        /**
         * @see TraversalListener#vertexTraversed(VertexTraversalEvent)
         */
        @Override
        public void vertexFinished(VertexTraversalEvent<String> e)
        {
            finishString += e.getVertex() + ":";
        }

        public String getFinishString()
        {
            return finishString;
        }
    }
}
