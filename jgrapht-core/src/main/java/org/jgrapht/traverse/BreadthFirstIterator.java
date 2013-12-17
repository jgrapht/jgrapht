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
/* -------------------------
 * BreadthFirstIterator.java
 * -------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Liviu Rau
 *                   Christian Hammer
 *                   Ross Judson
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 06-Aug-2003 : Extracted common logic to TraverseUtils.XXFirstIterator (BN);
 * 31-Jan-2004 : Reparented and changed interface to parent class (BN);
 * 28-Sep-2008 : Optimized using ArrayDeque per suggestion from Ross (JVS)
 *
 */
package org.jgrapht.traverse;

import java.util.*;

import org.jgrapht.*;


/**
 * A breadth-first iterator for a directed and an undirected graph. For this
 * iterator to work correctly the graph must not be modified during iteration.
 * Currently there are no means to ensure that, nor to fail-fast. The results of
 * such modifications are undefined.
 *
 * @author Barak Naveh
 * @since Jul 19, 2003
 */
public class BreadthFirstIterator<V, E>
    extends CrossComponentIterator<V, E, Object>
{
    

    private Deque<V> queue = new ArrayDeque<V>();

    

    /**
     * Creates a new breadth-first iterator for the specified graph.
     *
     * @param g the graph to be iterated.
     */
    public BreadthFirstIterator(Graph<V, E> g)
    {
        this(g, null);
    }

    /**
     * Creates a new breadth-first iterator for the specified graph. Iteration
     * will start at the specified start vertex and will be limited to the
     * connected component that includes that vertex. If the specified start
     * vertex is <code>null</code>, iteration will start at an arbitrary vertex
     * and will not be limited, that is, will be able to traverse all the graph.
     *
     * @param g the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     */
    public BreadthFirstIterator(Graph<V, E> g, V startVertex)
    {
        super(g, startVertex);
    }

    

    /**
     * @see CrossComponentIterator#isConnectedComponentExhausted()
     */
    protected boolean isConnectedComponentExhausted()
    {
        return queue.isEmpty();
    }

    /**
     * @see CrossComponentIterator#encounterVertex(Object, Object)
     */
    protected void encounterVertex(V vertex, E edge)
    {
        putSeenData(vertex, null);
        queue.add(vertex);
    }

    /**
     * @see CrossComponentIterator#encounterVertexAgain(Object, Object)
     */
    protected void encounterVertexAgain(V vertex, E edge)
    {
    }

    /**
     * @see CrossComponentIterator#provideNextVertex()
     */
    protected V provideNextVertex()
    {
        return queue.removeFirst();
    }
}

// End BreadthFirstIterator.java
