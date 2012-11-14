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
    //~ Static fields/initializers ---------------------------------------------

    private static final long serialVersionUID = 3688790267213918768L;

    //~ Instance fields --------------------------------------------------------

    /**
     * The traversed vertex.
     */
    protected V vertex;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new VertexTraversalEvent.
     *
     * @param eventSource the source of the event.
     * @param vertex the traversed vertex.
     */
    public VertexTraversalEvent(final Object eventSource, final V vertex)
    {
        super(eventSource);
        this.vertex = vertex;
    }

    //~ Methods ----------------------------------------------------------------

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
