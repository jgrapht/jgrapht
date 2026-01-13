/*
 * (C) Copyright 2020-2023, by Azim Barhoumi, Paul Enjalbert and Contributors.
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
package org.jgrapht.alg.connectivity;

import java.util.*;
import java.util.function.Function;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.flow.EdmondsKarpMFImpl;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.graph.AsUnweightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 * This class implements the computation, for a graph, of both 
 * <a href="https://en.wikipedia.org/wiki/K-edge-connected_graph"> edge connectivity and </a>
 * <a href="https://en.wikipedia.org/wiki/K-vertex-connected_graph"> vertex connectivity </a>.
 * 
 * A graph with n vertices is k-edge connected when k is smaller than n and if the size of the smallest subset of edges
 * which disconnects the graph is equal to k. It's the same definition for vertex connectivity, by replacing edges with vertices.
 * 
 * This implementation uses a maximum flow algorithm and computes the number of different paths between two 
 * vertices with this algorithm. By default, this implementation uses the Edmonds-Karp max-flow algorithm.
 * The algorithms implemented are based on
 * <a href="https://www.cse.msu.edu/~cse835/Papers/Graph_connectivity_revised.pdf"> this document </a>.
 * 
 * The worst-case complexity depends on the number of calls to this max-flow algorithm. All algorithms make O(n) calls to 
 * this flow algorithm. However, this number tries to be as low as possible. For further details on the exact 
 * number of calls, see the <a href="https://www.cse.msu.edu/~cse835/Papers/Graph_connectivity_revised.pdf"> document </a>
 * on which this implementation is based.
 * 
 * This algorithm works with both directed and undirected networks (Only SimpleGraph and 
 * SimpleDirectedGraph are tested). The algorithm doesn't have internal synchronization, 
 * thus any concurrent network modification has undefined behavior.
 * 
 * The inspector methods work in a lazy fashion: no computations are performed unless immediately
 * necessary. Computations are done once and results are cached within this class for future needs.
 * 
 * Further improvements of flow-based algorithms can be found in this paper: D. W. Matula, “Determining edge connectivity
 * in O(mn),” Proceedings of the 28th Symp. on Foundations of Computer Science, (1987),   pp. 249‐251.   
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Azim Barhoumi
 * @author Paul Enjalbert
 */

public class KConnectivityFlowAlgorithm<V, E>
{
    /**
     * Value of vertex connectivity, for avoid double computing
     */
    private int vertexConnectivity = -1;
    /**
     * The tested graph
     */
    private final Graph<V, E> graph;
    /**
     * The modified graph to compute vertex connectivity
     */
    private final Graph<Integer, DefaultEdge> vertexNetwork;
    /**
     * The hash maps for correspondence between real graph and vertex graph
     */
    private final Map<V, Integer> vertexMapping = new HashMap<>();
    
    /**
     * Value of edge connectivity, for avoid double computing
     */
    private int edgeConnectivity = -1;
    /**
     * minimum degree value.
     */
    private int minDegreeValue = 0;
    /**
     * minimum degree vertex.
     */
    private V minDegreeVertex = null;
    
    /**
     * Maximum flow algorithm for computing edge connectivity
     */
    private final MaximumFlowAlgorithm<V, E> edgeFlowAlgorithm;
    /**
     * Maximum flow algorithm for computing vertex connectivity
     */
    private final MaximumFlowAlgorithm<Integer, DefaultEdge> vertexFlowAlgorithm;
    
    /**
     * Constructs a k-connectivity algorithm for the given graph.
     * This 
     *
     * @param graph the input graph
     */
    public KConnectivityFlowAlgorithm(Graph<V, E> graph)
    {
        this(graph,
                (g) -> new EdmondsKarpMFImpl<V, E>(g),
                (g) -> new EdmondsKarpMFImpl<Integer, DefaultEdge>(g));
    }
    
    /**
     * Constructs a k-connectivity algorithm for the given graph.
     * Use algorithms given by the suppliers for edge and vertex flow algorithm
     * A supplier is a function which takes a graph and construct the algorithm which will be used
     * @param graph the input graph
     * @param edgeFlowAlgorithmSupplier edge maximum flow algorithm supplier
     * @param vertexFlowAlgorithmSupplier vertex maximum flow algorithm supplier
     */
    public KConnectivityFlowAlgorithm(Graph<V, E> graph
            , Function<Graph<V, E>, MaximumFlowAlgorithm<V, E>> edgeFlowAlgorithmSupplier
            , Function<Graph<Integer, DefaultEdge>, MaximumFlowAlgorithm<Integer, DefaultEdge>> vertexFlowAlgorithmSupplier)
    {
        this.graph = new AsUnweightedGraph<>(graph);
        this.edgeFlowAlgorithm = edgeFlowAlgorithmSupplier.apply(this.graph);
       
        this.vertexNetwork = buildVertexNetwork(graph);
        this.vertexFlowAlgorithm = vertexFlowAlgorithmSupplier.apply(this.vertexNetwork);
       
        this.setMinDegreeVertex();
    }

    /**
     * Computes the edge connectivity of the graph.
     *
     * @return the edge connectivity of the graph
     */
    public int getEdgeConnectivity()
    {
        if (edgeConnectivity == -1) {
            edgeConnectivity = graph.getType().isDirected()
                ? computeDirectedEdgeConnectivity()
                : computeUndirectedEdgeConnectivity();
        }
        return edgeConnectivity;
    }

    /**
     * Computes the vertex connectivity of the graph.
     *
     * @return the vertex connectivity of the graph
     */
    public int getVertexConnectivity()
    {
        if(vertexConnectivity == -1)
        {
            int connectivityFromNonNeighbors = computeConnectivityFromNonNeighbors(minDegreeVertex);
            int connectivityFromNeighbors = computeConnectivityFromNeighbors(minDegreeVertex);

            vertexConnectivity = Math.min(connectivityFromNonNeighbors, connectivityFromNeighbors);
        }
        return vertexConnectivity;
    }
    
    /**
     * Return if the graph is {@code k} edge connected, use {@link #getEdgeConnectivity() Compute Edge Connectivity Edge}
     * methods to have the result
     * @param k 
     * @return if the the graph is k edge connect or not
     */
    public boolean isKEdgeConnected(int k)
    {
        return k <= getEdgeConnectivity();
    }
    
    /**
     * Return if the graph is {@code k} vertex connected, use {@link #computeVertexConnectivity() Compute Edge Connectivity Edge}
     * methods to have the result
     * @param k
     * @return if the the graph is k vertex connect or not
     */
    public boolean isKVertexConnected(int k)
    {
        return k <= getVertexConnectivity();
    }

    private int computeConnectivityFromNonNeighbors(V minDegreeVertex)
    {
        int minConnectivity = Integer.MAX_VALUE;

        for (V vertex : graph.vertexSet()) {
            if (vertex.equals(minDegreeVertex)) continue;

            if (!graph.containsEdge(minDegreeVertex, vertex)) {
                minConnectivity = Math.min(minConnectivity, computeLocalVertexConnectivity(minDegreeVertex, vertex));
            }

            if (!graph.containsEdge(vertex, minDegreeVertex)) {
                minConnectivity = Math.min(minConnectivity, computeLocalVertexConnectivity(vertex, minDegreeVertex));
            }
        }

        return minConnectivity;
    }

    private int computeConnectivityFromNeighbors(V minDegreeVertex)
    {
        List<V> neighbors = Graphs.successorListOf(graph, minDegreeVertex);
        int minConnectivity = Integer.MAX_VALUE;

        for (int i = 0; i < neighbors.size(); i++) {
            for (int j = i + 1; j < neighbors.size(); j++) {
                V vertex1 = neighbors.get(i);
                V vertex2 = neighbors.get(j);

                if (!graph.containsEdge(vertex1, vertex2)) {
                    minConnectivity = Math.min(minConnectivity, computeLocalVertexConnectivity(vertex1, vertex2));
                }

                if (!graph.containsEdge(vertex2, vertex1)) {
                    minConnectivity = Math.min(minConnectivity, computeLocalVertexConnectivity(vertex2, vertex1));
                }
            }
        }
        
        //If all vertex are neighbors, i.e the graph is complete
        if(Integer.MAX_VALUE == minConnectivity){
            minConnectivity = graph.vertexSet().size() - 1;
        }

        return minConnectivity;
    }

    /**
     * Compute the edge connectivity for an undirected graph
     * @return edge connectivity
     */
    private int computeUndirectedEdgeConnectivity()
    {
        Set<V> dominatingSet = getDominatingSet();
        Iterator<V> iterator = dominatingSet.iterator();

        if (!iterator.hasNext()) return 0;
        V referenceVertex = iterator.next();

        int minConnectivity = Integer.MAX_VALUE;

        while (iterator.hasNext()) {
            V nextVertex = iterator.next();
            int localConnectivity = computeLocalEdgeConnectivity(referenceVertex, nextVertex);
            minConnectivity = Math.min(minConnectivity, localConnectivity);
        }

        return Math.min(minConnectivity, minDegreeValue);
    }

    /**
     * Compute the edge connectivity for an directed graph
     * @return edge connectivity
     */
    private int computeDirectedEdgeConnectivity()
    {
        List<V> vertices = new ArrayList<>(graph.vertexSet());

        int minConnectivity = Integer.MAX_VALUE;
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                minConnectivity = Math.min(minConnectivity, computeLocalEdgeConnectivity(vertices.get(i), vertices.get(j)));
            }
        }

        return minConnectivity;
    }

    private int computeLocalEdgeConnectivity(V source, V target)
    {
        return (int) edgeFlowAlgorithm.getMaximumFlowValue(source, target);
    }

    private int computeLocalVertexConnectivity(V source, V target)
    {
        Integer sourceId = vertexMapping.get(source);
        Integer targetId = vertexMapping.get(target);

        return (int) vertexFlowAlgorithm.getMaximumFlowValue(sourceId + 1, targetId);
    }

    private Set<V> getDominatingSet()
    {
        Set<V> dominatingSet = new HashSet<>();
        Set<V> vertices = new HashSet<>(graph.vertexSet());
        Set<V> coveredVertices = new HashSet<>();

        for (V vertex : vertices) {
            if (!coveredVertices.contains(vertex)) {
                dominatingSet.add(vertex);
                coveredVertices.add(vertex);
                coveredVertices.addAll(Graphs.neighborListOf(graph, vertex));
            }
        }

        return dominatingSet;
    }

    private void setMinDegreeVertex()
    {
        if (minDegreeVertex == null) {
            int minDegree = Integer.MAX_VALUE;
            V minDegreeVertex = null;

            for (V vertex : graph.vertexSet()) {
                int degree = graph.degreeOf(vertex);
                if (degree < minDegree) {
                    minDegree = degree;
                    minDegreeVertex = vertex;
                }
            }

            this.minDegreeVertex = minDegreeVertex;
            this.minDegreeValue = minDegree; 
        }
    }

    private Graph<Integer, DefaultEdge> buildVertexNetwork(Graph<V, E> graph)
    {
        Graph<Integer, DefaultEdge> network = new SimpleDirectedGraph<>(DefaultEdge.class);

        int vertexCounter = 0;
        for (V vertex : graph.vertexSet()) {
            vertexMapping.put(vertex, vertexCounter);
            network.addVertex(vertexCounter);
            network.addVertex(vertexCounter + 1);
            network.addEdge(vertexCounter, vertexCounter + 1);
            vertexCounter += 2;
        }

        for (V source : graph.vertexSet()) {
            for (V target : Graphs.successorListOf(graph, source)) {
                network.addEdge(vertexMapping.get(source) + 1, vertexMapping.get(target));
            }
        }

        return network;
    }
}
