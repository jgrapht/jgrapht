/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;

import java.util.*;

/**
 * Generate a set of fundamental cycles by building a spanning tree (forest) using an implementation
 * of BFS using a LIFO Stack. The implementation first constructs the spanning forest and then
 * builds the fundamental-cycles set. It supports graphs with self-loops and/or graphs with multiple
 * (parallel) edges.
 * 
 * <p>
 * The algorithm constructs the same fundamental cycle basis as the algorithm in the following
 * paper: K. Paton, An algorithm for finding a fundamental set of cycles for an undirected linear
 * graph, Comm. ACM 12 (1969), pp. 514-518.
 * 
 * <p>
 * The total length of the fundamental-cycle set can be as large as $O(n^3)$ where $n$ is the number
 * of vertices of the graph.
 * 
 * @param <V> the vertex type
 * @param <E> the edge type
 *
 * @author Dimitrios Michail
 * @since October 2016
 */
public class StackBFSFundamentalCycleBasis<V, E>
    extends
    AbstractFundamentalCycleBasis<V, E>
{
    /**
     * Constructor
     * 
     * @param graph the input graph
     */
    public StackBFSFundamentalCycleBasis(Graph<V, E> graph)
    {
        super(graph);
    }

    /**
     * Compute a spanning forest of the graph using a stack (LIFO) based BFS implementation.
     * 
     * <p>
     * The representation assumes that the map contains the predecessor edge of each vertex in the
     * forest. The predecessor edge is the forest edge that was used to discover the vertex. If no
     * such edge was used (the vertex is a leaf in the forest) then the corresponding entry must be
     * null.
     * 
     * @return a map representation of a spanning forest.
     */
    @Override
    protected Map<V, E> computeSpanningForest()
    {
        Map<V, E> pred = new HashMap<>();

        ArrayDeque<V> stack = new ArrayDeque<>();

        for (V s : graph.vertexSet()) {
            // loop over connected-components
            if (pred.containsKey(s)) {
                continue;
            }

            // add s in stack
            pred.put(s, null);
            stack.push(s);

            // start traversal
            while (!stack.isEmpty()) {
                V v = stack.pop();

                for (E e : graph.edgesOf(v)) {
                    V u = Graphs.getOppositeVertex(graph, e, v);
                    if (!pred.containsKey(u)) {
                        pred.put(u, e);
                        stack.push(u);
                    }
                }
            }
        }

        return pred;
    }

}
