/*
 * (C) Copyright 2016-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.traverse;

import org.jgrapht.*;
import org.jgrapht.event.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TraversalListener for testing basic graph traversal invariants
 */
public class VertexTrackingTraversalListener<V, E>
    extends
    TraversalListenerAdapter<V, E>
{
    private Set<V> verticesTraversed = new HashSet<>();
    private Set<V> verticesFinished = new HashSet<>();
    private Graph<V, E> graph;

    VertexTrackingTraversalListener(Graph<V, E> graph)
    {
        this.graph = graph;
    }

    @Override
    public void vertexTraversed(VertexTraversalEvent<V> e)
    {
        assertTrue(graph.containsVertex(e.getVertex()));
        verticesTraversed.add(e.getVertex());
    }

    @Override
    public void vertexFinished(VertexTraversalEvent<V> e)
    {
        assertTrue(graph.containsVertex(e.getVertex()));
        verticesFinished.add(e.getVertex());
    }

    public Set<V> getVerticesTraversed()
    {
        return verticesTraversed;
    }

    public Set<V> getVerticesFinished()
    {
        return verticesFinished;
    }

    public void checkAllVerticesTraversed()
    {
        assertEquals(graph.vertexSet(), verticesTraversed);
    }

    public void checkAllVerticesFinished()
    {
        assertEquals(graph.vertexSet(), verticesFinished);
    }
}
