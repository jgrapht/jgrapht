/*
 * (C) Copyright 2007-2021, by France Telecom and Contributors.
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

import org.jgrapht.util.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Helper for {@link MaskSubgraph}.
 *
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty()
    {
        return !iterator().hasNext();
    }
}
