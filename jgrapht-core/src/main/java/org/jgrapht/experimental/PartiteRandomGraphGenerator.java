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

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GraphGenerator;

import java.util.Arrays;
import java.util.Map;


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
    //~ Instance fields --------------------------------------------------------

    private final int [] numVertices;
    private final int numEdges;

    //~ Constructors -----------------------------------------------------------

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
        final int numVertices1,
        final int numVertices2,
        final int numEdges)
    {
        if (numVertices1 < 0 || numVertices2 < 0) {
            throw new IllegalArgumentException("must be non-negative");
        }

        if (numEdges < 0 || numEdges > numVertices1 * numVertices2) {
            throw new IllegalArgumentException("illegal number of edges");
        }

        numVertices = new int[]{ numVertices1, numVertices2 };
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
    public PartiteRandomGraphGenerator(final int [] numVertices, final int numEdges)
    {
        if (numEdges < 0) {
            throw new IllegalArgumentException("illegal number of edges");
        }

        for (int i = 0; i < numVertices.length; i++) {
            if (numVertices[i] < 0) {
                throw new IllegalArgumentException("must be non-negative");
            }

            for (int j = 0; j < i; j++) {
                if (numEdges > numVertices[i] * numVertices[j]) {
                    throw new IllegalArgumentException(
                        "illegal number of edges");
                }
            }
        }

        this.numVertices = numVertices;
        this.numEdges = numEdges;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * TODO hb 30-nov-05: document me
     *
     * @param target
     * @param vertexFactory
     * @param resultMap some array of vertices
     *
     * @see GraphGenerator#generateGraph
     */
    @Override
    public void generateGraph(
        final Graph<V, E> target,
        final VertexFactory<V> vertexFactory,
        final Map<String, Object[]> resultMap)
    {
        final Object [][] vertices = new Object[numVertices.length][];

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
