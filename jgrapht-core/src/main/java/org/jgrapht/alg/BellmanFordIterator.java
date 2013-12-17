/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
 * BellmanFordIterator.java
 * -------------------------
 * (C) Copyright 2006-2008, by France Telecom and Contributors.
 *
 * Original Author:  Guillaume Boulmier and Contributors.
 * Contributor(s):   John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Jan-2006 : Initial revision (GB);
 * 14-Jan-2006 : Added support for generics (JVS);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;


/**
 * Helper class for {@link BellmanFordShortestPath}; not intended for general
 * use.
 */
class BellmanFordIterator<V, E>
    implements Iterator<List<V>>
{
    

    /**
     * Error message.
     */
    protected final static String NEGATIVE_UNDIRECTED_EDGE =
        "Negative"
        + "edge-weights are not allowed in an unidrected graph!";

    

    /**
     * Graph on which shortest paths are searched.
     */
    protected Graph<V, E> graph;

    /**
     * Start vertex.
     */
    protected V startVertex;

    /**
     * Vertices whose shortest path cost have been improved during the previous
     * pass.
     */
    private List<V> prevImprovedVertices = new ArrayList<V>();

    private Map<V, BellmanFordPathElement<V, E>> prevVertexData;

    private boolean startVertexEncountered = false;

    /**
     * Stores the vertices that have been seen during iteration and (optionally)
     * some additional traversal info regarding each vertex.
     */
    private Map<V, BellmanFordPathElement<V, E>> vertexData;

    private double epsilon;

    

    /**
     * @param graph
     * @param startVertex start vertex.
     * @param epsilon tolerance factor.
     */
    protected BellmanFordIterator(
        Graph<V, E> graph,
        V startVertex,
        double epsilon)
    {
        assertBellmanFordIterator(graph, startVertex);

        this.graph = graph;
        this.startVertex = startVertex;
        this.epsilon = epsilon;
    }

    

    /**
     * Returns the path element of the shortest path with less than <code>
     * nMaxHops</code> edges between the start vertex and the end vertex.
     *
     * @param endVertex end vertex.
     *
     * @return .
     */
    public BellmanFordPathElement<V, E> getPathElement(V endVertex)
    {
        return getSeenData(endVertex);
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
     * Returns the list <code>Collection</code> of vertices whose path has been
     * improved during the current pass.
     *
     * @see java.util.Iterator#next()
     */
    public List<V> next()
    {
        if (!this.startVertexEncountered) {
            encounterStartVertex();
        }

        if (hasNext()) {
            List<V> improvedVertices = new ArrayList<V>();
            for (int i = this.prevImprovedVertices.size() - 1; i >= 0; i--) {
                V vertex = this.prevImprovedVertices.get(i);
                for (
                    Iterator<? extends E> iter = edgesOfIterator(vertex);
                    iter.hasNext();)
                {
                    E edge = iter.next();
                    V oppositeVertex =
                        Graphs.getOppositeVertex(
                            graph,
                            edge,
                            vertex);
                    if (getPathElement(oppositeVertex) != null) {
                        boolean relaxed =
                            relaxVertexAgain(oppositeVertex, edge);
                        if (relaxed) {
                            improvedVertices.add(oppositeVertex);
                        }
                    } else {
                        relaxVertex(oppositeVertex, edge);
                        improvedVertices.add(oppositeVertex);
                    }
                }
            }

            savePassData(improvedVertices);

            return improvedVertices;
        }

        throw new NoSuchElementException();
    }

    /**
     * Unsupported
     *
     * @see java.util.Iterator#remove()
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @param edge
     *
     * @throws IllegalArgumentException if the graph is undirected and the
     * edge-weight is negative.
     */
    protected void assertValidEdge(E edge)
    {
        if (this.graph instanceof UndirectedGraph<?, ?>) {
            if (graph.getEdgeWeight(edge) < 0) {
                throw new IllegalArgumentException(NEGATIVE_UNDIRECTED_EDGE);
            }
        }
    }

    /**
     * Costs taken into account are the weights stored in <code>Edge</code>
     * objects.
     *
     * @param vertex a vertex which has just been encountered.
     * @param edge the edge via which the vertex was encountered.
     *
     * @return the cost obtained by concatenation.
     *
     * @see Graph#getEdgeWeight(E)
     */
    protected double calculatePathCost(V vertex, E edge)
    {
        V oppositeVertex = Graphs.getOppositeVertex(graph, edge, vertex);

        // we get the data of the previous pass.
        BellmanFordPathElement<V, E> oppositePrevData =
            getPrevSeenData(oppositeVertex);

        double pathCost = graph.getEdgeWeight(edge);

        if (!oppositePrevData.getVertex().equals(this.startVertex)) {
            // if it's not the start vertex, we add the cost of the previous
            // pass.
            pathCost += oppositePrevData.getCost();
        }

        return pathCost;
    }

    /**
     * Returns an iterator to loop over outgoing edges <code>Edge</code> of the
     * vertex.
     *
     * @param vertex
     *
     * @return .
     */
    protected Iterator<E> edgesOfIterator(V vertex)
    {
        if (this.graph instanceof DirectedGraph<?, ?>) {
            return ((DirectedGraph<V, E>) this.graph).outgoingEdgesOf(vertex)
                .iterator();
        } else {
            return this.graph.edgesOf(vertex).iterator();
        }
    }

    /**
     * Access the data stored for a seen vertex in the previous pass.
     *
     * @param vertex a vertex which has already been seen.
     *
     * @return data associated with the seen vertex or <code>null</code> if no
     * data was associated with the vertex.
     */
    protected BellmanFordPathElement<V, E> getPrevSeenData(V vertex)
    {
        return this.prevVertexData.get(vertex);
    }

    /**
     * Access the data stored for a seen vertex in the current pass.
     *
     * @param vertex a vertex which has already been seen.
     *
     * @return data associated with the seen vertex or <code>null</code> if no
     * data was associated with the vertex.
     */
    protected BellmanFordPathElement<V, E> getSeenData(V vertex)
    {
        return this.vertexData.get(vertex);
    }

    /**
     * Determines whether a vertex has been seen yet by this traversal.
     *
     * @param vertex vertex in question.
     *
     * @return <tt>true</tt> if vertex has already been seen.
     */
    protected boolean isSeenVertex(V vertex)
    {
        return this.vertexData.containsKey(vertex);
    }

    /**
     * @param vertex
     * @param data
     *
     * @return .
     */
    protected BellmanFordPathElement<V, E> putPrevSeenData(
        V vertex,
        BellmanFordPathElement<V, E> data)
    {
        if (this.prevVertexData == null) {
            this.prevVertexData =
                new HashMap<V, BellmanFordPathElement<V, E>>();
        }

        return this.prevVertexData.put(vertex, data);
    }

    /**
     * Stores iterator-dependent data for a vertex that has been seen during the
     * current pass.
     *
     * @param vertex a vertex which has been seen.
     * @param data data to be associated with the seen vertex.
     *
     * @return previous value associated with specified vertex or <code>
     * null</code> if no data was associated with the vertex.
     */
    protected BellmanFordPathElement<V, E> putSeenData(
        V vertex,
        BellmanFordPathElement<V, E> data)
    {
        if (this.vertexData == null) {
            this.vertexData = new HashMap<V, BellmanFordPathElement<V, E>>();
        }

        return this.vertexData.put(vertex, data);
    }

    private void assertBellmanFordIterator(Graph<V, E> graph, V startVertex)
    {
        if (!(graph.containsVertex(startVertex))) {
            throw new IllegalArgumentException(
                "Graph must contain the start vertex!");
        }
    }

    /**
     * The first time we see a vertex, make up a new entry for it.
     *
     * @param vertex a vertex which has just been encountered.
     * @param edge the edge via which the vertex was encountered.
     * @param cost cost of the created path element.
     *
     * @return the new entry.
     */
    private BellmanFordPathElement<V, E> createSeenData(
        V vertex,
        E edge,
        double cost)
    {
        BellmanFordPathElement<V, E> prevPathElement =
            getPrevSeenData(
                Graphs.getOppositeVertex(graph, edge, vertex));

        BellmanFordPathElement<V, E> data =
            new BellmanFordPathElement<V, E>(
                graph,
                prevPathElement,
                edge,
                cost,
                epsilon);

        return data;
    }

    private void encounterStartVertex()
    {
        BellmanFordPathElement<V, E> data =
            new BellmanFordPathElement<V, E>(
                this.startVertex,
                epsilon);

        // first the only vertex considered as improved is the start vertex.
        this.prevImprovedVertices.add(this.startVertex);

        putSeenData(this.startVertex, data);
        putPrevSeenData(this.startVertex, data);

        this.startVertexEncountered = true;
    }

    /**
     * Upates data first time a vertex is reached by a path.
     *
     * @param vertex a vertex which has just been encountered.
     * @param edge the edge via which the vertex was encountered.
     */
    private void relaxVertex(V vertex, E edge)
    {
        assertValidEdge(edge);

        double shortestPathCost = calculatePathCost(vertex, edge);

        BellmanFordPathElement<V, E> data =
            createSeenData(vertex, edge,
                shortestPathCost);

        putSeenData(vertex, data);
    }

    /**
     * Check if the cost of the best path so far reaching the specified vertex
     * could be improved if the vertex is reached through the specified edge.
     *
     * @param vertex a vertex which has just been encountered.
     * @param edge the edge via which the vertex was encountered.
     *
     * @return <code>true</code> if the cost has been improved, <code>
     * false</code> otherwise.
     */
    private boolean relaxVertexAgain(V vertex, E edge)
    {
        assertValidEdge(edge);

        double candidateCost = calculatePathCost(vertex, edge);

        // we get the data of the previous pass.
        BellmanFordPathElement<V, E> oppositePrevData =
            getPrevSeenData(
                Graphs.getOppositeVertex(graph, edge, vertex));

        BellmanFordPathElement<V, E> pathElement = getSeenData(vertex);
        return pathElement.improve(oppositePrevData, edge, candidateCost);
    }

    private void savePassData(List<V> improvedVertices)
    {
        for (V vertex : improvedVertices) {
            BellmanFordPathElement<V, E> orig = getSeenData(vertex);
            BellmanFordPathElement<V, E> clonedData =
                new BellmanFordPathElement<V, E>(orig);
            putPrevSeenData(vertex, clonedData);
        }

        this.prevImprovedVertices = improvedVertices;
    }
}

// End BellmanFordIterator.java
