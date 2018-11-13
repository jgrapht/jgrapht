/*
 * (C) Copyright 2005-2018, by Assaf Lehr and Contributors.
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
package org.jgrapht;

/**
 * GraphMapping represents a bidirectional mapping between two graphs (called graph1 and graph2),
 * which allows the caller to obtain the matching vertex or edge in either direction, from graph1 to
 * graph2, or from graph2 to graph1. It does not have to always be a complete bidirectional mapping
 * (it could return null for some lookups).
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Assaf Lehr
 */
public interface GraphMapping<V, E>
{
    /**
     * Gets the mapped value where the key is <code>vertex</code>
     *
     * @param vertex vertex in one of the graphs
     * @param forward if true, uses mapping from graph1 to graph2; if false, use mapping from graph2
     *        to graph1
     *
     * @return corresponding vertex in other graph, or null if none
     */
    V getVertexCorrespondence(V vertex, boolean forward);

    /**
     * Gets the mapped value where the key is <code>edge</code>
     *
     * @param edge edge in one of the graphs
     * @param forward if true, uses mapping from graph1 to graph2; if false, use mapping from graph2
     *        to graph1
     *
     * @return corresponding edge in other graph, or null if none
     */
    E getEdgeCorrespondence(E edge, boolean forward);
}
