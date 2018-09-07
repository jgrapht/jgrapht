/*
 * (C) Copyright 2017-2017 Dimitrios Michail and Contributors.
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

import org.jgrapht.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * The largest degree first greedy coloring algorithm.
 * 
 * <p>
 * This is the greedy coloring algorithm which orders the vertices by non-increasing degree. See the
 * following paper for details.
 * <ul>
 * <li>D. J. A. Welsh and M. B. Powell. An upper bound for the chromatic number of a graph and its
 * application to timetabling problems. The Computer Journal, 10(1):85--86, 1967.</li>
 * </ul>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public class LargestDegreeFirstColoring<V, E>
    extends
    GreedyColoring<V, E>
{
    /**
     * Construct a new coloring algorithm.
     * 
     * @param graph the input graph
     */
    public LargestDegreeFirstColoring(Graph<V, E> graph)
    {
        super(graph);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Iterable<V> getVertexOrdering()
    {
        // compute degrees and maximum degree
        int n = graph.vertexSet().size();
        int maxDegree = 0;
        Map<V, Integer> degree = new HashMap<>(n);
        for (V v : graph.vertexSet()) {
            int d = graph.edgesOf(v).size();
            degree.put(v, d);
            if (d > maxDegree) {
                maxDegree = d;
            }
        }

        if (maxDegree > 3 * n) {
            /*
             * Order vertices by degree by using a comparison based sort.
             */
            List<V> nodes = new ArrayList<>(graph.vertexSet());
            nodes.sort((u, v) -> -1 * Integer.compare(degree.get(u), degree.get(v)));
            return nodes;
        } else {
            /*
             * Use bucket sort
             */
            List<V> nodes = new ArrayList<>(n);

            // create buckets
            final Set<V>[] buckets = (Set<V>[]) Array.newInstance(Set.class, maxDegree + 1);
            for (int i = 0; i <= maxDegree; i++) {
                buckets[i] = new HashSet<>();
            }

            // fill buckets
            for (V v : graph.vertexSet()) {
                buckets[degree.get(v)].add(v);
            }

            // collect result
            for (int i = maxDegree; i >= 0; i--) {
                nodes.addAll(buckets[i]);
            }

            return nodes;
        }

    }

}
