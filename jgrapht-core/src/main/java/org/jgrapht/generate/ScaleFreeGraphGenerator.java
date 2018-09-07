/*
 * (C) Copyright 2008-2018, by Ilya Razenshteyn and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.*;

import java.util.*;

/**
 * Generates directed or undirected
 * <a href = "http://mathworld.wolfram.com/Scale-FreeNetwork.html">scale-free network</a> of any
 * size. Scale-free network is a connected graph, where degrees of vertices are distributed in
 * unusual way. There are many vertices with small degrees and only small amount of vertices with
 * big degrees.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Ilya Razenshteyn
 */
public class ScaleFreeGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    private final int size;
    private final Random rng;

    /**
     * Constructor
     *
     * @param size number of vertices to be generated
     */
    public ScaleFreeGraphGenerator(int size)
    {
        this(size, new Random());
    }

    /**
     * Constructor
     *
     * @param size number of vertices to be generated
     * @param seed initial seed for the random generator
     */
    public ScaleFreeGraphGenerator(int size, long seed)
    {
        this(size, new Random(seed));
    }

    /**
     * Constructor
     *
     * @param size number of vertices to be generated
     * @param rng the random number generator
     */
    public ScaleFreeGraphGenerator(int size, Random rng)
    {
        if (size < 0) {
            throw new IllegalArgumentException("invalid size: " + size + " (must be non-negative)");
        }
        this.size = size;
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");
    }

    /**
     * Generates scale-free network with <tt>size</tt> passed to the constructor.
     *
     * @param target receives the generated edges and vertices; if this is non-empty on entry, the
     *        result will be a disconnected graph since generated elements will not be connected to
     *        existing elements
     * @param resultMap unused parameter, can be null
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        List<V> vertexList = new ArrayList<>();
        List<Integer> degrees = new ArrayList<>();
        int degreeSum = 0;
        for (int i = 0; i < size; i++) {
            V newVertex = target.addVertex();
            int newDegree = 0;
            while ((newDegree == 0) && (i != 0)) // we want our graph to be connected
            {
                for (int j = 0; j < vertexList.size(); j++) {
                    if ((degreeSum == 0) || (rng.nextInt(degreeSum) < degrees.get(j))) {
                        degrees.set(j, degrees.get(j) + 1);
                        newDegree++;
                        degreeSum += 2;
                        if (rng.nextBoolean()) {
                            target.addEdge(vertexList.get(j), newVertex);
                        } else {
                            target.addEdge(newVertex, vertexList.get(j));
                        }
                    }
                }
            }
            vertexList.add(newVertex);
            degrees.add(newDegree);
        }
    }
}

// End ScaleFreeGraphGenerator.java
