/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.event;

import java.util.*;

/**
 * A traversal event with respect to a connected component.
 *
 * @author Barak Naveh
 */
public class ConnectedComponentTraversalEvent
    extends
    EventObject
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
