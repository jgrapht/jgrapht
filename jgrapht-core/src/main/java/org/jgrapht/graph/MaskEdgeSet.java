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

import org.jgrapht.*;
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Helper for {@link MaskSubgraph}.
 *
 * @since July 5, 2007
 */
class MaskEdgeSet<V, E>
    extends
    AbstractSet<E>
    implements
    Serializable
{
    private static final long serialVersionUID = 4208908842850100708L;

    private final Graph<V, E> graph;
    private final Set<E> edgeSet;
    private final Predicate<V> vertexMask;
    private final Predicate<E> edgeMask;

    public MaskEdgeSet(
        Graph<V, E> graph, Set<E> edgeSet, Predicate<V> vertexMask, Predicate<E> edgeMask)
    {
        this.graph = graph;
        this.edgeSet = edgeSet;
        this.vertexMask = vertexMask;
        this.edgeMask = edgeMask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o)
    {
        if (!edgeSet.contains(o)) {
            return false;
        }
        E e = TypeUtil.uncheckedCast(o);

        return !edgeMask.test(e) && !vertexMask.test(graph.getEdgeSource(e))
            && !vertexMask.test(graph.getEdgeTarget(e));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<E> iterator()
    {
        return edgeSet
            .stream()
            .filter(
                e -> !edgeMask.test(e) && !vertexMask.test(graph.getEdgeSource(e))
                    && !vertexMask.test(graph.getEdgeTarget(e)))
            .iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return (int) edgeSet
            .stream()
            .filter(
                e -> !edgeMask.test(e) && !vertexMask.test(graph.getEdgeSource(e))
                    && !vertexMask.test(graph.getEdgeTarget(e)))
            .count();
    }

}

// End MaskEdgeSet.java
