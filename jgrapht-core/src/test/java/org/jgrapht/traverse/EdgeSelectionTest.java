/*
 * (C) Copyright 2019-2020, by Sean Hudson and Contributors.
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
import java.util.stream.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for overriding the {@link CrossComponentIterator#selectOutgoingEdges selectOutgoingEdges}
 * method. Overriding this method allows for selecting outgoing edges based on the source vertex and
 * other traversal state.
 *
 * @author Sean Hudson
 */
public class EdgeSelectionTest
{
    /**
     * Tests selecting the outgoing edges based on criteria that incorporates the source, edge and
     * traversal iterator state.
     */
    @Test
    public void testEdgeSelectionOverride()
    {
        Graph<StatefulVertex, StatefulEdge> graph = createGraph();
        DepthFirstIterator<StatefulVertex, StatefulEdge> iterator =
            new DepthFirstIterator<StatefulVertex, StatefulEdge>(graph)
            {
                String evenEdgeColor = "BLUE";
                String oddEdgeColor = "RED";

                @Override
                protected Set<StatefulEdge> selectOutgoingEdges(StatefulVertex vertex)
                {
                    return graph
                        .outgoingEdgesOf(vertex).stream().filter(e -> filterEdge(vertex, e))
                        .collect(Collectors.toSet());
                }

                /**
                 * Checks if the edge color corresponds to the vertex parity.
                 */
                private boolean filterEdge(StatefulVertex vertex, StatefulEdge edge)
                {
                    return vertex.getState() % 2 == 0 ? edge.getColor().equals(evenEdgeColor)
                        : edge.getColor().equals(oddEdgeColor);
                }
            };
        VertexTrackingTraversalListener<StatefulVertex, StatefulEdge> listener =
            new VertexTrackingTraversalListener<>(graph);

        iterator.addTraversalListener(listener);
        StringBuilder traversedOrder = new StringBuilder();
        while (iterator.hasNext()) {
            traversedOrder.append(iterator.next().getState());
        }
        listener.checkAllVerticesTraversed();
        listener.checkAllVerticesFinished();
        assertEquals("1342567", traversedOrder.toString());
    }

    private Graph<StatefulVertex, StatefulEdge> createGraph()
    {
        Graph<StatefulVertex, StatefulEdge> graph = new DefaultDirectedGraph<>(StatefulEdge.class);

        StatefulVertex v1 = new StatefulVertex(1);
        StatefulVertex v2 = new StatefulVertex(2);
        StatefulVertex v3 = new StatefulVertex(3);
        StatefulVertex v4 = new StatefulVertex(4);
        StatefulVertex v5 = new StatefulVertex(5);
        StatefulVertex v6 = new StatefulVertex(6);
        StatefulVertex v7 = new StatefulVertex(7);

        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);
        graph.addVertex(v4);
        graph.addVertex(v5);
        graph.addVertex(v6);
        graph.addVertex(v7);
        graph.addVertex(v7);

        graph.addEdge(v1, v2, new StatefulEdge("BLUE"));
        graph.addEdge(v1, v3, new StatefulEdge("RED"));
        graph.addEdge(v1, v3, new StatefulEdge("BLUE"));
        graph.addEdge(v2, v4, new StatefulEdge("BLUE"));
        graph.addEdge(v2, v5, new StatefulEdge("BLUE"));
        graph.addEdge(v2, v5, new StatefulEdge("RED"));
        graph.addEdge(v3, v4, new StatefulEdge("RED"));
        graph.addEdge(v3, v7, new StatefulEdge("BLUE"));
        graph.addEdge(v4, v6, new StatefulEdge("RED"));
        graph.addEdge(v6, v2, new StatefulEdge("BLUE"));
        graph.addEdge(v7, v5, new StatefulEdge("RED"));

        return graph;
    }

    private static class StatefulEdge
        extends
        DefaultWeightedEdge
    {
        private final String color;

        StatefulEdge(String color)
        {
            this.color = color;
        }

        String getColor()
        {
            return color;
        }
    }

    private static class StatefulVertex
    {
        private final int state;

        StatefulVertex(int state)
        {
            this.state = state;
        }

        int getState()
        {
            return state;
        }
    }
}
