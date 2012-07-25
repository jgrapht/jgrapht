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
/* -----------------
 * GraphMapping.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   John V. Sichi
 *
 * Changes
 * -------
 */
package org.jgrapht;

/**
 * GraphMapping represents a bidirectional mapping between two graphs (called
 * graph1 and graph2), which allows the caller to obtain the matching vertex or
 * edge in either direction, from graph1 to graph2, or from graph2 to graph1. It
 * does not have to always be a complete bidirectional mapping (it could return
 * null for some lookups).
 *
 * @author Assaf Lehr
 * @since Jul 30, 2005
 */
public interface GraphMapping<V, E>
{
    //~ Methods ----------------------------------------------------------------

    /**
     * Gets the mapped value where the key is <code>vertex</code>
     *
     * @param vertex vertex in one of the graphs
     * @param forward if true, uses mapping from graph1 to graph2; if false, use
     * mapping from graph2 to graph1
     *
     * @return corresponding vertex in other graph, or null if none
     */
    V getVertexCorrespondence(V vertex, boolean forward);

    /**
     * Gets the mapped value where the key is <code>edge</code>
     *
     * @param edge edge in one of the graphs
     * @param forward if true, uses mapping from graph1 to graph2; if false, use
     * mapping from graph2 to graph1
     *
     * @return corresponding edge in other graph, or null if none
     */
    E getEdgeCorrespondence(E edge, boolean forward);
}

// End GraphMapping.java
