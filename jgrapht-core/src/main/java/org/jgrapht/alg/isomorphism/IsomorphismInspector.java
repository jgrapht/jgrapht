/*
 * (C) Copyright 2015-2021, by Fabian Späh and Contributors.
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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.*;

import java.util.*;

/**
 * General interface for graph and subgraph isomorphism.
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 */
public interface IsomorphismInspector<V, E>
{
    /**
     * Get an iterator over all calculated (isomorphic) mappings between two graphs.
     * 
     * @return an iterator over all calculated (isomorphic) mappings between two graphs
     * @throws IsomorphismUndecidableException if the inspector cannot decide whether the graphs are
     *         isomorphic
     */
    Iterator<GraphMapping<V, E>> getMappings();

    /**
     * Check if an isomorphism exists.
     *
     * @return true if there is an isomorphism, false if there is no isomorphism
     * @throws IsomorphismUndecidableException if the inspector cannot decide whether the graphs are
     *         isomorphic
     */
    boolean isomorphismExists();
}
