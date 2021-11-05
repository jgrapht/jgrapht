/*
 * (C) Copyright 2018-2021, by Kirill Vishnyakov and Contributors.
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
import org.jgrapht.alg.util.extension.*;

import java.util.*;

/**
 * Implementation of {@literal <a href = "https://en.wikipedia.org/wiki/Dinic%27s_algorithm">}Dinic
 * algorithm{@literal </a>} with scaling for
 * {@literal <a href = "https://en.wikipedia.org/wiki/Maximum_flow_problem"maximum"}maximum flow
 * problem{@literal </a>}.
 *
 * The running time of the algorithm is $O(n^2m)$.
 *
 * Dinic algorithm firstly was mentioned in {@literal <i>}DINIC, E. A. 1970. Algorithm for Solution
 * of a Problem of Maximum Flow in Networks With Power Estimation. Soviet Math. Dokl. 11,
 * 1277-1280.{@literal </>}
 *
 * Scheme of the algorithm:
 *
 * 1). Create a level graph. If we can't reach the sink return flow value.
 *
 * 2). Find a blocking flow $f'$ in the level graph.
 *
 * 3). Add $f'$ to the flow $f$. Move to the step $1$.
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 *
 * @author Kirill Vishnyakov
 */

public class DinicMFImpl<V, E>
    extends
    MaximumFlowAlgorithmBase<V, E>
{

    /**
     * Current source vertex.
     */
    private VertexExtension currentSource;

    /**
     * Current sink vertex.
     */
    private VertexExtension currentSink;

    private final ExtensionFactory<VertexExtension> vertexExtensionsFactory;
    private final ExtensionFactory<AnnotatedFlowEdge> edgeExtensionsFactory;

    /**
     * Constructor. Constructs a new network on which we will calculate the maximum flow, using
     * Dinic algorithm.
     *
     * @param network the network on which we calculate the maximum flow.
     * @param epsilon the tolerance for the comparison of floating point values.
     */
    public DinicMFImpl(Graph<V, E> network, double epsilon)
    {
        super(network, epsilon);
        this.vertexExtensionsFactory = VertexExtension::new;

        this.edgeExtensionsFactory = AnnotatedFlowEdge::new;

        if (epsilon <= 0) {
            throw new IllegalArgumentException("Epsilon must be positive!");
        }

        for (E e : network.edgeSet()) {
            if (network.getEdgeWeight(e) < -epsilon) {
                throw new IllegalArgumentException("Capacity must be non-negative!");
            }
        }
    }

    /**
     * Constructor. Constructs a new network on which we will calculate the maximum flow.
     *
     * @param network the network on which we calculate the maximum flow.
     */
    public DinicMFImpl(Graph<V, E> network)
    {
        this(network, DEFAULT_EPSILON);
    }

    @Override
    public MaximumFlow<E> getMaximumFlow(V source, V sink)
    {
        this.calculateMaxFlow(source, sink);
        maxFlow = composeFlow();
        return new MaximumFlowImpl<>(maxFlowValue, maxFlow);
    }

    /**
     * Assigns source to currentSource and sink to currentSink. Afterwards invokes dinic() method to
     * calculate the maximum flow in the network using Dinic algorithm with scaling.
     *
     * @param source source vertex.
     * @param sink sink vertex.
     * @return the value of the maximum flow in the network.
     */
    private double calculateMaxFlow(V source, V sink)
    {
        super.init(source, sink, vertexExtensionsFactory, edgeExtensionsFactory);

        if (!network.containsVertex(source)) {
            throw new IllegalArgumentException("Network does not contain source!");
        }

        if (!network.containsVertex(sink)) {
            throw new IllegalArgumentException("Network does not contain sink!");
        }

        if (source.equals(sink)) {
            throw new IllegalArgumentException("Source is equal to sink!");
        }

        currentSource = getVertexExtension(source);
        currentSink = getVertexExtension(sink);

        dinic();

        return maxFlowValue;
    }

    /**
     * Creates a level graph. We can split all vertices of the graph in disjoint sets. In the same
     * set will lie vertices with equal distance from the source. It's obvious that level network
     * cannot contain edges $i \to j$, where $i$ and $j$ are two vertices for which holds: $|i.level
     * - j.level| > 1$. It follows from a property of the shortest paths. Level graph contains only
     * edges that lead from level $i$ to the level $i + 1$. Thus level graph does not contain
     * backward edges or edges that lead from $i$-th level to $i$-th.
     *
     * @return true, if level graph has been constructed(i.e we reached the sink), otherwise false.
     */
    private boolean bfs()
    {

        for (V v : network.vertexSet()) {
            getVertexExtension(v).level = -1;
        }

        Queue<VertexExtension> queue = new ArrayDeque<>();
        queue.offer(currentSource);

        currentSource.level = 0;

        while (!queue.isEmpty() && currentSink.level == -1) {
            VertexExtension v = queue.poll();
            for (AnnotatedFlowEdge edge : v.getOutgoing()) {
                VertexExtension u = edge.getTarget();
                if (comparator.compare(edge.flow, edge.capacity) < 0 && u.level == -1) {
                    u.level = v.level + 1;
                    queue.offer(u);
                }
            }
        }

        return currentSink.level != -1;
    }

    /**
     * Finds a blocking flow in the network. For each vertex we have a pointer on the first edge
     * which we can use to reach the sink. If we can't reach the sink using current edge, we
     * increment the pointer. So on each iteration we either saturate at least one edge or we
     * increment pointer.
     *
     * @param v current vertex.
     * @param flow we can push through.
     * @return value of the flow we can push.
     */
    public double dfs(VertexExtension v, double flow)
    {
        if (comparator.compare(0.0, flow) == 0) {
            return flow;
        }

        if (v.equals(currentSink)) {
            return flow;
        }

        double pushed;

        while (v.index < v.getOutgoing().size()) {
            AnnotatedFlowEdge edge = v.getOutgoing().get(v.index);
            VertexExtension u = edge.getTarget();
            if (comparator.compare(edge.flow, edge.capacity) < 0 && u.level == v.level + 1) {
                pushed = dfs(u, Math.min(flow, edge.capacity - edge.flow));
                if (comparator.compare(pushed, 0.0) != 0) {
                    pushFlowThrough(edge, pushed);
                    return pushed;
                }
            }
            v.index++;
        }

        return 0;
    }

    /**
     * Runs Dinic algorithm with scaling. Construct a level graph, then find blocking flow and
     * finally increase the flow.
     */
    public void dinic()
    {
        for (;;) {
            if (!bfs()) {
                break;
            }
            for (V v : network.vertexSet()) {
                getVertexExtension(v).index = 0;
            }

            while (true) {
                double pushed = dfs(currentSource, Double.POSITIVE_INFINITY);
                if (pushed == 0.0) {
                    break;
                }
                maxFlowValue += pushed;
            }
        }
    }

    private VertexExtension getVertexExtension(V v)
    {
        return (VertexExtension) vertexExtensionManager.getExtension(v);
    }

    /**
     * Extension for vertex class.
     */
    class VertexExtension
        extends
        VertexExtensionBase
    {

        /**
         * Stores index of the first unexplored edge from current vertex.
         */
        int index;

        /**
         * Level of vertex in the level graph.
         */
        int level;
    }
}
