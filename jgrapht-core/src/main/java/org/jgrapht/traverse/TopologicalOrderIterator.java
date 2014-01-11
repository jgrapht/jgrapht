/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -----------------------------
 * TopologicalOrderIterator.java
 * -----------------------------
 * (C) Copyright 2004-2008, by Marden Neubert and Contributors.
 *
 * Original Author:  Marden Neubert
 * Contributor(s):   Barak Naveh, John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 17-Dec-2004 : Initial revision (MN);
 * 25-Apr-2005 : Fixes for start vertex order (JVS);
 * 06-Jun-2005 : Made generic (CH);
 *
 */
package org.jgrapht.traverse;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.util.*;


/**
 * Implements topological order traversal for a directed acyclic graph. A
 * topological sort is a permutation <tt>p</tt> of the vertices of a graph such
 * that an edge <tt>(i,j)</tt> implies that <tt>i</tt> appears before <tt>j</tt>
 * in <tt>p</tt> (Skiena 1990, p. 208). See also <a
 * href="http://mathworld.wolfram.com/TopologicalSort.html">
 * http://mathworld.wolfram.com/TopologicalSort.html</a>.
 *
 * <p>See "Algorithms in Java, Third Edition, Part 5: Graph Algorithms" by
 * Robert Sedgewick and "Data Structures and Algorithms with Object-Oriented
 * Design Patterns in Java" by Bruno R. Preiss for implementation alternatives.
 * The latter can be found online at <a
 * href="http://www.brpreiss.com/books/opus5/">
 * http://www.brpreiss.com/books/opus5/</a></p>
 *
 * <p>For this iterator to work correctly the graph must be acyclic, and must
 * not be modified during iteration. Currently there are no means to ensure
 * that, nor to fail-fast; the results with cyclic input (including self-loops)
 * or concurrent modifications are undefined. To precheck a graph for cycles,
 * consider using {@link org.jgrapht.alg.CycleDetector} or {@link
 * org.jgrapht.alg.StrongConnectivityInspector}.</p>
 *
 * @author Marden Neubert
 * @since Dec 18, 2004
 */
public class TopologicalOrderIterator<V, E>
    extends CrossComponentIterator<V, E, Object>
{
    

    private Queue<V> queue;
    private Map<V, ModifiableInteger> inDegreeMap;

    

    /**
     * Creates a new topological order iterator over the directed graph
     * specified, with arbitrary tie-breaking in case of partial order.
     * Traversal will start at one of the graph's <i>sources</i>. See the
     * definition of source at <a
     * href="http://mathworld.wolfram.com/Source.html">
     * http://mathworld.wolfram.com/Source.html</a>.
     *
     * @param dg the directed graph to be iterated.
     */
    public TopologicalOrderIterator(DirectedGraph<V, E> dg)
    {
        this(dg, new LinkedListQueue<V>());
    }

    /**
     * Creates a new topological order iterator over the directed graph
     * specified, with a user-supplied queue implementation to allow customized
     * control over tie-breaking in case of partial order. Traversal will start
     * at one of the graph's <i>sources</i>. See the definition of source at <a
     * href="http://mathworld.wolfram.com/Source.html">
     * http://mathworld.wolfram.com/Source.html</a>.
     *
     * @param dg the directed graph to be iterated.
     * @param queue queue to use for tie-break in case of partial order (e.g. a
     * PriorityQueue can be used to break ties according to vertex priority);
     * must be initially empty
     */
    public TopologicalOrderIterator(DirectedGraph<V, E> dg, Queue<V> queue)
    {
        this(dg, queue, new HashMap<V, ModifiableInteger>());
    }

    // NOTE: This is a hack to deal with the fact that CrossComponentIterator
    // needs to know the start vertex in its constructor
    private TopologicalOrderIterator(
        DirectedGraph<V, E> dg,
        Queue<V> queue,
        Map<V, ModifiableInteger> inDegreeMap)
    {
        this(dg, initialize(dg, queue, inDegreeMap));
        this.queue = queue;
        this.inDegreeMap = inDegreeMap;

        // empty queue for non-empty graph would indicate presence of
        // cycles (no roots found)
        assert dg.vertexSet().isEmpty() || !queue.isEmpty();
    }

    // NOTE: This is intentionally private, because starting the sort "in the
    // middle" doesn't make sense.
    private TopologicalOrderIterator(DirectedGraph<V, E> dg, V start)
    {
        super(dg, start);
    }

    

    /**
     * @see CrossComponentIterator#isConnectedComponentExhausted()
     */
    protected boolean isConnectedComponentExhausted()
    {
        // FIXME jvs 25-Apr-2005: This isn't correct for a graph with more than
        // one component.  We will actually exhaust a connected component
        // before the queue is empty, because initialize adds roots from all
        // components to the queue.
        return queue.isEmpty();
    }

    /**
     * @see CrossComponentIterator#encounterVertex(Object, Object)
     */
    protected void encounterVertex(V vertex, E edge)
    {
        putSeenData(vertex, null);
        decrementInDegree(vertex);
    }

    /**
     * @see CrossComponentIterator#encounterVertexAgain(Object, Object)
     */
    protected void encounterVertexAgain(V vertex, E edge)
    {
        decrementInDegree(vertex);
    }

    /**
     * @see CrossComponentIterator#provideNextVertex()
     */
    protected V provideNextVertex()
    {
        return queue.remove();
    }

    /**
     * Decrements the in-degree of a vertex.
     *
     * @param vertex the vertex whose in-degree will be decremented.
     */
    private void decrementInDegree(V vertex)
    {
        ModifiableInteger inDegree = inDegreeMap.get(vertex);

        if (inDegree.value > 0) {
            inDegree.value--;

            if (inDegree.value == 0) {
                queue.offer(vertex);
            }
        }
    }

    /**
     * Initializes the internal traversal object structure. Sets up the internal
     * queue with the directed graph vertices and creates the control structure
     * for the in-degrees.
     *
     * @param dg the directed graph to be iterated.
     * @param queue initializer for queue
     * @param inDegreeMap initializer for inDegreeMap
     *
     * @return start vertex
     */
    private static <V, E> V initialize(
        DirectedGraph<V, E> dg,
        Queue<V> queue,
        Map<V, ModifiableInteger> inDegreeMap)
    {
        for (Iterator<V> i = dg.vertexSet().iterator(); i.hasNext();) {
            V vertex = i.next();

            int inDegree = dg.inDegreeOf(vertex);
            inDegreeMap.put(vertex, new ModifiableInteger(inDegree));

            if (inDegree == 0) {
                queue.offer(vertex);
            }
        }

        if (queue.isEmpty()) {
            return null;
        } else {
            return queue.peek();
        }
    }

    

    // NOTE jvs 22-Dec-2006:  For JDK1.4-compatibility, we can't assume
    // that LinkedList implements Queue, since that wasn't introduced
    // until JDK1.5, so use an adapter here.  Move this to
    // top-level in org.jgrapht.util if anyone else needs it.
    private static class LinkedListQueue<T>
        extends LinkedList<T>
        implements Queue<T>
    {
        private static final long serialVersionUID = 4217659843476891334L;

        public T element()
        {
            return getFirst();
        }

        public boolean offer(T o)
        {
            return add(o);
        }

        public T peek()
        {
            if (isEmpty()) {
                return null;
            }
            return getFirst();
        }

        public T poll()
        {
            if (isEmpty()) {
                return null;
            }
            return removeFirst();
        }

        public T remove()
        {
            return removeFirst();
        }
    }
}

// End TopologicalOrderIterator.java
