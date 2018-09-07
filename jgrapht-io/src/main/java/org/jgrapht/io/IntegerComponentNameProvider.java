/*
 * (C) Copyright 2005-2017, by Trevor Harmon and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
 */
public class IntegerComponentNameProvider<T>
    implements
    ComponentNameProvider<T>
{
    private int nextID = 1;
    private final Map<T, Integer> idMap = new HashMap<>();

    /**
     * Clears all cached identifiers, and resets the unique identifier counter.
     */
    public void clear()
    {
        nextID = 1;
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

// End IntegerComponentNameProvider.java
