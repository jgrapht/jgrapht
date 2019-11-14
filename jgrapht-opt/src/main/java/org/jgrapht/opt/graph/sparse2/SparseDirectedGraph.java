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

import java.util.List;

import org.jgrapht.alg.util.Pair;

/**
 * A simple and efficient sparse graph implementation.
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
 * @author Dimitrios Michail
 */
public class SparseDirectedGraph
    extends
    SparseGraph
{
    private static final long serialVersionUID = -3344944238118305582L;

    /**
     * Create a new graph from an edge list
     * 
     * @param numVertices number of vertices
     * @param edges edge list
     */
    public SparseDirectedGraph(int numVertices, List<Pair<Integer, Integer>> edges)
    {
        super(true, numVertices, edges);
    }

}
