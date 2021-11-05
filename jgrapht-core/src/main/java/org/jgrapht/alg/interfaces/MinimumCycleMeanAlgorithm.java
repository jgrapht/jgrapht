/*
 * (C) Copyright 2020-2021, by Semen Chudakov and Contributors.
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

import org.jgrapht.GraphPath;

/**
 * The algorithm for finding minimum cycle mean in a graph.
 *
 * <p>
 * Consider a cycle $C$ in a graph. The mean of cycle $C$ is defined as $\lambda
 * (C)=\frac{w(C)}{|C|}$, where $w(C)$ is the total weight of $C$ and $|C|$ is the length of $C$.
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 */
public interface MinimumCycleMeanAlgorithm<V, E>
{
    /**
     * Computes minimum mean among all cycle. Returns {@link Double#POSITIVE_INFINITY} if no cycle
     * has been found.
     *
     * @return minimum mean
     */
    double getCycleMean();

    /**
     * Computes cycle with minimum mean. Returns $null$ if no cycle has been found.
     *
     * @return cycle with minimum mean
     */
    GraphPath<V, E> getCycle();
}
