/*
 * (C) Copyright 2026-2026, by seilat and Contributors.
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
package org.jgrapht.alg.hamiltonian;

import org.jgrapht.*;
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

import java.util.*;

/**
 * Exact backtracking algorithm for the
 * <a href="https://en.wikipedia.org/wiki/Hamiltonian_path">Hamiltonian path problem</a> on
 * directed and undirected graphs.
 *
 * <p>
 * The algorithm performs depth-first search over simple paths. From each candidate start vertex
 * it tries to extend the current path by an unused adjacent vertex; the first path that visits
 * every vertex exactly once is returned. If the exhaustive search proves that no such path
 * exists, {@code null} is returned.
 *
 * <p>
 * The general Hamiltonian path problem is NP-complete. This implementation is exact and runs in
 * exponential time in the worst case, so callers should expect it to be suitable for relatively
 * small graphs (the practical limit depends heavily on graph structure: sparse and highly
 * constrained graphs are typically tractable for much larger {@code n} than dense random
 * graphs).
 *
 * <p>
 * The implementation applies the following correctness-preserving prechecks and search
 * heuristics, none of which can cause a false negative:
 * <ul>
 * <li>If the graph contains exactly one vertex, the singleton path is returned.</li>
 * <li>If an undirected graph is not connected, no Hamiltonian path can exist, so {@code null} is
 * returned without searching.</li>
 * <li>If an undirected graph has more than two vertices of degree 1, no Hamiltonian path can
 * exist (a Hamiltonian path has at most two endpoints, and any degree-1 vertex must be one of
 * them).</li>
 * <li>For undirected graphs, every cut vertex (articulation point) must belong to at most two
 * biconnected blocks. A Hamiltonian path visits each vertex once with at most two path-edges
 * incident to it, so it cannot enter more than two blocks meeting at a single cut vertex.</li>
 * <li>For directed graphs, the strongly connected component condensation must itself admit a
 * Hamiltonian path. Any Hamiltonian path in the original directed graph projects to one on the
 * condensation DAG; when that condensation has no Hamiltonian path, the original graph cannot
 * either.</li>
 * <li>At every search step the current endpoint must be able to reach every still-unvisited
 * vertex through unvisited intermediaries. Otherwise the branch is pruned.</li>
 * <li>Candidate next vertices are tried in ascending order of their remaining (unvisited)
 * onward degree. This is a minimum-remaining-values style heuristic: vertices with few onward
 * options tend to fail or commit early, which generally reduces search.</li>
 * </ul>
 *
 * <p>
 * Empty graphs are rejected with an {@link IllegalArgumentException}, matching the convention
 * used by other Hamiltonian / TSP solvers in JGraphT (for example
 * {@link org.jgrapht.alg.tour.HeldKarpTSP}). Graphs with self-loops are accepted but self-loops
 * are ignored, since they cannot be part of a simple path.
 *
 * <p>
 * The returned {@link GraphPath} is a {@link GraphWalk} whose vertex list contains every vertex
 * of the graph exactly once, whose consecutive vertices are connected by an edge of the graph
 * (respecting direction in directed graphs), and whose weight is the sum of the chosen edges'
 * weights according to {@link Graph#getEdgeWeight(Object)}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author seilat
 */
public class BacktrackingHamiltonianPath<V, E>
    implements HamiltonianPathAlgorithm<V, E>
{

    private long statesExpanded;

    /**
     * Constructs a new instance.
     */
    public BacktrackingHamiltonianPath()
    {
    }

    /**
     * Returns the number of DFS nodes the search explored during the most recent call to
     * {@link #getPath(Graph)}. A "state" corresponds to one entry into the recursive extension
     * routine, i.e. one partial path the solver considered. The counter is reset at the start
     * of every {@code getPath} invocation and is intended for benchmarking and diagnostic use
     * rather than as a stable part of the algorithmic contract.
     *
     * @return states (partial paths) explored during the last search
     */
    public long getStatesExpanded()
    {
        return statesExpanded;
    }

    @Override
    public GraphPath<V, E> getPath(Graph<V, E> graph)
    {
        Objects.requireNonNull(graph, "graph must not be null");
        GraphTests.requireDirectedOrUndirected(graph);
        statesExpanded = 0L;
        if (graph.vertexSet().isEmpty()) {
            throw new IllegalArgumentException("Graph contains no vertices");
        }

        final int n = graph.vertexSet().size();
        if (n == 1) {
            V only = graph.vertexSet().iterator().next();
            return new GraphWalk<>(
                graph, only, only, Collections.singletonList(only), Collections.emptyList(), 0d);
        }

        final boolean directed = graph.getType().isDirected();

        if (!directed && !cheapUndirectedPrechecks(graph)) {
            return null;
        }
        if (directed && !cheapDirectedPrechecks(graph)) {
            return null;
        }

        List<V> indexToVertex = new ArrayList<>(graph.vertexSet());
        Map<V, Integer> vertexToIndex = new HashMap<>(n);
        for (int i = 0; i < n; i++) {
            vertexToIndex.put(indexToVertex.get(i), i);
        }

        int[][] adjacency = buildAdjacency(graph, indexToVertex, vertexToIndex, directed);

        int[] pathIdx = new int[n];
        boolean[] visited = new boolean[n];

        for (int start = 0; start < n; start++) {
            pathIdx[0] = start;
            visited[start] = true;
            if (extend(adjacency, pathIdx, visited, 1, n)) {
                return buildResult(graph, indexToVertex, pathIdx);
            }
            visited[start] = false;
        }
        return null;
    }

    /**
     * Cheap necessary conditions for the existence of a Hamiltonian path in an undirected graph.
     * Returns {@code false} if the graph trivially has no Hamiltonian path, {@code true}
     * otherwise. A {@code true} result does not guarantee a path exists.
     */
    private boolean cheapUndirectedPrechecks(Graph<V, E> graph)
    {
        ConnectivityInspector<V, E> inspector = new ConnectivityInspector<>(graph);
        if (!inspector.isConnected()) {
            return false;
        }
        int leaves = 0;
        for (V v : graph.vertexSet()) {
            if (effectiveUndirectedDegree(graph, v) == 1) {
                leaves++;
                if (leaves > 2) {
                    return false;
                }
            }
        }
        return cutVertexBlockDegreeWithinPathBudget(graph);
    }

    /**
     * Necessary condition: in the block-cut tree of an undirected graph, every cut vertex must
     * belong to at most two biconnected blocks. A cut vertex incident to more than two blocks
     * disconnects the graph into more than two components on removal, but a Hamiltonian path
     * uses at most two edges at any internal vertex, so it cannot weave through more than two
     * such components.
     */
    private boolean cutVertexBlockDegreeWithinPathBudget(Graph<V, E> graph)
    {
        BiconnectivityInspector<V, E> bcc = new BiconnectivityInspector<>(graph);
        for (V cut : bcc.getCutpoints()) {
            if (bcc.getBlocks(cut).size() > 2) {
                return false;
            }
        }
        return true;
    }

    /**
     * Cheap necessary conditions for the existence of a Hamiltonian path in a directed graph.
     * The condensation DAG of strongly connected components must itself admit a Hamiltonian
     * path; otherwise the original graph cannot. Returns {@code true} when this necessary
     * condition is satisfied (a Hamiltonian path may or may not exist in the original) and
     * {@code false} when the condensation rules it out.
     */
    private boolean cheapDirectedPrechecks(Graph<V, E> graph)
    {
        KosarajuStrongConnectivityInspector<V, E> scc =
            new KosarajuStrongConnectivityInspector<>(graph);
        List<Graph<V, E>> components = scc.getStronglyConnectedComponents();
        if (components.size() <= 1) {
            // single SCC: condensation is one vertex, no further pruning possible here
            return true;
        }
        Graph<Graph<V, E>, DefaultEdge> condensation = scc.getCondensation();
        return condensationAdmitsHamiltonianPath(condensation);
    }

    /**
     * Linear-time Hamiltonian path existence test on the SCC condensation DAG. Uses the
     * standard longest-path-in-DAG DP over a topological order; if the longest path has fewer
     * vertices than the condensation does, no Hamiltonian projection exists.
     */
    private boolean condensationAdmitsHamiltonianPath(Graph<Graph<V, E>, DefaultEdge> condensation)
    {
        final int n = condensation.vertexSet().size();
        if (n <= 1) {
            return true;
        }
        Map<Graph<V, E>, Integer> position = new HashMap<>(n);
        List<Graph<V, E>> topo = new ArrayList<>(n);
        new TopologicalOrderIterator<>(condensation).forEachRemaining(c -> {
            position.put(c, topo.size());
            topo.add(c);
        });
        int[] longest = new int[n];
        Arrays.fill(longest, 1);
        int best = 1;
        for (int i = 0; i < n; i++) {
            Graph<V, E> v = topo.get(i);
            for (DefaultEdge e : condensation.incomingEdgesOf(v)) {
                int u = position.get(condensation.getEdgeSource(e));
                if (longest[u] + 1 > longest[i]) {
                    longest[i] = longest[u] + 1;
                }
            }
            if (longest[i] > best) {
                best = longest[i];
                if (best == n) {
                    return true;
                }
            }
        }
        return best == n;
    }

    /**
     * Degree of {@code v} in an undirected graph, ignoring self-loops since they cannot
     * participate in a simple path.
     */
    private int effectiveUndirectedDegree(Graph<V, E> graph, V v)
    {
        int degree = 0;
        for (E e : graph.edgesOf(v)) {
            V other = Graphs.getOppositeVertex(graph, e, v);
            if (!other.equals(v)) {
                degree++;
            }
        }
        return degree;
    }

    /**
     * Builds an adjacency-list representation indexed by vertex index. For directed graphs only
     * outgoing edges are recorded. Self-loops are skipped because they cannot extend a simple
     * path. Parallel edges collapse to a single neighbour entry to avoid redundant DFS branches.
     */
    private int[][] buildAdjacency(
        Graph<V, E> graph, List<V> indexToVertex, Map<V, Integer> vertexToIndex, boolean directed)
    {
        final int n = indexToVertex.size();
        int[][] adjacency = new int[n][];
        for (int u = 0; u < n; u++) {
            V uVertex = indexToVertex.get(u);
            Set<Integer> neighbours = new LinkedHashSet<>();
            Iterable<E> edges = directed ? graph.outgoingEdgesOf(uVertex) : graph.edgesOf(uVertex);
            for (E e : edges) {
                V other = directed
                    ? graph.getEdgeTarget(e) : Graphs.getOppositeVertex(graph, e, uVertex);
                if (other.equals(uVertex)) {
                    continue;
                }
                neighbours.add(vertexToIndex.get(other));
            }
            int[] row = new int[neighbours.size()];
            int idx = 0;
            for (int v : neighbours) {
                row[idx++] = v;
            }
            adjacency[u] = row;
        }
        return adjacency;
    }

    /**
     * Recursive DFS extension. Returns {@code true} and leaves {@code pathIdx} filled with a
     * Hamiltonian vertex sequence as soon as one is discovered. Applies reachability pruning
     * and minimum-remaining-values candidate ordering.
     */
    private boolean extend(int[][] adjacency, int[] pathIdx, boolean[] visited, int depth, int n)
    {
        statesExpanded++;
        if (depth == n) {
            return true;
        }
        int current = pathIdx[depth - 1];

        int remaining = n - depth;
        if (!allRemainingReachable(adjacency, visited, current, remaining)) {
            return false;
        }

        int[] neighbours = adjacency[current];
        int[] candidates = new int[neighbours.length];
        int[] onwardDegrees = new int[neighbours.length];
        int k = 0;
        for (int next : neighbours) {
            if (!visited[next]) {
                candidates[k] = next;
                onwardDegrees[k] = onwardDegree(adjacency, visited, next);
                k++;
            }
        }
        // Insertion sort: ascending by onward degree, with vertex index as tie-breaker
        // (insertion sort is stable, so candidates ordered by graph index already serve as
        // the secondary key).
        for (int i = 1; i < k; i++) {
            int cv = candidates[i];
            int dv = onwardDegrees[i];
            int j = i - 1;
            while (j >= 0 && onwardDegrees[j] > dv) {
                onwardDegrees[j + 1] = onwardDegrees[j];
                candidates[j + 1] = candidates[j];
                j--;
            }
            onwardDegrees[j + 1] = dv;
            candidates[j + 1] = cv;
        }

        for (int i = 0; i < k; i++) {
            int next = candidates[i];
            visited[next] = true;
            pathIdx[depth] = next;
            if (extend(adjacency, pathIdx, visited, depth + 1, n)) {
                return true;
            }
            visited[next] = false;
        }
        return false;
    }

    /**
     * Returns {@code true} when every still-unvisited vertex is reachable from {@code start}
     * through unvisited vertices using the supplied adjacency (which respects edge direction in
     * directed graphs). Returning {@code false} proves the current branch cannot be completed
     * into a Hamiltonian path. The check itself never causes a false negative because Hamiltonian
     * path existence from the current endpoint requires every remaining vertex to be reachable
     * from it.
     */
    private boolean allRemainingReachable(
        int[][] adjacency, boolean[] visited, int start, int remaining)
    {
        if (remaining == 0) {
            return true;
        }
        int n = visited.length;
        boolean[] reached = new boolean[n];
        int[] queue = new int[n];
        int head = 0;
        int tail = 0;
        queue[tail++] = start;
        reached[start] = true;
        int found = 0;
        while (head < tail) {
            int u = queue[head++];
            for (int v : adjacency[u]) {
                if (!reached[v] && !visited[v]) {
                    reached[v] = true;
                    queue[tail++] = v;
                    found++;
                    if (found == remaining) {
                        return true;
                    }
                }
            }
        }
        return found == remaining;
    }

    /**
     * Counts the unvisited neighbours of {@code v} reachable in one step along the supplied
     * adjacency.
     */
    private int onwardDegree(int[][] adjacency, boolean[] visited, int v)
    {
        int degree = 0;
        for (int w : adjacency[v]) {
            if (!visited[w]) {
                degree++;
            }
        }
        return degree;
    }

    /**
     * Materialises a found vertex-index sequence as a {@link GraphPath}.
     */
    private GraphPath<V, E> buildResult(Graph<V, E> graph, List<V> indexToVertex, int[] pathIdx)
    {
        final int n = pathIdx.length;
        List<V> vertices = new ArrayList<>(n);
        for (int i : pathIdx) {
            vertices.add(indexToVertex.get(i));
        }
        List<E> edges = new ArrayList<>(n - 1);
        double weight = 0d;
        for (int i = 1; i < n; i++) {
            V u = vertices.get(i - 1);
            V v = vertices.get(i);
            E edge = graph.getEdge(u, v);
            edges.add(edge);
            weight += graph.getEdgeWeight(edge);
        }
        return new GraphWalk<>(graph, vertices.get(0), vertices.get(n - 1), vertices, edges, weight);
    }
}
