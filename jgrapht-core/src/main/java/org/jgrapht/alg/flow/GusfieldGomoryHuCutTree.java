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
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * This class computes a Gomory-Hu tree (GHT) using the algorithm proposed by Dan Gusfield. For a
 * definition of GHTs, refer to: <i>Gomory, R., Hu, T. Multi-terminal network flows. Journal of the
 * Socieity for Industrial and Applied mathematics, 9(4), p551-570, 1961.</i> GHTs can be used to
 * efficiently query the maximum flows and minimum cuts for all pairs of vertices. The algorithm is
 * described in: <i>Gusfield, D, Very simple methods for all pairs network flow analysis. SIAM
 * Journal on Computing, 19(1), p142-155, 1990</i><br>
 * In an undirected graph, there exist $\frac{n(n-1)}{2}$ different vertex pairs. This class
 * computes the maximum flow/minimum cut between each of these pairs efficiently by performing
 * exactly $(n-1)$ minimum $s-t$ cut computations. If your application needs fewer than $n-1$
 * flow/cut computations, consider computing the maximum flows/minimum cuts manually through
 * {@link MaximumFlowAlgorithm}/{@link MinimumSTCutAlgorithm}.
 *
 *
 * <p>
 * The runtime complexity of this class is $O((V-1)Q)$, where $Q$ is the runtime complexity of the
 * algorithm used to compute $s-t$ cuts in the graph. By default, this class uses the
 * {@link PushRelabelMFImpl} implementation to calculate minimum s-t cuts. This class has a runtime
 * complexity of $O(V^3)$, resulting in a $O(V^4)$ runtime complexity for the overall algorithm.
 *
 *
 * <p>
 * Note: this class performs calculations in a lazy manner. The GHT is not calculated until the
 * first invocation of {@link GusfieldGomoryHuCutTree#calculateMaximumFlow(Object, Object)} or
 * {@link GusfieldGomoryHuCutTree#getGomoryHuTree()}. Moreover, this class <em>only</em> calculates
 * the value of the maximum flow between a source-destination pair; it does not calculate the
 * corresponding flow per edge. If you need to know the exact flow through an edge, use one of the
 * alternative {@link MaximumFlowAlgorithm} implementations.
 *
 * <p>
 * In contrast to an Equivalent Flow Tree ({@link GusfieldEquivalentFlowTree}), Gomory-Hu trees also
 * provide all minimum cuts for all pairs of vertices!
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
public class GusfieldGomoryHuCutTree<V, E>
    implements
    MaximumFlowAlgorithm<V, E>,
    MinimumSTCutAlgorithm<V, E>
{

    private final Graph<V, E> network;
    /* Number of vertices in the graph */
    private final int N;
    /* Algorithm used to computed the Maximum $s-t$ flows */
    private final MinimumSTCutAlgorithm<V, E> minimumSTCutAlgorithm;

    /* Data structures for computations */
    private List<V> vertexList = new ArrayList<>();
    private Map<V, Integer> indexMap = new HashMap<>();
    private int[] p; // See vector p in the paper description
    private double[] fl; // See vector fl in the paper description

    /* Matrix containing the flow values for every $s-t$ pair */
    private double[][] flowMatrix = null;

    private V lastInvokedSource = null;
    private V lastInvokedTarget = null;
    private Set<V> sourcePartitionLastInvokedSource = null;
    private SimpleWeightedGraph<V, DefaultWeightedEdge> gomoryHuTree = null;

    /**
     * Constructs a new GusfieldEquivalentFlowTree instance.
     * 
     * @param network input graph
     */
    public GusfieldGomoryHuCutTree(Graph<V, E> network)
    {
        this(network, MaximumFlowAlgorithmBase.DEFAULT_EPSILON);
    }

    /**
     * Constructs a new GusfieldEquivalentFlowTree instance.
     * 
     * @param network input graph
     * @param epsilon precision
     */
    public GusfieldGomoryHuCutTree(Graph<V, E> network, double epsilon)
    {
        this(network, new PushRelabelMFImpl<>(network, epsilon));
    }

    /**
     * Constructs a new GusfieldEquivalentFlowTree instance.
     * 
     * @param network input graph
     * @param minimumSTCutAlgorithm algorithm used to compute the minimum s-t cuts
     */
    public GusfieldGomoryHuCutTree(
        Graph<V, E> network, MinimumSTCutAlgorithm<V, E> minimumSTCutAlgorithm)
    {
        this.network = GraphTests.requireUndirected(network);
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
    private void calculateGomoryHuTree()
    {
        flowMatrix = new double[N][N];
        p = new int[N];
        fl = new double[N];

        for (int s = 1; s < N; s++) {
            int t = p[s];
            double flowValue =
                minimumSTCutAlgorithm.calculateMinCut(vertexList.get(s), vertexList.get(t));
            Set<V> sourcePartition = minimumSTCutAlgorithm.getSourcePartition(); // Set X in the
                                                                                 // paper
            fl[s] = flowValue;

            for (int i = 0; i < N; i++)
                if (i != s && sourcePartition.contains(vertexList.get(i)) && p[i] == t)
                    p[i] = s;
            if (sourcePartition.contains(vertexList.get(p[t]))) {
                p[s] = p[t];
                p[t] = s;
                fl[s] = fl[t];
                fl[t] = flowValue;
            }

            // populate the flow matrix
            flowMatrix[s][t] = flowMatrix[t][s] = flowValue;
            for (int i = 0; i < s; i++)
                if (i != t)
                    flowMatrix[s][i] =
                        flowMatrix[i][s] = Math.min(flowMatrix[s][t], flowMatrix[t][i]);
        }
    }

    /**
     * Returns the Gomory-Hu Tree as an actual tree (graph). Note that this tree is not necessarily
     * unique. The edge weights represent the flow values/cut weights. This method runs in $O(n)$
     * time.
     * 
     * @return Gomory-Hu Tree
     */
    public SimpleWeightedGraph<V, DefaultWeightedEdge> getGomoryHuTree()
    {
        if (p == null) // Lazy invocation of the algorithm
            this.calculateGomoryHuTree();

        // Compute the tree from scratch. Since we compute a new tree, the user is free to modify
        // this tree.
        SimpleWeightedGraph<V, DefaultWeightedEdge> gomoryHuTree =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(gomoryHuTree, vertexList);
        for (int i = 1; i < N; i++) {
            Graphs.addEdge(gomoryHuTree, vertexList.get(i), vertexList.get(p[i]), fl[i]);
        }

        return gomoryHuTree;
    }

    /* ================== Maximum Flow ================== */

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
            "Flows calculated via Gomory-Hu trees only provide a maximum flow value, not the exact flow per edge/arc.");
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
        sourcePartitionLastInvokedSource = null;
        gomoryHuTree = null;

        if (p == null) // Lazy invocation of the algorithm
            this.calculateGomoryHuTree();
        return flowMatrix[indexMap.get(source)][indexMap.get(sink)];
    }

    /**
     * Returns rhw maximum flow value, that was calculated during the last
     * {@link #calculateMaximumFlow(Object, Object)} call.
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
            "Flows calculated via Gomory-Hu trees only provide a maximum flow value, not the exact flow per edge/arc.");
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
            "Flows calculated via Gomory-Hu trees only provide a maximum flow value, not the exact flow per edge/arc.");
    }

    /* ================== Minimum Cut ================== */

    @Override
    public double calculateMinCut(V source, V sink)
    {
        return calculateMaximumFlow(source, sink);
    }

    /**
     * Calculates the minimum cut in the graph, that is, the minimum cut over all $s-t$ pairs. The
     * same result can be obtained with the {@link org.jgrapht.alg.StoerWagnerMinimumCut}
     * implementation. After invoking this method, the source/sink partitions corresponding to the
     * minimum cut can be queried through the {@link #getSourcePartition()} and
     * {@link #getSinkPartition()} methods. After computing the Gomory-Hu Cut tree, this method runs
     * in $O(N)$ time.
     * 
     * @return weight of the minimum cut in the graph
     */
    public double calculateMinCut()
    {
        if (this.gomoryHuTree == null)
            this.gomoryHuTree = this.getGomoryHuTree();
        DefaultWeightedEdge cheapestEdge = gomoryHuTree
            .edgeSet().stream().min(Comparator.comparing(gomoryHuTree::getEdgeWeight))
            .orElseThrow(() -> new RuntimeException("graph is empty?!"));
        lastInvokedSource = gomoryHuTree.getEdgeSource(cheapestEdge);
        lastInvokedTarget = gomoryHuTree.getEdgeTarget(cheapestEdge);
        sourcePartitionLastInvokedSource = null;
        return gomoryHuTree.getEdgeWeight(cheapestEdge);
    }

    @Override
    public double getCutCapacity()
    {
        return calculateMinCut(lastInvokedSource, lastInvokedTarget);
    }

    @Override
    public Set<V> getSourcePartition()
    {
        if (sourcePartitionLastInvokedSource != null)
            return sourcePartitionLastInvokedSource;

        if (this.gomoryHuTree == null)
            this.gomoryHuTree = this.getGomoryHuTree();

        Set<DefaultWeightedEdge> pathEdges =
            this.findPathBetween(gomoryHuTree, lastInvokedSource, lastInvokedTarget);
        DefaultWeightedEdge cheapestEdge =
            pathEdges.stream().min(Comparator.comparing(gomoryHuTree::getEdgeWeight)).orElseThrow(
                () -> new RuntimeException("path is empty?!"));

        // Remove the selected edge from the gomoryHuTree graph. The resulting graph consists of 2
        // components
        V source = gomoryHuTree.getEdgeSource(cheapestEdge);
        V target = gomoryHuTree.getEdgeTarget(cheapestEdge);
        gomoryHuTree.removeEdge(cheapestEdge);

        // Return the vertices in the component with the source vertex
        sourcePartitionLastInvokedSource =
            new ConnectivityInspector<>(gomoryHuTree).connectedSetOf(lastInvokedSource);

        // Restore the internal tree structure by putting the edge back
        gomoryHuTree.addEdge(source, target, cheapestEdge);

        return sourcePartitionLastInvokedSource;
    }

    /**
     * BFS method to find the edges in the shortest path from a source to a target vertex in a tree
     * graph.
     * 
     * @param tree input graph
     * @param source source
     * @param target target
     * @return edges constituting the shortest path between source and target
     */
    private Set<DefaultWeightedEdge> findPathBetween(
        SimpleWeightedGraph<V, DefaultWeightedEdge> tree, V source, V target)
    {
        boolean[] visited = new boolean[vertexList.size()];
        Map<V, V> predecessorMap = new HashMap<V, V>();
        Queue<V> queue = new LinkedList<V>();
        queue.add(source);

        boolean found = false;
        while (!found && !queue.isEmpty()) {
            V next = queue.poll();
            for (V v : Graphs.neighborListOf(tree, next)) {
                if (!visited[indexMap.get(v)]) {
                    predecessorMap.put(v, next);
                    queue.add(v);
                }
                if (v == target) {
                    found = true;
                    break;
                }
            }
            visited[indexMap.get(next)] = true;
        }

        Set<DefaultWeightedEdge> edges = new LinkedHashSet<>();
        V v = target;
        while (v != source) {
            V pred = predecessorMap.get(v);
            edges.add(tree.getEdge(v, pred));
            v = pred;
        }
        return edges;
    }

    @Override
    public Set<V> getSinkPartition()
    {
        Set<V> sinkPartition = new LinkedHashSet<>(network.vertexSet());
        sinkPartition.removeAll(this.getSourcePartition());
        return sinkPartition;
    }

    @Override
    public Set<E> getCutEdges()
    {
        Set<E> cutEdges = new LinkedHashSet<>();
        Set<V> sourcePartion = this.getSourcePartition();
        for (E e : network.edgeSet()) {
            V source = network.getEdgeSource(e);
            V sink = network.getEdgeTarget(e);
            if (sourcePartion.contains(source) ^ sourcePartion.contains(sink))
                cutEdges.add(e);
        }
        return cutEdges;
    }
}
