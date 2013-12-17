/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2009, by Barak Naveh and Contributors.
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
 * FloydWarshallShortestPaths.java
 * -------------------------
 * (C) Copyright 2009-2009, by Tom Larkworthy and Contributors
 *
 * Original Author:  Tom Larkworthy
 * Contributor(s):   Soren Davidsen
 *
 * $Id: FloydWarshallShortestPaths.java 755 2012-01-18 23:50:37Z perfecthash $
 *
 * Changes
 * -------
 * 29-Jun-2009 : Initial revision (TL);
 * 03-Dec-2009 : Optimized and enhanced version (SD);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;


/**
 * The <a href="http://en.wikipedia.org/wiki/Floyd-Warshall_algorithm">
 * Floyd-Warshall algorithm</a> finds all shortest paths (all n^2 of them) in
 * O(n^3) time. It can also calculate the graph diameter.
 *
 * @author Tom Larkworthy
 * @author Soren Davidsen <soren@tanesha.net>
 */
public class FloydWarshallShortestPaths<V, E>
{
    

    private Graph<V, E> graph;
    private List<V> vertices;
    private int nShortestPaths = 0;
    private double diameter = Double.NaN;
    private double [][] d = null;
    private int [][] backtrace = null;
    private Map<VertexPair<V>, GraphPath<V, E>> paths = null;

    

    public FloydWarshallShortestPaths(Graph<V, E> graph)
    {
        this.graph = graph;
        this.vertices = new ArrayList<V>(graph.vertexSet());
    }

    

    /**
     * @return the graph on which this algorithm operates
     */
    public Graph<V, E> getGraph()
    {
        return graph;
    }

    /**
     * @return total number of shortest paths
     */
    public int getShortestPathsCount()
    {
        lazyCalculatePaths();
        return nShortestPaths;
    }

    /**
     * Calculates the matrix of all shortest paths, but does not populate the
     * paths map.
     */
    private void lazyCalculateMatrix()
    {
        if (d != null) {
            // already done
            return;
        }

        int n = vertices.size();

        // init the backtrace matrix
        backtrace = new int[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(backtrace[i], -1);
        }

        // initialize matrix, 0
        d = new double[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(d[i], Double.POSITIVE_INFINITY);
        }

        // initialize matrix, 1
        for (int i = 0; i < n; i++) {
            d[i][i] = 0.0;
        }

        // initialize matrix, 2
        Set<E> edges = graph.edgeSet();
        for (E edge : edges) {
            V v1 = graph.getEdgeSource(edge);
            V v2 = graph.getEdgeTarget(edge);

            int v_1 = vertices.indexOf(v1);
            int v_2 = vertices.indexOf(v2);

            d[v_1][v_2] = graph.getEdgeWeight(edge);
            if (!(graph instanceof DirectedGraph<?, ?>)) {
                d[v_2][v_1] = graph.getEdgeWeight(edge);
            }
        }

        // run fw alg
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    double ik_kj = d[i][k] + d[k][j];
                    if (ik_kj < d[i][j]) {
                        d[i][j] = ik_kj;
                        backtrace[i][j] = k;
                    }
                }
            }
        }
    }

    /**
     * Get the length of a shortest path.
     *
     * @param a first vertex
     * @param b second vertex
     *
     * @return shortest distance between a and b
     */
    public double shortestDistance(V a, V b)
    {
        lazyCalculateMatrix();

        return d[vertices.indexOf(a)][vertices.indexOf(b)];
    }

    /**
     * @return the diameter (longest of all the shortest paths) computed for the
     * graph. If the graph is vertexless, return 0.0.
     */
    public double getDiameter()
    {
        lazyCalculateMatrix();

        if (Double.isNaN(diameter)) {
            diameter = 0.0;
            int n = vertices.size();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (!Double.isInfinite(d[i][j]) && (d[i][j] > diameter)) {
                        diameter = d[i][j];
                    }
                }
            }
        }
        return diameter;
    }

    private void shortestPathRecur(List<E> edges, int v_a, int v_b)
    {
        int k = backtrace[v_a][v_b];
        if (k == -1) {
            E edge = graph.getEdge(vertices.get(v_a), vertices.get(v_b));
            if (edge != null) {
                edges.add(edge);
            }
        } else {
            shortestPathRecur(edges, v_a, k);
            shortestPathRecur(edges, k, v_b);
        }
    }

    /**
     * Get the shortest path between two vertices. Note: The paths are
     * calculated using a recursive algorithm. It *will* give problems on paths
     * longer than the stack allows.
     *
     * @param a From vertice
     * @param b To vertice
     *
     * @return the path, or null if none found
     */
    public GraphPath<V, E> getShortestPath(V a, V b)
    {
        lazyCalculatePaths();
        return getShortestPathImpl(a, b);
    }

    private GraphPath<V, E> getShortestPathImpl(V a, V b)
    {
        int v_a = vertices.indexOf(a);
        int v_b = vertices.indexOf(b);

        List<E> edges = new ArrayList<E>();
        shortestPathRecur(edges, v_a, v_b);

        // no path, return null
        if (edges.size() < 1) {
            return null;
        }

        double weight = 0.;
        for (E e : edges) {
            weight += graph.getEdgeWeight(e);
        }

        GraphPathImpl<V, E> path =
            new GraphPathImpl<V, E>(graph, a, b, edges, weight);

        return path;
    }

    /**
     * Calculate the shortest paths (not done per default)
     */
    private void lazyCalculatePaths()
    {
        // already we have calculated it once.
        if (paths != null) {
            return;
        }

        lazyCalculateMatrix();

        Map<VertexPair<V>, GraphPath<V, E>> sps =
            new HashMap<VertexPair<V>, GraphPath<V, E>>();
        int n = vertices.size();

        nShortestPaths = 0;
        for (int i = 0; i < n; i++) {
            V v_i = vertices.get(i);
            for (int j = 0; j < n; j++) {
                // don't count this.
                if (i == j) {
                    continue;
                }

                V v_j = vertices.get(j);

                GraphPath<V, E> path = getShortestPathImpl(v_i, v_j);

                // we got a path
                if (path != null) {
                    sps.put(new VertexPair<V>(v_i, v_j), path);
                    nShortestPaths++;
                }
            }
        }

        this.paths = sps;
    }

    /**
     * Get shortest paths from a vertex to all other vertices in the graph.
     *
     * @param v the originating vertex
     *
     * @return List of paths
     */
    public List<GraphPath<V, E>> getShortestPaths(V v)
    {
        lazyCalculatePaths();
        List<GraphPath<V, E>> found = new ArrayList<GraphPath<V, E>>();

        // TODO:  two-level map for paths so that we don't have to
        // iterate over all paths here!
        for (VertexPair<V> pair : paths.keySet()) {
            if (pair.getFirst().equals(v)) {
                found.add(paths.get(pair));
            }
        }

        return found;
    }

    /**
     * Get all shortest paths in the graph.
     *
     * @return List of paths
     */
    public Collection<GraphPath<V, E>> getShortestPaths()
    {
        lazyCalculatePaths();
        return paths.values();
    }
}

// End FloydWarshallShortestPaths.java
