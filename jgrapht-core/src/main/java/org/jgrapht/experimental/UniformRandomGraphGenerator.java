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
 * UniformRandomGraphGenerator.java
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
import org.jgrapht.generate.*;


/**
 * UniformRandomGraphGenerator generates a <a
 * href="http://mathworld.wolfram.com/RandomGraph.html">uniform random graph</a>
 * of any size. A uniform random graph contains edges chosen independently
 * uniformly at random from the set of all possible edges.
 *
 * @author Michael Behrisch
 * @since Sep 13, 2004
 */
public class UniformRandomGraphGenerator
    implements GraphGenerator
{
    

    private final int numEdges;
    private final int numVertices;

    

    /**
     * Construct a new UniformRandomGraphGenerator.
     *
     * @param numVertices number of vertices to be generated
     * @param numEdges number of edges to be generated
     *
     * @throws IllegalArgumentException
     */
    public UniformRandomGraphGenerator(int numVertices, int numEdges)
    {
        if (numVertices < 0) {
            throw new IllegalArgumentException("must be non-negative");
        }

        if ((numEdges < 0)
            || (numEdges > (numVertices * (numVertices - 1) / 2)))
        {
            throw new IllegalArgumentException("illegal number of edges");
        }

        this.numVertices = numVertices;
        this.numEdges = numEdges;
    }

    

    /**
     * @see GraphGenerator#generateGraph
     */
    public void generateGraph(
        Graph target,
        VertexFactory vertexFactory,
        Map resultMap)
    {
        Object [] vertices =
            RandomGraphHelper.addVertices(
                target,
                vertexFactory,
                numVertices);
        RandomGraphHelper.addEdges(
            target,
            Arrays.asList(vertices),
            Arrays.asList(vertices),
            numEdges);
    }
}

// End UniformRandomGraphGenerator.java
