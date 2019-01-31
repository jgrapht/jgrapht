/*
 * (C) Copyright 2003-2018, by Linda Buisman and Contributors.
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
package org.jgrapht.alg.vertexcover;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * Finds a 2-approximation for a minimum vertex cover A vertex cover is a set of vertices that
 * touches all the edges in the graph. The graph's vertex set is a trivial cover. However, a
 * <i>minimal</i> vertex set (or at least an approximation for it) is usually desired. Finding a
 * true minimal vertex cover is an NP-Complete problem. For more on the vertex cover problem, see
 * <a href="http://mathworld.wolfram.com/VertexCover.html">
 * http://mathworld.wolfram.com/VertexCover.html</a>
 *
 * Note: this class supports pseudo-graphs
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Linda Buisman
 */
public class EdgeBasedTwoApproxVCImpl<V, E>
    implements
    VertexCoverAlgorithm<V>
{

    private final Graph<V, E> graph;

    /**
     * Constructs a new EdgeBasedTwoApproxVCImpl instance
     * 
     * @param graph input graph
     */
    public EdgeBasedTwoApproxVCImpl(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireUndirected(graph);
    }

    /**
     * Finds a 2-approximation for a minimal vertex cover of the specified graph. The algorithm
     * promises a cover that is at most double the size of a minimal cover. The algorithm takes
     * O(|E|) time.
     *
     * Note: this class supports pseudo-graphs Runtime: O(|E|)
     *
     * Albeit the fact that this is a 2-approximation algorithm for vertex cover, its results are
     * often of lower quality than the results produced by {@link BarYehudaEvenTwoApproxVCImpl} or
     * {@link ClarksonTwoApproxVCImpl}.
     *
     * <p>
     * For more details see Jenny Walter, CMPU-240: Lecture notes for Language Theory and
     * Computation, Fall 2002, Vassar College,
     * <a href="http://www.cs.vassar.edu/~walter/cs241index/lectures/PDF/approx.pdf">
     * http://www.cs.vassar.edu/~walter/cs241index/lectures/PDF/approx.pdf</a>.
     * </p>
     *
     *
     * @return a set of vertices which is a vertex cover for the specified graph.
     */
    @Override
    public VertexCoverAlgorithm.VertexCover<V> getVertexCover()
    {
        // C <-- {}
        Set<V> cover = new LinkedHashSet<>();

        // G'=(V',E') <-- G(V,E)
        Graph<V, E> sg = new AsSubgraph<>(graph, null, null);

        // while E' is non-empty
        while (!sg.edgeSet().isEmpty()) {
            // let (u,v) be an arbitrary edge of E'
            E e = sg.edgeSet().iterator().next();

            // C <-- C U {u,v}
            V u = graph.getEdgeSource(e);
            V v = graph.getEdgeTarget(e);
            cover.add(u);
            cover.add(v);

            // remove from E' every edge incident on either u or v
            sg.removeVertex(u);
            sg.removeVertex(v);
        }
        return new VertexCoverAlgorithm.VertexCoverImpl<>(cover);
    }

}
