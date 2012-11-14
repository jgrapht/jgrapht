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
/* -------------------------
 * MaskSubgraph.java
 * -------------------------
 * (C) Copyright 2007-2008, by France Telecom
 *
 * Original Author:  Guillaume Boulmier and Contributors.
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Jun-2007 : Initial revision (GB);
 *
 */
package org.jgrapht.graph;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;

import java.util.Collection;
import java.util.Set;


/**
 * An unmodifiable subgraph induced by a vertex/edge masking function. The
 * subgraph will keep track of edges being added to its vertex subset as well as
 * deletion of edges and vertices. When iterating over the vertices/edges, it
 * will iterate over the vertices/edges of the base graph and discard
 * vertices/edges that are masked (an edge with a masked extremity vertex is
 * discarded as well).
 *
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
public class MaskSubgraph<V, E>
    extends AbstractGraph<V, E>
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String UNMODIFIABLE = "this graph is unmodifiable";

    //~ Instance fields --------------------------------------------------------

    private final Graph<V, E> base;

    private final Set<E> edges;

    private final MaskFunctor<V, E> mask;

    private final Set<V> vertices;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new induced subgraph. Running-time = O(1).
     *
     * @param base the base (backing) graph on which the subgraph will be based.
     * @param mask vertices and edges to exclude in the subgraph. If a
     * vertex/edge is masked, it is as if it is not in the subgraph.
     */
    public MaskSubgraph(final Graph<V, E> base, final MaskFunctor<V, E> mask)
    {
        this.base = base;
        this.mask = mask;

        vertices = new MaskVertexSet<V, E>(base.vertexSet(), mask);
        edges = new MaskEdgeSet<V, E>(base, base.edgeSet(), mask);
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

    @Override
    public boolean addEdge(final V sourceVertex, final V targetVertex, final E edge)
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

    @Override
    public boolean containsEdge(final E e)
    {
        return edgeSet().contains(e);
    }

    @Override
    public boolean containsVertex(final V v)
    {
        return !mask.isVertexMasked(v) && base.containsVertex(v);
    }

    /**
     * @see UndirectedGraph#degreeOf(Object)
     */
    public int degreeOf(final V vertex)
    {
        return edgesOf(vertex).size();
    }

    @Override
    public Set<E> edgeSet()
    {
        return edges;
    }

    @Override
    public Set<E> edgesOf(final V vertex)
    {
        assertVertexExist(vertex);

        return new MaskEdgeSet<V, E>(base, base.edgesOf(vertex), mask);
    }

    @Override
    public Set<E> getAllEdges(final V sourceVertex, final V targetVertex)
    {
        final Set<E> edges = null;

        if (containsVertex(sourceVertex) && containsVertex(targetVertex)) {
            return new MaskEdgeSet<V, E>(base,
                base.getAllEdges(sourceVertex, targetVertex), mask);
        }

        return edges;
    }

    @Override
    public E getEdge(final V sourceVertex, final V targetVertex)
    {
        final Set<E> edges = getAllEdges(sourceVertex, targetVertex);

        if (edges == null || edges.isEmpty()) {
            return null;
        } else {
            return edges.iterator().next();
        }
    }

    @Override
    public EdgeFactory<V, E> getEdgeFactory()
    {
        return base.getEdgeFactory();
    }

    @Override
    public V getEdgeSource(final E edge)
    {
        assert edgeSet().contains(edge);

        return base.getEdgeSource(edge);
    }

    @Override
    public V getEdgeTarget(final E edge)
    {
        assert edgeSet().contains(edge);

        return base.getEdgeTarget(edge);
    }

    @Override
    public double getEdgeWeight(final E edge)
    {
        assert edgeSet().contains(edge);

        return base.getEdgeWeight(edge);
    }

    /**
     * @see DirectedGraph#incomingEdgesOf(Object)
     */
    public Set<E> incomingEdgesOf(final V vertex)
    {
        assertVertexExist(vertex);

        return new MaskEdgeSet<V, E>(base,
            ((DirectedGraph<V, E>) base).incomingEdgesOf(vertex), mask);
    }

    /**
     * @see DirectedGraph#inDegreeOf(Object)
     */
    public int inDegreeOf(final V vertex)
    {
        return incomingEdgesOf(vertex).size();
    }

    /**
     * @see DirectedGraph#outDegreeOf(Object)
     */
    public int outDegreeOf(final V vertex)
    {
        return outgoingEdgesOf(vertex).size();
    }

    /**
     * @see DirectedGraph#outgoingEdgesOf(Object)
     */
    public Set<E> outgoingEdgesOf(final V vertex)
    {
        assertVertexExist(vertex);

        return new MaskEdgeSet<V, E>(base,
            ((DirectedGraph<V, E>) base).outgoingEdgesOf(vertex), mask);
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

    @Override
    public Set<V> vertexSet()
    {
        return vertices;
    }
}

// End MaskSubgraph.java
