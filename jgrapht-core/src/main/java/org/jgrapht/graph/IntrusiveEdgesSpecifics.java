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

/**
 * An interface for the set of intrusive edges of a graph.
 * 
 * <p>
 * Since the library supports edges which can be any user defined object, we need to provide
 * explicit support for storing vertex source, target and weight.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
public interface IntrusiveEdgesSpecifics<V, E>
    extends
    Serializable
{
    /**
     * Get the source vertex of an edge.
     * 
     * @param e the edge
     * @return the source vertex
     */
    V getEdgeSource(E e);

    /**
     * Get the target vertex of an edge.
     * 
     * @param e the edge
     * @return the target vertex
     */
    V getEdgeTarget(E e);

    /**
     * Add a new edge.
     * 
     * @param e the edge to add
     * @param sourceVertex the source vertex
     * @param targetVertex the target vertex
     * @return true if the edge was added, false if the edge was already present
     */
    boolean add(E e, V sourceVertex, V targetVertex);

    /**
     * Check if an edge exists
     * 
     * @param e the input edge
     * @return true if an edge exists, false otherwise
     */
    boolean containsEdge(E e);

    /**
     * Get the edge set
     * 
     * @return the edge set
     */
    Set<E> getEdgeSet();

    /**
     * Remove an edge.
     * 
     * @param e the edge to remove.
     */
    void remove(E e);

    /**
     * Get the weight of an edge.
     * 
     * @param e the edge
     * @return the edge weight
     */
    double getEdgeWeight(E e);

    /**
     * Set the edge weight
     * 
     * @param e the edge
     * @param weight the new weight
     */
    void setEdgeWeight(E e, double weight);
}
