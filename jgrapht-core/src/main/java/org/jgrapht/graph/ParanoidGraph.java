/*
 * (C) Copyright 2007-2018, by John V Sichi and Contributors.
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
