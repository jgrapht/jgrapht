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

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.ListenableGraph;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.graph.AbstractBaseGraph;

import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * DOCUMENT ME!
 *
 * @author Michael Behrisch
 * @since Sep 14, 2004
 */
public class GraphSquare<V, E>
    extends AbstractBaseGraph<V, E>
{
    //~ Static fields/initializers ---------------------------------------------

    private static final long serialVersionUID = -2642034600395594304L;
    private static final String UNMODIFIABLE = "this graph is unmodifiable";

    //~ Constructors -----------------------------------------------------------

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
                    @Override
                    public void edgeAdded(final GraphEdgeChangeEvent<V, E> e)
                    {
                        final E edge = e.getEdge();
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

                    @Override
                    public void edgeRemoved(final GraphEdgeChangeEvent<V, E> e)
                    { // this is not a very performant implementation
                        GraphSquare.super.removeAllEdges(edgeSet());
                        addSquareEdges(g, createLoops);
                    }

                    @Override
                    public void vertexAdded(final GraphVertexChangeEvent<V> e)
                    {
                    }

                    @Override
                    public void vertexRemoved(final GraphVertexChangeEvent<V> e)
                    {
                    }
                });
        }
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
     * @see Graph#addEdge(Object, Object, E)
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
     * @see Graph#removeAllEdges(V, V)
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
     * @see Graph#removeEdge(E)
     */
    @Override
    public boolean removeEdge(final E e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#removeEdge(V, V)
     */
    @Override
    public E removeEdge(final V sourceVertex, final V targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * @see Graph#removeVertex(V)
     */
    @Override
    public boolean removeVertex(final V v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    private void addEdgesStartingAt(
        final Graph<V, E> g,
        final V v,
        final V u,
        final boolean createLoops)
    {
        if (!g.containsEdge(v, u)) {
            return;
        }

        final List<V> adjVertices = Graphs.neighborListOf(g, u);

        for (final V w : adjVertices) {
            if (g.containsEdge(u, w) && (v != w || createLoops)) {
                super.addEdge(v, w);
            }
        }
    }

    private void addSquareEdges(final Graph<V, E> g, final boolean createLoops)
    {
        for (final V v : g.vertexSet()) {
            final List<V> adjVertices = Graphs.neighborListOf(g, v);

            for (final V adjVertice : adjVertices) {
                addEdgesStartingAt(g, v, adjVertice, createLoops);
            }
        }
    }
}

// End GraphSquare.java
