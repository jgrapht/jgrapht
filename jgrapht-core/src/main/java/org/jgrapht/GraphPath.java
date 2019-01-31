/*
 * (C) Copyright 2008-2018, by John V Sichi and Contributors.
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
package org.jgrapht;

import java.util.*;

/**
 * A GraphPath represents a <a href="http://mathworld.wolfram.com/Path.html"> path</a> in a
 * {@link Graph}. Unlike some definitions, the path is not required to be a
 * <a href="https://en.wikipedia.org/wiki/Simple_path">Simple Path</a>.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author John Sichi
 */
public interface GraphPath<V, E>
{
    /**
     * Returns the graph over which this path is defined. The path may also be valid with respect to
     * other graphs.
     *
     * @return the containing graph
     */
    Graph<V, E> getGraph();

    /**
     * Returns the start vertex in the path.
     *
     * @return the start vertex
     */
    V getStartVertex();

    /**
     * Returns the end vertex in the path.
     *
     * @return the end vertex
     */
    V getEndVertex();

    /**
     * Returns the edges making up the path. The first edge in this path is incident to the start
     * vertex. The last edge is incident to the end vertex. The vertices along the path can be
     * obtained by traversing from the start vertex, finding its opposite across the first edge, and
     * then doing the same successively across subsequent edges; see {@link #getVertexList()}.
     *
     * <p>
     * Whether or not the returned edge list is modifiable depends on the path implementation.
     *
     * @return list of edges traversed by the path
     */
    default List<E> getEdgeList()
    {
        List<V> vertexList = this.getVertexList();
        if (vertexList.size() < 2)
            return Collections.emptyList();

        Graph<V, E> g = this.getGraph();
        List<E> edgeList = new ArrayList<>();
        Iterator<V> vertexIterator = vertexList.iterator();
        V u = vertexIterator.next();
        while (vertexIterator.hasNext()) {
            V v = vertexIterator.next();
            edgeList.add(g.getEdge(u, v));
            u = v;
        }
        return edgeList;
    }

    /**
     * Returns the path as a sequence of vertices.
     *
     * @return path, denoted by a list of vertices
     */
    default List<V> getVertexList()
    {
        List<E> edgeList = this.getEdgeList();

        if (edgeList.isEmpty()) {
            V startVertex = getStartVertex();
            if (startVertex != null && startVertex.equals(getEndVertex())) {
                return Collections.singletonList(startVertex);
            } else {
                return Collections.emptyList();
            }
        }

        Graph<V, E> g = this.getGraph();
        List<V> list = new ArrayList<>();
        V v = this.getStartVertex();
        list.add(v);
        for (E e : edgeList) {
            v = Graphs.getOppositeVertex(g, e, v);
            list.add(v);
        }
        return list;
    }

    /**
     * Returns the weight assigned to the path. Typically, this will be the sum of the weights of
     * the edge list entries (as defined by the containing graph), but some path implementations may
     * use other definitions.
     *
     * @return the weight of the path
     */
    double getWeight();

    /**
     * Returns the length of the path, measured in the number of edges.
     * 
     * @return the length of the path, measured in the number of edges
     */
    default int getLength()
    {
        return getEdgeList().size();
    }

}
