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
import java.util.List;
import java.util.stream.Collectors;

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
 * Compressed Sparse Row (CSR). This is a classic format for write-once read-many use cases. Thus,
 * the graph is unmodifiable. In order to also support constant time source and target lookups from
 * an edge identifier we also store the two transposed incidence matrices again in compressed sparse
 * row format. The edge weights are maintained in an array indexed by the edge identifier.
 * <p>
 * The graph is weighted. While unmodifiable with respect to the structure of the graph, the edge
 * weights can be changed even after the graph is constructed.
 * 
 * @author Dimitrios Michail
 */
public class SparseDirectedWeightedGraph
    extends
    SparseDirectedGraph
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
    public SparseDirectedWeightedGraph(
        int numVertices, List<Triple<Integer, Integer, Double>> edges)
    {
        super(
            numVertices,
            edges
                .stream().map(e -> Pair.of(e.getFirst(), e.getSecond()))
                .collect(Collectors.toList()));

        this.weights = new double[edges.size()];

        int eIndex = 0;
        for (Triple<Integer, Integer, Double> e : edges) {
            double edgeWeight = e.getThird() != null ? e.getThird() : Graph.DEFAULT_EDGE_WEIGHT;
            weights[eIndex++] = edgeWeight;
        }
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
        return weights[e];
    }

    @Override
    public void setEdgeWeight(Integer e, double weight)
    {
        assertEdgeExist(e);
        weights[e] = weight;
    }

}
