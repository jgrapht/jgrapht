/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2010, by Barak Naveh and Contributors.
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
/* -------------------------
 * KShortestPathsIterator.java
 * -------------------------
 * (C) Copyright 2007-2010, by France Telecom
 *
 * Original Author:  Guillaume Boulmier and Contributors.
 * Contributor(s):   John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Jun-2007 : Initial revision (GB);
 * 05-Jul-2007 : Added support for generics (JVS);
 * 06-Dec-2010 : Bugfixes (GB);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;


/**
 * Helper class for {@link KShortestPaths}.
 *
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
class KShortestPathsIterator<V, E>
    implements Iterator<Set<V>>
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
     * Vertices whose ranking shortest paths have been modified during the
     * previous pass.
     */
    private Set<V> prevImprovedVertices;

    /**
     * Stores the paths that improved the vertex in the previous pass.
     */
    private Map<V, RankingPathElementList<V, E>> prevSeenDataContainer;

    /**
     * Stores the vertices that have been seen during iteration and (optionally)
     * some additional traversal info regarding each vertex. Key = vertex, value
     * = <code>RankingPathElementList</code> list of calculated paths.
     */
    private Map<V, RankingPathElementList<V, E>> seenDataContainer;

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
    public KShortestPathsIterator(
        Graph<V, E> graph,
        V startVertex,
        V endVertex,
        int maxSize)
    {
        assertKShortestPathsIterator(graph, startVertex);

        this.graph = graph;
        this.startVertex = startVertex;
        this.endVertex = endVertex;

        this.k = maxSize;

        this.seenDataContainer = new HashMap<V, RankingPathElementList<V, E>>();
        this.prevSeenDataContainer =
            new HashMap<V, RankingPathElementList<V, E>>();

        this.prevImprovedVertices = new HashSet<V>();
    }

    

    /**
     * @return <code>true</code> if at least one path has been improved during
     * the previous pass, <code>false</code> otherwise.
     */
    public boolean hasNext()
    {
        if (!this.startVertexEncountered) {
            encounterStartVertex();
        }

        return !(this.prevImprovedVertices.isEmpty());
    }

    /**
     * Returns the list of vertices whose path has been improved during the
     * current pass. Complexity =
     *
     * <ul>
     * <li>O(<code>m*k*(m+n)</code>) where <code>k</code> is the maximum number
     * of shortest paths to compute, <code>m</code> is the number of edges of
     * the graph and <code>n</code> is the number of vertices of the graph</li>
     * </ul>
     *
     * @see java.util.Iterator#next()
     */
    public Set<V> next()
    {
        if (!this.startVertexEncountered) {
            encounterStartVertex();
        }

        // at the i-th pass the shortest paths with i edges are calculated.
        if (hasNext()) {
            Set<V> improvedVertices = new HashSet<V>();

            for (
                Iterator<V> iter = this.prevImprovedVertices.iterator();
                iter.hasNext();)
            {
                V vertex = iter.next();
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
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the path elements of the ranking shortest paths with less than
     * <code>nMaxHops</code> edges between the start vertex and the end vertex.
     *
     * @param endVertex end vertex.
     *
     * @return list of <code>RankingPathElement</code>, or <code>null</code> of
     * no path exists between the start vertex and the end vertex.
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

        RankingPathElementList<V, E> oppositeData =
            this.prevSeenDataContainer.get(oppositeVertex);

        // endVertex in argument to ensure that stored paths do not disconnect
        // the end-vertex
        RankingPathElementList<V, E> data =
            new RankingPathElementList<V, E>(
                this.graph,
                this.k,
                oppositeData,
                edge,
                this.endVertex);

        return data;
    }

    /**
     * Returns an iterator to loop over outgoing edges <code>Edge</code> of the
     * vertex.
     *
     * @param vertex
     *
     * @return .
     */
    private Iterator<E> edgesOfIterator(V vertex)
    {
        if (this.graph instanceof DirectedGraph<?, ?>) {
            return ((DirectedGraph<V, E>) this.graph).outgoingEdgesOf(vertex)
                .iterator();
        } else {
            return this.graph.edgesOf(vertex).iterator();
        }
    }

    /**
     * Initializes the list of paths at the start vertex and adds an empty path.
     */
    private void encounterStartVertex()
    {
        RankingPathElementList<V, E> data =
            new RankingPathElementList<V, E>(
                this.graph,
                this.k,
                new RankingPathElement<V, E>(
                    this.startVertex));

        this.seenDataContainer.put(this.startVertex, data);
        this.prevSeenDataContainer.put(this.startVertex, data);

        // initially the only vertex whose value is considered to have changed
        // is the start vertex
        this.prevImprovedVertices.add(this.startVertex);

        this.startVertexEncountered = true;
    }

    private void savePassData(Set<V> improvedVertices)
    {
        for (Iterator<V> iter = improvedVertices.iterator(); iter.hasNext();) {
            V vertex = iter.next();

            RankingPathElementList<V, E> pathElementList =
                this.seenDataContainer.get(vertex);

            RankingPathElementList<V, E> improvedPaths =
                new RankingPathElementList<V, E>(
                    this.graph,
                    pathElementList.maxSize,
                    vertex);

            for (
                Iterator<RankingPathElement<V, E>> pathIter =
                    pathElementList.iterator();
                pathIter.hasNext();)
            {
                RankingPathElement<V, E> path = pathIter.next();
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
     * Try to add the first paths to the specified vertex. These paths reached
     * the specified vertex and ended with the specified edge. A new
     * intermediary path is stored in the paths list of the specified vertex
     * provided that the path can be extended to the end-vertex.
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
     * Try to add new paths for the vertex. These new paths reached the
     * specified vertex and ended with the specified edge. A new intermediary
     * path is stored in the paths list of the specified vertex provided that
     * the path can be extended to the end-vertex.
     *
     * @param vertex a vertex which has just been encountered.
     * @param edge the edge via which the vertex was encountered.
     */
    private boolean tryToAddNewPaths(V vertex, E edge)
    {
        RankingPathElementList<V, E> data = this.seenDataContainer.get(vertex);

        V oppositeVertex = Graphs.getOppositeVertex(this.graph, edge, vertex);
        RankingPathElementList<V, E> oppositeData =
            this.prevSeenDataContainer.get(oppositeVertex);

        return data.addPathElements(oppositeData, edge);
    }

    /**
     * <p>Updates outgoing vertices of the vertex. For each outgoing vertex, the
     * new paths are obtained by concatenating the specified edge to the
     * calculated paths of the specified vertex. If the weight of a new path is
     * greater than the weight of any path stored so far at the outgoing vertex
     * then the path is not added, otherwise it is added to the list of paths in
     * increasing order of weight.</p>
     *
     * Complexity =
     *
     * <ul>
     * <li>O(<code>d(v)*k*(m+n)</code>) where <code>d(v)</code> is the outgoing
     * degree of the specified vertex, <code>k</code> is the maximum number of
     * shortest paths to compute, <code>m</code> is the number of edges of the
     * graph and <code>n</code> is the number of vertices of the graph</li>
     * </ul>
     *
     * @param vertex
     * @param improvedVertices
     */
    private void updateOutgoingVertices(V vertex, Set<V> improvedVertices)
    {
        // try to add new paths for the target vertices of the outgoing edges
        // of the vertex in argument.
        for (Iterator<E> iter = edgesOfIterator(vertex); iter.hasNext();) {
            E edge = iter.next();
            V vertexReachedByEdge =
                Graphs.getOppositeVertex(this.graph, edge,
                    vertex);

            // check if the path does not loop over the start vertex.
            if (!vertexReachedByEdge.equals(this.startVertex)) {
                if (this.seenDataContainer.containsKey(vertexReachedByEdge)) {
                    boolean relaxed =
                        tryToAddNewPaths(vertexReachedByEdge,
                            edge);
                    if (relaxed) {
                        improvedVertices.add(vertexReachedByEdge);
                    }
                } else {
                    boolean relaxed =
                        tryToAddFirstPaths(vertexReachedByEdge,
                            edge);
                    if (relaxed) {
                        improvedVertices.add(vertexReachedByEdge);
                    }
                }
            }
        }
    }
}

// End KShortestPathsIterator.java
