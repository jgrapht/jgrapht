/*
 * (C) Copyright 2015-2017, by Wil Selwood and Contributors.
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
package org.jgrapht.io;

import java.util.*;

/**
 * Provider of edges
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public interface EdgeProvider<V, E>
{
    /**
     * Construct an edge.
     *
     * @param from the source vertex
     * @param to the target vertex
     * @param label the label of the edge.
     * @param attributes extra attributes for the edge.
     *
     * @return the edge
     */
    E buildEdge(V from, V to, String label, Map<String, Attribute> attributes);
}
