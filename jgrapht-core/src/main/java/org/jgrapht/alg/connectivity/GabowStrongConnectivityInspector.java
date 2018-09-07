/*
 * (C) Copyright 2013-2017, by Sarah Komla-Ebri and Contributors.
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
package org.jgrapht.alg.connectivity;

import org.jgrapht.*;

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
 * @since September, 2013
 */
public class GabowStrongConnectivityInspector<V, E>
    extends
    AbstractStrongConnectivityInspector<V, E>
{
    // stores the vertices
    private Deque<VertexNumber<V>> stack = new ArrayDeque<>();

    // maps vertices to their VertexNumber object
    private Map<V, VertexNumber<V>> vertexToVertexNumber;

    // store the numbers
    private Deque<Integer> B = new ArrayDeque<>();

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
            stronglyConnectedSets = new Vector<>();

            // create VertexData objects for all vertices, store them
            createVertexNumber();

            // perform DFS
            for (VertexNumber<V> data : vertexToVertexNumber.values()) {
                if (data.getNumber() == 0) {
                    dfsVisit(graph, data);
                }
            }

            vertexToVertexNumber = null;
            stack = null;
            B = null;
        }

        return stronglyConnectedSets;
    }

    /*
     * Creates a VertexNumber object for every vertex in the graph and stores them in a HashMap.
     */

    private void createVertexNumber()
    {
        c = graph.vertexSet().size();
        vertexToVertexNumber = new HashMap<>(c);

        for (V vertex : graph.vertexSet()) {
            vertexToVertexNumber.put(vertex, new VertexNumber<>(vertex, 0));
        }

        stack = new ArrayDeque<>(c);
        B = new ArrayDeque<>(c);
    }

    /*
     * The subroutine of DFS.
     */
    private void dfsVisit(Graph<V, E> visitedGraph, VertexNumber<V> v)
    {
        VertexNumber<V> w;
        stack.add(v);
        B.add(v.setNumber(stack.size() - 1));

        // follow all edges

        for (E edge : visitedGraph.outgoingEdgesOf(v.getVertex())) {
            w = vertexToVertexNumber.get(visitedGraph.getEdgeTarget(edge));

            if (w.getNumber() == 0) {
                dfsVisit(graph, w);
            } else { /* contract if necessary */
                while (w.getNumber() < B.getLast()) {
                    B.removeLast();
                }
            }
        }
        Set<V> L = new HashSet<>();
        if (v.getNumber() == (B.getLast())) {
            /*
             * number vertices of the next strong component
             */
            B.removeLast();

            c++;
            while (v.getNumber() <= (stack.size() - 1)) {
                VertexNumber<V> r = stack.removeLast();
                L.add(r.getVertex());
                r.setNumber(c);
            }
            stronglyConnectedSets.add(L);
        }
    }

    private static final class VertexNumber<V>
    {
        V vertex;
        int number;

        private VertexNumber(V vertex, int number)
        {
            this.vertex = vertex;
            this.number = number;
        }

        int getNumber()
        {
            return number;
        }

        V getVertex()
        {
            return vertex;
        }

        Integer setNumber(int n)
        {
            return number = n;
        }
    }
}

// End GabowStrongConnectivityInspector.java
