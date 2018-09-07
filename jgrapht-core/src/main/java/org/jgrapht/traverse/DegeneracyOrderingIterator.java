/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.traverse;

import org.jgrapht.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * A degeneracy ordering iterator.
 * 
 * <p>
 * The degeneracy of a graph $G $is the smallest value d such that every nonempty subgraph of $G$
 * contains a vertex of degree at most $d.$ If a graph has degeneracy $d$, then it has a degeneracy
 * ordering, an ordering such that each vertex has $d$ or fewer neighbors that come later in the
 * ordering.
 * 
 * <p>
 * The iterator crosses components but does not track them, it only tracks visited vertices.
 * 
 * <p>
 * The iterator treats the input graph as undirected even if the graph is directed. Moreover, it
 * completely ignores self-loops, meaning that it operates as if self-loops do not contribute to the
 * degree of a vertex.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 * @since February, 2017
 */
public class DegeneracyOrderingIterator<V, E>
    extends
    AbstractGraphIterator<V, E>
{
    private Set<V>[] buckets;
    private Map<V, Integer> degrees;
    private int minDegree;
    private V cur;

    /**
     * Constructor
     *
     * @param graph the graph to be iterated
     */
    @SuppressWarnings("unchecked")
    public DegeneracyOrderingIterator(Graph<V, E> graph)
    {
        super(graph);

        /*
         * Count degrees, but skip self-loops
         */
        this.minDegree = Integer.MAX_VALUE;
        int maxDegree = 0;
        this.degrees = new HashMap<>();
        for (V v : graph.vertexSet()) {
            int d = 0;
            for (E e : graph.edgesOf(v)) {
                V u = Graphs.getOppositeVertex(graph, e, v);
                if (!v.equals(u)) {
                    d++;
                }
            }
            degrees.put(v, d);
            minDegree = Math.min(minDegree, d);
            maxDegree = Math.max(maxDegree, d);
        }
        minDegree = Math.min(minDegree, maxDegree);

        /*
         * Create buckets
         */
        this.buckets = (Set<V>[]) Array.newInstance(Set.class, maxDegree + 1);
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new HashSet<>();
        }
        for (V v : graph.vertexSet()) {
            buckets[degrees.get(v)].add(v);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * Always returns true since the iterator does not care about components.
     */
    @Override
    public boolean isCrossComponentTraversal()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * Trying to disable the cross components nature of this iterator will result into throwing a
     * {@link IllegalArgumentException}.
     */
    @Override
    public void setCrossComponentTraversal(boolean crossComponentTraversal)
    {
        if (!crossComponentTraversal) {
            throw new IllegalArgumentException("Iterator is always cross-component");
        }
    }

    @Override
    public boolean hasNext()
    {
        if (cur != null) {
            return true;
        }
        cur = advance();
        if (cur != null && nListeners != 0) {
            fireVertexTraversed(createVertexTraversalEvent(cur));
        }
        return cur != null;
    }

    @Override
    public V next()
    {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        V result = cur;
        cur = null;
        if (nListeners != 0) {
            fireVertexFinished(createVertexTraversalEvent(result));
        }
        return result;
    }

    private V advance()
    {
        while (minDegree < buckets.length && buckets[minDegree].isEmpty()) {
            minDegree++;
        }

        V result = null;

        if (minDegree < buckets.length) {
            Set<V> b = buckets[minDegree];
            V v = b.iterator().next();
            b.remove(v);
            degrees.remove(v);

            for (E e : graph.edgesOf(v)) {
                V u = Graphs.getOppositeVertex(graph, e, v);

                if (v.equals(u)) {
                    // ignore self-loop
                    continue;
                }

                if (degrees.containsKey(u)) {
                    int uDegree = degrees.get(u);
                    if (uDegree > minDegree) {
                        buckets[uDegree].remove(u);
                        uDegree--;
                        degrees.put(u, uDegree);
                        buckets[uDegree].add(u);
                    }
                }
            }
            result = v;
        }

        return result;
    }

}
