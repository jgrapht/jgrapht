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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm;
import org.jgrapht.alg.matching.HopcroftKarpMaximumCardinalityBipartiteMatching;
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
 * The implementation is based on: Bunus P., Fritzson P., Methods for Structural
 * Analysis and Debugging of Modelica Models, 2nd International Modelica
 * Conference 2002 It has a running time of $O(V + E)$.
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
     * Construct the algorithm for a given bipartite graph $G=(V_1,V_2,E)$ and
     * it's partitions $V_1$ and $V_2$, where $V_1\cap V_2=\emptyset$.
     *
     * @param graph bipartite graph
     * @param partition1 the first partition, V_1, of vertices in the bipartite graph
     * @param partition2 the second partition, V_2, of vertices in the bipartite graph
     */
    public DulmageMendelsohnDecomposition(Graph<V, E> graph, Set<V> partition1, Set<V> partition2) {
        this.graph = Objects.requireNonNull(graph);
        this.partition1 = partition1;
        this.partition2 = partition2;
        assert GraphTests.isBipartite(graph);
    }

    /**
     * Perform the decomposition, using the Hopcroft-Karp maximum-cardinality
     * matching algorithm to perform the matching.
     *
     * @param fine true if the fine decomposition is required, false if the
     * coarse decomposition is required
     * @return the {@link Decomposition}
     */
    public Decomposition<V, E> getDecomposition(boolean fine) {
        // Get a maximum matching to the bipartite problem
        HopcroftKarpMaximumCardinalityBipartiteMatching<V, E> hopkarp
                = new HopcroftKarpMaximumCardinalityBipartiteMatching<>(graph, partition1, partition2);
        Matching<V, E> matching = hopkarp.getMatching();
        return decompose(matching, fine);
    }

    /**
     * Perform the decomposition, using a pre-calculated bipartite matching
     *
     * @param matching the matching from a {@link MatchingAlgorithm}
     * @param fine true if the fine decomposition is required
     * @return the {@link Decomposition}
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
            Graph<E, DefaultEdge> graphH = asDirectedEdgeGraph(matching, subset3);

            // Perform strongly-connected-components on the graph
            StrongConnectivityAlgorithm<E, DefaultEdge> sci
                    = new KosarajuStrongConnectivityInspector<>(graphH);
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
            return new Decomposition<>(subset1, subset2, out);
        } else {
            return new Decomposition<>(subset1, subset2, Collections.singletonList(subset3));
        }
    }

    /**
     * The output of a decomposition operation
     *
     * @param <V> vertex type
     * @param <E> edge type
     */
    public static class Decomposition<V, E> {

        private final Set<V> subset1;
        private final Set<V> subset2;
        private final List<Set<V>> subset3;

        Decomposition(Set<V> subset1, Set<V> subset2, List<Set<V>> subset3) {
            this.subset1 = subset1;
            this.subset2 = subset2;
            this.subset3 = subset3;
        }

        /**
         * Gets the subset dominated by partition1.
         * Where D is the set of vertices in G that are not matched in the 
         * maximum matching of G, this set contains members of the first partition 
         * and vertices from the second partition that neighbour them.
         *
         * @return The vertices in D ∩ V_1 and their neighbours
         */
        public Set<V> getPartition1DominatedSet() {
            return subset1;
        }

        /**
         * Gets the subset dominated by partition2.
         * Where D is the set of vertices in G that are not matched in the 
         * maximum matching of G, this set contains members of the second partition 
         * and vertices from the first partition that neighbour them.
         *
         * @return The vertices in D ∩ V and their neighbours
         */
        public Set<V> getPartition2DominatedSet() {
            return subset2;
        }

        /**
         * Gets the remaining subset, or subsets in the fine decomposition.
         * This set contains vertices that are matched in the maximum matching 
         * of the graph G. If the fine decomposition was used, this will be 
         * multiple sets, each a strongly-connected-component of the matched 
         * subset of G.
         *
         * @return List of Sets of vertices in the subsets
         */
        public List<Set<V>> getPerfectMatchedSets() {
            return subset3;
        }
    }

    private void getUnmatched(Matching<V, E> matching, Set<V> unmatched1, Set<V> unmatched2) {
        unmatched1.addAll(partition1);
        unmatched2.addAll(partition2);
        matching.forEach((e) -> {
            V source = graph.getEdgeSource(e);
            V target = graph.getEdgeTarget(e);
            if (partition1.contains(source)) {
                unmatched1.remove(source);
                unmatched2.remove(target);
            } else {
                unmatched2.remove(source);
                unmatched1.remove(target);
            }
        });
    }

    private Graph<V, DefaultEdge> asDirectedGraph(Matching<V, E> matching) {
        GraphBuilder<V, DefaultEdge, ? extends DefaultDirectedGraph<V, DefaultEdge>> builder
                = DefaultDirectedGraph.createBuilder(DefaultEdge.class);
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
        GraphBuilder<E, DefaultEdge, ? extends DefaultDirectedGraph<E, DefaultEdge>> graphHBuilder
                = DefaultDirectedGraph.createBuilder(DefaultEdge.class);
        for (E e : graph.edgeSet()) {
            V v1 = graph.getEdgeSource(e);
            V v2 = graph.getEdgeTarget(e);
            if (subset.contains(v1) && subset.contains(v2)) {
                if (matching.getEdges().contains(e)) {
                    graphHBuilder.addVertex(e);
                } else {
                    E e1 = null;
                    E e2 = null;
                    for (E other : graph.edgesOf(v1)) {
                        if (matching.getEdges().contains(other)) {
                            e1 = other;
                            graphHBuilder.addVertex(e1);
                            break;
                        }
                    }
                    for (E other : graph.edgesOf(v2)) {
                        if (matching.getEdges().contains(other)) {
                            e2 = other;
                            graphHBuilder.addVertex(e2);
                            break;
                        }
                    }
                    graphHBuilder.addEdge(e1, e2);
                }
            }
        }
        return graphHBuilder.build();
    }
}
