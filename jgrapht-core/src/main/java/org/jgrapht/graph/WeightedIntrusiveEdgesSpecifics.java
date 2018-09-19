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
package org.jgrapht.graph;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A weighted variant of the intrusive edges specifics.
 * 
 * <p>
 * The implementation optimizes the use of {@link DefaultWeightedEdge} and subclasses. For other
 * custom user edge types, a map is used to store vertex source, target and weight.
 * 
 * @author Barak Naveh
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class WeightedIntrusiveEdgesSpecifics<V, E>
    extends
    BaseIntrusiveEdgesSpecifics<V, E, IntrusiveWeightedEdge>
    implements
    IntrusiveEdgesSpecifics<V, E>
{
    private static final long serialVersionUID = 5327226615635500554L;

    /**
     * Constructor
     * 
     * @deprecated Since default strategies should be decided at a higher level.
     */
    @Deprecated
    public WeightedIntrusiveEdgesSpecifics()
    {
        this(new LinkedHashMap<>());
    }
    
    /**
     * Constructor
     * 
     * @param map the map to use for storage
     */
    public WeightedIntrusiveEdgesSpecifics(Map<E, IntrusiveWeightedEdge> map)
    {
        super(map);
    }

    @Override
    public boolean add(E e, V sourceVertex, V targetVertex)
    {
        IntrusiveWeightedEdge intrusiveEdge;

        if (e instanceof IntrusiveWeightedEdge) {
            intrusiveEdge = (IntrusiveWeightedEdge) e;
        } else {
            intrusiveEdge = new IntrusiveWeightedEdge();
        }

        intrusiveEdge.source = sourceVertex;
        intrusiveEdge.target = targetVertex;

        return edgeMap.putIfAbsent(e, intrusiveEdge) == null;
    }

    @Override
    public double getEdgeWeight(E e)
    {
        IntrusiveWeightedEdge ie = getIntrusiveEdge(e);
        if (ie == null) {
            throw new IllegalArgumentException("no such edge in graph: " + e.toString());
        }
        return ie.weight;
    }

    @Override
    public void setEdgeWeight(E e, double weight)
    {
        IntrusiveWeightedEdge ie = getIntrusiveEdge(e);
        if (ie == null) {
            throw new IllegalArgumentException("no such edge in graph: " + e.toString());
        }
        ie.weight = weight;
    }

    @Override
    protected IntrusiveWeightedEdge getIntrusiveEdge(E e)
    {
        if (e instanceof IntrusiveWeightedEdge) {
            return (IntrusiveWeightedEdge) e;
        }
        return edgeMap.get(e);
    }
}
