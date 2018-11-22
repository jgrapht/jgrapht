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

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.util.*;
import java.lang.reflect.*;
import java.util.*;

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
 * to be defined as \[\frac{\sum\limits_{e \in E} w(e)}{\left|{V}\right|}\] and sets the weights of
 * the network as proposed in the above paper. For this case the weights of the network
 * must be chosen to be:
 * \[c_{ij}=w(ij)\forall \{i,j\}\in E\]
 * \[c_{it}=m+2g-d_i\forall i \in V\]
 * \[c_{si}=m\forall i \in V\]
 * where $m=\left|{E}\right|$ and $d_i$ is the degree of vertex $i$.
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
 * the internally used MinimumSTCutAlgorithm and $W$ is the sum all weights from $G$.
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
public class GoldbergMaximumDensitySubgraphAlgorithm<V,E> implements MaximumDensitySubgraphAlgorithm<V,E> {

    private double upper, lower, guess, epsilon;
    private final Graph<V, E> graph;
    private Graph<V,E> densestSubgraph;
    private Graph<V, DefaultWeightedEdge> currentNetwork;
    private Set<V> currentVertices;
    private V s,t;
    private MinimumSTCutAlgorithm<V, DefaultWeightedEdge> minSTCutAlg;

    /**
     * Constructor
     * @param alg instance of the type of subalgorithm to use
     * @param graph input for computation
     * @param s additional source vertex
     * @param t additional target vertex
     * @param epsilon to use for internal computation
     */
    public GoldbergMaximumDensitySubgraphAlgorithm(MinimumSTCutAlgorithm<V,E> alg, Graph<V, E> graph, V s, V t, double epsilon){
        if (graph.containsVertex(s) || graph.containsVertex(t)){
            throw new IllegalArgumentException("Source or sink vertex already in graph");
        }
        this.s = Objects.requireNonNull(s,"Source vertex is null");
        this.t = Objects.requireNonNull(t,"Sink vertex is null");
        this.graph = Objects.requireNonNull(graph, "Graph is null");
        this.epsilon = epsilon;
        this.guess = 0;
        this.lower = 0;
        this.currentNetwork = this.buildNetwork();
        this.upper = this.graph.edgeSet().stream().mapToDouble(this.graph::getEdgeWeight).sum();
        this.currentVertices = new HashSet<>();
        this.initializeNetwork();
        try {
            this.minSTCutAlg = this.setupMinCutAlg(alg);
        }
        catch (NoSuchMethodException | InstantiationException | IllegalAccessException| InvocationTargetException e){
            throw new IllegalArgumentException("Could not instantiate MinimumSTCutAlgorithm");
        }
    }

    /**
     * Helper method for constructing the internally used network
     */
    private Graph<V,DefaultWeightedEdge> buildNetwork(){
        return GraphTypeBuilder.<V,DefaultWeightedEdge> directed()
            .allowingMultipleEdges(true).allowingSelfLoops(true).weighted(true)
            .edgeSupplier(DefaultWeightedEdge::new).buildGraph();
    }

    /**
     * Updates network for next computation, e.g edges from v to t and from s to v
     **/
    private void updateNetwork(){
        for (V v : this.graph.vertexSet()){
            currentNetwork.setEdgeWeight(currentNetwork.getEdge(v,t), getEdgeWeightSink(v));
            currentNetwork.setEdgeWeight(currentNetwork.getEdge(s,v), getEdgeWeightSource());
        }
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
        for (V v : this.graph.vertexSet()){
            currentNetwork.addVertex(v);
            currentNetwork.addEdge(s,v);
            currentNetwork.addEdge(v,t);
        }
        for (E e : this.graph.edgeSet()){
            DefaultWeightedEdge e1 = currentNetwork.addEdge(this.graph.getEdgeSource(e), this.graph.getEdgeTarget(e));
            DefaultWeightedEdge e2 = currentNetwork.addEdge(this.graph.getEdgeTarget(e), this.graph.getEdgeSource(e));
            double weight = this.graph.getEdgeWeight(e);
            currentNetwork.setEdgeWeight(e1, weight);
            currentNetwork.setEdgeWeight(e2, weight);
        }
    }

    /**
     * Algorithm to compute max density subgraph
     * Performs binary search on the initial interval lower-upper until interval is smaller than epsilon
     * For to big epsilon calculate until a (first) solution is found, instead of returning an empty graph
     * @return max density subgraph of the graph
     */
    public Graph<V,E> calculateDensest(){
        Set<V> sourcePartition;
        while (Double.compare(upper-lower, this.epsilon)>=0) {
            guess = lower + ((upper - lower)) / 2;
            updateNetwork();
            minSTCutAlg.calculateMinCut(s, t);
            sourcePartition = minSTCutAlg.getSourcePartition();
            sourcePartition.remove(s);
            if (sourcePartition.isEmpty()) {
                upper = guess;
            } else {
                lower = guess;
                currentVertices = new HashSet<>(sourcePartition);
            }
        }
        this.densestSubgraph = new AsSubgraph<>(graph, currentVertices);
        return this.densestSubgraph;
    }

    /**
     * Computes density of a maximum density subgraph.
     *
     * @return the actual density of the maximum density subgraph
     */
    public double getDensity() throws NullPointerException{
        double sum = this.densestSubgraph.edgeSet().stream().mapToDouble(
            this.densestSubgraph::getEdgeWeight).sum();
        return sum/this.densestSubgraph.vertexSet().size();
    }

    /**
     * Wrapper for construction of MinimumSTCutAlgorithm
     * @param alg instance of the algorithm type to use
     * @return instance of MinimumSTCutAlgorithm for the constructed network
     */
    private MinimumSTCutAlgorithm<V, DefaultWeightedEdge> setupMinCutAlg(MinimumSTCutAlgorithm<V,E> alg)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
        InstantiationException
    {
        return TypeUtil.uncheckedCast(alg.getClass().getDeclaredConstructor(Graph.class).newInstance(this.currentNetwork));
    }

    /**
     * Getter for network weights of edges su for u in V
     * @return weight of the edge
     */
    private double getEdgeWeightSource(){
        return this.graph.edgeSet().size();
    }

    /**
     * Getter for network weights of edges ut for u in V
     * @param v of V
     * @return weight of the edge
     */
    private double getEdgeWeightSink(V v) {
        return this.graph.edgeSet().size() + 2*guess - this.graph.outgoingEdgesOf(v).stream().mapToDouble(
            this.graph::getEdgeWeight).sum();
    }
}
