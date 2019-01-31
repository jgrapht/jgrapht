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
 * @author Liviu Rau
 */
public abstract class AbstractGraphIteratorTest
{
    // ~ Instance fields --------------------------------------------------------

    StringBuilder result;

    /**
     * .
     */
    @Test
    public void testDirectedGraph()
    {

        Graph<String, DefaultWeightedEdge> graph = createDirectedGraph();
        AbstractGraphIterator<String, DefaultWeightedEdge> iterator = createIterator(graph, "1");

        doDirectedGraphTest(iterator);
    }

    protected void collectResult(Iterator<String> iterator, StringBuilder result)
    {
        while (iterator.hasNext()) {
            result.append(iterator.next());

            if (iterator.hasNext()) {
                result.append(',');
            }
        }
    }

    public void doDirectedGraphTest(AbstractGraphIterator<String, DefaultWeightedEdge> iterator)
    {

        result = new StringBuilder();

        MyTraversalListener<DefaultWeightedEdge> listener = new MyTraversalListener<>();
        iterator.addTraversalListener(listener);

        collectResult(iterator, result);
        assertEquals(getExpectedStr2(), result.toString());

        assertEquals(getExpectedFinishString(), listener.getFinishString());
    }

    abstract String getExpectedStr1();

    abstract String getExpectedStr2();

    String getExpectedFinishString()
    {
        return "";
    }

    Graph<String, DefaultWeightedEdge> createDirectedGraph()
    {
        Graph<String, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        //
        String v1 = "1";
        String v2 = "2";
        String v3 = "3";
        String v4 = "4";
        String v5 = "5";
        String v6 = "6";
        String v7 = "7";
        String v8 = "8";
        String v9 = "9";

        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex("3");
        graph.addVertex("4");
        graph.addVertex("5");
        graph.addVertex("6");
        graph.addVertex("7");
        graph.addVertex("8");
        graph.addVertex("9");

        graph.addVertex("orphan");

        // NOTE: set weights on some of the edges to test traversals like
        // ClosestFirstIterator where it matters. For other traversals, it
        // will be ignored. Rely on the default edge weight being 1.
        graph.addEdge(v1, v2);
        Graphs.addEdge(graph, v1, v3, 100);
        Graphs.addEdge(graph, v2, v4, 1000);
        graph.addEdge(v3, v5);
        Graphs.addEdge(graph, v3, v6, 100);
        graph.addEdge(v5, v6);
        Graphs.addEdge(graph, v5, v7, 200);
        graph.addEdge(v6, v1);
        Graphs.addEdge(graph, v7, v8, 100);
        graph.addEdge(v7, v9);
        graph.addEdge(v8, v2);
        graph.addEdge(v9, v4);

        return graph;
    }

    abstract AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(
        Graph<String, DefaultWeightedEdge> g, String startVertex);

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
                assertEquals(getExpectedStr1(), result.toString());
                assertEquals(9, numComponentVertices);

                break;

            case 2:
                assertEquals(getExpectedStr2(), result.toString());
                assertEquals(1, numComponentVertices);

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
