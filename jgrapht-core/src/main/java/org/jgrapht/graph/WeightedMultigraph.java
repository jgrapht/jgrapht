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
/* -----------------------
 * WeightedMultigraph.java
 * -----------------------
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
 * 06-Aug-2005 : Made generic (CH);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import org.jgrapht.*;


/**
 * A weighted multigraph. A weighted multigraph is a non-simple undirected graph
 * in which no loops are permitted, but multiple edges between any two vertices
 * are. The edges of a weighted multigraph have weights. If you're unsure about
 * multigraphs, see: <a href="http://mathworld.wolfram.com/Multigraph.html">
 * http://mathworld.wolfram.com/Multigraph.html</a>.
 */
public class WeightedMultigraph<V, E>
    extends Multigraph<V, E>
    implements WeightedGraph<V, E>
{
    

    private static final long serialVersionUID = 3544671793370640696L;

    

    /**
     * Creates a new weighted multigraph with the specified edge factory.
     *
     * @param ef the edge factory of the new graph.
     */
    public WeightedMultigraph(EdgeFactory<V, E> ef)
    {
        super(ef);
    }

    /**
     * Creates a new weighted multigraph.
     *
     * @param edgeClass class on which to base factory for edges
     */
    public WeightedMultigraph(Class<? extends E> edgeClass)
    {
        this(new ClassBasedEdgeFactory<V, E>(edgeClass));
    }
}

// End WeightedMultigraph.java
