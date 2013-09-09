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
 * DirectedSimpleCycles.java
 * -------------------------
 * (C) Copyright 2013, by Nikolay Ognyanov
 *
 * Original Author: Nikolay Ognyanov
 * Contributor(s) :
 *
 * $Id$
 *
 * Changes
 * -------
 * 06-Sep-2013 : Initial revision (NO);
 */
package org.jgrapht.alg.cycle;

import java.util.List;

import org.jgrapht.DirectedGraph;

/**
 * A common interface for classes implementing algorithms
 * for enumeration of the simple cycles of a directed graph.
 *
 * @author Nikolay Ognyanov
 *
 * @param <V> the vertex type.
 * @param <E> the edge type.
 */
public interface DirectedSimpleCycles<V, E>
{
    /**
     * Returns the graph on which the simple cycle
     * search algorithm is executed by this object.
     *
     * @return The graph.
     */
    DirectedGraph<V, E> getGraph();

    /**
     * Sets the graph on which the simple cycle
     * search algorithm is executed by this object.
     *
     * @param graph the graph.
     * 
     * @throws IllegalArgumentException if the
     *         argument is <code>null</code>.
     */
    void setGraph(DirectedGraph<V, E> graph);

    /**
     * Finds the simple cycles of the graph.<br/>
     * Note that the full algorithm is executed on
     * every call since the graph may have changed
     * between calls.
     *
     * @return The list of all simple cycles.
     * Possibly empty but never <code>null</code>.
     * 
     * @throws IllegalArgumentException if the
     * current graph is null.
     */
    List<List<V>> findSimpleCycles();
}
