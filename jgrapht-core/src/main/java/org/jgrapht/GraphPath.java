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

import java.util.List;


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
    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the graph over which this path is defined. The path may also be
     * valid with respect to other graphs.
     *
     * @return the containing graph
     */
    Graph<V, E> getGraph();

    /**
     * Returns the start vertex in the path.
     *
     * @return the start vertex
     */
    V getStartVertex();

    /**
     * Returns the end vertex in the path.
     *
     * @return the end vertex
     */
    V getEndVertex();

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
    List<E> getEdgeList();

    /**
     * Returns the weight assigned to the path. Typically, this will be the sum
     * of the weights of the edge list entries (as defined by the containing
     * graph), but some path implementations may use other definitions.
     *
     * @return the weight of the path
     */
    double getWeight();
}

// End GraphPath.java
