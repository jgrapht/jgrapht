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
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import java.util.*;
import java.util.stream.*;

/**
 * This abstract base class computes a maximum density subgraph based on the algorithm described
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
 * Additionally one defines the following weights for the network:
 * \[c_{ij}=1 \forall \{i,j\}\in E\]
 * \[c_{si}=m \forall i \in V\]
 * \[c_{it}=m+2g-d_i \forall i \in V\]
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
 * solutions for the maximum density can't be smaller than $\frac{1}{n(n-1)}$. This means shrinking
 * the binary search interval to this size, the correct solution is found.
 * The runtime can in this case be given by $O(M(n,n+m)\log{n}$, where $M(n,m)$ is the runtime of
 * the internally used MinimumSTCutAlgorithm.
 * </p>
 *
 * <p>
 * Similar the same argument can be applied for other definitions of density, where the network
 * needs to be adapted accordingly. Some generalizations can be found in the paper.
 * As these more general variants including edge weights are only guaranteed to terminate for
 * integer edge weights, instead of using the natural termination property, the algorithm needs to
 * be called with $\varepsilon$ . The computation then ensures, that the returned maximum density only
 * differs at most $\varepsilon$ from the correct solution. This is why subclasses of this class might
 * have a little different runtime analysis, regarding the $\log{n}$ part.
 * </p>
 *
 * @param <V> Type of vertices
 * @param <E> Type of edges
 *
 * @author Andre Immig
 */
public abstract class MaximumDensitySubgraphAlgorithmBase<V,E> implements MaximumDensitySubgraphAlg<V,E> {

    double upper, lower, guess;
    protected final int n,m;
    protected final Graph<V, E> graph;
    protected Graph<V,E> densestSubgraph;
    private Graph<V, E> currentNetwork;
    private Set<V> currentVertices;
    private V s,t;
    private MinimumSTCutAlgorithm<V, E> minSTCutAlg;
    private boolean checkWeights;

    /**
     * Constructor
     * @param edgeClass type of edges used
     * @param graph input for computation
     * @param s additional source vertex
     * @param t additional target vertex
     * @param checkWeights if true implementation will enforce all internal weights to be positive
     */
    protected MaximumDensitySubgraphAlgorithmBase(Class<? extends E> edgeClass, Graph<V, E> graph, V s, V t, boolean checkWeights){
        if (graph.containsVertex(s) || graph.containsVertex(t)){
            throw new IllegalArgumentException("Source or sink vertex already in graph");
        }
        this.checkWeights = checkWeights;
        this.s = Objects.requireNonNull(s,"Source vertex is null");
        this.t = Objects.requireNonNull(t,"Sink vertex is null");
        this.graph = Objects.requireNonNull(graph, "Graph is null");
        this.m = this.graph.edgeSet().size();
        this.n = this.graph.vertexSet().size();
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
     * Enforces positivity on network weights if specified
     **/
    private void updateNetwork(){
        for (V v : this.graph.vertexSet()){
            currentNetwork.setEdgeWeight(currentNetwork.getEdge(v,t), getEdgeWeightSink(v));
            currentNetwork.setEdgeWeight(currentNetwork.getEdge(s,v), getEdgeWeightSource(v));
        }
        if (this.checkWeights){
            double minCapacity = getMinimalCapacity();
            if (minCapacity < 0){
                E e;
                for (V v : this.graph.vertexSet()){
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
        DoubleStream sourceWeights = this.graph.vertexSet().stream().mapToDouble(
            v -> currentNetwork.getEdgeWeight(currentNetwork.getEdge(v,t)));
        DoubleStream sinkWeights = this.graph.vertexSet().stream().mapToDouble(
            v -> currentNetwork.getEdgeWeight(currentNetwork.getEdge(s,v)));
        OptionalDouble min = DoubleStream.concat(sourceWeights, sinkWeights).min();
        return min.isPresent() ? min.getAsDouble() : 0;
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
            E e1 = currentNetwork.addEdge(this.graph.getEdgeSource(e), this.graph.getEdgeTarget(e));
            E e2 = currentNetwork.addEdge(this.graph.getEdgeTarget(e), this.graph.getEdgeSource(e));
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
     * @return max density subgraph of the graph
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
        this.densestSubgraph = new AsSubgraph<>(graph, currentVertices);
        return this.densestSubgraph;
    }

    /**
     * Wrapper for construction of MinimumSTCutAlgorithm
     * @return instance of MinimumSTCutAlgorithm for the constructed network
     */
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
