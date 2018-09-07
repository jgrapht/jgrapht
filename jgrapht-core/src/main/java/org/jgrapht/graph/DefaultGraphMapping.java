/*
 * (C) Copyright 2005-2018, by Assaf Lehr and Contributors.
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
package org.jgrapht.graph;

import org.jgrapht.*;

import java.util.*;

/**
 * Implementation of the GraphMapping interface. The performance of <code>
 * getVertex/EdgeCorrespondence</code> is based on the performance of the concrete Map class which
 * is passed in the constructor. For example, using {@link HashMap} will provide expected $O(1)$
 * performance.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Assaf Lehr
 * @since Jul 30, 2005
 */
public class DefaultGraphMapping<V, E>
    implements
    GraphMapping<V, E>
{
    private Map<V, V> graphMappingForward;
    private Map<V, V> graphMappingReverse;

    private Graph<V, E> graph1;
    private Graph<V, E> graph2;

    /**
     * The maps themselves are used. There is no defensive-copy. Assumption: The key and value in
     * the mappings are of valid graph objects. It is not checked.
     *
     * @param g1ToG2 vertex mapping from the first graph to the second
     * @param g2ToG1 vertex mapping from the second graph to the first
     * @param g1 the first graph
     * @param g2 the second graph
     */
    public DefaultGraphMapping(Map<V, V> g1ToG2, Map<V, V> g2ToG1, Graph<V, E> g1, Graph<V, E> g2)
    {
        this.graph1 = g1;
        this.graph2 = g2;
        this.graphMappingForward = g1ToG2;
        this.graphMappingReverse = g2ToG1;
    }

    @Override
    public E getEdgeCorrespondence(E currEdge, boolean forward)
    {
        Graph<V, E> sourceGraph, targetGraph;

        if (forward) {
            sourceGraph = this.graph1;
            targetGraph = this.graph2;
        } else {
            sourceGraph = this.graph2;
            targetGraph = this.graph1;
        }

        V mappedSourceVertex =
            getVertexCorrespondence(sourceGraph.getEdgeSource(currEdge), forward);
        V mappedTargetVertex =
            getVertexCorrespondence(sourceGraph.getEdgeTarget(currEdge), forward);
        if ((mappedSourceVertex == null) || (mappedTargetVertex == null)) {
            return null;
        } else {
            return targetGraph.getEdge(mappedSourceVertex, mappedTargetVertex);
        }
    }

    @Override
    public V getVertexCorrespondence(V keyVertex, boolean forward)
    {
        Map<V, V> graphMapping;
        if (forward) {
            graphMapping = graphMappingForward;
        } else {
            graphMapping = graphMappingReverse;
        }

        return graphMapping.get(keyVertex);
    }
}

// End DefaultGraphMapping.java
