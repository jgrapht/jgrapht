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
 * Helper class for {@link KShortestSimplePaths}.
 *
 */
class KShortestSimplePathsIterator<V, E>
    implements
    Iterator<Set<V>>
{
    /**
     * End vertex.
     */
    private V endVertex;

    /**
     * Graph on which shortest paths are searched.
     */
    private Graph<V, E> graph;

    /**
     * Number of paths stored at each end vertex.
     */
    private int k;

    /**
     * Vertices whose ranking shortest paths have been modified during the previous pass.
     */
    private Set<V> prevImprovedVertices;

    /**
     * Stores the paths that improved the vertex in the previous pass.
     */
    private Map<V, RankingPathElementList<V, E>> prevSeenDataContainer;

    /**
     * Stores the vertices that have been seen during iteration and (optionally) some additional
     * traversal info regarding each vertex. Key = vertex, value =
     * <code>RankingPathElementList</code> list of calculated paths.
     */
    private Map<V, RankingPathElementList<V, E>> seenDataContainer;

    /**
     * Performs path validations in addition to the basics (source and target are connected w/o
     * loops)
     * 
     */
    private PathValidator<V, E> pathValidator = null;

    /**
     * Start vertex.
     */
    private V startVertex;

    private boolean startVertexEncountered;

    /**
     * Stores the number of the path.
     */
    private int passNumber = 1;

    /**
     * @param graph graph on which shortest paths are searched.
     * @param startVertex start vertex of the calculated paths.
     * @param endVertex end vertex of the calculated paths.
     * @param maxSize number of paths stored at end vertex of the graph.
     */
    public KShortestSimplePathsIterator(Graph<V, E> graph, V startVertex, V endVertex, int maxSize)
    {
        this(graph, startVertex, endVertex, maxSize, null);
    }

    /**
     * @param graph graph on which shortest paths are searched.
     * @param startVertex start vertex of the calculated paths.
     * @param endVertex end vertex of the calculated paths.
     * @param maxSize number of paths stored at end vertex of the graph.
     * @param pathValidator the path validator to use
     */
    public KShortestSimplePathsIterator(
        Graph<V, E> graph, V startVertex, V endVertex, int maxSize,
        PathValidator<V, E> pathValidator)
    {
        assertKShortestPathsIterator(graph, startVertex);

        this.graph = graph;
        this.startVertex = startVertex;
        this.endVertex = endVertex;

        this.k = maxSize;

        this.seenDataContainer = new HashMap<>();
        this.prevSeenDataContainer = new HashMap<>();

        this.prevImprovedVertices = new HashSet<>();
        this.pathValidator = pathValidator;
    }

    /**
     * @return <code>true</code> if at least one path has been improved during the previous pass,
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean hasNext()
    {
        if (!this.startVertexEncountered) {
            encounterStartVertex();
        }

        return !(this.prevImprovedVertices.isEmpty());
    }

    /**
     * Returns the list of vertices whose path has been improved during the current pass. Complexity
     * =
     *
     * <ul>
     * <li>O(<code>m*k*(m+n)</code>) where <code>k</code> is the maximum number of shortest paths to
     * compute, <code>m</code> is the number of edges of the graph and <code>n</code> is the number
     * of vertices of the graph</li>
     * </ul>
     *
     * @see java.util.Iterator#next()
     */
    @Override
    public Set<V> next()
    {
        if (!this.startVertexEncountered) {
            encounterStartVertex();
        }

        // at the i-th pass the shortest paths with i edges are calculated.
        if (hasNext()) {
            Set<V> improvedVertices = new HashSet<>();

            for (V vertex : this.prevImprovedVertices) {
                if (!vertex.equals(this.endVertex)) {
                    updateOutgoingVertices(vertex, improvedVertices);
                }
            }

            savePassData(improvedVertices);
            this.passNumber++;

            return improvedVertices;
        }
        throw new NoSuchElementException();
    }

    /**
     * Unsupported.
     *
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the path elements of the ranking shortest paths with less than <code>nMaxHops</code>
     * edges between the start vertex and the end vertex.
     *
     * @param endVertex end vertex.
     *
     * @return list of <code>RankingPathElement</code>, or <code>null</code> of no path exists
     *         between the start vertex and the end vertex.
     */
    RankingPathElementList<V, E> getPathElements(V endVertex)
    {
        return this.seenDataContainer.get(endVertex);
    }

    private void assertKShortestPathsIterator(Graph<V, E> graph, V startVertex)
    {
        if (graph == null) {
            throw new NullPointerException("graph is null");
        }
        if (startVertex == null) {
            throw new NullPointerException("startVertex is null");
        }
    }

    /**
     * The first time we see a vertex, make up a new entry for it.
     *
     * @param vertex a vertex which has just been encountered.
     * @param edge the edge via which the vertex was encountered.
     *
     * @return the new entry.
     */
    private RankingPathElementList<V, E> createSeenData(V vertex, E edge)
    {
        V oppositeVertex = Graphs.getOppositeVertex(this.graph, edge, vertex);

        RankingPathElementList<V, E> oppositeData = this.prevSeenDataContainer.get(oppositeVertex);

        // endVertex in argument to ensure that stored paths do not disconnect
        // the end-vertex

        return new RankingPathElementList<>(
            this.graph, this.k, oppositeData, edge, this.endVertex, this.pathValidator);
    }

    /**
     * Initializes the list of paths at the start vertex and adds an empty path.
     */
    private void encounterStartVertex()
    {
        RankingPathElementList<V, E> data = new RankingPathElementList<>(
            this.graph, this.k, new RankingPathElement<>(this.startVertex), this.pathValidator);

        this.seenDataContainer.put(this.startVertex, data);
        this.prevSeenDataContainer.put(this.startVertex, data);

        // initially the only vertex whose value is considered to have changed
        // is the start vertex
        this.prevImprovedVertices.add(this.startVertex);

        this.startVertexEncountered = true;
    }

    private void savePassData(Set<V> improvedVertices)
    {
        for (V vertex : improvedVertices) {
            RankingPathElementList<V, E> pathElementList = this.seenDataContainer.get(vertex);

            RankingPathElementList<V, E> improvedPaths = new RankingPathElementList<>(
                this.graph, pathElementList.maxSize, vertex, this.pathValidator);

            for (RankingPathElement<V, E> path : pathElementList) {
                if (path.getHopCount() == this.passNumber) {
                    // the path has just been computed.
                    improvedPaths.pathElements.add(path);
                }
            }

            this.prevSeenDataContainer.put(vertex, improvedPaths);
        }

        this.prevImprovedVertices = improvedVertices;
    }

    /**
     * Try to add the first paths to the specified vertex. These paths reached the specified vertex
     * and ended with the specified edge. A new intermediary path is stored in the paths list of the
     * specified vertex provided that the path can be extended to the end-vertex.
     *
     * @param vertex vertex reached by a path.
     * @param edge edge reaching the vertex.
     */
    private boolean tryToAddFirstPaths(V vertex, E edge)
    {
        // the vertex has not been reached yet
        RankingPathElementList<V, E> data = createSeenData(vertex, edge);

        if (!data.isEmpty()) {
            this.seenDataContainer.put(vertex, data);
            return true;
        }
        return false;
    }

    /**
     * Try to add new paths for the vertex. These new paths reached the specified vertex and ended
     * with the specified edge. A new intermediary path is stored in the paths list of the specified
     * vertex provided that the path can be extended to the end-vertex.
     *
     * @param vertex a vertex which has just been encountered.
     * @param edge the edge via which the vertex was encountered.
     */
    private boolean tryToAddNewPaths(V vertex, E edge)
    {
        RankingPathElementList<V, E> data = this.seenDataContainer.get(vertex);

        V oppositeVertex = Graphs.getOppositeVertex(this.graph, edge, vertex);
        RankingPathElementList<V, E> oppositeData = this.prevSeenDataContainer.get(oppositeVertex);

        return data.addPathElements(oppositeData, edge);
    }

    /**
     * <p>
     * Updates outgoing vertices of the vertex. For each outgoing vertex, the new paths are obtained
     * by concatenating the specified edge to the calculated paths of the specified vertex. If the
     * weight of a new path is greater than the weight of any path stored so far at the outgoing
     * vertex then the path is not added, otherwise it is added to the list of paths in increasing
     * order of weight.
     * </p>
     *
     * Complexity =
     *
     * <ul>
     * <li>$O(d(v) \cdot k \cdot (m+n))$ where <code>d(v)</code> is the outgoing degree of the
     * specified vertex, <code>k</code> is the maximum number of shortest paths to compute,
     * <code>m</code> is the number of edges of the graph and <code>n</code> is the number of
     * vertices of the graph</li>
     * </ul>
     *
     * @param vertex
     * @param improvedVertices
     */
    private void updateOutgoingVertices(V vertex, Set<V> improvedVertices)
    {
        // try to add new paths for the target vertices of the outgoing edges
        // of the vertex in argument.
        for (E edge : this.graph.outgoingEdgesOf(vertex)) {
            V vertexReachedByEdge = Graphs.getOppositeVertex(this.graph, edge, vertex);

            // check if the path does not loop over the start vertex.
            if (!vertexReachedByEdge.equals(this.startVertex)) {
                if (this.seenDataContainer.containsKey(vertexReachedByEdge)) {
                    boolean relaxed = tryToAddNewPaths(vertexReachedByEdge, edge);
                    if (relaxed) {
                        improvedVertices.add(vertexReachedByEdge);
                    }
                } else {
                    boolean relaxed = tryToAddFirstPaths(vertexReachedByEdge, edge);
                    if (relaxed) {
                        improvedVertices.add(vertexReachedByEdge);
                    }
                }
            }
        }
    }
}
