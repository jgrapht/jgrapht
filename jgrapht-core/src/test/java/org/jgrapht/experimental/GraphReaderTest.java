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

import java.io.*;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * .
 *
 * @author Michael Behrisch
 */
public class GraphReaderTest
    extends TestCase
{
    //~ Instance fields --------------------------------------------------------

    String _unweighted = "p 3\ne 1 2\ne 1 3\n";
    String _weighted = "p 3\ne 1 2 .5\ne 1 3 7\n";

    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testGraphReader()
    {
        GraphReader<Integer, DefaultEdge> reader;
        try {
            reader =
                new GraphReader<Integer, DefaultEdge>(
                    new StringReader(_unweighted));
            Graph<Integer, DefaultEdge> g =
                new SimpleGraph<Integer, DefaultEdge>(
                    DefaultEdge.class);
            VertexFactory<Integer> vf = new IntVertexFactory();
            reader.generateGraph(g, vf, null);
            Graph<Integer, DefaultEdge> g2 =
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
            GraphReader<Integer, DefaultWeightedEdge> reader =
                new GraphReader<Integer, DefaultWeightedEdge>(
                    new StringReader(_weighted),
                    1);
            Graph<Integer, DefaultWeightedEdge> g =
                new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(
                    DefaultWeightedEdge.class);
            VertexFactory<Integer> vf = new IntVertexFactory();
            reader.generateGraph(g, vf, null);
            WeightedGraph<Integer, DefaultWeightedEdge> g2 =
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
