/*
 * (C) Copyright 2016-2018, by Joris Kinable and Contributors.
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
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * This class computes an Equivalent Flow Tree (EFT) using the algorithm proposed by Dan Gusfield.
 * EFTs can be used to efficiently calculate the maximum flow for all pairs of vertices. The
 * algorithm is described in: <i>Gusfield, D, Very simple methods for all pairs network flow
 * analysis. SIAM Journal on Computing, 19(1), p142-155, 1990</i><br>
 * In an undirected graph, there exist $frac{n(n-1)}{2}$ different vertex pairs. This class computes
 * the maximum flow between each of these pairs efficiently by performing exactly $(n-1)$ minimum
 * $s-t$ cut computations. If your application requires fewer than $(n-1)$ flow calculations,
 * consider computing the maximum flows manually through {@link MaximumFlowAlgorithm}.
 *
 *
 * <p>
 * The runtime complexity of this class is $O((V-1)Q)$, where $Q$ is the runtime complexity of the
 * algorithm used to compute $s-t$ cuts in the graph. By default, this class uses the
 * {@link PushRelabelMFImpl} implementation to calculate minimum $s-t$ cuts. This class has a
 * runtime complexity of $O(V^3)$, resulting in a $O(V^4)$ runtime complexity for the overal
 * algorithm.
 *
 *
 * <p>
 * Note: this class performs calculations in a lazy manner. The EFT is not calculated until the
 * first invocation of {@link GusfieldEquivalentFlowTree#calculateMaximumFlow(Object, Object)} or
 * {@link GusfieldEquivalentFlowTree#getEquivalentFlowTree()}. Moreover, this class <em>only</em>
 * calculates the value of the maximum flow between a source-destination pair; it does not calculate
 * the corresponding flow per edge. If you need to know the exact flow through an edge, use one of
 * the alternative {@link MaximumFlowAlgorithm} implementations.
 *
 * <p>
 * Warning: EFTs do not allow you to calculate minimum cuts for all pairs of vertex! For that,
 * Gomory-Hu cut trees are required! Use the {@link GusfieldGomoryHuCutTree} implementation instead.
 *
 * <p>
 * This class does not support changes to the underlying graph. The behavior of this class is
 * undefined when the graph is modified after instantiating this class.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 */
public class GusfieldEquivalentFlowTree<V, E>
    implements
    MaximumFlowAlgorithm<V, E>
{

    /* Number of vertices in the graph */
    private final int N;
    /* Algorithm used to computed the Maximum s-t flows */
    private final MinimumSTCutAlgorithm<V, E> minimumSTCutAlgorithm;

    /* Data structures for computations */
    private List<V> vertexList = new ArrayList<>();
    private Map<V, Integer> indexMap = new HashMap<>();
    private int[] p; // See vector p in the paper description
    private int[] neighbors;

    /* Matrix containing the flow values for every s-t pair */
    private double[][] flowMatrix = null;

    private V lastInvokedSource = null;
    private V lastInvokedTarget = null;

    /**
     * Constructs a new GusfieldEquivalentFlowTree instance.
     * 
     * @param network input graph
     */
    public GusfieldEquivalentFlowTree(Graph<V, E> network)
    {
        this(network, MaximumFlowAlgorithmBase.DEFAULT_EPSILON);
    }

    /**
     * Constructs a new GusfieldEquivalentFlowTree instance.
     * 
     * @param network input graph
     * @param epsilon precision
     */
    public GusfieldEquivalentFlowTree(Graph<V, E> network, double epsilon)
    {
        this(network, new PushRelabelMFImpl<>(network, epsilon));
    }

    /**
     * Constructs a new GusfieldEquivalentFlowTree instance.
     * 
     * @param network input graph
     * @param minimumSTCutAlgorithm algorithm used to compute the minimum $s-t$ cuts
     */
    public GusfieldEquivalentFlowTree(
        Graph<V, E> network, MinimumSTCutAlgorithm<V, E> minimumSTCutAlgorithm)
    {
        GraphTests.requireUndirected(network);
        this.N = network.vertexSet().size();
        if (N < 2)
            throw new IllegalArgumentException("Graph must have at least 2 vertices");
        this.minimumSTCutAlgorithm = minimumSTCutAlgorithm;
        vertexList.addAll(network.vertexSet());
        for (int i = 0; i < vertexList.size(); i++)
            indexMap.put(vertexList.get(i), i);
    }

    /**
     * Runs the algorithm
     */
    private void calculateEquivalentFlowTree()
    {
        flowMatrix = new double[N][N];
        p = new int[N];
        neighbors = new int[N];

        for (int s = 1; s < N; s++) {
            int t = p[s];
            neighbors[s] = t;
            double flowValue =
                minimumSTCutAlgorithm.calculateMinCut(vertexList.get(s), vertexList.get(t));
            Set<V> sourcePartition = minimumSTCutAlgorithm.getSourcePartition(); // Set X in the
                                                                                 // paper
            for (int i = s; i < N; i++)
                if (sourcePartition.contains(vertexList.get(i)) && p[i] == t)
                    p[i] = s;

            // populate the flow matrix
            flowMatrix[s][t] = flowMatrix[t][s] = flowValue;
            for (int i = 0; i < s; i++)
                if (i != t)
                    flowMatrix[s][i] =
                        flowMatrix[i][s] = Math.min(flowMatrix[s][t], flowMatrix[t][i]);
        }
    }

    /**
     * Returns the Equivalent Flow Tree as an actual tree (graph). Note that this tree is not
     * necessarily unique. The edge weights represent the flow values/cut weights. This method runs
     * in $O(n)$ time
     * 
     * @return Equivalent Flow Tree
     */
    public SimpleWeightedGraph<V, DefaultWeightedEdge> getEquivalentFlowTree()
    {
        if (p == null) // Lazy invocation of the algorithm
            this.calculateEquivalentFlowTree();
        SimpleWeightedGraph<V, DefaultWeightedEdge> equivalentFlowTree =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(equivalentFlowTree, vertexList);
        for (int i = 1; i < N; i++) {
            DefaultWeightedEdge e =
                equivalentFlowTree.addEdge(vertexList.get(i), vertexList.get(neighbors[i]));
            equivalentFlowTree.setEdgeWeight(e, flowMatrix[i][neighbors[i]]);
        }
        return equivalentFlowTree;
    }

    /**
     * Unsupported operation
     * 
     * @param source source of the flow inside the network
     * @param sink sink of the flow inside the network
     *
     * @return nothing
     */
    @Override
    public MaximumFlow<E> getMaximumFlow(V source, V sink)
    {
        throw new UnsupportedOperationException(
            "Flows calculated via Equivalent Flow trees only provide a maximum flow value, not the exact flow per edge/arc.");
    }

    /**
     * Returns the Maximum flow between source and sink. The algorithm is only executed once;
     * successive invocations of this method will return in $O(1)$ time.
     * 
     * @param source source vertex
     * @param sink sink vertex
     * @return the Maximum flow between source and sink.
     */
    @Override
    public double calculateMaximumFlow(V source, V sink)
    {
        assert indexMap.containsKey(source) && indexMap.containsKey(sink);

        lastInvokedSource = source;
        lastInvokedTarget = sink;

        if (p == null) // Lazy invocation of the algorithm
            this.calculateEquivalentFlowTree();
        return flowMatrix[indexMap.get(source)][indexMap.get(sink)];
    }

    /**
     * Returns maximum flow value, that was calculated during last <tt>
     * calculateMaximumFlow</tt> call.
     * 
     * @return maximum flow value
     */
    @Override
    public double getMaximumFlowValue()
    {
        return calculateMaximumFlow(lastInvokedSource, lastInvokedTarget);
    }

    /**
     * Unsupported operation
     * 
     * @return nothing
     */
    @Override
    public Map<E, Double> getFlowMap()
    {
        throw new UnsupportedOperationException(
            "Flows calculated via Equivalent Flow trees only provide a maximum flow value, not the exact flow per edge/arc.");
    }

    /**
     * Unsupported operation
     * 
     * @param e edge
     * @return nothing
     */
    @Override
    public V getFlowDirection(E e)
    {
        throw new UnsupportedOperationException(
            "Flows calculated via Equivalent Flow trees only provide a maximum flow value, not the exact flow per edge/arc.");
    }

}
