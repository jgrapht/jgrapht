/*
 * (C) Copyright 2004-2021, by Marden Neubert and Contributors.
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
import org.jgrapht.util.*;

import java.util.*;

/**
 * A topological ordering iterator for a directed acyclic graph.
 * 
 * <p>
 * A topological order is a permutation <code>p</code> of the vertices of a graph such that an edge
 * <code>(i,j)</code> implies that <code>i</code> appears before <code>j</code> in <code>p</code>.
 * For more information see
 * <a href="https://en.wikipedia.org/wiki/Topological_sorting">wikipedia</a> or
 * <a href="http://mathworld.wolfram.com/TopologicalSort.html">wolfram</a>.
 *
 * <p>
 * The iterator crosses components but does not track them, it only tracks visited vertices. The
 * iterator will detect (at some point) if the graph is not a directed acyclic graph and throw a
 * {@link IllegalArgumentException}.
 * 
 * <p>
 * For this iterator to work correctly the graph must not be modified during iteration. Currently
 * there are no means to ensure that, nor to fail-fast. The results of such modifications are
 * undefined.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Marden Neubert
 * @author Dimitrios Michail
 */
public class TopologicalOrderIterator<V, E>
    extends
    AbstractGraphIterator<V, E>
{
    private static final String GRAPH_IS_NOT_A_DAG = "Graph is not a DAG";

    private Queue<V> queue;
    private Map<V, ModifiableInteger> inDegreeMap;
    private int remainingVertices;
    private V cur;

    /**
     * Construct a topological order iterator.
     * 
     * <p>
     * Traversal will start at one of the graph's <i>sources</i>. See the definition of source at
     * <a href="http://mathworld.wolfram.com/Source.html">
     * http://mathworld.wolfram.com/Source.html</a>. In case of partial order, tie-breaking is
     * arbitrary.
     *
     * @param graph the directed graph to be iterated
     */
    public TopologicalOrderIterator(Graph<V, E> graph)
    {
        this(graph, (Comparator<V>) null);
    }

    /**
     * Construct a topological order iterator.
     * 
     * <p>
     * Traversal will start at one of the graph's <i>sources</i>. See the definition of source at
     * <a href="http://mathworld.wolfram.com/Source.html">
     * http://mathworld.wolfram.com/Source.html</a>. In case of partial order, a comparator is used
     * to break ties.
     *
     * @param graph the directed graph to be iterated
     * @param comparator comparator in order to break ties in case of partial order
     */
    public TopologicalOrderIterator(Graph<V, E> graph, Comparator<V> comparator)
    {
        super(graph);
        GraphTests.requireDirected(graph);

        // create queue
        if (comparator == null) {
            this.queue = new ArrayDeque<>();
        } else {
            this.queue = new PriorityQueue<>(comparator);
        }

        // count in-degrees
        this.inDegreeMap = new HashMap<>();
        for (V v : graph.vertexSet()) {
            int d = 0;
            for (E e : graph.incomingEdgesOf(v)) {
                V u = Graphs.getOppositeVertex(graph, e, v);
                if (v.equals(u)) {
                    throw new IllegalArgumentException(GRAPH_IS_NOT_A_DAG);
                }
                d++;
            }
            inDegreeMap.put(v, new ModifiableInteger(d));
            if (d == 0) {
                queue.offer(v);
            }
        }

        // record vertices count
        this.remainingVertices = graph.vertexSet().size();
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
        V result = queue.poll();

        if (result != null) {
            for (E e : graph.outgoingEdgesOf(result)) {
                V other = Graphs.getOppositeVertex(graph, e, result);

                ModifiableInteger inDegree = inDegreeMap.get(other);
                if (inDegree.value > 0) {
                    inDegree.value--;

                    if (inDegree.value == 0) {
                        queue.offer(other);
                    }
                }
            }

            --remainingVertices;
        } else {
            /*
             * Still expecting some vertices, but no vertex has zero degree.
             */
            if (remainingVertices > 0) {
                throw new IllegalArgumentException(GRAPH_IS_NOT_A_DAG);
            }
        }

        return result;
    }

}
