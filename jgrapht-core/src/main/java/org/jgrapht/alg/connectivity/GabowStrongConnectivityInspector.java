/*
 * (C) Copyright 2013-2021, by Sarah Komla-Ebri and Contributors.
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
import org.jgrapht.util.*;

import java.util.*;

/**
 * Computes the strongly connected components of a directed graph. The implemented algorithm follows
 * Cheriyan-Mehlhorn/Gabow's algorithm presented in Path-based depth-first search for strong and
 * biconnected components by Gabow (2000). The running time is order of $O(|V|+|E|)$.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Sarah Komla-Ebri
 * @author Hannes Wellmann
 */
public class GabowStrongConnectivityInspector<V, E>
    extends
    AbstractStrongConnectivityInspector<V, E>
{
    // the sequence of (original) vertices encountered but not yet assigned to a component
    private Deque<VertexNumber<V>> stackS = new ArrayDeque<>();
    // the boundaries between contracted vertices on the current path of the dfs-tree
    private Deque<VertexNumber<V>> stackB = new ArrayDeque<>();

    // maps vertices to their VertexNumber object
    private Map<V, VertexNumber<V>> vertexToVertexNumber;

    // number of vertices
    private int c;

    /**
     * Constructor
     *
     * @param graph the graph to inspect
     * @throws NullPointerException in case the graph is null
     */
    public GabowStrongConnectivityInspector(Graph<V, E> graph)
    {
        super(graph);
    }

    @Override
    public List<Set<V>> stronglyConnectedSets()
    {
        if (stronglyConnectedSets == null) {
            stronglyConnectedSets = new ArrayList<>();

            // create VertexData objects for all vertices, store them
            createVertexNumber();

            // perform DFS
            for (VertexNumber<V> data : vertexToVertexNumber.values()) {
                if (data.number == 0) {
                    dfsVisit(data);
                }
            }

            vertexToVertexNumber = null;
            stackS = null;
            stackB = null;
        }

        return stronglyConnectedSets;
    }

    /*
     * Creates a VertexNumber object for every vertex in the graph and stores them in a HashMap.
     */
    private void createVertexNumber()
    {
        c = graph.vertexSet().size();
        vertexToVertexNumber = CollectionUtil.newHashMapWithExpectedSize(c);

        for (V vertex : graph.vertexSet()) {
            vertexToVertexNumber.put(vertex, new VertexNumber<>(vertex));
        }

        stackS = new ArrayDeque<>(c);
        stackB = new ArrayDeque<>(c);
    }

    /*
     * The subroutine of DFS.
     */
    private void dfsVisit(VertexNumber<V> v)
    {
        stackS.push(v);
        v.number = stackS.size();
        stackB.push(v);

        // follow all edges

        for (E edge : graph.outgoingEdgesOf(v.vertex)) {
            VertexNumber<V> w = vertexToVertexNumber.get(graph.getEdgeTarget(edge));

            if (w.number == 0) {
                dfsVisit(w);
            } else { /* contract if necessary */
                while (w.number < stackB.peek().number) {
                    stackB.pop();
                }
            }
        }
        if (v == stackB.peek()) {
            // number vertices of the next strong component
            stackB.pop();
            c++;
            Set<V> sccVertices = createSCCVertexSetAndNumberVertices(v);
            stronglyConnectedSets.add(sccVertices);
        }
    }

    private Set<V> createSCCVertexSetAndNumberVertices(VertexNumber<V> v)
    {
        int sccSize = stackS.size() - v.number + 1;
        // All VertexNumber objects on S above and including v form the current SCC.
        // To collect them from S, elements have to be popped while the size of S is greater or
        // equal to v.number. This results in <sccSize> removals(pops) from S.
        Set<V> scc;
        if (sccSize == 1) {
            VertexNumber<V> r = stackS.pop();
            scc = Collections.singleton(r.vertex);
            r.number = c;
        } else {
            scc = CollectionUtil.newHashSetWithExpectedSize(sccSize);
            for (int i = 0; i < sccSize; i++) {
                VertexNumber<V> r = stackS.pop();
                scc.add(r.vertex);
                r.number = c;
            }
        }
        return scc;
    }

    private static final class VertexNumber<V>
    {
        private final V vertex;
        private int number = 0;

        private VertexNumber(V vertex)
        {
            this.vertex = vertex;
        }
    }
}
