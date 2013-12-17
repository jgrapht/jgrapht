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
/* -------------------------------
 * DirectedWeightedMultigraph.java
 * -------------------------------
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
 * 06-Jun-2005 : Made generic (CH);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import org.jgrapht.*;


/**
 * A directed weighted multigraph. A directed weighted multigraph is a
 * non-simple directed graph in which loops and multiple edges between any two
 * vertices are permitted, and edges have weights.
 */
public class DirectedWeightedMultigraph<V, E>
    extends DirectedMultigraph<V, E>
    implements WeightedGraph<V, E>
{
    

    private static final long serialVersionUID = 4049071636005206066L;

    

    /**
     * Creates a new directed weighted multigraph.
     *
     * @param edgeClass class on which to base factory for edges
     */
    public DirectedWeightedMultigraph(Class<? extends E> edgeClass)
    {
        this(new ClassBasedEdgeFactory<V, E>(edgeClass));
    }

    /**
     * Creates a new directed weighted multigraph with the specified edge
     * factory.
     *
     * @param ef the edge factory of the new graph.
     */
    public DirectedWeightedMultigraph(EdgeFactory<V, E> ef)
    {
        super(ef);
    }
}

// End DirectedWeightedMultigraph.java
