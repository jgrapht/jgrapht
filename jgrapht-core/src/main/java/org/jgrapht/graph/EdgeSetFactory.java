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
/* ----------------
 * EdgeSetFactory.java
 * ----------------
 * (C) Copyright 2005-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 01-Jun-2005 : Initial revision (JVS);
 * 06-Aug-2005 : Made generic (CH);
 * 07-May-2006 : Renamed and changed from List<Edge> to Set<Edge> (JVS);
 *
 */
package org.jgrapht.graph;

import java.util.*;


/**
 * A factory for edge sets. This interface allows the creator of a graph to
 * choose the {@link Set} implementation used internally by the graph
 * to maintain sets of edges. This provides control over performance tradeoffs
 * between memory and CPU usage.
 *
 * @author John V. Sichi
 */
public interface EdgeSetFactory<V, E>
{
    //~ Methods ----------------------------------------------------------------

    /**
     * Create a new edge set for a particular vertex.
     *
     * @param vertex the vertex for which the edge set is being created;
     * sophisticated factories may be able to use this information to choose an
     * optimal set representation.
     *
     * @return new set
     */
    Set<E> createEdgeSet(V vertex);
}

// End EdgeSetFactory.java
