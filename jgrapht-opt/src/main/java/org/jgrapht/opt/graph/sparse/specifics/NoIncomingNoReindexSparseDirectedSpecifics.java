/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jgrapht.GraphType;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultGraphType;

/**
 * Specifics for a sparse directed graph which does not re-index the edges and does not support
 * incoming edges. No reindexing means that the edges are numbered in increasing order using the
 * order that the user loads the edges. Support for incoming edges is not provided. All methods that
 * need access to incoming edges throw an exception.
 *
 * @author Dimitrios Michail
 */
public class NoIncomingNoReindexSparseDirectedSpecifics
    implements
    SparseGraphSpecifics
{
    protected static final String UNMODIFIABLE = "this graph is unmodifiable";
    protected static final String NO_INCOMING = "this graph does not support incoming edges";

    /**
     * Source vertex of edge
     */
    protected int[] source;

    /**
     * Target vertex of edge
     */
    protected int[] target;

    /**
     * Incidence matrix with outgoing edges
     */
    protected CSRBooleanMatrix outIncidenceMatrix;

    /**
     * Create a new graph from an edge list.
     * 
     * @param numVertices the number of vertices
     * @param numEdges the number of edges
     * @param edges a supplier of an edge list
     */
    public NoIncomingNoReindexSparseDirectedSpecifics(
        int numVertices, int numEdges, Supplier<Stream<Pair<Integer, Integer>>> edges)
    {
        final int m = numEdges;
        source = new int[m];
        target = new int[m];

        List<Pair<Integer, Integer>> outgoing = new ArrayList<>(m);
        int[] eIndex = new int[1];
        edges.get().forEach(e -> {
            source[eIndex[0]] = e.getFirst();
            target[eIndex[0]] = e.getSecond();
            outgoing.add(Pair.of(e.getFirst(), eIndex[0]));
            eIndex[0]++;
        });

        outIncidenceMatrix = new CSRBooleanMatrix(numVertices, m, outgoing);
    }

    @Override
    public long edgesCount()
    {
        return outIncidenceMatrix.columns();
    }

    @Override
    public long degreeOf(Integer vertex)
    {
        throw new UnsupportedOperationException(NO_INCOMING);
    }

    @Override
    public Set<Integer> edgesOf(Integer vertex)
    {
        throw new UnsupportedOperationException(NO_INCOMING);
    }

    @Override
    public long inDegreeOf(Integer vertex)
    {
        throw new UnsupportedOperationException(NO_INCOMING);
    }

    @Override
    public Set<Integer> incomingEdgesOf(Integer vertex)
    {
        throw new UnsupportedOperationException(NO_INCOMING);
    }

    @Override
    public long outDegreeOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return outIncidenceMatrix.nonZeros(vertex);
    }

    @Override
    public Set<Integer> outgoingEdgesOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return outIncidenceMatrix.nonZerosSet(vertex);
    }

    @Override
    public long verticesCount()
    {
        return outIncidenceMatrix.rows();
    }

    @Override
    public Integer getEdgeSource(Integer e)
    {
        assertEdgeExist(e);
        return source[e];
    }

    @Override
    public Integer getEdgeTarget(Integer e)
    {
        assertEdgeExist(e);
        return target[e];
    }

    @Override
    public GraphType getType()
    {
        return new DefaultGraphType.Builder()
            .directed().weighted(false).modifiable(false).allowMultipleEdges(true)
            .allowSelfLoops(true).build();
    }

    /**
     * {@inheritDoc}
     * 
     * This operation costs $O(d)$ where $d$ is the out-degree of the source vertex.
     */
    @Override
    public Integer getEdge(Integer sourceVertex, Integer targetVertex)
    {
        if (sourceVertex < 0 || sourceVertex >= outIncidenceMatrix.rows()) {
            return null;
        }
        if (targetVertex < 0 || targetVertex >= outIncidenceMatrix.rows()) {
            return null;
        }

        Iterator<Integer> it = outIncidenceMatrix.nonZerosPositionIterator(sourceVertex);
        while (it.hasNext()) {
            int eId = it.next();
            if (getEdgeTarget(eId).equals(targetVertex)) {
                return eId;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * This operation costs $O(d)$ where $d$ is the out-degree of the source vertex.
     */
    @Override
    public Set<Integer> getAllEdges(Integer sourceVertex, Integer targetVertex)
    {
        if (sourceVertex < 0 || sourceVertex >= outIncidenceMatrix.rows()) {
            return null;
        }
        if (targetVertex < 0 || targetVertex >= outIncidenceMatrix.rows()) {
            return null;
        }

        Set<Integer> result = new LinkedHashSet<>();

        Iterator<Integer> it = outIncidenceMatrix.nonZerosPositionIterator(sourceVertex);
        while (it.hasNext()) {
            int eId = it.next();

            if (getEdgeTarget(eId).equals(targetVertex)) {
                result.add(eId);
            }
        }
        return result;
    }

}
