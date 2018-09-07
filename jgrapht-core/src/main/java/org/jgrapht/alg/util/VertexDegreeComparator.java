/*
 * (C) Copyright 2003-2018, by Linda Buisman and Contributors.
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
 * @since Nov 6, 2003
 */
public class VertexDegreeComparator<V, E>
    implements
    Comparator<V>
{

    /**
     * Order in which we sort the vertices: ascending vertex degree or descending vertex degree
     */
    public enum Order
    {
        ASCENDING,
        DESCENDING
    };

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
     */
    public VertexDegreeComparator(Graph<V, E> g)
    {
        this(g, Order.ASCENDING);
    }

    /**
     * Creates a comparator for comparing the degrees of vertices in the specified graph.
     *
     * @param g graph with respect to which the degree is calculated.
     * @param order order in which the vertices are sorted (ascending or descending)
     */
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
     */
    @Override
    public int compare(V v1, V v2)
    {
        int comparison = Integer.compare(graph.degreeOf(v1), graph.degreeOf(v2));

        if (order == Order.ASCENDING)
            return comparison;
        else
            return -1 * comparison;
    }
}

// End VertexDegreeComparator.java
