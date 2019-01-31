/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.util;

import java.util.*;

/**
 * Helper class for building a one-to-one mapping for a collection of vertices to the integer range
 * $[0, n)$ where $n$ is the number of vertices in the collection.
 *
 * <p>
 * This class computes the mapping only once, on instantiation. It does not support live updates.
 * </p>
 *
 * @author Alexandru Valeanu
 *
 * @param <V> the graph vertex type
 */
public class VertexToIntegerMapping<V>
{

    private final Map<V, Integer> vertexMap;
    private final List<V> indexList;

    /**
     * Create a new mapping from a set of vertices.
     *
     * @param vertices the input set of vertices
     * @throws NullPointerException if {@code vertices} is {@code null}
     */
    public VertexToIntegerMapping(Set<V> vertices)
    {
        Objects.requireNonNull(vertices, "the input collection of vertices cannot be null");

        vertexMap = new HashMap<>(vertices.size());
        indexList = new ArrayList<>(vertices.size());

        for (V v : vertices) {
            vertexMap.put(v, vertexMap.size());
            indexList.add(v);
        }
    }

    /**
     * Create a new mapping from a list of vertices. The input list will be used as the
     * {@code indexList} so it must not be modified.
     *
     * @param vertices the input list of vertices
     * @throws NullPointerException if {@code vertices} is {@code null}
     * @throws IllegalArgumentException if the vertices are not distinct
     */
    public VertexToIntegerMapping(List<V> vertices)
    {
        Objects.requireNonNull(vertices, "the input collection of vertices cannot be null");

        vertexMap = new HashMap<>(vertices.size());
        indexList = vertices;

        for (int i = 0; i < vertices.size(); i++) {
            V v = vertices.get(i);

            if (!vertexMap.containsKey(v)) {
                vertexMap.put(v, i);
            } else {
                throw new IllegalArgumentException("vertices are not distinct");
            }
        }
    }

    /**
     * Create a new mapping from a collection of vertices.
     *
     * @param vertices the input collection of vertices
     * @throws NullPointerException if {@code vertices} is {@code null}
     * @throws IllegalArgumentException if the vertices are not distinct
     */
    public VertexToIntegerMapping(Collection<V> vertices)
    {
        Objects.requireNonNull(vertices, "the input collection of vertices cannot be null");

        vertexMap = new HashMap<>(vertices.size());
        indexList = new ArrayList<>(vertices.size());

        for (V v : vertices) {
            if (!vertexMap.containsKey(v)) {
                vertexMap.put(v, vertexMap.size());
                indexList.add(v);
            } else {
                throw new IllegalArgumentException("vertices are not distinct");
            }
        }
    }

    /**
     * Get the {@code vertexMap}, a mapping from vertices to integers (i.e. the inverse of
     * {@code indexList}).
     *
     * @return a mapping from vertices to integers
     */
    public Map<V, Integer> getVertexMap()
    {
        return vertexMap;
    }

    /**
     * Get the {@code indexList}, a mapping from integers to vertices (i.e. the inverse of
     * {@code vertexMap}).
     *
     * @return a mapping from integers to vertices
     */
    public List<V> getIndexList()
    {
        return indexList;
    }
}
