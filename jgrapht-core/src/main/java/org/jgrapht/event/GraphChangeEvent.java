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
/* ---------------------
 * GraphChangeEvent.java
 * ---------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 10-Aug-2003 : Initial revision (BN);
 *
 */
package org.jgrapht.event;

import java.util.*;


/**
 * An event which indicates that a graph has changed. This class is a root for
 * graph change events.
 *
 * @author Barak Naveh
 * @since Aug 10, 2003
 */
public class GraphChangeEvent
    extends EventObject
{
    

    private static final long serialVersionUID = 3834592106026382391L;

    

    /**
     * The type of graph change this event indicates.
     */
    protected int type;

    

    /**
     * Creates a new graph change event.
     *
     * @param eventSource the source of the event.
     * @param type the type of event.
     */
    public GraphChangeEvent(Object eventSource, int type)
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

// End GraphChangeEvent.java
