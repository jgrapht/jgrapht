/*
 * (C) Copyright 2015-2017, by Wil Selwood and Contributors.
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
 * Provider of edges
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public interface EdgeProvider<V, E>
{
    /**
     * Construct an edge.
     *
     * @param from the source vertex
     * @param to the target vertex
     * @param label the label of the edge.
     * @param attributes extra attributes for the edge.
     *
     * @return the edge
     */
    E buildEdge(V from, V to, String label, Map<String, Attribute> attributes);
}

// End EdgeProvider.java
