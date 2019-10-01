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

import org.jgrapht.alg.util.Pair;

/**
 * Sparse unmodifiable undirected graph.
 *
 * <p>
 * Assuming the graph has $n$ vertices, the vertices are numbered from $0$ to $n-1$. Similarly,
 * edges are numbered from $0$ to $m-1$ where $m$ is the total number of edges.
 * 
 * <p>
 * It stores the boolean incidence matrix of the graph (rows are vertices and columns are edges) as
 * Compressed Sparse Row (CSR). This is a classic format for write-once read-many use cases. Thus,
 * the graph is unmodifiable. In order to also support constant time source and target lookups from
 * an edge identifier we also store the transposed of the incidence matrix again in compressed
 * sparse row format.
 * 
 * @author Dimitrios Michail
 */
public class SparseUndirectedGraph
    extends
    BaseSparseUndirectedGraph
    implements
    Serializable
{
    private static final long serialVersionUID = -4254356821672793855L;

    protected CSRBooleanMatrix incidenceMatrixT;

    /**
     * Create a new graph from an edge list
     * 
     * @param numVertices number of vertices
     * @param edges edge list
     */
    public SparseUndirectedGraph(int numVertices, List<Pair<Integer, Integer>> edges)
    {
        super(numVertices, edges);

        List<Pair<Integer, Integer>> nonZerosTranspose = new ArrayList<>();
        int eIndex = 0;
        for (Pair<Integer, Integer> e : edges) {
            nonZerosTranspose.add(Pair.of(eIndex, e.getFirst()));
            nonZerosTranspose.add(Pair.of(eIndex, e.getSecond()));
            eIndex++;
        }
        incidenceMatrixT = new CSRBooleanMatrix(edges.size(), numVertices, nonZerosTranspose);
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

}
