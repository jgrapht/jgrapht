/*
 * (C) Copyright 2015-2018, by Alexey Kudinkin and Contributors.
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
package org.jgrapht.alg.flow;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.alg.util.extension.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * <p>
 * <a href="https://en.wikipedia.org/wiki/Push%E2%80%93relabel_maximum_flow_algorithm"> Push-relabel
 * maximum flow</a> algorithm designed by Andrew V. Goldberg and Robert Tarjan. Current
 * implementation complexity upper-bound is $O(V^3)$. For more details see: <i>"A new approach to
 * the maximum flow problem"</i> by Andrew V. Goldberg and Robert Tarjan <i>STOC '86: Proceedings of
 * the eighteenth annual ACM symposium on Theory of computing</i>
 * </p>
 *
 * <p>
 * This implementation is based on <i>On Implementing the Push—Relabel Method for the Maximum Flow
 * Problem</i> by B. V. Cherkassky and A.V. Goldberg (Cherkassky, B. &amp; Goldberg, A. Algorithmica
 * (1997) 19: 390. https://doi.org/10.1007/PL00009180) and <i>Introduction to Algorithms</i> (3rd
 * Edition).
 * </p>
 *
 * <p>
 * This class can also computes minimum $s-t$ cuts. Effectively, to compute a minimum $s-t$ cut, the
 * implementation first computes a minimum $s-t$ flow, after which a BFS is run on the residual
 * graph.
 * </p>
 *
 * Note: even though the algorithm accepts any kind of graph, currently only Simple directed and
 * undirected graphs are supported (and tested!).
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexandru Valeanu
 * @author Alexey Kudinkin
 *
 */
public class PushRelabelMFImpl<V, E>
    extends
    MaximumFlowAlgorithmBase<V, E>
{
    // Diagnostic
    private static final boolean DIAGNOSTIC_ENABLED = false;

    public static boolean USE_GLOBAL_RELABELING_HEURISTIC = true;
    public static boolean USE_GAP_RELABELING_HEURISTIC = true;

    private final ExtensionFactory<VertexExtension> vertexExtensionsFactory;
    private final ExtensionFactory<AnnotatedFlowEdge> edgeExtensionsFactory;

    // countHeight[h] = number of vertices with height h
    private int[] countHeight;

    // queue of active vertices
    private Queue<VertexExtension> activeVertices;

    private PushRelabelDiagnostic diagnostic;

    // number of vertices
    private final int N;

    private final VertexExtension[] vertexExtension;

    // number of relabels already performed
    private int relabelCounter;

    private static ToleranceDoubleComparator comparator = new ToleranceDoubleComparator();

    /**
     * Construct a new push-relabel algorithm.
     *
     * @param network the network
     */
    public PushRelabelMFImpl(Graph<V, E> network)
    {
        this(network, DEFAULT_EPSILON);
    }

    /**
     * Construct a new push-relabel algorithm.
     *
     * @param network the network
     * @param epsilon tolerance used when comparing floating-point values
     */
    @SuppressWarnings("unchecked")
    public PushRelabelMFImpl(Graph<V, E> network, double epsilon)
    {
        super(network, epsilon);

        this.vertexExtensionsFactory = VertexExtension::new;

        this.edgeExtensionsFactory = AnnotatedFlowEdge::new;

        if (DIAGNOSTIC_ENABLED) {
            this.diagnostic = new PushRelabelDiagnostic();
        }

        this.N = network.vertexSet().size();
        this.vertexExtension = (VertexExtension[]) Array.newInstance(VertexExtension.class, N);
    }

    private void enqueue(VertexExtension vx)
    {
        if (!vx.active && vx.hasExcess()) {
            vx.active = true;
            activeVertices.add(vx);
        }
    }

    /**
     * Prepares all data structures to start a new invocation of the Maximum Flow or Minimum Cut
     * algorithms
     *
     * @param source source
     * @param sink sink
     */
    void init(V source, V sink)
    {
        super.init(source, sink, vertexExtensionsFactory, edgeExtensionsFactory);

        this.countHeight = new int[2 * N + 1];

        int id = 0;
        for (V v : network.vertexSet()) {
            VertexExtension vx = getVertexExtension(v);
            vx.id = id;
            vertexExtension[id] = vx;
            id++;
        }
    }

    /**
     * Initialization
     *
     * @param source the source
     * @param sink the sink
     * @param active resulting queue with all active vertices
     */
    public void initialize(
        VertexExtension source, VertexExtension sink, Queue<VertexExtension> active)
    {
        this.activeVertices = active;

        for (int i = 0; i < N; i++) {
            vertexExtension[i].excess = 0;
            vertexExtension[i].height = 0;
            vertexExtension[i].active = false;
            vertexExtension[i].currentArc = 0;
        }

        source.height = N;
        source.active = true;
        sink.active = true;

        countHeight[N] = 1;
        countHeight[0] = N - 1;

        for (AnnotatedFlowEdge ex : source.getOutgoing()) {
            source.excess += ex.capacity;
            push(ex);
        }

        if (USE_GLOBAL_RELABELING_HEURISTIC) {
            recomputeHeightsHeuristic();
            this.relabelCounter = 0;
        }
    }

    @Override
    public MaximumFlow<E> getMaximumFlow(V source, V sink)
    {
        this.calculateMaximumFlow(source, sink);
        maxFlow = composeFlow();
        return new MaximumFlowImpl<>(maxFlowValue, maxFlow);
    }

    /**
     * Sets current source to <tt>source</tt>, current sink to <tt>sink</tt>, then calculates
     * maximum flow from <tt>source</tt> to <tt>sink</tt>. Note, that <tt>source</tt> and
     * <tt>sink</tt> must be vertices of the <tt>
     * network</tt> passed to the constructor, and they must be different.
     *
     * @param source source vertex
     * @param sink sink vertex
     * @return the value of the maximum flow
     */
    public double calculateMaximumFlow(V source, V sink)
    {
        /*
         * Note: this implementation uses the FIFO selection rule (check wiki for more details)
         */

        init(source, sink);

        this.activeVertices = new ArrayDeque<>(N);
        initialize(getVertexExtension(source), getVertexExtension(sink), this.activeVertices);

        //
        while (!activeVertices.isEmpty()) {
            VertexExtension vx = activeVertices.poll();
            vx.active = false;
            discharge(vx);
        }

        // Calculate the max flow that reaches the sink. There may be more efficient ways to do
        // this.
        for (E e : network.edgesOf(sink)) {
            AnnotatedFlowEdge edge = edgeExtensionManager.getExtension(e);
            maxFlowValue += (directedGraph ? edge.flow : edge.flow + edge.getInverse().flow);
        }

        if (DIAGNOSTIC_ENABLED) {
            diagnostic.dump();
        }

        return maxFlowValue;
    }

    /**
     * Push flow through an edge.
     *
     * @param ex the edge
     * @param f the amount of flow to push through
     */
    protected void pushFlowThrough(AnnotatedFlowEdge ex, double f)
    {
        ex.getSource().excess -= f;
        ex.getTarget().excess += f;

        assert ((ex.getSource().excess >= 0.0) && (ex.getTarget().excess >= 0));

        super.pushFlowThrough(ex, f);
    }

    /*
     * The basic operation PUSH(u, v) is applied if u in an overflowing vertex (i.e. has excess) and
     * u.height = v.height + 1.
     * 
     * The operation can be either saturating (if ux.excess >= ex.capacity - ex.flow) or
     * nonsaturating (otherwise).
     */
    private void push(AnnotatedFlowEdge ex)
    {
        VertexExtension ux = ex.getSource();
        VertexExtension vx = ex.getTarget();
        double delta = Math.min(ux.excess, ex.capacity - ex.flow);

        // if v is not downhill from u or there is nothing to push (i.e. delta == 0) stop
        if (ux.height <= vx.height || comparator.compare(delta, 0.0) <= 0)
            return;

        if (DIAGNOSTIC_ENABLED) {
            diagnostic.incrementDischarges(ex);
        }

        pushFlowThrough(ex, delta);

        // check if we can 'activate' v
        enqueue(vx);
    }

    private void gapHeuristic(int l)
    {
        for (int i = 0; i < N; i++) {
            if (l < vertexExtension[i].height && vertexExtension[i].height < N) {
                countHeight[vertexExtension[i].height]--;
                vertexExtension[i].height = Math.max(vertexExtension[i].height, N + 1);
                countHeight[vertexExtension[i].height]++;
            }
        }
    }

    /*
     * The basic operation RELABEL(u) is applied if u is overflowing (i.e. has excess) and if
     * u.height <= v.height + 1.
     * 
     * We can relabel an overflowing vertex $u$ if for every vertex v for which there is residual
     * capacity from u to v, flow cannot be pushed from u to v because v is not downhill from u.
     */
    private void relabel(VertexExtension ux)
    {
        int oldHeight = ux.height;

        // Increase the height of u; u.h = 1 + min(v.h : (u, v) in Ef)

        countHeight[ux.height]--;
        ux.height = 2 * N;

        for (AnnotatedFlowEdge ex : ux.getOutgoing()) {
            if (ex.hasCapacity()) {
                ux.height = Math.min(ux.height, ex.<VertexExtension> getTarget().height + 1);
            }
        }

        countHeight[ux.height]++;

        if (USE_GAP_RELABELING_HEURISTIC) {
            /*
             * The gap heuristic detects gaps in the height function. If there is a height 0 < h <
             * |V| for which there is no node u such that u.height = h, then any node v with h <
             * v.height < |V| has been disconnected from sink and can be relabeled to (|V| + 1).
             */
            if (0 < oldHeight && oldHeight < N && countHeight[oldHeight] == 0) {
                gapHeuristic(oldHeight);
            }
        }

        if (DIAGNOSTIC_ENABLED) {
            diagnostic.incrementRelabels(ux.height, ux.height);
        }
    }

    private void bfs(Queue<Integer> queue, boolean[] visited)
    {
        while (!queue.isEmpty()) {
            int vertexID = queue.poll();

            for (AnnotatedFlowEdge flowEdge : vertexExtension[vertexID].getOutgoing()) {
                VertexExtension vx = flowEdge.getTarget();

                if (!visited[vx.id] && flowEdge.getInverse().hasCapacity()) {
                    vx.height = vertexExtension[vertexID].height + 1;
                    visited[vx.id] = true;
                    queue.add(vx.id);
                }
            }
        }
    }

    /*
     * The global relabeling heuristic updates the height function by computing shortest path
     * distances in the residual graph from all nodes to the sink.
     * 
     * This can be done in linear time by a backwards breadth-first search.
     */
    private void recomputeHeightsHeuristic()
    {
        Arrays.fill(countHeight, 0);

        Queue<Integer> queue = new ArrayDeque<>(N);
        boolean[] visited = new boolean[N];

        for (int i = 0; i < N; i++) {
            vertexExtension[i].height = 2 * N;
        }

        final int sinkID = getVertexExtension(getCurrentSink()).id;
        final int sourceID = getVertexExtension(getCurrentSource()).id;

        vertexExtension[sourceID].height = N;
        visited[sourceID] = true;

        vertexExtension[sinkID].height = 0;
        visited[sinkID] = true;

        queue.add(sinkID);
        bfs(queue, visited);

        queue.add(sourceID);
        bfs(queue, visited);

        for (int i = 0; i < N; i++) {
            ++countHeight[vertexExtension[i].height];
        }
    }

    /*
     * An overflowing vertex u is discharged by pushing all of its excess flow through admissible
     * edges to neighboring vertices, relabeling u as necessary to cause edges leaving u to become
     * admissible,
     */
    private void discharge(VertexExtension ux)
    {
        while (ux.hasExcess()) {
            // If there are no more edges
            if (ux.currentArc >= ux.getOutgoing().size()) {
                // then we relabel u
                relabel(ux);

                if (USE_GLOBAL_RELABELING_HEURISTIC) {
                    // If we already relabeled |V| vertices, then we do a global relabeling
                    // Note: Global relabelings are performed periodically
                    if ((++relabelCounter) == N) {
                        recomputeHeightsHeuristic();

                        for (int i = 0; i < N; i++)
                            vertexExtension[i].currentArc = 0;

                        relabelCounter = 0;
                    }
                }

                // rewind the pointer to the next edge
                ux.currentArc = 0;
            } else {
                AnnotatedFlowEdge flowEdge = ux.getOutgoing().get(ux.currentArc);

                /*
                 * Check if the edge is admissible. If it is then do a PUSH operation. Otherwise,
                 * make currentArc point to the next edge.
                 */
                if (isAdmissible(flowEdge))
                    push(flowEdge);
                else
                    ux.currentArc++;
            }

        }
    }

    private boolean isAdmissible(AnnotatedFlowEdge e)
    {
        return e.hasCapacity() && (e
            .<VertexExtension> getSource().height == (e.<VertexExtension> getTarget().height + 1));
    }

    private VertexExtension getVertexExtension(V v)
    {
        assert vertexExtensionManager != null;
        return (VertexExtension) vertexExtensionManager.getExtension(v);
    }

    private class PushRelabelDiagnostic
    {
        // Discharges
        Map<Pair<V, V>, Integer> discharges = new HashMap<>();
        long dischargesCounter = 0;

        // Relabels
        Map<Pair<Integer, Integer>, Integer> relabels = new HashMap<>();
        long relabelsCounter = 0;

        private void incrementDischarges(AnnotatedFlowEdge ex)
        {
            Pair<V, V> p = Pair.of(ex.getSource().prototype, ex.getTarget().prototype);
            if (!discharges.containsKey(p)) {
                discharges.put(p, 0);
            }
            discharges.put(p, discharges.get(p) + 1);

            dischargesCounter++;
        }

        private void incrementRelabels(int from, int to)
        {
            Pair<Integer, Integer> p = Pair.of(from, to);
            if (!relabels.containsKey(p)) {
                relabels.put(p, 0);
            }
            relabels.put(p, relabels.get(p) + 1);

            relabelsCounter++;
        }

        void dump()
        {
            Map<Integer, Integer> labels = new HashMap<>();

            for (V v : network.vertexSet()) {
                VertexExtension vx = getVertexExtension(v);

                if (!labels.containsKey(vx.height)) {
                    labels.put(vx.height, 0);
                }

                labels.put(vx.height, labels.get(vx.height) + 1);
            }

            System.out.println("LABELS  ");
            System.out.println("------  ");
            System.out.println(labels);

            List<Map.Entry<Pair<Integer, Integer>, Integer>> relabelsSorted =
                new ArrayList<>(relabels.entrySet());

            relabelsSorted.sort((o1, o2) -> -(o1.getValue() - o2.getValue()));

            System.out.println("RELABELS    ");
            System.out.println("--------    ");
            System.out.println("    Count:  " + relabelsCounter);
            System.out.println("            " + relabelsSorted);

            List<Map.Entry<Pair<V, V>, Integer>> dischargesSorted =
                new ArrayList<>(discharges.entrySet());

            dischargesSorted.sort((one, other) -> -(one.getValue() - other.getValue()));

            System.out.println("DISCHARGES  ");
            System.out.println("----------  ");
            System.out.println("    Count:  " + dischargesCounter);
            System.out.println("            " + dischargesSorted);
        }
    }

    /**
     * Vertex extension for the push-relabel algorithm, which contains an additional height.
     */
    public class VertexExtension
        extends
        VertexExtensionBase
    {
        private int id;
        private int height; // also called label (or distance label) in some papers
        private boolean active;
        private int currentArc;

        private boolean hasExcess()
        {
            return comparator.compare(excess, 0.0) > 0;
        }

        @Override
        public String toString()
        {
            return prototype.toString() + String.format(" { HGT: %d } ", height);
        }
    }
}
