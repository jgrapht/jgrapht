/*
 * (C) Copyright 2005-2017, by Christian Soltenborn and Contributors.
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
package org.jgrapht.alg.connectivity;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * Computes strongly connected components of a directed graph. The algorithm is implemented after
 * "Cormen et al: Introduction to algorithms", Chapter 22.5. It has a running time of $O(V + E)$.
 *
 * <p>
 * Unlike {@link ConnectivityInspector}, this class does not implement incremental inspection. The
 * full algorithm is executed at the first call of
 * {@link KosarajuStrongConnectivityInspector#stronglyConnectedSets()} or
 * {@link KosarajuStrongConnectivityInspector#isStronglyConnected()}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Christian Soltenborn
 * @author Christian Hammer
 */
public class KosarajuStrongConnectivityInspector<V, E>
    extends
    AbstractStrongConnectivityInspector<V, E>
{
    // stores the vertices, ordered by their finishing time in first dfs
    private LinkedList<VertexData<V>> orderedVertices;

    // maps vertices to their VertexData object
    private Map<V, VertexData<V>> vertexToVertexData;

    /**
     * Constructor
     *
     * @param graph the input graph
     * @throws NullPointerException if the input graph is null
     */
    public KosarajuStrongConnectivityInspector(Graph<V, E> graph)
    {
        super(graph);
    }

    @Override
    public List<Set<V>> stronglyConnectedSets()
    {
        if (stronglyConnectedSets == null) {
            orderedVertices = new LinkedList<VertexData<V>>();
            stronglyConnectedSets = new Vector<Set<V>>();

            // create VertexData objects for all vertices, store them
            createVertexData();

            // perform the first round of DFS, result is an ordering
            // of the vertices by decreasing finishing time
            for (VertexData<V> data : vertexToVertexData.values()) {
                if (!data.isDiscovered()) {
                    dfsVisit(graph, data, null);
                }
            }

            // 'create' inverse graph (i.e. every edge is reversed)
            Graph<V, E> inverseGraph = new EdgeReversedGraph<V, E>(graph);

            // get ready for next dfs round
            resetVertexData();

            // second dfs round: vertices are considered in decreasing
            // finishing time order; every tree found is a strongly
            // connected set
            for (VertexData<V> data : orderedVertices) {
                if (!data.isDiscovered()) {
                    // new strongly connected set
                    Set<V> set = new HashSet<V>();
                    stronglyConnectedSets.add(set);
                    dfsVisit(inverseGraph, data, set);
                }
            }

            // clean up for garbage collection
            orderedVertices = null;
            vertexToVertexData = null;
        }

        return stronglyConnectedSets;
    }

    /*
     * Creates a VertexData object for every vertex in the graph and stores them in a HashMap.
     */
    private void createVertexData()
    {
        vertexToVertexData = new HashMap<V, VertexData<V>>(graph.vertexSet().size());

        for (V vertex : graph.vertexSet()) {
            vertexToVertexData.put(vertex, new VertexData2<V>(vertex, false, false));
        }
    }

    /*
     * The subroutine of DFS. NOTE: the set is used to distinguish between 1st and 2nd round of DFS.
     * set == null: finished vertices are stored (1st round). set != null: all vertices found will
     * be saved in the set (2nd round)
     */
    private void dfsVisit(Graph<V, E> visitedGraph, VertexData<V> vertexData, Set<V> vertices)
    {
        Deque<VertexData<V>> stack = new ArrayDeque<VertexData<V>>();
        stack.add(vertexData);

        while (!stack.isEmpty()) {
            VertexData<V> data = stack.removeLast();

            if (!data.isDiscovered()) {
                data.setDiscovered(true);

                if (vertices != null) {
                    vertices.add(data.getVertex());
                }

                stack.add(new VertexData1<V>(data, true, true));

                // follow all edges
                for (E edge : visitedGraph.outgoingEdgesOf(data.getVertex())) {
                    VertexData<V> targetData =
                        vertexToVertexData.get(visitedGraph.getEdgeTarget(edge));

                    if (!targetData.isDiscovered()) {
                        // the "recursion"
                        stack.add(targetData);
                    }
                }
            } else if (data.isFinished()) {
                if (vertices == null) {
                    orderedVertices.addFirst(data.getFinishedData());
                }
            }
        }
    }

    /*
     * Resets all VertexData objects.
     */
    private void resetVertexData()
    {
        for (VertexData<V> data : vertexToVertexData.values()) {
            data.setDiscovered(false);
            data.setFinished(false);
        }
    }

    /*
     * Lightweight class storing some data for every vertex.
     */
    private static abstract class VertexData<V>
    {
        private byte bitfield;

        private VertexData(boolean discovered, boolean finished)
        {
            this.bitfield = 0;
            setDiscovered(discovered);
            setFinished(finished);
        }

        private boolean isDiscovered()
        {
            return (bitfield & 1) == 1;
        }

        private boolean isFinished()
        {
            return (bitfield & 2) == 2;
        }

        private void setDiscovered(boolean discovered)
        {
            if (discovered) {
                bitfield |= 1;
            } else {
                bitfield &= ~1;
            }
        }

        private void setFinished(boolean finished)
        {
            if (finished) {
                bitfield |= 2;
            } else {
                bitfield &= ~2;
            }
        }

        abstract VertexData<V> getFinishedData();

        abstract V getVertex();
    }

    private static final class VertexData1<V>
        extends
        VertexData<V>
    {
        private final VertexData<V> finishedData;

        private VertexData1(VertexData<V> finishedData, boolean discovered, boolean finished)
        {
            super(discovered, finished);
            this.finishedData = finishedData;
        }

        @Override
        VertexData<V> getFinishedData()
        {
            return finishedData;
        }

        @Override
        V getVertex()
        {
            return null;
        }
    }

    private static final class VertexData2<V>
        extends
        VertexData<V>
    {
        private final V vertex;

        private VertexData2(V vertex, boolean discovered, boolean finished)
        {
            super(discovered, finished);
            this.vertex = vertex;
        }

        @Override
        VertexData<V> getFinishedData()
        {
            return null;
        }

        @Override
        V getVertex()
        {
            return vertex;
        }
    }
}
