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
package org.jgrapht.opt.graph.sparse2;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.Triple;

/**
 * A simple and efficient sparse weighted undirected graph implementation.
 *
 * <p>
 * Assuming the graph has $n$ vertices, the vertices are numbered from $0$ to $n-1$. Similarly,
 * edges are numbered from $0$ to $m-1$ where $m$ is the total number of edges.
 * 
 * <p>
 * The representation uses six integer arrays, four of them have size $m$ where $m$ is the number of
 * edges and two of them have size $n+1$ where $n$ is the number of vertices. The first two contain
 * the source and target vertices of the edges. The third contains an index of the edge identifiers
 * if sorted by the composite key (source, target), while the fourth contains an index of the edge
 * identifiers if sorted by the composite key (target, source). The last two arrays contain the
 * cumulative sum of the outgoing and incoming vertex degrees respectively.
 *
 * <p>
 * The graph is initialized from the constructor and cannot be modified afterwards.
 *
 * <p>
 * The graph is weighted by maintaining an additional array with the edge weights. While
 * unmodifiable with respect to the structure of the graph, the edge weights can be changed even
 * after the graph is constructed.
 * 
 * @author Dimitrios Michail
 */
public class SparseUndirectedWeightedGraph
    extends
    SparseUndirectedGraph
    implements
    Serializable
{
    private static final long serialVersionUID = -3649817413000688910L;

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
    public SparseUndirectedWeightedGraph(
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
