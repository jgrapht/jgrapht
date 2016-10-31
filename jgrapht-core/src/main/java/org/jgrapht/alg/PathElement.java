/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2016, by Barak Naveh and Contributors.
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
 * PathElement.java
 * -------------------------
 * (C) Copyright 2016-2016, by Assaf Mizrachi and Contributors.
 *
 * Original Author: Assaf Mizrachi.
 * Contributor(s):
 *
 * $Id$
 *
 * Changes
 * -------
 * 31-Oct-2016 : Initial revision (AM);
 *
 */
package org.jgrapht.alg;

import java.util.List;

import org.jgrapht.*;

/**
 * PathElement is a linked list of elements representing a graph path from a source vertex
 * to the current vertex. However, it can only traversed back (down to the source vertex)
 * but has no reference to the next element.
 * The first path element (empty path) includes only the source vertex and has
 * no previous element.
 * <p>
 * This is an internal interface used in path search algorithms to represent a current
 * state of the algorithm. It may be referenced externally (see {@link PathValidator})
 * but not be implemented. Use {@link GraphPath} interface to externally expose graph paths.
 * 
 * 
 * @author Assaf Mizrachi
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 */
public interface PathElement<V, E> {

    /**
     * Returns the path as a list of edges.
     *
     * @return list of <code>Edge</code>.
     */
	public List<E> createEdgeListPath();

    /**
     * Returns the number of hops of the path.
     *
     * @return the number of hops.
     */
    public int getHopCount();

    /**
     * Returns the edge reaching the current vertex of the path.
     *
     * @return the previous edge, or <code>null</code> if the path is empty.
     */
    public E getPrevEdge();

    /**
     * Returns the previous path element.
     *
     * @return the previous element, or <code>null</code> is the path is empty.
     */
    public AbstractPathElement<V, E> getPrevPathElement();

    /**
     * Returns the current vertex of the path.
     *
     * @return the current vertex.
     */
    public V getVertex();
}
