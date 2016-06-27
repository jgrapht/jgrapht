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
 * DIMACSImporterTest.java
 * -------------------
 * (C) Copyright 2016-2016, by Joris Kinable and Contributors.
 *
 * Original Author:  Joris Kinable
 * Contributor(s):
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Dec-2008 : Initial revision (AN);
 *
 */
package org.jgrapht.ext;

import junit.framework.TestCase;
import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.*;

/**
 * .
 *
 * @author Joris Kinable
 */
public class DIMACSImporterTest extends TestCase {
    /**
     * Read and parse an actual instance
     */
    public void testReadDIMACSInstance(){
        InputStream fstream = getClass().getClassLoader().getResourceAsStream("myciel3.col");
        BufferedReader in = new BufferedReader(new InputStreamReader(fstream));
        try {
            DIMACSImporter<Integer, DefaultEdge> reader=new DIMACSImporter<>(in);
            VertexFactory<Integer> vf = new IntVertexFactory();
            Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
            reader.generateGraph(graph, vf, null);

            assertEquals(graph.vertexSet().size(), 11);
            assertEquals(graph.edgeSet().size(), 20);

            int[][] edges={{1, 2}, {1, 4}, {1, 7}, {1, 9}, {2, 3}, {2, 6}, {2, 8}, {3, 5}, {3, 7}, {3, 10}, {4, 5}, {4, 6}, {4, 10}, {5, 8}, {5, 9}, {6, 11}, {7, 11}, {8, 11}, {9, 11}, {10, 11}};
            for(int[] edge: edges)
                assertTrue(graph.containsEdge(edge[0],edge[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read and parse an weighted instance
     */
    public void testReadWeightedDIMACSInstance(){
        InputStream fstream = getClass().getClassLoader().getResourceAsStream("myciel3_weighted.col");
        BufferedReader in = new BufferedReader(new InputStreamReader(fstream));
        try {
            DIMACSImporter<Integer, DefaultWeightedEdge> reader=new DIMACSImporter<>(in);
            VertexFactory<Integer> vf = new IntVertexFactory();
            Graph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
            reader.generateGraph(graph, vf, null);

            assertEquals(graph.vertexSet().size(), 11);
            assertEquals(graph.edgeSet().size(), 20);

            int[][] edges={{1, 2, 1}, {1, 4, 2}, {1, 7, 3}, {1, 9, 4}, {2, 3, 5}, {2, 6, 6}, {2, 8, 7}, {3, 5, 8}, {3, 7, 9}, {3, 10, 10}, {4, 5, 11}, {4, 6, 12}, {4, 10, 13}, {5, 8, 14}, {5, 9, 15}, {6, 11, 16}, {7, 11, 17}, {8, 11, 18}, {9, 11, 19}, {10, 11, 20}};
            for(int[] edge: edges) {
                assertTrue(graph.containsEdge(edge[0], edge[1]));
                DefaultWeightedEdge e=graph.getEdge(edge[0], edge[1]);
                assertEquals((int)graph.getEdgeWeight(e), edge[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    private static final class IntVertexFactory
            implements VertexFactory<Integer>
    {
        int last = 1;

        @Override
        public Integer createVertex()
        {
            return last++;
        }
    }
}
