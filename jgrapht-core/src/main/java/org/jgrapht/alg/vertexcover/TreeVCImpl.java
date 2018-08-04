/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.vertexcover;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexCoverAlgorithm;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculates a <a href = "http://mathworld.wolfram.com/MinimumVertexCover.html">minimum
 * cardinality vertex cover</a> in a <a href="http://mathworld.wolfram.com/Tree.html">tree</a> or
 * <a href="http://mathworld.wolfram.com/Forest.html">forest</a>.
 *
 * <pre>
 * {@code
 *    Algorithm overview:
 *    VERTEX-COVER-TREES($G$)
 *      $C = \empty$
 *
 *      while $\exists$ leaves in $G$
 *          Add all parents to $C$
 *          Remove all leaves and their parents from $G$
 *
 *      return $C$}
 * </pre>
 *
 * The minimum vertex cover for a tree/forest is computed in $O(|V| + |E|)$ time.
 *
 * All the methods in this class are invoked in a lazy fashion, meaning that computations are only
 * started once the method gets invoked.
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 *
 * @author Alexandru Valeanu
 */
public class TreeVCImpl<V, E> implements VertexCoverAlgorithm<V> {

    // Input graph
    private final Graph<V, E> graph;

    // Input set of roots (one root per tree)
    private final Set<V> roots;

    // Cached minimum vertex cover
    private VertexCover<V> minVertexCover;

    /**
     * Creates a new TreeVCImpl instance.
     *
     * <p>
     *      Note: The constructor does NOT check if the input graph is a valid tree.
     * </p>
     *
     * <p>
     *      Note: The vertex cover is computed lazily, when needed.
     * </p>
     *
     * @param graph the input graph
     * @param root the input root
     * @throws NullPointerException if {@code graph} is {@code null}
     * @throws NullPointerException if {@code root} is {@code null}
     * @throws IllegalArgumentException if {@code root} is an invalid vertex
     */
    public TreeVCImpl(Graph<V, E> graph, V root){
        this(graph, Collections.singleton(Objects.requireNonNull(root, "root cannot be null")));
    }

    /**
     * Creates a new TreeVCImpl instance.
     *
     * <p>
     *      Note: The constructor does NOT check if the input graph is a valid tree.
     * </p>
     *
     * <p>
     *     Note: If two roots correspond to the same tree, an error will be thrown.
     * </p>
     *
     * <p>
     *      Note: The vertex cover is computed lazily, when needed.
     * </p>
     *
     * @param graph the input graph
     * @param roots the input root
     * @throws NullPointerException if {@code graph} is {@code null}
     * @throws NullPointerException if {@code roots} is {@code null}
     * @throws IllegalArgumentException if {@code roots} is empty is {@code graph} is not empty
     * @throws IllegalArgumentException if {@code roots} contains an invalid vertex
     */
    public TreeVCImpl(Graph<V, E> graph, Set<V> roots) {
        this.graph = Objects.requireNonNull(graph, "graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "set of roots cannot be null");

        if (!this.graph.vertexSet().isEmpty() && this.roots.isEmpty())
            throw new IllegalArgumentException("set of roots cannot be empty");

        if (!this.graph.vertexSet().containsAll(roots))
            throw new IllegalArgumentException("invalid set of roots");
    }

    // Populate parent map by iterating through the BFS tree of root
    private void bfs(V root, Set<V> visited, Map<V, V> parent){
        BreadthFirstIterator<V, E> bfs = new BreadthFirstIterator<>(graph, root);

        while (bfs.hasNext()) {
            V u = bfs.next();
            visited.add(u);
            V p = bfs.getParent(u);

            if (p != null)
                parent.put(u, p);
        }
    }

    private void computeMinimumVertexCover(){
        Map<V, V> parentMap =  new HashMap<>();
        Set<V> visited = new HashSet<>();

        for (V root: roots)
            if (!visited.contains(root))
                bfs(root, visited, parentMap);
            else
                throw new IllegalArgumentException("multiple roots in the same tree");

        Set<V> allParents = new HashSet<>(parentMap.values());

        // A vertex v is a leaf iff there is no vertex u such that v is the parent of u
        Set<V> leaves = graph.vertexSet().stream().filter(x -> !allParents.contains(x)).collect(Collectors.toSet());
        Set<V> vc = new HashSet<>();
        Set<V> deleted = new HashSet<>();

        do {
            Set<V> parents = new HashSet<>();
            Set<V> grandparents = new HashSet<>();

            for (V leaf: leaves){
                V p = parentMap.get(leaf);

                if (p != null) {
                    parents.add(p);
                    V pp = parentMap.get(p);

                    if (pp != null && !deleted.contains(pp))
                        grandparents.add(pp);
                }
            }

            deleted.addAll(leaves);
            deleted.addAll(parents);

            vc.addAll(parents);

            grandparents.removeAll(parents);
            grandparents.removeAll(grandparents.stream().map(parentMap::get).collect(Collectors.toSet()));

            leaves = grandparents;

        } while (!leaves.isEmpty());

        minVertexCover = new VertexCoverImpl<>(vc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VertexCover<V> getVertexCover() {
        if (minVertexCover == null) {
            computeMinimumVertexCover();
        }

        return minVertexCover;
    }
}
