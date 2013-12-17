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

import java.util.*;

import org.jgrapht.*;


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
    

    /**
     * Holds a mapping between key=V(vertex) and value=Integer(vertex order). It
     * can be used for identifying the order of regular vertex/edge.
     */
    private Map<V, Integer> mapVertexToOrder;

    /**
     * Holds a HashSet of all LabelsGraph of the graph.
     */
    private Set<LabelsEdge> labelsEdgesSet;

    

    /**
     * Creates a new labels graph according to the regular graph. After its
     * creation they will no longer be linked, thus changes to one will not
     * affect the other.
     *
     * @param regularGraph
     */
    public GraphOrdering(Graph<V, E> regularGraph)
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
        Graph<V, E> regularGraph,
        Set<V> vertexSet,
        Set<E> edgeSet)
    {
        init(regularGraph, vertexSet, edgeSet);
    }

    

    private void init(Graph<V, E> g, Set<V> vertexSet, Set<E> edgeSet)
    {
        // create a map between vertex value to its order(1st,2nd,etc)
        // "CAT"=1 "DOG"=2 "RHINO"=3

        this.mapVertexToOrder = new HashMap<V, Integer>(vertexSet.size());

        int counter = 0;
        for (V vertex : vertexSet) {
            mapVertexToOrder.put(vertex, new Integer(counter));
            counter++;
        }

        // create a friendlier representation of an edge
        // by order, like 2nd->3rd instead of B->A
        // use the map to convert vertex to order
        // on directed graph, edge A->B must be (A,B)
        // on undirected graph, edge A-B can be (A,B) or (B,A)

        this.labelsEdgesSet = new HashSet<LabelsEdge>(edgeSet.size());
        for (E edge : edgeSet) {
            V sourceVertex = g.getEdgeSource(edge);
            Integer sourceOrder = mapVertexToOrder.get(sourceVertex);
            int sourceLabel = sourceOrder.intValue();
            int targetLabel =
                (mapVertexToOrder.get(g.getEdgeTarget(edge))).intValue();

            LabelsEdge lablesEdge = new LabelsEdge(sourceLabel, targetLabel);
            this.labelsEdgesSet.add(lablesEdge);

            if (g instanceof UndirectedGraph<?, ?>) {
                LabelsEdge oppositeEdge =
                    new LabelsEdge(targetLabel, sourceLabel);
                this.labelsEdgesSet.add(oppositeEdge);
            }
        }
    }

    /**
     * Tests equality by order of edges
     */
    public boolean equalsByEdgeOrder(GraphOrdering otherGraph)
    {
        boolean result =
            this.getLabelsEdgesSet().equals(otherGraph.getLabelsEdgesSet());

        return result;
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
        StringBuffer sb = new StringBuffer();
        sb.append("mapVertexToOrder=");

        // vertex will be printed in their order
        Object [] vertexArray = new Object[this.mapVertexToOrder.size()];
        Set<V> keySet = this.mapVertexToOrder.keySet();
        for (V currVertex : keySet) {
            Integer index = this.mapVertexToOrder.get(currVertex);
            vertexArray[index.intValue()] = currVertex;
        }
        sb.append(Arrays.toString(vertexArray));
        sb.append("labelsOrder=").append(this.labelsEdgesSet.toString());
        return sb.toString();
    }

    

    private class LabelsEdge
    {
        private int source;
        private int target;
        private int hashCode;

        public LabelsEdge(int aSource, int aTarget)
        {
            this.source = aSource;
            this.target = aTarget;
            this.hashCode =
                new String(this.source + "" + this.target).hashCode();
        }

        /**
         * Checks both source and target. Does not check class type to be fast,
         * so it may throw ClassCastException. Careful!
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj)
        {
            LabelsEdge otherEdge = (LabelsEdge) obj;
            if ((this.source == otherEdge.source)
                && (this.target == otherEdge.target))
            {
                return true;
            } else {
                return false;
            }
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            return this.hashCode; // filled on constructor
        }

        public String toString()
        {
            return this.source + "->" + this.target;
        }
    }
}

// End GraphOrdering.java
