/*
 * (C) Copyright 2013-2020, by Leo Crawford and Contributors.
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
package org.jgrapht.alg.lca;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;

import java.util.*;

/**
 * Find the Lowest Common Ancestor of a directed graph.
 *
 * <p>
 * Find the LCA, defined as <i>Let $G = (V, E)$ be a DAG, and let $x, y \in V$ . Let $G x,y$ be the
 * subgraph of $G$ induced by the set of all common ancestors of $x$ and $y$. Define SLCA (x, y) to
 * be the set of out-degree 0 nodes (leafs) in $G x,y$. The lowest common ancestors of $x$ and $y$
 * are the elements of SLCA (x, y). This naive algorithm simply starts at $a$ and $b$, recursing
 * upwards to the root(s) of the DAG. Wherever the recursion paths cross we have found our LCA.</i>
 * from <i> Michael A. Bender, Martín Farach-Colton, Giridhar Pemmasani, Steven Skiena, Pavel
 * Sumazin, Lowest common ancestors in trees and directed acyclic graphs, Journal of Algorithms,
 * Volume 57, Issue 2, 2005, Pages 75-94, ISSN 0196-6774,
 * https://doi.org/10.1016/j.jalgor.2005.08.001. </i>
 *
 * <p>
 * The algorithm:
 *
 * <pre>
 * 1. Start at each of nodes you wish to find the lca for (a and b)
 * 2. Create sets aSet containing a, and bSet containing b
 * 3. If either set intersects with the union of the other sets previous values (i.e. the set of notes visited) then
 *    that intersection is LCA. if there are multiple intersections then the earliest one added is the LCA.
 * 4. Repeat from step 3, with aSet now the parents of everything in aSet, and bSet the parents of everything in bSet
 * 5. If there are no more parents to descend to then there is no LCA
 * </pre>
 *
 * The rationale for this working is that in each iteration of the loop we are considering all the
 * ancestors of a that have a path of length n back to a, where n is the depth of the recursion. The
 * same is true of b.
 *
 * <p>
 * We start by checking if a == b.<br>
 * if not we look to see if there is any intersection between parents(a) and (parents(b) union b)
 * (and the same with a and b swapped)<br>
 * if not we look to see if there is any intersection between parents(parents(a)) and
 * (parents(parents(b)) union parents(b) union b) (and the same with a and b swapped)<br>
 * continues
 *
 * <p>
 * This means at the end of recursion n, we know if there is an LCA that has a path of &lt;=n to a
 * and b. Of course we may have to wait longer if the path to a is of length n, but the path to
 * b&gt;n. at the first loop we have a path of 0 length from the nodes we are considering as LCA to
 * their respective children which we wish to find the LCA for.
 *
 * <p>
 * Preprocessing Time complexity: $O(1)$<br>
 * Preprocessing Space complexity: $O(1)$<br>
 * Query Time complexity: $O(|V|)$<br>
 * Query Space complexity: $O(|V|)$<br>
 * </p>
 *
 * <p>
 * For trees or forests please use either {@link BinaryLiftingLCAFinder},
 * {@link HeavyPathLCAFinder}, {@link EulerTourRMQLCAFinder} or {@link TarjanLCAFinder}.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Leo Crawford
 * @author Alexandru Valeanu
 */
public class NaiveLCAFinder<V, E>
    implements
    LowestCommonAncestorAlgorithm<V>
{
    private Graph<V, E> graph;

    /**
     * Create a new instance of the naive LCA finder.
     * 
     * @param graph the input graph
     */
    public NaiveLCAFinder(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getLCA(V a, V b)
    {
        checkNodes(a, b);
        Set<V> lcaSet = getLCASet(a, b);
        if (lcaSet.isEmpty()) {
            return null;
        } else {
            return lcaSet.iterator().next();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<V> getLCASet(V a, V b)
    {
        checkNodes(a, b);

        List<Set<V>> visitedSets = doubleBfs(a, b);
        // all common ancestors of both a and b
        Set<V> intersection;

        // optimization trick: save the intersection using the smaller set
        if (visitedSets.get(0).size() < visitedSets.get(1).size()) {
            visitedSets.get(0).retainAll(visitedSets.get(1));
            intersection = visitedSets.get(0);
        } else {
            visitedSets.get(1).retainAll(visitedSets.get(0));
            intersection = visitedSets.get(1);
        }

        /*
         * Find the set of all non-leaves by iterating through the set of common ancestors. When we
         * encounter a node which is still part of the SLCA(a, b) we remove its parent(s).
         */
        Set<V> nonLeaves = new LinkedHashSet<>();
        for (V node : intersection) {
            for (E edge : graph.incomingEdgesOf(node)) {
                if (graph.getEdgeTarget(edge).equals(node)) {
                    V source = graph.getEdgeSource(edge);

                    if (intersection.contains(source))
                        nonLeaves.add(source);
                }
            }
        }

        // perform the actual removal of non-leaves
        intersection.removeAll(nonLeaves);
        return intersection;
    }

    /**
     * Perform a simultaneous bottom-up bfs from a and b, saving all visited nodes. Once a a node
     * has been visited from both a and b, it is no longer expanded in our search (we know that its
     * ancestors won't be part of the SLCA(x, y) set).
     */
    private List<Set<V>> doubleBfs(V a, V b)
    {
        List<Queue<V>> queues =
            new ArrayList<>(Arrays.asList(new ArrayDeque<>(), new ArrayDeque<>()));
        List<Set<V>> visitedSets = new ArrayList<>(Arrays.asList(new HashSet<>(), new HashSet<>()));

        queues.get(0).add(a);
        queues.get(1).add(b);

        visitedSets.get(0).add(a);
        visitedSets.get(1).add(b);

        for (int ind = 0; !queues.get(0).isEmpty() || !queues.get(1).isEmpty(); ind ^= 1) {
            if (!queues.get(ind).isEmpty()) {
                V node = queues.get(ind).poll();

                if (!visitedSets.get(0).contains(node) || !visitedSets.get(1).contains(node))
                    for (E edge : graph.incomingEdgesOf(node)) {
                        if (graph.getEdgeTarget(edge).equals(node)) {
                            V source = graph.getEdgeSource(edge);

                            if (!visitedSets.get(ind).contains(source)) {
                                queues.get(ind).add(source);
                                visitedSets.get(ind).add(source);
                            }
                        }
                    }
            }
        }
        return visitedSets;
    }

    /**
     * Checks whether both {@code a} and {@code b} belong to the specified graph
     *
     * @param a first node
     * @param b second node
     */
    private void checkNodes(V a, V b)
    {
        if (!graph.containsVertex(a))
            throw new IllegalArgumentException("invalid vertex: " + a);

        if (!graph.containsVertex(b))
            throw new IllegalArgumentException("invalid vertex: " + b);
    }
}
