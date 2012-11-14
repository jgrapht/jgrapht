/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2009, by Barak Naveh and Contributors.
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
/* -------------------------
 * FloydWarshallShortestPathsTest.java
 * -------------------------
 * (C) Copyright 2009-2009, by Tom Larkworthy and Contributors
 *
 * Original Author:  Tom Larkworthy
 * Contributors:  Andrea Pagani
 *
 * $Id: FloydWarshallShortestPathsTest.java 715 2010-06-13 01:25:00Z perfecthash $
 *
 * Changes
 * -------
 * 29-Jun-2009 : Initial revision (TL);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;


/**
 * @author Tom Larkworthy
 * @version $Id: FloydWarshallShortestPathsTest.java 715 2010-06-13 01:25:00Z perfecthash $
 */
public class FloydWarshallShortestPathsTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    public void testCompareWithDijkstra()
    {
        final RandomGraphGenerator<Integer, DefaultWeightedEdge> gen =
            new RandomGraphGenerator<Integer, DefaultWeightedEdge>(
                10,
                15);
        final VertexFactory<Integer> f =
            new VertexFactory<Integer>() {
                int gid;

                @Override
                public Integer createVertex()
                {
                    return gid++;
                }
            };

        for (int i = 0; i < 10; i++) {
            final SimpleDirectedGraph<Integer, DefaultWeightedEdge> directed =
                new SimpleDirectedGraph<Integer, DefaultWeightedEdge>(
                    DefaultWeightedEdge.class);

            gen.generateGraph(directed, f, new HashMap<String, Integer>());

            // setup our shortest path measurer
            FloydWarshallShortestPaths<Integer, DefaultWeightedEdge> fw =
                new FloydWarshallShortestPaths<Integer, DefaultWeightedEdge>(
                    directed);

            for (final Integer v1 : directed.vertexSet()) {
                for (final Integer v2 : directed.vertexSet()) {
                    final double fwSp = fw.shortestDistance(v1, v2);
                    final double dijSp =
                        new DijkstraShortestPath<Integer, DefaultWeightedEdge>(
                            directed,
                            v1,
                            v2).getPathLength();
                    assertTrue(
                        Math.abs(dijSp - fwSp) < .01
                        || Double.isInfinite(fwSp)
                            && Double.isInfinite(dijSp));
                }
            }

            final SimpleGraph<Integer, DefaultWeightedEdge> undirected =
                new SimpleGraph<Integer, DefaultWeightedEdge>(
                    DefaultWeightedEdge.class);

            gen.generateGraph(undirected, f, new HashMap<String, Integer>());

            // setup our shortest path measurer
            fw = new FloydWarshallShortestPaths<Integer, DefaultWeightedEdge>(
                undirected);

            for (final Integer v1 : undirected.vertexSet()) {
                for (final Integer v2 : undirected.vertexSet()) {
                    final double fwSp = fw.shortestDistance(v1, v2);
                    final double dijSp =
                        new DijkstraShortestPath<Integer, DefaultWeightedEdge>(
                            undirected,
                            v1,
                            v2).getPathLength();
                    assertTrue(
                        Math.abs(dijSp - fwSp) < .01
                        || Double.isInfinite(fwSp)
                            && Double.isInfinite(dijSp));
                }
            }
        }
    }

    private static UndirectedGraph<String, DefaultEdge> createStringGraph()
    {
        final UndirectedGraph<String, DefaultEdge> g =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

        final String v1 = "v1";
        final String v2 = "v2";
        final String v3 = "v3";
        final String v4 = "v4";

        // add the vertices
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);

        // add edges to create a circuit
        g.addEdge(v1, v2);
        g.addEdge(v2, v3);
        g.addEdge(v3, v1);
        g.addEdge(v3, v4);

        return g;
    }

    public void testDiameter()
    {
        final UndirectedGraph<String, DefaultEdge> stringGraph = createStringGraph();
        final FloydWarshallShortestPaths<String, DefaultEdge> testFWPath =
            new FloydWarshallShortestPaths<String, DefaultEdge>(stringGraph);
        final double diameter = testFWPath.getDiameter();
        assertEquals(2.0, diameter);
    }

    public void testEmptyDiameter() {
        final DirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        final FloydWarshallShortestPaths<String, DefaultEdge> fw =
            new FloydWarshallShortestPaths<String, DefaultEdge>(graph);
        final double diameter = fw.getDiameter();
        assertEquals(0.0, diameter);
    }

    public void testEdgeLessDiameter() {
        final DirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        final String a = "a";
        final String b = "b";
        graph.addVertex(a);
        graph.addVertex(b);
        final FloydWarshallShortestPaths<String, DefaultEdge> fw =
            new FloydWarshallShortestPaths<String, DefaultEdge>(graph);
        final double diameter = fw.getDiameter();
        assertEquals(0.0, diameter);
    }
}

// End FloydWarshallShortestPathsTest.java
