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
 * BellmanFordShortestPath.java
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
 * <a href="http://www.nist.gov/dads/HTML/bellmanford.html">Bellman-Ford
 * algorithm</a>: weights could be negative, paths could be constrained by a
 * maximum number of edges.
 */
public class BellmanFordShortestPath<V, E>
{
    

    private static final double DEFAULT_EPSILON = 0.000000001;

    

    /**
     * Graph on which shortest paths are searched.
     */
    protected Graph<V, E> graph;

    /**
     * Start vertex.
     */
    protected V startVertex;

    private BellmanFordIterator<V, E> iter;

    /**
     * Maximum number of edges of the calculated paths.
     */
    private int nMaxHops;

    private int passNumber;

    private double epsilon;

    

    /**
     * Creates an object to calculate shortest paths between the start vertex
     * and others vertices using the Bellman-Ford algorithm.
     *
     * @param graph
     * @param startVertex
     */
    public BellmanFordShortestPath(Graph<V, E> graph, V startVertex)
    {
        this(graph, startVertex, graph.vertexSet().size() - 1);
    }

    /**
     * Creates an object to calculate shortest paths between the start vertex
     * and others vertices using the Bellman-Ford algorithm.
     *
     * @param graph
     * @param startVertex
     * @param nMaxHops maximum number of edges of the calculated paths.
     */
    public BellmanFordShortestPath(
        Graph<V, E> graph,
        V startVertex,
        int nMaxHops)
    {
        this(graph, startVertex, nMaxHops, DEFAULT_EPSILON);
    }

    /**
     * Creates an object to calculate shortest paths between the start vertex
     * and others vertices using the Bellman-Ford algorithm.
     *
     * @param graph
     * @param startVertex
     * @param nMaxHops maximum number of edges of the calculated paths.
     * @param epsilon tolerance factor.
     */
    public BellmanFordShortestPath(
        Graph<V, E> graph,
        V startVertex,
        int nMaxHops,
        double epsilon)
    {
        this.startVertex = startVertex;
        this.nMaxHops = nMaxHops;
        this.graph = graph;
        this.passNumber = 1;
        this.epsilon = epsilon;
    }

    

    /**
     * @param endVertex end vertex.
     *
     * @return the cost of the shortest path between the start vertex and the
     * end vertex.
     */
    public double getCost(V endVertex)
    {
        assertGetPath(endVertex);

        lazyCalculate();

        BellmanFordPathElement<V, E> pathElement =
            this.iter.getPathElement(endVertex);

        if (pathElement == null) {
            return Double.POSITIVE_INFINITY;
        }

        return pathElement.getCost();
    }

    /**
     * @param endVertex end vertex.
     *
     * @return list of <code>Edge</code>, or null if no path exists between the
     * start vertex and the end vertex.
     */
    public List<E> getPathEdgeList(V endVertex)
    {
        assertGetPath(endVertex);

        lazyCalculate();

        BellmanFordPathElement<V, E> pathElement =
            this.iter.getPathElement(endVertex);

        if (pathElement == null) {
            return null;
        }

        return pathElement.createEdgeListPath();
    }

    private void assertGetPath(V endVertex)
    {
        if (endVertex.equals(this.startVertex)) {
            throw new IllegalArgumentException(
                "The end vertex is the same as the start vertex!");
        }

        if (!this.graph.containsVertex(endVertex)) {
            throw new IllegalArgumentException(
                "Graph must contain the end vertex!");
        }
    }

    private void lazyCalculate()
    {
        if (this.iter == null) {
            this.iter =
                new BellmanFordIterator<V, E>(
                    this.graph,
                    this.startVertex,
                    epsilon);
        }

        // at the i-th pass the shortest paths with less (or equal) than i edges
        // are calculated.
        for (
            ;
            (this.passNumber <= this.nMaxHops) && this.iter.hasNext();
            this.passNumber++)
        {
            this.iter.next();
        }
    }

    /**
     * Convenience method to find the shortest path via a single static method
     * call. If you need a more advanced search (e.g. limited by hops, or
     * computation of the path length), use the constructor instead.
     *
     * @param graph the graph to be searched
     * @param startVertex the vertex at which the path should start
     * @param endVertex the vertex at which the path should end
     *
     * @return List of Edges, or null if no path exists
     */
    public static <V, E> List<E> findPathBetween(
        Graph<V, E> graph,
        V startVertex,
        V endVertex)
    {
        BellmanFordShortestPath<V, E> alg =
            new BellmanFordShortestPath<V, E>(
                graph,
                startVertex);

        return alg.getPathEdgeList(endVertex);
    }
}

// End BellmanFordShortestPath.java
