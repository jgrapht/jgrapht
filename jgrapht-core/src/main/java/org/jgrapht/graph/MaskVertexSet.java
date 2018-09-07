/*
 * (C) Copyright 2007-2018, by France Telecom and Contributors.
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

import org.jgrapht.util.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Helper for {@link MaskSubgraph}.
 *
 * @since July 5, 2007
 */
class MaskVertexSet<V>
    extends
    AbstractSet<V>
    implements
    Serializable
{
    private static final long serialVersionUID = 3751931017141472763L;

    private final Set<V> vertexSet;
    private final Predicate<V> mask;

    public MaskVertexSet(Set<V> vertexSet, Predicate<V> mask)
    {
        this.vertexSet = vertexSet;
        this.mask = mask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o)
    {
        if (!vertexSet.contains(o)) {
            return false;
        }
        V v = TypeUtil.uncheckedCast(o);
        return !mask.test(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<V> iterator()
    {
        return vertexSet.stream().filter(mask.negate()).iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return (int) vertexSet.stream().filter(mask.negate()).count();
    }

}

// End MaskVertexSet.java
