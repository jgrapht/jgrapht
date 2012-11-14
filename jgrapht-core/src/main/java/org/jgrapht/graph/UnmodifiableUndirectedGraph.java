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
/* --------------------------------
 * UnmodifiableUndirectedGraph.java
 * --------------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Aug-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH)
 *
 */
package org.jgrapht.graph;

import org.jgrapht.UndirectedGraph;


/**
 * An undirected graph that cannot be modified.
 *
 * @see UnmodifiableGraph
 */
public class UnmodifiableUndirectedGraph<V, E>
    extends UnmodifiableGraph<V, E>
    implements UndirectedGraph<V, E>
{
    //~ Static fields/initializers ---------------------------------------------

    private static final long serialVersionUID = 3258134639355704624L;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new unmodifiable undirected graph based on the specified
     * backing graph.
     *
     * @param g the backing graph on which an unmodifiable graph is to be
     * created.
     */
    public UnmodifiableUndirectedGraph(final UndirectedGraph<V, E> g)
    {
        super(g);
    }
}

// End UnmodifiableUndirectedGraph.java
