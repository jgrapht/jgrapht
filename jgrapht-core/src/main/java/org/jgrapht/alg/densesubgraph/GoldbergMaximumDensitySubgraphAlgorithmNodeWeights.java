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
package org.jgrapht.alg.densesubgraph;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

/**
 * This class computes a maximum density subgraph based on the algorithm described
 * by Andrew Vladislav Goldberg in <a href="https://www2.eecs.berkeley.edu/Pubs/TechRpts/1984/CSD-84-171.pdf">
 * Finding Maximum Density Subgraphs</a>, 1984, University of Berkley.
 * <br>
 * The basic concept is to construct a network that can be used to compute the maximum density
 * subgraph using a binary search approach.
 * <p>
 * In the simplest case of an unweighted graph $G=(V,E)$ the density of $G$ can be defined to be
 * \[\frac{\left|{E}\right|}{\left|{V}\right|}\]
 * Therefore it is in this case equal to half the average vertex degree.
 * For directed graphs one can consider the graph as undirected.
 * </p>
 * The idea of the algorithm is to construct a network based on the input graph $G=(V,E)$ and some
 * guess $g$ for the density. This network $N=(V_N, E_N)$ is constructed as follows:
 * \[V_N=V\cup {s,t}\]
 * \[E_N=\{(i,j)| \{i,j\} \in E\} \cup \{(s,i)| i\in V\} \cup \{(i,t)| i \in V\}\]
 * <br>
 * This variant of the algorithm assumes the density of a positive real-weighted graph G=(V,E)
 * to be defined as \[\frac{\sum\limits_{e \in E} w(e) + \sum\limits_{v \in V} w(v)}{\left|{V}\right|}\]
 * and sets the weights of the network as proposed in the above paper. For this case the weights of the network
 * must be chosen to be:
 * \[c_{ij}=w(ij)\forall \{i,j\}\in E\]
 * \[c_{it}=m'+2g-d_i-2w(i)\forall i \in V\]
 * \[c_{si}=m'\forall i \in V\]
 * where $m'$ is such that all weights are positive and $d_i$ is the degree of vertex $i$ and
 * $w(v)$ is the weight of vertex $v$.
 * <br>
 * As seen later these weights depend on the definition of the density. Therefore these weights and
 * the following applies to the definition of density from above.
 * <p>
 * Using this network one can show some important properties, that are essential
 * for the algorithm to work.
 * The capacity of a s-t of N is given by:
 * \[C(S,T) = m\left|{V}\right| + 2\left|{V_1}\right|\left(g - D_1\right)\] where
 * $V_1 \dot{\cup} V_2=V$ and $V_1 = S\setminus \{s\}, V_2=  T\setminus \{t\}$ and $D_1$ shall be
 * the density of the induced subgraph of $V_1$ regarding $G$.
 * </p>
 * <p>
 * Especially important is the capacity of minimum s-t Cut. Using the above equation, one can derive
 * that given a minimum s-t Cut of $N$ and the maximum density of $G$ to be $D$, then $g\geq D$ if
 * $V_1=\emptyset$,otherwise $g\leq D$. Moreover the induced subgraph of $V_1$ regarding G is
 * guaranteed to have density greater $g$ or it can be used to proof that there can't exist any
 * subgraph of $G$ greater $g$.
 * Based on this property one can use a binary search approach to shrink the possible interval which
 * contains the solution.
 * </p>
 * <p>
 * Because the density is per definition guaranteed to be rational, the distance of 2 possible
 * solutions for the maximum density can't be smaller than $\frac{1}{W(W-1)}$. This means shrinking
 * the binary search interval to this size, the correct solution is found.
 * The runtime can in this case be given by $O(M(n,n+m)\log{W}$, where $M(n,m)$ is the runtime of
 * the internally used MinimumSTCutAlgorithm and $W$ is the sum all edge and vertex weights from $G$.
 * </p>
 *
 * <p>
 * As the variants including edge weights are only guaranteed to terminate for
 * integer edge weights, instead of using the natural termination property, the algorithm needs to
 * be called with $\varepsilon$ . The computation then ensures, that the returned maximum density only
 * differs at most $\varepsilon$ from the correct solution.
 * </p>
 *
 * @param <V> Type of vertices
 * @param <E> Type of edges
 *
 * @author Andre Immig
 */
public class GoldbergMaximumDensitySubgraphAlgorithmNodeWeights<V extends Pair<?,Double>,E> extends GoldbergMaximumDensitySubgraphAlgorithmBase<V,E>{

    /**
     * Constructor
     * @param alg instance of the type of subalgorithm to use
     * @param graph input for computation
     * @param s additional source vertex
     * @param t additional target vertex
     * @param epsilon to use for internal computation
     */
    public GoldbergMaximumDensitySubgraphAlgorithmNodeWeights(MinimumSTCutAlgorithm<V,E> alg, Graph<V, E> graph, V s, V t, double epsilon){
        super(alg, graph, s,t, true, epsilon);
        this.upper =  this.graph.edgeSet().stream().mapToDouble(this.graph::getEdgeWeight).sum();
        for (V v: this.graph.vertexSet()){
            this.upper += v.getSecond();
        }
        if (this.graph.vertexSet().isEmpty() && this.graph.edgeSet().isEmpty()){
            this.densestSubgraph = new AsSubgraph<>(this.graph, null);
        }
    }

    /**
     * Computes density of a maximum density subgraph.
     *
     * @return the actual density of the maximum density subgraph
     */
    public double getDensity(){
        if (this.densestSubgraph == null){
            this.calculateDensest();
        }
        int n = this.densestSubgraph.vertexSet().size();
        if (n == 0){
            return 0;
        }
        double sum = this.densestSubgraph.edgeSet().stream().mapToDouble(
            this.densestSubgraph::getEdgeWeight).sum();
        for (V v: this.densestSubgraph.vertexSet()){
            sum+=v.getSecond();
        }
        return sum/this.densestSubgraph.vertexSet().size();
    }

    /**
     * Getter for network weights of edges su for u in V
     * @return weight of the edge
     */
    @Override
    protected double getEdgeWeightSource(V v){
        return 0;
    }

    /**
     * Getter for network weights of edges ut for u in V
     * @param v of V
     * @return weight of the edge
     */
    @Override
    protected double getEdgeWeightSink(V v) {
        return 2*guess - this.graph.outgoingEdgesOf(v).stream().mapToDouble(
            this.graph::getEdgeWeight).sum() - 2*v.getSecond();
    }
}
