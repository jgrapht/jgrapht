/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.graph;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Default implementation of an edge function which uses a map to store values.
 * 
 * @author Dimitrios Michail
 *
 * @param <E> the edge type
 * @param <T> the value type
 */
public class DefaultEdgeFunction<E, T>
    implements
    Function<E, T>,
    Serializable
{
    private static final long serialVersionUID = -4247429315268336855L;

    protected final Map<E, T> map;
    protected final T defaultValue;

    /**
     * Create a new function
     * 
     * @param defaultValue the default value
     */
    public DefaultEdgeFunction(T defaultValue)
    {
        this(defaultValue, new HashMap<>());
    }

    /**
     * Create a new function
     * 
     * @param defaultValue the default value
     * @param map the underlying map
     */
    public DefaultEdgeFunction(T defaultValue, Map<E, T> map)
    {
        this.defaultValue = Objects.requireNonNull(defaultValue, "Default value cannot be null");
        this.map = Objects.requireNonNull(map, "Map cannot be null");
    }

    /**
     * Get the function value for an edge.
     * 
     * @param e the edge
     */
    @Override
    public T apply(E e)
    {
        return map.getOrDefault(e, defaultValue);
    }

    /**
     * Get the function value for an edge.
     * 
     * @param e the edge
     * @return the function value for the edge
     */
    public T get(E e)
    {
        return map.getOrDefault(e, defaultValue);
    }

    /**
     * Set the function value for an edge.
     * 
     * @param e the edge
     * @param value the value
     */
    public void set(E e, T value)
    {
        map.put(e, value);
    }

}
