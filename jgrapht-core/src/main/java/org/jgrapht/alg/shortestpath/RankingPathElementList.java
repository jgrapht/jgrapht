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
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * List of simple paths in increasing order of weight.
 *
 */
final class RankingPathElementList<V, E>
    extends
    AbstractPathElementList<V, E, RankingPathElement<V, E>>
{
    /**
     * Vertex that paths of the list must not disconnect.
     */
    private V guardVertexToNotDisconnect = null;

    private Map<RankingPathElement<V, E>, Boolean> path2disconnect =
        new HashMap<RankingPathElement<V, E>, Boolean>();

    /**
     * To be used on top of general path validations. May invalidate the path though they pass the
     * basic validations done internally (path is from source to target and w/o loops).
     */
    private PathValidator<V, E> externalPathValidator = null;

    /**
     * Creates a list with an empty path. The list size is 1.
     *
     * @param maxSize max number of paths the list is able to store.
     */
    RankingPathElementList(Graph<V, E> graph, int maxSize, RankingPathElement<V, E> pathElement)
    {
        this(graph, maxSize, pathElement, null);
    }

    /**
     * Creates a list with an empty path. The list size is 1.
     *
     * @param maxSize max number of paths the list is able to store.
     * @param pathValidator path validator to be used in addition to basic validations (path is from
     *        source to target w/o loops)
     * 
     */
    RankingPathElementList(
        Graph<V, E> graph, int maxSize, RankingPathElement<V, E> pathElement,
        PathValidator<V, E> pathValidator)
    {
        super(graph, maxSize, pathElement);
        this.externalPathValidator = pathValidator;
    }

    /**
     * Creates paths obtained by concatenating the specified edge to the specified paths.
     *
     * @param elementList paths, list of <code>
     * RankingPathElement</code>.
     * @param edge edge reaching the end vertex of the created paths.
     * @param maxSize maximum number of paths the list is able to store.
     */
    RankingPathElementList(
        Graph<V, E> graph, int maxSize, RankingPathElementList<V, E> elementList, E edge)
    {
        this(graph, maxSize, elementList, edge, null);

        assert (!this.pathElements.isEmpty());
    }

    /**
     * Creates paths obtained by concatenating the specified edge to the specified paths.
     *
     * @param elementList paths, list of <code>
     * RankingPathElement</code>.
     * @param edge edge reaching the end vertex of the created paths.
     * @param maxSize maximum number of paths the list is able to store.
     */
    RankingPathElementList(
        Graph<V, E> graph, int maxSize, RankingPathElementList<V, E> elementList, E edge,
        V guardVertexToNotDisconnect)
    {
        this(graph, maxSize, elementList, edge, guardVertexToNotDisconnect, null);
    }

    /**
     * Creates paths obtained by concatenating the specified edge to the specified paths.
     *
     * @param elementList paths, list of <code>
     * RankingPathElement</code>.
     * @param edge edge reaching the end vertex of the created paths.
     * @param maxSize maximum number of paths the list is able to store.
     * @param pathValidator path validator to be used in addition to basic validations (path is from
     *        source to target w/o loops)
     */
    RankingPathElementList(
        Graph<V, E> graph, int maxSize, RankingPathElementList<V, E> elementList, E edge,
        V guardVertexToNotDisconnect, PathValidator<V, E> pathValidator)
    {
        super(graph, maxSize, elementList, edge);
        this.guardVertexToNotDisconnect = guardVertexToNotDisconnect;
        this.externalPathValidator = pathValidator;

        // loop over the path elements in increasing order of weight.
        for (int i = 0; (i < elementList.size()) && (size() < maxSize); i++) {
            RankingPathElement<V, E> prevPathElement = elementList.get(i);

            if (isNotValidPath(prevPathElement, edge)) {
                // go to the next path element in the loop
                continue;
            }

            double weight = calculatePathWeight(prevPathElement, edge);
            RankingPathElement<V, E> newPathElement =
                new RankingPathElement<>(this.graph, prevPathElement, edge, weight);

            // the new path is inserted at the end of the list.
            this.pathElements.add(newPathElement);
        }
    }

    /**
     * Creates an empty list. The list size is 0.
     *
     * @param maxSize max number of paths the list is able to store.
     */
    RankingPathElementList(Graph<V, E> graph, int maxSize, V vertex)
    {
        this(graph, maxSize, vertex, null);
    }

    /**
     * Creates an empty list. The list size is 0.
     *
     * @param maxSize max number of paths the list is able to store.
     * @param pathValidator path validator to be used in addition to basic validations (path is from
     *        source to target w/o loops)
     */
    RankingPathElementList(
        Graph<V, E> graph, int maxSize, V vertex, PathValidator<V, E> pathValidator)
    {
        super(graph, maxSize, vertex);
        this.externalPathValidator = pathValidator;
    }

    /**
     * <p>
     * Adds paths in the list at vertex $y$. Candidate paths are obtained by concatenating the
     * specified edge $(v, y)$ to the paths <code>
     * elementList</code> at vertex $v$.
     * </p>
     *
     * Complexity =
     *
     * <ul>
     * <li>w/o guard-vertex: $O(knp)$ where $k$ is the max size limit of the list and $np$ is the
     * maximum number of vertices in the paths stored in the list</li>
     * <li>with guard-vertex: $O(k(m+n)$</code>) where $k$ is the max size limit of the list, $m$ is
     * the number of edges of the graph and $n$ is the number of vertices of the graph, $O(m + n)$
     * being the complexity of the <code>
     * ConnectivityInspector</code> to check whether a path exists towards the guard-vertex</li>
     * </ul>
     *
     * @param elementList list of paths at vertex $v$.
     * @param edge edge $(v, y)$.
     *
     * @return <code>true</code> if at least one path has been added in the list, <code>false</code>
     *         otherwise.
     */
    public boolean addPathElements(RankingPathElementList<V, E> elementList, E edge)
    {
        assert (this.vertex
            .equals(Graphs.getOppositeVertex(this.graph, edge, elementList.getVertex())));

        boolean pathAdded = false;

        // loop over the paths elements of the list at vertex v.
        for (int vIndex = 0, yIndex = 0; vIndex < elementList.size(); vIndex++) {
            RankingPathElement<V, E> prevPathElement = elementList.get(vIndex);

            if (isNotValidPath(prevPathElement, edge)) {
                // checks if path is simple and if guard-vertex is not
                // disconnected.
                continue;
            }
            double newPathWeight = calculatePathWeight(prevPathElement, edge);
            RankingPathElement<V, E> newPathElement =
                new RankingPathElement<>(this.graph, prevPathElement, edge, newPathWeight);

            // loop over the paths of the list at vertex y from yIndex to the
            // end.
            RankingPathElement<V, E> yPathElement = null;
            for (; yIndex < size(); yIndex++) {
                yPathElement = get(yIndex);

                // case when the new path is shorter than the path Py stored at
                // index y
                if (newPathWeight < yPathElement.getWeight()) {
                    this.pathElements.add(yIndex, newPathElement);
                    pathAdded = true;

                    // ensures max size limit is not exceeded.
                    if (size() > this.maxSize) {
                        this.pathElements.remove(this.maxSize);
                    }
                    break;
                }

                // case when the new path is of the same length as the path Py
                // stored at index y
                if (newPathWeight == yPathElement.getWeight()) {
                    this.pathElements.add(yIndex + 1, newPathElement);
                    pathAdded = true;

                    // ensures max size limit is not exceeded.
                    if (size() > this.maxSize) {
                        this.pathElements.remove(this.maxSize);
                    }
                    break;
                }
            }

            // case when the new path is longer than the longest path in the
            // list (Py stored at the last index y)
            if (newPathWeight > yPathElement.getWeight()) {
                // ensures max size limit is not exceeded.
                if (size() < this.maxSize) {
                    // the new path is inserted at the end of the list.
                    this.pathElements.add(newPathElement);
                    pathAdded = true;
                } else {
                    // max size limit is reached -> end of the loop over the
                    // paths elements of the list at vertex v.
                    break;
                }
            }
        }

        return pathAdded;
    }

    /**
     * @return list of <code>RankingPathElement</code>.
     */
    List<RankingPathElement<V, E>> getPathElements()
    {
        return this.pathElements;
    }

    /**
     * Costs taken into account are the weights stored in <code>Edge</code> objects.
     *
     * @param pathElement
     * @param edge the edge via which the vertex was encountered.
     *
     * @return the cost obtained by concatenation.
     *
     * @see Graph#getEdgeWeight(E)
     */
    private double calculatePathWeight(RankingPathElement<V, E> pathElement, E edge)
    {
        double pathWeight = this.graph.getEdgeWeight(edge);

        // otherwise it's the start vertex.
        if ((pathElement.getPrevEdge() != null)) {
            pathWeight += pathElement.getWeight();
        }

        return pathWeight;
    }

    /**
     * Ensures that paths of the list do not disconnect the guard-vertex.
     *
     * @return <code>true</code> if the specified path element disconnects the guard-vertex,
     *         <code>false</code> otherwise.
     */
    private boolean isGuardVertexDisconnected(RankingPathElement<V, E> prevPathElement)
    {
        if (this.guardVertexToNotDisconnect == null) {
            return false;
        }

        if (this.path2disconnect.containsKey(prevPathElement)) {
            return this.path2disconnect.get(prevPathElement);
        }

        ConnectivityInspector<V, E> connectivityInspector;
        PathMask<V, E> connectivityMask = new PathMask<>(prevPathElement);

        MaskSubgraph<V, E> connectivityGraph = new MaskSubgraph<>(
            this.graph, connectivityMask::isVertexMasked, connectivityMask::isEdgeMasked);
        connectivityInspector = new ConnectivityInspector<>(connectivityGraph);

        if (connectivityMask.isVertexMasked(this.guardVertexToNotDisconnect)) {
            // the guard-vertex was already in the path element -> invalid path
            this.path2disconnect.put(prevPathElement, true);
            return true;
        }

        if (!connectivityInspector.pathExists(this.vertex, this.guardVertexToNotDisconnect)) {
            this.path2disconnect.put(prevPathElement, true);
            return true;
        }

        this.path2disconnect.put(prevPathElement, false);
        return false;
    }

    private boolean isNotValidPath(RankingPathElement<V, E> prevPathElement, E edge)
    {
        if (!isSimplePath(prevPathElement, edge)) {
            return true;
        }
        if (isGuardVertexDisconnected(prevPathElement)) {
            return true;
        }

        if (externalPathValidator != null) {
            GraphPath<V, E> prevPath;
            if (prevPathElement.getPrevEdge() == null) {
                prevPath = new GraphWalk<>(
                    graph, Collections.singletonList(prevPathElement.getVertex()),
                    prevPathElement.getWeight());
            } else {
                List<E> prevEdges = prevPathElement.createEdgeListPath();
                prevPath = new GraphWalk<V, E>(
                    graph, graph.getEdgeSource(prevEdges.get(0)), prevPathElement.getVertex(),
                    prevEdges, prevPathElement.getWeight());
            }

            if (!externalPathValidator.isValidPath(prevPath, edge)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Ensures that paths of the list are simple (check that the vertex was not already in the path
     * element).
     *
     * @param prevPathElement
     * @param edge
     *
     * @return <code>true</code> if the resulting path (obtained by concatenating the specified edge
     *         to the specified path) is simple, <code>
     * false</code> otherwise.
     */
    private boolean isSimplePath(RankingPathElement<V, E> prevPathElement, E edge)
    {
        V endVertex = Graphs.getOppositeVertex(this.graph, edge, prevPathElement.getVertex());
        assert (endVertex.equals(this.vertex));

        RankingPathElement<V, E> pathElementToTest = prevPathElement;
        do {
            if (pathElementToTest.getVertex().equals(endVertex)) {
                return false;
            } else {
                pathElementToTest = pathElementToTest.getPrevPathElement();
            }
        } while (pathElementToTest != null);

        return true;
    }

    private static class PathMask<V, E>
    {
        private Set<E> maskedEdges;

        private Set<V> maskedVertices;

        /**
         * Creates a mask for all the edges and the vertices of the path (including the 2 extremity
         * vertices).
         *
         * @param pathElement
         */
        PathMask(RankingPathElement<V, E> pathElement)
        {
            this.maskedEdges = new HashSet<>();
            this.maskedVertices = new HashSet<>();

            while (pathElement.getPrevEdge() != null) {
                this.maskedEdges.add(pathElement.getPrevEdge());
                this.maskedVertices.add(pathElement.getVertex());
                pathElement = pathElement.getPrevPathElement();
            }
            this.maskedVertices.add(pathElement.getVertex());
        }

        public boolean isEdgeMasked(E edge)
        {
            return this.maskedEdges.contains(edge);
        }

        public boolean isVertexMasked(V vertex)
        {
            return this.maskedVertices.contains(vertex);
        }
    }
}
