/*
 * (C) Copyright 2019-2019, by Dimitrios Michail and Contributors.
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
package org.jgrapht.opt.graph.sparse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultGraphType;

/**
 * Sparse undirected graph.
 *
 * <p>
 * Assuming the graph has $n$ vertices, the vertices are numbered from $0$ to $n-1$. Similarly,
 * edges are numbered from $0$ to $m-1$ where $m$ is the total number of edges.
 * 
 * <p>
 * It stores the boolean incidence matrix of the graph (rows are vertices and columns are edges) as
 * Compressed Sparse Rows (CSR). In order to also support constant time source and target lookups
 * from an edge identifier we also store the transposed of the incidence matrix again in compressed
 * sparse rows format. This is a classic format for write-once read-many use cases. Thus, the graph
 * is unmodifiable.
 * 
 * @author Dimitrios Michail
 */
public class SparseUndirectedGraph
    extends
    AbstractGraph<Integer, Integer>
{
    protected static final String UNMODIFIABLE = "this graph is unmodifiable";

    protected CSRBooleanMatrix incidenceMatrix;
    protected CSRBooleanMatrix incidenceMatrixT;

    /**
     * Create a new graph from an edge list
     * 
     * @param numVertices number of vertices
     * @param edges edge list
     */
    public SparseUndirectedGraph(int numVertices, List<Pair<Integer, Integer>> edges)
    {
        List<Pair<Integer, Integer>> nonZeros = new ArrayList<>();
        List<Pair<Integer, Integer>> nonZerosTranspose = new ArrayList<>();

        int eIndex = 0;
        for (Pair<Integer, Integer> e : edges) {
            nonZeros.add(Pair.of(e.getFirst(), eIndex));
            nonZerosTranspose.add(Pair.of(eIndex, e.getFirst()));
            nonZeros.add(Pair.of(e.getSecond(), eIndex));
            nonZerosTranspose.add(Pair.of(eIndex, e.getSecond()));
            eIndex++;
        }
        incidenceMatrix = new CSRBooleanMatrix(numVertices, edges.size(), nonZeros);
        incidenceMatrixT = new CSRBooleanMatrix(edges.size(), numVertices, nonZerosTranspose);
    }

    @Override
    public Supplier<Integer> getVertexSupplier()
    {
        return null;
    }

    @Override
    public Supplier<Integer> getEdgeSupplier()
    {
        return null;
    }

    @Override
    public Integer addEdge(Integer sourceVertex, Integer targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean addEdge(Integer sourceVertex, Integer targetVertex, Integer e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public Integer addVertex()
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean addVertex(Integer v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean containsEdge(Integer e)
    {
        return e >= 0 && e < incidenceMatrix.columns();
    }

    @Override
    public boolean containsVertex(Integer v)
    {
        return v >= 0 && v < incidenceMatrix.rows();
    }

    @Override
    public Set<Integer> edgeSet()
    {
        return new CompleteIntegerSet(incidenceMatrix.columns());
    }

    @Override
    public int degreeOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return incidenceMatrix.nonZeros(vertex);
    }

    @Override
    public Set<Integer> edgesOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return incidenceMatrix.nonZerosSet(vertex);
    }

    @Override
    public int inDegreeOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return incidenceMatrix.nonZeros(vertex);
    }

    @Override
    public Set<Integer> incomingEdgesOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return incidenceMatrix.nonZerosSet(vertex);
    }

    @Override
    public int outDegreeOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return incidenceMatrix.nonZeros(vertex);
    }

    @Override
    public Set<Integer> outgoingEdgesOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return incidenceMatrix.nonZerosSet(vertex);
    }

    @Override
    public Integer removeEdge(Integer sourceVertex, Integer targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean removeEdge(Integer e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean removeVertex(Integer v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public Set<Integer> vertexSet()
    {
        return new CompleteIntegerSet(incidenceMatrix.rows());
    }

    @Override
    public GraphType getType()
    {
        return new DefaultGraphType.Builder()
            .undirected().weighted(false).modifiable(false).allowMultipleEdges(true)
            .allowSelfLoops(true).build();
    }

    @Override
    public double getEdgeWeight(Integer e)
    {
        return Graph.DEFAULT_EDGE_WEIGHT;
    }

    @Override
    public void setEdgeWeight(Integer e, double weight)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public Integer getEdgeSource(Integer e)
    {
        assertEdgeExist(e);
        return incidenceMatrixT.nonZerosPositionIterator(e).next();
    }

    @Override
    public Integer getEdgeTarget(Integer e)
    {
        assertEdgeExist(e);
        Iterator<Integer> it = incidenceMatrixT.nonZerosPositionIterator(e);
        it.next();
        return it.next();
    }

    @Override
    public Integer getEdge(Integer sourceVertex, Integer targetVertex)
    {
        if (sourceVertex < 0 || sourceVertex >= incidenceMatrix.rows()) {
            return null;
        }
        if (targetVertex < 0 || targetVertex >= incidenceMatrix.rows()) {
            return null;
        }

        Iterator<Integer> it = incidenceMatrix.nonZerosPositionIterator(sourceVertex);
        while (it.hasNext()) {
            int eId = it.next();

            int v = getEdgeSource(eId);
            int u = getEdgeTarget(eId);

            if (v == sourceVertex && u == targetVertex || v == targetVertex && u == sourceVertex) {
                return eId;
            }
        }
        return null;
    }

    @Override
    public Set<Integer> getAllEdges(Integer sourceVertex, Integer targetVertex)
    {
        if (sourceVertex < 0 || sourceVertex >= incidenceMatrix.rows()) {
            return null;
        }
        if (targetVertex < 0 || targetVertex >= incidenceMatrix.rows()) {
            return null;
        }

        Set<Integer> result = new HashSet<>();
        Iterator<Integer> it = incidenceMatrix.nonZerosPositionIterator(sourceVertex);
        while (it.hasNext()) {
            int eId = it.next();

            int v = getEdgeSource(eId);
            int u = getEdgeTarget(eId);

            if (v == sourceVertex && u == targetVertex || v == targetVertex && u == sourceVertex) {
                result.add(eId);
            }
        }
        return result;
    }

    /**
     * Ensures that the specified vertex exists in this graph, or else throws exception.
     *
     * @param v vertex
     * @return <code>true</code> if this assertion holds.
     * @throws IllegalArgumentException if specified vertex does not exist in this graph.
     */
    protected boolean assertVertexExist(Integer v)
    {
        if (v >= 0 && v < incidenceMatrix.rows()) {
            return true;
        } else {
            throw new IllegalArgumentException("no such vertex in graph: " + v.toString());
        }
    }

    /**
     * Ensures that the specified edge exists in this graph, or else throws exception.
     *
     * @param e edge
     * @return <code>true</code> if this assertion holds.
     * @throws IllegalArgumentException if specified edge does not exist in this graph.
     */
    protected boolean assertEdgeExist(Integer e)
    {
        if (e >= 0 && e < incidenceMatrix.columns()) {
            return true;
        } else {
            throw new IllegalArgumentException("no such edge in graph: " + e.toString());
        }
    }

}
