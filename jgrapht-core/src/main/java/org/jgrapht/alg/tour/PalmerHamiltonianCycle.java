/*
 * (C) Copyright 2018-2021, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.alg.tour;

import org.jgrapht.*;

import java.util.*;

import static org.jgrapht.util.ArrayUtil.*;

/**
 * Palmer's algorithm for computing Hamiltonian cycles in graphs that meet Ore's condition. Ore gave
 * a sufficient condition for a graph to be Hamiltonian, essentially stating that a graph with
 * sufficiently many edges must contain a Hamilton cycle.
 *
 * Specifically, Ore's theorem considers the sum of the degrees of pairs of non-adjacent vertices:
 * if every such pair has a sum that at least equals the total number of vertices in the graph, then
 * the graph is Hamiltonian.
 *
 * <p>
 * A Hamiltonian cycle, also called a Hamiltonian circuit, Hamilton cycle, or Hamilton circuit, is a
 * graph cycle (i.e., closed loop) through a graph that visits each node exactly once (Skiena 1990,
 * p. 196).
 * </p>
 *
 * <p>
 * This is an implementation of the CRISS-CROSS algorithm described by E. M. Palmer in his paper.
 * The algorithm takes a simple graph that meets Ore's condition (see
 * {@link GraphTests#hasOreProperty(Graph)}) and returns a Hamiltonian cycle. The algorithm runs in
 * $O(|V|^3)$ time and uses $O(|V|)$ space. In contrast to the most other algorithms in this package
 * this algorithm does only attempt to find any Hamiltonian cycle in the graph and does not attempt
 * to find the shortest cycle. The advantage of this algorithm is that accepted graphs only need to
 * meet Ore's condition which is less strict than the completeness requirement of most of the other
 * algorithms.
 * </p>
 *
 * <p>
 * The original algorithm is described in: Palmer, E. M. (1997), "The hidden algorithm of Ore's
 * theorem on Hamiltonian cycles", Computers &amp; Mathematics with Applications, 34 (11): 113–119,
 * doi:10.1016/S0898-1221(97)00225-3
 *
 * See <a href="https://en.wikipedia.org/wiki/Ore%27s_theorem">wikipedia</a> for a short description
 * of Ore's theorem and Palmer's algorithm.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexandru Valeanu
 * @author Hannes Wellmann
 */
public class PalmerHamiltonianCycle<V, E>
    extends
    HamiltonianCycleAlgorithmBase<V, E>
{

    /**
     * Computes a Hamiltonian tour.
     *
     * @param graph the input graph
     * @return a Hamiltonian tour
     *
     * @throws IllegalArgumentException if the graph doesn't meet Ore's condition
     * @see GraphTests#hasOreProperty(Graph)
     */
    @Override
    public GraphPath<V, E> getTour(Graph<V, E> graph)
    {
        if (!GraphTests.hasOreProperty(graph)) { // requires vertexSet().size() >= 3
            throw new IllegalArgumentException("Graph doesn't have Ore's property");
        }

        Set<V> vertices = graph.vertexSet();
        final int n = vertices.size(); // number of vertices
        @SuppressWarnings("unchecked") V[] tour = (V[]) vertices.toArray(new Object[n + 1]);

        while (searchAndCloseGap(tour, n, graph)) {
            // repeat until not gap exists anymore
        }

        tour[n] = tour[0]; // close tour manually. Arrays.asList does not support add
        return closedVertexListToTour(Arrays.asList(tour), graph);
    }

    private static <V, E> boolean searchAndCloseGap(V[] tour, final int n, Graph<V, E> graph)
    {
        // search for a gap, i.e.: two consecutive vertices v and vN that are not adjacent in the
        // graph (connected by an edge)

        V v = tour[n - 1]; // start with last so "last to first"-connection is checked first
        for (int i = 0; i < n; i++) {
            V vN = tour[i]; // vN - the successor of v, i - its index

            // check if we found a gap in our cycle
            if (!graph.containsEdge(v, vN)) {

                // Search for a node 'u' such that the four vertices v, vN, u, and uN are all
                // distinct and that the graph contains edges from v to u and from vN to uN
                // ("a pair of crossing chords from the vertices of the gap to some other pair of
                // consecutive vertices that may or may not be adjacent")

                V u = tour[n - 1]; // again, start with last vertex
                for (int j = 0; j < n; j++) {
                    V uN = tour[j]; // uN - the successor of u, j - its index

                    boolean distinct = v != u && vN != u && v != uN; // v != u implies vN != uN
                    if (distinct && graph.containsEdge(v, u) && graph.containsEdge(vN, uN)) {
                        // found "a pair of crossing chords"
                        reverseInCircle(tour, i, j - 1);
                        return true;
                    }
                    u = uN;
                }
                throw new IllegalStateException("Found a gap but no mean to close it");
            }
            v = vN;
        }
        return false;
    }

    private static <V> void reverseInCircle(V[] array, int start, int end)
    {
        if (start < end) { // interval to reverse is completely contained in the array bounds
            reverse(array, start, end);

        } else { // interval to reverse wraps around the array end

            // Happens if the first gap to swap is closer to the "end" than the second gap.
            // Since it is all relative, instead of swapping "over the end" the opposite segment
            // (within the array) is swapped.
            reverse(array, end + 1, start - 1);
        }
    }
}
