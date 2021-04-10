/*
 * (C) Copyright 2018-2018, by Christoph Grüne and Contributors.
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
package org.jgrapht.graph.interval;

import java.io.Serializable;
import java.util.Objects;

/**
 * Implementation for IntervalVertexPair
 * This class implements the container class for vertices in an interval graph.
 *
 * An interval graph is constructed from a set of intervals, which represent the vertices of the graph. This class
 * provides the implementation of a container that assigns an interval, whose representation is based on some generic
 * <code><T> extends Comparable<T></code>, to the actual vertex representation, which is represented by generic
 * <code><V></code>. That is, this container represents the vertex type of an interval graph.
 *
 * @param <V> the vertex
 * @param <T> the interval element
 *
 * @author Christoph Grüne (christophgruene)
 * @since Apr 26, 2018
 */
public class IntervalVertexPair<V, T extends Comparable<T>> implements Serializable {
    private V vertex;
    private Interval<T> interval;

    private static final long serialVersionUID = 7632653463458053425L;

    /**
     * Constructor for an IntervalVertexPair (container class for vertices in interval graphs)
     *
     * @param vertex the vertex
     * @param interval the interval
     */
    private IntervalVertexPair(V vertex, Interval<T> interval) {
        this.vertex = vertex;
        this.interval = interval;
    }

    /**
     * Getter for <code>interval</code>
     *
     * @return the interval
     */
    public Interval<T> getInterval() {
        return interval;
    }

    /**
     * Getter for <code>vertex</code>
     *
     * @return the vertex
     */
    public V getVertex() {
        return vertex;
    }

    @Override
    public String toString()
    {
        return "(" + vertex + "," + interval + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        else if (!(o instanceof IntervalVertexPair))
            return false;

        @SuppressWarnings("unchecked") IntervalVertexPair<V, T> other = (IntervalVertexPair<V, T>) o;
        return Objects.equals(vertex, other.vertex) && Objects.equals(interval, other.interval);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(vertex, interval);
    }

    /**
     * Creates new IntervalVertexPair of elements pulling of the necessity to provide corresponding types of the
     * elements supplied.
     *
     * @param <V> the vertex type
     * @param <T> the interval element type
     * @param vertex the vertex
     * @param interval the interval
     * @return new pair
     */
    public static <V, T extends Comparable<T>> IntervalVertexPair<V, T> of(V vertex, Interval<T> interval) {
        return new IntervalVertexPair<>(vertex, interval);
    }
}
