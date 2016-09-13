/*
 * (C) Copyright 2015-2016, by Alexey Kudinkin, Joris Kinable and Contributors.
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
package org.jgrapht.alg.flow;

import java.util.*;
import java.util.stream.Collectors;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.extension.Extension;
import org.jgrapht.alg.util.extension.ExtensionFactory;
import org.jgrapht.alg.util.extension.ExtensionManager;


/**
 * Base class backing algorithms allowing to derive <a
 * href="https://en.wikipedia.org/wiki/Maximum_flow_problem">maximum-flow</a>
 * from the supplied <a href="https://en.wikipedia.org/wiki/Flow_network">flow
 * network</a>
 *
 * @param <V> vertex concept type
 * @param <E> edge concept type
 *
 * @author Alexey Kudinkin
 * @author Joris Kinable
 */
public abstract class MaximumFlowAlgorithmBase<V, E>
    implements MaximumFlowAlgorithm<V, E>, MinimumSTCutAlgorithm<V,E>
{
    /**
     * Default tolerance.
     */
    public static final double DEFAULT_EPSILON = 1e-9;
    /* tolerance (DEFAULT_EPSILON or user-defined) */
    public final double epsilon;

    /* input network */
    protected Graph<V, E> network;
    /* indicates whether the input graph is directed or not */
    protected final boolean directed_graph;


    protected ExtensionManager<V, ? extends VertexExtensionBase> vertexExtensionManager;
    protected ExtensionManager<E, ? extends AnnotatedFlowEdge> edgeExtensionManager;

    /* Source used during the last invocation of this algorithm */
    protected V source=null;
    /* Sink used during the last invocation of this algorithm */
    protected V sink=null;
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

    public MaximumFlowAlgorithmBase(Graph<V,E> network, double epsilon){
        this.network = network;
        this.directed_graph = network instanceof DirectedGraph;
        this.epsilon = epsilon;
    }

    /**
     * Prepares all datastructures to start a new invocation of the Maximimum Flow or Minimum Cut algorithms
     * @param source source
     * @param sink sink
     */
    protected <VE extends VertexExtensionBase> void init(
        V source,
        V sink,
        ExtensionFactory<VE> vertexExtensionFactory,
        ExtensionFactory<AnnotatedFlowEdge> edgeExtensionFactory)
    {
        vertexExtensionManager = new ExtensionManager<>(vertexExtensionFactory);
        edgeExtensionManager = new ExtensionManager<>(edgeExtensionFactory);

        buildInternal();
        this.source=source;
        this.sink=sink;
        maxFlowValue = 0;
        maxFlow = null;
        sourcePartition=null;
        sinkPartition=null;
        cutEdges=null;
    }

    /**
     * Create internal data structure
     */
    private void buildInternal()
    {
        if(directed_graph) { //Directed graph
            DirectedGraph<V,E> directedGraph=(DirectedGraph<V,E>) network;
            for (V v : directedGraph.vertexSet())
            {
                VertexExtensionBase vx = vertexExtensionManager.getExtension(v);
                vx.prototype = v;
            }
            for (V u : directedGraph.vertexSet())
            {
                VertexExtensionBase ux = vertexExtensionManager.getExtension(u);

                for (E e : directedGraph.outgoingEdgesOf(u))
                {
                    V v = directedGraph.getEdgeTarget(e);
                    VertexExtensionBase vx = vertexExtensionManager.getExtension(v);

                    AnnotatedFlowEdge forwardEdge = createEdge(ux, vx, e, directedGraph.getEdgeWeight(e));
                    AnnotatedFlowEdge backwardEdge = createBackwardEdge(forwardEdge);

                    ux.getOutgoing().add(forwardEdge);

                    if (backwardEdge.prototype == null) {
                        vx.getOutgoing().add(backwardEdge);
                    }
                }
            }
        }else{ //Undirected graph
            for (V v : network.vertexSet())
            {
                VertexExtensionBase vx = vertexExtensionManager.getExtension(v);
                vx.prototype = v;
            }
            for (E e : network.edgeSet())
            {
                VertexExtensionBase ux = vertexExtensionManager.getExtension(network.getEdgeSource(e));
                VertexExtensionBase vx = vertexExtensionManager.getExtension(network.getEdgeTarget(e));
                AnnotatedFlowEdge forwardEdge = createEdge(ux, vx, e, network.getEdgeWeight(e));
                AnnotatedFlowEdge backwardEdge = createBackwardEdge(forwardEdge);
                ux.getOutgoing().add(forwardEdge);
                vx.getOutgoing().add(backwardEdge);
            }
        }
    }

    private AnnotatedFlowEdge createEdge(
            VertexExtensionBase source,
            VertexExtensionBase target,
            E e,
            double weight)
    {
        AnnotatedFlowEdge ex = edgeExtensionManager.getExtension(e);
        ex.source = source;
        ex.target = target;
        ex.capacity = weight;
        ex.prototype = e;

        return ex;
    }

    private AnnotatedFlowEdge createBackwardEdge(
            AnnotatedFlowEdge forwardEdge)
    {
        AnnotatedFlowEdge backwardEdge;
        E backwardPrototype = network.getEdge(forwardEdge.target.prototype, forwardEdge.source.prototype);

        if (directed_graph && backwardPrototype != null) { //if edge exists in directed input graph
            backwardEdge = createEdge(forwardEdge.target, forwardEdge.source, backwardPrototype, network.getEdgeWeight(backwardPrototype));
        } else {
            backwardEdge = edgeExtensionManager.createExtension();
            backwardEdge.source = forwardEdge.target;
            backwardEdge.target = forwardEdge.source;
            if (!directed_graph) { // Undirected graph: if (u,v) exists, then so much (v,u)
                backwardEdge.capacity = network.getEdgeWeight(backwardPrototype);
                backwardEdge.prototype = backwardPrototype;
            }
        }

        forwardEdge.inverse = backwardEdge;
        backwardEdge.inverse = forwardEdge;

        return backwardEdge;
    }

    /**
     * Increase flow in the direction denoted by edge (u,v). Any existing flow in the reverse direction (v,u) gets reduced first. More precisely, let f2 be the existing flow
     * in the direction (v,u), and f1 be the desired increase of flow in direction (u,v). If f1 >= f2, then the flow on (v,u) becomes 0, and the flow on (u,v) becomes f1-f2. Else, if f1<f2, the flow in the direction
     * (v,u) is reduced, i.e. the flow on (v,u) becomes f2-f1, whereas the flow on (u,v) remains zero.
     * @param edge desired direction in which the flow is increased
     * @param flow increase of flow in the the direction indicated by the forwardEdge
     */
    protected void pushFlowThrough(AnnotatedFlowEdge edge, double flow)
    {
        AnnotatedFlowEdge inverseEdge = edge.getInverse();

        assert ((compareFlowTo(edge.flow, 0.0) == 0) || (compareFlowTo(inverseEdge.flow, 0.0) == 0));

        if (compareFlowTo(inverseEdge.flow, flow) == -1) { //If f1 >= f2
            double flowDifference = flow - inverseEdge.flow;

            edge.flow += flowDifference;
            edge.capacity -= inverseEdge.flow; //Capacity on edge (u,v) PLUS flow on (v,u) gives the MAXIMUM flow in the direction (u,v) i.e edge.weight in the graph 'network'.

            inverseEdge.flow = 0;
            inverseEdge.capacity += flowDifference;
        } else { //If f1 < f2
            edge.capacity -= flow;
            inverseEdge.flow -= flow;
        }
    }

    /**
     * Create a map which specifies for each edge in the input map the amount of flow that flows through it
     * @return a map which specifies for each edge in the input map the amount of flow that flows through it
     */
    protected Map<E, Double> composeFlow()
    {
        Map<E, Double> maxFlow = new HashMap<>();

        for (E e : network.edgeSet())
        {
            AnnotatedFlowEdge annotatedFlowEdge = edgeExtensionManager.getExtension(e);
            maxFlow.put(e, directed_graph ? annotatedFlowEdge.flow : Math.max(annotatedFlowEdge.flow, annotatedFlowEdge.inverse.flow));
        }

        return maxFlow;
    }

    /**
     * Compares flow against val. Returns 0 if they are equal, -1 if flow < val, 1 otherwise
     * @param flow flow
     * @param val value
     * @return 0 if they are equal, -1 if flow < val, 1 otherwise
     */
    protected int compareFlowTo(double flow, double val)
    {
        if (Math.abs(flow-val) < epsilon) {
            return 0;
        } else {
            return (flow < val) ? -1 : 1;
        }
    }

    class VertexExtensionBase implements Extension
    {
        private final List<AnnotatedFlowEdge> outgoing = new ArrayList<>();

        V prototype;

        double excess;

        public List<AnnotatedFlowEdge> getOutgoing()
        {
            return outgoing;
        }
    }

    class AnnotatedFlowEdge implements Extension
    {
        /* Edge source */
        private VertexExtensionBase source;
        /* Edge target */
        private VertexExtensionBase target;
        /* Inverse edge */
        private AnnotatedFlowEdge inverse;

        E prototype; //Edge
        double capacity; //Maximum by which the flow in the direction can be increased (on top of the flow already in this direction).
        double flow; //Flow in the direction denoted by this edge

        public <VE extends VertexExtensionBase> VE getSource()
        {
            return (VE) source;
        }

        public void setSource(VertexExtensionBase source)
        {
            this.source = source;
        }

        public <VE extends VertexExtensionBase> VE getTarget()
        {
            return (VE) target;
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
            return compareFlowTo(capacity, flow) > 0;
        }

        @Override
        public String toString(){
            return "("+(source == null ? null : source.prototype)+","+(target == null ? null : target.prototype)+",c:"+capacity+" f: "+flow+")";
        }
    }

    /**
     * Returns current source vertex, or <tt>null</tt> if there was no <tt>
     * calculateMaximumFlow</tt> calls.
     *
     * @return current source
     */
    public V getCurrentSource(){ return source; }

    /**
     * Returns current sink vertex, or <tt>null</tt> if there was no <tt>
     * calculateMaximumFlow</tt> calls.
     *
     * @return current sink
     */
    public V getCurrentSink(){ return sink; }

    /**
     * Returns maximum flow value, that was calculated during last <tt>
     * calculateMaximumFlow</tt> call.
     *
     * @return maximum flow value
     */
    public double getMaximumFlowValue(){
        return maxFlowValue;
    }

    /**
     * Returns maximum flow, that was calculated during last <tt>
     * calculateMaximumFlow</tt> call, or <tt>null</tt>, if there was no <tt>
     * calculateMaximumFlow</tt> calls.
     *
     * @return <i>read-only</i> mapping from edges to doubles - flow values
     */
    public Map<E, Double> getFlowMap(){
        if(maxFlow == null) //Lazily calculate the max flow map
            maxFlow=composeFlow();
        return maxFlow;
    }

    /**
     * Returns the direction of the flow on an edge (u,v). In case (u,v) is a directed edge (arc), this function will always
     * return the edge target v. However, if (u,v) is an edge in an undirected graph, flow may go through the edge in either side.
     * If the flow goes from u to v, we return v, otherwise u. If the flow on an edge equals 0, the returned value has no meaning.
     * @param e edge
     * @return the vertex where the flow leaves the edge
     */
    public V getFlowDirection(E e){
        if(!network.containsEdge(e)) throw new IllegalArgumentException("Cannot query the flow on an edge which does not exist in the input graph!");
        AnnotatedFlowEdge annotatedFlowEdge = edgeExtensionManager.getExtension(e);

        if(directed_graph) return annotatedFlowEdge.getTarget().prototype;

        AnnotatedFlowEdge inverseEdge = annotatedFlowEdge.getInverse();
        if(annotatedFlowEdge.flow > inverseEdge.flow)
            return annotatedFlowEdge.getTarget().prototype;
        else
            return inverseEdge.getTarget().prototype;
    }


    /*---------------- Minimum s-t cut related methods -------------------*/


    @Override
    public double calculateMinCut(V source, V sink){
        return this.calculateMaximumFlow(source, sink);
    }
    @Override
    public double getCutCapacity(){
        return getMaximumFlowValue();
    }
    @Override
    public Set<V> getSourcePartition(){
        if(sourcePartition==null)
            calculateSourcePartition();
        return sourcePartition;
    }

    @Override
    public Set<V> getSinkPartition(){
        if(sinkPartition==null){
            sinkPartition=new LinkedHashSet<>(network.vertexSet());
            sinkPartition.removeAll(this.getSourcePartition());
        }
        return sinkPartition;
    }

    @Override
    public Set<E> getCutEdges(){
        if(cutEdges != null)
            return cutEdges;
        cutEdges = new LinkedHashSet<>();

        Set<V> p1=getSourcePartition();
        if(directed_graph) {
            DirectedGraph<V,E> directedGraph=(DirectedGraph<V,E>)network;
            for (V vertex : p1) {
                cutEdges.addAll(directedGraph.outgoingEdgesOf(vertex).stream().filter(edge -> !p1.contains(network.getEdgeTarget(edge))).collect(Collectors.toList()));
            }
        }else{
            cutEdges.addAll(network.edgeSet().stream().filter(e -> p1.contains(network.getEdgeSource(e)) ^ p1.contains(network.getEdgeTarget(e))).collect(Collectors.toList()));
        }
        return cutEdges;
    }

    protected void calculateSourcePartition(){
        //the source partition contains all vertices reachable fromt he s in the residual graph
        this.sourcePartition=new LinkedHashSet<>();
        Queue<VertexExtensionBase> processQueue = new LinkedList<>();
        processQueue.add(vertexExtensionManager.getExtension(getCurrentSource()));
        while(!processQueue.isEmpty()){
            VertexExtensionBase vx=processQueue.poll();
            if(sourcePartition.contains(vx.prototype))
                continue;
            sourcePartition.add(vx.prototype);
            for (AnnotatedFlowEdge ex : vx.getOutgoing()) {
                if(ex.hasCapacity())
                    processQueue.add(ex.getTarget());
            }
        }
    }
}

// End MaximumFlowAlgorithmBase.java



