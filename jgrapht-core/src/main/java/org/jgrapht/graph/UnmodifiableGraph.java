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
 * UnmodifiableGraph.java
 * ----------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH);
 * 07-May-2006 : Changed from List<Edge> to Set<Edge> (JVS);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import java.io.*;

import java.util.*;

import org.jgrapht.*;


/**
 * An unmodifiable view of the backing graph specified in the constructor. This
 * graph allows modules to provide users with "read-only" access to internal
 * graphs. Query operations on this graph "read through" to the backing graph,
 * and attempts to modify this graph result in an <code>
 * UnsupportedOperationException</code>.
 *
 * <p>This graph does <i>not</i> pass the hashCode and equals operations through
 * to the backing graph, but relies on <tt>Object</tt>'s <tt>equals</tt> and
 * <tt>hashCode</tt> methods. This graph will be serializable if the backing
 * graph is serializable.</p>
 *
 * @author Barak Naveh
 * @since Jul 24, 2003
 */
public class UnmodifiableGraph<V, E>
    extends GraphDelegator<V, E>
    implements Serializable
{
    

    private static final long serialVersionUID = 3544957670722713913L;
    private static final String UNMODIFIABLE = "this graph is unmodifiable";

    

    /**
     * Creates a new unmodifiable graph based on the specified backing graph.
     *
     * @param g the backing graph on which an unmodifiable graph is to be
     * created.
     */
    public UnmodifiableGraph(Graph<V, E> g)
    {
        super(g);
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
}

// End UnmodifiableGraph.java
