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
/* -------------------
 * GraphDelegator.java
 * -------------------
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

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.WeightedGraph;

import java.io.Serializable;
import java.util.Set;


/**
 * A graph backed by the the graph specified at the constructor, which delegates
 * all its methods to the backing graph. Operations on this graph "pass through"
 * to the to the backing graph. Any modification made to this graph or the
 * backing graph is reflected by the other.
 *
 * <p>This graph does <i>not</i> pass the hashCode and equals operations through
 * to the backing graph, but relies on <tt>Object</tt>'s <tt>equals</tt> and
 * <tt>hashCode</tt> methods.</p>
 *
 * <p>This class is mostly used as a base for extending subclasses.</p>
 *
 * @author Barak Naveh
 * @since Jul 20, 2003
 */
public class GraphDelegator<V, E>
    extends AbstractGraph<V, E>
    implements Serializable
{
    //~ Static fields/initializers ---------------------------------------------

    private static final long serialVersionUID = 3257005445226181425L;

    //~ Instance fields --------------------------------------------------------

    /**
     * The graph to which operations are delegated.
     */
    private final Graph<V, E> delegate;

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor for GraphDelegator.
     *
     * @param g the backing graph (the delegate).
     *
     * @throws IllegalArgumentException iff <code>g==null</code>
     */
    public GraphDelegator(final Graph<V, E> g)
    {

        if (g == null) {
            throw new IllegalArgumentException("g must not be null.");
        }

        delegate = g;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @see Graph#getAllEdges(Object, Object)
     */
    @Override
    public Set<E> getAllEdges(final V sourceVertex, final V targetVertex)
    {
        return delegate.getAllEdges(sourceVertex, targetVertex);
    }

    /**
     * @see Graph#getEdge(Object, Object)
     */
    @Override
    public E getEdge(final V sourceVertex, final V targetVertex)
    {
        return delegate.getEdge(sourceVertex, targetVertex);
    }

    /**
     * @see Graph#getEdgeFactory()
     */
    @Override
    public EdgeFactory<V, E> getEdgeFactory()
    {
        return delegate.getEdgeFactory();
    }

    /**
     * @see Graph#addEdge(Object, Object)
     */
    @Override
    public E addEdge(final V sourceVertex, final V targetVertex)
    {
        return delegate.addEdge(sourceVertex, targetVertex);
    }

    /**
     * @see Graph#addEdge(Object, Object, Object)
     */
    @Override
    public boolean addEdge(final V sourceVertex, final V targetVertex, final E e)
    {
        return delegate.addEdge(sourceVertex, targetVertex, e);
    }

    /**
     * @see Graph#addVertex(Object)
     */
    @Override
    public boolean addVertex(final V v)
    {
        return delegate.addVertex(v);
    }

    /**
     * @see Graph#containsEdge(Object)
     */
    @Override
    public boolean containsEdge(final E e)
    {
        return delegate.containsEdge(e);
    }

    /**
     * @see Graph#containsVertex(Object)
     */
    @Override
    public boolean containsVertex(final V v)
    {
        return delegate.containsVertex(v);
    }

    /**
     * @see UndirectedGraph#degreeOf(Object)
     */
    public int degreeOf(final V vertex)
    {
        return ((UndirectedGraph<V, E>) delegate).degreeOf(vertex);
    }

    /**
     * @see Graph#edgeSet()
     */
    @Override
    public Set<E> edgeSet()
    {
        return delegate.edgeSet();
    }

    /**
     * @see Graph#edgesOf(Object)
     */
    @Override
    public Set<E> edgesOf(final V vertex)
    {
        return delegate.edgesOf(vertex);
    }

    /**
     * @see DirectedGraph#inDegreeOf(Object)
     */
    public int inDegreeOf(final V vertex)
    {
        return ((DirectedGraph<V, ? extends E>) delegate).inDegreeOf(vertex);
    }

    /**
     * @see DirectedGraph#incomingEdgesOf(Object)
     */
    public Set<E> incomingEdgesOf(final V vertex)
    {
        return ((DirectedGraph<V, E>) delegate).incomingEdgesOf(vertex);
    }

    /**
     * @see DirectedGraph#outDegreeOf(Object)
     */
    public int outDegreeOf(final V vertex)
    {
        return ((DirectedGraph<V, ? extends E>) delegate).outDegreeOf(vertex);
    }

    /**
     * @see DirectedGraph#outgoingEdgesOf(Object)
     */
    public Set<E> outgoingEdgesOf(final V vertex)
    {
        return ((DirectedGraph<V, E>) delegate).outgoingEdgesOf(vertex);
    }

    /**
     * @see Graph#removeEdge(Object)
     */
    @Override
    public boolean removeEdge(final E e)
    {
        return delegate.removeEdge(e);
    }

    /**
     * @see Graph#removeEdge(Object, Object)
     */
    @Override
    public E removeEdge(final V sourceVertex, final V targetVertex)
    {
        return delegate.removeEdge(sourceVertex, targetVertex);
    }

    /**
     * @see Graph#removeVertex(Object)
     */
    @Override
    public boolean removeVertex(final V v)
    {
        return delegate.removeVertex(v);
    }

    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return delegate.toString();
    }

    /**
     * @see Graph#vertexSet()
     */
    @Override
    public Set<V> vertexSet()
    {
        return delegate.vertexSet();
    }

    /**
     * @see Graph#getEdgeSource(Object)
     */
    @Override
    public V getEdgeSource(final E e)
    {
        return delegate.getEdgeSource(e);
    }

    /**
     * @see Graph#getEdgeTarget(Object)
     */
    @Override
    public V getEdgeTarget(final E e)
    {
        return delegate.getEdgeTarget(e);
    }

    /**
     * @see Graph#getEdgeWeight(Object)
     */
    @Override
    public double getEdgeWeight(final E e)
    {
        return delegate.getEdgeWeight(e);
    }

    /**
     * @see WeightedGraph#setEdgeWeight(Object, double)
     */
    public void setEdgeWeight(final E e, final double weight)
    {
        ((WeightedGraph<V, E>) delegate).setEdgeWeight(e, weight);
    }
}

// End GraphDelegator.java
