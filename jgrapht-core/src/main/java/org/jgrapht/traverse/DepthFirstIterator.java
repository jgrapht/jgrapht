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
/* -----------------------
 * DepthFirstIterator.java
 * -----------------------
 * (C) Copyright 2003-2008, by Liviu Rau and Contributors.
 *
 * Original Author:  Liviu Rau
 * Contributor(s):   Barak Naveh
 *                   Christian Hammer
 *                   Welson Sun
 *                   Ross Judson
 *
 * $Id$
 *
 * Changes
 * -------
 * 29-Jul-2003 : Initial revision (LR);
 * 31-Jul-2003 : Fixed traversal across connected components (BN);
 * 06-Aug-2003 : Extracted common logic to TraverseUtils.XXFirstIterator (BN);
 * 31-Jan-2004 : Reparented and changed interface to parent class (BN);
 * 04-May-2004 : Made generic (CH)
 * 27-Aug-2006 : Added WHITE/GRAY/BLACK to fix bug reported by Welson Sun (JVS)
 * 28-Sep-2008 : Optimized using ArrayDeque per suggestion from Ross (JVS)
 *
 */
package org.jgrapht.traverse;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.util.*;


/**
 * A depth-first iterator for a directed and an undirected graph. For this
 * iterator to work correctly the graph must not be modified during iteration.
 * Currently there are no means to ensure that, nor to fail-fast. The results of
 * such modifications are undefined.
 *
 * @author Liviu Rau
 * @author Barak Naveh
 * @since Jul 29, 2003
 */
public class DepthFirstIterator<V, E>
    extends CrossComponentIterator<V, E, CrossComponentIterator.VisitColor>
{
    

    /**
     * Sentinel object. Unfortunately, we can't use null, because ArrayDeque
     * won't accept those. And we don't want to rely on the caller to provide a
     * sentinel object for us. So we have to play typecasting games.
     */
    public static final Object SENTINEL = new Object();

    

    /**
     * @see #getStack
     */
    private Deque<Object> stack = new ArrayDeque<Object>();

    private transient TypeUtil<V> vertexTypeDecl = null;

    

    /**
     * Creates a new depth-first iterator for the specified graph.
     *
     * @param g the graph to be iterated.
     */
    public DepthFirstIterator(Graph<V, E> g)
    {
        this(g, null);
    }

    /**
     * Creates a new depth-first iterator for the specified graph. Iteration
     * will start at the specified start vertex and will be limited to the
     * connected component that includes that vertex. If the specified start
     * vertex is <code>null</code>, iteration will start at an arbitrary vertex
     * and will not be limited, that is, will be able to traverse all the graph.
     *
     * @param g the graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     */
    public DepthFirstIterator(Graph<V, E> g, V startVertex)
    {
        super(g, startVertex);
    }

    

    /**
     * @see CrossComponentIterator#isConnectedComponentExhausted()
     */
    protected boolean isConnectedComponentExhausted()
    {
        for (;;) {
            if (stack.isEmpty()) {
                return true;
            }
            if (stack.getLast() != SENTINEL) {
                // Found a non-sentinel.
                return false;
            }

            // Found a sentinel:  pop it, record the finish time,
            // and then loop to check the rest of the stack.

            // Pop null we peeked at above.
            stack.removeLast();

            // This will pop corresponding vertex to be recorded as finished.
            recordFinish();
        }
    }

    /**
     * @see CrossComponentIterator#encounterVertex(Object, Object)
     */
    protected void encounterVertex(V vertex, E edge)
    {
        putSeenData(vertex, VisitColor.WHITE);
        stack.addLast(vertex);
    }

    /**
     * @see CrossComponentIterator#encounterVertexAgain(Object, Object)
     */
    protected void encounterVertexAgain(V vertex, E edge)
    {
        VisitColor color = getSeenData(vertex);
        if (color != VisitColor.WHITE) {
            // We've already visited this vertex; no need to mess with the
            // stack (either it's BLACK and not there at all, or it's GRAY
            // and therefore just a sentinel).
            return;
        }

        // Since we've encountered it before, and it's still WHITE, it
        // *must* be on the stack.  Use removeLastOccurrence on the
        // assumption that for typical topologies and traversals,
        // it's likely to be nearer the top of the stack than
        // the bottom of the stack.
        boolean found = stack.removeLastOccurrence(vertex);
        assert (found);
        stack.addLast(vertex);
    }

    /**
     * @see CrossComponentIterator#provideNextVertex()
     */
    protected V provideNextVertex()
    {
        V v;
        for (;;) {
            Object o = stack.removeLast();
            if (o == SENTINEL) {
                // This is a finish-time sentinel we previously pushed.
                recordFinish();
                // Now carry on with another pop until we find a non-sentinel
            } else {
                // Got a real vertex to start working on
                v = TypeUtil.uncheckedCast(o, vertexTypeDecl);
                break;
            }
        }

        // Push a sentinel for v onto the stack so that we'll know
        // when we're done with it.
        stack.addLast(v);
        stack.addLast(SENTINEL);
        putSeenData(v, VisitColor.GRAY);
        return v;
    }

    private void recordFinish()
    {
        V v = TypeUtil.uncheckedCast(stack.removeLast(), vertexTypeDecl);
        putSeenData(v, VisitColor.BLACK);
        finishVertex(v);
    }

    /**
     * Retrieves the LIFO stack of vertices which have been encountered but not
     * yet visited (WHITE). This stack also contains <em>sentinel</em> entries
     * representing vertices which have been visited but are still GRAY. A
     * sentinel entry is a sequence (v, SENTINEL), whereas a non-sentinel entry
     * is just (v).
     *
     * @return stack
     */
    public Deque<Object> getStack()
    {
        return stack;
    }
}

// End DepthFirstIterator.java
