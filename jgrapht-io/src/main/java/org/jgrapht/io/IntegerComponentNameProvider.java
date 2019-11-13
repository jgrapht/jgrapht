/*
 * (C) Copyright 2005-2017, by Trevor Harmon and Contributors.
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
package org.jgrapht.io;

import java.util.*;

/**
 * Assigns a unique integer to represent each component. Each instance of provider maintains an
 * internal map between every component it has ever seen and the unique integer representing that
 * edge. As a result it is probably desirable to have a separate instance for each distinct graph.
 * 
 * @param <T> the component type
 *
 * @author Trevor Harmon
 * @author Amr ALHOSSARY
 */
public class IntegerComponentNameProvider<T>
    implements
    ComponentNameProvider<T>
{
    private static final int DEFAULT_BASE = 1;

    /**
     * The first ID to use.
     */
    private int base;
    private int nextID;
    private final Map<T, Integer> idMap = new HashMap<>();

    /**
     * Create a provider with the default base id (1).
     */
    public IntegerComponentNameProvider()
    {
        this(DEFAULT_BASE);
    }

    /**
     * Create a provider with a given arbitrary base
     * 
     * @param base the first Id to use.
     */
    public IntegerComponentNameProvider(int base)
    {
        this.base = base;
        clear();
    }

    /**
     * Clears all cached identifiers, and resets the unique identifier counter.
     */
    public void clear()
    {
        nextID = base;
        idMap.clear();
    }

    /**
     * Returns the string representation of a component.
     *
     * @param component the component to be named
     */
    @Override
    public String getName(T component)
    {
        Integer id = idMap.get(component);
        if (id == null) {
            id = nextID++;
            idMap.put(component, id);
        }
        return id.toString();
    }
}
