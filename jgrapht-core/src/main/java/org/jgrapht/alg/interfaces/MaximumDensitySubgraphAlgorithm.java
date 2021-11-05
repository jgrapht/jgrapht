/*
 * (C) Copyright 2018-2021, by Andre Immig and Contributors.
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

import org.jgrapht.*;

/**
 * Interface for algorithms computing the maximum density subgraph
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Andre Immig
 */
public interface MaximumDensitySubgraphAlgorithm<V, E>
{

    /**
     * Calculate a maximum density subgraph
     *
     * @return the maximum density subgraph
     */
    Graph<V, E> calculateDensest();

    /**
     * Computes density of a maximum density subgraph.
     *
     * @return the actual density of the maximum density subgraph
     */
    double getDensity();

}
