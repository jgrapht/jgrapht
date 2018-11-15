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
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * Class for calculating the maximum density subgraph of a weighted Graph
 * Uses the binary search approach combined with min-Cut-computations
 * described in 'Finding a maximum density subgraph, Goldberg'
 * @param <V> Type of vertices
 * @param <E> Type of edges
 */
public class MaximumDensitySubgraphAlgorithm<V,E extends DefaultWeightedEdge> extends MaximumDensitySubgraphAlgorithmBase<V,E> {

    /**
     * Constructor
     * @param edgeClass type of edges used
     * @param g input for computation
     * @param s additional source vertex
     * @param t additional target vertex
     */
    public MaximumDensitySubgraphAlgorithm(Class<? extends E> edgeClass, Graph<V, E> g, V s, V t) {
        super(edgeClass,g,s,t,false);
    }

    @Override
    protected void initBinarySearchInterval(){
        this.guess = 0;
        this.lower = 0;
        this.upper = this.original.edgeSet().stream().mapToDouble(
            e-> this.original.getEdgeWeight(e)).sum();
    }

    @Override
    protected double getInternalEdgeWeight(E e) {
        return this.original.getEdgeWeight(e);
    }

    @Override
    protected double getEdgeWeightSource(V v){
        return m;
    }

    @Override
    protected double getEdgeWeightSink(V v) {
        return m + 2*guess + this.original.outgoingEdgesOf(v).stream().mapToDouble(
            e->this.original.getEdgeWeight(e)).sum();
    }

    /**
     * Calls calculateDensest with default epsilon
     * @return exact maximum density subgraph for integer weights, for non integer weights use epsilon method.
     */
    public Graph<V,E> calculateDensestExact(){
        return this.calculateDensest((double) 1/(this.n*(this.n-1)));
    }

    /**
     * Calculates maximal density of the original graph
     * @throws NullPointerException if densest Subgraph has not been calculated before
     * @return density of given graph
     */
    @Override
    public double getDensity() throws NullPointerException{
        if (this.densestSubgraph == null){
            throw new NullPointerException("First need to calculate densest Subgraph");
        }
        double sum = this.densestSubgraph.edgeSet().stream().mapToDouble(
            e ->this.densestSubgraph.getEdgeWeight(e)).sum();
        return sum/this.densestSubgraph.vertexSet().size();
    }
}
