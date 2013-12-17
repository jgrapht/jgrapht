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
 * A directed weighted graph which is also {@link org.jgrapht.ListenableGraph}.
 *
 * @see DefaultListenableGraph
 */
public class ListenableDirectedWeightedGraph<V, E>
    extends ListenableDirectedGraph<V, E>
    implements WeightedGraph<V, E>
{
    

    private static final long serialVersionUID = 3977582476627621938L;

    

    /**
     * Creates a new listenable directed weighted graph.
     *
     * @param edgeClass class on which to base factory for edges
     */
    public ListenableDirectedWeightedGraph(Class<? extends E> edgeClass)
    {
        this(new DefaultDirectedWeightedGraph<V, E>(edgeClass));
    }

    /**
     * Creates a new listenable directed weighted graph.
     *
     * @param base the backing graph.
     */
    public ListenableDirectedWeightedGraph(WeightedGraph<V, E> base)
    {
        super((DirectedGraph<V, E>) base);
    }
}

// End ListenableDirectedWeightedGraph.java
