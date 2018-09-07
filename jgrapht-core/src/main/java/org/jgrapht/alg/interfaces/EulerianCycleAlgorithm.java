/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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
 * @since October 2016
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
