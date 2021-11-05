/*
 * (C) Copyright 2019-2021, by Dimitrios Michail and Contributors.
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

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.opt.graph.sparse.specifics.AbstractSparseSpecificsGraph;
import org.jgrapht.opt.graph.sparse.specifics.IncomingNoReindexSparseDirectedSpecifics;
import org.jgrapht.opt.graph.sparse.specifics.NoIncomingNoReindexSparseDirectedSpecifics;
import org.jgrapht.opt.graph.sparse.specifics.SparseGraphSpecifics;

/**
 * A sparse directed graph.
 *
 * <p>
 * Assuming the graph has $n$ vertices, the vertices are numbered from $0$ to $n-1$. Similarly,
 * edges are numbered from $0$ to $m-1$ where $m$ is the total number of edges.
 * 
 * <p>
 * It stores two boolean incidence matrix of the graph (rows are vertices and columns are edges) as
 * Compressed Sparse Rows (CSR). Constant time source and target lookups are provided by storing the
 * edge lists in arrays. This is a classic format for write-once read-many use cases. Thus, the
 * graph is unmodifiable.
 * 
 * <p>
 * The question of whether a sparse or dense representation is more appropriate is highly dependent
 * on various factors such as the graph, the machine running the algorithm and the algorithm itself.
 * Wilkinson defined a matrix as "sparse" if it has enough zeros that it pays to take advantage of
 * them. For more details see
 * <ul>
 * <li>Wilkinson, J. H. 1971. Linear algebra; part II: the algebraic eigenvalue problem. In Handbook
 * for Automatic Computation, J. H. Wilkinson and C. Reinsch, Eds. Vol. 2. Springer-Verlag, Berlin,
 * New York.</li>
 * </ul>
 * 
 * Additional information about sparse representations can be found in the
 * <a href="https://en.wikipedia.org/wiki/Sparse_matrix">wikipedia</a>.
 * 
 * @author Dimitrios Michail
 */
public class SparseIntDirectedGraph
    extends
    AbstractSparseSpecificsGraph<SparseGraphSpecifics>
{
    protected static final String UNMODIFIABLE = "this graph is unmodifiable";

    /**
     * Create a new graph from an edge list.
     * 
     * @param numVertices the number of vertices
     * @param edges the edge list
     */
    public SparseIntDirectedGraph(int numVertices, List<Pair<Integer, Integer>> edges)
    {
        this(
            numVertices, edges.size(), () -> edges.stream(),
            IncomingEdgesSupport.FULL_INCOMING_EDGES);
    }

    /**
     * Create a new graph from an edge list.
     * 
     * @param numVertices the number of vertices
     * @param edges the edge list
     * @param incomingEdgesSupport whether to support incoming edges or not
     */
    public SparseIntDirectedGraph(
        int numVertices, List<Pair<Integer, Integer>> edges,
        IncomingEdgesSupport incomingEdgesSupport)
    {
        this(numVertices, edges.size(), () -> edges.stream(), incomingEdgesSupport);
    }

    /**
     * Create a new graph from an edge stream.
     * 
     * @param numVertices the number of vertices
     * @param numEdges the number of edges
     * @param edges the edge stream
     * @param incomingEdgesSupport whether to support incoming edges or not
     */
    public SparseIntDirectedGraph(
        int numVertices, int numEdges, Supplier<Stream<Pair<Integer, Integer>>> edges,
        IncomingEdgesSupport incomingEdgesSupport)
    {
        super(() -> {
            switch (incomingEdgesSupport) {
            case FULL_INCOMING_EDGES:
                return new IncomingNoReindexSparseDirectedSpecifics(
                    numVertices, numEdges, edges, false);
            case LAZY_INCOMING_EDGES:
                return new IncomingNoReindexSparseDirectedSpecifics(
                    numVertices, numEdges, edges, true);
            case NO_INCOMING_EDGES:
            default:
                return new NoIncomingNoReindexSparseDirectedSpecifics(numVertices, numEdges, edges);
            }
        });
    }

}
