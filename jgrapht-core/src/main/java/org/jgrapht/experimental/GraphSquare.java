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
/* ----------------------
 * GraphSquare.java
 * ----------------------
 * (C) Copyright 2004-2008, by Michael Behrisch and Contributors.
 *
 * Original Author:  Michael Behrisch
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 14-Sep-2004 : Initial revision (MB);
 *
 */
package org.jgrapht.experimental;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.event.*;
import org.jgrapht.graph.*;


/**
 * DOCUMENT ME!
 *
 * @author Michael Behrisch
 * @since Sep 14, 2004
 */
public class GraphSquare<V, E>
    extends AbstractBaseGraph<V, E>
{
    

    private static final long serialVersionUID = -2642034600395594304L;
    private static final String UNMODIFIABLE = "this graph is unmodifiable";

    

    /**
     * Constructor for GraphSquare.
     *
     * @param g the graph of which a square is to be created.
     * @param createLoops
     */
    public GraphSquare(final Graph<V, E> g, final boolean createLoops)
    {
        super(g.getEdgeFactory(), false, createLoops);
        Graphs.addAllVertices(this, g.vertexSet());
        addSquareEdges(g, createLoops);

        if (g instanceof ListenableGraph) {
            ((ListenableGraph<V, E>) g).addGraphListener(
                new GraphListener<V, E>() {
                    public void edgeAdded(GraphEdgeChangeEvent<V, E> e)
                    {
                        E edge = e.getEdge();
                        addEdgesStartingAt(
                            g,
                            g.getEdgeSource(edge),
                            g.getEdgeTarget(edge),
                            createLoops);
                        addEdgesStartingAt(
                            g,
                            g.getEdgeTarget(edge),
                            g.getEdgeSource(edge),
                            createLoops);
                    }

                    public void edgeRemoved(GraphEdgeChangeEvent<V, E> e)
                    { // this is not a very performant implementation
                        GraphSquare.super.removeAllEdges(edgeSet());
                        addSquareEdges(g, createLoops);
                    }

                    public void vertexAdded(GraphVertexChangeEvent<V> e)
                    {
                    }

                    public void vertexRemoved(GraphVertexChangeEvent<V> e)
                    {
                    }
                });
        }
    }

    

    /**
     * @see Graph#addEdge(Object, Object)
     */
    public E addEdge(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#addEdge(Object, Object, Object)
     */
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#addVertex(Object)
     */
    public boolean addVertex(V v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#removeAllEdges(Collection)
     */
    public boolean removeAllEdges(Collection<? extends E> edges)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#removeAllEdges(Object, Object)
     */
    public Set<E> removeAllEdges(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#removeAllVertices(Collection)
     */
    public boolean removeAllVertices(Collection<? extends V> vertices)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#removeEdge(Object)
     */
    public boolean removeEdge(E e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#removeEdge(Object, Object)
     */
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#removeVertex(Object)
     */
    public boolean removeVertex(V v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    private void addEdgesStartingAt(
        final Graph<V, E> g,
        final V v,
        final V u,
        boolean createLoops)
    {
        if (!g.containsEdge(v, u)) {
            return;
        }

        final List<V> adjVertices = Graphs.neighborListOf(g, u);

        for (int i = 0; i < adjVertices.size(); i++) {
            final V w = adjVertices.get(i);

            if (g.containsEdge(u, w) && ((v != w) || createLoops)) {
                super.addEdge(v, w);
            }
        }
    }

    private void addSquareEdges(Graph<V, E> g, boolean createLoops)
    {
        for (V v : g.vertexSet()) {
            List<V> adjVertices = Graphs.neighborListOf(g, v);

            for (int i = 0; i < adjVertices.size(); i++) {
                addEdgesStartingAt(g, v, adjVertices.get(i), createLoops);
            }
        }
    }
}

// End GraphSquare.java
