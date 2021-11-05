/*
 * (C) Copyright 2003-2021, by Linda Buisman and Contributors.
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
package org.jgrapht.alg.util;

import org.jgrapht.*;

import java.util.*;

/**
 * Compares two vertices based on their degree.
 *
 * <p>
 * Used by greedy algorithms that need to sort vertices by their degree. Two vertices are considered
 * equal if their degrees are equal.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Linda Buisman
 *
 */
public class VertexDegreeComparator<V, E>
    implements
    Comparator<V>
{
    /**
     * Returns a {@link Comparator} that compares vertices by their degrees in the specified graph.
     * <p>
     * The comparator compares in ascending order of degrees (lower degree first). To obtain a
     * comparator that compares in descending order call {@link Comparator#reversed()} on the
     * returned comparator.
     * </p>
     * 
     * @param <V> the graph vertex type
     * @param g graph with respect to which the degree is calculated.
     * @return a {@code Comparator} to compare vertices by their degree in ascending order
     */
    public static <V> Comparator<V> of(Graph<V, ?> g)
    {
        return Comparator.comparingInt(g::degreeOf);
    }

    // TODO: after next release remove everything below this line and remove implementation of
    // comparator (and the type parameters)

    /**
     * Order in which we sort the vertices: ascending vertex degree or descending vertex degree
     * 
     * @deprecated use {@link VertexDegreeComparator#of(Graph)}
     */
    @Deprecated(forRemoval = true, since = "1.5.1")
    public enum Order
    {
        ASCENDING,
        DESCENDING
    }

    /**
     * The graph that contains the vertices to be compared.
     */
    private Graph<V, E> graph;

    /**
     * Order in which the vertices are sorted: ascending or descending
     */
    private Order order;

    /**
     * Creates a comparator for comparing the degrees of vertices in the specified graph. The
     * comparator compares in ascending order of degrees (lowest first).
     *
     * @param g graph with respect to which the degree is calculated.
     * @deprecated use {@link VertexDegreeComparator#of(Graph)}
     */
    @Deprecated(forRemoval = true, since = "1.5.1")
    public VertexDegreeComparator(Graph<V, E> g)
    {
        this(g, Order.ASCENDING);
    }

    /**
     * Creates a comparator for comparing the degrees of vertices in the specified graph.
     *
     * @param g graph with respect to which the degree is calculated.
     * @param order order in which the vertices are sorted (ascending or descending)
     * @deprecated use {@link VertexDegreeComparator#of(Graph)} for ascending order or
     *             {@link Comparator#reversed() reverse the comparator } for descending order.
     */
    @Deprecated(forRemoval = true, since = "1.5.1")
    public VertexDegreeComparator(Graph<V, E> g, Order order)
    {
        graph = g;
        this.order = order;
    }

    /**
     * Compare the degrees of <code>v1</code> and <code>v2</code>, taking into account whether
     * ascending or descending order is used.
     *
     * @param v1 the first vertex to be compared.
     * @param v2 the second vertex to be compared.
     *
     * @return -1 if <code>v1</code> comes before <code>v2</code>, +1 if <code>
     * v1</code> comes after <code>v2</code>, 0 if equal.
     * @deprecated use {@link VertexDegreeComparator#of(Graph)}
     */
    @Override
    @Deprecated(forRemoval = true, since = "1.5.1")
    public int compare(V v1, V v2)
    {
        int comparison = Integer.compare(graph.degreeOf(v1), graph.degreeOf(v2));

        if (order == Order.ASCENDING) {
            return comparison;
        } else {
            return -1 * comparison;
        }
    }
}
