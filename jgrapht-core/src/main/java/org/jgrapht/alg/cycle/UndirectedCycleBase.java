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
 * UndirectedCycleBase.java
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

import java.util.*;

import org.jgrapht.*;


/**
 * A common interface for classes implementing algorithms for finding a cycle
 * base of an undirected graph.
 *
 * @param <V> the vertex type.
 * @param <E> the edge type.
 *
 * @author Nikolay Ognyanov
 */
public interface UndirectedCycleBase<V, E>
{
    

    /**
     * Returns the graph on which the cycle base search algorithm is executed by
     * this object.
     *
     * @return The graph.
     */
    UndirectedGraph<V, E> getGraph();

    /**
     * Sets the graph on which the cycle base search algorithm is executed by
     * this object.
     *
     * @param graph the graph.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    void setGraph(UndirectedGraph<V, E> graph);

    /**
     * Finds a cycle base of the graph.<br/>
     * Note that the full algorithm is executed on every call since the graph
     * may have changed between calls.
     *
     * @return A list of cycles constituting a cycle base for the graph.
     * Possibly empty but never <code>null</code>.
     *
     * @throws IllegalArgumentException if the current graph is null.
     */
    List<List<V>> findCycleBase();
}

// End UndirectedCycleBase.java
