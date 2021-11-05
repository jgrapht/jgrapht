/*
 * (C) Copyright 2018-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * Tests whether a graph is <a href="https://en.wikipedia.org/wiki/Chordal_graph">chordal</a>. A
 * chordal graph is a simple graph in which all
 * <a href="http://mathworld.wolfram.com/GraphCycle.html"> cycles</a> of four or more vertices have
 * a <a href="http://mathworld.wolfram.com/CycleChord.html"> chord</a>. A chord is an edge that is
 * not part of the cycle but connects two vertices of the cycle. A graph is chordal if and only if
 * it has a <a href=
 * "https://en.wikipedia.org/wiki/Chordal_graph#Perfect_elimination_and_efficient_recognition">
 * perfect elimination order</a>. A perfect elimination order in a graph is an ordering of the
 * vertices of the graph such that, for each vertex $v$, $v$ and the neighbors of $v$ that occur
 * after $v$ in the order form a clique. This implementation uses either
 * {@link MaximumCardinalityIterator} or {@link LexBreadthFirstIterator} to compute a perfect
 * elimination order. The desired method is specified during construction time.
 * <p>
 * Chordal graphs are a subset of the <a href="http://mathworld.wolfram.com/PerfectGraph.html">
 * perfect graphs</a>. They may be recognized in polynomial time, and several problems that are hard
 * on other classes of graphs such as minimum vertex coloring or determining maximum cardinality
 * cliques and independent set can be performed in polynomial time when the input is chordal.
 * <p>
 * All methods in this class run in $\mathcal{O}(|V| + |E|)$ time. Determining whether a graph is
 * chordal, as well as computing a perfect elimination order takes $\mathcal{O}(|V| + |E|)$ time,
 * independent of the algorithm ({@link MaximumCardinalityIterator} or
 * {@link LexBreadthFirstIterator}) used to compute the perfect elimination order.
 * <p>
 * All the methods in this class are invoked in a lazy fashion, meaning that computations are only
 * started once the method gets invoked.
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 *
 * @author Timofey Chudakov
 */
public class ChordalityInspector<V, E>
{
    /**
     * Stores the type of iterator used by this {@code ChordalityInspector}.
     */
    private final IterationOrder iterationOrder;
    /**
     * Iterator used for producing perfect elimination order.
     */
    private final GraphIterator<V, E> orderIterator;
    /**
     * The inspected graph.
     */
    private final Graph<V, E> graph;
    /**
     * Contains true if the graph is chordal, otherwise false.
     */
    private boolean chordal = false;
    /**
     * Order produced by {@code orderIterator}.
     */
    private List<V> order;

    /**
     * A hole contained in the inspected {@code graph}.
     */
    private GraphPath<V, E> hole;

    /**
     * Creates a chordality inspector for {@code graph}, which uses
     * {@link MaximumCardinalityIterator} as a default iterator.
     *
     * @param graph the graph for which a chordality inspector to be created.
     */
    public ChordalityInspector(Graph<V, E> graph)
    {
        this(graph, IterationOrder.MCS);
    }

    /**
     * Creates a chordality inspector for {@code graph}, which uses an iterator defined by the
     * second parameter as an internal iterator.
     *
     * @param graph the graph for which a chordality inspector is to be created.
     * @param iterationOrder the constant, which defines iterator to be used by this
     *        {@code ChordalityInspector}.
     */
    public ChordalityInspector(Graph<V, E> graph, IterationOrder iterationOrder)
    {
        Objects.requireNonNull(graph);
        if (graph.getType().isDirected()) {
            this.graph = new AsUndirectedGraph<>(graph);
        } else {
            this.graph = graph;
        }
        this.iterationOrder = iterationOrder;
        this.hole = null;
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
    public boolean isChordal()
    {
        if (order == null) {
            order = Collections.unmodifiableList(lazyComputeOrder());
            chordal = isPerfectEliminationOrder(order, true);
        }
        return chordal;
    }

    /**
     * Returns a <a href=
     * "https://en.wikipedia.org/wiki/Chordal_graph#Perfect_elimination_and_efficient_recognition">
     * perfect elimination order</a> if one exists. The existence of a perfect elimination order
     * certifies that the graph is chordal. This method returns null if the graph is not chordal.
     *
     * @return a perfect elimination order of a graph or null if graph is not chordal.
     */
    public List<V> getPerfectEliminationOrder()
    {
        isChordal();
        if (chordal) {
            return order;
        }
        return null;
    }

    /**
     * A graph which is not chordal, must contain a
     * <a href="http://mathworld.wolfram.com/GraphHole.html">hole</a> (chordless cycle on 4 or more
     * vertices). The existence of a hole certifies that the graph is not chordal. This method
     * returns a chordless cycle if the graph is not chordal, or null if the graph is chordal.
     *
     * @return a hole if the {@code graph} is not chordal, or null if the graph is chordal.
     */
    public GraphPath<V, E> getHole()
    {
        isChordal();

        return hole;
    }

    /**
     * Checks whether the vertices in the {@code vertexOrder} form a <a href=
     * "https://en.wikipedia.org/wiki/Chordal_graph#Perfect_elimination_and_efficient_recognition">
     * perfect elimination order</a> with respect to the inspected graph. Returns false otherwise.
     *
     * @param vertexOrder the sequence of vertices of the {@code graph}.
     * @return true if the {@code graph} is chordal and the vertices in {@code vertexOrder} are in
     *         perfect elimination order, otherwise false.
     */
    public boolean isPerfectEliminationOrder(List<V> vertexOrder)
    {
        return isPerfectEliminationOrder(vertexOrder, false);
    }

    /**
     * Computes vertex order via {@code orderIterator}.
     *
     * @return computed order.
     */
    private List<V> lazyComputeOrder()
    {
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
     * Checks whether the vertices in the {@code vertexOrder} form a <a href=
     * "https://en.wikipedia.org/wiki/Chordal_graph#Perfect_elimination_and_efficient_recognition">
     * perfect elimination order</a> with respect to the inspected graph. Returns false otherwise.
     * Computes a hole if the {@code computeHole} is true.
     *
     * @param vertexOrder the sequence of vertices of {@code graph}.
     * @param computeHole tells whether to compute the hole if the graph isn't chordal.
     * @return true if the {@code graph} is chordal and the vertices in {@code vertexOrder} are in
     *         perfect elimination order.
     */
    private boolean isPerfectEliminationOrder(List<V> vertexOrder, boolean computeHole)
    {
        Set<V> graphVertices = graph.vertexSet();
        if (graphVertices.size() == vertexOrder.size() && graphVertices.containsAll(vertexOrder)) {
            Map<V, Integer> vertexInOrder = getVertexInOrder(vertexOrder);
            for (V vertex : vertexOrder) {
                Set<V> predecessors = getPredecessors(vertexInOrder, vertex);
                if (predecessors.size() > 0) {
                    V maxPredecessor =
                        Collections.max(predecessors, Comparator.comparingInt(vertexInOrder::get));
                    for (V predecessor : predecessors) {
                        if (!predecessor.equals(maxPredecessor)
                            && !graph.containsEdge(predecessor, maxPredecessor))
                        {
                            if (computeHole) {
                                // predecessor, vertex and maxPredecessor are vertices, which lie
                                // consecutively on
                                // some chordless cycle in the graph
                                findHole(predecessor, vertex, maxPredecessor);
                            }
                            return false;
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a map containing vertices from the {@code vertexOrder} mapped to their indices in
     * {@code vertexOrder}.
     *
     * @param vertexOrder a list with vertices.
     * @return a mapping of vertices from {@code vertexOrder} to their indices in
     *         {@code vertexOrder}.
     */
    private Map<V, Integer> getVertexInOrder(List<V> vertexOrder)
    {
        Map<V, Integer> vertexInOrder =
            CollectionUtil.newHashMapWithExpectedSize(vertexOrder.size());
        int i = 0;
        for (V vertex : vertexOrder) {
            vertexInOrder.put(vertex, i++);
        }
        return vertexInOrder;
    }

    /**
     * Computes a hole from the vertices of {@code subgraph} of the inspected {@code graph} with
     * vertices {@code a}, {@code b} and {@code c} on this cycle (there must be no edge between
     * {@code a} and {@code c}.
     *
     * @param a vertex that belongs to the cycle
     * @param b vertex that belongs to the cycle
     * @param c vertex that belongs to the cycle
     */
    private void findHole(V a, V b, V c)
    {
        // b is the first vertex in the order produced by the iterator whose predecessors don't form
        // a clique.
        // a and c are a pair of vertices, which are predecessors of b and are not adjacent. These
        // three vertices
        // belong to some chordless cycle in the G[S] where G[S] is a subgraph of G on vertices in
        // S = {u : index_in_order(u) <= index_in_order(v)}.
        // this method uses dfs to find any cycle in G, in which every vertex isn't adjacent to b,
        // except for a and b.
        // then it finds a chordless subcycle in linear time and returns it.

        List<V> cycle = new ArrayList<>(Arrays.asList(a, b, c));
        Map<V, Boolean> visited =
            CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());
        for (V vertex : graph.vertexSet()) {
            visited.put(vertex, false);
        }
        visited.put(a, true);
        visited.put(b, true);
        dfsVisit(cycle, visited, a, b, c);
        cycle = minimizeCycle(cycle);
        hole = new GraphWalk<>(graph, cycle, 0);
    }

    /**
     * Computes some cycle in the graph on the vertices from the domain of the map {@code visited}.
     * More precisely, finds some path from {@code middle} to {@code finish}. The vertex
     * {@code middle} isn't the endpoint of any chord in this cycle.
     *
     * @param cycle already computed part of the cycle
     * @param visited the map that defines which vertex has been visited by this method
     * @param finish the last vertex in the cycle.
     * @param middle the vertex, which must be adjacent onl
     * @param current currently examined vertex.
     */
    private void dfsVisit(List<V> cycle, Map<V, Boolean> visited, V finish, V middle, V current)
    {
        visited.put(current, true);
        for (E edge : graph.edgesOf(current)) {
            V opposite = Graphs.getOppositeVertex(graph, edge, current);
            if ((!visited.get(opposite) && !graph.containsEdge(opposite, middle))
                || opposite.equals(finish))
            {
                cycle.add(opposite);
                if (opposite.equals(finish)) {
                    return;
                }
                dfsVisit(cycle, visited, finish, middle, opposite);
                if (cycle.get(cycle.size() - 1).equals(finish)) {
                    return;
                } else {
                    cycle.remove(cycle.size() - 1);
                }
            }
        }
    }

    /**
     * Minimizes the cycle represented by the list {@code cycle}. More precisely it retains first 2
     * vertices and finds a chordless cycle starting from the third vertex.
     *
     * @param cycle vertices of the graph that represent the cycle.
     * @return a chordless cycle
     */
    private List<V> minimizeCycle(List<V> cycle)
    {
        Set<V> cycleVertices = new HashSet<>(cycle);
        cycleVertices.remove(cycle.get(1));
        List<V> minimized = new ArrayList<>();
        minimized.add(cycle.get(0));
        minimized.add(cycle.get(1));
        for (int i = 2; i < cycle.size() - 1;) {
            V vertex = cycle.get(i);
            minimized.add(vertex);
            cycleVertices.remove(vertex);
            Set<V> forward = new HashSet<>();

            // compute vertices with the higher index in the cycle
            for (E edge : graph.edgesOf(vertex)) {
                V opposite = Graphs.getOppositeVertex(graph, edge, vertex);
                if (cycleVertices.contains(opposite)) {
                    forward.add(opposite);
                }
            }
            // jump to the vertex with the highest index with respect to the current vertex
            for (V forwardVertex : forward) {
                if (cycleVertices.contains(forwardVertex)) {
                    do {
                        cycleVertices.remove(cycle.get(i));
                        i++;
                    } while (i < cycle.size() && !cycle.get(i).equals(forwardVertex));
                }
            }
        }
        minimized.add(cycle.get(cycle.size() - 1));
        return minimized;
    }

    /**
     * Returns the predecessors of {@code vertex} in the order defined by {@code map}. More
     * precisely, returns those of {@code vertex}, whose mapped index in {@code map} is less then
     * the index of {@code vertex}.
     *
     * @param vertexInOrder defines the mapping of vertices in {@code graph} to their indices in
     *        order.
     * @param vertex the vertex whose predecessors in order are to be returned.
     * @return the predecessors of {@code vertex} in order defines by {@code map}.
     */
    private Set<V> getPredecessors(Map<V, Integer> vertexInOrder, V vertex)
    {
        Set<V> predecessors = new HashSet<>();
        Integer vertexPosition = vertexInOrder.get(vertex);
        Set<E> edges = graph.edgesOf(vertex);
        for (E edge : edges) {
            V oppositeVertex = Graphs.getOppositeVertex(graph, edge, vertex);
            Integer destPosition = vertexInOrder.get(oppositeVertex);
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
    public IterationOrder getIterationOrder()
    {
        return iterationOrder;
    }

    /**
     * Specifies internal iterator type.
     */
    public enum IterationOrder
    {
        MCS,
        LEX_BFS,
    }
}
