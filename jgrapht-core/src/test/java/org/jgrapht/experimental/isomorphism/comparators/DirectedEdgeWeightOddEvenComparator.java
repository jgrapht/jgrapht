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
/* -----------------
 * DirectedEdgeWeightOddEvenComparator.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id: DirectedEdgeWeightOddEvenComparator.java 489 2006-07-02 02:05:47Z
 * perfecthash $
 *
 * Changes
 * -------
 */
package org.jgrapht.experimental.isomorphism.comparators;

import org.jgrapht.*;
import org.jgrapht.experimental.equivalence.*;


/**
 * eq.set according to the weights of the edges. Uses Graph.getEdgeWeight(Edge)
 * (cast to integer) and checks odd/even.
 *
 * @author Assaf
 * @since Aug 12, 2005
 */
public class DirectedEdgeWeightOddEvenComparator
    implements EquivalenceComparator
{
    //~ Instance fields --------------------------------------------------------

    private final Graph graph;

    //~ Constructors -----------------------------------------------------------

    public DirectedEdgeWeightOddEvenComparator(Graph graph)
    {
        this.graph = graph;
    }

    //~ Methods ----------------------------------------------------------------

    /* (non-Javadoc)
     * @see
     *
     *
     *
     *
     *
     * org.jgrapht.experimental.equivalence.EquivalenceComparator#equivalenceCompare(java.lang.Object,
     * java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equivalenceCompare(
        Object arg1,
        Object arg2,
        Object context1,
        Object context2)
    {
        int int1 = (int) graph.getEdgeWeight(arg1);
        int int2 = (int) graph.getEdgeWeight(arg2);

        boolean result = ((int1 % 2) == (int2 % 2));
        return result;
    }

    /* (non-Javadoc)
     * @see
     *
     *
     *
     *
     *
     * org.jgrapht.experimental.equivalence.EquivalenceComparator#equivalenceHashcode(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public int equivalenceHashcode(Object arg1, Object context)
    {
        int int1 = (int) graph.getEdgeWeight(arg1);
        return int1 % 2;
    }
}

// End DirectedEdgeWeightOddEvenComparator.java
