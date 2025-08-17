/*
 * (C) Copyright 2025, by Lena Büttel and Contributors.
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
package org.jgrapht.alg.steiner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.interfaces.SteinerTreeAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * Implementation of the Kou-Markowsky-Berman algorithm for computing Steiner trees.
 *
 * <p>
 * The Kou-Markowsky-Berman algorithm is an approximation algorithm for the Steiner tree problem. It
 * constructs a Steiner tree by first creating a complete distance graph on the Steiner points
 * (terminals), computing a minimum spanning tree on this graph, replacing each edge with the
 * corresponding shortest path in the original graph, and then optimizing the result.
 *
 * <p>
 * The algorithm runs in $O(|S||V|^2)$ time and it guarantees to output a tree that spans $S$ with
 * total distance on its edges no more than $2 (1-\frac{1}{l})$ times that of the optimal tree,
 * where $l$ is the number of leaves in the optimal tree.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Lena Büttel
 *
 * @see SteinerTreeAlgorithm
 * @see <a href="https://en.wikipedia.org/wiki/Steiner_tree_problem">Steiner tree problem</a>
 */
public class KouMarkowskyBermanAlgorithm<V, E>
    implements
    SteinerTreeAlgorithm<V, E>
{

    private final Graph<V, E> graph;

    /**
     * Construct a new instance of the algorithm.
     *
     * @param graph the input graph; must be connected with non-negative edge weights
     * @throws NullPointerException if the graph is null
     */
    public KouMarkowskyBermanAlgorithm(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireUndirected(graph);
    }

    /**
     * Computes a Steiner tree using the Kou-Markowsky-Berman algorithm.
     *
     * <p>
     * The algorithm finds a tree that connects all the specified Steiner points (terminals) with
     * minimum total weight, potentially using intermediate vertices not in the terminal set. The
     * result is guaranteed to be at most twice the weight of the optimal Steiner tree.
     *
     * @param steinerPoints the set of vertices (terminals) that must be connected by the Steiner
     *        tree; must not be null or empty
     * @return a Steiner tree connecting all the specified vertices
     * @throws IllegalArgumentException if steinerPoints is null or empty
     * @throws RuntimeException if the graph is not connected or contains negative edge weights
     */
    @Override
    public SteinerTree<E> getSteinerTree(Set<V> steinerPoints)
    {
        if (steinerPoints == null || steinerPoints.isEmpty()) {
            throw new IllegalArgumentException("Steiner points cannot be null or empty");
        }

        // Single vertex case
        if (steinerPoints.size() == 1) {
            return new SteinerTreeAlgorithm.SteinerTreeImpl<>(Set.of(), 0.0);
        }

        // Step 1: Create the complete distance graph of selected vertices
        DijkstraShortestPath<V, E> dijkstraAlg = new DijkstraShortestPath<>(graph);

        Graph<V, E> completeGraph =
            new SimpleWeightedGraph<>(graph.getVertexSupplier(), graph.getEdgeSupplier());
        for (V vertex : steinerPoints) {
            completeGraph.addVertex(vertex);
        }

        // Store the actual paths for later reconstruction
        Map<Pair<V, V>, GraphPath<V, E>> storePaths = new HashMap<>();
        List<V> selectedList = new ArrayList<>(steinerPoints);

        // Compute shortest paths between all pairs of Steiner points
        for (int i = 0; i < selectedList.size(); i++) {
            for (int j = i + 1; j < selectedList.size(); j++) {
                V source = selectedList.get(i);
                V target = selectedList.get(j);
                GraphPath<V, E> path = dijkstraAlg.getPath(source, target);
                double weight = path.getWeight();
                storePaths.put(Pair.of(source, target), path);

                E edge = completeGraph.addEdge(source, target);
                completeGraph.setEdgeWeight(edge, weight);
            }
        }

        // Step 2: MST of complete distance graph
        KruskalMinimumSpanningTree<V, E> kruskal = new KruskalMinimumSpanningTree<>(completeGraph);
        Graph<V, E> mstGraph =
            new SimpleWeightedGraph<>(graph.getVertexSupplier(), graph.getEdgeSupplier());

        for (V vertex : completeGraph.vertexSet()) {
            mstGraph.addVertex(vertex);
        }
        for (E edge : kruskal.getSpanningTree().getEdges()) {
            V source = completeGraph.getEdgeSource(edge);
            V target = completeGraph.getEdgeTarget(edge);
            E newEdge = mstGraph.addEdge(source, target);
            double edgeWeight = completeGraph.getEdgeWeight(edge);
            mstGraph.setEdgeWeight(newEdge, edgeWeight);
        }

        // Step 3: Replace MST edges with actual shortest paths
        // This step reconstructs the full paths in the original graph
        Graph<V, E> mstPathGraph =
            new SimpleWeightedGraph<>(graph.getVertexSupplier(), graph.getEdgeSupplier());

        for (E edge : mstGraph.edgeSet()) {
            V source = mstGraph.getEdgeSource(edge);
            V target = mstGraph.getEdgeTarget(edge);

            GraphPath<V, E> path = storePaths.get(Pair.of(source, target));
            if (path == null)
                path = storePaths.get(Pair.of(target, source));
            if (path == null)
                continue;

            List<V> vertices = path.getVertexList();
            for (int i = 0; i < vertices.size() - 1; i++) {
                V v1 = vertices.get(i);
                V v2 = vertices.get(i + 1);

                mstPathGraph.addVertex(v1);
                mstPathGraph.addVertex(v2);

                if (!mstPathGraph.containsEdge(v1, v2)) {
                    E originalEdge = graph.getEdge(v1, v2);
                    if (originalEdge != null) {
                        E newEdge = mstPathGraph.addEdge(v1, v2);
                        mstPathGraph.setEdgeWeight(newEdge, graph.getEdgeWeight(originalEdge));
                    }
                }
            }
        }

        // Step 4: Compute MST of the expanded graph
        // This removes any redundant edges introduced by path expansion
        KruskalMinimumSpanningTree<V, E> kruskal1 = new KruskalMinimumSpanningTree<>(mstPathGraph);
        Graph<V, E> finalMST =
            new SimpleWeightedGraph<>(graph.getVertexSupplier(), graph.getEdgeSupplier());

        for (V vertex : mstPathGraph.vertexSet()) {
            finalMST.addVertex(vertex);
        }
        for (E edge : kruskal1.getSpanningTree().getEdges()) {
            V source = mstPathGraph.getEdgeSource(edge);
            V target = mstPathGraph.getEdgeTarget(edge);
            E newEdge = finalMST.addEdge(source, target);
            finalMST.setEdgeWeight(newEdge, mstPathGraph.getEdgeWeight(edge));
        }

        // Step 5: Prune non-Steiner leaves
        // Remove leaf vertices that are not required Steiner points
        // This optimization step removes unnecessary vertices
        Set<V> leaves = MSTLeavesFinder.findLeaves(finalMST);
        while (true) {
            int removed = 0;
            for (V leaf : new HashSet<>(leaves)) {
                if (!steinerPoints.contains(leaf)) {
                    finalMST.removeVertex(leaf);
                    removed++;
                }
            }
            if (removed == 0)
                break;
            leaves = MSTLeavesFinder.findLeaves(finalMST);
        }

        double totalWeight = finalMST.edgeSet().stream().mapToDouble(finalMST::getEdgeWeight).sum();

        return new SteinerTreeAlgorithm.SteinerTreeImpl<>(finalMST.edgeSet(), totalWeight);
    }

    /**
     * Utility class for finding leaf vertices in a graph.
     *
     * <p>
     * A leaf vertex is a vertex with degree 1 (connected to exactly one other vertex).
     */
    private static class MSTLeavesFinder
    {
        /**
         * Finds all leaf vertices in the given graph.
         *
         * @param <V> the graph vertex type
         * @param <E> the graph edge type
         * @param graph the input graph
         * @return a set of all vertices with degree 1
         */
        public static <V, E> Set<V> findLeaves(Graph<V, E> graph)
        {
            Set<V> leaves = new HashSet<>();
            for (V vertex : graph.vertexSet()) {
                if (graph.degreeOf(vertex) == 1) {
                    leaves.add(vertex);
                }
            }
            return leaves;
        }
    }

}
