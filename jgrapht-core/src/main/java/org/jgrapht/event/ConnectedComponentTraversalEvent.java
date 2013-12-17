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
/* -------------------------------------
 * ConnectedComponentTraversalEvent.java
 * -------------------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id: ConnectedComponentTraversalEvent.java 487 2006-07-02 00:53:17Z
 * perfecthash $
 *
 * Changes
 * -------
 * 11-Aug-2003 : Initial revision (BN);
 *
 */
package org.jgrapht.event;

import java.util.*;


/**
 * A traversal event with respect to a connected component.
 *
 * @author Barak Naveh
 * @since Aug 11, 2003
 */
public class ConnectedComponentTraversalEvent
    extends EventObject
{
    

    private static final long serialVersionUID = 3834311717709822262L;

    /**
     * Connected component traversal started event.
     */
    public static final int CONNECTED_COMPONENT_STARTED = 31;

    /**
     * Connected component traversal finished event.
     */
    public static final int CONNECTED_COMPONENT_FINISHED = 32;

    

    /**
     * The type of this event.
     */
    private int type;

    

    /**
     * Creates a new ConnectedComponentTraversalEvent.
     *
     * @param eventSource the source of the event.
     * @param type the type of event.
     */
    public ConnectedComponentTraversalEvent(Object eventSource, int type)
    {
        super(eventSource);
        this.type = type;
    }

    

    /**
     * Returns the event type.
     *
     * @return the event type.
     */
    public int getType()
    {
        return type;
    }
}

// End ConnectedComponentTraversalEvent.java
