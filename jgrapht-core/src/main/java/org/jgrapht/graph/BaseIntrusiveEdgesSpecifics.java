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

import java.io.*;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.util.*;

/**
 * A base implementation for the intrusive edges specifics.
 * 
 * @author Barak Naveh
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @param <IE> the intrusive edge type
 */
public abstract class BaseIntrusiveEdgesSpecifics<V, E, IE extends IntrusiveEdge>
    implements
    Serializable
{
    private static final long serialVersionUID = -7498268216742485L;

    protected Map<E, IE> edgeMap;
    protected transient Set<E> unmodifiableEdgeSet = null;

    /**
     * Constructor
     */
    public BaseIntrusiveEdgesSpecifics()
    {
        this.edgeMap = new LinkedHashMap<>();
    }

    /**
     * Check if an edge exists
     * 
     * @param e the edge
     * @return true if the edge exists, false otherwise
     */
    public boolean containsEdge(E e)
    {
        return edgeMap.containsKey(e);
    }

    /**
     * Get the edge set.
     * 
     * @return an unmodifiable edge set
     */
    public Set<E> getEdgeSet()
    {
        if (unmodifiableEdgeSet == null) {
            unmodifiableEdgeSet = Collections.unmodifiableSet(edgeMap.keySet());
        }
        return unmodifiableEdgeSet;
    }

    /**
     * Remove an edge.
     * 
     * @param e the edge
     */
    public void remove(E e)
    {
        edgeMap.remove(e);
    }

    /**
     * Get the source of an edge.
     * 
     * @param e the edge
     * @return the source vertex of an edge
     */
    public V getEdgeSource(E e)
    {
        IntrusiveEdge ie = getIntrusiveEdge(e);
        if (ie == null) {
            throw new IllegalArgumentException("no such edge in graph: " + e.toString());
        }
        return TypeUtil.uncheckedCast(ie.source);
    }

    /**
     * Get the target of an edge.
     * 
     * @param e the edge
     * @return the target vertex of an edge
     */
    public V getEdgeTarget(E e)
    {
        IntrusiveEdge ie = getIntrusiveEdge(e);
        if (ie == null) {
            throw new IllegalArgumentException("no such edge in graph: " + e.toString());
        }
        return TypeUtil.uncheckedCast(ie.target);
    }

    /**
     * Get the weight of an edge.
     * 
     * @param e the edge
     * @return the weight of an edge
     */
    public double getEdgeWeight(E e)
    {
        return Graph.DEFAULT_EDGE_WEIGHT;
    }

    /**
     * Set the weight of an edge
     * 
     * @param e the edge
     * @param weight the new weight
     */
    public void setEdgeWeight(E e, double weight)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Add a new edge
     * 
     * @param e the edge
     * @param sourceVertex the source vertex of the edge
     * @param targetVertex the target vertex of the edge
     * @return true if the edge was added, false if the edge was already present
     */
    public abstract boolean add(E e, V sourceVertex, V targetVertex);

    /**
     * Get the intrusive edge of an edge.
     * 
     * @param e the edge
     * @return the intrusive edge
     */
    protected abstract IE getIntrusiveEdge(E e);
}
