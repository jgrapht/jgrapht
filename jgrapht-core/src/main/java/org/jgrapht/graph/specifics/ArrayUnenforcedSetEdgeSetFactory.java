/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
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
