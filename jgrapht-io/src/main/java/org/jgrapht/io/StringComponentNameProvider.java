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

/**
 * Generates names by invoking {@link #toString()} on them. This assumes that the object's
 * {@link #toString()} method returns a unique string representation.
 *
 * @param <T> the component type
 * 
 * @author Trevor Harmon
 */
public class StringComponentNameProvider<T>
    implements
    ComponentNameProvider<T>
{

    /**
     * Returns the string representation of a component.
     *
     * @param component the component
     * @return a unique string representation
     */
    @Override
    public String getName(T component)
    {
        return component.toString();
    }
}

// End StringComponentNameProvider.java
