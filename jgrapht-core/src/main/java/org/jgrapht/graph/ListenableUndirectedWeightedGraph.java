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
/* --------------------------------------
 * ListenableUndirectedWeightedGraph.java
 * --------------------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id: ListenableUndirectedWeightedGraph.java 485 2006-06-26 09:12:14Z
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
 * An undirected weighted graph which is also {@link
 * org.jgrapht.ListenableGraph}.
 *
 * @see DefaultListenableGraph
 */
public class ListenableUndirectedWeightedGraph<V, E>
    extends ListenableUndirectedGraph<V, E>
    implements WeightedGraph<V, E>
{
    

    private static final long serialVersionUID = 3690762799613949747L;

    

    /**
     * Creates a new listenable undirected weighted graph.
     *
     * @param edgeClass class on which to base factory for edges
     */
    public ListenableUndirectedWeightedGraph(Class<? extends E> edgeClass)
    {
        this(new SimpleWeightedGraph<V, E>(edgeClass));
    }

    /**
     * Creates a new listenable undirected weighted graph.
     *
     * @param base the backing graph.
     */
    public ListenableUndirectedWeightedGraph(WeightedGraph<V, E> base)
    {
        super((UndirectedGraph<V, E>) base);
    }
}

// End ListenableUndirectedWeightedGraph.java
