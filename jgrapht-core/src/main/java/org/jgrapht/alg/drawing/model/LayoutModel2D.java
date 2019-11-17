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
import org.jgrapht.alg.drawing.LayoutAlgorithm2D;

/**
 * A general interface for the 2D layout model.
 * 
 * The layout model provides the necessary components to a {@link LayoutAlgorithm2D} in order to
 * draw a graph. Its responsibility is to provide the available drawable area, to be able to store
 * and answer queries about vertex coordinates, to allow someone to fix (make permanent) a vertex
 * location and potentially provide an initializer. If provided, the initializer, maybe called by a
 * layout algorithm in order to calculate initial positions for each vertex.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the vertex type
 */
public interface LayoutModel2D<V>
    extends
    Iterable<Map.Entry<V, Point2D>>
{

    /**
     * Get the drawable area of the model.
     * 
     * @return the drawable area of the model
     */
    Box2D getDrawableArea();

    /**
     * Set the drawable area of the model.
     * 
     * @param drawableArea the drawable area to use
     */
    void setDrawableArea(Box2D drawableArea);

    /**
     * Get the last location of a particular vertex in the model. May return null if the vertex has
     * not been assigned a location or if the particular implementation does not store the
     * coordinates.
     * 
     * @param vertex the graph vertex
     * @return the last location of the vertex
     */
    Point2D get(V vertex);

    /**
     * Set the location of a vertex.
     * 
     * @param vertex the graph vertex
     * @param point the location
     * @return the previous location or null if the vertex did not have a previous location or if
     *         the model does not store locations
     */
    Point2D put(V vertex, Point2D point);

    /**
     * Set a point as being a "fixed-point" or not.
     * 
     * It is the model's responsibility to make sure that changing the coordinates of a fixed point
     * by calling {@link #put(Object, Point2D)} has no effect.
     * 
     * @param vertex a vertex
     * @param fixed whether it is a fixed point or not.
     */
    void setFixed(V vertex, boolean fixed);

    /**
     * Check whether a vertex is a fixed point.
     * 
     * It is the model's responsibility to make sure that changing the coordinates of a fixed point
     * by calling {@link #put(Object, Point2D)} has no effect.
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
    default Map<V, Point2D> collect()
    {
        Map<V, Point2D> map = new LinkedHashMap<>();
        for (Map.Entry<V, Point2D> p : this) {
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
    Iterator<Map.Entry<V, Point2D>> iterator();

    /**
     * Get the initializer of the model. The role of the initializer is to set the initial
     * coordinates of graph vertices. A particular layout algorithm may choose to ignore or to
     * execute the initializer in order to compute the initial vertex coordinates.
     * 
     * @return the initializer or null if no initializer is present
     */
    Function<V, Point2D> getInitializer();

    /**
     * Initialize the coordinates for all vertices of the input graph using the model's initializer.
     * 
     * @param graph the graph
     * 
     * @param <E> the edge type
     */
    default <E> void init(Graph<V, E> graph)
    {
        Function<V, Point2D> initializer = getInitializer();
        if (initializer != null) {
            for (V v : graph.vertexSet()) {
                Point2D value = initializer.apply(v);
                if (value != null) {
                    put(v, value);
                }
            }
        }
    }

}
