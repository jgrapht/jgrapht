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

import org.jgrapht.Graph;
import org.jgrapht.experimental.equivalence.EquivalenceComparator;


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

    public DirectedEdgeWeightOddEvenComparator(final Graph graph)
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
        final Object arg1,
        final Object arg2,
        final Object context1,
        final Object context2)
    {
        final int int1 = (int) graph.getEdgeWeight(arg1);
        final int int2 = (int) graph.getEdgeWeight(arg2);

        return int1 % 2 == int2 % 2;
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
    public int equivalenceHashcode(final Object arg1, final Object context)
    {
        final int int1 = (int) graph.getEdgeWeight(arg1);
        return int1 % 2;
    }
}

// End DirectedEdgeWeightOddEvenComparator.java
