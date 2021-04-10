/*
 * (C) Copyright 2018-2018, by Oliver Feith, Dennis Fischer and Contributors.
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
package org.jgrapht.alg.interval;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.interval.Interval;
import org.jgrapht.util.CollectionUtil;

import java.util.*;

import static org.jgrapht.alg.interval.LexBreadthFirstSearch.*;

/**
 * A recognizer for interval graphs.
 * <p>
 * An interval graph is a intersection graph of a set of intervals on the line, i.e. they contain a
 * vertex for each interval and two vertices are connected if the corresponding intervals have a
 * nonempty intersection.
 * <p>
 * The recognizer uses the algorithm described in <a href=
 * "https://webdocs.cs.ualberta.ca/~stewart/Pubs/IntervalSIAM.pdf">https://webdocs.cs.ualberta.ca/~stewart/Pubs/IntervalSIAM.pdf</a>
 * (<i>The LBFS Structure and Recognition of Interval Graphs. SIAM J. Discrete Math.. 23. 1905-1953.
 * 10.1137/S0895480100373455.</i>) by Derek Corneil, Stephan Olariu and Lorna Stewart based on
 * multiple lexicographical breadth-first search (LBFS) sweeps. The algorithm runs in $O(|V| + |E|)$.
 * <p>
 * For this recognizer to work correctly the graph must not be modified during iteration.
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 * @author Oliver Feith
 * @author Dennis Fischer
 * @since April 2018
 */
public final class IntervalGraphRecognizer<V, E> {

    private final Graph<V, E> graph;
    /**
     * Stores whether or not the graph is an interval graph.
     */
    private boolean isIntervalGraph;
    /**
     * Stores whether or not the algorithm was executed.
     */
    private boolean isComputationComplete;
    /**
     * Stores the computed interval graph representation (or <tt>null</tt> if no such representation
     * exists) of the graph.
     */
    private ArrayList<Interval<Integer>> intervalsSortedByStartingPoint;
    private Map<Interval<Integer>, V> intervalToVertexMap;
    private Map<V, Interval<Integer>> vertexToIntervalMap;

    /**
     * Creates a new interval graph recognizer instance for the given graph.
     *
     * @param graph the graph to be tested.
     */
    public IntervalGraphRecognizer(Graph<V, E> graph) {
        this.graph = graph;
        this.isComputationComplete = false;
    }

    /**
     * check if the graph is an interval graph
     */
    private void computeIsIntervalGraph() {

        // An empty graph is an interval graph.
        if (graph.vertexSet().isEmpty()) {
            // Create (empty) interval representation
            this.intervalsSortedByStartingPoint = new ArrayList<>();
            this.intervalToVertexMap = new HashMap<>();
            this.vertexToIntervalMap = new HashMap<>();
            this.isIntervalGraph = true;

            return;
        }

        // Step 1 - LBFS from an arbitrary vertex
        // Input - random vertex r
        // Output - the result of current sweep alpha and vertex a, which is the last vertex visited by current sweep
        HashMap<V, Integer> sweepAlpha = lexBreadthFirstSearch(graph);

        // Step 2 - LBFS+ from the last vertex of the previous sweep
        // Input - the result of previous sweep alpha, vertex a
        // Output - the result of current sweep beta and vertex b, which is the last vertex visited by current sweep
        HashMap<V, Integer> sweepBeta = lexBreadthFirstSearchPlus(graph, sweepAlpha);

        // Step 3 - LBFS+ from the last vertex of the previous sweep
        // Input - the result of previous sweep beta, vertex b
        // Output - the result of current sweep gamma and vertex c, which is the last vertex visited by current sweep
        HashMap<V, Integer> sweepGamma = lexBreadthFirstSearchPlus(graph, sweepBeta);

        // Step 4 - LBFS+ from the last vertex of the previous sweep
        // Input - the result of previous sweep gamma, vertex c
        // Output - the result of current sweep delta and vertex d, which is the last vertex visited by current sweep
        // sweep
        HashMap<V, Integer> sweepDelta = lexBreadthFirstSearchPlus(graph, sweepGamma);

        // Step 5 - LBFS+ from the last vertex of the previous sweep
        // Input - the result of previous sweep delta, vertex d
        // Output - the result of current sweep epsilon and vertex e, which is the last vertex visited by current sweep
        // sweep
        HashMap<V, Integer> sweepEpsilon = lexBreadthFirstSearchPlus(graph, sweepDelta);

        // Step 6 - LBFS* with the resulting sweeps
        // Input - the result of sweep gamma and sweep epsilon
        // Output - the result of current sweep zeta
        HashMap<V, Integer> sweepZeta = lexBreadthFirstSearchStar(graph, sweepDelta, sweepEpsilon);

        // if sweepZeta is umbrella-free, then the graph is interval.
        // otherwise, the graph is not interval

        if (isIOrdering(sweepZeta, graph)) {

            HashMap<V, Integer> neighborIndex = new HashMap<>();
            for (V vertex : graph.vertexSet()) {
                int maxNeighbor = 0;

                List<V> neighbors = Graphs.neighborListOf(graph, vertex);
                neighbors.add(vertex);

                for (V neighbor : neighbors) {
                    maxNeighbor = Math.max(maxNeighbor, sweepZeta.get(neighbor));
                }

                neighborIndex.put(vertex, maxNeighbor);
            }

            ArrayList<Interval<Integer>> intervals = new ArrayList<>(graph.vertexSet().size());
            for (int i = 0; i < graph.vertexSet().size(); ++i) {
                intervals.add(null);
            }
            this.intervalsSortedByStartingPoint = new ArrayList<>(graph.vertexSet().size());

            // Initialize the vertex map. Because we know the number of vertices we can make sure the hashmap does not
            // need to rehash by setting the capacity to the number of vertices divided by the default load factor of
            // 0.75.
            this.intervalToVertexMap = CollectionUtil.newHashMapWithExpectedSize((int) Math.ceil(graph.vertexSet().size() / 0.75));
            this.vertexToIntervalMap = CollectionUtil.newHashMapWithExpectedSize((int) Math.ceil(graph.vertexSet().size() / 0.75));

            // Compute intervals and store them associated by their starting point ...
            for (V vertex : graph.vertexSet()) {
                Interval<Integer> vertexInterval = new Interval<>(sweepZeta.get(vertex), neighborIndex.get(vertex));

                intervals.set(sweepZeta.get(vertex), vertexInterval);

                this.intervalToVertexMap.put(vertexInterval, vertex);
                this.vertexToIntervalMap.put(vertex, vertexInterval);
            }

            // ... and produce a list sorted by the starting points for an efficient construction of
            // the graph
            this.intervalsSortedByStartingPoint.addAll(intervals);

            this.isIntervalGraph = true;
        } else {
            this.isIntervalGraph = false;
        }
    }

    /**
     * Calculates interval ordering if the given sweep encodes an interval ordering (according to the Graph graph) in linear time.
     *
     * @param sweep the order we want to check if its  interval order
     * @param graph the graph we want to check if sweep encodes interval order
     * @return true, if sweep is an  interval order according to graph
     */
    private boolean isIOrdering(HashMap<V, Integer> sweep, Graph<V, E> graph) {
        // Compute inverse sweep map to quickly find vertices at given indices
        ArrayList<V> inverseSweep = new ArrayList<>(graph.vertexSet().size());
        for (int i = 0; i < graph.vertexSet().size(); ++i) {
            inverseSweep.add(null);
        }

        for (V vertex : graph.vertexSet()) {
            int index = sweep.get(vertex);
            inverseSweep.set(index, vertex);
        }
        // Compute maximal neighbors w.r.t. sweep ordering for every vertex
        HashMap<V, V> maxNeighbors = new HashMap<>(graph.vertexSet().size());

        for (V vertex : graph.vertexSet()) {
            List<V> neighbors = Graphs.neighborListOf(graph, vertex);
            V maxNeighbor = vertex;

            for (V neighbor : neighbors) {
                if (sweep.get(neighbor) > sweep.get(maxNeighbor)) {
                    maxNeighbor = neighbor;
                }
            }

            maxNeighbors.put(vertex, maxNeighbor);
        }

        // Check if every vertex is connected to all vertices between itself and its maximal neighbor
        for (V vertex : graph.vertexSet()) {
            int index = sweep.get(vertex);
            int maxIndex = sweep.get(maxNeighbors.get(vertex));

            for (int i = index; i < maxIndex; i++) {
                if (vertex != inverseSweep.get(i) && !graph.containsEdge(vertex, inverseSweep.get(i))) {
                    // Found missing edge
                    return false;
                }
            }
        }

        // No missing edge found
        return true;
    }

    /**
     * Makes sure the algorithm has been run and all fields are populated with their proper value.
     */
    private void lazyComputeIsIntervalGraph() {
        if (!this.isComputationComplete) {
            computeIsIntervalGraph();
            isComputationComplete = true;
        }
    }

    /**
     * Returns whether or not the graph is an interval graph.
     *
     * @return <tt>true</tt> if the graph is an interval graph, otherwise false.
     */
    public boolean isIntervalGraph() {
        lazyComputeIsIntervalGraph();
        return isIntervalGraph;
    }

    /**
     * Returns the list of all intervals sorted by starting point, or null, if the graph was not an
     * interval graph.
     *
     * @return The list of all intervals sorted by starting point, or null, if the graph was not an
     * interval graph.
     */
    public List<Interval<Integer>> getIntervalsSortedByStartingPoint() {
        lazyComputeIsIntervalGraph();
        return Collections.unmodifiableList(this.intervalsSortedByStartingPoint);
    }

    /**
     * Returns a mapping of the constructed intervals to the vertices of the original graph, or
     * null, if the graph was not an interval graph.
     *
     * @return A mapping of the constructed intervals to the vertices of the original graph, or
     * null, if the graph was not an interval graph.
     */
    public Map<Interval<Integer>, V> getIntervalToVertexMap() {
        lazyComputeIsIntervalGraph();
        return this.intervalToVertexMap;
    }

    /**
     * Returns a mapping of the vertices of the original graph to the constructed intervals, or
     * null, if the graph was not an interval graph.
     *
     * @return A mapping of the vertices of the original graph to the constructed intervals, or
     * null, if the graph was not an interval graph.
     */
    public Map<V, Interval<Integer>> getVertexToIntervalMap() {
        lazyComputeIsIntervalGraph();
        return this.vertexToIntervalMap;
    }
}
