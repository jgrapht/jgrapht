/*
 * (C) Copyright 2017-2026, by Dimitrios Michail and Contributors.
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
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * A label-correcting algorithm for the multi-objective shortest paths problem.
 *
 * <p>
 * The algorithm is a multiple objective extension of the Bellman-Ford relaxation process. It
 * maintains a set of non-dominated labels for each vertex and iteratively propagates labels through
 * outgoing edges. A newly generated label is discarded if it is dominated by an existing label at
 * the same vertex. Existing labels dominated by the new label are removed.
 *
 * <p>
 * Optionally, an approximation tolerance $\varepsilon$ may be provided. In that case labels which
 * are sufficiently close according to the tolerance are also discarded in order to reduce the
 * number of stored labels, and the algorithm computes an approximation of the Pareto set. With a
 * tolerance equal to zero the complete Pareto set is computed.
 *
 * <p>
 * All objective values must be non-negative.
 *
 * <p>
 * The label-correcting strategy is described in: A. J. V. Skriver and K. A. Andersen. (2000). A
 * label correcting approach for solving bicriterion shortest-path problems. Computers &amp;
 * Operations Research. 27. 507-524. 10.1016/S0305-0548(99)00037-4.
 *
 * <p>
 * The approximation of the Pareto set is described in: A. Warburton. (1987). Approximation of
 * Pareto Optima in Multiple-Objective, Shortest-Path Problems. Operations Research. 35. 70-79.
 * 10.1287/opre.35.1.70.
 *
 * <p>
 * Note that the multi-objective shortest path problem is a well-known NP-hard problem.
 *
 * @author Mario Fuentes Jimenez
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class LabelCorrectingMultiObjectiveShortestPath<V, E> extends BaseMultiObjectiveShortestPathAlgorithm<V, E>
{
    // the edge weight function
    private final Function<E, double[]> edgeWeightFunction;
    // the number of objectives
    private final int objectives;
    // the approximation tolerance
    private final double epsilon;
    // final labels for each node
    private final Map<V, LinkedList<Label>> nodeLabels;
    // labels waiting to be expanded
    private final Queue<Label> queue;

    /**
     * Create a new shortest path algorithm which computes the complete Pareto set.
     *
     * @param graph the input graph
     * @param edgeWeightFunction the edge weight function
     */
    public LabelCorrectingMultiObjectiveShortestPath(
        Graph<V, E> graph, Function<E, double[]> edgeWeightFunction)
    {
        this(graph, edgeWeightFunction, 0d);
    }

    /**
     * Create a new shortest path algorithm which computes an epsilon-approximation of the Pareto
     * set. A tolerance equal to zero computes the complete Pareto set.
     *
     * @param graph the input graph
     * @param edgeWeightFunction the edge weight function
     * @param epsilon the approximation tolerance, must be non-negative
     */
    public LabelCorrectingMultiObjectiveShortestPath(
        Graph<V, E> graph, Function<E, double[]> edgeWeightFunction, double epsilon)
    {
        super(graph);
        this.edgeWeightFunction =
            Objects.requireNonNull(edgeWeightFunction, "Function cannot be null");
        if (Double.compare(epsilon, 0d) < 0) {
            throw new IllegalArgumentException("Epsilon must be non-negative");
        }
        this.objectives = validateEdgeWeightFunction(edgeWeightFunction);
        this.epsilon = epsilon;
        this.nodeLabels = new HashMap<>();
        this.queue = new ArrayDeque<>();
    }

    @Override
    public List<GraphPath<V, E>> getPaths(V source, V sink)
    {
        return this.getPaths(source).getPaths(sink);
    }

    @Override
    public MultiObjectiveSingleSourcePaths<V, E> getPaths(V source)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(
                BaseMultiObjectiveShortestPathAlgorithm.GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }

        if (graph.vertexSet().isEmpty() || graph.edgeSet().isEmpty()) {
            return new ListMultiObjectiveSingleSourcePathsImpl<>(
                graph, source, Collections.emptyMap());
        }

        if (nodeLabels.isEmpty()) {
            runAlgorithm(source);
        }

        Map<V, List<GraphPath<V, E>>> paths = buildPaths(source);
        return new ListMultiObjectiveSingleSourcePathsImpl<>(graph, source, paths);
    }

    /**
     * Execute the main algorithm.
     *
     * @param source the source vertex
     */
    private void runAlgorithm(V source)
    {
        Label sourceLabel = new Label(source, new double[objectives], null, null);

        for (V v : graph.vertexSet()) {
            nodeLabels.put(v, new LinkedList<>());
        }

        nodeLabels.get(source).add(sourceLabel);
        queue.add(sourceLabel);

        while (!queue.isEmpty()) {
            Label curLabel = queue.poll();
            V v = curLabel.node;

            for (E e : graph.outgoingEdgesOf(v)) {
                V u = Graphs.getOppositeVertex(graph, e, v);
                Label newLabel = new Label(
                    u, MathUtil.vectorSum(curLabel.value, edgeWeightFunction.apply(e)), curLabel,
                    e);

                boolean discard = false;
                LinkedList<Label> uLabels = nodeLabels.get(u);
                ListIterator<Label> it = uLabels.listIterator();

                while (it.hasNext()) {
                    Label oldLabel = it.next();
                    if (MathUtil.vectorDominates(oldLabel.value, newLabel.value)) {
                        discard = true;
                        break;
                    }
                    if (epsilon > 0d && close(oldLabel.value, newLabel.value, epsilon)) {
                        discard = true;
                        break;
                    }
                    if (MathUtil.vectorDominates(newLabel.value, oldLabel.value)) {
                        it.remove();
                    }
                }

                if (!discard) {
                    uLabels.add(newLabel);
                    queue.add(newLabel);
                }
            }
        }
    }

    /**
     * Build the actual paths from the final labels of each node.
     *
     * @param source the source vertex
     * @return the paths
     */
    private Map<V, List<GraphPath<V, E>>> buildPaths(V source)
    {
        Map<V, List<GraphPath<V, E>>> paths = new HashMap<>();
        for (V sink : graph.vertexSet()) {
            if (sink.equals(source)) {
                paths.put(sink, List.of(createEmptyPath(source, sink)));
            } else {
                paths.put(sink, nodeLabels.get(sink).stream().map(l -> {
                    double weight = 0d;
                    LinkedList<E> edgeList = new LinkedList<>();
                    Label cur = l;
                    while (cur != null && cur.fromPrevious != null) {
                        weight += graph.getEdgeWeight(cur.fromPrevious);
                        edgeList.push(cur.fromPrevious);
                        cur = cur.previous;
                    }
                    return new GraphWalk<>(graph, source, sink, edgeList, weight);
                }).collect(Collectors.toList()));
            }
        }
        return paths;
    }

    /**
     * Return whether two vectors are close according to the epsilon tolerance.
     *
     * @param a the first vector
     * @param b the second vector
     * @param epsilon the approximation tolerance
     * @return true if the vectors are close
     */
    private static boolean close(double[] a, double[] b, double epsilon)
    {
        int d = a.length;
        for (int i = 0; i < d; i++) {
            double tolerance = Math.abs(a[i]) * epsilon;
            if (Math.abs(a[i] - b[i]) > tolerance) {
                return false;
            }
        }
        return true;
    }

    /**
     * A node label.
     */
    private class Label
    {
        public V node;
        public double[] value;
        public Label previous;
        public E fromPrevious;

        public Label(V node, double[] value, Label previous, E fromPrevious)
        {
            this.node = node;
            this.value = value;
            this.previous = previous;
            this.fromPrevious = fromPrevious;
        }

        @Override
        public String toString()
        {
            return "Label [node=" + node + ", value=" + Arrays.toString(value) + ", fromPrevious="
                + fromPrevious + "]";
        }
    }
}
