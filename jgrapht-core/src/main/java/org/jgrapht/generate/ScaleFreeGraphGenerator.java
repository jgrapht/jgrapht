/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* -----------------
 * ScaleFreeGraphGenerator.java
 * -----------------
 * (C) Copyright 2008-2008, by Ilya Razenshteyn and Contributors.
 *
 * Original Author:  Ilya Razenshteyn
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 */
package org.jgrapht.generate;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Generates directed or undirected <a href =
 * "http://mathworld.wolfram.com/Scale-FreeNetwork.html">scale-free network</a>
 * of any size. Scale-free network is a connected graph, where degrees of
 * vertices are distributed in unusual way. There are many vertices with small
 * degrees and only small amount of vertices with big degrees.
 *
 * @author Ilya Razenshteyn
 */
public class ScaleFreeGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
{
    //~ Instance fields --------------------------------------------------------

    private final int size; // size of graphs, generated by this instance of generator
    private final long seed; // initial seed
    private final Random random; // the source of randomness

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructs a new <tt>ScaleFreeGraphGenerator</tt>.
     *
     * @param size number of vertices to be generated
     */
    public ScaleFreeGraphGenerator(
        final int size)
    {
        if (size < 0) {
            throw new IllegalArgumentException(
                "invalid size: " + size + " (must be non-negative)");
        }
        this.size = size;
        random = new Random();
        seed = random.nextLong();
    }

    /**
     * Constructs a new <tt>ScaleFreeGraphGenerator</tt> using fixed <tt>
     * seed</tt> for the random generator.
     *
     * @param size number of vertices to be generated
     * @param seed initial seed for the random generator
     */
    public ScaleFreeGraphGenerator(
        final int size,
        final long seed)
    {
        if (size < 0) {
            throw new IllegalArgumentException(
                "invalid size: " + size + " (must be non-negative)");
        }
        this.size = size;
        random = new Random();
        this.seed = seed;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Generates scale-free network with <tt>size</tt> passed to the
     * constructor. Each call of this method produces identical output (but if
     * <tt>target</tt> is an undirected graph, the directions of edges will be
     * lost).
     *
     * @param target receives the generated edges and vertices; if this is
     * non-empty on entry, the result will be a disconnected graph since
     * generated elements will not be connected to existing elements
     * @param vertexFactory called to produce new vertices
     * @param resultMap unused parameter
     */
    @Override
    public void generateGraph(
        final Graph<V, E> target,
        final VertexFactory<V> vertexFactory,
        final Map<String, V> resultMap)
    {
        random.setSeed(seed);
        final List<V> vertexList = new ArrayList<V>();
        final List<Integer> degrees = new ArrayList<Integer>();
        int degreeSum = 0;
        for (int i = 0; i < size; i++) {
            final V newVertex = vertexFactory.createVertex();
            target.addVertex(newVertex);
            int newDegree = 0;
            while (newDegree == 0 && i != 0) // we want our graph to be
                                                 // connected

            {
                for (int j = 0; j < vertexList.size(); j++) {
                    if (degreeSum == 0
                        || random.nextInt(degreeSum) < degrees.get(j))
                    {
                        degrees.set(j, degrees.get(j) + 1);
                        newDegree++;
                        degreeSum += 2;
                        if (random.nextInt(2) == 0) {
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
