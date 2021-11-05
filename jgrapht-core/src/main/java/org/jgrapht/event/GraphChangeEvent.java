/*
 * (C) Copyright 2003-2021, by Barak Naveh and Contributors.
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
 * An event which indicates that a graph has changed. This class is a root for graph change events.
 *
 * @author Barak Naveh
 */
public class GraphChangeEvent
    extends
    EventObject
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
