/*
 * (C) Copyright 2005-2018, by John V Sichi and Contributors.
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

// End EdgeSetFactory.java
