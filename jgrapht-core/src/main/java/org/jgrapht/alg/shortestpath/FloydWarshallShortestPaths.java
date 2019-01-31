/*
 * (C) Copyright 2009-2018, by Tom Larkworthy and Contributors.
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
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * The Floyd-Warshall algorithm.
 * 
 * <p>
 * The <a href="http://en.wikipedia.org/wiki/Floyd-Warshall_algorithm"> Floyd-Warshall algorithm</a>
 * finds all shortest paths (all $n^2$ of them) in $O(n^3)$ time. Note that during construction
 * time, no computations are performed! All computations are performed the first time one of the
 * member methods of this class is invoked. The results are stored, so all subsequent calls to the
 * same method are computationally efficient.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Tom Larkworthy
 * @author Soren Davidsen (soren@tanesha.net)
 * @author Joris Kinable
 * @author Dimitrios Michail
 */
public class FloydWarshallShortestPaths<V, E>
    extends
    BaseShortestPathAlgorithm<V, E>
{
    private final List<V> vertices;
    private final List<Integer> degrees;
    private final Map<V, Integer> vertexIndices;
    // minimum vertex with degree at least 1
    private final int minDegreeOne;
    // minimum vertex with degree at least 2
    private final int minDegreeTwo;

    private double[][] d = null;
    private Object[][] backtrace = null;
    private Object[][] lastHopMatrix = null;

    /**
     * Create a new instance of the Floyd-Warshall all-pairs shortest path algorithm.
     * 
     * @param graph the input graph
     */
    public FloydWarshallShortestPaths(Graph<V, E> graph)
    {
        super(graph);

        /*
         * Sort vertices by degree in ascending order and index them. Also compute the minimum
         * vertex which has degree at least one and at least two.
         */
        this.vertices = new ArrayList<>(graph.vertexSet());
        Collections.sort(
            vertices, new VertexDegreeComparator<>(graph, VertexDegreeComparator.Order.ASCENDING));
        this.degrees = new ArrayList<>();
        this.vertexIndices = new HashMap<>(this.vertices.size());

        int i = 0;
        int minDegreeOne = vertices.size();
        int minDegreeTwo = vertices.size();
        for (V vertex : vertices) {
            vertexIndices.put(vertex, i);
            int degree = graph.degreeOf(vertex);
            degrees.add(degree);

            if (degree > 1) {
                if (i < minDegreeOne) {
                    minDegreeOne = i;
                }
                if (i < minDegreeTwo) {
                    minDegreeTwo = i;
                }
            } else if (i < minDegreeOne && degree == 1) {
                minDegreeOne = i;
            }

            ++i;
        }
        this.minDegreeOne = minDegreeOne;
        this.minDegreeTwo = minDegreeTwo;
    }

    /**
     * Get the total number of shortest paths. Does not count the paths from a vertex to itself.
     * 
     * @return total number of shortest paths
     */
    public int getShortestPathsCount()
    {
        lazyCalculateMatrix();

        // count shortest paths
        int n = vertices.size();
        int nShortestPaths = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && Double.isFinite(d[i][j])) {
                    nShortestPaths++;
                }
            }
        }

        return nShortestPaths;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> getPath(V a, V b)
    {
        if (!graph.containsVertex(a)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }
        if (!graph.containsVertex(b)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SINK_VERTEX);
        }

        lazyCalculateMatrix();

        int v_a = vertexIndices.get(a);
        int v_b = vertexIndices.get(b);

        if (backtrace[v_a][v_b] == null) { // No path exists
            return createEmptyPath(a, b);
        }

        // Reconstruct the path
        List<E> edges = new ArrayList<>();
        V u = a;
        while (!u.equals(b)) {
            int v_u = vertexIndices.get(u);
            E e = TypeUtil.uncheckedCast(backtrace[v_u][v_b]);
            edges.add(e);
            u = Graphs.getOppositeVertex(graph, e, u);
        }
        return new GraphWalk<>(graph, a, b, null, edges, d[v_a][v_b]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPathWeight(V source, V sink)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }
        if (!graph.containsVertex(sink)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SINK_VERTEX);
        }

        lazyCalculateMatrix();

        return d[vertexIndices.get(source)][vertexIndices.get(sink)];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SingleSourcePaths<V, E> getPaths(V source)
    {
        return new FloydWarshallSingleSourcePaths(source);
    }

    /**
     * Returns the first hop, i.e., the second node on the shortest path from $a$ to $b$. Lookup
     * time is $O(1)$. If the shortest path from $a$ to $b$ is $a,c,d,e,b$, this method returns $c$.
     * If the next invocation would query the first hop on the shortest path from $c$ to $b$, vertex
     * $d$ would be returned, etc. This method is computationally cheaper than calling
     * {@link #getPath(Object, Object)} and then reading the first vertex.
     * 
     * @param a source vertex
     * @param b target vertex
     * @return next hop on the shortest path from a to b, or null when there exists no path from $a$
     *         to $b$.
     */
    public V getFirstHop(V a, V b)
    {
        lazyCalculateMatrix();

        int v_a = vertexIndices.get(a);
        int v_b = vertexIndices.get(b);

        if (backtrace[v_a][v_b] == null) { // No path exists
            return null;
        } else {
            E e = TypeUtil.uncheckedCast(backtrace[v_a][v_b]);
            return Graphs.getOppositeVertex(graph, e, a);
        }
    }

    /**
     * Returns the last hop, i.e., the second to last node on the shortest path from $a$ to $b$.
     * Lookup time is $O(1)$. If the shortest path from $a$ to $b$ is $a,c,d,e,b$, this method
     * returns $e$. If the next invocation would query the next hop on the shortest path from $c$ to
     * $e$, vertex $d$ would be returned, etc. This method is computationally cheaper than calling
     * {@link #getPath(Object, Object)} and then reading the vertex. The first invocation of this
     * method populates a last hop matrix.
     * 
     * @param a source vertex
     * @param b target vertex
     * @return last hop on the shortest path from $a$ to $b$, or null when there exists no path from
     *         $a$ to $b$.
     */
    public V getLastHop(V a, V b)
    {
        lazyCalculateMatrix();

        int v_a = vertexIndices.get(a);
        int v_b = vertexIndices.get(b);

        if (backtrace[v_a][v_b] == null) { // No path exists
            return null;
        } else {
            populateLastHopMatrix();
            E e = TypeUtil.uncheckedCast(lastHopMatrix[v_a][v_b]);
            return Graphs.getOppositeVertex(graph, e, b);
        }
    }

    /**
     * Calculates the matrix of all shortest paths, but does not populate the last hops matrix.
     */
    private void lazyCalculateMatrix()
    {
        if (d != null) {
            // already done
            return;
        }

        int n = vertices.size();

        // init the backtrace matrix
        backtrace = new Object[n][n];

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
        if (graph.getType().isUndirected()) {
            for (E edge : graph.edgeSet()) {
                V source = graph.getEdgeSource(edge);
                V target = graph.getEdgeTarget(edge);
                if (!source.equals(target)) {
                    int v_1 = vertexIndices.get(source);
                    int v_2 = vertexIndices.get(target);
                    double edgeWeight = graph.getEdgeWeight(edge);
                    if (Double.compare(edgeWeight, d[v_1][v_2]) < 0) {
                        d[v_1][v_2] = d[v_2][v_1] = edgeWeight;
                        backtrace[v_1][v_2] = edge;
                        backtrace[v_2][v_1] = edge;
                    }
                }
            }
        } else { // This works for both Directed and Mixed graphs! Iterating over
                 // the arcs and querying source/sink does not suffice for graphs
                 // which contain both edges and arcs
            for (V v1 : graph.vertexSet()) {
                int v_1 = vertexIndices.get(v1);
                for (E e : graph.outgoingEdgesOf(v1)) {
                    V v2 = Graphs.getOppositeVertex(graph, e, v1);
                    if (!v1.equals(v2)) {
                        int v_2 = vertexIndices.get(v2);
                        double edgeWeight = graph.getEdgeWeight(e);
                        if (Double.compare(edgeWeight, d[v_1][v_2]) < 0) {
                            d[v_1][v_2] = edgeWeight;
                            backtrace[v_1][v_2] = e;
                        }
                    }
                }
            }
        }

        // run fw alg
        for (int k = minDegreeTwo; k < n; k++) {
            for (int i = minDegreeOne; i < n; i++) {
                if (i == k) {
                    continue;
                }
                for (int j = minDegreeOne; j < n; j++) {
                    if (i == j || j == k) {
                        continue;
                    }

                    double ik_kj = d[i][k] + d[k][j];
                    if (Double.compare(ik_kj, d[i][j]) < 0) {
                        d[i][j] = ik_kj;
                        backtrace[i][j] = backtrace[i][k];
                    }
                }
            }
        }
    }

    /**
     * Populate the last hop matrix, using the earlier computed backtrace matrix.
     */
    private void populateLastHopMatrix()
    {
        lazyCalculateMatrix();

        if (lastHopMatrix != null)
            return;

        // Initialize matrix
        int n = vertices.size();
        lastHopMatrix = new Object[n][n];

        // Populate matrix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j || lastHopMatrix[i][j] != null || backtrace[i][j] == null)
                    continue;

                // Reconstruct the path from i to j
                V u = vertices.get(i);
                V b = vertices.get(j);
                while (!u.equals(b)) {
                    int v_u = vertexIndices.get(u);
                    E e = TypeUtil.uncheckedCast(backtrace[v_u][j]);
                    V other = Graphs.getOppositeVertex(graph, e, u);
                    lastHopMatrix[i][vertexIndices.get(other)] = e;
                    u = other;
                }
            }
        }
    }

    class FloydWarshallSingleSourcePaths
        implements
        SingleSourcePaths<V, E>
    {
        private V source;

        public FloydWarshallSingleSourcePaths(V source)
        {
            this.source = source;
        }

        @Override
        public Graph<V, E> getGraph()
        {
            return graph;
        }

        @Override
        public V getSourceVertex()
        {
            return source;
        }

        @Override
        public double getWeight(V sink)
        {
            return FloydWarshallShortestPaths.this.getPathWeight(source, sink);
        }

        @Override
        public GraphPath<V, E> getPath(V sink)
        {
            return FloydWarshallShortestPaths.this.getPath(source, sink);
        }
    }

}
