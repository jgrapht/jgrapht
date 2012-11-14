/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
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

import org.jgrapht.UndirectedGraph;

import java.util.Comparator;


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
    implements Comparator<V>
{
    //~ Instance fields --------------------------------------------------------

    /**
     * The graph that contains the vertices to be compared.
     */
    private final UndirectedGraph<V, E> graph;

    /**
     * The sort order for vertex degree. <code>true</code> for ascending degree
     * order (smaller degrees first), <code>false</code> for descending.
     */
    private final boolean ascendingOrder;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a comparator for comparing the degrees of vertices in the
     * specified graph. The comparator compares in ascending order of degrees
     * (lowest first).
     *
     * @param g graph with respect to which the degree is calculated.
     */
    public VertexDegreeComparator(final UndirectedGraph<V, E> g)
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
        final UndirectedGraph<V, E> g,
        final boolean ascendingOrder)
    {
        graph = g;
        this.ascendingOrder = ascendingOrder;
    }

    //~ Methods ----------------------------------------------------------------

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
    @Override
    public int compare(final V v1, final V v2)
    {
        final int degree1 = graph.degreeOf(v1);
        final int degree2 = graph.degreeOf(v2);

        if (degree1 < degree2 && ascendingOrder
            || degree1 > degree2 && !ascendingOrder)
        {
            return -1;
        } else if (
            degree1 > degree2 && ascendingOrder
            || degree1 < degree2 && !ascendingOrder)
        {
            return 1;
        } else {
            return 0;
        }
    }
}

// End VertexDegreeComparator.java
