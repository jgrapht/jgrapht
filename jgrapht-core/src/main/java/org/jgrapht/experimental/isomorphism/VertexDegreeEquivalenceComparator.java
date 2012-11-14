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
 * VertexDegreeEquivalenceComparator.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id: VertexDegreeEquivalenceComparator.java 485 2006-06-26 09:12:14Z
 * perfecthash $
 *
 * Changes
 * -------
 */
package org.jgrapht.experimental.isomorphism;

import org.jgrapht.*;
import org.jgrapht.experimental.equivalence.*;

import java.lang.Object;


/**
 * Two vertexes are equivalent under this comparator if and only if:
 *
 * <ol>
 * <li>they have the same IN degree
 *
 * <p>AND
 * <li>they have the same OUT degree
 * </ol>
 *
 * @author Assaf
 * @since Jul 21, 2005
 */
public class VertexDegreeEquivalenceComparator<V, E>
    implements EquivalenceComparator<V, Graph<V, E>>
{
    //~ Constructors -----------------------------------------------------------

    //~ Methods ----------------------------------------------------------------

    /**
     * Compares the in degrees and the out degrees of the two vertexes.
     *
     * <p>One may reside in an Undirected Graph and the other in a Directed
     * graph, or both on the same graph type.
     *
     * @see EquivalenceComparator#equivalenceCompare(Object, Object, Object,
     * Object)
     */
    @Override
    public boolean equivalenceCompare(
        final V vertex1,
        final V vertex2,
        final Graph<V, E> context1,
        final Graph<V, E> context2)
    {
        // note that VertexDegreeComparator cannot be used. It supports only
        // directed graphs.
        final InOutDegrees inOut1 = getInOutDegrees(context1, vertex1);
        final InOutDegrees inOut2 = getInOutDegrees(context2, vertex2);
        return inOut1.equals(inOut2);
    }

    /**
     * Hashes using the in & out degree of a vertex
     *
     * @see EquivalenceComparator#equivalenceHashcode(Object, Object)
     */
    @Override
    public int equivalenceHashcode(final V vertex, final Graph<V, E> context)
    {
        final InOutDegrees inOut = getInOutDegrees(context, vertex);

        // hash it using the string hash. use the format N '-' N
        final StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf(inOut.inDegree));
        sb.append("-"); // to diffrentiate inner and outer
        sb.append(String.valueOf(inOut.outDegree));
        return sb.toString().hashCode();
    }

    /**
     * Calculates the In and Out degrees of vertexes. Supported graph types:
     * UnDirectedGraph, DirectedGraph. In UnDirected graph, the in = out (as if
     * it was undirected and every edge is both an in and out edge)
     *
     * @param aContextGraph
     * @param vertex
     */
    protected InOutDegrees getInOutDegrees(final Graph<V, E> aContextGraph,
        final V vertex)
    {
        int inVertexDegree = 0;
        int outVertexDegree = 0;
        if (aContextGraph instanceof UndirectedGraph<?, ?>) {
            final UndirectedGraph<V, E> undirectedGraph =
                (UndirectedGraph<V, E>) aContextGraph;
            inVertexDegree = undirectedGraph.degreeOf(vertex);
            outVertexDegree = inVertexDegree; // it is UNdirected
        } else if (aContextGraph instanceof DirectedGraph<?, ?>) {
            final DirectedGraph<V, E> directedGraph =
                (DirectedGraph<V, E>) aContextGraph;
            inVertexDegree = directedGraph.inDegreeOf(vertex);
            outVertexDegree = directedGraph.outDegreeOf(vertex);
        } else {
            throw new RuntimeException(
                "contextGraph is of unsupported type . It must be one of these two :"
                + " UndirectedGraph or DirectedGraph");
        }
        return new InOutDegrees(inVertexDegree, outVertexDegree);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Simple structure used to hold the two ints: vertex in degree and vertex
     * out degree. Useful as returned value for methods which calculate both at
     * the same time.
     *
     * @author Assaf
     * @since Jul 21, 2005
     */
    protected class InOutDegrees
    {
        public int inDegree;
        public int outDegree;

        public InOutDegrees(final int aInDegree, final int aOutDegree)
        {
            inDegree = aInDegree;
            outDegree = aOutDegree;
        }

        /**
         * Checks both inDegree and outDegree. Does not check class type to save
         * time. If should be used with caution.
         *
         * @see Object#equals(Object)
         */
        public boolean equals(final Object obj)
        {
            final InOutDegrees other = (InOutDegrees) obj;
            return inDegree == other.inDegree
                && outDegree == other.outDegree;
        }
    }
}

// End VertexDegreeEquivalenceComparator.java
