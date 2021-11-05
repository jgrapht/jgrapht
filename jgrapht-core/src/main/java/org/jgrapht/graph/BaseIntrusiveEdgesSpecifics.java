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
package org.jgrapht.graph;

import org.jgrapht.*;
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;

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
     * 
     * @param edgeMap the map to use for storage
     */
    public BaseIntrusiveEdgesSpecifics(Map<E, IE> edgeMap)
    {
        this.edgeMap = Objects.requireNonNull(edgeMap);
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

    protected boolean addIntrusiveEdge(E edge, V sourceVertex, V targetVertex, IE e)
    {
        if (e.source == null && e.target == null) { // edge not yet in any graph
            e.source = sourceVertex;
            e.target = targetVertex;

        } else if (e.source != sourceVertex || e.target != targetVertex) {
            // Edge already contained in this or another graph but with different touching
            // edges. Reject the edge to not reset the touching vertices of the edge.
            // Changing the touching vertices causes major inconsistent behavior.
            throw new IntrusiveEdgeException(e.source, e.target);
        }
        return edgeMap.putIfAbsent(edge, e) == null;
    }

    /**
     * Get the intrusive edge of an edge.
     * 
     * @param e the edge
     * @return the intrusive edge
     */
    protected abstract IE getIntrusiveEdge(E e);
}
