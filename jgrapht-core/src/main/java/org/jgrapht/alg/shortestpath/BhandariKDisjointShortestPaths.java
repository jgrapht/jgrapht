/*
 * (C) Copyright 2018-2018, by Assaf Mizrachi and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

import java.util.*;
import java.util.stream.*;

/**
 * An implementation of Bhandari algorithm for finding $K$ edge-<em>disjoint</em> shortest paths.
 * The algorithm determines the $k$ edge-disjoint shortest simple paths in increasing order of
 * weight. Weights can be negative (but no negative cycle is allowed). Only directed simple graphs
 * are allowed.
 *
 * <p>
 * The algorithm is running $k$ sequential Bellman-Ford iterations to find the shortest path at each
 * step. Hence, yielding a complexity of $k$*O(Bellman-Ford).
 * 
 * <ul>
 * <li>Bhandari, Ramesh 1999. Survivable networks: algorithms for diverse routing. 477. Springer. p.
 * 46. ISBN 0-7923-8381-8.
 * <li>Iqbal, F. and Kuipers, F. A. 2015.
 * <a href="https://www.nas.ewi.tudelft.nl/people/Fernando/papers/Wiley.pdf"> Disjoint Paths in
 * Networks </a>. Wiley Encyclopedia of Electrical and Electronics Engineering. 1–11.
 * </ul>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Assaf Mizrachi
 * @since February 12, 2018
 */
public class BhandariKDisjointShortestPaths<V, E>
    implements
    KShortestPathAlgorithm<V, E>
{
    /**
     * Graph on which shortest paths are searched.
     */
    private final Graph<V, E> workingGraph;

    private List<List<E>> pathList;

    private Set<E> overlappingEdges;

    /**
     * Creates an object to calculate $k$ disjoint shortest paths between the start vertex and
     * others vertices.
     *
     * @param graph graph on which shortest paths are searched.
     *
     * @throws IllegalArgumentException if nPaths is negative or 0.
     * @throws IllegalArgumentException if the graph is null.
     * @throws IllegalArgumentException if the graph is undirected.
     */
    public BhandariKDisjointShortestPaths(Graph<V, E> graph)
    {

        GraphTests.requireDirected(graph);
        if (!GraphTests.isSimple(graph)) {
            throw new IllegalArgumentException("Graph must be simple");
        }
        if (graph.getType().isWeighted()) {
            this.workingGraph = new DefaultDirectedWeightedGraph<>(
                graph.getVertexSupplier(), graph.getEdgeSupplier());
        } else {
            this.workingGraph = new AsWeightedGraph<>(graph, new HashMap<>());
        }
        Graphs.addGraph(workingGraph, graph);
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
        if (endVertex == null) {
            throw new IllegalArgumentException("endVertex is null");
        }
        if (startVertex == null) {
            throw new IllegalArgumentException("startVertex is null");
        }
        if (endVertex.equals(startVertex)) {
            throw new IllegalArgumentException("The end vertex is the same as the start vertex!");
        }
        if (!workingGraph.vertexSet().contains(startVertex)) {
            throw new IllegalArgumentException("graph must contain the start vertex!");
        }
        if (!workingGraph.vertexSet().contains(endVertex)) {
            throw new IllegalArgumentException("graph must contain the end vertex!");
        }

        GraphPath<V, E> currentPath;
        this.pathList = new ArrayList<>();
        BellmanFordShortestPath<V, E> bellmanFordShortestPath;

        for (int cPath = 1; cPath <= k; cPath++) {
            if (cPath > 1) {
                prepare(this.pathList.get(cPath - 2));
            }
            bellmanFordShortestPath = new BellmanFordShortestPath<>(workingGraph);
            currentPath = bellmanFordShortestPath.getPath(startVertex, endVertex);
            if (currentPath != null) {
                pathList.add(currentPath.getEdgeList());
            } else {
                break;
            }
        }

        return pathList.size() > 0 ? resolvePaths(startVertex, endVertex) : Collections.emptyList();

    }

    /**
     * Prepares the graph for a search of the next path: Replacing the edges of the previous path
     * with reversed edges with negative weight
     * 
     * @param previousPath shortest path found on previous round.
     */
    private void prepare(List<E> previousPath)
    {

        V source, target;
        E reversedEdge;

        // replace previous path edges with reversed edges with negative weight
        for (E originalEdge : previousPath) {
            source = workingGraph.getEdgeSource(originalEdge);
            target = workingGraph.getEdgeTarget(originalEdge);
            workingGraph.removeEdge(originalEdge);
            reversedEdge = workingGraph.addEdge(target, source);
            if (reversedEdge != null) {
                workingGraph.setEdgeWeight(reversedEdge, -workingGraph.getEdgeWeight(originalEdge));
            }
        }
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
            V u = workingGraph.getEdgeSource(e);
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
            V v = workingGraph.getEdgeTarget(path.get(0));
            while (!v.equals(endVertex)) {
                E e = sourceToEdgeLookup.get(v).poll();
                path.add(e);
                v = workingGraph.getEdgeTarget(e);
            }
        }

        return paths
            .stream().map(path -> createGraphPath(new ArrayList<>(path), startVertex, endVertex))
            .collect(Collectors.toList());
    }

    /**
     * Iterate over all paths to remove overlapping edges (i.e. those edges contained in more than 
     * one path).
     * Two edges are considered as overlapping in case both edges connect the same vertex pair, 
     * disregarding direction.
     * At the end of this method, each path contains unique edges but not necessarily connecting the
     * start to end vertex.
     * 
     */
    private void findOverlappingEdges()
    {
        Map<UnorderedPair<V, V>, Integer> edgeOccurrenceCount = new HashMap<>();
        for (List<E> path : pathList) {
            for (E e : path) {                
                V v = this.workingGraph.getEdgeSource(e);
                V u = this.workingGraph.getEdgeTarget(e);                
                UnorderedPair<V, V> edgePair = new UnorderedPair<>(v, u);
                
                if (edgeOccurrenceCount.containsKey(edgePair)) {
                    edgeOccurrenceCount.put(edgePair, 2);
                } else {
                    edgeOccurrenceCount.put(edgePair, 1);
                }
            }
        }

        this.overlappingEdges = pathList.stream().flatMap(List::stream).filter(
            e -> edgeOccurrenceCount.get(new UnorderedPair<>(
                this.workingGraph.getEdgeSource(e), 
                this.workingGraph.getEdgeTarget(e))) > 1)
            .collect(Collectors.toSet());
    }

    private GraphPath<V, E> createGraphPath(List<E> edgeList, V startVertex, V endVertex)
    {
        double weight = 0;
        for (E edge : edgeList) {
            weight += workingGraph.getEdgeWeight(edge);
        }
        return new GraphWalk<>(workingGraph, startVertex, endVertex, edgeList, weight);
    }

}
