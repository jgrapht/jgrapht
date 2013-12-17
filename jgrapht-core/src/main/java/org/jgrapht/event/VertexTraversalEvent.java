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
/* -------------------------
 * VertexTraversalEvent.java
 * -------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 11-Aug-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org.jgrapht.event;

import java.util.*;


/**
 * A traversal event for a graph vertex.
 *
 * @author Barak Naveh
 * @since Aug 11, 2003
 */
public class VertexTraversalEvent<V>
    extends EventObject
{
    

    private static final long serialVersionUID = 3688790267213918768L;

    

    /**
     * The traversed vertex.
     */
    protected V vertex;

    

    /**
     * Creates a new VertexTraversalEvent.
     *
     * @param eventSource the source of the event.
     * @param vertex the traversed vertex.
     */
    public VertexTraversalEvent(Object eventSource, V vertex)
    {
        super(eventSource);
        this.vertex = vertex;
    }

    

    /**
     * Returns the traversed vertex.
     *
     * @return the traversed vertex.
     */
    public V getVertex()
    {
        return vertex;
    }
}

// End VertexTraversalEvent.java
