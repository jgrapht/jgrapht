/*
 * (C) Copyright 2015-2021, by Fabian Späh and Contributors.
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
 * Base implementation of the VF2 algorithm using its feature of detecting
 * <a href="http://mathworld.wolfram.com/GraphIsomorphism.html">isomorphism between two graphs</a>
 * as described in Cordella et al. A (sub)graph isomorphism algorithm for matching large graphs
 * (2004), DOI:10.1109/TPAMI.2004.75,
 * <a href="http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=1323804">
 * http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=1323804</a>
 *
 * <p>
 * This implementation of the VF2 algorithm does not support graphs with multiple edges.
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 */
public abstract class VF2AbstractIsomorphismInspector<V, E>
    implements
    IsomorphismInspector<V, E>
{
    protected Graph<V, E> graph1, graph2;

    protected Comparator<V> vertexComparator;
    protected Comparator<E> edgeComparator;

    protected GraphOrdering<V, E> ordering1, ordering2;

    /**
     * Construct a new base implementation of the VF2 isomorphism inspector.
     *
     * @param graph1 the first graph
     * @param graph2 the second graph
     * @param vertexComparator comparator for semantic equivalence of vertices
     * @param edgeComparator comparator for semantic equivalence of edges
     * @param cacheEdges if true, edges get cached for faster access
     */
    public VF2AbstractIsomorphismInspector(
        Graph<V, E> graph1, Graph<V, E> graph2, Comparator<V> vertexComparator,
        Comparator<E> edgeComparator, boolean cacheEdges)
    {
        GraphType type1 = graph1.getType();
        GraphType type2 = graph2.getType();
        if (type1.isAllowingMultipleEdges() || type2.isAllowingMultipleEdges()) {
            throw new IllegalArgumentException(
                "graphs with multiple (parallel) edges are not supported");
        }

        if (type1.isMixed() || type2.isMixed()) {
            throw new IllegalArgumentException("mixed graphs not supported");
        }

        if (type1.isUndirected() && type2.isDirected()
            || type1.isDirected() && type2.isUndirected())
        {
            throw new IllegalArgumentException(
                "can not match directed with " + "undirected graphs");
        }

        this.graph1 = graph1;
        this.graph2 = graph2;
        this.vertexComparator = vertexComparator;
        this.edgeComparator = edgeComparator;
        this.ordering1 = new GraphOrdering<>(graph1, true, cacheEdges);
        this.ordering2 = new GraphOrdering<>(graph2, true, cacheEdges);
    }

    /**
     * Construct a new base implementation of the VF2 isomorphism inspector.
     *
     * @param graph1 the first graph
     * @param graph2 the second graph
     * @param vertexComparator comparator for semantic equivalence of vertices
     * @param edgeComparator comparator for semantic equivalence of edges
     */
    public VF2AbstractIsomorphismInspector(
        Graph<V, E> graph1, Graph<V, E> graph2, Comparator<V> vertexComparator,
        Comparator<E> edgeComparator)
    {
        this(graph1, graph2, vertexComparator, edgeComparator, true);
    }

    /**
     * Construct a new base implementation of the VF2 isomorphism inspector.
     *
     * @param graph1 the first graph
     * @param graph2 the second graph
     * @param cacheEdges if true, edges get cached for faster access
     */
    public VF2AbstractIsomorphismInspector(
        Graph<V, E> graph1, Graph<V, E> graph2, boolean cacheEdges)
    {
        this(graph1, graph2, null, null, cacheEdges);
    }

    /**
     * Construct a new base implementation of the VF2 isomorphism inspector.
     *
     * @param graph1 the first graph
     * @param graph2 the second graph
     */
    public VF2AbstractIsomorphismInspector(Graph<V, E> graph1, Graph<V, E> graph2)
    {
        this(graph1, graph2, true);
    }

    @Override
    public abstract Iterator<GraphMapping<V, E>> getMappings();

    @Override
    public boolean isomorphismExists()
    {
        Iterator<GraphMapping<V, E>> iter = getMappings();
        return iter.hasNext();
    }
}
