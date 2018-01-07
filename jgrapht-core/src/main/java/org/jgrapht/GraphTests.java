/*
 * (C) Copyright 2003-2017, by Barak Naveh, Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht;

import java.util.*;
import java.util.stream.*;

import org.jgrapht.alg.*;
import org.jgrapht.alg.cycle.*;

/**
 * A collection of utilities to test for various graph properties.
 * 
 * @author Barak Naveh
 * @author Dimitrios Michail
 * @author Joris Kinable
 */
public abstract class GraphTests
{
    private static final String GRAPH_CANNOT_BE_NULL = "Graph cannot be null";
    private static final String GRAPH_MUST_BE_DIRECTED_OR_UNDIRECTED =
        "Graph must be directed or undirected";
    private static final String GRAPH_MUST_BE_UNDIRECTED = "Graph must be undirected";
    private static final String GRAPH_MUST_BE_DIRECTED = "Graph must be directed";
    private static final String FIRST_PARTITION_CANNOT_BE_NULL = "First partition cannot be null";
    private static final String SECOND_PARTITION_CANNOT_BE_NULL = "Second partition cannot be null";

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
     * Check if a graph is simple. A graph is simple if it has no self-loops and multiple edges.
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
     * Test whether an undirected graph is connected.
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

        if (!graph.getType().isUndirected()) {
            throw new IllegalArgumentException(GRAPH_MUST_BE_UNDIRECTED);
        }

        return new ConnectivityInspector<>(graph).isGraphConnected();
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
        Objects.requireNonNull(graph, GRAPH_CANNOT_BE_NULL);
        return new ConnectivityInspector<>(graph).isGraphConnected();
    }

    /**
     * Test whether a directed graph is strongly connected.
     * 
     * <p>
     * This method does not performing any caching, instead recomputes everything from scratch. In
     * case more control is required use {@link KosarajuStrongConnectivityInspector} directly.
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
        return new KosarajuStrongConnectivityInspector<>(graph).isStronglyConnected();
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
     */
    public static <V, E> boolean isBipartite(Graph<V, E> graph)
    {
        if (isEmpty(graph)) {
            return true;
        }
        try {
            // at most n^2/4 edges
            if (Math.multiplyExact(4, graph.edgeSet().size()) > Math
                .multiplyExact(graph.vertexSet().size(), graph.vertexSet().size()))
            {
                return false;
            }
        } catch (ArithmeticException e) {
            // ignore
        }

        Set<V> unknown = new HashSet<>(graph.vertexSet());
        Set<V> odd = new HashSet<>();
        Deque<V> queue = new LinkedList<>();

        while (!unknown.isEmpty()) {
            if (queue.isEmpty()) {
                queue.add(unknown.iterator().next());
            }

            V v = queue.removeFirst();
            unknown.remove(v);

            for (E e : graph.edgesOf(v)) {
                V n = Graphs.getOppositeVertex(graph, e, v);
                if (unknown.contains(n)) {
                    queue.add(n);
                    if (!odd.contains(v)) {
                        odd.add(n);
                    }
                } else if (!(odd.contains(v) ^ odd.contains(n))) {
                    return false;
                }
            }
        }
        return true;
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
     */
    public static <V, E> boolean isBipartitePartition(
        Graph<V, E> graph, Set<? extends V> firstPartition, Set<? extends V> secondPartition)
    {
        Objects.requireNonNull(graph, GRAPH_CANNOT_BE_NULL);
        Objects.requireNonNull(firstPartition, FIRST_PARTITION_CANNOT_BE_NULL);
        Objects.requireNonNull(secondPartition, SECOND_PARTITION_CANNOT_BE_NULL);

        if (graph.vertexSet().size() != firstPartition.size() + secondPartition.size()) {
            return false;
        }

        for (V v : graph.vertexSet()) {
            Collection<? extends V> otherPartition;
            if (firstPartition.contains(v)) {
                otherPartition = secondPartition;
            } else if (secondPartition.contains(v)) {
                otherPartition = firstPartition;
            } else {
                // v does not belong to any of the two partitions
                return false;
            }

            for (E e : graph.edgesOf(v)) {
                V other = Graphs.getOppositeVertex(graph, e, v);
                if (!otherPartition.contains(other)) {
                    return false;
                }
            }
        }

        return true;
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

}

// End GraphTests.java
