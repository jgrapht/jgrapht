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
 * DirectedMaskSubgraph.java
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


/**
 * A directed graph that is a {@link MaskSubgraph} on another graph.
 *
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
public class DirectedMaskSubgraph<V, E>
    extends MaskSubgraph<V, E>
    implements DirectedGraph<V, E>
{
    //~ Constructors -----------------------------------------------------------

    public DirectedMaskSubgraph(
        final DirectedGraph<V, E> base,
        final MaskFunctor<V, E> mask)
    {
        super(base, mask);
    }
}

// End DirectedMaskSubgraph.java
