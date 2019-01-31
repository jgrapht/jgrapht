/*
 * (C) Copyright 2018-2018, by Assaf Mizrachi and Contributors.
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

    protected Set<E> overlappingEdges;

    protected Graph<V, E> originalGraph;

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
        findOverlappingEdges();

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
        List<List<E>> paths = new ArrayList<>();
        Map<V, ArrayDeque<E>> sourceToEdgeLookup = new HashMap<>();
        Set<E> nonOverlappingEdges = pathList
            .stream().flatMap(List::stream).filter(e -> !this.overlappingEdges.contains(e))
            .collect(Collectors.toSet());

        for (E e : nonOverlappingEdges) {
            V u = getEdgeSource(e);
            if (u.equals(startVertex)) { // start of a new path
                List<E> path = new ArrayList<>();
                path.add(e);
                paths.add(path);
            } else { // some edge which is part of a path
                if (!sourceToEdgeLookup.containsKey(u)) {
                    sourceToEdgeLookup.put(u, new ArrayDeque<>());
                }
                sourceToEdgeLookup.get(u).add(e);
            }
        }

        // Build the paths using the lookup table
        for (List<E> path : paths) {
            V v = getEdgeTarget(path.get(0));
            while (!v.equals(endVertex)) {
                E e = sourceToEdgeLookup.get(v).poll();
                path.add(e);
                v = getEdgeTarget(e);
            }
        }

        return paths
            .stream().map(path -> createGraphPath(new ArrayList<>(path), startVertex, endVertex))
            .collect(Collectors.toList());
    }

    /**
     * Iterate over all paths to remove overlapping edges (i.e. those edges contained in more than
     * one path). Two edges are considered as overlapping in case both edges connect the same vertex
     * pair, disregarding direction. At the end of this method, each path contains unique edges but
     * not necessarily connecting the start to end vertex.
     * 
     */
    private void findOverlappingEdges()
    {
        Map<UnorderedPair<V, V>, Integer> edgeOccurrenceCount = new HashMap<>();
        for (List<E> path : pathList) {
            for (E e : path) {
                V v = this.getEdgeSource(e);
                V u = this.getEdgeTarget(e);
                UnorderedPair<V, V> edgePair = new UnorderedPair<>(v, u);

                if (edgeOccurrenceCount.containsKey(edgePair)) {
                    edgeOccurrenceCount.put(edgePair, 2);
                } else {
                    edgeOccurrenceCount.put(edgePair, 1);
                }
            }
        }

        this.overlappingEdges = pathList
            .stream().flatMap(List::stream)
            .filter(
                e -> edgeOccurrenceCount
                    .get(new UnorderedPair<>(this.getEdgeSource(e), this.getEdgeTarget(e))) > 1)
            .collect(Collectors.toSet());
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
