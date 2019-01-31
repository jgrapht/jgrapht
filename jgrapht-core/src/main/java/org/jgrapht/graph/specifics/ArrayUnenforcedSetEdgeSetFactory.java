/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
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
package org.jgrapht.graph.specifics;

import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;

/**
 * An edge set factory which creates {@link ArrayUnenforcedSet} of size 1, suitable for small degree
 * vertices.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Barak Naveh
 */
public class ArrayUnenforcedSetEdgeSetFactory<V, E>
    implements
    EdgeSetFactory<V, E>,
    Serializable
{
    private static final long serialVersionUID = 5936902837403445985L;

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> createEdgeSet(V vertex)
    {
        // NOTE: use size 1 to keep memory usage under control
        // for the common case of vertices with low degree
        return new ArrayUnenforcedSet<>(1);
    }

}
