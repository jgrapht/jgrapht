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
import org.jgrapht.alg.shortestpath.TreeMeasurer;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

/**
 * This is an implementation of the AHU algorithm for detecting an (unweighted) isomorphism between two unrooted trees.
 * Please see <a href="http://mathworld.wolfram.com/GraphIsomorphism.html">mathworld.wolfram.com</a> for a complete
 * definition of the isomorphism problem for general graphs.
 *
 * <p>
 *     The original algorithm was first presented in "Alfred V. Aho and John E. Hopcroft. 1974.
 *     The Design and Analysis of Computer Algorithms (1st ed.). Addison-Wesley Longman Publishing Co., Inc., Boston, MA, USA."
 * </p>
 *
 * <p>
 *     This implementation runs in linear time (in the number of vertices of the input trees)
 *     while using a linear amount of memory.
 * </p>
 *
 * <p>
 *      For an implementation that supports rooted trees see {@link AHURootedTreeIsomorphismInspector}. <br>
 *      For an implementation that supports rooted forests see {@link AHUForestIsomorphismInspector}.
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
public class AHUUnrootedTreeIsomorphismInspector<V, E> implements IsomorphismInspector<V, E> {

    private final Graph<V, E> tree1;
    private final Graph<V, E> tree2;

    private AHURootedTreeIsomorphismInspector<V, E> ahuRootedTreeIsomorphismInspector;

    /**
     * Construct a new AHU unrooted tree isomorphism inspector.
     *
     * Note: The constructor does NOT check if the input trees are valid.
     *
     * @param tree1 the first tree
     * @param tree2 the second tree
     * @throws NullPointerException if {@code tree1} is {@code null}
     * @throws NullPointerException if {@code tree2} is {@code null}
     * @throws IllegalArgumentException if {@code tree1} is empty
     * @throws IllegalArgumentException if {@code tree2} is empty
     */
    public AHUUnrootedTreeIsomorphismInspector(Graph<V, E> tree1, Graph<V, E> tree2){
        this.tree1 = Objects.requireNonNull(tree1, "tree1 cannot be null");
        this.tree2 = Objects.requireNonNull(tree2, "tree2 cannot be null");

        if (tree1.vertexSet().isEmpty()){
            throw new IllegalArgumentException("tree1 cannot be empty");
        }

        if (tree2.vertexSet().isEmpty()){
            throw new IllegalArgumentException("tree2 cannot be empty");
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
    @SuppressWarnings("unchecked")
    public boolean isomorphismExists(){
        if (ahuRootedTreeIsomorphismInspector != null){
            return ahuRootedTreeIsomorphismInspector.isomorphismExists();
        }

        TreeMeasurer<V, E> treeMeasurer1 = new TreeMeasurer<>(tree1);
        V[] centers1 = (V[]) treeMeasurer1.getGraphCenter().toArray();

        TreeMeasurer<V, E> treeMeasurer2 = new TreeMeasurer<>(tree2);
        V[] centers2 = (V[]) treeMeasurer2.getGraphCenter().toArray();

        if (centers1.length == 1 && centers2.length == 1){
            ahuRootedTreeIsomorphismInspector =
                    new AHURootedTreeIsomorphismInspector<>(tree1, centers1[0], tree2, centers2[0]);
        }
        else if (centers1.length == 2 && centers2.length == 2){
            ahuRootedTreeIsomorphismInspector =
                    new AHURootedTreeIsomorphismInspector<>(tree1, centers1[0], tree2, centers2[0]);

            if (!ahuRootedTreeIsomorphismInspector.isomorphismExists()){
                ahuRootedTreeIsomorphismInspector =
                        new AHURootedTreeIsomorphismInspector<>(tree1, centers1[1], tree2, centers2[0]);
            }
        }
        else{
            // different number of centers
            return false;
        }

        return ahuRootedTreeIsomorphismInspector.isomorphismExists();
    }

    /**
     * Get an isomorphism between the input trees or {@code null} if none exists.
     *
     * @return isomorphic mapping, {@code null} is none exists
     */
    public IsomorphicGraphMapping<V, E> getMapping(){
        isomorphismExists();
        return ahuRootedTreeIsomorphismInspector.getMapping();
    }
}
