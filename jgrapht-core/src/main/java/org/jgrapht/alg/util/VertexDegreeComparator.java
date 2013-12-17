/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* ---------------------------
 * VertexDegreeComparator.java
 * ---------------------------
 * (C) Copyright 2003-2008, by Linda Buisman and Contributors.
 *
 * Original Author:  Linda Buisman
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 06-Nov-2003 : Initial revision (LB);
 * 07-Jun-2005 : Made generic (CH);
 *
 */
package org.jgrapht.alg.util;

import org.jgrapht.*;


/**
 * Compares two vertices based on their degree.
 *
 * <p>Used by greedy algorithms that need to sort vertices by their degree. Two
 * vertices are considered equal if their degrees are equal.</p>
 *
 * @author Linda Buisman
 * @since Nov 6, 2003
 */
public class VertexDegreeComparator<V, E>
    implements java.util.Comparator<V>
{
    

    /**
     * The graph that contains the vertices to be compared.
     */
    private UndirectedGraph<V, E> graph;

    /**
     * The sort order for vertex degree. <code>true</code> for ascending degree
     * order (smaller degrees first), <code>false</code> for descending.
     */
    private boolean ascendingOrder;

    

    /**
     * Creates a comparator for comparing the degrees of vertices in the
     * specified graph. The comparator compares in ascending order of degrees
     * (lowest first).
     *
     * @param g graph with respect to which the degree is calculated.
     */
    public VertexDegreeComparator(UndirectedGraph<V, E> g)
    {
        this(g, true);
    }

    /**
     * Creates a comparator for comparing the degrees of vertices in the
     * specified graph.
     *
     * @param g graph with respect to which the degree is calculated.
     * @param ascendingOrder true - compares in ascending order of degrees
     * (lowest first), false - compares in descending order of degrees (highest
     * first).
     */
    public VertexDegreeComparator(
        UndirectedGraph<V, E> g,
        boolean ascendingOrder)
    {
        graph = g;
        this.ascendingOrder = ascendingOrder;
    }

    

    /**
     * Compare the degrees of <code>v1</code> and <code>v2</code>, taking into
     * account whether ascending or descending order is used.
     *
     * @param v1 the first vertex to be compared.
     * @param v2 the second vertex to be compared.
     *
     * @return -1 if <code>v1</code> comes before <code>v2</code>, +1 if <code>
     * v1</code> comes after <code>v2</code>, 0 if equal.
     */
    public int compare(V v1, V v2)
    {
        int degree1 = graph.degreeOf(v1);
        int degree2 = graph.degreeOf(v2);

        if (((degree1 < degree2) && ascendingOrder)
            || ((degree1 > degree2) && !ascendingOrder))
        {
            return -1;
        } else if (
            ((degree1 > degree2) && ascendingOrder)
            || ((degree1 < degree2) && !ascendingOrder))
        {
            return 1;
        } else {
            return 0;
        }
    }
}

// End VertexDegreeComparator.java
