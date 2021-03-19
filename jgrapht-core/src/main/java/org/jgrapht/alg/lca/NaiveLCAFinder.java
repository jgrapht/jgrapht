/*
 * (C) Copyright 2013-2021, by Leo Crawford and Contributors.
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
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;

/**
 * Find the Lowest Common Ancestor of a directed graph.
 *
 * <p>
 * Find the LCA, defined as <i>"Let $G = (V, E)$ be a DAG, and let $x, y \in V$. Let $G_{x,y}$ be
 * the subgraph of $G$ induced by the set of all common ancestors of $x$ and $y$. Define SLCA (x, y)
 * to be the set of out-degree 0 nodes (leafs) in $G_{x,y}$. The lowest common ancestors of $x$ and
 * $y$ are the elements of SLCA (x, y). "</i> from <i> Michael A. Bender, Martín Farach-Colton,
 * Giridhar Pemmasani, Steven Skiena, Pavel Sumazin, Lowest common ancestors in trees and directed
 * acyclic graphs, Journal of Algorithms, Volume 57, Issue 2, 2005, Pages 75-94, ISSN 0196-6774,
 * https://doi.org/10.1016/j.jalgor.2005.08.001.</i>
 *
 * <p>
 * The algorithm:
 *
 * <ol>
 * <li>Find ancestor sets for nodes $a$ and $b$.</li>
 * <li>Find their intersection.</li>
 * <li>Extract leaf nodes from the intersection set.</li>
 * </ol>
 *
 * The algorithm is straightforward in the way it finds the LCA set by definition.
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
    private final Graph<V, E> graph;

    /**
     * Create a new instance of the naive LCA finder.
     *
     * @param graph the input graph
     */
    public NaiveLCAFinder(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireDirected(graph);
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

        Graph<V, E> edgeReversed = new EdgeReversedGraph<>(graph);
        Set<V> aAncestors = getAncestors(edgeReversed, a);
        Set<V> bAncestors = getAncestors(edgeReversed, b);
        Set<V> commonAncestors;

        // optimization trick: save the intersection using the smaller set
        if (aAncestors.size() < bAncestors.size()) {
            aAncestors.retainAll(bAncestors);
            commonAncestors = aAncestors;
        } else {
            bAncestors.retainAll(aAncestors);
            commonAncestors = bAncestors;
        }

        /*
         * Find the set of all non-leaves by iterating through the set of common ancestors. When we
         * encounter a node which is still part of the SLCA(a, b) we remove its parent(s).
         */
        Set<V> leaves = new HashSet<>();
        for (V ancestor : commonAncestors) {
            boolean isLeaf = true;
            for (E edge : graph.outgoingEdgesOf(ancestor)) {
                V target = graph.getEdgeTarget(edge);
                if (commonAncestors.contains(target)) {
                    isLeaf = false;
                    break;
                }
            }
            if (isLeaf) {
                leaves.add(ancestor);
            }
        }

        return leaves;
    }

    /**
     * Returns a set of nodes reachable from the {@code start}.
     *
     * @param graph a graph
     * @param start a node to start from.
     * @return returns a set of nodes reachable from the {@code start}.
     */
    private Set<V> getAncestors(Graph<V, E> graph, V start)
    {
        Set<V> ancestors = new HashSet<>();
        BreadthFirstIterator<V, E> bfs = new BreadthFirstIterator<>(graph, start);
        while (bfs.hasNext()) {
            ancestors.add(bfs.next());
        }
        return ancestors;
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
