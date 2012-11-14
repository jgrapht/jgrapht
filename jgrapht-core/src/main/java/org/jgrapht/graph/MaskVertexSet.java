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
 * MaskVertexSet.java
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

import org.jgrapht.util.*;
import org.jgrapht.util.PrefetchIterator.*;


/**
 * Helper for {@link MaskSubgraph}.
 *
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
class MaskVertexSet<V, E>
    extends AbstractSet<V>
{
    //~ Instance fields --------------------------------------------------------

    private final MaskFunctor<V, E> mask;

    private int size;

    private final Set<V> vertexSet;

    private final transient TypeUtil<V> vertexTypeDecl = null;

    //~ Constructors -----------------------------------------------------------

    public MaskVertexSet(final Set<V> vertexSet, final MaskFunctor<V, E> mask)
    {
        this.vertexSet = vertexSet;
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
        return
            !mask.isVertexMasked(TypeUtil.uncheckedCast(o, vertexTypeDecl))
            && vertexSet.contains(o);
    }

    /**
     * @see Set#iterator()
     */
    @Override
    public Iterator<V> iterator()
    {
        return new PrefetchIterator<V>(new MaskVertexSetNextElementFunctor());
    }

    /**
     * @see Set#size()
     */
    @Override
    public int size()
    {
        if (size == -1) {
            size = 0;
            for (final V v : this) {
                size++;
            }
        }
        return size;
    }

    //~ Inner Classes ----------------------------------------------------------

    private class MaskVertexSetNextElementFunctor
        implements NextElementFunctor<V>
    {
        private final Iterator<V> iter;

        public MaskVertexSetNextElementFunctor()
        {
            iter = vertexSet.iterator();
        }

        @Override
        public V nextElement()
            throws NoSuchElementException
        {
            V element = iter.next();
            while (mask.isVertexMasked(element)) {
                element = iter.next();
            }
            return element;
        }
    }
}

// End MaskVertexSet.java
