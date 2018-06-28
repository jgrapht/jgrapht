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
 * @since June 2018
 */
public class TreeVCImpl<V, E> implements VertexCoverAlgorithm<V> {

    private final Graph<V, E> graph;
    private final Set<V> roots;

    private VertexCover<V> minVertexCover = null;

    /**
     * Creates a new TreeVCImpl instance.
     *
     * @param graph the input graph
     * @param root the input root
     * @throws NullPointerException if {@code graph} is {@code null}
     * @throws NullPointerException if {@code root} is {@code null}
     */
    public TreeVCImpl(Graph<V, E> graph, V root){
        this(graph, Collections.singleton(Objects.requireNonNull(root, "Root cannot be null")));
    }

    /**
     * Creates a new TreeVCImpl instance.
     *
     * @param graph the input graph
     * @param roots the input root
     * @throws NullPointerException if {@code graph} is {@code null}
     * @throws NullPointerException if {@code roots} is {@code null}
     * @throws IllegalArgumentException if {@code roots} is empty
     */
    public TreeVCImpl(Graph<V, E> graph, Set<V> roots) {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "Set of roots cannot be null");

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("Set of roots cannot be empty");
    }

    /*
        Populate parent map by iterating through the BFS tree of root
     */
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
        Map<V, V> parent =  new HashMap<>();
        Set<V> visited = new HashSet<>();

        for (V root: roots)
            if (!visited.contains(root))
                bfs(root, visited, parent);

        Set<V> leaves = new HashSet<>();
        Set<V> vc = new HashSet<>();
        Set<V> deleted = new HashSet<>();

        // A vertex v is a leaf iff there is no vertex u such that v is the parent of u
        for (V v: graph.vertexSet())
            if (!parent.values().contains(v))
                leaves.add(v);
        do {
            Set<V> parents = new HashSet<>();
            Set<V> grandparents = new HashSet<>();

            for (V leaf: leaves){
                V p = parent.get(leaf);

                if (p != null) {
                    parents.add(p);
                    V pp = parent.get(p);

                    if (pp != null && !deleted.contains(pp))
                        grandparents.add(pp);
                }
            }

            deleted.addAll(leaves);
            deleted.addAll(parents);

            vc.addAll(parents);

            grandparents.removeAll(parents);
            grandparents.removeAll(grandparents.stream().map(parent::get).collect(Collectors.toSet()));

            leaves = grandparents;

        } while (!leaves.isEmpty());

        minVertexCover = new VertexCoverImpl<>(vc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VertexCover<V> getVertexCover() {
        if (minVertexCover == null)
            computeMinimumVertexCover();

        return minVertexCover;
    }
}
