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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jgrapht.GraphType;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.Triple;

/**
 * Sparse unmodifiable directed weighted graph.
 *
 * <p>
 * Assuming the graph has $n$ vertices, the vertices are numbered from $0$ to $n-1$. Similarly,
 * edges are numbered from $0$ to $m-1$ where $m$ is the total number of edges.
 * 
 * <p>
 * The graph is weighted. While unmodifiable with respect to the structure of the graph, the edge
 * weights can be changed even after the graph is constructed.
 * 
 * <p>
 * It stores two boolean incidence matrix of the graph (rows are vertices and columns are edges) as
 * Compressed Sparse Row (CSR). This is a classic format for write-once read-many use cases. Thus,
 * the graph is unmodifiable. In order to also support constant time source and target lookups from
 * an edge identifier we also store the two transposed incidence matrices again in compressed sparse
 * row format. In these transposed matrices we also maintain the edge weights.
 * 
 * @author Dimitrios Michail
 */
public class SparseDirectedWeightedGraph
    extends
    BaseSparseDirectedGraph
    implements
    Serializable
{
    private static final long serialVersionUID = -7601401110000642281L;

    protected CSRDoubleMatrix outIncidenceMatrixT;
    protected CSRDoubleMatrix inIncidenceMatrixT;

    /**
     * Create a new graph from an edge list.
     * 
     * @param numVertices the number of vertices
     * @param edges the edge list with additional weights
     */
    public SparseDirectedWeightedGraph(
        int numVertices, List<Triple<Integer, Integer, Double>> edges)
    {
        super(
            numVertices,
            edges
                .stream().map(e -> Pair.of(e.getFirst(), e.getSecond()))
                .collect(Collectors.toList()));

        List<Triple<Integer, Integer, Double>> outgoingT = new ArrayList<>();
        List<Triple<Integer, Integer, Double>> incomingT = new ArrayList<>();

        int eIndex = 0;
        for (Triple<Integer, Integer, Double> e : edges) {
            outgoingT.add(Triple.of(eIndex, e.getFirst(), e.getThird()));
            incomingT.add(Triple.of(eIndex, e.getSecond(), e.getThird()));
            eIndex++;
        }

        outIncidenceMatrixT = new CSRDoubleMatrix(edges.size(), numVertices, outgoingT);
        inIncidenceMatrixT = new CSRDoubleMatrix(edges.size(), numVertices, incomingT);
    }

    @Override
    public Integer getEdgeSource(Integer e)
    {
        assertEdgeExist(e);
        return outIncidenceMatrixT.nonZerosPositionIterator(e).next();
    }

    @Override
    public Integer getEdgeTarget(Integer e)
    {
        assertEdgeExist(e);
        return inIncidenceMatrixT.nonZerosPositionIterator(e).next();
    }

    @Override
    public GraphType getType()
    {
        return super.getType().asWeighted();
    }

    @Override
    public double getEdgeWeight(Integer e)
    {
        assertEdgeExist(e);

        Iterator<Pair<Integer, Double>> it = outIncidenceMatrixT.nonZerosIterator(e);
        if (it.hasNext()) {
            return it.next().getSecond();
        }
        return inIncidenceMatrixT.nonZerosIterator(e).next().getSecond();
    }

    @Override
    public void setEdgeWeight(Integer e, double weight)
    {
        assertEdgeExist(e);

        outIncidenceMatrixT.setNonZeros(e, weight);
        inIncidenceMatrixT.setNonZeros(e, weight);
    }

}
