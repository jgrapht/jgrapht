/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

import java.io.*;
import java.util.*;

/**
 * An implementation of {@link SingleSourcePaths} which uses linear space.
 * 
 * <p>
 * This implementation uses the traditional representation of maintaining for each vertex the
 * predecessor in the shortest path tree. In order to keep space to linear, the paths are recomputed
 * in each invocation of the {@link #getPath(Object)} method. The complexity of
 * {@link #getPath(Object)} is linear to the number of edges of the path while the complexity of
 * {@link #getWeight(Object)} is $O(1)$.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class TreeSingleSourcePathsImpl<V, E>
    implements
    SingleSourcePaths<V, E>,
    Serializable
{
    private static final long serialVersionUID = -5914007312734512847L;

    /**
     * The graph
     */
    protected Graph<V, E> g;

    /**
     * The source vertex
     */
    protected V source;

    /**
     * A map which keeps for each target vertex the predecessor edge and the total length of the
     * shortest path.
     */
    protected Map<V, Pair<Double, E>> map;

    /**
     * Construct a new instance.
     * 
     * @param g the graph
     * @param source the source vertex
     * @param distanceAndPredecessorMap a map which contains for each vertex the distance and the
     *        last edge that was used to discover the vertex. The map does not need to contain any
     *        entry for the source vertex. In case it does contain the predecessor at the source
     *        vertex must be null.
     */
    public TreeSingleSourcePathsImpl(
        Graph<V, E> g, V source, Map<V, Pair<Double, E>> distanceAndPredecessorMap)
    {
        this.g = Objects.requireNonNull(g, "Graph is null");
        this.source = Objects.requireNonNull(source, "Source vertex is null");
        this.map = Objects
            .requireNonNull(distanceAndPredecessorMap, "Distance and predecessor map is null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Graph<V, E> getGraph()
    {
        return g;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getSourceVertex()
    {
        return source;
    }

    /**
     * Get the internal map used for representing the paths.
     * 
     * @return the internal distance and predecessor map used for representing the paths.
     */
    public Map<V, Pair<Double, E>> getDistanceAndPredecessorMap()
    {
        return Collections.unmodifiableMap(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getWeight(V targetVertex)
    {
        Pair<Double, E> p = map.get(targetVertex);
        if (p == null) {
            if (source.equals(targetVertex)) {
                return 0d;
            } else {
                return Double.POSITIVE_INFINITY;
            }
        } else {
            return p.getFirst();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> getPath(V targetVertex)
    {
        if (source.equals(targetVertex)) {
            return GraphWalk.singletonWalk(g, source, 0d);
        }

        LinkedList<E> edgeList = new LinkedList<>();

        V cur = targetVertex;
        Pair<Double, E> p = map.get(cur);
        if (p == null || p.getFirst().equals(Double.POSITIVE_INFINITY)) {
            return null;
        }

        double weight = 0d;
        while (p != null && !cur.equals(source)) {
            E e = p.getSecond();
            if (e == null) {
                break;
            }
            edgeList.addFirst(e);
            weight += g.getEdgeWeight(e);
            cur = Graphs.getOppositeVertex(g, e, cur);
            p = map.get(cur);
        }

        return new GraphWalk<>(g, source, targetVertex, null, edgeList, weight);
    }

}
