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
 * Sparse unmodifiable undirected weighted graph.
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
 * It stores the boolean incidence matrix of the graph (rows are vertices and columns are edges) as
 * Compressed Sparse Row (CSR). This is a classic format for write-once read-many use cases. Thus,
 * the graph is unmodifiable. In order to also support constant time source and target lookups from
 * an edge identifier we also store the transposed of the incidence matrix again in compressed
 * sparse row format. In this transposed matrix we also maintain the edge weights.
 * 
 * @author Dimitrios Michail
 */
public class SparseUndirectedWeightedGraph
    extends
    BaseSparseUndirectedGraph
    implements
    Serializable
{
    private static final long serialVersionUID = -5410680356868181247L;

    protected CSRDoubleMatrix incidenceMatrixT;

    /**
     * Create a new graph from an edge list
     * 
     * @param numVertices number of vertices
     * @param edges edge list with weights
     */
    public SparseUndirectedWeightedGraph(
        int numVertices, List<Triple<Integer, Integer, Double>> edges)
    {
        super(
            numVertices,
            edges
                .stream().map(e -> Pair.of(e.getFirst(), e.getSecond()))
                .collect(Collectors.toList()));

        List<Triple<Integer, Integer, Double>> nonZerosTranspose = new ArrayList<>();
        int eIndex = 0;
        for (Triple<Integer, Integer, Double> e : edges) {
            nonZerosTranspose.add(Triple.of(eIndex, e.getFirst(), e.getThird()));
            nonZerosTranspose.add(Triple.of(eIndex, e.getSecond(), e.getThird()));
            eIndex++;
        }
        incidenceMatrixT = new CSRDoubleMatrix(edges.size(), numVertices, nonZerosTranspose);
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
        return super.getType().asWeighted();
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

}
