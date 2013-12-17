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
/* ----------
 * Graph.java
 * ----------
 * (C) Copyright 2008-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 1-Jan-2008 : Initial revision (JVS);
 *
 */
package org.jgrapht;

import java.util.*;


/**
 * A GraphPath represents a <a href="http://mathworld.wolfram.com/Path.html">
 * path</a> in a {@link Graph}. Note that a path is defined primarily in terms
 * of edges (rather than vertices) so that multiple edges between the same pair
 * of vertices can be discriminated.
 *
 * @author John Sichi
 * @since Jan 1, 2008
 */
public interface GraphPath<V, E>
{
    

    /**
     * Returns the graph over which this path is defined. The path may also be
     * valid with respect to other graphs.
     *
     * @return the containing graph
     */
    public Graph<V, E> getGraph();

    /**
     * Returns the start vertex in the path.
     *
     * @return the start vertex
     */
    public V getStartVertex();

    /**
     * Returns the end vertex in the path.
     *
     * @return the end vertex
     */
    public V getEndVertex();

    /**
     * Returns the edges making up the path. The first edge in this path is
     * incident to the start vertex. The last edge is incident to the end
     * vertex. The vertices along the path can be obtained by traversing from
     * the start vertex, finding its opposite across the first edge, and then
     * doing the same successively across subsequent edges; {@link
     * Graphs#getPathVertexList} provides a convenience method for this.
     *
     * <p>Whether or not the returned edge list is modifiable depends on the
     * path implementation.
     *
     * @return list of edges traversed by the path
     */
    public List<E> getEdgeList();

    /**
     * Returns the weight assigned to the path. Typically, this will be the sum
     * of the weights of the edge list entries (as defined by the containing
     * graph), but some path implementations may use other definitions.
     *
     * @return the weight of the path
     */
    public double getWeight();
}

// End GraphPath.java
