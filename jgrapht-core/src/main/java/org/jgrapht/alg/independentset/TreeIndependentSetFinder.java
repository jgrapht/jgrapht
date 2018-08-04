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
package org.jgrapht.alg.independentset;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.IndependentSetAlgorithm;
import org.jgrapht.alg.vertexcover.TreeVCImpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Calculates a <a href = "http://mathworld.wolfram.com/MaximumIndependentVertexSet.html">maximum
 * cardinality independent set</a> in a <a href="http://mathworld.wolfram.com/Tree.html">tree</a> or
 * <a href="http://mathworld.wolfram.com/Forest.html">forest</a>.
 *
 * To compute the maximum independent set, this implementation relies on the {@link TreeVCImpl} to
 * compute a <a href=
 * "http://mathworld.wolfram.com/MinimumVertexCover.html">
 * minimum vertex cover</a>.
 *
 * The maximum cardinality independent set for a tree/forest is computed in $O(|V| + |E|)$ time.
 *
 * All the methods in this class are invoked in a lazy fashion, meaning that computations are only
 * started once the method gets invoked.
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 *
 * @author Alexandru Valeanu
 */
public class TreeIndependentSetFinder<V, E> implements IndependentSetAlgorithm<V> {

    // Input graph
    private final Graph<V, E> graph;

    // Input set of roots
    private final Set<V> roots;

    // Cached independent set
    private IndependentSetAlgorithm.IndependentSet<V> maxIndSet = null;

    /**
     * Creates a new TreeIndependentSetFinder instance.
     *
     * <p>
     *      Note: The constructor does NOT check if the input graph is a valid tree.
     * </p>
     *
     * <p>
     *      Note: The independent set is computed lazily, when needed.
     * </p>
     *
     * @param graph the input graph
     * @param root the input root
     * @throws NullPointerException if {@code graph} is {@code null}
     * @throws NullPointerException if {@code root} is {@code null}
     * @throws IllegalArgumentException if {@code root} is an invalid vertex
     */
    public TreeIndependentSetFinder(Graph<V, E> graph, V root){
        this(graph, Collections.singleton(Objects.requireNonNull(root, "root cannot be null")));
    }

    /**
     * Creates a new TreeIndependentSetFinder instance.<br>
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
     *      Note: The independent set is computed lazily, when needed.
     * </p>
     *
     * @param graph the input graph
     * @param roots the input root
     * @throws NullPointerException if {@code graph} is {@code null}
     * @throws NullPointerException if {@code roots} is {@code null}
     * @throws IllegalArgumentException if {@code roots} is empty
     * @throws IllegalArgumentException if {@code roots} contains an invalid vertex
     */
    public TreeIndependentSetFinder(Graph<V, E> graph, Set<V> roots) {
        this.graph = Objects.requireNonNull(graph, "graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "set of roots cannot be null");

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("set of roots cannot be empty");

        if (!this.graph.vertexSet().containsAll(roots))
            throw new IllegalArgumentException("invalid set of roots");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndependentSet<V> getIndependentSet() {
        if (maxIndSet == null){
            Set<V> vertices = new HashSet<>(graph.vertexSet());
            vertices.removeAll(new TreeVCImpl<>(graph, roots).getVertexCover());
            maxIndSet = new IndependentSetImpl<>(vertices);
        }

        return maxIndSet;
    }
}
