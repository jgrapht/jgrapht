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
 * MaskEdgeSet.java
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

import java.lang.Object;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.util.*;
import org.jgrapht.util.PrefetchIterator.*;


/**
 * Helper for {@link MaskSubgraph}.
 *
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
class MaskEdgeSet<V, E>
    extends AbstractSet<E>
{
    //~ Instance fields --------------------------------------------------------

    private final Set<E> edgeSet;

    private final Graph<V, E> graph;

    private final MaskFunctor<V, E> mask;

    private final transient TypeUtil<E> edgeTypeDecl = null;

    private int size;

    //~ Constructors -----------------------------------------------------------

    public MaskEdgeSet(
        final Graph<V, E> graph,
        final Set<E> edgeSet,
        final MaskFunctor<V, E> mask)
    {
        this.graph = graph;
        this.edgeSet = edgeSet;
        this.mask = mask;
        size = -1;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @see Collection#contains(Object)
     */
    @Override
    public boolean contains(final Object o)
    {
        return edgeSet.contains(o)
            && !mask.isEdgeMasked(TypeUtil.uncheckedCast(o, edgeTypeDecl));
    }

    /**
     * @see Set#iterator()
     */
    @Override
    public Iterator<E> iterator()
    {
        return new PrefetchIterator<E>(new MaskEdgeSetNextElementFunctor());
    }

    /**
     * @see Set#size()
     */
    @Override
    public int size()
    {
        if (size == -1) {
            size = 0;
            for (final E e : this) {
                size++;
            }
        }
        return size;
    }

    //~ Inner Classes ----------------------------------------------------------

    private class MaskEdgeSetNextElementFunctor
        implements NextElementFunctor<E>
    {
        private final Iterator<E> iter;

        public MaskEdgeSetNextElementFunctor()
        {
            iter = edgeSet.iterator();
        }

        @Override
        public E nextElement()
            throws NoSuchElementException
        {
            E edge = iter.next();
            while (isMasked(edge)) {
                edge = iter.next();
            }
            return edge;
        }

        private boolean isMasked(final E edge)
        {
            return mask.isEdgeMasked(edge)
                || mask.isVertexMasked(graph.getEdgeSource(edge))
                || mask.isVertexMasked(graph.getEdgeTarget(edge));
        }
    }
}

// End MaskEdgeSet.java
