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
    

    /**
     * @param graph1
     * @param graph2
     * @param vertexChecker eq. group checker for vertexes. If null,
     * UniformEquivalenceComparator will be used as default (always return true)
     * @param edgeChecker eq. group checker for edges. If null,
     * UniformEquivalenceComparator will be used as default (always return true)
     */
    public PermutationIsomorphismInspector(
        Graph<V, E> graph1,
        Graph<V, E> graph2,

        // XXX hb 060128: FOllowing parameter may need Graph<? super V,? super
        // E>
        EquivalenceComparator<? super V, ? super Graph<? super V, ? super E>> vertexChecker,
        EquivalenceComparator<? super E, ? super Graph<? super V, ? super E>> edgeChecker)
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
        Graph<V, E> graph1,
        Graph<V, E> graph2)
    {
        super(graph1, graph2);
    }

    

    /**
     * Creates the permutation iterator, not dependant on equality group, or the
     * other vertexset.
     *
     * @param vertexSet1 FIXME Document me
     * @param vertexSet2 FIXME Document me
     *
     * @return the permutation iterator
     */
    protected CollectionPermutationIter<V> createPermutationIterator(
        Set<V> vertexSet1,
        Set<V> vertexSet2)
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
    protected boolean areVertexSetsOfTheSameEqualityGroup(
        Set<V> vertexSet1,
        Set<V> vertexSet2)
    {
        if (vertexSet1.size() != vertexSet2.size()) {
            return false;
        }
        Iterator<V> iter2 = vertexSet2.iterator();

        // only check hasNext() of one , cause they are of the same size
        for (Iterator<V> iter1 = vertexSet1.iterator(); iter1.hasNext();) {
            V vertex1 = iter1.next();
            V vertex2 = iter2.next();
            if (!this.vertexComparator.equivalenceCompare(
                    vertex1,
                    vertex2,
                    this.graph1,
                    this.graph2))
            {
                return false;
            }
        }
        return true;
    }
}

// End PermutationIsomorphismInspector.java
