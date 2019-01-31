/*
 * (C) Copyright 2015-2018, by Joris Kinable, Jon Robison, Thomas Breitbart and Contributors.
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

/**
 * Interface for an admissible heuristic used in A* search.
 *
 * @param <V> vertex type
 *
 * @author Joris Kinable
 * @author Jon Robison
 * @author Thomas Breitbart
 */
public interface AStarAdmissibleHeuristic<V>
{
    /**
     * An admissible "heuristic estimate" of the distance from $x$, the sourceVertex, to the goal
     * (usually denoted $h(x)$). This is the good guess function which must never overestimate the
     * distance.
     * 
     * @param sourceVertex the source vertex
     * @param targetVertex the target vertex
     * 
     * @return an estimate of the distance from the source to the target vertex
     */
    double getCostEstimate(V sourceVertex, V targetVertex);

}
