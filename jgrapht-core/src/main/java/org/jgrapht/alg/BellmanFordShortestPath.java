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

import org.jgrapht.Graph;

import java.util.List;


/**
 * <a href="http://www.nist.gov/dads/HTML/bellmanford.html">Bellman-Ford
 * algorithm</a>: weights could be negative, paths could be constrained by a
 * maximum number of edges.
 */
public class BellmanFordShortestPath<V, E>
{
    //~ Static fields/initializers ---------------------------------------------

    private static final double DEFAULT_EPSILON = 0.000000001;

    //~ Instance fields --------------------------------------------------------

    /**
     * Graph on which shortest paths are searched.
     */
    protected final Graph<V, E> graph;

    /**
     * Start vertex.
     */
    protected final V startVertex;

    private BellmanFordIterator<V, E> iter;

    /**
     * Maximum number of edges of the calculated paths.
     */
    private final int nMaxHops;

    private int passNumber;

    private final double epsilon;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates an object to calculate shortest paths between the start vertex
     * and others vertices using the Bellman-Ford algorithm.
     *
     * @param graph
     * @param startVertex
     */
    public BellmanFordShortestPath(final Graph<V, E> graph, final V startVertex)
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
        final Graph<V, E> graph,
        final V startVertex,
        final int nMaxHops)
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
        final Graph<V, E> graph,
        final V startVertex,
        final int nMaxHops,
        final double epsilon)
    {
        this.startVertex = startVertex;
        this.nMaxHops = nMaxHops;
        this.graph = graph;
        passNumber = 1;
        this.epsilon = epsilon;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @param endVertex end vertex.
     *
     * @return the cost of the shortest path between the start vertex and the
     * end vertex.
     */
    public double getCost(final V endVertex)
    {
        assertGetPath(endVertex);

        lazyCalculate();

        final BellmanFordPathElement<V, E> pathElement = iter
            .getPathElement(endVertex);

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
    public List<E> getPathEdgeList(final V endVertex)
    {
        assertGetPath(endVertex);

        lazyCalculate();

        final BellmanFordPathElement<V, E> pathElement = iter
            .getPathElement(endVertex);

        if (pathElement == null) {
            return null;
        }

        return pathElement.createEdgeListPath();
    }

    private void assertGetPath(final V endVertex)
    {
        if (endVertex.equals(startVertex)) {
            throw new IllegalArgumentException(
                "The end vertex is the same as the start vertex!");
        }

        if (!graph.containsVertex(endVertex)) {
            throw new IllegalArgumentException(
                "Graph must contain the end vertex!");
        }
    }

    private void lazyCalculate()
    {
        if (iter == null) {
            iter =
                new BellmanFordIterator<V, E>(graph, startVertex,
                    epsilon);
        }

        // at the i-th pass the shortest paths with less (or equal) than i edges
        // are calculated.
        for (
            ;
            passNumber <= nMaxHops && iter.hasNext(); passNumber++)
        {
            iter.next();
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
        final Graph<V, E> graph,
        final V startVertex,
        final V endVertex)
    {
        final BellmanFordShortestPath<V, E> alg =
            new BellmanFordShortestPath<V, E>(
                graph,
                startVertex);

        return alg.getPathEdgeList(endVertex);
    }
}

// End BellmanFordShortestPath.java
