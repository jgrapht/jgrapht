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
import java.util.List;

import org.jgrapht.alg.util.Pair;

/**
 * Sparse unmodifiable directed graph.
 *
 * <p>
 * Assuming the graph has $n$ vertices, the vertices are numbered from $0$ to $n-1$. Similarly,
 * edges are numbered from $0$ to $m-1$ where $m$ is the total number of edges.
 * 
 * <p>
 * It stores two boolean incidence matrix of the graph (rows are vertices and columns are edges) as
 * Compressed Sparse Row (CSR). This is a classic format for write-once read-many use cases. Thus,
 * the graph is unmodifiable. In order to also support constant time source and target lookups from
 * an edge identifier we also store the two transposed incidence matrices again in compressed sparse
 * row format.
 * 
 * @author Dimitrios Michail
 */
public class SparseDirectedGraph
    extends
    BaseSparseDirectedGraph
{
    protected CSRBooleanMatrix outIncidenceMatrixT;
    protected CSRBooleanMatrix inIncidenceMatrixT;

    /**
     * Create a new graph from an edge list.
     * 
     * @param numVertices the number of vertices
     * @param edges the edge list
     */
    public SparseDirectedGraph(int numVertices, List<Pair<Integer, Integer>> edges)
    {
        super(numVertices, edges);

        List<Pair<Integer, Integer>> outgoingT = new ArrayList<>();
        List<Pair<Integer, Integer>> incomingT = new ArrayList<>();

        int eIndex = 0;
        for (Pair<Integer, Integer> e : edges) {
            outgoingT.add(Pair.of(eIndex, e.getFirst()));
            incomingT.add(Pair.of(eIndex, e.getSecond()));
            eIndex++;
        }

        outIncidenceMatrixT = new CSRBooleanMatrix(edges.size(), numVertices, outgoingT);
        inIncidenceMatrixT = new CSRBooleanMatrix(edges.size(), numVertices, incomingT);
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

}
