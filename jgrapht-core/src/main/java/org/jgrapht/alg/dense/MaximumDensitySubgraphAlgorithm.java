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

/**
 * This class computes a maximum density subgraph based on the algorithm described
 * by A. V. Goldberg in "Finding Maximum Density Subgraphs", 1984,University of Berkley,
 * https://www2.eecs.berkeley.edu/Pubs/TechRpts/1984/CSD-84-171.pdf
 * <br>
 * The basic concept is to construct a network that can be used to compute the maximum density
 * subgraph using a binary search approach. For more details see
 * {@link MaximumDensitySubgraphAlgorithmBase}.
 * <br>
 * This variant of the algorithm assumes the density of a positive real-weighted graph G=(V,E)
 * to be defined as $frac{\sum\limits_{e \in E} w(e)}{\left|{V}\right|}$ and sets the weights of
 * the network as proposed in the above paper. For this case the weights of the network from
 * {@link MaximumDensitySubgraphAlgorithmBase} must be chosen to be:
 * <ul>
 * <li>$c_{ij}=w(ij)\forall \{i,j\}\in E$</li>
 * <li>$c_{it}=m+2g-d_i\forall i \in V$, where $d_i$ is the degree of vertex i. </li>
 * <li>$c_{si}=m \forall i \in V$, where $m=\left|{E}\right|$</li>
 * </ul>
 * According to the base class the runtime of this algorithm is $O(M(n,n+m)\log{\frac{W}{\eps}})$,
 * where $W$ is the sum of all weights of $G$, $\eps$ is the given accuracy and $M(n,m)$ is the
 * runtime of the internally used MinimumSTCutAlgorithm.
 *
 * @param <V> Type of vertices
 * @param <E> Type of edges
 *
 * @author Andre Immig
 */
public class MaximumDensitySubgraphAlgorithm<V,E> extends MaximumDensitySubgraphAlgorithmBase<V,E> {

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
        this.upper = this.graph.edgeSet().stream().mapToDouble(this.graph::getEdgeWeight).sum();
    }

    @Override
    protected double getInternalEdgeWeight(E e) {
        return this.graph.getEdgeWeight(e);
    }

    @Override
    protected double getEdgeWeightSource(V v){
        return m;
    }

    @Override
    protected double getEdgeWeightSink(V v) {
        return m + 2*guess - this.graph.outgoingEdgesOf(v).stream().mapToDouble(
            this.graph::getEdgeWeight).sum();
    }

    /**
     * Calls calculateDensest with default epsilon
     * @return exact maximum density subgraph for integer weights, for non integer weights use epsilon method.
     */
    public Graph<V,E> calculateDensestExact(){
        return this.calculateDensest((double) 1/(this.n*(this.n-1)));
    }

    @Override
    public double getDensity() throws NullPointerException{
        if (this.densestSubgraph == null){
            throw new NullPointerException("First need to calculate densest Subgraph");
        }
        double sum = this.densestSubgraph.edgeSet().stream().mapToDouble(
            this.densestSubgraph::getEdgeWeight).sum();
        return sum/this.densestSubgraph.vertexSet().size();
    }
}
