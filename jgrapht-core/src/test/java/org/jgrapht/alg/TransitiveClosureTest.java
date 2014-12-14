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
/* ------------------------------
 * TransitiveClosureTest.java
 * ------------------------------
 * (C) Copyright 2007, by Vinayak R. Borkar.
 *
 * Original Author:  Vinayak R. Borkar
 * Contributor(s):
 *
 * Changes
 * -------
 * 5-May-2007: Initial revision (VRB);
 *
 */
package org.jgrapht.alg;

import junit.framework.*;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;


/**
 */
public class TransitiveClosureTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    public void testLinearGraph()
    {
        SimpleDirectedGraph<Integer, DefaultEdge> graph =
            new SimpleDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);

        int N = 10;
        LinearGraphGenerator<Integer, DefaultEdge> gen =
            new LinearGraphGenerator<Integer, DefaultEdge>(N);

        VertexFactory<Integer> vf =
            new VertexFactory<Integer>() {
                private int m_index = 0;

                @Override
                public Integer createVertex()
                {
                    return Integer.valueOf(m_index++);
                }
            };
        gen.generateGraph(graph, vf, null);
        TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(graph);

        assertEquals(true, graph.edgeSet().size() == ((N * (N - 1)) / 2));
        for (int i = 0; i < N; ++i) {
            for (int j = i + 1; j < N; ++j) {
                assertEquals(
                    true,
                    graph.getEdge(Integer.valueOf(i), Integer.valueOf(j))
                    != null);
            }
        }
    }

    public void testRingGraph()
    {
        SimpleDirectedGraph<Integer, DefaultEdge> graph =
            new SimpleDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);

        int N = 10;
        RingGraphGenerator<Integer, DefaultEdge> gen =
            new RingGraphGenerator<Integer, DefaultEdge>(N);

        VertexFactory<Integer> vf =
            new VertexFactory<Integer>() {
                private int m_index = 0;

                @Override
                public Integer createVertex()
                {
                    return Integer.valueOf(m_index++);
                }
            };
        gen.generateGraph(graph, vf, null);
        TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(graph);

        assertEquals(true, graph.edgeSet().size() == (N * (N - 1)));
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) {
                assertEquals(
                    true,
                    (i == j)
                    || (graph.getEdge(Integer.valueOf(i), Integer.valueOf(j))
                        != null));
            }
        }
    }
}

// End TransitiveClosureTest.java
