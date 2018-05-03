/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.LexBreadthFirstIterator;
import org.jgrapht.traverse.MaximumCardinalityIterator;

import java.util.*;

/**
 * Tests whether a graph is <a href="https://en.wikipedia.org/wiki/Chordal_graph">chordal</a>.
 * A chordal graph is a simple graph in which all <a href="http://mathworld.wolfram.com/GraphCycle.html">
 * cycles</a> of four or more vertices have a <a href="http://mathworld.wolfram.com/CycleChord.html">
 * chord</a>. A chord is an edge that is not part of the cycle but connects two vertices of the cycle.
 * A graph is chordal if and only if it has a
 * <a href="https://en.wikipedia.org/wiki/Chordal_graph#Perfect_elimination_and_efficient_recognition">
 * perfect elimination order</a>. A perfect elimination order in a graph is an ordering of the vertices
 * of the graph such that, for each vertex $v$, $v$ and the neighbors of $v$ that occur after $v$ in the
 * order form a clique. This implementation uses either {@link MaximumCardinalityIterator} or
 * {@link LexBreadthFirstIterator} to compute a perfect elimination order. The desired method
 * is specified during construction time.
 * <p>
 * Chordal graphs are a subset of the <a href="http://mathworld.wolfram.com/PerfectGraph.html">
 * perfect graphs</a>. They may be recognized in polynomial time, and several problems that are hard on
 * other classes of graphs such as minimum vertex coloring or determining maximum cardinality cliques and
 * independent set can be performed in polynomial time when the input is chordal.
 * <p>
 * All methods in this class run in $\mathcal{O}(|V| + |E|)$ time. Determining whether a graph is
 * chordal takes $\mathcal{O}(|V| + |E|)$ time, independent of the algorithm ({@link MaximumCardinalityIterator}
 * or {@link LexBreadthFirstIterator}) used to compute the perfect elimination order. Similarly, for chordal
 * graphs, this class can determine a perfect elmination order, a minimum vertex coloring, a maximum cardinality
 * clique, or an independent set in $\mathcal{O}(|V| + |E|)$ time. Finally, if the input graph is not chordal,
 * this class can detect a chordless cycle in $\mathcal{O}(|V| + |E|)$ time.
 * <p>
 * All the methods in this class are invoked in a lazy fashion, meaning that computations are only
 * started once the method gets invoked.
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 * @see LexBreadthFirstIterator
 * @see MaximumCardinalityIterator
 *
 * @author Timofey Chudakov
 * @since March 2018
 */
public class ChordalityInspector<V, E> implements VertexColoringAlgorithm<V> {
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
     * A minimum graph vertex coloring of the inspected {@code graph}.
     * The number of colors used in the coloring equals the chromatic number of the input graph.
     */
    private Coloring<V> coloring;

    /**
     * A maximum cardinality independent set of the inspected {@code graph}.
     */
    private Set<V> maximumIndependentSet;

    /**
     * A maximum cardinality clique of the inspected {@code graph}. The cardinality of this
     * clique equals to the number of colors used for the minimum graph vertex coloring.
     */
    private Set<V> maximumClique;

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
    public boolean isChordal() {
        if (order == null) {
            order = Collections.unmodifiableList(lazyComputeOrder());
            chordal = isPerfectEliminationOrder(order, true);
        }
        return chordal;
    }

    /**
     * Returns a <a href="https://en.wikipedia.org/wiki/Chordal_graph#Perfect_elimination_and_efficient_recognition">
     * perfect elimination order</a> if one exists. The existence of a perfect elimination order
     * certifies that the graph is chordal. This method returns null if the graph is not chordal.
     *
     * @return a perfect elimination order of a graph or null if graph is not chordal.
     */
    public List<V> getPerfectEliminationOrder() {
        isChordal();
        if (chordal) {
            return order;
        }
        return null;
    }

    /**
     * A graph which is not chordal, must contain a <a href="http://mathworld.wolfram.com/GraphHole.html">hole</a>
     * (chordless cycle on 4 or more vertices). The existence of a hole certifies that the graph
     * is not chordal. This method returns a chordless cycle if the graph is not chordal, or null if the
     * graph is chordal.
     *
     * @return a hole if the {@code graph} is not chordal, or null if the graph is chordal.
     */
    public GraphPath<V, E> getHole() {
        isChordal();

        return hole;
    }

    /**
     * Returns a <a href="http://mathworld.wolfram.com/MinimumVertexColoring.html">minimum vertex coloring</a>
     * of the inspected {@code graph}. If the graph isn't chordal, returns null. The number of colors used in
     * the coloring equals the chromatic number of the input graph.
     *
     * @return a coloring of the {@code graph} if it is chordal, null otherwise.
     */
    @Override
    public Coloring<V> getColoring() {
        return lazyComputeColoring();
    }

    /**
     * Returns a <a href = "http://mathworld.wolfram.com/MaximumIndependentVertexSet.html">
     * maximum cardinality independent set</a> of the inspected {@code graph}. If the graph
     * isn't chordal, returns null.
     *
     * @return a maximum independent set of the {@code graph} if it is chordal, false otherwise.
     */
    public Set<V> getMaximumIndependentSet() {
        return lazyComputeMaximumIndependentSet();
    }

    /**
     * Returns a <a href="http://mathworld.wolfram.com/MaximumClique.html">maximum cardinality clique</a>
     * of the inspected {@code graph}. If the graph isn't chordal, returns null.
     *
     * @return a maximum clique of the {@code graph} if it is chordal, null otherwise.
     */
    public Set<V> getMaximumClique() {
        return lazyComputeMaximumClique();
    }

    /**
     * Checks whether the vertices in the {@code vertexOrder} form a
     * <a href="https://en.wikipedia.org/wiki/Chordal_graph#Perfect_elimination_and_efficient_recognition">
     * perfect elimination order</a> with respect to the inspected graph. Returns false otherwise.
     *
     * @param vertexOrder the sequence of vertices of the {@code graph}.
     * @return true if the {@code graph} is chordal and the vertices in {@code vertexOrder} are in
     * perfect elimination order, otherwise false.
     */
    public boolean isPerfectEliminationOrder(List<V> vertexOrder) {
        return isPerfectEliminationOrder(vertexOrder, false);
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
     * Lazily computes the coloring of the graph. Returns null if the graph isn't chordal.
     *
     * @return coloring of the graph if it is chordal, null otherwise.
     */
    private Coloring<V> lazyComputeColoring() {
        if (coloring == null) {
            isChordal();

            if (chordal) {
                Map<V, Integer> vertexColoring = new HashMap<>(order.size());
                Map<V, Integer> vertexInOrder = getVertexInOrder(order);
                for (V vertex : order) {
                    Set<V> predecessors = getPredecessors(vertexInOrder, vertex);
                    Set<Integer> predecessorColors = new HashSet<>(predecessors.size());
                    predecessors.forEach(v -> predecessorColors.add(vertexColoring.get(v)));

                    // find the minimum unused color in the set of predecessors
                    int minUnusedColor = 0;
                    while (predecessorColors.contains(minUnusedColor)) {
                        ++minUnusedColor;
                    }
                    vertexColoring.put(vertex, minUnusedColor);
                }
                int maxColor = (int) vertexColoring.values().stream().distinct().count();
                return coloring = new ColoringImpl<>(vertexColoring, maxColor);
            }
            return null;
        }
        return coloring;

    }

    /**
     * Lazily computes a maximum independent set of the inspected {@code graph}.
     * If the graph isn't chordal, returns null.
     *
     * @return a maximum independent set of the {@code graph} if it is chordal, false otherwise.
     */
    private Set<V> lazyComputeMaximumIndependentSet() {
        if (maximumIndependentSet == null) {
            isChordal();

            if (chordal) {
                // iterate the order from the end to the beginning
                // chooses vertices, that don't have neighbors in the current independent set
                // adds all its neighbors to the restricted set

                Set<V> restricted = new HashSet<>();
                Set<V> maximumIndependentSet = new HashSet<>();
                ListIterator<V> reverse = order.listIterator(order.size());

                while (reverse.hasPrevious()) {
                    V previous = reverse.previous();
                    if (!restricted.contains(previous)) {
                        maximumIndependentSet.add(previous);
                        for (E edge : graph.edgesOf(previous)) {
                            V opposite = Graphs.getOppositeVertex(graph, edge, previous);
                            if (!previous.equals(opposite)) {
                                restricted.add(opposite);
                            }
                        }
                    }
                }
                return this.maximumIndependentSet = maximumIndependentSet;
            }
            return null;
        }
        return maximumIndependentSet;
    }

    /**
     * Lazily computes some maximum clique of the {@code graph}. Returns null if the graph isn't chordal.
     *
     * @return @return a maximum clique of the {@code graph} if it is chordal, null otherwise.
     */
    private Set<V> lazyComputeMaximumClique() {
        if (maximumClique == null) {
            isChordal();

            if (chordal) {
                // finds the vertex with the maximum cardinality predecessor list

                lazyComputeColoring();
                Map<V, Integer> vertexInOrder = getVertexInOrder(order);
                Map.Entry<V, Integer> maxEntry = coloring.getColors().entrySet().stream().max(
                        Comparator.comparing(Map.Entry::getValue)).orElse(null);
                if (maxEntry == null) {
                    return new HashSet<>();
                } else {
                    maximumClique = getPredecessors(vertexInOrder, maxEntry.getKey());
                    maximumClique.add(maxEntry.getKey());
                    return maximumClique;
                }
            }
            return null;
        }
        return maximumClique;
    }

    /**
     * Checks whether the vertices in the {@code vertexOrder} form a
     * <a href="https://en.wikipedia.org/wiki/Chordal_graph#Perfect_elimination_and_efficient_recognition">
     * perfect elimination order</a> with respect to the inspected graph. Returns false otherwise.
     * Computes a hole if the {@code computeHole} is true.
     *
     * @param vertexOrder           the sequence of vertices of {@code graph}.
     * @param computeHole tells whether to compute the hole if the graph isn't chordal.
     * @return true if the {@code graph} is chordal and the vertices in {@code vertexOrder} are in
     * perfect elimination order.
     */
    private boolean isPerfectEliminationOrder(List<V> vertexOrder, boolean computeHole) {
        Set<V> graphVertices = graph.vertexSet();
        if (graphVertices.size() == vertexOrder.size() && graphVertices.containsAll(vertexOrder)) {
            Map<V, Integer> vertexInOrder = getVertexInOrder(vertexOrder);
            for (V vertex : vertexOrder) {
                Set<V> predecessors = getPredecessors(vertexInOrder, vertex);
                if (predecessors.size() > 0) {
                    V maxPredecessor = Collections.max(predecessors, Comparator.comparingInt(vertexInOrder::get));
                    for (V predecessor : predecessors) {
                        if (!predecessor.equals(maxPredecessor) && !graph.containsEdge(predecessor, maxPredecessor)) {
                            if (computeHole) {
                                // predecessor, vertex and maxPredecessor are vertices, which lie consecutively on
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
     * Returns a map containing vertices from the {@code vertexOrder} mapped to their
     * indices in {@code vertexOrder}.
     *
     * @param vertexOrder a list with vertices.
     * @return a mapping of vertices from {@code vertexOrder} to their indices in {@code vertexOrder}.
     */
    private Map<V, Integer> getVertexInOrder(List<V> vertexOrder) {
        Map<V, Integer> vertexInOrder = new HashMap<>(vertexOrder.size());
        int i = 0;
        for (V vertex : vertexOrder) {
            vertexInOrder.put(vertex, i++);
        }
        return vertexInOrder;
    }

    /**
     * Computes a hole from the vertices of {@code subgraph} of the inspected {@code graph}
     * with vertices {@code a}, {@code b} and {@code c} on this cycle (there must be no edge between
     * {@code a} and {@code c}.
     *
     * @param a vertex that belongs to the cycle
     * @param b vertex that belongs to the cycle
     * @param c vertex that belongs to the cycle
     */
    private void findHole(V a, V b, V c) {
        // b is the first vertex in the order produced by the iterator whose predecessors don't form a clique.
        // a and c are a pair of vertices, which are predecessors of b and are not adjacent. These three vertices
        // belong to some chordless cycle in the G[S] where G[S] is a subgraph of G on vertices in
        // S = {u : index_in_order(u) <= index_in_order(v)}.
        // this method uses dfs to find any cycle in G, in which every vertex isn't adjacent to b, except for a and b.
        // then it finds a chordless subcycle in linear time and returns it.

        List<V> cycle = new ArrayList<>(Arrays.asList(a, b, c));
        Map<V, Boolean> visited = new HashMap<>(graph.vertexSet().size());
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
     * More precisely, finds some path from {@code middle} to {@code finish}.
     * The vertex {@code middle} isn't the endpoint of any chord in this cycle.
     *
     * @param cycle   already computed part of the cycle
     * @param visited the map that defines which vertex has been visited by this method
     * @param finish  the last vertex in the cycle.
     * @param middle  the vertex, which must be adjacent onl
     * @param current currently examined vertex.
     */
    private void dfsVisit(List<V> cycle, Map<V, Boolean> visited, V finish, V middle, V current) {
        visited.put(current, true);
        for (E edge : graph.edgesOf(current)) {
            V opposite = Graphs.getOppositeVertex(graph, edge, current);
            if ((!visited.get(opposite) && !graph.containsEdge(opposite, middle)) || opposite.equals(finish)) {
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
     * Minimizes the cycle represented by the list {@code cycle}. More precisely it
     * retains first 2 vertices and finds a chordless cycle starting from the third vertex.
     *
     * @param cycle vertices of the graph that represent the cycle.
     * @return a chordless cycle
     */
    private List<V> minimizeCycle(List<V> cycle) {
        Set<V> cycleVertices = new HashSet<>(cycle);
        cycleVertices.remove(cycle.get(1));
        List<V> minimized = new ArrayList<>();
        minimized.add(cycle.get(0));
        minimized.add(cycle.get(1));
        for (int i = 2; i < cycle.size() - 1; ) {
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
     * Returns the predecessors of {@code vertex} in the order defined by {@code map}. More precisely,
     * returns those of {@code vertex}, whose mapped index in {@code map} is less then the index of {@code vertex}.
     *
     * @param vertexInOrder defines the mapping of vertices in {@code graph} to their indices in order.
     * @param vertex        the vertex whose predecessors in order are to be returned.
     * @return the predecessors of {@code vertex} in order defines by {@code map}.
     */
    private Set<V> getPredecessors(Map<V, Integer> vertexInOrder, V vertex) {
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


