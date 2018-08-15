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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.GraphMapping;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.AsGraphUnion;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.*;

/**
 * This is an implementation of the AHU algorithm for detecting an (unweighted) isomorphism between two rooted forests.
 * Please see <a href="http://mathworld.wolfram.com/GraphIsomorphism.html">mathworld.wolfram.com</a> for a complete
 * definition of the isomorphism problem for general graphs.
 *
 * <p>
 *     The original algorithm was first presented in "Alfred V. Aho and John E. Hopcroft. 1974.
 *     The Design and Analysis of Computer Algorithms (1st ed., page 84). Addison-Wesley
 *     Longman Publishing Co., Inc., Boston, MA, USA."
 * </p>
 *
 * <p>
 *     This implementation runs in linear time (in the number of vertices of the input forests)
 *     while using a linear amount of memory.
 * </p>
 *
 * <p>
 *      For an implementation that supports rooted trees see {@link AHURootedTreeIsomorphismInspector} and for one
 *      for unrooted trees see {@link AHUUnrootedTreeIsomorphismInspector}.
 * </p>
 *
 * <p>
 *     Note: This implementation requires the input graphs to have valid vertex suppliers
 *     (see {@link Graph#getVertexSupplier()}).
 * </p>
 *
 * <p>
 *     Note: This inspector only returns a single mapping (chosen arbitrarily) rather than all possible mappings.
 * </p>
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 *
 * @author Alexandru Valeanu
 */
public class AHUForestIsomorphismInspector<V, E> implements IsomorphismInspector<V, E> {
    private final Graph<V, E> forest1;
    private final Graph<V, E> forest2;

    private final Set<V> roots1;
    private final Set<V> roots2;

    private boolean computed = false;
    private IsomorphicGraphMapping<V, E> isomorphicMapping;

    /**
     * Construct a new AHU rooted forest isomorphism inspector.
     *
     * Note: The constructor does NOT check if the input forests are valid trees.
     *
     * @param forest1 the first rooted forest
     * @param roots1 the roots of the first forest
     * @param forest2 the second rooted forest
     * @param roots2 the roots of the second forest
     * @throws NullPointerException if {@code forest1} or {@code forest2} is {@code null}
     * @throws NullPointerException if {@code roots1} or {@code roots2} is {@code null}
     * @throws IllegalArgumentException if {@code forest1} or {@code forest2} is empty
     * @throws IllegalArgumentException if {@code roots1} or {@code roots2}  is empty
     * @throws IllegalArgumentException if {@code roots1} or {@code roots2} contain an invalid vertex
     */
    public AHUForestIsomorphismInspector(Graph<V, E> forest1, Set<V> roots1, Graph<V, E> forest2, Set<V> roots2){
        validateForest(forest1, roots1);
        this.forest1 = forest1;
        this.roots1 = roots1;

        validateForest(forest2, roots2);
        this.forest2 = forest2;
        this.roots2 = roots2;
    }

    private void validateForest(Graph<V, E> forest, Set<V> roots){
        assert GraphTests.isSimple(forest);
        Objects.requireNonNull(forest, "input forest cannot be null");
        Objects.requireNonNull(roots, "set of roots cannot be null");

        if (forest.vertexSet().isEmpty()){
            throw new IllegalArgumentException("input forest cannot be empty");
        }

        if (roots.isEmpty()){
            throw new IllegalArgumentException("set of roots cannot be empty");
        }

        if (!forest.vertexSet().containsAll(roots)){
            throw new IllegalArgumentException("root not contained in forest");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<GraphMapping<V, E>> getMappings() {
        GraphMapping<V, E> iterMapping = getMapping();

        if (iterMapping == null)
            return Collections.emptyIterator();
        else
            return Collections.singletonList(iterMapping).iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isomorphismExists(){
        return getMapping() != null;
    }

    private Pair<V, Graph<V, E>> createSingleRootGraph(Graph<V, E> forest, Set<V> roots){
        Graph<V, E> freshForest = GraphTypeBuilder.forGraph(forest).weighted(false).buildGraph();

        roots.forEach(freshForest::addVertex);
        V freshVertex = freshForest.addVertex();

        for (V root: roots)
            freshForest.addEdge(freshVertex, root);

        return Pair.of(freshVertex, new AsGraphUnion<>(freshForest, forest));
    }

    /**
     * Get an isomorphism between the input forests or {@code null} if none exists.
     *
     * @return isomorphic mapping, {@code null} is none exists
     */
    public IsomorphicGraphMapping<V, E> getMapping(){
        if (computed) {
            return isomorphicMapping;
        }

        if (roots1.size() == 1 && roots2.size() == 1) {
            V root1 = roots1.iterator().next();
            V root2 = roots2.iterator().next();

            isomorphicMapping = new AHURootedTreeIsomorphismInspector<>(forest1, root1, forest2, root2).getMapping();
        }
        else{
            Pair<V, Graph<V, E>> pair1 = createSingleRootGraph(forest1, roots1);
            Pair<V, Graph<V, E>> pair2 = createSingleRootGraph(forest2, roots2);

            V fresh1 = pair1.getFirst();
            Graph<V, E> freshForest1 = pair1.getSecond();

            V fresh2 = pair2.getFirst();
            Graph<V, E> freshForest2 = pair2.getSecond();

            IsomorphicGraphMapping<V, E> mapping =
                    new AHURootedTreeIsomorphismInspector<>(freshForest1, fresh1, freshForest2, fresh2).getMapping();

            if (mapping != null){
                Map<V, V> newForwardMapping = new HashMap<>(mapping.getForwardMapping());
                Map<V, V> newBackwardMapping = new HashMap<>(mapping.getBackwardMapping());

                // remove the mapping from fresh1 to fresh 2 (and vice-versa)
                newForwardMapping.remove(fresh1);
                newBackwardMapping.remove(fresh2);

                isomorphicMapping =
                        new IsomorphicGraphMapping<>(newForwardMapping, newBackwardMapping, forest1, forest2);
            }
        }

        computed = true;
        return isomorphicMapping;
    }
}
