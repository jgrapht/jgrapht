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

import org.jgrapht.*;


/**
 * An undirected graph that cannot be modified.
 *
 * @see UnmodifiableGraph
 */
public class UnmodifiableUndirectedGraph<V, E>
    extends UnmodifiableGraph<V, E>
    implements UndirectedGraph<V, E>
{
    

    private static final long serialVersionUID = 3258134639355704624L;

    

    /**
     * Creates a new unmodifiable undirected graph based on the specified
     * backing graph.
     *
     * @param g the backing graph on which an unmodifiable graph is to be
     * created.
     */
    public UnmodifiableUndirectedGraph(UndirectedGraph<V, E> g)
    {
        super(g);
    }
}

// End UnmodifiableUndirectedGraph.java
