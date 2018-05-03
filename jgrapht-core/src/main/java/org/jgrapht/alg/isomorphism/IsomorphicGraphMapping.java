/*
 * (C) Copyright 2015-2018, by Fabian Späh and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.alg.isomorphism;

import java.util.*;

import org.jgrapht.*;

/**
 * This class represents a GraphMapping between two (subgraph)isomorphic graphs. In the subgraph
 * isomorphic case, the second one is assumed to be a subgraph of the first one.
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 */
public class IsomorphicGraphMapping<V, E>
    implements GraphMapping<V, E>
{
    GraphOrdering<V, E> g1, g2;

    int[] core1, core2;

    /**
     * Construct a new isomorphic graph mapping
     * 
     * @param g1 the first graph
     * @param g2 the second graph which is a possible subgraph of g1
     * @param core1 the mapping as array (forwards)
     * @param core2 the mapping as array (backwards)
     */
    public IsomorphicGraphMapping(
        GraphOrdering<V, E> g1, GraphOrdering<V, E> g2, int[] core1, int[] core2)
    {
        this.g1 = g1;
        this.g2 = g2;
        this.core1 = core1.clone();
        this.core2 = core2.clone();
    }

    @Override
    public V getVertexCorrespondence(V v, boolean forward)
    {
        GraphOrdering<V, E> firstGraph, secondGraph;
        int[] core;

        if (forward) {
            firstGraph = g1;
            secondGraph = g2;
            core = core1;
        } else {
            firstGraph = g2;
            secondGraph = g1;
            core = core2;
        }

        int vNumber = firstGraph.getVertexNumber(v), uNumber = core[vNumber];

        if (uNumber == VF2State.NULL_NODE) {
            return null;
        }

        return secondGraph.getVertex(uNumber);
    }

    @Override
    public E getEdgeCorrespondence(E e, boolean forward)
    {
        GraphOrdering<V, E> firstGraph, secondGraph;
        int[] core;

        if (forward) {
            firstGraph = g1;
            secondGraph = g2;
            core = core1;
        } else {
            firstGraph = g2;
            secondGraph = g1;
            core = core2;
        }

        int[] eNumbers = firstGraph.getEdgeNumbers(e);
        if ((core[eNumbers[0]] == VF2State.NULL_NODE)
            || (core[eNumbers[1]] == VF2State.NULL_NODE))
        {
            return null;
        }

        return secondGraph.getEdge(core[eNumbers[0]], core[eNumbers[1]]);
    }

    /**
     * Checks if a vertex $v$ from the first graph has a corresponding vertex in the second graph
     *
     * @param v the vertex
     * @return is there a corresponding vertex to $v$ in the subgraph
     */
    public boolean hasVertexCorrespondence(V v)
    {
        return getVertexCorrespondence(v, true) != null;
    }

    /**
     * Checks if a edge e from the first graph has a corresponding edge in the second graph
     *
     * @param e the edge
     * @return is there a corresponding edge to $e$ in the subgraph
     */
    public boolean hasEdgeCorrespondence(E e)
    {
        return getEdgeCorrespondence(e, true) != null;
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder("[");
        Set<V> vertexSet = g1.getGraph().vertexSet();
        Map<String, V> vertexMap = new TreeMap<>();

        for (V v : vertexSet) {
            vertexMap.put(v.toString(), v);
        }

        int i = 0;
        for (Map.Entry<String, V> entry : vertexMap.entrySet()) {
            V u = getVertexCorrespondence(entry.getValue(), true);
            str.append((i++ == 0) ? "" : " ").append(entry.getKey()).append("=").append((u == null) ? "~~" : u);
        }

        return str + "]";
    }

    /**
     * Checks for equality. Assuming both are mappings on the same graphs.
     *
     * @param rel the corresponding mapping
     *
     * @return do both relations map to the same vertices
     */
    public boolean isEqualMapping(GraphMapping<V, E> rel)
    {
        for (V v : g2.getGraph().vertexSet()) {
            if (!getVertexCorrespondence(v, false).equals(rel.getVertexCorrespondence(v, false))) {
                return false;
            }
        }

        return true;
    }
}

// End IsomorphicGraphMapping.java
