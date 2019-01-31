/*
 * (C) Copyright 2015-2018, by Christophe Thiebaud and Contributors.
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
package org.jgrapht.alg;

import org.jgrapht.*;

import java.util.*;

/**
 * An implementation of Harry Hsu's
 * <a href="https://en.wikipedia.org/wiki/Transitive_reduction">transitive reduction algorithm</a>.
 *
 * <p>
 * cf. <a href="http://projects.csail.mit.edu/jacm/References/hsu1975:11.html">Harry Hsu. "An
 * algorithm for finding a minimal equivalent graph of a digraph.", Journal of the ACM, 22(1):11-16,
 * January 1975.</a>
 * </p>
 *
 * <p>
 * This is a port from a python example by Michael Clerx, posted as an answer to a question about
 * <a href= "http://stackoverflow.com/questions/1690953/transitive-reduction-algorithm-pseudocode">
 * transitive reduction algorithm pseudocode</a> on <a href="http://stackoverflow.com">Stack
 * Overflow</a>
 * </p>
 *
 * @author Christophe Thiebaud
 */

public class TransitiveReduction
{
    /**
     * Singleton instance.
     */
    public static final TransitiveReduction INSTANCE = new TransitiveReduction();

    /**
     * Private Constructor.
     */
    private TransitiveReduction()
    {
    }

    /**
     * The matrix passed as input parameter will be transformed into a path matrix.
     *
     * <p>
     * This method is package visible for unit testing, but it is meant as a private method.
     * </p>
     *
     * @param matrix the original matrix to transform into a path matrix
     */
    static void transformToPathMatrix(BitSet[] matrix)
    {
        // compute path matrix
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if (i == j) {
                    continue;
                }
                if (matrix[j].get(i)) {
                    for (int k = 0; k < matrix.length; k++) {
                        if (!matrix[j].get(k)) {
                            matrix[j].set(k, matrix[i].get(k));
                        }
                    }
                }
            }
        }
    }

    /**
     * The path matrix passed as input parameter will be transformed into a transitively reduced
     * matrix.
     *
     * <p>
     * This method is package visible for unit testing, but it is meant as a private method.
     * </p>
     *
     * @param pathMatrix the path matrix to reduce
     */
    static void transitiveReduction(BitSet[] pathMatrix)
    {
        // transitively reduce
        for (int j = 0; j < pathMatrix.length; j++) {
            for (int i = 0; i < pathMatrix.length; i++) {
                if (pathMatrix[i].get(j)) {
                    for (int k = 0; k < pathMatrix.length; k++) {
                        if (pathMatrix[j].get(k)) {
                            pathMatrix[i].set(k, false);
                        }
                    }
                }
            }
        }
    }

    /**
     * This method will remove all transitive edges from the graph passed as input parameter.
     *
     * <p>
     * You may want to clone the graph before, as transitive edges will be pitilessly removed.
     * </p>
     *
     * e.g.
     *
     * <pre>
     * {
     *     &#64;code DirectedGraph&lt;V, T&gt; soonToBePrunedDirectedGraph;
     *
     *     TransitiveReduction.INSTANCE.reduce(soonToBePrunedDirectedGraph);
     *
     *     // pruned !
     * }
     * </pre>
     *
     * @param directedGraph the directed graph that will be reduced transitively
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    public <V, E> void reduce(final Graph<V, E> directedGraph)
    {
        GraphTests.requireDirected(directedGraph, "Graph must be directed");

        final List<V> vertices = new ArrayList<>(directedGraph.vertexSet());

        final int n = vertices.size();

        BitSet[] originalMatrix = new BitSet[n];
        for (int i = 0; i < originalMatrix.length; i++) {
            originalMatrix[i] = new BitSet(n);
        }

        // initialize matrix with zeros
        // 'By default, all bits in the set initially have the value false.'
        // cf. http://docs.oracle.com/javase/7/docs/api/java/util/BitSet.html

        // initialize matrix with edges
        final Set<E> edges = directedGraph.edgeSet();
        for (final E edge : edges) {
            final V v1 = directedGraph.getEdgeSource(edge);
            final V v2 = directedGraph.getEdgeTarget(edge);

            final int v_1 = vertices.indexOf(v1);
            final int v_2 = vertices.indexOf(v2);

            originalMatrix[v_1].set(v_2);
        }

        // create path matrix from original matrix
        final BitSet[] pathMatrix = originalMatrix;

        transformToPathMatrix(pathMatrix);

        // create reduced matrix from path matrix
        final BitSet[] transitivelyReducedMatrix = pathMatrix;

        transitiveReduction(transitivelyReducedMatrix);

        // remove edges from the DirectedGraph which are not in the reduced
        // matrix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (!transitivelyReducedMatrix[i].get(j)) {
                    directedGraph
                        .removeEdge(directedGraph.getEdge(vertices.get(i), vertices.get(j)));
                }
            }
        }
    }
}
