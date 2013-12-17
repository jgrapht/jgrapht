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
    

    private MaskFunctor<V, E> mask;

    private int size;

    private Set<V> vertexSet;

    private transient TypeUtil<V> vertexTypeDecl = null;

    

    public MaskVertexSet(Set<V> vertexSet, MaskFunctor<V, E> mask)
    {
        this.vertexSet = vertexSet;
        this.mask = mask;
        this.size = -1;
    }

    

    /**
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object o)
    {
        return
            !this.mask.isVertexMasked(TypeUtil.uncheckedCast(o, vertexTypeDecl))
            && this.vertexSet.contains(o);
    }

    /**
     * @see java.util.Set#iterator()
     */
    public Iterator<V> iterator()
    {
        return new PrefetchIterator<V>(new MaskVertexSetNextElementFunctor());
    }

    /**
     * @see java.util.Set#size()
     */
    public int size()
    {
        if (this.size == -1) {
            this.size = 0;
            for (Iterator<V> iter = iterator(); iter.hasNext();) {
                iter.next();
                this.size++;
            }
        }
        return this.size;
    }

    

    private class MaskVertexSetNextElementFunctor
        implements NextElementFunctor<V>
    {
        private Iterator<V> iter;

        public MaskVertexSetNextElementFunctor()
        {
            this.iter = MaskVertexSet.this.vertexSet.iterator();
        }

        public V nextElement()
            throws NoSuchElementException
        {
            V element = this.iter.next();
            while (MaskVertexSet.this.mask.isVertexMasked(element)) {
                element = this.iter.next();
            }
            return element;
        }
    }
}

// End MaskVertexSet.java
