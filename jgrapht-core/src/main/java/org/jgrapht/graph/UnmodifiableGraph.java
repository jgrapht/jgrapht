/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
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

import org.jgrapht.Graph;

import java.util.Collection;
import java.util.Set;


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
{
    //~ Static fields/initializers ---------------------------------------------

    private static final long serialVersionUID = 3544957670722713913L;
    private static final String UNMODIFIABLE = "this graph is unmodifiable";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new unmodifiable graph based on the specified backing graph.
     *
     * @param g the backing graph on which an unmodifiable graph is to be
     * created.
     */
    public UnmodifiableGraph(final Graph<V, E> g)
    {
        super(g);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @see Graph#addEdge(Object, Object)
     */
    @Override
    public E addEdge(final V sourceVertex, final V targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#addEdge(Object, Object, Object)
     */
    @Override
    public boolean addEdge(final V sourceVertex, final V targetVertex, final E e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#addVertex(Object)
     */
    @Override
    public boolean addVertex(final V v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#removeAllEdges(Collection)
     */
    @Override
    public boolean removeAllEdges(final Collection<? extends E> edges)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#removeAllEdges(Object, Object)
     */
    @Override
    public Set<E> removeAllEdges(final V sourceVertex, final V targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#removeAllVertices(Collection)
     */
    @Override
    public boolean removeAllVertices(final Collection<? extends V> vertices)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#removeEdge(Object)
     */
    @Override
    public boolean removeEdge(final E e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#removeEdge(Object, Object)
     */
    @Override
    public E removeEdge(final V sourceVertex, final V targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#removeVertex(Object)
     */
    @Override
    public boolean removeVertex(final V v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }
}

// End UnmodifiableGraph.java
