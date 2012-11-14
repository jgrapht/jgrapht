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
    //~ Methods ----------------------------------------------------------------

    /**
     * Returns <code>true</code> if the edge is masked, <code>false</code>
     * otherwise.
     *
     * @param edge edge.
     *
     * @return .
     */
    boolean isEdgeMasked(E edge);

    /**
     * Returns <code>true</code> if the vertex is masked, <code>false</code>
     * otherwise.
     *
     * @param vertex vertex.
     *
     * @return .
     */
    boolean isVertexMasked(V vertex);
}

// End MaskFunctor.java
