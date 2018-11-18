/*
 * (C) Copyright 2018-2019, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.drawing.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.jgrapht.Graph;

/**
 * A general interface for the layout model.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the vertex type
 * @param <N> the number type
 * @param <P> the point type
 * @param <R> the rectangle type 
 */
public interface LayoutModel<V, N extends Number, P extends Point<N>, R extends Rectangle<N>>
    extends
    Iterable<Map.Entry<V, P>>
{

    /**
     * Get the drawable area of the model.
     * 
     * @return the drawable area of the model
     */
    R getDrawableArea();

    /**
     * Set the drawable area of the model.
     * 
     * @param drawableArea the drawable area to use
     */
    void setDrawableArea(R drawableArea);

    /**
     * Get the last location of a particular vertex in the model. May return null if the vertex has
     * not been assigned a location or if the particular implementation does not store the
     * coordinates.
     * 
     * @param vertex the graph vertex
     * @return the last location of the vertex
     */
    P get(V vertex);

    /**
     * Set the location of a vertex.
     * 
     * @param vertex the graph vertex
     * @param point the location
     * @return the previous location or null if the vertex did not have a previous location or if
     *         the model does not store locations
     */
    P put(V vertex, P point);

    /**
     * Set a point as being a "fixed-point" or not.
     * 
     * A fixed point can be assigned a location but cannot move after that.
     * 
     * @param vertex a vertex
     * @param fixed whether it is a fixed point or not.
     */
    void setFixed(V vertex, boolean fixed);

    /**
     * Check whether a vertex is a fixed point.
     * 
     * @param vertex the vertex
     * @return true if a fixed point, false otherwise
     */
    boolean isFixed(V vertex);

    /**
     * Collect a map of all vertices locations. May return null if the model does not store
     * locations.
     * 
     * @return a map with all the locations
     */
    default Map<V, P> collect()
    {
        Map<V, P> map = new LinkedHashMap<>();
        for (Map.Entry<V, P> p : this) {
            map.put(p.getKey(), p.getValue());
        }
        return map;
    }

    /**
     * Get an iterator with all vertices' locations. May return an empty iterator if the model does
     * not store locations.
     * 
     * @return an iterator which returns all vertices with their locations. May return an empty
     *         iterator if the model does not store locations.
     */
    Iterator<Map.Entry<V, P>> iterator();

    /**
     * Get the initializer of the model. The role of the initializer is to set the initial
     * coordinates of graph vertices.
     * 
     * @return the initializer or null if no initializer is present
     */
    Function<V, P> getInitializer();

    /**
     * Initialize the coordinates for all vertices of the input graph using the model's initializer.
     * 
     * @param graph the graph
     * 
     * @param <E> the edge type
     */
    default <E> void init(Graph<V, E> graph)
    {
        Function<V, P> initializer = getInitializer();
        if (initializer != null) {
            for (V v : graph.vertexSet()) {
                P value = initializer.apply(v);
                if (value != null) {
                    put(v, value);
                }
            }
        }
    }

}
