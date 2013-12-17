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
 * RandomGraphHelper.java
 * -------------------
 * (C) Copyright 2003-2008, by Michael Behrisch and Contributors.
 *
 * Original Author:  Michael Behrisch
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 13-Sep-2004 : Initial revision (MB);
 *
 */
// package org.jgrapht.generate;
package org.jgrapht.experimental;

import java.util.*;

import org.jgrapht.*;


/**
 * UniformRandomGraphGenerator generates a <a
 * href="http://mathworld.wolfram.com/RandomGraph.html">uniform random graph</a>
 * of any size. A uniform random graph contains edges chosen independently
 * uniformly at random from the set of all possible edges.
 *
 * @author Michael Behrisch
 * @since Sep 13, 2004
 */
public final class RandomGraphHelper
{
    

    private static final Random randSingleton = new Random();

    

    /**
     * .
     */
    private RandomGraphHelper()
    {
    }

    

    /**
     * @see org.jgrapht.generate.GraphGenerator#generateGraph
     */
    @SuppressWarnings("unchecked")
    public static void addEdges(
        Graph target,
        List sourceVertices,
        List destVertices,
        int numEdges)
    {
        int sourceSize = sourceVertices.size();
        int destSize = destVertices.size();

        for (int i = 0; i < numEdges; ++i) {
            while (
                target.addEdge(
                    sourceVertices.get(randSingleton.nextInt(
                            sourceSize)),
                    destVertices.get(randSingleton.nextInt(destSize)))
                == null)
            {
                ;
            }
        }
    }

    /**
     * .
     *
     * @param target
     * @param vertexFactory
     * @param numVertices
     *
     * @return vertices
     */
    @SuppressWarnings("unchecked")
    public static Object [] addVertices(
        Graph target,
        VertexFactory vertexFactory,
        int numVertices)
    {
        Object [] vertices = new Object[numVertices];

        for (int i = 0; i < numVertices; ++i) {
            vertices[i] = vertexFactory.createVertex();
            target.addVertex(vertices[i]);
        }

        return vertices;
    }
}

// End RandomGraphHelper.java
