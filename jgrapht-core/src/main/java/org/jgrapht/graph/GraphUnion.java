/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2009, by Barak Naveh and Contributors.
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
 * GraphUnion.java
 * -------------------------
 * (C) Copyright 2009-2009, by Ilya Razenshteyn
 *
 * Original Author:  Ilya Razenshteyn and Contributors.
 *
 * $Id$
 *
 * Changes
 * -------
 * 02-Feb-2009 : Initial revision (IR);
 *
 */
package org.jgrapht.graph;

import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.util.WeightCombiner;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * <p>Read-only union of two graphs: G<sub>1</sub> and G<sub>2</sub>. If
 * G<sub>1</sub> = (V<sub>1</sub>, E<sub>1</sub>) and G<sub>2</sub> =
 * (V<sub>2</sub>, E<sub>2</sub>) then their union G = (V, E), where V is the
 * union of V<sub>1</sub> and V<sub>2</sub>, and E is the union of E<sub>1</sub>
 * and E<sub>1</sub>.</p>
 *
 * <p><tt>GraphUnion</tt> implements <tt>Graph</tt> interface. <tt>
 * GraphUnion</tt> uses <tt>WeightCombiner</tt> to choose policy for calculating
 * edge weight.</p>
 */
public class GraphUnion<V, E, G extends Graph<V, E>>
    extends AbstractGraph<V, E>
    implements Serializable
{
    //~ Static fields/initializers ---------------------------------------------

    private static final long serialVersionUID = -740199233080172450L;

    private static final String READ_ONLY = "union of graphs is read-only";

    //~ Instance fields --------------------------------------------------------

    private final G g1;
    private final G g2;
    private final WeightCombiner operator;

    //~ Constructors -----------------------------------------------------------

    public GraphUnion(final G g1, final G g2, final WeightCombiner operator)
    {
        if (g1 == null) {
            throw new NullPointerException("g1 is null");
        }
        if (g2 == null) {
            throw new NullPointerException("g2 is null");
        }
        if (g1 == g2) {
            throw new IllegalArgumentException("g1 is equal to g2");
        }
        this.g1 = g1;
        this.g2 = g2;
        this.operator = operator;
    }

    public GraphUnion(final G g1, final G g2)
    {
        this(g1, g2, WeightCombiner.SUM);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Set<E> getAllEdges(final V sourceVertex, final V targetVertex)
    {
        final Set<E> res = new HashSet<E>();
        if (g1.containsVertex(sourceVertex)
            && g1.containsVertex(targetVertex))
        {
            res.addAll(g1.getAllEdges(sourceVertex, targetVertex));
        }
        if (g2.containsVertex(sourceVertex)
            && g2.containsVertex(targetVertex))
        {
            res.addAll(g2.getAllEdges(sourceVertex, targetVertex));
        }
        return Collections.unmodifiableSet(res);
    }

    @Override
    public E getEdge(final V sourceVertex, final V targetVertex)
    {
        E res = null;
        if (g1.containsVertex(sourceVertex)
            && g1.containsVertex(targetVertex))
        {
            res = g1.getEdge(sourceVertex, targetVertex);
        }
        if (res == null
            && g2.containsVertex(sourceVertex)
            && g2.containsVertex(targetVertex))
        {
            res = g2.getEdge(sourceVertex, targetVertex);
        }
        return res;
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>, because <tt>
     * GraphUnion</tt> is read-only.
     */
    @Override
    public EdgeFactory<V, E> getEdgeFactory()
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>, because <tt>
     * GraphUnion</tt> is read-only.
     */
    @Override
    public E addEdge(final V sourceVertex, final V targetVertex)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>, because <tt>
     * GraphUnion</tt> is read-only.
     */
    @Override
    public boolean addEdge(final V sourceVertex, final V targetVertex, final E e)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>, because <tt>
     * GraphUnion</tt> is read-only.
     */
    @Override
    public boolean addVertex(final V v)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    @Override
    public boolean containsEdge(final E e)
    {
        return g1.containsEdge(e) || g2.containsEdge(e);
    }

    @Override
    public boolean containsVertex(final V v)
    {
        return g1.containsVertex(v) || g2.containsVertex(v);
    }

    @Override
    public Set<E> edgeSet()
    {
        final Set<E> res = new HashSet<E>();
        res.addAll(g1.edgeSet());
        res.addAll(g2.edgeSet());
        return Collections.unmodifiableSet(res);
    }

    @Override
    public Set<E> edgesOf(final V vertex)
    {
        final Set<E> res = new HashSet<E>();
        if (g1.containsVertex(vertex)) {
            res.addAll(g1.edgesOf(vertex));
        }
        if (g2.containsVertex(vertex)) {
            res.addAll(g2.edgesOf(vertex));
        }
        return Collections.unmodifiableSet(res);
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>, because <tt>
     * GraphUnion</tt> is read-only.
     */
    @Override
    public E removeEdge(final V sourceVertex, final V targetVertex)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>, because <tt>
     * GraphUnion</tt> is read-only.
     */
    @Override
    public boolean removeEdge(final E e)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>, because <tt>
     * GraphUnion</tt> is read-only.
     */
    @Override
    public boolean removeVertex(final V v)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    @Override
    public Set<V> vertexSet()
    {
        final Set<V> res = new HashSet<V>();
        res.addAll(g1.vertexSet());
        res.addAll(g2.vertexSet());
        return Collections.unmodifiableSet(res);
    }

    @Override
    public V getEdgeSource(final E e)
    {
        if (g1.containsEdge(e)) {
            return g1.getEdgeSource(e);
        }
        if (g2.containsEdge(e)) {
            return g2.getEdgeSource(e);
        }
        return null;
    }

    @Override
    public V getEdgeTarget(final E e)
    {
        if (g1.containsEdge(e)) {
            return g1.getEdgeTarget(e);
        }
        if (g2.containsEdge(e)) {
            return g2.getEdgeTarget(e);
        }
        return null;
    }

    @Override
    public double getEdgeWeight(final E e)
    {
        if (g1.containsEdge(e) && g2.containsEdge(e)) {
            return operator.combine(g1.getEdgeWeight(e), g2.getEdgeWeight(e));
        }
        if (g1.containsEdge(e)) {
            return g1.getEdgeWeight(e);
        }
        if (g2.containsEdge(e)) {
            return g2.getEdgeWeight(e);
        }
        throw new IllegalArgumentException("no such edge in the union");
    }

    /**
     * @return G<sub>1</sub>
     */
    public G getG1()
    {
        return g1;
    }

    /**
     * @return G<sub>2</sub>
     */
    public G getG2()
    {
        return g2;
    }
}

// End GraphUnion.java
