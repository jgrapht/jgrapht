/*
 * (C) Copyright 2003-2018, by Barak Naveh, Dimitrios Michail and Contributors.
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

import org.jgrapht.alg.connectivity.*;
import org.jgrapht.alg.cycle.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.partition.*;

import java.util.*;
import java.util.stream.*;

/**
 * A collection of utilities to test for various graph properties.
 * 
 * @author Barak Naveh
 * @author Dimitrios Michail
 * @author Joris Kinable
 * @author Alexandru Valeanu
 */
public abstract class GraphTests
{
    private static final String GRAPH_CANNOT_BE_NULL = "Graph cannot be null";
    private static final String GRAPH_MUST_BE_DIRECTED_OR_UNDIRECTED =
        "Graph must be directed or undirected";
    private static final String GRAPH_MUST_BE_UNDIRECTED = "Graph must be undirected";
    private static final String GRAPH_MUST_BE_DIRECTED = "Graph must be directed";
    private static final String GRAPH_MUST_BE_WEIGHTED = "Graph must be weighted";

    /**
     * Test whether a graph is empty. An empty graph on n nodes consists of n isolated vertices with
     * no edges.
     * 
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is empty, false otherwise
     */
    public static <V, E> boolean isEmpty(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, GRAPH_CANNOT_BE_NULL);
        return graph.edgeSet().isEmpty();
    }

    /**
     * Check if a graph is simple. A graph is simple if it has no self-loops and multiple (parallel)
     * edges.
     * 
     * @param graph a graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if a graph is simple, false otherwise
     */
    public static <V, E> boolean isSimple(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, GRAPH_CANNOT_BE_NULL);

        GraphType type = graph.getType();
        if (type.isSimple()) {
            return true;
        }

        // no luck, we have to check
        for (V v : graph.vertexSet()) {
            Set<V> neighbors = new HashSet<>();
            for (E e : graph.outgoingEdgesOf(v)) {
                V u = Graphs.getOppositeVertex(graph, e, v);
                if (u.equals(v) || !neighbors.add(u)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check if a graph has self-loops. A self-loop is an edge with the same source and target
     * vertices.
     * 
     * @param graph a graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if a graph has self-loops, false otherwise
     */
    public static <V, E> boolean hasSelfLoops(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, GRAPH_CANNOT_BE_NULL);

        if (!graph.getType().isAllowingSelfLoops()) {
            return false;
        }

        // no luck, we have to check
        for (E e : graph.edgeSet()) {
            if (graph.getEdgeSource(e).equals(graph.getEdgeTarget(e))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a graph has multiple edges (parallel edges), that is, whether the graph contains two
     * or more edges connecting the same pair of vertices.
     * 
     * @param graph a graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if a graph has multiple edges, false otherwise
     */
    public static <V, E> boolean hasMultipleEdges(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, GRAPH_CANNOT_BE_NULL);

        if (!graph.getType().isAllowingMultipleEdges()) {
            return false;
        }

        // no luck, we have to check
        for (V v : graph.vertexSet()) {
            Set<V> neighbors = new HashSet<>();
            for (E e : graph.outgoingEdgesOf(v)) {
                V u = Graphs.getOppositeVertex(graph, e, v);
                if (!neighbors.add(u)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Test whether a graph is complete. A complete undirected graph is a simple graph in which
     * every pair of distinct vertices is connected by a unique edge. A complete directed graph is a
     * directed graph in which every pair of distinct vertices is connected by a pair of unique
     * edges (one in each direction).
     * 
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is complete, false otherwise
     */
    public static <V, E> boolean isComplete(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, GRAPH_CANNOT_BE_NULL);
        int n = graph.vertexSet().size();
        int allEdges;
        if (graph.getType().isDirected()) {
            allEdges = Math.multiplyExact(n, n - 1);
        } else if (graph.getType().isUndirected()) {
            if (n % 2 == 0) {
                allEdges = Math.multiplyExact(n / 2, n - 1);
            } else {
                allEdges = Math.multiplyExact(n, (n - 1) / 2);
            }
        } else {
            throw new IllegalArgumentException(GRAPH_MUST_BE_DIRECTED_OR_UNDIRECTED);
        }
        return graph.edgeSet().size() == allEdges && isSimple(graph);
    }

    /**
     * Test if the inspected graph is connected. A graph is connected when, while ignoring edge
     * directionality, there exists a path between every pair of vertices. In a connected graph,
     * there are no unreachable vertices. When the inspected graph is a <i>directed</i> graph, this
     * method returns true if and only if the inspected graph is <i>weakly</i> connected. An empty
     * graph is <i>not</i> considered connected.
     * 
     * <p>
     * This method does not performing any caching, instead recomputes everything from scratch. In
     * case more control is required use {@link ConnectivityInspector} directly.
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is connected, false otherwise
     * @see ConnectivityInspector
     */
    public static <V, E> boolean isConnected(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, GRAPH_CANNOT_BE_NULL);
        return new ConnectivityInspector<>(graph).isConnected();
    }

    /**
     * Tests if the inspected graph is biconnected. A biconnected graph is a connected graph on two
     * or more vertices having no cutpoints.
     *
     * <p>
     * This method does not performing any caching, instead recomputes everything from scratch. In
     * case more control is required use
     * {@link org.jgrapht.alg.connectivity.BiconnectivityInspector} directly.
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is biconnected, false otherwise
     * @see org.jgrapht.alg.connectivity.BiconnectivityInspector
     */
    public static <V, E> boolean isBiconnected(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, GRAPH_CANNOT_BE_NULL);
        return new BiconnectivityInspector<>(graph).isBiconnected();
    }

    /**
     * Test whether a directed graph is weakly connected.
     * 
     * <p>
     * This method does not performing any caching, instead recomputes everything from scratch. In
     * case more control is required use {@link ConnectivityInspector} directly.
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is weakly connected, false otherwise
     * @see ConnectivityInspector
     */
    public static <V, E> boolean isWeaklyConnected(Graph<V, E> graph)
    {
        return isConnected(graph);
    }

    /**
     * Test whether a graph is strongly connected.
     * 
     * <p>
     * This method does not performing any caching, instead recomputes everything from scratch. In
     * case more control is required use {@link KosarajuStrongConnectivityInspector} directly.
     * 
     * <p>
     * In case of undirected graphs this method delegated to {@link #isConnected(Graph)}.
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is strongly connected, false otherwise
     * @see KosarajuStrongConnectivityInspector
     */
    public static <V, E> boolean isStronglyConnected(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, GRAPH_CANNOT_BE_NULL);
        if (graph.getType().isUndirected()) {
            return isConnected(graph);
        } else {
            return new KosarajuStrongConnectivityInspector<>(graph).isStronglyConnected();
        }
    }

    /**
     * Test whether an undirected graph is a tree.
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is tree, false otherwise
     */
    public static <V, E> boolean isTree(Graph<V, E> graph)
    {
        if (!graph.getType().isUndirected()) {
            throw new IllegalArgumentException(GRAPH_MUST_BE_UNDIRECTED);
        }

        return (graph.edgeSet().size() == (graph.vertexSet().size() - 1)) && isConnected(graph);
    }

    /**
     * Test whether an undirected graph is a forest. A forest is a set of disjoint trees. By
     * definition, any acyclic graph is a forest. This includes the empty graph and the class of
     * tree graphs.
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is forest, false otherwise
     */
    public static <V, E> boolean isForest(Graph<V, E> graph)
    {
        if (!graph.getType().isUndirected()) {
            throw new IllegalArgumentException(GRAPH_MUST_BE_UNDIRECTED);
        }
        if (graph.vertexSet().isEmpty()) // null graph is not a forest
            return false;

        int nrConnectedComponents = new ConnectivityInspector<>(graph).connectedSets().size();
        return graph.edgeSet().size() + nrConnectedComponents == graph.vertexSet().size();
    }

    /**
     * Test whether a graph is <a href="https://en.wikipedia.org/wiki/Overfull_graph">overfull</a>.
     * A graph is overfull if $|E|&gt;\Delta(G)\lfloor |V|/2 \rfloor$, where $\Delta(G)$ is the
     * maximum degree of the graph.
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is overfull, false otherwise
     */
    public static <V, E> boolean isOverfull(Graph<V, E> graph)
    {
        int maxDegree = graph.vertexSet().stream().mapToInt(graph::degreeOf).max().getAsInt();
        return graph.edgeSet().size() > maxDegree * Math.floor(graph.vertexSet().size() / 2.0);
    }

    /**
     * Test whether an undirected graph is a
     * <a href="https://en.wikipedia.org/wiki/Split_graph">split graph</a>. A split graph is a graph
     * in which the vertices can be partitioned into a clique and an independent set. Split graphs
     * are a special class of chordal graphs. Given the degree sequence $d_1 \geq,\dots,\geq d_n$ of
     * $G$, a graph is a split graph if and only if : \[\sum_{i=1}^m d_i = m (m - 1) + \sum_{i=m +
     * 1}^nd_i\], where $m = \max_i \{d_i\geq i-1\}$. If the graph is a split graph, then the $m$
     * vertices with the largest degrees form a maximum clique in $G$, and the remaining vertices
     * constitute an independent set. See Brandstadt, A., Le, V., Spinrad, J. Graph Classes: A
     * Survey. Philadelphia, PA: SIAM, 1999. for details.
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is a split graph, false otherwise
     */
    public static <V, E> boolean isSplit(Graph<V, E> graph)
    {
        requireUndirected(graph);
        if (!isSimple(graph) || graph.vertexSet().isEmpty())
            return false;

        List<Integer> degrees = new ArrayList<>(graph.vertexSet().size());
        degrees
            .addAll(graph.vertexSet().stream().map(graph::degreeOf).collect(Collectors.toList()));
        Collections.sort(degrees, Collections.reverseOrder()); // sort degrees descending order
        // Find m = \max_i \{d_i\geq i-1\}
        int m = 1;
        for (; m < degrees.size() && degrees.get(m) >= m; m++) {
        }
        m--;

        int left = 0;
        for (int i = 0; i <= m; i++)
            left += degrees.get(i);
        int right = m * (m + 1);
        for (int i = m + 1; i < degrees.size(); i++)
            right += degrees.get(i);
        return left == right;
    }

    /**
     * Test whether a graph is bipartite.
     * 
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is bipartite, false otherwise
     * @see BipartitePartitioning#isBipartite()
     */
    public static <V, E> boolean isBipartite(Graph<V, E> graph)
    {
        return new BipartitePartitioning<>(graph).isBipartite();
    }

    /**
     * Test whether a partition of the vertices into two sets is a bipartite partition.
     * 
     * @param graph the input graph
     * @param firstPartition the first vertices partition
     * @param secondPartition the second vertices partition
     * @return true if the partition is a bipartite partition, false otherwise
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @see BipartitePartitioning#isValidPartitioning(PartitioningAlgorithm.Partitioning)
     */
    @SuppressWarnings("unchecked")
    public static <V, E> boolean isBipartitePartition(
        Graph<V, E> graph, Set<? extends V> firstPartition, Set<? extends V> secondPartition)
    {
        return new BipartitePartitioning<>(graph).isValidPartitioning(
            new PartitioningAlgorithm.PartitioningImpl<>(
                Arrays.asList((Set<V>) firstPartition, (Set<V>) secondPartition)));
    }

    /**
     * Tests whether a graph is <a href="http://mathworld.wolfram.com/CubicGraph.html">cubic</a>. A
     * graph is cubic if all vertices have degree 3.
     * 
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is cubic, false otherwise
     */
    public static <V, E> boolean isCubic(Graph<V, E> graph)
    {
        for (V v : graph.vertexSet())
            if (graph.degreeOf(v) != 3)
                return false;
        return true;
    }

    /**
     * Test whether a graph is Eulerian. An undirected graph is Eulerian if it is connected and each
     * vertex has an even degree. A directed graph is Eulerian if it is strongly connected and each
     * vertex has the same incoming and outgoing degree. Test whether a graph is Eulerian. An
     * <a href="http://mathworld.wolfram.com/EulerianGraph.html">Eulerian graph</a> is a graph
     * containing an <a href="http://mathworld.wolfram.com/EulerianCycle.html">Eulerian cycle</a>.
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return true if the graph is Eulerian, false otherwise
     * @see HierholzerEulerianCycle#isEulerian(Graph)
     */
    public static <V, E> boolean isEulerian(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, GRAPH_CANNOT_BE_NULL);
        return new HierholzerEulerianCycle<V, E>().isEulerian(graph);
    }

    /**
     * Checks whether a graph is chordal. A <a href="https://en.wikipedia.org/wiki/Chordal_graph">
     * chordal graph</a> is one in which all cycles of four or more vertices have a chord, which is
     * an edge that is not part of the cycle but connects two vertices of the cycle.
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is chordal, false otherwise
     * @see ChordalityInspector#isChordal()
     */
    public static <V, E> boolean isChordal(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, GRAPH_CANNOT_BE_NULL);
        return new ChordalityInspector<>(graph).isChordal();
    }

    /**
     * Checks whether a graph is <a href="http://www.graphclasses.org/classes/gc_14.html">weakly
     * chordal</a>.
     * <p>
     * The following definitions are equivalent:
     * <ol>
     * <li>A graph is weakly chordal (weakly triangulated) if neither it nor its complement contains
     * a <a href="http://mathworld.wolfram.com/ChordlessCycle.html">chordless cycles</a> with five
     * or more vertices.</li>
     * <li>A 2-pair in a graph is a pair of non-adjacent vertices $x$, $y$ such that every chordless
     * path has exactly two edges. A graph is weakly chordal if every connected
     * <a href="https://en.wikipedia.org/wiki/Induced_subgraph">induced subgraph</a> $H$ that is not
     * a complete graph, contains a 2-pair.</li>
     * </ol>
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is weakly chordal, false otherwise
     * @see WeakChordalityInspector#isWeaklyChordal()
     */
    public static <V, E> boolean isWeaklyChordal(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, GRAPH_CANNOT_BE_NULL);
        return new WeakChordalityInspector<>(graph).isWeaklyChordal();
    }

    /**
     * Tests whether an undirected graph meets Ore's condition to be Hamiltonian.
     *
     * Let $G$ be a (finite and simple) graph with $n \geq 3$ vertices. We denote by $deg(v)$ the
     * degree of a vertex $v$ in $G$, i.e. the number of incident edges in $G$ to $v$. Then, Ore's
     * theorem states that if $deg(v) + deg(w) \geq n$ for every pair of distinct non-adjacent
     * vertices $v$ and $w$ of $G$, then $G$ is Hamiltonian.
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph meets Ore's condition, false otherwise
     * @see org.jgrapht.alg.tour.PalmerHamiltonianCycle
     */
    public static <V, E> boolean hasOreProperty(Graph<V, E> graph)
    {
        requireUndirected(graph);

        final int n = graph.vertexSet().size();

        if (!graph.getType().isSimple() || n < 3)
            return false;

        List<V> vertexList = new ArrayList<>(graph.vertexSet());

        for (int i = 0; i < vertexList.size(); i++) {
            for (int j = i + 1; j < vertexList.size(); j++) {
                V v = vertexList.get(i);
                V w = vertexList.get(j);

                if (!v.equals(w) && !graph.containsEdge(v, w)
                    && graph.degreeOf(v) + graph.degreeOf(w) < n)
                    return false;
            }
        }

        return true;
    }

    /**
     * Tests whether an undirected graph is triangle-free (i.e. no three distinct vertices form a
     * triangle of edges).
     *
     * The implementation of this method uses {@link GraphMetrics#getNumberOfTriangles(Graph)}.
     *
     * @param graph the input graph
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is triangle-free, false otherwise
     */
    public static <V, E> boolean isTriangleFree(Graph<V, E> graph)
    {
        return GraphMetrics.getNumberOfTriangles(graph) == 0;
    }

    /**
     * Checks that the specified graph is perfect. Due to the Strong Perfect Graph Theorem Berge
     * Graphs are the same as perfect Graphs. The implementation of this method is delegated to
     * {@link org.jgrapht.alg.cycle.BergeGraphInspector}
     *
     * @param graph the graph reference to check for being perfect or not
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return true if the graph is perfect, false otherwise
     */
    public static <V, E> boolean isPerfect(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, GRAPH_CANNOT_BE_NULL);
        return new BergeGraphInspector<V, E>().isBerge(graph);
    }

    /**
     * Checks that the specified graph is directed and throws a customized
     * {@link IllegalArgumentException} if it is not. Also checks that the graph reference is not
     * {@code null} and throws a {@link NullPointerException} if it is.
     *
     * @param graph the graph reference to check for beeing directed and not null
     * @param message detail message to be used in the event that an exception is thrown
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return {@code graph} if directed and not {@code null}
     * @throws NullPointerException if {@code graph} is {@code null}
     * @throws IllegalArgumentException if {@code graph} is not directed
     */
    public static <V, E> Graph<V, E> requireDirected(Graph<V, E> graph, String message)
    {
        if (graph == null)
            throw new NullPointerException(GRAPH_CANNOT_BE_NULL);
        if (!graph.getType().isDirected()) {
            throw new IllegalArgumentException(message);
        }
        return graph;
    }

    /**
     * Checks that the specified graph is directed and throws an {@link IllegalArgumentException} if
     * it is not. Also checks that the graph reference is not {@code null} and throws a
     * {@link NullPointerException} if it is.
     *
     * @param graph the graph reference to check for beeing directed and not null
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return {@code graph} if directed and not {@code null}
     * @throws NullPointerException if {@code graph} is {@code null}
     * @throws IllegalArgumentException if {@code graph} is not directed
     */
    public static <V, E> Graph<V, E> requireDirected(Graph<V, E> graph)
    {
        return requireDirected(graph, GRAPH_MUST_BE_DIRECTED);
    }

    /**
     * Checks that the specified graph is undirected and throws a customized
     * {@link IllegalArgumentException} if it is not. Also checks that the graph reference is not
     * {@code null} and throws a {@link NullPointerException} if it is.
     *
     * @param graph the graph reference to check for being undirected and not null
     * @param message detail message to be used in the event that an exception is thrown
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return {@code graph} if undirected and not {@code null}
     * @throws NullPointerException if {@code graph} is {@code null}
     * @throws IllegalArgumentException if {@code graph} is not undirected
     */
    public static <V, E> Graph<V, E> requireUndirected(Graph<V, E> graph, String message)
    {
        if (graph == null)
            throw new NullPointerException(GRAPH_CANNOT_BE_NULL);
        if (!graph.getType().isUndirected()) {
            throw new IllegalArgumentException(message);
        }
        return graph;
    }

    /**
     * Checks that the specified graph is undirected and throws an {@link IllegalArgumentException}
     * if it is not. Also checks that the graph reference is not {@code null} and throws a
     * {@link NullPointerException} if it is.
     *
     * @param graph the graph reference to check for being undirected and not null
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return {@code graph} if undirected and not {@code null}
     * @throws NullPointerException if {@code graph} is {@code null}
     * @throws IllegalArgumentException if {@code graph} is not undirected
     */
    public static <V, E> Graph<V, E> requireUndirected(Graph<V, E> graph)
    {
        return requireUndirected(graph, GRAPH_MUST_BE_UNDIRECTED);
    }

    /**
     * Checks that the specified graph is directed or undirected and throws a customized
     * {@link IllegalArgumentException} if it is not. Also checks that the graph reference is not
     * {@code null} and throws a {@link NullPointerException} if it is.
     *
     * @param graph the graph reference to check for beeing directed or undirected and not null
     * @param message detail message to be used in the event that an exception is thrown
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return {@code graph} if directed and not {@code null}
     * @throws NullPointerException if {@code graph} is {@code null}
     * @throws IllegalArgumentException if {@code graph} is mixed
     */
    public static <V, E> Graph<V, E> requireDirectedOrUndirected(Graph<V, E> graph, String message)
    {
        if (graph == null)
            throw new NullPointerException(GRAPH_CANNOT_BE_NULL);
        if (!graph.getType().isDirected() && !graph.getType().isUndirected()) {
            throw new IllegalArgumentException(message);
        }
        return graph;
    }

    /**
     * Checks that the specified graph is directed and throws an {@link IllegalArgumentException} if
     * it is not. Also checks that the graph reference is not {@code null} and throws a
     * {@link NullPointerException} if it is.
     *
     * @param graph the graph reference to check for beeing directed and not null
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return {@code graph} if directed and not {@code null}
     * @throws NullPointerException if {@code graph} is {@code null}
     * @throws IllegalArgumentException if {@code graph} is mixed
     */
    public static <V, E> Graph<V, E> requireDirectedOrUndirected(Graph<V, E> graph)
    {
        return requireDirectedOrUndirected(graph, GRAPH_MUST_BE_DIRECTED_OR_UNDIRECTED);
    }

    /**
     * Checks that the specified graph is weighted and throws a customized
     * {@link IllegalArgumentException} if it is not. Also checks that the graph reference is not
     * {@code null} and throws a {@link NullPointerException} if it is.
     *
     * @param graph the graph reference to check for being weighted and not null
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return {@code graph} if directed and not {@code null}
     * @throws NullPointerException if {@code graph} is {@code null}
     * @throws IllegalArgumentException if {@code graph} is not weighted
     */
    public static <V, E> Graph<V, E> requireWeighted(Graph<V, E> graph)
    {
        if (graph == null)
            throw new NullPointerException(GRAPH_CANNOT_BE_NULL);
        if (!graph.getType().isWeighted()) {
            throw new IllegalArgumentException(GRAPH_MUST_BE_WEIGHTED);
        }
        return graph;
    }
}
