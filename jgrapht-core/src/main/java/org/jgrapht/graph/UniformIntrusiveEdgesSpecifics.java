/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An uniform weights variant of the intrusive edges specifics.
 * 
 * <p>
 * The implementation optimizes the use of {@link DefaultEdge} and subclasses. For other custom user
 * edge types, a map is used to store vertex source and target.
 * 
 * @author Barak Naveh
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class UniformIntrusiveEdgesSpecifics<V, E>
    extends
    BaseIntrusiveEdgesSpecifics<V, E, IntrusiveEdge>
    implements
    IntrusiveEdgesSpecifics<V, E>
{
    private static final long serialVersionUID = -5736320893697031114L;

    /**
     * Constructor
     * 
     * @deprecated Since, default strategies should be decided at a higher level.
     */
    @Deprecated
    public UniformIntrusiveEdgesSpecifics()
    {
        this(new LinkedHashMap<>());
    }
    
    /**
     * Constructor
     * 
     * @param map the map to use for storage
     */
    public UniformIntrusiveEdgesSpecifics(Map<E, IntrusiveEdge> map)
    {
        super(map);
    }

    @Override
    public boolean add(E e, V sourceVertex, V targetVertex)
    {
        IntrusiveEdge intrusiveEdge;
        if (e instanceof IntrusiveEdge) {
            intrusiveEdge = (IntrusiveEdge) e;
        } else {
            intrusiveEdge = new IntrusiveEdge();
        }

        intrusiveEdge.source = sourceVertex;
        intrusiveEdge.target = targetVertex;

        return edgeMap.putIfAbsent(e, intrusiveEdge) == null;
    }

    @Override
    protected IntrusiveEdge getIntrusiveEdge(E e)
    {
        if (e instanceof IntrusiveEdge) {
            return (IntrusiveEdge) e;
        }
        return edgeMap.get(e);
    }
}
