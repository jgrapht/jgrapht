/*
 * (C) Copyright 2007-2018, by France Telecom and Contributors.
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

import java.util.*;

/**
 * Helper class for {@link KShortestPaths}.
 *
 */
final class RankingPathElement<V, E>
    extends
    AbstractPathElement<V, E>
    implements
    GraphPath<V, E>
{
    /**
     * Weight of the path.
     */
    private double weight;

    private Graph<V, E> graph;

    /**
     * Creates a path element by concatenation of an edge to a path element.
     *
     * @param pathElement
     * @param edge edge reaching the end vertex of the path element created.
     * @param weight total cost of the created path element.
     */
    RankingPathElement(
        Graph<V, E> graph, RankingPathElement<V, E> pathElement, E edge, double weight)
    {
        super(graph, pathElement, edge);
        this.weight = weight;
        this.graph = graph;
    }

    /**
     * Creates an empty path element.
     *
     * @param vertex end vertex of the path element.
     */
    RankingPathElement(V vertex)
    {
        super(vertex);
        this.weight = 0;
    }

    /**
     * Returns the weight of the path.
     *
     * @return .
     */
    public double getWeight()
    {
        return this.weight;
    }

    /**
     * Returns the previous path element.
     *
     * @return <code>null</code> is the path is empty.
     */
    @Override
    public RankingPathElement<V, E> getPrevPathElement()
    {
        return (RankingPathElement<V, E>) super.getPrevPathElement();
    }

    @Override
    public Graph<V, E> getGraph()
    {
        return this.graph;
    }

    @Override
    public V getStartVertex()
    {
        if (getPrevPathElement() == null) {
            return super.getVertex();
        }
        return getPrevPathElement().getStartVertex();
    }

    @Override
    public V getEndVertex()
    {
        return super.getVertex();
    }

    @Override
    public List<E> getEdgeList()
    {
        return super.createEdgeListPath();
    }

}
