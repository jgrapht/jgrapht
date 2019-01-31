/*
 * (C) Copyright 2005-2018, by Assaf Lehr and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
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
