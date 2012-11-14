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
/* ------------------
 * VertexNameProvider.java
 * ------------------
 * (C) Copyright 2005-2008, by Avner Linder and Contributors.
 *
 * Original Author:  Avner Linder
 *
 * $Id$
 *
 * Changes
 * -------
 * 27-May-2004 : Initial Version (AL);
 * 13-Dec-2005 : Split out of VisioExporter (CF);
 *
 */
package org.jgrapht.ext;

/**
 * Assigns a display name for each of the graph vertices.
 */
public interface VertexNameProvider<V>
{
    //~ Methods ----------------------------------------------------------------

    /**
     * Returns a unique name for a vertex. This is useful when exporting a a
     * graph, as it ensures that all vertices are assigned simple, consistent
     * names.
     *
     * @param vertex the vertex to be named
     *
     * @return the name of the vertex
     */
    String getVertexName(V vertex);
}

// End VertexNameProvider.java
