/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (barak_naveh@users.sourceforge.net)
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
 * DirectedPseudograph.java
 * ----------------
 * (C) Copyright 2004-2008, by Christian Hammer and Contributors.
 *
 * Original Author:  Christian Hammer
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 11-Mar-2004 : Initial revision: generic (CH);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import org.jgrapht.*;


/**
 * A directed pseudograph. A directed pseudograph is a non-simple directed graph
 * in which both graph loops and multiple edges are permitted. If you're unsure
 * about pseudographs, see: <a
 * href="http://mathworld.wolfram.com/Pseudograph.html">
 * http://mathworld.wolfram.com/Pseudograph.html</a>.
 */
public class DirectedPseudograph<V, E>
    extends AbstractBaseGraph<V, E>
    implements DirectedGraph<V, E>
{
    

    private static final long serialVersionUID = -8300409752893486415L;

    

    /**
     * @see AbstractBaseGraph
     */
    public DirectedPseudograph(Class<? extends E> edgeClass)
    {
        this(new ClassBasedEdgeFactory<V, E>(edgeClass));
    }

    /**
     * @see AbstractBaseGraph
     */
    public DirectedPseudograph(EdgeFactory<V, E> ef)
    {
        super(ef, true, true);
    }
}

// End DirectedPseudograph.java
