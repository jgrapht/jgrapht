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

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GraphGenerator;

import java.util.Arrays;
import java.util.Map;


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
    //~ Instance fields --------------------------------------------------------

    private final int numEdges;
    private final int numVertices;

    //~ Constructors -----------------------------------------------------------

    /**
     * Construct a new UniformRandomGraphGenerator.
     *
     * @param numVertices number of vertices to be generated
     * @param numEdges number of edges to be generated
     *
     * @throws IllegalArgumentException
     */
    public UniformRandomGraphGenerator(final int numVertices, final int numEdges)
    {
        if (numVertices < 0) {
            throw new IllegalArgumentException("must be non-negative");
        }

        if (numEdges < 0
            || numEdges > numVertices * (numVertices - 1) / 2)
        {
            throw new IllegalArgumentException("illegal number of edges");
        }

        this.numVertices = numVertices;
        this.numEdges = numEdges;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @see GraphGenerator#generateGraph
     */
    @Override
    public void generateGraph(
        final Graph target,
        final VertexFactory vertexFactory,
        final Map resultMap)
    {
        final Object [] vertices =
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
