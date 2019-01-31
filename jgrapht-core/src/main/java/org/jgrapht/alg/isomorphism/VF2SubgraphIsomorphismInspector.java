/*
 * (C) Copyright 2015-2018, by Fabian Späh and Contributors.
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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.*;

import java.util.*;

/**
 * This is an implementation of the VF2 algorithm using its feature of detecting subgraph
 * isomorphism between two graphs as described in Cordella et al. A (sub)graph isomorphism algorithm
 * for matching large graphs (2004), DOI:10.1109/TPAMI.2004.75,
 * <a href="http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=1323804">
 * http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=1323804</a>
 *
 * <p>
 * Note that this inspector only finds isomorphisms between a smaller graph and all
 * <a href="http://mathworld.wolfram.com/Vertex-InducedSubgraph.html">induced subgraphs</a> of a
 * larger graph. It does not find isomorphisms between the smaller graph and arbitrary subgraphs of
 * the larger graph. For example, given as input the
 * <a href="http://mathworld.wolfram.com/CubicalGraph.html">cubical graph</a> $Q_{3}$ and the
 * <a href="http://mathworld.wolfram.com/SquareGraph.html">square graph</a>, isomorphic mappings
 * will be found between the square and the faces of the cube. However, given the
 * <a href="http://mathworld.wolfram.com/CompleteGraph.html">complete graph</a> $K_{5}$ and the
 * square graph as input, no isomorphisms will be found since all induced subgraphs of a complete
 * graph are themselves complete graphs.
 *
 * <p>
 * Consequently, in the case where both input graphs have the same number of vertices, this
 * algorithm is equivalent to running {@link VF2GraphIsomorphismInspector}.
 *
 * <p>
 * This implementation of the VF2 algorithm does not support graphs with multiple (parallel) edges.
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 */
public class VF2SubgraphIsomorphismInspector<V, E>
    extends
    VF2AbstractIsomorphismInspector<V, E>
{

    /**
     * Construct a new VF2 subgraph isomorphism inspector.
     *
     * @param graph1 the first graph
     * @param graph2 the second graph (possible induced subgraph of graph1)
     * @param vertexComparator comparator for semantic equivalence of vertices
     * @param edgeComparator comparator for semantic equivalence of edges
     * @param cacheEdges if true, edges get cached for faster access
     */
    public VF2SubgraphIsomorphismInspector(
        Graph<V, E> graph1, Graph<V, E> graph2, Comparator<V> vertexComparator,
        Comparator<E> edgeComparator, boolean cacheEdges)
    {
        super(graph1, graph2, vertexComparator, edgeComparator, cacheEdges);
    }

    /**
     * Construct a new VF2 subgraph isomorphism inspector.
     *
     * @param graph1 the first graph
     * @param graph2 the second graph (possible induced subgraph of graph1)
     * @param vertexComparator comparator for semantic equivalence of vertices
     * @param edgeComparator comparator for semantic equivalence of edges
     */
    public VF2SubgraphIsomorphismInspector(
        Graph<V, E> graph1, Graph<V, E> graph2, Comparator<V> vertexComparator,
        Comparator<E> edgeComparator)
    {
        super(graph1, graph2, vertexComparator, edgeComparator, true);
    }

    /**
     * Construct a new VF2 subgraph isomorphism inspector.
     *
     * @param graph1 the first graph
     * @param graph2 the second graph (possible induced subgraph of graph1)
     * @param cacheEdges if true, edges get cached for faster access
     */
    public VF2SubgraphIsomorphismInspector(
        Graph<V, E> graph1, Graph<V, E> graph2, boolean cacheEdges)
    {
        super(graph1, graph2, null, null, cacheEdges);
    }

    /**
     * Construct a new VF2 subgraph isomorphism inspector.
     *
     * @param graph1 the first graph
     * @param graph2 the second graph (possible induced subgraph of graph1)
     */
    public VF2SubgraphIsomorphismInspector(Graph<V, E> graph1, Graph<V, E> graph2)
    {
        super(graph1, graph2, true);
    }

    @Override
    public VF2SubgraphMappingIterator<V, E> getMappings()
    {
        return new VF2SubgraphMappingIterator<>(
            ordering1, ordering2, vertexComparator, edgeComparator);
    }
}
