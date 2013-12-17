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
 * MaskFunctor.java
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

/**
 * A functor interface for masking out vertices and edges of a graph.
 *
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
public interface MaskFunctor<V, E>
{
    

    /**
     * Returns <code>true</code> if the edge is masked, <code>false</code>
     * otherwise.
     *
     * @param edge edge.
     *
     * @return .
     */
    public boolean isEdgeMasked(E edge);

    /**
     * Returns <code>true</code> if the vertex is masked, <code>false</code>
     * otherwise.
     *
     * @param vertex vertex.
     *
     * @return .
     */
    public boolean isVertexMasked(V vertex);
}

// End MaskFunctor.java
