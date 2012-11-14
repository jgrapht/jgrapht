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
 * HyperCubeGraphGenerator.java
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

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;

import java.util.LinkedList;
import java.util.Map;


/**
 * Generates a <a href="http://mathworld.wolfram.com/HypercubeGraph.html">hyper
 * cube graph</a> of any size. This is a graph that can be represented by bit
 * strings, so for an n-dimensial hypercube each vertex resembles an n-length
 * bit string. Then, two vertices are adjacent if and only if their bitstring
 * differ by exactly one element.
 *
 * @author Andrew Newell
 * @since Dec 21, 2008
 */
public class HyperCubeGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
{
    //~ Instance fields --------------------------------------------------------

    private final int dim;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HyperCubeGraphGenerator object.
     *
     * @param dim This is the dimension of the hypercube.
     */
    public HyperCubeGraphGenerator(final int dim)
    {
        this.dim = dim;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * This will generate the hypercube graph
     */
    @Override
    public void generateGraph(
        final Graph<V, E> target,
        final VertexFactory<V> vertexFactory,
        final Map<String, V> resultMap)
    {
        //Vertices are created, and they are included in the resultmap as their
        //bitstring representation
        final int order = (int) Math.pow(2, dim);
        final LinkedList<V> vertices = new LinkedList<V>();
        for (int i = 0; i < order; i++) {
            final V newVertex = vertexFactory.createVertex();
            target.addVertex(newVertex);
            vertices.add(newVertex);
            if (resultMap != null) {
                String s = Integer.toBinaryString(i);
                while (s.length() < dim) {
                    s = "0" + s;
                }
                resultMap.put(s, newVertex);
            }
        }

        //Two vertices will have an edge if their bitstrings differ by exactly
        //1 element
        for (int i = 0; i < order; i++) {
            for (int j = i + 1; j < order; j++) {
                for (int z = 0; z < dim; z++) {
                    if ((j ^ i) == 1 << z) {
                        target.addEdge(vertices.get(i), vertices.get(j));
                        break;
                    }
                }
            }
        }
    }
}

// End HyberCubeGraphGenerator.java
