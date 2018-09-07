/*
 * (C) Copyright 2015-2018, by Fabian Späh and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.isomorphism;

import org.jgrapht.*;

import java.util.*;

/**
 * This class represents the order on the graph vertices. There are also some helper-functions for
 * receiving outgoing/incoming edges, etc.
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 */

class GraphOrdering<V, E>
{
    private Graph<V, E> graph;

    private Map<V, Integer> mapVertexToOrder;
    private ArrayList<V> mapOrderToVertex;
    private int vertexCount;

    private int[][] outgoingEdges;
    private int[][] incomingEdges;
    private Boolean[][] adjMatrix;

    private boolean cacheEdges;

    /**
     * @param graph the graph to be ordered
     * @param orderByDegree should the vertices be ordered by their degree. This speeds up the VF2
     *        algorithm.
     * @param cacheEdges if true, the class creates a adjacency matrix and two arrays for incoming
     *        and outgoing edges for fast access.
     */
    public GraphOrdering(Graph<V, E> graph, boolean orderByDegree, boolean cacheEdges)
    {
        this.graph = graph;
        this.cacheEdges = cacheEdges;

        List<V> vertexSet = new ArrayList<>(graph.vertexSet());
        if (orderByDegree) {
            vertexSet.sort(new GeneralVertexDegreeComparator<>(graph));
        }

        vertexCount = vertexSet.size();
        mapVertexToOrder = new HashMap<>();
        mapOrderToVertex = new ArrayList<>(vertexCount);

        if (cacheEdges) {
            outgoingEdges = new int[vertexCount][];
            incomingEdges = new int[vertexCount][];
            adjMatrix = new Boolean[vertexCount][vertexCount];
        }

        Integer i = 0;
        for (V vertex : vertexSet) {
            mapVertexToOrder.put(vertex, i++);
            mapOrderToVertex.add(vertex);
        }
    }

    /**
     * @param graph the graph to be ordered
     */
    public GraphOrdering(Graph<V, E> graph)
    {
        this(graph, false, true);
    }

    /**
     * @return returns the number of vertices in the graph.
     */
    public int getVertexCount()
    {
        return this.vertexCount;
    }

    /**
     * @param vertexNumber the number which identifies the vertex $v$ in this order.
     *
     * @return the identifying numbers of all vertices which are connected to $v$ by an edge
     *         outgoing from $v$.
     */
    public int[] getOutEdges(int vertexNumber)
    {
        if (cacheEdges && (outgoingEdges[vertexNumber] != null)) {
            return outgoingEdges[vertexNumber];
        }

        V v = getVertex(vertexNumber);
        Set<E> edgeSet = graph.outgoingEdgesOf(v);

        int[] vertexArray = new int[edgeSet.size()];
        int i = 0;

        for (E edge : edgeSet) {
            V source = graph.getEdgeSource(edge), target = graph.getEdgeTarget(edge);
            vertexArray[i++] = mapVertexToOrder.get(source.equals(v) ? target : source);
        }

        if (cacheEdges) {
            outgoingEdges[vertexNumber] = vertexArray;
        }

        return vertexArray;
    }

    /**
     * @param vertexNumber the number which identifies the vertex $v$ in this order.
     *
     * @return the identifying numbers of all vertices which are connected to $v$ by an edge
     *         incoming to $v$.
     */
    public int[] getInEdges(int vertexNumber)
    {
        if (cacheEdges && (incomingEdges[vertexNumber] != null)) {
            return incomingEdges[vertexNumber];
        }

        V v = getVertex(vertexNumber);
        Set<E> edgeSet = graph.incomingEdgesOf(v);

        int[] vertexArray = new int[edgeSet.size()];
        int i = 0;

        for (E edge : edgeSet) {
            V source = graph.getEdgeSource(edge), target = graph.getEdgeTarget(edge);
            vertexArray[i++] = mapVertexToOrder.get(source.equals(v) ? target : source);
        }

        if (cacheEdges) {
            incomingEdges[vertexNumber] = vertexArray;
        }

        return vertexArray;
    }

    /**
     * @param v1Number the number of the first vertex $v_1$
     * @param v2Number the number of the second vertex $v_2$
     *
     * @return exists the edge from $v_1$ to $v_2$
     */
    public boolean hasEdge(int v1Number, int v2Number)
    {
        V v1, v2;
        Boolean containsEdge = null;

        if (cacheEdges) {
            containsEdge = adjMatrix[v1Number][v2Number];
        }

        if (!cacheEdges || (containsEdge == null)) {
            v1 = getVertex(v1Number);
            v2 = getVertex(v2Number);
            containsEdge = graph.containsEdge(v1, v2);
        }

        if (cacheEdges && (adjMatrix[v1Number][v2Number] == null)) {
            adjMatrix[v1Number][v2Number] = containsEdge;
        }

        return containsEdge;
    }

    /**
     * be careful: there's no check against an invalid vertexNumber
     *
     * @param vertexNumber the number identifying the vertex $v$
     *
     * @return $v$
     */
    public V getVertex(int vertexNumber)
    {
        return mapOrderToVertex.get(vertexNumber);
    }

    /**
     * @param v1Number the number identifying the vertex $v_1$
     * @param v2Number the number identifying the vertex $v_2$
     *
     * @return the edge from $v_1$ to $v_2$
     */
    public E getEdge(int v1Number, int v2Number)
    {
        V v1 = getVertex(v1Number), v2 = getVertex(v2Number);

        return graph.getEdge(v1, v2);
    }

    public int getVertexNumber(V v)
    {
        return mapVertexToOrder.get(v);
    }

    public int[] getEdgeNumbers(E e)
    {
        V v1 = graph.getEdgeSource(e), v2 = graph.getEdgeTarget(e);

        int[] edge = new int[2];
        edge[0] = mapVertexToOrder.get(v1);
        edge[1] = mapVertexToOrder.get(v2);

        return edge;
    }

    public Graph<V, E> getGraph()
    {
        return graph;
    }

    private static class GeneralVertexDegreeComparator<V2>
        implements
        Comparator<V2>
    {
        private Graph<V2, ?> graph;

        GeneralVertexDegreeComparator(Graph<V2, ?> graph)
        {
            this.graph = graph;
        }

        @Override
        public int compare(V2 v1, V2 v2)
        {
            return graph.edgesOf(v1).size() - graph.edgesOf(v2).size();
        }
    }
}

// End GraphOrdering.java
