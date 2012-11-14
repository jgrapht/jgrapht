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
 * PermutationIsomorphismInspector.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id: PermutationIsomorphismInspector.java 485 2006-06-26 09:12:14Z
 * perfecthash $
 *
 * Changes
 * -------
 */
package org.jgrapht.experimental.isomorphism;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.experimental.equivalence.*;
import org.jgrapht.experimental.permutation.*;


/**
 * Checks every possible permutation.
 *
 * <p>It does not uses the graph topology to enhance the performance. It is
 * recommended to use only if there cannot be a useful division into equivalence
 * sets.
 *
 * @author Assaf
 * @since Jul 29, 2005
 */
class PermutationIsomorphismInspector<V, E>
    extends AbstractExhaustiveIsomorphismInspector<V, E>
{
    //~ Constructors -----------------------------------------------------------

    /**
     * @param graph1
     * @param graph2
     * @param vertexChecker eq. group checker for vertexes. If null,
     * UniformEquivalenceComparator will be used as default (always return true)
     * @param edgeChecker eq. group checker for edges. If null,
     * UniformEquivalenceComparator will be used as default (always return true)
     */
    public PermutationIsomorphismInspector(
        final Graph<V, E> graph1,
        final Graph<V, E> graph2,

        // XXX hb 060128: FOllowing parameter may need Graph<? super V,? super
        // E>
        final EquivalenceComparator<? super V, ? super Graph<? super V, ? super E>> vertexChecker,
        final EquivalenceComparator<? super E, ? super Graph<? super V, ? super E>> edgeChecker)
    {
        super(graph1, graph2, vertexChecker, edgeChecker);
    }

    /**
     * Constructor which uses the default comparators.
     *
     * @see AbstractExhaustiveIsomorphismInspector#AbstractExhaustiveIsomorphismInspector(Graph,
     * Graph)
     */
    public PermutationIsomorphismInspector(
        final Graph<V, E> graph1,
        final Graph<V, E> graph2)
    {
        super(graph1, graph2);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates the permutation iterator, not dependant on equality group, or the
     * other vertexset.
     *
     * @param vertexSet1 FIXME Document me
     * @param vertexSet2 FIXME Document me
     *
     * @return the permutation iterator
     */
    @Override
    protected CollectionPermutationIter<V> createPermutationIterator(
        final Set<V> vertexSet1,
        final Set<V> vertexSet2)
    {
        return new CollectionPermutationIter<V>(vertexSet2);
    }

    /**
     * FIXME Document me FIXME Document me
     *
     * @param vertexSet1 FIXME Document me
     * @param vertexSet2 FIXME Document me
     *
     * @return FIXME Document me
     */
    @Override
    protected boolean areVertexSetsOfTheSameEqualityGroup(
        final Set<V> vertexSet1,
        final Set<V> vertexSet2)
    {
        if (vertexSet1.size() != vertexSet2.size()) {
            return false;
        }
        final Iterator<V> iter2 = vertexSet2.iterator();

        // only check hasNext() of one , cause they are of the same size
        for (final V vertex1 : vertexSet1) {
            final V vertex2 = iter2.next();
            if (!vertexComparator
                .equivalenceCompare(vertex1, vertex2, graph1, graph2))
            {
                return false;
            }
        }
        return true;
    }
}

// End PermutationIsomorphismInspector.java
