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

import java.io.Serializable;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.Triple;

/**
 * Sparse directed weighted graph.
 *
 * <p>
 * Assuming the graph has $n$ vertices, the vertices are numbered from $0$ to $n-1$. Similarly,
 * edges are numbered from $0$ to $m-1$ where $m$ is the total number of edges.
 * 
 * <p>
 * It stores two boolean incidence matrix of the graph (rows are vertices and columns are edges) as
 * Compressed Sparse Rows (CSR). Constant time source and target lookups are provided by storing the
 * edge lists in arrays. This is a classic format for write-once read-many use cases. Thus, the
 * graph is unmodifiable. The edge weights are maintained in an array indexed by the edge
 * identifier. If the user does not require support for incoming edges, then the second incidence
 * matrix can be either completely avoided or constructed lazily. See
 * {@link #SparseIntDirectedWeightedGraph(int, List, IncomingEdgesSupport)} for more details.
 *
 * <p>
 * The graph is weighted. While unmodifiable with respect to the structure of the graph, the edge
 * weights can be changed even after the graph is constructed.
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
public class SparseIntDirectedWeightedGraph
    extends
    SparseIntDirectedGraph
    implements
    Serializable
{
    private static final long serialVersionUID = -7601401110000642281L;

    /**
     * The edge weights
     */
    protected double[] weights;

    /**
     * Create a new graph from an edge list.
     * 
     * @param numVertices the number of vertices
     * @param edges the edge list with additional weights
     */
    public SparseIntDirectedWeightedGraph(
        int numVertices, List<Triple<Integer, Integer, Double>> edges)
    {
        this(
            numVertices, edges.size(), () -> edges.stream(),
            IncomingEdgesSupport.FULL_INCOMING_EDGES);
    }

    /**
     * Create a new graph from an edge list.
     * 
     * @param numVertices the number of vertices
     * @param edges the edge list with additional weights
     * @param incomingEdgeSupport the kind of incoming edges support needed
     */
    public SparseIntDirectedWeightedGraph(
        int numVertices, List<Triple<Integer, Integer, Double>> edges,
        IncomingEdgesSupport incomingEdgeSupport)
    {
        this(numVertices, edges.size(), () -> edges.stream(), incomingEdgeSupport);
    }

    /**
     * Create a new graph from an edge stream.
     * 
     * @param numVertices the number of vertices
     * @param numEdges the number of edges
     * @param edges a supplier of an edge stream with additional weights
     * @param incomingEdgeSupport the kind of incoming edges support needed
     */
    public SparseIntDirectedWeightedGraph(
        int numVertices, int numEdges, Supplier<Stream<Triple<Integer, Integer, Double>>> edges,
        IncomingEdgesSupport incomingEdgeSupport)
    {
        super(
            numVertices, numEdges, () -> edges.get().map(e -> Pair.of(e.getFirst(), e.getSecond())),
            incomingEdgeSupport);

        this.weights = new double[numEdges];

        int[] eIndex = new int[1];
        edges.get().forEach(e -> {
            double edgeWeight = e.getThird() != null ? e.getThird() : Graph.DEFAULT_EDGE_WEIGHT;
            weights[eIndex[0]++] = edgeWeight;
        });
    }

    @Override
    public GraphType getType()
    {
        return super.getType().asWeighted();
    }

    @Override
    public double getEdgeWeight(Integer e)
    {
        specifics.assertEdgeExist(e);
        return weights[e];
    }

    @Override
    public void setEdgeWeight(Integer e, double weight)
    {
        specifics.assertEdgeExist(e);
        weights[e] = weight;
    }

}
