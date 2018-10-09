/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

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
 * This is an implementation of the algorithm described by E. M. Palmer in his paper. The algorithm
 * takes a simple graph that meets Ore's condition (see {@link GraphTests#hasOreProperty(Graph)})
 * and returns a Hamiltonian cycle. The algorithm runs in $O(|V|^2)$ time and uses $O(|V|)$ space.
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
    implements
    HamiltonianCycleAlgorithm<V, E>
{
    /**
     * Construct a new instance
     */
    public PalmerHamiltonianCycle()
    {
    }

    /**
     * Computes a Hamiltonian tour.
     *
     * @param graph the input graph
     * @return a Hamiltonian tour
     *
     * @throws IllegalArgumentException if the graph doesn't meet Ore's condition
     * @see GraphTests#hasOreProperty(Graph)
     */
    public GraphPath<V, E> getTour(Graph<V, E> graph)
    {
        if (!GraphTests.hasOreProperty(graph))
            throw new IllegalArgumentException("Graph doesn't have Ore's property");

        List<V> indexList = new ArrayList<>(graph.vertexSet());

        // n - number of vertices
        final int n = graph.vertexSet().size();

        // L[u] = the node just before u (in the cycle)
        // R[u] = the node after u (in the cycle)
        int[] L = new int[n], R = new int[n];

        // arrange nodes in a cycle: 0, 1, 2, ..., n - 1, 0
        for (int i = 0; i < n; i++) {
            L[i] = (i - 1 + n) % n;
            R[i] = (i + 1) % n;
        }

        boolean changed;

        do {
            changed = false;

            // search for a gap (two consecutive vertices x and R[x] that are not adjacent in the
            // graph)
            int x = 0;

            search: do {
                // check if we found a gap in our cycle
                if (!graph.containsEdge(indexList.get(x), indexList.get(R[x]))) {
                    changed = true;

                    /*
                     * Search for a node y such that the four vertices x, R[x], y, and R[y] are all
                     * distinct and such that the graph contains edges from x to y and from R[y] to
                     * R[x]
                     */
                    int y = 0;
                    do {
                        int u = x, v = R[x];
                        int p = y, q = R[y];

                        if (v != p && u != p && u != q) {
                            if (graph.containsEdge(indexList.get(u), indexList.get(p))
                                && graph.containsEdge(indexList.get(v), indexList.get(q)))
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

        List<V> vertexList = new ArrayList<>(n);
        List<E> edgeList = new ArrayList<>(n);

        int x = 0;
        do {
            vertexList.add(indexList.get(x));
            edgeList.add(graph.getEdge(indexList.get(x), indexList.get(R[x])));
            x = R[x];
        } while (x != 0);

        // add start vertex
        vertexList.add(indexList.get(0));

        return new GraphWalk<>(
            graph, indexList.get(0), indexList.get(0), vertexList, edgeList, edgeList.size());
    }
}
