/*
 * (C) Copyright 2018-2020, by Alexandru Valeanu and Contributors.
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

        List<V> indexList = new ArrayList<>(graph.vertexSet());

        // n - number of vertices
        final int n = indexList.size();

        int[] L = new int[n]; // L[u] = the node just before u (in the cycle)
        int[] R = new int[n]; // R[u] = the node after u (in the cycle)

        // arrange nodes in a cycle: 0, 1, 2, ..., n - 1, 0
        for (int i = 0; i < n - 1; i++) {
            L[i + 1] = i; // L = [n-1, 0, ..., n-2]
            R[i] = i + 1; // R = [1, ..., n-1, 0]
        }
        L[0] = n - 1;
        R[n - 1] = 0;

        boolean changed;
        do {
            changed = false;

            // search for a gap, i.e.: two consecutive vertices x and R[x] that are not adjacent
            // (connected by a edge) in the graph
            int x = 0;

            search: do {
                // check if we found a gap in our cycle
                if (!containsEdge(x, R[x], graph, indexList)) {
                    changed = true;

                    /*
                     * Search for a node y such that the four vertices x, R[x], y, and R[y] are all
                     * distinct and such that the graph contains edges from x to y and from R[y] to
                     * R[x] ("a pair of crossing chords from the vertices of the gap to some other
                     * pair of consecutive vertices that may or may not be adjacent" )
                     */
                    int y = 0;
                    do {
                        int u = x, v = R[x];
                        int p = y, q = R[y];

                        if (v != p && u != p && u != q) {
                            if (containsEdge(u, p, graph, indexList)
                                && containsEdge(v, q, graph, indexList))
                            {
                                R[u] = L[u];
                                L[u] = p;
                                R[v] = R[v];
                                L[v] = q;
                                L[p] = L[p];
                                R[p] = u;
                                L[q] = R[q];
                                R[q] = v;

                                for (int z = R[u]; z != q; z = R[z]) {
                                    int tmp = R[z];
                                    R[z] = L[z];
                                    L[z] = tmp;
                                }
                                break search;
                            }
                        }
                        y = R[y];
                    } while (y != 0);
                }
                x = R[x];
            } while (x != 0);

        } while (changed);

        return buildTour(R, indexList, graph);
    }

    private static <V, E> boolean containsEdge(int u, int v, Graph<V, E> graph, List<V> indexList)
    {
        return graph.containsEdge(indexList.get(u), indexList.get(v));
    }

    private GraphPath<V, E> buildTour(int[] R, List<V> indexList, Graph<V, E> graph)
    {
        List<V> vertexList = new ArrayList<>(indexList.size() + 1);
        int x = 0;
        do {
            vertexList.add(indexList.get(x));
            x = R[x];
        } while (x != 0);
        return vertexListToTour(vertexList, graph);
    }
}
