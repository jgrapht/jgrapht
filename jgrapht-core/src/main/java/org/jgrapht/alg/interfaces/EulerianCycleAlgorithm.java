/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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
 * Computes an Eulerian cycle of an Eulerian graph. An
 * <a href="http://mathworld.wolfram.com/EulerianGraph.html">Eulerian graph</a> is a graph
 * containing an <a href="http://mathworld.wolfram.com/EulerianCycle.html">Eulerian cycle</a>.
 * 
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public interface EulerianCycleAlgorithm<V, E>
{

    /**
     * Compute an Eulerian cycle of a graph.
     * 
     * @param graph the input graph
     * @return an Eulerian cycle
     * @throws IllegalArgumentException in case the graph is not Eulerian
     */
    GraphPath<V, E> getEulerianCycle(Graph<V, E> graph);

}
