/*
 * (C) Copyright 2010-2021, by Michael Behrisch, Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.color;

import org.jgrapht.*;
import org.jgrapht.util.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * The smallest degree last greedy coloring algorithm.
 * 
 * <p>
 * This is the greedy coloring algorithm with the smallest-last ordering of the vertices. The basic
 * idea is as follows: Assuming that vertices $v_{k+1}, \dotso, v_n$ have been already selected,
 * choose $v_k$ so that the degree of $v_k$ in the subgraph induced by $V - $(v_{k+1}, \dotso, v_n)$
 * is minimal. See the following paper for details.
 * <ul>
 * <li>D. Matula, G. Marble, and J. Isaacson. Graph coloring algorithms in Graph Theory and
 * Computing. Academic Press, 104--122, 1972.</li>
 * </ul>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Michael Behrisch
 * @author Dimitrios Michail
 */
public class SmallestDegreeLastColoring<V, E>
    extends
    GreedyColoring<V, E>
{
    /**
     * Construct a new coloring algorithm.
     * 
     * @param graph the input graph
     */
    public SmallestDegreeLastColoring(Graph<V, E> graph)
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
        Map<V, Integer> degree = CollectionUtil.newHashMapWithExpectedSize(n);
        for (V v : graph.vertexSet()) {
            int d = graph.edgesOf(v).size();
            degree.put(v, d);
            if (d > maxDegree) {
                maxDegree = d;
            }
        }

        // create buckets
        final Set<V>[] buckets = (Set<V>[]) Array.newInstance(Set.class, maxDegree + 1);
        for (int i = 0; i <= maxDegree; i++) {
            buckets[i] = new HashSet<>();
        }

        // fill buckets
        for (V v : graph.vertexSet()) {
            buckets[degree.get(v)].add(v);
        }

        // create order
        Deque<V> order = new ArrayDeque<>();
        for (int i = 0; i <= maxDegree; i++) {
            while (buckets[i].size() > 0) {
                V v = buckets[i].iterator().next();
                buckets[i].remove(v);
                order.addFirst(v);
                degree.remove(v);

                for (E e : graph.edgesOf(v)) {
                    V u = Graphs.getOppositeVertex(graph, e, v);
                    if (v.equals(u)) {
                        throw new IllegalArgumentException(SELF_LOOPS_NOT_ALLOWED);
                    }
                    Integer d = degree.get(u);
                    if (d != null && d > 0) {
                        buckets[d].remove(u);
                        d--;
                        degree.put(u, d);
                        buckets[d].add(u);
                        if (d < i) {
                            i = d;
                        }
                    }
                }
            }
        }

        return order;
    }

}
