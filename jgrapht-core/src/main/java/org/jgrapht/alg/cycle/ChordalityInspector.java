/*
 * (C) Copyright 2018, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.LexBreadthFirstIterator;
import org.jgrapht.traverse.MaximumCardinalityIterator;

import java.util.*;

/**
 * Allows testing chordality of a graph. The inspected {@code graph} is specified at construction time
 * and cannot be modified. Currently chordality of a graph is tested via {@link MaximumCardinalityIterator}
 * by default. When {@link IterationOrder#LEX_BFS} is specified as a second constructor parameter, this
 * {@code ChordalityInspector} uses {@link LexBreadthFirstIterator} to compute perfect elimination order.
 * <p>
 * A <a href="https://en.wikipedia.org/wiki/Chordal_graph">chordal graph</a> is one in which all cycles of
 * four or more vertices have a chord, which is an edge that is not part of the cycle but connects two vertices
 * of the cycle.
 * <p>
 * A graph is chordal iff its the vertices can be arranged into a perfect elimination order. More than one perfect
 * elimination order may exist for a given graph. Either maximum cardinality search or lexicographical breadth-first
 * search can be used to produce such an order.
 * <p>
 * Both lexicographical BFS and maximum cardinality search run in $\mathcal{O}(|V| + |E|)$. Checking whether given order
 * is the perfect elimination order via {@link ChordalityInspector#isPerfectEliminationOrder(List)} takes
 * $\mathcal{O}(|V| + |E|)$ as well. So, overall time complexity of the method
 * {@link ChordalityInspector#isChordal()} is $\mathcal{O}(|V| + |E|)$.
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 * @author Timofey Chudakov
 * @see LexBreadthFirstIterator
 * @see MaximumCardinalityIterator
 * @since March 2018
 */
public class ChordalityInspector<V, E> {
    /**
     * The inspected graph.
     */
    private Graph<V, E> graph;
    /**
     * Contains true if the graph is chordal, otherwise false. Is null before the first call to the
     * {@link ChordalityInspector#isChordal()}.
     */
    private Boolean chordal = null;
    /**
     * Order produced by {@code orderIterator}.
     */
    private List<V> order;
    /**
     * Iterator used for producing perfect elimination order.
     */
    private GraphIterator<V, E> orderIterator;
    /**
     * Stores the type of iterator used by this {@code ChordalityInspector}.
     */
    private final IterationOrder iterationOrder;

    /**
     * Creates a chordality inspector for {@code graph}, which uses {@link MaximumCardinalityIterator}
     * as a default iterator.
     *
     * @param graph the graph for which a chordality inspector to be created.
     */
    public ChordalityInspector(Graph<V, E> graph) {
        this(graph, IterationOrder.MCS);
    }

    /**
     * Creates a chordality inspector for {@code graph}, which uses an iterator defined by the second
     * parameter as an internal iterator.
     *
     * @param graph          the graph for which a chordality inspector is to be created.
     * @param iterationOrder the constant, which defines iterator to be used by this {@code ChordalityInspector}.
     */
    public ChordalityInspector(Graph<V, E> graph, IterationOrder iterationOrder) {
        this.graph = Objects.requireNonNull(graph);
        this.iterationOrder = iterationOrder;
        if (graph.getType().isDirected()) {
            this.graph = new AsUndirectedGraph<>(graph);
        }
        if (iterationOrder == IterationOrder.MCS) {
            this.orderIterator = new MaximumCardinalityIterator<>(graph);
        } else {
            this.orderIterator = new LexBreadthFirstIterator<>(graph);
        }
    }

    /**
     * Checks whether the inspected graph is chordal.
     *
     * @return true if this graph is chordal, otherwise false.
     */
    public boolean isChordal() {
        if (chordal == null) {
            order = lazyComputeOrder();
            chordal = isPerfectEliminationOrder(order);
        }
        return chordal;
    }

    /**
     * Returns the computed vertex order. In the case where inspected graph is chordal, returned order
     * is a perfect elimination order.
     *
     * @return computed vertex order.
     */
    public List<V> getSearchOrder() {
        return lazyComputeOrder();
    }

    /**
     * Checks whether the vertices in the {@code vertexOrder} are in perfect elimination order with
     * respect to the inspected graph. Returns false, if the inspected graph isn't chordal.
     *
     * @param vertexOrder the sequence of vertices of the {@code graph}.
     * @return true if the {@code graph} is chordal and the vertices in {@code vertexOrder} are in
     * perfect elimination order, otherwise false.
     */
    public boolean isPerfectEliminationOrder(List<V> vertexOrder) {
        Set<V> graphVertices = graph.vertexSet();
        if (graphVertices.size() == vertexOrder.size() && graphVertices.containsAll(vertexOrder)) {
            Map<V, Integer> map = new HashMap<>(vertexOrder.size());
            int i = 0;
            for (V vertex : vertexOrder) {
                map.put(vertex, i);
                ++i;
            }
            return isPerfectEliminationOrder(vertexOrder, map);
        } else {
            return false;
        }
    }

    /**
     * Computes vertex order via {@code orderIterator}.
     *
     * @return computed order.
     */
    private List<V> lazyComputeOrder() {
        if (order == null) {
            int vertexNum = graph.vertexSet().size();
            order = new ArrayList<>(vertexNum);
            for (int i = 0; i < vertexNum; i++) {
                order.add(orderIterator.next());
            }
        }
        return order;
    }

    /**
     * Checks whether the vertices in the {@code vertexOrder} are in perfect elimination order.
     * Returns false, if the inspected graph isn't chordal.
     *
     * @param vertexOrder the sequence of vertices of {@code graph}.
     * @param map         maps every vertex in {@code graph} to its position in {@code vertexOrder}.
     * @return true if the {@code graph} is chordal and the vertices in {@code vertexOrder} are in
     * perfect elimination order.
     */
    private boolean isPerfectEliminationOrder(List<V> vertexOrder, Map<V, Integer> map) {
        for (V vertex : vertexOrder) {
            Set<V> predecessors = getPredecessors(map, vertex);
            if (predecessors.size() > 0) {
                V maxPredecessor = Collections.max(predecessors, Comparator.comparingInt(map::get));
                for (V predecessor : predecessors) {
                    if (!predecessor.equals(maxPredecessor) && !graph.containsEdge(predecessor, maxPredecessor)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns the predecessors of {@code vertex} in the order defined by {@code map}. More precisely,
     * returns those of {@code vertex}, whose mapped index in {@code map} is less then the index of {@code vertex}.
     *
     * @param map    defines the mapping of vertices in {@code graph} to their indices in order.
     * @param vertex the vertex whose predecessors in order are to be returned.
     * @return the predecessors of {@code vertex} in order defines by {@code map}.
     */
    private Set<V> getPredecessors(Map<V, Integer> map, V vertex) {
        Set<V> predecessors = new HashSet<>();
        Integer vertexPosition = map.get(vertex);
        Set<E> edges = graph.edgesOf(vertex);
        for (E edge : edges) {
            V oppositeVertex = Graphs.getOppositeVertex(graph, edge, vertex);
            Integer destPosition = map.get(oppositeVertex);
            if (destPosition < vertexPosition) {
                predecessors.add(oppositeVertex);
            }
        }
        return predecessors;
    }

    /**
     * Returns the type of iterator used in this {@code ChordalityInspector}
     *
     * @return the type of iterator used in this {@code ChordalityInspector}
     */
    public IterationOrder getIterationOrder() {
        return iterationOrder;
    }

    /**
     * Specifies internal iterator type.
     */
    public enum IterationOrder {
        MCS, LEX_BFS,
    }
}


