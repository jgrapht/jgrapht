/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* -----------------
 * EdmondsKarpMaximumFlow.java
 * -----------------
 * (C) Copyright 2008-2008, by Ilya Razenshteyn and Contributors.
 *
 * Original Author:  Ilya Razenshteyn
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 */
package org.jgrapht.alg;

import com.google.common.collect.Maps;
import org.jgrapht.DirectedGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


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
 * <p>For more details see Andrew V. Goldberg's <i>Combinatorial Optimization
 * (Lecture Notes)</i>.
 */
public final class EdmondsKarpMaximumFlow<V, E>
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     * Default tolerance.
     */
    public static final double DEFAULT_EPSILON = 0.000000001;

    //~ Instance fields --------------------------------------------------------

    private final DirectedGraph<V, E> network; // our network
    private final double epsilon; // tolerance (DEFAULT_EPSILON or user-defined)
    private int currentSource; // current source vertex
    private int currentSink; // current sink vertex
    private Map<E, Double> maximumFlow; // current maximum flow
    private Double maximumFlowValue; // current maximum flow value
    private int numNodes; // number of nodes in the network
    private Map<V, Integer> indexer; // mapping from vertices to their indexes
                                     // in the internal representation
    private List<Node> nodes; // internal representation of the network

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructs <tt>MaximumFlow</tt> instance to work with <i>a copy of</i>
     * <tt>network</tt>. Current source and sink are set to <tt>null</tt>. If
     * <tt>network</tt> is weighted, then capacities are weights, otherwise all
     * capacities are equal to one. Doubles are compared using <tt>
     * DEFAULT_EPSILON</tt> tolerance.
     *
     * @param network network, where maximum flow will be calculated
     */
    public EdmondsKarpMaximumFlow(final DirectedGraph<V, E> network)
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
    public EdmondsKarpMaximumFlow(final DirectedGraph<V, E> network,
        final double epsilon)
    {
        if (network == null) {
            throw new NullPointerException("network is null");
        }
        if (epsilon <= 0) {
            throw new IllegalArgumentException(
                "invalid epsilon (must be positive)");
        }
        for (final E e : network.edgeSet()) {
            if (network.getEdgeWeight(e) < -epsilon) {
                throw new IllegalArgumentException(
                    "invalid capacity (must be non-negative)");
            }
        }

        this.network = network;
        this.epsilon = epsilon;

        currentSource = -1;
        currentSink = -1;
        maximumFlow = null;
        maximumFlowValue = null;

        buildInternalNetwork();
    }

    //~ Methods ----------------------------------------------------------------

    // converting the original network into internal more convenient format
    private void buildInternalNetwork()
    {
        numNodes = network.vertexSet().size();
        nodes = new ArrayList<Node>();
        final Iterator<V> it = network.vertexSet().iterator();
        indexer = Maps.newHashMap();
        for (int i = 0; i < numNodes; i++) {
            final V currentNode = it.next();
            nodes.add(new Node(currentNode));
            indexer.put(currentNode, i);
        }
        for (int i = 0; i < numNodes; i++) {
            final V we = nodes.get(i).prototype;
            for (final E e : network.outgoingEdgesOf(we)) {
                final V he = network.getEdgeTarget(e);
                final int j = indexer.get(he);
                final Arc e1 = new Arc(i, j, network.getEdgeWeight(e), e);
                final Arc e2 = new Arc(j, i, 0.0, null);
                e1.reversed = e2;
                e2.reversed = e1;
                nodes.get(i).outgoingArcs.add(e1);
                nodes.get(j).outgoingArcs.add(e2);
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
    public void calculateMaximumFlow(
        final V source,
        final V sink)
    {
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

        currentSource = indexer.get(source);
        currentSink = indexer.get(sink);

        for (int i = 0; i < numNodes; i++) {
            for (final Arc currentArc : nodes.get(i).outgoingArcs) {
                currentArc.flow = 0.0;
            }
        }
        maximumFlowValue = 0.0;
        for (;;) {
            breadthFirstSearch();
            if (!nodes.get(currentSink).visited) {
                maximumFlow = Maps.newHashMap();
                for (int i = 0; i < numNodes; i++) {
                    for (final Arc currentArc : nodes.get(i).outgoingArcs) {
                        if (currentArc.prototype != null) {
                            maximumFlow.put(
                                currentArc.prototype,
                                currentArc.flow);
                        }
                    }
                }
                return;
            }
            augmentFlow();
        }
    }

    private void breadthFirstSearch()
    {
        for (int i = 0; i < numNodes; i++) {
            nodes.get(i).visited = false;
        }
        final Queue<Integer> queue = new LinkedList<Integer>();
        queue.offer(currentSource);
        nodes.get(currentSource).visited = true;
        nodes.get(currentSource).flowAmount = Double.POSITIVE_INFINITY;
        while (!queue.isEmpty()) {
            final int currentNode = queue.poll();
            for (final Arc currentArc : nodes.get(currentNode).outgoingArcs) {
                if (currentArc.flow + epsilon < currentArc.capacity) {
                    if (!nodes.get(currentArc.head).visited) {
                        nodes.get(currentArc.head).visited = true;
                        nodes.get(currentArc.head).flowAmount =
                            Math.min(
                                nodes.get(currentNode).flowAmount,
                                currentArc.capacity - currentArc.flow);
                        nodes.get(currentArc.head).lastArc = currentArc;
                        queue.add(currentArc.head);
                    }
                }
            }
        }
    }

    private void augmentFlow()
    {
        final double deltaFlow = nodes.get(currentSink).flowAmount;
        maximumFlowValue += deltaFlow;
        int currentNode = currentSink;
        while (currentNode != currentSource) {
            nodes.get(currentNode).lastArc.flow += deltaFlow;
            nodes.get(currentNode).lastArc.reversed.flow -= deltaFlow;
            currentNode = nodes.get(currentNode).lastArc.tail;
        }
    }

    /**
     * Returns maximum flow value, that was calculated during last <tt>
     * calculateMaximumFlow</tt> call, or <tt>null</tt>, if there was no <tt>
     * calculateMaximumFlow</tt> calls.
     *
     * @return maximum flow value
     */
    public Double getMaximumFlowValue()
    {
        return maximumFlowValue;
    }

    /**
     * Returns maximum flow, that was calculated during last <tt>
     * calculateMaximumFlow</tt> call, or <tt>null</tt>, if there was no <tt>
     * calculateMaximumFlow</tt> calls.
     *
     * @return <i>read-only</i> mapping from edges to doubles - flow values
     */
    public Map<E, Double> getMaximumFlow()
    {
        if (maximumFlow == null) {
            return null;
        }
        return Collections.unmodifiableMap(maximumFlow);
    }

    /**
     * Returns current source vertex, or <tt>null</tt> if there was no <tt>
     * calculateMaximumFlow</tt> calls.
     *
     * @return current source
     */
    public V getCurrentSource()
    {
        if (currentSource == -1) {
            return null;
        }
        return nodes.get(currentSource).prototype;
    }

    /**
     * Returns current sink vertex, or <tt>null</tt> if there was no <tt>
     * calculateMaximumFlow</tt> calls.
     *
     * @return current sink
     */
    public V getCurrentSink()
    {
        if (currentSink == -1) {
            return null;
        }
        return nodes.get(currentSink).prototype;
    }

    //~ Inner Classes ----------------------------------------------------------

    // class used for internal representation of network
    class Node
    {
        final V prototype; // corresponding node in the original network
        final List<Arc> outgoingArcs = new ArrayList<Arc>(); // list of outgoing arcs
                                                       // in the residual
                                                       // network
        boolean visited; // this mark is used during BFS to mark visited nodes
        Arc lastArc; // last arc in the shortest path
        double flowAmount; // amount of flow, we are able to push here

        Node(
            final V prototype)
        {
            this.prototype = prototype;
        }
    }

    // class used for internal representation of network
    class Arc
    {
        final int tail; // "from"
        final int head; // "to"
        final double capacity; // capacity (can be zero)
        double flow; // current flow (can be negative)
        Arc reversed; // for each arc in the original network we are to create
                      // reversed arc
                      final E prototype; // corresponding edge in the original network, can be null,
                     // if it is reversed arc

        Arc(
            final int tail,
            final int head,
            final double capacity,
            final E prototype)
        {
            this.tail = tail;
            this.head = head;
            this.capacity = capacity;
            this.prototype = prototype;
        }
    }
}

// End EdmondsKarpMaximumFlow.java
