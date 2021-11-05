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
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.util.UnmodifiableUnionSet;

/**
 * Specifics for a sparse directed graph which does not re-index the edges and supports incoming
 * edges. No reindexing means that the edges are numbered in increasing order using the order that
 * the user loads the edges. Support for incoming edges is provided but is initialized lazily the
 * first time the user accessed a corresponding method.
 *
 * @author Dimitrios Michail
 */
public class IncomingNoReindexSparseDirectedSpecifics
    extends
    NoIncomingNoReindexSparseDirectedSpecifics
{
    /**
     * Incidence matrix with incoming edges
     */
    protected CSRBooleanMatrix inIncidenceMatrix;

    /**
     * Create a new graph from an edge list.
     * 
     * @param numVertices the number of vertices
     * @param numEdges the number of edges
     * @param edges a supplier of an edge stream
     * @param lazyIncomingEdges whether to lazily support incoming edge traversals, only if actually
     *        needed by the user
     */
    public IncomingNoReindexSparseDirectedSpecifics(
        int numVertices, int numEdges, Supplier<Stream<Pair<Integer, Integer>>> edges,
        boolean lazyIncomingEdges)
    {
        super(numVertices, numEdges, edges);

        if (!lazyIncomingEdges) {
            indexIncomingEdges();
        }
    }

    @Override
    public long degreeOf(Integer vertex)
    {
        assertVertexExist(vertex);
        if (inIncidenceMatrix == null) {
            indexIncomingEdges();
        }
        return outIncidenceMatrix.nonZeros(vertex) + inIncidenceMatrix.nonZeros(vertex);
    }

    @Override
    public Set<Integer> edgesOf(Integer vertex)
    {
        assertVertexExist(vertex);
        if (inIncidenceMatrix == null) {
            indexIncomingEdges();
        }
        return new UnmodifiableUnionSet<>(
            outIncidenceMatrix.nonZerosSet(vertex), inIncidenceMatrix.nonZerosSet(vertex));
    }

    @Override
    public long inDegreeOf(Integer vertex)
    {
        assertVertexExist(vertex);
        if (inIncidenceMatrix == null) {
            indexIncomingEdges();
        }
        return inIncidenceMatrix.nonZeros(vertex);
    }

    @Override
    public Set<Integer> incomingEdgesOf(Integer vertex)
    {
        assertVertexExist(vertex);
        if (inIncidenceMatrix == null) {
            indexIncomingEdges();
        }
        return inIncidenceMatrix.nonZerosSet(vertex);
    }

    /**
     * Build the index for the incoming edges.
     */
    protected void indexIncomingEdges()
    {
        int n = outIncidenceMatrix.rows();
        int m = source.length;
        List<Pair<Integer, Integer>> incoming = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            incoming.add(Pair.of(target[i], i));
        }
        inIncidenceMatrix = new CSRBooleanMatrix(n, m, incoming);
    }

}
