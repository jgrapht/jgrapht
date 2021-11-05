/*
 * (C) Copyright 2015-2021, by Alexey Kudinkin, Joris Kinable and Contributors.
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
import org.jgrapht.util.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.alg.util.extension.*;

import java.util.*;
import java.util.stream.*;

/**
 * Base class backing algorithms allowing to derive
 * <a href="https://en.wikipedia.org/wiki/Maximum_flow_problem">maximum-flow</a> from the supplied
 * <a href="https://en.wikipedia.org/wiki/Flow_network">flow network</a>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexey Kudinkin
 * @author Joris Kinable
 */
public abstract class MaximumFlowAlgorithmBase<V, E>
    implements
    MaximumFlowAlgorithm<V, E>,
    MinimumSTCutAlgorithm<V, E>
{
    /**
     * Default tolerance.
     */
    public static final double DEFAULT_EPSILON = 1e-9;

    /* input network */
    protected Graph<V, E> network;
    /* indicates whether the input graph is directed or not */
    protected final boolean directedGraph;
    /* Used to compare floating point values */
    protected Comparator<Double> comparator;

    protected ExtensionManager<V, ? extends VertexExtensionBase> vertexExtensionManager;
    protected ExtensionManager<E, ? extends AnnotatedFlowEdge> edgeExtensionManager;

    /* Source used during the last invocation of this algorithm */
    protected V source = null;
    /* Sink used during the last invocation of this algorithm */
    protected V sink = null;
    /* Max flow established after last invocation of the algorithm. */
    protected double maxFlowValue = -1;
    /* Mapping of the flow on each edge. */
    protected Map<E, Double> maxFlow = null;
    /* Source parition of S-T cut */
    protected Set<V> sourcePartition;
    /* Sink parition of S-T cut */
    protected Set<V> sinkPartition;
    /* Cut edges */
    protected Set<E> cutEdges;

    /**
     * Construct a new maximum flow
     * 
     * @param network the network
     * @param epsilon the tolerance for the comparison of floating point values
     */
    public MaximumFlowAlgorithmBase(Graph<V, E> network, double epsilon)
    {
        this.network = network;
        this.directedGraph = network.getType().isDirected();
        this.comparator = new ToleranceDoubleComparator(epsilon);
    }

    /**
     * Prepares all data structures to start a new invocation of the Maximum Flow or Minimum Cut
     * algorithms
     * 
     * @param source source
     * @param sink sink
     * @param vertexExtensionFactory vertex extension factory
     * @param edgeExtensionFactory edge extension factory
     * @param <VE> vertex extension type
     */
    protected <VE extends VertexExtensionBase> void init(
        V source, V sink, ExtensionFactory<VE> vertexExtensionFactory,
        ExtensionFactory<AnnotatedFlowEdge> edgeExtensionFactory)
    {
        vertexExtensionManager = new ExtensionManager<>(vertexExtensionFactory);
        edgeExtensionManager = new ExtensionManager<>(edgeExtensionFactory);

        buildInternal();
        this.source = source;
        this.sink = sink;
        maxFlowValue = 0;
        maxFlow = null;
        sourcePartition = null;
        sinkPartition = null;
        cutEdges = null;
    }

    /**
     * Create internal data structure
     */
    private void buildInternal()
    {
        if (directedGraph) { // Directed graph
            for (V v : network.vertexSet()) {
                VertexExtensionBase vx = vertexExtensionManager.getExtension(v);
                vx.prototype = v;
            }
            for (V u : network.vertexSet()) {
                VertexExtensionBase ux = vertexExtensionManager.getExtension(u);

                for (E e : network.outgoingEdgesOf(u)) {
                    V v = network.getEdgeTarget(e);
                    VertexExtensionBase vx = vertexExtensionManager.getExtension(v);

                    AnnotatedFlowEdge forwardEdge = createEdge(ux, vx, e, network.getEdgeWeight(e));
                    AnnotatedFlowEdge backwardEdge = createBackwardEdge(forwardEdge);

                    ux.getOutgoing().add(forwardEdge);

                    if (backwardEdge.prototype == null) {
                        vx.getOutgoing().add(backwardEdge);
                    }
                }
            }
        } else { // Undirected graph
            for (V v : network.vertexSet()) {
                VertexExtensionBase vx = vertexExtensionManager.getExtension(v);
                vx.prototype = v;
            }
            for (E e : network.edgeSet()) {
                VertexExtensionBase ux =
                    vertexExtensionManager.getExtension(network.getEdgeSource(e));
                VertexExtensionBase vx =
                    vertexExtensionManager.getExtension(network.getEdgeTarget(e));
                AnnotatedFlowEdge forwardEdge = createEdge(ux, vx, e, network.getEdgeWeight(e));
                AnnotatedFlowEdge backwardEdge = createBackwardEdge(forwardEdge);
                ux.getOutgoing().add(forwardEdge);
                vx.getOutgoing().add(backwardEdge);
            }
        }
    }

    private AnnotatedFlowEdge createEdge(
        VertexExtensionBase source, VertexExtensionBase target, E e, double weight)
    {
        AnnotatedFlowEdge ex = edgeExtensionManager.getExtension(e);
        ex.source = source;
        ex.target = target;
        ex.capacity = weight;
        ex.prototype = e;

        return ex;
    }

    private AnnotatedFlowEdge createBackwardEdge(AnnotatedFlowEdge forwardEdge)
    {
        AnnotatedFlowEdge backwardEdge;
        E backwardPrototype =
            network.getEdge(forwardEdge.target.prototype, forwardEdge.source.prototype);

        if (directedGraph && backwardPrototype != null) { // if edge exists in directed input graph
            backwardEdge = createEdge(
                forwardEdge.target, forwardEdge.source, backwardPrototype,
                network.getEdgeWeight(backwardPrototype));
        } else {
            backwardEdge = edgeExtensionManager.createExtension();
            backwardEdge.source = forwardEdge.target;
            backwardEdge.target = forwardEdge.source;
            if (!directedGraph) { // Undirected graph: if (u,v) exists, then so much (v,u)
                backwardEdge.capacity = network.getEdgeWeight(backwardPrototype);
                backwardEdge.prototype = backwardPrototype;
            }
        }

        forwardEdge.inverse = backwardEdge;
        backwardEdge.inverse = forwardEdge;

        return backwardEdge;
    }

    /**
     * Increase flow in the direction denoted by edge $(u,v)$. Any existing flow in the reverse
     * direction $(v,u)$ gets reduced first. More precisely, let $f_2$ be the existing flow in the
     * direction $(v,u)$, and $f_1$ be the desired increase of flow in direction $(u,v)$. If $f_1
     * \geq f_2$, then the flow on $(v,u)$ becomes $0$, and the flow on $(u,v)$ becomes $f_1-f_2$.
     * Else, if $f_1 \textlptr f_2$, the flow in the direction $(v, u)$ is reduced, i.e. the flow on
     * $(v, u)$ becomes $f_2 - f_1$, whereas the flow on $(u,v)$ remains zero.
     * 
     * @param edge desired direction in which the flow is increased
     * @param flow increase of flow in the the direction indicated by the forwardEdge
     */
    protected void pushFlowThrough(AnnotatedFlowEdge edge, double flow)
    {
        AnnotatedFlowEdge inverseEdge = edge.getInverse();

        assert ((comparator.compare(edge.flow, 0.0) == 0)
            || (comparator.compare(inverseEdge.flow, 0.0) == 0));

        if (comparator.compare(inverseEdge.flow, flow) < 0) { // If f_1 >= f_2
            double flowDifference = flow - inverseEdge.flow;

            edge.flow += flowDifference;
            edge.capacity -= inverseEdge.flow; // Capacity on edge (u,v) PLUS flow on (v,u) gives
                                               // the MAXIMUM flow in the direction (u,v) i.e
                                               // edge.weight in the graph 'network'.

            inverseEdge.flow = 0;
            inverseEdge.capacity += flowDifference;
        } else { // If f1 < f2
            edge.capacity -= flow;
            inverseEdge.flow -= flow;
        }
    }

    /**
     * Create a map which specifies for each edge in the input map the amount of flow that flows
     * through it
     * 
     * @return a map which specifies for each edge in the input map the amount of flow that flows
     *         through it
     */
    protected Map<E, Double> composeFlow()
    {
        Map<E, Double> maxFlow = new HashMap<>();

        for (E e : network.edgeSet()) {
            AnnotatedFlowEdge annotatedFlowEdge = edgeExtensionManager.getExtension(e);
            maxFlow
                .put(
                    e, directedGraph ? annotatedFlowEdge.flow
                        : Math.max(annotatedFlowEdge.flow, annotatedFlowEdge.inverse.flow));
        }

        return maxFlow;
    }

    class VertexExtensionBase
        implements
        Extension
    {
        private final List<AnnotatedFlowEdge> outgoing = new ArrayList<>();

        V prototype;

        double excess;

        public List<AnnotatedFlowEdge> getOutgoing()
        {
            return outgoing;
        }
    }

    class AnnotatedFlowEdge
        implements
        Extension
    {
        /* Edge source */
        private VertexExtensionBase source;
        /* Edge target */
        private VertexExtensionBase target;
        /* Inverse edge */
        private AnnotatedFlowEdge inverse;

        E prototype; // Edge
        double capacity; // Maximum by which the flow in the direction can be increased (on top of
                         // the flow already in this direction).
        double flow; // Flow in the direction denoted by this edge

        public <VE extends VertexExtensionBase> VE getSource()
        {
            return TypeUtil.uncheckedCast(source);
        }

        public void setSource(VertexExtensionBase source)
        {
            this.source = source;
        }

        public <VE extends VertexExtensionBase> VE getTarget()
        {
            return TypeUtil.uncheckedCast(target);
        }

        public void setTarget(VertexExtensionBase target)
        {
            this.target = target;
        }

        public AnnotatedFlowEdge getInverse()
        {
            return inverse;
        }

        public boolean hasCapacity()
        {
            return comparator.compare(capacity, flow) > 0;
        }

        public double getResidualCapacity()
        {
            return capacity - flow;
        }

        @Override
        public String toString()
        {
            return "(" + (source == null ? null : source.prototype) + ","
                + (target == null ? null : target.prototype) + ",c:" + capacity + " f: " + flow
                + ")";
        }
    }

    /**
     * Returns current source vertex, or <code>null</code> if there was no <code>
     * calculateMaximumFlow</code> calls.
     *
     * @return current source
     */
    public V getCurrentSource()
    {
        return source;
    }

    /**
     * Returns current sink vertex, or <code>null</code> if there was no <code>
     * calculateMaximumFlow</code> calls.
     *
     * @return current sink
     */
    public V getCurrentSink()
    {
        return sink;
    }

    /**
     * Returns maximum flow value, that was calculated during last <code>
     * calculateMaximumFlow</code> call.
     *
     * @return maximum flow value
     */
    public double getMaximumFlowValue()
    {
        return maxFlowValue;
    }

    /**
     * Returns maximum flow, that was calculated during last <code>
     * calculateMaximumFlow</code> call, or <code>null</code>, if there was no <code>
     * calculateMaximumFlow</code> calls.
     *
     * @return <i>read-only</i> mapping from edges to doubles - flow values
     */
    public Map<E, Double> getFlowMap()
    {
        if (maxFlow == null) // Lazily calculate the max flow map
            maxFlow = composeFlow();
        return maxFlow;
    }

    /**
     * Returns the direction of the flow on an edge $(u,v)$. In case $(u,v)$ is a directed edge
     * (arc), this function will always return the edge target $v$. However, if $(u,v)$ is an edge
     * in an undirected graph, flow may go through the edge in either side. If the flow goes from
     * $u$ to $v$, we return $v$, otherwise $u$. If the flow on an edge equals $0$, the returned
     * value has no meaning.
     * 
     * @param e edge
     * @return the vertex where the flow leaves the edge
     */
    public V getFlowDirection(E e)
    {
        if (!network.containsEdge(e))
            throw new IllegalArgumentException(
                "Cannot query the flow on an edge which does not exist in the input graph!");
        AnnotatedFlowEdge annotatedFlowEdge = edgeExtensionManager.getExtension(e);

        if (directedGraph)
            return annotatedFlowEdge.getTarget().prototype;

        AnnotatedFlowEdge inverseEdge = annotatedFlowEdge.getInverse();
        if (annotatedFlowEdge.flow > inverseEdge.flow)
            return annotatedFlowEdge.getTarget().prototype;
        else
            return inverseEdge.getTarget().prototype;
    }

    /*---------------- Minimum s-t cut related methods -------------------*/

    @Override
    public double calculateMinCut(V source, V sink)
    {
        return this.getMaximumFlowValue(source, sink);
    }

    @Override
    public double getCutCapacity()
    {
        return getMaximumFlowValue();
    }

    @Override
    public Set<V> getSourcePartition()
    {
        if (sourcePartition == null)
            calculateSourcePartition();
        return sourcePartition;
    }

    @Override
    public Set<V> getSinkPartition()
    {
        if (sinkPartition == null) {
            sinkPartition = new LinkedHashSet<>(network.vertexSet());
            sinkPartition.removeAll(this.getSourcePartition());
        }
        return sinkPartition;
    }

    @Override
    public Set<E> getCutEdges()
    {
        if (cutEdges != null)
            return cutEdges;
        cutEdges = new LinkedHashSet<>();

        Set<V> p1 = getSourcePartition();
        if (directedGraph) {
            for (V vertex : p1) {
                cutEdges
                    .addAll(
                        network
                            .outgoingEdgesOf(vertex).stream()
                            .filter(edge -> !p1.contains(network.getEdgeTarget(edge)))
                            .collect(Collectors.toList()));
            }
        } else {
            cutEdges
                .addAll(
                    network
                        .edgeSet().stream()
                        .filter(
                            e -> p1.contains(network.getEdgeSource(e))
                                ^ p1.contains(network.getEdgeTarget(e)))
                        .collect(Collectors.toList()));
        }
        return cutEdges;
    }

    /**
     * Calculate the set of reachable vertices from $s$ in the residual graph.
     */
    protected void calculateSourcePartition()
    {
        // the source partition contains all vertices reachable from s in the residual graph
        this.sourcePartition = new LinkedHashSet<>();
        Queue<VertexExtensionBase> processQueue = new ArrayDeque<>();
        processQueue.add(vertexExtensionManager.getExtension(getCurrentSource()));
        while (!processQueue.isEmpty()) {
            VertexExtensionBase vx = processQueue.poll();
            if (sourcePartition.contains(vx.prototype))
                continue;
            sourcePartition.add(vx.prototype);
            for (AnnotatedFlowEdge ex : vx.getOutgoing()) {
                if (ex.hasCapacity())
                    processQueue.add(ex.getTarget());
            }
        }
    }
}
