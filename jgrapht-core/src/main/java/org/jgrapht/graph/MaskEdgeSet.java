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
    

    private Set<E> edgeSet;

    private Graph<V, E> graph;

    private MaskFunctor<V, E> mask;

    private transient TypeUtil<E> edgeTypeDecl = null;

    private int size;

    

    public MaskEdgeSet(
        Graph<V, E> graph,
        Set<E> edgeSet,
        MaskFunctor<V, E> mask)
    {
        this.graph = graph;
        this.edgeSet = edgeSet;
        this.mask = mask;
        this.size = -1;
    }

    

    /**
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object o)
    {
        return this.edgeSet.contains(o)
            && !this.mask.isEdgeMasked(TypeUtil.uncheckedCast(o, edgeTypeDecl));
    }

    /**
     * @see java.util.Set#iterator()
     */
    public Iterator<E> iterator()
    {
        return new PrefetchIterator<E>(new MaskEdgeSetNextElementFunctor());
    }

    /**
     * @see java.util.Set#size()
     */
    public int size()
    {
        if (this.size == -1) {
            this.size = 0;
            for (Iterator<E> iter = iterator(); iter.hasNext();) {
                iter.next();
                this.size++;
            }
        }
        return this.size;
    }

    

    private class MaskEdgeSetNextElementFunctor
        implements NextElementFunctor<E>
    {
        private Iterator<E> iter;

        public MaskEdgeSetNextElementFunctor()
        {
            this.iter = MaskEdgeSet.this.edgeSet.iterator();
        }

        public E nextElement()
            throws NoSuchElementException
        {
            E edge = this.iter.next();
            while (isMasked(edge)) {
                edge = this.iter.next();
            }
            return edge;
        }

        private boolean isMasked(E edge)
        {
            return MaskEdgeSet.this.mask.isEdgeMasked(edge)
                || MaskEdgeSet.this.mask.isVertexMasked(
                    MaskEdgeSet.this.graph.getEdgeSource(edge))
                || MaskEdgeSet.this.mask.isVertexMasked(
                    MaskEdgeSet.this.graph.getEdgeTarget(edge));
        }
    }
}

// End MaskEdgeSet.java
