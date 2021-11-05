/*
 * (C) Copyright 2005-2021, by John V Sichi and Contributors.
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
package org.jgrapht.graph;

import java.util.*;

/**
 * A factory for edge sets. This interface allows the creator of a graph to choose the
 * {@link java.util.Set} implementation used internally by the graph to maintain sets of edges. This
 * provides control over performance tradeoffs between memory and CPU usage.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author John V. Sichi
 */
public interface EdgeSetFactory<V, E>
{
    /**
     * Create a new edge set for a particular vertex.
     *
     * @param vertex the vertex for which the edge set is being created; sophisticated factories may
     *        be able to use this information to choose an optimal set representation (e.g.
     *        ArrayUnenforcedSet for a vertex expected to have low degree, and LinkedHashSet for a
     *        vertex expected to have high degree)
     *
     * @return new set
     */
    Set<E> createEdgeSet(V vertex);
}
