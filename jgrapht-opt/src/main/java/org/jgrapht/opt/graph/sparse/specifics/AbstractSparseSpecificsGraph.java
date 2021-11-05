/*
 * (C) Copyright 2021-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.opt.graph.sparse.specifics;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.GraphType;
import org.jgrapht.graph.AbstractGraph;

/**
 * Helper class to ease the implementation of different sparse graphs with different backends.
 * 
 * @author Dimitrios Michail
 *
 * @param <S> the type of the graph specifics
 */
public class AbstractSparseSpecificsGraph<S extends SparseGraphSpecifics>
    extends
    AbstractGraph<Integer, Integer>
{
    protected static final String UNMODIFIABLE = "this graph is unmodifiable";
    protected S specifics;

    /**
     * Constructor
     * 
     * @param specificsSupplier a specifics supplier
     */
    public AbstractSparseSpecificsGraph(Supplier<S> specificsSupplier)
    {
        this.specifics = Objects.requireNonNull(specificsSupplier.get());
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
        return specifics.containsEdge(e);
    }

    @Override
    public boolean containsVertex(Integer v)
    {
        return specifics.containsVertex(v);
    }

    @Override
    public Set<Integer> edgeSet()
    {
        return specifics.edgeSet();
    }

    @Override
    public int degreeOf(Integer vertex)
    {
        return (int) specifics.degreeOf(vertex);
    }

    @Override
    public Set<Integer> edgesOf(Integer vertex)
    {
        return specifics.edgesOf(vertex);
    }

    @Override
    public int inDegreeOf(Integer vertex)
    {
        return (int) specifics.inDegreeOf(vertex);
    }

    @Override
    public Set<Integer> incomingEdgesOf(Integer vertex)
    {
        return specifics.incomingEdgesOf(vertex);
    }

    @Override
    public int outDegreeOf(Integer vertex)
    {
        return (int) specifics.outDegreeOf(vertex);
    }

    @Override
    public Set<Integer> outgoingEdgesOf(Integer vertex)
    {
        return specifics.outgoingEdgesOf(vertex);
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
        return specifics.vertexSet();
    }

    @Override
    public Integer getEdgeSource(Integer e)
    {
        return specifics.getEdgeSource(e);
    }

    @Override
    public Integer getEdgeTarget(Integer e)
    {
        return specifics.getEdgeTarget(e);
    }

    @Override
    public GraphType getType()
    {
        return specifics.getType();
    }

    @Override
    public double getEdgeWeight(Integer e)
    {
        return specifics.getEdgeWeight(e);
    }

    @Override
    public void setEdgeWeight(Integer e, double weight)
    {
        specifics.setEdgeWeight(e, weight);
    }

    @Override
    public Integer getEdge(Integer sourceVertex, Integer targetVertex)
    {
        return specifics.getEdge(sourceVertex, targetVertex);
    }

    @Override
    public Set<Integer> getAllEdges(Integer sourceVertex, Integer targetVertex)
    {
        return specifics.getAllEdges(sourceVertex, targetVertex);
    }

}
