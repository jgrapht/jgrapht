/*
 * (C) Copyright 2008-2018, by Ilya Razenshteyn and Contributors.
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
 * This class computes a maximum flow in a
 * <a href = "http://en.wikipedia.org/wiki/Flow_network">flow network</a> using
 * <a href = "http://en.wikipedia.org/wiki/Edmonds-Karp_algorithm">Edmonds-Karp algorithm</a>. Given
 * is a weighted directed or undirected graph $G(V,E)$ with vertex set $V$ and edge set $E$. Each
 * edge $e\in E$ has an associated non-negative capacity $u_e$. The maximum flow problem involves
 * finding a feasible flow from a source vertex $s$ to a sink vertex $t$ which is maximum. The
 * amount of flow $f_e$ through any edge $e$ cannot exceed capacity $u_e$. Moreover, flow
 * conservation must hold: the sum of flows entering a node must equal the sum of flows exiting that
 * node, except for the source and the sink nodes.
 * <p>
 * Mathematically, the maximum flow problem is stated as follows: \[ \begin{align} \max~&amp;
 * \sum_{e\in \delta^+(s)}f_e &amp;\\ \mbox{s.t. }&amp;\sum_{e\in \delta^-(i)} f_e=\sum_{e\in
 * \delta^+(i)} f_e &amp; \forall i\in V\setminus\{s,t\}\\ &amp;0\leq f_e \leq u_e &amp; \forall
 * e\in E \end{align} \] Here $\delta^+(i)$ resp $\delta^-(i)$ denote resp the outgoing and incoming
 * edges of vertex $i$.
 * <p>
 * When the input graph is undirected, an edge $(i,j)$ is treated as two directed arcs: $(i,j)$ and
 * $(j,i)$. In such a case, there is the additional restriction that the flow can only go in one
 * direction: the flow either goes form $i$ to $j$, or from $j$ to $i$, but there cannot be a
 * positive flow on $(i,j)$ and $(j,i)$ simultaneously.
 * <p>
 * The runtime complexity of this class is $O(nm^2)$, where $n$ is the number of vertices and $m$
 * the number of edges in the graph. For a more efficient algorithm, consider using
 * {@link PushRelabelMFImpl} instead.
 *
 * <p>
 * This class can also compute minimum s-t cuts. Effectively, to compute a minimum s-t cut, the
 * implementation first computes a minimum s-t flow, after which a BFS is run on the residual graph.
 *
 * <p>
 * For more details see Andrew V. Goldberg's <i>Combinatorial Optimization (Lecture Notes)</i>.
 *
 * Note: even though the algorithm accepts any kind of graph, currently only Simple directed and
 * undirected graphs are supported (and tested!).
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Ilya Razensteyn
 */

public final class EdmondsKarpMFImpl<V, E>
    extends
    MaximumFlowAlgorithmBase<V, E>
{

    /* current source vertex */
    private VertexExtension currentSource;
    /* current sink vertex */
    private VertexExtension currentSink;

    private final ExtensionFactory<VertexExtension> vertexExtensionsFactory;
    private final ExtensionFactory<AnnotatedFlowEdge> edgeExtensionsFactory;

    /**
     * Constructs <tt>MaximumFlow</tt> instance to work with <i>a copy of</i> <tt>network</tt>.
     * Current source and sink are set to <tt>null</tt>. If <tt>network</tt> is weighted, then
     * capacities are weights, otherwise all capacities are equal to one. Doubles are compared using
     * <tt>
     * DEFAULT_EPSILON</tt> tolerance.
     *
     * @param network network, where maximum flow will be calculated
     */
    public EdmondsKarpMFImpl(Graph<V, E> network)
    {
        this(network, DEFAULT_EPSILON);
    }

    /**
     * Constructs <tt>MaximumFlow</tt> instance to work with <i>a copy of</i> <tt>network</tt>.
     * Current source and sink are set to <tt>null</tt>. If <tt>network</tt> is weighted, then
     * capacities are weights, otherwise all capacities are equal to one.
     *
     * @param network network, where maximum flow will be calculated
     * @param epsilon tolerance for comparing doubles
     */
    public EdmondsKarpMFImpl(Graph<V, E> network, double epsilon)
    {
        super(network, epsilon);
        this.vertexExtensionsFactory = () -> new VertexExtension();

        this.edgeExtensionsFactory = () -> new AnnotatedFlowEdge();

        if (network == null) {
            throw new NullPointerException("network is null");
        }
        if (epsilon <= 0) {
            throw new IllegalArgumentException("invalid epsilon (must be positive)");
        }
        for (E e : network.edgeSet()) {
            if (network.getEdgeWeight(e) < -epsilon) {
                throw new IllegalArgumentException("invalid capacity (must be non-negative)");
            }
        }
    }

    /**
     * Sets current source to <tt>source</tt>, current sink to <tt>sink</tt>, then calculates
     * maximum flow from <tt>source</tt> to <tt>sink</tt>. Note, that <tt>source</tt> and
     * <tt>sink</tt> must be vertices of the <tt>
     * network</tt> passed to the constructor, and they must be different.
     *
     * @param source source vertex
     * @param sink sink vertex
     * 
     * @return a maximum flow
     */
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
     * network</tt> passed to the constructor, and they must be different. If desired, a flow map
     * can be queried afterwards; this will not require a new invocation of the algorithm.
     *
     * @param source source vertex
     * @param sink sink vertex
     * 
     * @return the value of the maximum flow
     */
    public double calculateMaximumFlow(V source, V sink)
    {
        super.init(source, sink, vertexExtensionsFactory, edgeExtensionsFactory);

        if (!network.containsVertex(source)) {
            throw new IllegalArgumentException("invalid source (null or not from this network)");
        }
        if (!network.containsVertex(sink)) {
            throw new IllegalArgumentException("invalid sink (null or not from this network)");
        }

        if (source.equals(sink)) {
            throw new IllegalArgumentException("source is equal to sink");
        }

        currentSource = getVertexExtension(source);
        currentSink = getVertexExtension(sink);

        for (;;) {
            breadthFirstSearch();

            if (!currentSink.visited) {
                break;
            }

            maxFlowValue += augmentFlow();
        }

        return maxFlowValue;
    }

    /**
     * Method which finds a path from source to sink the in the residual graph. Note that this
     * method tries to find multiple paths at once. Once a single path has been discovered, no new
     * nodes are added to the queue, but nodes which are already in the queue are fully explored. As
     * such there's a chance that multiple paths are discovered.
     */
    private void breadthFirstSearch()
    {
        for (V v : network.vertexSet()) {
            getVertexExtension(v).visited = false;
            getVertexExtension(v).lastArcs = null;
        }

        Queue<VertexExtension> queue = new LinkedList<>();
        queue.offer(currentSource);

        currentSource.visited = true;
        currentSource.excess = Double.POSITIVE_INFINITY;

        currentSink.excess = 0.0;

        boolean seenSink = false;

        while (queue.size() != 0) {
            VertexExtension ux = queue.poll();

            for (AnnotatedFlowEdge ex : ux.getOutgoing()) {
                if (comparator.compare(ex.flow, ex.capacity) < 0) {
                    VertexExtension vx = ex.getTarget();

                    if (vx == currentSink) {
                        vx.visited = true;

                        if (vx.lastArcs == null) {
                            vx.lastArcs = new ArrayList<>();
                        }

                        vx.lastArcs.add(ex);
                        vx.excess += Math.min(ux.excess, ex.capacity - ex.flow);

                        seenSink = true;
                    } else if (!vx.visited) {
                        vx.visited = true;
                        vx.excess = Math.min(ux.excess, ex.capacity - ex.flow);

                        vx.lastArcs = Collections.singletonList(ex);

                        if (!seenSink) {
                            queue.add(vx);
                        }
                    }
                }
            }
        }
    }

    /**
     * For all paths which end in the sink. trace them back to the source and push flow through
     * them.
     * 
     * @return total increase in flow from source to sink
     */
    private double augmentFlow()
    {
        double flowIncrease = 0;
        Set<VertexExtension> seen = new HashSet<>();

        for (AnnotatedFlowEdge ex : currentSink.lastArcs) {
            double deltaFlow = Math.min(ex.getSource().excess, ex.capacity - ex.flow);

            if (augmentFlowAlongInternal(deltaFlow, ex.<VertexExtension> getSource(), seen)) {
                pushFlowThrough(ex, deltaFlow);
                flowIncrease += deltaFlow;
            }
        }
        return flowIncrease;
    }

    private boolean augmentFlowAlongInternal(
        double deltaFlow, VertexExtension node, Set<VertexExtension> seen)
    {
        if (node == currentSource) {
            return true;
        }
        if (seen.contains(node)) {
            return false;
        }

        seen.add(node);

        AnnotatedFlowEdge prev = node.lastArcs.get(0);
        if (augmentFlowAlongInternal(deltaFlow, prev.<VertexExtension> getSource(), seen)) {
            pushFlowThrough(prev, deltaFlow);
            return true;
        }

        return false;
    }

    private VertexExtension getVertexExtension(V v)
    {
        return (VertexExtension) vertexExtensionManager.getExtension(v);
    }

    class VertexExtension
        extends
        VertexExtensionBase
    {
        boolean visited; // this mark is used during BFS to mark visited nodes
        List<AnnotatedFlowEdge> lastArcs; // last arc(-s) in the shortest path used to reach this
                                          // vertex

    }
}
