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

import org.jgrapht.GraphType;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.Triple;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultGraphType;

/**
 * A sparse unmodifiable undirected and weighted graph.
 * 
 * @author Dimitrios Michail
 */
public class SparseUndirectedWeightedGraph
    extends
    AbstractGraph<Integer, Integer>
{
    private static final String UNMODIFIABLE = "this graph is unmodifiable";

    private CSRBooleanMatrix incidenceMatrix;
    private CSRDoubleMatrix incidenceMatrixT;

    public SparseUndirectedWeightedGraph(
        int numVertices, List<Triple<Integer, Integer, Double>> edges)
    {
        List<Pair<Integer, Integer>> nonZeros = new ArrayList<>();
        List<Triple<Integer, Integer, Double>> nonZerosTranspose = new ArrayList<>();
        int eIndex = 0;
        for (Triple<Integer, Integer, Double> e : edges) {
            nonZeros.add(Pair.of(e.getFirst(), eIndex));
            nonZeros.add(Pair.of(e.getSecond(), eIndex));
            nonZerosTranspose.add(Triple.of(eIndex, e.getFirst(), e.getThird()));
            nonZerosTranspose.add(Triple.of(eIndex, e.getSecond(), e.getThird()));
            eIndex++;
        }
        incidenceMatrix = new CSRBooleanMatrix(numVertices, edges.size(), nonZeros);
        incidenceMatrixT = new CSRDoubleMatrix(edges.size(), numVertices, nonZerosTranspose);
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
        Iterator<Integer> it = incidenceMatrix.nonZerosIterator(sourceVertex);
        while (it.hasNext()) {
            int eId = it.next();

            Iterator<Integer> vIt = incidenceMatrixT.nonZerosPositionIterator(eId);
            int v = vIt.next();
            int u = vIt.next();

            if (v == sourceVertex && u == targetVertex || v == targetVertex && u == sourceVertex) {
                result.add(eId);
            }
        }
        return result;
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

        Iterator<Integer> it = incidenceMatrix.nonZerosIterator(sourceVertex);
        while (it.hasNext()) {
            int eId = it.next();

            Iterator<Integer> vIt = incidenceMatrixT.nonZerosPositionIterator(eId);
            int v = vIt.next();
            int u = vIt.next();

            if (v == sourceVertex && u == targetVertex || v == targetVertex && u == sourceVertex) {
                return eId;
            }
        }
        return null;
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
        return new IntegerSet(incidenceMatrix.columns());
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
        return incidenceMatrix.rowSet(vertex);
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
        return incidenceMatrix.rowSet(vertex);
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
        return incidenceMatrix.rowSet(vertex);
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
        return new IntegerSet(incidenceMatrix.rows());
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
    public GraphType getType()
    {
        return new DefaultGraphType.Builder()
            .undirected().weighted(true).modifiable(false).allowMultipleEdges(true)
            .allowSelfLoops(true).build();
    }

    @Override
    public double getEdgeWeight(Integer e)
    {
        assertEdgeExist(e);
        return incidenceMatrixT.nonZerosIterator(e).next().getSecond();
    }

    @Override
    public void setEdgeWeight(Integer e, double weight)
    {
        assertEdgeExist(e);
        incidenceMatrixT.setNonZeros(e, weight);
    }

    protected boolean assertVertexExist(Integer v)
    {
        if (v >= 0 && v < incidenceMatrix.rows()) {
            return true;
        } else {
            throw new IllegalArgumentException("no such vertex in graph: " + v.toString());
        }
    }

    protected boolean assertEdgeExist(Integer e)
    {
        if (e >= 0 && e < incidenceMatrixT.rows()) {
            return true;
        } else {
            throw new IllegalArgumentException("no such edge in graph: " + e.toString());
        }
    }

}
