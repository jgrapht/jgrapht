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
 * CompleteBipartiteGraphGenerator.java
 * -------------------
 * (C) Copyright 2008-2008, by Andrew Newell and Contributors.
 *
 * Original Author:  Andrew Newell
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Dec-2008 : Initial revision (AN);
 *
 */
package org.jgrapht.generate;

import java.util.*;

import org.jgrapht.*;


/**
 * Generates a <a
 * href="http://mathworld.wolfram.com/CompleteBipartiteGraph.html">complete
 * bipartite graph</a> of any size. This is a graph with two partitions; two
 * vertices will contain an edge if and only if they belong to different
 * partitions.
 *
 * @author Andrew Newell
 * @since Dec 21, 2008
 */
public class CompleteBipartiteGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
{
    

    private int sizeA, sizeB;

    

    /**
     * Creates a new CompleteBipartiteGraphGenerator object.
     *
     * @param partitionOne This is the number of vertices in the first partition
     * @param partitionTwo This is the number of vertices in the second parition
     */
    public CompleteBipartiteGraphGenerator(int partitionOne, int partitionTwo)
    {
        if ((partitionOne < 0) || (partitionTwo < 0)) {
            throw new IllegalArgumentException("must be non-negative");
        }
        this.sizeA = partitionOne;
        this.sizeB = partitionTwo;
    }

    

    /**
     * Construct a complete bipartite graph
     */
    public void generateGraph(
        Graph<V, E> target,
        final VertexFactory<V> vertexFactory,
        Map<String, V> resultMap)
    {
        if ((sizeA < 1) && (sizeB < 1)) {
            return;
        }

        //Create vertices in each of the partitions
        Set<V> a = new HashSet<V>();
        Set<V> b = new HashSet<V>();
        for (int i = 0; i < sizeA; i++) {
            V newVertex = vertexFactory.createVertex();
            target.addVertex(newVertex);
            a.add(newVertex);
        }
        for (int i = 0; i < sizeB; i++) {
            V newVertex = vertexFactory.createVertex();
            target.addVertex(newVertex);
            b.add(newVertex);
        }

        //Add an edge for each pair of vertices in different partitions
        for (Iterator<V> iterA = a.iterator(); iterA.hasNext();) {
            V v = iterA.next();
            for (Iterator<V> iterB = b.iterator(); iterB.hasNext();) {
                target.addEdge(v, iterB.next());
            }
        }
    }
}

// End CompleteBipartiteGraphGenerator.java
