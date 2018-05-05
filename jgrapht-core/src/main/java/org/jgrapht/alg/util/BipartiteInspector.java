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
package org.jgrapht.alg.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
import org.jgrapht.util.ArrayUnenforcedSet;

/**
 *
 * @author Peter Harman
 * @param <V> Vertex type
 * @param <E> Edge type
 */
public class BipartiteInspector<V, E> {

    private final Graph<V, E> graph;
    private final Set<V> partition1;
    private final Set<V> partition2;

    /**
     * Constructs a BipartiteInspector with graph and partitions
     * 
     * @param graph the graph
     * @param partition1 first partition
     * @param partition2 second partition
     */
    public BipartiteInspector(Graph<V, E> graph, List<V> partition1, List<V> partition2) {
        this(graph, (Set<V>) new ArrayUnenforcedSet<V>(partition1), 
                (Set<V>) new ArrayUnenforcedSet<V>(partition2));
    }

    /**
     * Constructs a BipartiteInspector with graph and partitions
     * 
     * @param graph the graph
     * @param partition1 first partition
     * @param partition2 second partition
     */
    public BipartiteInspector(Graph<V, E> graph, Set<V> partition1, Set<V> partition2) {
        this.graph = graph;
        this.partition1 = partition1;
        this.partition2 = partition2;
    }

    /**
     * Ensures the graph and partitions form a valid bipartite graph
     * 
     * @return true if graph is bipartite, false if not
     */
    public boolean isBipartite() {
        // Check the partitions match the vertices
        if (intersection(partition1, partition2).isEmpty()) {
            Set<V> union = union(partition1, partition2);
            if (difference(graph.vertexSet(), union).isEmpty()) {
                if (difference(union, graph.vertexSet()).isEmpty()) {
                    // Check the edges are all between partitions
                    return graph.edgeSet().stream().noneMatch(
                            (e) -> ((partition1.contains(graph.getEdgeSource(e))
                            && partition1.contains(graph.getEdgeTarget(e)))
                            || (partition2.contains(graph.getEdgeSource(e))
                                    && partition2.contains(graph.getEdgeTarget(e)))));
                }
            }
        }
        return false;
    }

    /**
     * Gets the edges from the bipartite matching
     *
     * @param matching Matching from a bipartite MatchingAlgorithm
     * @return Set of edges
     */
    public Set<E> getMatchedEdges(Matching<V, E> matching) {
        return matching.getEdges();
    }

    /**
     * Gets the vertices from the bipartite matching
     *
     * @param matching Matching from a bipartite MatchingAlgorithm
     * @return Set of vertices
     */
    public Set<V> getMatchedVertices(Matching<V, E> matching) {
        Set<V> out = new HashSet<>();
        getMatchedEdges(matching).stream().map((e) -> {
            out.add(graph.getEdgeSource(e));
            return e;
        }).forEachOrdered((e) -> {
            out.add(graph.getEdgeTarget(e));
        });
        return out;
    }

    /**
     * Gets the edges not matched in the bipartite matching
     *
     * @param matching Matching from a bipartite MatchingAlgorithm
     * @return Set of edges
     */
    public Set<E> getUnmatchedEdges(Matching<V, E> matching) {
        return difference(graph.edgeSet(), getMatchedEdges(matching));
    }

    /**
     * Gets the vertices not matched in the bipartite matching
     *
     * @param matching Matching from a bipartite MatchingAlgorithm
     * @return Set of vertices
     */
    public Set<V> getUnmatchedVertices(Matching<V, E> matching) {
        return difference(graph.vertexSet(), getMatchedVertices(matching));
    }

    /**
     * Set difference
     * @param <T> Vertex type
     * @param set1 first set
     * @param set2 second set
     * @return Set of objects in set1 and not in set2
     */
    protected static <T> Set<T> difference(Set<T> set1, Set<T> set2) {
        Set<T> diff = new HashSet<>(set1);
        diff.removeAll(set2);
        return diff;
    }

    /**
     * Set union
     * @param <T> Vertex type
     * @param set1 first set
     * @param set2 second set
     * @return Set of objects in either set1 or set2
     */
    protected static <T> Set<T> union(Set<T> set1, Set<T> set2) {
        Set<T> union = new HashSet<>(set1);
        union.addAll(set2);
        return union;
    }

    /**
     * Set intersection
     * @param <T> Vertex type
     * @param set1 first set
     * @param set2 second set
     * @return Set of objects in both set1 and set2
     */
    protected static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        Set<T> inter = new HashSet<>(set1);
        inter.retainAll(set2);
        return inter;
    }

}
