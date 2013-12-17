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
    

    /**
     */
    public VertexDegreeEquivalenceComparator()
    {
    }

    

    /**
     * Compares the in degrees and the out degrees of the two vertexes.
     *
     * <p>One may reside in an Undirected Graph and the other in a Directed
     * graph, or both on the same graph type.
     *
     * @see EquivalenceComparator#equivalenceCompare(Object, Object, Object,
     * Object)
     */
    public boolean equivalenceCompare(
        V vertex1,
        V vertex2,
        Graph<V, E> context1,
        Graph<V, E> context2)
    {
        // note that VertexDegreeComparator cannot be used. It supports only
        // directed graphs.
        InOutDegrees inOut1 = getInOutDegrees(context1, vertex1);
        InOutDegrees inOut2 = getInOutDegrees(context2, vertex2);
        boolean result = inOut1.equals(inOut2);
        return result;
    }

    /**
     * Hashes using the in & out degree of a vertex
     *
     * @see EquivalenceComparator#equivalenceHashcode(Object, Object)
     */
    public int equivalenceHashcode(V vertex, Graph<V, E> context)
    {
        InOutDegrees inOut = getInOutDegrees(context, vertex);

        // hash it using the string hash. use the format N '-' N
        StringBuffer sb = new StringBuffer();
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
    protected InOutDegrees getInOutDegrees(Graph<V, E> aContextGraph,
        V vertex)
    {
        int inVertexDegree = 0;
        int outVertexDegree = 0;
        if (aContextGraph instanceof UndirectedGraph<?, ?>) {
            UndirectedGraph<V, E> undirectedGraph =
                (UndirectedGraph<V, E>) aContextGraph;
            inVertexDegree = undirectedGraph.degreeOf(vertex);
            outVertexDegree = inVertexDegree; // it is UNdirected
        } else if (aContextGraph instanceof DirectedGraph<?, ?>) {
            DirectedGraph<V, E> directedGraph =
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

        public InOutDegrees(int aInDegree, int aOutDegree)
        {
            this.inDegree = aInDegree;
            this.outDegree = aOutDegree;
        }

        /**
         * Checks both inDegree and outDegree. Does not check class type to save
         * time. If should be used with caution.
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj)
        {
            InOutDegrees other = (InOutDegrees) obj;
            return ((this.inDegree == other.inDegree)
                && (this.outDegree == other.outDegree));
        }
    }
}

// End VertexDegreeEquivalenceComparator.java
