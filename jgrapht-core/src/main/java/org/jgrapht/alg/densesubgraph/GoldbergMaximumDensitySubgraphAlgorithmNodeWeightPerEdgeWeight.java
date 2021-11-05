/*
 * (C) Copyright 2018-2021, by Andre Immig and Contributors.
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
import org.jgrapht.alg.flow.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

import java.util.function.*;

/**
 * This class computes a maximum density subgraph based on the algorithm described by Andrew
 * Vladislav Goldberg in <a href="https://www2.eecs.berkeley.edu/Pubs/TechRpts/1984/CSD-84-171.pdf">
 * Finding Maximum Density Subgraphs</a>, 1984, University of Berkley. <br>
 * The basic concept is to construct a network that can be used to compute the maximum density
 * subgraph using a binary search approach.
 * <p>
 * This variant of the algorithm assumes the density of a positive real edge and vertex weighted
 * graph G=(V,E) to be defined as \[\frac{\sum\limits_{e \in E} w(e)}{\sum\limits_{v \in V} w(v)}\]
 * and sets the weights of the network from {@link GoldbergMaximumDensitySubgraphAlgorithmBase} as
 * proposed in the above paper. For this case the weights of the network must be chosen to be:
 * \[c_{ij}=w(ij)\,\forall \{i,j\}\in E\] \[c_{it}=m'+2gw(i)-d_i\,\forall i \in V\]
 * \[c_{si}=m'\,\forall i \in V\] where $m'$ is such, that all weights are positive and $d_i$ is the
 * degree of vertex $i$ and $w(v)$ is the weight of vertex $v$. <br>
 * All the math to prove the correctness of these weights is the same as in
 * {@link GoldbergMaximumDensitySubgraphAlgorithmBase}. <br>
 * <p>
 * Because the density is per definition guaranteed to be rational, the distance of 2 possible
 * solutions for the maximum density can't be smaller than $\frac{1}{W(W-1)}$. This means shrinking
 * the binary search interval to this size, the correct solution is found. The runtime can in this
 * case be given by $O(M(n,n+m)\log{W})$, where $M(n,m)$ is the runtime of the internally used
 * {@link MinimumSTCutAlgorithm} and $W$ is the sum of all edge weights from $G$.
 * </p>
 *
 * @param <V> Type of vertices
 * @param <E> Type of edges
 *
 * @author Andre Immig
 */
public class GoldbergMaximumDensitySubgraphAlgorithmNodeWeightPerEdgeWeight<
    V extends Pair<?, Double>, E>
    extends
    GoldbergMaximumDensitySubgraphAlgorithmBase<V, E>
{

    /**
     * Constructor
     * 
     * @param graph input for computation
     * @param s additional source vertex
     * @param t additional target vertex
     * @param epsilon to use for internal computation
     * @param algFactory function to construct the subalgorithm
     */
    public GoldbergMaximumDensitySubgraphAlgorithmNodeWeightPerEdgeWeight(
        Graph<V, E> graph, V s, V t, double epsilon, Function<Graph<V, DefaultWeightedEdge>,
            MinimumSTCutAlgorithm<V, DefaultWeightedEdge>> algFactory)
    {
        super(graph, s, t, true, epsilon, algFactory);
    }

    /**
     * Convenience constructor that uses PushRelabel as default MinimumSTCutAlgorithm
     * 
     * @param graph input for computation
     * @param s additional source vertex
     * @param t additional target vertex
     * @param epsilon to use for internal computation
     */
    public GoldbergMaximumDensitySubgraphAlgorithmNodeWeightPerEdgeWeight(
        Graph<V, E> graph, V s, V t, double epsilon)
    {
        this(graph, s, t, epsilon, PushRelabelMFImpl::new);
    }

    @Override
    protected double computeDensityNumerator(Graph<V, E> g)
    {
        return g.edgeSet().stream().mapToDouble(g::getEdgeWeight).sum();
    }

    @Override
    protected double computeDensityDenominator(Graph<V, E> g)
    {
        return g.vertexSet().stream().mapToDouble(v -> v.getSecond()).sum();
    }

    @Override
    protected double getEdgeWeightFromSourceToVertex(V v)
    {
        return 0;
    }

    @Override
    protected double getEdgeWeightFromVertexToSink(V v)
    {
        return 2 * guess * v.getSecond()
            - this.graph.outgoingEdgesOf(v).stream().mapToDouble(this.graph::getEdgeWeight).sum();
    }
}
