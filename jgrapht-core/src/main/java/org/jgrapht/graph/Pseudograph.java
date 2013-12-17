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
/* ----------------
 * Pseudograph.java
 * ----------------
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
 * 11-Mar-2004 : Made generic (CH);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import org.jgrapht.*;


/**
 * A pseudograph. A pseudograph is a non-simple undirected graph in which both
 * graph loops and multiple edges are permitted. If you're unsure about
 * pseudographs, see: <a href="http://mathworld.wolfram.com/Pseudograph.html">
 * http://mathworld.wolfram.com/Pseudograph.html</a>.
 */
public class Pseudograph<V, E>
    extends AbstractBaseGraph<V, E>
    implements UndirectedGraph<V, E>
{
    

    private static final long serialVersionUID = 3833183614484755253L;

    

    /**
     * Creates a new pseudograph.
     *
     * @param edgeClass class on which to base factory for edges
     */
    public Pseudograph(Class<? extends E> edgeClass)
    {
        this(new ClassBasedEdgeFactory<V, E>(edgeClass));
    }

    /**
     * Creates a new pseudograph with the specified edge factory.
     *
     * @param ef the edge factory of the new graph.
     */
    public Pseudograph(EdgeFactory<V, E> ef)
    {
        super(ef, true, true);
    }
}

// End Pseudograph.java
