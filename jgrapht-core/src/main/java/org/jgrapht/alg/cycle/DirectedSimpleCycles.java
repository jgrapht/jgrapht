/*
 * (C) Copyright 2013-2021, by Nikolay Ognyanov and Contributors.
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
