/*
 * (C) Copyright 2015-2026, by Vera-Licona Research Group and Contributors.
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
import org.jgrapht.graph.*;

import java.util.*;

/**
 * A Dijkstra-like algorithm to find all paths between two sets of nodes in a directed graph, with
 * options to search only simple paths and to limit the path length.
 *
 * <p>
 * The algorithm runs in two phases. Preprocessing decorates each edge with the minimum number of
 * edges still needed to reach a target. Enumeration then walks the decorated edges from the source
 * set, using the decoration to abandon any partial path that cannot complete within the budget. By
 * default a forward BFS from the source set is also run as part of preprocessing, so that edges
 * whose source endpoint is not reachable from any requested source within the budget are dropped
 * from the decoration up front. This can substantially reduce preprocessing cost on graphs where a
 * non-trivial fraction of the target-reachable subgraph is not reachable from the sources. See
 * {@link #setForwardPruning(boolean)} for how to disable it.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Andrew Gainer-Dewar, Google LLC
 */
public class AllDirectedPaths<V, E>
{
    private final Graph<V, E> graph;

    /**
     * Provides validation for the paths which will be computed. If the validator is {@code null},
     * this means that all paths are valid.
     */
    private final PathValidator<V, E> pathValidator;

    /**
     * Whether to apply the forward-pruning preprocessing step. When {@code true} (the default), a
     * forward BFS from the source set is run before the backward edge-decoration sweep and edges
     * that cannot lie on any source-to-target walk within the budget are dropped. When
     * {@code false}, the historical backward-only preprocessing behaviour is used.
     *
     * @see #setForwardPruning(boolean)
     */
    private boolean forwardPruning = true;

    /**
     * Create a new instance.
     *
     * @param graph the input graph
     * @throws IllegalArgumentException if the graph is not directed
     */
    public AllDirectedPaths(Graph<V, E> graph)
    {
        this(graph, null);
    }

    /**
     * Create a new instance with given {@code pathValidator}.
     *
     * If non-{@code null}, the {@code pathValidator} will be used while searching for paths,
     * validating the addition of any edge to a partial path. Zero-length paths will therefore not
     * be subject to {@code pathValidator}; length-1 paths will.
     *
     * @param graph the input graph
     * @param pathValidator validator for computed paths; may be null
     * @throws IllegalArgumentException if the graph is not directed
     */
    public AllDirectedPaths(Graph<V, E> graph, PathValidator<V, E> pathValidator)
    {
        this.graph = GraphTests.requireDirected(graph);
        this.pathValidator = pathValidator;
    }

    /**
     * Configure whether the preprocessing step applies the forward-pruning optimisation.
     *
     * <p>
     * When forward pruning is enabled (the default), {@link #getAllPaths} first runs a forward
     * BFS from the source set and uses the result to drop edges whose source endpoint is not
     * reachable from any source within the bound, or whose forward-plus-backward length exceeds
     * the bound. The prune is exact &mdash; it never drops an edge that could lie on a feasible
     * source-to-target walk &mdash; and can be a large win when a substantial fraction of the
     * graph is backward-reachable from the targets but not forward-reachable from the sources.
     * On graphs where the prune never fires (e.g. small dense strongly-connected digraphs), the
     * optimisation adds the cost of one extra {@code O(V + E)} BFS per {@code getAllPaths}
     * call.
     * </p>
     *
     * <p>
     * Setting forward pruning to {@code false} recovers the historical preprocessing behaviour
     * exactly &mdash; useful when the cost of the extra BFS is known to dominate.
     * </p>
     *
     * @param forwardPruning whether to apply the forward-pruning preprocessing step
     */
    public void setForwardPruning(boolean forwardPruning)
    {
        this.forwardPruning = forwardPruning;
    }

    /**
     * Whether the preprocessing step currently applies the forward-pruning optimisation.
     *
     * @return {@code true} if forward pruning is enabled
     * @see #setForwardPruning(boolean)
     */
    public boolean isForwardPruning()
    {
        return forwardPruning;
    }

    /**
     * Calculate (and return) all paths from the source vertex to the target vertex.
     *
     * @param sourceVertex the source vertex
     * @param targetVertex the target vertex
     * @param simplePathsOnly if true, only search simple (non-self-intersecting) paths
     * @param maxPathLength maximum number of edges to allow in a path (if null, all paths are
     *        considered, which may be very slow due to potentially huge output)
     * @return all paths from the source vertex to the target vertex
     */
    public List<GraphPath<V, E>> getAllPaths(
        V sourceVertex, V targetVertex, boolean simplePathsOnly, Integer maxPathLength)
    {
        return getAllPaths(
            Collections.singleton(sourceVertex), Collections.singleton(targetVertex),
            simplePathsOnly, maxPathLength);
    }

    /**
     * Calculate (and return) all paths from the source vertices to the target vertices.
     *
     * @param sourceVertices the source vertices
     * @param targetVertices the target vertices
     * @param simplePathsOnly if true, only search simple (non-self-intersecting) paths
     * @param maxPathLength maximum number of edges to allow in a path (if null, all paths are
     *        considered, which may be very slow due to potentially huge output)
     *
     * @return list of all paths from the sources to the targets containing no more than
     *         maxPathLength edges
     */
    public List<GraphPath<V, E>> getAllPaths(
        Set<V> sourceVertices, Set<V> targetVertices, boolean simplePathsOnly,
        Integer maxPathLength)
    {
        if ((maxPathLength != null) && (maxPathLength < 0)) {
            throw new IllegalArgumentException("maxPathLength must be non-negative if defined");
        }

        if (!simplePathsOnly && (maxPathLength == null)) {
            throw new IllegalArgumentException(
                "If search is not restricted to simple paths, a maximum path length must be set to avoid infinite cycles");
        }

        if ((sourceVertices.isEmpty()) || (targetVertices.isEmpty())) {
            return Collections.emptyList();
        }

        // Decorate the edges with the minimum path lengths through them. When forward pruning
        // is enabled (the default), first compute forward distances from the source set and use
        // them to drop edges that cannot lie on any feasible source -> target walk within the
        // budget. When disabled, behave identically to the historical backward-only sweep.
        Map<V, Integer> vertexMinDistancesFromSources = forwardPruning
            ? vertexMinDistancesForwards(sourceVertices, maxPathLength) : null;
        Map<E, Integer> edgeMinDistancesFromTargets = edgeMinDistancesBackwards(
            targetVertices, vertexMinDistancesFromSources, maxPathLength);

        // Generate all the paths

        return generatePaths(
            sourceVertices, targetVertices, simplePathsOnly, maxPathLength,
            edgeMinDistancesFromTargets);
    }

    /**
     * Compute the minimum number of edges in a walk to the targets through each edge, so long as
     * it is not greater than a bound and the edge can lie on at least one source -&gt; target walk
     * of length at most {@code maxPathLength} (this includes every simple path of length at most
     * {@code maxPathLength}, since every simple path is also a walk).
     *
     * <p>
     * When {@code vertexMinDistancesFromSources} is non-{@code null} the sandwich condition is
     * enforced: an edge {@code (u, v)} is retained only when {@code u} is forward-reachable from
     * some source and {@code dF(u) + (1 + dB(v)) <= maxPathLength} (when bounded), where
     * {@code dF(u)} is the forward BFS distance from the source set to {@code u} and
     * {@code 1 + dB(v)} is the backward BFS distance to a target through this edge. Edges that
     * fail the sandwich cannot appear on any feasible source -&gt; target walk and would therefore
     * never be traversed by the forward enumeration in {@link #generatePaths} in either
     * {@code simplePathsOnly = true} or {@code simplePathsOnly = false} mode. When
     * {@code vertexMinDistancesFromSources} is {@code null}, the historical backward-only sweep
     * is performed.
     * </p>
     *
     * @param targetVertices the target vertices
     * @param vertexMinDistancesFromSources forward BFS distances from the source set, computed
     *        with the same {@code maxPathLength} bound; vertices not in the map are not
     *        forward-reachable within that bound. May be {@code null} to disable the sandwich
     *        prune.
     * @param maxPathLength maximum number of edges to allow in a path (if null, all edges will be
     *        considered, which may be expensive)
     *
     * @return the minimum number of edges in a path from each edge to the targets, encoded in a
     *         Map
     */
    private Map<E, Integer> edgeMinDistancesBackwards(
        Set<V> targetVertices, Map<V, Integer> vertexMinDistancesFromSources,
        Integer maxPathLength)
    {
        /*
         * We walk backwards through the network from the target vertices, marking edges and
         * vertices with their minimum distances as we go.
         */
        Map<E, Integer> edgeMinDistances = new HashMap<>();
        Map<V, Integer> vertexMinDistances = new HashMap<>();
        Queue<V> verticesToProcess = new ArrayDeque<>();

        // Input sanity checking
        if (maxPathLength != null) {
            if (maxPathLength < 0) {
                throw new IllegalArgumentException("maxPathLength must be non-negative if defined");
            }
            if (maxPathLength == 0) {
                return edgeMinDistances;
            }
        }

        // Bootstrap the process with the target vertices. When the sandwich prune is enabled,
        // skip targets that no source can reach within the budget.
        for (V target : targetVertices) {
            if (vertexMinDistancesFromSources != null
                && !vertexMinDistancesFromSources.containsKey(target))
            {
                continue;
            }
            vertexMinDistances.put(target, 0);
            verticesToProcess.add(target);
        }

        // Work through the node queue. When it's empty, we're done!
        for (V vertex; (vertex = verticesToProcess.poll()) != null;) {
            assert vertexMinDistances.containsKey(vertex);

            Integer childDistance = vertexMinDistances.get(vertex) + 1;

            // Check whether the incoming edges of this node are correctly
            // decorated
            for (E edge : graph.incomingEdgesOf(vertex)) {
                V edgeSource = graph.getEdgeSource(edge);

                // Sandwich prune: drop edges whose source side is not reachable from the
                // source set, or whose total forward + backward length already exceeds the
                // budget. Skipped entirely when the prune is disabled. The bound comparison is
                // written as (forwardOfSource > maxPathLength - childDistance) rather than the
                // addition form to avoid integer overflow at extreme maxPathLength values; the
                // BFS bounds guarantee childDistance <= maxPathLength when maxPathLength is set,
                // so the right-hand side is non-negative.
                if (vertexMinDistancesFromSources != null) {
                    Integer forwardOfSource = vertexMinDistancesFromSources.get(edgeSource);
                    if (forwardOfSource == null) {
                        continue;
                    }
                    if (maxPathLength != null
                        && forwardOfSource > maxPathLength - childDistance)
                    {
                        continue;
                    }
                }

                // Mark the edge if needed
                if (!edgeMinDistances.containsKey(edge)
                    || (edgeMinDistances.get(edge) > childDistance))
                {
                    edgeMinDistances.put(edge, childDistance);
                }

                // Mark the edge's source vertex if needed
                if (!vertexMinDistances.containsKey(edgeSource)
                    || (vertexMinDistances.get(edgeSource) > childDistance))
                {
                    vertexMinDistances.put(edgeSource, childDistance);

                    if ((maxPathLength == null) || (childDistance < maxPathLength)) {
                        verticesToProcess.add(edgeSource);
                    }
                }
            }
        }

        assert verticesToProcess.isEmpty();
        return edgeMinDistances;
    }

    /**
     * Compute the minimum number of edges in a forward BFS from the source vertices, capped at
     * {@code maxPathLength} when given. Used together with
     * {@link #edgeMinDistancesBackwards(Set, Map, Integer)} to apply sandwich-style pruning of the
     * edge decoration map.
     *
     * @param sourceVertices the source vertices
     * @param maxPathLength maximum number of forward edges to expand (if null, the BFS is
     *        unrestricted)
     *
     * @return for every vertex reachable from {@code sourceVertices} within {@code maxPathLength}
     *         edges, its minimum forward distance from the source set
     */
    private Map<V, Integer> vertexMinDistancesForwards(
        Set<V> sourceVertices, Integer maxPathLength)
    {
        Map<V, Integer> vertexMinDistances = new HashMap<>();
        Queue<V> verticesToProcess = new ArrayDeque<>();

        for (V source : sourceVertices) {
            if (!vertexMinDistances.containsKey(source)) {
                vertexMinDistances.put(source, 0);
                verticesToProcess.add(source);
            }
        }

        for (V vertex; (vertex = verticesToProcess.poll()) != null;) {
            int currentDistance = vertexMinDistances.get(vertex);
            if (maxPathLength != null && currentDistance >= maxPathLength) {
                continue;
            }
            int childDistance = currentDistance + 1;

            for (E edge : graph.outgoingEdgesOf(vertex)) {
                V child = graph.getEdgeTarget(edge);
                if (!vertexMinDistances.containsKey(child)) {
                    vertexMinDistances.put(child, childDistance);
                    verticesToProcess.add(child);
                }
            }
        }

        return vertexMinDistances;
    }

    /**
     * Generate all paths from the sources to the targets, using pre-computed minimum distances.
     *
     * @param sourceVertices the source vertices
     * @param targetVertices the target vertices
     * @param maxPathLength maximum number of edges to allow in a path
     * @param simplePathsOnly if true, only search simple (non-self-intersecting) paths (if null,
     *        all edges will be considered, which may be expensive)
     * @param edgeMinDistancesFromTargets the minimum number of edges in a path to a target through
     *        each edge, as computed by {@code
     * edgeMinDistancesBackwards}.
     *
     * @return a List of all GraphPaths from the sources to the targets satisfying the given
     *         constraints
     */
    private List<GraphPath<V, E>> generatePaths(
        Set<V> sourceVertices, Set<V> targetVertices, boolean simplePathsOnly,
        Integer maxPathLength, Map<E, Integer> edgeMinDistancesFromTargets)
    {
        /*
         * We walk forwards through the network from the source vertices, exploring all outgoing
         * edges whose minimum distances is small enough.
         */
        List<GraphPath<V, E>> completePaths = new ArrayList<>();
        Deque<List<E>> incompletePaths = new ArrayDeque<>();

        // Input sanity checking
        if (maxPathLength != null && maxPathLength < 0) {
            throw new IllegalArgumentException("maxPathLength must be non-negative if defined");
        }

        // Bootstrap the search with the source vertices
        for (V source : sourceVertices) {
            if (targetVertices.contains(source)) {
                // pathValidator intentionally not invoked here
                completePaths.add(GraphWalk.singletonWalk(graph, source, 0d));
            }

            if (maxPathLength != null && maxPathLength == 0) {
                continue;
            }

            for (E edge : graph.outgoingEdgesOf(source)) {
                assert graph.getEdgeSource(edge).equals(source);

                if (pathValidator == null
                    || pathValidator.isValidPath(GraphWalk.emptyWalk(graph), edge))
                {
                    if (targetVertices.contains(graph.getEdgeTarget(edge))) {
                        completePaths.add(makePath(Collections.singletonList(edge)));
                    }

                    if (edgeMinDistancesFromTargets.containsKey(edge)
                        && (maxPathLength == null || maxPathLength > 1))
                    {
                        List<E> path = Collections.singletonList(edge);
                        incompletePaths.add(path);
                    }
                }
            }
        }

        if (maxPathLength != null && maxPathLength == 0) {
            return completePaths;
        }

        // Walk through the queue of incomplete paths
        for (List<E> incompletePath; (incompletePath = incompletePaths.poll()) != null;) {
            Integer lengthSoFar = incompletePath.size();
            assert (maxPathLength == null) || (lengthSoFar < maxPathLength);

            E leafEdge = incompletePath.get(lengthSoFar - 1);
            V leafNode = graph.getEdgeTarget(leafEdge);

            // pathVertices is only consulted by the simple-path filter below;
            // building it in non-simple mode is wasted work proportional to path length.
            Set<V> pathVertices;
            if (simplePathsOnly) {
                pathVertices = new HashSet<>();
                for (E pathEdge : incompletePath) {
                    pathVertices.add(graph.getEdgeSource(pathEdge));
                    pathVertices.add(graph.getEdgeTarget(pathEdge));
                }
            } else {
                pathVertices = null;
            }

            for (E outEdge : graph.outgoingEdgesOf(leafNode)) {
                // Proceed if the outgoing edge is marked and the mark
                // is sufficiently small
                if (edgeMinDistancesFromTargets.containsKey(outEdge) && ((maxPathLength == null)
                    || ((edgeMinDistancesFromTargets.get(outEdge) + lengthSoFar) <= maxPathLength)))
                {
                    List<E> newPath = new ArrayList<>(incompletePath);
                    newPath.add(outEdge);

                    // If requested, make sure this path isn't self-intersecting
                    if (simplePathsOnly && pathVertices.contains(graph.getEdgeTarget(outEdge))) {
                        continue;
                    }

                    // If requested, validate the path
                    if (pathValidator != null
                        && !pathValidator.isValidPath(makePath(incompletePath), outEdge))
                    {
                        continue;
                    }

                    // If this path reaches a target, add it to completePaths
                    if (targetVertices.contains(graph.getEdgeTarget(outEdge))) {
                        GraphPath<V, E> completePath = makePath(newPath);
                        assert sourceVertices.contains(completePath.getStartVertex());
                        assert targetVertices.contains(completePath.getEndVertex());
                        assert (maxPathLength == null)
                            || (completePath.getLength() <= maxPathLength);
                        completePaths.add(completePath);
                    }

                    // If this path is short enough, consider further
                    // extensions of it
                    if ((maxPathLength == null) || (newPath.size() < maxPathLength)) {
                        incompletePaths.addFirst(newPath); // We use
                                                           // incompletePaths in
                                                           // FIFO mode to avoid
                                                           // memory blowup
                    }
                }
            }
        }

        assert incompletePaths.isEmpty();
        return completePaths;
    }

    /**
     * Transform an ordered list of edges into a GraphPath.
     *
     * The weight of the generated GraphPath is set to the sum of the weights of the edges.
     *
     * @param edges the edges
     *
     * @return the corresponding GraphPath
     */
    private GraphPath<V, E> makePath(List<E> edges)
    {
        V source = graph.getEdgeSource(edges.get(0));
        V target = graph.getEdgeTarget(edges.get(edges.size() - 1));
        double weight = edges.stream().mapToDouble(edge -> graph.getEdgeWeight(edge)).sum();
        return new GraphWalk<>(graph, source, target, edges, weight);
    }
}
