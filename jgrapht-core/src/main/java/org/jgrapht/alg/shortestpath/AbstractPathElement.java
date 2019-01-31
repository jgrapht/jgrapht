/*
 * (C) Copyright 2006-2018, by France Telecom and Contributors.
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
 * A new path is created from a path concatenated to an edge. It's like a linked list.<br>
 * The empty path is composed only of one vertex.<br>
 * In this case the path has no previous path element.<br>
 * .
 *
 * <p>
 * NOTE jvs 1-Jan-2008: This is an internal data structure for use in algorithms. For returning
 * paths to callers, use the public {@link GraphPath} interface instead.
 *
 */
abstract class AbstractPathElement<V, E>
{
    /**
     * Number of hops of the path.
     */
    protected int nHops;

    /**
     * Edge reaching the target vertex of the path.
     */
    protected E prevEdge;

    /**
     * Previous path element.
     */
    protected AbstractPathElement<V, E> prevPathElement;

    /**
     * Target vertex.
     */
    private V vertex;

    /**
     * Creates a path element by concatenation of an edge to a path element.
     *
     * @param pathElement
     * @param edge edge reaching the end vertex of the path element created.
     */
    protected AbstractPathElement(Graph<V, E> graph, AbstractPathElement<V, E> pathElement, E edge)
    {
        this.vertex = Graphs.getOppositeVertex(graph, edge, pathElement.getVertex());
        this.prevEdge = edge;
        this.prevPathElement = pathElement;

        this.nHops = pathElement.getHopCount() + 1;
    }

    /**
     * Copy constructor.
     *
     * @param original source to copy from
     */
    protected AbstractPathElement(AbstractPathElement<V, E> original)
    {
        this.nHops = original.nHops;
        this.prevEdge = original.prevEdge;
        this.prevPathElement = original.prevPathElement;
        this.vertex = original.vertex;
    }

    /**
     * Creates an empty path element.
     *
     * @param vertex end vertex of the path element.
     */
    protected AbstractPathElement(V vertex)
    {
        this.vertex = vertex;
        this.prevEdge = null;
        this.prevPathElement = null;

        this.nHops = 0;
    }

    /**
     * Returns the path as a list of edges.
     *
     * @return list of <code>Edge</code>.
     */
    public List<E> createEdgeListPath()
    {
        List<E> path = new ArrayList<>();
        AbstractPathElement<V, E> pathElement = this;

        // while start vertex is not reached.
        while (pathElement.getPrevEdge() != null) {
            path.add(pathElement.getPrevEdge());

            pathElement = pathElement.getPrevPathElement();
        }

        Collections.reverse(path);

        return path;
    }

    /**
     * Returns the number of hops (or number of edges) of the path.
     *
     * @return .
     */
    public int getHopCount()
    {
        return this.nHops;
    }

    /**
     * Returns the edge reaching the target vertex of the path.
     *
     * @return <code>null</code> if the path is empty.
     */
    public E getPrevEdge()
    {
        return this.prevEdge;
    }

    /**
     * Returns the previous path element.
     *
     * @return <code>null</code> is the path is empty.
     */
    public AbstractPathElement<V, E> getPrevPathElement()
    {
        return this.prevPathElement;
    }

    /**
     * Returns the target vertex of the path.
     *
     * @return .
     */
    public V getVertex()
    {
        return this.vertex;
    }
}
