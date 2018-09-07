/*
 * (C) Copyright 2013-2018, by Nikolay Ognyanov and Contributors.
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
package org.jgrapht.alg.cycle;

import java.util.*;

/**
 * A common interface for classes implementing algorithms for enumeration of the simple cycles of a
 * directed graph.
 *
 * @param <V> the vertex type.
 * @param <E> the edge type.
 *
 * @author Nikolay Ognyanov
 */
public interface DirectedSimpleCycles<V, E>
{
    /**
     * Find the simple cycles of the graph.
     *
     * @return The list of all simple cycles. Possibly empty but never <code>null</code>.
     */
    List<List<V>> findSimpleCycles();
}

// End DirectedSimpleCycles.java
