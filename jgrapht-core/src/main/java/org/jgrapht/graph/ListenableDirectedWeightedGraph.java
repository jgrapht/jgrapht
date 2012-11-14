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
/* ------------------------------------
 * ListenableDirectedWeightedGraph.java
 * ------------------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id: ListenableDirectedWeightedGraph.java 485 2006-06-26 09:12:14Z
 * perfecthash $
 *
 * Changes
 * -------
 * 05-Aug-2003 : Initial revision (BN);
 * 06-Jun-2005 : Made generic (CH);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import org.jgrapht.*;


/**
 * A directed weighted graph which is also {@link ListenableGraph}.
 *
 * @see DefaultListenableGraph
 */
public class ListenableDirectedWeightedGraph<V, E>
    extends ListenableDirectedGraph<V, E>
    implements WeightedGraph<V, E>
{
    //~ Static fields/initializers ---------------------------------------------

    private static final long serialVersionUID = 3977582476627621938L;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new listenable directed weighted graph.
     *
     * @param edgeClass class on which to base factory for edges
     */
    public ListenableDirectedWeightedGraph(final Class<? extends E> edgeClass)
    {
        this(new DefaultDirectedWeightedGraph<V, E>(edgeClass));
    }

    /**
     * Creates a new listenable directed weighted graph.
     *
     * @param base the backing graph.
     */
    public ListenableDirectedWeightedGraph(final WeightedGraph<V, E> base)
    {
        super((DirectedGraph<V, E>) base);
    }
}

// End ListenableDirectedWeightedGraph.java
