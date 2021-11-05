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
