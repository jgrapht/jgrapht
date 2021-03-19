/*
 * (C) Copyright 2018-2021, by Assaf Mizrachi and Contributors.
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
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

import java.util.*;
import java.util.stream.*;

/**
 * A base implementation of a $k$ disjoint shortest paths algorithm based on the strategy used in
 * Suurballe and Bhandari algorithms. The algorithm procedure goes as follow:
 * <ol>
 * <li>Using some known shortest path algorithm (e.g. Dijkstra) to find the shortest path $P_1$ from
 * source to target.
 * <li>For i = 2,...,$k$
 * <li>&emsp;Perform some graph transformations based on the previously found path
 * <li>&emsp;Find the shortest path $P_i$ from source to target
 * <li>Remove all overlapping edges to get $k$ disjoint paths.
 * </ol>
 * The class implements the above procedure and resolves final paths (step 5) from the intermediate
 * path results found in step 4. An extending class has to implement two methods:
 * <ul>
 * <li>{@link #transformGraph} - to be used in step 3.
 * <li>{@link #calculateShortestPath} - to be used in step 4.
 * </ul>
 * Currently known extensions are {@link SuurballeKDisjointShortestPaths} and
 * {@link BhandariKDisjointShortestPaths}.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Assaf Mizrachi
 * @author Benjamin Krogh
 */
abstract class BaseKDisjointShortestPathsAlgorithm<V, E>
    implements
    KShortestPathAlgorithm<V, E>
{

    /**
     * Graph on which shortest paths are searched.
     */
    protected Graph<V, E> workingGraph;

    protected List<List<E>> pathList;

    protected Graph<V, E> originalGraph;
    private Set<E> validEdges;

    /**
     * Creates a new instance of the algorithm
     *
     * @param graph graph on which shortest paths are searched.
     *
     * @throws IllegalArgumentException if the graph is null.
     * @throws IllegalArgumentException if the graph is undirected.
     * @throws IllegalArgumentException if the graph is not simple.
     */
    public BaseKDisjointShortestPathsAlgorithm(Graph<V, E> graph)
    {

        this.originalGraph = graph;
        GraphTests.requireDirected(graph);
        if (!GraphTests.isSimple(graph)) {
            throw new IllegalArgumentException("Graph must be simple");
        }

    }

    /**
     * Returns the $k$ shortest simple paths in increasing order of weight.
     *
     * @param startVertex source vertex of the calculated paths.
     * @param endVertex target vertex of the calculated paths.
     *
     * @return list of disjoint paths between the start vertex and the end vertex
     * 
     * @throws IllegalArgumentException if the graph does not contain the startVertex or the
     *         endVertex
     * @throws IllegalArgumentException if the startVertex and the endVertex are the same vertices
     * @throws IllegalArgumentException if the startVertex or the endVertex is null
     */
    @Override
    public List<GraphPath<V, E>> getPaths(V startVertex, V endVertex, int k)
    {
        if (k <= 0) {
            throw new IllegalArgumentException("Number of paths must be positive");
        }
        Objects.requireNonNull(startVertex, "startVertex is null");
        Objects.requireNonNull(endVertex, "endVertex is null");
        if (endVertex.equals(startVertex)) {
            throw new IllegalArgumentException("The end vertex is the same as the start vertex!");
        }
        if (!originalGraph.containsVertex(startVertex)) {
            throw new IllegalArgumentException("graph must contain the start vertex!");
        }
        if (!originalGraph.containsVertex(endVertex)) {
            throw new IllegalArgumentException("graph must contain the end vertex!");
        }

        // Create a working graph copy to avoid modifying the underlying graph. This gets
        // reinitialized for every call to getPaths since previous calls may have modified it. Since
        // the original graph may be using intrusive edges, we have to use an AsWeightedGraph view
        // (even when the graph copy is already weighted) to avoid writing weight changes through to
        // the underlying graph.
        this.workingGraph = new AsWeightedGraph<>(
            new DefaultDirectedWeightedGraph<>(
                this.originalGraph.getVertexSupplier(), this.originalGraph.getEdgeSupplier()),
            new HashMap<>(), false);
        Graphs.addGraph(workingGraph, this.originalGraph);

        this.pathList = new ArrayList<>();
        GraphPath<V, E> currentPath = calculateShortestPath(startVertex, endVertex);
        if (currentPath != null) {
            pathList.add(currentPath.getEdgeList());

            for (int i = 0; i < k - 1; i++) {
                transformGraph(this.pathList.get(i));
                currentPath = calculateShortestPath(startVertex, endVertex);

                if (currentPath != null) {
                    pathList.add(currentPath.getEdgeList());
                } else {
                    break;
                }
            }
        }

        return pathList.size() > 0 ? resolvePaths(startVertex, endVertex) : Collections.emptyList();

    }

    /**
     * At the end of the search we have list of intermediate paths - not necessarily disjoint and
     * may contain reversed edges. Here we go over all, removing overlapping edges and merging them
     * to valid paths (from start to end). Finally, we sort them according to their weight.
     * 
     * @param startVertex the start vertex
     * @param endVertex the end vertex
     * 
     * @return sorted list of disjoint paths from start vertex to end vertex.
     */
    private List<GraphPath<V, E>> resolvePaths(V startVertex, V endVertex)
    {
        // first we need to remove overlapping edges.
        findValidEdges();

        // now we might be left with path fragments (not necessarily leading from start to end).
        // We need to merge them to valid paths.
        List<GraphPath<V, E>> paths = buildPaths(startVertex, endVertex);

        // sort paths by overall weight (ascending)
        Collections.sort(paths, Comparator.comparingDouble(GraphPath::getWeight));
        return paths;
    }

    /**
     * After removing overlapping edges, each path is not necessarily connecting start to end
     * vertex. Here we connect the path fragments to valid paths (from start to end).
     * 
     * @param startVertex the start vertex
     * @param endVertex the end vertex
     * 
     * @return list of disjoint paths from start to end.
     */
    private List<GraphPath<V, E>> buildPaths(V startVertex, V endVertex)
    {
        Map<V,
            ArrayDeque<E>> sourceVertexToEdge = this.validEdges
                .stream().collect(
                    Collectors
                        .groupingBy(this::getEdgeSource, Collectors.toCollection(ArrayDeque::new)));
        ArrayDeque<E> startEdges = sourceVertexToEdge.get(startVertex);
        List<GraphPath<V, E>> result = new ArrayList<>();
        for (E edge : startEdges) {
            final List<E> resultPath = new ArrayList<>();
            resultPath.add(edge);
            while (true) {
                final V edgeTarget = getEdgeTarget(edge);
                if (edgeTarget.equals(endVertex)) {
                    break;
                }
                ArrayDeque<E> outgoingEdges = sourceVertexToEdge.get(edgeTarget);
                edge = outgoingEdges.poll();
                resultPath.add(edge);
            }
            GraphPath<V, E> graphPath = createGraphPath(resultPath, startVertex, endVertex);
            result.add(graphPath);
        }
        return result;
    }

    /**
     * Iterate over all paths and remove all edges used an even number of times. The remaining edges
     * forms the valid edge set, which is used in the buildPaths method to construct the k-shortest
     * paths
     */
    private void findValidEdges()
    {
        Map<UnorderedPair<V, V>, E> validEdges = new LinkedHashMap<>();
        for (List<E> path : pathList) {
            for (E e : path) {
                V v = this.getEdgeSource(e);
                V u = this.getEdgeTarget(e);
                UnorderedPair<V, V> edgePair = new UnorderedPair<>(v, u);
                validEdges.compute(edgePair, (unused, edge) -> edge == null ? e : null);
            }
        }
        this.validEdges = new LinkedHashSet<>(validEdges.values());
    }

    private GraphPath<V, E> createGraphPath(List<E> edgeList, V startVertex, V endVertex)
    {
        double weight = 0;
        for (E edge : edgeList) {
            weight += originalGraph.getEdgeWeight(edge);
        }
        return new GraphWalk<>(originalGraph, startVertex, endVertex, edgeList, weight);
    }

    private V getEdgeSource(E e)
    {
        return this.workingGraph.containsEdge(e) ? this.workingGraph.getEdgeSource(e)
            : this.originalGraph.getEdgeSource(e);
    }

    private V getEdgeTarget(E e)
    {
        return this.workingGraph.containsEdge(e) ? this.workingGraph.getEdgeTarget(e)
            : this.originalGraph.getEdgeTarget(e);
    }

    /**
     * Calculates the shortest paths for the current iteration. Path is not final; rather, it is
     * intended to be used in a "post-production" phase (see resolvePaths method).
     * 
     * @param startVertex the start vertex
     * @param endVertex the end vertex
     * 
     * @return the shortest path between start and end vertices.
     */
    protected abstract GraphPath<V, E> calculateShortestPath(V startVertex, V endVertex);

    /**
     * Prepares the working graph for next iteration. To be called from the second iteration and on
     * so implementation may assume a preceding {@link #calculateShortestPath} call.
     * 
     * @param previousPath the path found at the previous iteration.
     */
    protected abstract void transformGraph(List<E> previousPath);

}
