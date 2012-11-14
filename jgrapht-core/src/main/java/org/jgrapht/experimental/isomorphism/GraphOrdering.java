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
 * GraphOrdering.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 */
package org.jgrapht.experimental.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Holds graph information as int labels only. vertexes: 1,2,3,4 edges:1->2 ,
 * 3->4 ,1->1. Implementation as imutable graph by int[] for vetexes and
 * LabelsEdge[] for edges. The current algorithms do not support graph with
 * multiple edges (Multigraph / Pseudograph). For the maintaner: The reason for
 * it is the use of edges sets of LabelsEdge in which the equals checks for
 * source and target vertexes. Thus there cannot be two LabelsEdge with the same
 * source and target in the same Set.
 *
 * @author Assaf
 * @since May 20, 2005
 */
public class GraphOrdering<V, E>
{
    //~ Instance fields --------------------------------------------------------

    /**
     * Holds a mapping between key=V(vertex) and value=Integer(vertex order). It
     * can be used for identifying the order of regular vertex/edge.
     */
    private Map<V, Integer> mapVertexToOrder;

    /**
     * Holds a HashSet of all LabelsGraph of the graph.
     */
    private Set<LabelsEdge> labelsEdgesSet;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new labels graph according to the regular graph. After its
     * creation they will no longer be linked, thus changes to one will not
     * affect the other.
     *
     * @param regularGraph
     */
    public GraphOrdering(final Graph<V, E> regularGraph)
    {
        this(regularGraph, regularGraph.vertexSet(), regularGraph.edgeSet());
    }

    /**
     * Creates a new labels graph according to the regular graph. After its
     * creation they will no longer be linked, thus changes to one will not
     * affect the other.
     *
     * @param regularGraph
     * @param vertexSet
     * @param edgeSet
     */
    public GraphOrdering(
        final Graph<V, E> regularGraph,
        final Set<V> vertexSet,
        final Set<E> edgeSet)
    {
        init(regularGraph, vertexSet, edgeSet);
    }

    //~ Methods ----------------------------------------------------------------

    private void init(final Graph<V, E> g, final Set<V> vertexSet, final Set<E> edgeSet)
    {
        // create a map between vertex value to its order(1st,2nd,etc)
        // "CAT"=1 "DOG"=2 "RHINO"=3

        mapVertexToOrder = new HashMap<V, Integer>(vertexSet.size());

        int counter = 0;
        for (final V vertex : vertexSet) {
            mapVertexToOrder.put(vertex, new Integer(counter));
            counter++;
        }

        // create a friendlier representation of an edge
        // by order, like 2nd->3rd instead of B->A
        // use the map to convert vertex to order
        // on directed graph, edge A->B must be (A,B)
        // on undirected graph, edge A-B can be (A,B) or (B,A)

        labelsEdgesSet = new HashSet<LabelsEdge>(edgeSet.size());
        for (final E edge : edgeSet) {
            final V sourceVertex = g.getEdgeSource(edge);
            final Integer sourceOrder = mapVertexToOrder.get(sourceVertex);
            final int sourceLabel = sourceOrder.intValue();
            final int targetLabel =
                mapVertexToOrder.get(g.getEdgeTarget(edge)).intValue();

            final LabelsEdge lablesEdge = new LabelsEdge(sourceLabel, targetLabel);
            labelsEdgesSet.add(lablesEdge);

            if (g instanceof UndirectedGraph<?, ?>) {
                final LabelsEdge oppositeEdge =
                    new LabelsEdge(targetLabel, sourceLabel);
                labelsEdgesSet.add(oppositeEdge);
            }
        }
    }

    /**
     * Tests equality by order of edges
     */
    public boolean equalsByEdgeOrder(final GraphOrdering otherGraph)
    {

        return getLabelsEdgesSet().equals(otherGraph.getLabelsEdgesSet());
    }

    public Set<LabelsEdge> getLabelsEdgesSet()
    {
        return labelsEdgesSet;
    }

    /**
     * This is the format example:
     *
     * <pre>
       mapVertexToOrder=        labelsOrder=
     * </pre>
     */
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        sb.append("mapVertexToOrder=");

        // vertex will be printed in their order
        final Object [] vertexArray = new Object[mapVertexToOrder.size()];
        final Set<V> keySet = mapVertexToOrder.keySet();
        for (final V currVertex : keySet) {
            final Integer index = mapVertexToOrder.get(currVertex);
            vertexArray[index.intValue()] = currVertex;
        }
        sb.append(Arrays.toString(vertexArray));
        sb.append("labelsOrder=").append(labelsEdgesSet.toString());
        return sb.toString();
    }

    //~ Inner Classes ----------------------------------------------------------

    private class LabelsEdge
    {
        private final int source;
        private final int target;
        private final int hashCode;

        public LabelsEdge(final int aSource, final int aTarget)
        {
            source = aSource;
            target = aTarget;
            hashCode =
                new String(source + "" + target).hashCode();
        }

        /**
         * Checks both source and target. Does not check class type to be fast,
         * so it may throw ClassCastException. Careful!
         *
         * @see Object#equals(Object)
         */
        public boolean equals(final Object obj)
        {
            final LabelsEdge otherEdge = (LabelsEdge) obj;
            return source == otherEdge.source && target == otherEdge.target;
        }

        /**
         * @see Object#hashCode()
         */
        public int hashCode()
        {
            return hashCode; // filled on constructor
        }

        public String toString()
        {
            return source + "->" + target;
        }
    }
}

// End GraphOrdering.java
