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
 * PartiteRandomGraphGenerator.java
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
 * PartiteRandomGraphGenerator generates a <a
 * href="http://mathworld.wolfram.com/RandomGraph.html">partite uniform random
 * graph</a> of any size. A partite uniform random graph contains edges chosen
 * independently uniformly at random from the set of possible edges between
 * partition classes.
 *
 * @author Michael Behrisch
 * @since Sep 13, 2004
 */
public class PartiteRandomGraphGenerator<V, E>
    implements GraphGenerator<V, E, Object[]>
{
    

    private final int [] numVertices;
    private final int numEdges;

    

    /**
     * Construct a new PartiteRandomGraphGenerator for a bipartite graph.
     *
     * @param numVertices1 number of vertices in the first partition
     * @param numVertices2 number of vertices in the second partition
     * @param numEdges number of edges to be generated
     *
     * @throws IllegalArgumentException
     */
    public PartiteRandomGraphGenerator(
        int numVertices1,
        int numVertices2,
        int numEdges)
    {
        if ((numVertices1 < 0) || (numVertices2 < 0)) {
            throw new IllegalArgumentException("must be non-negative");
        }

        if ((numEdges < 0) || (numEdges > (numVertices1 * numVertices2))) {
            throw new IllegalArgumentException("illegal number of edges");
        }

        final int [] numVertices = {
            numVertices1,
            numVertices2
        };
        this.numVertices = numVertices;
        this.numEdges = numEdges;
    }

    /**
     * Construct a new PartiteRandomGraphGenerator for a k-partite graph.
     *
     * @param numVertices number of vertices in the k partitions
     * @param numEdges number of edges to be generated between any two
     * partitions
     *
     * @throws IllegalArgumentException
     */
    public PartiteRandomGraphGenerator(int [] numVertices, int numEdges)
    {
        if (numEdges < 0) {
            throw new IllegalArgumentException("illegal number of edges");
        }

        for (int i = 0; i < numVertices.length; i++) {
            if (numVertices[i] < 0) {
                throw new IllegalArgumentException("must be non-negative");
            }

            for (int j = 0; j < i; j++) {
                if (numEdges > (numVertices[i] * numVertices[j])) {
                    throw new IllegalArgumentException(
                        "illegal number of edges");
                }
            }
        }

        this.numVertices = numVertices;
        this.numEdges = numEdges;
    }

    

    /**
     * TODO hb 30-nov-05: document me
     *
     * @param target
     * @param vertexFactory
     * @param resultMap some array of vertices
     *
     * @see GraphGenerator#generateGraph
     */
    public void generateGraph(
        Graph<V, E> target,
        VertexFactory<V> vertexFactory,
        Map<String, Object[]> resultMap)
    {
        Object [][] vertices = new Object[numVertices.length][];

        for (int i = 0; i < numVertices.length; i++) {
            vertices[i] =
                RandomGraphHelper.addVertices(
                    target,
                    vertexFactory,
                    numVertices[i]);

            if (resultMap != null) {
                resultMap.put(Integer.toString(i), vertices[i]);
            }

            for (int j = 0; j < i; j++) {
                RandomGraphHelper.addEdges(
                    target,
                    Arrays.asList(vertices[i]),
                    Arrays.asList(vertices[j]),
                    numEdges);
            }
        }
    }
}

// End PartiteRandomGraphGenerator.java
