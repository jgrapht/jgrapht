/*
 * (C) Copyright 2018-2018, by Andre Immig and Contributors.
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
package org.jgrapht.alg.dense;

import org.jgrapht.Graph;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.interfaces.MinimumSTCutAlgorithm;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import java.util.*;

/**
 * Abstract class for calculating maximum density subgraph based on TO-DO
 * Methods that depend on the density should be overridden by subclass
 * @param <V> Type of vertices
 * @param <E> Type of edges
 */
public abstract class MaximumDensitySubgraphAlgorithmBase<V,E>{

    double upper, lower, guess;
    protected final int n,m;
    Graph<V, E> original, densestSubgraph;
    private Graph<V, E> currentNetwork;
    private HashSet<V> currentVertices;
    private V s,t;
    private MinimumSTCutAlgorithm<V, E> minSTCutAlg;
    private boolean checkWeights;

    /**
     * Constructor
     * @param edgeClass type of edges used
     * @param g input for computation
     * @param s additional source vertex
     * @param t additional target vertex
     * @param checkWeights if true implementation will enforce all internal weights to be positive
     */
    protected MaximumDensitySubgraphAlgorithmBase(Class<? extends E> edgeClass, Graph<V, E> g, V s, V t, boolean checkWeights){
        this.checkWeights = checkWeights;
        this.s = s;
        this.t = t;
        this.original = g;
        this.m = g.edgeSet().size();
        this.n = g.vertexSet().size();
        this.initBinarySearchInterval();
        this.currentNetwork = new DirectedWeightedPseudograph<>(edgeClass);
        this.currentVertices = new HashSet<>();
        this.initializeNetwork();
        this.minSTCutAlg = this.setupMinCutAlg();
    }

    /**
     * Performs minCut Computation on network
     * @return vertex Set S of a minimal S-T cut
     */
    private Set<V> getSink() {
        minSTCutAlg.calculateMinCut(s, t);
        Set<V> sourcePartition = minSTCutAlg.getSourcePartition();
        sourcePartition.remove(s);
        return sourcePartition;
    }

    /**
     * Updates network for next computation, e.g edges from v to t and from s to v
     */
    private void updateNetwork(){
        for (V v : this.original.vertexSet()){
            currentNetwork.setEdgeWeight(currentNetwork.getEdge(v,t), getEdgeWeightSink(v));
            currentNetwork.setEdgeWeight(currentNetwork.getEdge(s,v), getEdgeWeightSource(v));
        }
        //if needed make all capacities non-negative
        if (this.checkWeights){
            double minCapacity = getMinimalCapacity();
            if (minCapacity < 0){
                E e;
                for (V v : this.original.vertexSet()){
                    e = currentNetwork.getEdge(v,t);
                    currentNetwork.setEdgeWeight(e, currentNetwork.getEdgeWeight(e)- minCapacity);
                    e = currentNetwork.getEdge(s,t);
                    currentNetwork.setEdgeWeight(e, currentNetwork.getEdgeWeight(e)- minCapacity);
                }
            }
        }
    }

    /**
     * @return the minimal capacity of all edges vt and sv
     */
    private double getMinimalCapacity(){
        /*
        double min = 0;
        double w;
        for (V v : this.original.vertexSet()){
            w =  currentNetwork.getEdgeWeight(currentNetwork.getEdge(v,t));
            if (w < min){
                min = w;
            }
            w = currentNetwork.getEdgeWeight(currentNetwork.getEdge(s,v));
            if (w < min){
                min = w;
            }
        }
        return min;
        */
        double a = this.original.vertexSet().stream().mapToDouble(v -> currentNetwork.getEdgeWeight(currentNetwork.getEdge(v,t))).min().getAsDouble();
        double b = this.original.vertexSet().stream().mapToDouble(v -> currentNetwork.getEdgeWeight(currentNetwork.getEdge(s,v))).min().getAsDouble();
        return a < b ? a : b;
    }

    /**
     * Initializes network (only once) for Min-Cut computation
     * Adds s,t to vertex set
     * Adds every v in V to vertex set
     * Adds edge sv and vt for each v in V to edge set
     * Adds every edge uv and vu from E to edge set
     * Sets edge weights for all edges from E
     */
    private void initializeNetwork(){
        currentNetwork.addVertex(s);
        currentNetwork.addVertex(t);
        for (V v : this.original.vertexSet()){
            currentNetwork.addVertex(v);
            currentNetwork.addEdge(s,v);
            currentNetwork.addEdge(v,t);
        }
        for (E e : this.original.edgeSet()){
            E e1 = currentNetwork.addEdge(this.original.getEdgeSource(e), this.original.getEdgeTarget(e));
            E e2 = currentNetwork.addEdge(this.original.getEdgeTarget(e), this.original.getEdgeSource(e));
            double weight = this.getInternalEdgeWeight(e);
            currentNetwork.setEdgeWeight(e1, weight);
            currentNetwork.setEdgeWeight(e2, weight);
        }
    }

    /**
     * Algorithm to compute max density subgraph
     * Performs binary search on the initial interval lower-upper until interval is smaller than epsilon
     * For to big epsilon calculate until a (first) solution is found, instead of returning an empty graph
     * @param epsilon accuracy for the binary search
     * @return max density subgraph of original graph
     */
    public Graph<V,E> calculateDensest(double epsilon){
        Set<V> sourcePartition;
        while (Double.compare(upper-lower, epsilon)>=0) {
            guess = (upper + lower) / 2;
            updateNetwork();
            sourcePartition = this.getSink();
            if (sourcePartition.isEmpty()) {
                upper = guess;
            } else {
                lower = guess;
                currentVertices = new HashSet<>();
                currentVertices.addAll(sourcePartition);
            }
        }
        this.densestSubgraph = new AsSubgraph<>(original, currentVertices);
        return this.densestSubgraph;
    }

    private MinimumSTCutAlgorithm<V,E> setupMinCutAlg(){
        return new PushRelabelMFImpl<>(this.currentNetwork);
    }

    /**
     * Template method to be implemented by subclass
     */
    protected abstract void initBinarySearchInterval();

    /**
     * Getter for network weights of edges uv in V
     * @param e edge of G
     * @return weight of the edge
     */
    protected abstract double getInternalEdgeWeight(E e);

    /**
     * Getter for network weights of edges su for u in V
     * @param vertex of V
     * @return weight of the edge
     */
    protected abstract double getEdgeWeightSource(V vertex);

    /**
     * Getter for network weights of edges ut for u in V
     * @param vertex of V
     * @return weight of the edge
     */
    protected abstract double getEdgeWeightSink(V vertex);

    /**
     * Getter for final density result
     * @return maximum density calculated
     * @throws NullPointerException maximum density subgraph has not been calculated before
     */
    public abstract double getDensity() throws NullPointerException;
}
