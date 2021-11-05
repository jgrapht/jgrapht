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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.*;

import java.util.*;

/**
 * This is an implementation of the AHU algorithm for detecting an (unweighted) isomorphism between
 * two unrooted trees. Please see
 * <a href="http://mathworld.wolfram.com/GraphIsomorphism.html">mathworld.wolfram.com</a> for a
 * complete definition of the isomorphism problem for general graphs.
 *
 * <p>
 * The original algorithm was first presented in "Alfred V. Aho and John E. Hopcroft. 1974. The
 * Design and Analysis of Computer Algorithms (1st ed.). Addison-Wesley Longman Publishing Co.,
 * Inc., Boston, MA, USA."
 * </p>
 *
 * <p>
 * This implementation runs in linear time (in the number of vertices of the input trees) while
 * using a linear amount of memory.
 * </p>
 *
 * <p>
 * For an implementation that supports rooted trees see {@link AHURootedTreeIsomorphismInspector}.
 * <br>
 * For an implementation that supports rooted forests see {@link AHUForestIsomorphismInspector}.
 * </p>
 *
 * <p>
 * Note: This inspector only returns a single mapping (chosen arbitrarily) rather than all possible
 * mappings.
 * </p>
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 *
 * @author Alexandru Valeanu
 */
public class AHUUnrootedTreeIsomorphismInspector<V, E>
    implements
    IsomorphismInspector<V, E>
{

    private final Graph<V, E> tree1;
    private final Graph<V, E> tree2;

    private boolean computed;
    private AHURootedTreeIsomorphismInspector<V, E> ahuRootedTreeIsomorphismInspector;

    /**
     * Construct a new AHU unrooted tree isomorphism inspector.
     *
     * Note: The constructor does NOT check if the input trees are valid.
     *
     * @param tree1 the first tree
     * @param tree2 the second tree
     * @throws NullPointerException if {@code tree1} or {@code tree2} is {@code null}
     * @throws IllegalArgumentException if {@code tree1} or {@code tree2} is not undirected
     * @throws IllegalArgumentException if {@code tree1} or {@code tree2} is empty
     */
    public AHUUnrootedTreeIsomorphismInspector(Graph<V, E> tree1, Graph<V, E> tree2)
    {
        validateTree(tree1);
        this.tree1 = tree1;

        validateTree(tree2);
        this.tree2 = tree2;
    }

    private void validateTree(Graph<V, E> tree)
    {
        GraphTests.requireUndirected(tree);
        assert GraphTests.isSimple(tree);

        if (tree.vertexSet().isEmpty()) {
            throw new IllegalArgumentException("tree cannot be empty");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<GraphMapping<V, E>> getMappings()
    {
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
    public boolean isomorphismExists()
    {
        if (computed) {
            if (ahuRootedTreeIsomorphismInspector != null) {
                return ahuRootedTreeIsomorphismInspector.isomorphismExists();
            } else {
                return false;
            }
        }

        computed = true;

        TreeMeasurer<V, E> treeMeasurer1 = new TreeMeasurer<>(tree1);
        List<V> centers1 = new ArrayList<>(treeMeasurer1.getGraphCenter());

        TreeMeasurer<V, E> treeMeasurer2 = new TreeMeasurer<>(tree2);
        List<V> centers2 = new ArrayList<>(treeMeasurer2.getGraphCenter());

        if (centers1.size() == 1 && centers2.size() == 1) {
            ahuRootedTreeIsomorphismInspector = new AHURootedTreeIsomorphismInspector<>(
                tree1, centers1.get(0), tree2, centers2.get(0));
        } else if (centers1.size() == 2 && centers2.size() == 2) {
            ahuRootedTreeIsomorphismInspector = new AHURootedTreeIsomorphismInspector<>(
                tree1, centers1.get(0), tree2, centers2.get(0));

            if (!ahuRootedTreeIsomorphismInspector.isomorphismExists()) {
                ahuRootedTreeIsomorphismInspector = new AHURootedTreeIsomorphismInspector<>(
                    tree1, centers1.get(1), tree2, centers2.get(0));
            }
        } else {
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
    public IsomorphicGraphMapping<V, E> getMapping()
    {
        if (isomorphismExists()) {
            return ahuRootedTreeIsomorphismInspector.getMapping();
        } else {
            return null;
        }
    }
}
