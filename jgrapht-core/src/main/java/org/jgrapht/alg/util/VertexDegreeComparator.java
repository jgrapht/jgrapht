/*
 * (C) Copyright 2003-2026, by Linda Buisman and Contributors.
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
package org.jgrapht.alg.util;

import org.jgrapht.*;

import java.util.*;

/**
 * Compares two vertices based on their degree.
 *
 * <p>
 * Used by greedy algorithms that need to sort vertices by their degree. Two vertices are considered
 * equal if their degrees are equal.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Linda Buisman
 *
 */
public abstract class VertexDegreeComparator<V, E> implements Comparator<V>
{
    private VertexDegreeComparator()
    {
    }

    /**
     * Returns a {@link Comparator} that compares vertices by their degrees in the specified graph.
     * <p>
     * The comparator compares in ascending order of degrees (lower degree first). To obtain a
     * comparator that compares in descending order call {@link Comparator#reversed()} on the
     * returned comparator.
     * </p>
     *
     * @param <V> the graph vertex type
     * @param g graph with respect to which the degree is calculated.
     * @return a {@code Comparator} to compare vertices by their degree in ascending order
     */
    public static <V> Comparator<V> of(Graph<V, ?> g)
    {
        return Comparator.comparingInt(g::degreeOf);
    }
}
