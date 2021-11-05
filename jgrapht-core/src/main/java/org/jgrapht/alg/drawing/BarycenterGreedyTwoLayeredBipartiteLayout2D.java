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
package org.jgrapht.alg.drawing;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.drawing.model.LayoutModel2D;
import org.jgrapht.alg.drawing.model.Point2D;
import org.jgrapht.alg.util.Pair;

/**
 * The barycenter heuristic greedy algorithm for edge crossing minimization in two layered bipartite
 * layouts.
 * 
 * The algorithm draws a bipartite graph using straight edges. Vertices are arranged along two
 * vertical or horizontal lines, trying to minimize crossings. This algorithm targets the one-sided
 * problem where one of the two layers is considered fixed and the algorithm is allowed to adjust
 * the positions of vertices in the other layer.
 * 
 * The algorithm is described in the following paper: K. Sugiyama, S. Tagawa, and M. Toda. Methods
 * for visual understanding of hierarchical system structures. IEEE Transaction on Systems, Man, and
 * Cybernetics, 11(2):109–125, 1981.
 * 
 * The problem of minimizing edge crossings when drawing bipartite graphs as two layered graphs is
 * NP-complete. If the coordinates of the nodes in the fixed layer are allowed to vary wildly, then
 * the barycenter heuristic can perform badly. If the coordinates of the nodes in the fixed layer
 * are $1, 2, 3, \ldots, ...$ then it is an $\mathcal{O}(\sqrt{n})$-approximation algorithm.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class BarycenterGreedyTwoLayeredBipartiteLayout2D<V, E>
    extends
    TwoLayeredBipartiteLayout2D<V, E>
{
    /**
     * Create a new layout
     */
    public BarycenterGreedyTwoLayeredBipartiteLayout2D()
    {
        super();
    }

    /**
     * Create a new layout
     * 
     * @param partition one of the two partitions, can be null
     * @param vertexComparator vertex order, can be null
     * @param vertical draw on two vertical or horizontal lines
     */
    public BarycenterGreedyTwoLayeredBipartiteLayout2D(
        Set<V> partition, Comparator<V> vertexComparator, boolean vertical)
    {
        super(partition, vertexComparator, vertical);
    }

    @Override
    protected void drawSecondPartition(Graph<V, E> graph, List<V> partition, LayoutModel2D<V> model)
    {
        if (partition.isEmpty()) {
            throw new IllegalArgumentException("Partition cannot be empty");
        }

        // compute new order
        final Map<V, Pair<Double, Integer>> order = new HashMap<>();
        int i = 0;
        for (V v : partition) {
            int degree = graph.degreeOf(v);
            if (degree == 0) {
                // singleton
                order.put(v, Pair.of(-Double.MAX_VALUE, i));
            } else {
                double barycenter = 0d;
                for (E e : graph.outgoingEdgesOf(v)) {
                    V u = Graphs.getOppositeVertex(graph, e, v);
                    Point2D p2d = model.get(u);
                    double coord = vertical ? p2d.getX() : p2d.getY();
                    barycenter += coord;
                }
                barycenter /= degree;
                order.put(v, Pair.of(barycenter, i));
            }
            i++;
        }

        // create comparator for new order
        Comparator<V> newOrderComparator = (v, u) -> {
            Pair<Double, Integer> pv = order.get(v);
            Pair<Double, Integer> pu = order.get(u);

            int d = Double.compare(pv.getFirst(), pu.getFirst());
            if (d != 0) {
                return d;
            }

            int degreeV = graph.degreeOf(v);
            int degreeU = graph.degreeOf(u);

            if (degreeV % 2 == 1 && degreeU % 2 == 0) {
                return -1;
            }
            if (degreeV % 2 == 0 && degreeU % 2 == 1) {
                return 1;
            }

            return Integer.compare(pv.getSecond(), pu.getSecond());
        };

        // sort with new order and delegate
        partition.sort(newOrderComparator);
        super.drawSecondPartition(graph, partition, model);
    }

}
