/*
 * (C) Copyright 2018-2018, by CAE Tech Limited and Contributors.
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
package org.jgrapht.alg.connectivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm;
import org.jgrapht.alg.matching.HopcroftKarpMaximumCardinalityBipartiteMatching;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.traverse.DepthFirstIterator;

/**
 * The Dulmage-Mendelsohn Decomposition
 * (https://en.wikipedia.org/wiki/Dulmage%E2%80%93Mendelsohn_decomposition)
 * partitions a bipartite graph into subsets. One subset is dominated by the
 * first partition, one by the second partition, and the third is the remaining
 * vertices.
 *
 * A fine decomposition can be performed that additionally performs a
 * strongly-connected-components algorithm on the remaining subset.
 * 
 * The implementation is based on:
 * Bunus P., Fritzson P., Methods for Structural Analysis and Debugging of Modelica Models, 2nd International Modelica Conference 2002
 * 
 * @author Peter Harman
 * @param <V> Vertex type
 * @param <E> Edge type
 */
public class DulmageMendelsohnDecomposition<V, E> {

    private final Graph<V, E> graph;
    private final Set<V> partition1;
    private final Set<V> partition2;

    /**
     * Construct the algorithm for a given (bipartite) graph and it's partitions
     *
     * @param graph bipartite graph
     * @param partition1 the first partition of vertices in the bipartite graph
     * @param partition2 the second partition of vertices in the bipartite graph
     */
    public DulmageMendelsohnDecomposition(Graph<V, E> graph, Set<V> partition1, Set<V> partition2) {
        this.graph = graph;
        this.partition1 = partition1;
        this.partition2 = partition2;
    }

    /**
     * Perform the decomposition, using Hopcroft-Karp for the matching
     *
     * @param fine true if the fine decomposition is required
     * @return the Decomposition
     */
    public Decomposition<V, E> decompose(boolean fine) {
        // Get a maximum matching to the bipartite problem
        HopcroftKarpMaximumCardinalityBipartiteMatching<V, E> hopkarp = new HopcroftKarpMaximumCardinalityBipartiteMatching<>(graph, partition1, partition2);
        Matching<V, E> matching = hopkarp.getMatching();
        return decompose(matching, fine);
    }

    /**
     * Perform the decomposition, using a precalculated bipartite matching
     *
     * @param matching the matching from a MatchingAlgorithm
     * @param fine true if the fine decomposition is required
     * @return the Decomposition
     */
    public Decomposition<V, E> decompose(Matching<V, E> matching, boolean fine) {
        // Determine the unmatched vertices from both partitions
        Set<V> unmatched1 = new HashSet<>();
        Set<V> unmatched2 = new HashSet<>();
        getUnmatched(matching, unmatched1, unmatched2);
        // Assemble a directed graph
        Graph<V, DefaultEdge> dg = asDirectedGraph(matching);
        // Find the non-square subgraph dominated by partition1
        Set<V> subset1 = new HashSet<>();
        unmatched1.stream().map((v) -> {
            subset1.add(v);
            return v;
        }).map((v) -> new DepthFirstIterator<>(dg, v)).forEachOrdered((it) -> {
            while (it.hasNext()) {
                subset1.add(it.next());
            }
        });
        // Find the non-square subgraph dominated by partition2
        Graph<V, DefaultEdge> gd = new EdgeReversedGraph<>(dg);
        Set<V> subset2 = new HashSet<>();
        unmatched2.stream().map((v) -> {
            subset2.add(v);
            return v;
        }).map((v) -> new DepthFirstIterator<>(gd, v)).forEachOrdered((it) -> {
            while (it.hasNext()) {
                subset2.add(it.next());
            }
        });
        // Find the square subgraph
        Set<V> subset3 = new HashSet<>();
        subset3.addAll(partition1);
        subset3.addAll(partition2);
        subset3.removeAll(subset1);
        subset3.removeAll(subset2);
        if (fine) {
            List<Set<V>> out = new ArrayList<>();
            // Build a directed graph between edges of the matching in subset3
            Graph<E, DefaultEdge> H = asDirectedEdgeGraph(matching, subset3);

            // Perform strongly-connected-components on the graph
            StrongConnectivityAlgorithm<E, DefaultEdge> sci
                    = new KosarajuStrongConnectivityInspector<>(H);
            // Divide into sets of vertices
            for (Set<E> edgeSet : sci.stronglyConnectedSets()) {
                Set<V> vertexSet = new HashSet<>();
                edgeSet.stream().map((edge) -> {
                    vertexSet.add(graph.getEdgeSource(edge));
                    return edge;
                }).forEachOrdered((edge) -> {
                    vertexSet.add(graph.getEdgeTarget(edge));
                });
                out.add(vertexSet);
            }
            return new Decomposition<>(graph, subset1, subset2, out);
        } else {
            return new Decomposition<>(graph, subset1, subset2, Arrays.asList(subset3));
        }
    }

    /**
     * The output of a decomposition operation
     *
     * @param <V>
     * @param <E>
     */
    public static class Decomposition<V, E> {

        private final Graph<V, E> graph;
        private final Set<V> subset1;
        private final Set<V> subset2;
        private final List<Set<V>> subset3;

        Decomposition(Graph<V, E> graph, Set<V> subset1, Set<V> subset2, List<Set<V>> subset3) {
            this.graph = graph;
            this.subset1 = subset1;
            this.subset2 = subset2;
            this.subset3 = subset3;
        }

        /**
         * Gets the subset dominated by partition1
         *
         * @return Set of vertices in the subset
         */
        public Set<V> getPartition1DominatedSet() {
            return subset1;
        }

        /**
         * Gets the subset dominated by partition2
         *
         * @return Set of vertices in the subset
         */
        public Set<V> getPartition2DominatedSet() {
            return subset2;
        }

        /**
         * Gets the remaining subset, or subsets in the fine decomposition
         *
         * @return List of Sets of vertices in the subsets
         */
        public List<Set<V>> getPerfectMatchedSets() {
            return subset3;
        }

        /**
         * Gets the subgraph dominated by partition1
         *
         * @return Subgraph
         */
        public Graph<V, E> getPartition1DominatedSubgraph() {
            return getUndirectedSubgraph(subset1);
        }

        /**
         * Gets the subgraph dominated by partition2
         *
         * @return Subgraph
         */
        public Graph<V, E> getPartition2DominatedSubgraph() {
            return getUndirectedSubgraph(subset2);
        }

        /**
         * Gets the remaining subgraphs
         *
         * @return List of subgraphs
         */
        public List<Graph<V, E>> getPerfectMatchedSubgraphs() {
            List<Graph<V, E>> out = new ArrayList<>(subset3.size());
            subset3.forEach((set) -> {
                out.add(getUndirectedSubgraph(set));
            });
            return out;
        }

        private Graph<V, E> getUndirectedSubgraph(Set<V> subset) {
            return new AsSubgraph<>(graph, subset);
        }
    }

    private void getUnmatched(Matching<V, E> matching, Set<V> unmatched1, Set<V> unmatched2) {
        unmatched1.addAll(partition1);
        unmatched2.addAll(partition2);
        matching.forEach((e) -> {
            if (partition1.contains(graph.getEdgeSource(e))) {
                unmatched1.remove(graph.getEdgeSource(e));
                unmatched2.remove(graph.getEdgeTarget(e));
            } else {
                unmatched2.remove(graph.getEdgeSource(e));
                unmatched1.remove(graph.getEdgeTarget(e));
            }
        });
    }

    private Graph<V, DefaultEdge> asDirectedGraph(Matching<V, E> matching) {
        GraphBuilder<V, DefaultEdge, ? extends DefaultDirectedGraph<V, DefaultEdge>> builder = DefaultDirectedGraph.createBuilder(DefaultEdge.class);
        graph.vertexSet().forEach((v) -> {
            builder.addVertex(v);
        });
        graph.edgeSet().forEach((e) -> {
            V v1 = graph.getEdgeSource(e);
            V v2 = graph.getEdgeTarget(e);
            if (partition1.contains(v1)) {
                builder.addEdge(v1, v2);
                if (matching.getEdges().contains(e)) {
                    builder.addEdge(v2, v1);
                }
            } else {
                builder.addEdge(v2, v1);
                if (matching.getEdges().contains(e)) {
                    builder.addEdge(v1, v2);
                }
            }
        });
        return builder.build();
    }

    private Graph<E, DefaultEdge> asDirectedEdgeGraph(Matching<V, E> matching, Set<V> subset) {
        GraphBuilder<E, DefaultEdge, ? extends DefaultDirectedGraph<E, DefaultEdge>> H = DefaultDirectedGraph.createBuilder(DefaultEdge.class);
        for (E e : graph.edgeSet()) {
            V v1 = graph.getEdgeSource(e);
            V v2 = graph.getEdgeTarget(e);
            if (subset.contains(v1) && subset.contains(v2)) {
                if (matching.getEdges().contains(e)) {
                    H.addVertex(e);
                } else {
                    E e1 = null;
                    E e2 = null;
                    for (E other : graph.edgesOf(v1)) {
                        if (matching.getEdges().contains(other)) {
                            e1 = other;
                            H.addVertex(e1);
                            break;
                        }
                    }
                    for (E other : graph.edgesOf(v2)) {
                        if (matching.getEdges().contains(other)) {
                            e2 = other;
                            H.addVertex(e2);
                            break;
                        }
                    }
                    H.addEdge(e1, e2);
                }
            }
        }
        return H.build();
    }
}
