/*
 * (C) Copyright 2018-2018, by Meghana M Reddy and Contributors.
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
package org.jgrapht.alg.color;

import java.util.*;

import org.jgrapht.util.*;
import org.jgrapht.*;
import org.jgrapht.alg.interfaces.* ;


/**
 * A 2-coloring algorithm for bipartite graphs using a DFS approach.
 * The running time of the algorithm is O(V+E). 
 * The implementation uses parts of code from the isBipartite() method in GraphTests.java.
 * 
 * @author Meghana M Reddy
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class Bipartite2Coloring<V, E> 
    implements VertexColoringAlgorithm<V>
{
    /**
     * The input graph
     */
    protected final Graph<V, E> graph;
    
    /**
     * Construct a new bipartite coloring algorithm.
     * 
     * @param graph the input graph
     */
    public Bipartite2Coloring(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Coloring<V> getColoring()
    {
        Deque<V> stack = new ArrayDeque<>();
        Set<V> unseen = new HashSet<>(graph.vertexSet());
        Set<V> odd = new HashSet<>();

        while (!unseen.isEmpty()) {
            if (stack.isEmpty()) {
                stack.addLast(unseen.iterator().next());
            }

            V v = stack.removeLast();
            unseen.remove(v);

            for (E e : graph.edgesOf(v)) {
                V n = Graphs.getOppositeVertex(graph, e, v);
                if (unseen.contains(n)) {
                    stack.addLast(n);
                    if (!odd.contains(v)) {
                        odd.add(n);
                    }
                } else if (!(odd.contains(v) ^ odd.contains(n))) {
                    throw new IllegalArgumentException("Input graph is not bipartite");
                }
            }
        }
        

        /**
         * Stores the final colors of the vertices after finding a 2-coloring.
         */
        Map<V, Integer> bipartiteColors = new HashMap<>();
        
        Set<V> allVertices = new HashSet<>(graph.vertexSet());
        for (Iterator<V> iter = allVertices.iterator(); iter.hasNext(); ) {
            V element = iter.next();
            if (odd.contains(element)) {
                bipartiteColors.put(element, 0);
            }
            else {
                bipartiteColors.put(element, 1);
            }
        }

        return new ColoringImpl<>(bipartiteColors, 2);
    }
    
}

