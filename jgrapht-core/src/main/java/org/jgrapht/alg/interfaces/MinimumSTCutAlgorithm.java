/*
 * (C) Copyright 2016-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.interfaces;

import java.util.*;

/**
 * Given a weighted graph $G(V,E)$ (directed or undirected). This class computes a minimum $s-t$
 * cut. A cut is a partitioning of the vertices into two disjoint sets $S, T $such that $s \in S, t
 * \in T$, and that $S \cup T = V%. The <i>capacity</i> of a cut is defined as the sum of the
 * weights of the edges from $S$ to $T$. In case of a directed graph, only the edges with their tail
 * in $S$ and their head in $T$ are counted. In cased of a undirected graph, all edges with one
 * endpoint in $S$ and one endpoint in $T$ are counted. For a given $s$ and $t$, this class computes
 * two partitions $S$ and $T$ such that the capacity of the cut is minimized. When each edge has
 * equal weight, by definition this class minimizes the number of edges from $S$ to $T$.
 *
 * Note: it is not recommended to use this class to calculate the overall minimum cut in a graph by
 * iteratively invoking this class for all source-sink pairs. This is computationally expensive.
 * Instead, use the StoerWagnerMinimumCut implementation.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 */
public interface MinimumSTCutAlgorithm<V, E>
{

    /**
     * Computes a minimum capacity $s-t$ cut.
     * 
     * @param source s
     * @param sink t
     * @return capacity of the cut
     */
    double calculateMinCut(V source, V sink);

    /**
     * Returns the capacity of the cut obtained after the last invocation of
     * {@link #calculateMinCut(Object, Object)}
     * 
     * @return capacity of the cut
     */
    double getCutCapacity();

    /**
     * Returns the source partition $S$, $s \in S$, of the cut obtained after the last invocation of
     * {@link #calculateMinCut(Object, Object)}
     * 
     * @return source partition S
     */
    Set<V> getSourcePartition();

    /**
     * Returns the sink partition $T$, $t \in T$, of the cut obtained after the last invocation of
     * {@link #calculateMinCut(Object, Object)}
     * 
     * @return source partition T
     */
    Set<V> getSinkPartition();

    /**
     * Returns the set of edges which run from $S$ to $T$, in the $s-t$ cut obtained after the last
     * invocation of {@link #calculateMinCut(Object, Object)} In case of a directed graph, only the
     * edges with their tail in $S$ and their head in $T$ are returned. In cased of a undirected
     * graph, all edges with one endpoint in $S$ and one endpoint in $T$ are returned.
     * 
     * @return set of edges which run from $S$ to $T$
     */
    Set<E> getCutEdges();

}
