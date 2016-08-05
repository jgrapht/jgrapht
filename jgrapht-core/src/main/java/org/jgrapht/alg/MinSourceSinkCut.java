/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2012, by Barak Naveh and Contributors.
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
/* -------------------------
 * MinSourceSinkCut.java
 * -------------------------
 * (C) Copyright 2012-2012, by Joris Kinable and Contributors.
 *
 * Original Author:  Joris Kinable
 * Contributor(s):
 *
 * Changes
 * -------
 * 26-Nov-2012 : Initial revision (JK);
 * 4-Aug-2016 : Added support for undirected graphs, improved efficiency of algorithm to O(E)
 *
 */
package org.jgrapht.alg;

import java.util.*;
import java.util.stream.Collectors;

import org.jgrapht.*;
import org.jgrapht.alg.flow.MaximumFlowAlgorithmBase;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm.*;


/**
 * Given a weighted graph G(V,E) (directed or undirected). This class computes a minimum s-t
 * cut. For this purpose this class relies on a Maximum Flow Algorithm. Note:
 * it is not recommended to use this class to calculate the overall minimum cut
 * in a graph by iteratively invoking this class for all source-sink pairs. This
 * is computationally expensive. Instead, use the StoerWagnerMinimumCut
 * implementation.
 *
 * Runtime: O(E)
 *
 * @author Joris Kinable
 */
public class MinSourceSinkCut<V, E>
{
    MaximumFlowAlgorithm<V, E> maxFlowAlg;
    Set<V> minCut = null;
    Graph<V, E> graph;
    double cutWeight;
    V source = null;
    V sink = null;
    double epsilon = MaximumFlowAlgorithmBase.DEFAULT_EPSILON;

    public MinSourceSinkCut(Graph<V, E> graph)
    {
        this.maxFlowAlg = new PushRelabelMFImpl<>(graph);
        this.graph = graph;
    }

    public MinSourceSinkCut(Graph<V, E> graph, double epsilon)
    {
        this(graph, new PushRelabelMFImpl<>(graph), epsilon);
    }

    public MinSourceSinkCut(Graph<V, E> graph, MaximumFlowAlgorithm<V, E> maximumFlowAlgorithm, double epsilon)
    {
        this.maxFlowAlg = maximumFlowAlgorithm;
        this.graph = graph;
        this.epsilon = epsilon;
    }

    /**
     * Compute a minimum s-t cut
     * Runtime: O(E)
     *
     * @param source s
     * @param sink t
     */
    public void computeMinCut(V source, V sink)
    {
        this.source = source;
        this.sink = sink;
        minCut = new HashSet<>();

        //Compute a maxFlow from source to sink
        MaximumFlow<E> maxFlow = maxFlowAlg.buildMaximumFlow(source, sink);
        this.cutWeight = maxFlow.getValue();

        if(graph instanceof DirectedGraph)
            processDirectedGraph(maxFlow);
        else
            processUndirectedGraph(maxFlow);
    }

    private void processDirectedGraph( MaximumFlow<E> maxFlow){
        minCut=new LinkedHashSet<>();
        DirectedGraph<V,E> directedGraph=(DirectedGraph<V,E>)graph;
        Queue<V> processQueue = new LinkedList<>();
        processQueue.add(source);

        while (!processQueue.isEmpty()) {
            V vertex = processQueue.remove();
            if (minCut.contains(vertex))
                continue;

            minCut.add(vertex);

            //1. Get the forward edges with residual capacity
            for (E edge : directedGraph.outgoingEdgesOf(vertex)) {
                double edgeCapacity = directedGraph.getEdgeWeight(edge);
                double flowValue = maxFlow.getFlow().get(edge);
                if (edgeCapacity - flowValue >= epsilon) { //Has some residual capacity left
                    processQueue.add(directedGraph.getEdgeTarget(edge));
                }
            }

            //2. Get the backward edges with non-zero flow
            for (E edge : directedGraph.incomingEdgesOf(vertex)) {
                double flowValue = maxFlow.getFlow().get(edge);
                if (flowValue >= epsilon) { //Has non-zero flow
                    processQueue.add(directedGraph.getEdgeSource(edge));
                }
            }
        }
    }

    public void processUndirectedGraph(MaximumFlow<E> maxFlow){
        minCut = new LinkedHashSet<>();
        Queue<V> processQueue = new LinkedList<>();
        processQueue.add(source);

        //Let G' be the graph consisting of edges with residual cost. An edge has residual cost if c(e)-f(e)>0.
        //All vertices reachable from the source vertex in graph G' belong to the same partition
        while (!processQueue.isEmpty()) {
            V vertex = processQueue.remove();
            if (minCut.contains(vertex))
                continue;

            minCut.add(vertex);

            for(E edge : graph.edgesOf(vertex)){
                double flowValue = maxFlow.getFlow().get(edge);
                double edgeCapacity = graph.getEdgeWeight(edge);
                if(edgeCapacity-flowValue > epsilon)
                    processQueue.add(Graphs.getOppositeVertex(graph, edge, vertex));
            }
        }
    }


    /**
     * @return Returns the min cut partition containing the source, or null if
     * there was no call to computeMinCut(V source, V sink)
     */
    public Set<V> getSourcePartition()
    {
        return Collections.unmodifiableSet(minCut);
    }

    /**
     * Returns the min cut partition containing the sink
     *
     * @return returns the min cut partition containing the sink
     */
    public Set<V> getSinkPartition()
    {
        if (minCut == null) {
            return null;
        }
        Set<V> set = new HashSet<>(graph.vertexSet());
        set.removeAll(minCut);
        return Collections.unmodifiableSet(set);
    }

    /**
     * Get the cut weight. This is equal to the max s-t flow
     *
     * @return cut weight
     */
    public double getCutWeight()
    {
        return cutWeight;
    }

    /**
     * Let S be the set containing the source, and T be the set containing the
     * sink, i.e. T=V\S. This method returns the edges which have their tail in
     * S, and their head in T
     *
     * @return all edges which have their tail in S, and their head in T. If
     * computeMinCut(V source, V sink) has not been invoked, this method returns
     * null.
     */
    public Set<E> getCutEdges()
    {
        if (minCut == null)
            return null;

        Set<E> cutEdges = new HashSet<>();
        if(graph instanceof DirectedGraph) {
            DirectedGraph<V,E> directedGraph=(DirectedGraph<V,E>)graph;
            for (V vertex : minCut) {
                cutEdges.addAll(directedGraph.outgoingEdgesOf(vertex).stream().filter(edge -> !minCut.contains(graph.getEdgeTarget(edge))).collect(Collectors.toList()));
            }
        }else{
            cutEdges.addAll(graph.edgeSet().stream().filter(e -> minCut.contains(graph.getEdgeSource(e)) ^ minCut.contains(graph.getEdgeTarget(e))).collect(Collectors.toList()));
        }
        return Collections.unmodifiableSet(cutEdges);
    }

    /**
     * Returns the source of the last call
     *
     * @return source of last minCut call, null if there was no call
     */
    public V getCurrentSource()
    {
        return source;
    }

    /**
     * Returns the sink of the last call
     *
     * @return sink of last minCut call, null if there was no call
     */
    public V getCurrentSink()
    {
        return sink;
    }
}

// End MinSourceSinkCut.java
