/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -------------------
 * RingGraphGenerator.java
 * -------------------
 * (C) Copyright 2003-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 16-Sep-2003 : Initial revision (JVS);
 *
 */
package org.jgrapht.generate;

import java.util.*;

import org.jgrapht.*;


/**
 * Generates a ring graph of any size. A ring graph is a graph that contains a
 * single cycle that passes through all its vertices exactly once. For a
 * directed graph, the generated edges are oriented consistently around the
 * ring.
 *
 * @author John V. Sichi
 * @since Sep 16, 2003
 */
public class RingGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
{
    

    private int size;

    

    /**
     * Construct a new RingGraphGenerator.
     *
     * @param size number of vertices to be generated
     *
     * @throws IllegalArgumentException if the specified size is negative.
     */
    public RingGraphGenerator(int size)
    {
        if (size < 0) {
            throw new IllegalArgumentException("must be non-negative");
        }

        this.size = size;
    }

    

    /**
     * {@inheritDoc}
     */
    public void generateGraph(
        Graph<V, E> target,
        VertexFactory<V> vertexFactory,
        Map<String, V> resultMap)
    {
        if (size < 1) {
            return;
        }

        LinearGraphGenerator<V, E> linearGenerator =
            new LinearGraphGenerator<V, E>(size);
        Map<String, V> privateMap = new HashMap<String, V>();
        linearGenerator.generateGraph(target, vertexFactory, privateMap);

        V startVertex = privateMap.get(LinearGraphGenerator.START_VERTEX);
        V endVertex = privateMap.get(LinearGraphGenerator.END_VERTEX);
        target.addEdge(endVertex, startVertex);
    }
}

// End RingGraphGenerator.java
