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
 * GraphReaderTest.java
 * -------------------
 * (C) Copyright 2010-2010, by Michael Behrisch and Contributors.
 *
 * Original Author:  Michael Behrisch
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Dec-2008 : Initial revision (AN);
 *
 */
package org.jgrapht.experimental;

import junit.framework.TestCase;
import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.IOException;
import java.io.StringReader;


/**
 * .
 *
 * @author Michael Behrisch
 */
public class GraphReaderTest
    extends TestCase
{
    //~ Instance fields --------------------------------------------------------

    static final String _unweighted = "p 3\ne 1 2\ne 1 3\n";
    static final String _weighted = "p 3\ne 1 2 .5\ne 1 3 7\n";

    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testGraphReader()
    {
        final GraphReader<Integer, DefaultEdge> reader;
        try {
            reader =
                new GraphReader<Integer, DefaultEdge>(
                    new StringReader(_unweighted));
            final Graph<Integer, DefaultEdge> g =
                new SimpleGraph<Integer, DefaultEdge>(
                    DefaultEdge.class);
            final VertexFactory<Integer> vf = new IntVertexFactory();
            reader.generateGraph(g, vf, null);
            final Graph<Integer, DefaultEdge> g2 =
                new SimpleGraph<Integer, DefaultEdge>(
                    DefaultEdge.class);
            g2.addVertex(0);
            g2.addVertex(1);
            g2.addVertex(2);
            g2.addEdge(0, 1);
            g2.addEdge(0, 2);
            assertEquals(g2.toString(), g.toString());
        } catch (IOException e) {
        }
    }

    /**
     * .
     */
    public void testGraphReaderWeighted()
    {
        try {
            final GraphReader<Integer, DefaultWeightedEdge> reader =
                new GraphReader<Integer, DefaultWeightedEdge>(
                    new StringReader(_weighted),
                    1);
            final Graph<Integer, DefaultWeightedEdge> g =
                new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(
                    DefaultWeightedEdge.class);
            final VertexFactory<Integer> vf = new IntVertexFactory();
            reader.generateGraph(g, vf, null);
            final WeightedGraph<Integer, DefaultWeightedEdge> g2 =
                new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(
                    DefaultWeightedEdge.class);
            g2.addVertex(0);
            g2.addVertex(1);
            g2.addVertex(2);
            g2.setEdgeWeight(g2.addEdge(0, 1), .5);
            g2.setEdgeWeight(g2.addEdge(0, 2), 7);
            assertEquals(g2.toString(), g.toString());
        } catch (IOException e) {
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    private static final class IntVertexFactory
        implements VertexFactory<Integer>
    {
        int last = 0;

        @Override
        public Integer createVertex()
        {
            return last++;
        }
    }
}

// End GraphReaderTest.java
