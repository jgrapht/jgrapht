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

import java.util.*;

/**
 * An algorithm which computes $k$-shortest paths between vertices.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public interface KShortestPathAlgorithm<V, E>
{

    /**
     * Get a list of k-shortest paths from a source vertex to a sink vertex. If no such paths exist
     * this method returns an empty list.
     * 
     * @param source the source vertex
     * @param sink the target vertex
     * @param k the number of shortest paths to return
     * @return a list of the k-shortest paths
     */
    List<GraphPath<V, E>> getPaths(V source, V sink, int k);

}
