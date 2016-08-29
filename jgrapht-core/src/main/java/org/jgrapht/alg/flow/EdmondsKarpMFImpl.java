/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -----------------
 * EdmondsKarpMFImpl.java
 * -----------------
 * (C) Copyright 2008-2008, by Ilya Razenshteyn and Contributors.
 * (C) Copyright 2015-2015, by Alexey Kudinkin and Contributors.
 *
 * Original Author:  Ilya Razenshteyn
 * Contributor(s):   Alexey Kudinkin, Joris Kinable
 *
 * $Id$
 *
 * Changes
 * -------
 */
package org.jgrapht.alg.flow;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.util.extension.ExtensionFactory;


/**
 * A <a href = "http://en.wikipedia.org/wiki/Flow_network">flow network</a> is a
 * directed graph where each edge has a capacity and each edge receives a flow.
 * The amount of flow on an edge can not exceed the capacity of the edge (note,
 * that all capacities must be non-negative). A flow must satisfy the
 * restriction that the amount of flow into a vertex equals the amount of flow
 * out of it, except when it is a source, which "produces" flow, or sink, which
 * "consumes" flow.
 *
 * <p>This class computes maximum flow in a network using <a href =
 * "http://en.wikipedia.org/wiki/Edmonds-Karp_algorithm">Edmonds-Karp
 * algorithm</a>. Be careful: for large networks this algorithm may consume
 * significant amount of time (its upper-bound complexity is O(VE^2), where V -
 * amount of vertices, E - amount of edges in the network).
 *
 * <p>This class can also computes minimum s-t cuts. Effectively, to compute a
 * minimum s-t cut, the implementation first computes a minimum s-t flow, after
 * which a BFS is run on the residual graph.
 *
 * <p>For more details see Andrew V. Goldberg's <i>Combinatorial Optimization
 * (Lecture Notes)</i>.
 *
 * Note: even though the algorithm accepts any kind of graph, currently only Simple directed and undirected graphs are supported (and tested!).
 *
 *
 *
 * @author Ilya Razensteyn
 */

public final class EdmondsKarpMFImpl<V, E>
    extends MaximumFlowAlgorithmBase<V, E>
{

    /* current source vertex */
    private VertexExtension currentSource;
    /* current sink vertex */
    private VertexExtension currentSink;

    private final ExtensionFactory<VertexExtension> vertexExtensionsFactory;
    private final ExtensionFactory<AnnotatedFlowEdge> edgeExtensionsFactory;

    /**
     * Constructs <tt>MaximumFlow</tt> instance to work with <i>a copy of</i>
     * <tt>network</tt>. Current source and sink are set to <tt>null</tt>. If
     * <tt>network</tt> is weighted, then capacities are weights, otherwise all
     * capacities are equal to one. Doubles are compared using <tt>
     * DEFAULT_EPSILON</tt> tolerance.
     *
     * @param network network, where maximum flow will be calculated
     */
    public EdmondsKarpMFImpl(Graph<V, E> network)
    {
        this(network, DEFAULT_EPSILON);
    }

    /**
     * Constructs <tt>MaximumFlow</tt> instance to work with <i>a copy of</i>
     * <tt>network</tt>. Current source and sink are set to <tt>null</tt>. If
     * <tt>network</tt> is weighted, then capacities are weights, otherwise all
     * capacities are equal to one.
     *
     * @param network network, where maximum flow will be calculated
     * @param epsilon tolerance for comparing doubles
     */
    public EdmondsKarpMFImpl(Graph<V, E> network, double epsilon)
    {
        super(network, epsilon);
        this.vertexExtensionsFactory =
                () -> new VertexExtension();

        this.edgeExtensionsFactory =
                () -> new AnnotatedFlowEdge();

        if (network == null) {
            throw new NullPointerException("network is null");
        }
        if (epsilon <= 0) {
            throw new IllegalArgumentException(
                "invalid epsilon (must be positive)");
        }
        for (E e : network.edgeSet())
        {
            if (network.getEdgeWeight(e) < -epsilon) {
                throw new IllegalArgumentException(
                    "invalid capacity (must be non-negative)");
            }
        }
    }

    /**
     * Sets current source to <tt>source</tt>, current sink to <tt>sink</tt>,
     * then calculates maximum flow from <tt>source</tt> to <tt>sink</tt>. Note,
     * that <tt>source</tt> and <tt>sink</tt> must be vertices of the <tt>
     * network</tt> passed to the constructor, and they must be different.
     *
     * @param source source vertex
     * @param sink sink vertex
     */
    public MaximumFlow<E> buildMaximumFlow(V source, V sink)
    {
        this.calculateMaximumFlow(source, sink);
        maxFlow = composeFlow();
        return new MaximumFlowImpl<>(maxFlowValue, maxFlow);
    }

    /**
     * Sets current source to <tt>source</tt>, current sink to <tt>sink</tt>,
     * then calculates maximum flow from <tt>source</tt> to <tt>sink</tt>. Note,
     * that <tt>source</tt> and <tt>sink</tt> must be vertices of the <tt>
     * network</tt> passed to the constructor, and they must be different.
     * If desired, a flow map can be queried afterwards; this will not require
     * a new invocation of the algorithm.
     *
     * @param source source vertex
     * @param sink sink vertex
     */
    public double calculateMaximumFlow(V source,V sink){
        super.init(source, sink, vertexExtensionsFactory, edgeExtensionsFactory);

        if (!network.containsVertex(source)) {
            throw new IllegalArgumentException(
                    "invalid source (null or not from this network)");
        }
        if (!network.containsVertex(sink)) {
            throw new IllegalArgumentException(
                    "invalid sink (null or not from this network)");
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
     * Method which finds a path from source to sink the in the residual graph. Note that this method tries to find multiple
     * paths at once. Once a single path has been discovered, no new nodes are added to the queue, but nodes which are
     * already in the queue are fully explored. As such there's a chance that multiple paths are discovered.
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
                if ((ex.flow + epsilon) < ex.capacity) {
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
     * For all paths which end in the sink. trace them back to the source and push flow through them.
     * @return total increase in flow from source to sink
     */
    private double augmentFlow()
    {
        double flowIncrease=0;
        Set<VertexExtension> seen = new HashSet<>();

        for (AnnotatedFlowEdge ex : currentSink.lastArcs) {
            double deltaFlow =
                Math.min(ex.getSource().excess, ex.capacity - ex.flow);

            if (augmentFlowAlongInternal(
                    deltaFlow,
                    ex.<VertexExtension>getSource(),
                    seen))
            {
                pushFlowThrough(ex, deltaFlow);
                flowIncrease += deltaFlow;
            }
        }
        return flowIncrease;
    }

    private boolean augmentFlowAlongInternal(
        double deltaFlow,
        VertexExtension node,
        Set<VertexExtension> seen)
    {
        if (node == currentSource) {
            return true;
        }
        if (seen.contains(node)) {
            return false;
        }

        seen.add(node);

        AnnotatedFlowEdge prev = node.lastArcs.get(0);
        if (augmentFlowAlongInternal(
                deltaFlow,
                prev.<VertexExtension>getSource(),
                seen))
        {
            pushFlowThrough(prev, deltaFlow);
            return true;
        }

        return false;
    }

    private VertexExtension getVertexExtension(V v){ return (VertexExtension)vertexExtensionManager.getExtension(v);}


    class VertexExtension extends VertexExtensionBase
    {
        boolean visited; // this mark is used during BFS to mark visited nodes
        List<AnnotatedFlowEdge> lastArcs; // last arc(-s) in the shortest path used to reach this vertex


    }
}

// End EdmondsKarpMFImpl.java

