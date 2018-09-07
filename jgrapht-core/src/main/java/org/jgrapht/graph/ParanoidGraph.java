/*
 * (C) Copyright 2007-2018, by John V Sichi and Contributors.
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

import java.util.*;

/**
 * ParanoidGraph provides a way to verify that objects added to a graph obey the standard
 * equals/hashCode contract. It can be used to wrap an underlying graph to be verified. Note that
 * the verification is very expensive, so ParanoidGraph should only be used during debugging.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author John Sichi
 */
public class ParanoidGraph<V, E>
    extends
    GraphDelegator<V, E>
{
    private static final long serialVersionUID = 5075284167422166539L;

    /**
     * Create a new paranoid graph.
     * 
     * @param g the underlying wrapped graph
     */
    public ParanoidGraph(Graph<V, E> g)
    {
        super(g);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        verifyAdd(edgeSet(), e);
        return super.addEdge(sourceVertex, targetVertex, e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addVertex(V v)
    {
        verifyAdd(vertexSet(), v);
        return super.addVertex(v);
    }

    private static <T> void verifyAdd(Set<T> set, T t)
    {
        for (T o : set) {
            if (o == t) {
                continue;
            }
            if (o.equals(t) && (o.hashCode() != t.hashCode())) {
                throw new IllegalArgumentException(
                    "ParanoidGraph detected objects " + "o1 (hashCode=" + o.hashCode()
                        + ") and o2 (hashCode=" + t.hashCode() + ") where o1.equals(o2) "
                        + "but o1.hashCode() != o2.hashCode()");
            }
        }
    }
}

// End ParanoidGraph.java
