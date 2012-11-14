/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
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

import com.google.common.collect.Maps;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


/**
 * Helper class for {@link BellmanFordShortestPath}; not intended for general
 * use.
 */
class BellmanFordIterator<V, E>
    implements Iterator<List<V>>
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     * Error message.
     */
    protected final static String NEGATIVE_UNDIRECTED_EDGE =
        "Negative"
        + "edge-weights are not allowed in an unidrected graph!";

    //~ Instance fields --------------------------------------------------------

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

    private final double epsilon;

    //~ Constructors -----------------------------------------------------------

    /**
     * @param graph
     * @param startVertex start vertex.
     * @param epsilon tolerance factor.
     */
    protected BellmanFordIterator(
        final Graph<V, E> graph,
        final V startVertex,
        final double epsilon)
    {
        assertBellmanFordIterator(graph, startVertex);

        this.graph = graph;
        this.startVertex = startVertex;
        this.epsilon = epsilon;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the path element of the shortest path with less than <code>
     * nMaxHops</code> edges between the start vertex and the end vertex.
     *
     * @param endVertex end vertex.
     *
     * @return .
     */
    public BellmanFordPathElement<V, E> getPathElement(final V endVertex)
    {
        return getSeenData(endVertex);
    }

    /**
     * @return <code>true</code> if at least one path has been improved during
     * the previous pass, <code>false</code> otherwise.
     */
    @Override
    public boolean hasNext()
    {
        if (!startVertexEncountered) {
            encounterStartVertex();
        }

        return !prevImprovedVertices.isEmpty();
    }

    /**
     * Returns the list <code>Collection</code> of vertices whose path has been
     * improved during the current pass.
     *
     * @see Iterator#next()
     */
    @Override
    public List<V> next()
    {
        if (!startVertexEncountered) {
            encounterStartVertex();
        }

        if (hasNext()) {
            final List<V> improvedVertices = new ArrayList<V>();
            for (int i = prevImprovedVertices.size() - 1; i >= 0; i--) {
                final V vertex = prevImprovedVertices.get(i);
                for (
                    Iterator<? extends E> iter = edgesOfIterator(vertex);
                    iter.hasNext();)
                {
                    final E edge = iter.next();
                    final V oppositeVertex =
                        Graphs.getOppositeVertex(
                            graph,
                            edge,
                            vertex);
                    if (getPathElement(oppositeVertex) != null) {
                        final boolean relaxed =
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
     * @see Iterator#remove()
     */
    @Override
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
    protected void assertValidEdge(final E edge)
    {
        if (graph instanceof UndirectedGraph<?, ?>) {
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
    protected double calculatePathCost(final V vertex, final E edge)
    {
        final V oppositeVertex = Graphs.getOppositeVertex(graph, edge, vertex);

        // we get the data of the previous pass.
        final BellmanFordPathElement<V, E> oppositePrevData =
            getPrevSeenData(oppositeVertex);

        double pathCost = graph.getEdgeWeight(edge);

        if (!oppositePrevData.getVertex().equals(startVertex)) {
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
    protected Iterator<E> edgesOfIterator(final V vertex)
    {
        if (graph instanceof DirectedGraph<?, ?>) {
            return ((DirectedGraph<V, E>) graph).outgoingEdgesOf(vertex)
                .iterator();
        } else {
            return graph.edgesOf(vertex).iterator();
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
    protected BellmanFordPathElement<V, E> getPrevSeenData(final V vertex)
    {
        return prevVertexData.get(vertex);
    }

    /**
     * Access the data stored for a seen vertex in the current pass.
     *
     * @param vertex a vertex which has already been seen.
     *
     * @return data associated with the seen vertex or <code>null</code> if no
     * data was associated with the vertex.
     */
    protected BellmanFordPathElement<V, E> getSeenData(final V vertex)
    {
        return vertexData.get(vertex);
    }

    /**
     * Determines whether a vertex has been seen yet by this traversal.
     *
     * @param vertex vertex in question.
     *
     * @return <tt>true</tt> if vertex has already been seen.
     */
    protected boolean isSeenVertex(final V vertex)
    {
        return vertexData.containsKey(vertex);
    }

    /**
     * @param vertex
     * @param data
     *
     * @return .
     */
    protected BellmanFordPathElement<V, E> putPrevSeenData(
        final V vertex,
        final BellmanFordPathElement<V, E> data)
    {
        if (prevVertexData == null) {
            prevVertexData = Maps.newHashMap();
        }

        return prevVertexData.put(vertex, data);
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
        final V vertex,
        final BellmanFordPathElement<V, E> data)
    {
        if (vertexData == null) {
            vertexData = Maps.newHashMap();
        }

        return vertexData.put(vertex, data);
    }

    private void assertBellmanFordIterator(final Graph<V, E> graph, final V startVertex)
    {
        if (!graph.containsVertex(startVertex)) {
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
        final V vertex,
        final E edge,
        final double cost)
    {
        final BellmanFordPathElement<V, E> prevPathElement =
            getPrevSeenData(
                Graphs.getOppositeVertex(graph, edge, vertex));

        return new BellmanFordPathElement<V, E>(graph, prevPathElement, edge,
            cost, epsilon);
    }

    private void encounterStartVertex()
    {
        final BellmanFordPathElement<V, E> data =
            new BellmanFordPathElement<V, E>(startVertex,
                epsilon);

        // first the only vertex considered as improved is the start vertex.
        prevImprovedVertices.add(startVertex);

        putSeenData(startVertex, data);
        putPrevSeenData(startVertex, data);

        startVertexEncountered = true;
    }

    /**
     * Upates data first time a vertex is reached by a path.
     *
     * @param vertex a vertex which has just been encountered.
     * @param edge the edge via which the vertex was encountered.
     */
    private void relaxVertex(final V vertex, final E edge)
    {
        assertValidEdge(edge);

        final double shortestPathCost = calculatePathCost(vertex, edge);

        final BellmanFordPathElement<V, E> data =
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
    private boolean relaxVertexAgain(final V vertex, final E edge)
    {
        assertValidEdge(edge);

        final double candidateCost = calculatePathCost(vertex, edge);

        // we get the data of the previous pass.
        final BellmanFordPathElement<V, E> oppositePrevData =
            getPrevSeenData(
                Graphs.getOppositeVertex(graph, edge, vertex));

        final BellmanFordPathElement<V, E> pathElement = getSeenData(vertex);
        return pathElement.improve(oppositePrevData, edge, candidateCost);
    }

    private void savePassData(final List<V> improvedVertices)
    {
        for (final V vertex : improvedVertices) {
            final BellmanFordPathElement<V, E> orig = getSeenData(vertex);
            final BellmanFordPathElement<V, E> clonedData =
                new BellmanFordPathElement<V, E>(orig);
            putPrevSeenData(vertex, clonedData);
        }

        prevImprovedVertices = improvedVertices;
    }
}

// End BellmanFordIterator.java
